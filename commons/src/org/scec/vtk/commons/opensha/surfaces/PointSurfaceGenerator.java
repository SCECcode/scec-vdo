package org.scec.vtk.commons.opensha.surfaces;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.surfaces.params.ColorParameter;
import org.scec.vtk.commons.opensha.surfaces.params.DiscreteSizeParam;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PointPickEnabledActor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import vtk.vtkDataSetMapper;
import vtk.vtkPoints;
import vtk.vtkUnsignedCharArray;
import vtk.vtkUnstructuredGrid;
import vtk.vtkVertex;

public class PointSurfaceGenerator extends GeometryGenerator implements ParameterChangeListener {
	
	private static final String NAME = "Points";

	private ParameterList faultDisplayParams;

	private static final String OUTLINE_COLOR_PARAM_NAME = "Outline Color";
	private ColorParameter outlineColorParam = new ColorParameter(OUTLINE_COLOR_PARAM_NAME, Color.WHITE);

	public static final String POINT_SIZE_PARAM_NAME = "Point Size";
	private DiscreteSizeParam pointSizeParam = new DiscreteSizeParam(POINT_SIZE_PARAM_NAME, 1d, 10d, 1d);
	
	private enum SurfaceType {
		SOLID("Solid"),
		SOLID_WITH_OUTLINE("Solid w/ Outline"),
		OUTLINE_ONLY("Outline Only"),
		TRACE_ONLY("Trace Only");
		
		private String name;
		private SurfaceType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}

	private static final String SURFACE_TYPE_PARAM_NAME = "Surface Type";
	private static final SurfaceType SURFACE_TYPE_DEFAULT = SurfaceType.SOLID;
	private EnumParameter<SurfaceType> surfaceTypeParam;

	public PointSurfaceGenerator() {
		super(NAME);
		
		faultDisplayParams = new ParameterList();

		surfaceTypeParam = new EnumParameter<SurfaceType>(
				SURFACE_TYPE_PARAM_NAME, EnumSet.allOf(SurfaceType.class), SURFACE_TYPE_DEFAULT, null);
		faultDisplayParams.addParameter(surfaceTypeParam);
		surfaceTypeParam.addParameterChangeListener(this);

		faultDisplayParams.addParameter(outlineColorParam);
		outlineColorParam.addParameterChangeListener(this);

		faultDisplayParams.addParameter(pointSizeParam);
		pointSizeParam.addParameterChangeListener(this);
		
		enableForSurfaceType();
	}

	private boolean shouldUseOutlineColor() {
		return surfaceTypeParam.getValue() == SurfaceType.SOLID_WITH_OUTLINE;
	}

	private void enableForSurfaceType() {
		outlineColorParam.getEditor().setEnabled(shouldUseOutlineColor());
	}
	
	private boolean isSolid() {
		return surfaceTypeParam.getValue() == SurfaceType.SOLID;
	}
	
