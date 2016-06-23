package org.scec.vtk.timeline;

import java.util.ArrayList;
import java.util.List;

import org.scec.vtk.plugins.PluginState;

import com.google.common.base.Preconditions;

public class RangeKeyFrame extends KeyFrame {
	
	private double endTime;
	
	private double curFractionalTime;
	
	private List<RangeAnimationListener> listeners = new ArrayList<>();

	public RangeKeyFrame(double startTime, double endTime, PluginState state) {
		super(startTime, state);
		Preconditions.checkArgument(endTime >= 0);
		this.endTime = endTime;
	}
	
	public double getEndTime() {
		return endTime;
	}
	
	public double getCurFractionalTime() {
		return curFractionalTime;
	}
	
	void setRangeStarted() {
		curFractionalTime = 0d;
		for (RangeAnimationListener l : listeners)
			l.rangeStarted();
	}
	
	void setRangeEnded() {
		curFractionalTime = 1d;
		for (RangeAnimationListener l : listeners)
			l.rangeEnded();
	}
	
	void setRangeTime(double fractionalTime) {
		curFractionalTime = fractionalTime;
		for (RangeAnimationListener l : listeners)
			l.rangeTimeChanged(fractionalTime);
	}
	
	boolean isEnded() {
		return curFractionalTime >= 1d;
	}

}
