package org.scec.vtk.plugins.opensha.ucerf3Rups;

import java.util.HashMap;
import java.util.Map;

import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;
import org.scec.geo3d.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.geo3d.commons.opensha.surfaces.ActorBundle;
import org.scec.geo3d.commons.opensha.surfaces.FaultActorBundler;

public class ParentFaultSectionBundler implements FaultActorBundler {
	
	private Map<Integer, ActorBundle> bundleMap = new HashMap<>();

	@Override
	public synchronized ActorBundle getBundle(AbstractFaultSection fault) {
		if (!(fault instanceof PrefDataSection))
			return null;
		Integer parentID = ((PrefDataSection)fault).getFaultSection().getParentSectionId();
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
