package org.scec.vtk.commons.opensha.gui.anim;

import javax.swing.JSlider;

import org.scec.vtk.commons.opensha.gui.EventManager;

public class AnimThread extends Thread {
	
	private static final boolean D = false;

	private JSlider slider;
	private boolean pause = false;
	private boolean loop = false;
	
	private StepTimeCalculator timeCalc;
	
	public AnimThread(JSlider slider, StepTimeCalculator timeCalc) {
		this.slider = slider;
		this.timeCalc = timeCalc;
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
		if (slider.getValue() == slider.getMaximum())
			slider.setValue(slider.getMinimum());
		int currentStep = slider.getValue();
		long start = System.currentTimeMillis();
		if (currentStep > 1)
			start -= timeCalc.getTimeUntil(0l, currentStep);
		
		int newStep;
		long milis;
		while (!pause && currentStep < slider.getMaximum()) {
			milis = System.currentTimeMillis() - start;
			if (D) System.out.println("curStep = "+currentStep+". it's been " + milis + " milis!");
			newStep = timeCalc.getStepForTime(currentStep, milis);
			if (D) System.out.println("newStep = " + newStep);
			if (newStep <= currentStep) {
				long sleepTime = timeCalc.getTimeUntil(milis, currentStep+1);
				if (sleepTime > 0) {
					if (D) System.out.println("Sleeping for " + sleepTime + " milis");
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {}
				}
			} else {
				currentStep = newStep;
				slider.setValue(currentStep);
				EventManager.flushRenders();
			}
			if (loop && currentStep >= slider.getMaximum()) {
				slider.setValue(slider.getMinimum());
				EventManager.flushRenders();
				currentStep = slider.getValue();
				start = System.currentTimeMillis();
				if (currentStep > 1)
					start -= timeCalc.getTimeUntil(0l, currentStep);
			}
		}
		
//		for (int i=slider.getValue(); !pause && i<=slider.getMaximum(); i++) {
//			slider.setValue(i);
//		}
	}

}
