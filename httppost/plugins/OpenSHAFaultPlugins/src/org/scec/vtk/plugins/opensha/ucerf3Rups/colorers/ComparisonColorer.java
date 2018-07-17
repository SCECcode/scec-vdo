package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.editor.impl.FileParameterEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3FaultSystemRupturesBuilder;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.inversion.InversionInputGenerator;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoProbabilityModel;

public class ComparisonColorer extends CPTBasedColorer implements UCERF3RupSetChangeListener, ParameterChangeListener {

	private InversionFaultSystemSolution sol;
	
	private static final String COMPARISON_FILE_PARAM_NAME = "Load Solution Comparison File";
	private FileParameter comparisonFileParam;
	private InversionFaultSystemSolution comparisonSol;
	
	public enum CompareType {
		SOLUTION_SLIP_MISFIT("Sol. Slip vs Def Model Slip", 3, 0.005, false),
		SOLUTION_RATES_VS_PALEO_VISIBLE("Paleo Visible vs. Sol. Part. Rates", 3, 1e-3, false),
		SOLUTION_PARTICIPATION_RATES_COMPARISON("Sol. Part. Rates vs Comparison Sol.", 3, 1e-3, true),
		SOLUTION_SLIP_RATES_COMPARISON("Sol. Slip Rates vs Comparison Sol.", 3, 1e-3, true);
		
		private String name;
		private double ratioMin, ratioMax;
		private double diffMin, diffMax;
		private double absDiffMin, absDiffMax;
		private double fractDiffMin, fractDiffMax;
		private boolean external;
		
		private CompareType(String name, double ratioMult, double diffDelta, boolean external) {
			this.name = name;
			this.ratioMin = 1d / ratioMult;
			this.ratioMax = 1d * ratioMult;
			this.diffMin = -diffDelta;
			this.diffMax = diffDelta;
			this.absDiffMin = 0;
			this.absDiffMax = diffDelta;
			this.fractDiffMin = -1;
			this.fractDiffMax = 1;
			this.external = external;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public static CompareType forString(String str) {
			for (CompareType type : values()) {
				if (type.toString().equals(str))
					return type;
			}
			throw new NoSuchElementException("Unknown CompareType: "+str);
		}
	}
	
	public enum ComparePlotType {
		RATIOS("Ratio"),
		DIFFERENCE("Difference"),
		ABSOLUTE_DIFFERENCE("Abs. Difference"),
		FRACTIONAL_DIFFERENCE("Fractional Difference");
		
		private String name;
		
		private ComparePlotType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public static ComparePlotType forString(String str) {
			for (ComparePlotType type : values()) {
				if (type.toString().equals(str))
					return type;
			}
			throw new NoSuchElementException("Unknown CompareType: "+str);
		}
	}
	
	private ColorerChangeListener l;
	
	private CPT polar;
	
	private static final String COMPARE_TYPE_PARAM_NAME = "Comparison Type";
	private StringParameter compareTypeParam;
	private CompareType comp;
	
	private static final String COMPARE_PLOT_TYPE_PARAM_NAME = "Plot";
	private StringParameter plotTypeParam;
	private ComparePlotType plot;
	
	private DoubleParameter magMinParam;
	private double min_patic_mag = 0d;
	private double max_patic_mag = 10d;
	private DoubleParameter magMaxParam;
	
	private ParameterList params;
	
	private HashMap<Integer, double[]> valCache;
	
	private UCERF3FaultSystemRupturesBuilder builder;
	
