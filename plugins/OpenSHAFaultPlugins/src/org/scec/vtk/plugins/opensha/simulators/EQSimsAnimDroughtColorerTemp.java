package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.commons.util.cpt.LinearBlender;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;
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
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import vtk.vtkCellPicker;

public class EQSimsAnimDroughtColorerTemp extends CPTBasedColorer implements
		TimeBasedFaultAnimation, IDBasedFaultAnimation, ParameterChangeListener, EQSimsEventListener, PickHandler<AbstractFaultSection> {

	/**
	 * DISCLAIMER: this has all been taken from EQSimsEventAnimColorer.java, and we simply only use it 
	 * for reference. at the moment, none of these functionatlities are final, nor are tey working 
	 * towards the goal. This is a huge TODO list. 
	 */
	private static final long serialVersionUID = 1L;
	
	private static String NAME = "Simulator Drought Animation";
	
	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	private EventManager eventManager;
	
	private List<? extends SimulatorEvent> unfilteredevents;
	private Map<Integer, Integer> idToUnfilteredStepMap;
	private List<Integer> filterIndexes;
	private LoadingCache<Integer, Map<Integer, Color>> eventColorCache;
	
	private int currentStep = -1;
	
	private static final String MAG_MIN_PARAM_NAME = "Min Mag";
	private static final Double MAG_PARAM_MIN = 0d;
	private static final Double MAG_PARAM_MAX = 10d;
	private static final Double MAG_MIN_PARAM_DEFAULT = MAG_PARAM_MIN;
	private DoubleParameter magMinParam =
		new DoubleParameter(MAG_MIN_PARAM_NAME, MAG_PARAM_MIN, MAG_PARAM_MAX, MAG_MIN_PARAM_DEFAULT);

	
	private static final String SECT_FILTER_PARAM_NAME = "Only Events Involing";
	private static final String SECT_FILTER_PARAM_DEFAULT = "(all sections)";
	private StringParameter sectFilterParam;
	
	private static final String FAULT_FILTER_PARAM_NAME = "Filter By Fault";
	private static final String FAULT_FILTER_PARAM_DEFAULT = "(all faults)";
	private StringParameter faultFilterParam;
	
	
	private static final String FADE_YEARS_PARAM_NAME = "Fade Out Time (years)";
	private DoubleParameter fadeYearsParam;
	

	private Map<Integer, Color> fadeColors;
	
	private BooleanParameter onlyCurrentVisibleParam;
	
	private Map<Integer, HashSet<Integer>> faultMappings;
	private Map<String, Integer> faultNamesMap;
	private Map<String, Integer> sectNamesMap;
	
	private List<SimulatorElement> elements;
	private General_EQSIM_Tools tools;
	
	private List<SimulatorElement> controlClickedElements;
	
	private ParameterList animParams = new ParameterList();
	
	private List<FaultSectionPrefData> subSects;
	private RSQSimSubSectionMapper subSectMapper;
	
	private static CPT getDefaultCPT() {
		CPT cpt = new CPT();
		
		float delta = 10f / 6f;
		
		cpt.add(new CPTVal(0f, new Color(0, 0, 255),
				delta * 1f, new Color(0, 255, 255)));
		cpt.add(new CPTVal(delta * 1f, new Color(0, 255, 255),
				delta * 2f, new Color(0, 255, 0)));
		cpt.add(new CPTVal(delta * 2f, new Color(0, 255, 0),
				delta * 3f, new Color(255, 255, 0)));
		cpt.add(new CPTVal(delta * 3f, new Color(255, 255, 0),
				delta * 4f, new Color(255, 127, 0)));
		cpt.add(new CPTVal(delta * 4f, new Color(255, 127, 0),
				delta * 5f, new Color(255, 0, 0)));
		cpt.add(new CPTVal(delta * 5f, new Color(255, 0, 0),
				delta * 6f, new Color(255, 0, 255)));
		
		cpt.setBelowMinColor(new Color(0, 0, 255));
		cpt.setAboveMaxColor(new Color(255, 0, 255));
		cpt.setNanColor(new Color(127, 127, 127));
		
		return cpt;
	}
	
	public EQSimsAnimDroughtColorerTemp() {
		super(getDefaultCPT(), false);
		
		animParams.addParameter(magMinParam);
		magMinParam.addParameterChangeListener(this);

		
		ArrayList<String> strings = new ArrayList<String>();
		strings.add(SECT_FILTER_PARAM_DEFAULT);
		
		sectFilterParam = new StringParameter(SECT_FILTER_PARAM_NAME, strings, SECT_FILTER_PARAM_DEFAULT);
		animParams.addParameter(sectFilterParam);
		sectFilterParam.addParameterChangeListener(this);
		
		strings = new ArrayList<String>();
		strings.add(FAULT_FILTER_PARAM_DEFAULT);
		
		faultFilterParam = new StringParameter(FAULT_FILTER_PARAM_NAME, strings, FAULT_FILTER_PARAM_DEFAULT);
		animParams.addParameter(faultFilterParam);
		faultFilterParam.addParameterChangeListener(this);
		
		controlClickedElements = new ArrayList<>();
		
		//todo
		fadeYearsParam = new DoubleParameter(FADE_YEARS_PARAM_NAME, 0d, Double.POSITIVE_INFINITY, new Double(0d));
		animParams.addParameter(fadeYearsParam);
		fadeYearsParam.addParameterChangeListener(this);


		
		onlyCurrentVisibleParam = new BooleanParameter("Hide Other Elements", false);
		animParams.addParameter(onlyCurrentVisibleParam);
		onlyCurrentVisibleParam.addParameterChangeListener(this);
		
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
		SimulatorElement elem = ((SimulatorElementFault)fault).getElement();
		if (!isStepValid(currentStep))
			return getCPT().getNaNColor();
		else {
			Color c;
			if (fadeColors != null)
				c = fadeColors.get(fault.getId());
			else
				c = getColorCacheForStep(currentStep).get(fault.getId());
			if (c == null)
				return getCPT().getNaNColor();
			else
				return c;
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public synchronized int getNumSteps() {
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
	public void setCPTLog(boolean cptLog) {
		super.setCPTLog(cptLog);
		clearCache();
	}

	/* (non-Javadoc)
	 * @see org.scec.geo3d.library.wgcep.faults.anim.EQSimsEventListener#setEvents(java.util.ArrayList)
	 */
	@Override
	public void setEvents(List<? extends SimulatorEvent> events) {
		this.unfilteredevents = events;
		idToUnfilteredStepMap = Maps.newHashMap();
		if (events != null) {
			for (int step=0; step<events.size(); step++)
				idToUnfilteredStepMap.put(events.get(step).getID(), step);
		}
//		clearCache();
		//filterEvents();
	}

	@Override
	public void addRangeChangeListener(ChangeListener l) {
		listeners.add(l);
	}
	
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
	
	public List<FaultSectionPrefData> getSubSectsForEvent(SimulatorEvent event) {
		List<List<SubSectionMapping>> mappingsBundled = subSectMapper.getFilteredSubSectionMappings(event);
		if (mappingsBundled == null)
			// this will happen for small events which break no subsections
			return null;
		List<FaultSectionPrefData> sects = new ArrayList<>();
		for (List<SubSectionMapping> bundle : mappingsBundled)
			for (SubSectionMapping mapping : bundle)
				sects.add(mapping.getSubSect());
		return sects;
	}
	
	public Collection<SimulatorElement> getElementsForSubSect(FaultSectionPrefData subSect) {
		return subSectMapper.getElementsForSection(subSect);
	}
	
	private void filterEvents() {
		// first wait on any colorer change before entering synchronized block, as doing otherwise can cause deadlock
//		try {
//			eventManager.waitOnCalcThread();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		doFilterEvents();
	}
	
	private synchronized void doFilterEvents() {
		double minMag = magMinParam.getValue();
		
		String sectFilterName = sectFilterParam.getValue();
		int filterSectionID;
		if (!sectFilterName.equals(SECT_FILTER_PARAM_DEFAULT))
			filterSectionID = sectNamesMap.get(sectFilterName);
		else
			filterSectionID = -1;
		if (filterSectionID >= 0)
			System.out.println("Filtering on sect id="+filterSectionID);
		
		String faultFilterName = faultFilterParam.getValue();
		HashSet<Integer> filterFault;
		if (!faultFilterName.equals(FAULT_FILTER_PARAM_DEFAULT))
			filterFault = faultMappings.get(faultNamesMap.get(faultFilterName));
		else
			filterFault = null;
		

		
		double startTime = Double.NaN;
		double endTime = Double.NaN;
		
		if (minMag > MAG_PARAM_MIN  || filterSectionID >= 0 || filterFault != null
				|| !Double.isNaN(startTime) || !controlClickedElements.isEmpty()) {
			filterIndexes = new ArrayList<Integer>();
			for (int i=0; i<unfilteredevents.size(); i++) {
				SimulatorEvent event = unfilteredevents.get(i);
				if (event.getMagnitude() < minMag)
					continue;
				if (filterSectionID >= 0 && !event.doesEventIncludeSection(filterSectionID))
					continue;
				if (filterFault != null && !event.doesEventIncludeFault(filterFault)) {
//					System.out.println("Filtered by fault and it failed!");
					continue;
				}
				if (!controlClickedElements.isEmpty()) {
					boolean match = true;
					HashSet<Integer> rupElems = new HashSet<>(Ints.asList(event.getAllElementIDs()));
					for (SimulatorElement elem : controlClickedElements) {
						if (!rupElems.contains(elem.getID())) {
							match = false;
							break;
						}
					}
					if (!match)
						continue;
				}
				if (!Double.isNaN(startTime)) {
					if (event.getTime() < startTime)
						continue;
					if (event.getTime() > endTime)
						break;
				}
				filterIndexes.add(i);
			}
			System.out.println("Filtered out "+(unfilteredevents.size()-filterIndexes.size())+"/"+unfilteredevents.size());
			if (filterIndexes != null && filterIndexes.size() > 600000) {
				System.out.println("Only animating first 600000 events!");
				filterIndexes = filterIndexes.subList(0, 600000);
			}
		} else {
			filterIndexes = null;
		}
//		System.out.println("about to fire colorer change");
		fireColorerChangeEvent();
//		System.out.println("about to check start preload");
		checkStartPreloadThread();
//		System.out.println("about to fire range change");
		fireRangeChangeEvent();
//		setCurrentStep(0);
//		try {
//			if (eventManager != null) {
//				eventManager.animationStepChanged(this);
//				//			eventManager.colorerChanged(getFaultColorer());
//				eventManager.waitOnCalcThread();
//			}
//		} catch (InterruptedException e) {}
//		fireColorerChangeEvent();
////		try {
////			if (eventManager != null) {
////				eventManager.animationStepChanged(this);
//////				eventManager.colorerChanged(getFaultColorer());
////				eventManager.waitOnCalcThread();
////			}
////		} catch (InterruptedException e) {}
//		System.out.println("about to update render window");
		MainGUI.updateRenderWindow();
//		System.out.println("done filter events");
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getSource() == magMinParam) {
			filterEvents();
		} else if (event.getSource() == sectFilterParam) {
			filterEvents();
		} else if (event.getSource() == faultFilterParam) {
			filterEvents();
		} 
		else if (event.getSource() == fadeYearsParam) {
			fadeColors = null;
			fireRangeChangeEvent();
		}else if (event.getSource() == onlyCurrentVisibleParam) {
			fireColorerChangeEvent();
		}
	}

	@Override
	public synchronized int getStepForID(int id) {
		Integer step = idToUnfilteredStepMap.get(id);
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
			magMinParam.setValue(MAG_PARAM_MIN);
			magMinParam.getEditor().setParameter(magMinParam);
			return step;
		} else {
			// it's not a valid ID
			return -1;
		}
	}

	@Override
	public synchronized int getIDForStep(int step) {
		if (filterIndexes == null && step >= 0 && unfilteredevents != null && step < unfilteredevents.size()) {
			return unfilteredevents.get(step).getID();
//			return step+1;
		} else if (filterIndexes != null && step >= 0 && step < filterIndexes.size()) {
//			return filterIndexes.get(step)+1;
			return unfilteredevents.get(filterIndexes.get(step)).getID();
		}
		return -1;
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
	
	private void updateSectionNames() {
		ArrayList<String> strings = Lists.newArrayList();
		strings.add(SECT_FILTER_PARAM_DEFAULT);
		if (sectNamesMap != null && !sectNamesMap.isEmpty()) {
			List<String> names = Lists.newArrayList(sectNamesMap.keySet());
			Collections.sort(names);
			strings.addAll(names);
		}
		StringConstraint sconst = (StringConstraint)sectFilterParam.getConstraint();
		sectFilterParam.setValue(SECT_FILTER_PARAM_DEFAULT);
		sconst.setStrings(strings);
		sectFilterParam.getEditor().setParameter(sectFilterParam);
	}
	
	private void updateFaultNames() {
		ArrayList<String> strings = Lists.newArrayList();
		strings.add(FAULT_FILTER_PARAM_DEFAULT);
		if (faultNamesMap != null && !faultNamesMap.isEmpty()) {
			List<String> names = Lists.newArrayList(faultNamesMap.keySet());
			Collections.sort(names);
			strings.addAll(names);
		}
		StringConstraint sconst = (StringConstraint)faultFilterParam.getConstraint();
		faultFilterParam.setValue(FAULT_FILTER_PARAM_DEFAULT);
		sconst.setStrings(strings);
		faultFilterParam.getEditor().setParameter(faultFilterParam);
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
		this.tools = null;
		this.elements = elements;
		if (elements != null) {
			faultMappings = Maps.newHashMap();
			faultNamesMap = Maps.newHashMap();
			sectNamesMap = Maps.newHashMap();
			if (subSects == null) {
				FaultModels fm = FaultModels.FM3_1;
				DeformationModels geom = DeformationModels.GEOLOGIC;
				subSects = RSQSimUtils.getUCERF3SubSectsForComparison(fm, geom);
			}
			// TODO make this a parameter
			// a rupture is mapped to a subsection if at least this fraction of the subsection (by area) participates
			double minSectFractForInclusion = 0.2;
			subSectMapper = new RSQSimSubSectionMapper(subSects, controlClickedElements, minSectFractForInclusion);
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
		
		updateSectionNames();
		updateFaultNames();
	}

	@Override
	public double getCurrentDuration() {
		// if fixed duration, use that
		if (unfilteredevents != null)
			return getTimeForStep(getNumSteps()-1) - getTimeForStep(0);
		return 0d;
	}
	
	private LinearBlender colorBlender;

	@Override
	public boolean timeChanged(double time) {
		Double fadeYears = fadeYearsParam.getValue();
		if (fadeYears == null || fadeYears == 0d || unfilteredevents == null)
			return false;
		if (colorBlender == null)
			colorBlender = new LinearBlender();
		Color nanColor = getCPT().getNaNColor();
		Map<Integer, Color> fadeColors = Maps.newHashMap();
		for (int step=currentStep; step >= 0; step--) {
			SimulatorEvent event = getEventForStep(step);
			double stepTime = event.getTime();
			Map<Integer, Color> eventColors = getColorCacheForStep(step);
			double timeSinceYears = (time - stepTime)/General_EQSIM_Tools.SECONDS_PER_YEAR;
			if (timeSinceYears < 0) {
//				System.out.println("Negative time since! "+timeSinceYears);
				timeSinceYears = 0;
			}
			if (timeSinceYears > fadeYears)
				break;
			// 1: nanColor, 0: event color
			double fade = timeSinceYears/fadeYears;
			for (Integer patchID : eventColors.keySet()) {
				if (fadeColors.containsKey(patchID))
					// patch already present in more recent event, skip
					continue;
				Color eventColor = eventColors.get(patchID);
				Color faded = colorBlender.blend(eventColor,  nanColor, (float)fade);
				fadeColors.put(patchID, faded);
			}
		}
		this.fadeColors = fadeColors;
		return true;
	}
	


	
	//Most likely not needed
	@Override
	public void actorPicked(PickEnabledActor<AbstractFaultSection> actor, AbstractFaultSection reference,
			vtkCellPicker picker, MouseEvent e) {
		if (reference instanceof SimulatorElementFault) {
			SimulatorElementFault fault = (SimulatorElementFault)reference;
			if (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown()) {
				System.out.println("shift down, adding "+fault.getId());
				
				controlClickedElements.add(fault.getElement());
				filterEvents();
			}
		}
	}
}
