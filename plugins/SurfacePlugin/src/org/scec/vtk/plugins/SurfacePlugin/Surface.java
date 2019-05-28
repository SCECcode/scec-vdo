package org.scec.vtk.plugins.SurfacePlugin;

import vtk.vtkActor;
import org.scec.vtk.plugins.utils.AbstractDataAccessor;


public class Surface extends AbstractDataAccessor{
	ImageInfo imageInfo = new ImageInfo(null, null, null, null, false);
	GeographicSurfaceInfo geoSurfaceInfo = new GeographicSurfaceInfo();
	vtkActor surfaceActor = new vtkActor();
	boolean displayed = true;
	private int visibility = 1;

	Surface(ImageInfo imageInfo, GeographicSurfaceInfo geoSurfaceInfo,vtkActor surfaceActor)
	{
		this.imageInfo = imageInfo;
		this.geoSurfaceInfo = geoSurfaceInfo;
		this.surfaceActor = surfaceActor;
	}
	
	public ImageInfo getImageInfo()
	{
		return imageInfo;
	}
	
	public GeographicSurfaceInfo getGeoSurfaceInfo()
	{
		return geoSurfaceInfo;
	}
	
	public vtkActor getSurfaceActor()
	{
		return surfaceActor;
	}
	
	public Object[] createRow() {
		String geoSurfaceFileName;
		String imageSurfaceFileName;
		if(geoSurfaceInfo==null)
			geoSurfaceFileName="-";
		else
			geoSurfaceFileName = geoSurfaceInfo.getFilename(); 

		String data = new String();
		String file = new String();
		//get only the surface file name
		file = geoSurfaceFileName;
		int begin;
		int end;
		if(file.equalsIgnoreCase("-")){
			data = "-";
		}
		else{
			if(file.contains("\\")) {
				begin = file.lastIndexOf("\\") + 1;
			}
			else {
				begin = file.lastIndexOf("/") + 1;
			}
			end = file.length()-1;
			if(file.endsWith(".txt")){
				end = file.indexOf(".txt");
			}
			else if(file.endsWith(".dem")){
				end = file.indexOf(".dem");
			}
			data = file.substring(begin,end);
		}
		geoSurfaceFileName = data;
		//get only the image file name 
		file = imageInfo.getFilename();
		if(file.equalsIgnoreCase("-")){
			data = "-";
		}
		else{
			if(file.contains("\\")) {
				begin = file.lastIndexOf("\\") + 1;
			}
			else {
				begin = file.lastIndexOf("/") + 1;
			}
			end = file.length() - 1;
			if(file.endsWith(".png")){
				end = file.indexOf(".png");
			}
			else if(file.endsWith(".jpg")){
				end = file.indexOf(".jpg");
			}
			else if(file.endsWith(".jpeg")){
				end = file.indexOf(".jpeg");
			}
			data = file.substring(begin,end);
		}
		imageSurfaceFileName = data;

		Object[] newRow = {	displayed,
				imageSurfaceFileName, 
				geoSurfaceFileName
		};
		return newRow;
	}
	
	public int getVisibility()
	{
		return visibility;
	}

	public void setVisibility(int visible)
	{
		visibility = visible;
	}

}
