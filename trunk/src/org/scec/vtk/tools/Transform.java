package org.scec.vtk.tools;

import javax.swing.SwingUtilities;

import org.scec.vtk.main.MainGUI;

public class Transform {
    public static final double re = 6378.140;				// equatorial radius
	public static final double rp = 6356.755;				// polar radius
	
	public static double calcRadius(double lat) {
		double radius =
			re * Math.pow((1 + (((re * re - rp * rp) / (rp * rp))
							* (Math.sin(Math.toRadians(lat)) * Math.sin(Math.toRadians(lat))))),-0.5);
		return radius;
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

    //transformed point to earth's radius
    double[] x = new double[3];
    x[0] = n0 * latlon[0];
    x[1] = n1 * latlon[0];
    x[2] = n2 * latlon[0];
	
    return x;
	
}
}
