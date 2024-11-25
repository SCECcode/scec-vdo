package org.scec.vtk.commons.opensha.surfaces;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

public class GenericFaultActorBundler implements FaultActorBundler {
	
	private int maxBundleSize;
	private int curBundleSize;
	
	private FaultActorBundle curBundle;
	
	public GenericFaultActorBundler(int maxBundleSize) {
		this.maxBundleSize = maxBundleSize;
	}

	@Override
	public synchronized FaultActorBundle getBundle(AbstractFaultSection fault) {
		if (curBundle == null || maxBundleSize > 0 && curBundleSize >= maxBundleSize) {
			curBundleSize = 0;
			curBundle = new FaultActorBundle();
		}
		curBundleSize++;
		return curBundle;
	}

	@Override
	public synchronized void clearBundles() {
		curBundle = null;
	}

}
