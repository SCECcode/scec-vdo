package org.scec.geo3d.commons.opensha.faults.colorers;

import java.awt.Color;

import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;

public class SlipRateColorer extends CPTBasedColorer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String NAME = "Slip Rate (mm/yr)";

	public static CPT getDefaultCPT() {
		CPT cpt = new CPT();

		cpt.setBelowMinColor(Color.GRAY);
		cpt.setNanColor(Color.GRAY);

//		cpt.add(new CPTVal(0f, Color.GRAY, 0f, Color.GRAY));
		cpt.add(new CPTVal(Float.MIN_VALUE, Color.BLUE, 10f, Color.MAGENTA));
		cpt.add(new CPTVal(10f, Color.MAGENTA, 20f, Color.RED));
		cpt.add(new CPTVal(20f, Color.RED, 30f, Color.ORANGE));
		cpt.add(new CPTVal(30f, Color.ORANGE, 40f, Color.YELLOW));

		cpt.setAboveMaxColor(Color.YELLOW);

		return cpt;
	}

	public SlipRateColorer() {
		super(getDefaultCPT(), false);
	}

	public SlipRateColorer(CPT cpt, boolean cptLog) {
		super(cpt, cptLog);
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		return fault.getSlipRate();
	}

	@Override
	public String getName() {
		return NAME;
	}

}
