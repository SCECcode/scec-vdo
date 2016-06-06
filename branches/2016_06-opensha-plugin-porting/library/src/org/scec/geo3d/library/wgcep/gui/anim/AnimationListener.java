package org.scec.geo3d.library.wgcep.gui.anim;

import org.scec.geo3d.library.wgcep.faults.anim.FaultAnimation;

public interface AnimationListener {
	
	public void animationRangeChanged(FaultAnimation anim);
	
	public void animationStepChanged(FaultAnimation anim);

}
