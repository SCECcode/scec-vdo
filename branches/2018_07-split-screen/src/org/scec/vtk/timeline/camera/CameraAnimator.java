package org.scec.vtk.timeline.camera;

import javax.swing.SwingUtilities;

import org.opensha.commons.util.ExceptionUtils;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.timeline.AnimationTimeListener;
import org.scec.vtk.timeline.KeyFrameList;

import vtk.vtkCamera;
import vtk.vtkCardinalSpline;
import vtk.vtkKochanekSpline;
import vtk.vtkRenderer;
import vtk.vtkSCurveSpline;
import vtk.vtkSpline;

public class CameraAnimator implements AnimationTimeListener {
	
	private KeyFrameList keys;
	
	public enum SplineElement {
		POSITION,
		FOCAL_POINT,
		VIEW_UP
	}
	
	public enum SplineType {
		CARDINAL("Cardinal") {
			@Override
			vtkSpline instance() {
				return new vtkCardinalSpline();
			}
		},
		KOCHANEK("Kochanek") {
			@Override
			vtkSpline instance() {
				return new vtkKochanekSpline();
			}
		},
		S_CURVE("S Curve") {
			@Override
			vtkSpline instance() {
				return new vtkSCurveSpline();
			}
		};
		
		private String name;
		
		private SplineType(String name) {
			this.name = name;
		}
		
		abstract vtkSpline instance();
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private CameraSplineCalculator positionSpline;
	private CameraSplineCalculator focalSpline;
	private CameraSplineCalculator upSpline;
	
	private SplineType type;
	
	private vtkCamera cam;
	
	public CameraAnimator(KeyFrameList keys, SplineType type) {
		this.keys = keys;
		this.type = type;
		
		positionSpline = new CameraSplineCalculator(keys, SplineElement.POSITION, type);
		focalSpline = new CameraSplineCalculator(keys, SplineElement.FOCAL_POINT, type);
		upSpline = new CameraSplineCalculator(keys, SplineElement.VIEW_UP, type);
	}
	
	public void setSplineType(SplineType type) {
		this.type = type;
		positionSpline.setSplineType(type);
		focalSpline.setSplineType(type);
		upSpline.setSplineType(type);
	}
	
	public SplineType getSplineType() {
		return type;
	}
	
	public void activateTime(double time) {
		synchronized (keys) {
			if (keys.isEmpty())
				return;
			
			double[] position = positionSpline.getPoint(time);
			double[] focal = focalSpline.getPoint(time);
			double[] up = upSpline.getPoint(time);
			
			if (cam == null)
//				cam = new vtkCamera();
				cam = MainGUI.getRenderWindow().getActiveCamera();
			
			cam.SetPosition(position[0], position[1], position[2]);
			cam.SetFocalPoint(focal[0], focal[1], focal[2]);
			cam.SetViewUp(up[0], up[1], up[2]);
			cam.OrthogonalizeViewUp();

			setActiveCamera(cam);
		}
	}
	
	static void setActiveCamera(final vtkCamera cam) {
		final vtkRenderer renderer = MainGUI.getRenderWindow().getRenderWindow().GetRenderers().GetFirstRenderer();
		setActiveCamera(cam, renderer);
		if (MainGUI.getRenderWindowSplit() != null) {
			final vtkRenderer splitRenderer = MainGUI.getRenderWindowSplit().getRenderWindow().GetRenderers().GetFirstRenderer();
			setActiveCamera(cam, splitRenderer);
		}
	}
	
	static void setActiveCamera(final vtkCamera cam, final vtkRenderer renderer) {
		Runnable run = new Runnable() {
			public void run() {	
//				MainGUI.updateRenderWindow();
				renderer.SetActiveCamera(cam);
				renderer.ResetCameraClippingRange();
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			run.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(run);
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);;
			}
		}
	}

	@Override
	public void animationTimeChanged(double curTime) {
		activateTime(curTime);
	}

	@Override
	public void animationBoundsChanged(double maxTime) {}

}
