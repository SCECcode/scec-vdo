package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;


import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPlugin;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginGUI;
import org.scec.vtk.plugins.utils.DataImport;
import org.scec.vtk.plugins.utils.components.ObjectInfoDialog;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;

import vtk.vtkActor;
import vtk.vtkAppendFilter;
import vtk.vtkAppendPolyData;
import vtk.vtkCellArray;
import vtk.vtkDataSetMapper;
import vtk.vtkDoubleArray;
import vtk.vtkGlyph3D;
import vtk.vtkIntArray;
import vtk.vtkLookupTable;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkScalarBarActor;
import vtk.vtkSphereSource;
import vtk.vtkUnsignedCharArray;
import vtk.vtkUnstructuredGrid;
import vtk.vtkVertexGlyphFilter;


public class EQCatalog extends CatalogAccessor {
    
    private Logger log = Logger.getLogger(EQCatalog.class);
    
    /** Value to represent events as points (default). */
    public static final int GEOMETRY_POINT  = 0;
    /** Value to represent events as spheres. */
    public static final int GEOMETRY_SPHERE = 1;
    /** Value to represent events as cows. */
    public static final int GEOMETRY_COW = 2;
    
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

	private long currentTimeSec;
	private long firstLimit;
	private long secondLimit;
	private long thirdLimit;
	ArrayList<Earthquake> eqList= new ArrayList<>();
	public ArrayList masterEarthquakeCatalogBranchGroup = new ArrayList<vtkActor>();
	Component parent;

	private double[][] eventCoords;

	public int gradientDivisions;

	public Color[] gradientColors;
	
	//private ArrayList<Earthquake> earthquakes; 
	
	//	Added so that we can get the earthquakes that are currently 
	//  being displayed for disabling or enabling pickability
	//ArrayList<Earthquake> displayedEQs = new ArrayList<Earthquake>(); 
	
    /**
     * Constructs a new <code>EQCatalog</code> (display catalog) with a given parent
     * and derived from a given source.
     * 
     * @param parent <code>Component</code> (plugin GUI)
     * @param source catalog
     */
    
    public EQCatalog(Component parent, CatalogAccessor source) {
        super(parent);
        
        this.parent=parent;
        
        if (newDocument(source)) {
            this.initialized = true;
        }
        this.setMasterCatBranchGroup();
       
    }
    
    private void setupSizedEventPoint()
    {
    	/*sizedEventPoints.clear();
    	 for(int i=0;i<10;i++)
         {
         	sizedEventPoints.add(new ArrayList<PointColor>());
         }*/
    }
    
    /**
     * Reconstructs an <code>EQCatalog</code> (display catalog) with a given parent
     * from a given <i>ScecVideo</i> library source (attribute file).
     * 
     * @param parent <code>Component</code> (plugin GUI)
     * @param sourcefile object attribute file
     */
    public EQCatalog(Component parent, File sourcefile) {
        super(parent);
        
        this.parent = parent;
        
        if (readAttributeFile(sourcefile)) {
            this.initialized = true;
        }
        //this.setMasterCatBranchGroup();
    }
  
    public EQCatalog(EarthquakeCatalogPluginGUI parent) {
        super(parent);
        
        this.parent = parent;
        //initializing default values
        this.geometry      = 0;//this.displayAttributes.getChild("geometry").getAttribute("value").getIntValue();
        this.scaling       = 1;//this.displayAttributes.getChild("scaling").getAttribute("value").getIntValue();
        //try { this.recentEQcoloring = this.displayAttributes.getChild("recentEQcoloring").getAttribute("value").getIntValue();} catch (Exception e) {/*System.out.println("Error reading RecentEQColor property");*/}
        //this.focalMech     = this.displayAttributes.getChild("focal_mech").getAttribute("value").getIntValue();
        //this.applyGradient = this.displayAttributes.getChild("colors").getAttribute("apply_gradient").getIntValue();
        this.color1        = Color.BLUE;//readColorElement(this.displayAttributes.getChild("colors").getChild("color_1"));
        this.color2        = Color.RED;//readColorElement(this.displayAttributes.getChild("colors").getChild("color_2"));
        this.transparency=100;
        this.displayName="_New Comcat Catalog-"+ parent.getCatalogTable().getRowCount();
        
        //this.setMasterCatBranchGroup();
    }
    
