package org.scec.vtk.timeline;

import org.scec.vtk.plugins.PluginState;

import com.google.common.base.Preconditions;

public class KeyFrame implements Comparable<KeyFrame> {
	
	private double startTime;
	
	private PluginState state;
	
	public KeyFrame(double startTime, PluginState state) {
		Preconditions.checkArgument(startTime >= 0);
		Preconditions.checkNotNull(state);
		this.startTime = startTime;
		this.state = state;
	}
	
	KeyFrame(double startTime) {
		Preconditions.checkArgument(startTime >= 0);
		this.startTime = startTime;
	}
	
	public PluginState getState() {
		return state;
	}
	
	public void load() {
		state.load();
	}

	@Override
	public int compareTo(KeyFrame o) {
		return Double.compare(startTime, o.startTime);
	}
	
	public double getStartTime() {
		return startTime;
	}

}
