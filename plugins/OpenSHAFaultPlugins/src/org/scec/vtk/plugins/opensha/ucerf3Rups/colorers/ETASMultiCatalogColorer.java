package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemSolution;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BPTAveragingTypeOptions;
import org.opensha.sha.earthquake.param.BPTAveragingTypeParam;
import org.opensha.sha.earthquake.param.HistoricOpenIntervalParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.faultSurface.FaultSection;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;
import org.scec.vtk.plugins.opensha.ucerf3Rups.anims.ETASCatalogAnim;

import com.google.common.base.Preconditions;

import scratch.UCERF3.analysis.FaultSysSolutionERF_Calc;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO;
import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO.ETAS_Catalog;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.ETAS.ETAS_SimAnalysisTools;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.utils.LastEventData;
import vtk.vtkActor;

public class ETASMultiCatalogColorer extends CPTBasedColorer implements ParameterChangeListener, UCERF3RupSetChangeListener {
	
	private FileParameter catalogFileParam;
	private EnumParameter<DisplayType> displayTypeParam;
	private DoubleParameter minMagParam;
	private DoubleParameter maxDaysParam;
	private IntegerParameter descendantsOfParam;
	private IntegerParameter scenIndexParam;
	private BooleanParameter displayBoxesParam;
	private BooleanParameter hideNansParam;
	private DoubleParameter opacityParam;
	
	private enum DisplayType {
		ETAS_ALL("ETAS All"),
		UCERF3_TI("UCERF3-TI"),
		UCERF3_TD("UCERF3-TD"),
		UCERF3_TD_GAIN("UCERF3-TD/TI Gain"),
		ETAS_GAIN("ETAS/UCERF3-TD Gain");
		
		private String name;
		private DisplayType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public boolean usesETAS() {
			return this == ETAS_ALL || this == ETAS_GAIN;
		}
		
		public boolean usesTimeIndep() {
			return this == DisplayType.UCERF3_TI || this == UCERF3_TD_GAIN;
		}
		
		public boolean usesTimeDep() {
			return this == UCERF3_TD || this == UCERF3_TD_GAIN || this == ETAS_GAIN;
		}
		
		public boolean isGain() {
			return this == ETAS_GAIN || this == DisplayType.UCERF3_TD_GAIN;
		}
	}
	
	private ParameterList params;
	
	private List<ETAS_Catalog> catalogs;
	
	private FaultSystemRupSet rupSet;
	private FaultSystemSolution sol;
	private Map<Integer, List<LastEventData>> lastEventData;
	private FaultSystemSolutionERF erf;
	
	// mapping of fault section ID to rupture rate
	private Map<Integer, Double> etasFaultRatesMap;
	// UCERF3-TI rates
	private double[] timeIndepFaultRates;
	// UCERF3-TD rates
	private double[] timeDepFaultRates;
	// set of all ETAS eqk ruptures that have been mapped to a fault and should be skipped in bin display
	private HashSet<ETAS_EqkRupture> faultRups;
	
	private vtkActor boxActor;
	private PluginActors actors;
	
