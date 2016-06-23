package org.scec.vtk.drawingTools;

import java.util.ArrayList;

import org.scec.vtk.plugins.utils.AbstractDataAccessor;

import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkObject;

public class DrawingTool extends AbstractDataAccessor{

	private static ArrayList<vtkObject> masterDrawingToolBranchGroup = new ArrayList<vtkObject>();
	double latitude, longitude,  altitude;
	String textString;
	DisplayAttributes displayAttributes;
	
	public DrawingTool(double latitude, double longitude, double altitude, String textString, DisplayAttributes displayAttributes){//,vtkActor actorPin,vtkActor2D actor) {
		// TODO Auto-generated constructor stub
		this.latitude=latitude;
		this.longitude=longitude;
		this.altitude = altitude;
		this.textString = textString;
		this.displayAttributes = displayAttributes;
	}
	public DrawingTool() {
	}

	public double getLatitude()
	{
		return this.latitude;
	}
	public double getLongitude()
	{
		return this.longitude;
	}
	public double getaltitude()
	{
		return this.altitude;
	}
	public String getTextString()
	{
		return this.textString;
	}
	public DisplayAttributes getDisplayAttributes()
	{
		return this.displayAttributes;
	}

}
