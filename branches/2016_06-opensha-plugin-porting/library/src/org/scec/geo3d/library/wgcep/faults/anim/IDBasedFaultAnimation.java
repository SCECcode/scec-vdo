package org.scec.geo3d.library.wgcep.faults.anim;

public interface IDBasedFaultAnimation extends FaultAnimation {
	
	public int getStepForID(int id);

	public int getIDForStep(int step);

}

