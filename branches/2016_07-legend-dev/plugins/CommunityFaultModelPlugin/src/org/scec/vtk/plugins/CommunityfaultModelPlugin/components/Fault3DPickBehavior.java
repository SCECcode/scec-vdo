package org.scec.vtk.plugins.CommunityfaultModelPlugin.components;

import java.awt.event.MouseEvent;

import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import vtk.vtkCellPicker;

public class Fault3DPickBehavior implements PickHandler<Fault3D> {
	
	private FaultInfoDisplayPickHandler defaultPicker;
	
	public Fault3DPickBehavior() {
		defaultPicker = new FaultInfoDisplayPickHandler();
	}


	@Override
	public void actorPicked(PickEnabledActor<Fault3D> actor, Fault3D reference, vtkCellPicker picker, MouseEvent e) {
		// TODO Auto-generated method stub
		defaultPicker.actorPicked(actor, reference, picker, e);
	}

}
