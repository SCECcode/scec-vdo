package org.scec.vtk.commons.opensha.surfaces.pickBehavior;

import java.awt.event.MouseEvent;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import vtk.vtkCellPicker;

public class FaultSectionPickBehavior implements PickHandler<AbstractFaultSection> {
	
	private NameDispalyPickHandler defaultPicker;
	
	private FaultColorer colorer;
	
	public FaultSectionPickBehavior() {
		defaultPicker = new NameDispalyPickHandler();
	}
	
	public void setColorer(FaultColorer colorer) {
		defaultPicker.setColorer(colorer);
		this.colorer = colorer;
	}

	@Override
	public void actorPicked(PickEnabledActor<AbstractFaultSection> actor,
			AbstractFaultSection reference, vtkCellPicker picker, MouseEvent e) {
		defaultPicker.actorPicked(actor, reference, picker, e);
		if (colorer instanceof PickHandler)
			((PickHandler<AbstractFaultSection>)colorer).actorPicked(actor, reference, picker, e);
	}

}
