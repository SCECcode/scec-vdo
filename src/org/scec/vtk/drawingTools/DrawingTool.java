package org.scec.vtk.drawingTools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import org.scec.vtk.plugins.utils.AbstractDataAccessor;

import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkObject;
import vtk.vtkProp;

public class DrawingTool extends AbstractDataAccessor{

	private static ArrayList<vtkObject> masterDrawingToolBranchGroup = new ArrayList<vtkObject>();
	double latitude, longitude,  altitude;
	String textString;
	DisplayAttributes displayAttributes;
	vtkActor actorPin = new vtkActor();
	vtkActor2D actorText = new vtkActor2D();
	private Color color;
	HashMap<String, String> attributesData= new HashMap<>();
	public DrawingTool(double latitude, double longitude, double altitude, String textString, 
			DisplayAttributes displayAttributes,Color color,vtkActor actorPin,vtkActor2D actor) {
		// TODO Auto-generated constructor stub
		this.latitude=latitude;
		this.longitude=longitude;
		this.altitude = altitude;
		this.textString = textString;
		this.displayAttributes = displayAttributes;
		this.actorPin = actorPin;
		this.actorText = actor;
		this.color = color;
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
	public void setTextString(String s){
		this.textString = s;
	}
	public DisplayAttributes getDisplayAttributes()
	{
		return this.displayAttributes;
	}
	public Color getColor() {
		// TODO Auto-generated method stub
		return color ;
	}
	public void setColor(Color col) {
		// TODO Auto-generated method stub
		 color = col ;
	}
	public void setActors(vtkActor actorPin2, vtkActor2D actor) {
		// TODO Auto-generated method stub
		this.actorPin =actorPin2;
	
		this.actorText = actor;
	}
	public vtkProp getActorPin() {
		// TODO Auto-generated method stub
		return actorPin;
	}
	public vtkProp getActorText() {
		// TODO Auto-generated method stub
		return actorText;
	}
	public void setAttributes(HashMap<String, String> locData) {
		// TODO Auto-generated method stub
		attributesData =  locData;
	}
	public HashMap<String, String> getAttributes() {
		// TODO Auto-generated method stub
		return attributesData;
	}

}
