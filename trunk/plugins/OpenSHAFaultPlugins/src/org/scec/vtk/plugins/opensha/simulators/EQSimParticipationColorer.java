package org.scec.vtk.plugins.opensha.simulators;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;

import com.google.common.collect.Maps;

public class EQSimParticipationColorer extends CPTBasedColorer implements EQSimsEventListener, ParameterChangeListener {
	
	private static final double cpt_min = 1.0e-6;
	private static final double cpt_max = 1.0e-2;
	
	private DoubleParameter magMinParam;
	private static final double MIN_MAG_DEFAULT = 6.5;
	private static final double MAX_MAG_DEFAULT = 10d;
	private DoubleParameter magMaxParam;
	private double minMag = MIN_MAG_DEFAULT;
	private double maxMag = MAX_MAG_DEFAULT;
	
	private double minEventMag, maxEventMag;
	
	private BooleanParameter probabilityParam;
	private DoubleParameter probDurationParam;
	
	private ParameterList params;
	
	private List<EQSIM_Event> events;
	private HashMap<Integer, EQSIM_Event> eventsMap;
	protected HashMap<Integer, Double> rates;
	
	List<SimulatorElement> elements;
	
	protected static CPT getDefaultCPT() {
		try {
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			cpt = cpt.rescale(cpt_min, cpt_max);
			return cpt;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public EQSimParticipationColorer() {
		super(getDefaultCPT(), false);
		setCPTLog(true);
		
		params = new ParameterList();
		magMinParam = new DoubleParameter("Min Mag", 0d, 10d);
		magMinParam.setValue(MIN_MAG_DEFAULT);
		magMinParam.addParameterChangeListener(this);
		params.addParameter(magMinParam);
		magMaxParam = new DoubleParameter("Max Mag", 0d, 10d);
		magMaxParam.setValue(MAX_MAG_DEFAULT);
		magMaxParam.addParameterChangeListener(this);
		params.addParameter(magMaxParam);
		
		probabilityParam = new BooleanParameter("Probabilities", false);
		probabilityParam.addParameterChangeListener(this);
		params.addParameter(probabilityParam);
		
		probDurationParam = new DoubleParameter("Duration", 1d/365.25, 100000d, "Years");
		probDurationParam.setValue(50);
		probDurationParam.addParameterChangeListener(this);
		params.addParameter(probDurationParam);
	}

	@Override
	public String getName() {
		return "Simulator Participation Rates";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		return getValue(fault.getId());
	}
	
	public double getValue(int id) {
		if (events == null)
			return Double.NaN;
		if (rates == null) {
			synchronized (this) {
				if (rates == null)
					updateCache();
			}
		}
		Double rate = rates.get(id);
		if (rate == null)
			return 0;
		if (probabilityParam.getValue()) {
			// compute probabilities
			double duration = probDurationParam.getValue();
			double prob = 1d - Math.exp(-rate*duration);
			return prob;
		}
		return rate;
	}

	@Override
	public void setEvents(List<EQSIM_Event> events) {
		this.events = events;
		eventsMap = Maps.newHashMap();
		if (events == null || events.isEmpty()) {
			minEventMag = 0;
			maxEventMag = 0;
		} else {
			minEventMag = Double.MAX_VALUE;
			maxEventMag = 0d;
			
			for (EQSIM_Event e : events) {
				double mag = e.getMagnitude();
				if (mag < minEventMag)
					minEventMag = mag;
				if (mag > maxEventMag)
					maxEventMag = mag;
				eventsMap.put(e.getID(), e);
			}
		}
		rates = null;
	}
	
	double getMinEventMag() {
		return minEventMag;
	}

	double getMaxEventMag() {
		return maxEventMag;
	}

	void clearCache() {
		rates = null;
	}
	
	synchronized void updateCache() {
		if (events == null)
			return;
		
		rates = new HashMap<Integer, Double>();
		
		if (events.isEmpty())
			return;
		
		double years = General_EQSIM_Tools.getSimulationDurationYears(events);
		double rate = 1d/years; // each event happens once in the simulation
		
		System.out.println("Updating participation rates! Time span: "+years+" years. Rate/event: "+rate);
		System.out.println("Num events: "+events.size());
		
		int eventCount = 0;
		for (EQSIM_Event e : events) {
			double mag = e.getMagnitude();
			if (!isWithinMagRange(mag))
				continue;
			
			for (Integer elementID : e.getAllElementIDs()) {
				Double val = rates.get(elementID);
				if (val == null)
					val = rate;
				else
					val += rate;
				rates.put(elementID, val);
			}
			eventCount++;
		}
		System.out.println("Found "+eventCount+" events in the given range");
	}
	
	boolean isWithinMagRange(double mag) {
		return mag >= minMag && mag < maxMag;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == magMinParam || event.getParameter() == magMaxParam) {
			minMag = magMinParam.getValue();
			maxMag = magMaxParam.getValue();
			clearCache();
			fireColorerChangeEvent();
		} else if (event.getParameter() == probabilityParam || event.getParameter() == probDurationParam) {
			probDurationParam.getEditor().setEnabled(probabilityParam.getValue());
			fireColorerChangeEvent();
		}
	}
	
	protected List<EQSIM_Event> getEvents() {
		return events;
	}
	
	protected EQSIM_Event getEvent(int id) {
		return eventsMap.get(id);
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

	@Override
	public void setGeometry(List<SimulatorElement> elements) {
		this.elements = elements;
	}

}