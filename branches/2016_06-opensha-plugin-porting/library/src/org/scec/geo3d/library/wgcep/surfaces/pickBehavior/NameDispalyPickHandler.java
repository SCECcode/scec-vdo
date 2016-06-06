package org.scec.geo3d.library.wgcep.surfaces.pickBehavior;

import java.awt.event.MouseEvent;

import javax.media.j3d.Node;

import org.scec.geo3d.library.wgcep.faults.colorers.CPTBasedColorer;
import org.scec.geo3d.library.wgcep.faults.colorers.FaultColorer;
import org.scec.geo3d.library.wgcep.surfaces.FaultSectionActorList;

public class NameDispalyPickHandler implements PickHandler {
	
	private FaultColorer colorer;

	@Override
	public void faultPicked(FaultSectionActorList faultShape, MouseEvent mouseEvent) {
		String s = faultShape.getInfo();
		if (this.colorer != null && this.colorer instanceof CPTBasedColorer) {
			try {
				double val = ((CPTBasedColorer)colorer).getValue(faultShape.getFault());
				s = "Val: "+(float)val+"\n"+s;
			} catch (Exception e) {}
		}
		s = s.replaceAll("\n", ", ");
//		Geo3dInfo.getMainWindow().setMessage(s); // TODO
	}

	@Override
	public void nothingPicked(MouseEvent mouseEvent) {
//		Geo3dInfo.getMainWindow().setMessageDefault(); // TODO
	}

	@Override
	public void otherPicked(Node node, MouseEvent mouseEvent) {
		nothingPicked(mouseEvent);
	}
	
	public void setColorer(FaultColorer colorer) {
		this.colorer = colorer;
	}

}
