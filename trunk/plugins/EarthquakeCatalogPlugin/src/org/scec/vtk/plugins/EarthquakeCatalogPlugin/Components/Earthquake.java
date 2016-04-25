package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

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
	
	public Earthquake(double minDepth,double maxDepth,double minMagnitude,double maxMagnitude,double minLat,double maxLat,double minLon,double maxLon,String startTime,String endTime,int limit)
	{
		  latMinField = minLat;
		  latMaxField = maxLat;
		  lonMinField = minLon;
		  lonMaxField = maxLon;
		  depMinField = minDepth;
		  depMaxField = maxDepth;
		  magMinField = minMagnitude;
		  magMaxField = maxMagnitude;
		  dateStartField = startTime;
		  dateEndField = endTime;
		  maxEventsField = limit;
	}
	
}
