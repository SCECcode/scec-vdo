package org.scec.vtk.plugins.SurfacePlugin;

/*
 * Created on Jul 22, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 * @author scottcal
 */
public class ImageInfo {
	
	private String imageName;
	private String filename;
	private double[] upperLeft;
	private double[] lowerRight;
	private boolean meshType;
	private float transparency;
	//private BranchGroup bg;
	private GeographicSurfaceInfo attachedSurface;
	
	public ImageInfo(String imgName, String f, double[] ul, double[] lr, boolean mt) {
		this.imageName = imgName;
		filename = f;
		upperLeft = ul;
		lowerRight = lr;
		meshType = mt;
		if (mt == true) // default values
			transparency = 0.0f;
		else
			transparency = 0.4f;
	}
	
	public String getImageName()
	{
		return this.imageName;
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

	/*public BranchGroup getBranchGroup() {
		return bg;
	}

	public void setBranchGroup(BranchGroup bg) {
		this.bg = bg;
	}*/

	public float getTransparency() {
		return transparency;
	}

	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}
	

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setUpperLeft(double[] upperLeft) {
		this.upperLeft = upperLeft;
	}

	public void setLowerRight(double[] lowerRight) {
		this.lowerRight = lowerRight;
	}

	public void setMeshType(boolean meshType) {
		this.meshType = meshType;
	}

}
