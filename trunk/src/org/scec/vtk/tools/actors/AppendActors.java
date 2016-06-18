package org.scec.vtk.tools.actors;

import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkAssembly;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkPropAssembly;

public class AppendActors {
	
	private vtkAppendPolyData appendedPoly;
	private vtkPropAssembly assembleData;
	private vtkActor appendedActor; 

	public AppendActors()
	{   
		//appendedPoly = new vtkAppendPolyData();
		//appendedActor= new vtkActor(); 
		assembleData = new vtkPropAssembly();
	}
	public void addToAppendedPolyData(vtkProp polydata)
	{
		//appendedPoly.AddInputConnection(polydata);
		assembleData.AddPart(polydata);
	}
	public vtkPropAssembly getAppendedActor()
	{
		return assembleData;
	}
}
