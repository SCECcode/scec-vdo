package org.scec.geo3d.library.wgcep.faults.anim;

import javax.swing.event.ChangeListener;

import org.opensha.commons.data.Named;
import org.opensha.commons.param.ParameterList;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;
import org.scec.geo3d.library.wgcep.faults.colorers.FaultColorer;

public interface FaultAnimation extends Named {
	
	public void addRangeChangeListener(ChangeListener l);
	
	/**
	 * The number of steps in the animation
	 * 
	 * @return
	 */
	public int getNumSteps();
	
	/**
	 * Sets the current step in the animation, where 0 is the first step, or -1 for no step.
	 * @param step
	 */
	public void setCurrentStep(int step);
	
	/**
	 * Called whenever the number of steps changes to select the preferred initial step.
	 * 
	 * @return
	 */
	public int getPreferredInitialStep();
	
	/**
	 * @return true if the current step should be included in the label, false otherwise
	 */
	public boolean includeStepInLabel();
	
	public String getCurrentLabel();
	
	public ParameterList getAnimationParameters();
	
	/**
	 * Get the visibility state of the given fault, or null for no change.
	 * 
	 * @param fault
	 * @return
	 */
	public Boolean getFaultVisibility(AbstractFaultSection fault);
	
	public FaultColorer getFaultColorer();
	
	public void fireRangeChangeEvent();

}
