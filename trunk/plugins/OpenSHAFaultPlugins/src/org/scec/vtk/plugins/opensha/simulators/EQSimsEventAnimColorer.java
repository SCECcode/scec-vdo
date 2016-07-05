package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.IDBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.anim.TimeBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EQSimsEventAnimColorer extends CPTBasedColorer implements
		TimeBasedFaultAnimation, IDBasedFaultAnimation, ParameterChangeListener, EQSimsEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static String NAME = "Simulator Output Animation: slip (m) per event";
	
	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	private List<EQSIM_Event> unfilteredevents;
	private Map<Integer, Integer> idToUnfilteredStepMap;
	private ArrayList<Integer> filterIndexes;
	private HashMap<Integer, Color>[] eventColorCache;
	
	private int currentStep = -1;
	
	private static final String MAG_FILTER_PARAM_NAME = "Min Mag";
	private static final Double MAG_FILTER_PARAM_MIN = 0d;
	private static final Double MAG_FILTER_PARAM_MAX = 10d;
	private static final Double MAG_FILTER_PARAM_DEFAULT = MAG_FILTER_PARAM_MIN;
	private DoubleParameter magFilterParam =
		new DoubleParameter(MAG_FILTER_PARAM_NAME, MAG_FILTER_PARAM_MIN, MAG_FILTER_PARAM_MAX, MAG_FILTER_PARAM_DEFAULT);
	
	private static final String SECT_FILTER_PARAM_NAME = "Only Events Involing";
	private static final String SECT_FILTER_PARAM_DEFAULT = "(all sections)";
	private StringParameter sectFilterParam;
	
	private static final String FAULT_FILTER_PARAM_NAME = "Filter By Fault";
	private static final String FAULT_FILTER_PARAM_DEFAULT = "(all faults)";
	private StringParameter faultFilterParam;
	
	private static final String SUPRA_SEISMOGENIC_FILTER_PARAM_NAME = "Only Supra-Seismogenic";
	private BooleanParameter supraSeismogenicFilterParam;
	
	private Map<Integer, HashSet<Integer>> faultMappings;
	private Map<String, Integer> faultNamesMap;
	private Map<String, Integer> sectNamesMap;
	
	private List<SimulatorElement> elements;
	private General_EQSIM_Tools tools;
	
	private ParameterList animParams = new ParameterList();
	
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
	
	public EQSimsEventAnimColorer() {
		super(getDefaultCPT(), false);
		
		animParams.addParameter(magFilterParam);
		magFilterParam.addParameterChangeListener(this);
		
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
		
		supraSeismogenicFilterParam = new BooleanParameter(SUPRA_SEISMOGENIC_FILTER_PARAM_NAME, false);
		animParams.addParameter(supraSeismogenicFilterParam);
		supraSeismogenicFilterParam.addParameterChangeListener(this);
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
			Color c = getColorCacheForStep(currentStep).get(fault.getId());
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
	}
	
	private HashMap<Integer, Color> getColorCacheForStep(int step) {
		if (unfilteredevents == null)
			return null;
		if (filterIndexes == null)
			return eventColorCache[step];
		else
			return eventColorCache[filterIndexes.get(step)];
	}
	
	private EQSIM_Event getEventForStep(int step) {
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
		cacheEvents();
	}

	@SuppressWarnings("unchecked")
	private void cacheEvents() {
		if (unfilteredevents == null || unfilteredevents.size() == 0) {
			eventColorCache = null;
			return;
		}
		eventColorCache = new HashMap[unfilteredevents.size()];
		for (int i=0; i<unfilteredevents.size(); i++) {
			EQSIM_Event event = unfilteredevents.get(i);
			int[] ids = event.getAllElementIDs();
			double[] slips = event.getAllElementSlips();
			eventColorCache[i] = new HashMap<Integer, Color>();
			
			for (int j=0; ids != null && j<ids.length; j++) {
				int id = ids[j];
				double slip = slips[j];
				Color c = getColorForValue(slip);
				eventColorCache[i].put(id, c);
			}
		}
	}
	
	@Override
	public void setCPTLog(boolean cptLog) {
		super.setCPTLog(cptLog);
		cacheEvents();
	}

	/* (non-Javadoc)
	 * @see org.scec.geo3d.library.wgcep.faults.anim.EQSimsEventListener#setEvents(java.util.ArrayList)
	 */
	@Override
	public void setEvents(List<EQSIM_Event> events) {
		if (events != null && events.size() > 600000) {
			System.out.println("Only animating first 600000 events!");
			events = events.subList(0, 600000);
		}
		this.unfilteredevents = events;
		idToUnfilteredStepMap = Maps.newHashMap();
		if (events != null) {
			for (int step=0; step<events.size(); step++)
				idToUnfilteredStepMap.put(events.get(step).getID(), step);
		}
		cacheEvents();
		filterEvents();
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

	@Override
	public String getCurrentLabel() {
		if (!isStepValid(currentStep))
			return null;
		EQSIM_Event event = getEventForStep(currentStep);
		return "Mag: " + event.getMagnitude();
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
		double magThresh = magFilterParam.getValue();
		String sectFilterName = sectFilterParam.getValue();
		int filterSectionID;
		if (!sectFilterName.equals(SECT_FILTER_PARAM_DEFAULT))
			filterSectionID = sectNamesMap.get(sectFilterName);
		else
			filterSectionID = -1;
		String faultFilterName = faultFilterParam.getValue();
		HashSet<Integer> filterFault;
		if (!faultFilterName.equals(FAULT_FILTER_PARAM_DEFAULT))
			filterFault = faultMappings.get(faultNamesMap.get(faultFilterName));
		else
			filterFault = null;
		boolean supraSeis = supraSeismogenicFilterParam.getValue();
		if (magThresh > 0.0 || filterSectionID >= 0 || filterFault != null || supraSeis) {
			filterIndexes = new ArrayList<Integer>();
			for (int i=0; i<unfilteredevents.size(); i++) {
				EQSIM_Event event = unfilteredevents.get(i);
//				if (i % 1000 == 0) {
//					System.out.println("Testing rup "+i);
//					HashSet<Integer> rupSects = new HashSet<Integer>();
//					for (EventRecord e : event)
//						rupSects.add(e.getSectionID());
//					System.out.println("Sections: "+Joiner.on(",").join(rupSects));
//				}
				if (event.getMagnitude() < magThresh)
					continue;
				if (filterSectionID >= 0 && !event.doesEventIncludeSection(filterSectionID))
					continue;
				if (filterFault != null && !event.doesEventIncludeFault(filterFault)) {
//					System.out.println("Filtered by fault and it failed!");
					continue;
				}
				if (supraSeis) {
					if (tools == null)
						tools = new General_EQSIM_Tools(elements);
					if (!tools.isEventSupraSeismogenic(event, Double.NaN))
						continue;
				}
				filterIndexes.add(i);
			}
			System.out.println("Filtered out "+(unfilteredevents.size()-filterIndexes.size())+"/"+unfilteredevents.size());
		} else {
			filterIndexes = null;
		}
		fireRangeChangeEvent();
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getSource() == magFilterParam) {
			filterEvents();
		} else if (event.getSource() == sectFilterParam) {
			filterEvents();
		} else if (event.getSource() == faultFilterParam) {
			filterEvents();
		} else if (event.getSource() == supraSeismogenicFilterParam) {
			filterEvents();
		}
	}

	@Override
	public int getStepForID(int id) {
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
			magFilterParam.setValue(MAG_FILTER_PARAM_MIN);
			magFilterParam.getEditor().setParameter(magFilterParam);
			return step;
		} else {
			// it's not a valid ID
			return -1;
		}
	}

	@Override
	public int getIDForStep(int step) {
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setGeometry(List<SimulatorElement> elements) {
		faultMappings = null;
		faultNamesMap = null;
		sectNamesMap = null;
		this.tools = null;
		this.elements = elements;
		if (elements != null) {
			faultMappings = Maps.newHashMap();
			faultNamesMap = Maps.newHashMap();
			sectNamesMap = Maps.newHashMap();
			Map<Integer, HashSet<String>> faultNames = Maps.newHashMap();
			for (SimulatorElement e : elements) {
				Integer faultID = e.getFaultID();
				if (faultID < 0)
					continue;
				Integer sectID = e.getSectionID();
				String sectName = e.getSectionName();
				HashSet<Integer> sectsForFault = faultMappings.get(faultID);
				if (sectsForFault == null) {
					sectsForFault = new HashSet<Integer>();
					faultMappings.put(faultID, sectsForFault);
					faultNames.put(faultID, new HashSet<String>());
				}
				sectsForFault.add(sectID);
				faultNames.get(faultID).add(sectName);
				if (!sectNamesMap.containsKey(sectName))
					sectNamesMap.put(sectName, sectID);
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

}