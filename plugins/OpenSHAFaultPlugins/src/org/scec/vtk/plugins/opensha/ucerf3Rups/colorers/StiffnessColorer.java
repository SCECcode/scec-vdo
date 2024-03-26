package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.gui.plot.GraphWidget;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.IDPairing;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemSolution;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.ClusterRupture;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.FaultSubsectionCluster;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.RuptureConnectionSearch;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.SectionDistanceAzimuthCalculator;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.Surface3D;
import org.opensha.sha.simulators.stiffness.AggregatedStiffnessCalculator;
import org.opensha.sha.simulators.stiffness.AggregatedStiffnessCalculator.AggregationMethod;
import org.opensha.sha.simulators.stiffness.StiffnessCalc;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator.LogDistributionPlot;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator.PatchAlignment;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator.PatchLocation;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator.StiffnessDistribution;
import org.opensha.sha.simulators.stiffness.SubSectStiffnessCalculator.StiffnessType;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.gui.EventManager;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundle;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundler;
import org.scec.vtk.commons.opensha.surfaces.FaultSectionActorList;
import org.scec.vtk.commons.opensha.surfaces.FaultSectionBundledActorList;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.commons.opensha.surfaces.LineSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.PolygonSurfaceGenerator;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;
import org.scec.vtk.tools.picking.PointPickEnabledActor;

import com.google.common.base.Preconditions;

import vtk.vtkActor;
import vtk.vtkCellPicker;

