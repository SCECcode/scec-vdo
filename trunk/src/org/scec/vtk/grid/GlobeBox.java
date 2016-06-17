package org.scec.vtk.grid;



import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.scec.vtk.main.MainGUI;
import org.scec.vtk.tools.Transform;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkLabelPlacementMapper;
import vtk.vtkMapper;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyLine;
import vtk.vtkSphericalTransform;
import vtk.vtkTransformPolyDataFilter;


public class GlobeBox {
	
	private GlobeLayout layout;		// the gridLayout object that holds customization parameters
	static double PiBy2 = Math.PI /2;	
	
	//BranchGroup globeScene;			// scene to hold and detatch the grid
	public vtkMapper globeScene ;//= new vtkActor();
	
	//TransformGroup globeTransform;	// transform group for transforming the globe
	
    //private Appearance lineAppearance;
    //private Appearance textAppearance;
    //private Material lineMaterial;
    //private ColoringAttributes lineColAtts;
    //private ColoringAttributes textColAtts;
    private Color lineColor = null;
    
	boolean bLatTexts;				// whether or not data for latitude text exist
	boolean bLonTexts; 				// whether or not data for longitude text exist
	
	boolean bLatHashes;				
	boolean bLonHashes;
	
	private boolean showLabels;
	//private BranchGroup labelsGroup; //group for storing the lat and lon text labels
	public vtkLabelPlacementMapper labelMapperLat;
	public vtkPolyDataMapper ptMapper;
	
	public GlobeLayout getLayout(){
		return layout;
	}
	public void setLineColor(Color c)
	{
		this.lineColor = c;
	}
	/*public GlobeBox(TransformGroup globalTransformG, GlobeLayout layout, Color3f color, boolean showTextLabels) {
		this.layout = layout;
		//this.globeScene = new BranchGroup();
		this.lineColor = color;
		//this.labelsGroup = new BranchGroup();
		//globeScene.setCapability(BranchGroup.ALLOW_DETACH);
		//globeTransform = globalTransformG;
		//globeTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		//globeTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		//labelsGroup.setCapability(BranchGroup.ALLOW_DETACH);
		showLabels = showTextLabels;
	}*/
	public GlobeBox( GlobeLayout layout, Color color, boolean showTextLabels) {
		this.layout = layout;
		//this.globeScene = new BranchGroup();
		this.lineColor = color;
		//this.labelsGroup = new BranchGroup();
		//globeScene.setCapability(BranchGroup.ALLOW_DETACH);
		//globeTransform = globalTransformG;
		//globeTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		//globeTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		//labelsGroup.setCapability(BranchGroup.ALLOW_DETACH);
		showLabels = showTextLabels;
		this.globeScene = new vtkMapper();
	}
	public void drawGlobe() {
		
		ArrayList<vtkPoints> points = getPoints();
		//ArrayList<vtkPoints> innerPoints = getInnerPoints();
		//init();
		//globeScene = new vtkActor();
		//globeScene = drawLatData(points);
		globeScene = drawLonData(points);
		//drawLonData(points);
		//drawConnectionData(globeScene, points, innerPoints);
		//if (showLabels) {
		//	globeScene.addChild(labelsGroup);
		//}
		
		//return globeScene;		
	}
	
//	public void setShowLabels(boolean sl) {
//		if (showLabels!=sl) {
//			showLabels = sl;
//			if (showLabels) {
//				globeScene.addChild(labelsGroup);
//			} else {
//				globeScene.removeChild(labelsGroup);
//			}
//		}
//	}

	/*
	private void drawConnectionData(BranchGroup returnMe, ArrayList<Point3d[]> points, ArrayList<Point3d[]> innerPoints) {
		LineStripArray[] connections   = drawConnectionsFromPoints(points,innerPoints);
		for(int i=0;i<connections.length;i++){
			returnMe.addChild(new Shape3D(connections[i], lineAppearance));
		}
	}*/

