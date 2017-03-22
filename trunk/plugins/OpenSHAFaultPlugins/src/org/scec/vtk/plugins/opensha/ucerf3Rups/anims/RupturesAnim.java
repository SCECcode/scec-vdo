package org.scec.vtk.plugins.opensha.ucerf3Rups.anims;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.WeightedSampler;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.AnimMultiColorerPickHandlerWrapper;
import org.scec.vtk.commons.opensha.faults.anim.AnimMultiColorerWrapper;
import org.scec.vtk.commons.opensha.faults.anim.IDBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.colorers.RakeColorer;
import org.scec.vtk.commons.opensha.faults.colorers.SlipRateColorer;
import org.scec.vtk.commons.opensha.faults.colorers.StrikeColorer;
import org.scec.vtk.commons.opensha.surfaces.FaultSectionActorList;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import vtk.vtkActor;
import vtk.vtkCellPicker;

public class RupturesAnim implements IDBasedFaultAnimation,
		UCERF3RupSetChangeListener, ParameterChangeListener, PickHandler<AbstractFaultSection> {
	
	private static final String SECTION_SELECT_PARAM_NAME = "Ruptures for Section";
	private static final String SECTION_SELECT_PARAM_INFO = "Filters the rupture list to only include\n" +
			"ruptures involving the given section.\n" +
			"\nYou can also shift+click on a secion\n" +
			"in the 3D view to set this.";
	private static final String SECTION_SELECT_PARAM_DEFAULT = "(all sections)";
	private StringParameter sectionSelect;
	private ArrayList<String> sectionNames;
	private int selectedSectionID = -1;
	
	private static final String MIN_MAG_PARAM_NAME = "Min Mag.";
	private static final double MIN_MAG_PARAM_DEFAULT = 0d;
	private DoubleParameter minMagParam;
	
	private static final String MAX_MAG_PARAM_NAME = "Max Mag.";
	private static final double MAX_MAG_PARAM_DEFAULT = 10d;
	private DoubleParameter maxMagParam;
	
	private static final String MIN_RATE_PARAM_NAME = "Min Rate.";
	private static final double MIN_RATE_PARAM_DEFAULT = 0d;
	private DoubleParameter minRateParam;
	
	private static final String MAX_RATE_PARAM_NAME = "Max Rate.";
	private static final double MAX_RATE_PARAM_DEFAULT = 100d;
	private DoubleParameter maxRateParam;
	
	enum SortType {
		ID("Rupture ID"),
		NUM_SECTIONS("Num Sections (Decreasing)"),
		MAGNITUDE_DECREASING("Mag (Decreasing)"),
		MAGNITUDE_INCREASING("Mag (Increasing)"),
		RATE("Rate (Decreasing)"),
		RANDOM("Random"),
//		POISSON_SAMPLING("Rate-Weighted Random Sample");
		POISSON_SAMPLING("Rate-Weighted Sample"),
		MOMENT_RATE("Moment*Rate (Decreasing)");
		
		private String name;
		
		private SortType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public static SortType fromString(String str) {
			for (SortType t : values())
				if (t.toString().equals(str))
					return t;
			throw new NoSuchElementException("Unknown Sort Type: "+str);
		}
	}
	
	private static final String SORT_PARAM_NAME = "Sort By";
	private static final String SORT_PARAM_DEFAULT = SortType.ID.toString();
	private StringParameter sortParam;
	private SortType sortType;
	private Comparator<Integer> rupComparator;
	
	private static final String POISSON_SAMPLES_PARAM_NAME = "# Samples";
	private static final int POISSON_SAMPLES_DEFAULT = 1000;
	private static final int POISSON_SAMPLES_MIN = 1;
	private static final int POISSON_SAMPLES_MAX = 1000000;
	private IntegerParameter poissonSamplesParam;
	
	private static final String SURFS_VISIBLE_PARAM_NAME = "Only Ruptures Visible";
	private BooleanParameter surfsVisibleParam;
	
	private static final String DISPLAY_PARAM_NAME = "Display Type";
	private enum DisplayType {
		SOLID("Solid Colors"),
		RATE("Color By Rate"),
		MAG("Color By Mag"),
		SLIP("Color By Rupture Slip"),
		SECTION_SLIP("Color By Section Slip Rate"),
		RAKE("Color By Rake"),
		STRIKE("Color By Strike"),
		ORDER("Color By Section Order"),
		PARENT("Color By Parent Section");
		
		private String name;
		private DisplayType(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	private static final DisplayType DISPLAY_DEFAULT = DisplayType.SOLID;
	private EnumParameter<DisplayType> displayParam;
	private SlipRateColorer sectionSlipColorer = new SlipRateColorer() {

		@Override
		public double getValue(AbstractFaultSection fault) {
			if (isSectionAnimated(fault.getId()))
				return super.getValue(fault);
			return Double.NaN;
		}
		
	};
	private RakeColorer rakeColorer = new RakeColorer() {

		@Override
		public double getValue(AbstractFaultSection fault) {
			if (isSectionAnimated(fault.getId()))
				return super.getValue(fault);
			return Double.NaN;
		}
		
	};
	private StrikeColorer strikeColorer = new StrikeColorer() {

		@Override
		public double getValue(AbstractFaultSection fault) {
			if (isSectionAnimated(fault.getId()))
				return super.getValue(fault);
			return Double.NaN;
		}
		
	};
	private CPTBasedColorer magColorer = new CPTBasedColorer(SlipRateColorer.getDefaultCPT().rescale(6, 8.5), false) {
		
		@Override
		public String getName() {
			return DisplayType.MAG.toString();
		}
		
		@Override
		public double getValue(AbstractFaultSection fault) {
			if (isSectionAnimated(fault.getId())) {
				int rupID = ruptureIDs.get(curStep);
				return rupSet.getMagForRup(rupID);
			}
			return Double.NaN;
		}
	};
	private CPTBasedColorer rateColorer = new CPTBasedColorer(SlipRateColorer.getDefaultCPT().rescale(1e-7, 0.05), false) {
		
		@Override
		public String getName() {
			return DisplayType.RATE.toString();
		}
		
		@Override
		public double getValue(AbstractFaultSection fault) {
			if (sol != null && isSectionAnimated(fault.getId())) {
				int rupID = ruptureIDs.get(curStep);
				return sol.getRateForRup(rupID);
			}
			return Double.NaN;
		}
	};
	private CPTBasedColorer orderColorer = new CPTBasedColorer(SlipRateColorer.getDefaultCPT().rescale(0, 1), false) {
		
		@Override
		public String getName() {
			return DisplayType.RATE.toString();
		}
		
		@Override
		public double getValue(AbstractFaultSection fault) {
			if (rupSet == null)
				return Double.NaN;
			if (curStep < 0)
				return Double.NaN;
			int rupID = ruptureIDs.get(curStep);
			List<Integer> list = rupSet.getSectionsIndicesForRup(rupID);
			int ind = list.indexOf(fault.getId());
			if (ind >= 0) {
//				System.out.println("ind: "+ind+"/"+list.size());
				return (double)ind / (double)(list.size()-1);
			}
			return Double.NaN;
		}
	};
	private int last_parent_color_rupture_id = -1;
	private List<Integer> last_parents = null;
	private CPTBasedColorer parentColorer = new CPTBasedColorer(SlipRateColorer.getDefaultCPT().rescale(0, 1), false) {
		
		@Override
		public String getName() {
			return DisplayType.RATE.toString();
		}
		
		@Override
		public double getValue(AbstractFaultSection fault) {
			if (isSectionAnimated(fault.getId())) {
				int rupID = ruptureIDs.get(curStep);
				List<Integer> parents;
				if (rupID == last_parent_color_rupture_id) {
					parents = last_parents;
				} else {
					parents = rupSet.getParentSectionsForRup(rupID);
					last_parent_color_rupture_id = rupID;
					last_parents = parents;
				}
				if (parents.size() == 1)
					return 0;
				int ind = parents.indexOf(rupSet.getFaultSectionData(fault.getId()).getParentSectionId());
				if (ind >= 0)
					return (double)ind / (double)(parents.size()-1);
			}
			return Double.NaN;
		}
	};
	private CPTBasedColorer slipColorer = new CPTBasedColorer(SlipRateColorer.getDefaultCPT().rescale(1e-2, 10), false) {
		
		private double[] slips;
		private int rupID;
		private List<Integer> sections;
		
		@Override
		public String getName() {
			return DisplayType.SLIP.toString();
		}
		
		@Override
		public double getValue(AbstractFaultSection fault) {
			if (invRupSet == null)
				return Double.NaN;
			if (curStep < 0)
				return Double.NaN;
			int rupID = ruptureIDs.get(curStep);
			if (slips == null || rupID != this.rupID) {
				try {
					slips = invRupSet.getSlipOnSectionsForRup(rupID);
				} catch (NullPointerException e) {
					// this means that slips are null...normal, but we can't display them
					return Double.NaN;
				}
				sections = rupSet.getSectionsIndicesForRup(rupID);
				this.rupID = rupID;
			}
			int index = sections.indexOf(fault.getId());
			if (index >= 0)
				return slips[index];
			return Double.NaN;
		}
	};
	private FaultColorer simpleFaultColorer = new FaultColorer() {
		
		@Override
		public String getName() {
			return "Selected Rupture";
		}
		
		@Override
		public void setColorerChangeListener(ColorerChangeListener l) {}
		
		@Override
		public ParameterList getColorerParameters() {
			return null;
		}
		
		@Override
		public Color getColor(AbstractFaultSection fault) {
			if (rupSet == null)
				return Color.LIGHT_GRAY;
			int secID = fault.getId();
			if (isSectionAnimated(secID))
				return Color.RED;
			return Color.GREEN;
		}
		
		public String getLegendLabel() {
			return getName();
		}
	};
	private AnimMultiColorerWrapper multiColorerWrapper;
	
	private ParameterList params;
	
	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	private FaultSystemSolution sol;
	private FaultSystemRupSet rupSet;
	private InversionFaultSystemRupSet invRupSet;
	
	private List<Integer> ruptureIDs;
	
	private int curStep = -1;
	
	public RupturesAnim() {
		sectionNames = new ArrayList<String>();
		sectionNames.add(SECTION_SELECT_PARAM_DEFAULT);
		sectionSelect = new StringParameter(SECTION_SELECT_PARAM_NAME, sectionNames, SECTION_SELECT_PARAM_DEFAULT);
		sectionSelect.addParameterChangeListener(this);
		sectionSelect.setInfo(SECTION_SELECT_PARAM_INFO);
		
		minMagParam = new DoubleParameter(MIN_MAG_PARAM_NAME, 0d, 10d);
		minMagParam.setValue(MIN_MAG_PARAM_DEFAULT);
		minMagParam.addParameterChangeListener(this);
		
		maxMagParam = new DoubleParameter(MAX_MAG_PARAM_NAME, 0d, 10d);
		maxMagParam.setValue(MAX_MAG_PARAM_DEFAULT);
		maxMagParam.addParameterChangeListener(this);
		
		minRateParam = new DoubleParameter(MIN_RATE_PARAM_NAME, 0d, 10d);
		minRateParam.setValue(MIN_RATE_PARAM_DEFAULT);
		minRateParam.addParameterChangeListener(this);
		
		maxRateParam = new DoubleParameter(MAX_RATE_PARAM_NAME, 0d, 100d);
		maxRateParam.setValue(MAX_RATE_PARAM_DEFAULT);
		maxRateParam.addParameterChangeListener(this);
		
		ArrayList<String> sortStrings = new ArrayList<String>();
		for (SortType t : SortType.values())
			sortStrings.add(t.toString());
		sortParam = new StringParameter(SORT_PARAM_NAME, sortStrings, SORT_PARAM_DEFAULT);
		sortType = SortType.fromString(sortParam.getValue());
		sortParam.addParameterChangeListener(this);
		
		poissonSamplesParam = new IntegerParameter(POISSON_SAMPLES_PARAM_NAME, POISSON_SAMPLES_MIN, POISSON_SAMPLES_MAX);
		poissonSamplesParam.setDefaultValue(POISSON_SAMPLES_DEFAULT);
		poissonSamplesParam.setValueAsDefault();
		poissonSamplesParam.getEditor().getComponent(); // make it paint before disabling it
		poissonSamplesParam.getEditor().setEnabled(sortType == SortType.POISSON_SAMPLING);
		poissonSamplesParam.addParameterChangeListener(this);
		
		surfsVisibleParam = new BooleanParameter(SURFS_VISIBLE_PARAM_NAME, false);
		surfsVisibleParam.addParameterChangeListener(this);
		
		rupComparator = new Comparator<Integer>() {
			
			@Override
			public int compare(Integer o1, Integer o2) {
				if (rupSet == null)
					return 0;
				
				switch (sortType) {
				case ID:
					return o1.compareTo(o2);
				case NUM_SECTIONS:
					return ((Integer)rupSet.getSectionsIndicesForRup(o2).size()).compareTo(
							rupSet.getSectionsIndicesForRup(o1).size());
				case MAGNITUDE_DECREASING:
					return ((Double)rupSet.getMagForRup(o2)).compareTo(rupSet.getMagForRup(o1));
				case MAGNITUDE_INCREASING:
					return ((Double)rupSet.getMagForRup(o1)).compareTo(rupSet.getMagForRup(o2));
				case RATE:
					if (sol == null)
						return 0;
					return ((Double)sol.getRateForRup(o2)).compareTo(sol.getRateForRup(o1));
				case MOMENT_RATE:
					if (sol == null)
						return 0;
					Double mr1 = getMomentRate(o1);
					Double mr2 = getMomentRate(o2);
					return mr2.compareTo(mr1);
				default:
					break;
				}
				return 0;
			}
		};
		
		displayParam = new EnumParameter<DisplayType>(DISPLAY_PARAM_NAME,
				EnumSet.allOf(DisplayType.class), DISPLAY_DEFAULT, null);
		displayParam.setValue(DISPLAY_DEFAULT);
		displayParam.addParameterChangeListener(this);
		rateColorer.setCPTLog(true);
		rateColorer.getCPT().setBelowMinColor(rateColorer.getCPT().getMinColor());
		try {
			CPT max01 = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, 1);
			max01.setNanColor(Color.GRAY);
			orderColorer.setCPT(max01);
			parentColorer.setCPT((CPT)max01.clone());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		multiColorerWrapper = new AnimMultiColorerPickHandlerWrapper(getName(), getColorerForDisplayParam(), this);
		
		params = new ParameterList();
		
		params.addParameter(sectionSelect);
		params.addParameter(sortParam);
		params.addParameter(minMagParam);
		params.addParameter(maxMagParam);
		params.addParameter(minRateParam);
		params.addParameter(maxRateParam);
		params.addParameter(displayParam);
		params.addParameter(poissonSamplesParam);
		params.addParameter(surfsVisibleParam);
	}
	
	private double getMomentRate(int rupIndex) {
		if (invRupSet == null)
			return Double.NaN;
		return sol.getRateForRup(rupIndex) * FaultMomentCalc.getMoment(
				invRupSet.getAreaForRup(rupIndex), invRupSet.getAveSlipForRup(rupIndex));
	}
	
	private double getAveSlip(int rupIndex) {
		if (invRupSet == null)
			return Double.NaN;
		return invRupSet.getAveSlipForRup(rupIndex);
	}

	@Override
	public void addRangeChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	@Override
	public int getNumSteps() {
		if (rupSet == null)
			return 0;
		return ruptureIDs.size();
	}

	@Override
	public void setCurrentStep(int step) {
		
		this.curStep = step;
	}

	@Override
	public boolean includeStepInLabel() {
		return false;
	}

	@Override
	public String getCurrentLabel() {
		if (curStep < 0 || rupSet == null || ruptureIDs.isEmpty())
			return null;
		int rupID = ruptureIDs.get(curStep);
		String str = "Rupture "+rupID;
		str += " ("+rupSet.getSectionsIndicesForRup(rupID).size()+" sects).";
		str += " Mag: "+(float)rupSet.getMagForRup(rupID);
		try {
			str += " Area: "+(float)rupSet.getAreaForRup(rupID)+" m^2";
		} catch (Exception e) {};
		try {
			str += " Len: "+(float)rupSet.getLengthForRup(rupID)+" m";
		} catch (Exception e) {};
		if (sol != null) {
			str += " Rate: "+(float)sol.getRateForRup(rupID);
			double moRate = getMomentRate(rupID);
			if (!Double.isNaN(moRate))
				str += " MoRate: "+(float)moRate;
			try {
				double aveSlip = getAveSlip(rupID);
				if (!Double.isNaN(aveSlip))
					str += " aveSlip: "+(float)aveSlip+" m";
			} catch (Exception e) {};
		}
		return str;
	}

	@Override
	public ParameterList getAnimationParameters() {
		return params;
	}

	@Override
	public Boolean getFaultVisibility(AbstractFaultSection fault) {
		// no change in visibility, return null
		if (surfsVisibleParam.getValue()) {
			// we want to only show ruptures
			return isSectionAnimated(fault.getId());
		}
		// else we don't care
		return null;
	}

	@Override
	public FaultColorer getFaultColorer() {
		return multiColorerWrapper;
	}

	@Override
	public void fireRangeChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener l : listeners)
			l.stateChanged(e);
	}

	@Override
	public String getName() {
		return "Ruptures Animation";
	}
	
	private boolean isSectionAnimated(int secID) {
		if (rupSet == null)
			return false;
		if (curStep < 0)
			return false;
		int rupID = ruptureIDs.get(curStep);
		return rupSet.getSectionsIndicesForRup(rupID).contains(secID);
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.rupSet = rupSet;
		if (rupSet instanceof InversionFaultSystemRupSet)
			invRupSet = (InversionFaultSystemRupSet)rupSet;
		else
			invRupSet = null;
		this.sol = sol;
		
		sectionSelect.removeParameterChangeListener(this);
		StringConstraint sconst = (StringConstraint) sectionSelect.getConstraint();
		sectionNames = new ArrayList<String>();
		sectionNames.add(SECTION_SELECT_PARAM_DEFAULT);
		if (rupSet != null) {
			for (FaultSectionPrefData data : rupSet.getFaultSectionDataList()) {
				String name = data.getSectionId()+". "+data.getSectionName();
				name = name.replaceAll("Subsection", "#");
				sectionNames.add(name);
			}
		}
		sectionSelect.setValue(SECTION_SELECT_PARAM_DEFAULT);
		sconst.setStrings(sectionNames);
		sectionSelect.getEditor().refreshParamEditor();
		sectionSelect.addParameterChangeListener(this);
		
		updateRuptures();
	}

	@Override
	public int getStepForID(int id) {
		if (ruptureIDs == null || ruptureIDs.isEmpty())
			return -1;
		return ruptureIDs.indexOf(id);
	}

	@Override
	public int getIDForStep(int step) {
		if (ruptureIDs == null || ruptureIDs.isEmpty())
			return -1;
		return ruptureIDs.get(step);
	}
	
	private void updateRuptures() {
		ruptureIDs = new ArrayList<Integer>();
		if (rupSet != null) {
			double minMag = minMagParam.getValue();
			double maxMag = maxMagParam.getValue();
			double minRate = minRateParam.getValue();
			double maxRate = maxRateParam.getValue();
			for (int rupID=0; rupID<rupSet.getNumRuptures(); rupID++) {
				if (selectedSectionID >= 0) {
					if (!rupSet.getSectionsIndicesForRup(rupID).contains(selectedSectionID))
						continue;
				}
				double mag = rupSet.getMagForRup(rupID);
				if (mag < minMag || mag > maxMag)
					continue;
				if (sol != null) {
					double rate = sol.getRateForRup(rupID);
					if (rate < minRate || rate > maxRate)
						continue;
				}
				ruptureIDs.add(rupID);
			}
		}
		if (sortType == SortType.RANDOM)
			Collections.shuffle(ruptureIDs);
		else if (sortType == SortType.POISSON_SAMPLING)
			ruptureIDs = generatePoissonSequence(ruptureIDs);
		else
			Collections.sort(ruptureIDs, rupComparator);
		fireRangeChangeEvent();
	}
	
	private List<Integer> generatePoissonSequence(List<Integer> rupIDs) {
		// if it's not a solution, just return an empty list
		if (sol == null)
			return new ArrayList<Integer>();
		
		ArrayList<Integer> nonZeros = new ArrayList<Integer>();
		ArrayList<Double> rates = new ArrayList<Double>();
		
		for (int rupID : rupIDs) {
			double rate = sol.getRateForRup(rupID);
			if (rate > 0) {
				nonZeros.add(rupID);
				rates.add(rate);
			}
		}
		
		WeightedSampler<Integer> s = new WeightedSampler<Integer>(nonZeros, rates);
		
		int numSamples = poissonSamplesParam.getValue();
		return s.generateSeries(numSamples);
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == sectionSelect) {
			int sectionIndex = sectionNames.indexOf(sectionSelect.getValue()) - 1;
			if (rupSet == null || sectionIndex < 0)
				selectedSectionID = -1;
			else
				selectedSectionID = rupSet.getFaultSectionData(sectionIndex).getSectionId();
			updateRuptures();
		} else if (event.getParameter() == minMagParam || event.getParameter() == maxMagParam) {
			updateRuptures();
		} else if (event.getParameter() == minRateParam || event.getParameter() == maxRateParam) {
			updateRuptures();
		} else if (event.getParameter() == sortParam) {
			sortType = SortType.fromString(sortParam.getValue());
			poissonSamplesParam.getEditor().setEnabled(sortType == SortType.POISSON_SAMPLING);
			updateRuptures();
		} else if (event.getParameter() == displayParam) {
			multiColorerWrapper.setAnimColorer(getColorerForDisplayParam());
		} else if (event.getParameter() == poissonSamplesParam) {
			updateRuptures();
		} else if (event.getParameter() == surfsVisibleParam) {
			updateRuptures();
		}
	}
	
	private FaultColorer getColorerForDisplayParam() {
		switch (displayParam.getValue()) {
		case RATE:
			return rateColorer;
		case RAKE:
			return rakeColorer;
		case MAG:
			return magColorer;
		case SLIP:
			return slipColorer;
		case SECTION_SLIP:
			return sectionSlipColorer;
		case STRIKE:
			return strikeColorer;
		case ORDER:
			return orderColorer;
		case PARENT:
			return parentColorer;
		default:
			return simpleFaultColorer;
		}
	}

	@Override
	public int getPreferredInitialStep() {
		return 0;
	}

	@Override
	public void actorPicked(PickEnabledActor<AbstractFaultSection> actor,
			AbstractFaultSection fault, vtkCellPicker picker, MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown()) {
			int pickedID = fault.getId();
			int nameIndex = pickedID+1;
			if (nameIndex < sectionNames.size() && nameIndex >= 0) {
				sectionSelect.setValue(sectionNames.get(nameIndex));
				sectionSelect.getEditor().refreshParamEditor();
			}
		}
	}

}