	@Override
	public FaultSectionActorList createFaultActors(Surface3D surface, Color color, AbstractFaultSection fault) {
//		System.out.println("Building surface of type: "+surface.getClass().getName());
		if (surface instanceof CompoundSurface)
			return handleCompound((CompoundSurface)surface, color, fault);
		if (surface instanceof EvenlyGriddedSurface)
			return createFaultActors((EvenlyGriddedSurface)surface, color, fault);
		
		// generic approach
		
		List<double[]> points = new ArrayList<>();
		Iterator<Location> it = surface.getLocationsIterator();
		while(it.hasNext()){
			Location loc = (Location)it.next();
			points.add(getPointForLoc(loc));
		}
		Preconditions.checkState(!points.isEmpty(), "NumSurfacePoints must be >0");
		
		double initialOpacity;
		FaultActorBundle currentBundle;
		if (isBundlerEnabled() && bundler != null) {
			// initialized to transparent, will get updated when displayed
			initialOpacity = 0;
			currentBundle = bundler.getBundle(fault);
		} else {
			initialOpacity = 255;
			currentBundle = null;
		}
		
		boolean bundle = isBundlerEnabled() && currentBundle != null;
		
		vtkUnstructuredGrid gridData;
		vtkPoints pts;
		vtkUnsignedCharArray colors;
		PickEnabledActor<AbstractFaultSection> actor;
		boolean newBundle = currentBundle == null || !currentBundle.isInitialized();
		
		Object synchOn = this;
		if (newBundle) {
			gridData = new vtkUnstructuredGrid();
			pts = new vtkPoints();
			if (bundle) {
				colors = new vtkUnsignedCharArray();
				colors.SetNumberOfComponents(4);
				colors.SetName("Colors");
			} else {
				colors = null;
			}
			
			if (bundle) {
				PointPickEnabledActor<AbstractFaultSection> myActor =
						new PointPickEnabledActor<AbstractFaultSection>(getPickHandler());
				actor = myActor;
				currentBundle.initialize(myActor, gridData, pts, colors, null);
				synchOn = currentBundle;
			} else {
				actor = new PickEnabledActor<AbstractFaultSection>(getPickHandler(), fault);
			}
		} else {
			gridData = (vtkUnstructuredGrid) currentBundle.getVtkDataSet();
			pts = currentBundle.getPoints();
			colors = currentBundle.getColorArray();
			Preconditions.checkState(colors.GetNumberOfComponents() == 4);
			
			actor = currentBundle.getActor();
		}
		
		int firstIndex;
		synchronized (synchOn) {
			firstIndex = (int) pts.GetNumberOfPoints();
			
			for (double[] point : points) {
				pts.InsertNextPoint(point);
				if (bundle)
					colors.InsertNextTuple4(color.getRed(), color.getGreen(), color.getBlue(), initialOpacity);
			}
			
			for (int i=firstIndex; i<pts.GetNumberOfPoints(); i++) {
				vtkVertex vertex = new vtkVertex();
				
				vertex.GetPointIds().SetId(0, i);
				gridData.InsertNextCell(vertex.GetCellType(), vertex.GetPointIds());
			}
			
			if (newBundle) {
				// new bundle
				gridData.SetPoints(pts);
				if (bundle)
					gridData.GetPointData().AddArray(colors);
				
				vtkDataSetMapper mapper = new vtkDataSetMapper();
				mapper.SetInputData(gridData);
				if (bundle) {
					mapper.ScalarVisibilityOn();
					mapper.SetScalarModeToUsePointFieldData();
					mapper.SelectColorArray("Colors");
				}
				
				actor.SetMapper(mapper);
				actor.GetProperty().SetPointSize(pointSizeParam.getValue().floatValue());
				setActorProperties(actor, bundle, color, 1d);
			} else {
				if (currentBundle != null)
					currentBundle.modified();
			}
		}

		FaultSectionActorList list;
		if (bundle) {
			Preconditions.checkState(pts.GetNumberOfPoints() == colors.GetNumberOfTuples());
			int totNumPoints = (int) (pts.GetNumberOfPoints()-firstIndex);
			list = new FaultSectionBundledActorList(fault, currentBundle, firstIndex, totNumPoints, totNumPoints, 255);
		} else {
			list = new FaultSectionActorList(fault);
			list.add(actor);
		}
		
		return list;
	}
	
	private List<double[]> getPointsForLocs(LocationList locs, LocationList excludeLocs) {
		ArrayList<double[]> pts = new ArrayList<double[]>();
		HashSet<Location> excludes;
		if (excludeLocs == null)
			excludes = null;
		else
			excludes = new HashSet<Location>(excludeLocs);
		
		for (Location loc : locs) {
			if (excludes != null && excludes.contains(loc))
				continue;
			pts.add(getPointForLoc(loc));
		}
		
		return pts;
	}

