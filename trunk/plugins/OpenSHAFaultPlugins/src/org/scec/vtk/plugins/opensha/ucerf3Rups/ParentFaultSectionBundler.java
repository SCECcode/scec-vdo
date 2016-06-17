package org.scec.vtk.plugins.opensha.ucerf3Rups;

import java.util.HashMap;
import java.util.Map;

import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundle;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundler;

public class ParentFaultSectionBundler implements FaultActorBundler {
	
	private Map<Integer, FaultActorBundle> bundleMap = new HashMap<>();

	@Override
	public synchronized FaultActorBundle getBundle(AbstractFaultSection fault) {
		if (!(fault instanceof PrefDataSection))
			return null;
		Integer parentID = ((PrefDataSection)fault).getFaultSection().getParentSectionId();
		if (parentID < 0)
			return null;
		FaultActorBundle bundle = bundleMap.get(parentID);
		if (bundle == null) {
			bundle = new FaultActorBundle();
			bundleMap.put(parentID, bundle);
		}
		return bundle;
	}

	@Override
	public synchronized void clearBundles() {
		bundleMap.clear();
	}

}