	private vtkPolyDataMapper drawLonData (ArrayList<vtkPoints> points) {
			//LineStripArray[] latLines = drawLatLinesFromPoints(points);
			
			vtkPoints allPoints =new vtkPoints();
			for(int i = 0;i<points.size();i++)
			{
				vtkPoints temp = (points.get(i));
				for(int j = 0;j<temp.GetNumberOfPoints();j++)
				{
				allPoints.InsertNextPoint(temp.GetPoint(j));
				double[] pts= temp.GetPoint(j);
				System.out.println(pts[0]);
				System.out.println(pts[1]);
				System.out.println(pts[2]);
				}
			}
			ArrayList<vtkPolyLine> polyLine = drawLonLinesFromPoints(points);
			
			
			  vtkCellArray  cellsPolyLine= new vtkCellArray();
			  for(int i = 0;i<polyLine.size();i++)
				cellsPolyLine.InsertNextCell(polyLine.get(i));
				
				vtkPolyData linesPolyData = new vtkPolyData();
				vtkPolyDataMapper globeMapper = new vtkPolyDataMapper();
				
				linesPolyData.SetPoints(allPoints);
				linesPolyData.SetLines(cellsPolyLine);
				//vtkSphericalTransform vts= new vtkSphericalTransform();
				//vtkTransformPolyDataFilter tpoly21 = new vtkTransformPolyDataFilter();
		 			//tpoly21.SetInputData(linesPolyData);
		 			//tpoly21.SetTransform(vts);
				//globeMapper.SetInputConnection(tpoly21.GetOutputPort());
				globeMapper.SetInputDataObject(linesPolyData);
				return globeMapper;
	}

	private vtkPolyDataMapper drawLatData(ArrayList<vtkPoints> points) {
		//LineStripArray[] latLines = drawLatLinesFromPoints(points);
		
		vtkPoints allPoints =new vtkPoints();
		for(int i = 0;i<points.size();i++)
		{
			vtkPoints temp = (points.get(i));
			for(int j = 0;j<temp.GetNumberOfPoints();j++)
			{
			allPoints.InsertNextPoint(temp.GetPoint(j));
			double[] pts= temp.GetPoint(j);
			System.out.println(pts[0]);
			System.out.println(pts[1]);
			System.out.println(pts[2]);
			}
		}
		ArrayList<vtkPolyLine> polyLine = drawLatLinesFromPoints(points);
		
		
		  vtkCellArray  cellsPolyLine= new vtkCellArray();
		  for(int i = 0;i<polyLine.size();i++)
			cellsPolyLine.InsertNextCell(polyLine.get(i));
			
			vtkPolyData linesPolyData = new vtkPolyData();
			vtkPolyDataMapper globeMapper = new vtkPolyDataMapper();
			
			linesPolyData.SetPoints(allPoints);
			linesPolyData.SetLines(cellsPolyLine);
			//vtkSphericalTransform vts= new vtkSphericalTransform();
			//vtkTransformPolyDataFilter tpoly21 = new vtkTransformPolyDataFilter();
	 			//tpoly21.SetInputData(linesPolyData);
	 			//tpoly21.SetTransform(vts);
			//globeMapper.SetInputConnection(tpoly21.GetOutputPort());
			globeMapper.SetInputDataObject(linesPolyData);
			
			//tempGlobeScene.GetProperty().SetColor(1,0,1);
		//TransformGroup[] latTexts = drawLatitudeTexts(points);
		/*for(int i=0;i<latLines.length;i++){
			globeScene.addChild(new Shape3D(latLines[i], lineAppearance));
		}
		for(int i=0;i<latTexts.length;i++)
		{
			labelsGroup.addChild(latTexts[i]);
		}*/
			return globeMapper;
	}
	
