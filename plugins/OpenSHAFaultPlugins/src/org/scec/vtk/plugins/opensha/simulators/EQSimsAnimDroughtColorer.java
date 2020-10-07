package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import org.opensha.commons.util.cpt.LinearBlender;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.utils.RSQSimSubSectionMapper;
import org.opensha.sha.simulators.utils.RSQSimSubSectionMapper.SubSectionMapping;
import org.opensha.sha.simulators.utils.RSQSimUtils;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.IDBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.anim.TimeBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.SimulatorElementFault;
import org.scec.vtk.commons.opensha.gui.EventManager;
import org.scec.vtk.main.MainGUI;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;

/**  
 * This file builds up the Open Interval Animation class, which allows the user to 
 * input a geometry file and a simulator event file, and can visualize the length 
 * between events on every fault. Many functions were brought in from the other 
 * animation class, EQSimsEventAnimColorer.java, and just simply focuses on 
 * coloring faults inbetween events rather than at event time. 
 * timeChanged is the main function that determines the count of every fault when 
 * rendering. 
 * 
 * @authors:  Joses Galdamez, Gina Yang, Afe Addeh, Brandon O'Neil, Kevin Milner 
 * @version: 2019 Grand Challenge.  
 */
public class EQSimsAnimDroughtColorer extends CPTBasedColorer
implements TimeBasedFaultAnimation, IDBasedFaultAnimation, EQSimsEventListener, ParameterChangeListener {

	private static CPT getDefaultCPT() {
	//	CPT cpt = new CPT();
//		cpt.add(new CPTVal(0f, Color.WHITE, (float)(droughtYearParam.getValue()-0.1), Color.RED));
//		cpt.add(new CPTVal((float)(droughtYearParam.getValue()-0.1), Color.RED, (float)(droughtYearParam.getValue()-0.0), Color.CYAN));
//		cpt.add(new CPTVal((float)(droughtYearParam.getValue()-0.0), Color.CYAN, (float)(droughtYearParam.getValue()*2), Color.BLUE));
		CPT cpt = new CPT(0, droughtYearParam.getValue()*2, Color.white, Color.red, Color.blue);
		cpt.setNanColor(Color.GRAY);
		cpt.setBelowMinColor(cpt.getMinColor());
		cpt.setAboveMaxColor(cpt.getMaxColor());
		return cpt; 
	}

	private EventManager eventManager;

	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();


	//Title on drop down menu
	private static String TITLE = "Open Interval Animation (yrs)";

	//Min magnitude option
	private static final String MIN_MAG_PARAM_NAME = "Min Mag";
	private static Double MIN_MAG_PARAM = 7.0d;
	private static final Double MAX_MAG_PARAM = 10d;
	private DoubleParameter minMagParam = 
			new DoubleParameter(MIN_MAG_PARAM_NAME, MIN_MAG_PARAM, MAX_MAG_PARAM, MIN_MAG_PARAM);

	//Color Bounds
	private static final String MAX_VALUE_COLOR_WHEEL = "Drought  Period Indicator (years)";
	private static  Double MIN_YEAR_PARAM = 100d;
	private static final Double MAX_YEAR_PARAM = 1000d; 
	private static DoubleParameter droughtYearParam = 
			new DoubleParameter(MAX_VALUE_COLOR_WHEEL, MIN_YEAR_PARAM, MAX_YEAR_PARAM,MIN_YEAR_PARAM );


	//Parameter List
	private ParameterList animParams = new ParameterList();

	private HashMap<Object, Object> idToUnfilteredStepMap;
	private List<Integer> filterIndexes ;

	private List<? extends SimulatorEvent> unfilteredevents;

	private LoadingCache<Integer, Map<Integer, Color>> eventColorCache;
	private int currentStep = -1;
	private BooleanParameter onlyCurrentVisibleParam;

	private HashMap<Integer, Integer> faultDroughtLength;
	private HashMap<Integer, Color> faultDroughtColor;

	private Map<Integer, HashSet<Integer>> faultMappings;
	private Map<String, Integer> faultNamesMap;
	private Map<String, Integer> sectNamesMap;
	
	private List<SimulatorElement> elements;

	private List<? extends FaultSection> subSects;
	private RSQSimSubSectionMapper subSectMapper;

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

		faultDroughtLength=new HashMap<>();
		faultDroughtColor= new HashMap<>();

		//Putting section ids in the hash maps and setting default drought length to zero and the color gray
		for (int i = 0; i <= 921; i++)	{	
			faultDroughtLength.put(i, 0);
			faultDroughtColor.put(i, null);
		}



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
		if (fault != null && faultDroughtLength.containsKey(fault.getId())) {
			return faultDroughtLength.get(fault.getId());
		}
		return Double.NaN;
	}

	int max = 0;

	@Override
	public Color getColor(AbstractFaultSection fault) {
		if (!isStepValid(currentStep)|| !(fault instanceof SimulatorElementFault)) {
			return getCPT().getNaNColor();
		}
		else {
			checkInit();
			Color c;
			SimulatorElement elem = ((SimulatorElementFault)fault).getElement();
			FaultSection sect = subSectMapper.getMappedSection(elem);
			int parentID = sect.getParentSectionId();
			if (parentID > max) {
				max = parentID;
			}
			if (faultDroughtColor != null) {
				c = faultDroughtColor.get(parentID);
				//System.out.println (" color of parent ID " + max+ " is "+ c + " size " + faultDroughtColor.size());
			} else {
				c = getColorCacheForStep(currentStep).get(fault.getId());
			}
			if (c == null)
				return getCPT().getNaNColor();
			else
				return c;
		}
	}

	@Override
	public String getName() {
		return "Drought Duration";
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
		//clearCache();
		//filterEvents();

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

		double startTime = Double.NaN;
		double endTime = Double.NaN;

		if (minMag > MIN_MAG_PARAM|| !Double.isNaN(startTime)) {
			filterIndexes = new ArrayList<Integer>();
			for (int i = 0; i < unfilteredevents.size(); i++) {
				SimulatorEvent event = unfilteredevents.get(i);

				if (event.getMagnitude() < minMag) 
					continue;
				if (!Double.isNaN(startTime)) {
					if (event.getTime() < startTime)
						continue;
					if (event.getTime() > endTime)
						break;
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
			MIN_MAG_PARAM = (Double) arg0.getNewValue();
			filterEvents();
		} else if (arg0.getSource()== droughtYearParam ) {
			MIN_YEAR_PARAM = (Double) arg0.getNewValue();
			setCPT(getDefaultCPT());
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
		faultMappings = null;
		faultNamesMap = null;
		sectNamesMap = null;
		subSectMapper = null;
		this.elements = elements;
	}
	
	private void checkInit() {
		if (elements != null && subSectMapper == null) {
			if (subSects == null) {
				FaultModels fm = FaultModels.FM3_1;
				DeformationModels geom = DeformationModels.GEOLOGIC;
				subSects = RSQSimUtils.getUCERF3SubSectsForComparison(fm, geom);
			}
			// a rupture is mapped to a subsection if at least this fraction of the subsection (by area) participates
			double minSectFractForInclusion = 0.2;
			subSectMapper = new RSQSimSubSectionMapper(subSects, elements, minSectFractForInclusion);
			faultMappings = Maps.newHashMap();
			faultNamesMap = Maps.newHashMap();
			sectNamesMap = Maps.newHashMap();
			Map<Integer, HashSet<String>> faultNames = Maps.newHashMap();
			for (SimulatorElement e : elements) {
				Integer sectID = e.getSectionID();
				String sectName = e.getSectionName();
				if (!sectNamesMap.containsKey(sectName))
					sectNamesMap.put(sectName, sectID);
				Integer faultID = e.getFaultID();
				if (faultID < 0)
					continue;
				HashSet<Integer> sectsForFault = faultMappings.get(faultID);
				if (sectsForFault == null) {
					sectsForFault = new HashSet<Integer>();
					faultMappings.put(faultID, sectsForFault);
					faultNames.put(faultID, new HashSet<String>());
				}
				sectsForFault.add(sectID);
				faultNames.get(faultID).add(sectName);
			}

			int maxFaultID = 0;
			for (Integer faultID : faultMappings.keySet())
				if (faultID > maxFaultID)
					maxFaultID = faultID;
			int faultID_digits = (""+maxFaultID).length();

			for (Integer faultID : faultMappings.keySet()) {
				// get common name
				HashSet<String> names = faultNames.get(faultID);
				String commonPrefix = "";
				lenLoop:
					for (int i=0; i<names.iterator().next().length(); i++) {
						String start = null;
						for (String name : names) {
							if (start == null)
								start = name.charAt(i)+"";
							else if (name.charAt(i) != start.charAt(0))
								break lenLoop;
						}
						commonPrefix += start;
					}
				if (commonPrefix.length() < 2)
					commonPrefix = Joiner.on(",").join(faultMappings.get(faultID));
				else
					commonPrefix += "*";
				String faultIDstr = faultID+"";
				while (faultIDstr.length() < faultID_digits)
					faultIDstr = "0"+faultIDstr;
				faultNamesMap.put(faultIDstr+". "+commonPrefix, faultID);
			}
		}


	}

	@Override
	public double getCurrentDuration() {
		if (unfilteredevents != null)
			return getTimeForStep(getNumSteps()-1) - getTimeForStep(0);
		return 0d;
	}

	private LinearBlender colorBlender;
	@Override
	public  boolean timeChanged(double time) {
		double eventPrevTime = 0;
		Color nanColor= getCPT().getNaNColor();
		Color droughtColor;
		Integer numDroughtLength;
		if (colorBlender == null) 
			colorBlender = new LinearBlender();


		int step = currentStep;
		SimulatorEvent event = getEventForStep(step);
		HashMap<Integer, Integer> eventParentIDS = 	getParentIDsForEvent(event);

		if (step  == 1) {
			eventPrevTime = 0;
		} else if (step > 0) {
			SimulatorEvent eventPrevious = getEventForStep(step-1);
			eventPrevTime = eventPrevious.getTimeInYears();
		} else {
			return true;
		}

		double eventTime = event.getTimeInYears(); 
		double timeSinceYears = eventTime-eventPrevTime;

		if (timeSinceYears < 0) {
			timeSinceYears = 0;  
		}
		checkInit();

		for(Integer key :faultDroughtLength.keySet()) {
				if (!eventParentIDS.containsKey(key)) {
					Color eventColor = faultDroughtColor.get(key);
					numDroughtLength = (int) (faultDroughtLength.get(key) + timeSinceYears);
					faultDroughtLength.put (key, numDroughtLength);
					droughtColor= getColorForValue(numDroughtLength);
	
					
					if (faultDroughtColor.get(key)!= null) {
						Color fade = colorBlender.blend(droughtColor, eventColor, (float) .1);
						faultDroughtColor.put(key, fade);
						

					} else if  (numDroughtLength >= droughtYearParam.getValue().intValue()) {
						Color fade = colorBlender.blend(droughtColor, nanColor, (float) .1);
						faultDroughtColor.put(key, fade);	  
					}  
				} else {
					faultDroughtLength.put(key, 0);
					faultDroughtColor.put(key, nanColor);	
				}

			}
		return true;
	}
	
	@Override 
	public synchronized int getIDForStep(int step) {
		if (filterIndexes == null && step >= 0 && unfilteredevents != null && step < unfilteredevents.size()) {
			return unfilteredevents.get(step).getID();
		} else if (filterIndexes != null && step >= 0 && step < filterIndexes.size()) {
			return unfilteredevents.get(filterIndexes.get(step)).getID();
		}
		return -1;
	}
	@Override
	public int getStepForID(int id) {

		Integer step = (Integer) idToUnfilteredStepMap.get(id);
		if (step == null)
			// it's not a valid ID
			return -1;
		int filterIndex = -1;
		if (filterIndexes == null)
			return step;
		else
			filterIndex = filterIndexes.indexOf(step);
		if (filterIndex >= 0) {
			// this is a valid ID, and it's in the filter
			return filterIndex;
		} else if (id > 0 && id <= unfilteredevents.size()) {
			// this is a valid ID, but it's currently filtered out. remove the filter.
			return step;
		} else {
			// it's not a valid ID
			return -1;
		}
	}



	@Override
	public Color getColorForValue(double value) {
		Color color = super.getColorForValue(value);
		// if saturation, change color here
		return color;
	}

	public HashMap <Integer, Integer> getParentIDsForEvent(SimulatorEvent event) {
		checkInit();
		List<List<SubSectionMapping>> mappingsBundled = subSectMapper.getFilteredSubSectionMappings(event);
		if (mappingsBundled == null)
			// this will happen for small events which break no subsections
			return null;
		HashMap<Integer, Integer> sects = new HashMap<>();
		for (List<SubSectionMapping> bundle : mappingsBundled)
			for (SubSectionMapping mapping : bundle)
				sects.put(mapping.getSubSect().getParentSectionId(), 0);
		
		return sects;
	}

	public Collection<SimulatorElement> getElementsForSubSect(FaultSectionPrefData subSect) {
		checkInit();
		return subSectMapper.getElementsForSection(subSect);
	}

}
