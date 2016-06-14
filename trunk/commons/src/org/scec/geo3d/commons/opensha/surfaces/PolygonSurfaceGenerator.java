package org.scec.geo3d.commons.opensha.surfaces;

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
import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolygon;

public class PolygonSurfaceGenerator extends GeometryGenerator implements ParameterChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String NAME = "Polygons";
	
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
		
		vtkPolyData polyData = new vtkPolyData();
		
		vtkPoints pts = new vtkPoints();
	   	
		for (int row=0; row<(rows-1); row++) {
			for (int col=0; col<(cols-1); col++) {
				double[] pt0 = pointSurface.get(row,	col);		// top left
				double[] pt1 = pointSurface.get(row+1,	col);		// bottom left
				double[] pt2 = pointSurface.get(row+1,	col+1);	// bottom right
				double[] pt3 = pointSurface.get(row,	col+1);	// top right
				
				pts.InsertNextPoint(pt0);
				pts.InsertNextPoint(pt1);
				pts.InsertNextPoint(pt2);
				pts.InsertNextPoint(pt3);
			}
		}
		
		polyData.SetPoints(pts);
		
		int numPoints = pts.GetNumberOfPoints();
		Preconditions.checkState(numPoints % 4 == 0, "Must be even number of points");
		
		vtkCellArray polys = new vtkCellArray();
		
		for (int li=0; li<numPoints/4; li++) {
			int index1 = li*4;
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
		
		polyData.SetPolys(polys);
		
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.SetInputData(polyData);
		FaultSectionActorList list = new FaultSectionActorList(fault);
		
		vtkActor actor = new vtkActor();
		actor.SetMapper(mapper);
		actor.GetProperty().SetColor(getColorDoubleArray(color));
		actor.GetProperty().SetOpacity(opacityParam.getValue());
		list.add(actor);
		
		return list;
	}
	
	@Override
	public boolean updateColor(FaultSectionActorList actorList, Color color) {
		for (vtkActor actor : actorList)
			actor.GetProperty().SetColor(getColorDoubleArray(color));
		return true;
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
