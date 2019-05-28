package org.scec.vtk.commons.opensha.faults.anim;

public interface IDBasedFaultAnimation extends FaultAnimation {
	
	public int getStepForID(int id);

	public int getIDForStep(int step);

}

