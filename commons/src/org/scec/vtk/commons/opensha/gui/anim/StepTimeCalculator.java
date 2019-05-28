package org.scec.vtk.commons.opensha.gui.anim;

public interface StepTimeCalculator {
	
	/**
	 * @param secs into the animation, which is <= duration of current animation (NOT ABSOLUTE TIME FOR TIME BASED)
	 * @return step for the given time in the animation
	 */
	public int getStepForAnimTimeSecs(double secs);
	
	/**
	 * @param prevStep
	 * @param secs into the animation, which is <= duration of current animation (NOT ABSOLUTE TIME FOR TIME BASED)
	 * @return step for the given time in the animation
	 */
	public int getStepForAnimTime(int prevStep, double secs);
	
	/**
	 * Convert a time from the animation time frame to absolute time
	 * @param animTimeSecs
	 * @return
	 */
	public double getAbsoluteTime(double animTimeSecs);
	
	/**
	 * Convert a time from absolute time frame to animation time
	 * @param absoluteTimeSecs
	 * @return
	 */
	public double getAnimTime(double absoluteTimeSecs);
	
	/**
	 * @param seconds current time in the animation (NOT ABSOLUTE TIME FOR TIME BASED)
	 * @param step
	 * @return animation time until the next step
	 */
	public double getAnimTimeUntil(double secs, int step);

}
