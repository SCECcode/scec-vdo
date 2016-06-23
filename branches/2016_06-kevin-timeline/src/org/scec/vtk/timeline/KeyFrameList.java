package org.scec.vtk.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.google.common.collect.Lists;

public class KeyFrameList implements Iterable<KeyFrame> {
	
	private ArrayList<KeyFrame> keys;
	// keep separate times list for quick binary searches by time
	private ArrayList<Double> startTimes;
	
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
		int ind = Collections.binarySearch(startTimes, time);
		if (ind >= 0)
			return keys.get(ind);
		// it's an insertion point
		// ind = -insertionPoint - 1;
		// ind + 1 = -insertionPoint
		int insertionPoint = -(ind + 1);
		return keys.get(insertionPoint - 1);
	}
	
	public synchronized void addKeyFrame(KeyFrame key) {
		keys.add(key);
		startTimes.add(key.getStartTime());
	}
	
	public synchronized void timesChanged() {
		startTimes = new ArrayList<>();
		Collections.sort(keys);
		for (KeyFrame key : keys)
			startTimes.add(key.getStartTime());
	}

}