    private void setMasterCatBranchGroup() 
    {
        // When EQCatalog plugin loads, GlobeView is already live so one can only add a BranchGroup
        // to the pluginBranchGroup. Initialize master branch group to which picking behavior is
        // added so that it affects all faultBranchGroup children
      /*  masterCatBranchGroup = new BranchGroup();
        masterCatBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        masterCatBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        masterCatBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        masterCatBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        catBranchGroup = new BranchGroup();
        catBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        catBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        catBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        catBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		if(Geo3dInfo.getMainWindow() != null)
		{
			Geo3dInfo.getMainWindow().getPluginBranchGroup().addChild(masterCatBranchGroup);
		}*/
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
                EarthquakeCatalogPluginGUI.status.setText(DataImport.ERROR_OBJECT_LOAD);
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
    
    	// detach children (remove any previously attached earthquakes)
        /*if (this.catBranchGroup != null) {
            this.masterCatBranchGroup.removeChild(catBranchGroup);
        }
        
        // (re)initialize branch group
        this.catBranchGroup = new BranchGroup();
        this.catBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        */
        makePoints();
        
        
        
        // POINTS:
        // TODO Create Earthquake Objects for Points
        // For now objects have been created for spheres
        
//        System.out.println("LOAD...geom? " + getGeometry());
        
        if(parent instanceof EarthquakeCatalogPluginGUI)
        {

        		addEqList();
       
        }
        	// COWS:
        	/*else if (getGeometry() == GEOMETRY_COW){
        	
        		// init cow transform groups if not already
        		int numevents = getNumEvents();
        		if (eventCows == null) {
        			eventCows = new TransformGroup[getNumEvents()];
        			if ( numevents > 30 ){
        				JOptionPane.showMessageDialog(Geo3dInfo.getDesktop(),
							"<html>The Unit Cow shape is meant to be displayed for small catalogs.<br>" +
							"Only the first 30 earthquakes in your catalog will display.</html>");
        				numevents = 30;
        			}
        			for (int i = 0; i< numevents; i++) {
        				eventCows[i] = new TransformGroup();
        				eventCows[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        			}
        		}
            
        		// set position (and perform some other necessary transforms for our friendly cow)
        		for (int i = 0; i < numevents; i++) {
        			this.eventTrans.rotX(45); //make the cows look like they're standing - feet tangent to earth
        			//you want to be able to rotate so the cow is touching the ground, all four feet (as close as you can)
        			this.eventTrans.setScale(6*getEventDisplaySize(eq_magnitude[i])); //make the cows big 'cuz they start unit
        			this.eventTrans.setTranslation(new Vector3f(this.eventCoords[i])); 
        			eventCows[i].setTransform(this.eventTrans);
        			// TODO: figure out why it crashes here after you change the display settings once
        		}
            
        		// add cows to transforms
        		for (int i = 0; i < numevents; i++) {

        			// init cow w/o Appearance
        			Cow3D event = new Cow3D();
                
        			// remove existing cow if any
        			// (catch ArrayIndexOutOfBounds exception)
        			try {
        				this.eventCows[i].removeChild(0);
        			} catch (Exception e) {
        				// do nothing; continue
        			}
                
        			// add to transform
        			this.eventCows[i].addChild(event);
                
        			// add to branch group
        			this.catBranchGroup.addChild(eventCows[i]);                
        		}
            
        		if (recentEQcoloring == 1) {
        			initRecentEQAppearance();
        			for (int i = 0; i < numevents; i++) {
        				((Cow3D)this.eventCows[i].getChild(0)).setAppearance(
        						sphereGradientAppearance[getGradientScaleValue(i)]);
        			}    
        		}
        		// paint as single color
        		else if (getColor1().equals(getColor2())) {
        			initColorAppearance();
        			for (int i = 0; i < numevents; i++) {
        				((Cow3D)this.eventCows[i].getChild(0)).setAppearance(
        						sphereColorAppearance);
        			}
        		}
        		// paint gradient on depth or magnitude (initGradientMagnitude takes care
        		// of initializing appearance array with correct number of divisions
        		// getGradientScales value also checks apply style before returning
        		// a value
        		else {
        			initGradientAppearance();
        			for (int i = 0; i < numevents; i++) {
        				((Cow3D)this.eventCows[i].getChild(0)).setAppearance(
        						sphereGradientAppearance[getGradientScaleValue(i)]);
        			}                
        		}
        	}
        	// SPHERES:
        	else if (getGeometry() == GEOMETRY_SPHERE) {
        		if( (focalDisplay == FOCAL_BALL) || (focalDisplay == FOCAL_NONE) ) {
        			// init sphere transform groups if not already
        			initializeEventSpheres();
	            
        			// add spheres to transforms
        			addSpheresToBranchGroup();
	            
        		} else if (focalDisplay == FOCAL_DISC) {
        			addDiscsToBranchGroup();
        		}
        	}
        }
        */
        //EarthquakeCatalogPluginGUI.status.setText("Status");
    }

