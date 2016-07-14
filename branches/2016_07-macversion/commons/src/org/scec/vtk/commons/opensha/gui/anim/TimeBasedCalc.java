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
	}
	
	@Override
	public synchronized int getStepForAnimTime(long milis) {
		int step = getStepForAnimTime(prevStep, milis);
		prevStep = step;
		return step;
	}

	@Override
	public int getStepForAnimTime(int prevStep, long milis) {
		double secs = milis / 1000d;
		if (D) System.out.println("TimeBasedCalc: getting step for time: "+milis+" (= " + secs + " secs)");
		return getStepForAnimTime(prevStep, secs);
	}

	@Override
	public synchronized int getStepForAnimTime(double secs) {
		int step = getStepForAnimTime(prevStep, secs);
		prevStep = step;
		return step;
	}
	
	@Override
	public int getStepForAnimTime(int prevStep, double secs) {
		if (D) System.out.println("TimeBasedCalc: Getting step for anim time: "+secs);
		double stepTime;
		if (anim.getTimeForStep(prevStep) > secs)
			prevStep = 0;
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
	public long getAnimTimeUntil(long milis, int step) {
		double absTime = anim.getTimeForStep(step);
		double aninTime = getAnimTime(absTime);
		if (D) System.out.println("TimeBasedCalc getTimeUntil: step "+step+" is at "+aninTime);
		long milisToSleep = (long)(aninTime * 1000l) - milis;
		if (D) System.out.println("Should sleep for "+milisToSleep+" milis");
		return milisToSleep;
	}

	@Override
	public double getAbsoluteTime(double animTimeSecs) {
		return minTime + (animTimeSecs/multiplier);
	}

	@Override
	public double getAnimTime(double absoluteTimeSecs) {
		return (absoluteTimeSecs - minTime)*multiplier;
	}

}
