package org.scec.geo3d.commons.opensha.faults.anim;

import java.awt.event.MouseEvent;

import org.scec.geo3d.commons.opensha.faults.colorers.FaultColorer;
import org.scec.geo3d.commons.opensha.surfaces.FaultSectionActorList;
import org.scec.geo3d.commons.opensha.surfaces.pickBehavior.PickHandler;

import vtk.vtkActor;

public class AnimMultiColorerPickHandlerWrapper extends
		AnimMultiColorerWrapper implements PickHandler {
	
	private PickHandler pickHandler;

	public AnimMultiColorerPickHandlerWrapper(String name, FaultColorer colorer, PickHandler pickHandler) {
		super(name, colorer);
		this.pickHandler = pickHandler;
	}

	@Override
	public void faultPicked(FaultSectionActorList faultShape,
			MouseEvent mouseEvent) {
		if (pickHandler != null)
			pickHandler.faultPicked(faultShape, mouseEvent);
	}

	@Override
	public void nothingPicked(MouseEvent mouseEvent) {
		if (pickHandler != null)
			pickHandler.nothingPicked(mouseEvent);
	}

	@Override
	public void otherPicked(vtkActor node, MouseEvent mouseEvent) {
		nothingPicked(mouseEvent);
	}

}