	private void addDiscsToBranchGroup() {
		/*FocalEQ eq;
		BranchGroup focalBG;
		
		Color3f compColor3f = new Color3f(focalDiscCompColor);
		Color3f extColor3f = new Color3f(focalDiscExtColor);

		ArrayList list = EarthquakeCatalogPluginGUI.getEarthquakes();
		for (int i = 0; i<list.size(); i++) {
			eq = (FocalEQ)list.get(i);
			
			eq.setColor1(compColor3f);
			eq.setColor2(extColor3f);
			
			eq.setScaleFactor(getEventDisplaySize(eq_magnitude[i]) * 5.0f);
			
			focalBG = eq.displayFocal();
			focalBG.detach();
			this.catBranchGroup.addChild(focalBG);
		}*/
	}
	
	public ArrayList<Earthquake> getSelectedEqList(){
		return eqList;
	}
	
	public void addEqList()
	{
		
		eqList = EarthquakeCatalogPluginGUI.getEarthquakes();
		addPointsToBranchGroup(false,eqList);
		addPointsToBranchGroup(true,eqList);
	}
	public void addComcatEqList()
	{
		if(((EarthquakeCatalogPluginGUI) parent).getComcatResourceDialog()!=null)
		{
			eqList = ((EarthquakeCatalogPluginGUI) parent).getComcatResourceDialog().getAllEarthquakes();
		}
		addPointsToBranchGroup(false,eqList);
		addPointsToBranchGroup(true,eqList);
	}
	