	public ArrayList<vtkPoints> getPoints(){
		ArrayList<vtkPoints> points = new ArrayList<vtkPoints>();
		int count = 0;	// Initialize the counter
		
		for(double lat = layout.topLat; lat >= layout.botLat; lat -= layout.latDegreeSpacing,count++){
			points.add(count,getPointsOnLatLine(Transform.calcRadius(lat),lat));
			//layout.centerOfEarth,
		}
		
		return points;
	}
	
	public ArrayList<vtkPoints> getInnerPoints(){
		ArrayList<vtkPoints> points = new ArrayList<vtkPoints>();
		int count = 0;	// Initialize the counter
		
		for(double lat = layout.topLat; lat >= layout.botLat; lat -= layout.latDegreeSpacing,count++){
			//layout.centerOfEarth,
			points.add(count,getPointsOnLatLine(Transform.calcRadius(lat) - layout.maxDepth,lat));
		}
		
		return points;
	}
	
	public ArrayList<vtkPolyLine> drawLatLinesFromPoints(ArrayList<vtkPoints> points) {
		//LineStripArray[] latLines = new LineStripArray[layout.numLatLinesWithSpacing +1];
		ArrayList<vtkPolyLine> latLines = new ArrayList<vtkPolyLine>();
		
		int resolution 	= layout.numLonLinesWithSpacing + 1;
		int length		= resolution + 1;
	 int loncount;
		
		for(int latcount = 0; latcount< points.size(); latcount++){
			//latLines[latcount] = new LineStripArray(length,GeometryArray.COORDINATES, new int[] { length });
			vtkPoints pts = points.get(latcount);
			vtkPolyLine plyline =new vtkPolyLine();
			//latLines.add(plyline);
			plyline.GetPointIds().SetNumberOfIds(pts.GetNumberOfPoints());
			for(loncount = 0;loncount<pts.GetNumberOfPoints();loncount++){
				plyline.GetPointIds().SetId(loncount, loncount);
			}
			latLines.add(plyline);
			//latLines.get(latcount).GetPointIds().SetId(resolution, pts.GetPoint(--loncount));//.setCoordinate(resolution,); //handle last point

		}
		return latLines;
	}

/*	public TransformGroup[] drawLatitudeTexts(ArrayList<Point3d[]> points) {
		Text3D[] texts 	= new Text3D[layout.numLatLinesWithSpacing +1];
		TransformGroup[] trans = new TransformGroup[layout.numLatLinesWithSpacing +1];
		
        // cancelled extrusion
        //double x1=0, y1 =0, x2=3, y2=0;
		//Shape extrusionShape = new java.awt.geom.Line2D.Double(x1,y1,x2,y2);
	    
        // test tesselation changes with switch to line appearance for text3d
		Font3D f3d 		= new Font3D(new Font("SansSerif",Font.PLAIN, layout.labelFontSize), 0.5, null);
		double degreeDiff = 0; //this is the number to subtract from the starting degree
		int latcount;
		DecimalFormat myFormatter = new DecimalFormat("0.######");
		for(latcount = 0;latcount< layout.numLatLinesWithSpacing + 1; latcount++){
			
			Point3d[] pts   = points.get(latcount);
			
			texts[latcount] = new Text3D(f3d,myFormatter.format(layout.topLat-degreeDiff),new Point3f(0.0f,0.0f,0.0f),Text3D.ALIGN_LAST,Text3D.PATH_RIGHT);
			OrientedShape3D txtShape = new OrientedShape3D(texts[latcount], textAppearance, OrientedShape3D.ROTATE_ABOUT_POINT, new Vector3f(0.0f,1.0f,0.0f));
			Transform3D offset = new Transform3D();
			offset.setTranslation( new Vector3f((float)pts[pts.length-1].x,(float)pts[pts.length-1].y,(float)pts[pts.length-1].z));
			trans[latcount] = new TransformGroup( offset );
			trans[latcount].addChild(txtShape);
			degreeDiff = degreeDiff + layout.latDegreeSpacing;
		}
		
		return trans;
	}
	
	public TransformGroup[] drawLongitudeTexts(ArrayList<Point3d[]> points) {
		Text3D[] texts 	= new Text3D[layout.numLonLinesWithSpacing +1];
		TransformGroup[] trans = new TransformGroup[layout.numLonLinesWithSpacing +1];
		Font3D f3d 		= new Font3D(new Font("SansSerif",Font.PLAIN, layout.labelFontSize), 0.5, null);
		
		Point3d[] pts   = points.get(0);
		double degreeDiff = 0; //this is the number to subtract from the starting degree
		DecimalFormat myFormatter = new DecimalFormat("0.######");
		for(int loncount = 0;loncount < layout.numLonLinesWithSpacing +1; loncount++){
			texts[loncount] = new Text3D(f3d,myFormatter.format(layout.rightLon-degreeDiff),new Point3f(0.0f,0.0f,0.0f),Text3D.ALIGN_FIRST,Text3D.PATH_RIGHT);
			OrientedShape3D txtShape = new OrientedShape3D(texts[loncount], textAppearance,OrientedShape3D.ROTATE_ABOUT_POINT, new Vector3f(0.0f,1.0f,1000.0f));
			Transform3D offset = new Transform3D();
			offset.setTranslation( new Vector3f((float)pts[loncount].x,(float)pts[loncount].y,(float)pts[loncount].z));
			trans[loncount] = new TransformGroup( offset );
			trans[loncount].addChild(txtShape);
			degreeDiff = degreeDiff + layout.lonDegreeSpacing;
		}

		return trans;		
	}
	

	public LineStripArray[] drawConnectionsFromPoints(ArrayList<Point3d[]> points, ArrayList<Point3d[]> innerPoints){
		LineStripArray[] cons = new LineStripArray[4];
		//Font3D f3d = new Font3D(new Font("SansSerif",Font.PLAIN, layout.labelFontSize),new FontExtrusion());
		
		cons[0] = new LineStripArray(2,GeometryArray.COORDINATES, new int[] {2});			//TOP RIGHT
		cons[0].setCoordinate(0,points.get(0)[0]);
		cons[0].setCoordinate(1,innerPoints.get(0)[0]);
		
		cons[1] = new LineStripArray(2,GeometryArray.COORDINATES, new int[] {2});			//BOTTOM RIGHT
		cons[1].setCoordinate(0,points.get(points.size()-1)[0]);
		cons[1].setCoordinate(1,innerPoints.get(innerPoints.size()-1)[0]);
		
		cons[2] = new LineStripArray(2,GeometryArray.COORDINATES, new int[] {2});			//TOP LEFT
		cons[2].setCoordinate(0,points.get(0)[points.get(0).length-1]);
		cons[2].setCoordinate(1,innerPoints.get(0)[points.get(0).length-1]);
		
		cons[3] = new LineStripArray(2,GeometryArray.COORDINATES, new int[] {2});			//BOTTOM LEFT
		cons[3].setCoordinate(0,points.get(points.size()-1)[points.get(0).length-1]);
		cons[3].setCoordinate(1,innerPoints.get(points.size()-1)[points.get(0).length-1]);
		
		return cons;		
	}
	*/
	public ArrayList<vtkPolyLine> drawLonLinesFromPoints(ArrayList<vtkPoints> points) {
		ArrayList<vtkPolyLine> lonLines = new ArrayList<vtkPolyLine>();
		//Font3D f3d = new Font3D(new Font("SansSerif",Font.PLAIN, layout.labelFontSize),new FontExtrusion());
		int resolution  			= layout.numLatLinesWithSpacing + 1;
		int length					= resolution + 1;
		int latcount;
		
		for(int loncount = 0; loncount< points.size(); loncount++){
			vtkPoints pts = points.get(loncount);
			vtkPolyLine plyline =new vtkPolyLine();;
			plyline.GetPointIds().SetNumberOfIds(pts.GetNumberOfPoints());
			for(latcount = 0;latcount<pts.GetNumberOfPoints();latcount++){
				plyline.GetPointIds().SetId(latcount, latcount);
			}
			lonLines.add(plyline);
		}
		return lonLines;
	}
	
