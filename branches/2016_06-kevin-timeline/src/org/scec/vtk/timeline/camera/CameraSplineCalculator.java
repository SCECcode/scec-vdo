package org.scec.vtk.timeline.camera;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.vtk.timeline.KeyFrame;
import org.scec.vtk.timeline.KeyFrameChangeListener;
import org.scec.vtk.timeline.KeyFrameList;
import org.scec.vtk.timeline.camera.CameraAnimator.SplineElement;

import com.google.common.base.Preconditions;

import vtk.vtkCamera;
import vtk.vtkCardinalSpline;
import vtk.vtkSpline;

public class CameraSplineCalculator implements KeyFrameChangeListener {
	
	private KeyFrameList keys;
	private SplineElement element;
	
	// this maps real time to camera time by incorperating pauses
	private ArbitrarilyDiscretizedFunc timeMapFunc;
	
	private vtkSpline splineX;
	private vtkSpline splineY;
	private vtkSpline splineZ;
	
	public CameraSplineCalculator(KeyFrameList keys, SplineElement element) {
		this.keys = keys;
		this.element = element;
		keys.addKeyFrameChangeListener(this);
	}
	
	public double[] getPoint(double time) {
		Preconditions.checkArgument(Double.isFinite(time));
		synchronized (keys) {
			checkBuildSpline();
			if (keys.isEmpty())
				return null;
			double t;
			if (time <= timeMapFunc.getMinX())
				t = 0;
			else if (time >= timeMapFunc.getMaxX())
				t = timeMapFunc.getMaxY();
			else
				t = timeMapFunc.getInterpolatedY(time);
			Preconditions.checkState(Double.isFinite(t));
			return new double[] { splineX.Evaluate(t), splineY.Evaluate(t), splineZ.Evaluate(t) };
		}
	}
	
	// synchronized externally
	private void checkBuildSpline() {
		if (splineX == null && !keys.isEmpty()) {
			// TODO allow other spline types
			splineX = new vtkCardinalSpline();
			splineY = new vtkCardinalSpline();
			splineZ = new vtkCardinalSpline();
			
			timeMapFunc = new ArbitrarilyDiscretizedFunc();
			
			double splineTime = 0d;
			double prevTime = keys.getKeyAt(0).getStartTime();
			boolean paused = false;
			
			for (KeyFrame key : keys) {
				Preconditions.checkState(key instanceof CameraKeyFrame);
				CameraKeyFrame camKey = (CameraKeyFrame)key;
				double time = camKey.getStartTime();
				
				if (!paused)
					splineTime += (time - prevTime);
				
				timeMapFunc.set(time, splineTime);
				prevTime = time;
				paused = camKey.isPause();
				
				vtkCamera cam = camKey.getCam();
				double[] pts;
				switch (element) {
				case POSITION:
					pts = cam.GetPosition();
					break;
				case FOCAL_POINT:
					pts = cam.GetFocalPoint();
					break;
				case VIEW_UP:
					pts = cam.GetViewUp();
					break;

				default:
					throw new IllegalStateException("Unknown spline element: "+element);
				}
				
				splineX.AddPoint(splineTime, pts[0]);
				splineY.AddPoint(splineTime, pts[1]);
				splineZ.AddPoint(splineTime, pts[2]);
			}
		}
	}
	
	public void clearSpline() {
		synchronized (keys) {
			splineX = null;
			splineY = null;
			splineZ = null;
		}
	}

	@Override
	public void keyChanged(KeyFrame key) {
		clearSpline();
//		// synchronized externally by the KeyFrameList itself
//		// don't call the synchronized clearSpline() method above or deadlock
//		splineX = null;
//		splineY = null;
//		splineZ = null;
	}

}
