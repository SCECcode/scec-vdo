package org.scec.geo3d.commons.opensha.tree.events;

import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;

public interface VisibilityChangeListener {
	
	public void visibilityChanged(AbstractFaultSection fault, boolean newVisibility);

}
