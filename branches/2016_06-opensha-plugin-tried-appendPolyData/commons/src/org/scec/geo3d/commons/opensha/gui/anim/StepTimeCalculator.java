package org.scec.geo3d.commons.opensha.gui.anim;

public interface StepTimeCalculator {
	
	public int getStepForTime(int prevStep, long milis);
	
	public long getTimeUntil(long milis, int currentStep);

}
