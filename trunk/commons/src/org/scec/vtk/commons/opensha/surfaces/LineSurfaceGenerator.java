package org.scec.vtk.commons.opensha.surfaces;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opensha.commons.data.Container2DImpl;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.surfaces.params.DiscreteSizeParam;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PointPickEnabledActor;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkUnsignedCharArray;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class LineSurfaceGenerator extends GeometryGenerator implements ParameterChangeListener {

	public static final String NAME = "Lines";
	
	private ParameterList faultDisplayParams;
	
	private boolean bundle = true;

	private static final String LINE_SIZE_PARAM_NAME = "Line Size";
	private DiscreteSizeParam lineSizeParam = new DiscreteSizeParam(LINE_SIZE_PARAM_NAME, 1d, 10d, 1d);

	private static final String SURFACE_TYPE_PARAM_NAME = "Surface Type";
	private static final String SURFACE_TYPE_SOLID = "All Lines";
	private static final String SURFACE_TYPE_OUTLINE_ONLY = "Outline Only";
	private static final String SURFACE_TYPE_TRACE_ONLY = "Trace Only";
	private static final String SURFACE_TYPE_DEFAULT = SURFACE_TYPE_SOLID;
	private StringParameter surfaceTypeParam;

	private static ArrayList<String> getAllSurfaceTypes() {
		ArrayList<String> surfaceTypes = new ArrayList<String>();
		surfaceTypes.add(SURFACE_TYPE_SOLID);
		surfaceTypes.add(SURFACE_TYPE_OUTLINE_ONLY);
		surfaceTypes.add(SURFACE_TYPE_TRACE_ONLY);
		return surfaceTypes;
	}

	public LineSurfaceGenerator() {
		super(NAME);
		
		faultDisplayParams = new ParameterList();

		surfaceTypeParam = new StringParameter(SURFACE_TYPE_PARAM_NAME, getAllSurfaceTypes(), SURFACE_TYPE_DEFAULT);
		faultDisplayParams.addParameter(surfaceTypeParam);
		surfaceTypeParam.addParameterChangeListener(this);

		faultDisplayParams.addParameter(lineSizeParam);
		lineSizeParam.addParameterChangeListener(this);
	}

	@Override
	public FaultSectionActorList createFaultActors(RuptureSurface surface, Color color, AbstractFaultSection fault) {
		if (surface instanceof CompoundSurface) {
			FaultSectionActorList list = new FaultSectionActorList(fault);
			for (RuptureSurface subSurf : ((CompoundSurface)surface).getSurfaceList()) {
				FaultSectionActorList sub;
				if (subSurf instanceof EvenlyGriddedSurface)
					sub = createFaultActors((EvenlyGriddedSurface)subSurf, color, fault);
				else
					sub = createFaultActors(subSurf, color, fault);
				list.addAll(sub);
			}
			return list;
		}
		if (surface instanceof EvenlyGriddedSurface) {
			return createFaultActors((EvenlyGriddedSurface)surface, color, fault);
		}
		// TODO
		throw new UnsupportedOperationException("Not yet implemented for non evenly gridded surfaces");
		
//		// this just draws the outline
//		LocationList outline = surface.getPerimeter();
//		
//		Preconditions.checkState(!outline.isEmpty());
//		
//		Point3f[] pts = new Point3f[outline.size()+1];
//		for (int i=0; i<outline.size(); i++) {
//			pts[i] = getPointForLoc(outline.get(i));
//		}
//		pts[pts.length-1] = pts[0];
//		
//		// +1 to connect it at the end
//		LineArray la = new LineArray(pts.length, LineArray.COORDINATES);
//		for (int i=0; i<pts.length; i++) {
//			la.setCoordinate(i, pts[i]);
//		}
//		
//		BranchGroup bg = createBranchGroup();
//		
//		Appearance lapp = buildApp(color);
//		
//		FaultSectionShape3D shape = new FaultSectionShape3D(la,lapp, fault);
//		bg.addChild(shape);
//		
//		return bg;
	}
		
	public synchronized FaultSectionActorList createFaultActors(EvenlyGriddedSurface surface, Color color, AbstractFaultSection fault) {
		// TODO Auto-generated method stub

		int cols = surface.getNumCols();
		int rows = surface.getNumRows();
		
		String surfaceType = surfaceTypeParam.getValue();
		boolean outlineOnly = surfaceType.equals(SURFACE_TYPE_OUTLINE_ONLY);
		boolean traceOnly = surfaceType.equals(SURFACE_TYPE_TRACE_ONLY);
		
		List<double[]> points = new ArrayList<double[]>();

		Container2DImpl<double[]> pointSurface = cacheSurfacePoints(surface);
		
		int lastLoopCol = cols-2;
		int lastLoopRow = rows-2;
		
		if (rows == 0) {
			return null;
		} else if (rows == 1) {
			for (int col=0; col<lastLoopCol; col++) {
				double[] pt0 = pointSurface.get(0,	col);		// top left
				double[] pt3 = pointSurface.get(0,	col+1);	// top right
				points.add(pt0);
				points.add(pt3);
			}
		} else {
			for (int row=0; row<=lastLoopRow; row++) {
				if (traceOnly && row>0)
					break;
				for (int col=0; col<=lastLoopCol; col++) {
					if (outlineOnly && !(row == 0 || row == lastLoopRow || col == 0 || col == lastLoopCol))
						continue;
					double[] pt0 = pointSurface.get(row,	col);		// top left
					double[] pt1 = pointSurface.get(row+1,	col);		// bottom left
					double[] pt2 = pointSurface.get(row+1,	col+1);	// bottom right
					double[] pt3 = pointSurface.get(row,	col+1);	// top right

					// left edge...only if it's not a trace and it's either the left col, or not an outline
					if (!traceOnly && (!outlineOnly || col == 0)) {
						points.add(pt0);
						points.add(pt1);
					}
					// bottom edge...only if it's not a trace and it's either the bottom row, or not an outline
					if (!traceOnly && (!outlineOnly || row == lastLoopRow)) {
						points.add(pt1);
						points.add(pt2);
					}
					// right edge...only if it's not a trace and it's either the right col, or not an outline
					if (!traceOnly && (!outlineOnly || col == lastLoopCol)) {
						points.add(pt2);
						points.add(pt3);
					}
					// top edge...always included
					if (row == 0 || (!outlineOnly || (col == 0 && row == 0))) {
						points.add(pt3);
						points.add(pt0);
					}
				}
			}
		}
		
		double initialOpacity;
		ActorBundle currentBundle;
		if (bundle && bundler != null) {
			// initialized to transparent, will get updated when displayed
			initialOpacity = 0;
			currentBundle = bundler.getBundle(fault);
		} else {
			initialOpacity = 255;
			currentBundle = null;
		}
		
		boolean bundle = this.bundle && currentBundle != null;
		
//		System.out.println("rows: " + rows + ", cols:" + cols + ", pnts: " + points.size());
		vtkPolyData linesPolyData;
		vtkPoints pts;
		vtkUnsignedCharArray colors;
		vtkCellArray lines;
		PickEnabledActor<AbstractFaultSection> actor;
		boolean newBundle = currentBundle == null || !currentBundle.isInitialized();
		if (newBundle) {
			linesPolyData = new vtkPolyData();
			pts = new vtkPoints();
			if (bundle) {
				colors = new vtkUnsignedCharArray();
				colors.SetNumberOfComponents(4);
				colors.SetName("Colors");
			} else {
				colors = null;
			}
			lines = new vtkCellArray();
			
			if (bundle) {
				PointPickEnabledActor<AbstractFaultSection> myActor =
						new PointPickEnabledActor<AbstractFaultSection>(getPickHandler());
				actor = myActor;
				currentBundle.initialize(myActor, linesPolyData, pts, colors, lines);
			} else {
				actor = new PickEnabledActor<AbstractFaultSection>(getPickHandler(), fault);
			}
		} else {
			linesPolyData = currentBundle.getPolyData();
			pts = currentBundle.getPoints();
			colors = currentBundle.getColorArray();
			Preconditions.checkState(colors.GetNumberOfComponents() == 4);
			lines = currentBundle.getCellArray();
			
			actor = currentBundle.getActor();
		}
		int firstIndex;
		synchronized (currentBundle) {
			firstIndex = pts.GetNumberOfPoints();
			for (double[] point : points) {
				pts.InsertNextPoint(point);
				if (bundle)
					colors.InsertNextTuple4(color.getRed(), color.getGreen(), color.getBlue(), initialOpacity);
			}
			
			Preconditions.checkState(points.size() % 2 == 0, "Must be even number of points");
			
			for (int li=0; li<points.size()/2; li++) {
				int index1 = firstIndex + li*2;
				int index2 = index1+1;
				
				vtkLine line = new vtkLine();
				line.GetPointIds().SetId(0, index1);
				line.GetPointIds().SetId(1, index2);
				
				lines.InsertNextCell(line);
			}
			
			if (newBundle) {
				// new bundle
				linesPolyData.SetPoints(pts);
				linesPolyData.SetLines(lines);
				if (bundle)
					linesPolyData.GetPointData().AddArray(colors);
				
				vtkPolyDataMapper mapper = new vtkPolyDataMapper();
				mapper.SetInputData(linesPolyData);
				if (bundle) {
					mapper.ScalarVisibilityOn();
					mapper.SetScalarModeToUsePointFieldData();
					mapper.SelectColorArray("Colors");
				}
				
				actor.SetMapper(mapper);
				actor.GetProperty().SetLineWidth(lineSizeParam.getValue());
				if (bundle)
					actor.GetProperty().SetOpacity(0.999); // needed to trick it to using a transparancey enabled renderer
				else
					actor.GetProperty().SetColor(getColorDoubleArray(color));
				
//				System.out.println("Created new bundle. Currently has "+pts.GetNumberOfPoints()+" points, "
//						+lines.GetNumberOfCells()+" lines");
			} else {
				currentBundle.modified();
			}
		}
		
		FaultSectionActorList list;
		if (bundle) {
			Preconditions.checkState(pts.GetNumberOfPoints() == colors.GetNumberOfTuples());
			list = new FaultSectionBundledActorList(fault, currentBundle, firstIndex, points.size(), 255);
		} else {
			list = new FaultSectionActorList(fault);
			list.add(actor);
		}
		
		return list;
	}
	
	public void setSize(double size) {
		lineSizeParam.setValue(size);
	}

	@Override
	public ParameterList getDisplayParams() {
		return faultDisplayParams;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		super.firePlotSettingsChangeEvent();
	}

}
