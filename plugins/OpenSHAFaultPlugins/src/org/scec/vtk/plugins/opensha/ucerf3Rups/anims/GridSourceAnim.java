package org.scec.vtk.plugins.opensha.ucerf3Rups.anims;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemSolution;
import org.opensha.sha.earthquake.faultSysSolution.modules.GridSourceList;
import org.opensha.sha.earthquake.faultSysSolution.modules.GridSourceList.GriddedRupture;
import org.opensha.sha.earthquake.faultSysSolution.modules.GridSourceProvider;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.faultSurface.PointSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.anim.IDBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.surfaces.FaultSectionActorList;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.commons.opensha.surfaces.LineSurfaceGenerator;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;

import com.google.common.base.Preconditions;

import vtk.vtkActor;
import vtk.vtkDataSetMapper;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnstructuredGrid;
import vtk.vtkVertexGlyphFilter;

public class GridSourceAnim implements IDBasedFaultAnimation, UCERF3RupSetChangeListener, ParameterChangeListener {
	
	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	private FaultSystemSolution sol;
	private boolean initialized = false;
	
	private GridSourceProvider gridProv;
	private int totNumRups;
	private int[] sourceRupIndexes;
	private ProbEqkSource[] gridSources;
	
	private int curStep = -1;
	private int curSourceIndex = -1;
	private vtkActor locActor;
	private vtkActor surfActor;
	private ProbEqkSource curSource;
	private ProbEqkRupture curRupture;
	
	private BackgroundRupType bgType = BackgroundRupType.POINT;
	private BackgroundRupParam bgTypeParam;
	private ParameterList params;

	private PluginActors actors;
	
	private GeometryGenerator geomGen;
	
	public GridSourceAnim(PluginActors actors) {
		this.actors = actors;
		params = new ParameterList();
		
		bgTypeParam = new BackgroundRupParam();
		bgTypeParam.setValue(bgType);
		bgTypeParam.addParameterChangeListener(this);
		params.addParameter(bgTypeParam);
	}

	@Override
	public String getName() {
		return "Grid Sources";
	}

	@Override
	public synchronized void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.sol = sol;
		this.initialized = false;
		
