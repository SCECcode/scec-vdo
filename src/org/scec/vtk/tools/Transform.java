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
						* (Math.pow(Math.sin(Math.toRadians(lat)), 2)))),-0.5);
		return radius;
	}

	/** Project a scene point radially onto the zero-elevation Earth ellipsoid. The origin has no projection. */
	public static double[] projectToEarthSurface(double[] point) {
		double radius = scaledRadius(point);
		if (!(radius > 0d) || !Double.isFinite(radius))
			return null;
		return new double[] { point[0] / radius, point[1] / radius, point[2] / radius };
	}

	/** Return the first forward intersection of the ray from origin through target, or null if it misses. */
	public static double[] intersectEarthSurface(double[] origin, double[] target) {
		// In these coordinates the ellipsoid is a unit sphere.
		double x = origin[0] / re;
		double y = origin[1] / re;
		double z = origin[2] / rp;
		double dx = (target[0] - origin[0]) / re;
		double dy = (target[1] - origin[1]) / re;
		double dz = (target[2] - origin[2]) / rp;
		double length = Math.hypot(Math.hypot(dx, dy), dz);
		if (!(length > 0d) || !Double.isFinite(length))
			return null;
		dx /= length;
		dy /= length;
		dz /= length;

		// Find the closest point on the ray's line. The intersections are equidistant from it.
		double closest = -(x * dx + y * dy + z * dz);
		double crossX = y * dz - z * dy;
		double crossY = z * dx - x * dz;
		double crossZ = x * dy - y * dx;
		double halfChordSquared = 1d - (crossX * crossX + crossY * crossY + crossZ * crossZ);
		// Allow roundoff at a tangent. Skip hits at the camera itself to keep a usable view direction.
		if (!Double.isFinite(halfChordSquared) || halfChordSquared < -1e-14)
			return null;
		double halfChord = Math.sqrt(Math.max(0d, halfChordSquared));
		double distance = closest - halfChord;
		if (distance <= 1e-12)
			distance = closest + halfChord;
		if (!(distance > 1e-12) || !Double.isFinite(distance))
			return null;
		return new double[] { (x + distance * dx) * re, (y + distance * dy) * re, (z + distance * dz) * rp };
	}

	static boolean isInsideEarth(double[] point) {
		return scaledRadius(point) < 1d - 1e-12;
	}

	private static double scaledRadius(double[] point) {
		return Math.hypot(Math.hypot(point[0] / re, point[1] / re), point[2] / rp);
	}

	public static double[] transformLatLonHeight(double lat, double lon, double height) {
		return Transform.customTransform(new double[]{Transform.calcRadius(lat)+height, lat, lon});
	}

	public static double[] transformLatLon(double lat, double lon) {
		return Transform.customTransform(new double[]{Transform.calcRadius(lat), lat, lon});
	}

	public static double[] customTransform(double[] latlon)
	{

		double phi = latlon[1]; // lat
		double theta = latlon[2]; // lon

		// Clamp to lat/long bounds
//		theta = (theta >  180.0) ?  180.0 : theta;
//		theta = (theta < -180.0) ? -180.0 : theta;
		while (theta < -180)
			theta += 360;
		while (theta > 180)
			theta -= 360;
		phi = (phi >  90.0) ?  90.0 : phi;
		phi = (phi < -90.0) ? -90.0 : phi;
		
		double thetaRad = Math.toRadians(theta);
		double phiRad = Math.toRadians(phi);

		// Lets keep this conversion code in a single place.
		double tmp = Math.cos(phiRad);
		double n0 = -tmp * Math.sin(thetaRad);
		double n1 = tmp * Math.cos(thetaRad);
		double n2 = Math.sin(phiRad);

		double[] x = new double[3];
		x[0] = n0 * latlon[0];
		x[1] = n1 * latlon[0];
		x[2] = n2 * latlon[0];


		return x;

	}

	public static double[] WorldPointToLatLonHeight(double[] x){

		double[] latLon= new double[3];
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
		double radius = Transform.calcRadius(latLon[0]);
		double height =  rho - radius;
		latLon[2] = height;
		return latLon;
	}
}
