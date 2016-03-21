package org.scec.vtk.plugins.CommunityfaultModelPlugin.components;


import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.utils.AbstractDataAccessor;
import org.scec.vtk.plugins.utils.DataImport;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkGeoAssignCoordinates;
import vtk.vtkMapper;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkSphericalTransform;
import vtk.vtkTransformPolyDataFilter;
import vtk.vtkTriangle;
import vtk.vtkXMLPolyDataReader;
import vtk.vtkXMLPolyDataWriter;

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
    // master branch group to which behaviors and faultBranchGroups are added
    private static ArrayList<vtkActor> masterFaultBranchGroup;

    // j3d parts of a Fault3D
    private vtkActor faultBranchGroup;
    private vtkPolyDataMapper faultRepresentation;       
    //private TransparencyAttributes alphTransAtts;

    // rendering/appearance components
    // mesh appearance
   /* //private FaultShape3D        meshFaultShape;
    private Shape3D        		meshFaultShape;
    private Appearance          meshFaultApp;
    private Material            meshMaterial;
    private PolygonAttributes   meshPolyAtts;
    private RenderingAttributes meshRenderAtts;
    // partially transparent appearance
    private FaultShape3D        alphFaultShape;
    private Appearance          alphFaultApp;
    private Material            alphMaterial;
    private PolygonAttributes   alphPolyAtts;
    private RenderingAttributes alphRenderAtts;
    // solid appearance
    private FaultShape3D        fillFaultShape;
    private Appearance          fillFaultApp;
    private Material            fillMaterial;
    private PolygonAttributes   fillPolyAtts;
    private RenderingAttributes fillRenderAtts;
    */
    // are data arrays loaded or set to null
    private boolean objectInMemory = false;
    
    // JDOM utility
    private static SAXBuilder parser = new SAXBuilder();
    
    private int selectedIndex;
       

    
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
                //Fault3DGUI.status.setText(DataImport.ERROR_OBJECT_LOAD);
                //Geo3dInfo.getActiveViewer().setMessage(DataImport.ERROR_OBJECT_LOAD);
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
    	/*vtkSphericalTransform vts= new vtkSphericalTransform();
        
		vtkTransformPolyDataFilter tpoly2 = new vtkTransformPolyDataFilter();
		tpoly2.SetInputData(objIn.GetOutput());
		tpoly2.SetTransform(vts);
		tpoly2.Update();*/
    	/*vtkDoubleArray latitude = this.lat;
    	latitude.SetName("latitude");
    	vtkDoubleArray longitude = this.lon;
    	longitude.SetName("longitude");*/
    	
    	//vtkGeoAssignCoordinates assign = new vtkGeoAssignCoordinates();
    	vtkPolyData pd = objIn.GetOutput();
	 
    	
    	
		/*assign.SetInputData(pd);
		//assign.set
		assign.SetLatitudeArrayName("latitude");
		assign.SetLongitudeArrayName("longitude");
		assign.SetGlobeRadius(Transform.re);
		
		assign.Update();
		 
	
		vtkPolyDataMapper mapperassign = new vtkPolyDataMapper();
		mapperassign.SetInputConnection(assign.GetOutputPort());
		*/
		vtkPolyDataMapper mapperassign1 = new vtkPolyDataMapper();
		mapperassign1.SetInputData(objIn.GetOutput());
		
		//vtkActor tempactor = new vtkActor();
		//tempactor.SetMapper(mapperassign);
		//System.out.println(tempactor.GetPosition()[0]);
			//vtkPolyDataMapper mapperassign =new vtkPolyDataMapper();
			//mapperassign.SetInputConnection(tpoly2.GetOutputPort());
			//mapperassign.SetInputData();
			this.faultBranchGroup = new vtkActor();
			this.faultBranchGroup.SetMapper(mapperassign1);
			//setFaultBranch(this.faultBranchGroup);
			//this.faultBranchGroup.SetPosition(tempactor.GetPosition());
    }
       catch (Exception e) {
           log.debug("problem reading binary data file");
           //Fault3DGUI.status.setText(DataImport.ERROR_OBJECT_LOAD);
           return false;
       }
       return true;  
    }
    /*public boolean readDataFile() {
        try {
            ObjectInputStream objIn = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(getDataFile())));
            /*double[] pts= (double[]) objIn.readObject();
            this.vertices.InsertNextPoint(pts);
            int[] triangleIndex = (int[]) objIn.readObject();
            vtkTriangle triangle =new vtkTriangle();
      	  	triangle.GetPointIds().SetId(0, triangleIndex[0]);
      	  	triangle.GetPointIds().SetId(1, triangleIndex[1]);
      	  	triangle.GetPointIds().SetId(2, triangleIndex[2]);
            this.triangles.InsertNextCell(triangle);
            this.faultBranchGroup = (vtkActor) objIn.readObject();
            if (getColor() == null) {
                this.colors = (Color[])objIn.readObject();
            }
            objIn.close();
        }
        catch (Exception e) {
            log.debug("problem reading binary data file");
            //Fault3DGUI.status.setText(DataImport.ERROR_OBJECT_LOAD);
            return false;
        }
        return true;       
    }*/
    
    /**
     * Writes this <code>FaultAccessor</code> geometry data to disk/library.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#writeDataFile()
     */
    /*public boolean writeDataFile() {
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(getDataFile())));
            objOut.writeObject(this.faultBranchGroup);
            //objOut.writeObject(this.triangles);
            if (getColor() == null) {
                objOut.writeObject(Color.BLUE);
            }
            objOut.close();
        }
        catch (Exception e) {
            log.debug("problem writing binary data file");
            //Fault3DGUI.status.setText(DataImport.ERROR_FILE_WRITE);
            return false;
        }
        return true;
    }*/
    public boolean writeDataFile() {
        try {
            //ObjectOutputStream objOut = new ObjectOutputStream(
             //       new BufferedOutputStream(new FileOutputStream(getDataFile())));
            vtkPolyDataWriter objOut = new vtkPolyDataWriter();
            objOut.SetFileName(getDataFile().getAbsolutePath());
            vtkPolyData polydata = new vtkPolyData();

           // polydata = this.graphToPlyData;
            polydata.SetPoints(this.vertices);
 			polydata.SetPolys(this.triangles);
 			
            objOut.SetInputData(polydata);
            //objOut.writeObject(this.triangles);
            /*if (getColor() == null) {
                objOut.writeObject(Color.BLUE);
            }*/
            objOut.Write();
        }
        catch (Exception e) {
            log.debug("problem writing binary data file");
            //Fault3DGUI.status.setText(DataImport.ERROR_FILE_WRITE);
            return false;
        }
        return true;
    }
    
    // loads a fault from fault data (.dat) and metadata (.flt)
    private void load() {       
        
        /* build coordinate array for GeometryInfo
        //   -- coords is a 1D array of triangle vertices; every 3 coords = 1 triangle
        int n = this.triangles.GetNumberOfCells();
        Point3d[] coords = new Point3d[n * 3];
        for (int i=0; i < n; i++) {
            coords[3*i]   = this.vertices[this.triangles[i].x];
            coords[3*i+1] = this.vertices[this.triangles[i].y];
            coords[3*i+2] = this.vertices[this.triangles[i].z];
        }

        
        // NOTE: not sure if this (colorIndices) is necessary
        // if color array expected build color and color indices array
        //   -- colors is a 1D array of colors that coorespond to each vertex
        Color3f[] colorArray = new Color3f[0];
        //int[] colorIndices;
        if (getColor() == null) 
        {
            colorArray = new Color3f[n * 3];
            for (int i=0; i < n; i++) 
            {
                for (int j=0; j < 3; j++ ) 
                {
                    colorArray[3*i+j] = new Color3f(this.colors[i]);
                }
            }
            // set indices
            //colorIndices = new int[colors.length];
            //for (int i = 0; i < colors.length; i++) {
            //    colorIndices[i] = i;
            //}
        }
        
        // build object via GeometryInfo
        GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
        gi.setCoordinates(coords);

        // generate normals
        NormalGenerator ng = new NormalGenerator();
        ng.setCreaseAngle(Math.PI);
        ng.generateNormals(gi);
        
        
        // correct the normals (Why is this done?)
        // TODO test
//        Vector3f[] normals = gi.getNormals();
//        for (int i = 0; i < normals.length; i++) {
//            normals[i].absolute();
//        }
//        gi.setNormals(normals);
        
        // set color if necessary
        if (this.color == null) {
            //gi.setColorIndices(colorIndices);
            gi.setColors(colorArray);
        }
        
        // stripify data
        Stripifier st = new Stripifier();
        st.stripify(gi);

        this.faultRepresentation = gi.getGeometryArray();
        
                
        // set geometry and appearance of each display component
        this.meshFaultShape = new FaultShape3D(this.faultRepresentation, this.getMeshAppearance());
        this.alphFaultShape = new FaultShape3D(this.faultRepresentation, this.getAlphAppearance());
        this.fillFaultShape = new FaultShape3D(this.faultRepresentation, this.getFillAppearance());

        // set level of pick detail
        PickTool.setCapabilities(this.meshFaultShape, PickTool.INTERSECT_TEST);
        PickTool.setCapabilities(this.alphFaultShape, PickTool.INTERSECT_TEST);
        PickTool.setCapabilities(this.fillFaultShape, PickTool.INTERSECT_TEST);
        
        BranchGroup temp = new BranchGroup();
  
        if ( getIndex() == 0 ) {
	        // add components to object branch group
	        this.faultBranchGroup.addChild(this.meshFaultShape);
	        this.faultBranchGroup.addChild(this.alphFaultShape);
	        this.faultBranchGroup.addChild(this.fillFaultShape);        
        }
        //can add child while live
        else {
        	temp.addChild(this.meshFaultShape);
	        temp.addChild(this.alphFaultShape);
	        temp.addChild(this.fillFaultShape);
	        faultBranchGroup.addChild(temp);
        }
    
        // (re)process current attributes
        // may be redundant but ensures correct appearance
        // setMeshState calls setColor so unnecessary here
        setMeshState(this.meshState);
        */
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
        this.faultBranchGroup = new vtkActor();
        
    }
        //this.faultBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        //this.faultBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
                
        // init material and capabilities
       /* this.meshMaterial = new Material(
                new Color3f(Prefs.DEFAULT_MATERIAL_AMBIENT),
                new Color3f(Prefs.DEFAULT_MATERIAL_EMISSIVE),
                new Color3f(Prefs.DEFAULT_MATERIAL_DIFFUSE),
                new Color3f(Prefs.DEFAULT_MATERIAL_SPECULAR),
                Prefs.DEFAULT_MATERIAL_SHININESS);
        this.meshMaterial.setCapability(Material.ALLOW_COMPONENT_WRITE);
        this.meshMaterial.setCapability(Material.ALLOW_COMPONENT_READ);
        this.alphMaterial = new Material(
                new Color3f(Prefs.DEFAULT_MATERIAL_AMBIENT),
                new Color3f(Prefs.DEFAULT_MATERIAL_EMISSIVE),
                new Color3f(Prefs.DEFAULT_MATERIAL_DIFFUSE),
                new Color3f(Prefs.DEFAULT_MATERIAL_SPECULAR),
                Prefs.DEFAULT_MATERIAL_SHININESS);
        this.alphMaterial.setCapability(Material.ALLOW_COMPONENT_WRITE);
        this.fillMaterial = new Material(
                new Color3f(Prefs.DEFAULT_MATERIAL_AMBIENT),
                new Color3f(Prefs.DEFAULT_MATERIAL_EMISSIVE),
                new Color3f(Prefs.DEFAULT_MATERIAL_DIFFUSE),
                new Color3f(Prefs.DEFAULT_MATERIAL_SPECULAR),
                Prefs.DEFAULT_MATERIAL_SHININESS);
        this.fillMaterial.setCapability(Material.ALLOW_COMPONENT_WRITE);
        this.fillMaterial.setCapability(Material.ALLOW_COMPONENT_READ);
                
        // init polygon attributes and capabilities
        this.meshPolyAtts = new PolygonAttributes(PolygonAttributes.POLYGON_LINE,PolygonAttributes.CULL_NONE,-1.0f,false);
        this.alphPolyAtts = new PolygonAttributes(PolygonAttributes.POLYGON_FILL,PolygonAttributes.CULL_NONE,0.0f,false);
        this.fillPolyAtts = new PolygonAttributes(PolygonAttributes.POLYGON_FILL,PolygonAttributes.CULL_NONE,0.0f,false);
        
        // init rendering attributes and capabilities
        this.meshRenderAtts = new RenderingAttributes();
        this.meshRenderAtts.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        this.alphRenderAtts = new RenderingAttributes();
        this.alphRenderAtts.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        this.fillRenderAtts = new RenderingAttributes();
        this.fillRenderAtts.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        
        // init transparaeny attributes (alpha fill component only)
        this.alphTransAtts = new TransparencyAttributes(TransparencyAttributes.NICEST,0.4f);
    }

    // Returns the mesh Appearance for this fault; only called when fault is 
    // instantiated the first time
    private Appearance getMeshAppearance() {
        this.meshFaultApp = new Appearance();
        this.meshFaultApp.setMaterial(this.meshMaterial);
        this.meshFaultApp.setPolygonAttributes(this.meshPolyAtts);
        this.meshFaultApp.setRenderingAttributes(this.meshRenderAtts);
        return this.meshFaultApp;
    }
    
    // Returns the alpha/transparent Appearance for this fault; only called when fault is 
    // instantiated the first time
    private Appearance getAlphAppearance() {
        this.alphFaultApp = new Appearance();
        this.alphFaultApp.setMaterial(this.fillMaterial);
        this.alphFaultApp.setPolygonAttributes(this.alphPolyAtts);
        this.alphFaultApp.setTransparencyAttributes(this.alphTransAtts);
        this.alphFaultApp.setRenderingAttributes(this.alphRenderAtts);
        return this.alphFaultApp;
    }

    // Returns the solid/fill Appearance for this fault; only called when fault is 
    // instantiated the first time
    private Appearance getFillAppearance() {
        this.fillFaultApp = new Appearance();
        this.fillFaultApp.setMaterial(this.fillMaterial);
        this.fillFaultApp.setPolygonAttributes(this.fillPolyAtts);
        this.fillFaultApp.setRenderingAttributes(this.fillRenderAtts);
        return this.fillFaultApp;
    }
*/
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
            // importers may want to set field and build J3D objects 
            // without reloading data so check a data array
            //if (this.vertices == null) {
                readDataFile();
                
            //}
            //else
            //{
            	
            //}
            setDisplayed(load);
            //load();
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
        if (show) {
        	 //this.masterFaultBranchGroup.add(this.faultBranchGroup);
        	//this.faultBranchGroup.VisibilityOn();
        } else {
        	//this.masterFaultBranchGroup.remove(this.faultBranchGroup);
            //this.faultBranchGroup.VisibilityOff();
        }
        super.setDisplayed(show);
        //MainGUI.updateCFM(this.masterFaultBranchGroup);
        //MainGUI.updateRenderWindow();
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
        
        /*if (this.faultRepresentation != null) {
            this.fillMaterial.setDiffuseColor(new Color3f(newColor));
            this.fillMaterial.setAmbientColor(new Color3f(newColor));
            this.fillMaterial.setSpecularColor(new Color3f(newColor));
            this.meshMaterial.setDiffuseColor(new Color3f(newColor.darker()));
            this.meshMaterial.setAmbientColor(new Color3f(newColor.darker()));

        }
        if (this.meshMaterial.getLightingEnable()==false){//assume that if one is set, so are both to save time
        	this.meshMaterial.setLightingEnable(true);
        	this.fillMaterial.setLightingEnable(true);
        }
        writeColorElement((this.displayAttributes.getChild("color")), this.color);
        */
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
        
       /* if (this.faultRepresentation != null) {
            this.fillMaterial.setDiffuseColor(new Color3f(newColor));
            this.fillMaterial.setAmbientColor(new Color3f(newColor));
            this.fillMaterial.setSpecularColor(new Color3f(newColor));
            this.fillMaterial.setLightingEnable(false);
            this.meshMaterial.setDiffuseColor(new Color3f(newColor));
            this.meshMaterial.setAmbientColor(new Color3f(newColor));
            this.meshMaterial.setLightingEnable(false);
        }
        
        writeColorElement((this.displayAttributes.getChild("color")), this.color);
        */
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
    
    public void setMasterFaultBranchGroup(ArrayList<vtkActor> masterFaultBranchGroup) 
    {
    	this.masterFaultBranchGroup = masterFaultBranchGroup;
    }
    public static ArrayList<vtkActor> getMasterFaultBranchGroup() 
    {
    	return masterFaultBranchGroup;
    }
    
    public vtkPoints getVertices(){
    	return vertices;
    }
    
    public void setFaultBranch(vtkActor faultBranchGroup)
    {
    	this.faultBranchGroup = faultBranchGroup;
    	this.masterFaultBranchGroup.add(this.faultBranchGroup);
    }
    public vtkActor getFaultBranch()
    {
    	return this.faultBranchGroup;
    }
    
    
    //****************************************
    //     NESTED CLASSES
    //****************************************

    /**
     * Inner class designed to allow pick behaviors to discern instances of <code>Fault3D</code> objects.
     * Wrapping a <code>Shape3D</code> in this way ensures future compatibility if pick behaviors
     * are added to other SCEC-VIDEO objects.
     * 
     * @see org.scec.geo3d.plugins.CommunityFaultModelPlugin.components.FaultPickBehavior
     */
   /* public class FaultShape3D extends Shape3D {
        
    	Color oldAppearance;
                
        FaultShape3D(GeometryArray ga, Appearance app) {
            super(ga,app);
            this.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
            oldAppearance = getColor();
        }
        
        public Point3d getPos(){
        	if(vertices[0] != null)
        		return vertices[0];
        	else return null;
        }
        
        public void hLight(){
        	oldAppearance = getColor();
        	setColorNoLighting(new Color(1.0f,1.0f,1.0f));
        }
        public void unhLight(){
        	setColor(oldAppearance);
        }
        /**
         * Returns the display name of this <code>FaultShape3D</code>.
         * 
         * @return the display name of this fault
         */
      /*  public String getInfo() {
            return getDisplayName();
        } 
    }*/
    
    public void setIndex(int index) {
    	this.selectedIndex = index;
    }
    
    public int getIndex() {
    	return this.selectedIndex;
    }

}