	private static CPT getDefaultCPT() {
		CPT cpt;
		try {
			cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		
		cpt = cpt.rescale(-3, 2);
		
		return cpt;
	}
	
	private static CPT getDefaultGainCPT() {
		CPT cpt;
		try {
			cpt = GMT_CPT_Files.GMT_POLAR.instance();
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		
		cpt = cpt.rescale(-1, 1);
		
		return cpt;
	}
	
	private CPT rateCPT;
	private boolean rateCPTLog = true; // default is log
	
	private CPT gainCPT;
	private boolean gainCPTLog = true; // default is log

	public ETASMultiCatalogColorer(PluginActors actors) {
		super(getDefaultCPT(), true);
		
		this.actors = actors;
		
		params = new ParameterList();

		catalogFileParam = new FileParameter("Catalogs Binary/Zip File");
		catalogFileParam.addParameterChangeListener(this);
		params.addParameter(catalogFileParam);
		// if Kevin's machine, open right where you need it
		File defaultInitialDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations");
		if (defaultInitialDir.exists())
			catalogFileParam.setDefaultInitialDir(defaultInitialDir);
		
		displayTypeParam = new EnumParameter<DisplayType>(
				"Display Type", EnumSet.allOf(DisplayType.class), DisplayType.ETAS_ALL, null);
		displayTypeParam.addParameterChangeListener(this);
		params.addParameter(displayTypeParam);
		
		minMagParam = new DoubleParameter("Min Mag", 0d, 10d, new Double(0d));
		minMagParam.addParameterChangeListener(this);
		params.addParameter(minMagParam);
		
		maxDaysParam = new DoubleParameter("Max Days After", 0d, 100*365.25d, new Double(365.25d));
		maxDaysParam.addParameterChangeListener(this);
		params.addParameter(maxDaysParam);
		
		descendantsOfParam = new IntegerParameter("Only Descendants Of", -1, Integer.MAX_VALUE, new Integer(-1));
		descendantsOfParam.addParameterChangeListener(this);
		params.addParameter(descendantsOfParam);
		
		scenIndexParam = new IntegerParameter("Scenario FSS Index", -1, Integer.MAX_VALUE, new Integer(-1));
		scenIndexParam.addParameterChangeListener(this);
		params.addParameter(scenIndexParam);
		
		displayBoxesParam = new BooleanParameter("Display Boxes");
		displayBoxesParam.addParameterChangeListener(this);
		params.addParameter(displayBoxesParam);
		
		hideNansParam = new BooleanParameter("Hide NaNs");
		hideNansParam.setValue(true);
		hideNansParam.addParameterChangeListener(this);
		params.addParameter(hideNansParam);
		
		opacityParam = new DoubleParameter("Gridded Data Opacity", 0d, 1d);
		opacityParam.setDefaultValue(0.7);
		opacityParam.setValue(opacityParam.getDefaultValue());
		opacityParam.addParameterChangeListener(this);
		params.addParameter(opacityParam);
	}

	@Override
	public String getName() {
		return "ETAS Multi-Catalog Density";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		if (etasFaultRatesMap != null) {
			Double val;
			switch (displayTypeParam.getValue()) {
			case ETAS_ALL:
				val = etasFaultRatesMap.get(fault.getId());
				break;
			case UCERF3_TI:
				if (timeIndepFaultRates == null)
					val = Double.NaN;
				else
					val = timeIndepFaultRates[fault.getId()];
				break;
			case UCERF3_TD:
				if (timeDepFaultRates == null)
					val = Double.NaN;
				else
					val = timeDepFaultRates[fault.getId()];
				break;
			case UCERF3_TD_GAIN:
				if (timeIndepFaultRates == null || timeDepFaultRates == null)
					val = Double.NaN;
				else
					val = timeDepFaultRates[fault.getId()] / timeIndepFaultRates[fault.getId()];
				break;
			case ETAS_GAIN:
				Double etasVal = etasFaultRatesMap.get(fault.getId());
				if (timeDepFaultRates == null || etasVal == null)
					val = Double.NaN;
				else
					val = (timeDepFaultRates[fault.getId()] + etasVal) / timeDepFaultRates[fault.getId()];
				break;

			default:
				throw new IllegalStateException("Unknown display type: "+displayTypeParam.getValue());
			}
//			Double val = etasFaultRatesMap.get(fault.getId());
			if (val != null)
				return val;
		}
		return Double.NaN;
	}
	
	@Override
	public Color getColorForValue(double value) {
		if (hideNansParam.getValue() && Double.isNaN(value))
			return null;
		return super.getColorForValue(value);
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}
	
	private void loadCatalogs() {
		catalogs = null;
		
		File file = catalogFileParam.getValue();
		Preconditions.checkState(file.exists(), "File doesn't exist");
		
		try {
			catalogs = ETAS_CatalogIO.loadCatalogs(file);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		loadFaultAssociations();
		clearUCERF3();
		updateUCERF3();
		fireColorerChangeEvent();
	}
	
	@Override
	protected void fireColorerChangeEvent() {
		super.fireColorerChangeEvent();
		clearBoxes();
		if (displayBoxesParam.getValue())
			displayBoxes();
	}
	
	private long getMaxOccurTime() {
		double maxDays = maxDaysParam.getValue();
//		long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
		List<ETAS_EqkRupture> catalog = null;
		if (catalogs == null) {
			catalog = new ArrayList<>();
		} else {
			for (List<ETAS_EqkRupture> myCat : catalogs) {
				if (!myCat.isEmpty()) {
					catalog = myCat;
					break;
				}
			}
			if (catalog == null)
				catalog = new ArrayList<>();
		}
		long ot = ETASCatalogAnim.detectScenarioOT(catalog);
		return ot + (long)(maxDays*ProbabilityModelsCalc.MILLISEC_PER_DAY);
	}

	private void loadFaultAssociations() {
		etasFaultRatesMap = null;
		faultRups = null;
		
		if (catalogs == null || catalogs.isEmpty() || rupSet == null)
			return;
		
		double fractRate = 1d/catalogs.size();
		
		double minMag = minMagParam.getValue();
		long maxTime = getMaxOccurTime();
		
		etasFaultRatesMap = new HashMap<>();
		faultRups = new HashSet<>();
		int descendantsOf = descendantsOfParam.getValue();
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			if (descendantsOf >= 0)
				catalog = ETAS_SimAnalysisTools.getChildrenFromCatalog(catalog, descendantsOf);
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getMag() < minMag)
					continue;
				if (rup.getOriginTime() > maxTime)
					break;
				if (rup.getFSSIndex() >= 0) {
					for (int id : rupSet.getSectionsIndicesForRup(rup.getFSSIndex())) {
						Double val = etasFaultRatesMap.get(id);
						if (val == null)
							val = 0d;
						val += fractRate;
						etasFaultRatesMap.put(id, val);
					}
					faultRups.add(rup);
				}
			}
		}
		System.out.println("Loaded "+faultRups.size()+" fault rups on "+etasFaultRatesMap.size()+" sections");
	}
	
