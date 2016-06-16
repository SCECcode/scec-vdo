package org.scec.geo3d.commons.opensha.surfaces;

import com.google.common.base.Preconditions;

import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProperty;

public class ActorBundle {
	
	private vtkActor actor;
	private vtkAppendPolyData appendPolyData;
	private vtkPolyDataMapper polyDataMapper;
	
	private int numPolyDatas = 0;
	
	public ActorBundle() {
		super();
		appendPolyData = new vtkAppendPolyData();
		
		polyDataMapper = new vtkPolyDataMapper();
		polyDataMapper.SetInputConnection(appendPolyData.GetOutputPort());
		
		polyDataMapper.ScalarVisibilityOn();
		polyDataMapper.SetScalarModeToUsePointFieldData();
		polyDataMapper.SelectColorArray("Colors");
		
		actor = new vtkActor();
		actor.SetMapper(polyDataMapper);
	}
	
	public synchronized void addPolyData(vtkPolyData polyData) {
		appendPolyData.AddInputData(polyData);
		numPolyDatas++;
		modified();
	}
	
	public synchronized void removePolyData(vtkPolyData polyData) {
		appendPolyData.RemoveInputData(polyData);
		numPolyDatas--;
		modified();
	}
	
	public vtkActor getActor() {
		return actor;
	}
	
	public vtkProperty getActorProperty() {
		return actor.GetProperty();
	}
	
	public synchronized int getNumPolyDatas() {
		return numPolyDatas;
	}
	
	public boolean isEmpty() {
		return getNumPolyDatas() == 0;
	}
	
	public void modified() {
		appendPolyData.Modified();
		polyDataMapper.Modified();
		actor.Modified();
	}

}
