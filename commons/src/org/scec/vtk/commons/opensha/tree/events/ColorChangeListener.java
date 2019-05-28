package org.scec.vtk.commons.opensha.tree.events;

import java.awt.Color;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

public interface ColorChangeListener {
	
	public void colorChanged(AbstractFaultSection fault, Color newColor);

}
