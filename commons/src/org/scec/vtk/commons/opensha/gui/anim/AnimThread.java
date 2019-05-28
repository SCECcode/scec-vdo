package org.scec.vtk.commons.opensha.gui.anim;

import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.gui.EventManager;

import com.google.common.base.Preconditions;

public class AnimThread extends Thread {
	
	private static final boolean D = false;
	
	private static final long max_sleep_millis_time_based = 1000l/30l; // appox 30 fps when playing back time based

	private final AnimationPanel animPanel;
	private final FaultAnimation anim;
	private boolean pause = false;
	private boolean loop = false;
	private final boolean timeBased;
	private final double durationSeconds;
	
	private final StepTimeCalculator timeCalc;
	
	public AnimThread(AnimationPanel animPanel, FaultAnimation anim, StepTimeCalculator timeCalc, double durationSeconds) {
		Preconditions.checkNotNull(animPanel);
		this.animPanel = animPanel;
		Preconditions.checkNotNull(anim);
		this.anim = anim;
		Preconditions.checkNotNull(timeCalc);
		this.timeCalc = timeCalc;
		timeBased = animPanel.isTimeBasedEnabled();
		this.durationSeconds = durationSeconds;
	}
	
	protected void pause() {
		pause = true;
	}
	
	protected void setLoop(boolean loop) {
		this.loop = loop;
	}
	
	public boolean isLoop() {
		return loop;
	}
	
	@Override
	public void run() {
		pause = false;
		if (animPanel.getCurrentStep() == (anim.getNumSteps()-1))
			animPanel.setCurrentStep(0);
		int currentStep = animPanel.getCurrentStep();
		long start = System.currentTimeMillis();
		if (currentStep > 0)
			start -= (long)(timeCalc.getAnimTimeUntil(0l, currentStep)*1000d+0.5);
		
		long millisEnd;
		if (timeBased)
			millisEnd = (long)(durationSeconds*1000l+0.5);
		else
			millisEnd = (long)(timeCalc.getAnimTimeUntil(0l, anim.getNumSteps()-1)*1000d+0.5);
		
		int newStep;
		boolean stepsLeft;
		long millis;
		while (!pause) {
			millis = System.currentTimeMillis() - start;
			// check to see if we're done
			stepsLeft = currentStep < (anim.getNumSteps()-1);
			if (timeBased) {
				// check duration
				if (!stepsLeft && millis > millisEnd)
					break;
			} else {
				if (!stepsLeft)
					break;
			}
			if (D) System.out.println("curStep = "+currentStep+". it's been " + millis + " milis (end = "+millisEnd+")!");
			newStep = timeCalc.getStepForAnimTime(currentStep, ((double)millis)/1000d);
			if (D) System.out.println("newStep = " + newStep+", numSteps = "+anim.getNumSteps());
			if (newStep <= currentStep) {
				if (timeBased) {
					// set current time
					animPanel.setCurrentAnimTime(millis/1000d);
					EventManager.flushRenders();
				}
				long sleepTime = (long)(timeCalc.getAnimTimeUntil(millis, currentStep+1)*1000d+0.5);
				if (timeBased && sleepTime > max_sleep_millis_time_based)
					sleepTime = max_sleep_millis_time_based;
				if (sleepTime > 1) {
					if (D) System.out.println("Sleeping for " + sleepTime + " milis");
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {}
				}
			} else {
				currentStep = newStep;
				animPanel.setCurrentStep(currentStep);
				EventManager.flushRenders();
			}
			if (loop && currentStep >= anim.getNumSteps()-1) {
				animPanel.setCurrentStep(0);
				EventManager.flushRenders();
				currentStep = 0;
				start = System.currentTimeMillis();
				if (currentStep > 0)
					start -= (long)(timeCalc.getAnimTimeUntil(0l, currentStep)*1000d+0.5);
			}
		}
		if (D) System.out.println("ending anim loop");
		animPanel.enableAnimControlsAfterAnimThread();
	}

}
