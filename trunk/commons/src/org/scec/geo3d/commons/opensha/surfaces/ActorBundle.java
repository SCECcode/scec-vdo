package org.scec.geo3d.commons.opensha.surfaces;

import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.tools.picking.PointPickEnabledActor;

import com.google.common.base.Preconditions;

import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnsignedCharArray;

public class ActorBundle {
	
	private PointPickEnabledActor<AbstractFaultSection> actor;
	private vtkPolyData polyData;
	private vtkPoints points;
	private vtkUnsignedCharArray colorArray;
	private vtkCellArray cellArray;
	
	public ActorBundle() {
		super();
	}
	
	public void initialize(PointPickEnabledActor<AbstractFaultSection> actor, vtkPolyData polyData, vtkPoints points,
			vtkUnsignedCharArray colorArray, vtkCellArray cellArray) {
		this.actor = actor;
		this.polyData = polyData;
		this.points = points;
		this.colorArray = colorArray;
		this.cellArray = cellArray;
	}
	
	public boolean isInitialized() {
		return colorArray != null;
	}

	public PointPickEnabledActor<AbstractFaultSection> getActor() {
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
	
	public void setVisible(FaultSectionBundledActorList faultBundle, boolean visible) {
		Preconditions.checkState(faultBundle.getBundle() == this);
		
		int firstIndex = faultBundle.getMyFirstPointIndex();
		int lastIndex = firstIndex + faultBundle.getMyNumPoints() - 1;
		double opacity;
		if (visible)
			opacity = faultBundle.getMyOpacity();
		else
			opacity = 0;
		int totNumTuples = colorArray.GetNumberOfTuples();
		for (int index=firstIndex; index<=lastIndex; index++) {
			Preconditions.checkState(index < totNumTuples, "Bad tuple index. index=%s, num tuples=%s", index, totNumTuples);
			double[] orig = colorArray.GetTuple4(index);
			colorArray.SetTuple4(index, orig[0], orig[1], orig[2], opacity); // keep same color
			
			if (visible)
				actor.registerPointID(index, faultBundle.getFault());
			else
				actor.unregisterPointID(index);
		}
		modified();
	}
	
	public boolean areAnyPointVisible() {
		return actor.getNumRegisteredPoints() > 0;
	}
	
	public void modified() {
		actor.Modified();
		polyData.Modified();
		points.Modified();
		colorArray.Modified();
		cellArray.Modified();
	}

}
