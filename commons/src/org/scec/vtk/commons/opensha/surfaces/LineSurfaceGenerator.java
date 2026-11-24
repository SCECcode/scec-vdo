package org.scec.vtk.commons.opensha.surfaces;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.Container2DImpl;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.surfaces.params.ColorParameter;
import org.scec.vtk.commons.opensha.surfaces.params.DiscreteSizeParam;

import com.google.common.base.Preconditions;

import vtk.vtkActor;

/**
 * Draws outlines and interior grid lines with independent widths. All Lines can use the supplied color everywhere,
 * or keep either part at a fixed color. Outline Only, Trace Only, and explicit outlines use the supplied color.
 */
public class LineSurfaceGenerator extends GeometryGenerator implements ParameterChangeListener {

	public static final String NAME = "Lines";
	public static final String LINE_SIZE_PARAM_NAME = "Line Size";
	public static final String INTERIOR_LINE_SIZE_PARAM_NAME = "Interior Line Size";
	public static final String COLORING_PARAM_NAME = "All Lines Coloring";
	public static final String FIXED_COLOR_PARAM_NAME = "Fixed Line Color";
	public static final String OPACITY_PARAM_NAME = "Line Opacity";

	private final ParameterList faultDisplayParams = new ParameterList();
	private final DiscreteSizeParam lineSizeParam = new DiscreteSizeParam(LINE_SIZE_PARAM_NAME, 1d, 10d, 2d);
	private final DiscreteSizeParam interiorSizeParam =
			new DiscreteSizeParam(INTERIOR_LINE_SIZE_PARAM_NAME, 1d, 10d, 1d);
	private final ColorParameter fixedColorParam = new ColorParameter(FIXED_COLOR_PARAM_NAME, Color.LIGHT_GRAY);
	private final DoubleParameter opacityParam = new DoubleParameter(OPACITY_PARAM_NAME, 0d, 1d);

	// Use the existing bundler's grouping, including custom parent-based grouping, for both styles.
	// Each outline bundle has one companion because VTK line width applies to an entire actor.
	private final Map<FaultActorBundle, FaultActorBundle> interiorBundles = new IdentityHashMap<>();

	public enum SurfaceType {
		SOLID("All Lines"),
		OUTLINE_ONLY("Outline Only"),
		TRACE_ONLY("Trace Only");

		private final String name;

