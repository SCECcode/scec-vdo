package org.scec.vtk.plugins.ShakeMapPlugin.Component;

public class EQDot {

	private double lat; //latitude
	private double lng; //longitude
	private double mag; //magnitude
	private double alpha;

	public EQDot(double lat, double lng, double mag, double alpha) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.mag = mag;
		this.alpha = alpha;
	}

	public double getLat() {
		return lat;
	}


	public double getLng() {
		return lng;
	}

	public double getMag() {
		return mag;
	}

	public double getAlpha(){
		return alpha;
	}

}
