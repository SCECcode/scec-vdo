package org.scec.vtk.plugins.opensha.simulators;

import java.util.HashMap;
import java.util.Map;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.SimulatorElementFault;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundle;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundler;

public class EQSimsFaultSectionBundler implements FaultActorBundler {
	
	private Map<Integer, FaultActorBundle> bundleMap = new HashMap<>();

	@Override
	public synchronized FaultActorBundle getBundle(AbstractFaultSection fault) {
		if (!(fault instanceof SimulatorElementFault))
			return null;
		Integer parentID = ((SimulatorElementFault)fault).getParentID();
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
