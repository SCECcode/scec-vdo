package org.scec.vtk.plugins.SurfacePlugin.Component;

/**
 * A class that represents a WMS mapping service. 
 * See http://en.wikipedia.org/wiki/Web_Map_Service for more information.
 * @author joshy, milner
 */
public class WMSUrlGenerator {
	private String baseUrl;
	private String layer;
	private String style = "";
	/** Creates a new instance of WMSService */

	public WMSUrlGenerator(String baseUrl, String layer) {
		this(baseUrl, layer, "");
	}
	
	public WMSUrlGenerator(String baseUrl, String layer, String style) {
		this.baseUrl = baseUrl;
		this.layer = layer;
		this.style = style;
	}

	public String toWMSURL(double latS, double latN, double lonE, double lonW, int pixelsPerDegree) {
		String format = "image/jpeg";
		String styles = this.style;
		String srs = "EPSG:4326";	//JR: Coordinate system specification eg. lat, long
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

		int width = (int)((maxLon - minLon) * (double)pixelsPerDegree + .5);
		int height = (int)((maxLat - minLat) * (double)pixelsPerDegree + .5);

		String bbox = minLon + "," + minLat +"," + maxLon + "," + maxLat;
		String url = getBaseUrl() + 
		"version=1.1.1&request="+
		"GetMap&Layers="+layer+
		"&format="+format+
		"&BBOX="+bbox+
		"&width="+width+"&height="+height+
		"&SRS="+srs;
		if (styles == null)
			styles = "";
//		if (styles != null && !styles.isEmpty())
			url += "&Styles="+styles;
		//"&transparent=TRUE"+
		return url;
	}

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}