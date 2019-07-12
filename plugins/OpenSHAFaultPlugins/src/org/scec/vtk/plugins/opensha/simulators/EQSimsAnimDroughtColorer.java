package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.util.List;

import javax.swing.event.ChangeListener;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.TimeBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;

public class EQSimsAnimDroughtColorer extends CPTBasedColorer
		implements TimeBasedFaultAnimation, EQSimsEventListener, ParameterChangeListener {

	private static CPT getDefaultCPT() {
		CPT cpt = new CPT(0d, 1000d, Color.BLUE, Color.RED);
		cpt.setNanColor(Color.GRAY);
		cpt.setBelowMinColor(cpt.getMinColor());
		cpt.setAboveMaxColor(cpt.getMaxColor());
		return cpt;
	}
	
	public EQSimsAnimDroughtColorer() {
		super(getDefaultCPT(), false);
	}

	@Override
	public void addRangeChangeListener(ChangeListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumSteps() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setCurrentStep(int step) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPreferredInitialStep() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean includeStepInLabel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getCurrentLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParameterList getAnimationParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getFaultVisibility(AbstractFaultSection fault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FaultColorer getFaultColorer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fireRangeChangeEvent() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parameterChange(ParameterChangeEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEvents(List<? extends SimulatorEvent> events) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGeometry(List<SimulatorElement> elements) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getTimeForStep(int step) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCurrentDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean timeChanged(double time) {
		// this will be called whenever a new frame is rendered, regardless of if there was an event
		return false;
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		return 0;
	}

	@Override
	public Color getColorForValue(double value) {
		Color color = super.getColorForValue(value);
		// if saturation, change color here
		return color;
	}
	
	

}