public class StiffnessColorer extends CPTBasedColorer implements PickHandler<AbstractFaultSection>,
UCERF3RupSetChangeListener, ParameterChangeListener {
	
	private static CPT getDefaultCPT() {
		CPT cpt = SubSectStiffnessCalculator.getPreferredPosNegCPT();
		cpt.setNanColor(Color.DARK_GRAY);
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
	private SectionDistanceAzimuthCalculator distAzCalc;
	
	private static final String AGG_1_NAME = "Patch-To-Patch Agg";
	private static final AggregationMethod AGG_1_DEFAULT = AggregationMethod.PASSTHROUGH;
	private EnumParameter<AggregationMethod> agg1Param;
	
	private static final String AGG_2_NAME = "Sect-To-Sect Agg";
	private static final AggregationMethod AGG_2_DEFAULT = AggregationMethod.SUM;
	private EnumParameter<AggregationMethod> agg2Param;
	
	private static final String AGG_3_NAME = "Sects-To-Sect Agg";
	private static final AggregationMethod AGG_3_DEFAULT = AggregationMethod.SUM;
	private EnumParameter<AggregationMethod> agg3Param;
	
	private static final String AGG_4_NAME = "Sects-To-Sects Agg";
	private static final AggregationMethod AGG_4_DEFAULT = AggregationMethod.SUM;
	private EnumParameter<AggregationMethod> agg4Param;
	
	private static final String PARENT_SECT_PARAM_NAME = "Parent Sections";
	private static final boolean PARENT_SECT_PARAM_DEFAULT = false;
	private BooleanParameter parentSectParam;
	
	private static final String PATCH_ALIGNMENT_PARAM_NAME = "Patch Alignment";
	private EnumParameter<PatchAlignment> alignmentParam;
	
	private static final String GRID_SPACING_PARAM_NAME = "Grid Spacing";
	private static final double GRID_SPACING_DEFAULT = 2d;
	private static final double GRID_SPACING_MIN = 0.1d;
	private static final double GRID_SPACING_MAX = 10d;
	private DoubleParameter gridSpacingParam;
	
	private static final String TYPE_PARAM_NAME = "Type";
	private EnumParameter<StiffnessType> typeParam;
	
	public static ButtonParameter clearButton;
	
	private static final String SELF_STIFFNESS_PARAM_NAME = "Self-Stiffness Cap (Multiplier)";
	private static final double SELF_STIFFNESS_DEFAULT = 1d;
	private static final double SELF_STIFFNESS_MIN = 0.0d;
	private static final double SELF_STIFFNESS_MAX = 10d;
	private DoubleParameter selfStiffParam;
	
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
	private SubSectStiffnessCalculator subSectCalc;
	private AggregatedStiffnessCalculator aggCalc;
	private ExecutorService exec;
	private Map<Integer, Double> faultResults;
	private Map<Integer, Double> rupResults;
	private Map<IDPairing, Double> distances;
	
	private List<FaultSection> sources = new ArrayList<>();
	private List<FaultSection> receivers = new ArrayList<>();

	private PluginActors pluginActors;

	private EventManager em;

	public StiffnessColorer(PluginActors pluginActors) {
		super(getDefaultCPT(), false);
		this.pluginActors = pluginActors;
		
		agg1Param = new EnumParameter<>(AGG_1_NAME, EnumSet.allOf(AggregationMethod.class), AGG_1_DEFAULT, null);
		agg1Param.addParameterChangeListener(this);
		params.addParameter(agg1Param);
		
		agg2Param = new EnumParameter<>(AGG_2_NAME, EnumSet.allOf(AggregationMethod.class), AGG_2_DEFAULT, null);
		agg2Param.addParameterChangeListener(this);
		params.addParameter(agg2Param);
		
		agg3Param = new EnumParameter<>(AGG_3_NAME, EnumSet.allOf(AggregationMethod.class), AGG_3_DEFAULT, null);
		agg3Param.addParameterChangeListener(this);
		params.addParameter(agg3Param);
		
		agg4Param = new EnumParameter<>(AGG_4_NAME, EnumSet.allOf(AggregationMethod.class), AGG_4_DEFAULT, null);
		agg4Param.addParameterChangeListener(this);
		params.addParameter(agg4Param);
		

		showPatchesParam = new BooleanParameter(SHOW_PATCHES_NAME, SHOW_PATCHES_PARAM_DEFAULT);
		showPatchesParam.addParameterChangeListener(this);
		params.addParameter(showPatchesParam);
		
		showLinesParam = new BooleanParameter(SHOW_LINES_NAME, SHOW_LINES_PARAM_DEFAULT);
		showLinesParam.addParameterChangeListener(this);
		params.addParameter(showLinesParam);
		
		parentSectParam = new BooleanParameter(PARENT_SECT_PARAM_NAME, PARENT_SECT_PARAM_DEFAULT);
		parentSectParam.addParameterChangeListener(this);
		params.addParameter(parentSectParam);
		
		clearButton = new ButtonParameter("Selected Sources & Receivers", "Clear");
		clearButton.addParameterChangeListener(this);
		params.addParameter(clearButton);
		
		typeParam = new EnumParameter<>(TYPE_PARAM_NAME,
				EnumSet.allOf(StiffnessType.class), StiffnessType.CFF, null);
		typeParam.addParameterChangeListener(this);
		params.addParameter(typeParam);
		
		gridSpacingParam = new DoubleParameter(GRID_SPACING_PARAM_NAME, GRID_SPACING_MIN, GRID_SPACING_MAX);
		gridSpacingParam.setValue(GRID_SPACING_DEFAULT);
		gridSpacingParam.addParameterChangeListener(this);
		params.addParameter(gridSpacingParam);
		
		selfStiffParam = new DoubleParameter(SELF_STIFFNESS_PARAM_NAME, SELF_STIFFNESS_MIN, SELF_STIFFNESS_MAX);
		selfStiffParam.setValue(SELF_STIFFNESS_DEFAULT);
		selfStiffParam.addParameterChangeListener(this);
		params.addParameter(selfStiffParam);
		
		alignmentParam = new EnumParameter<>(PATCH_ALIGNMENT_PARAM_NAME,
				EnumSet.allOf(PatchAlignment.class), SubSectStiffnessCalculator.alignment_default, null);
		alignmentParam.addParameterChangeListener(this);
		params.addParameter(alignmentParam);
		
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
//		rupIndexParam.addParameterChangeListener(this);
//		params.addParameter(rupIndexParam);
//		
		rupQuantityParam = new EnumParameter<>(RUP_QUANTITY_PARAM_NAME, EnumSet.allOf(RupQuantity.class),
				RupQuantity.SECTION_NET, null);
//		rupQuantityParam.getEditor().setEnabled(false);
//		rupQuantityParam.addParameterChangeListener(this);
//		params.addParameter(rupQuantityParam);
		
		reuseRecieverWindowParam = new BooleanParameter(
				REUSE_RECEIVER_WINDOW_PARAM_NAME, REUSE_RECEIVER_WINDOW_PARAM_DEFAULT);
		reuseRecieverWindowParam.setInfo(
				"You can ctrl+click on a fault to set a receiver, which will pop up a plot "
				+ "of the value distribution. If this is selected, the graph window will be "
				+ "reused for each plot (otherwise a new window will pop up).");
		params.addParameter(reuseRecieverWindowParam);
		
		faultResults = new HashMap<>();
		distances = new HashMap<>();
		rupResults = new HashMap<>();
	}
	
	public void setEventManager(EventManager em) {
		this.em = em;
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
					return getCPT().getNanColor();
				} else if (startCluster.contains(sect)) {
					return highlightColor;
				}
			}
		} else if (sources != null) {
			for (FaultSection source : sources)
				if (source.getSectionId() == fault.getId())
					return highlightColor;
		}
		double value = getValue(fault);
		return super.getColorForValue(value);
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		if (curRupture == null) {
			int id = fault.getId();
			Double result = faultResults.get(id);
			if (result == null)
				return Double.NaN;
			return result;
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
		clearPatches();
		
		if (rupSet == null)
			return;
		
		if (subSectCalc == null)
			subSectCalc = new SubSectStiffnessCalculator(rupSet.getFaultSectionDataList(), gridSpacingParam.getValue(),
					lambdaParam.getValue(), muParam.getValue(), coefOfFrictionParam.getValue(), alignmentParam.getValue(), selfStiffParam.getValue());
		if (aggCalc == null)
			aggCalc = new AggregatedStiffnessCalculator(typeParam.getValue(), subSectCalc, false,
					agg1Param.getValue(), agg2Param.getValue(), agg3Param.getValue(), agg4Param.getValue());
		
		if (rupIndexParam.getValue() < 0) {
			if (sources == null || sources.isEmpty())
				return;
			
			if (sources.size() == 1)
				System.out.println("Computing from "+sources.get(0).getName());
			else
				System.out.println("Computing from "+sources.size()+" sources");
			
			if (exec == null) {
				int threads = Integer.min(32, Runtime.getRuntime().availableProcessors());
				if (threads >= 8)
					threads -= 2;
				threads = Integer.max(1, threads);
				exec = Executors.newFixedThreadPool(threads);
			}
			
			List<? extends FaultSection> subSects = rupSet.getFaultSectionDataList();
			
			HashSet<FaultSection> calcReceiverPatches = null;
			if (showPatchesParam.getValue()) {
				calcReceiverPatches = new HashSet<>();
				if (receivers == null || receivers.isEmpty())
					calcReceiverPatches.addAll(subSects);
				else
					calcReceiverPatches.addAll(receivers);
			}
			
			// calculate for everything within the given distance
			List<Future<CalcRunnable>> futures = new ArrayList<>();
			if (parentSectParam.getValue()) {
				Map<Integer, List<FaultSection>> parentSectsMap = subSects.stream().collect(Collectors.groupingBy(s -> s.getParentSectionId()));
				
				for (int parentID : parentSectsMap.keySet())
					futures.add(exec.submit(new CalcRunnable(sources, parentSectsMap.get(parentID), calcReceiverPatches)));
			} else {
				for (FaultSection receiver : subSects)
					futures.add(exec.submit(new CalcRunnable(sources, receiver, calcReceiverPatches)));
			}
			
			System.out.println("Waiting on "+futures.size()+" futures...");
			Map<FaultSection, double[]> patchDists = null;
			if (calcReceiverPatches != null)
				patchDists = new HashMap<>();
			for (Future<CalcRunnable> future : futures) {
				try {
					CalcRunnable run = future.get();
					if (patchDists != null && run.receiverPatchAgg != null)
						patchDists.putAll(run.receiverPatchAgg);
				} catch (Exception e) {
					throw ExceptionUtils.asRuntimeException(e);
				}
			}
			if (patchDists != null && !patchDists.isEmpty()) {
				System.out.println("Displaying patches for "+sources.size()+" source and "
						+patchDists.size()+" receiver sections");
				showPatches(sources, patchDists);
			}
		} else {
			// TODO bring this back?
//			int rupIndex = rupIndexParam.getValue();
//			RupQuantity quantity = rupQuantityParam.getValue();
//			if (rupIndex < rupSet.getNumRuptures()) {
//				if (search == null)
//					search = new RuptureConnectionSearch(rupSet, new SectionDistanceAzimuthCalculator(
//							rupSet.getFaultSectionDataList()), 100d, false);
//				startCluster = null;
//				curRupture = search.buildClusterRupture(rupIndex);
//				if (sourceSection != null) {
//					// see if that applies
//					for (FaultSubsectionCluster cluster : curRupture.getClustersIterable()) {
//						if (cluster.contains(sourceSection)) {
//							startCluster = cluster;
//							break;
//						}
//					}
//					if (startCluster == null) {
//						// selection doesn't involve that cluster, reset it
//						sourceSection = null;
//					}
//				}
//				if (quantity == RupQuantity.CLUSTER_PATH && startCluster == null) {
//					fireColorerChangeEvent();
//					return;
//				}
//				switch (quantity) {
//				case RUPTURE_NET:
//					// calculate net, assign to everything
//					RuptureCoulombResult rupResult = new RuptureCoulombResult(curRupture, calc, quantityParam.getValue());
//					double rupVal = rupResult.getValue(RupCoulombQuantity.SUM_SECT_CFF);
//					for (FaultSubsectionCluster cluster : curRupture.getClustersIterable())
//						for (FaultSection sect : cluster.subSects)
//							rupResults.put(sect.getSectionId(), rupVal);
//					break;
//				case CLUSTER_NET:
//					for (FaultSubsectionCluster cluster : curRupture.getClustersIterable()) {
//						List<FaultSubsectionCluster> sources = new ArrayList<>();
//						for (FaultSubsectionCluster source : curRupture.getClustersIterable())
//							if (cluster != source)
//								sources.add(source);
//						StiffnessResult clusterResult = calc.calcAggClustersToClusterStiffness(StiffnessType.CFF, sources, cluster);
//						double clusterVal = SubSectStiffnessCalculator.getValue(clusterResult, quantityParam.getValue());
//						for (FaultSection sect : cluster.subSects)
//							rupResults.put(sect.getSectionId(), clusterVal);
//					}
//					break;
//				case SECTION_NET:
//					List<FaultSection> allSects = rupSet.getFaultSectionDataForRupture(rupIndex);
//					for (FaultSubsectionCluster cluster : curRupture.getClustersIterable()) {
//						for (FaultSection sect : cluster.subSects) {
//							List<FaultSection> receivers = new ArrayList<>();
//							receivers.add(sect);
//							StiffnessResult sectResult = calc.calcAggStiffness(StiffnessType.CFF, allSects, receivers, -1, -1);
//							double sectVal = SubSectStiffnessCalculator.getValue(sectResult, quantityParam.getValue());
//							rupResults.put(sect.getSectionId(), sectVal);
//						}
//					}
//					break;
//				case CLUSTER_PATH:
//					Preconditions.checkNotNull(startCluster, "Start cluster is null?");
//					calcClusterPath(curRupture.getTreeNavigator(), startCluster, new HashSet<>());
//					break;
//
//				default:
//					break;
//				}
//			}
//			updateReceiver();
		}
		
		System.out.println("Done calculating");
//		updateCPT();
		fireColorerChangeEvent();
	}
	
	private synchronized void updateReceiver() {
		clearLines();
		if (sources == null || sources.isEmpty() || receivers == null || receivers.isEmpty() || subSectCalc == null)
			return;
		StiffnessDistribution plotDist = null;
		int sourceID = sources.size() > 1 ? sources.get(0).getSectionId() : -1;
		int receiverID = receivers.size() > 1 ? receivers.get(0).getSectionId() : -1;
		for (FaultSection source : sources) {
			for (FaultSection receiver : receivers) {
				StiffnessDistribution dist = subSectCalc.calcStiffnessDistribution(source, receiver);
				if (showLinesParam.getValue())
					showLines(dist, source, receiver);
				if (agg1Param.getValue().isTerminal()) {
					// aggregate at this level
					dist = dist.receiverAggregate(agg1Param.getValue());
				}
				if (plotDist == null)
					plotDist = dist;
				else
					plotDist = plotDist.add(dist);
			}
		}
		if (!reuseRecieverWindowParam.getValue() || receiverGW == null || !receiverGW.isVisible())
			receiverGW = new GraphWindow(new GraphWidget());
		LogDistributionPlot plot = subSectCalc.plotDistributionHistograms(plotDist, typeParam.getValue(),
				sourceID, receiverID);
		plot.plotInGW(receiverGW);
//		if (showPatchesParam.getValue()) {
//			List<PatchLocation> sourcePatches = calc.getPatches(sourceSection);
//			showPatches(sourcePatches, null);
//			List<PatchLocation> receiverPatches = calc.getPatches(receiverSection);
//			showPatches(receiverPatches, dist);
//		}
	}
	
	private HashSet<vtkActor> patchActors;
	
	private synchronized void clearPatches() {
		if (patchActors == null || patchActors.isEmpty())
			return;
		System.out.println("Clearing patches");
		for (vtkActor actor : patchActors)
			pluginActors.removeActor(actor);
		patchActors = null;
		em.updateViewer();
	}
	
	private class PatchSection extends AbstractFaultSection {

		private FaultSection sect;
		private PatchLocation patch;
		private int patchIndex;
		private double val;

		public PatchSection(FaultSection sect, PatchLocation patch, int patchIndex, double val) {
			super(sect.getName()+", Patch "+patchIndex, 1000*sect.getSectionId()+patchIndex);
			this.sect = sect;
			this.patch = patch;
			this.patchIndex = patchIndex;
			this.val = val;
		}

		@Override
		public String getInfo() {
			return sect.getSectionId()+". "+sect.getName()+", Patch "+patchIndex+", value: "+val+", parID="+sect.getParentSectionId();
		}

		@Override
		public Surface3D createSurface(ParameterList faultRepresentationParams) {
			return null;
		}

		@Override
		public double getSlipRate() {
			return Double.NaN;
		}

		@Override
		public double getAvgRake() {
			return Double.NaN;
		}

		@Override
		public double getAvgStrike() {
			return Double.NaN;
		}

		@Override
		public double getAvgDip() {
			return Double.NaN;
		}
		
	}
	
	private class PatchSectBundler implements FaultActorBundler {
		
		private Map<Integer, FaultActorBundle> bundleMap = new HashMap<>();

		@Override
		public FaultActorBundle getBundle(AbstractFaultSection fault) {
			if (!(fault instanceof PatchSection)) {
				return null;
			}
			PatchSection patchSect = (PatchSection)fault;
			Integer parentID = patchSect.sect.getParentSectionId();
			if (parentID < 0) {
				return null;
			}
			FaultActorBundle bundle = bundleMap.get(parentID);
			if (bundle == null) {
				System.out.println("Building a *new* bundle for "+patchSect.sect.getParentSectionName());
				bundle = new FaultActorBundle();
				bundleMap.put(parentID, bundle);
			}
			return bundle;
		}

		@Override
		public void clearBundles() {
			bundleMap.clear();
		}
		
	}
	
	private void showPatches(Collection<FaultSection> sources, Map<FaultSection, double[]> faultPatchDists) {
		SwingUtilities.invokeLater(new PatchDisplayRunnable(sources, faultPatchDists));
	}
	
	private class PatchDisplayRunnable implements Runnable {
		
		private Collection<FaultSection> sources;
		private Map<FaultSection, double[]> faultPatchDists;

		public PatchDisplayRunnable(Collection<FaultSection> sources, Map<FaultSection, double[]> faultPatchDists) {
			this.sources = sources;
			this.faultPatchDists = faultPatchDists;
		}

		@Override
		public void run() {
			PolygonSurfaceGenerator polyGen = new PolygonSurfaceGenerator();
			polyGen.setBundlerEneabled(true);
			polyGen.setFaultActorBundler(new PatchSectBundler());
//			polyGen.setPickHandler(new NameDispalyPickHandler());
			polyGen.setPickHandler(StiffnessColorer.this);
			polyGen.setOpacity(0.8);

			HashSet<FaultSection> allSects = new HashSet<>(sources);
			allSects.addAll(faultPatchDists.keySet());

			for (FaultSection sect : allSects) {
				List<PatchLocation> patches = subSectCalc.getPatches(sect);
				double[] patchVals = faultPatchDists.get(sect);
				if (patchVals != null) {
					Preconditions.checkState(patches.size() == patchVals.length,
							"Have %s patches on %s but have %s patch values", patches.size(), sect.getSectionId(), patchVals.length);
				}

				if (patchActors == null)
					patchActors = new HashSet<>();

				for (int i=0; i<patches.size(); i++) {
					PatchLocation patch = patches.get(i);
					Color c;
					double val;
					if (patchVals == null) {
						c = highlightColor;
						val = Double.NaN;
					} else {
						val = patchVals[i];
						c = getCPT().getColor((float)val);
					}

					LocationList outline = new LocationList();
					Preconditions.checkState(patch.corners.length == 4);
					for (Location corner : patch.corners)
						outline.add(corner);
					FaultSectionActorList list = polyGen.buildSimplePolygon(outline, c, new PatchSection(sect, patch, i, val));

					em.displayActors(list, false);

					if (list instanceof FaultSectionBundledActorList) {
						FaultSectionBundledActorList bundled = (FaultSectionBundledActorList)list;
						PointPickEnabledActor<AbstractFaultSection> actor = bundled.getBundle().getActor();
						if (!patchActors.contains(actor)) {
							patchActors.add(actor);
							//						pluginActors.addActor(actor);
						}
						bundled.getBundle().setVisible(bundled, true);
					} else {
						for (vtkActor actor : list) {
							patchActors.add(actor);
							//						pluginActors.addActor(actor);
						}
					}
				}
			}

			em.updateViewer();
		}
		
	}
	
	private HashSet<vtkActor> lineActors;
	
	private synchronized void clearLines() {
		if (lineActors == null || lineActors.isEmpty())
			return;
		System.out.println("Clearing lines");
		for (vtkActor actor : lineActors)
			pluginActors.removeActor(actor);
		lineActors = null;
		em.updateViewer();
	}
	
	private class LineSection extends AbstractFaultSection {

		private FaultSection source;
		private FaultSection receiver;
		private int sourceIndex;
		private int receiverIndex;
		private double val;

		public LineSection(FaultSection source, FaultSection receiver, int sourceIndex,
				int receiverIndex, double val) {
			super(source.getSectionId()+"["+sourceIndex+"] => "+receiver.getSectionId()+"["+receiverIndex+"]",
					source.getSectionId()*10000 + receiver.getSectionId()*1000 + sourceIndex*100 + receiverIndex);
			this.source = source;
			this.receiver = receiver;
			this.sourceIndex = sourceIndex;
			this.receiverIndex = receiverIndex;
			this.val = val;
		}

		@Override
		public String getInfo() {
			return getName()+", value: "+val;
		}

		@Override
		public Surface3D createSurface(ParameterList faultRepresentationParams) {
			return null;
		}

		@Override
		public double getSlipRate() {
			return Double.NaN;
		}

		@Override
		public double getAvgRake() {
			return Double.NaN;
		}

		@Override
		public double getAvgStrike() {
			return Double.NaN;
		}

		@Override
		public double getAvgDip() {
			return Double.NaN;
		}
		
	}
	
	private class LineSectBundler implements FaultActorBundler {
		
		FaultActorBundle bundle;

		@Override
		public FaultActorBundle getBundle(AbstractFaultSection fault) {
			if (!(fault instanceof LineSection)) {
				return null;
			}
			
			if (bundle == null) {
				System.out.println("Building a *new* line bundle");
				bundle = new FaultActorBundle();
			}
			return bundle;
		}

		@Override
		public void clearBundles() {
			bundle = null;
		}
		
	}
	
	private void showLines(StiffnessDistribution dist, FaultSection source, FaultSection receiver) {
		SwingUtilities.invokeLater(new LineDisplayRunnable(dist, source, receiver));
	}
	
	private class LineDisplayRunnable implements Runnable {
		private StiffnessDistribution dist;
		private FaultSection source;
		private FaultSection receiver;
		
		private LineDisplayRunnable(StiffnessDistribution dist, FaultSection source, FaultSection receiver) {
			this.dist = dist;
			this.source = source;
			this.receiver = receiver;
		}
		
		@Override
		public void run() {
			double[][] fullVals = dist.get(typeParam.getValue());
			if (lineActors == null)
				lineActors = new HashSet<>();
			
			LineSurfaceGenerator lineGen = new LineSurfaceGenerator();
			lineGen.setBundlerEneabled(true);
			lineGen.setFaultActorBundler(new LineSectBundler());
			lineGen.setOpacity(0.75d);
			
			for (int s=0; s<dist.sourcePatches.size(); s++) {
				PatchLocation source = dist.sourcePatches.get(s);
				double[] sourcePt = GeometryGenerator.getPointForLoc(source.center);
				for (int r=0; r<dist.receiverPatches.size(); r++) {
					PatchLocation receiver = dist.receiverPatches.get(r);
					double val = fullVals[r][s];
					Color c = getCPT().getColor((float)val);
					
					LocationList line = new LocationList();
					line.add(source.center);
					line.add(receiver.center);
					
					FaultSectionActorList list = lineGen.createFaultActors(
							line, c, new LineSection(this.source, this.receiver, s, r, val));
					
					em.displayActors(list, false);

					if (list instanceof FaultSectionBundledActorList) {
						FaultSectionBundledActorList bundled = (FaultSectionBundledActorList)list;
						PointPickEnabledActor<AbstractFaultSection> actor = bundled.getBundle().getActor();
						if (!lineActors.contains(actor)) {
							lineActors.add(actor);
							//						pluginActors.addActor(actor);
						}
						bundled.getBundle().setVisible(bundled, true);
					} else {
						for (vtkActor actor : list) {
							lineActors.add(actor);
							//						pluginActors.addActor(actor);
						}
					}
				}
			}
			
			em.updateViewer();
		}
	}
	
	// TODO bring this back?
