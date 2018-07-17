package org.scec.vtk.commons.opensha.surfaces;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.Container2DImpl;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

public class PolygonSurfaceGenerator extends GeometryGenerator implements ParameterChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String NAME = "Polygons";
	
	ParameterList faultDisplayParams = new ParameterList();
	
	public static final String OPACITY_PARAM_NAME = "Surface Opacity";
	private static final double OPACITY_DEFAULT = 1d;
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
	public FaultSectionActorList createFaultActors(Surface3D surface,
			Color color, AbstractFaultSection fault) {
		if (surface instanceof CompoundSurface)
			return handleCompound((CompoundSurface)surface, color, fault);
		if (surface instanceof EvenlyGriddedSurface) {
			return createFaultActors((EvenlyGriddedSurface)surface, color, fault);
		}
		
		// generic approach. this creates a polygon from the outline
		
//		LocationList outline = surface.getEvenlyDiscritizedPerimeter();
		LocationList outline = surface.getPerimeter();
		
		List<PointArray> polygons = new ArrayList<>();
		
		double[][] points = new double[outline.size()][];
		
		for (int i=0; i<outline.size(); i++) {
			Location loc = outline.get(i);
			points[i] = getPointForLoc(loc);
		}
		
		polygons.add(new PointArray(points));
		
		return createFaultActors(GeometryType.POLYGON, polygons, color, opacityParam.getValue(), fault);
	}
	
	public FaultSectionActorList createFaultActors(EvenlyGriddedSurface surface,
			Color color, AbstractFaultSection fault) {

		int cols = surface.getNumCols();
		int rows = surface.getNumRows();

		Container2DImpl<double[]> pointSurface = cacheSurfacePoints(surface);
		
		List<PointArray> polygons = new ArrayList<>();
		
		for (int row=0; row<(rows-1); row++) {
			for (int col=0; col<(cols-1); col++) {
				double[] pt0 = pointSurface.get(row,	col);		// top left
				double[] pt1 = pointSurface.get(row+1,	col);		// bottom left
				double[] pt2 = pointSurface.get(row+1,	col+1);	// bottom right
				double[] pt3 = pointSurface.get(row,	col+1);	// top right
				
				PointArray polygon = new PointArray(pt0, pt3, pt2, pt1);
				
				polygons.add(polygon);
			}
		}
		
		return createFaultActors(GeometryType.POLYGON, polygons, color, opacityParam.getValue(), fault);
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
