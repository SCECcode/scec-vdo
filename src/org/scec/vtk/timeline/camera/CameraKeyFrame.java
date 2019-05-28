package org.scec.vtk.timeline.camera;

import org.scec.vtk.timeline.KeyFrame;

import vtk.vtkCamera;

public class CameraKeyFrame extends KeyFrame {
	
	private vtkCamera cam;
	private boolean pause;

	public CameraKeyFrame(double startTime, vtkCamera c, boolean pause) {
		super(startTime);
		
		cam = new vtkCamera();
		cam.SetPosition(c.GetPosition()[0], c.GetPosition()[1], c.GetPosition()[2]);
		cam.SetFocalPoint(c.GetFocalPoint()[0], c.GetFocalPoint()[1], c.GetFocalPoint()[2]);
		cam.SetViewUp(c.GetViewUp()[0], c.GetViewUp()[1], c.GetViewUp()[2]);
		this.pause = pause;
	}
	
	public vtkCamera getCam() {
		return cam;
	}

	@Override
	public void load() {
		CameraAnimator.setActiveCamera(getCam());
	}
	
	public boolean isPause() {
		return pause;
	}

	@Override
	public KeyFrame duplicate() {
		return new CameraKeyFrame(getStartTime(), getCam(), isPause());
	}
	
	@Override
	public String toString() {
		return "CameraKeyFrame("+getStartTime()+"s, pause="+isPause()+")";
	}

}
