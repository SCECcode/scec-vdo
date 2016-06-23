package org.scec.vtk.timeline;

public interface RangeAnimationListener {
	
	public void rangeStarted();
	
	public void rangeEnded();
	
	public void rangeTimeChanged(double fractionalTime);

}
