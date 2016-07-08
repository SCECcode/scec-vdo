package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JProgressBar;


import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPlugin;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginGUI;
import org.scec.vtk.plugins.utils.components.ObjectInfoDialog;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;
import org.scec.vtk.tools.picking.PickEnabledActor;

import gov.usgs.earthquake.event.EventQuery;
import vtk.vtkActor;
import vtk.vtkDoubleArray;
import vtk.vtkGlyph3D;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;
import vtk.vtkUnsignedCharArray;
import vtk.vtkVertexGlyphFilter;


public class EQCatalog extends CatalogAccessor {

	private Logger log = Logger.getLogger(EQCatalog.class);

	/** Value to represent events as points (default). */
	public static final int GEOMETRY_POINT  = 0;
	/** Value to represent events as spheres. */
	public static final int GEOMETRY_SPHERE = 1;

	/** Value to apply color gradient to magnitude (default). */
	public static final int GRADIENT_APPLY_MAGNITUDE = 0;
	/** Value to apply color gradient to depth. */
	public static final int GRADIENT_APPLY_DEPTH     = 1;

	/** Value to not scale events by magnitude (default). */
	public static final int SCALING_NONE  = 0;
	/** Value to scale events by 0.05 magnitude bins. */
	public static final int SCALING_FIFTIETH = 1;
	/** Value to scale events by 0.1 magnitude bins. */
	public static final int SCALING_TENTH = 2;
	/** Value to scale events by 0.2 magnitude bins. */
	public static final int SCALING_FIFTH = 3;
	/** Value to scale events by 0.5 magnitude bins. */
	public static final int SCALING_HALF  = 4;
	/** Value to scale events by 1.0 magnitude bins. */
	public static final int SCALING_ONE   = 5;
	/** Value to scale events by 2.0 magnitude bins. */
	public static final int SCALING_TWO   = 6;
	/** Value to scale events by 4.0 magnitude bins. */
	public static final int SCALING_FOUR   = 7;
	/** Value to scale events by 10.0 magnitude bins. */
	public static final int SCALING_TEN   = 8;

	/** Value to ignore recent eq coloring */
	public static final int RECENT_EQ_COLOR_DISABLED = 0;
	/** Value to color eqs with recent earthquake coloring */
	public static final int RECENT_EQ_COLOR_ENABLED  = 1;

	/** Value to represent focal mechanisms as balls (default). */
	public static final int FOCAL_BALL = 0;
	/** Value to represent focal mechanisms as discs. */
	public static final int FOCAL_DISC = 1;
	/** Value to represent not using focal mechanisms (although available). */
	public static final int FOCAL_NONE = 2;

	/** Specifies that the display properties did not change. */
	public static final int CHANGE_NONE     = 0;
	/** Specifies that the display geometry changed. */
	public static final int CHANGE_GEOMETRY = 1 << 0;
	/** Specifies that the display scaling changed. */
	public static final int CHANGE_SCALING  = 1 << 1;
	/** Specifies that the display focal mechanism changed. */
	public static final int CHANGE_FOCAL    = 1 << 2;
	/** Specifies that the display color changed. */
	public static final int CHANGE_COLOR    = 1 << 3;
	/** Specifies that the display gradient-apply style changed. */
	public static final int CHANGE_GRADIENT = 1 << 4;
	/** Specifies that the focal mechanism type is changed. */
	public static final int CHANGE_FOCAL_DISPLAY = 1 << 5;
	/** Specifies that the focal mechanism disc compression color is changed. */
	public static final int CHANGE_FOCAL_DISC_COMP_COLOR = 1 << 6;
	/** Specifies that the focal mechanism disc extension color is changed. */
	public static final int CHANGE_FOCAL_DISC_EXT_COLOR = 1 << 7;
	/** Specifies that the recentEQcoloring setting changed. */
	public static final int CHANGE_RECENT_EQ_COLOR	= 1 << 8;
	/** Specifies that the focal none radio button has changed. */
	public static final int CHANGE_FOCAL_NONE = 1 << 9;
	public static final int CHANGE_SIZE_SLIDER = 1 << 10;
	/** Specifies that that EarthQuake Transparency has changed. */
	public static final int CHANGE_TRANSPARENCY_SLIDER = 1 << 11;

