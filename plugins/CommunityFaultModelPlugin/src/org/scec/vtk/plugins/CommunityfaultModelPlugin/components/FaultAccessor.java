package org.scec.vtk.plugins.CommunityfaultModelPlugin.components;


import java.awt.Color;
import java.io.File;
import java.net.URL;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.utils.AbstractDataAccessor;
import org.scec.vtk.tools.picking.PickEnabledActor;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkMapper;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkStringArray;

/**
 * This subclass of <code>AbstractDataAccessor</code> provides the bulk of the framework
 * necessary to deal with 3D fault representations. Concrete subclasses need only implement
 * constructors and custom i/o methods and fields as necessary.
 *
 * Created on Mar 7, 2005
 * 
 * @author P. Powers
 * @version $Id: FaultAccessor.java 4362 2013-07-03 19:38:09Z frias $
 */
public abstract class FaultAccessor extends AbstractDataAccessor {

	private Logger log = Logger.getLogger(FaultAccessor.class);

	// stored display attributes
	private Color color;
	private int meshState;

	/** XML <code>Element</code> for access to object attribute <code>Element</code>s */
	protected Element objectAttributes;
	/** XML <code>Element</code> for display attribute access. */
	protected Element displayAttributes;

	// data arrays
	/** Array of fault vertices. Initialized to <code>null</code> */
	protected vtkPoints vertices = null;
	/** Array of fault triangles. Initialized to <code>null</code> */
	protected vtkCellArray triangles = null;
	/** Array of fault colors. Initialized to <code>null</code> */
	protected Color colors = null;

	protected vtkPolyData graphToPlyData = null;

	protected vtkDoubleArray lat = new vtkDoubleArray();
	protected vtkDoubleArray lon = new vtkDoubleArray();

	private vtkActor faultActor;
	private vtkPolyDataMapper faultRepresentation;       
	// are data arrays loaded or set to null
	private boolean objectInMemory = false;

	// JDOM utility
	private static SAXBuilder parser = new SAXBuilder();

	private int selectedIndex;

	private vtkMapper faultMapper;

	private int visible;



	/**
	 * Constructs a new <code>FaultAccessor</code>.
	 * 
	 */
	public FaultAccessor() {
		init();
	}

	//****************************************
	//  PROTECTED & PUBLIC UTILITY METHODS
	//****************************************

