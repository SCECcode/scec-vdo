package org.scec.vtk.plugins.GISHazusEventsPlugin;

import java.awt.Color;
import org.opensha.commons.geo.LocationList;
import org.scec.vtk.tools.Transform;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolygon;

public class FilledBoundary 
{
	
	private float[] latitude, longitude;
	private Color color;
	protected String name;
	private boolean bDisplayed;
	public static int counter=0;
	public static float lineWidth = 1;
	vtkActor segmentActors;
	/**
	 * 
	 * Creates a boundary with nothing set.
	 * 
	 * @author punihaol
	 * @param void
	 * @return void
	 * 
	 **/
	public FilledBoundary()
	{
		latitude = new float[2];
		longitude = new float[2];
		color = new Color(1.0f,1.0f,1.0f);
		name = "";
		bDisplayed = false;
	}
	
	/**
	 * 
	 * Creates a boundary with latitude and longitude outline, a color, and 
	 * name.
	 * @author punihaol
	 * @param name - the name of the boundary
	 * @param color - the color of the boundary
	 * @param latitude - an array of latitude points
	 * @param longitude - an array of longitude points
	 * 
	 **/
	public FilledBoundary(String name, Color color, float[] latitude, float[] longitude)
	{
		this();
		setName(name);
		setColor(color);
		setCoordinates(latitude, longitude);
	}
	public FilledBoundary(String name, Color color, float[] latitude, float[] longitude, float width)
	{
		this();
		setName(name);
		setColor(color);
		setCoordinates(latitude, longitude);
		setWidth(lineWidth);
	}
	
	public vtkActor getActor()
	{
		return segmentActors;
	}
	/**
	 * 
	 * Constructs the line array.
	 * 
	 * @author punihaol
	 * @param void
	 * @return void
	 * 
	 **/
	public void addSegment(LocationList locList)
	{
		addSegment(locList, false);
	}
	
	/**
	 * 
	 * Constructs the line array.
	 * 
	 * @author punihaol
	 * @param void
	 * @return void
	 * 
	 **/
	public void addSegment(LocationList locList, boolean close)
	{
		int size = locList.size();
		boolean addFirst = false;
		if (!locList.get(0).equals(locList.get(locList.size()-1)) && close) {
			addFirst = true;
			size++;
		}
		float[] latitude=new float[size];
		float[] longitude=new float[size];
		
		for(int i=0; i<locList.size();i++)
		{
			latitude[i]=(float) locList.get(i).getLatitude();
			longitude[i]=(float) locList.get(i).getLongitude();
		}
		if (addFirst) {
			latitude[size-1] = (float)locList.get(0).getLatitude();
			longitude[size-1] = (float)locList.get(0).getLongitude();
		}
		addSegment(latitude,longitude);
	}
	
	public void addSegment(float[] latitude, float[] longitude)
	{
		setCoordinates(latitude, longitude);
		addSegment();
	}
	
	
	@SuppressWarnings("unused")
	//keep track of all segments
	public void addSegment()
	{
		int vertices = latitude.length;
		
		
		if(!(vertices == 1 || vertices == 2)){

		vtkPoints pts = new vtkPoints();
		vtkPolygon polygon = new vtkPolygon();
		//polyLine.GetPointIds().SetNumberOfIds(vertices);
		  polygon.GetPointIds().SetNumberOfIds(vertices); //make a quad
		  //polygon.GetPointIds().SetId(0, 0);
		for (int i = 0; i < vertices; i++)
		{
			//requires altitude to fix clipping issues
			pts.InsertNextPoint(Transform.transformLatLonHeight(latitude[i], longitude[i],0));
			polygon.GetPointIds().SetId(i, i);
		}
		vtkCellArray lines = new vtkCellArray();
		lines.InsertNextCell(polygon);
		vtkPolyData polydata = new vtkPolyData();
		polydata.SetPolys(lines);
		polydata.SetPoints(pts);
		
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.SetInputData(polydata);
//		
		vtkActor segmentActor = new vtkActor();
		segmentActor.SetMapper(mapper);
		segmentActor.GetProperty().SetEdgeColor(1,1,1);
		segmentActor.GetProperty().SetEdgeVisibility(1);
		this.segmentActors = segmentActor;
		}
	
	}
	
