package org.scec.vtk.plugins.SurfacePlugin.Component;

import org.jdom.Element;

public class LatLonBoundingBox {
	
	double minLat;
	double minLon;
	double maxLat;
	double maxLon;
	
	public LatLonBoundingBox(double minLat, double minLon, double maxLat, double maxLon) {
		if (minLat >= maxLat || minLon >= maxLon) {
			throw new RuntimeException("Lat Lon Bounding Box has zero or negative area!");
		}
		
		this.minLat = minLat;
		this.minLon = minLon;
		this.maxLat = maxLat;
		this.maxLon = maxLon;
	}
	
	public boolean isInside(double lat, double lon) {
		return lat >= minLat && lat <= maxLat && lon >= minLon && lon <= minLon;
	}
	
	public boolean isContained(LatLonBoundingBox box) {
		if (box.getMinLat() < this.getMinLat())
			return false;
		if (box.getMinLon() < this.getMinLon())
			return false;
		if (box.getMaxLat() > this.getMaxLat())
			return false;
		if (box.getMinLon() > this.getMinLon())
			return false;
		return true;
	}
	
	public boolean isContained(double minLat, double minLon, double maxLat, double maxLon) {
		
		return this.isContained(new LatLonBoundingBox(minLat, minLon, maxLat, maxLon));
	}

	public double getMinLat() {
		return minLat;
	}

	public void setMinLat(double minLat) {
		this.minLat = minLat;
	}

	public double getMinLon() {
		return minLon;
	}

	public void setMinLon(double minLon) {
		this.minLon = minLon;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public void setMaxLat(double maxLat) {
		this.maxLat = maxLat;
	}

	public double getMaxLon() {
		return maxLon;
	}

	public void setMaxLon(double maxLon) {
		this.maxLon = maxLon;
	}
	
	public String toString() {
		return "MinLat: " + this.getMinLat() + " MinLon: " + this.getMinLon() +
			" MaxLat: " + this.getMaxLat() + " MaxLon: " + this.getMaxLon();
	}
	
	public static LatLonBoundingBox fromXML(Element box) {
		double minLat = Double.parseDouble(box.getAttributeValue("miny"));
		double minLon = Double.parseDouble(box.getAttributeValue("minx"));
		double maxLat = Double.parseDouble(box.getAttributeValue("maxy"));
		double maxLon = Double.parseDouble(box.getAttributeValue("maxx"));
		
		return new LatLonBoundingBox(minLat, minLon, maxLat, maxLon);
	}
}
