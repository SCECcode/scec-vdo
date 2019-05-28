package org.scec.vtk.plugins.opensha.obsEqkRup;

import java.awt.Color;

import org.opensha.commons.util.cpt.CPT;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.ObsEqkRupSection;

public class RupMagColorer extends CPTBasedColorer {
	
	private static CPT buildCPT() {
		return new CPT(2.5d, 8d, Color.BLUE, Color.GREEN, Color.RED);
	}

	public RupMagColorer() {
		super(buildCPT(), false);
	}

	@Override
	public String getName() {
		return "Magnitude";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		if (fault instanceof ObsEqkRupSection) {
			return ((ObsEqkRupSection)fault).getRup().getMag();
		}
		return Double.NaN;
	}

}
