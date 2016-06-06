package org.scec.geo3d.library.wgcep.surfaces;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.commons.data.Container2DImpl;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;

//public class PolygonSurfaceGenerator extends GeometryGenerator implements ParameterChangeListener {
public class PolygonSurfaceGenerator {

//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	
//	private static final String NAME = "Polygons";
//	
//	ParameterList faultDisplayParams = new ParameterList();
//	
//	public static final String OPACITY_PARAM_NAME = "Surface Opacity";
//	private static final double OPACITY_DEFAULT = 0.7d;
//	private static final double OPACITY_MIN = 0d;
//	private static final double OPACITY_MAX = 1d;
//	private DoubleParameter opacityParam;
//	
//	public PolygonSurfaceGenerator() {
//		super(NAME);
//		
//		opacityParam = new DoubleParameter(OPACITY_PARAM_NAME, OPACITY_MIN, OPACITY_MAX);
//		opacityParam.setDefaultValue(OPACITY_DEFAULT);
//		opacityParam.setValue(OPACITY_DEFAULT);
//		opacityParam.addParameterChangeListener(this);
//		
//		faultDisplayParams.addParameter(opacityParam);
//	}
//	
//	public void setOpacity(double opacity) {
//		opacityParam.setValue(opacity);
//	}
//
//	@Override
//	public BranchGroup createFaultActors(RuptureSurface surface,
//			Color color, AbstractFaultSection fault) {
//		if (surface instanceof CompoundSurface) {
//			BranchGroup mainBG = createBranchGroup();
//			for (RuptureSurface subSurf : ((CompoundSurface)surface).getSurfaceList()) {
//				BranchGroup bg;
//				if (subSurf instanceof EvenlyGriddedSurface)
//					bg = createFaultBranchGroup((EvenlyGriddedSurface)subSurf, color, fault);
//				else
//					bg = createFaultActors(subSurf, color, fault);
//				mainBG.addChild(bg);
//			}
//			return mainBG;
//		}
//		if (surface instanceof EvenlyGriddedSurface) {
//			return createFaultBranchGroup((EvenlyGriddedSurface)surface, color, fault);
//		}
//		
//		// generic approach. this creates a polygon from the outline
//		
//		LocationList outline = surface.getEvenlyDiscritizedPerimeter();
//		
////		System.out.println("Generic polygon approach. Surface: "+surface+" ("+outline.size()+" pts)");
//		
//		Point3f[] pts = new Point3f[outline.size()];
//		
//		for (int i=0; i<outline.size(); i++)
//			pts[i] = getPointForLoc(outline.get(i));
//		
//		GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
//		gi.setCoordinates(pts);
//		
//		int[] stripVertexCounts = {pts.length};
//		gi.setStripCounts( stripVertexCounts );
//		int[] countourCountArray = {stripVertexCounts.length};
//
//		gi.setContourCounts( countourCountArray );
//		
//		NormalGenerator normalGenerator = new NormalGenerator();
//		normalGenerator.generateNormals( gi );
//		
//		//int indexArray = (vertices*2-2);
//		
//		GeometryArray ga = gi.getGeometryArray();
//		
//		ga.setCapability(GeometryArray.ALLOW_COLOR_READ);
//		ga.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
//		ga.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
//		ga.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
//		ga.setCapability(GeometryArray.ALLOW_COUNT_READ);
//		
//		Appearance papp = buildApp(color);
//		
//		Shape3D shp = new FaultSectionActor(ga, papp, fault);
//		
//		BranchGroup bg = createBranchGroup();
//		bg.addChild(shp);
//		
//		return bg;
//	}
//	
//	private Appearance buildApp(Color color) {
//		Appearance papp = new Appearance();
//	   	double opacity = opacityParam.getValue();
//	   	if (opacity < 1d)
//	   		papp.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST,
//	   				1f - (float)opacity));
//	   	else
//	   		papp.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NONE, 0.5f));
//	   	PolygonAttributes pa = new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
//				PolygonAttributes.CULL_NONE, 0, true);
//		papp.setPolygonAttributes(pa);
//		ColoringAttributes ca = new ColoringAttributes(new Color3f(color), ColoringAttributes.SHADE_FLAT);
//		ca.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
//		ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
//		papp.setColoringAttributes(ca);
//		
//		return papp;
//	}
//	
//	public BranchGroup createFaultBranchGroup(EvenlyGriddedSurface surface,
//			Color color, AbstractFaultSection fault) {
//		
//		int cols = surface.getNumCols();
//	   	int rows = surface.getNumRows();
//	   	
//	   	Container2DImpl<Point3f> pointSurface = cacheSurfacePoints(surface);
//	   	
//	   	Appearance papp = buildApp(color);
//		
//		BranchGroup bg = createBranchGroup();
//		
//		// TODO: replace with TriangleStripArray?
//		int numPolys = (rows-1)*(cols-1);
//		int numVerts = numPolys * 4;
//		QuadArray quads = new QuadArray(numVerts, QuadArray.COORDINATES);
//		
//		int vertCount = 0;
//	   	
//		for (int row=0; row<(rows-1); row++) {
//			for (int col=0; col<(cols-1); col++) {
//				Point3f pt0 = pointSurface.get(row,	col);		// top left
//				Point3f pt1 = pointSurface.get(row+1,	col);		// bottom left
//				Point3f pt2 = pointSurface.get(row+1,	col+1);	// bottom right
//				Point3f pt3 = pointSurface.get(row,	col+1);	// top right
//				
////				QuadArray polygon1 = new QuadArray (4, QuadArray.COORDINATES);
//				quads.setCoordinate(vertCount++, pt0);
//				quads.setCoordinate(vertCount++, pt1);
//				quads.setCoordinate(vertCount++, pt2);
//				quads.setCoordinate(vertCount++, pt3);
//			}
//		}
//		FaultSectionActor shape = new FaultSectionActor(quads, papp, fault);
//		bg.addChild(shape);
//		return bg;
//	}
//	
//	@Override
//	public boolean updateColor(BranchGroup bg, Color color) {
//		for (int i=0; i<bg.numChildren(); i++) {
//			Node node = bg.getChild(i);
//			if (node instanceof FaultSectionActor)
//				if (!updateColorAttributesForNode(node, new Color3f(color)))
//					return false;
//		}
//		return true;
//	}
//
//	@Override
//	public ParameterList getDisplayParams() {
//		// TODO Auto-generated method stub
//		return faultDisplayParams;
//	}
//
//	@Override
//	public void parameterChange(ParameterChangeEvent event) {
//		if (event.getSource() == opacityParam)
//			firePlotSettingsChangeEvent();
//	}

}
