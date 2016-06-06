package org.scec.vtk.plugins.SurfacePlugin.Component;

public class GoogleStaticMapsURLGenerator {
	
	public static String API_KEY = "ABQIAAAA4f8vvgle4-eShjZCjrGuzRSZbwByw_b0gEnSl-UQR_XHRcIqhBSvL_l7IdhR_qdoiIAzcAZsi966Xw";
	
	public static String toURL(double latS, double latN, double lonE, double lonW, int pixelsPerDegree) {
		String url = "http://maps.google.com/maps/api/staticmap?";
		
		double minLat, maxLat, minLon, maxLon;
		if (latS > latN) {
			minLat = latN;
			maxLat = latS;
		} else {
			minLat = latS;
			maxLat = latN;
		}
		if (lonW > lonE) {
			minLon = lonE;
			maxLon = lonW;
		} else {
			minLon = lonW;
			maxLon = lonE;
		}
		
		double centerLat = 0.5 * (minLat + maxLat);
		double centerLon = 0.5 * (minLon + maxLon);
		
		url += "center=" + centerLat + "," + centerLon;
		
		double latSpan = maxLat - minLat;
		double lonSpan = maxLon - minLon;
		
		//url+="&visible="+minLat+","+maxLon+"|"+maxLat+","+minLon;		
		
		double SCALING_CONSTANT=.35;
		
		url+="&visible="+(centerLat-(SCALING_CONSTANT*latSpan))+","+(centerLon+(SCALING_CONSTANT*lonSpan))+"|"+(centerLat+(SCALING_CONSTANT*latSpan))+","+(centerLon-(SCALING_CONSTANT*lonSpan));
//		
//		url += "&span=" + latSpan + "," + lonSpan;
		
		int width =(int)(pixelsPerDegree * lonSpan * 0.5);
		int height =(int)(pixelsPerDegree * latSpan * 0.5);
		
		url += "&size=" + width + "x" + height;
		
		url += "&format=jpg";
		
		url += "&maptype=roadmap";
		
		url += "&sensor=false";
//		url += "&key=" + API_KEY;
		
		return url;
	}

}
