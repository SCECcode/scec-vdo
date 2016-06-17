package org.scec.vtk.commons.opensha.surfaces;

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
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PointPickEnabledActor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolygon;
import vtk.vtkUnsignedCharArray;

public class PolygonSurfaceGenerator extends GeometryGenerator implements ParameterChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String NAME = "Polygons";
	
	private boolean bundle = true;
	
	ParameterList faultDisplayParams = new ParameterList();
	
	public static final String OPACITY_PARAM_NAME = "Surface Opacity";
	private static final double OPACITY_DEFAULT = 0.7d;
	private static final double OPACITY_MIN = 0d;
	private static final double OPACITY_MAX = 1d;
	private DoubleParameter opacityParam;
	
	public PolygonSurfaceGenerator() {
		super(NAME);
		
		opacityParam = new DoubleParameter(OPACITY_PARAM_NAME, OPACITY_MIN, OPACITY_MAX);
		opacityParam.setDefaultValue(OPACITY_DEFAULT);
		opacityParam.setValue(OPACITY_DEFAULT);
		opacityParam.addParameterChangeListener(this);
		
		faultDisplayParams.addParameter(opacityParam);
	}
	
	public void setOpacity(double opacity) {
		opacityParam.setValue(opacity);
	}

	@Override
	public FaultSectionActorList createFaultActors(RuptureSurface surface,
			Color color, AbstractFaultSection fault) {
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
		
		// generic approach. this creates a polygon from the outline
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
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
	}
	
	public FaultSectionActorList createFaultActors(EvenlyGriddedSurface surface,
			Color color, AbstractFaultSection fault) {

		int cols = surface.getNumCols();
		int rows = surface.getNumRows();

		Container2DImpl<double[]> pointSurface = cacheSurfacePoints(surface);

//		int numPolys = (rows-1)*(cols-1);
//		int numVerts = numPolys * 4;
//		
//		int vertCount = 0;

		int opacity = (int)(opacityParam.getValue()*255d);
		double initialOpacity;
		ActorBundle currentBundle;
		if (bundle && bundler != null) {
			// initialized to transparent, will get updated when displayed
			initialOpacity = 0;
			currentBundle = bundler.getBundle(fault);
		} else {
			initialOpacity = opacity;
			currentBundle = null;
		}
		
		boolean bundle = this.bundle && currentBundle != null;
		
		vtkPolyData polyData;
		vtkPoints pts;
		vtkUnsignedCharArray colors;
		vtkCellArray polys;
		PickEnabledActor<AbstractFaultSection> actor;
		boolean newBundle = currentBundle == null || !currentBundle.isInitialized();
		if (newBundle) {
			polyData = new vtkPolyData();
			pts = new vtkPoints();
			if (bundle) {
				colors = new vtkUnsignedCharArray();
				colors.SetNumberOfComponents(4);
				colors.SetName("Colors");
			} else {
				colors = null;
			}
			polys = new vtkCellArray();
			
			if (bundle) {
				PointPickEnabledActor<AbstractFaultSection> myActor =
						new PointPickEnabledActor<AbstractFaultSection>(getPickHandler());
				actor = myActor;
				currentBundle.initialize(myActor, polyData, pts, colors, polys);
			} else {
				actor = new PickEnabledActor<AbstractFaultSection>(getPickHandler(), fault);
			}
		} else {
			polyData = currentBundle.getPolyData();
			pts = currentBundle.getPoints();
			colors = currentBundle.getColorArray();
			Preconditions.checkState(colors.GetNumberOfComponents() == 4);
			polys = currentBundle.getCellArray();
			
			actor = currentBundle.getActor();
		}
		int firstIndex;
		int myNumPoints = 0;
		synchronized (currentBundle) {
			firstIndex = pts.GetNumberOfPoints();
			for (int row=0; row<(rows-1); row++) {
				for (int col=0; col<(cols-1); col++) {
					double[] pt0 = pointSurface.get(row,	col);		// top left
					double[] pt1 = pointSurface.get(row+1,	col);		// bottom left
					double[] pt2 = pointSurface.get(row+1,	col+1);	// bottom right
					double[] pt3 = pointSurface.get(row,	col+1);	// top right
					
					int[] ids = new int[4];
					ids[0] = pts.InsertNextPoint(pt0);
					ids[1] = pts.InsertNextPoint(pt1);
					ids[2] = pts.InsertNextPoint(pt2);
					ids[3] = pts.InsertNextPoint(pt3);
					myNumPoints += 4;
					if (bundle) {
						colors.InsertNextTuple4(color.getRed(), color.getGreen(), color.getBlue(), initialOpacity);
						colors.InsertNextTuple4(color.getRed(), color.getGreen(), color.getBlue(), initialOpacity);
						colors.InsertNextTuple4(color.getRed(), color.getGreen(), color.getBlue(), initialOpacity);
						colors.InsertNextTuple4(color.getRed(), color.getGreen(), color.getBlue(), initialOpacity);
						
						PointPickEnabledActor<AbstractFaultSection> pointPickActor = currentBundle.getActor();
					}
				}
			}
			
			int numPoints = pts.GetNumberOfPoints()-firstIndex;
			Preconditions.checkState(numPoints % 4 == 0, "Must be even number of points");
			
			for (int li=0; li<numPoints/4; li++) {
				int index1 = firstIndex+li*4;
				int index2 = index1+1;
				int index3 = index1+2;
				int index4 = index1+3;
				
				vtkPolygon poly = new vtkPolygon();
				poly.GetPointIds().SetNumberOfIds(4);
				poly.GetPointIds().SetId(0, index1);
				poly.GetPointIds().SetId(1, index2);
				poly.GetPointIds().SetId(2, index3);
				poly.GetPointIds().SetId(3, index4);
				
				polys.InsertNextCell(poly);
			}
			
			if (newBundle) {
				// new bundle
				polyData.SetPoints(pts);
				polyData.SetPolys(polys);
				if (bundle)
					polyData.GetPointData().AddArray(colors);
				
				vtkPolyDataMapper mapper = new vtkPolyDataMapper();
				mapper.SetInputData(polyData);
				if (bundle) {
					mapper.ScalarVisibilityOn();
					mapper.SetScalarModeToUsePointFieldData();
					mapper.SelectColorArray("Colors");
				}
				
				actor.SetMapper(mapper);
				
				if (bundle)
					actor.GetProperty().SetOpacity(0.999); // needed to trick it to using a transparancey enabled renderer
				else
					actor.GetProperty().SetColor(getColorDoubleArray(color));
			} else {
				currentBundle.modified();
			}
		}
		
		FaultSectionActorList list;
		if (bundle) {
			list = new FaultSectionBundledActorList(fault, currentBundle, firstIndex, myNumPoints, opacity);
		} else {
			list = new FaultSectionActorList(fault);
			list.add(actor);
		}
		
		return list;
	}

	@Override
	public ParameterList getDisplayParams() {
		// TODO Auto-generated method stub
		return faultDisplayParams;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getSource() == opacityParam)
			firePlotSettingsChangeEvent();
	}

}
