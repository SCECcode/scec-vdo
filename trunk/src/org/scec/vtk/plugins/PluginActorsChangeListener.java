package org.scec.vtk.plugins;

import org.scec.vtk.commons.legend.LegendItem;

import vtk.vtkProp;

public interface PluginActorsChangeListener {
	
	public void actorAdded(vtkProp actor);
	
	public void actorRemoved(vtkProp actor);
	
	public void legendAdded(LegendItem legend);
	
	public void legendRemoved(LegendItem legend);

}
