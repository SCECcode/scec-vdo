package org.scec.vtk.plugins.GISHazusEventsPlugin;

import org.scec.vtk.plugins.SurfacePlugin.GeographicSurfaceInfo;

/**
 * @author scottcal
 */
public class ImageInfo {
	private String filename;
	private double[] upperLeft;
	private double[] lowerRight;
	private boolean meshType;
	private float transparency;
	private GeographicSurfaceInfo attachedSurface;
	
	public ImageInfo(String f, double[] ul, double[] lr, boolean mt) {
		filename = f;
		upperLeft = ul;
		lowerRight = lr;
		meshType = mt;
		if (mt == true) // default values
			transparency = 0.0f;
		else
			transparency = 0.4f;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public double[] getUpperLeft() {
		return upperLeft;
	}
	
	public double[] getLowerRight() {
		return lowerRight;
	}
	
	public boolean getMeshType() {
		return meshType;
	}

	public GeographicSurfaceInfo getAttachedSurface() {
		return attachedSurface;
	}

	public void setAttachedSurface(GeographicSurfaceInfo attachedSurface) {
		this.attachedSurface = attachedSurface;
	}

	public float getTransparency() {
		return transparency;
	}

	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}
	

}
