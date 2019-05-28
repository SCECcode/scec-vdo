package org.scec.vtk.plugins.GISHazusEventsPlugin;

import java.awt.Color;
import java.util.ArrayList;

import org.scec.vtk.tools.actors.AppendActors;

import vtk.vtkActor;



public class FilledBoundaryCluster {
	private String name;
	private int category = 0;
	private ArrayList<FilledBoundary> boundaries;
	private ArrayList<Boundary> boundaries2;
	private Color color = new Color(1.0f,1.0f,1.0f);
	private boolean bDisplayed=false;
	private AppendActors segmentActors;
	public FilledBoundaryCluster(AppendActors append)
	{
		init(append);
	}
	
	public FilledBoundaryCluster() {
		init(null);
	}
	public void init(AppendActors append)
	{
		boundaries=new ArrayList<FilledBoundary>();
		boundaries2 =new ArrayList<Boundary>(); 
		name="";
		segmentActors =append;
	}
	public vtkActor addSegment(float[] latitude, float[] longitude)
	{
		FilledBoundary currentBoundary= new FilledBoundary();
		//Boundary currentBoundary2= new Boundary();
		currentBoundary.setCoordinates(latitude, longitude);
		currentBoundary.addSegment();
//		currentBoundary2.setCoordinates(latitude, longitude);
//		currentBoundary2.addSegment();
		boundaries.add(currentBoundary);
		//boundaries2.add(currentBoundary2);
		if(currentBoundary.getActor()!=null){
			segmentActors.addToAppendedPolyData(currentBoundary.getActor());
			return currentBoundary.getActor();
		}
		return null;
	}

	public ArrayList<FilledBoundary> getBoundaries(){
		return boundaries;
	}
	public ArrayList<Boundary> getBoundaries2(){
		return boundaries2;
	}
	public Color getColor()
	{
		return color;
	}
	
	public void setColor(Color inColor)
	{
		color=inColor;
		for(FilledBoundary b : boundaries)
		{
			b.setColor(inColor);
		}
	}
	
	public boolean isDisplayed()
	{
		return bDisplayed;
	}
	
	public void setDisplayed(boolean active) 
	{
		bDisplayed=active;
		for(FilledBoundary b : boundaries)
		{
			b.setDisplayed(active);
		}
	}
	
	public void setName(String inName)
	{
		name=inName;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getCategory() {
		return category;
	}
	
	public void setCategory(int c) {
		category = c;
	}

	public AppendActors getAppendActor() {
		// TODO Auto-generated method stub
		
		 
		//System.out.println(segmentActors.getAppendedActor().GetParts().GetNumberOfItems());//.GetOutput().GetNumberOfPoints());
		return segmentActors;
	}

	public void setBoundaries(ArrayList<FilledBoundary> arrayList) {
		boundaries = arrayList;
		
	}

	public void setBoundaries2(ArrayList<Boundary> arrayList) {
		boundaries2 = arrayList;
		
	}

	public void setAppendActor(AppendActors appendActors) {
		segmentActors = appendActors;
		
	}

}

