package org.scec.vtk.plugins.ShakeMapPlugin.Component;

import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.scec.vtk.commons.opensha.geoDataSet.XYZToColorPalette;

import vtk.vtkActor;

public class ShakeMap extends XYZToColorPalette{


	vtkActor shakeaMapActor = new vtkActor();
	GriddedGeoDataSet dataset;
	
	public ShakeMap(String fp) {
		super(fp);
	}

	public vtkActor getActor()
	{
		return shakeaMapActor;
	}

	public void setActor(vtkActor actor)
	{
		 shakeaMapActor = actor;
	}
}
