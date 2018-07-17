package org.scec.vtk.plugins.ShakeMapPlugin.Component;

import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.scec.vtk.commons.opensha.geoDataSet.XYZToColorPalette;

import vtk.vtkActor;

public class ShakeMap extends XYZToColorPalette{


	vtkActor shakeaMapActor = new vtkActor();
	GriddedGeoDataSet dataset;
	String parameter; //mmi, pga, pgv, etc.
	
	public ShakeMap(String fp, String parameter) {
		super(fp);
		this.parameter = parameter;
	}

	public vtkActor getActor()
	{
		return shakeaMapActor;
	}

	public void setActor(vtkActor actor)
	{
		 shakeaMapActor = actor;
	}
	
	public String getParameter(){
		return parameter;
	}
}
