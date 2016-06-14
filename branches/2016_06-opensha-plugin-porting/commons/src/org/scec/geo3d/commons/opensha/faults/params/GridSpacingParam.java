package org.scec.geo3d.commons.opensha.faults.params;

import org.opensha.commons.exceptions.ConstraintException;
import org.opensha.commons.param.constraint.impl.DoubleConstraint;
import org.opensha.commons.param.impl.WarningDoubleParameter;

public class GridSpacingParam extends WarningDoubleParameter {
	
	private static final double min = 0.01;
	private static final double max = 10.0;
	private static final double warn_min = min;
	private static final double warn_max = max;
	
	private static final double default_val = 1.0;

	public static final String NAME = "Grid Spacing";
	
	public GridSpacingParam()
			throws ConstraintException {
		super(NAME, min, max, default_val);
		super.setWarningConstraint(new DoubleConstraint(warn_min, warn_max));
	}

}