	public void updateWidth(float newWidth) {
		lineWidth=newWidth;
	}
	
	/**
	 * 
	 * Displays the boundary in the 3D world.
	 * 
	 * @author punihaol
	 * @param void
	 * @return void
	 * 
	 **/
	private void draw()
	{
		bDisplayed=true;
	}
	
	/**
	 * 
	 * Undisplays the boundary in the 3D world.
	 * 
	 * @author punihaol
	 * @param void
	 * @return void
	 * 
	 **/
	private void hide()
	{
		
		bDisplayed=false;
	}
	
//	
// Getters and Setters
//
	/**
	 * 
	 * Sets the name of the boundary.
	 * 
	 * @author punihaol
	 * @param name - the name of the boundary
	 * @return void
	 * 
	 **/
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * 
	 * Returns the name of the boundary.
	 * 
	 * @author punihaol
	 * @param void
	 * @return String
	 * 
	 **/
	public String getName()
	{
		return name;
	}
	
	/**
	 * 
	 * Sets the color of the boundary.
	 * 
	 * @author punihaol
	 * @param color - the color of the boundary
	 * @return void
	 * 
	 **/
	public void setColor(Color inColor)
	{
		
		color = inColor;
		if(segmentActors!=null){
			//System.out.println(color.getRed()+","+color.getGreen()+","+color.getBlue());
		segmentActors.GetProperty().SetColor(color.getRed()/255.0,color.getGreen()/255.0,color.getBlue()/255.0);
		segmentActors.Modified();
		}
	}
	
	public void setWidth(float width) {
		this.lineWidth=width;
	}
	
	/**
	 * 
	 * Returns the color of the boundary.
	 * 
	 * @author punihaol
	 * @param void
	 * @return Color
	 * 
	 **/
	public Color getColor()
	{
		return color;
	}
	
	/**
	 * 
	 * Sets the latitude and longitude points of the boundary.
	 * 
	 * @author punihaol
	 * @param latitude - an array of latitude points
	 * @param longitude - an array of longitude points
	 * @return void
	 * 
	 **/
	public void setCoordinates(float[] latitude, float[] longitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		
	}
	
	/**
	 * 
	 * Returns the latitude points of the boundary.
	 * 
	 * @author punihaol
	 * @param void
	 * @return float[]
	 * 
	 **/
	public float[] getLatitude()
	{
		return latitude;
	}
	
	/**
	 * 
	 * Returns the longitude points of the boundary
	 * 
	 * @author punihaol
	 * @param void
	 * @return float[]
	 * 
	 **/
	public float[] getLongitude()
	{
		return longitude;
	}
	
	/**
	 * 
	 * Returns true if the boundary is being drawn.
	 * 
	 * @author punihaol
	 * @param void
	 * @return boolean
	 * 
	 **/
	public boolean isDisplayed()
	{
		return bDisplayed;
	}
	
	
	public String getInfo()
	{
		String lines;
		lines = "The " + name + " Plate:";
		
		return lines;
	}
	
	public void setDisplayed(boolean active) 
	{
		bDisplayed=active;
		if(segmentActors!=null)
		{
			if(active)
				segmentActors.SetVisibility(1);
			else
				segmentActors.SetVisibility(0);
			
		}
	}

		public void setTransparency(float transparency) {
//	          for(Shape3D segment:Shapes){
//	        	  TransparencyAttributes ta = segment.getAppearance().getTransparencyAttributes();
//	              ta.setTransparency(transparency);
//	          }
		}

		public void setLineApperance(Color color2, float value) {
			// TODO Auto-generated method stub
			
		}

}


