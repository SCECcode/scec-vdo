package org.scec.geo3d.library.wgcep.surfaces;

import java.awt.Color;
import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import org.opensha.commons.data.Container2DImpl;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;
import org.scec.geo3d.library.wgcep.surfaces.params.DiscreteSizeParam;

import com.google.common.base.Preconditions;

public class LineSurfaceGenerator extends GeometryGenerator implements ParameterChangeListener {

	public static final String NAME = "Lines";
	
	private ParameterList faultDisplayParams;

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
	public BranchGroup createFaultActor(RuptureSurface surface, Color color, AbstractFaultSection fault) {
		if (surface instanceof CompoundSurface) {
			BranchGroup mainBG = createBranchGroup();
			for (RuptureSurface subSurf : ((CompoundSurface)surface).getSurfaceList()) {
				BranchGroup bg;
				if (subSurf instanceof EvenlyGriddedSurface)
					bg = createFaultBranchGroup((EvenlyGriddedSurface)subSurf, color, fault);
				else
					bg = createFaultActor(subSurf, color, fault);
				mainBG.addChild(bg);
			}
			return mainBG;
		}
		if (surface instanceof EvenlyGriddedSurface) {
			return createFaultBranchGroup((EvenlyGriddedSurface)surface, color, fault);
		}
		
		// this just draws the outline
		LocationList outline = surface.getPerimeter();
		
		Preconditions.checkState(!outline.isEmpty());
		
		Point3f[] pts = new Point3f[outline.size()+1];
		for (int i=0; i<outline.size(); i++) {
			pts[i] = getPointForLoc(outline.get(i));
		}
		pts[pts.length-1] = pts[0];
		
		// +1 to connect it at the end
		LineArray la = new LineArray(pts.length, LineArray.COORDINATES);
		for (int i=0; i<pts.length; i++) {
			la.setCoordinate(i, pts[i]);
		}
		
		BranchGroup bg = createBranchGroup();
		
		Appearance lapp = buildApp(color);
		
		FaultSectionShape3D shape = new FaultSectionShape3D(la,lapp, fault);
		bg.addChild(shape);
		
		return bg;
	}
		
	public BranchGroup createFaultBranchGroup(EvenlyGriddedSurface surface, Color color, AbstractFaultSection fault) {
		// TODO Auto-generated method stub

		int cols = surface.getNumCols();
		int rows = surface.getNumRows();
		
		String surfaceType = surfaceTypeParam.getValue();
		boolean outlineOnly = surfaceType.equals(SURFACE_TYPE_OUTLINE_ONLY);
		boolean traceOnly = surfaceType.equals(SURFACE_TYPE_TRACE_ONLY);
		
		ArrayList<Point3f> points = new ArrayList<Point3f>();

		Container2DImpl<Point3f> pointSurface = cacheSurfacePoints(surface);
		
		int lastLoopCol = cols-2;
		int lastLoopRow = rows-2;
		
		if (rows == 0) {
			return null;
		} else if (rows == 1) {
			for (int col=0; col<lastLoopCol; col++) {
				Point3f pt0 = pointSurface.get(0,	col);		// top left
				Point3f pt3 = pointSurface.get(0,	col+1);	// top right
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
					Point3f pt0 = pointSurface.get(row,	col);		// top left
					Point3f pt1 = pointSurface.get(row+1,	col);		// bottom left
					Point3f pt2 = pointSurface.get(row+1,	col+1);	// bottom right
					Point3f pt3 = pointSurface.get(row,	col+1);	// top right

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
		
//		System.out.println("rows: " + rows + ", cols:" + cols + ", pnts: " + points.size()); 
		LineArray la = new LineArray(points.size(), LineArray.COORDINATES);
		for (int i=0; i<points.size(); i++) {
			Point3f point = points.get(i);
			la.setCoordinate(i, point);
		}
		
		BranchGroup bg = createBranchGroup();
		
		Appearance lapp = buildApp(color);
		
		FaultSectionShape3D shape = new FaultSectionShape3D(la,lapp, fault);
		bg.addChild(shape);
		
		return bg;
	}
	
	private Appearance buildApp(Color color) {
		return buildApp(color, lineSizeParam.getValue());
	}
	
	public static Appearance buildApp(Color color, double lineWidth) {
		ColoringAttributes ca = new ColoringAttributes(new Color3f(color), ColoringAttributes.SHADE_FLAT);
		ca.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
		ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
		
		Appearance lapp = new Appearance();
		lapp.setColoringAttributes(ca);
		LineAttributes latt = new LineAttributes();
		latt.setLineWidth((float) lineWidth);
		lapp.setLineAttributes(latt);
		
		return lapp;
	}
	
	@Override
	public boolean updateColor(BranchGroup bg, Color color) {
		for (int i=0; i<bg.numChildren(); i++) {
			Node node = bg.getChild(i);
			if (node instanceof FaultSectionShape3D)
				if (!updateColorAttributesForNode(node, new Color3f(color)))
					return false;
		}
		return true;
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
