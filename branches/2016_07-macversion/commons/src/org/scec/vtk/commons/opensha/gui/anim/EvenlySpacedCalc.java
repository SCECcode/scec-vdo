package org.scec.vtk.commons.opensha.gui.anim;

import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.anim.TimeBasedFaultAnimation;

public class EvenlySpacedCalc implements StepTimeCalculator {
	
	private final double maxStep;
	private final double duration;
	
	private final double absMinTime;
	private final double absDuration;
	
	public EvenlySpacedCalc(FaultAnimation faultAnim, int maxStep, double duration) {
		this.maxStep = maxStep;
		this.duration = duration;
//		System.out.println("maxStep: " + maxStep);
//		System.out.println("duration: " + duration);
		if (faultAnim instanceof TimeBasedFaultAnimation) {
			TimeBasedFaultAnimation timeAnim = (TimeBasedFaultAnimation)faultAnim;
			absMinTime = timeAnim.getTimeForStep(0);
			absDuration = timeAnim.getTimeForStep(timeAnim.getNumSteps()-1);
		} else {
			absMinTime = 0d;
			absDuration = duration;
		}
	}
	
	@Override
	public int getStepForAnimTime(long milis) {
		return getStepForAnimTime(-1, milis);
	}

	@Override
	public int getStepForAnimTime(int prevStep, long milis) {
		double secs = milis / 1000d;
		return getStepForAnimTime(prevStep, secs);
	}

	@Override
	public int getStepForAnimTime(double secs) {
		return getStepForAnimTime(-1, secs);
	}
	
	public int getStepForAnimTime(int prevStep, double secs) {
//		System.out.println("maxStep * secs = " + (maxStep * secs));
//		System.out.println("maxStep * secs / duration = " + (maxStep * secs / duration));
		return (int)(maxStep * secs / duration);
	}
	
	@Override
	public long getAnimTimeUntil(long milis, int currentStep) {
		double secsForNext = currentStep * duration / maxStep;
		return (long)(secsForNext * 1000l) - milis;
	}

	@Override
	public double getAbsoluteTime(double animTimeSecs) {
		return absMinTime + absDuration*(animTimeSecs/duration);
	}

	@Override
	public double getAnimTime(double absoluteTimeSecs) {
		return (absoluteTimeSecs - absMinTime)*duration/absoluteTimeSecs;
	}

}
