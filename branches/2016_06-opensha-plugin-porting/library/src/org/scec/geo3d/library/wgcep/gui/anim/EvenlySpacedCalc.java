package org.scec.geo3d.library.wgcep.gui.anim;

public class EvenlySpacedCalc implements StepTimeCalculator {
	
	private double maxStep;
	private double duration;
	
	public EvenlySpacedCalc(int maxStep, double duration) {
		this.maxStep = maxStep-1; // normalized to 0-based
		this.duration = duration;
//		System.out.println("maxStep: " + maxStep);
//		System.out.println("duration: " + duration);
	}

	@Override
	public int getStepForTime(int prevStep, long milis) {
		double secs = milis / 1000d;
		
//		System.out.println("maxStep * secs = " + (maxStep * secs));
//		System.out.println("maxStep * secs / duration = " + (maxStep * secs / duration));
		return (int)(maxStep * secs / duration)+1;
	}
	
	@Override
	public long getTimeUntil(long milis, int currentStep) {
		currentStep -= 1; // normalized to 0-based
		double secsForNext = currentStep * duration / maxStep;
		return (long)(secsForNext * 1000l) - milis;
	}

}
