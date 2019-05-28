package org.scec.vtk.commons.opensha.faults.colorers;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.attributeInterfaces.AseismicityFaultSection;

public class AseismicityColorer extends CPTBasedColorer {

	public AseismicityColorer() {
		super(SlipRateColorer.getDefaultCPT().rescale(0d, 1d), false);
	}

	@Override
	public String getName() {
		return "Aseismicity";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
//		if (fault instanceof PrefDataSection) {
//			FaultSectionPrefData data = ((PrefDataSection)fault).getFaultSection();
//			return data.getAseismicSlipFactor();
//		} else if (fault instanceof DeformationFault) {
//			return ((DeformationFault)fault).getMomentReduction();
//		}
		if (fault instanceof AseismicityFaultSection)
			return ((AseismicityFaultSection)fault).getAseismicSlipFactor();
		return Double.NaN;
	}

}
