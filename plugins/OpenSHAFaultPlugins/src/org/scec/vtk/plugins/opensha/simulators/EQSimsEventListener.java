package org.scec.vtk.plugins.opensha.simulators;

import java.util.List;

import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;

public interface EQSimsEventListener {

	public abstract void setEvents(List<? extends SimulatorEvent> events);
	
	public abstract void setGeometry(List<SimulatorElement> elements);

}