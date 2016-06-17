package org.scec.vtk.commons.opensha.surfaces;

import java.util.NoSuchElementException;

public enum FaultSurfaceType {
	
	FRANKEL("Frankel"),
	STIRLING("Stirling");
	
	private String name;
	
	private FaultSurfaceType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static FaultSurfaceType forName(String name) {
		for (FaultSurfaceType rep : FaultSurfaceType.values()) {
			if (rep.getName().equals(name))
				return rep;
		}
		throw new NoSuchElementException("No fault representation exists named '"+name+"'");
	}

}
