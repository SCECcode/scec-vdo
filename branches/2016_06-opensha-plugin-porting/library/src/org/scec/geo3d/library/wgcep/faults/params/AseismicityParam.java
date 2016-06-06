package org.scec.geo3d.library.wgcep.faults.params;

import org.opensha.commons.param.impl.BooleanParameter;

public class AseismicityParam extends BooleanParameter {
	
	public static final String NAME = "Aseis Reduces Area";
	public static final Boolean DEFAULT = false;
	
	public AseismicityParam() {
		this(DEFAULT);
	}
	
	public AseismicityParam(boolean defaultVal) {
		super(NAME, defaultVal);
		super.setDefaultValue(defaultVal);
	}

}