	public static final int CHANGE_DEPTH_DISPLAY = 1 << 12;

	private int pointSize = 1;

	// JDOM accessor objects
	/** XML <code>Element</code> for display attribute access. */
	protected Element displayAttributes;
	/** XML <code>Element</code> for query structure/history access. */
	protected Element queryStructure;



	// display attributes
	private int geometry;
	private int scaling;
	private int focalMech;
	private int applyGradient;
	private static int transparency;
	/** A variable whose integer value represents focal mechanism ball or disc, a sphere, or none of these.
        Value is initialized to FOCAL_NONE **/
	private int focalDisplay = FOCAL_NONE;
	private Color color1;
	private Color color2;
	private int recentEQcoloring;
	public int index;
	private boolean initialized = false;

	// j3d parts of an EQCatalog

	private Color focalDiscCompColor;
	private Color focalDiscExtColor;

	ArrayList<Earthquake> eqList= new ArrayList<>();
	private ArrayList<vtkActor> myActors = new ArrayList<vtkActor>();
	Component parent;

	private PluginActors pluginActors;

	private double[][] eventCoords;

	public int gradientDivisions;

	public Color[] gradientColors;

	private boolean catalogTypeIsComcat = false;

	private EventQuery comcatQuery;

	private String comcatFilePathString;

	private ComcatResourcesDialog crd;

	private String valuesBy;



	/**
	 * Reconstructs an <code>EQCatalog</code> (display catalog) with a given parent
	 * from a given <i>ScecVideo</i> library source (attribute file).
	 * 
	 * @param parent <code>Component</code> (plugin GUI)
	 * @param sourcefile object attribute file
	 */
	public EQCatalog(Component parent, File sourcefile, PluginActors pluginActors) {
		super(parent);
		this.pluginActors = pluginActors;

		this.parent = parent;

		if (readAttributeFile(sourcefile)) {
			this.initialized = true;
		}
		this.valuesBy ="Magnitude";
		//this.setMasterCatBranchGroup();
	}

	public EQCatalog(EarthquakeCatalogPluginGUI parent) {
		super(parent);

		this.pluginActors = parent.getPluginActors();
		this.parent = parent;
		//initializing default values
		this.geometry      = 1;//this.displayAttributes.getChild("geometry").getAttribute("value").getIntValue();
		this.scaling       = 4;//this.displayAttributes.getChild("scaling").getAttribute("value").getIntValue();
		this.color1        = Color.BLUE;//readColorElement(this.displayAttributes.getChild("colors").getChild("color_1"));
		this.color2        = Color.RED;//readColorElement(this.displayAttributes.getChild("colors").getChild("color_2"));
		transparency=100;

		setCrd(new ComcatResourcesDialog());
		this.valuesBy ="Magnitude";
		//this.setMasterCatBranchGroup();
	}

	/**
	 * Constructs a new catalog document/file from a given parent <i>ScecVideo</i> catalog.
	 * 
	 * @param parent catalog 
	 * @return whether catalog was properly initialized
	 */
	protected boolean newDocument(CatalogAccessor parent) {
		if (this.newDocument()) {

			// set object info attributes
			setObjectClass(((EQCatalog)this).getClass().getName());
			setSourceFile(parent.getAttributeFileLibPath());
			String catName = generateNewCatName();
			setAttributeFile(
					File.separator + EarthquakeCatalogPlugin.dataStoreDir +
					File.separator + "display" +
					File.separator + catName + ".cat");
			setDataFile(
					File.separator + EarthquakeCatalogPlugin.dataStoreDir +
					File.separator + "display" +
					File.separator + "data" +
					File.separator + catName + ".dat");
			setCitation(parent.getCitation());
			setReference(parent.getReference());
			setNotes(parent.getNotes());
			setDisplayName(parent.toString());

			// let user edit source info and abort on cancel
			if (!showInfoDialog()) {
				return false;
			}
			// add default display tags
			URL displayDoc = CatalogAccessor.class.getResource("resources/xml/display_template.xml");
			try {
				Document temp = parser.build(displayDoc);
				this.displayAttributes = (Element)temp.getRootElement().getChild("display").detach();
				this.objectAttributes.addContent(this.displayAttributes);
				if (!readDisplayAttributes()) return false;
			}
			catch (Exception e) {
				log.debug("problem parsing XML");
				return false;
			}
			return true;
		}
		return false;
	}

