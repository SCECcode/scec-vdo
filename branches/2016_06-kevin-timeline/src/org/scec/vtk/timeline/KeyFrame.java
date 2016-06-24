package org.scec.vtk.timeline;

import java.util.ArrayList;
import java.util.List;

import org.scec.vtk.plugins.PluginState;

import com.google.common.base.Preconditions;

public class KeyFrame implements Comparable<KeyFrame> {
	
	private double startTime;
	
	private PluginState state;
	
	private List<KeyFrameChangeListener> listeners = new ArrayList<>();
	
	public KeyFrame(double startTime, PluginState state) {
		Preconditions.checkArgument(startTime >= 0);
		Preconditions.checkNotNull(state);
		this.startTime = startTime;
		this.state = state;
	}
	
	protected KeyFrame(double startTime) {
		Preconditions.checkArgument(startTime >= 0);
		this.startTime = startTime;
	}
	
	public void addKeyFrameChangeListener(KeyFrameChangeListener l) {
		listeners.add(l);
	}
	
	public boolean removeKeyFrameChangeListener(KeyFrameChangeListener l) {
		return listeners.remove(l);
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
	
	public void setStartTime(double startTime) {
		this.startTime = startTime;
		fireKeyChangedEvent();
	}
	
	protected void fireKeyChangedEvent() {
		for (KeyFrameChangeListener l : listeners)
			l.keyChanged(this);
	}
	
	public KeyFrame duplicate() {
		KeyFrame key = new KeyFrame(getStartTime(), getState());
		// don't copy listeners, they will be set up when added to the key frame list
		return key;
	}

}
