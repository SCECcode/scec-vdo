package org.scec.geo3d.library.wgcep.faults.anim;

import java.awt.event.MouseEvent;

import javax.media.j3d.Node;

import org.scec.geo3d.library.wgcep.faults.colorers.FaultColorer;
import org.scec.geo3d.library.wgcep.surfaces.FaultSectionActorList;
import org.scec.geo3d.library.wgcep.surfaces.pickBehavior.PickHandler;

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
	public void otherPicked(Node node, MouseEvent mouseEvent) {
		nothingPicked(mouseEvent);
	}

}
