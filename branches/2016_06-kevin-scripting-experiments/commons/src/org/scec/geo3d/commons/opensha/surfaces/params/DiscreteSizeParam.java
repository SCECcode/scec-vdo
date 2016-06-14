package org.scec.geo3d.commons.opensha.surfaces.params;

import java.util.ArrayList;

import org.opensha.commons.param.impl.DoubleDiscreteParameter;

public class DiscreteSizeParam extends DoubleDiscreteParameter {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static ArrayList<Double> getVals(double min, double max) {
		ArrayList<Double> vals = new ArrayList<Double>();
		for (double val=min; val<=max; val++) {
			vals.add(val);
		}
		return vals;
	}
	
	public DiscreteSizeParam(String name, double min, double max, double value) {
		super(name, getVals(min, max), value);
	}

}
