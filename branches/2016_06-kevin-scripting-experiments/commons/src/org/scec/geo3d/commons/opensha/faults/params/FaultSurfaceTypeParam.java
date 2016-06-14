package org.scec.geo3d.commons.opensha.faults.params;

import java.util.ArrayList;

import org.opensha.commons.param.impl.StringParameter;
import org.scec.geo3d.commons.opensha.surfaces.FaultSurfaceType;

public class FaultSurfaceTypeParam extends StringParameter {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "Surface Type";
	
	private static ArrayList<String> getDefaultAllowedStrings() {
		ArrayList<String> strings = new ArrayList<String>();
		for (FaultSurfaceType fsr : FaultSurfaceType.values()) {
			strings.add(fsr.getName());
		}
		return strings;
	}
	
	public FaultSurfaceTypeParam() {
		super(NAME, getDefaultAllowedStrings(), FaultSurfaceType.STIRLING.getName());
	}
	
	public FaultSurfaceTypeParam(FaultSurfaceType defaultVal) {
		super(NAME, getDefaultAllowedStrings(), defaultVal.getName());
	}
	
	public FaultSurfaceType getFaultSurfaceType() {
		return FaultSurfaceType.forName(this.getValue());
	}

}
