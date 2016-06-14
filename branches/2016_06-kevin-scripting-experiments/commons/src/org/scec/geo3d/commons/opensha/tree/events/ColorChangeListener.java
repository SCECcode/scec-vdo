package org.scec.geo3d.commons.opensha.tree.events;

import java.awt.Color;

import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;

public interface ColorChangeListener {
	
	public void colorChanged(AbstractFaultSection fault, Color newColor);

}