		fireRangeChangeEvent();
	}
	
	private void checkInit() {
		if (initialized)
			return;
		synchronized (this) {
			if (initialized)
				return;
			this.gridProv = sol == null ? null : sol.getGridSourceProvider();
			if (this.gridProv == null) {
				this.totNumRups = 0;
				this.sourceRupIndexes = null;
			} else {
				int numSources = gridProv.getNumSources();
				sourceRupIndexes = new int[numSources];
				gridSources = new ProbEqkSource[numSources];
				this.totNumRups = 0;
				for (int sourceIndex=0; sourceIndex<numSources; sourceIndex++) {
					sourceRupIndexes[sourceIndex] = totNumRups;
					gridSources[sourceIndex] = gridProv.getSource(sourceIndex, 1d, null, bgType);
					totNumRups += gridSources[sourceIndex].getNumRuptures();
				}
			}
		}
	}

	@Override
	public void addRangeChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	@Override
	public int getNumSteps() {
		checkInit();
		if (gridProv instanceof GridSourceList)
			return totNumRups+2;
		return totNumRups+1;
	}

	@Override
	public synchronized void setCurrentStep(int step) {
		if (step <= 0 || sol == null) {
			if (locActor != null || surfActor != null) {
				if (locActor != null) {
					actors.removeActor(locActor);
					locActor = null;
				}
				if (surfActor != null) {
					actors.removeActor(surfActor);
					surfActor = null;
				}
				MainGUI.updateRenderWindow();
			}
			curRupture = null;
			return;
		}
		checkInit();
		
		if (step == curStep)
			return;
		
		curStep = step;
		
		if (locActor != null) {
			actors.removeActor(locActor);
			locActor = null;
		}
		if (surfActor != null) {
			actors.removeActor(surfActor);
			surfActor = null;
		}
		
		int index = getIDForStep(step);
		if (index == totNumRups) {
			// special case to show them all
			LocationList uniqueLocs = new LocationList();
			Preconditions.checkState(gridProv instanceof GridSourceList);
			GridSourceList gridSources = (GridSourceList)gridProv;
			for (int gridIndex=0; gridIndex<gridSources.getNumLocations(); gridIndex++) {
				Location loc = gridSources.getLocation(gridIndex);
				HashSet<Float> uniqueDepths =new HashSet<>();
				for (GriddedRupture rup : gridSources.getRuptures(null, gridIndex)) {
					double depth;
					if (Double.isFinite(rup.properties.hypocentralDepth))
						depth = rup.properties.hypocentralDepth;
					else
						depth = rup.properties.upperDepth + 0.5*(rup.properties.lowerDepth-rup.properties.upperDepth);
					if (!uniqueDepths.contains((float)depth)) {
						uniqueDepths.add((float)depth);
						uniqueLocs.add(new Location(loc.lat, loc.lon, depth));
					}
				}
			}
			System.out.println("Displaying "+uniqueLocs.size()+" unique locations (gridProv.getNumLocations()="+gridProv.getNumLocations()+")");
			vtkPoints pts = new vtkPoints();
//			vtkUnsignedCharArray colors;
			
			for (Location loc : uniqueLocs)
				pts.InsertNextPoint(GeometryGenerator.getPointForLoc(loc));
//			gridData.SetPoints(pts);
			vtkDataSetMapper mapper = new vtkDataSetMapper();
//			mapper.SetInputData(gridData);
			
			vtkPolyData polydata =new vtkPolyData();
			polydata.SetPoints(pts);
			vtkVertexGlyphFilter vertexGlyphFilter = new vtkVertexGlyphFilter();
			vertexGlyphFilter.AddInputData(polydata);
			vertexGlyphFilter.Update();
			
			mapper.SetInputConnection(vertexGlyphFilter.GetOutputPort());
			
			locActor = new vtkActor();
			locActor.SetMapper(mapper);
//			curActor.GetProperty().SetPointSize(pointSizeParam.getValue());
			locActor.GetProperty().SetPointSize(3f);
			locActor.GetProperty().SetColor(GeometryGenerator.getColorDoubleArray(Color.GREEN.darker()));
			locActor.GetProperty().SetOpacity(1d);
			locActor.Modified();
			actors.addActor(locActor);
		} else {
			Preconditions.checkState(index < totNumRups, "Bad index=%s for step=%s, totNumRups=%s", index, step, totNumRups);
			ProbEqkRupture rupture = null;
			if (curSourceIndex >= 0 && index >= sourceRupIndexes[curSourceIndex]) {
				// see if it's the same or nexmyNumt source
				int delta = index - sourceRupIndexes[curSourceIndex];
				int myNum = gridSources[curSourceIndex].getNumRuptures();
				if (delta < myNum) {
					// still within the same source
					rupture = gridSources[curSourceIndex].getRupture(delta);
				} else if (delta == myNum) {
					rupture = gridSources[curSourceIndex+1].getRupture(0);
					curSourceIndex++;
				}
			}
			if (rupture == null) {
				// need to do a search
				curSourceIndex = Arrays.binarySearch(sourceRupIndexes, index);
				if (curSourceIndex < 0) {
					// this is an "insertion index"
					curSourceIndex = -(curSourceIndex + 1);
					// we actually need one minus that
					curSourceIndex--;
				}
				int rupIndex = index - sourceRupIndexes[curSourceIndex];
				Preconditions.checkState(rupIndex >= 0, "rupIndex=%s, curStep=%s, sourceRupIndexes[%s] = %s",
						rupIndex, index, curSourceIndex, sourceRupIndexes[curSourceIndex]);
				rupture = gridSources[curSourceIndex].getRupture(rupIndex);
			}
			curSource =  gridSources[curSourceIndex];
			curRupture = rupture;
			
			RuptureSurface surf = rupture.getRuptureSurface();
			System.out.println("Displaying M"+(rupture.getMag())+" with surf type: "+surf.getClass().getName());
			
			Location pointLoc = null;
			if (surf instanceof PointSurface) {
				pointLoc = ((PointSurface)surf).getLocation();
			} else {
				pointLoc = rupture.getHypocenterLocation();
				
				if (geomGen == null)
					geomGen = new LineSurfaceGenerator();
				FaultSectionActorList list = geomGen.createFaultActors(surf, Color.RED, null);
				Preconditions.checkState(list.size() == 1, "Expected a single actor");
				surfActor = list.get(0);
				surfActor.Modified();
				actors.addActor(surfActor);
				System.out.println("\tDisplaying finite surface with upperDepth="+surf.getAveRupTopDepth()
					+", width="+surf.getAveWidth()+", length="+surf.getAveLength()+", area="+surf.getArea());
			}
			
			if (pointLoc != null) {
				System.out.println("Displaying loc at");
				if (gridProv.getGriddedRegion() != null)
					System.out.println("\tDisplaying location ("+pointLoc+") at gridIndex="+gridProv.getGriddedRegion().indexForLocation(pointLoc));
				else
					System.out.println("\tDisplaying location: "+pointLoc);
				double[] point = GeometryGenerator.getPointForLoc(pointLoc);
//				vtkUnstructuredGrid gridData = new vtkUnstructuredGrid();
				vtkPoints pts = new vtkPoints();
//				vtkUnsignedCharArray colors;
				
				pts.InsertNextPoint(point);
//				gridData.SetPoints(pts);
				vtkDataSetMapper mapper = new vtkDataSetMapper();
//				mapper.SetInputData(gridData);
				
				vtkPolyData polydata =new vtkPolyData();
				polydata.SetPoints(pts);
				vtkVertexGlyphFilter vertexGlyphFilter = new vtkVertexGlyphFilter();
				vertexGlyphFilter.AddInputData(polydata);
				vertexGlyphFilter.Update();
				
				mapper.SetInputConnection(vertexGlyphFilter.GetOutputPort());
				
				locActor = new vtkActor();
				locActor.SetMapper(mapper);
//				curActor.GetProperty().SetPointSize(pointSizeParam.getValue());
				locActor.GetProperty().SetPointSize(5f);
				locActor.GetProperty().SetColor(GeometryGenerator.getColorDoubleArray(surfActor == null ? Color.RED : Color.BLUE));
				locActor.GetProperty().SetOpacity(1d);
				locActor.Modified();
				actors.addActor(locActor);
			}
		}
		

		MainGUI.updateRenderWindow();
	}

	@Override
	public int getPreferredInitialStep() {
		return 0;
	}

	@Override
	public boolean includeStepInLabel() {
		return false;
	}

	@Override
	public String getCurrentLabel() {
		if (curRupture == null)
			return "";
		String str = "M"+(float)curRupture.getMag()+", rake="+(float)curRupture.getAveRake()+", trt="+curSource.getTectonicRegionType().name();
		if (!Double.isNaN(curRupture.getRuptureSurface().getAveStrike()))
			str += ", strike="+(int)(curRupture.getRuptureSurface().getAveStrike()+0.5);
		if (curRupture.getHypocenterLocation() != null)
			str += ", hypo="+curRupture.getHypocenterLocation();
		return str;
	}

	@Override
	public ParameterList getAnimationParameters() {
		return params;
	}

	@Override
	public Boolean getFaultVisibility(AbstractFaultSection fault) {
		return null;
	}

	@Override
	public FaultColorer getFaultColorer() {
		return new FaultColorer() {
			
			@Override
			public String getName() {
				return GridSourceAnim.this.getName();
			}
			
			@Override
			public void setColorerChangeListener(ColorerChangeListener l) {
			}
			
			@Override
			public String getLegendLabel() {
				return null;
			}
			
			@Override
			public ParameterList getColorerParameters() {
				return null;
			}
			
			@Override
			public Color getColor(AbstractFaultSection fault) {
				return Color.GRAY;
			}
		};
	}

	@Override
	public void fireRangeChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener l : listeners)
			l.stateChanged(e);
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		fireRangeChangeEvent();
	}

	@Override
	public int getStepForID(int id) {
		return id+1;
	}

	@Override
	public int getIDForStep(int step) {
		return step-1;
	}

}
