package org.scec.vtk.timeline;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.timeline.camera.CameraAnimator;
import org.scec.vtk.timeline.camera.CameraAnimator.SplineType;
import org.scec.vtk.timeline.render.H264Renderer;
import org.scec.vtk.timeline.render.ImageSequenceRenderer;
import org.scec.vtk.timeline.render.MP4JPEGSequenceRenderer;
import org.scec.vtk.timeline.render.MP4PNGSequenceRenderer;
import org.scec.vtk.timeline.render.Renderer;
import org.scec.vtk.timeline.render.GIFRenderer;
import com.google.common.base.Preconditions;

public class Timeline implements StatefulPlugin {
	
	private static final boolean D = false;
	
	private KeyFrameList cameraKeys;
	private CameraAnimator cameraAnim;
	
	
	public List<Plugin> plugins;
	// used to hide a plugin completely regardless of keyframes
	private List<Boolean> pluginsDisplayed;
	// used to freeze a plugin at it's current state, never activating another keyframe until unfrozen
	private List<Boolean> pluginsFrozen;
	private List<PluginActors> pluginActors;
	private List<KeyFrameList> pluginKeyFrameLists;
	private List<KeyFrame> currentActivatedKeys;
	
	private List<AnimationTimeListener> timeListeners;
	private List<TimelinePluginChangeListener> pluginChangeListeners;
	
	private double maxTime = 15d;
	private double fps = 30;
	private Renderer renderer;
	private List<Renderer> availableRenderers;
	private Dimension renderDimensions;
	
	GIFRenderer gif;
	
	private boolean isLive = true; // can be set to false for external GUI tests;

	private TimelinePluginState state;
	
	public Timeline() {
		cameraKeys = new KeyFrameList();
		cameraAnim = new CameraAnimator(cameraKeys, SplineType.CARDINAL);
		timeListeners = new ArrayList<>();
		// camera will update through listener interface
		addAnimationTimeListener(cameraAnim);
		
		plugins = new ArrayList<>();
		pluginsDisplayed = new ArrayList<>();
		pluginsFrozen = new ArrayList<>();
		pluginActors = new ArrayList<>();
		pluginKeyFrameLists = new ArrayList<>();
		currentActivatedKeys = new ArrayList<>();
		
		pluginChangeListeners = new ArrayList<>();
		
		availableRenderers = new ArrayList<>();
		availableRenderers.add(new H264Renderer()); // worse than PNG (which is lossless and often smaller), but plays everywhere
		availableRenderers.add(new MP4PNGSequenceRenderer()); // lossless, doesn't play in Quicktime
		availableRenderers.add(new MP4JPEGSequenceRenderer());
		availableRenderers.add(ImageSequenceRenderer.getPNG());
		availableRenderers.add(ImageSequenceRenderer.getJPEG());
		
		gif  = new GIFRenderer("gif", "GIF File");
		availableRenderers.add(gif);

		

		availableRenderers = Collections.unmodifiableList(availableRenderers);
		renderer = availableRenderers.get(0);
	}
	
	public synchronized void addPlugin(Plugin p, PluginActors actors) {
		Preconditions.checkNotNull(p);
		Preconditions.checkNotNull(actors);
		plugins.add(p);
		pluginsDisplayed.add(true);
		pluginsFrozen.add(false);
		pluginActors.add(actors);
		pluginKeyFrameLists.add(new KeyFrameList());
		currentActivatedKeys.add(null);
		fireTimelinePluginsChanged();
	}
	
