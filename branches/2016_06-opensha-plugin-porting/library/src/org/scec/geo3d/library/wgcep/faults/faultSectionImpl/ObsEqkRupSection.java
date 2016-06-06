package org.scec.geo3d.library.wgcep.faults.faultSectionImpl;

import org.opensha.commons.param.ParameterList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.faultSurface.GriddedSurfaceImpl;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;

public class ObsEqkRupSection extends AbstractFaultSection {
	
	private ObsEqkRupture rup;
	private RuptureSurface finiteSurf;

	public ObsEqkRupSection(String name, int id, ObsEqkRupture rup) {
		super(name, id);
		
		this.rup = rup;
		finiteSurf = rup.getRuptureSurface();
	}

	@Override
	public RuptureSurface createSurface(ParameterList faultRepresentationParams) {
		if (finiteSurf != null)
			return finiteSurf;
		
		GriddedSurfaceImpl surf = new GriddedSurfaceImpl(1, 1, 1.0);
		surf.set(0, 0, rup.getHypocenterLocation());
		
		return surf;
	}

	@Override
	public double getSlipRate() {
		return Double.NaN;
	}

	@Override
	public double getAvgRake() {
		return rup.getAveRake();
	}

	@Override
	public double getAvgStrike() {
		if (finiteSurf == null)
			return Double.NaN;
		return finiteSurf.getAveStrike();
	}

	@Override
	public double getAvgDip() {
		if (finiteSurf == null)
			return Double.NaN;
		try {
			return finiteSurf.getAveDip();
		} catch (Exception e) {
			return Double.NaN;
		}
	}
	
	public ObsEqkRupture getRup() {
		return rup;
	}

}
