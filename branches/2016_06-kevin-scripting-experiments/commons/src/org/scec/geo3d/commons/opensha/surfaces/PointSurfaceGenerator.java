package org.scec.geo3d.commons.opensha.surfaces;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;
import org.scec.geo3d.commons.opensha.surfaces.params.ColorParameter;
import org.scec.geo3d.commons.opensha.surfaces.params.DiscreteSizeParam;

import com.google.common.base.Preconditions;

import vtk.vtkActor;
import vtk.vtkDoubleArray;
import vtk.vtkPointData;

//public class PointSurfaceGenerator extends GeometryGenerator implements ParameterChangeListener {
public class PointSurfaceGenerator {
	
//	private static final String NAME = "Points";
//
//	private ParameterList faultDisplayParams;
//
//	private static final String OUTLINE_COLOR_PARAM_NAME = "Outline Color";
//	private ColorParameter outlineColorParam = new ColorParameter(OUTLINE_COLOR_PARAM_NAME, Color.WHITE);
//
//	private static final String POINT_SIZE_PARAM_NAME = "Point Size";
//	private DiscreteSizeParam pointSizeParam = new DiscreteSizeParam(POINT_SIZE_PARAM_NAME, 1d, 10d, 1d);
//
//	private static final String SURFACE_TYPE_PARAM_NAME = "Surface Type";
//	private static final String SURFACE_TYPE_SOLID = "Solid";
//	private static final String SURFACE_TYPE_SOLID_WITH_OUTLINE = "Solid w/ Outline";
//	private static final String SURFACE_TYPE_OUTLINE_ONLY = "Outline Only";
//	private static final String SURFACE_TYPE_TRACE_ONLY = "Trace Only";
//	private static final String SURFACE_TYPE_DEFAULT = SURFACE_TYPE_SOLID;
//	private StringParameter surfaceTypeParam;
//
//	private static ArrayList<String> getAllSurfaceTypes() {
//		ArrayList<String> surfaceTypes = new ArrayList<String>();
//		surfaceTypes.add(SURFACE_TYPE_SOLID);
//		surfaceTypes.add(SURFACE_TYPE_SOLID_WITH_OUTLINE);
//		surfaceTypes.add(SURFACE_TYPE_OUTLINE_ONLY);
//		surfaceTypes.add(SURFACE_TYPE_TRACE_ONLY);
//		return surfaceTypes;
//	}
//
//	public PointSurfaceGenerator() {
//		super(NAME);
//		
//		faultDisplayParams = new ParameterList();
//
//		surfaceTypeParam = new StringParameter(SURFACE_TYPE_PARAM_NAME, getAllSurfaceTypes(), SURFACE_TYPE_DEFAULT);
//		faultDisplayParams.addParameter(surfaceTypeParam);
//		surfaceTypeParam.addParameterChangeListener(this);
//
//		faultDisplayParams.addParameter(outlineColorParam);
//		outlineColorParam.addParameterChangeListener(this);
//
//		faultDisplayParams.addParameter(pointSizeParam);
//		pointSizeParam.addParameterChangeListener(this);
//		
//		enableForSurfaceType();
//	}
//
//	private boolean shouldUseOutlineColor() {
//		String surfaceType = surfaceTypeParam.getValue();
//		return surfaceType.equals(SURFACE_TYPE_SOLID_WITH_OUTLINE);
//	}
//
//	private void enableForSurfaceType() {
//		outlineColorParam.getEditor().setEnabled(shouldUseOutlineColor());
//	}
//	
//	public static PointArray buildPointArray(int size) {
//		PointArray pa;
//		try {
//			pa = new PointArray(size, PointArray.COORDINATES);
//		} catch (RuntimeException e) {
//			System.out.println("VERTEX COUNT: " + size);
//			throw e;
//		}
//		pa.setCapability(PointArray.ALLOW_COLOR_WRITE);
//		return pa;
//	}
//	
//	private boolean isSolid() {
//		return surfaceTypeParam.getValue().equals(SURFACE_TYPE_SOLID);
//	}
//	
//	@Override
//	public vtkActor createFaultActor(RuptureSurface surface, Color color, AbstractFaultSection fault) {
//		if (surface instanceof CompoundSurface) {
//			// TODO
//			throw new UnsupportedOperationException("Not yet implemented for compound surfaces");
////			BranchGroup mainBG = createBranchGroup();
////			ArrayList<Node> colorNodes = new ArrayList<Node>();
////			for (RuptureSurface subSurf : ((CompoundSurface)surface).getSurfaceList()) {
////				BranchGroup bg;
////				if (subSurf instanceof EvenlyGriddedSurface)
////					bg = createFaultBranchGroup((EvenlyGriddedSurface)subSurf, color, fault);
////				else
////					bg = createFaultActor(subSurf, color, fault);
////				colorNodes.addAll((ArrayList<Node>)bg.getUserData());
////				mainBG.addChild(bg);
////			}
////			mainBG.setUserData(colorNodes);
////			return mainBG;
//		}
//		if (surface instanceof EvenlyGriddedSurface)
//			return createFaultActor((EvenlyGriddedSurface)surface, color, fault);
//		
//		// TODO
//		throw new UnsupportedOperationException("Not yet implemented for non evenly gridded surfaces");
//		
////		// TODO untested. should work, but we don't have any other surface representations to test with!
////		String surfaceType = surfaceTypeParam.getValue();
////		
////		List<Point3f> coloredPts;
////		List<Point3f> highlightPts;
////		
////		if (surfaceType.equals(SURFACE_TYPE_SOLID)) {
////			coloredPts = getPointsForLocs(surface.getEvenlyDiscritizedListOfLocsOnSurface(), null);
////			highlightPts = null;
////		} else if (surfaceType.equals(SURFACE_TYPE_SOLID_WITH_OUTLINE)) {
////			LocationList surfaceLocs = surface.getEvenlyDiscritizedListOfLocsOnSurface();
////			LocationList outlinePts = surface.getEvenlyDiscritizedPerimeter();
////			
////			coloredPts = getPointsForLocs(surfaceLocs, outlinePts);
////			highlightPts = getPointsForLocs(outlinePts, null);
////		} else if (surfaceType.equals(SURFACE_TYPE_OUTLINE_ONLY)) {
////			coloredPts = getPointsForLocs(surface.getEvenlyDiscritizedPerimeter(), null);
////			highlightPts = null;
////		} else { // trace only
////			coloredPts = getPointsForLocs(surface.getEvenlyDiscritizedUpperEdge(), null);
////			highlightPts = null;
////		}
////		
////		PointAttributes pa = buildPA();
////		BranchGroup bg = createBranchGroup();
////		ArrayList<Node> colorNodes = new ArrayList<Node>();
////		
////		if (!coloredPts.isEmpty()) {
////			Shape3D mainShape = getShape(pa, coloredPts, fault, color);
////			colorNodes.add(mainShape);
////			bg.addChild(mainShape);
////		}
////		
////		if (highlightPts != null) {
////			Shape3D highlightShape = getShape(pa, highlightPts, fault, outlineColorParam.getValue());
////			bg.addChild(highlightShape);
////		}
////		
////		return bg;
//	}
//	
//	private Shape3D getShape(PointAttributes pa, List<Point3f> pts, AbstractFaultSection fault, Color color) {
//		Appearance app = new Appearance();
//		app.setColoringAttributes(buildCA(new Color3f(color)));
//		app.setPointAttributes(pa);
//		
//		PointArray ga = buildPointArray(pts.size());
//		for (int i=0; i<pts.size(); i++) {
//			ga.setCoordinate(i, pts.get(i));
//		}
//		
//		return new FaultSectionShape3D(ga, app, fault);
//	}
//	
//	private List<Point3f> getPointsForLocs(LocationList locs, LocationList excludeLocs) {
//		ArrayList<Point3f> pts = new ArrayList<Point3f>();
//		HashSet<Location> excludes;
//		if (excludeLocs == null)
//			excludes = null;
//		else
//			excludes = new HashSet<Location>(excludeLocs);
//		
//		for (Location loc : locs) {
//			if (excludes != null && excludes.contains(loc))
//				continue;
//			pts.add(getPointForLoc(loc));
//		}
//		
//		
//		
//		return pts;
//	}
//	
//	private vtkActor getPointActor
//
//	private List<vtkActor> createFaultActors(EvenlyGriddedSurface surface, Color color, AbstractFaultSection fault) {
//		// TODO Auto-generated method stub
//
//		int numSurfacePoints = (int) surface.size();
//		
//		Preconditions.checkState(numSurfacePoints>0, "NumSurfacePoints must be >0");
//
//		double[][] points = new double[numSurfacePoints][];
//		int cnt = 0;
//		Iterator<Location> it = surface.getLocationsIterator();
//		while(it.hasNext()){
//			Location loc = (Location)it.next();
//			points[cnt++] = getPointForLoc(loc);
//		}
//		vtkPointData pointData = new vtkPointData();
//		new vtkDoubleArray().
//		pointData.AddArray(id0)
//		// make points array
//		PointArray center = null;
//		PointArray top = null;
//		PointArray bottom = null;
//		PointArray left = null;
//		PointArray right = null;
//
//		int rows = surface.getNumRows();
//		int cols = surface.getNumCols();
//
//		String surfaceType = surfaceTypeParam.getValue();
//
//		boolean solid = isSolid();
//		boolean drawCenter = solid || surfaceType == SURFACE_TYPE_SOLID_WITH_OUTLINE;
//		boolean drawSideBottoms = surfaceType == SURFACE_TYPE_SOLID_WITH_OUTLINE
//		|| surfaceType == SURFACE_TYPE_OUTLINE_ONLY;
//		// completely solid
//		if (solid) {
//			center = buildPointArray(points.length);
//		} else {
//			if (drawCenter) {
//				int centerPts = points.length - 2 * cols - 2 * (rows - 2);
//				if (centerPts > 0)
//					center = buildPointArray(centerPts);
//			}
//			if (drawSideBottoms) {
//				if (cols - 2 > 0)
//					bottom = buildPointArray(cols - 2);
//				left = buildPointArray(rows-1);
//				right = buildPointArray(rows-1);
//			}
//			top = buildPointArray(cols);
//		}
//		cnt = 0;
//		int n = points.length;
//		Color3f color3f = new Color3f(color);
//		ColoringAttributes ca = buildCA(color3f);
//		ColoringAttributes ca_out;
//		// if we're drawing the center, then it's an outline color...lets use that
//		// if we're not drawing the center, then color it as the fault should be colored
//		if (!drawCenter)
//			ca_out = ca;
//		else
//			ca_out = buildCA(new Color3f(outlineColorParam.getValue()));
//		
//		/*if(!isHighLight) color3f = new Color3f(color);
//	   	else color3f = new Color3f(HIGHLIGHT_COLOR);*/
//		int i=0, tops=0, bottoms=0, lefts=0, rights=0;
//		while(cnt<n){
//			if (!solid) {
//				if (cnt < cols) {
//					// we always draw the top if it's not solid
//					top.setCoordinate(tops, points[cnt]);
//					tops++;
//					cnt++;
//					continue;
//				}
//				if (cnt % cols == 0) {
//					if (drawSideBottoms) {
//						right.setCoordinate(rights, points[cnt]);
//						rights++;
//					}
//					cnt++;
//					continue;
//				}
//				if ((cnt + 1) % cols == 0) {
//					if (drawSideBottoms) {
//						left.setCoordinate(lefts, points[cnt]);
//						lefts++;
//					}
//					cnt++;
//					continue;
//				}
//				if (cnt > (n - cols) && bottom != null) {
//					if (drawSideBottoms) {
//						bottom.setCoordinate(bottoms, points[cnt]);
//						bottoms++;
//					}
//					cnt++;
//					continue;
//				}
//			}
//			if (drawCenter && center != null) {
//				center.setCoordinate(i, points[cnt]);
//			}
//			cnt++;
//			i++;
//		}
//
//		//pt2.setCapability(Geometry.ALLOW_INTERSECT);
//		//if(faultSectionTG.numChildren()>0)  faultSectionTG.removeChild(0);
//		
//		// increase the size of each point and turn on AA
//		PointAttributes pa = buildPA();
//		
//		Appearance app = new Appearance();
//		app.setColoringAttributes(ca);
//		app.setPointAttributes(pa);
//		Appearance app_out;
//		if (ca_out == ca) {
//			app_out = app;
//		} else {
//			app_out = new Appearance();
//			app_out.setColoringAttributes(ca_out);
//			app_out.setPointAttributes(pa);
//		}
//
//		BranchGroup bg = createBranchGroup();
//		
//		// this is the list of nodes that are the color of the fault
//		ArrayList<Node> colorNodes = new ArrayList<Node>();
//
//		if (drawCenter && center != null) {
//			Shape3D pt2 = new FaultSectionShape3D(center, app, fault);
//			colorNodes.add(pt2);
//			pt2.setPickable(true);
//			pt2.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
//
//			bg.addChild(pt2);
//		}
//		
//		if (!solid) {
//			Shape3D topShape = new FaultSectionShape3D(top, app_out, fault);
//			if (!drawCenter)
//				colorNodes.add(topShape);
//			topShape.setPickable(true);
//			topShape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
//
//			bg.addChild(topShape);
//			
//			if (drawSideBottoms) {
//				if (bottom != null) {
//					Shape3D bottomShape = new FaultSectionShape3D(bottom, app_out, fault);
//					if (!drawCenter)
//						colorNodes.add(bottomShape);
//					bottomShape.setPickable(true);
//					bottomShape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
//					bg.addChild(bottomShape);
//				}
//
//				Shape3D leftShape = new FaultSectionShape3D(left, app_out, fault);
//				if (!drawCenter)
//					colorNodes.add(leftShape);
//				leftShape.setPickable(true);
//				leftShape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
//				bg.addChild(leftShape);
//
//				Shape3D rightShape = new FaultSectionShape3D(right, app_out, fault);
//				if (!drawCenter)
//					colorNodes.add(rightShape);
//				rightShape.setPickable(true);
//				rightShape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
//				bg.addChild(rightShape);
//			}
//		}
//		
//		bg.setUserData(colorNodes);
//
//		return bg;
//	}
//	
//	public static ColoringAttributes buildCA(Color3f color) {
//		ColoringAttributes ca = new ColoringAttributes(color, ColoringAttributes.SHADE_FLAT);
//		ca.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
//		ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
//		return ca;
//	}
//	
//	private PointAttributes buildPA() {
//		return buildPA(pointSizeParam.getValue().floatValue());
//	}
//	
//	public static PointAttributes buildPA(float size) {
//		return new PointAttributes(size, true);
//	}
//	
//	@Override
//	public boolean updateColor(BranchGroup bg, Color color) {
//		Color3f color3f = new Color3f(color);
//		
//		for (Node node : (ArrayList<Node>)bg.getUserData()) {
//			if (!updateColorAttributesForNode(node, color3f))
//				return false;
//		}
//		return true;
//	}
//
//	@Override
//	public ParameterList getDisplayParams() {
//		return faultDisplayParams;
//	}
//
//	@Override
//	public void parameterChange(ParameterChangeEvent event) {
//		if (event.getSource() == surfaceTypeParam) {
//			enableForSurfaceType();
//		}
//		super.firePlotSettingsChangeEvent();
//	}

}
