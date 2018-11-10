package org.scec.vtk.plugins.opensha.obsEqkRup;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.sha.faultSurface.PointSurface;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.surfaces.FaultSectionActorList;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.commons.opensha.surfaces.LineSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.PointSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.PolygonSurfaceGenerator;

public class ObsEqkRupGeometryGenerator extends GeometryGenerator implements ParameterChangeListener {
	
	private StringParameter finiteFaultGeomGenSelect;
	private Map<String, GeometryGenerator> finiteGeomGens;
	
	private GeometryGenerator finiteGeomGen;
	
	private PointSurfaceGenerator pointGeomGen;
	
	private ParameterList paramList;

	public ObsEqkRupGeometryGenerator() {
		super("Observed EQ Geometry Gen");
		
		ArrayList<String> geomNames = new ArrayList<>();
		finiteGeomGens = new HashMap<>();
		
//		PolygonSurfaceGenerator polyGen = new PolygonSurfaceGenerator();
//		geomNames.add(polyGen.getName());
//		finiteGeomGens.put(polyGen.getName(), polyGen);
//		LineSurfaceGenerator lineGen = new LineSurfaceGenerator();
//		geomNames.add(lineGen.getName());
//		finiteGeomGens.put(lineGen.getName(), lineGen);
		// currently points are the only one which works for arbitrary surfaces
		PointSurfaceGenerator pointGen = new PointSurfaceGenerator();
		geomNames.add(pointGen.getName());
		finiteGeomGens.put(pointGen.getName(), pointGen);
		
		finiteFaultGeomGenSelect = new StringParameter("Geometry for Finite Ruptures", geomNames);
		finiteFaultGeomGenSelect.setValue(pointGen.getName());
		finiteGeomGen = pointGen;
		finiteFaultGeomGenSelect.addParameterChangeListener(this);
		
		paramList = new ParameterList();
		if (geomNames.size() > 1)
			paramList.addParameter(finiteFaultGeomGenSelect);
		
		pointGeomGen = new PointSurfaceGenerator();
		Parameter<?> pointSizeParam = pointGeomGen.getDisplayParams().getParameter(PointSurfaceGenerator.POINT_SIZE_PARAM_NAME);
		pointSizeParam = new RenamedParameterWrapper<>(pointSizeParam, "Point Surf "+pointSizeParam.getName());
		paramList.addParameter(pointSizeParam);
		pointSizeParam.addParameterChangeListener(this);
		
		for (GeometryGenerator geomGen : finiteGeomGens.values()) {
			geomGen.setBundlerEneabled(false);
			ParameterList params = geomGen.getDisplayParams();
			if (params != null) {
				for (Parameter<?> param : params) {
					param = new RenamedParameterWrapper<>(param, "Finite Surf "+geomGen.getName()+": "+param.getName());
					param.addParameterChangeListener(this);;
					paramList.addParameter(param);
					if (geomGen != finiteGeomGen)
						param.getEditor().setVisible(false);
				}
			}
		}
	}

	@Override
	public FaultSectionActorList createFaultActors(Surface3D surface, Color color, AbstractFaultSection fault) {
		if (surface instanceof PointSurface)
			return pointGeomGen.createFaultActors(surface, color, fault);
		
//		System.out.println("Building finite "+finiteGeomGen.getName()+" surface for "+fault.getName());
		
		return finiteGeomGen.createFaultActors(surface, color, fault);
	}

	@Override
	public ParameterList getDisplayParams() {
		return paramList;
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {
		if (e.getParameter() == finiteFaultGeomGenSelect) {
			String name = finiteFaultGeomGenSelect.getValue();
			finiteGeomGen = finiteGeomGens.get(name);
			for (GeometryGenerator geomGen : finiteGeomGens.values())
				if (geomGen.getDisplayParams() != null)
					for (Parameter<?> param : geomGen.getDisplayParams())
						param.getEditor().setVisible(geomGen == finiteGeomGen);
		}
		// else it's a geom gen param change
		// always fire a change event
		firePlotSettingsChangeEvent();
	}

}
