package org.scec.vtk.plugins.opensha.ucerf3Disagg;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.data.Site;
import org.opensha.commons.geo.Location;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.WarningParameter;
import org.opensha.commons.param.editor.impl.ParameterListParameterEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.ParameterListParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.ServerPrefUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.faultSurface.PointSurface;
import org.opensha.sha.faultSurface.utils.PtSrcDistCorr;
import org.opensha.sha.gui.beans.IMR_MultiGuiBean;
import org.opensha.sha.gui.beans.IMT_NewGuiBean;
import org.opensha.sha.gui.beans.Site_GuiBean;
import org.opensha.sha.gui.beans.TimeSpanGuiBean;
import org.opensha.sha.gui.controls.SiteDataControlPanel;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.event.ScalarIMRChangeEvent;
import org.opensha.sha.imr.event.ScalarIMRChangeListener;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.util.TRTUtils;
import org.opensha.sha.util.TectonicRegionType;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.tree.FaultCategoryNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.tools.Transform;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.mean.MeanUCERF3;
import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkDoubleArray;
import vtk.vtkGlyph3D;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;
import vtk.vtkUnsignedCharArray;

public class UCERF3DisaggBuilder implements FaultTreeBuilder, ParameterChangeListener, ScalarIMRChangeListener {
	
	private ERF erf;
	private FaultSystemRupSet rupSet;
	
//	private FindEquivUCERF2_FM2pt1_Ruptures equivUCERF2;
	
	private EnumParameter<ERF_CHOICES> erfParam;
	private ParameterListParameter erfParamsParam = new ParameterListParameter("ERF Params");
	private ButtonParameter timeSpanButton = new ButtonParameter("Time Span", "Set Time Span");
	private ButtonParameter imrParamsButton = new ButtonParameter("IMR", "Set IMR Params");
	private ButtonParameter imtParamsButton = new ButtonParameter("IMT", "Set IMT");
	private ButtonParameter siteParamsButton = new ButtonParameter("Site", "Set Site");
	private ButtonParameter siteDataParamsButton = new ButtonParameter("Site Data", "Set Site Data From Web");
	private DoubleParameter imlParam;
	private ButtonParameter computeButton = new ButtonParameter("Hazard Calculation", "Compute");
	private DoubleParameter magMinParam;
	private DoubleParameter magMaxParam;
	
	private ParameterListParameter subParamsParam;
	
	private TimeSpanGuiBean timeSpanBean;
	private IMR_MultiGuiBean imrBean;
	private IMT_NewGuiBean imtBean;
	private Site_GuiBean siteBean;
	private SiteDataControlPanel siteControl;
	
	private ParameterList faultParams = PrefDataSection.createPrefDataParams();
	
	private enum ERF_CHOICES {
//		UCERF2("UCERF2"),
		MEAN_UCERF3("Mean UCERF3"),
		FSS("Fault System Solution");
		
		private String name;
		
