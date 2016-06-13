package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.util.ArrayList;
import java.util.Date;

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
			String endTime,int limit)
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
	}
	
	public Earthquake(double eq_depth, double eq_magnitude, double eq_latitude, double eq_longitude, Date eq_time) {
		this.depMinField = eq_depth;
		  this.magMinField = eq_magnitude;
		this.latMinField = eq_latitude;
		this.lonMinField = eq_longitude;
		  this.dateStartField = eq_time.toString();
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

	public double getEq_latitude(int i) {
		// TODO Auto-generated method stub
		return latMinField;
	}

	public double getEq_longitude(int i) {
		// TODO Auto-generated method stub
		return lonMinField;
	}

	public double getEq_depth(int i) {
		// TODO Auto-generated method stub
		return depMinField;
	}

	public double getEq_magnitude(int i) {
		// TODO Auto-generated method stub
		return magMinField;
	}
}
