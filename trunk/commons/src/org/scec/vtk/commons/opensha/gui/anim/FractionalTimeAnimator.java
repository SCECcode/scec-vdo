package org.scec.vtk.commons.opensha.gui.anim;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import org.opensha.commons.util.ExceptionUtils;
import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;

public class FractionalTimeAnimator {
	
	private AnimationPanel animPanel;
	private FaultAnimation anim;
	private StepTimeCalculator timeCalc;
	
	private int curStep;
	private long totTimeMillis;
	private double curFractTime;
	
	public FractionalTimeAnimator(AnimationPanel animPanel, FaultAnimation anim, StepTimeCalculator timeCalc) {
		this.animPanel = animPanel;
		this.anim = anim;
		this.timeCalc = timeCalc;
		
		curStep = animPanel.getCurrentStep();
		totTimeMillis = timeCalc.getAnimTimeUntil(0l, anim.getNumSteps()-1);
		long curTimeMillis = timeCalc.getAnimTimeUntil(0l, curStep);
		curFractTime = (double)curTimeMillis/(double)totTimeMillis;
	}
	
	public synchronized void goToTime(double fractionalTime) {
		double animTime = fractionalTime*animPanel.getAnimDuration();
		animPanel.setCurrentAnimTime(animTime);
//		int prevStep;
//		if (fractionalTime < curFractTime) {
//			// went backwards, must do full search
//			prevStep = 0;
//		} else {
//			prevStep = curStep;
//		}
//		long millis = (long)(totTimeMillis * fractionalTime);
//		curFractTime = fractionalTime;
//		curStep = timeCalc.getStepForAnimTime(prevStep, millis);
//		animPanel.setCurrentStep(curStep);
		
	}

}
