package org.scec.vtk.plugins.opensha.simulators;

import java.util.HashMap;
import java.util.Map;

import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;
import org.scec.geo3d.commons.opensha.faults.faultSectionImpl.RectangularElementFault;
import org.scec.geo3d.commons.opensha.surfaces.ActorBundle;
import org.scec.geo3d.commons.opensha.surfaces.FaultActorBundler;

public class EQSimsFaultSectionBundler implements FaultActorBundler {
	
	private Map<Integer, ActorBundle> bundleMap = new HashMap<>();

	@Override
	public synchronized ActorBundle getBundle(AbstractFaultSection fault) {
		if (!(fault instanceof RectangularElementFault))
			return null;
		Integer parentID = ((RectangularElementFault)fault).getParentID();
		if (parentID < 0)
			return null;
		ActorBundle bundle = bundleMap.get(parentID);
		if (bundle == null) {
			bundle = new ActorBundle();
			bundleMap.put(parentID, bundle);
		}
		return bundle;
	}

	@Override
	public synchronized void clearBundles() {
		bundleMap.clear();
	}

}
