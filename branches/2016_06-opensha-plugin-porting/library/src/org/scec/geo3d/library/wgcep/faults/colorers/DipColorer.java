package org.scec.geo3d.library.wgcep.faults.colorers;

import java.awt.Color;

import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;

public class DipColorer extends CPTBasedColorer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String NAME = "Dip (degrees)";
	
	public static CPT getDefaultCPT() {
		CPT cpt = new CPT();
		cpt.add(new CPTVal(0f, Color.RED, 90f, Color.BLUE));
		cpt.setNanColor(Color.GRAY);
		cpt.setBelowMinColor(Color.GRAY);
		cpt.setAboveMaxColor(Color.GRAY);
		return cpt;
	}

	public DipColorer() {
		super(getDefaultCPT(), false);
	}

	public DipColorer(CPT cpt, boolean cptLog) {
		super(cpt, cptLog);
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		return fault.getAvgDip();
	}

	public static Color getColor(double dip, boolean cptLog, CPT cpt) {
		if (cptLog)
			dip = Math.log(dip);
		return cpt.getColor((float)dip);
	}

	@Override
	public String getName() {
		return NAME;
	}

}
