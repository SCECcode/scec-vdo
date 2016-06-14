package org.scec.geo3d.commons.opensha.surfaces;

import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnsignedCharArray;

public class FaultSectionBundledActorList extends FaultSectionActorList {
	
	private ActorBundle bundle;
	private int myFirstPointIndex;
	private int myNumPoints;
	private int myOpacity;

	public FaultSectionBundledActorList(AbstractFaultSection fault, ActorBundle bundle,
			int myFirstPointIndex, int myNumPoints, int myOpacity) {
		super(fault);
		this.bundle = bundle;
		this.myFirstPointIndex = myFirstPointIndex;
		this.myNumPoints = myNumPoints;
		this.myOpacity = myOpacity;
	}

	public ActorBundle getBundle() {
		return bundle;
	}

	public int getMyFirstPointIndex() {
		return myFirstPointIndex;
	}

	public int getMyNumPoints() {
		return myNumPoints;
	}
	
	public int getMyOpacity() {
		return myOpacity;
	}
}
