package org.scec.vtk.timeline;

import java.util.ArrayList;
import java.util.List;

import org.scec.vtk.plugins.AnimatablePlugin;
import org.scec.vtk.plugins.PluginState;

import com.google.common.base.Preconditions;

public class RangeKeyFrame extends KeyFrame {
	
	private double endTime;
	
	private double curFractionalTime;
	
	private AnimatablePlugin plugin;

	public RangeKeyFrame(double startTime, double endTime, PluginState state, AnimatablePlugin plugin) {
		super(startTime, state);
		Preconditions.checkArgument(endTime >= 0);
		this.endTime = endTime;
		this.plugin = plugin;
	}
	
	public double getEndTime() {
		return endTime;
	}
	
	@Override
	public synchronized void setStartTime(double startTime) {
		// also update duration
		double duration = getDuration();
		this.endTime = startTime + duration;
		super.setStartTime(startTime); // will fire event
	}

	public double getDuration() {
		return getEndTime() - getStartTime();
	}
	
	public synchronized void setDuration(double duration) {
		this.endTime = getStartTime() + duration;
		fireKeyChangedEvent();
	}
	
	public double getCurFractionalTime() {
		return curFractionalTime;
	}
	
	void setRangeStarted() {
		curFractionalTime = 0d;
		plugin.animationStarted();
	}
	
	void setRangeEnded() {
		curFractionalTime = 1d;
		plugin.animationEnded();
	}
	
	void setRangeTime(double fractionalTime) {
		curFractionalTime = fractionalTime;
		plugin.animationTimeChanged(fractionalTime);
	}
	
	boolean isEnded() {
		return curFractionalTime >= 1d;
	}
	
	public RangeKeyFrame duplicate() {
		RangeKeyFrame key = new RangeKeyFrame(getStartTime(), getEndTime(), getState().deepCopy(), plugin);
		// don't copy listeners, they will be set up when added to the key frame list
		return key;
	}
	
	@Override
	public String toString() {
		return "RangeKeyFrame("+getStartTime()+"-"+getEndTime()+"s)";
	}

}
