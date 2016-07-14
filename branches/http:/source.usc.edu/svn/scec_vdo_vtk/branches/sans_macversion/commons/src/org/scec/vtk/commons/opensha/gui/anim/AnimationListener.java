package org.scec.vtk.commons.opensha.gui.anim;

import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;

public interface AnimationListener {
	
	public void animationRangeChanged(FaultAnimation anim);
	
	public void animationStepChanged(FaultAnimation anim);

}
