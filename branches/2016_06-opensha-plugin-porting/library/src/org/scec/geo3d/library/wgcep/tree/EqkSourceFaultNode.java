package org.scec.geo3d.library.wgcep.tree;

import java.awt.Color;

import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;

public class EqkSourceFaultNode extends FaultSectionNode {

	public EqkSourceFaultNode(AbstractFaultSection fault) {
		super(fault);
	}
	
	@Override
	public void setColor(Color color) {
		for (Object child : super.children) {
			AbstractFaultNode node = AbstractFaultNode.checkCast(child);
			node.setColor(color);
		}
		super.setColor(color);
	}

}