		private ERF_CHOICES(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	private ParameterList builderParams = new ParameterList();
	
	private TreeChangeListener l;
	
	private CPT defaultCPT;
	
	private DisaggColorer colorer;
	
	private JFileChooser choose;
	private PluginActors actors;
	
	public UCERF3DisaggBuilder(PluginActors actors) {
		this.actors = actors;
		ParameterList subParams = new ParameterList();
		
		erfParam = new EnumParameter<ERF_CHOICES>("ERF Select", EnumSet.allOf(ERF_CHOICES.class), ERF_CHOICES.MEAN_UCERF3, null);
		erfParam.addParameterChangeListener(this);
		builderParams.addParameter(erfParam);

		erfParamsParam.setValue(new ParameterList());
		erfParamsParam.addParameterChangeListener(this);
		subParams.addParameter(erfParamsParam);

		timeSpanButton.addParameterChangeListener(this);
		subParams.addParameter(timeSpanButton);

		imrParamsButton.addParameterChangeListener(this);
		subParams.addParameter(imrParamsButton);

		imtParamsButton.addParameterChangeListener(this);
		subParams.addParameter(imtParamsButton);
		
		siteParamsButton.addParameterChangeListener(this);
		subParams.addParameter(siteParamsButton);
		
		siteDataParamsButton.addParameterChangeListener(this);
		subParams.addParameter(siteDataParamsButton);
		
		magMinParam = new DoubleParameter("Min Max", 0d, 10d, new Double(0d));
		subParams.addParameter(magMinParam);
		
		magMaxParam = new DoubleParameter("Max Max", 0d, 10d, new Double(10d));
		subParams.addParameter(magMaxParam);
		
		subParamsParam = new ParameterListParameter("ERF/IMR/IMT/Site Params", subParams);
		((ParameterListParameterEditor)subParamsParam.getEditor()).setModal(false);
		builderParams.addParameter(subParamsParam);
		
		imlParam = new DoubleParameter("IML", 0d, 100d, new Double(0.2));
		imlParam.addParameterChangeListener(this);
		builderParams.addParameter(imlParam);
		
		computeButton.addParameterChangeListener(this);
		builderParams.addParameter(computeButton);
		
		try {
			defaultCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-8, -1);
			defaultCPT.setBelowMinColor(Color.GRAY);
			defaultCPT.setNanColor(Color.GRAY);
		} catch (IOException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		colorer = new DisaggColorer();
		
		List<? extends ScalarIMR> imrs =
				AttenRelRef.instanceList(null, true, ServerPrefUtils.SERVER_PREFS);
		for (ScalarIMR imr : imrs) {
			imr.setParamDefaults();
		}

		imrBean = new IMR_MultiGuiBean(imrs);
		imrBean.setSelectedSingleIMR(AttenRelRef.CB_2008.toString());
		imrBean.setMaxChooserChars(30);
		imrBean.rebuildGUI();
		imrBean.addIMRChangeListener(this);
		
		imtBean = new IMT_NewGuiBean(imrs);
		imtBean.addIMTChangeListener(imrBean);
		imtBean.setSelectedIMT(SA_Param.NAME);
		imtBean.setMinimumSize(new Dimension(200, 90));
		imtBean.setPreferredSize(new Dimension(290, 220));
		
		siteBean = new Site_GuiBean();
		siteBean.addSiteParams(imrBean.getMultiIMRSiteParamIterator());
		
		loadERF();
	}
	
	public ArrayList<FaultColorer> getColorers() {
		ArrayList<FaultColorer> colorers = Lists.newArrayList();
		colorers.add(colorer);
		return colorers;
	}

	@Override
	public ParameterList getBuilderParams() {
		return builderParams;
	}

	@Override
	public ParameterList getFaultParams() {
		return faultParams;
	}

	@Override
	public void setTreeChangeListener(TreeChangeListener l) {
		this.l = l;
	}

	@Override
	public void buildTree(DefaultMutableTreeNode root) {
		
		if (rupSet == null)
			return;
		
		FaultCategoryNode catNode = new FaultCategoryNode("Fault Based Sources", "");

		List<? extends FaultSection> sectionData = rupSet.getFaultSectionDataList();

		for (FaultSection data : sectionData) {
			String name = data.getSectionId()+". "+data.getName();
			PrefDataSection fault = new PrefDataSection(name, data);

			FaultSectionNode faultNode = new FaultSectionNode(fault);

			catNode.add(faultNode);
		}
		
		catNode.setVisible(true);

		root.add(catNode);
	}
	
	private void loadERF() {
		this.erf = null;
		this.rupSet = null;
		switch (erfParam.getValue()) {
//		case UCERF2:
//			if (equivUCERF2 == null) {
//				File rupSetFile = new File(dataDir, "ucerf2_rup_set.zip");
//				System.out.println("RupSetFile: "+rupSetFile.getAbsolutePath());
//				FaultSystemRupSet rupSet = null;
//				if (rupSetFile.exists()) {
//					try {
//						rupSet = FaultSystemIO.loadRupSet(rupSetFile);
//					} catch (Exception e) {
//						// bad file
//						e.printStackTrace();
//						rupSetFile.delete();
//					}
//				}
//				if (rupSet == null) {
//					rupSet = InversionFaultSystemRupSetFactory.forBranch(
//						LaughTestFilter.getDefault(), 0, FaultModels.FM2_1, DeformationModels.UCERF2_ALL,
//						ScalingRelationships.AVE_UCERF2, SlipAlongRuptureModels.UNIFORM,
//						InversionModels.CHAR_CONSTRAINED, SpatialSeisPDF.UCERF2);
//					try {
//						FaultSystemIO.writeRupSet(rupSet, rupSetFile);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//				if (!dataDir.exists())
//					dataDir.mkdirs();
//				equivUCERF2 = new FindEquivUCERF2_FM2pt1_Ruptures(rupSet, dataDir);
//				equivUCERF2.getUCERF2_ERF().setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//				equivUCERF2.getUCERF2_ERF().setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
//			}
//			this.erf = equivUCERF2.getUCERF2_ERF();
//			break;
		case MEAN_UCERF3:
			MeanUCERF3 erf = new MeanUCERF3();
			erf.setMeanParams(0d, false, 0.1d, MeanUCERF3.RAKE_BASIS_NONE);
			this.erf = erf;
			break;
		case FSS:
			// browse for file
			if (choose == null)
				choose = new JFileChooser();
			
		    File defaultDir = new File(MainGUI.getCWD(),
					"data");	    
			if (defaultDir.exists())
				choose.setCurrentDirectory(defaultDir);
			
			int ret = choose.showOpenDialog(null);
			if (ret == JFileChooser.APPROVE_OPTION)
				this.erf = new FaultSystemSolutionERF(choose.getSelectedFile().getAbsolutePath());
			else
				this.erf = new FaultSystemSolutionERF();
			break;
			
		default:
			throw new IllegalStateException("Unknown ERF: "+erfParam.getValue());
		}
		
		erfParamsParam.removeParameterChangeListener(this);
		erfParamsParam.setValue(erf.getAdjustableParameterList());
		erfParamsParam.getEditor().setParameter(erfParamsParam);
		erfParamsParam.addParameterChangeListener(this);
	}
	
	private void loadRupSet() {
		this.rupSet = null;
		switch (erfParam.getValue()) {
//		case UCERF2:
//			this.rupSet = equivUCERF2.getFaultSysRupSet();
//			break;
		case MEAN_UCERF3:
			this.rupSet = ((MeanUCERF3)erf).getSolution().getRupSet();
			break;
		case FSS:
			this.rupSet = ((FaultSystemSolutionERF)erf).getSolution().getRupSet();
			break;

		default:
			throw new IllegalStateException("Unknown ERF: "+erfParam.getValue());
		}
	}
	
	private Map<Location, Double> ptSourceVals;
	private Map<Integer, Double> sectVals;
	
	private void compute() {
		// will need to rebuild the tree later if ERF was null before this call
		if (erf == null) {
			loadERF();
		}
		erf.updateForecast();
		clearActors();
		ptSourceVals = Maps.newHashMap();
		sectVals = Maps.newHashMap();
		if (rupSet == null) {
			loadRupSet();
			fireTreeChangeEvent();
		}
		
		// currently hardcoded for CB 2008
		Map<TectonicRegionType, ScalarIMR> imrMap = imrBean.getIMRMap();
		
		Site site = siteBean.getSite();
		
//		DiscretizedFunc func = new IMT_Info().getDefaultHazardCurve(imtBean.getSelectedIMT());
//		
//		// calculate
//		calc.getHazardCurve(func, site, imrMap, erf);
		
		// now disagg
		double maxDist = 200;
		int numSources = erf.getNumSources();
		
		double iml = imlParam.getValue();
		
		// set iml/site in imrs
		for (ScalarIMR imr : imrMap.values()) {
			imtBean.setIMTinIMR(imr);
			Parameter<Double> im = imr.getIntensityMeasure();
			System.out.println("IMT: "+im.getName());
			if (im instanceof WarningParameter<?>) {
				WarningParameter<Double> warnIM = (WarningParameter<Double>)im;
				warnIM.setValueIgnoreWarning(new Double(iml));
			} else {
				im.setValue(new Double(iml));
			}
			imr.setSite(site);
		}
		
		int rupCount = 0;
		double minMag = magMinParam.getValue();
		double maxMag = magMaxParam.getValue();
		
		for (int i = 0; i < numSources; i++) {
			// get source and get its distance from the site
			ProbEqkSource source = erf.getSource(i);

			String sourceName = source.getName();
			int numRuptures = erf.getNumRuptures(i);

			// check the distance of the source
			double distance = source.getMinDistance(site);
			if (distance > maxDist) {
				rupCount += numRuptures;
				continue;
			}
			
			// set the IMR according to the tectonic region of the source (if there is more than one)
			TectonicRegionType trt = source.getTectonicRegionType();
			ScalarIMR imr = TRTUtils.getIMRforTRT(imrMap, trt);
			
//			// Set Tectonic Region Type in IMR
//			if(setTRTinIMR_FromSource) { // (otherwise leave as originally set)
//				TRTUtils.setTRTinIMR(imr, trt, nonSupportedTRT_OptionsParam, trtDefaults.get(imr));
//			}
			
			for (int n = 0; n < numRuptures; n++) {

				// get the rupture
				ProbEqkRupture rupture = source.getRupture(n);

				double mag = rupture.getMag();
				
				if (mag < minMag || mag > maxMag) {
					rupCount++;
					continue;
				}
				
				// set point-source distance correction type & mag if it's a pointSurface
				if(rupture.getRuptureSurface() instanceof PointSurface)
					((PointSurface)rupture.getRuptureSurface()).setDistCorrMagAndType(rupture.getMag(), PtSrcDistCorr.Type.NSHMP08);

				double qkProb = rupture.getProbability();

				// set the rupture in the imr
				imr.setEqkRupture(rupture);
//				Parameter<Double> im = imr.getIntensityMeasure();
//				if (im instanceof WarningParameter<?>) {
//					WarningParameter<Double> warnIM = (WarningParameter<Double>)im;
//					warnIM.setValueIgnoreWarning(new Double(iml));
//				} else {
//					im.setValue(new Double(iml));
//				}
//				imr.setAll(rupture, site, im);

				// get the cond prob
				double condProb = imr.getExceedProbability(iml);
				if (condProb <= 0) {
					rupCount++;
					continue;
				}

				// get the equiv. Poisson rate over the time interval (not annualized)
				double rate = -condProb * Math.log(1 - qkProb);
				
				if (rate > 0)
					registerRate(i, source, n, rupCount, rupture, rate);
				rupCount++;
			}
		}
		colorer.fireColorerChangeEvent();
		// now done in fire above
	}
	
	private void registerRate(int sourceID, ProbEqkSource source, int rupID, int rupCount, ProbEqkRupture rup, double rate) {
		if (rup.getRuptureSurface() instanceof PointSurface) {
			// point source
			Location ptLoc = rup.getRuptureSurface().getFirstLocOnUpperEdge();
			Double prevVal = ptSourceVals.get(ptLoc);
			if (prevVal == null)
				ptSourceVals.put(ptLoc, rate);
			else
				ptSourceVals.put(ptLoc, prevVal+rate);
		} else {
			int fssIndex;
//			if (erfParam.getValue() == ERF_CHOICES.UCERF2) {
//				// ucerf2
//				// TODO
//				fssIndex = equivUCERF2.getEquivFaultSystemRupIndexForUCERF2_Rupture(rupCount);
//				if (fssIndex < 0) {
//					System.out.println("WARNING: ignoring UCERF2 src="+sourceID+", rup="+rupID
//							+", mag="+rup.getMag()+", contrib_rate="+rate+" ("+source.getName()+")");
//					return;
//				}
//			} else {
				// FSS
				String name = source.getName();
				Preconditions.checkState(name.startsWith("Inversion Src #"));
				name = name.substring(name.indexOf('#')+1, name.indexOf(';'));
				fssIndex = Integer.parseInt(name.trim());
//			}
			for (int sect : rupSet.getSectionsIndicesForRup(fssIndex)) {
				Double prevVal = sectVals.get(sect);
				if (prevVal == null)
					sectVals.put(sect, rate);
				else
					sectVals.put(sect, prevVal+rate);
			}
		}
	}
	
	private vtkActor sphereActor;
	private vtkPoints spherePoints;
	private vtkDoubleArray sphereRadius;
	private vtkUnsignedCharArray sphereColors;
	
	private vtkActor coneActor;
	private vtkPoints conePoints;
	private vtkDoubleArray coneRadius;
	private vtkUnsignedCharArray coneColors;
	
	private void clearActors() {
		actors.clearActors();
		
		sphereActor = null;
		spherePoints = null;
		sphereRadius = null;
		sphereColors = null;
	}
	
	private void displayGridded() {
		clearActors();
		if (ptSourceVals == null)
			return;
		for (Location loc : ptSourceVals.keySet()) {
			double val = ptSourceVals.get(loc);
			if (colorer.isCPTLog()) {
				if (Math.log10(val) < colorer.getCPT().getMinValue())
					continue;
			} else {
				if (val < colorer.getCPT().getMinValue())
					continue;
			}
			
			Color color = colorer.getColorForValue(val);
			
			displayLoc(color, false, loc);
		}
		displayLoc(Color.PINK, true, siteBean.getSite().getLocation());
	}
	
	private void displayLoc(Color color, boolean cone, Location loc) {
		vtkActor actor;
		vtkPoints points;
		vtkDoubleArray radiusArray;
		vtkUnsignedCharArray colorArray;
		
		if (cone) {
			if (coneActor == null) {
				conePoints = new vtkPoints();
				coneRadius = new vtkDoubleArray();
				coneRadius.SetName("radius");
				coneColors = new vtkUnsignedCharArray();
				coneColors.SetName("colors");
				coneColors.SetNumberOfComponents(4);
				
				vtkPolyDataMapper mapper = new vtkPolyDataMapper();
				
				vtkPolyData inputData = new vtkPolyData();
				inputData.SetPoints(conePoints);
				inputData.GetPointData().AddArray(coneRadius);
				inputData.GetPointData().AddArray(coneColors);
				inputData.GetPointData().SetActiveScalars("radius");
				
				// Use cone as glyph source
				vtkConeSource balls = new vtkConeSource();
				balls.SetRadius(1.0);//.01);
				balls.SetResolution(7);
				
				vtkGlyph3D glyphPoints = new vtkGlyph3D();
				glyphPoints.SetInputData(inputData);
				glyphPoints.SetSourceConnection(balls.GetOutputPort());
				mapper.SetInputConnection(glyphPoints.GetOutputPort());
				
				mapper.ScalarVisibilityOn();
				mapper.SetScalarModeToUsePointFieldData();
				mapper.SelectColorArray("colors");
				
				coneActor = new vtkActor();
				coneActor.SetMapper(mapper);
				coneActor.SetVisibility(1);
				coneActor.GetProperty().SetOpacity(1d);
				
				actors.addActor(coneActor);
			}
			actor = coneActor;
			points = conePoints;
			radiusArray = coneRadius;
			colorArray = coneColors;
		} else {
			if (sphereActor == null) {
				spherePoints = new vtkPoints();
				sphereRadius = new vtkDoubleArray();
				sphereRadius.SetName("radius");
				sphereColors = new vtkUnsignedCharArray();
				sphereColors.SetName("colors");
				sphereColors.SetNumberOfComponents(4);
				
				vtkPolyDataMapper mapper = new vtkPolyDataMapper();
				
				vtkPolyData inputData = new vtkPolyData();
				inputData.SetPoints(spherePoints);
				inputData.GetPointData().AddArray(sphereRadius);
				inputData.GetPointData().AddArray(sphereColors);
				inputData.GetPointData().SetActiveScalars("radius");
				
				// Use sphere as glyph source
				vtkSphereSource balls = new vtkSphereSource();
				balls.SetRadius(1.0);//.01);
				balls.SetPhiResolution(7);
				balls.SetThetaResolution(7);
				
				vtkGlyph3D glyphPoints = new vtkGlyph3D();
				glyphPoints.SetInputData(inputData);
				glyphPoints.SetSourceConnection(balls.GetOutputPort());
				mapper.SetInputConnection(glyphPoints.GetOutputPort());
				
				mapper.ScalarVisibilityOn();
				mapper.SetScalarModeToUsePointFieldData();
				mapper.SelectColorArray("colors");
				
				sphereActor = new vtkActor();
				sphereActor.SetMapper(mapper);
				sphereActor.SetVisibility(1);
				sphereActor.GetProperty().SetOpacity(1d);
				
				actors.addActor(sphereActor);
			}
			actor = sphereActor;
			points = spherePoints;
			radiusArray = sphereRadius;
			colorArray = sphereColors;
		}
		
		double[] pt = Transform.transformLatLonHeight(loc.getLatitude(), loc.getLongitude(), -loc.getDepth());
		
		double size = 2d;
		
		int index = points.GetNumberOfPoints();
		points.InsertNextPoint(pt);
		radiusArray.InsertNextTuple1(size);
		colorArray.InsertNextTuple4(color.getRed(), color.getGreen(), color.getBlue(), 255);
		points.Modified();
		radiusArray.Modified();
		colorArray.Modified();
		actor.Modified();
		
//		Appearance app = new Appearance();
//		Color3f color3f = new Color3f(color);
//		app.setColoringAttributes(new ColoringAttributes(color3f , ColoringAttributes.SHADE_FLAT));
//		
//		Primitive prim;
//		if (cone)
//			prim = new Cone(2f, 2f, app);
//		else
//			prim = new Sphere(2f, app);
//		
//		Point3d point = LatLongToPoint.plotPoint(loc.getLatitude(), loc.getLongitude());
//		Vector3d v = new Vector3d(point);
//		Transform3D t3d = SurfaceOrientationUtils.orientOnSurface(
//				loc.getLatitude(), loc.getLongitude(),
//				0, 90, 0);
//
//		t3d.setTranslation(v);
//		TransformGroup tGroup = new TransformGroup(t3d);
//		tGroup.addChild(prim);
//		
//		BranchGroup subBG = createBG();
//		subBG.addChild(tGroup);
//		bg.addChild(subBG);
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getSource() == computeButton) {
			compute();
		} else if (event.getSource() == erfParam) {
			erf = null;
			rupSet = null;
			loadERF();
			clearActors();
			fireTreeChangeEvent();
		} else if (event.getSource() == erfParamsParam) {
			if (erf == null)
				loadERF();
			rupSet = null;
			clearActors();
			fireTreeChangeEvent();
		} else if (event.getSource() == timeSpanButton) {
			if (erf == null)
				loadERF();
			if (timeSpanBean == null)
				timeSpanBean = new TimeSpanGuiBean(erf.getTimeSpan());
			else
				timeSpanBean.setTimeSpan(erf.getTimeSpan());
			showDialog(timeSpanBean);
		} else if (event.getSource() == imrParamsButton) {
			showDialog(imrBean);
		} else if (event.getSource() == imtParamsButton) {
			showDialog(imtBean);
		} else if (event.getSource() == siteParamsButton) {
			showDialog(siteBean);
		} else if (event.getSource() == siteDataParamsButton) {
			siteControl = new SiteDataControlPanel(null, this.imrBean,
					this.siteBean);
			siteControl.showControlPanel();
		}
	}
	
