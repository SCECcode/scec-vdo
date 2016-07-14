package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.awt.event.MouseEvent;

import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import vtk.vtkCellPicker;

public class EQPickBehavior implements PickHandler<EQCatalog> {
	
	private EQInfoDisplayPickHandler defaultPicker;
	
	public EQPickBehavior() {
		defaultPicker = new EQInfoDisplayPickHandler();
	}


	@Override
	public void actorPicked(PickEnabledActor<EQCatalog> actor, EQCatalog reference, vtkCellPicker picker, MouseEvent e) {
		// TODO Auto-generated method stub
		defaultPicker.actorPicked(actor, reference, picker, e);
	}

}
