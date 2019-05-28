package org.scec.vtk.timeline;

import org.scec.vtk.plugins.PluginActors;

public class VisibilityKeyFrame extends KeyFrame {
	
	private PluginActors actors;
	private final boolean visibile;

	public VisibilityKeyFrame(double startTime, PluginActors actors, boolean visible) {
		super(startTime);
		
		this.actors = actors;
		this.visibile = visible;
	}

	@Override
	public void load() {
		if (visibile)
			actors.visibilityOn();
		else
			actors.visibilityOff();
	}
	
	public boolean isVisible() {
		return visibile;
	}
	
	public VisibilityKeyFrame duplicate() {
		VisibilityKeyFrame key = new VisibilityKeyFrame(getStartTime(), actors, isVisible());
		// don't copy listeners, they will be set up when added to the key frame list
		return key;
	}
	
	@Override
	public String toString() {
		return "VisibilityKeyFrame("+getStartTime()+"s, v="+isVisible()+")";
	}

}