	private FaultSectionActorList createFaultActors(EvenlyGriddedSurface surface, Color color, AbstractFaultSection fault) {
		int numSurfacePoints = (int) surface.size();
//		System.out.println("Building for evenly gridded with "+numSurfacePoints+" points");
		
		Preconditions.checkState(numSurfacePoints>0, "NumSurfacePoints must be >0");

		double[][] points = new double[numSurfacePoints][];
		int cnt = 0;
		Iterator<Location> it = surface.getLocationsIterator();
		while(it.hasNext()){
			Location loc = (Location)it.next();
			points[cnt++] = getPointForLoc(loc);
		}
		// make points array
		List<double[]> center = null;
		List<double[]> top = null;
		List<double[]> bottom = null;
		List<double[]> left = null;
		List<double[]> right = null;

		int rows = surface.getNumRows();
		int cols = surface.getNumCols();

		SurfaceType surfaceType = surfaceTypeParam.getValue();

		boolean solid = isSolid();
		boolean drawCenter = solid || surfaceType == SurfaceType.SOLID_WITH_OUTLINE;
		boolean drawSideBottoms = surfaceType == SurfaceType.SOLID_WITH_OUTLINE || surfaceType == SurfaceType.OUTLINE_ONLY;
		cnt = 0;
		int n = points.length;
		
		if (drawCenter)
			center = Lists.newArrayList();
		if (!solid)
			top = Lists.newArrayList();
		if (drawSideBottoms) {
			left = Lists.newArrayList();
			bottom = Lists.newArrayList();
			right = Lists.newArrayList();
		}
		
		while(cnt<n){
			if (!solid) {
				if (cnt < cols) {
					// we always draw the top if it's not solid
					top.add(points[cnt]);
					cnt++;
					continue;
				}
				if (cnt % cols == 0) {
					if (drawSideBottoms) {
						right.add(points[cnt]);
					}
					cnt++;
					continue;
				}
				if ((cnt + 1) % cols == 0) {
					if (drawSideBottoms) {
						left.add(points[cnt]);
					}
					cnt++;
					continue;
				}
				if (cnt > (n - cols) && bottom != null) {
					if (drawSideBottoms) {
						bottom.add(points[cnt]);
					}
					cnt++;
					continue;
				}
			}
			if (drawCenter && center != null) {
				center.add(points[cnt]);
			}
			cnt++;
		}
		
		double initialOpacity;
		FaultActorBundle currentBundle;
		if (isBundlerEnabled() && bundler != null) {
			// initialized to transparent, will get updated when displayed
			initialOpacity = 0;
			currentBundle = bundler.getBundle(fault);
		} else {
			initialOpacity = 255;
			currentBundle = null;
		}
		
		boolean bundle = isBundlerEnabled() && currentBundle != null;
		
		vtkUnstructuredGrid gridData;
		vtkPoints pts;
		vtkUnsignedCharArray colors;
		PickEnabledActor<AbstractFaultSection> actor;
		boolean newBundle = currentBundle == null || !currentBundle.isInitialized();
		
		Object synchOn = this;
		if (newBundle) {
			gridData = new vtkUnstructuredGrid();
			pts = new vtkPoints();
			if (bundle) {
				colors = new vtkUnsignedCharArray();
				colors.SetNumberOfComponents(4);
				colors.SetName("Colors");
			} else {
				colors = null;
			}
			
			if (bundle) {
				PointPickEnabledActor<AbstractFaultSection> myActor =
						new PointPickEnabledActor<AbstractFaultSection>(getPickHandler());
				actor = myActor;
				currentBundle.initialize(myActor, gridData, pts, colors, null);
				synchOn = currentBundle;
			} else {
				actor = new PickEnabledActor<AbstractFaultSection>(getPickHandler(), fault);
			}
		} else {
			gridData = (vtkUnstructuredGrid) currentBundle.getVtkDataSet();
			pts = currentBundle.getPoints();
			colors = currentBundle.getColorArray();
			Preconditions.checkState(colors.GetNumberOfComponents() == 4);
			
			actor = currentBundle.getActor();
		}
		
		List<double[]> outlinePoints = null;
		if (!solid) {
			outlinePoints = new ArrayList<>();
			outlinePoints.addAll(top);
			if (drawSideBottoms) {
				outlinePoints.addAll(left);
				outlinePoints.addAll(bottom);
				outlinePoints.addAll(right);
			}
		}
		
		int firstIndex;
		int numMainColor = 0;
		synchronized (synchOn) {
			firstIndex = (int) pts.GetNumberOfPoints();
			
			if (drawCenter) {
				for (double[] point : center) {
					pts.InsertNextPoint(point);
					numMainColor++;
					if (bundle)
						colors.InsertNextTuple4(color.getRed(), color.getGreen(), color.getBlue(), initialOpacity);
				}
				if (!solid) {
					Color outlineColor = outlineColorParam.getValue();
					// top or outline but drawn as the outline color
					// don't increment numMainColor here
					for (double[] point : outlinePoints) {
						pts.InsertNextPoint(point);
						if (bundle)
							colors.InsertNextTuple4(
									outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), initialOpacity);
					}
				}
			} else {
				// top or outline but drawn as the main color
				for (double[] point : outlinePoints) {
					pts.InsertNextPoint(point);
					numMainColor++;
					if (bundle)
						colors.InsertNextTuple4(color.getRed(), color.getGreen(), color.getBlue(), initialOpacity);
				}
			}
			
			for (int i=firstIndex; i<pts.GetNumberOfPoints(); i++) {
				vtkVertex vertex = new vtkVertex();
				
				vertex.GetPointIds().SetId(0, i);
				gridData.InsertNextCell(vertex.GetCellType(), vertex.GetPointIds());
			}
			
			if (newBundle) {
				// new bundle
				gridData.SetPoints(pts);
				if (bundle)
					gridData.GetPointData().AddArray(colors);
				
				vtkDataSetMapper mapper = new vtkDataSetMapper();
				mapper.SetInputData(gridData);
				if (bundle) {
					mapper.ScalarVisibilityOn();
					mapper.SetScalarModeToUsePointFieldData();
					mapper.SelectColorArray("Colors");
				}
				
				actor.SetMapper(mapper);
				actor.GetProperty().SetPointSize(pointSizeParam.getValue().floatValue());
				setActorProperties(actor, bundle, color, 1d);
			} else {
				if (currentBundle != null)
					currentBundle.modified();
			}
		}

		FaultSectionActorList list;
		if (bundle) {
			Preconditions.checkState(pts.GetNumberOfPoints() == colors.GetNumberOfTuples());
			int totNumPoints = (int) (pts.GetNumberOfPoints()-firstIndex);
//			System.out.println("Adding "+totNumPoints+" points");
			list = new FaultSectionBundledActorList(fault, currentBundle, firstIndex, totNumPoints, numMainColor, 255);
		} else {
			list = new FaultSectionActorList(fault);
			list.add(actor);
		}
		
		return list;
	}

	@Override
	public ParameterList getDisplayParams() {
		return faultDisplayParams;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getSource() == surfaceTypeParam) {
			enableForSurfaceType();
		}
		super.firePlotSettingsChangeEvent();
	}

}