		private SurfaceType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public enum LineColoring {
		SUPPLIED("Supplied Color Everywhere"),
		FIXED_OUTLINE("Fixed Outline"),
		FIXED_INTERIOR("Fixed Interior");

		private final String name;

		private LineColoring(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final String SURFACE_TYPE_PARAM_NAME = "Surface Type";
	private final EnumParameter<SurfaceType> surfaceTypeParam = new EnumParameter<>(
			SURFACE_TYPE_PARAM_NAME, EnumSet.allOf(SurfaceType.class), SurfaceType.SOLID, null);
	private final EnumParameter<LineColoring> coloringParam = new EnumParameter<>(
			COLORING_PARAM_NAME, EnumSet.allOf(LineColoring.class), LineColoring.SUPPLIED, null);

	public LineSurfaceGenerator() {
		super(NAME);
		lineSizeParam.setInfo("Width of outline and trace lines, in pixels.");
		interiorSizeParam.setInfo("Width of interior grid lines, in pixels; applies to All Lines only.");
		opacityParam.setDefaultValue(1d);
		opacityParam.setValue(1d);

		faultDisplayParams.addParameter(surfaceTypeParam);
		faultDisplayParams.addParameter(coloringParam);
		faultDisplayParams.addParameter(fixedColorParam);
		faultDisplayParams.addParameter(lineSizeParam);
		faultDisplayParams.addParameter(interiorSizeParam);
		faultDisplayParams.addParameter(opacityParam);
		for (var parameter : faultDisplayParams)
			parameter.addParameterChangeListener(this);
		enableForSurfaceType();
	}

	private void enableForSurfaceType() {
		boolean allLines = surfaceTypeParam.getValue() == SurfaceType.SOLID;
		coloringParam.getEditor().setEnabled(allLines);
		interiorSizeParam.getEditor().setEnabled(allLines);
		fixedColorParam.getEditor().setEnabled(allLines && coloringParam.getValue() != LineColoring.SUPPLIED);
	}

	/** Select All Lines, Outline Only, or Trace Only. */
	public void setSurfaceType(SurfaceType type) {
		surfaceTypeParam.setValue(Preconditions.checkNotNull(type));
	}

	/** Select which portion of All Lines follows the supplied color. */
	public void setLineColoring(LineColoring coloring) {
		coloringParam.setValue(Preconditions.checkNotNull(coloring));
	}

	/** Set the color used by Fixed Outline and Fixed Interior. */
	public void setFixedLineColor(Color color) {
		fixedColorParam.setValue(Preconditions.checkNotNull(color));
	}

	/** Set the width of interior grid lines, in pixels. */
	public void setInteriorSize(double size) {
		interiorSizeParam.setValue(size);
	}

	public void setOpacity(double opacity) {
		opacityParam.setValue(opacity);
	}

	@Override
	public FaultSectionActorList createFaultActors(Surface3D surface, Color color, AbstractFaultSection fault) {
		if (surface instanceof CompoundSurface)
			return handleCompound((CompoundSurface) surface, color, fault);
		if (surface instanceof EvenlyGriddedSurface)
			return createFaultActors((EvenlyGriddedSurface) surface, color, fault);

		// A surface without a grid supplies only a perimeter; preserve the standalone outline behavior.
		return createFaultActors(surface.getPerimeter(), color, fault);
	}

	/** Draw an explicit outline using the supplied color and the outline/trace width. */
	public FaultSectionActorList createFaultActors(LocationList outline, Color color, AbstractFaultSection fault) {
		Preconditions.checkArgument(!outline.isEmpty(), "Outline must not be empty");
		List<PointArray> lines = new ArrayList<>();
		List<double[]> points = new ArrayList<>();
		for (var location : outline)
			points.add(getPointForLoc(location));
		for (int i = 0; i < points.size(); i++)
			lines.add(new PointArray(points.get(i), points.get((i + 1) % points.size())));
		return createLines(lines, color, fault, getOutlineBundle(fault), true, lineSizeParam.getValue());
	}

	public synchronized FaultSectionActorList createFaultActors(EvenlyGriddedSurface surface, Color color,
			AbstractFaultSection fault) {
		int cols = surface.getNumCols();
		int rows = surface.getNumRows();
		if (rows == 0 || cols == 0)
			return new FaultSectionActorList(fault);

		SurfaceType type = surfaceTypeParam.getValue();
		List<PointArray> outline = new ArrayList<>();
		List<PointArray> interior = new ArrayList<>();
		Container2DImpl<double[]> points = cacheSurfacePoints(surface);

		// Visit each grid edge once. Boundary/interior lists get separate VTK points even at shared locations,
		// keeping their colors independent at intersections.
		for (int row = 0; row < rows; row++) {
			if (type == SurfaceType.TRACE_ONLY && row > 0)
				break;
			boolean boundary = row == 0 || row == rows - 1;
			if (boundary || type == SurfaceType.SOLID) {
				List<PointArray> target = boundary ? outline : interior;
				for (int col = 0; col < cols - 1; col++)
					target.add(new PointArray(points.get(row, col), points.get(row, col + 1)));
			}
		}
		if (type != SurfaceType.TRACE_ONLY) {
			for (int col = 0; col < cols; col++) {
				boolean boundary = col == 0 || col == cols - 1;
				if (boundary || type == SurfaceType.SOLID) {
					List<PointArray> target = boundary ? outline : interior;
					for (int row = 0; row < rows - 1; row++)
						target.add(new PointArray(points.get(row, col), points.get(row + 1, col)));
				}
			}
		}

		FaultActorBundle outlineBundle = getOutlineBundle(fault);
		LineColoring coloring = type == SurfaceType.SOLID ? coloringParam.getValue() : LineColoring.SUPPLIED;
		boolean fixedOutline = coloring == LineColoring.FIXED_OUTLINE;
		FaultSectionActorList outlineActors = createLines(outline, fixedOutline ? fixedColorParam.getValue() : color,
				fault, outlineBundle, !fixedOutline, lineSizeParam.getValue());
		if (interior.isEmpty())
			return outlineActors;

		FaultActorBundle interiorBundle = outlineBundle == null ? null
				: interiorBundles.computeIfAbsent(outlineBundle, key -> new FaultActorBundle());
		boolean fixedInterior = coloring == LineColoring.FIXED_INTERIOR;
		FaultSectionActorList interiorActors = createLines(interior, fixedInterior ? fixedColorParam.getValue() : color,
				fault, interiorBundle, !fixedInterior, interiorSizeParam.getValue());
		CompositeFaultSectionActorList result = new CompositeFaultSectionActorList(fault);
		result.addPart(outlineActors);
		result.addPart(interiorActors);
		return result;
	}

	private FaultActorBundle getOutlineBundle(AbstractFaultSection fault) {
		return bundler != null && isBundlerEnabled() && fault != null ? bundler.getBundle(fault) : null;
	}

	private synchronized FaultSectionActorList createLines(List<PointArray> lines, Color color,
			AbstractFaultSection fault, FaultActorBundle bundle, boolean colorUpdateEnabled, double width) {
		if (lines.isEmpty())
			return new FaultSectionActorList(fault);
		FaultSectionActorList actors = createFaultActors(GeometryType.LINE, lines, color, opacityParam.getValue(),
				fault, bundle, colorUpdateEnabled);
		if (actors instanceof FaultSectionBundledActorList) {
			bundle.getActor().GetProperty().SetLineWidth((float) width);
		} else {
			for (vtkActor actor : actors)
				actor.GetProperty().SetLineWidth((float) width);
		}
		return actors;
	}

	@Override
	protected void setActorProperties(vtkActor actor, boolean bundle, Color color, double opacity) {
		super.setActorProperties(actor, bundle, color, opacity);
		actor.GetProperty().SetLineWidth(lineSizeParam.getValue().floatValue());
	}

	@Override
	public synchronized void setFaultActorBundler(FaultActorBundler bundler) {
		super.setFaultActorBundler(bundler);
		interiorBundles.clear();
	}

	/** Set the width of outlines, traces, and explicit line lists. */
	public void setSize(double size) {
		lineSizeParam.setValue(size);
	}

	@Override
	public synchronized void clearBundles() {
		super.clearBundles();
		interiorBundles.clear();
	}

	@Override
	public ParameterList getDisplayParams() {
		return faultDisplayParams;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameterName().equals(SURFACE_TYPE_PARAM_NAME)
				|| event.getParameterName().equals(COLORING_PARAM_NAME))
			enableForSurfaceType();
		firePlotSettingsChangeEvent();
	}
}