	public boolean showInfoDialog(){
		ObjectInfoDialog oid = ((EarthquakeCatalogPluginGUI)this.owner).getSourceInfoDialog();
		oid.showInfo(this, "Edit Catalog Information");
		if (oid.windowWasCancelled())
		{
			return false;
		}
		return true;
	}

	/**
	 * Reads/loads catalog attribute information from a given <i>ScecVideo</i>
	 * library file.
	 * 
	 * @see org.scec.geo3d.plugins.utils.DataAccessor#readAttributeFile(java.io.File)
	 */
	public boolean readAttributeFile(File file) {
		if (!(super.readAttributeFile(file))) return false;

		// set display Element
		if (this.objectAttributes.getChild("display") != null) {
			this.displayAttributes = this.objectAttributes.getChild("display");
			if (!readDisplayAttributes()) {
				return false;
			}
		}

		// set query Element
		if (this.objectAttributes.getChild("query_structure") != null) {
			this.queryStructure = this.objectAttributes.getChild("query_structure");
			// TODO query storage
			// handle error
		}
		return true;        
	}

	//****************************************
	//     PRIVATE METHODS
	//****************************************

	public void makePoints() {
		// initialize display locations (if not already initialized)
		if (this.eventCoords == null) {
			this.eventCoords = new double[getNumEvents()][3];
			for (int i = 0; i<getNumEvents(); i++) {
				this.eventCoords[i] = Transform.transformLatLonHeight(
						eq_latitude[i],
						eq_longitude[i],
						-eq_depth[i]);  
				// TODO convert depth to negative on import
			}
		}
	}

	private void load() {
		makePoints();
		if(parent instanceof EarthquakeCatalogPluginGUI)
		{
			addEqList();
		}
	}

	public ArrayList<Earthquake> getSelectedEqList(){
		return eqList;
	}

	public void addEqList()
	{
		if(catalogTypeIsComcat != true)
		{
			catalogTypeIsComcat = false;
			eqList = EarthquakeCatalogPluginGUI.getEarthquakes();
			geometry=0;
			addPointsToBranchGroup(false,eqList);
			geometry=1;
			addPointsToBranchGroup(true,eqList);
			EarthquakeCatalogPluginGUI.eqCatalogs.add(this);
		}
	}
	public void addComcatEqList()
	{

		if(((EarthquakeCatalogPluginGUI) parent).getComcatResourceDialog()!=null)
			eqList = ((EarthquakeCatalogPluginGUI) parent).getComcatResourceDialog().getAllEarthquakes();
		else
		{
			//loaded form json file
			eqList = getCrd().getAllEarthquakes();
		}
		catalogTypeIsComcat = true;
		geometry=0;
		addPointsToBranchGroup(false,eqList);
		geometry=1;
		addPointsToBranchGroup(true,eqList);
		EarthquakeCatalogPluginGUI.eqCatalogs.add(this);
	}

