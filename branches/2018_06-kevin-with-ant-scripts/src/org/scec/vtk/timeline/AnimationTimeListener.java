package org.scec.vtk.timeline;

public interface AnimationTimeListener {
	
	/**
	 * 
	 * @param curTime in seconds
	 */
	public void animationTimeChanged(double curTime);
	
	public void animationBoundsChanged(double maxTime);

}
