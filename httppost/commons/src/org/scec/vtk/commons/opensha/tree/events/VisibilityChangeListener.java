package org.scec.vtk.commons.opensha.tree.events;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

public interface VisibilityChangeListener {
	
	public void visibilityChanged(AbstractFaultSection fault, boolean newVisibility);

}
