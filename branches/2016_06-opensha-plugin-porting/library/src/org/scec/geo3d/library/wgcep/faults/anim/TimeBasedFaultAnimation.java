package org.scec.geo3d.library.wgcep.faults.anim;

public interface TimeBasedFaultAnimation extends FaultAnimation {
	
	/**
	 * Return the time (in seconds) for the given step
	 * 
	 * @param step
	 * @return
	 */
	public double getTimeForStep(int step);

}
