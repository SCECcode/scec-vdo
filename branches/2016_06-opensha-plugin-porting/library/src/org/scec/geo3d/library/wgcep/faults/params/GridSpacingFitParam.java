package org.scec.geo3d.library.wgcep.faults.params;

import org.opensha.commons.param.impl.BooleanParameter;

public class GridSpacingFitParam extends BooleanParameter {
	
	public static final String NAME = "Fit Grid Spacing Exactly";
	public static final String INFO = "Fitting exactly will trim ends, whereas not will" +
			" adjust the grid spacing down enough to fit exactly";
	public static final Boolean DEFAULT = false;
	
	public GridSpacingFitParam() {
		super(NAME, DEFAULT);
		setDefaultValue(DEFAULT);
		setInfo(INFO);
	}
	

}
