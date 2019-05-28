package org.scec.vtk.commons.opensha.faults.anim;

public interface TimeBasedFaultAnimation extends FaultAnimation {
	
	/**
	 * Return the time (in seconds) for the given step
	 * 
	 * @param step
	 * @return
	 */
	public double getTimeForStep(int step);
	
	/**
	 * 
	 * @return the current duration in seconds for the animation
	 */
	public double getCurrentDuration();
	
	/**
	 * Called whenever the time is changed, regardless of if it signifies a new step or not. Will be called after
	 * setCurrentStep(step) if the step is changed as well.
	 * @param time
	 * @return true if fault colors should be updated, false if no update required
	 */
	public boolean timeChanged(double time);

}
