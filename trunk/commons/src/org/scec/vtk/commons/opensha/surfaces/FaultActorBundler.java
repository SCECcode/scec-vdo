package org.scec.vtk.commons.opensha.surfaces;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

public interface FaultActorBundler {
	
	public ActorBundle getBundle(AbstractFaultSection fault);
	
	public void clearBundles();

}
