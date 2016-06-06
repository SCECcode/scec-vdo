package org.scec.geo3d.library.wgcep.tree.events;

import java.awt.Color;

import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;

public interface ColorChangeListener {
	
	public void colorChanged(AbstractFaultSection fault, Color newColor);

}