//	private void calcClusterPath(RuptureTreeNavigator navigator, FaultSubsectionCluster receiver,
//			HashSet<FaultSubsectionCluster> parents) {
//		if (!parents.isEmpty()) {
//			StiffnessResult clusterResult = calc.calcAggClustersToClusterStiffness(StiffnessType.CFF, parents, receiver);
//			double clusterVal = SubSectStiffnessCalculator.getValue(clusterResult, quantityParam.getValue());
//			for (FaultSection sect : receiver.subSects)
//				rupResults.put(sect.getSectionId(), clusterVal);
//		}
//		HashSet<FaultSubsectionCluster> newParents = new HashSet<>(parents);
//		newParents.add(receiver);
//		for (FaultSubsectionCluster descendant : navigator.getDescendants(receiver)) {
//			if (!parents.contains(descendant))
//				calcClusterPath(navigator, descendant, newParents);
//		}
//		// look the other way
//		FaultSubsectionCluster predecessor = navigator.getPredecessor(receiver);
//		if (predecessor != null && !parents.contains(predecessor))
//			calcClusterPath(navigator, predecessor, newParents);
//	}
	
	private class CalcRunnable implements Callable<CalcRunnable> {

		private List<FaultSection> sources;
		private List<FaultSection> receivers;
		
		private Map<FaultSection, double[]> receiverPatchAgg;
		private HashSet<FaultSection> calcReceiverPatches;

		public CalcRunnable(List<FaultSection> sources, FaultSection receiver, HashSet<FaultSection> calcReceiverPatches) {
			this.sources = sources;
			this.calcReceiverPatches = calcReceiverPatches;
			this.receivers = new ArrayList<>();
			receivers.add(receiver);
		}

		public CalcRunnable(List<FaultSection> sources, List<FaultSection> receivers, HashSet<FaultSection> calcReceiverPatches) {
			this.sources = sources;
			this.receivers = receivers;
			this.calcReceiverPatches = calcReceiverPatches;
		}

		@Override
		public CalcRunnable call() {
			// filter out any overlap
			HashSet<Integer> sourceIDs = new HashSet<>();
			for (FaultSection source : sources)
				sourceIDs.add(source.getSectionId());
			List<FaultSection> calcReceivers = new ArrayList<>();
			for (FaultSection receiver : receivers)
				if (!sourceIDs.contains(receiver.getSectionId()))
					calcReceivers.add(receiver);
			if (calcReceivers.isEmpty())
				return this;

			// check distance threshold. include if *any* within threshold
			double distThreshold = maxDistParam.getValue();
			double minDist = Double.POSITIVE_INFINITY;
			for (FaultSection source : sources) {
				for (FaultSection receiver : calcReceivers) {
					double dist = distAzCalc.getDistance(source, receiver);
					minDist = Math.min(dist, minDist);
					if (minDist <= distThreshold)
						break;
				}
				if (minDist <= distThreshold)
					break;
			}
			
			if (minDist > distThreshold)
				return this;
			
			if (calcReceiverPatches != null) {
				receiverPatchAgg = new HashMap<>();
				for (FaultSection receiver : calcReceivers) {
					if (calcReceiverPatches.contains(receiver))
						receiverPatchAgg.put(receiver, aggCalc.calcReceiverPatchAgg(sources, receiver));
				}
			}
			double val = aggCalc.calc(sources, calcReceivers);
			for (FaultSection receiver : calcReceivers)
				faultResults.put(receiver.getSectionId(), val);
			return this;
		}
		
	}
	
