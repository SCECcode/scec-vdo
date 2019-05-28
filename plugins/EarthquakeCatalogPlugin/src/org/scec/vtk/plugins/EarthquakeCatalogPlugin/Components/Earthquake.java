package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.util.Date;

import vtk.vtkActor;

public class Earthquake {

	private Double latMinField = 0.0;
	private Double lonMinField = 0.0;
	private Double depMinField = 0.0;
	private Double magMinField = 0.0;
	private String dateStartField = new String("yyyy/mm/dd");
	private String dateEndField   = new String("yyyy/mm/dd");
	Date time;
	private vtkActor earthquakeActor;

	public Earthquake(double depth,double mag,double lat,double lon,Date time,int limit)
	{
		this.depMinField = depth;
		this.magMinField = mag;
		this.latMinField = lat;
		this.lonMinField = lon;
		//this.dateStartField = startTime;
		this.time = time;
		//this.dateEndField = endTime;
	}

	public Earthquake(double eq_depth, double eq_magnitude, double eq_latitude, double eq_longitude, Date eq_time) {
		this.depMinField = eq_depth;
		this.magMinField = eq_magnitude;
		this.latMinField = eq_latitude;
		this.lonMinField = eq_longitude;
		//this.dateStartField = eq_time.toString();
		this.time = eq_time;
		// this.earthquakeActor = earthquakeActor;
	}

	public vtkActor getEarthquakeCatalogActor()
	{
		return this.earthquakeActor;
	}

	public double getMag() {
		// TODO Auto-generated method stub
		return magMinField;
	}

	public double getEq_latitude() {
		// TODO Auto-generated method stub
		return latMinField;
	}

	public double getEq_longitude() {
		// TODO Auto-generated method stub
		return lonMinField;
	}

	public double getEq_depth() {
		// TODO Auto-generated method stub
		return depMinField;
	}

	public double getEq_magnitude() {
		// TODO Auto-generated method stub
		return magMinField;
	}
	public Date getEq_time() {
		// TODO Auto-generated method stub
		return time;
	}
}
