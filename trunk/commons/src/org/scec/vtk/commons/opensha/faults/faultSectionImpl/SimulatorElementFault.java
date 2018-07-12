package org.scec.vtk.commons.opensha.faults.faultSectionImpl;

import org.opensha.commons.param.ParameterList;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.simulators.SimulatorElement;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

public class SimulatorElementFault extends AbstractFaultSection {
	
	private SimulatorElement element;
	
	public SimulatorElementFault(SimulatorElement element) {
		super(element.getSectionName() + " ("+element.getID()+")", element.getID());
		this.element = element;
	}

	@Override
	public RuptureSurface createSurface(
			ParameterList faultRepresentationParams) {
		return getSurface();
	}
	
	public RuptureSurface getSurface() {
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
		if (element.getFocalMechanism() != null)
			return element.getFocalMechanism().getRake();
		return Double.NaN;
	}

	@Override
	public double getAvgStrike() {
		if (element.getFocalMechanism() != null)
			return element.getFocalMechanism().getStrike();
		return Double.NaN;
	}

	@Override
	public double getAvgDip() {
		if (element.getFocalMechanism() != null)
			return element.getFocalMechanism().getDip();
		return Double.NaN;
	}

	@Override
	public String getInfo() {
		String info = super.getInfo()+"\nParent: "+element.getSectionName()+" ("+element.getSectionID()+")";
		
		if (element.getFaultID() >= 0)
			info += "\nFault ID: "+element.getFaultID();
		
		return info;
	}
	
	public SimulatorElement getElement() {
		return element;
	}

}