//	private void updateCPT() {
//		CPT cpt;
//		boolean hasFract = false;
//		for (AggregationMethod method : aggCalc.getAggregationLayers())
//			if (method == AggregationMethod.FRACT_POSITIVE)
//				hasFract = true;
//		if (hasFract) {
//			cpt = getFractCPT();
//		} else {
//			double maxVal = 0.1;
//			double minNonZero = 0.01;
//			for (double val : faultResults.values()) {
//				val = Math.abs(val);
//				maxVal = Math.max(maxVal, val);
//				if (val > 0)
//					minNonZero = Math.min(minNonZero, val);
//			}
////			if (vals.isEmpty())
////				maxVal = 1d;
////			else if (vals.size() < 5)
////				maxVal = StatUtils.max(Doubles.toArray(vals));
////			else
////				maxVal = StatUtils.percentile(Doubles.toArray(vals), 95d);
//			maxVal = Math.max(maxVal, 0.01);
////			cpt = getDefaultCPT().rescale(-maxVal, maxVal);
//			
////			double minColorVal = 0.0001;
////			CPT upperCPT = new CPT(minColorVal, maxVal,
////					new Color(255, 180, 180), new Color(255, 0, 0), new Color(100, 0, 0));
////			CPT lowerCPT = new CPT(-maxVal, -minColorVal,
////					new Color(0, 0, 100), new Color(0, 0, 255), new Color(180, 180, 255));
////			cpt = new CPT();
////			cpt.addAll(lowerCPT);
////			cpt.add(new CPTVal(lowerCPT.getMaxValue(), lowerCPT.getMaxColor(), 0f, Color.WHITE));
////			cpt.add(new CPTVal(0f, Color.WHITE, upperCPT.getMinValue(), upperCPT.getMinColor()));
////			cpt.addAll(upperCPT);
////			cpt.setBelowMinColor(cpt.getMinColor());
////			cpt.setAboveMaxColor(cpt.getMaxColor());
//			
//			// another log-ish try
//			double logMinVal = Math.max(-6, Math.floor(Math.log10(minNonZero)));
//			double logMaxVal = Math.min(1, Math.ceil(Math.log10(maxVal)));
//			if (logMaxVal < -4)
//				logMinVal = logMaxVal - 3;
//			CPT logUpperCPT = new CPT(logMinVal, logMaxVal-1, new Color(255, 220, 220),
//					new Color(255, 0, 0));
//			logUpperCPT.add(new CPTVal(logUpperCPT.getMaxValue(), logUpperCPT.getMaxColor(),
//					(float)logMaxVal, new Color(100, 0, 0)));
//			EvenlyDiscretizedFunc logDiscr = new EvenlyDiscretizedFunc(logMinVal, logMaxVal, 100);
//			CPT logLowerCPT = new CPT(logMinVal, logMaxVal-1, new Color(220, 220, 255),
//					new Color(0, 0, 255));
//			logLowerCPT.add(new CPTVal(logLowerCPT.getMaxValue(), logLowerCPT.getMaxColor(),
//					(float)logMaxVal, new Color(0, 0, 100)));
//			cpt = new CPT();
//			for (int i=logDiscr.size(); --i>0;) {
//				double x1 = logDiscr.getX(i); // abs larger value, neg smaller
//				double x2 = logDiscr.getX(i-1); // abs smaller value, neg larger
//				Color c1 = logLowerCPT.getColor((float)x1);
//				Color c2 = logLowerCPT.getColor((float)x2);
//				cpt.add(new CPTVal(-(float)Math.pow(10, x1), c1, -(float)Math.pow(10, x2), c2));
//			}
//			cpt.add(new CPTVal(cpt.getMaxValue(), cpt.getMaxColor(), 0f, Color.WHITE));
//			cpt.add(new CPTVal(0f, Color.WHITE, (float)Math.pow(10, logUpperCPT.getMinValue()),
//					logUpperCPT.getMinColor()));
//			for (int i=0; i<logDiscr.size()-1; i++) {
//				double x1 = logDiscr.getX(i);
//				double x2 = logDiscr.getX(i+1);
//				Color c1 = logUpperCPT.getColor((float)x1);
//				Color c2 = logUpperCPT.getColor((float)x2);
//				cpt.add(new CPTVal((float)Math.pow(10, x1), c1, (float)Math.pow(10, x2), c2));
//			}
//			cpt.setBelowMinColor(cpt.getMinColor());
//			cpt.setAboveMaxColor(cpt.getMaxColor());
////			
////			double minNonZero = Double.POSITIVE_INFINITY;
////			double maxNonZero = 0d;
////			for (StiffnessResult[] results : faultResults.values()) {
////				StiffnessResult result = typeParam.getValue() == Type.SIGMA ? results[0] : results[1];
////				double val = Math.abs(getValue(result));
////				if (Double.isFinite(val) && val > 0d) {
////					minNonZero = Math.min(minNonZero, val);
////					maxNonZero = Math.max(maxNonZero, val);
////				}
////			}
//			
//			// do log-ish
////			double minNonZero = Double.POSITIVE_INFINITY;
////			double maxNonZero = 0d;
////			for (StiffnessResult[] results : faultResults.values()) {
////				StiffnessResult result = typeParam.getValue() == Type.SIGMA ? results[0] : results[1];
////				double val = Math.abs(getValue(result));
////				if (Double.isFinite(val) && val > 0d) {
////					minNonZero = Math.min(minNonZero, val);
////					maxNonZero = Math.max(maxNonZero, val);
////				}
////			}
////			if (!Double.isFinite(minNonZero)) {
////				cpt = getDefaultCPT();
////			} else {
////				if (minNonZero == maxNonZero)
////					maxNonZero += 1d;
////				cpt = buildSemiLogCPT(minNonZero, maxNonZero);
////			}
//		}
//		cpt.setNanColor(Color.DARK_GRAY);
//		setCPT(cpt, false);
//		fireColorerChangeEvent();
//	}
	
