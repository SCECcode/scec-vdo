package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.interp.BicubicInterpolation2D;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemSolution;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.HistoricOpenIntervalParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.geoDataSet.GeoDataSetGeometryGenerator;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import scratch.UCERF3.analysis.CompoundFSSPlots.FSSRupNodesCache;
import scratch.UCERF3.analysis.CompoundFSSPlots.MapBasedPlot;
import scratch.UCERF3.analysis.CompoundFSSPlots.MapPlotData;
import scratch.UCERF3.analysis.FaultSysSolutionERF_Calc;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import vtk.vtkActor;
import vtk.vtkCellPicker;

public class ParticipationRateColorer extends CPTBasedColorer implements
		UCERF3RupSetChangeListener, ParameterChangeListener, PickHandler<AbstractFaultSection> {
	
	private ParameterList params;
	
	private DoubleParameter magMinParam;
	private double min = 6.5;
	private double max = 10d;
	private DoubleParameter magMaxParam;
	
	private BooleanParameter probabilityParam;
	private DoubleParameter probDurationParam;
	
	private EnumParameter<PlotType> plotTypeParam;
	private IntegerParameter tdStartYearParam;
	
	private enum PlotType {
		TIME_INDEP("Long Term Rate"),
		TIME_DEP("UCERF3-TD Rate"),
		RATIO("Ratio TD/TI");
		
		private String name;
		private PlotType(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
		
		public boolean usesTD() {
			return this == TIME_DEP || this == RATIO;
		}
	}
	
	private FaultSystemSolution sol;
	
	private static CPT getDefaultRateCPT() {
		try {
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			cpt = cpt.rescale(-6, -2);
			cpt.setNanColor(Color.GRAY);
			return cpt;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private static CPT getDefaultRatioCPT() {
		try {
			CPT cpt = GMT_CPT_Files.GMT_POLAR.instance();
			cpt = cpt.rescale(-2, 2d);
			cpt.setNanColor(Color.GRAY);
			return cpt;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private CPT rateCPT;
	private boolean rateCPTLog;
	private CPT ratioCPT = getDefaultRatioCPT();
	private boolean ratioCPTLog = true;
	
	// this is for gridded seismicity display
	private BooleanParameter displayGriddedParam;
	private BooleanParameter includeFaultsInGriddedParam;
	private DoubleParameter griddedDepthParam;
	private DoubleParameter griddedDataOpacityParam;
	
	private GriddedGeoDataSet loadedGriddedData;
	
	private vtkActor griddedDataActor;
	
	private PluginActors actors;

	public ParticipationRateColorer(PluginActors actors) {
		super(getDefaultRateCPT(), true);
		rateCPT = getCPT();
		rateCPTLog = isCPTLog();
		
		this.actors = actors;
		
		params = new ParameterList();
		magMinParam = new DoubleParameter("Min Mag", 0d, 10d);
		magMinParam.setValue(min);
		magMinParam.addParameterChangeListener(this);
		params.addParameter(magMinParam);
		magMaxParam = new DoubleParameter("Max Mag", 0d, 10d);
		magMaxParam.setValue(max);
		magMaxParam.addParameterChangeListener(this);
		params.addParameter(magMaxParam);
		
		displayGriddedParam = new BooleanParameter("Display Gridded Seismicity", false);
		displayGriddedParam.addParameterChangeListener(this);
		params.addParameter(displayGriddedParam);
		includeFaultsInGriddedParam = new BooleanParameter("Include Faults in Gridded", false);
		includeFaultsInGriddedParam.addParameterChangeListener(this);
		params.addParameter(includeFaultsInGriddedParam);
		griddedDepthParam = new DoubleParameter("Gridded Display Depth", 0d, 30d);
		griddedDepthParam.setValue(0d);
		griddedDepthParam.addParameterChangeListener(this);
		params.addParameter(griddedDepthParam);
		griddedDataOpacityParam = new DoubleParameter("Gridded Data Opacity", 0d, 1d);
		griddedDataOpacityParam.setDefaultValue(0.7);
		griddedDataOpacityParam.setValue(griddedDataOpacityParam.getDefaultValue());
		griddedDataOpacityParam.addParameterChangeListener(this);
		params.addParameter(griddedDataOpacityParam);
		
		probabilityParam = new BooleanParameter("Probabilities", false);
		probabilityParam.addParameterChangeListener(this);
		probabilityParam.setInfo("If selected, Poisson probabilities with the given duration. Otherwise annualized rates");
		params.addParameter(probabilityParam);
		
		probDurationParam = new DoubleParameter("Duration", 1d/365.25, 100000d, "Years");
		probDurationParam.setValue(30);
		probDurationParam.addParameterChangeListener(this);
		probDurationParam.getEditor().setEnabled(false);
		params.addParameter(probDurationParam);
		
		plotTypeParam = new EnumParameter<PlotType>("Plot Type",
				EnumSet.allOf(PlotType.class), PlotType.TIME_INDEP, null);
		plotTypeParam.addParameterChangeListener(this);
		params.addParameter(plotTypeParam);
		
		tdStartYearParam = new IntegerParameter("TD Start Year", 2012, 3000);
		tdStartYearParam.setValue(new GregorianCalendar().get(GregorianCalendar.YEAR));
		tdStartYearParam.addParameterChangeListener(this);
		tdStartYearParam.getEditor().setEnabled(false);
		params.addParameter(tdStartYearParam);
	}
	
	@Override
	public double getValue(AbstractFaultSection fault) {
		if (sol == null)
			return Double.NaN;
		int sectIndex = fault.getId();
		return getValue(sectIndex);
	}
	
	public double getValue(int sectIndex) {
		switch (plotTypeParam.getValue()) {
		case TIME_INDEP:
			return getTI(sectIndex);
		case TIME_DEP:
			return getTD(sectIndex);
		case RATIO:
			double tiVal = getTI(sectIndex);
			double tdVal = getTD(sectIndex);
			return tdVal/tiVal;

		default:
			throw new IllegalStateException("Unknown plot type!");
		}
	}
	
	private synchronized double getTI(int sectIndex) {
		// cached internally
		return getProbIfApplicable(sol.calcParticRateForSect(sectIndex, min, max));
	}
	
	private double[] tdProbsCache;
	
	private synchronized double getTD(int sectIndex) {
//		return sol.calcParticRateForSect(sectIndex, min, max);
		if (tdProbsCache == null) {
			CalcProgressBar p = new CalcProgressBar("Calculating UCERF3-TD Participation Rates", "Calculating Participation Rates");
			FaultSystemSolutionERF erf = new FaultSystemSolutionERF(sol);
			erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.U3_PREF_BLEND);
			erf.setParameter(AleatoryMagAreaStdDevParam.NAME, 0.0);
			int startYear = tdStartYearParam.getValue();
			double duration = probDurationParam.getValue();
			erf.setParameter(HistoricOpenIntervalParam.NAME, startYear-1875d);
			erf.getTimeSpan().setStartTime(startYear);
			erf.getTimeSpan().setDuration(duration);
			erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
			erf.updateForecast();
			
			// now calculate
			FaultSystemRupSet rupSet = sol.getRupSet();
			List<List<Double>> rupProbs = Lists.newArrayList(); // list of probs for every rupture involving each section
			for (int r=0; r<rupSet.getNumSections(); r++)
				rupProbs.add(new ArrayList<Double>());
			for (int sourceID=0; sourceID<erf.getNumFaultSystemSources(); sourceID++) {
				int fssIndex = erf.getFltSysRupIndexForSource(sourceID);
				Preconditions.checkState(fssIndex >= 0);
				for (ProbEqkRupture rup : erf.getSource(sourceID)) {
					if (rup.getMag() >= min && rup.getMag() <= max) {
						double prob = rup.getProbability();
						for (int s : rupSet.getSectionsIndicesForRup(fssIndex))
							rupProbs.get(s).add(prob);
					}
				}
			}
			tdProbsCache = new double[rupSet.getNumSections()];
			for (int s=0; s<tdProbsCache.length; s++)
				tdProbsCache[s] = FaultSysSolutionERF_Calc.calcSummedProbs(rupProbs.get(s));
			p.dispose();
		}
		double prob = tdProbsCache[sectIndex];
		if (probabilityParam.getValue())
			return prob;
		double rate = -Math.log(1-prob)/probDurationParam.getValue();
		return rate;
	}
	
	private double getProbIfApplicable(double rate) {
		// only for TI
		if (probabilityParam.getValue()) {
			double duration = probDurationParam.getValue();
			double prob = 1d - Math.exp(-rate*duration);
			return prob;
		}
		return rate;
	}

	@Override
	public String getName() {
		return "Solution Participation Rates (events/yr)";
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.sol = sol;
		loadedGriddedData = null;
	}
	
	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == magMinParam) {
			double newMin = magMinParam.getValue();
			if (newMin < max) {
				min = newMin;
				fireColorerChangeEvent();
			} else {
				magMinParam.removeParameterChangeListener(this);
				magMinParam.setValue(min);
				magMinParam.addParameterChangeListener(this);
				magMinParam.getEditor().refreshParamEditor();
				JOptionPane.showMessageDialog(null, "Min must be < Max!", "Invalid Range",
						JOptionPane.ERROR_MESSAGE);
			}
			loadedGriddedData = null;
			tdProbsCache = null;
		} else if (event.getParameter() == magMaxParam) {
			double newMax = magMaxParam.getValue();
			if (newMax > min) {
				max = newMax;
				fireColorerChangeEvent();
			} else {
				magMaxParam.removeParameterChangeListener(this);
				magMaxParam.setValue(max);
				magMaxParam.addParameterChangeListener(this);
				magMaxParam.getEditor().refreshParamEditor();
				JOptionPane.showMessageDialog(null, "Max must be > Min!", "Invalid Range",
						JOptionPane.ERROR_MESSAGE);
			}
			loadedGriddedData = null;
			tdProbsCache = null;
		} else if (event.getParameter() == displayGriddedParam
				|| event.getParameter() == includeFaultsInGriddedParam
				|| event.getParameter() == griddedDepthParam) {
			if (event.getParameter() == displayGriddedParam
					|| event.getParameter() == includeFaultsInGriddedParam)
				loadedGriddedData = null;
			displayGriddedData();
		} else if (event.getParameter() == griddedDataOpacityParam) {
			if (griddedDataActor != null) {
				griddedDataActor.GetProperty().SetOpacity(griddedDataOpacityParam.getValue());
				griddedDataActor.Modified();
				MainGUI.updateRenderWindow();
			}
		} else if (event.getParameter() == probabilityParam) {
			probDurationParam.getEditor().setEnabled(probabilityParam.getValue() || plotTypeParam.getValue().usesTD());
			fireColorerChangeEvent();
		} else if (event.getParameter() == probDurationParam) {
			tdProbsCache = null;
			fireColorerChangeEvent();
		} else if (event.getParameter() == plotTypeParam) {
			if (plotTypeParam.getValue() == PlotType.TIME_INDEP || plotTypeParam.getValue() == PlotType.TIME_DEP) {
				if (event.getOldValue().equals(PlotType.RATIO)) {
					// switching from ratio, store any custom ratio settings
					ratioCPT = getCPT();
					ratioCPTLog = isCPTLog();
					setCPT(rateCPT, rateCPTLog);
				}
			} else {
				// switching to ratio, store any current rate settings
				rateCPT = getCPT();
				rateCPTLog = isCPTLog();
				setCPT(ratioCPT, ratioCPTLog);
			}
			boolean usesTD = plotTypeParam.getValue().usesTD();
			displayGriddedParam.getEditor().setEnabled(!usesTD);
			if (usesTD && displayGriddedParam.getValue())
				displayGriddedParam.setValue(false);
			tdStartYearParam.getEditor().setEnabled(usesTD);
			fireColorerChangeEvent();
		} else if (event.getParameter() == tdStartYearParam) {
			tdProbsCache = null;
			if (plotTypeParam.getValue().usesTD())
				fireColorerChangeEvent();
		}
	}

	@Override
	public void actorPicked(PickEnabledActor<AbstractFaultSection> actor,
			AbstractFaultSection fault, vtkCellPicker picker, MouseEvent e) {
		int clickCount = e.getClickCount();
		// return if we don't have a solution, or it's not a double click
		if (sol == null || clickCount < 2 || e.getButton() != MouseEvent.BUTTON1)
			return;
		
		int faultID = fault.getId();
		
		FaultSection sect = sol.getRupSet().getFaultSectionData(faultID);
		
		GraphWindow graph = null;
		
		for (boolean parent : new boolean[] {false, true}) {
			IncrementalMagFreqDist mfd;
			if (parent)
				mfd = sol.calcParticipationMFD_forParentSect(sect.getParentSectionId(), 5.55d, 8.55d, 31);
			else
				mfd = sol.calcParticipationMFD_forSect(faultID, 5.55d, 8.55d, 31);
			
			mfd.setName("Incremental MFD");
			mfd.setInfo(" ");
			EvenlyDiscretizedFunc cumMFD = mfd.getCumRateDistWithOffset();
			cumMFD.setName("Cumulative MFD");
			cumMFD.setInfo(" ");
			ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<>();
			ArrayList<PlotCurveCharacterstics> chars = new ArrayList<>();
			funcs.add(mfd);
			chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));
			funcs.add(cumMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			
//			MinMaxAveTracker yTrack = new MinMaxAveTracker();
//			for (EvenlyDiscretizedFunc func : funcs)
//				for (Point2D pt : func)
//					if (pt.getY() > 0)
//						yTrack.addValue(pt.getY());
//			
//			double minY = yTrack.getMin()*0.5;
//			double maxY = yTrack.getMax()*2d;
			
			String name;
			if (parent)
				name = sect.getParentSectionName();
			else
				name = fault.getName();
			
			PlotSpec spec = new PlotSpec(funcs, chars, name, "Magnitude", "Participation Rate (1/yr)");
			spec.setLegendVisible(true);
			
			if (graph == null)
				graph = new GraphWindow(spec, false);
			else
				graph.addTab(spec);
			
			graph.setYLog(true);
			graph.setAxisRange(6, 9, 1e-10, 1e-1);
		}
		
		graph.setSelectedTab(0);
		graph.setVisible(true);
	}
	
	static GriddedGeoDataSet loadGriddedData(FaultSystemSolution sol, GriddedRegion griddedRegion, double minMag, double maxMag,
			FSSRupNodesCache cache, boolean participation, boolean includeFaultsInGridded) {
		if (!(sol instanceof InversionFaultSystemSolution) && sol.getGridSourceProvider() == null) {
			int ret = JOptionPane.showConfirmDialog(null, "Must be an InversionFaultSystemSolution or have gridded\n" +
					"source data embedded to calculate gridded rates.\nWould you like to load in a COMPOUND_PLOT xml file?",
					"Can't Load Gridded Data", JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.YES_OPTION) {
				JFileChooser choose = new JFileChooser();
				ret = choose.showOpenDialog(null);
				if (ret == JFileChooser.APPROVE_OPTION) {
					try {
						List<MapPlotData> datas = MapBasedPlot.loadPlotData(choose.getSelectedFile());
						ArrayList<String> strings = Lists.newArrayList();
						for (MapPlotData data : datas)
							strings.add(data.getLabel());
						StringParameter sparam = new StringParameter("Select Dataset", strings, strings.get(0));
//						JPanel panel = new JPanel();
//						panel.add(sparam.getEditor().getComponent());
						sparam.getEditor().getComponent().setPreferredSize(new Dimension(500, 50));
						ret = JOptionPane.showConfirmDialog(null, sparam.getEditor().getComponent(), "Dataset From File",
								JOptionPane.OK_CANCEL_OPTION);
						String selected = sparam.getValue();
						if (ret == JOptionPane.OK_OPTION) {
							for (MapPlotData data : datas) {
								if (data.getLabel().equals(selected)) {
									boolean log = selected.contains("Log10");
									GeoDataSet geo = data.getGriddedData();
									GriddedGeoDataSet gridded;
									if (geo instanceof GriddedGeoDataSet) {
										gridded = (GriddedGeoDataSet) data.getGriddedData();
									} else {
										gridded = new GriddedGeoDataSet(new CaliforniaRegions.RELM_TESTING_GRIDDED(0.1), true);
										Preconditions.checkState(gridded.size() == geo.size(), "Can't map to gridded geo dataset!");
										for (int i=0; i<gridded.size(); i++)
											gridded.set(i, geo.get(i));
									}
									if (log)
										gridded.exp(10);
									return gridded;
								}
							}
						}
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, e, "Error Loading Plot Data", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			return null;
		}
		
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(sol);
		erf.getParameter(
				ApplyGardnerKnopoffAftershockFilterParam.NAME)
				.setValue(false); // TODO ?? if true, won't be consistent with fault rates
		if (includeFaultsInGridded)
			erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.INCLUDE);
		else
			erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.ONLY);
//		erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.ONLY);
		erf.updateForecast();
		
		if (participation)
			return ERF_Calculator.getParticipationRatesInRegion(erf,
					griddedRegion, minMag, maxMag, cache);
		else
			return ERF_Calculator.getNucleationRatesInRegion(erf,
					griddedRegion, minMag, maxMag, cache);
	}
	
	static vtkActor buildActorForGriddedData(GriddedGeoDataSet geo, boolean cptLog, CPT cpt,
			double depth, boolean skipNaN, double opacity) {
		if (cptLog) {
			geo = geo.copy();
			geo.log10();
		}
//		GriddedRegion reg = geo.getRegion();
//		vtkActor actor = GeoDataSetGeometryGenerator.buildPixelSurface(geo, cpt, skipNaN, reg.getLatSpacing(), reg.getLonSpacing());
		// super sample it
		geo = superSample(geo, 0.05);
		vtkActor actor = GeoDataSetGeometryGenerator.buildPolygonSurface(geo, cpt, true);
		actor.GetProperty().SetOpacity(opacity);
		return actor;
	}
	
	private static GriddedGeoDataSet superSample(GriddedGeoDataSet in, double targetSpacing) {
		GriddedRegion inReg = in.getRegion();
		double inSpacing = inReg.getLatSpacing();
		System.out.println("Super-sampling with inSpacing="+inSpacing+", target="+targetSpacing);
		Preconditions.checkState((float)inSpacing == (float)inReg.getLonSpacing(), "latSpacing != lonSpacing");
		if (inSpacing <= targetSpacing)
			return in;
		double outSpacing = inSpacing;
		int numPer = 1;
		while (outSpacing > targetSpacing) {
			numPer++;
			outSpacing = inSpacing/(double)numPer;
		}
		System.out.println("Output spacing: "+outSpacing+", numPer="+numPer);
		
		// map to rectangular grid
		MinMaxAveTracker latTrack = new MinMaxAveTracker();
		MinMaxAveTracker lonTrack = new MinMaxAveTracker();
		for (Location loc : inReg.getNodeList()) {
			latTrack.addValue(loc.getLatitude());
			lonTrack.addValue(loc.getLongitude());
		}
		
		int inNX = (int)((lonTrack.getMax() - lonTrack.getMin())/inSpacing + 0.5)+1;
		int inNY = (int)((latTrack.getMax() - latTrack.getMin())/inSpacing + 0.5)+1;
		EvenlyDiscrXYZ_DataSet inXYZ = new EvenlyDiscrXYZ_DataSet(inNX, inNY, lonTrack.getMin(), latTrack.getMin(), inSpacing);
		// initialize to NaN
		for (int i=0; i<inXYZ.size(); i++)
			inXYZ.set(i, Double.NaN);
		EvenlyDiscrXYZ_DataSet outXYZ = new EvenlyDiscrXYZ_DataSet(
				inNX*numPer, inNY*numPer, lonTrack.getMin(), latTrack.getMin(), outSpacing);
		for (int i=0; i<outXYZ.size(); i++)
			outXYZ.set(i, Double.NaN);
		
		// fill in values where available
		for (Location loc : inReg.getNodeList())
			inXYZ.set(loc.getLongitude(), loc.getLatitude(), in.get(loc));
		
		double each = 1d/numPer;
		
		for (int x=0; x<inXYZ.getNumX()-1; x++) {
			for (int y=0; y<inXYZ.getNumY()-1; y++) {
				double[][] G = new double[4][];
				G[0] = getG_row(x, y, 0, inXYZ);
				G[1] = getG_row(x, y, 1, inXYZ);
				G[2] = getG_row(x, y, 2, inXYZ);
				G[3] = getG_row(x, y, 3, inXYZ);
				BicubicInterpolation2D interp = new BicubicInterpolation2D(G);
				
				int outX = x*numPer;
				int outY = y*numPer;
				for (int i=0; i<numPer; i++) {
					double x1 = each*i;
					for (int j=0; j<numPer; j++) {
						double y1 = each*j;
						double val = interp.eval(x1, y1);
						outXYZ.set(outX+i, outY+j, val);
					}
				}
			}
		}
		
		// convert to GriddedGeo
		Location lowerLeft = new Location(inXYZ.getMinY(), inXYZ.getMinX());
		Location upperRight = new Location(inXYZ.getMaxY(), inXYZ.getMaxX());
		GriddedRegion outReg = new GriddedRegion(lowerLeft, upperRight, outSpacing, lowerLeft);
		GriddedGeoDataSet out = new GriddedGeoDataSet(outReg, in.isLatitudeX());
		for (int i=0; i<outXYZ.size(); i++) {
			Point2D pt = outXYZ.getPoint(i);
			Location loc = new Location(pt.getY(), pt.getX());
			if (out.contains(loc))
				out.set(loc, outXYZ.get(i));
		}
		
		return out;
	}
	
	private static double[] getG_row(int x, int y, int row, EvenlyDiscrXYZ_DataSet inXYZ) {
		double[] ret = new double[4];
		
		y += row - 1;
		
		if (y < 0 || y >= inXYZ.getNumY()) {
			for (int i=0; i<4; i++)
				ret[i] = Double.NaN;
			return ret;
		}
		
		for (int i=0; i<4; i++) {
			int myX = x + (i - 1);
			if (myX < 0 || myX >= inXYZ.getNumX())
				ret[i] = Double.NaN;
			else
				ret[i] = inXYZ.get(x, y);
		}
		return ret;
	}
	
	private void displayGriddedData() {
		if (griddedDataActor != null) {
			actors.removeActor(griddedDataActor);
			griddedDataActor = null;
			MainGUI.updateRenderWindow();
		}
		
		if (!displayGriddedParam.getValue())
			return;
		
		if (sol == null)
			return;
		
		if (loadedGriddedData == null) {
			GriddedRegion griddedRegion = new CaliforniaRegions.RELM_TESTING_GRIDDED(0.1);
			double minMag = magMinParam.getValue();
			double maxMag = magMaxParam.getValue();
			loadedGriddedData = loadGriddedData(
					sol, griddedRegion, minMag, maxMag, null,
					true, includeFaultsInGriddedParam.getValue());
		}
		
		if (loadedGriddedData == null) {
			displayGriddedParam.setValue(false);
		} else {
			griddedDataActor = buildActorForGriddedData(loadedGriddedData, isCPTLog(), getCPT(),
					griddedDepthParam.getValue(), false, griddedDataOpacityParam.getValue());
			actors.addActor(griddedDataActor);
			MainGUI.updateRenderWindow();
		}
	}
	@Override
	public String getLegendLabel() {
		String label = plotTypeParam.getValue().toString();
		
		if (max < 9)
			label = smartMagLabel(min)+"≤M≤"+smartMagLabel(max)+" "+label;
		else
			label = "M≥"+smartMagLabel(min)+" "+label;
		
		if (probabilityParam.getValue()) {
			double duration = probDurationParam.getValue();
			String probStr;
			if (duration == (int)duration)
				probStr = (int)duration+"yr";
			else
				probStr = (float)duration+"yr";
			probStr += " Prob";
			label = label.replace("Rate", probStr);
		}
		
		return label;
	}
	
	private String smartMagLabel(double mag) {
		if (mag == (int)mag)
			return (int)mag+"";
		return (float)mag+"";
	}

}
