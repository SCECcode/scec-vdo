package org.scec.vtk.plugins;

import vtk.vtkActor;
import vtk.vtkProp;

public interface PluginActorsChangeListener {
	
	public void actorAdded(vtkProp actor);
	
	public void actorRemoved(vtkProp actor);

}
