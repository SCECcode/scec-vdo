package org.scec.geo3d.commons.opensha.faults.anim;

import java.awt.event.MouseEvent;

import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;
import org.scec.geo3d.commons.opensha.faults.colorers.FaultColorer;
import org.scec.geo3d.commons.opensha.surfaces.FaultSectionActorList;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import vtk.vtkActor;
import vtk.vtkCellPicker;

public class AnimMultiColorerPickHandlerWrapper extends
		AnimMultiColorerWrapper implements PickHandler<AbstractFaultSection> {
	
	private PickHandler<AbstractFaultSection> pickHandler;

	public AnimMultiColorerPickHandlerWrapper(String name, FaultColorer colorer,
			PickHandler<AbstractFaultSection> pickHandler) {
		super(name, colorer);
		this.pickHandler = pickHandler;
	}

	@Override
	public void actorPicked(PickEnabledActor<AbstractFaultSection> actor,
			AbstractFaultSection reference, vtkCellPicker picker, MouseEvent e) {
		if (pickHandler != null)
			pickHandler.actorPicked(actor, reference, picker, e);
	}

}
