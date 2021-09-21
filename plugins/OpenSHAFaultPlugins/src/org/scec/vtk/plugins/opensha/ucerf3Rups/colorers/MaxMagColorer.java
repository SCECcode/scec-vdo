package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.io.IOException;

import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemSolution;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;

public class MaxMagColorer extends CPTBasedColorer implements UCERF3RupSetChangeListener {
	
	private FaultSystemRupSet rupSet;
	
	private static CPT getDefaultCPT() {
		try {
			return GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(6d, 8.5d);
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	public MaxMagColorer() {
		super(getDefaultCPT(), false);
	}

	@Override
	public String getName() {
		return "Section Maximum Magnitude";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		if (rupSet == null)
			return Double.NaN;
		return rupSet.getMaxMagForSection(fault.getId());
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.rupSet = rupSet;
		fireColorerChangeEvent();
	}

}
