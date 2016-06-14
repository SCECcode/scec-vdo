package org.scec.geo3d.commons.opensha.gui.anim;

import org.scec.geo3d.commons.opensha.faults.anim.FaultAnimation;

public interface AnimationListener {
	
	public void animationRangeChanged(FaultAnimation anim);
	
	public void animationStepChanged(FaultAnimation anim);

}
