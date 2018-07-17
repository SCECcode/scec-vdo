package org.scec.vtk.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class KeyFrameList implements Iterable<KeyFrame>, KeyFrameChangeListener {
	
	private ArrayList<KeyFrame> keys;
	// keep separate times list for quick binary searches by time
	private ArrayList<Double> startTimes;
	
	private List<KeyFrameChangeListener> listeners = new ArrayList<>();
	
	public KeyFrameList() {
		keys = new ArrayList<>();
		startTimes = Lists.newArrayList();
	}

	@Override
	public Iterator<KeyFrame> iterator() {
		return keys.iterator();
	}
	
	public synchronized KeyFrame getCurrentFrame(double time) {
		if (keys.isEmpty())
			return null;
		checkBuildStartTimes();
		int ind = Collections.binarySearch(startTimes, time);
		if (ind >= 0)
			return keys.get(ind);
		// it's an insertion point
		// ind = -insertionPoint - 1;
		// ind + 1 = -insertionPoint
		int insertionPoint = -(ind + 1);
		if (insertionPoint == 0)
			return null;
		return keys.get(insertionPoint - 1);
	}
	
	public synchronized void addKeyFrame(KeyFrame key) {
		checkBuildStartTimes();
		int ind = Collections.binarySearch(startTimes, key.getStartTime());
		int index;
		if (ind >= 0)
			// duplicate, just stack them
			index = ind + 1;
		else
			// convert to insertion point
			index = -(ind + 1);
		keys.add(index, key);
		startTimes.add(index, key.getStartTime());
		key.addKeyFrameChangeListener(this);
		fireKeyChanged(key);
	}
	
	public synchronized void removeKeyFrame(KeyFrame key) {
		int index = keys.indexOf(key);
		Preconditions.checkState(index >= 0, "Keyframe not found");
		keys.remove(index);
		if (startTimes != null)
			startTimes.remove(index);
		key.removeKeyFrameChangeListener(this);
		fireKeyChanged(null);
	}
	
	public synchronized void clear() {
		startTimes = null;
		keys.clear();
		for (KeyFrame key : keys)
			key.removeKeyFrameChangeListener(this);
		fireKeyChanged(null);
	}
	
	private void checkBuildStartTimes() {
		if (startTimes == null) {
			startTimes = new ArrayList<>();
			Collections.sort(keys);
			for (KeyFrame key : keys)
				startTimes.add(key.getStartTime());
		}
	}

	@Override
	public synchronized void keyChanged(KeyFrame key) {
		startTimes = null;
		fireKeyChanged(key);
	}
	
	public void addKeyFrameChangeListener(KeyFrameChangeListener l) {
		listeners.add(l);
	}
	
	public boolean removeKeyFrameChangeListener(KeyFrameChangeListener l) {
		return listeners.remove(l);
	}
	
	private void fireKeyChanged(KeyFrame key) {
		for (KeyFrameChangeListener l : listeners)
			l.keyChanged(key);
	}
	
	public KeyFrame getKeyAt(int index) {
		return keys.get(index);
	}
	
	public int size() {
		return keys.size();
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}

}
