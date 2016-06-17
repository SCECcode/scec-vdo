package org.scec.geo3d.commons.opensha.faults.faultSectionImpl;

import org.opensha.commons.param.ParameterList;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FourPointEvenlyGriddedSurface;
import org.opensha.sha.simulators.RectangularElement;
import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;

public class RectangularElementFault extends AbstractFaultSection {
	
	private RectangularElement element;
	
	public RectangularElementFault(RectangularElement element) {
		super(element.getSectionName() + " ("+element.getID()+")", element.getID());
		this.element = element;
	}

	@Override
	public EvenlyGriddedSurface createSurface(
			ParameterList faultRepresentationParams) {
		return getSurface();
	}
	
	public FourPointEvenlyGriddedSurface getSurface() {
		return element.getSurface();
	}
	
	public int getParentID() {
		return element.getSectionID();
	}

	@Override
	public double getSlipRate() {
		// convert to mm/yr
		return element.getSlipRate() * 1000d;
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

	@Override
	public String getInfo() {
		return super.getInfo()+"\nParent: "+element.getSectionName()+" ("+element.getSectionID()+")";
	}

}
