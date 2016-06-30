package org.scec.vtk.commons.opensha.surfaces.pickBehavior;

import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.main.Info;
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
		if (fault == null || e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() < 2 || e.getClickCount() > 2)
			return;
		String s = fault.getInfo();
		if (this.colorer != null && this.colorer instanceof CPTBasedColorer) {
			try {
				double val = ((CPTBasedColorer)colorer).getValue(fault);
				s = "Val: "+(float)val+"\n"+s;
			} catch (Exception ex) {}
		}
		
		JOptionPane.showMessageDialog(Info.getMainGUI(), s, "Fault Section Information", JOptionPane.INFORMATION_MESSAGE);
//		s = s.replaceAll("\n", ", ");
//		System.out.println(s);
		// TODO show in GUI
	}

}
