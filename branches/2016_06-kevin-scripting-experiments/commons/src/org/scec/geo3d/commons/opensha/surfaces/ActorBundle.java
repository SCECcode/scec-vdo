package org.scec.geo3d.commons.opensha.surfaces;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnsignedCharArray;

public class ActorBundle {
	
	private vtkActor actor;
	private vtkPolyData polyData;
	private vtkPoints points;
	private vtkUnsignedCharArray colorArray;
	private vtkCellArray cellArray;
	
	public ActorBundle(vtkActor actor, vtkPolyData polyData, vtkPoints points,
			vtkUnsignedCharArray colorArray, vtkCellArray cellArray) {
		super();
		this.actor = actor;
		this.polyData = polyData;
		this.points = points;
		this.colorArray = colorArray;
		this.cellArray = cellArray;
	}

	public vtkActor getActor() {
		return actor;
	}

	public vtkPolyData getPolyData() {
		return polyData;
	}

	public vtkPoints getPoints() {
		return points;
	}

	public vtkUnsignedCharArray getColorArray() {
		return colorArray;
	}

	public vtkCellArray getCellArray() {
		return cellArray;
	}
	
	public void modified() {
		actor.Modified();
		polyData.Modified();
		points.Modified();
		colorArray.Modified();
		cellArray.Modified();
	}

}
