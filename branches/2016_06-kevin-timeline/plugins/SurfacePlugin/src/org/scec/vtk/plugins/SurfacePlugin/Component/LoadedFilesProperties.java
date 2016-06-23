package org.scec.vtk.plugins.SurfacePlugin.Component;

import org.scec.vtk.plugins.SurfacePlugin.GeographicSurfaceInfo;
import org.scec.vtk.plugins.SurfacePlugin.ImageInfo;

public class LoadedFilesProperties{
	private String imageFilePath;
	private String surfaceFilePath;
	private String setFilePath;
	private String xmlFilePath;
	private double[] imageCoordinates = new double[5];
	private double[] surfaceCoordinates = new double[5];
	private ImageInfo imgInfo;
	private GeographicSurfaceInfo geoInfo;
	//private MapSetInfo mapSetInfo;
	private boolean show;
	private boolean haveSet;
	private boolean plot;
	
	public LoadedFilesProperties(String imageFilePath, double[] imageCoordinates, 
			String surfaceFilePath, double[] surfaceCoordinates, 
			String setFilePath, boolean haveSet, String xmlFilePath)
	{
		this.imageFilePath = imageFilePath;
		this.surfaceFilePath = surfaceFilePath;
		this.imageCoordinates = imageCoordinates;
		this.surfaceCoordinates = surfaceCoordinates;
		this.xmlFilePath = xmlFilePath;
		this.setFilePath = setFilePath;
		this.show = false;
		this.plot = false;
		this.haveSet = haveSet;
	}
	/*public LoadedFilesProperties(GeographicSurfaceInfo geoInfo, ImageInfo imgInfo, MapSetInfo mapSetInfo, String xmlFilePath){
		this.geoInfo = geoInfo;
		this.imgInfo = imgInfo;
		//this.mapSetInfo = mapSetInfo;
		this.xmlFilePath = xmlFilePath;
	}*/
	
	public void setShow(boolean show){
		this.show = show;
	}
	
	public void setHaveSet(boolean haveSet){
		this.haveSet = haveSet;
	}
	public void addImageInfo(ImageInfo imgInfo){
		this.imgInfo  = imgInfo;
	}
	public void addGeographicSurfaceInfo(GeographicSurfaceInfo geoInfo){
		this.geoInfo = geoInfo;
	}
//	public void addMapSetInfo(MapSetInfo setInfo){
//		this.setInfo = setInfo;
//	}
	/*public MapSetInfo getMapSetInfo(){
		return mapSetInfo;
	}*/
	public GeographicSurfaceInfo getGeoInfo(){
		return this.geoInfo;
	}
	public ImageInfo getImgInfo(){
		return this.imgInfo;
	}
	public void setPlot(boolean plot){
		this.plot = plot;
	}
	public void setImageCoordinates(double[] coords){
		imageCoordinates = coords;
	}
	public void setSurfaceCoordinates(double[] coords){
		surfaceCoordinates = coords;
	}
	public boolean getPlot(){
		return this.plot;
	}
	public String getImageFilePath(){
		return this.imageFilePath;
	}
	public String getXmlFilePath(){
		return this.xmlFilePath;
	}
	public String getSurfaceFilePath(){
		return this.surfaceFilePath;
	}
	
	public double[] getImageCoordinates(){
		return this.imageCoordinates;
	}
	
	public double[] getSurfaceCoordinates(){
		return this.surfaceCoordinates;
	}
	
	public String getSetFilePath(){
		return this.setFilePath;
	}
	
	public boolean getShow(){
		return show;
	}
	
	public boolean getHaveSet(){
		return haveSet;
	}
	public ImageInfo getImageInfo(){
		return imgInfo;
	}
	
	
};

