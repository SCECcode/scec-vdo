package org.scec.geo3d.commons.opensha.surfaces.pickBehavior;

import java.awt.event.MouseEvent;

import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;
import org.scec.geo3d.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.geo3d.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import vtk.vtkCellPicker;

public class NameDispalyPickHandler implements PickHandler<AbstractFaultSection> {
	
	private FaultColorer colorer;
	
	public void setColorer(FaultColorer colorer) {
		this.colorer = colorer;
	}

	@Override
	public void actorPicked(PickEnabledActor<AbstractFaultSection> actor,
			AbstractFaultSection fault, vtkCellPicker picker, MouseEvent e) {
		if (fault == null || e.getButton() != MouseEvent.BUTTON1)
			return;
		String s = fault.getInfo();
		if (this.colorer != null && this.colorer instanceof CPTBasedColorer) {
			try {
				double val = ((CPTBasedColorer)colorer).getValue(fault);
				s = "Val: "+(float)val+"\n"+s;
			} catch (Exception ex) {}
		}
		s = s.replaceAll("\n", ", ");
		System.out.println(s);
		// TODO show in GUI
	}

}
