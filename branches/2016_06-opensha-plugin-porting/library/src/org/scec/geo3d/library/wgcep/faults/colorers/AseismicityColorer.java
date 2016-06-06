package org.scec.geo3d.library.wgcep.faults.colorers;

import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;
import org.scec.geo3d.library.wgcep.faults.attributeInterfaces.AseismicityFaultSection;

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
