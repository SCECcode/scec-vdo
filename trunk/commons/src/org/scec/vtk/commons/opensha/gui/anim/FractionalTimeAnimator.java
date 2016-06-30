package org.scec.vtk.commons.opensha.gui.anim;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import org.opensha.commons.util.ExceptionUtils;

public class FractionalTimeAnimator {
	
	private JSlider slider;
	private StepTimeCalculator timeCalc;
	
	private int curStep;
	private long totTimeMillis;
	private double curFractTime;
	
	private Runnable updateSliderRunnable = new Runnable() {
		
		@Override
		public void run() {
			slider.setValue(curStep);
		}
	};
	
	public FractionalTimeAnimator(JSlider slider, StepTimeCalculator timeCalc) {
		this.slider = slider;
		this.timeCalc = timeCalc;
		
		curStep = slider.getValue();
		totTimeMillis = timeCalc.getTimeUntil(0l, slider.getMaximum());
		long curTimeMillis = timeCalc.getTimeUntil(0l, curStep);
		curFractTime = (double)curTimeMillis/(double)totTimeMillis;
	}
	
	public synchronized void goToTime(double fractionalTime) {
		int prevStep;
		if (fractionalTime < curFractTime) {
			// went backwards, must do full search
			prevStep = slider.getMinimum();
		} else {
			prevStep = curStep;
		}
		long millis = (long)(totTimeMillis * fractionalTime);
		curFractTime = fractionalTime;
		curStep = timeCalc.getStepForTime(prevStep, millis);
		// update slider in event dispatch thread
		if (SwingUtilities.isEventDispatchThread()) {
			updateSliderRunnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(updateSliderRunnable);
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
	}

}