	public void addPointsToBranchGroup(boolean sphere,ArrayList<Earthquake> eqList)
	{
		initGradientAppearance();
		vtkVertexGlyphFilter vertexGlyphFilter =new vtkVertexGlyphFilter();
		vtkPoints pts = new vtkPoints();
		vtkPolyDataMapper mapperEQCatalog = new vtkPolyDataMapper();
		vtkDoubleArray radi = new vtkDoubleArray();
		radi.SetName("radi");
		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
		colors.SetName("colors");
		colors.SetNumberOfComponents(4);
		colors.SetNumberOfTuples(eqList.size());

		//double stepSize = Math.ceil((getMaxMagnitude()-getMinMagnitude())/gradientDivisions);
		double[] xForm = new double[3];
		for (int i = 0; i < eqList.size(); i++) 
		{

			Earthquake eq = eqList.get(i);
			xForm = Transform.transformLatLonHeight(eq.getEq_latitude(), eq.getEq_longitude(), -eq.getEq_depth());
			pts.InsertNextPoint(xForm);
			radi.InsertNextTuple1(eq.getEq_magnitude());
			// Color based on magnitude
			int ind =  (int)Math.floor(eq.getEq_magnitude()) - (int)getMinMagnitude();
			if(ind<0)
				ind=0;


			float[] grad = new float[3];
			gradientColors[ind].getRGBColorComponents(grad);

			colors.InsertTuple4(i,gradientColors[ind].getRed(),gradientColors[ind].getGreen(),gradientColors[ind].getBlue(),255);
		}

		vtkPolyData inputData = new vtkPolyData();
		inputData.SetPoints(pts);
		inputData.GetPointData().AddArray(radi);
		inputData.GetPointData().AddArray(colors);
		inputData.GetPointData().SetActiveScalars("radi");

		//spheres
		if(sphere){
			// Use sphere as glyph source.
			vtkSphereSource balls = new vtkSphereSource();
			balls.SetRadius(1.0);//.01);
			balls.SetPhiResolution(10);
			balls.SetThetaResolution(10);

			vtkGlyph3D glyphPoints = new vtkGlyph3D();
			glyphPoints.SetInputData(inputData);
			glyphPoints.SetSourceConnection(balls.GetOutputPort());

			mapperEQCatalog.SetInputConnection(glyphPoints.GetOutputPort());

		}
		else
		{
			//points

			vertexGlyphFilter.AddInputData(inputData);

			vertexGlyphFilter.Update();

			mapperEQCatalog.SetInputConnection(vertexGlyphFilter.GetOutputPort());
		}
		//mapperEQCatalog.SetLookupTable(lut);
		mapperEQCatalog.ScalarVisibilityOn();
		mapperEQCatalog.SetScalarModeToUsePointFieldData();
		//mapperEQCatalog.colorve
		mapperEQCatalog.SelectColorArray("colors");
		
		PickEnabledActor<EQCatalog> actorEQCatalog = new PickEnabledActor<EQCatalog>(((EarthquakeCatalogPluginGUI) parent).getPickHandler(), this);
		actorEQCatalog.SetMapper(mapperEQCatalog);
		actorEQCatalog.SetVisibility(0);
		myActors.add(actorEQCatalog);
		pluginActors.addActor(actorEQCatalog);
		Info.getMainGUI().updateRenderWindow();
	}

	public List<vtkActor> getActors() {
		return myActors;
	}




	// clears data arrays and j3d component references
	private void unload() {
		pluginActors.removeActor(this.getActors().get(0)); //remove points and then remove spheres
		pluginActors.removeActor(this.getActors().get(1));
		Info.getMainGUI().updateRenderWindow();
	}



	// sets the appearance array for gradient representations
	public void initGradientAppearance() {
		int magDivs ;
		// calc mag divisions
		if(this.valuesBy=="Magnitude")
		{ 
			magDivs= (int)Math.ceil(getMaxMagnitude()) - (int)Math.floor(getMinMagnitude());
		}else
		{
			magDivs= (int)Math.ceil(getMaxDepth()) - (int)Math.floor(getMinDepth());
		}
		// find number of increments/divisions:
		//    -- depth gradient is always 3km intervals from 0-18km
		//    -- magnitude gradient spans the min and max mags for the catalog
		this.gradientDivisions = (getApplyGradientTo() == GRADIENT_APPLY_DEPTH) ? 6 : magDivs;

		// initialize color array
		initGradientColors(this.gradientDivisions);

	}

