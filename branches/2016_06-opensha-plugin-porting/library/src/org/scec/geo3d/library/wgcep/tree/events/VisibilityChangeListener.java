package org.scec.geo3d.library.wgcep.tree.events;

import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;

public interface VisibilityChangeListener {
	
	public void visibilityChanged(AbstractFaultSection fault, boolean newVisibility);

}
