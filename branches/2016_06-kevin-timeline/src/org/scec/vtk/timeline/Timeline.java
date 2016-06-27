package org.scec.vtk.timeline;

import java.util.ArrayList;
import java.util.List;

import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.timeline.camera.CameraAnimator;

import com.google.common.base.Preconditions;

public class Timeline {
	
	private KeyFrameList cameraKeys;
	private CameraAnimator cameraAnim;
	
	private List<Plugin> plugins;
	private List<PluginActors> pluginActors;
	private List<KeyFrameList> pluginKeyFrameLists;
	private List<KeyFrame> currentActivatedKeys;
	
	private List<AnimationTimeListener> timeListeners;
	private List<TimelinePluginChangeListener> pluginChangeListeners;
	
	private double maxTime = 15d;
	
	public Timeline() {
		cameraKeys = new KeyFrameList();
		cameraAnim = new CameraAnimator(cameraKeys);
		timeListeners = new ArrayList<>();
		// camera will update through listener interface
		addAnimationTimeListener(cameraAnim);
		
		plugins = new ArrayList<>();
		pluginActors = new ArrayList<>();
		pluginKeyFrameLists = new ArrayList<>();
		currentActivatedKeys = new ArrayList<>();
		
		pluginChangeListeners = new ArrayList<>();
	}
	
	public synchronized void addPlugin(Plugin p, PluginActors actors) {
		Preconditions.checkNotNull(p);
		Preconditions.checkNotNull(actors);
		plugins.add(p);
		pluginActors.add(actors);
		pluginKeyFrameLists.add(new KeyFrameList());
		currentActivatedKeys.add(null);
		fireTimelinePluginsChanged();
	}
	
	public synchronized void removePlugin(Plugin p) {
		int index = indexForPlugin(p);
		Preconditions.checkState(index >= 0, "Plugin not found in timeline!");
		plugins.remove(index);
		pluginActors.remove(index);
		pluginKeyFrameLists.remove(index);
		currentActivatedKeys.remove(index);
		fireTimelinePluginsChanged();
	}
	
	private int indexForPlugin(Plugin p) {
		// could make faster if needed with a map that tracks indexes
		return plugins.indexOf(p);
	}
	
	public synchronized void addKeyFrame(Plugin p, KeyFrame key) {
		addKeyFrame(indexForPlugin(p), key);
	}
	
	public synchronized void addKeyFrame(int index, KeyFrame key) {
		Preconditions.checkState(index >= 0 && index < plugins.size());
		pluginKeyFrameLists.get(index).addKeyFrame(key);
	}
	
	public synchronized void removeKeyFrame(int index, KeyFrame key) {
		Preconditions.checkState(index >= 0 && index < plugins.size());
		pluginKeyFrameLists.get(index).removeKeyFrame(key);
	}
	
	public synchronized void addCameraKeyFrame(KeyFrame key) {
		cameraKeys.addKeyFrame(key);
	}
	
	public synchronized void removeCameraKeyFrame(KeyFrame key) {
		cameraKeys.removeKeyFrame(key);
	}
	
	public KeyFrameList getCameraKeys() {
		return cameraKeys;
	}
	
	public synchronized void activateTime(double time) {
		if (time > maxTime)
			time = maxTime;
		for (int index=0; index<plugins.size(); index++) {
			KeyFrameList keys = pluginKeyFrameLists.get(index);
			KeyFrame cur = currentActivatedKeys.get(index);
			KeyFrame newKey = keys.getCurrentFrame(time);
			if (newKey != cur) {
				// need to laod new key frame, otherwise do nothing
				if (newKey != null) {
					newKey.load();
					if (!(newKey instanceof VisibilityKeyFrame))
						pluginActors.get(index).visibilityOn();
				}
				currentActivatedKeys.set(index, newKey);
			}
			if (newKey instanceof RangeKeyFrame) {
				RangeKeyFrame range = (RangeKeyFrame)newKey;
				double f = (range.getEndTime() - time)/range.getStartTime();
				if (newKey != cur) {
					// just loaded
					range.setRangeStarted();
				}
				if (f >= 1) {
					// we're done
					// only fire new time if done previously
					if (!range.isEnded()) {
						range.setRangeTime(f);
						range.setRangeEnded();
					}
				} else {
					range.setRangeTime(f);
				}
			}
		}
		// notify any listeners of time change
		// primary listener is camera animator, this call will update the camera position
		fireAnimationTimeChanged(time);
		
		// finally update the render window
		MainGUI.updateRenderWindow();
	}
	
	public int getNumPlugins() {
		return plugins.size();
	}
	
	public Plugin getPluginAt(int index) {
		return plugins.get(index);
	}
	
	public KeyFrameList getKeysForPlugin(int index) {
		return pluginKeyFrameLists.get(index);
	}
	
	public PluginActors getActorsForPlugin(int index) {
		return pluginActors.get(index);
	}
	
	public void addAnimationTimeListener(AnimationTimeListener l) {
		timeListeners.add(l);
	}
	
	public boolean removeAnimationTimeListener(AnimationTimeListener l) {
		return timeListeners.remove(l);
	}
	
	private void fireAnimationTimeChanged(double time) {
		for (AnimationTimeListener l : timeListeners)
			l.animationTimeChanged(time);
	}
	
	public void addTimelinePluginChangeListener(TimelinePluginChangeListener l) {
		pluginChangeListeners.add(l);
	}
	
	public boolean removeTimelinePluginChangeListener(TimelinePluginChangeListener l) {
		return pluginChangeListeners.remove(l);
	}
	
	private void fireTimelinePluginsChanged() {
		for (TimelinePluginChangeListener l : pluginChangeListeners)
			l.timelinePluginsChanged();
	}
	
	public double getMaxTime() {
		return maxTime;
	}
	
	public synchronized void setMaxTime(double maxTime) {
		this.maxTime = maxTime;
		for (AnimationTimeListener l : timeListeners)
			l.animationBoundsChanged(maxTime);
	}

}
