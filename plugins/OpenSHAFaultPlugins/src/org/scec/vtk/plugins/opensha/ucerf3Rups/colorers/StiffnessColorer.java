package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.IDPairing;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.utils.GriddedSurfaceUtils;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.surfaces.params.ColorParameter;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import com.google.common.primitives.Doubles;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.kevin.simulators.multiFault.SubSectStiffnessCalculator;
import scratch.kevin.simulators.multiFault.SubSectStiffnessCalculator.StiffnessResult;
import vtk.vtkCellPicker;

public class StiffnessColorer extends CPTBasedColorer implements PickHandler<AbstractFaultSection>,
UCERF3RupSetChangeListener, ParameterChangeListener {
	
	private static CPT getDefaultCPT() {
		CPT cpt;
		try {
			cpt = GMT_CPT_Files.GMT_POLAR.instance();
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		cpt = cpt.rescale(-1d, 1d);
		cpt.setNanColor(Color.LIGHT_GRAY);
		return cpt;
	}
	
	private static CPT getFractCPT() {
		CPT cpt;
		try {
			cpt = GMT_CPT_Files.RAINBOW_UNIFORM.instance();
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		cpt = cpt.rescale(0d, 1d);
		cpt.setNanColor(Color.LIGHT_GRAY);
		return cpt;
	}
	
	private FaultSystemRupSet rupSet;
	
	private enum Quantity {
		MEAN("Mean"),
		MEDIAN("Median"),
		MIN("Min"),
		MAX("Max"),
		FRACT_POSITIVE("Fract Positive");
		
		private String name;
		private Quantity(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	private enum Type {
		SIGMA("Sigma"),
		TAU("Tau");
		
		private String name;
		private Type(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	private static final String TYPE_PARAM_NAME = "Type";
	private EnumParameter<Type> typeParam;
	
	private static final String QUANTITY_PARAM_NAME = "Plot Quantity";
	private EnumParameter<Quantity> quantityParam;
	
	private static final String GRID_SPACING_PARAM_NAME = "Grid Spacing";
	private static final double GRID_SPACING_DEFAULT = 4d;
	private static final double GRID_SPACING_MIN = 0.1d;
	private static final double GRID_SPACING_MAX = 10d;
	private DoubleParameter gridSpacingParam;
	
	private static final String PARENT_SECT_PARAM_NAME = "Parent Sections";
	private static final boolean PARENT_SECT_PARAM_DEFAULT = true;
	private BooleanParameter parentSectParam;
	
	private static final String MAX_DIST_PARAM_NAME = "Max Distance";
	private static final String MAX_DIST_PARAM_UNITS = "km";
	private static final double MAX_DIST_DEFAULT = 100d;
	private static final double MAX_DIST_MIN = 10d;
	private static final double MAX_DIST_MAX = 1000d;
	private DoubleParameter maxDistParam;
	
	private static final String LAME_PARAMS_UNITS = "MPa";
	
	private static final String LAMBDA_PARAM_NAME = "Lame Lambda";
	private static final double LAMBDA_DEFAULT = 1e4;
	private static final double LAMBDA_MIN = 1e3;
	private static final double LAMBDA_MAX = 1e5;
	private DoubleParameter lambdaParam;
	
	private static final String MU_PARAM_NAME = "Lame Mu";
	private static final double MU_DEFAULT = 1e4;
	private static final double MU_MIN = 1e3;
	private static final double MU_MAX = 1e5;
	private DoubleParameter muParam;
	
	private ParameterList params = new ParameterList();
	
	private Color highlightColor = Color.GRAY;
	private FaultSection sourceSection;
	private SubSectStiffnessCalculator calc;
	private Map<Integer, StiffnessResult[]> faultResults;
	private Map<IDPairing, Double> distances;

	public StiffnessColorer() {
		super(getDefaultCPT(), false);
		
		typeParam = new EnumParameter<Type>(TYPE_PARAM_NAME,
				EnumSet.allOf(Type.class), Type.TAU, null);
		typeParam.addParameterChangeListener(this);
		params.addParameter(typeParam);
		
		quantityParam = new EnumParameter<Quantity>(QUANTITY_PARAM_NAME,
				EnumSet.allOf(Quantity.class), Quantity.MEDIAN, null);
		quantityParam.addParameterChangeListener(this);
		params.addParameter(quantityParam);
		
		parentSectParam = new BooleanParameter(PARENT_SECT_PARAM_NAME, PARENT_SECT_PARAM_DEFAULT);
		parentSectParam.addParameterChangeListener(this);
		params.addParameter(parentSectParam);
		
		gridSpacingParam = new DoubleParameter(GRID_SPACING_PARAM_NAME, GRID_SPACING_MIN, GRID_SPACING_MAX);
		gridSpacingParam.setValue(GRID_SPACING_DEFAULT);
		gridSpacingParam.addParameterChangeListener(this);
		params.addParameter(gridSpacingParam);
		
		maxDistParam = new DoubleParameter(MAX_DIST_PARAM_NAME, MAX_DIST_MIN, MAX_DIST_MAX);
		maxDistParam.setUnits(MAX_DIST_PARAM_UNITS);
		maxDistParam.setValue(MAX_DIST_DEFAULT);
		maxDistParam.addParameterChangeListener(this);
		params.addParameter(maxDistParam);
		
		lambdaParam = new DoubleParameter(LAMBDA_PARAM_NAME, LAMBDA_MIN, LAMBDA_MAX);
		lambdaParam.setValue(LAMBDA_DEFAULT);
		lambdaParam.setUnits(LAME_PARAMS_UNITS);
		lambdaParam.addParameterChangeListener(this);
		params.addParameter(lambdaParam);
		
		muParam = new DoubleParameter(MU_PARAM_NAME, MU_MIN, MU_MAX);
		muParam.setValue(MU_DEFAULT);
		muParam.setUnits(LAME_PARAMS_UNITS);
		muParam.addParameterChangeListener(this);
		params.addParameter(muParam);
		
		faultResults = new HashMap<>();
		distances = new HashMap<>();
	}

	@Override
	public String getName() {
		return "Stiffness";
	}

	@Override
	public Color getColor(AbstractFaultSection fault) {
		if (sourceSection != null) {
			if (parentSectParam.getValue()) {
				int myParent = ((PrefDataSection)fault).getFaultSection().getParentSectionId();
				if (myParent == sourceSection.getParentSectionId())
					return highlightColor;
			} else {
				if (sourceSection.getSectionId() == fault.getId())
					return highlightColor;
			}
		}
		double value = getValue(fault);
		return super.getColorForValue(value);
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		int id = parentSectParam.getValue() ? ((PrefDataSection)fault).getFaultSection().getParentSectionId() : fault.getId();
		StiffnessResult[] results = faultResults.get(id);
		if (results == null)
			return Double.NaN;
		StiffnessResult result = typeParam.getValue() == Type.SIGMA ? results[0] : results[1];
		return getValue(result);
	}
	
	private double getValue(StiffnessResult result) {
		switch (quantityParam.getValue()) {
		case MEAN:
			return result.mean;
		case MEDIAN:
			return result.median;
		case FRACT_POSITIVE:
			return result.fractPositive;
		case MIN:
			return result.min;
		case MAX:
			return result.max;

		default:
			return Double.NaN;
		}
	}
	
	private void update() {
		faultResults.clear();
		
		if (rupSet == null || sourceSection == null)
			return;
		
		System.out.println("Computing from "+sourceSection.getName());
		
		if (calc == null)
			calc = new SubSectStiffnessCalculator(rupSet.getFaultSectionDataList(), gridSpacingParam.getValue(),
					lambdaParam.getValue(), muParam.getValue());
		
		if (parentSectParam.getValue()) {
			int id1 = sourceSection.getParentSectionId();
			Map<Integer, List<FaultSection>> parentSectsMap = new HashMap<>();
			for (FaultSection sect : rupSet.getFaultSectionDataList()) {
				List<FaultSection> parentSects = parentSectsMap.get(sect.getParentSectionId());
				if (parentSects == null) {
					parentSects = new ArrayList<>();
					parentSectsMap.put(sect.getParentSectionId(), parentSects);
				}
				parentSects.add(sect);
			}
			List<RuptureSurface> surfs1 = new ArrayList<>();
			for (FaultSection sourceSect : parentSectsMap.get(id1))
				surfs1.add(sourceSect.getFaultSurface(gridSpacingParam.getValue(), false, false));
			for (int id2 : parentSectsMap.keySet()) {
				IDPairing pair = id2 > id1 ? new IDPairing(id1, id2) : new IDPairing(id2, id1);
				Double distance = distances.get(pair);
				if (distance == null) {
					distance = Double.POSITIVE_INFINITY;
					for (FaultSection receiver : parentSectsMap.get(id2)) {
						RuptureSurface destSurf = receiver.getFaultSurface(gridSpacingParam.getValue(), false, false);
						for (RuptureSurface sourceSurf : surfs1)
							for (Location loc : sourceSurf.getPerimeter())
								distance = Math.min(distance, destSurf.getQuickDistance(loc));
					}
					distances.put(pair, distance);
				}
				if (distance <= maxDistParam.getValue())
					faultResults.put(id2, calc.calcParentStiffness(id1, id2));
			}
		} else {
			int id1 = sourceSection.getSectionId();
			RuptureSurface sourceSurf = sourceSection.getFaultSurface(gridSpacingParam.getValue(), false, false);
			for (FaultSection receiver : rupSet.getFaultSectionDataList()) {
				int id2 = receiver.getSectionId();
				IDPairing pair = id2 > id1 ? new IDPairing(id1, id2) : new IDPairing(id2, id1);
				Double distance = distances.get(pair);
				if (distance == null) {
					RuptureSurface destSurf = receiver.getFaultSurface(gridSpacingParam.getValue(), false, false);
					distance = Double.POSITIVE_INFINITY;
					for (Location loc : sourceSurf.getPerimeter())
						distance = Math.min(distance, destSurf.getQuickDistance(loc));
					distances.put(pair, distance);
				}
				if (distance <= maxDistParam.getValue())
					faultResults.put(id2, calc.calcStiffness(sourceSection, receiver));
			}
		}
		
		System.out.println("Done calculating");
		updateCPT();
	}
	
	private void updateCPT() {
		CPT cpt;
		if (quantityParam.getValue() == Quantity.FRACT_POSITIVE) {
			cpt = getFractCPT();
		} else {
			// scale to 90th percentile
			List<Double> vals = new ArrayList<>();
			for (StiffnessResult[] results : faultResults.values()) {
				StiffnessResult result = typeParam.getValue() == Type.SIGMA ? results[0] : results[1];
				double val = Math.abs(getValue(result));
				if (val > 0)
					vals.add(val);
			}
			double maxVal;
			if (vals.isEmpty())
				maxVal = 1d;
			else if (vals.size() < 5)
				maxVal = StatUtils.max(Doubles.toArray(vals));
			else
				maxVal = StatUtils.percentile(Doubles.toArray(vals), 90d);
			cpt = getDefaultCPT().rescale(-maxVal, maxVal);
			// do log-ish
//			double minNonZero = Double.POSITIVE_INFINITY;
//			double maxNonZero = 0d;
//			for (StiffnessResult[] results : faultResults.values()) {
//				StiffnessResult result = typeParam.getValue() == Type.SIGMA ? results[0] : results[1];
//				double val = Math.abs(getValue(result));
//				if (Double.isFinite(val) && val > 0d) {
//					minNonZero = Math.min(minNonZero, val);
//					maxNonZero = Math.max(maxNonZero, val);
//				}
//			}
//			if (!Double.isFinite(minNonZero)) {
//				cpt = getDefaultCPT();
//			} else {
//				if (minNonZero == maxNonZero)
//					maxNonZero += 1d;
//				cpt = buildSemiLogCPT(minNonZero, maxNonZero);
//			}
		}
		cpt.setNanColor(Color.LIGHT_GRAY);
		setCPT(cpt, false);
		fireColorerChangeEvent();
	}
	
	private static CPT buildSemiLogCPT(double absMin, double absMax) {
		double logMin = Math.log10(absMin);
		double logMax = Math.log10(absMax);
		System.out.println("LogMin: "+logMin);
		System.out.println("LogMax: "+logMax);
		CPT logPositive = new CPT(logMin, logMax, Color.WHITE, Color.RED.brighter(), Color.RED, Color.RED.darker());
		CPT logNegative = new CPT(-logMax, -logMin, Color.BLUE.darker(), Color.BLUE, Color.BLUE.brighter(), Color.WHITE);
		
		EvenlyDiscretizedFunc logDiscr = new EvenlyDiscretizedFunc(logMin, logMax, 20);
		CPT cpt = new CPT();
		// add negative
		for (int i=logDiscr.size(); --i>=0;) {
			double logVal1 = logDiscr.getX(i);
			Color color1 = logNegative.getColor((float)logVal1);
			float val1 = (float)-Math.pow(10, logVal1);
			
			float val2;
			Color color2;
			if (i == 0) {
				val2 = 0f;
				color2 = Color.WHITE;
			} else {
				double logVal2 = logDiscr.getX(i-1);
				color2 = logNegative.getColor((float)-logVal2);
				val2 = (float)-Math.pow(10, logVal2);
			}
			
			cpt.add(new CPTVal(val1, color1, val2, color2));
		}
		// add positive
		for (int i=0; i<logDiscr.size(); i++) {
			float val1;
			Color color1;
			if (i == 0) {
				val1 = 0f;
				color1 = Color.WHITE;
			} else {
				double logVal1 = logDiscr.getX(i-1);
				color1 = logPositive.getColor((float)logVal1);
				val1 = (float)Math.pow(10, logVal1);
			}

			double logVal2 = logDiscr.getX(i);
			Color color2 = logPositive.getColor((float)logVal2);
			float val2 = (float)Math.pow(10, logVal2);
			
			cpt.add(new CPTVal(val1, color1, val2, color2));
		}
		return cpt;
	}

	@Override
	public void actorPicked(PickEnabledActor<AbstractFaultSection> actor,
			AbstractFaultSection reference, vtkCellPicker picker, MouseEvent e) {
		
		if (reference instanceof PrefDataSection) {
			
			PrefDataSection fault = (PrefDataSection)reference;
			if (e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown()) {
				sourceSection = fault.getFaultSection();
				System.out.println("shift down, picked "+sourceSection.getName());
				update();
			}
		}
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.rupSet = null;
		this.distances.clear();
		update();
		this.rupSet = rupSet;
		update();
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		Parameter<?> param = event.getParameter();
		if (param == quantityParam || param == typeParam) {
			updateCPT();
		} else if (param == gridSpacingParam || param == lambdaParam || param == muParam || param == parentSectParam) {
			if (calc != null)
				calc = null;
			if (param == gridSpacingParam || param == parentSectParam)
				distances.clear();
			update();
		}
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

}
