package org.scec.vtk.plugins.ScriptingPlugin;

import org.scec.vtk.main.Info;

import vtk.vtkCamera;
import vtk.vtkPoints;

public class KeyFrame {

 double[] camPos;
 vtkCamera cam ;
 public KeyFrame() {
	// TODO Auto-generated constructor stub
	 camPos = new double[3];
	 cam = Info.getMainGUI().getRenderWindow().getRenderer().GetActiveCamera();
	// camPos = cam.GetPosition();
}
 public vtkCamera getCamPos()
 {
	 return cam;
 }
 public void setCamPos(vtkCamera campos)
 {
	 this.cam = campos;
 }
}
