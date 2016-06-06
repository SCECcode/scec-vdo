package org.scec.vtk.plugins.SurfacePlugin;


import vtk.vtkTriangleStrip;
/*
 * Created on Jul 12, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */


/**
 * @author scottcal
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class GeographicSurfaceInfo {
	private double[] upperRight;
	private double[] lowerLeft;
	private double horizontalSteps, verticalSteps;
	private vtkTriangleStrip data;
	//private Shape3D shapeData;
	private String filename;
	//private BranchGroup bg;
	private ImageInfo image;
	
	public GeographicSurfaceInfo() {
		upperRight = new double[3];
		lowerLeft = new double[3];
	}
	
	public GeographicSurfaceInfo(String s, double[] ul, double[] lr, double hs, double vs, vtkTriangleStrip d) {
		setCorners(ul,lr);
		setHorizSteps(hs);
		setVertSteps(vs);
		setData(d);
		filename = s;
	}
	
	public GeographicSurfaceInfo(String s, double[] ul, double[] lr) {
		setCorners(ul,lr);
		filename = s;
	}
	
	public void setCorners(double[] ul, double[] lr) {
		upperRight = ul;
		lowerLeft = lr;
	}
	
	public double[] getUpperRight() {
		return upperRight;
	}
	
	public double[] getLowerLeft() {
		return lowerLeft;
	}

	public void setHorizSteps(double h) {
		horizontalSteps = h;
	}

	public double getHorizSteps() {
		return horizontalSteps;
	}
	
	public void setVertSteps(double v) {
		verticalSteps = v;
	}
	
	public double getVertSteps() {
		return verticalSteps;
	}
	
	public void setData(vtkTriangleStrip d) {
		data = d;
	}
	
	public vtkTriangleStrip getData() {
		return data;
	}

	public String getFilename() {
		return filename;
	}
	
	/*public void setBranchGroup(BranchGroup b) {
		bg = b;
	}
	
	public BranchGroup getBranchGroup() {
		return bg;
	}*/

	public void setImage(ImageInfo i) {
		image = i;
	}
	
	public ImageInfo getImage() {
		return image;
	}

}
