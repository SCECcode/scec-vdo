package org.scec.geo3d.commons.opensha.surfaces;

import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;

public interface FaultActorBundler {
	
	public ActorBundle getBundle(AbstractFaultSection fault);
	
	public void clearBundles();

}
