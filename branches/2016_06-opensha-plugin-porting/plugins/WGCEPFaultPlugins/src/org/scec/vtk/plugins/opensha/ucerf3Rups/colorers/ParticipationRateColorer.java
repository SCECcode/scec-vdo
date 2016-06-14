package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dom4j.DocumentException;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.faultSurface.FourPointEvenlyGriddedSurface;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;
import org.scec.geo3d.library.wgcep.faults.colorers.CPTBasedColorer;
import org.scec.geo3d.library.wgcep.surfaces.FaultSectionActorList;
import org.scec.geo3d.library.wgcep.surfaces.PolygonSurfaceGenerator;
import org.scec.geo3d.library.wgcep.surfaces.pickBehavior.NameDispalyPickHandler;
import org.scec.geo3d.library.wgcep.surfaces.pickBehavior.PickHandler;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.analysis.CompoundFSSPlots.FSSRupNodesCache;
import scratch.UCERF3.analysis.CompoundFSSPlots.MapBasedPlot;
import scratch.UCERF3.analysis.CompoundFSSPlots.MapPlotData;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import vtk.vtkActor;

public class ParticipationRateColorer extends CPTBasedColorer implements
		UCERF3RupSetChangeListener, ParameterChangeListener, PickHandler {
	
	private ParameterList params;
	
	private DoubleParameter magMinParam;
	private double min = 6.5;
	private double max = 10d;
	private DoubleParameter magMaxParam;
	
	private FaultSystemSolution sol;
	
	private static final double cpt_min = 1.0e-6;
	private static final double cpt_max = 1.0e-2;
	
	protected static CPT getDefaultCPT() {
		try {
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			cpt = cpt.rescale(cpt_min, cpt_max);
			return cpt;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	// this is for gridded seismicity display
	private BooleanParameter displayGriddedParam;
	private BooleanParameter includeFaultsInGriddedParam;
	private DoubleParameter griddedDepthParam;
	
	private PolygonSurfaceGenerator polygonSurfGen;
	
	private GriddedGeoDataSet loadedGriddedData;

	public ParticipationRateColorer() {
		super(getDefaultCPT(), false);
		setCPTLog(true);
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
		
		polygonSurfGen = new PolygonSurfaceGenerator();
		for (Parameter<?> param : polygonSurfGen.getDisplayParams()) {
			param.addParameterChangeListener(this);
			params.addParameter(param);
		}
	}
	
	@Override
	public double getValue(AbstractFaultSection fault) {
		if (sol == null)
			return Double.NaN;
		int sectIndex = fault.getId();
		return sol.calcParticRateForSect(sectIndex, min, max);
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
		} else if (event.getParameter() == displayGriddedParam
				|| event.getParameter() == includeFaultsInGriddedParam
				|| event.getParameter() == griddedDepthParam) {
			if (event.getParameter() == displayGriddedParam
					|| event.getParameter() == includeFaultsInGriddedParam)
				loadedGriddedData = null;
			displayGriddedData();
		}
	}

	@Override
	public void faultPicked(FaultSectionActorList faultShape, MouseEvent mouseEvent) {
		int clickCount = mouseEvent.getClickCount();
		// return if we don't have a solution, or it's not a double click
		if (sol == null || clickCount < 2)
			return;
		
		AbstractFaultSection fault = faultShape.getFault();
		int faultID = fault.getId();
		
		boolean parent = mouseEvent.isShiftDown();
		FaultSectionPrefData sect = sol.getRupSet().getFaultSectionData(faultID);

		IncrementalMagFreqDist mfd;
		if (parent)
			mfd = sol.calcParticipationMFD_forParentSect(sect.getParentSectionId(), 5.55d, 8.55d, 31);
		else
			mfd = sol.calcParticipationMFD_forSect(faultID, 5.55d, 8.55d, 31);
		mfd.setName("Incremental Mag Freq Dist");
		mfd.setInfo(" ");
		EvenlyDiscretizedFunc cumMFD = mfd.getCumRateDistWithOffset();
		cumMFD.setName("Cumulative Mag Freq Dist");
		cumMFD.setInfo(" ");
		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
		funcs.add(mfd);
		funcs.add(cumMFD);
//		funcs.add(sol.calcNucleationMFD_forSect(faultID, 6d, 8.5d, 26));
		
		String name;
		if (parent)
			name = sect.getParentSectionName();
		else
			name = fault.getName();
		
		GraphWindow graph = new GraphWindow(funcs, "Participation Rates for "+name);
		graph.setYLog(true);
		graph.setAxisRange(6, 9, 1e-10, 1e-1);
		
		graph.setX_AxisLabel("Magnitude");
		graph.setY_AxisLabel("Participation Rate");
	}

	@Override
	public void nothingPicked(MouseEvent mouseEvent) {
		// nothing to do here
	}

	@Override
	public void otherPicked(vtkActor node, MouseEvent mouseEvent) {
		nothingPicked(mouseEvent);
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
	
	// TODO
//	static BranchGroup buildBGForGriddedData(GriddedGeoDataSet geo, boolean cptLog, CPT cpt,
//			PolygonSurfaceGenerator surfGen, double depth, boolean skipNaN) {
//		BranchGroup bg = new BranchGroup();
//		
//        bg.setCapability(BranchGroup.ALLOW_DETACH);
//        bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
//        bg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
//        bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
//        
//        double halfLatSpacing = 0.5*geo.getRegion().getLatSpacing();
//        double halfLonSpacing = 0.5*geo.getRegion().getLonSpacing();
//        
//        for (int i=0; i<geo.size(); i++) {
//        	Location loc = geo.getLocation(i);
//        	double val = geo.get(i);
//        	if (skipNaN && Double.isNaN(val))
//        		continue;
//        	if (cptLog)
//        		val = Math.log10(val);
//        	
//        	double lat = loc.getLatitude();
//        	double lon = loc.getLongitude();
//        	
//        	Location nw = new Location(lat+halfLatSpacing, lon-halfLonSpacing, depth);
//        	Location ne = new Location(lat+halfLatSpacing, lon+halfLonSpacing, depth);
//        	Location sw = new Location(lat-halfLatSpacing, lon-halfLonSpacing, depth);
//        	Location se = new Location(lat-halfLatSpacing, lon+halfLonSpacing, depth);
//        	
//        	FourPointEvenlyGriddedSurface surf = new FourPointEvenlyGriddedSurface(nw, sw, se, ne);
//        	
//        	Color color = cpt.getColor((float)val);
//        	
//        	bg.addChild(surfGen.createFaultBranchGroup(surf, color, null));
//        }
//        
//        return bg;
//	}
	
	private void displayGriddedData() {
		// TODO
//		if (bg != null)
//			bg.removeAllChildren();
//		
//		if (!displayGriddedParam.getValue())
//			return;
//		
//		if (sol == null)
//			return;
//		
//		if (bg == null) {
//			bg = new BranchGroup();
//			bg.setCapability(BranchGroup.ALLOW_DETACH);
//			bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
//			bg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
//			bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
//			
//			Geo3dInfo.getMainWindow().getPluginBranchGroup().addChild(bg);
//		}
//		
//		if (loadedGriddedData == null) {
//			GriddedRegion griddedRegion = new CaliforniaRegions.RELM_TESTING_GRIDDED(0.1);
//			double minMag = magMinParam.getValue();
//			double maxMag = magMaxParam.getValue();
//			loadedGriddedData = loadGriddedData(
//					sol, griddedRegion, minMag, maxMag, null,
//					true, includeFaultsInGriddedParam.getValue());
//		}
//		
//		if (loadedGriddedData == null)
//			displayGriddedParam.setValue(false);
//		else
//			bg.addChild(buildBGForGriddedData(loadedGriddedData, isCPTLog(), getCPT(),
//					polygonSurfGen, griddedDepthParam.getValue(), false));
	}

}
