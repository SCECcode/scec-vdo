package org.scec.vtk.timeline;

import org.scec.vtk.plugins.PluginActors;

public class VisibilityKeyFrame extends KeyFrame {
	
	private PluginActors actors;
	private boolean visibile;

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

}
