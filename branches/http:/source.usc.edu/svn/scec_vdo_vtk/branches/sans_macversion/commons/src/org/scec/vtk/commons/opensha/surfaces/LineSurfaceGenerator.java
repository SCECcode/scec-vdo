package org.scec.vtk.commons.opensha.surfaces;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.opensha.commons.data.Container2DImpl;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.surfaces.params.DiscreteSizeParam;

import com.google.common.base.Preconditions;

import vtk.vtkActor;

public class LineSurfaceGenerator extends GeometryGenerator implements ParameterChangeListener {

	public static final String NAME = "Lines";
	
	private ParameterList faultDisplayParams;

	private static final String LINE_SIZE_PARAM_NAME = "Line Size";
	private DiscreteSizeParam lineSizeParam = new DiscreteSizeParam(LINE_SIZE_PARAM_NAME, 1d, 10d, 1d);
	
	public static final String OPACITY_PARAM_NAME = "Line Opacity";
	private static final double OPACITY_DEFAULT = 1d;
	private static final double OPACITY_MIN = 0d;
	private static final double OPACITY_MAX = 1d;
	private DoubleParameter opacityParam;
	
	private enum SurfaceType {
		SOLID("All Lines"),
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

	public LineSurfaceGenerator() {
		super(NAME);
		
		faultDisplayParams = new ParameterList();

		surfaceTypeParam = new EnumParameter<SurfaceType>(
				SURFACE_TYPE_PARAM_NAME, EnumSet.allOf(SurfaceType.class), SURFACE_TYPE_DEFAULT, null);
		faultDisplayParams.addParameter(surfaceTypeParam);
		surfaceTypeParam.addParameterChangeListener(this);

		faultDisplayParams.addParameter(lineSizeParam);
		lineSizeParam.addParameterChangeListener(this);
		
		opacityParam = new DoubleParameter(OPACITY_PARAM_NAME, OPACITY_MIN, OPACITY_MAX);
		opacityParam.setDefaultValue(OPACITY_DEFAULT);
		opacityParam.setValue(OPACITY_DEFAULT);
		
		faultDisplayParams.addParameter(opacityParam);
		opacityParam.addParameterChangeListener(this);
	}

	@Override
	public FaultSectionActorList createFaultActors(RuptureSurface surface, Color color, AbstractFaultSection fault) {
		if (surface instanceof CompoundSurface)
			return handleCompound((CompoundSurface)surface, color, fault);
		if (surface instanceof EvenlyGriddedSurface) {
			return createFaultActors((EvenlyGriddedSurface)surface, color, fault);
		}
		
		// this just draws the outline
		LocationList outline = surface.getPerimeter();
		
		Preconditions.checkState(!outline.isEmpty());
		
		List<PointArray> points = new ArrayList<PointArray>();
		for (int i=0; i<outline.size(); i++) {
			double[] pt1 = getPointForLoc(outline.get(i));
			double[] pt2;
			if (i+1 == outline.size())
				// wrap around to first
				pt2 = points.get(0).get(0);
			else
				pt2 = getPointForLoc(outline.get(i+1));
			points.add(new PointArray(pt1, pt2));
		}
		
		return createFaultActors(points, color, fault);
	}
		
	public FaultSectionActorList createFaultActors(EvenlyGriddedSurface surface, Color color, AbstractFaultSection fault) {
		int cols = surface.getNumCols();
		int rows = surface.getNumRows();
		
		SurfaceType surfaceType = surfaceTypeParam.getValue();
		boolean outlineOnly = surfaceType == SurfaceType.OUTLINE_ONLY;
		boolean traceOnly = surfaceType == SurfaceType.TRACE_ONLY;
		
		List<PointArray> points = new ArrayList<PointArray>();

		Container2DImpl<double[]> pointSurface = cacheSurfacePoints(surface);
		
		int lastLoopCol = cols-2;
		int lastLoopRow = rows-2;
		
		if (rows == 0) {
			return null;
		} else if (rows == 1) {
			for (int col=0; col<pointSurface.getNumCols()-1; col++)
				points.add(new PointArray(pointSurface.get(0, col), pointSurface.get(0, col+1)));
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
					if (!traceOnly && (!outlineOnly || col == 0))
						points.add(new PointArray(pt0, pt1));
					// bottom edge...only if it's not a trace and it's either the bottom row, or not an outline
					if (!traceOnly && (!outlineOnly || row == lastLoopRow))
						points.add(new PointArray(pt1, pt2));
					// right edge...only if it's not a trace and it's either the right col, or not an outline
					if (!traceOnly && (!outlineOnly || col == lastLoopCol))
						points.add(new PointArray(pt2, pt3));
					// top edge...always included
					if (row == 0 || (!outlineOnly || (col == 0 && row == 0)))
						points.add(new PointArray(pt3, pt0));
				}
			}
		}
		
		return createFaultActors(points, color, fault);
	}
	
	private synchronized FaultSectionActorList createFaultActors(
			List<PointArray> points, Color color, AbstractFaultSection fault) {
		
		return createFaultActors(GeometryType.LINE, points, color, opacityParam.getValue(), fault);
	}
	
	@Override
	protected void setActorProperties(vtkActor actor, boolean bundle, Color color, double opacity) {
		super.setActorProperties(actor, bundle, color, opacity);
		actor.GetProperty().SetLineWidth(lineSizeParam.getValue());
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