	// builds an array of colors to be referenced by sphere appearances and point arrays
	private void initGradientColors(int divisions) {

		// init color incrementing values
		float[] colorStart = getColor1().getColorComponents(null);
		float[] colorEnd = getColor2().getColorComponents(null);
		float[] colorIntervals = new float[3];
		for (int j=0; j<3; j++) {
			colorIntervals[j] = (colorEnd[j]-colorStart[j])/(divisions-1);
		}

		// init color array
		this.gradientColors = new Color[divisions];
		for (int i = 0; i<divisions; i++) {
			this.gradientColors[i] = new Color(
					colorStart[0] + (colorIntervals[0]*i),
					colorStart[1] + (colorIntervals[1]*i),
					colorStart[2] + (colorIntervals[2]*i));
		}     
	}
	// generates a random catalog name
	private String generateNewCatName() {
		String dataLib = Prefs.getLibLoc() + 
				File.separator + EarthquakeCatalogPlugin.dataStoreDir +
				File.separator + "display" + 
				File.separator;
		String id = "000000";
		NumberFormat fmt = NumberFormat.getIntegerInstance();
		fmt.setMaximumIntegerDigits(6);
		fmt.setMinimumIntegerDigits(6);
		fmt.setGroupingUsed(false);
		File f = new File(dataLib + "filteredCat_" + id + ".cat");
		while (f.exists()) {
			id = fmt.format(1000000*Math.random());
			f = new File(dataLib + "filteredCat_" + id + ".cat");
		}
		return "filteredCat_" + id;
	}

	// reads display attributes
	private boolean readDisplayAttributes() {
		try {
			this.geometry      = this.displayAttributes.getChild("geometry").getAttribute("value").getIntValue();
			this.scaling       = 4;//this.displayAttributes.getChild("scaling").getAttribute("value").getIntValue();
			try { this.recentEQcoloring = this.displayAttributes.getChild("recentEQcoloring").getAttribute("value").getIntValue();} catch (Exception e) {/*System.out.println("Error reading RecentEQColor property");*/}
			this.focalMech     = this.displayAttributes.getChild("focal_mech").getAttribute("value").getIntValue();
			this.applyGradient = this.displayAttributes.getChild("colors").getAttribute("apply_gradient").getIntValue();
			this.color1        = readColorElement(this.displayAttributes.getChild("colors").getChild("color_1"));
			this.color2        = readColorElement(this.displayAttributes.getChild("colors").getChild("color_2"));
			if(this.displayAttributes.getChild("transparency")!=null)
			{
				transparency=this.displayAttributes.getChild("transparency").getAttribute("value").getIntValue();
			}
			else
			{
				transparency=100;
			}
		}
		catch (Exception e) {
			log.debug("problem reading XML");
			e.printStackTrace();
			return false;
		}
		return true;        
	}

	/**
	 * Kills current catalog display and redraws, regardless of <code>isDisplayed</code>
	 * status.
	 */
	public void updateDisplay() {
		// kill and reinitialize branch group
		load();
	}

	/**
	 * Returns whether this catalog was successfully initialized (read/built
	 * attribute file).
	 * 
	 * @return whether catalog was initialized
	 */
	public boolean isInitialized() {
		return this.initialized;
	}

	/**
	 * Sets whether this catalog should be displayed or not.
	 * 
	 * @see org.scec.geo3d.plugins.utils.DataAccessor#setDisplayed(boolean)
	 */
	public void setDisplayed(boolean show) {
		super.setDisplayed(show);
	}

	/**
	 * Sets whether this catalog's data and display representation should be loaded
	 * or released.
	 * 
	 * @see org.scec.geo3d.plugins.utils.DataAccessor#setInMemory(boolean)
	 */
	public void setInMemory(boolean load) {
		if (load) {
			// importers may have already loaded data arrays; bypass by checking one
			if (this.eq_id == null) {
				readDataFile();
			}
			load();
		} else {
			if (isDisplayed()) {
				unload();
				setDisplayed(false);
			}

		}
		super.setInMemory(load);
	}

	public JProgressBar getProgressBar(){
		return EarthquakeCatalogPluginGUI.progbar;
	}

	public JLabel getProgessLabel(){
		return EarthquakeCatalogPluginGUI.progLabel;
	}

