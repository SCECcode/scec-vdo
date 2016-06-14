package org.scec.vtk.plugins.ScriptingPlugin;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;

import com.google.common.base.Throwables;

import vtk.vtkCamera;
import vtk.vtkPoints;

public class CameraAnimator implements RenderStepListener {
	
	private vtkPoints pointsPosition;
	private vtkPoints pointsFocalPoint;
	private vtkPoints pointsViewUp;
	
	public CameraAnimator (vtkPoints pointsPosition, vtkPoints pointsFocalPoint, vtkPoints pointsViewUp) {
		this.pointsPosition = pointsPosition;
		this.pointsFocalPoint = pointsFocalPoint;
		this.pointsViewUp = pointsViewUp;
	}

	@Override
	public void renderStarted() {}

	@Override
	public void renderStopped() {}

	@Override
	public void renderFrameToBeProcessed(double startTime, double curTime, double endTime) {
		System.out.println("Camera start for frame at "+curTime);
		double relativeTime = (curTime - startTime)/endTime;
		int cameraFrame = (int) ((1-relativeTime)*(1)+ relativeTime*(pointsPosition.GetNumberOfPoints()-1));
		
		vtkCamera cam = MainGUI.getRenderWindow().GetRenderWindow().GetRenderers().GetFirstRenderer().GetActiveCamera();
		
		if(cameraFrame < pointsPosition.GetNumberOfPoints()) {
			cam.SetPosition(pointsPosition.GetPoint(cameraFrame)[0],pointsPosition.GetPoint(cameraFrame)[1],pointsPosition.GetPoint(cameraFrame)[2]);
			cam.SetFocalPoint(pointsFocalPoint.GetPoint(cameraFrame)[0],pointsFocalPoint.GetPoint(cameraFrame)[1],pointsFocalPoint.GetPoint(cameraFrame)[2]);  
			cam.SetViewUp(pointsViewUp.GetPoint(cameraFrame)[0],pointsViewUp.GetPoint(cameraFrame)[1],pointsViewUp.GetPoint(cameraFrame)[2]);
			cam.OrthogonalizeViewUp();
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {	
//						Info.getMainGUI().getRenderWindow().GetRenderWindow().Render();
						MainGUI.getRenderWindow().GetRenderWindow().Render();
//						Info.getMainGUI().getRenderWindow().GetRenderWindow().GetRenderers().GetFirstRenderer().SetActiveCamera(camnew);
//						Info.getMainGUI().getRenderWindow().GetRenderWindow().GetRenderers().GetFirstRenderer().ResetCameraClippingRange();
						MainGUI.getRenderWindow().GetRenderWindow().GetRenderers().GetFirstRenderer().ResetCameraClippingRange();
					}
				});
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		}
		System.out.println("Camera end for frame at "+curTime);
	}

}
