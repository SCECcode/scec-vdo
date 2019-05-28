package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.util.cpt.CPT;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.SimulatorElementFault;

import com.google.common.base.Preconditions;

public class EQSimsDepthColorer extends CPTBasedColorer {
	
	private static CPT getDefaultCPT() {
		CPT cpt = new CPT(0d, 15d, Color.BLUE, Color.RED);
		return cpt;
	}

	public EQSimsDepthColorer() {
		super(getDefaultCPT(), false);
	}

	@Override
	public String getName() {
		return "Depth (km)";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		Preconditions.checkState(fault instanceof SimulatorElementFault);
		return ((SimulatorElementFault)fault).getElement().getCenterLocation().getDepth();
	}


}
