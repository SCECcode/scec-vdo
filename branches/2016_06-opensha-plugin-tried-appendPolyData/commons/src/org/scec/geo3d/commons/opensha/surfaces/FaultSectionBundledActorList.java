package org.scec.geo3d.commons.opensha.surfaces;

import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnsignedCharArray;

public class FaultSectionBundledActorList extends FaultSectionActorList {

	private ActorBundle bundle;

	private vtkPolyData polyData;
	private vtkPoints points;
	private vtkUnsignedCharArray colorArray;
	private vtkCellArray cellArray;

	private int myOpacity;

	public ActorBundle getBundle() {
		return bundle;
	}

	public FaultSectionBundledActorList(AbstractFaultSection fault,
			ActorBundle bundle, vtkPolyData polyData, vtkPoints points,
			vtkUnsignedCharArray colorArray, vtkCellArray cellArray, int myOpacity) {
		super(fault);
		this.bundle = bundle;
		this.polyData = polyData;
		this.points = points;
		this.colorArray = colorArray;
		this.cellArray = cellArray;
		this.myOpacity = myOpacity;
	}

	public int getMyOpacity() {
		return myOpacity;
	}

	public vtkUnsignedCharArray getColorArray() {
		return colorArray;
	}
}
