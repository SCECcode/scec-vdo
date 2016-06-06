package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.util.ArrayList;

import vtk.vtkActor;

public class Earthquake {

	private Double latMinField = 0.0;
	private Double latMaxField = 0.0;
	private Double lonMinField = 0.0;
	private Double lonMaxField = 0.0;
	private Double depMinField = 0.0;
	private Double depMaxField = 0.0;
	private Double magMinField = 0.0;
	private Double magMaxField = 0.0;
	private String dateStartField = new String("yyyy/mm/dd");
	private String dateEndField   = new String("yyyy/mm/dd");
	private int maxEventsField = 0;
	private vtkActor earthquakeActor;
	
	public Earthquake(double depth,double mag,double lat,double lon,String startTime,
			String endTime,int limit,vtkActor earthquakeActor)
	{
		 /* this.latMinField = minLat;
		  this.latMaxField = maxLat;
		  this.lonMinField = minLon;
		  this.lonMaxField = maxLon;
		  this.depMinField = minDepth;
		  this.depMaxField = maxDepth;
		  this.magMinField = minMagnitude;
		  this. magMaxField = maxMagnitude;*/
		this.depMinField = depth;
		  this.magMinField = mag;
		this.latMinField = lat;
		this.lonMinField = lon;
		  this.dateStartField = startTime;
		  this.dateEndField = endTime;
		  this.maxEventsField = limit;
		  this.earthquakeActor = earthquakeActor;
	}
	
	public vtkActor getEarthquakeCatalogActor()
	{
		return this.earthquakeActor;
	}

	public double getMag() {
		// TODO Auto-generated method stub
		return magMinField;
	}
}