//	private static CPT buildSemiLogCPT(double absMin, double absMax) {
//		double logMin = Math.log10(absMin);
//		double logMax = Math.log10(absMax);
//		System.out.println("LogMin: "+logMin);
//		System.out.println("LogMax: "+logMax);
//		CPT logPositive = new CPT(logMin, logMax, Color.WHITE, Color.RED.brighter(), Color.RED, Color.RED.darker());
//		CPT logNegative = new CPT(-logMax, -logMin, Color.BLUE.darker(), Color.BLUE, Color.BLUE.brighter(), Color.WHITE);
//		
//		EvenlyDiscretizedFunc logDiscr = new EvenlyDiscretizedFunc(logMin, logMax, 20);
//		CPT cpt = new CPT();
//		// add negative
//		for (int i=logDiscr.size(); --i>=0;) {
//			double logVal1 = logDiscr.getX(i);
//			Color color1 = logNegative.getColor((float)logVal1);
//			float val1 = (float)-Math.pow(10, logVal1);
//			
//			float val2;
//			Color color2;
//			if (i == 0) {
//				val2 = 0f;
//				color2 = Color.WHITE;
//			} else {
//				double logVal2 = logDiscr.getX(i-1);
//				color2 = logNegative.getColor((float)-logVal2);
//				val2 = (float)-Math.pow(10, logVal2);
//			}
//			
//			cpt.add(new CPTVal(val1, color1, val2, color2));
//		}
//		// add positive
//		for (int i=0; i<logDiscr.size(); i++) {
//			float val1;
//			Color color1;
//			if (i == 0) {
//				val1 = 0f;
//				color1 = Color.WHITE;
//			} else {
//				double logVal1 = logDiscr.getX(i-1);
//				color1 = logPositive.getColor((float)logVal1);
//				val1 = (float)Math.pow(10, logVal1);
//			}
//
//			double logVal2 = logDiscr.getX(i);
//			Color color2 = logPositive.getColor((float)logVal2);
//			float val2 = (float)Math.pow(10, logVal2);
//			
//			cpt.add(new CPTVal(val1, color1, val2, color2));
//		}
//		return cpt;
//	}

	@Override
	public synchronized void actorPicked(PickEnabledActor<AbstractFaultSection> actor,
			AbstractFaultSection reference, vtkCellPicker picker, MouseEvent e) {
		if (reference instanceof PatchSection)
			System.out.println(((PatchSection)reference).getInfo());
		
		if (reference instanceof PrefDataSection || reference instanceof PatchSection) {
			
			FaultSection sect;
			if (reference instanceof PrefDataSection)
				sect = ((PrefDataSection)reference).getFaultSection();
			else
				sect = ((PatchSection)reference).sect;
			if (e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown()) {
				List<FaultSection> sectList = new ArrayList<>();
				boolean isReceiver = e.isAltDown();
				String mode = isReceiver ? "RECEIVER" : "SOURCE";
				System.out.println("shift down, mode="+mode+", picked: "+sect.getName());
				if 	(e.isControlDown()) {
					// we're adding a section to a previous list
					if (isReceiver) {
						if (receivers != null)
							sectList.addAll(receivers);
					} else {
						if (sources != null)
							sectList.addAll(sources);
					}
					System.out.println("\twill add to "+sectList.size()+" previously selected");
				}
				if (sectList.contains(sect)) {
					sectList.remove(sect);
				} else {
					if (parentSectParam.getValue()) {
						for (FaultSection s : rupSet.getFaultSectionDataList().stream().filter(
								S -> S.getParentSectionId() == sect.getParentSectionId()).collect(Collectors.toList())) {
							if (!sectList.contains(s)) {
								System.out.println("\tadding new section on parentID="+s.getParentSectionId()+": "+s.getName());
								sectList.add(s);
							}
						}
					} else {
						sectList.add(sect);
					}
				}
				
				if (isReceiver) {
					System.out.println("\tnow have "+sectList.size()+" receivers");
					receivers = sectList;
					updateReceiver();
				}
//				} else {
				if (!isReceiver || (receivers != null && !receivers.isEmpty() && showPatchesParam.getValue())) {	
					System.out.println("\tnow have "+sectList.size()+" sources");
					sources = sectList;
					update();
				}
			}
		}
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.rupSet = null;
		this.distances.clear();
		this.search = null;
		this.distAzCalc = null;
		this.subSectCalc = null;
		this.aggCalc = null;
		update();
		this.rupSet = rupSet;
		if (rupSet != null)
			distAzCalc = new SectionDistanceAzimuthCalculator(rupSet.getFaultSectionDataList());
		update();
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		Parameter<?> param = event.getParameter();
		if (param == agg1Param || param == agg2Param || param == agg3Param || param == agg4Param) {
			aggCalc = null;
			update();
			if (receivers != null && !receivers.isEmpty())
				updateReceiver();
		} else if (param == gridSpacingParam || param == lambdaParam || param == muParam || param == rupIndexParam || param == selfStiffParam) {
			// for these ones we need to rebuild the calculator
			subSectCalc = null;
			aggCalc = null;
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
				} else {
					rupQuantityParam.getEditor().setEnabled(false);
				}
				rupQuantityParam.getEditor().refreshParamEditor();
			}
			update();
		} else if (param == parentSectParam) {
			distances.clear();
			update();
		} else if (param == typeParam || param == alignmentParam || param == showPatchesParam
				|| param == rupQuantityParam) {
			// just need to update (with old calculator)
			update();
		} else if (param == showLinesParam) {
			updateReceiver();
		} else if (param == clearButton) {
			sources = null;
			receivers = null;
			update();
			updateReceiver();
		}
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

}