	public ComparisonColorer(UCERF3FaultSystemRupturesBuilder builder) {
		super(null, false);
		
		this.builder = builder;
		
		try {
			polar = GMT_CPT_Files.GMT_POLAR.instance();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		comparisonFileParam = new FileParameter(COMPARISON_FILE_PARAM_NAME);
		comparisonFileParam.addParameterChangeListener(this);
		comparisonFileParam.getEditor().setEnabled(false);
		
		params = new ParameterList();
		
		ArrayList<String> compStrings = new ArrayList<String>();
		for (CompareType t : CompareType.values()) {
			if (!t.external)
				compStrings.add(t.toString());
		}
		compareTypeParam = new StringParameter(COMPARE_TYPE_PARAM_NAME, compStrings, compStrings.get(0));
		compareTypeParam.addParameterChangeListener(this);
		comp = CompareType.forString(compareTypeParam.getValue());

		ArrayList<String> plotStrings = new ArrayList<String>();
		for (ComparePlotType t : ComparePlotType.values()) {
			plotStrings.add(t.toString());
		}
		plotTypeParam = new StringParameter(COMPARE_PLOT_TYPE_PARAM_NAME, plotStrings, plotStrings.get(0));
		plotTypeParam.addParameterChangeListener(this);
		plot = ComparePlotType.forString(plotTypeParam.getValue());
		
		params.addParameter(compareTypeParam);
		params.addParameter(plotTypeParam);
		params.addParameter(comparisonFileParam);
		
		magMinParam = new DoubleParameter("Min Mag (Part. Rates)", min_patic_mag, max_patic_mag);
		magMinParam.setValue(min_patic_mag);
		magMinParam.addParameterChangeListener(this);
		params.addParameter(magMinParam);
		magMaxParam = new DoubleParameter("Max Mag (Part. Rates)", min_patic_mag, max_patic_mag);
		magMaxParam.setValue(max_patic_mag);
		magMaxParam.addParameterChangeListener(this);
		params.addParameter(magMaxParam);
		enableMinMax();
		
		valCache = new HashMap<Integer, double[]>();
		
		setCPT(null);
	}
	
	private void enableMinMax() {
		boolean enabled = comp == CompareType.SOLUTION_PARTICIPATION_RATES_COMPARISON;
		magMinParam.getEditor().setEnabled(enabled);
		magMinParam.getEditor().refreshParamEditor();
		magMaxParam.getEditor().setEnabled(enabled);
		magMaxParam.getEditor().refreshParamEditor();
	}
	
	private void updateCompTypeStrings() {
		boolean allowExternal = comparisonSol != null;
		ArrayList<String> compStrings = new ArrayList<String>();
		for (CompareType t : CompareType.values()) {
			if (!t.external || allowExternal)
				compStrings.add(t.toString());
		}
		if (comp.external && !allowExternal) {
			for (CompareType t : CompareType.values()) {
				if (!t.external) {
					compareTypeParam.setValue(t.toString());
					break;
				}
			}
		}
		StringConstraint sconst = (StringConstraint)compareTypeParam.getConstraint();
		sconst.setStrings(compStrings);
		compareTypeParam.getEditor().refreshParamEditor();
	}

	@Override
	public String getName() {
		return "Solution Comparisons";
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		if (sol != null && sol instanceof InversionFaultSystemSolution)
			this.sol = (InversionFaultSystemSolution)sol;
		else
			this.sol = null;
		
		if (sol != null && comparisonSol != null) {
			if (comparisonSol.getRupSet().getNumRuptures() != sol.getRupSet().getNumRuptures()) {
				comparisonFileParam.setValue(null);
			}
		}
		comparisonFileParam.getEditor().setEnabled(rupSet != null);
		valCache.clear();
	}
	
	public void setComparisonSolution(InversionFaultSystemSolution comparisonSol) {
		System.out.println("Got new comparison!");
		valCache.clear();
		this.comparisonSol = comparisonSol;
		updateCompTypeStrings();
	}
	
	@Override
	public double getValue(AbstractFaultSection fault) {
		int secID = fault.getId();
		if (sol == null)
			return Double.NaN;
		double val1;
		double val2;
		if (valCache.containsKey(secID)) {
			double[] cached = valCache.get(secID);
			val1 = cached[0];
			val2 = cached[1];
		} else {
			switch (comp) {
			case SOLUTION_SLIP_MISFIT:
				val1 = sol.calcSlipRateForSect(secID);
				val2 = sol.getRupSet().getSlipRateForSection(secID);
				break;
			case SOLUTION_RATES_VS_PALEO_VISIBLE:
				try {
					val1 = sol.calcTotPaleoVisibleRateForSect(secID, InversionInputGenerator.loadDefaultPaleoProbabilityModel());
				} catch (IOException e) {
					throw ExceptionUtils.asRuntimeException(e);
				}
				val2 = sol.calcTotParticRateForSect(secID);
				break;
			case SOLUTION_PARTICIPATION_RATES_COMPARISON:
				val1 = sol.calcParticRateForSect(secID, min_patic_mag, max_patic_mag);
				val2 = comparisonSol.calcParticRateForSect(secID, min_patic_mag, max_patic_mag);
				break;
			case SOLUTION_SLIP_RATES_COMPARISON:
				val1 = sol.calcSlipRateForSect(secID);
				val2 = comparisonSol.calcSlipRateForSect(secID);
				break;
			default:
				return Double.NaN;
			}
			double[] cached = {val1, val2};
			valCache.put(secID, cached);
		}
		double comp = getComparison(val1, val2);
		if (Double.isInfinite(comp))
			return Double.NaN;
		return comp;
	}
	
	private double getComparison(double val1, double val2) {
		return getComparison(plot, val1, val2);
	}
		
	public static double getComparison(ComparePlotType plot, double val1, double val2) {
		switch (plot) {
		case RATIOS:
			if (val1 == 0 && val2 == 0)
				return 1;
			return val1 / val2;
		case DIFFERENCE:
			return val1 - val2;
		case ABSOLUTE_DIFFERENCE:
			return Math.abs(val1 - val2);
		case FRACTIONAL_DIFFERENCE:
			return (val1 - val2) / val2;
		default:
			throw new IllegalStateException("Unknown plot type: "+plot);
		}
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}
	
	@Override
	public void setCPT(CPT origCPT) {
		if (polar == null) {
			// this is from the constructor
			super.setCPT(null);
			return;
		}
		if (origCPT != null) {
			// this means the user rescaled it!
			double min = origCPT.getMinValue();
			double max = origCPT.getMaxValue();
			switch (plot) {
			case RATIOS:
				if (min < 1)
					comp.ratioMin = min;
				if (max > 1)
					comp.ratioMax = max;
				break;
			case DIFFERENCE:
				if (min < 0)
					comp.diffMin = min;
				if (max > 0)
					comp.diffMax = max;
				break;
			case ABSOLUTE_DIFFERENCE:
				if (max > 0) {
					comp.absDiffMax = max;
					if (min < 0)
						min = 0;
					if (min < max)
						comp.absDiffMin = min;
				}
				break;
			case FRACTIONAL_DIFFERENCE:
				if (min < 0)
					comp.fractDiffMin = min;
				if (max > 0)
					comp.fractDiffMax = max;
				break;
			default:
				throw new IllegalStateException("Unknown plot type: "+plot);
			}
		}
		CPT cpt = (CPT) polar.clone();
		switch (plot) {
		case RATIOS:
			cpt.get(0).start = (float)comp.ratioMin;
			cpt.get(0).end = 1f;
			cpt.get(1).start = 1f;
			cpt.get(1).end = (float)comp.ratioMax;
			break;
		case DIFFERENCE:
			cpt.get(0).start = (float)comp.diffMin;
			cpt.get(1).end = (float)comp.diffMax;
			break;
		case ABSOLUTE_DIFFERENCE:
			cpt.remove(0);
			cpt.get(0).start = (float)comp.absDiffMin;
			cpt.get(0).end = (float)comp.absDiffMax;
			break;
		case FRACTIONAL_DIFFERENCE:
			cpt.get(0).start = (float)comp.fractDiffMin;
			cpt.get(0).end = 0f;
			cpt.get(1).start = 0f;
			cpt.get(1).end = (float)comp.fractDiffMax;
			break;
		default:
			throw new IllegalStateException("Unknown plot type: "+plot);
		}
		
//		System.out.println("CPT for: "+plot+": "+comp);
//		System.out.println(cpt);
		setCPTLog(false);
		super.setCPT(cpt);
		
		fireColorerChangeEvent();
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getSource() == compareTypeParam) {
			comp = CompareType.forString(compareTypeParam.getValue());
			enableMinMax();
			valCache.clear();
		} else if (event.getSource() == plotTypeParam) {
			plot = ComparePlotType.forString(plotTypeParam.getValue());
		} else if (event.getSource() == comparisonFileParam) {
			setComparisonSolution(null);
			System.gc();
			InversionFaultSystemSolution comp;
			try {
				File file = comparisonFileParam.getValue();
				if (file != null) {
					FaultSystemSolution newSol = FaultSystemIO.loadSol(file);
					Preconditions.checkState(newSol instanceof InversionFaultSystemSolution,
							"Comparison isn't an InversionFaultSystemSolution");
					comp = (InversionFaultSystemSolution)newSol;
					Preconditions.checkState(sol.getRupSet().getNumRuptures() == comp.getRupSet().getNumRuptures(),
							"Comparison has a different number of ruptures!");
					builder.setDefaultDir(file.getParentFile());
				} else {
					comp = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				UCERF3FaultSystemRupturesBuilder.showErrorMessage(e);
				comparisonFileParam.setValue(null);
				return;
			}
			setComparisonSolution(comp);
		} else if (event.getSource() == magMinParam) {
			min_patic_mag = magMinParam.getValue();
			valCache.clear();
		} else if (event.getSource() == magMaxParam) {
			max_patic_mag = magMaxParam.getValue();
			valCache.clear();
		}
		setCPT(null);
	}
	
	public FaultSystemSolution getCompFaultSystemSolution() {
		return comparisonSol;
	}
	
	public void setLoadDir(File defaultLoadDir) {
		((FileParameterEditor)comparisonFileParam.getEditor()).setDefaultDir(defaultLoadDir);
	}

}
