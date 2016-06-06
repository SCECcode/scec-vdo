package org.scec.geo3d.library.wgcep.faults.colorers;

import java.awt.Color;

import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;

public class RakeColorer extends CPTBasedColorer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String NAME = "Rake (degrees)";

	public static CPT getDefaultCPT() {
		CPT cpt = new CPT();
		// original CPT - has some color ambiguity (places where the color is the same!)
//		cpt.add(new CPTVal(-180f, Color.MAGENTA, -90f, Color.GREEN));
//		cpt.add(new CPTVal(-90f, Color.GREEN, 0f, Color.RED));
//		cpt.add(new CPTVal(0f, Color.RED, 90f, Color.BLUE));
//		cpt.add(new CPTVal(90f, Color.BLUE, 180f, Color.MAGENTA));
		// new CPT without any duplicate colors
		cpt.add(new CPTVal(-180f, Color.RED, -90f, Color.YELLOW));
		cpt.add(new CPTVal(-90f, Color.YELLOW, 0f, Color.GREEN));
		cpt.add(new CPTVal(0f, Color.GREEN, 90f, Color.BLUE));
		cpt.add(new CPTVal(90f, Color.BLUE, 180f, Color.RED));
		cpt.setNanColor(Color.GRAY);
		cpt.setBelowMinColor(Color.GRAY);
		cpt.setAboveMaxColor(Color.GRAY);
		return cpt;
	}

	public RakeColorer() {
		super(getDefaultCPT(), false);
	}

	public RakeColorer(CPT cpt, boolean cptLog) {
		super(cpt, cptLog);
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		return fault.getAvgRake();
	}

	@Override
	public String getName() {
		return NAME;
	}

}
