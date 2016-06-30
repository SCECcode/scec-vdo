package org.scec.vtk.tools;

import javax.swing.SwingUtilities;

import org.scec.vtk.main.MainGUI;

public class Transform {
	public static final double re = 6378.140;				// equatorial radius
	public static final double rp = 6356.755;				// polar radius
	private static final double PIBy2 = Math.PI/2;

	public static double calcRadius(double lat) {
		double radius =
				re * Math.pow((1 + (((re * re - rp * rp) / (rp * rp))
						* (Math.sin(Math.toRadians(lat)) * Math.sin(Math.toRadians(lat))))),-0.5);
		return radius;
	}

	public static double[] transformLatLonHeight(double lat, double lon, double height) {
		return Transform.customTransform(new double[]{Transform.calcRadius(lat)+height, lat, lon});
	}

	public static double[] transformLatLon(double lat, double lon) {
		return Transform.customTransform(new double[]{Transform.calcRadius(lat), lat, lon});
	}

	public static double[] customTransform(double[] latlon)
	{

		double phi = latlon[1];
		double theta = latlon[2];

		// Clamp to lat/long bounds
		theta = (theta >  180.0) ?  180.0 : theta;
		theta = (theta < -180.0) ? -180.0 : theta;
		phi = (phi >  90.0) ?  90.0 : phi;
		phi = (phi < -90.0) ? -90.0 : phi;


		// Lets keep this conversion code in a single place.
		double tmp = Math.cos(Math.toRadians( phi ) );
		double n0 = -tmp * Math.sin( Math.toRadians( theta ) );
		double n1 = tmp * Math.cos( Math.toRadians( theta ) );
		double n2 = Math.sin( Math.toRadians( phi ) );

		double[] x = new double[3];
		x[0] = n0 * latlon[0];
		x[1] = n1 * latlon[0];
		x[2] = n2 * latlon[0];


		return x;

	}

	public static double[] WorldPointToLatLon(double[] x){

		double[] latLon= new double[2];
		double rho = Math.sqrt(x[0]*x[0] + x[1]*x[1] + x[2]*x[2]);
		double S = Math.sqrt(x[0]*x[0] + x[1]*x[1]);
		double phi = Math.acos(x[2] / rho);
		double theta =0;
		if (x[0] >= 0)
		{
			theta = Math.asin(x[1] / S);
		}
		else
		{
			theta = Math.PI - Math.asin(x[1] / S);
		}
		phi =  Math.toDegrees(Math.PI / 2.0 - phi );
		theta =  Math.toDegrees( theta - Math.PI/2.0 );
		latLon[0]=  Math.round(phi);
		latLon[1] =  Math.round(theta);
		return latLon;
	}
}
