package org.scec.vtk.commons.opensha.faults;

import org.opensha.commons.param.ParameterList;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FrankelGriddedSurface;
import org.opensha.sha.faultSurface.SimpleFaultData;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.scec.vtk.commons.opensha.faults.params.FaultSurfaceTypeParam;
import org.scec.vtk.commons.opensha.faults.params.GridSpacingFitParam;
import org.scec.vtk.commons.opensha.faults.params.GridSpacingParam;
import org.scec.vtk.commons.opensha.surfaces.FaultSurfaceType;

public abstract class AbstractSimpleFaultDataFaultSection extends AbstractFaultSection {

	public AbstractSimpleFaultDataFaultSection(String name, int id) {
		super(name, id);
	}
	
	protected abstract SimpleFaultData getSimpleFaultData(ParameterList faultRepresentationParams);

	@Override
	public EvenlyGriddedSurface createSurface(
			ParameterList faultRepresentationParams) {
//		System.out.println("creating surface for: "+this.getName());
		SimpleFaultData data = getSimpleFaultData(faultRepresentationParams);
		if (data == null)
			return null;
		
		checkHasParam(faultRepresentationParams, FaultSurfaceTypeParam.NAME);
		FaultSurfaceTypeParam typeParam =
			(FaultSurfaceTypeParam)faultRepresentationParams.getParameter(FaultSurfaceTypeParam.NAME);
		FaultSurfaceType type = typeParam.getFaultSurfaceType();
		
		checkHasParam(faultRepresentationParams, GridSpacingParam.NAME);
		GridSpacingParam spacingParam = (GridSpacingParam)faultRepresentationParams.getParameter(GridSpacingParam.NAME);
		
		checkHasParam(faultRepresentationParams, GridSpacingFitParam.NAME);
		GridSpacingFitParam spacingFitParam = 
			(GridSpacingFitParam)faultRepresentationParams.getParameter(GridSpacingFitParam.NAME);
		
		double spacing = spacingParam.getValue();
		if (spacingFitParam.getValue()) {
			if (type == FaultSurfaceType.FRANKEL) {
				return new FrankelGriddedSurface(data, spacing);
			} else if (type == FaultSurfaceType.STIRLING) {
				return new StirlingGriddedSurface(data, spacing);
			} else {
				throw new RuntimeException("Unkown fault sufrace type: " + type.getName());
			}
		} else {
			if (type == FaultSurfaceType.FRANKEL) {
				return new FrankelGriddedSurface(data, spacing, spacing);
			} else if (type == FaultSurfaceType.STIRLING) {
				return new StirlingGriddedSurface(data, spacing, spacing);
			} else {
				throw new RuntimeException("Unkown fault sufrace type: " + type.getName());
			}
		}
	}
	
	public static ParameterList createSimpleFaultDataParams() {
		ParameterList params = new ParameterList();
		
		params.addParameter(new FaultSurfaceTypeParam());
		params.addParameter(new GridSpacingParam());
		params.addParameter(new GridSpacingFitParam());
		
		return params;
	}

}