	private void showDialog(JComponent comp) {
//		JDialog d = new JDialog();
//		d.setContentPane(comp);
//		d.setModal(true);
//		d.setVisible(true);
		JFrame frame = new JFrame();
		frame.setContentPane(comp);
		frame.pack();
		frame.setVisible(true);
	}
	
	private void fireTreeChangeEvent() {
		if (l != null) {
			l.treeChanged(null);
		}
	}
	
	private class DisaggColorer extends CPTBasedColorer {

		public DisaggColorer() {
			super(defaultCPT, true);
			
		}

		@Override
		public String getName() {
			return "Disaggregation Colorer";
		}

		@Override
		public double getValue(AbstractFaultSection fault) {
			if (sectVals == null || !(fault instanceof PrefDataSection))
				return Double.NaN;
			FaultSection sect = ((PrefDataSection)fault).getFaultSection();
			Double val = sectVals.get(sect.getSectionId());
			if (val == null || val == 0)
				return Double.NaN;
			return val;
		}

		@Override
		public void setCPT(CPT cpt) {
			displayGridded();
			super.setCPT(cpt);
		}

		@Override
		public void setCPT(CPT cpt, boolean isLog) {
			displayGridded();
			super.setCPT(cpt, isLog);
		}

		@Override
		public void setCPTLog(boolean newCPTLog) {
			displayGridded();
			super.setCPTLog(newCPTLog);
		}

		@Override
		public void fireColorerChangeEvent() {
			displayGridded();
			super.fireColorerChangeEvent();
		}
		
	}

	@Override
	public void imrChange(ScalarIMRChangeEvent event) {
		// then update site params
		siteBean.replaceSiteParams(imrBean.getMultiIMRSiteParamIterator());
		siteBean.validate();
		siteBean.repaint();
	}

}