	/**
	 * Returns the value that represents how a color gradient (if it exists) is applied to
	 * this catalog. Possible values are:
	 * <pre>
	 * GRADIENT_APPLY_DEPTH
	 * GRADIENT_APPLY_MAGNITUDE
	 * </pre>  
	 *
	 * @return the earthquake data type to apply a gradient to
	 */
	public int getApplyGradientTo() {
		return this.applyGradient;
	}

	/**
	 * Sets the value that represents how a color gradient (if it exists) is applied to this
	 * catalog. Possible values are:
	 * <pre>
	 * GRADIENT_APPLY_DEPTH
	 * GRADIENT_APPLY_MAGNITUDE
	 * </pre>
	 * 
	 * @param value the earthquake data type (value) to apply color gradients to
	 */
	public void setApplyGradientTo(int value) {
		this.applyGradient = value;
		this.displayAttributes.getChild("colors").getAttribute("apply_gradient").setValue(String.valueOf(value));
	}

	/**
	 * Returns the first color used for gradients.
	 *
	 * @return the first gradient color
	 */
	public Color getColor1() {
		return this.color1;
	}

	/**
	 * Sets the first color used for gradients. Set the two catalog colors equal for
	 * uniform color appearance.
	 *
	 * @param color the color to set
	 */
	public void setGradColor1(Color color) {
		this.color1 = color;
	}
	public void setGradColor2(Color color) {
		this.color2 = color;
	}
	public void setColor1(Color color) {
		this.color1 = color;
		writeColorElement((this.displayAttributes.getChild("colors").getChild("color_1")), color);
	}

	/**
	 * Returns the second color used for gradients.
	 *
	 * @return the second gradient color
	 */
	public Color getColor2() {
		return this.color2;
	}

	/**
	 * Sets the second color used for gradients. Set the two catalog colors equal for
	 * uniform color appearance.
	 *
	 * @param color the color to set
	 */
	public void setColor2(Color color) {
		this.color2 = color;
		writeColorElement((this.displayAttributes.getChild("colors").getChild("color_2")), color);
	}

	/**
	 * Applies current catalog colors to display representation.
	 */
	public void setColor() {

	}


	public int getRecentEQColoring() {
		return recentEQcoloring;
	}

	public void setRecentEQColoring(int recentEQcoloring) {
		this.recentEQcoloring = recentEQcoloring;
		try {this.displayAttributes.getChild("recentEQcoloring").getAttribute("value").setValue(String.valueOf(recentEQcoloring));} catch (Exception e) {System.out.println("Error writing RecentEQColor property");}
	}

	/**
	 * Returns the value that represents a focal mechanism color pattern.
	 *
	 * @return the focal mechanism pattern representation value
	 * @see FocalMechIcons
	 */
	public int getFocalMech() {
		return this.focalMech;
	}

	/**
	 * Sets the value that represents a focal mechanism color pattern.
	 *
	 * @param pattern the focal mechanism pattern to set
	 * @see FocalMechIcons
	 */
	public void setFocalMech(int pattern) {
		this.focalMech = pattern;
		this.displayAttributes.getChild("focal_mech").getAttribute("value").setValue(String.valueOf(pattern));
	}

	public void setFocalDisplay(int display) {
		this.focalDisplay = display;
	}

	/**
	 * Returns the value representing the geometry of earthquakes representations.
	 * Possible values are:
	 * <pre>
	 * GEOMETRY_POINT
	 * GEOMETRY_SPHERE
	 * </pre>
	 *
	 * @return the geometry value
	 */
	public int getGeometry() {
		return this.geometry;
	}

	/**
	 * Sets the value representing the geometry of earthquakes representations.
	 * Possible values are:
	 * <pre>
	 * GEOMETRY_POINT
	 * GEOMETRY_SPHERE
	 * </pre>
	 * 
	 * @param value the earthquake geometry value to set
	 */
	public void setGeometry(int value) {
		this.geometry = value;
		// this.displayAttributes.getChild("geometry").getAttribute("value").setValue(String.valueOf(value));
	}