	private void clearUCERF3() {
		timeIndepFaultRates = null;
		timeDepFaultRates = null;
	}
	
	private void updateUCERF3() {
		DisplayType display = displayTypeParam.getValue();
		if (timeIndepFaultRates == null && display.usesTimeIndep())
			calcUCERF3Probs(false);
		if (timeDepFaultRates == null && display.usesTimeDep())
			calcUCERF3Probs(true);
	}
	
	private synchronized void calcUCERF3Probs(boolean timeDep) {
		int index = scenIndexParam.getValue();
		if (timeDep)
			timeDepFaultRates = null;
		else
			timeIndepFaultRates = null;
		if (sol == null || etasFaultRatesMap == null)
			return;
		
		System.out.println("Calculating UCERF3 Probabilities! timeDep="+timeDep);
		
		List<ETAS_EqkRupture> catalog = null;
		if (catalogs == null) {
			catalog = new ArrayList<>();
		} else {
			for (List<ETAS_EqkRupture> myCat : catalogs) {
				if (!myCat.isEmpty()) {
					catalog = myCat;
					break;
				}
			}
			if (catalog == null)
				catalog = new ArrayList<>();
		}
		int startYear = ETASCatalogAnim.detectScenarioYear(catalog);
		double duration = maxDaysParam.getValue()/365.25;
		
		if (erf == null) {
			erf = new FaultSystemSolutionERF(sol);
			erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
			erf.setParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME, false);
			// set assuming time dependence, will change later if not
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_PREF_BLEND);
			BPTAveragingTypeOptions aveType = BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE;
			erf.setParameter(BPTAveragingTypeParam.NAME, aveType);
			erf.setParameter(HistoricOpenIntervalParam.NAME, startYear-1875d);
			erf.setParameter(AleatoryMagAreaStdDevParam.NAME, 0.0);
		}
		if (timeDep) {
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_PREF_BLEND);
			erf.getTimeSpan().setStartTime(startYear);
			if (lastEventData == null) {
				try {
					lastEventData = LastEventData.load();
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			}
			// clear any old last event data
			for (FaultSection sect : rupSet.getFaultSectionDataList())
				sect.setDateOfLastEvent(Long.MIN_VALUE);
			LastEventData.populateSubSects(rupSet.getFaultSectionDataList(), lastEventData);
			if (index >= 0) {
				// use the start time of the ERF, not the scenario time. our haphazard use of fractional
				// days (365.25 days/yr to account for leap years) can lead to the scenario happening slightly
				// after the ERF start time, which makes the ERF ignore elastic rebound. Fix this by using
				// the ERF start time
				long ot = erf.getTimeSpan().getStartTimeInMillis()-1;
				erf.setFltSystemSourceOccurranceTimeForFSSIndex(index, ot);
			}
		} else {
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		}
		erf.getTimeSpan().setDuration(duration);		
		erf.updateForecast();
		if (timeDep)
			Preconditions.checkState(erf.getTimeSpan().getStartTimeYear() == startYear);
		
		double[] erfParticRates = FaultSysSolutionERF_Calc.calcParticipationRateForAllSects(erf, minMagParam.getValue());
		if (duration != 1d)	
			for (int i=0; i<erfParticRates.length; i++)
				erfParticRates[i] *= duration;
		
		if (timeDep)
			timeDepFaultRates = erfParticRates;
		else
			timeIndepFaultRates = erfParticRates;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == catalogFileParam) {
			loadCatalogs();
		} else if (event.getParameter() == minMagParam || event.getParameter() == maxDaysParam) {
			clearUCERF3();
			updateUCERF3();
			if (catalogs != null) {
				loadFaultAssociations();
			}
			fireColorerChangeEvent();
		} else if (event.getParameter() == displayBoxesParam || event.getParameter() == opacityParam) {
			clearBoxes();
			if (displayBoxesParam.getValue())
				displayBoxes();
		} else if (event.getParameter() == hideNansParam) {
			if (catalogs != null) {
				fireColorerChangeEvent();
			}
		} else if (event.getParameter() == descendantsOfParam) {
			if (catalogs != null) {
				loadFaultAssociations();
				if (displayTypeParam.getValue().usesETAS())
					fireColorerChangeEvent();
			}
		} else if (event.getParameter() == scenIndexParam) {
			erf = null;
			timeDepFaultRates = null;
			updateUCERF3();
			if (displayTypeParam.getValue().usesTimeDep())
				fireColorerChangeEvent();
		} else if (event.getParameter() == displayTypeParam) {
			DisplayType display = displayTypeParam.getValue();
			updateUCERF3();
			DisplayType prevDisplay = (DisplayType)event.getOldValue();
			if (display.isGain() && !prevDisplay.isGain()) {
				// was rate, now gain
				rateCPT = getCPT();
				rateCPTLog = isCPTLog();
				if (gainCPT == null)
					gainCPT = getDefaultGainCPT();
				setCPT(gainCPT, gainCPTLog);
			} else if (!display.isGain() && prevDisplay.isGain()) {
				// was gain, now rate
				gainCPT = getCPT();
				gainCPTLog = isCPTLog();
				if (rateCPT == null)
					// should never happen, oh well
					rateCPT = getDefaultCPT();
				setCPT(rateCPT, rateCPTLog);
			}
			fireColorerChangeEvent();
		}
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.rupSet = rupSet;
		this.sol = sol;
		clearUCERF3();
		updateUCERF3();
		if (catalogs != null) {
			loadFaultAssociations();
			fireColorerChangeEvent();
		}
	}
	
	private void clearBoxes() {
		if (boxActor != null) {
			actors.removeActor(boxActor);
			boxActor = null;
		}
	}
	
	private void displayBoxes() {
		clearBoxes();
		if (catalogs == null || catalogs.isEmpty())
			return;
		
		double minMag = minMagParam.getValue();
		long maxTime = getMaxOccurTime();
		
		double fractRate = 1d/catalogs.size();
		
		// need to keep track of total rate for each bin, some data structure goes here
		GriddedRegion reg = new CaliforniaRegions.RELM_TESTING_GRIDDED();
		GriddedGeoDataSet xyz = new GriddedGeoDataSet(reg, false);
		
		// for each catalog
		int descendantsOf = descendantsOfParam.getValue();
		for (List<ETAS_EqkRupture> catalog : catalogs) {
			if (descendantsOf >= 0)
				catalog = ETAS_SimAnalysisTools.getChildrenFromCatalog(catalog, descendantsOf);
			// for each rupture in that catalog
			for (ETAS_EqkRupture rup : catalog) {
				// if it's fault based, we want to display it on the fault instead...skip here
				if (faultRups.contains(rup))
					continue;
				if (rup.getMag() < minMag)
					continue;
				if (rup.getOriginTime() > maxTime)
					break;
				
				Location loc = rup.getHypocenterLocation();
				int index = reg.indexForLocation(loc);
				if (index < 0)
					continue;
				xyz.set(index, xyz.get(index)+fractRate);
			}
		}
		boolean hideNans = hideNansParam.getValue();
		for (int index=0; index<xyz.size() && hideNans; index++)
			if (xyz.get(index) == 0)
				xyz.set(index, Double.NaN);
		
		// finally, you need to get a color for each bin and display them
		// you can use this CPT object to get a color for each value: cpt.getColor((float)value);
		// only display if value > 0
		boxActor = ParticipationRateColorer.buildActorForGriddedData(xyz, isCPTLog(), getCPT(), 0d, hideNans, 0.7);
		actors.addActor(boxActor);
		MainGUI.updateRenderWindow();
	}

}