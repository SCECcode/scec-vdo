package org.scec.vtk.commons.opensha.faults.colorers;

import java.awt.Color;

import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

public class StrikeColorer extends CPTBasedColorer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String NAME = "Strike (degrees)";

	public static CPT getDefaultCPT() {
		CPT cpt = new CPT();
		cpt.add(new CPTVal(0f, Color.MAGENTA, 90f, Color.GREEN));
		cpt.add(new CPTVal(90f, Color.GREEN, 180f, Color.RED));
		cpt.add(new CPTVal(180f, Color.RED, 270f, Color.BLUE));
		cpt.add(new CPTVal(270f, Color.BLUE, 360f, Color.MAGENTA));
		cpt.setNanColor(Color.GRAY);
		cpt.setBelowMinColor(Color.GRAY);
		cpt.setAboveMaxColor(Color.GRAY);
		return cpt;
	}
	
	public StrikeColorer() {
		super(getDefaultCPT(), false);
	}

	public StrikeColorer(CPT cpt, boolean cptLog) {
		super(cpt, cptLog);
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		return fault.getAvgStrike();
	}

	@Override
	public String getName() {
		return NAME;
	}

}