	/**
	 * Returns the integer value corresponding to a magnitude-bin scale value.
	 * This value represents the magnitude interval into which earthquakes are
	 * grouped (e.g 0.1, 0.2, 0.5, 1.0, or 2.0). Possible values are:
	 * <pre>
	 * SCALING_NONE
	 * SCALING_TENTH
	 * SCALING_FIFTH
	 * SCALING_HALF
	 * SCALING_ONE
	 * SCALING_TWO
	 * </pre>
	 *
	 * @return the scale interval value
	 */
	public int getScaling() {
		return this.scaling;
	}

	/**
	 * Sets the magnitude-bin scale value. This value represents the magnitude interval into
	 * which earthquakes are grouped (e.g 0.1, 0.2, 0.5, 1.0, or 2.0). Possible values are:
	 * <pre>
	 * SCALING_NONE
	 * SCALING_TENTH
	 * SCALING_FIFTH
	 * SCALING_HALF
	 * SCALING_ONE
	 * SCALING_TWO
	 * </pre>
	 * 
	 * @param scale the scale interval value to set.
	 */
	public boolean setScaling(int scale) {
		if (scale >= 0)
		{
			this.scaling = scale;
			//this.displayAttributes.getChild("scaling").getAttribute("value").setValue(String.valueOf(scale));
			return true;
		}
		else
			return false;
	}

	public int getPointSize() {
		return pointSize;
	}

	public void setPointSize(int pointSize) {
		if (pointSize <= 0)
			throw new IllegalArgumentException("Point size must be > 0");
		//    	System.out.println("New size: " + pointSize);
		this.pointSize = pointSize;
	}

	/**
	 * Different integer values represent sphere, focal mechanism ball, focal mechanism disc, or none of these	
	 * 
	 */
	public int getFocalDisplay() {
		return this.focalDisplay;
	}

	/**
	 * Sets the focal mechanism disc compression color
	 * 
	 * @param compColor the color used to represent compression
	 */
	public void setFocalDiscCompColor(Color compColor) {
		this.focalDiscCompColor = compColor;

	}

	/**
	 * Sets the focal mechanism disc extension color
	 * 
	 * @param extColor the color used to represent extension
	 */
	public void setFocalDiscExtColor(Color extColor) {
		this.focalDiscExtColor = extColor;

	}

	/**
	 * Returns the focal mechanism disc compression color
	 * 
	 */
	public Color getDiscCompColor() {
		if (focalDiscCompColor != null) {
			return this.focalDiscCompColor;
		}
		else {
			return Color.RED;
		}
	}

	/**
	 * Returns the focal mechanism disc extension color
	 * 
	 */
	public Color getDiscExtColor() {
		if (focalDiscExtColor != null) {
			return this.focalDiscExtColor;
		}
		else {
			return Color.YELLOW;
		}
	}

	public static int getTransparency()
	{
		return transparency;
	}

	public void setTransparency(int trans)
	{
		transparency=trans;
	}

	public void setDisplayName(String name)
	{
		super.setDisplayName(name);
		writeAttributeFile();
	}

	public void setComcatQuery(EventQuery query) {
		// TODO Auto-generated method stub
		comcatQuery = query;
	}
	public EventQuery getComcatQuery() {
		// TODO Auto-generated method stub
		return comcatQuery;
	}
	public boolean getCatalogTypeIsComcat()
	{
		//true then comcat else from cat file
		return catalogTypeIsComcat;
	}

	public boolean isSphere() {
		// TODO Auto-generated method stub
		return true;
	}

	public String getComcatFilePathString() {
		return comcatFilePathString;
	}

	public void setComcatFilePathString(String comcatFilePathString) {
		this.comcatFilePathString = comcatFilePathString;
	}

	public ComcatResourcesDialog getCrd() {
		return crd;
	}

	public void setCrd(ComcatResourcesDialog crd) {
		this.crd = crd;
	}

	public void setValuesBy(String string) {
		// TODO Auto-generated method stub
		this.valuesBy=string;
	}
	public String getValuesBy() {
		// TODO Auto-generated method stub
		return this.valuesBy;
	}
}



