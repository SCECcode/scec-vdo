package org.scec.vtk.plugins.opensha.ucerf3Rups;

import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemSolution;

public interface UCERF3RupSetChangeListener {
	
	/**
	 * set the rupture set, or null if none loaded.
	 * @param rupSet
	 * @param sol
	 */
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol);

}
