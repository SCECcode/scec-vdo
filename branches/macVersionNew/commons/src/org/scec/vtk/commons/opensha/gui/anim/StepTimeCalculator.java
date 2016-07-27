package org.scec.vtk.commons.opensha.gui.anim;

public interface StepTimeCalculator {
	
	/**
	 * @param millis into the animation, which is <= duration of current animation (NOT ABSOLUTE TIME FOR TIME BASED)
	 * @return step for the given time in the animation
	 */
	public int getStepForAnimTime(long millis);
	
	/**
	 * @param secs into the animation, which is <= duration of current animation (NOT ABSOLUTE TIME FOR TIME BASED)
	 * @return step for the given time in the animation
	 */
	public int getStepForAnimTime(double secs);
	
	/**
	 * @param prevStep
	 * @param millis into the animation, which is <= duration of current animation (NOT ABSOLUTE TIME FOR TIME BASED)
	 * @return step for the given time in the animation
	 */
	public int getStepForAnimTime(int prevStep, long millis);
	
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
	 * @param milis current time in the animation (NOT ABSOLUTE TIME FOR TIME BASED)
	 * @param step
	 * @return animation time until the next step
	 */
	public long getAnimTimeUntil(long milis, int step);

}