	public synchronized void removePlugin(Plugin p) {
		int index = indexForPlugin(p);
		Preconditions.checkState(index >= 0, "Plugin not found in timeline!");
		plugins.remove(index);
		pluginsDisplayed.remove(index);
		pluginsFrozen.remove(index);
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
	
	public synchronized void clearKeys(Plugin plugin) {
		int index = indexForPlugin(plugin);
		Preconditions.checkState(index >= 0 && index < plugins.size());
		KeyFrameList keys = pluginKeyFrameLists.get(index);
		keys.clear();
	}
	
	public synchronized void clearCameraKeys() {
		if (cameraKeys == null)
			return;
		cameraKeys.clear();
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
	
	public SplineType getCameraSplineType() {
		return cameraAnim.getSplineType();
	}
	
	public synchronized void setCameraSplineType(SplineType type) {
		cameraAnim.setSplineType(type);
	}
	
	public synchronized void activateTime(double time) {
		if (time > maxTime)
			time = maxTime;
		if (time < 0)
			time = 0;
		for (int index=0; index<plugins.size(); index++) {
			if (pluginsFrozen.get(index) || !pluginsDisplayed.get(index))
				// frozen or not currently displayed, skip
				continue;
			KeyFrameList keys = pluginKeyFrameLists.get(index);
			KeyFrame cur = currentActivatedKeys.get(index);
			KeyFrame newKey = keys.getCurrentFrame(time);
			try {
				if (newKey != cur) {
					if (D) System.out.println("New KeyFrame for plugin "+index+": "+newKey);
					if (cur instanceof RangeKeyFrame) {
						RangeKeyFrame range = (RangeKeyFrame)cur;
						if (!range.isEnded())
							range.setRangeEnded();
					}
					// need to laod new key frame, otherwise do nothing
					if (newKey != null) {
						if (D) System.out.println("Loading KeyFrame");
						newKey.load();
						if (!(newKey instanceof VisibilityKeyFrame)) {
							if (D) System.out.println("Forcing visibility on");
							pluginActors.get(index).visibilityOn();
						}
					}
					currentActivatedKeys.set(index, newKey);
				}
				if (newKey instanceof RangeKeyFrame) {
					RangeKeyFrame range = (RangeKeyFrame)newKey;
					double f = (time - range.getStartTime())/range.getDuration();
					boolean previouslyEnded = range.isEnded();
					boolean justLoaded = newKey != cur;
					boolean currentlyDone = f >= 1;
					if (justLoaded || (!currentlyDone && previouslyEnded)) {
						// just loaded, or finished previously and we're restarting
						range.setRangeStarted();
					}
					if (currentlyDone) {
						// we're done
						if (!previouslyEnded) {
							// only fire new time if done previously
							range.setRangeTime(f);
							range.setRangeEnded();
						}
					} else {
						range.setRangeTime(f);
					}
				}
			} catch (Exception e) {
				// don't die on an exception from an individual plugin, continue
				System.err.println("WARNING: Error activating KeyFrame (new: "+newKey+", prev: "+cur+")");
				e.printStackTrace();
			}
		}
		// notify any listeners of time change
		// primary listener is camera animator, this call will update the camera position
		fireAnimationTimeChanged(time);
		
		// finally update the render window
		if (isLive)
			MainGUI.updateRenderWindow();
	}
	
	public void setDisplayed(Plugin plugin, boolean displayed) {
		setDisplayed(indexForPlugin(plugin), displayed);
	}
	
	public synchronized void setDisplayed(int index, boolean displayed) {
		Preconditions.checkState(index >= 0 && index < plugins.size());
		pluginsDisplayed.set(index, displayed);
		currentActivatedKeys.set(index, null);
		if (displayed)
			pluginActors.get(index).visibilityOn();
		else
			pluginActors.get(index).visibilityOff();
		MainGUI.updateRenderWindow();
	}
	
	public boolean isDisplayed(Plugin plugin) {
		return isDisplayed(indexForPlugin(plugin));
	}
	
	public boolean isDisplayed(int index) {
		Preconditions.checkState(index >= 0 && index < plugins.size());
		return pluginsDisplayed.get(index);
	}
	
	public void setFrozen(Plugin plugin, boolean frozen) {
		setFrozen(indexForPlugin(plugin), frozen);
	}
	
	public synchronized void setFrozen(int index, boolean frozen) {
		Preconditions.checkState(index >= 0 && index < plugins.size());
		pluginsFrozen.set(index, frozen);
	}
	
	public boolean isFrozen(Plugin plugin) {
		return isFrozen(indexForPlugin(plugin));
	}
	
	public boolean isFrozen(int index) {
		Preconditions.checkState(index >= 0 && index < plugins.size());
		return pluginsFrozen.get(index);
	}
	
	/**
	 * Can be used to disable rendering for use debuging/testing GUI components outside of SCEC-VDO
	 * @param isLive
	 */
	public void setLive(boolean isLive) {
		this.isLive = isLive;
	}
	
	public boolean isLive() {
		return isLive;
	}
	
	/**
	 * Set custom render dimensions, or null to use the current viewer dimensions
	 * @param renderDimensions
	 */
	public void setRenderDimensions(Dimension renderDimensions) {
		this.renderDimensions = renderDimensions;
	}
	
	public Dimension getRenderDimensions() {
		return renderDimensions;
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
	
	public PluginActors getActorsForPlugin(Plugin plugin) {
		return getActorsForPlugin(indexForPlugin(plugin));
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
		
		gif.setMaxTime(maxTime);
	}
	
	public double getFamerate() {
		return fps;
	}
	
	public void setFramerate(double fps) {
		Preconditions.checkState(fps > 0);
		this.fps = fps;
		gif.setFps(fps);
	}
	
	public void setRenderer(Renderer renderer) {
		Preconditions.checkState(availableRenderers.contains(renderer), "Unknown renderer: %s", renderer);
		this.renderer = renderer;
	}
	
	public Renderer getRenderer() {
		return renderer;
	}
	
	public List<Renderer> getAvailableRenderers() {
		return availableRenderers;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(PluginInfo metadata, PluginActors pluginActors) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void load() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void passivate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PluginInfo getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PluginActors getPluginActors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PluginState getState() {
		if(state==null)
			state = new TimelinePluginState(this);
		return state;
	}

}
