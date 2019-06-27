package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.RSQSimEvent;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.SimulatorElementFault;

import com.google.common.base.Preconditions;

public class EQSimsDroughtColorer extends CPTBasedColorer implements EQSimsEventListener, ParameterChangeListener {
	
	private List<SimulatorElement> elements;
	private List<? extends SimulatorEvent> events;
	
	// we will eventually define droughts in multiple ways
	private enum DroughtType {
		STATEWIDE("No EQs Statewide M>X");

		private String name;
		private DroughtType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			// this is what the drop down menu will show
			return name;
		}
	}
	
	// this will allow the user to calculate/color the faults by different things
	private enum PlotType {
		TIME_INDEPENDENT("Time-Independent"),
		DURING_DROUGHT("During Drought"),
		AFTER_DROUGHT("After Drought");
		
		private String name;
		private PlotType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			// this is what the drop down menu will show
			return name;
		}
	}
	
	/*
	 * PARAMETERS
	 */
	// these parameters have pre-built GUI elements which will show up in the bottom panel,
	// allowing the user to adjust parameters within SCEC-VDO
	private ParameterList params;

	private EnumParameter<DroughtType> droughtTypeParam;
	private DoubleParameter droughtDurationParam;
	private DoubleParameter droughtMinMagParam;
	private EnumParameter<PlotType> plotTypeParam;
	private DoubleParameter forecastMinMagParam;
	private DoubleParameter forecastDurationParam;
	
	/*
	 * Cached data
	 */
	private Map<SimulatorElement, Double> tiProbs; // time independent rates
	private Map<SimulatorElement, Double> droughtProbs; // event rates during drought
	private Map<SimulatorElement, Double> stormProbs; // event rates afgter drought

	private static CPT getDefaultCPT() {
		// default color palette, in log space
		// goes from 10^-5 to 10^0
		try {
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-5, 0);
			cpt.setNanColor(Color.GRAY);
			return cpt;
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	public EQSimsDroughtColorer() {
		super(getDefaultCPT(), true); // true here means that the CPT is in Log10 space
		
		// build parameters
		params = new ParameterList();
		
		// drought type
		droughtTypeParam = new EnumParameter<>("Drought Type",
				EnumSet.allOf(DroughtType.class), DroughtType.STATEWIDE, null);
		droughtTypeParam.addParameterChangeListener(this);
		params.addParameter(droughtTypeParam);
		
		// drought min mag param
		droughtMinMagParam = new DoubleParameter("Drought Min Mag", 0d, 10d);
		droughtMinMagParam.setValue(7.5d);
		droughtMinMagParam.addParameterChangeListener(this);
		params.addParameter(droughtMinMagParam);
		
		// duration of the drought
		droughtDurationParam = new DoubleParameter("Drought Duration", 0d, 1000);
		droughtDurationParam.setUnits("Years");
		droughtDurationParam.setValue(50);
		droughtDurationParam.addParameterChangeListener(this);
		params.addParameter(droughtDurationParam);
		
		// drought type
		plotTypeParam = new EnumParameter<>("Plot Type",
				EnumSet.allOf(PlotType.class), PlotType.TIME_INDEPENDENT, null);
		plotTypeParam.addParameterChangeListener(this);
		params.addParameter(plotTypeParam);
		
		// minimum magnitude that we are forecasting
		forecastMinMagParam = new DoubleParameter("Forecast Min Mag", 0d, 10d);
		forecastMinMagParam.setValue(7d);
		forecastMinMagParam.addParameterChangeListener(this);
		params.addParameter(forecastMinMagParam);
		
		// duration of the forecast
		forecastDurationParam = new DoubleParameter("Forecast Duration", 0d, 1000d);
		forecastDurationParam.setUnits("Years");
		forecastDurationParam.setValue(30d);
		forecastDurationParam.addParameterChangeListener(this);
		params.addParameter(forecastDurationParam);
	}

	@Override
	public String getName() {
		// this is the name that shows up in the "Color Faults By" list
		return "Drought/Storm Probabilities";
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

	@Override
	public synchronized double getValue(AbstractFaultSection fault) {
		// this is called to return a scalar value for this fault element. the parent class
		// will then loop up a color for that scalar value from the color palette table (CPT)
		Preconditions.checkState(fault instanceof SimulatorElementFault);
		SimulatorElementFault simFault = (SimulatorElementFault)fault;
		SimulatorElement elem = simFault.getElement();
		
		if (tiProbs == null) {
			// need to compute everything
			calculateProbs();
			if (tiProbs == null)
				// if null here, we don't have events loaded
				return Double.NaN;
		}
		
		Double ret;
		switch (plotTypeParam.getValue()) {
		case TIME_INDEPENDENT:
			ret = tiProbs.get(elem);
			break;
		case DURING_DROUGHT:
			ret = droughtProbs.get(elem);
			break;
		case AFTER_DROUGHT:
			ret = stormProbs.get(elem);
			break;

		default:
			ret = Double.NaN;
			break;
		}
		if (ret == null)
			ret = Double.NaN;
		
		return ret;
	}

	@Override
	public synchronized void setEvents(List<? extends SimulatorEvent> events) {
		this.events = events;
		clear();
	}

	@Override
	public synchronized void setGeometry(List<SimulatorElement> elements) {
		// this is called whenever the user loads a new geometry file
		this.elements = elements;
		clear();
	}
	
	/**
	 * clear any cached data (when a parameter has changed, or a new geometry or event file
	 * has been loaded)
	 */
	private void clear() {
		tiProbs = null;
		droughtProbs = null;
		stormProbs = null;
	}
	
	private class Drought {
		private double startTime;
		private double endTime;
		private double forecastEndTime;
		
		private List<SimulatorEvent> eventsDuringDrought;
		private List<SimulatorEvent> eventsAfterDrought;
		
		public Drought(double startTime) {
			this.startTime = startTime;
			this.endTime = startTime + droughtDurationParam.getValue();
			this.forecastEndTime = endTime + forecastDurationParam.getValue();
			
			eventsDuringDrought = new ArrayList<>();
			eventsAfterDrought = new ArrayList<>();
		}
		
		public double getDroughtEndTime() {
			return endTime;
		}
		
		public double getForecastEndTime() {
			return forecastEndTime;
		}
		
		public void addEvent(SimulatorEvent event) {
			Preconditions.checkNotNull(event);
			double time = event.getTimeInYears();
			Preconditions.checkState(time >= startTime && time <= forecastEndTime);
			if (time <= endTime)
				eventsDuringDrought.add(event);
			else
				eventsAfterDrought.add(event);
		}
	}
	
	/**
	 * Calculate and cache time-independent, drought, and storm rates. Do this lazily when needed
	 */
	private synchronized void calculateProbs() {
		if (events == null || elements == null || events.size() < 2)
			return;
		System.out.println("Computing drought rates");
		double totDurationYears = events.get(events.size()-1).getTimeInYears() - events.get(0).getTimeInYears();
		Map<SimulatorElement, Integer> tiElementCounts = new HashMap<>();
		
		double forecastMinMag = forecastMinMagParam.getValue();
		double forecastDuration = forecastDurationParam.getValue();
		
		List<Drought> droughts = new ArrayList<>();
		Drought curDrought = null;
		
		double lastTime = 0d;
		
		for (SimulatorEvent event : events) {
			double eventTime = event.getTimeInYears();
			lastTime = eventTime;
			boolean endsDrought = doesEventEndDrought(event);
			if (endsDrought) {
				// this event signifies the end of a drought
				if (curDrought != null) {
					// see if the current drought is long enough, and if so keep it
					if (eventTime > curDrought.getDroughtEndTime()) {
						// this drought was long enough, add it to the list
						droughts.add(curDrought);
					}
					curDrought = null;
				}
			} else {
				// this event does not end a drought
				if (curDrought == null)
					// weren't in a drought, start a new one
					curDrought = new Drought(eventTime);
				// have the current drought track this event
				if (eventTime <= curDrought.getForecastEndTime())
					curDrought.addEvent(event);
			}
			// now have all ended droughts track this event if it's during the post drought (storm) forecast window
			for (Drought drought : droughts)
				if (eventTime > drought.getDroughtEndTime() && eventTime <= drought.getForecastEndTime())
					drought.addEvent(event);
			
			double mag = event.getMagnitude();
			if (mag >= forecastMinMag) {
				// this event is at or above our forecast min mag
				// count it on every element which participated in the event
				for (SimulatorElement element : event.getAllElements()) {
					Integer prevCount = tiElementCounts.get(element);
					if (prevCount == null)
						prevCount = 0;
					tiElementCounts.put(element, prevCount+1);
				}
			}
		}
		if (curDrought != null && lastTime >= curDrought.getForecastEndTime())
			droughts.add(curDrought);
		// cull any droughts where forecast end time > last event time
		for (int i=droughts.size(); --i>=0;) {
			if (droughts.get(i).getForecastEndTime() > lastTime)
				droughts.remove(i);
			else
				break;
		}
		System.out.println("Found "+droughts.size()+" droughts");
		
		// now convert time independent counts to probabilities
		tiProbs = new HashMap<>();
		for (SimulatorElement elem : tiElementCounts.keySet()) {
			int elemCount = tiElementCounts.get(elem);
			double annualRate = (double)elemCount/totDurationYears;
			// poisson probability: p = 1 - e^(-r*t)
			double forecastProb = 1d - Math.exp(-annualRate*forecastDuration);
			tiProbs.put(elem, forecastProb);
		}
		
		// now compute drought and storm probs
		int numDroughts = droughts.size();
		if (numDroughts > 0) {
			Map<SimulatorElement, Integer> droughtHitCounts = new HashMap<>();
			Map<SimulatorElement, Integer> stormHitCounts = new HashMap<>();
			for (Drought drought : droughts) {
				// set of elements which had any event >= forecast mag during the drought
				HashSet<SimulatorElement> droughtHits = new HashSet<>();
				for (SimulatorEvent e : drought.eventsDuringDrought)
					if (e.getMagnitude() >= forecastMinMag)
						droughtHits.addAll(e.getAllElements());
				// set of elements which had any event >= forecast mag during the forecast period after drought
				HashSet<SimulatorElement> stormHits = new HashSet<>();
				for (SimulatorEvent e : drought.eventsAfterDrought)
					if (e.getMagnitude() >= forecastMinMag)
						stormHits.addAll(e.getAllElements());
				for (SimulatorElement e : droughtHits) {
					Integer prevCount = droughtHitCounts.get(e);
					if (prevCount == null)
						prevCount = 0;
					droughtHitCounts.put(e, prevCount+1);
				}
				for (SimulatorElement e : stormHits) {
					Integer prevCount = stormHitCounts.get(e);
					if (prevCount == null)
						prevCount = 0;
					stormHitCounts.put(e, prevCount+1);
				}
			}
			// convert to probabilities
			// prob = numHits/numDroughts
			droughtProbs = new HashMap<>();
			for (SimulatorElement e : droughtHitCounts.keySet()) {
				int count = droughtHitCounts.get(e);
				double prob = (double)count/(double)numDroughts;
				droughtProbs.put(e, prob);
			}
			stormProbs = new HashMap<>();
			for (SimulatorElement e : stormHitCounts.keySet()) {
				int count = stormHitCounts.get(e);
				double prob = (double)count/(double)numDroughts;
				stormProbs.put(e, prob);
			}
		}
		
		System.out.println("Done computing drought rates");
	}
	
	/**
	 * Return true of this event signifies the end of a drought, false otherwise
	 * @param event
	 * @return
	 */
	private boolean doesEventEndDrought(SimulatorEvent event) {
		switch (droughtTypeParam.getValue()) {
		case STATEWIDE:
			return event.getMagnitude() >= droughtMinMagParam.getValue();
		default:
			throw new IllegalStateException("Drought type not yet implemented: "+droughtTypeParam.getValue());
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {
		if (e.getParameter() != plotTypeParam) {
			// if any parameter but plot type was changed, clear cached data
			
			// don't do this if only plot type was changed, as we calculate values
			// for each plot type at once
			clear();
		}
		// fire an event telling the plugin to redraw everything
		fireColorerChangeEvent();
	}

}
