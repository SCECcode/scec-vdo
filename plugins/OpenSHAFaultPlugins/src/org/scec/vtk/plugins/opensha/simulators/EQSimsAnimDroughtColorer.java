package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeListener;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.TimeBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.gui.EventManager;

public class EQSimsAnimDroughtColorer extends CPTBasedColorer
		implements TimeBasedFaultAnimation, EQSimsEventListener, ParameterChangeListener {

	private static CPT getDefaultCPT() {
		CPT cpt = new CPT(0d, 1000d, Color.BLUE, Color.RED);
		cpt.setNanColor(Color.GRAY);
		cpt.setBelowMinColor(cpt.getMinColor());
		cpt.setAboveMaxColor(cpt.getMaxColor());
		return cpt;
	}
	
	private EventManager eventManager;
	
	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	
	//Title on drop down menu
	private static String TITLE = "Drought Duration Animation";
	
	//Min magnitude option
	private static final String MIN_MAG_PARAM_NAME = "Min Mag";
	private static final Double MIN_MAG_PARAM = 0d;
	private static final Double MAX_MAG_PARAM = 10d;
	private DoubleParameter minMagParam = 
			new DoubleParameter(MIN_MAG_PARAM_NAME, MIN_MAG_PARAM, MAX_MAG_PARAM, MIN_MAG_PARAM);
	
	//Color Bounds
	private static final String MAX_VALUE_COLOR_WHEEL = "Drought Indicator";
	private static final int MIN_YEAR_PARAM = 100;
	private static final int MAX_YEAR_PARAM = 1000;
	private DoubleParameter droughtYearParam = 
			new DoubleParameter(MAX_VALUE_COLOR_WHEEL, MIN_YEAR_PARAM, MAX_YEAR_PARAM);
	
	//Parameter List
	private ParameterList animParams = new ParameterList();
	
	public EQSimsAnimDroughtColorer() {
		super(getDefaultCPT(), false);
		//add minMag
		animParams.addParameter(minMagParam);
		minMagParam.addParameterChangeListener(this);
		
		//add Color Bounds
		animParams.addParameter(droughtYearParam);
		droughtYearParam.addParameterChangeListener(this);
		
	}
	
	//we dont know why yet 
	public void setEventManager(EventManager eventManager) {
		this.eventManager = eventManager; 
	}

	@Override
	public void addRangeChangeListener(ChangeListener l) {
		listeners.add(l);

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
		return animParams;
	}

	@Override
	public Boolean getFaultVisibility(AbstractFaultSection fault) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FaultColorer getFaultColorer() {
		return this;
	}

	@Override
	public void fireRangeChangeEvent() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return TITLE;
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
	
	//new functions added below
	@Override
	public void setCPT(CPT cpt) {
		super.setCPT(cpt);
	}
	
	
	
	

}
