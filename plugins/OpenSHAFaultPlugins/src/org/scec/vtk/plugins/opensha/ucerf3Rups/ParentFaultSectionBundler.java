package org.scec.vtk.plugins.opensha.ucerf3Rups;

import java.util.HashMap;
import java.util.Map;

import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundle;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundler;

public class ParentFaultSectionBundler implements FaultActorBundler {
	
	private static final boolean D = false;
	
	private Map<Integer, FaultActorBundle> bundleMap = new HashMap<>();

	@Override
	public synchronized FaultActorBundle getBundle(AbstractFaultSection fault) {
		if (!(fault instanceof PrefDataSection)) {
			if (D) System.out.println("Can't bundle for non PrefDataSection");
			return null;
		}
		FaultSectionPrefData sect = ((PrefDataSection)fault).getFaultSection();
		Integer parentID = sect.getParentSectionId();
		if (parentID < 0) {
			if (D) System.out.println("Can't bundle for no parent. Sect: "+sect.getName());
			return null;
		}
		FaultActorBundle bundle = bundleMap.get(parentID);
		if (bundle == null) {
			bundle = new FaultActorBundle();
			if (D) System.out.println("Creating new bundle for: "+sect.getParentSectionName());
			bundleMap.put(parentID, bundle);
		}
		return bundle;
	}

	@Override
	public synchronized void clearBundles() {
		bundleMap.clear();
	}

}
