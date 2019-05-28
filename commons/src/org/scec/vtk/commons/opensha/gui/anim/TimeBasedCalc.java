package org.scec.vtk.commons.opensha.gui.anim;

import org.scec.vtk.commons.opensha.faults.anim.TimeBasedFaultAnimation;

import com.google.common.base.Preconditions;

public class TimeBasedCalc implements StepTimeCalculator {
	
	private static final boolean D = false;
	
	private final TimeBasedFaultAnimation anim;
	private final double multiplier;
	private final double minTime;
	
	private int prevStep;
	
	public TimeBasedCalc(TimeBasedFaultAnimation anim, int maxStep, double duration) {
		this.anim = anim;
		
		minTime = anim.getTimeForStep(0);
		double absoluteDuration = anim.getCurrentDuration();
		double maxTime = minTime + absoluteDuration;
		Preconditions.checkState(maxTime > minTime, "the time difference for the min and max step must be > 0");
		multiplier = duration / absoluteDuration;
		if (D) System.out.println("Created a new TimeBasedCalc with minTime="+minTime+", multiplier="+multiplier);
	}

	@Override
	public synchronized int getStepForAnimTimeSecs(double secs) {
		int step = getStepForAnimTime(prevStep, secs);
		prevStep = step;
		return step;
	}
	
	@Override
	public int getStepForAnimTime(int prevStep, double secs) {
		if (D) System.out.println("TimeBasedCalc: Getting step for anim time: "+secs+", prevStep="+prevStep);
		double stepTime;
		if (getAnimTime(anim.getTimeForStep(prevStep)) > secs) {
			prevStep = 0;
			if (D) System.out.println("TimeBasedCalc: resetting search to beginning");
		}
		int step = prevStep;
		for (int i=step; i<anim.getNumSteps(); i++) {
			// this is absolute time
			stepTime = anim.getTimeForStep(i);
			if (D) System.out.println("TimeBasedCalc: loop for step: "+i+" absolute time: "+stepTime);
			// convert to animation time
			stepTime = getAnimTime(stepTime);
			if (D) System.out.println("TimeBasedCalc: anim time: "+stepTime);
			if (stepTime <= secs) {
				step = i;
			} else {
				if (D) System.out.println("Went too far...breaking loop");
				break;
			}
		}
		return step;
	}

	@Override
	public double getAnimTimeUntil(double secs, int step) {
		double absTime = anim.getTimeForStep(step);
		double aninTime = getAnimTime(absTime);
		if (D) System.out.println("TimeBasedCalc getTimeUntil: step "+step+" is at "+aninTime);
		double secsToSleep = aninTime - secs;
		if (D) System.out.println("Should sleep for "+secsToSleep+" secs");
		return secsToSleep;
	}

	@Override
	public double getAbsoluteTime(double animTimeSecs) {
		if (D) System.out.println("TimeBasedCalc getAbsoluteTime: "+minTime+" + ("+animTimeSecs+"/"+multiplier+")");
		return minTime + (animTimeSecs/multiplier);
	}

	@Override
	public double getAnimTime(double absoluteTimeSecs) {
		return (absoluteTimeSecs - minTime)*multiplier;
	}

}
