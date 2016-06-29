package org.scec.vtk.timeline.camera;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.vtk.timeline.KeyFrame;
import org.scec.vtk.timeline.KeyFrameChangeListener;
import org.scec.vtk.timeline.KeyFrameList;
import org.scec.vtk.timeline.camera.CameraAnimator.SplineElement;
import org.scec.vtk.timeline.camera.CameraAnimator.SplineType;

import com.google.common.base.Preconditions;

import vtk.vtkCamera;
import vtk.vtkSpline;

public class CameraSplineCalculator implements KeyFrameChangeListener {
	
	private KeyFrameList keys;
	private SplineElement element;
	private SplineType type;
	
	// this maps real time to camera time by incorperating pauses
	private ArbitrarilyDiscretizedFunc timeMapFunc;
	
	private vtkSpline splineX;
	private vtkSpline splineY;
	private vtkSpline splineZ;
	
	public CameraSplineCalculator(KeyFrameList keys, SplineElement element, SplineType type) {
		this.keys = keys;
		this.element = element;
		this.type = type;
		keys.addKeyFrameChangeListener(this);
	}
	
	public double[] getPoint(double time) {
		Preconditions.checkArgument(Double.isFinite(time));
		synchronized (keys) {
			checkBuildSpline();
			if (keys.isEmpty())
				return null;
			if (keys.size() == 1)
				return getPoints((CameraKeyFrame)keys.getKeyAt(0));
			double t;
			if (time <= timeMapFunc.getMinX())
				t = 0;
			else if (time >= timeMapFunc.getMaxX())
				t = timeMapFunc.getMaxY();
			else
				t = timeMapFunc.getInterpolatedY(time);
//			System.out.println("Getting "+element.name()+" for time="+time+" (spline time="+t+")");
			Preconditions.checkState(Double.isFinite(t));
			return new double[] { splineX.Evaluate(t), splineY.Evaluate(t), splineZ.Evaluate(t) };
		}
	}
	
	public void setSplineType(SplineType type) {
		this.type = type;
		clearSpline();
	}
	
	// synchronized externally
	private void checkBuildSpline() {
		if (splineX == null && !keys.isEmpty()) {
			splineX = type.instance();
			splineY = type.instance();
			splineZ = type.instance();
			
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
				
				double[] pts = getPoints(camKey);
				
				splineX.AddPoint(splineTime, pts[0]);
				splineY.AddPoint(splineTime, pts[1]);
				splineZ.AddPoint(splineTime, pts[2]);
			}
		}
	}
	
	private double[] getPoints(CameraKeyFrame camKey) {
		vtkCamera cam = camKey.getCam();
		switch (element) {
		case POSITION:
			return cam.GetPosition();
		case FOCAL_POINT:
			return cam.GetFocalPoint();
		case VIEW_UP:
			return cam.GetViewUp();

		default:
			throw new IllegalStateException("Unknown spline element: "+element);
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
