package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.GraphWidget;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.IDPairing;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.ClusterRupture;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.FaultSubsectionCluster;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.Jump;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.RuptureConnectionSearch;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.RuptureTreeNavigator;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.SectionDistanceAzimuthCalculator;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.simulators.stiffness.RuptureCoulombResult;
import org.opensha.sha.simulators.stiffness.RuptureCoulombResult.RupCoulombQuantity;
import org.opensha.sha.simulators.stiffness.StiffnessCalc.Patch;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator.LogDistributionPlot;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator.StiffnessAggregationMethod;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator.StiffnessDistribution;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator.StiffnessResult;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator.StiffnessType;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator.PointArray;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator.PointOrganizer;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkCellPicker;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnsignedCharArray;

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
	
	private static final String TYPE_PARAM_NAME = "Type";
	private EnumParameter<StiffnessType> typeParam;
	
	private static final String QUANTITY_PARAM_NAME = "Plot Quantity";
	private EnumParameter<StiffnessAggregationMethod> quantityParam;
	
	private static final String GRID_SPACING_PARAM_NAME = "Grid Spacing";
	private static final double GRID_SPACING_DEFAULT = 2d;
	private static final double GRID_SPACING_MIN = 0.1d;
	private static final double GRID_SPACING_MAX = 10d;
	private DoubleParameter gridSpacingParam;
	
	private static final String PARENT_SECT_PARAM_NAME = "Parent Sections";
	private static final boolean PARENT_SECT_PARAM_DEFAULT = false;
	private BooleanParameter parentSectParam;
	
	private static final String MAX_DIST_PARAM_NAME = "Max Distance";
	private static final String MAX_DIST_PARAM_UNITS = "km";
	private static final double MAX_DIST_DEFAULT = 100d;
	private static final double MAX_DIST_MIN = 10d;
	private static final double MAX_DIST_MAX = 1000d;
	private DoubleParameter maxDistParam;
	
	private static final String LAME_PARAMS_UNITS = "MPa";
	
	private static final String LAMBDA_PARAM_NAME = "Lame Lambda";
	private static final double LAMBDA_DEFAULT = 3e4;
	private static final double LAMBDA_MIN = 1e3;
	private static final double LAMBDA_MAX = 1e5;
	private DoubleParameter lambdaParam;
	
	private static final String MU_PARAM_NAME = "Lame Mu";
	private static final double MU_DEFAULT = 3e4;
	private static final double MU_MIN = 1e3;
	private static final double MU_MAX = 1e5;
	private DoubleParameter muParam;
	
	private static final String COEF_OF_FRICTION_PARAM_NAME = "Coef. Of Friction";
	private static final double COEF_OF_FRICTION_DEFAULT = 0.5;
	private static final double COEF_OF_FRICTION_MIN = 0d;
	private static final double COEF_OF_FRICTION_MAX = 1d;
	private DoubleParameter coefOfFrictionParam;
	
	private static final String RUP_INDEX_PARAM_NAME = "Rupture Index";
	private IntegerParameter rupIndexParam;
	
	private static final String RUP_QUANTITY_PARAM_NAME = "Rupture Quantity";
	private EnumParameter<RupQuantity> rupQuantityParam;
	
	private static final String REUSE_RECEIVER_WINDOW_PARAM_NAME = "Reuse Receiver Window";
	private static final boolean REUSE_RECEIVER_WINDOW_PARAM_DEFAULT = true;
	private BooleanParameter reuseRecieverWindowParam;
	private GraphWindow receiverGW;
	
	private static final String SHOW_PATCHES_NAME = "Show Src/Receiver Patches";
	private static final boolean SHOW_PATCHES_PARAM_DEFAULT = false;
	private BooleanParameter showPatchesParam;
	
	private static final String SHOW_LINES_NAME = "Show Src/Receiver Lines";
	private static final boolean SHOW_LINES_PARAM_DEFAULT = false;
	private BooleanParameter showLinesParam;
	
	private ClusterRupture curRupture;
	private FaultSubsectionCluster startCluster;
	private RuptureConnectionSearch search;
	
	private enum RupQuantity {
		SECTION_NET("Section Net"),
		CLUSTER_NET("Cluster Net"),
		CLUSTER_PATH("Cluster Path"),
		RUPTURE_NET("Rupture Net");
		
		private String name;
		private RupQuantity(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private ParameterList params = new ParameterList();
	
	private Color highlightColor = Color.GREEN.darker().darker();
	private FaultSection sourceSection;
	private SubSectStiffnessCalculator calc;
	private ExecutorService exec;
	private Map<Integer, StiffnessResult> faultResults;
	private Map<Integer, Double> rupResults;
	private Map<IDPairing, Double> distances;
	
	private FaultSection receiverSection;

	private PluginActors pluginActors;

	public StiffnessColorer(PluginActors pluginActors) {
		super(getDefaultCPT(), false);
		this.pluginActors = pluginActors;
		
		typeParam = new EnumParameter<StiffnessType>(TYPE_PARAM_NAME,
				EnumSet.allOf(StiffnessType.class), StiffnessType.CFF, null);
		typeParam.addParameterChangeListener(this);
		params.addParameter(typeParam);
		
		quantityParam = new EnumParameter<StiffnessAggregationMethod>(QUANTITY_PARAM_NAME,
				EnumSet.allOf(StiffnessAggregationMethod.class), StiffnessAggregationMethod.MEDIAN, null);
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
		
		coefOfFrictionParam = new DoubleParameter(
				COEF_OF_FRICTION_PARAM_NAME, COEF_OF_FRICTION_MIN, COEF_OF_FRICTION_MAX);
		coefOfFrictionParam.setValue(COEF_OF_FRICTION_DEFAULT);
		coefOfFrictionParam.addParameterChangeListener(this);
		params.addParameter(coefOfFrictionParam);
		
		rupIndexParam = new IntegerParameter(RUP_INDEX_PARAM_NAME, -1);
		rupIndexParam.addParameterChangeListener(this);
		params.addParameter(rupIndexParam);
		
		rupQuantityParam = new EnumParameter<RupQuantity>(RUP_QUANTITY_PARAM_NAME, EnumSet.allOf(RupQuantity.class),
				RupQuantity.SECTION_NET, null);
		rupQuantityParam.getEditor().setEnabled(false);
		rupQuantityParam.addParameterChangeListener(this);
		params.addParameter(rupQuantityParam);
		
		reuseRecieverWindowParam = new BooleanParameter(
				REUSE_RECEIVER_WINDOW_PARAM_NAME, REUSE_RECEIVER_WINDOW_PARAM_DEFAULT);
		reuseRecieverWindowParam.setInfo(
				"You can ctrl+click on a fault to set a receiver, which will pop up a plot "
				+ "of the value distribution. If this is selected, the graph window will be "
				+ "reused for each plot (otherwise a new window will pop up).");
		params.addParameter(reuseRecieverWindowParam);
		
		showPatchesParam = new BooleanParameter(SHOW_PATCHES_NAME, SHOW_PATCHES_PARAM_DEFAULT);
		showPatchesParam.addParameterChangeListener(this);
		params.addParameter(showPatchesParam);
		
		showLinesParam = new BooleanParameter(SHOW_LINES_NAME, SHOW_LINES_PARAM_DEFAULT);
		showLinesParam.addParameterChangeListener(this);
		params.addParameter(showLinesParam);
		
		faultResults = new HashMap<>();
		distances = new HashMap<>();
		rupResults = new HashMap<>();
	}

	@Override
	public String getName() {
		return "Stiffness";
	}

	@Override
	public Color getColor(AbstractFaultSection fault) {
		if (curRupture != null) {
			if (rupQuantityParam.getValue() == RupQuantity.CLUSTER_PATH) {
				FaultSection sect = ((PrefDataSection)fault).getFaultSection();
				if (startCluster == null) {
					// user needs to select a starting point
					if (curRupture.contains(sect))
						return new Color(150, 255, 150);
					return getCPT().getNaNColor();
				} else if (startCluster.contains(sect)) {
					return highlightColor;
				}
			}
		} else if (sourceSection != null) {
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
		if (curRupture == null) {
			int id = parentSectParam.getValue() ? ((PrefDataSection)fault).getFaultSection().getParentSectionId() : fault.getId();
			StiffnessResult results = faultResults.get(id);
			if (results == null)
				return Double.NaN;
			return SubSectStiffnessCalculator.getValue(results, quantityParam.getValue());
		}
		// we're in rupture mode
		Double value = rupResults.get(fault.getId());
		if (value == null)
			return Double.NaN;
		return value;
	}
	
	private void update() {
		faultResults.clear();
		rupResults.clear();
		curRupture = null;
		startCluster = null;
		
		if (rupSet == null)
			return;
		
		if (calc == null)
			calc = new SubSectStiffnessCalculator(rupSet.getFaultSectionDataList(), gridSpacingParam.getValue(),
					lambdaParam.getValue(), muParam.getValue(), coefOfFrictionParam.getValue());
		
		if (rupIndexParam.getValue() < 0) {
			if (sourceSection == null)
				return;
			
			System.out.println("Computing from "+sourceSection.getName());
			
			if (exec == null)
				exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			
			StiffnessType type = typeParam.getValue();
			
			List<Future<?>> futures = new ArrayList<>();
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
				for (int id2 : parentSectsMap.keySet())
					futures.add(exec.submit(new ParentCalcRun(type, id1, id2, parentSectsMap, surfs1)));
			} else {
				RuptureSurface sourceSurf = sourceSection.getFaultSurface(gridSpacingParam.getValue(), false, false);
				for (FaultSection receiver : rupSet.getFaultSectionDataList())
					futures.add(exec.submit(new SectCalcRun(type, sourceSection, sourceSurf, receiver)));
			}
			
			System.out.println("Waiting on "+futures.size()+" futures...");
			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (Exception e) {
					throw ExceptionUtils.asRuntimeException(e);
				}
			}
		} else {
			int rupIndex = rupIndexParam.getValue();
			RupQuantity quantity = rupQuantityParam.getValue();
			if (rupIndex < rupSet.getNumRuptures()) {
				if (search == null)
					search = new RuptureConnectionSearch(rupSet, new SectionDistanceAzimuthCalculator(
							rupSet.getFaultSectionDataList()), 100d, false);
				startCluster = null;
				curRupture = search.buildClusterRupture(rupIndex);
				if (sourceSection != null) {
					// see if that applies
					for (FaultSubsectionCluster cluster : curRupture.getClustersIterable()) {
						if (cluster.contains(sourceSection)) {
							startCluster = cluster;
							break;
						}
					}
					if (startCluster == null) {
						// selection doesn't involve that cluster, reset it
						sourceSection = null;
					}
				}
				if (quantity == RupQuantity.CLUSTER_PATH && startCluster == null) {
					fireColorerChangeEvent();
					return;
				}
				switch (quantity) {
				case RUPTURE_NET:
					// calculate net, assign to everything
					RuptureCoulombResult rupResult = new RuptureCoulombResult(curRupture, calc, quantityParam.getValue());
					double rupVal = rupResult.getValue(RupCoulombQuantity.SUM_SECT_CFF);
					for (FaultSubsectionCluster cluster : curRupture.getClustersIterable())
						for (FaultSection sect : cluster.subSects)
							rupResults.put(sect.getSectionId(), rupVal);
					break;
				case CLUSTER_NET:
					for (FaultSubsectionCluster cluster : curRupture.getClustersIterable()) {
						List<FaultSubsectionCluster> sources = new ArrayList<>();
						for (FaultSubsectionCluster source : curRupture.getClustersIterable())
							if (cluster != source)
								sources.add(source);
						StiffnessResult clusterResult = calc.calcAggClustersToClusterStiffness(StiffnessType.CFF, sources, cluster);
						double clusterVal = SubSectStiffnessCalculator.getValue(clusterResult, quantityParam.getValue());
						for (FaultSection sect : cluster.subSects)
							rupResults.put(sect.getSectionId(), clusterVal);
					}
					break;
				case SECTION_NET:
					List<FaultSection> allSects = rupSet.getFaultSectionDataForRupture(rupIndex);
					for (FaultSubsectionCluster cluster : curRupture.getClustersIterable()) {
						for (FaultSection sect : cluster.subSects) {
							List<FaultSection> receivers = new ArrayList<>();
							receivers.add(sect);
							StiffnessResult sectResult = calc.calcAggStiffness(StiffnessType.CFF, allSects, receivers, -1, -1);
							double sectVal = SubSectStiffnessCalculator.getValue(sectResult, quantityParam.getValue());
							rupResults.put(sect.getSectionId(), sectVal);
						}
					}
					break;
				case CLUSTER_PATH:
					Preconditions.checkNotNull(startCluster, "Start cluster is null?");
					calcClusterPath(curRupture.getTreeNavigator(), startCluster, new HashSet<>());
					break;

				default:
					break;
				}
			}
		}
		
		System.out.println("Done calculating");
		updateCPT();
	}
	
	private synchronized void updateReceiver() {
		if (sourceSection == null || receiverSection == null || calc == null)
			return;
		StiffnessDistribution dist = calc.calcStiffnessDistribution(
				sourceSection, receiverSection);
		if (!reuseRecieverWindowParam.getValue() || receiverGW == null || !receiverGW.isVisible())
			receiverGW = new GraphWindow(new GraphWidget());
		LogDistributionPlot plot = calc.plotDistributionHistograms(dist, typeParam.getValue());
		plot.plotInGW(receiverGW);
		if (showPatchesParam.getValue()) {
			List<Patch> sourcePatches = calc.getPatches(receiverSection);
		}
	}
	
	private void showPatches(List<Patch> patches, StiffnessDistribution dist) {
		StiffnessResult fullResult = null;
		double[][] fullVals = null;
		if (dist != null) {
			fullResult = dist.results[typeParam.getValue().getArrayIndex()];
			switch (typeParam.getValue()) {
			case CFF:
				fullVals = dist.cffVals;
				break;
			case SIGMA:
				fullVals = dist.sigmaVals;
				break;
			case TAU:
				fullVals = dist.tauVals;
				break;

			default:
				throw new IllegalStateException();
			}
		}
		
		vtkPolyData polyData = new vtkPolyData();
		vtkPoints pts = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
		vtkActor actor = new vtkActor();
		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
		colors.SetNumberOfComponents(4);
		colors.SetName("Colors");
		
		for (int i=0; i<patches.size(); i++) {
			Patch patch = patches.get(i);
			Color c;
			if (fullResult == null) {
				c = highlightColor;
			} else {
				// it's a receiver, color according to the CPT
				StiffnessAggregationMethod aggMethod = quantityParam.getValue();
				double[][] subVals = new double[fullVals.length][1];
				for (int j=0; j<subVals.length; j++)
					subVals[j][0] = fullVals[j][i];
				StiffnessResult patchResult = new StiffnessResult(fullResult.sourceID, fullResult.receiverID,
						subVals, typeParam.getValue());
				double val = patchResult.getValue(aggMethod);
				c = getCPT().getColor((float)val);
			}
			PointOrganizer organizer = new PointOrganizer(pts, colors, c);
			List<PointArray> polygons = new ArrayList<>();
			
			double[][] points = new double[4][];
			
//			=
			
			polygons.add(new PointArray(points));
		}
	}
	
	private void calcClusterPath(RuptureTreeNavigator navigator, FaultSubsectionCluster receiver,
			HashSet<FaultSubsectionCluster> parents) {
		if (!parents.isEmpty()) {
			StiffnessResult clusterResult = calc.calcAggClustersToClusterStiffness(StiffnessType.CFF, parents, receiver);
			double clusterVal = SubSectStiffnessCalculator.getValue(clusterResult, quantityParam.getValue());
			for (FaultSection sect : receiver.subSects)
				rupResults.put(sect.getSectionId(), clusterVal);
		}
		HashSet<FaultSubsectionCluster> newParents = new HashSet<>(parents);
		newParents.add(receiver);
		for (FaultSubsectionCluster descendant : navigator.getDescendants(receiver)) {
			if (!parents.contains(descendant))
				calcClusterPath(navigator, descendant, newParents);
		}
		// look the other way
		FaultSubsectionCluster predecessor = navigator.getPredecessor(receiver);
		if (predecessor != null && !parents.contains(predecessor))
			calcClusterPath(navigator, predecessor, newParents);
	}
	
	private class ParentCalcRun implements Runnable {
		
		private int id1;
		private int id2;
		private Map<Integer, List<FaultSection>> parentSectsMap;
		private List<RuptureSurface> surfs1;
		private StiffnessType type;

		public ParentCalcRun(StiffnessType type, int id1, int id2, Map<Integer, List<FaultSection>> parentSectsMap,
				List<RuptureSurface> surfs1) {
			this.type = type;
			this.id1 = id1;
			this.id2 = id2;
			this.parentSectsMap = parentSectsMap;
			this.surfs1 = surfs1;
		}

		@Override
		public void run() {
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
				faultResults.put(id2, calc.calcParentStiffness(type, id1, id2));
		}
		
	}
	
	private class SectCalcRun implements Runnable {

		private StiffnessType type;
		private FaultSection source;
		private RuptureSurface sourceSurf;
		private FaultSection receiver;

		public SectCalcRun(StiffnessType type, FaultSection source, RuptureSurface sourceSurf, FaultSection receiver) {
			this.type = type;
			this.source = source;
			this.sourceSurf = sourceSurf;
			this.receiver = receiver;
		}

		@Override
		public void run() {
			int id1 = source.getSectionId();
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
				faultResults.put(id2, calc.calcStiffness(type, sourceSection, receiver));
		}
		
	}
	
	private void updateCPT() {
		CPT cpt;
		if (quantityParam.getValue() == StiffnessAggregationMethod.FRACT_POSITIVE) {
			cpt = getFractCPT();
		} else {
			StiffnessAggregationMethod quantity = quantityParam.getValue();
			double maxVal = 0.1;
			double minNonZero = 0.01;
			for (StiffnessResult results : faultResults.values()) {
				double val = Math.abs(SubSectStiffnessCalculator.getValue(results, quantity));
				maxVal = Math.max(maxVal, val);
				if (val > 0)
					minNonZero = Math.min(minNonZero, val);
			}
//			if (vals.isEmpty())
//				maxVal = 1d;
//			else if (vals.size() < 5)
//				maxVal = StatUtils.max(Doubles.toArray(vals));
//			else
//				maxVal = StatUtils.percentile(Doubles.toArray(vals), 95d);
			maxVal = Math.max(maxVal, 0.01);
//			cpt = getDefaultCPT().rescale(-maxVal, maxVal);
			
//			double minColorVal = 0.0001;
//			CPT upperCPT = new CPT(minColorVal, maxVal,
//					new Color(255, 180, 180), new Color(255, 0, 0), new Color(100, 0, 0));
//			CPT lowerCPT = new CPT(-maxVal, -minColorVal,
//					new Color(0, 0, 100), new Color(0, 0, 255), new Color(180, 180, 255));
//			cpt = new CPT();
//			cpt.addAll(lowerCPT);
//			cpt.add(new CPTVal(lowerCPT.getMaxValue(), lowerCPT.getMaxColor(), 0f, Color.WHITE));
//			cpt.add(new CPTVal(0f, Color.WHITE, upperCPT.getMinValue(), upperCPT.getMinColor()));
//			cpt.addAll(upperCPT);
//			cpt.setBelowMinColor(cpt.getMinColor());
//			cpt.setAboveMaxColor(cpt.getMaxColor());
			
			// another log-ish test
			double logMinVal = Math.max(-6, Math.floor(Math.log10(minNonZero)));
			double logMaxVal = Math.ceil(Math.log10(maxVal));
			if (logMaxVal < -4)
				logMinVal = logMaxVal - 3;
			CPT logUpperCPT = new CPT(logMinVal, logMaxVal-1, new Color(255, 220, 220),
					new Color(255, 0, 0));
			logUpperCPT.add(new CPTVal(logUpperCPT.getMaxValue(), logUpperCPT.getMaxColor(),
					(float)logMaxVal, new Color(100, 0, 0)));
			EvenlyDiscretizedFunc logDiscr = new EvenlyDiscretizedFunc(logMinVal, logMaxVal, 100);
			CPT logLowerCPT = new CPT(logMinVal, logMaxVal-1, new Color(220, 220, 255),
					new Color(0, 0, 255));
			logLowerCPT.add(new CPTVal(logLowerCPT.getMaxValue(), logLowerCPT.getMaxColor(),
					(float)logMaxVal, new Color(0, 0, 100)));
			cpt = new CPT();
			for (int i=logDiscr.size(); --i>0;) {
				double x1 = logDiscr.getX(i); // abs larger value, neg smaller
				double x2 = logDiscr.getX(i-1); // abs smaller value, neg larger
				Color c1 = logLowerCPT.getColor((float)x1);
				Color c2 = logLowerCPT.getColor((float)x2);
				cpt.add(new CPTVal(-(float)Math.pow(10, x1), c1, -(float)Math.pow(10, x2), c2));
			}
			cpt.add(new CPTVal(cpt.getMaxValue(), cpt.getMaxColor(), 0f, Color.WHITE));
			cpt.add(new CPTVal(0f, Color.WHITE, (float)Math.pow(10, logUpperCPT.getMinValue()),
					logUpperCPT.getMinColor()));
			for (int i=0; i<logDiscr.size()-1; i++) {
				double x1 = logDiscr.getX(i);
				double x2 = logDiscr.getX(i+1);
				Color c1 = logUpperCPT.getColor((float)x1);
				Color c2 = logUpperCPT.getColor((float)x2);
				cpt.add(new CPTVal((float)Math.pow(10, x1), c1, (float)Math.pow(10, x2), c2));
			}
			cpt.setBelowMinColor(cpt.getMinColor());
			cpt.setAboveMaxColor(cpt.getMaxColor());
//			
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
		cpt.setNanColor(Color.DARK_GRAY);
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
				System.out.println("shift down, picked source: "+sourceSection.getName());
				update();
			}
			if (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown()) {
				receiverSection = fault.getFaultSection();
				System.out.println("control down, picked receiver: "+sourceSection.getName());
				updateReceiver();
			}
		}
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.rupSet = null;
		this.distances.clear();
		this.search = null;
		update();
		this.rupSet = rupSet;
		update();
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		Parameter<?> param = event.getParameter();
		if (param == quantityParam) {
			updateCPT();
		} else if (param == gridSpacingParam || param == lambdaParam || param == muParam ||
				param == parentSectParam || param == typeParam || param == rupIndexParam ||
				param == rupQuantityParam) {
			if (calc != null)
				calc = null;
			if (param == gridSpacingParam || param == parentSectParam)
				distances.clear();
			if (param == rupIndexParam) {
				if (rupIndexParam.getValue() >= 0) {
					rupQuantityParam.getEditor().setEnabled(true);
					if (typeParam.getValue() != StiffnessType.CFF) {
						typeParam.removeParameterChangeListener(this);
						typeParam.setValue(StiffnessType.CFF);
						typeParam.getEditor().refreshParamEditor();
						typeParam.addParameterChangeListener(this);
					}
					if (quantityParam.getValue() != StiffnessAggregationMethod.MEDIAN) {
						quantityParam.removeParameterChangeListener(this);
						quantityParam.setValue(StiffnessAggregationMethod.MEDIAN);
						quantityParam.getEditor().refreshParamEditor();
						quantityParam.addParameterChangeListener(this);
					}
				} else {
					rupQuantityParam.getEditor().setEnabled(false);
				}
				rupQuantityParam.getEditor().refreshParamEditor();
			}
			update();
		}
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

}