	/**
	 * Creates a new, empty fault <code>Document</code> with default attributes.
	 * 
	 * @see org.scec.geo3d.plugins.utils.AbstractDataAccessor#newDocument()
	 */
	protected boolean newDocument() {
		// add attribute component
		if (super.newDocument()) {
			URL attsDoc = FaultAccessor.class.getResource("resources/xml/fault_template.xml");
			try {
				Document temp = parser.build(attsDoc);
				this.objectAttributes = (Element)temp.getRootElement().getChild("object_attributes").detach();
				this.displayAttributes = this.objectAttributes.getChild("display");
				this.objectDocument.getRootElement().addContent(this.objectAttributes);
			}
			catch (Exception e) {
				log.debug("problem parsing/reading XML");
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Reads fault attributes from a given file.
	 * 
	 * @see org.scec.geo3d.plugins.utils.DataAccessor#readAttributeFile(java.io.File)
	 */
	public boolean readAttributeFile(File file) {
		if (!(super.readAttributeFile(file))) return false;
		this.objectAttributes = this.objectDocument.getRootElement().getChild("object_attributes");
		this.displayAttributes = this.objectAttributes.getChild("display");
		try {
			this.meshState = this.displayAttributes.getChild("mesh_state").getAttribute("value").getIntValue();
			this.color     = readColorElement(this.displayAttributes.getChild("color"));
		}
		catch (Exception e) { 
			log.debug("problem reading XML");
			//Fault3DGUI.status.setText(DataImport.ERROR_OBJECT_LOAD);
			return false;
		}
		return true;
	}

	/**
	 * Reads fault geometry data. File loactaion is known from attribute file.
	 * 
	 * @see org.scec.geo3d.plugins.utils.DataAccessor#readDataFile()
	 */
	public boolean readDataFile()
	{
		try{

			vtkPolyDataReader objIn = new vtkPolyDataReader();
			objIn.SetFileName(getDataFile().getAbsolutePath());
			objIn.Update();

			vtkStringArray infoArray = new vtkStringArray();
			infoArray.SetName("Info");
			infoArray.InsertNextValue(getDisplayName());

			//create new polygon to overwrite default polygon points color and add it to fault actor

			vtkPolyData pd =  new vtkPolyData();
			pd.SetPoints(objIn.GetOutput().GetPoints());
			pd.SetPolys(objIn.GetOutput().GetPolys());
			pd.GetPointData().AddArray(infoArray);

			vtkPolyDataMapper mapperassign1 = new vtkPolyDataMapper();
			mapperassign1.SetInputData(pd);

			this.faultMapper = mapperassign1;

			vtkDoubleArray c1 = (vtkDoubleArray) objIn.GetOutput().GetPointData().GetScalars("Colors");
			double[] c = c1.GetTuple3(0);
			Color color = new Color((int)c[0], (int) c[1], (int)c[2]); 
			setColor(color);
			c[0] /= Info.rgbMax;
			c[1] /= Info.rgbMax;
			c[2] /= Info.rgbMax;
			this.faultActor.GetProperty().SetColor(c);

		}
		catch (Exception e) {
			log.debug("problem reading binary data file");
			//Fault3DGUI.status.setText(DataImport.ERROR_OBJECT_LOAD);
			return false;
		}
		return true;  
	}

	/**
	 * Writes this <code>FaultAccessor</code> geometry data to disk/library.
	 * 
	 * @see org.scec.geo3d.plugins.utils.DataAccessor#writeDataFile()
	 */

	public boolean writeDataFile() {
		try {

			vtkPolyDataWriter objOut = new vtkPolyDataWriter();
			objOut.SetFileName(getDataFile().getAbsolutePath());
			vtkPolyData polydata = new vtkPolyData();

			polydata.SetPoints(this.vertices);
			polydata.SetPolys(this.triangles);

			Color defaultColorFault = getColor();
			// Setup the colors array vtk
			vtkDoubleArray colors =new vtkDoubleArray();
			colors.SetNumberOfComponents(3);
			colors.SetName("Colors");
			if (defaultColorFault == null && this.colors == null) {
				defaultColorFault = Color.lightGray;
			}
			if(this.colors!=null)
			{
				defaultColorFault =this.colors;
			}
			// Add the color we have created to the array
			colors.InsertNextTuple3(defaultColorFault.getRed(), defaultColorFault.getGreen(),defaultColorFault.getBlue());
			colors.InsertNextTuple3(defaultColorFault.getRed(), defaultColorFault.getGreen(),defaultColorFault.getBlue());
			colors.InsertNextTuple3(defaultColorFault.getRed(), defaultColorFault.getGreen(),defaultColorFault.getBlue());

			polydata.GetPointData().SetScalars(colors);
			objOut.SetInputData(polydata);

			objOut.Write();
		}
		catch (Exception e) {
			log.debug("problem writing binary data file");
			return false;
		}
		return true;
	}


	private void unload() {
		this.vertices = null;
		this.triangles = null;
		this.colors = null;
		this.faultRepresentation = null;
	}

	//****************************************
	//     PRIVATE METHODS
	//****************************************

	/**
	 * Initializes fault appearance attributes and properties.
	 */
	private void init() {



		// inti J3D object root
		this.faultActor = new vtkActor();

	}
	//****************************************
	//     GETTERS & SETTERS
	//****************************************

	/**
	 * Returns whether this fault's data and display representation are loaded.
	 * 
	 * @see org.scec.geo3d.plugins.utils.DataAccessor#isInMemory()
	 */
	public boolean isInMemory() {
		return this.objectInMemory;
	}

	/**
	 * Sets whether this fault's data and display representation should be loaded
	 * or released.
	 * 
	 * @see org.scec.geo3d.plugins.utils.DataAccessor#setInMemory(boolean)
	 */
	public void setInMemory(boolean load) {
		if (load) {
			readDataFile();
			setDisplayed(load);
		} else {
			setDisplayed(load);
			unload();
		}
		this.objectInMemory = load;
	}


	/**
	 * Sets whether this fault should be displayed or not.
	 * 
	 * @see org.scec.geo3d.plugins.utils.DataAccessor#setDisplayed(boolean)
	 */
	public void setDisplayed(boolean show) {
		super.setDisplayed(show);
	}

	/**
	 * Returns the current fault color. If fault is multi-colored, color value is
	 * <code>null</code> and can not be set or retrieved.
	 * 
	 * @return this fault's color
	 */
	public Color getColor() {
		return this.color;
	}

	/**
	 * Sets the current color for this fault. If fault is multi-colored, color value is
	 * <code>null</code> and can not be set or retrieved. This method sets the 
	 * diffuse color of this fault's <code>Material</code> and also updates its color
	 * attribute should it's display state be saved.
	 * 
	 * @param newColor the new fault color
	 */
	public void setColor(Color newColor) {
		// TODO prevent setting of color if value is null (get current color etc...)
		// TODO checking if onnbject is live is handled by LibraryModel
		this.color = newColor;

		if (this.faultRepresentation != null) {
			this.faultActor.GetProperty().SetColor(newColor.getRed(),newColor.getGreen(),newColor.getBlue());

		}

		writeColorElement((this.displayAttributes.getChild("color")), this.color);

	}


	/**
	 * Sets the current color for this fault. If fault is multi-colored, color value is
	 * <code>null</code> and can not be set or retrieved. This method sets the 
	 * diffuse color of this fault's <code>Material</code> and also updates its color
	 * attribute should it's display state be saved.
	 * 
	 * @param newColor the new fault color
	 */
	public void setColorNoLighting(Color newColor) {
		// TODO prevent setting of color if value is null (get current color etc...)
		// TODO checking if onnbject is live is handled by LibraryModel
		this.color = newColor;
	}

	/**
	 * Returns the mesh display state of this fault. Possible values are:
	 * <pre>
	 * MESH_NO_FILL
	 * MESH_TRANS_FILL
	 * MESH_SOLID_FILL
	 * NO_MESH_TRANS_FILL
	 * NO_MESH_SOLID_FILL
	 * </pre>
	 * 
	 * @return this fault's mesh value.
	 * @see org.scec.geo3d.plugins.CommunityFaultModelPlugin.components.MeshStateIcon
	 */
	public int getMeshState() {
		return this.meshState;
	}

	/**
	 * Sets how this fault is drawn. Currently 1 of 5 states are possible that
	 * vary transparency and mesh vs solid/filled appearance:
	 * <pre>
	 * MESH_NO_FILL
	 * MESH_TRANS_FILL
	 * MESH_SOLID_FILL
	 * NO_MESH_TRANS_FILL
	 * NO_MESH_SOLID_FILL
	 * </pre>
	 *  Method controls
	 * a fault's 3-component visibility via <code>RenderingAttributes</code>.<br/>
	 *<br/>
	 * <i>Note: display of overlapping transparent objects is problematic in Java3D
	 * and the mixing of transparent and non-transparent objects can be used to
	 * "trick" a viewers eye.</i>
	 * 
	 * @param meshValue the value to set.
	 * @see org.scec.geo3d.plugins.CommunityFaultModelPlugin.components.MeshStateIcon
	 */
	public void setMeshState(int meshValue) {

		this.meshState = meshValue;

		// depending on meshState some colors are adjusted (see setColor())
		// this is a little circular
		setColor(this.color);

		// reset mesh color from MESH_SOLID_FILL option below
		//this.meshMaterial.setDiffuseColor(new Color3f(this.color));

		/*if (getMeshState() == MeshStateIcon.MESH_NO_FILL) {
            this.meshRenderAtts.setVisible(true);
            this.alphRenderAtts.setVisible(false);
            this.fillRenderAtts.setVisible(false);
        } else if (getMeshState() == MeshStateIcon.MESH_TRANS_FILL) {
            this.meshRenderAtts.setVisible(true);
            this.alphRenderAtts.setVisible(true);
            this.fillRenderAtts.setVisible(false);
        } else if (getMeshState() == MeshStateIcon.MESH_SOLID_FILL) {
            this.meshMaterial.setDiffuseColor(new Color3f(this.color.darker()));
            this.meshRenderAtts.setVisible(true);
            this.alphRenderAtts.setVisible(false);
            this.fillRenderAtts.setVisible(true);
        } else if (getMeshState() == MeshStateIcon.NO_MESH_TRANS_FILL) {
            this.meshRenderAtts.setVisible(false);
            this.alphRenderAtts.setVisible(true);
            this.fillRenderAtts.setVisible(false);
        } else {
            this.meshRenderAtts.setVisible(false);
            this.alphRenderAtts.setVisible(false);
            this.fillRenderAtts.setVisible(true);
        }

        this.displayAttributes.getChild("mesh_state").getAttribute("value").setValue(
                String.valueOf(meshValue));*/
	}   

	public vtkPoints getVertices(){
		return vertices;
	}

	public vtkMapper getFaultMapper()
	{
		return this.faultMapper;
	}
	public void  setFaultActor(PickEnabledActor<Fault3D> actor)
	{
		 this.faultActor = actor;
	}
	public vtkActor getFaultActor()
	{
		return this.faultActor;
	}
	
//	public void setVisible(int index) {
//		this.visible = index;
//	}
//
//	public int getVisible() {
//		return this.visible;
//	}
	
	public void setIndex(int index) {
		this.selectedIndex = index;
	}

	public int getIndex() {
		return this.selectedIndex;
	}

}

