package org.scec.vtk.commons.opensha.faults.colorers;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.attributeInterfaces.CouplingCoefficientFaultSection;

public class CouplingCoefficientColorer extends CPTBasedColorer {

	public CouplingCoefficientColorer() {
		super(SlipRateColorer.getDefaultCPT().rescale(0d, 1d), false);
	}

	@Override
	public String getName() {
		return "Coupling Coefficient";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
//		if (fault instanceof PrefDataSection) {
//			FaultSectionPrefData data = ((PrefDataSection)fault).getFaultSection();
//			return data.getCouplingCoeff();
//		} else if (fault instanceof DeformationFault) {
//			return ((DeformationFault)fault).getMomentReduction();
//		}
		if (fault instanceof CouplingCoefficientFaultSection)
			return ((CouplingCoefficientFaultSection)fault).getCouplingCoeff();
		return Double.NaN;
	}

}
