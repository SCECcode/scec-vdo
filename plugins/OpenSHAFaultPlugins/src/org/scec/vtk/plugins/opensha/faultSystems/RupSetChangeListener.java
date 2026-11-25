package org.scec.vtk.plugins.opensha.faultSystems;

import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemSolution;

public interface RupSetChangeListener {
	
	/**
	 * set the rupture set, or null if none loaded.
	 * @param rupSet
	 * @param sol
	 */
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol);

}
