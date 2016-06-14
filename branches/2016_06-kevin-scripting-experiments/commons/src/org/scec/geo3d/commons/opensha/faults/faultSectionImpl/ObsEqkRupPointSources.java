package org.scec.geo3d.commons.opensha.faults.faultSectionImpl;

import java.util.ArrayList;

import org.opensha.commons.param.ParameterList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.GriddedSurfaceImpl;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;

public class ObsEqkRupPointSources extends AbstractFaultSection {
	
	private ObsEqkRupList rups;

	public ObsEqkRupPointSources(int id, ObsEqkRupList rups) {
		super("Point Sources", id);
		this.rups = rups;
	}

	@Override
	public RuptureSurface createSurface(ParameterList faultRepresentationParams) {
		ArrayList<EvenlyGriddedSurface> surfaces = new ArrayList<EvenlyGriddedSurface>();
		for (ObsEqkRupture rup : rups) {
			GriddedSurfaceImpl surf = new GriddedSurfaceImpl(1, 1, 1.0) {

				@Override
				public double getAveDip() throws UnsupportedOperationException {
					return 90;
				}
			};
			surf.set(0, 0, rup.getHypocenterLocation());
			surfaces.add(surf);
		}
		
		return new CompoundSurface(surfaces);
	}

	@Override
	public double getSlipRate() {
		return Double.NaN;
	}

	@Override
	public double getAvgRake() {
		return Double.NaN;
	}

	@Override
	public double getAvgStrike() {
		return Double.NaN;
	}

	@Override
	public double getAvgDip() {
		return Double.NaN;
	}
	
	public ObsEqkRupList getRups() {
		return rups;
	}

}
