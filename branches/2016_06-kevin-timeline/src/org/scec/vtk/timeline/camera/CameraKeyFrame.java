package org.scec.vtk.timeline.camera;

import org.scec.vtk.timeline.KeyFrame;

import vtk.vtkCamera;

public class CameraKeyFrame extends KeyFrame {
	
	private vtkCamera cam;
	private boolean pause;

	public CameraKeyFrame(double startTime, vtkCamera cam, boolean pause) {
		super(startTime);
		
		this.cam = cam;
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

}
