package org.scec.vtk.timeline;

public interface CueAnimatorListener {
	
	public void animationStarted(boolean rendering);
	
	public void animationPaused(boolean rendering);
	
	public void animationFinished(boolean rendering);

}
