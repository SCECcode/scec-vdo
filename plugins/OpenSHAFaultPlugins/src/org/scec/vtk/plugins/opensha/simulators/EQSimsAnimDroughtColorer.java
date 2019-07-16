package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.TimeBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.gui.EventManager;
import org.scec.vtk.main.MainGUI;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

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
	private static final Double MIN_YEAR_PARAM = 100d;
	private static final Double MAX_YEAR_PARAM = 100d; 
	private DoubleParameter droughtYearParam = 
			new DoubleParameter(MAX_VALUE_COLOR_WHEEL, MIN_YEAR_PARAM, MAX_YEAR_PARAM,MIN_YEAR_PARAM );

	//Parameter List
	private ParameterList animParams = new ParameterList();

	private HashMap<Object, Object> idToUnfilteredStepMap;
	private List<Integer> filterIndexes ;

	private List<? extends SimulatorEvent> unfilteredevents;

	private LoadingCache<Integer, Map<Integer, Color>> eventColorCache;
	private int currentStep = -1;
	private BooleanParameter onlyCurrentVisibleParam;
   
	public EQSimsAnimDroughtColorer() {
		super(getDefaultCPT(), false);
		//add minMag
		animParams.addParameter(minMagParam);
		minMagParam.addParameterChangeListener(this);

		//add Color Bounds
		animParams.addParameter(droughtYearParam);
		droughtYearParam.addParameterChangeListener(this);
		
		//makes the faults visible
		onlyCurrentVisibleParam = new BooleanParameter("Hide Other Elements", false);
		animParams.addParameter(onlyCurrentVisibleParam);

		// cache for event colors for faster loading
		// PreloadThread below will actively try to preload this cache with the next steps 
		eventColorCache = CacheBuilder.newBuilder().maximumSize(10000).build(new CacheLoader<Integer, Map<Integer, Color>>() {

			@Override
			public Map<Integer, Color> load(Integer index) throws Exception {
				SimulatorEvent event = unfilteredevents.get(index);
				int[] ids = event.getAllElementIDs();
				double[] slips = event.getAllElementSlips();
				Map<Integer, Color> slipMap = new HashMap<>();

				for (int j=0; ids != null && j<ids.length; j++) {
					int id = ids[j];
					double slip = slips[j];
					Color c = getColorForValue(slip);
					slipMap.put(id, c);
				}
				return slipMap;
			}

		});

	}

	public void setEventManager(EventManager eventManager) {
		this.eventManager = eventManager; 
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		return Double.NaN;
	}
	
	@Override
	public Color getColor(AbstractFaultSection fault) {
		if (!isStepValid(currentStep))
			return getCPT().getNaNColor();
		else {
			Color c;
				c = getColorCacheForStep(currentStep).get(fault.getId());
			if (c == null)
				return getCPT().getNaNColor();
			else
				return c;
		}
	}
	
	@Override
	public String getName() {
		return TITLE;
	}
	

	@Override
	public int getNumSteps() {
		if (unfilteredevents != null)
			if (filterIndexes == null)
				return unfilteredevents.size();
			else
				return filterIndexes.size();
		else
			return 0;  
	}
	
	@Override
	public void setCurrentStep(int step) {
		this.currentStep = step;
		checkStartPreloadThread();

	}
	
	private synchronized Map<Integer, Color> getColorCacheForStep(int step) {
		if (unfilteredevents == null)
			return null;
		try {
			if (filterIndexes == null)
				return eventColorCache.get(step);
			else
				return eventColorCache.get(filterIndexes.get(step));
		} catch (ExecutionException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
	

	private synchronized SimulatorEvent getEventForStep(int step) {
		if (unfilteredevents == null)
			return null;
		if (filterIndexes == null)
			return unfilteredevents.get(step);
		else
			return unfilteredevents.get(filterIndexes.get(step));
	}

		@Override
		public void setCPT(CPT cpt) {
			super.setCPT(cpt);
			clearCache();
		}

	
		@SuppressWarnings("unchecked")
		private void clearCache() {
			eventColorCache.invalidateAll();
		}
		
		
		private PreloadThread preloadThread;

		/**
		 * If the PreloadThread is not started it will be started. Otherwise it's counter will be reset
		 */
		private synchronized void checkStartPreloadThread() {
			if (preloadThread == null) {
				preloadThread = new PreloadThread();
				preloadThread.start();
			} else {
				if (preloadThread.isAlive()) {
					preloadThread.currentIteration = 0;
				}
				if (!preloadThread.isAlive()) {
					preloadThread = new PreloadThread();
					preloadThread.start();
				}
			}
		}

		private class PreloadThread extends Thread {

			// number of steps ahead to preload
			final int preload_num = 100;
			final long sleep_time_millis = 500;
			final int max_iterations = 200;
			private int currentIteration = 0;

			@Override
			public void run() {
				while (currentIteration < max_iterations) {
					//				currentStep;
					if (currentStep >= 0) {
						if (unfilteredevents == null)
							continue;
						for (int i=0; i<preload_num; i++) {
							int step = currentStep + i;
							if (!isStepValid(step))
								break;
							getColorCacheForStep(step);
						}
					}
					try {
						Thread.sleep(sleep_time_millis);
					} catch (InterruptedException e) {}
					currentIteration++;
				}
			}

		}
		
		@Override
		public void setEvents(List<? extends SimulatorEvent> events) {
			this.unfilteredevents = events;
			idToUnfilteredStepMap = Maps.newHashMap();
			if (events != null) {
				for (int step=0; step<events.size(); step++)
					idToUnfilteredStepMap.put(events.get(step).getID(), step);
			}
			clearCache();
			filterEvents();

		}


	@Override
	public void addRangeChangeListener(ChangeListener l) {
		listeners.add(l);

	}
	@Override
	public void fireRangeChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener l : listeners) {
			l.stateChanged(e);
		}

	}
	
	private static final DecimalFormat magDF = new DecimalFormat("0.00");
	@Override
	public String getCurrentLabel() {
		if (!isStepValid(currentStep))
			return null;
		SimulatorEvent event = getEventForStep(currentStep);
		return "Mag: "+magDF.format(event.getMagnitude());
	}
	
	private boolean isStepValid(int step) {
		if (step < 0 || unfilteredevents == null || step >= getNumSteps())
			return false;
		return true;
	}
	
	@Override
	public double getTimeForStep(int step) {
		if (!isStepValid(step))
			return 0;
		return getEventForStep(step).getTime();
	}


	@Override
	public ParameterList getAnimationParameters() {
		return animParams;
	}
	
	
	private void filterEvents() {
		// first wait on any colorer change before entering synchronized block, as doing otherwise can cause deadlock
		try {
			eventManager.waitOnCalcThread();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		doFilterEvents();

	}
	
	private synchronized void doFilterEvents() {
		double minMag = minMagParam .getValue();
		if (minMag > MIN_MAG_PARAM) {
			for (int i = 0; i < unfilteredevents.size(); i++) {
				SimulatorEvent event = unfilteredevents.get(i);

				filterIndexes = new ArrayList<Integer>();
				if (event.getMagnitude() < minMag) {
					continue;
				}

				filterIndexes.add(i);
			}

		}
		fireColorerChangeEvent();
		checkStartPreloadThread();
		fireRangeChangeEvent();
		MainGUI.updateRenderWindow();
	}

	@Override
	public void parameterChange(ParameterChangeEvent arg0) {
		if (arg0.getSource() == minMagParam ) {
			filterEvents();
		} else if (arg0.getSource()==MAX_VALUE_COLOR_WHEEL) {
			filterEvents();
		}
	}

	
	@Override
	public Boolean getFaultVisibility(AbstractFaultSection fault) {
		if (onlyCurrentVisibleParam.getValue()) {
			return !getColor(fault).equals(getCPT().getNaNColor());
		}
		return null;
	}

	@Override
	public FaultColorer getFaultColorer() {
		return this;
	}
	
	@Override
	public boolean includeStepInLabel() {
		return true;
	}
	
	@Override
	public int getPreferredInitialStep() {
		return 0;
	}


	@Override
	public void setGeometry(List<SimulatorElement> elements) {
		// TODO Auto-generated method stub

	}
	

	@Override
	public double getCurrentDuration() {
		if (unfilteredevents != null)
			return getTimeForStep(getNumSteps()-1) - getTimeForStep(0);
		return 0d;
	}


	@Override
	public boolean timeChanged(double time) {
		// this will be called whenever a new frame is rendered, regardless of if there was an event
		return false;
	}


	@Override
	public Color getColorForValue(double value) {
		Color color = super.getColorForValue(value);
		// if saturation, change color here
		return color;
	}



}
