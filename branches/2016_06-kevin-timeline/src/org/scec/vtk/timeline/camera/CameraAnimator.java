package org.scec.vtk.timeline.camera;

import javax.swing.SwingUtilities;

import org.opensha.commons.util.ExceptionUtils;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.timeline.AnimationTimeListener;
import org.scec.vtk.timeline.KeyFrameList;

import vtk.vtkCamera;
import vtk.vtkRenderer;

public class CameraAnimator implements AnimationTimeListener {
	
	private KeyFrameList keys;
	
	public enum SplineElement {
		POSITION,
		FOCAL_POINT,
		VIEW_UP
	}
	
	private CameraSplineCalculator positionSpline;
	private CameraSplineCalculator focalSpline;
	private CameraSplineCalculator upSpline;
	
	private vtkCamera cam;
	
	public CameraAnimator(KeyFrameList keys) {
		this.keys = keys;
		
		positionSpline = new CameraSplineCalculator(keys, SplineElement.POSITION);
		focalSpline = new CameraSplineCalculator(keys, SplineElement.FOCAL_POINT);
		upSpline = new CameraSplineCalculator(keys, SplineElement.VIEW_UP);
	}
	
	public void activateTime(double time) {
		synchronized (keys) {
			if (keys.isEmpty())
				return;
			
			double[] position = positionSpline.getPoint(time);
			double[] focal = focalSpline.getPoint(time);
			double[] up = upSpline.getPoint(time);
			
			cam.SetPosition(position[0], position[1], position[2]);
			cam.SetFocalPoint(focal[0], focal[1], focal[2]);
			cam.SetViewUp(up[0], up[1], up[2]);
			cam.OrthogonalizeViewUp();

			setActiveCamera(cam);
		}
	}
	
	static void setActiveCamera(final vtkCamera cam) {
		final vtkRenderer renderer = MainGUI.getRenderWindow().GetRenderWindow().GetRenderers().GetFirstRenderer();
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {	
					MainGUI.updateRenderWindow();//.GetRenderWindow().Render();
					renderer.SetActiveCamera(cam);
					renderer.ResetCameraClippingRange();
				}
			});
		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);;
		}
	}

	@Override
	public void animationTimeChanged(double time) {
		activateTime(time);
	}

}