	public void addPointsToBranchGroup(boolean sphere,ArrayList<Earthquake> eqList)
	{
		initGradientAppearance();
		vtkActor actorEQCatalog = new vtkActor();
		vtkVertexGlyphFilter vertexGlyphFilter =new vtkVertexGlyphFilter();
		vtkPoints pts = new vtkPoints();
		vtkPolyDataMapper mapperEQCatalog = new vtkPolyDataMapper();
		vtkDoubleArray radi = new vtkDoubleArray();
		radi.SetName("radi");
		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
		colors.SetName("colors");
		colors.SetNumberOfComponents(4);
		colors.SetNumberOfTuples(eqList.size());
		
		double stepSize = (getMaxMagnitude()-getMinMagnitude())/gradientDivisions;
		double[] xForm = new double[3];
		for (int i = 0; i < eqList.size(); i++) 
		{
			
			Earthquake eq = eqList.get(i);
			xForm = Transform.transformLatLonHeight(eq.getEq_latitude(i), eq.getEq_longitude(i), -eq.getEq_depth(i));
			pts.InsertNextPoint(xForm);
			radi.InsertNextTuple1(eq.getEq_magnitude(i));
			// Color based on magnitude
			int ind= (int) ( Math.floor( Math.floor(eq.getEq_magnitude(i)) / stepSize)-getMinMagnitude());
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
		
		actorEQCatalog.SetMapper(mapperEQCatalog);
//		vtkScalarBarActor scalarBar = new vtkScalarBarActor();
//			  scalarBar.SetLookupTable(lut);
//			  scalarBar.SetTitle("Title");
//			  scalarBar.SetNumberOfLabels(4);
//			  Info.getMainGUI().getRenderWindow().GetRenderer().AddActor2D(scalarBar);
		masterEarthquakeCatalogBranchGroup.add(actorEQCatalog);
		Info.getMainGUI().addActors(masterEarthquakeCatalogBranchGroup);
	}
	
	
	
	
	private void addSpheresToBranchGroup() {
    	/*Earthquake eq;
    	BranchGroup sphereBG;
    	
		ArrayList eqList = EarthquakeCatalogPluginGUI.getEarthquakes();
		for (int i = 0; i<getNumEvents(); i++) {
			
		    // set size and primitive flags
		    float size = getEventDisplaySize(eq_magnitude[i]);

		    int primflags = (getFocalDisplay() != EQCatalog.FOCAL_NONE) ?
		            Sphere.GENERATE_NORMALS | Sphere.GENERATE_TEXTURE_COORDS :
		            Sphere.GENERATE_NORMALS;
		    
		    /** This is where objects are being displayed onto the scenegraph
		     * 1. Collection of earthquake objects are retrieved from Geo3dInfo
		     * 2. Iterate through collection and for each earthquake object call its
		     *    display method that returns a Sphere object after having taken in
		     *    relevant display parameters
		     *    Note: The earthquake class extends Shape3D
		     * 3. The individual earthquake object is added to a TransformGroup which
		     *    in turn is added to the main branchgroup
		     */
		   /* if(i == eqList.size())//hack to make sure filter doesnt crash
		    	break;
		    eq = (Earthquake)eqList.get(i);
		    
		    if ( getFocalDisplay() != FOCAL_NONE) {
		    	//create and set focal mechanism appearance
		    	initFocalAppearance();
		    	eq.setAppearance(sphereFocalAppearance);
		    } else if (recentEQcoloring == 1) {
		    	initRecentEQAppearance();
		    	eq.setAppearance(sphereRecentEQAppearance[getRecentEQScaleValue(i)]);
		    } else if (getColor1().equals(getColor2())){
		    	initColorAppearance();
		    	if(sphereColorAppearance!= null){
		    		eq.setAppearance(sphereColorAppearance);
		    	} else {
		    		System.out.println("sphere apperance is null");
		    	}
		    } else {
		    	initGradientAppearance();
		    	if (this.gradientDivisions != 1)
		    		eq.setAppearance(sphereGradientAppearance[getGradientScaleValue(i)]);
		    	else {
		    		//if there is not enough range within the catalog for a gradient
		    		//color all earthquakes the first selected color
		    		initColorAppearance();
		        	eq.setAppearance(sphereColorAppearance);
		        	EarthquakeCatalogPluginGUI.status.setText("Magnitude range too small for gradient");
		    	}
		    }
		    sphereBG = eq.display(size,primflags);
			this.catBranchGroup.addChild(sphereBG);
			displayedEQs.add(eq); 
		}*/
	}

	/*public ArrayList<Earthquake> getDisplayedEQs(){
		return displayedEQs;
	}*/
	
	private void initializeEventSpheres() {
		/*if (eventSpheres == null) {
			eventSpheres = new TransformGroup[getNumEvents()];
			for (int i = 0; i < getNumEvents(); i++) {
				eventSpheres[i] = new TransformGroup();
				eventSpheres[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		    }
		}*/
	}
  
   /* public BranchGroup getBranchGroup(){
    	return this.masterCatBranchGroup;
    }*/
    
    // clears data arrays and j3d component references
    private void unload() {
    	/*displayedEQs.clear();
        clearArrays();
        catBranchGroup.removeAllChildren();
        this.catBranchGroup = null;
        this.eventTrans = null;
        this.eventCoords = null;
        this.eventSpheres = null;
        this.gradientColors = null;
        this.recentEQColors = null;
        this.catMaterial = null;
        this.sphereColorAppearance = null;
        this.sphereFocalAppearance = null;
        this.sphereGradientAppearance = null;
        this.sharedPolyAtts = null;
        this.sizedEventPoints=null;*/
    }
    
    // given an event magnitude, returns a sphere radius which
    // can be adjusted by varying masterEventScale 
    /*public float getEventDisplaySize(float magnitude) {
        switch (getScaling()) {
        case SCALING_NONE:
            return magnitude;
        case SCALING_TEN:
        	return (float)(Math.floor((magnitude) + 1)*5) * masterEventScale;
        case SCALING_FOUR:
        	return (float)(Math.floor((magnitude) + 1)*2) * masterEventScale;
        case SCALING_TWO:
        	return (float)(Math.floor((magnitude) + 1)) * masterEventScale;
        case SCALING_ONE:
            return (float)(Math.floor((magnitude) + 1)/2) * masterEventScale;
        case SCALING_HALF:
            return (float)(Math.floor((magnitude*2) + 1)/4) * masterEventScale;
        case SCALING_FIFTH:
            return (float)(Math.floor((magnitude*5) + 1)/10) * masterEventScale;
        case SCALING_TENTH:
            return (float)(Math.floor((magnitude*10) + 1)/20) * masterEventScale;
        case SCALING_FIFTIETH:
            return (float)(Math.floor((magnitude*10) + 1)/40) * masterEventScale;
        default:
            return magnitude;
    }	
/*        switch (getScaling()) {
            case SCALING_NONE:
                return magnitude;
            case SCALING_TWO:
                return (float)(Math.floor((magnitude/2) + 1)) * masterEventScale;
            case SCALING_ONE:
                return (float)(Math.floor((magnitude) + 1)/2) * masterEventScale;
            case SCALING_HALF:
                return (float)(Math.floor((magnitude*2) + 1)/4) * masterEventScale;
            case SCALING_FIFTH:
                return (float)(Math.floor((magnitude*5) + 1)/10) * masterEventScale;
            case SCALING_TENTH:
                return (float)(Math.floor((magnitude*10) + 1)/20) * masterEventScale;
            default:
                return magnitude;
        }*/
             
    //}
    
   /* public  float getMasterEventScale() {
		return masterEventScale;
	}*/

	public void setMasterEventScale(float masterEventScale) {
		//this.masterEventScale = masterEventScale;
	}

	// initialize j3d fields
    public void initDisplay() {
       /* this.eventTrans = new Transform3D();
        //sets the material for the earthquake catalog for rendering
        this.catMaterial = new Material(
                new Color3f(Prefs.DEFAULT_MATERIAL_AMBIENT),
                new Color3f(Prefs.DEFAULT_MATERIAL_EMISSIVE),
                new Color3f(Prefs.DEFAULT_MATERIAL_DIFFUSE),
                new Color3f(Prefs.DEFAULT_MATERIAL_SPECULAR),
                Prefs.DEFAULT_MATERIAL_SHININESS);
/*        this.catMaterial = new Material(
                new Color3f(Color.GREEN),
                new Color3f(Color.BLACK),
                new Color3f(Color.GREEN),
                new Color3f(Color.BLACK),
                80.0f);*/
        /*this.catMaterial.setCapability(Material.ALLOW_COMPONENT_WRITE);
        this.sharedPolyAtts = new PolygonAttributes(
        		PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_BACK, 0.0f);*/
    }
        
    // sets the focal mech Appearance to this catalogs current focal colors
    private void initFocalAppearance() {
       /* this.sphereFocalAppearance = new Appearance();
        this.sphereFocalAppearance.setPolygonAttributes(this.sharedPolyAtts);
        
        this.catMaterial.setDiffuseColor(new Color3f(Color.WHITE));
        this.catMaterial.setAmbientColor(new Color3f(0.7f, 0.7f, 0.7f));
        this.sphereFocalAppearance.setMaterial(catMaterial);
        
        Texture focalTex = FocalMechIcons.getTexture(getFocalMech());
        focalTex.setBoundaryModeS(Texture.WRAP);
        focalTex.setBoundaryModeT(Texture.WRAP);
        focalTex.setMagFilter(Texture.BASE_LEVEL_POINT);
        TextureAttributes texAtt = new TextureAttributes();
        texAtt.setTextureMode(TextureAttributes.MODULATE);
        this.sphereFocalAppearance.setTexture(focalTex);
        this.sphereFocalAppearance.setTextureAttributes(texAtt);
		this.sphereFocalAppearance.setTransparencyAttributes(getTransparencyAttributes());*/
    }
    
    // sets the single color appearance to catalog color 1
    // play around with the catMaterial to change appearance of earthquake spheres
    private void initColorAppearance() {
        /*this.sphereColorAppearance = new Appearance();
        this.sphereColorAppearance.setPolygonAttributes(this.sharedPolyAtts);
        catMaterial.setDiffuseColor(new Color3f(getColor1()));
//        catMaterial.setShininess(100.0f);
//        catMaterial.setEmissiveColor(new Color3f(Color.WHITE));
        catMaterial.setAmbientColor(new Color3f(getColor1()));
//        this.catMaterial.setSpecularColor(new Color3f(getColor1()));
        this.sphereColorAppearance.setMaterial(this.catMaterial);
		this.sphereColorAppearance.setTransparencyAttributes(getTransparencyAttributes());*/
    }
    
    // set the appearance array for gradient representations
    private void initRecentEQAppearance() {
        // initialize color array
       /* initRecentEQColors();
        initRecentEQLimits();

        // init and apply colors to appearance array if sphere or cow geometry
        if ((getGeometry() == GEOMETRY_COW) || (getGeometry() == GEOMETRY_SPHERE)) {
        	this.sphereRecentEQAppearance = new Appearance[4];
            for (int i = 0; i < 4; i++) {
                Appearance app = new Appearance();
                app.setPolygonAttributes(this.sharedPolyAtts);
                Material gradMat = (Material)this.catMaterial.cloneNodeComponent(true);
                gradMat.setDiffuseColor(this.recentEQColors[i]);
                gradMat.setAmbientColor(this.recentEQColors[i]);
                app.setMaterial(gradMat);
	    		app.setTransparencyAttributes(getTransparencyAttributes());
                this.sphereRecentEQAppearance[i] = app;
            }
        }*/
    }
    // builds an array of colors to be referenced by sphere appearances and point arrays
    private void initRecentEQColors() {
    	/*this.recentEQColors = new Color3f[4];
    	this.recentEQColors[0] = new Color3f(1.0f,0.0f,0.0f);
    	this.recentEQColors[1] = new Color3f(0.0f,0.4f,1.0f);
    	this.recentEQColors[2] = new Color3f(1.0f,1.0f,0.0f);
    	this.recentEQColors[3] = new Color3f(0.5f,0.5f,0.5f);*/
    }
    // sets the time limit values for recent EQ coloring (in milliseconds)
    private void initRecentEQLimits() {
    	Calendar cal=Calendar.getInstance();
    	// TODO: tlrobins - add GUI and code to allow custom date
//    	if ()
//        	cal.set(1994, 0, 17, 13, 0, 0);
    	this.currentTimeSec=(cal.getTimeInMillis()-cal.get(Calendar.ZONE_OFFSET)-cal.get(Calendar.DST_OFFSET))/1000;    	
    	// TODO: tlrobins - add GUI and code to allow custom colors and limits
    	this.firstLimit  = currentTimeSec - (60*60);		// one hour
    	this.secondLimit = currentTimeSec - (60*60*24);		// one day
    	this.thirdLimit  = currentTimeSec - (60*60*24*7);	// one week
    }
    // returns the appropriate index for color or appearance selection for use with recent EQ coloring scheme
    private int getRecentEQScaleValue(int i) {
    	long eqTime = getEq_time(i).getTime()/1000;
    	if (eqTime>currentTimeSec){
    		System.out.println("Warning, your earthquakes are occuring in the future...check your clock");
    	}
    	if (eqTime >= this.firstLimit)
    		return 0;
    	else if (eqTime > this.secondLimit)
    		return 1;
    	else if (eqTime > this.thirdLimit)
    		return 2;
    	else {
    		return 3;
    	}
    }
    private int getRecentEQScaleValue(Date d)
    {
    	long eqTime = d.getTime()/1000;
    	if (eqTime>currentTimeSec){
    		System.out.println("Warning, your earthquakes are occuring in the future...check your clock");
    	}
    	if (eqTime >= this.firstLimit)
    		return 0;
    	else if (eqTime > this.secondLimit)
    		return 1;
    	else if (eqTime > this.thirdLimit)
    		return 2;
    	else {
    		return 3;
    	}
    }

	// sets the appearance array for gradient representations
    public void initGradientAppearance() {
        // calc mag divisions
       int magDivs = (int)Math.ceil(getMaxMagnitude()) - (int)Math.floor(getMinMagnitude());
        
        // find number of increments/divisions:
        //    -- depth gradient is always 3km intervals from 0-18km
        //    -- magnitude gradient spans the min and max mags for the catalog
        this.gradientDivisions = (getApplyGradientTo() == GRADIENT_APPLY_DEPTH) ? 6 : magDivs;
        
        // initialize color array
        initGradientColors(this.gradientDivisions);

        // init and apply colors to appearance array if sphere or cow geometry
        /*if ((getGeometry() == GEOMETRY_COW) || (getGeometry() == GEOMETRY_SPHERE)) {
        	this.sphereGradientAppearance = new Appearance[this.gradientDivisions];
            for (int i = 0; i<this.gradientDivisions; i++) {
                Appearance app = new Appearance();
                app.setPolygonAttributes(this.sharedPolyAtts);
                Material gradMat = (Material)this.catMaterial.cloneNodeComponent(true);
                gradMat.setDiffuseColor(this.gradientColors[i]);
                gradMat.setAmbientColor(this.gradientColors[i]);
                app.setMaterial(gradMat);
	    		app.setTransparencyAttributes(getTransparencyAttributes());
                this.sphereGradientAppearance[i] = app;
            }
        }*/
    }
    
   /* private TransparencyAttributes getTransparencyAttributes()
    {
    	TransparencyAttributes ta=new TransparencyAttributes(TransparencyAttributes.BLENDED,((100-transparency)/100.0f),
				TransparencyAttributes.BLEND_SRC_ALPHA,TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA);
    	return ta;
    }*/
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
    // returns the appropriate index for color or appearance selection for use with gradients
    /*private int getGradientScaleValue(int eventIndex) {
        
        int divSpan = 1;
        float datMin, datMax, dat;
        
        if (getApplyGradientTo() == GRADIENT_APPLY_DEPTH) {
            divSpan = 3;
            datMin = 0.0f;
            datMax = 18.0f;
            dat = eq_depth[eventIndex];
        } else {
            datMin = (float)Math.floor(getMinMagnitude());
            datMax = (float)Math.ceil(getMaxMagnitude());
            dat = eq_magnitude[eventIndex];
        }
        
        if (dat < datMin) {
            return 0;
        } else if (dat >= datMin && dat < datMax) {
            return (int)Math.floor(dat/divSpan) - (int)datMin;
        } else {
            return this.gradientDivisions-1;
        }
    }
    
 private int getGradientScaleValue(Earthquake event) {
        
        int divSpan = 1;
        float datMin, datMax, dat;
        
        if (getApplyGradientTo() == GRADIENT_APPLY_DEPTH) {
            divSpan = 3;
            datMin = 0.0f;
            datMax = 18.0f;
            dat = -event.getEq_depth();
        } else {
            datMin = (float)Math.floor(getMinMagnitude());
            datMax = (float)Math.ceil(getMaxMagnitude());
            dat = event.getEq_magnitude();
        }
        
        if (dat < datMin) {
            return 0;
        } else if (dat >= datMin && dat < datMax) {
            return (int)Math.floor(dat/divSpan) - (int)datMin;
        } else {
            return this.gradientDivisions-1;
        }
    }

    */
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
            this.scaling       = this.displayAttributes.getChild("scaling").getAttribute("value").getIntValue();
            try { this.recentEQcoloring = this.displayAttributes.getChild("recentEQcoloring").getAttribute("value").getIntValue();} catch (Exception e) {/*System.out.println("Error reading RecentEQColor property");*/}
            this.focalMech     = this.displayAttributes.getChild("focal_mech").getAttribute("value").getIntValue();
            this.applyGradient = this.displayAttributes.getChild("colors").getAttribute("apply_gradient").getIntValue();
            this.color1        = readColorElement(this.displayAttributes.getChild("colors").getChild("color_1"));
            this.color2        = readColorElement(this.displayAttributes.getChild("colors").getChild("color_2"));
            if(this.displayAttributes.getChild("transparency")!=null)
            {
            	this.transparency=this.displayAttributes.getChild("transparency").getAttribute("value").getIntValue();
            }
            else
            {
            	this.transparency=100;
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
    	EarthquakeCatalogPluginGUI.status.setText("Updating catalog display...");
        // kill and reinitialize branch group

        load();
        if (isDisplayed()) 
        {
        	//masterCatBranchGroup.addChild(this.catBranchGroup);
        	
        }
        EarthquakeCatalogPluginGUI.status.setText("Status");
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
      /*  if (show) {
            masterCatBranchGroup.addChild(this.catBranchGroup);
        } else {
            this.catBranchGroup.detach();
        }
        super.setDisplayed(show);*/
    }
    
    /**
     * Sets whether this catalog's data and display representation should be loaded
     * or released.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#setInMemory(boolean)
     */
    public void setInMemory(boolean load) {
        if (load) {
        	EarthquakeCatalogPluginGUI.status.setText("Loading catalog...");
            // importers may have already loaded data arrays; bypass by checking one
            if (this.eq_id == null) {
                readDataFile();
            }
            initDisplay();
            load();
            EarthquakeCatalogPluginGUI.status.setText("Status");
        } else {
            if (isDisplayed()) {
                setDisplayed(false);
            }
            unload();
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
    		this.displayAttributes.getChild("scaling").getAttribute("value").setValue(String.valueOf(scale));
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
	
/*	public ArrayList<Earthquake> getEarthquakes()
	{
		if(earthquakes!=null)
		{
			return earthquakes;
		}
		else
		{
			earthquakes= new ArrayList<Earthquake>();
			for(int i=0;i<eq_id.length;i++)
			{
				Earthquake eq= new Earthquake(eq_id[i],eq_time[i],eq_latitude[i],
						eq_longitude[i],eq_depth[i],eq_magnitude[i]);
				earthquakes.add(eq);
			}
	
			return earthquakes;
		}
	}
	
	private class PointColor
	{
		public Point3f location;
		public Color3f color;
	}*/
}



