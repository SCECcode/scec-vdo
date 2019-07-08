package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.awt.Cursor;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.ParameterListParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.scec.useit.forecasting.droughts.CatalogMinMagDroughtType;
import org.scec.useit.forecasting.droughts.DroughtCalculator;
import org.scec.useit.forecasting.droughts.DroughtType;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.SimulatorElementFault;
//import org.scec.vtk.plugins.ProgressBar.ProgressBar;

import com.google.common.base.Preconditions;

public class EQSimsDroughtColorer extends CPTBasedColorer implements EQSimsEventListener, ParameterChangeListener {

	private List<SimulatorElement> elements;
	private List<? extends SimulatorEvent> events;

	JOptionPane hey = new JOptionPane();


	// we will eventually define droughts in multiple ways
	private enum DroughtTypes {
		STATEWIDE(new CatalogMinMagDroughtType(7.5));

		private DroughtType type;
		private DroughtTypes(DroughtType type) {
			this.type = type;
		}

		@Override
		public String toString() {
			// this is what the drop down menu will show
			return type.getName();
		}
	}

	// this will allow the user to calculate/color the faults by different things
	private enum PlotType {
		TIME_INDEPENDENT("Time-Independent"),
		DURING_DROUGHT("During Drought"),
		AFTER_DROUGHT("After Drought"),
		DROUGHT_GAIN("Drought Gain");

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

	private EnumParameter<DroughtTypes> droughtTypeParam;
	private DoubleParameter droughtDurationParam;
	private ParameterListParameter droughtParamsParam;
	private EnumParameter<PlotType> plotTypeParam;
	private DoubleParameter forecastMinMagParam;
	private DoubleParameter forecastDurationParam;

	/*
	 * Cached data
	 */
	private Map<SimulatorElement, Double> tiProbs; // time independent rates
	private Map<SimulatorElement, Double> droughtProbs; // event rates during drought
	private Map<SimulatorElement, Double> afterProbs; // event rates after drought
	private Map<SimulatorElement, Double> droughtGain;
	static CPT getDefaultCPT() {
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
				EnumSet.allOf(DroughtTypes.class), DroughtTypes.STATEWIDE, null);
		droughtTypeParam.addParameterChangeListener(this);
		params.addParameter(droughtTypeParam);

		// duration of the drought
		droughtDurationParam = new DoubleParameter("Drought Duration", 0d, 1000);
		droughtDurationParam.setUnits("Years");
		droughtDurationParam.setValue(50);
		droughtDurationParam.addParameterChangeListener(this);
		params.addParameter(droughtDurationParam);

		// add all drought parameters
		droughtParamsParam = new ParameterListParameter("Dought Parameters", droughtTypeParam.getValue().type.getParameters());
		droughtParamsParam.addParameterChangeListener(this);
		params.addParameter(droughtParamsParam);

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
		return "Drought Probabilities";
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
			ret = afterProbs.get(elem);
			break;
		case DROUGHT_GAIN:
			ret = droughtGain.get(elem);
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
		afterProbs = null;
		droughtGain = null;
	}

	/**
	 * Calculate and cache time-independent, drought, and storm rates. Do this lazily when needed
	 */
	private synchronized void calculateProbs() {
		if (events == null || elements == null || events.size() < 2)
			return;
		System.out.println("Computing drought rates");
		//Progress Bar experiment
		// @Joses (7/3/2019)
		//ProgressBar progress = new ProgressBar("Loading");
		//progress.runProgressBar();

		DroughtCalculator calc = new DroughtCalculator(elements, events, droughtTypeParam.getValue().type);
		double droughtDuration = droughtDurationParam.getValue();
		double forecastMinMag = forecastMinMagParam.getValue();
		double forecastDuration = forecastDurationParam.getValue();

		tiProbs = calc.getElementTimeIndependentProbs(forecastMinMag, forecastDuration);
		droughtProbs = calc.getElementProbsDuringDrought(forecastMinMag, droughtDuration);
		afterProbs = calc.getElementProbAfterDroughtYears(forecastMinMag, droughtDuration, forecastDuration);

		double difference;
		droughtGain = new HashMap<>();
		for (SimulatorElement event : tiProbs.keySet()) {
			if ( event != null && afterProbs.containsKey(event)) {
				System.out.println(afterProbs.get(event)+ "  "+ tiProbs.get(event));
				difference = (afterProbs.get(event) - tiProbs.get(event))/tiProbs.get(event);
				droughtGain.put(event, difference);
			}
		}

		System.out.println("Done computing drought rates");
		//Progress Bar experiment
		//@Joses (7/3/19)
		//progress.stopProgressBar();
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {
		if (e.getParameter() != plotTypeParam) {
			// if any parameter but plot type was changed, clear cached data

			// don't do this if only plot type was changed, as we calculate values
			// for each plot type at once
			clear();
		}
		if (e.getParameter() == droughtTypeParam) {
			// update drought type parameters
			droughtParamsParam.setValue(droughtTypeParam.getValue().type.getParameters());
		}
		// fire an event telling the plugin to redraw everything
		fireColorerChangeEvent();
	}

}
