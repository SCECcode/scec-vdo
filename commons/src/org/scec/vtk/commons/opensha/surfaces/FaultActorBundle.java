package org.scec.vtk.commons.opensha.surfaces;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.tools.picking.PointPickEnabledActor;

import com.google.common.base.Preconditions;

import vtk.vtkCellArray;
import vtk.vtkDataSet;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnsignedCharArray;

public class FaultActorBundle {
	
	private PointPickEnabledActor<AbstractFaultSection> actor;
	private vtkDataSet dataSet;
	private vtkPoints points;
	private vtkUnsignedCharArray colorArray;
	private vtkCellArray cellArray;
	
	public FaultActorBundle() {
		super();
	}
	
	public void initialize(PointPickEnabledActor<AbstractFaultSection> actor, vtkDataSet dataSet, vtkPoints points,
			vtkUnsignedCharArray colorArray, vtkCellArray cellArray) {
		this.actor = actor;
		this.dataSet = dataSet;
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
		Preconditions.checkState(dataSet instanceof vtkPolyData);
		return (vtkPolyData)getVtkDataSet();
	}
	
	public vtkDataSet getVtkDataSet() {
		return dataSet;
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
		int totNumTuples = (int) colorArray.GetNumberOfTuples();
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
		if (actor != null) actor.Modified();
		if (dataSet != null) dataSet.Modified();
		if (points != null) points.Modified();
		if (colorArray != null) colorArray.Modified();
		if (cellArray != null) cellArray.Modified();
	}

}
