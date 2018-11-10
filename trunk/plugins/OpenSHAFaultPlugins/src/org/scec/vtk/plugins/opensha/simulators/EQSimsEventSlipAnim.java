package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.RSQSimEvent;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.srf.RSQSimEventSlipTimeFunc;
import org.opensha.sha.simulators.srf.RSQSimStateTime;
import org.opensha.sha.simulators.srf.RSQSimStateTransitionFileReader;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.TimeBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;

import com.google.common.base.Preconditions;

public class EQSimsEventSlipAnim extends CPTBasedColorer implements TimeBasedFaultAnimation, EQSimsEventListener,
ParameterChangeListener {
	
	private enum DisplayType {
		CUMULATIVE_SLIP("Cumulative Slip (m)"),
		VELOCITY("Slip Velocity (m/s)");
		
		private String name;
		private DisplayType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private static DisplayType DISPLAY_TYPE_DEFAULT = DisplayType.CUMULATIVE_SLIP;
	
	private static CPT getDefaultSlipCPT() {
		try {
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, 10d);
			cpt.setNanColor(Color.GRAY);
			return cpt;
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
	
	private static CPT getDefaultVelocityCPT() {
		try {
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, 1d);
			cpt.setNanColor(Color.GRAY);
			return cpt;
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
	
	private static CPT getDefaultCPT() {
		switch (DISPLAY_TYPE_DEFAULT) {
		case CUMULATIVE_SLIP:
			return getDefaultSlipCPT();
		case VELOCITY:
			return getDefaultVelocityCPT();

		default:
			throw new IllegalStateException("Unknown display type: "+DISPLAY_TYPE_DEFAULT);
		}
	}
	
	private EnumParameter<DisplayType> displayParam;
	private IntegerParameter eventIDParam;
	private FileParameter transFileParam;
	private DoubleParameter slipVelParam;
	
	private ParameterList paramList;
	
	private List<SimulatorElement> elements;
	private List<? extends SimulatorEvent> events;
	private RSQSimEvent curEvent;
	private RSQSimEventSlipTimeFunc curSlipTimeFunc;
	private RSQSimStateTransitionFileReader transReader;
	private double curTime;
	
	private CPT slipCPT = null;
	private boolean slipCPTlog = false;
	
	private CPT velCPT = null;
	private boolean velCPTlog = false;
	
	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

	public EQSimsEventSlipAnim() {
		super(getDefaultCPT(), false);
		
		paramList = new ParameterList();
		
		transFileParam = new FileParameter("Trans. File");
		transFileParam.addParameterChangeListener(this);
		paramList.addParameter(transFileParam);
		
		displayParam = new EnumParameter<DisplayType>("Display Type",
				EnumSet.allOf(DisplayType.class), DISPLAY_TYPE_DEFAULT, null);
		displayParam.addParameterChangeListener(this);
		paramList.addParameter(displayParam);
		
		eventIDParam = new IntegerParameter("Event ID");
		eventIDParam.setValue(-1);
		eventIDParam.addParameterChangeListener(this);
		paramList.addParameter(eventIDParam);
		
		slipVelParam = new DoubleParameter("Slip Velocity", 0d, Double.POSITIVE_INFINITY);
		slipVelParam.setValue(1d);
		slipVelParam.setUnits("m/s");
		slipVelParam.addParameterChangeListener(this);
		paramList.addParameter(slipVelParam);
	}
	
	public void setInitialDir(File dir) {
		transFileParam.setDefaultInitialDir(dir);
	}

	@Override
	public void addRangeChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	@Override
	public int getNumSteps() {
		if (curSlipTimeFunc == null)
			return 0;
		// step 0 is start, step 1 is end
		return 2;
	}

	@Override
	public void setCurrentStep(int step) {
		// do nothing, all time based
	}

	@Override
	public int getPreferredInitialStep() {
		return 0;
	}

	@Override
	public boolean includeStepInLabel() {
		return false;
	}

	@Override
	public String getCurrentLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParameterList getAnimationParameters() {
		return paramList;
	}

	@Override
	public Boolean getFaultVisibility(AbstractFaultSection fault) {
		return null;
	}

	@Override
	public FaultColorer getFaultColorer() {
		return this;
	}

	@Override
	public void fireRangeChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}

	@Override
	public String getName() {
		return "RSQSim Rup Slip/Time Animation";
	}

	@Override
	public void setEvents(List<? extends SimulatorEvent> events) {
		this.events = events;
		eventChanged();
	}

	@Override
	public void setGeometry(List<SimulatorElement> elements) {
		this.elements = elements;
	}

	@Override
	public double getTimeForStep(int step) {
		checkCalcSlipFunc();
		if (step < 0 || curSlipTimeFunc == null)
			return 0;
		Preconditions.checkState(step <= 1, "Should either be -1, 0, or 1");
		if (step == 0)
			return 0;
		return curSlipTimeFunc.getEndTime() - curSlipTimeFunc.getStartTime();
	}

	@Override
	public double getCurrentDuration() {
		checkCalcSlipFunc();
		if (curSlipTimeFunc != null) {
			return curSlipTimeFunc.getEndTime() - curSlipTimeFunc.getStartTime();
		}
		return 0;
	}

	@Override
	public boolean timeChanged(double time) {
		this.curTime = time;
//		return curSlipTimeFunc != null;
		return true;
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		checkCalcSlipFunc();
		if (curSlipTimeFunc == null)
			return Double.NaN;
		switch (displayParam.getValue()) {
		case CUMULATIVE_SLIP:
			return curSlipTimeFunc.getCumulativeEventSlip(fault.getId(), curSlipTimeFunc.getStartTime()+curTime);
		case VELOCITY:
			return curSlipTimeFunc.getVelocity(fault.getId(), curSlipTimeFunc.getStartTime()+curTime);

		default:
			throw new IllegalStateException();
		}
	}
	
	private synchronized void checkCalcSlipFunc() {
		if (curSlipTimeFunc != null || curEvent == null || transReader == null)
			return;
		try {
			Map<Integer, List<RSQSimStateTime>> trans = transReader.getTransitions(curEvent);
			Map<Integer, Double> slipVels = null;
			if (!transReader.isVariableSlipSpeed()) {
				slipVels = new HashMap<>();
				for (SimulatorElement elem : elements)
					slipVels.put(elem.getID(), slipVelParam.getValue());
			}
			curSlipTimeFunc = new RSQSimEventSlipTimeFunc(trans, slipVels, transReader.isVariableSlipSpeed());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error calculating/loading slip time function",
					e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == transFileParam) {
			transFileChanged();
		} else if (event.getParameter() == eventIDParam) {
			eventChanged();
		} else if (event.getParameter() == slipVelParam) {
			curSlipTimeFunc = null;
			fireColorerChangeEvent();
		} else if (event.getParameter() == displayParam) {
			if (displayParam.getValue() == DisplayType.CUMULATIVE_SLIP) {
				// was velocity
				velCPT = getCPT();
				velCPTlog = isCPTLog();
				
				if (slipCPT == null) {
					slipCPT = getDefaultSlipCPT();
					slipCPTlog = false;
				}
				setCPT(slipCPT, slipCPTlog);
			} else if (displayParam.getValue() == DisplayType.VELOCITY) {
				// was slip
				slipCPT = getCPT();
				slipCPTlog = isCPTLog();
				
				if (velCPT == null) {
					velCPT = getDefaultVelocityCPT();
					velCPTlog = false;
				}
				setCPT(velCPT, velCPTlog);
			} else {
				throw new IllegalStateException();
			}
			fireColorerChangeEvent();
		}
	}
	
	private void eventChanged() {
		curEvent = null;
		curSlipTimeFunc = null;
		
		int eventID = eventIDParam.getValue();
		if (eventID >= 0 && events != null) {
			for (SimulatorEvent e : events) {
				if (e.getID() == eventID) {
					System.out.println("Found a match: M"+(float)e.getMagnitude()+" at "+(float)e.getTimeInYears());
					Preconditions.checkState(e instanceof RSQSimEvent, "Must be an RSQSim event");
					curEvent = (RSQSimEvent)e;
					break;
				}
			}
		}
		
		fireRangeChangeEvent();
	}
	
	private void transFileChanged() {
		transReader = null;
		curSlipTimeFunc = null;
		
		File file = transFileParam.getValue();
		if (file != null) {
			try {
				boolean transV = file.getName().toLowerCase().contains("transv");
				transReader = new RSQSimStateTransitionFileReader(file, elements, transV);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		fireRangeChangeEvent();
	}

}