	private vtkPoints getPointsOnLatLine(double radius, double lat) {
		
		double rCosPhi = radius * Math.cos((PiBy2) - Math.toRadians(lat));
		double rSinPhi = radius * Math.sin((PiBy2) - Math.toRadians(lat));
	
		//INVERT IMAGE//
		double leftLon  = -1 * layout.rightLon;
		double rightLon = -1 * layout.leftLon; 
		//END IMAGE INVERT//
	
		int resolution 		= layout.numLonLinesWithSpacing + 1;
		//int length			= resolution + 1;
		vtkPoints returnme 	= new vtkPoints();
				
		int count = 0;
		double[] pt =new double[3];
		
		//Using -0.00001 because of errors in comparing doubles to 0
		for(double i=leftLon; -0.000001 <= rightLon-i; i += layout.lonDegreeSpacing,count++) {   /*<-- not ++by 1!!!!!!!!*/
			pt[0] = rSinPhi * Math.cos(Math.toRadians(i));
			pt[1] = rCosPhi;
			pt[2] = rSinPhi * Math.sin(Math.toRadians(i));
			//returnme[count] = new Point3d(pt);
			returnme.InsertNextPoint(pt);
		}
		
		return returnme;			
	}
	
	/**
	 * Initializes line and text appearances; should get info from preferences.
	 * 
     * Derek Desens 2004
     * 
	 */
	private void init() {
        
        // TODO need to centralize 
        // common Appearance elements (for now)
        /*Color ambientColor  = new Color(0.0f, 0.0f, 0.0f);
        Color specularColor = new Color(0.0f, 0.0f, 0.0f);
        Color emissiveColor = new Color(0.0f, 0.0f, 0.0f);
        float shininess = 1.0f;
        
        // line Appearance
		lineAppearance = new Appearance();
		TransparencyAttributes lineTransAtts = new TransparencyAttributes();
		lineTransAtts.setTransparency(0.4f);
		lineTransAtts.setTransparencyMode(TransparencyAttributes.NICEST);
		lineAppearance.setTransparencyAttributes(lineTransAtts);
		
        Color3f lineDiffuseColor = lineColor;
        lineColAtts = new ColoringAttributes(lineDiffuseColor,ColoringAttributes.SHADE_FLAT);
        lineColAtts.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
        lineColAtts.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        lineMaterial = new Material(
                ambientColor,
                emissiveColor,
                lineDiffuseColor,
                specularColor,
                shininess);
        lineAppearance.setMaterial(lineMaterial);
        lineAppearance.setPolygonAttributes(
                new PolygonAttributes(
                        PolygonAttributes.POLYGON_LINE,
                        PolygonAttributes.CULL_NONE,
                        0));
        lineAppearance.setColoringAttributes(lineColAtts);

        if(Geo3dInfo.getPluginAntialiasing()){
			//Enable Antialiasing
			LineAttributes la = new LineAttributes();
			la.setLineAntialiasingEnable(true);
			lineAppearance.setLineAttributes(la);
		}  
        
        // text Appearance - lighting is disbled by default
        textAppearance = new Appearance();
        textAppearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
        textAppearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
        Color3f textColor = lineColor;
        textColAtts = new ColoringAttributes(textColor,ColoringAttributes.SHADE_FLAT);
        textColAtts.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
        textColAtts.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        textAppearance.setColoringAttributes(textColAtts);    
        */
	}
	public Color getLineColor() {
		// TODO Auto-generated method stub
		return this.lineColor;
	}

	/*public BranchGroup getGlobeScene() {
		return this.globeScene;
	}
	
	public void setColor(Color3f clr)
	{
		lineColAtts.setColor(clr);
		textColAtts.setColor(clr);
	}
	
	public Color3f getColor()
	{
		Color3f color = new Color3f();
		lineColAtts.getColor(color);
		return color;
	}*/
}
