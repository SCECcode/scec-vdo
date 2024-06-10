package org.scec.vtk.plugins.opensha.ucerf3Rups.anims;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dom4j.DocumentException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemSolution;
import org.opensha.sha.faultSurface.FaultSection;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.IDBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.anim.TimeBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;
import org.scec.vtk.tools.Transform;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;
import org.scec.vtk.tools.picking.PointPickEnabledActor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.ETAS.ETAS_SimAnalysisTools;
import scratch.UCERF3.erf.ETAS.launcher.ETAS_Launcher;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.kevin.ucerf3.etas.MPJ_ETAS_Simulator;
import vtk.vtkCellPicker;
import vtk.vtkDoubleArray;
import vtk.vtkGlyph3D;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;
import vtk.vtkUnsignedCharArray;
import vtk.vtkVertexGlyphFilter;

public class ETASCatalogAnim extends CPTBasedColorer implements TimeBasedFaultAnimation, IDBasedFaultAnimation,
UCERF3RupSetChangeListener, ParameterChangeListener, PickHandler<AbstractFaultSection> {

	private FileParameter catalogFileParam;
	private BooleanParameter hideFaultsParam;
	protected DoubleParameter minMagParam;
	protected DoubleParameter opacityParam;
	private StringParameter subSectParam;
	private static final String ALL_SUB_SECTS = "(all)";
	private BooleanParameter onlyCurrentParam;
	private DoubleParameter catDurationParam;

	private ParameterList animParams;

	private List<ETAS_EqkRupture> catalog;
	private List<ETAS_EqkRupture> displayCatalog;
	private Map<Integer, ETAS_EqkRupture> catalogMap;
	private Map<Integer, Integer> parentsMap;
	private Map<Integer, List<Integer>> childrenMap;

	private FaultSystemSolutionERF erf;
	protected FaultSystemSolution sol;

	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

	private int curStep = -1;
	protected HashSet<Integer> sectsRuptured;
	protected Map<Integer, Integer> mostRecentSectRups;
	protected Map<Integer, Double> mostRecentSectMag;
	
	private PickHandler<ETAS_EqkRupture> spherePicker;
	
	private PluginActors actors;
	
	private static final boolean point_display = false;
	private vtkVertexGlyphFilter vertexGlyphFilter;
	
	private static final double[] sphere_mag_bins = {3.5,	4d,	5d,	6d,	100d}; // max mag for bin
	private static final int[] sphere_resolutions = {5, 	7,	12,	20,	40};
//	private static final double[] sphere_mag_bins = {100d}; // max mag for bin
//	private static final int[] sphere_resolutions = {10};
	private PointPickEnabledActor<ETAS_EqkRupture>[] sphereActor;
	private vtkPoints[] spherePoints;
	private vtkDoubleArray[] sphereRadius;
	private vtkUnsignedCharArray[] sphereColors;
	
	private static CPT getDefaultCPT() {
		//		CPT cpt = new CPT();
		//		
		//		cpt.add(new CPTVal(2.5f, Color.GREEN, 8.0f, Color.RED));
		//		cpt.setBelowMinColor(Color.GREEN);
		//		cpt.setAboveMaxColor(Color.RED);

		//		CPT cpt = new CPT(2.5, 8.0, Color.GREEN, Color.YELLOW, Color.RED, new Color(170, 0, 0)); // end dark red
		CPT cpt = new CPT(2.5, 8.0, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN); // end dark red

		return cpt;
	}

	public ETASCatalogAnim(PluginActors actors) {
		super(getDefaultCPT(), false);
		
		this.actors = actors;
		
		animParams = new ParameterList();

		catalogFileParam = new FileParameter("Catalog File");
		catalogFileParam.addParameterChangeListener(this);
		animParams.addParameter(catalogFileParam);
		// if Kevin's machine, open right where you need it
		File defaultInitialDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations");
		if (defaultInitialDir.exists())
			catalogFileParam.setDefaultInitialDir(defaultInitialDir);

		hideFaultsParam = new BooleanParameter("Hide Faults", true);
		hideFaultsParam.setValueAsDefault();
		hideFaultsParam.addParameterChangeListener(this);
		animParams.addParameter(hideFaultsParam);

		minMagParam = new DoubleParameter("Min Mag", 0d, 10d, Double.valueOf(0d));
		minMagParam.addParameterChangeListener(this);
		animParams.addParameter(minMagParam);

		opacityParam = new DoubleParameter("Opacity", 0d, 1d, Double.valueOf(0.5d));
		opacityParam.addParameterChangeListener(this);
		animParams.addParameter(opacityParam);
		
		subSectParam = new StringParameter("Events Involving Sect", Lists.newArrayList(ALL_SUB_SECTS), ALL_SUB_SECTS);
		subSectParam.addParameterChangeListener(this);
		animParams.addParameter(subSectParam);

		onlyCurrentParam = new BooleanParameter("Only Show Current", false);
		onlyCurrentParam.setValueAsDefault();
		onlyCurrentParam.addParameterChangeListener(this);
		animParams.addParameter(onlyCurrentParam);
		
		catDurationParam = new DoubleParameter("Duration To Animate", 0d, 100000, Double.valueOf(0d));
		catDurationParam.setUnits("Years");
		catDurationParam.addParameterChangeListener(this);
		animParams.addParameter(catDurationParam);
		
		spherePicker = new PickHandler<ETAS_EqkRupture>() {
			
			@Override
			public void actorPicked(PickEnabledActor<ETAS_EqkRupture> actor, ETAS_EqkRupture reference, vtkCellPicker picker,
					MouseEvent e) {
				etasSphereActorPicked(actor, reference, picker, e);
			}
		};
	}
	
	private synchronized void initSphereActor() {
		sphereActor = new PointPickEnabledActor[sphere_mag_bins.length];
		spherePoints = new vtkPoints[sphere_mag_bins.length];
		sphereRadius = new vtkDoubleArray[sphere_mag_bins.length];
		sphereColors = new vtkUnsignedCharArray[sphere_mag_bins.length];
		for (int i=0; i<sphere_mag_bins.length; i++) {
			spherePoints[i] = new vtkPoints();
			sphereRadius[i] = new vtkDoubleArray();
			sphereRadius[i].SetName("radius");
			sphereColors[i] = new vtkUnsignedCharArray();
			sphereColors[i].SetName("colors");
			sphereColors[i].SetNumberOfComponents(4);
			
			vtkPolyDataMapper mapper = new vtkPolyDataMapper();
			
			vtkPolyData inputData = new vtkPolyData();
			inputData.SetPoints(spherePoints[i]);
			inputData.GetPointData().AddArray(sphereRadius[i]);
			inputData.GetPointData().AddArray(sphereColors[i]);
			inputData.GetPointData().SetActiveScalars("radius");
			
			if (point_display) {
				vertexGlyphFilter = new vtkVertexGlyphFilter();
				vertexGlyphFilter.AddInputData(inputData);
				vertexGlyphFilter.Update();

				mapper.SetInputConnection(vertexGlyphFilter.GetOutputPort());
			} else {
				// Use sphere as glyph source
				vtkSphereSource balls = new vtkSphereSource();
				balls.SetRadius(1.0);//.01);
				balls.SetPhiResolution(sphere_resolutions[i]);
				balls.SetThetaResolution(sphere_resolutions[i]);
				
				vtkGlyph3D glyphPoints = new vtkGlyph3D();
				glyphPoints.SetInputData(inputData);
				glyphPoints.SetSourceConnection(balls.GetOutputPort());
				mapper.SetInputConnection(glyphPoints.GetOutputPort());
			}

			mapper.ScalarVisibilityOn();
			mapper.SetScalarModeToUsePointFieldData();
			mapper.SelectColorArray("colors");
			
			sphereActor[i] = new PointPickEnabledActor<>(spherePicker);
			sphereActor[i].SetMapper(mapper);
			sphereActor[i].SetVisibility(1);
			sphereActor[i].GetProperty().SetOpacity(opacityParam.getValue());
			
			actors.addActor(sphereActor[i]);
		}
	}

	protected synchronized void displayEvent(ETAS_EqkRupture rup) {
		if (rup.getMag() < minMagParam.getValue())
			return;
		if (onlyCurrentParam.getValue())
			clearEvents();
		int fssIndex = rup.getFSSIndex();
		if (fssIndex >= 0) {
			// fault based
			System.out.println("Fault based rupture, M="+rup.getMag());
			List<Integer> sects = sol.getRupSet().getSectionsIndicesForRup(fssIndex);
			sectsRuptured.addAll(sects);
			for (int sect : sects) {
				mostRecentSectMag.put(sect, rup.getMag());
				mostRecentSectRups.put(sect, rup.getID());
			}

		} else {
			if (sphereActor == null)
				initSphereActor();

			// point source
			Location loc = rup.getHypocenterLocation();
			
			double[] pt = Transform.transformLatLonHeight(loc.getLatitude(), loc.getLongitude(), -loc.getDepth());

			double size = getSize(rup);
//			int sphereDivisions = (int)(size*6);
//			if (sphereDivisions < 5)
//				sphereDivisions = 5;
			//			if (size < 1)
			//				sphereDivisions = 5;
			//			else if (sze > 1.5)
			//				sphereDivisions = 10;
			int i;
			for (i=0; i<sphere_mag_bins.length; i++) {
				if (rup.getMag() < sphere_mag_bins[i])
					break;
			}
			Preconditions.checkState(i < sphere_mag_bins.length);

			Color color = getColorForValue(rup.getMag());
			
			int index = spherePoints[i].GetNumberOfPoints();
			spherePoints[i].InsertNextPoint(pt);
			sphereRadius[i].InsertNextTuple1(size);
			sphereColors[i].InsertNextTuple4(color.getRed(), color.getGreen(), color.getBlue(), 255);
			spherePoints[i].Modified();
			sphereRadius[i].Modified();
			sphereColors[i].Modified();
			sphereActor[i].registerPointID(index, rup);
			sphereActor[i].Modified();
//			System.out.println("Point index: "+index);
			if (point_display) {
				vertexGlyphFilter.Update();
				vertexGlyphFilter.Modified();
			}
		}
	}

	protected double getSize(ETAS_EqkRupture rup) {
		double mag = rup.getMag();
		//		return (float)Math.pow((mag/5d), 2d);
		return (mag - 2.5);
	}

	private synchronized void clearEvents() {
		sectsRuptured = new HashSet<Integer>();
		mostRecentSectMag = Maps.newHashMap();
		mostRecentSectRups = Maps.newHashMap();
		if (sphereActor != null) {
			for (int i=0; i<sphereActor.length; i++)
				actors.removeActor(sphereActor[i]);
			spherePoints = null;
			sphereRadius = null;
			sphereColors = null;
			sphereActor = null;
		}
	}

	private void loadCatalog(File catalogFile) throws IOException {
		curStep = -1;
		clearEvents();
		catalog = ETAS_CatalogIO.loadCatalog(catalogFile);


		if (sol == null)
			return;
		updateFSSIndexesInCatalog(catalog);
		File infoFile = new File(catalogFile.getParentFile(), "infoString.txt");
		if (infoFile.exists()) {
			// parse info file for any trigger events
			System.out.println("Parsing info file");
			// detect ot
			long ot = detectScenarioOT(catalog);
			List<ETAS_EqkRupture> triggerEvents = Lists.newArrayList();
			for (String line : Files.readLines(infoFile, Charset.defaultCharset())) {
				line = line.trim();
				if (line.startsWith("FSS simulation. ")) {
					line = line.substring(line.indexOf("M=")+2);
					double mag = Double.parseDouble(line.substring(0, line.indexOf(",")));
					line = line.substring(line.indexOf("ID=")+3);
					int fssIndex = Integer.parseInt(line);
					ETAS_EqkRupture rup = new ETAS_EqkRupture();
					rup.setOriginTime(ot);
					rup.setRuptureSurface(sol.getRupSet().getSurfaceForRupture(fssIndex, 1d));
					rup.setMag(mag);
					rup.setFSSIndex(fssIndex);
					rup.setID(0);
					triggerEvents.add(rup);
					System.out.println("Found fault based trigger");
				} else if (line.startsWith("Pt Source. ")) {
					line = line.substring(line.indexOf("M=")+2);
					line = line.replaceAll(",", "");
					String[] lineSplit = line.split(" ");
					Preconditions.checkState(lineSplit.length == 4);
					double mag = Double.parseDouble(lineSplit[0]);
					double lat = Double.parseDouble(lineSplit[1]);
					double lon = Double.parseDouble(lineSplit[2]);
					double dep = Double.parseDouble(lineSplit[3]);
					Location loc = new Location(lat, lon, dep);
					ETAS_EqkRupture rup = new ETAS_EqkRupture();
					rup.setPointSurface(loc);
					rup.setHypocenterLocation(loc);
					rup.setMag(mag);
					rup.setOriginTime(ot);
					rup.setID(0);
					triggerEvents.add(rup);
					System.out.println("Found point source trigger");
				}
			}
			catalog.addAll(0, triggerEvents);
		}
		// now register parent child relationships
		catalogMap = Maps.newHashMap();
		parentsMap = Maps.newHashMap();
		childrenMap = Maps.newHashMap();
		for (ETAS_EqkRupture rup : catalog) {
			catalogMap.put(rup.getID(), rup);
			Integer parentID = rup.getParentID();
			if (parentID >= 0) {
				parentsMap.put(rup.getID(), parentID);
				// now register as child
				List<Integer> children = childrenMap.get(parentID);
				if (children == null) {
					children = Lists.newArrayList();
					childrenMap.put(parentID, children);
				}
				children.add(rup.getID());
			}
			int fssIndex = rup.getFSSIndex();
			if (fssIndex >= 0)
				// set the finite fault surface for later
				rup.setRuptureSurface(sol.getRupSet().getSurfaceForRupture(fssIndex, 1d));
		}
		filterCatalog();
		fireRangeChangeEvent();
	}
	
	public static long detectScenarioOT(List<ETAS_EqkRupture> catalog) {
		return Math.round((detectScenarioYear(catalog)-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR);
	}
	
	public static int detectScenarioYear(List<ETAS_EqkRupture> catalog) {
		int detectedYear = 2014;
		if (catalog.size() > 0) {
			long catStart = catalog.get(0).getOriginTime();
			long minDiff = Long.MAX_VALUE;
			for (int year=2000; year<2050; year++) {
				long myOT = Math.round((year-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR);
				long diff = myOT - catStart;
				if (diff < 0l)
					diff = -diff;
				if (diff < minDiff) {
					minDiff = diff;
					detectedYear = year;
				}
			}
			System.out.println("Detected "+detectedYear);
		}
		return detectedYear;
	}
	
	private void filterCatalog() {
		if (catalog == null)
			return;
		double minMag = minMagParam.getValue();
		int subSectIndex = subSectParam.getAllowedStrings().indexOf(subSectParam.getValue())-1; // -1 for ALL at the top
		double duration = catDurationParam.getValue();
		if (minMag == 0d && subSectIndex < 0 && duration == 0) {
			displayCatalog = catalog;
			return;
		}
		
		System.out.println("Filtering for "+subSectParam.getValue()+" at index "+subSectIndex);
		HashSet<Integer> fssIndexes = null;
		if (subSectIndex >= 0)
			fssIndexes = new HashSet<Integer>(sol.getRupSet().getRupturesForSection(subSectIndex));
		
		long maxTime = Long.MAX_VALUE;
		if (duration > 0 && !catalog.isEmpty())
			maxTime = catalog.get(0).getOriginTime() + (long)(duration*ProbabilityModelsCalc.MILLISEC_PER_YEAR);
		
		displayCatalog = Lists.newArrayList();
		for (ETAS_EqkRupture rup : catalog) {
			if (fssIndexes != null && !fssIndexes.contains(rup.getFSSIndex()))
				continue;
			if (rup.getOriginTime() > maxTime)
				break;
			if (rup.getMag() >= minMag)
				displayCatalog.add(rup);
		}
	}

	private void updateFSSIndexesInCatalog(List<ETAS_EqkRupture> catalog) {
		if (sol == null)
			return;
		// see if the catalogs already have FSS indexes
		for (ETAS_EqkRupture rup : catalog)
			if (rup.getFSSIndex() >= 0 || rup.getGridNodeIndex() >= 0)
				return;
		if (erf == null) {
			System.out.println("Have to build ERF...");
			erf = ETAS_Launcher.buildERF(sol, false, 1d, 2012);
			erf.updateForecast();
			System.out.println("Done building ERF.");
		} else if (sol != erf.getSolution()) {
			erf.setSolution(sol);
			erf.updateForecast();
		}
		ETAS_SimAnalysisTools.loadFSSIndexesFromNth(catalog, erf);
	}

	@Override
	public void addRangeChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	@Override
	public int getNumSteps() {
		if (displayCatalog == null || sol == null)
			return 0;
		return displayCatalog.size();
	}

	@Override
	public synchronized void setCurrentStep(int step) {
		System.out.println("setCurrentStep("+step+"), cur="+curStep);
		if (step < curStep || step < 0) {
			// going backwards, clear everything
			clearEvents();
			curStep = -1;
			if (step < 0)
				return;
		}
		if (step == curStep)
			// do nothing
			return;
		if (displayCatalog != null) {
			curStep++;
			while (curStep <= step) {
				displayEvent(displayCatalog.get(curStep));
				curStep++;
			}
			MainGUI.updateRenderWindow();
		}
		curStep = step;
	}

	@Override
	public int getPreferredInitialStep() {
		if (displayCatalog == null)
			return 0;
		// show them all at the start
		return displayCatalog.size()-1;
	}

	@Override
	public boolean includeStepInLabel() {
		return false;
	}

	@Override
	public String getCurrentLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParameterList getAnimationParameters() {
		return animParams;
	}

	@Override
	public Boolean getFaultVisibility(AbstractFaultSection fault) {
		boolean contains = sectsRuptured.contains(fault.getId());
		if (contains)
			return true;
		if (hideFaultsParam.getValue())
			// doesn't contain, and we're hiding
			return false;
		// doesn't contain and we're not hiding
		return null;
	}

	@Override
	public FaultColorer getFaultColorer() {
		return this;
	}

	@Override
	public void fireRangeChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener l : listeners)
			l.stateChanged(e);
	}

	@Override
	public String getName() {
		return "ETAS Catalog Animation";
	}

	@Override
	public void actorPicked(PickEnabledActor<AbstractFaultSection> actor, AbstractFaultSection fault,
			vtkCellPicker picker, MouseEvent e) {
		Integer sectID = mostRecentSectRups.get(fault.getId());
		if (sectID == null)
			return;
		ETAS_EqkRupture rup = catalogMap.get(sectID);
		if (rup == null)
			return;

		String s = fault.getInfo();
		s = s.replaceAll("\n", ", ");
		String addition;
		if (rup.getParentID() >= 0 && catalogMap.get(rup.getParentID()) != null)
			addition = "Mag: "+(float)rup.getMag()+", Parent="+rup.getParentID()
			+" (Mag="+(float)catalogMap.get(rup.getParentID()).getMag()+")";
		else
			addition = "Mag: "+(float)rup.getMag()+", Spontaneous";
		int numChildren = 0;
		if (childrenMap.get(rup.getID()) != null)
			numChildren = childrenMap.get(rup.getID()).size();
		addition += ", "+numChildren+" children";
		s = addition+", "+s;
//		Geo3dInfo.getMainWindow().setMessage(s);
		System.out.println(s);

		if (e.isShiftDown()) {
			if (e.isControlDown()) {
				// move up the chain, show the parent
				Integer parentID = parentsMap.get(rup.getID());
				if (parentID == null) {
					System.out.println("Parent ID not found, spontaneous");
					clearChildren();
					return;
				}
				rup = catalogMap.get(parentID);
				if (rup == null) {
					System.out.println("Parent rup not found for ID?");
					clearChildren();
					return;
				}
			}
			displayChildren(rup);
		}
	}
	
	private void etasSphereActorPicked(PickEnabledActor<ETAS_EqkRupture> actor, ETAS_EqkRupture rup,
			vtkCellPicker picker, MouseEvent e) {
		Preconditions.checkNotNull(rup, "Picked but no rup registered for PointID=%s", picker.GetPointId());
		String s;
		if (rup.getParentID() >= 0 && catalogMap.get(rup.getParentID()) != null)
			s = "Mag: "+(float)rup.getMag()+", Parent="+rup.getParentID()
			+" (Mag="+(float)catalogMap.get(rup.getParentID()).getMag()+")";
		else
			s = "Mag: "+(float)rup.getMag()+", Spontaneous";
		int numChildren = 0;
		if (childrenMap.get(rup.getID()) != null)
			numChildren = childrenMap.get(rup.getID()).size();
		s += ", "+numChildren+" children";
//		Geo3dInfo.getMainWindow().setMessage(s);
		System.out.println(s);

		if (e.isShiftDown()) {
			if (e.isControlDown()) {
				// move up the chain, show the parent
				Integer parentID = parentsMap.get(rup.getID());
				if (parentID == null) {
					System.out.println("Parent ID not found, spontaneous");
					clearChildren();
					return;
				}
				rup = catalogMap.get(parentID);
				if (rup == null) {
					System.out.println("Parent rup not found for ID?");
					clearChildren();
					return;
				}
			}
			displayChildren(rup);
		}
	}

//	@Override
//	public void nothingPicked(MouseEvent mouseEvent) {
//		if (mouseEvent.isShiftDown())
//			clearChildren();
//		Geo3dInfo.getMainWindow().setMessageDefault();
//	}

	private List<Location> getSurfaceLocs(ETAS_EqkRupture rup) {
		if (rup.getRuptureSurface() == null)
			return Lists.newArrayList(rup.getHypocenterLocation());
		return rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
	}

	private Location[] getConnectionLocations(ETAS_EqkRupture rup1, ETAS_EqkRupture rup2) {
		Location[] closestPts = null;
		double closestDist = Double.POSITIVE_INFINITY;
		for (Location pt1 : getSurfaceLocs(rup1)) {
			for (Location pt2 : getSurfaceLocs(rup2)) {
				double dist = LocationUtils.linearDistanceFast(pt1, pt2);
				if (dist < closestDist) {
					closestDist = dist;
					closestPts = new Location[] {pt1, pt2};
				}
			}
		}

		return closestPts;
	}

	private void clearChildren() {
		// TODO
//		if (connectionBG != null)
//			connectionBG.removeAllChildren();
	}

	private void displayChildren(ETAS_EqkRupture rup) {
		// TODO
//		if (connectionBG == null) {
//			connectionBG = createBranchGroup();
//			Geo3d.getMainWindow().getCorrectAnimationBG().addChild(connectionBG);
//		} else {
//			connectionBG.removeAllChildren();
//		}
//
//		float thickness = 2f;
//		Color c = Color.GRAY;
//
//		List<Integer> children = childrenMap.get(rup.getID());
//		if (children == null)
//			// some events have no children
//			return;
//
//		for (int childID : children) {
//			ETAS_EqkRupture child = catalogMap.get(childID);
//			if (child.getMag() < minMagParam.getValue())
//				continue;
//			Preconditions.checkNotNull(child, "Child not found");
//
//			Location[] locs = getConnectionLocations(rup, child);
//			Location p1 = locs[0];
//			Location p2 = locs[1];
//
//			LineArray la = new LineArray(2, LineArray.COORDINATES);
//			la.setCapability(LineArray.ALLOW_COLOR_READ);
//			la.setCapability(LineArray.ALLOW_COLOR_WRITE);
//			la.setCapability(LineArray.ALLOW_COORDINATE_READ);
//			la.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
//			la.setCapability(LineArray.ALLOW_COUNT_READ);
//
//			la.setCoordinate(0, LatLongToPoint.plotPoint(p1.getLatitude(), p1.getLongitude(), -p1.getDepth()));
//			la.setCoordinate(1, LatLongToPoint.plotPoint(p2.getLatitude(), p2.getLongitude(), -p2.getDepth()));
//
//			Appearance app = new Appearance();
//			ColoringAttributes ca = new ColoringAttributes(new Color3f(), ColoringAttributes.SHADE_FLAT);
//			ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
//			ca.setColor(new Color3f(c));
//			app.setColoringAttributes(ca);
//			LineAttributes latt = new LineAttributes();
//			latt.setLineWidth(thickness);
//			app.setLineAttributes(latt);
//
//			Shape3D shp = new Shape3D(la, app);
//
//			BranchGroup bg = new BranchGroup();
//			bg.setCapability(BranchGroup.ALLOW_DETACH);
//			bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
//
//			bg.addChild(shp);
//
//			connectionBG.addChild(bg);
//		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == catalogFileParam) {
			try {
				loadCatalog(catalogFileParam.getValue());
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error Loading Catalog", JOptionPane.ERROR_MESSAGE);
				clearEvents();
				catalog = null;
			}
		} else if (event.getParameter() == hideFaultsParam) {
			//			fireRangeChangeEvent();
		} else if (event.getParameter() == minMagParam || event.getParameter() == subSectParam
				|| event.getParameter() == catDurationParam) {
			filterCatalog();
			fireRangeChangeEvent();
			setCurrentStep(getNumSteps()-1);
		} else if (event.getParameter() == opacityParam || event.getParameter() == onlyCurrentParam) {
			forceRedraw();
		}
	}
	
	private void forceRedraw() {
		int origCurStep = curStep;
		curStep++;
		setCurrentStep(origCurStep);
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		clearEvents();
		this.sol = sol;
		ArrayList<String> subSectNames = Lists.newArrayList(ALL_SUB_SECTS);
		if (sol != null) {
			for (FaultSection sect : sol.getRupSet().getFaultSectionDataList())
				subSectNames.add(sect.getSectionId()+". "+sect.getName());
		}
		((StringConstraint)subSectParam.getConstraint()).setStrings(subSectNames);
		subSectParam.setValue(ALL_SUB_SECTS);
		subSectParam.getEditor().refreshParamEditor();
	}

	@Override
	public double getTimeForStep(int step) {
		if (step >= 0 && displayCatalog != null && step < displayCatalog.size()) {
//			long catStart = displayCatalog.get(0).getOriginTime();
			long catStart = catalog.get(0).getOriginTime();
			long curTime = displayCatalog.get(step).getOriginTime();
			long diff = curTime - catStart;
			Preconditions.checkState(diff >= 0, "Bad time? diff=%s, start=%s, cur=%s, step=%s", diff, catStart, curTime, step);
			double secs = (double)diff/1000d;
			return secs;
		}
		return 0;
	}

	//TODO
	//	@Override
	//	public Color getColor(AbstractFaultSection fault) {
	//		if (sectsRuptured != null && sectsRuptured.contains(fault.getId()))
	//			return Color.RED;
	//		return Color.GRAY;
	//	}

	@Override
	public ParameterList getColorerParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setColorerChangeListener(ColorerChangeListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getStepForID(int id) {
		for (int i=0; i<displayCatalog.size(); i++) {
			ETAS_EqkRupture rup = displayCatalog.get(i);
			if (rup.getID() == id)
				return i;
		}
		return -1;
	}

	@Override
	public int getIDForStep(int step) {
		if (displayCatalog == null || step >= displayCatalog.size())
			return -1;
		return displayCatalog.get(step).getID();
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		if (sectsRuptured != null && sectsRuptured.contains(fault.getId()))
			return mostRecentSectMag.get(fault.getId());
		return Double.NaN;
	}

	@Override
	public double getCurrentDuration() {
		if (displayCatalog != null)
			return getTimeForStep(displayCatalog.size()-1) - getTimeForStep(0);
		return 0;
	}

	@Override
	public boolean timeChanged(double time) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static void main(String[] args) throws IOException {
		ETASCatalogAnim anim = new ETASCatalogAnim(null);
		FaultSystemSolution sol = FaultSystemSolution.load(new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/"
				+ "InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_SpatSeisU3_MEAN_BRANCH_AVG_SOL.zip"));
		anim.setRupSet(sol.getRupSet(), sol);
		anim.catalogFileParam.setValue(new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/"
				+ "2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14/"
				+ "selected_catalogs/fract_1.0_cat1/simulatedEvents.txt"));
		System.out.println("Num events: "+anim.displayCatalog.size());
		for (int i=0; i<100; i++)
			System.out.println("Time "+i+": "+anim.getTimeForStep(i));
//		System.out.println("Time 1: "+anim.getTimeForStep(1));
	}
}