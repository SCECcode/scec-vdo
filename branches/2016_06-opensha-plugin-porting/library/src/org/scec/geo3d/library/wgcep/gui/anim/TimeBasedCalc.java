package org.scec.geo3d.library.wgcep.gui.anim;

import org.scec.geo3d.library.wgcep.faults.anim.TimeBasedFaultAnimation;

import com.google.common.base.Preconditions;

public class TimeBasedCalc implements StepTimeCalculator {
	
	private static final boolean D = false;
	
	private TimeBasedFaultAnimation anim;
	private int maxStep;
	double multiplier;
	
	public TimeBasedCalc(TimeBasedFaultAnimation anim, int maxStep, double duration) {
		this.anim = anim;
		this.maxStep = maxStep;
		
		double minTime = anim.getTimeForStep(0);
		double maxTime = anim.getTimeForStep(maxStep-1);
		double animUnitsDuration = maxTime - minTime;
		Preconditions.checkState(animUnitsDuration > 0, "the time difference for the min and max step must be > 0");
		multiplier = duration / maxTime;
	}

	@Override
	public int getStepForTime(int prevStep, long milis) {
		double secs = milis / 1000d;
		if (D) System.out.println("TimeBasedCalc: getting step for time: "+milis+" (= " + secs + " secs)");
		double stepTime = 0;
		int step = prevStep;
		for (int i=step; i<=maxStep; i++) {
			stepTime = anim.getTimeForStep(i-1); // colorer steps start at 0, so step-1
			if (D) System.out.println("TimeBasedCalc: loop for step: "+i+" unscaled time: "+stepTime);
			stepTime  *= multiplier; // scale it to our animation time
			if (D) System.out.println("TimeBasedCalc: scaled time: "+stepTime);
			if (stepTime < secs) {
				step = i;
			} else {
				if (D) System.out.println("Went too far...breaking loop");
				break;
			}
		}
		return step;
	}

	@Override
	public long getTimeUntil(long milis, int step) {
		double secsForNext = anim.getTimeForStep(step-1) * multiplier;
		if (D) System.out.println("TimeBasedCalc getTimeUntil: step "+step+" is at "+secsForNext);
		long milisToSleep = (long)(secsForNext * 1000l) - milis;
		if (D) System.out.println("Should sleep for "+milisToSleep+" milis");
		return milisToSleep;
	}

}
