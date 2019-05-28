package org.scec.vtk.commons.opensha.surfaces;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

public class FaultSectionBundledActorList extends FaultSectionActorList {
	
	private FaultActorBundle bundle;
	private int myFirstPointIndex;
	private int myNumPoints;
	// optional have only the first set of points be used for changing color, later points in the array will be of a static color
	private int myNumPointsForColoring; 
	private int myOpacity;

	public FaultSectionBundledActorList(AbstractFaultSection fault, FaultActorBundle bundle,
			int myFirstPointIndex, int myNumPoints, int myOpacity) {
		this(fault, bundle, myFirstPointIndex, myNumPoints, myNumPoints, myOpacity);
	}

	public FaultSectionBundledActorList(AbstractFaultSection fault, FaultActorBundle bundle,
			int myFirstPointIndex, int myNumPoints, int myNumPointsForColoring, int myOpacity) {
		super(fault);
		this.bundle = bundle;
		this.myFirstPointIndex = myFirstPointIndex;
		this.myNumPoints = myNumPoints;
		this.myNumPointsForColoring = myNumPointsForColoring;
		this.myOpacity = myOpacity;
	}

	public FaultActorBundle getBundle() {
		return bundle;
	}

	public int getMyFirstPointIndex() {
		return myFirstPointIndex;
	}

	public int getMyNumPoints() {
		return myNumPoints;
	}

	public int getMyNumPointsForColoring() {
		return myNumPointsForColoring;
	}
	
	public int getMyOpacity() {
		return myOpacity;
	}
}
