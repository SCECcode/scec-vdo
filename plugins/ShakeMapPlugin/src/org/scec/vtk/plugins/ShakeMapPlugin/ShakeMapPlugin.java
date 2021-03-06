package org.scec.vtk.plugins.ShakeMapPlugin;

import javax.swing.JPanel;

import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.AnimatableChangeListener;
import org.scec.vtk.plugins.AnimatablePlugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.plugins.SurfacePlugin.SurfacePluginState;

public class ShakeMapPlugin  extends ActionPlugin implements StatefulPlugin, AnimatablePlugin{


	private ShakeMapGUI gui;
	private PluginState state;
	
	public ShakeMapPlugin() {
	}

	protected JPanel createGUI() {
		gui = new ShakeMapGUI(this, getPluginActors());
		return gui.getPanel();
	}

	public void unload(){
		gui.unloadPlugin();
	}

	@Override
	public PluginState getState() {
		// TODO Auto-generated method stub
		if(state==null)
			state = new ShakeMapPluginState(this.gui);
		return state;
	}

	@Override
	public void animationStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void animationEnded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void animationTimeChanged(double fractionalTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isAnimatable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addAnimatableChangeListener(AnimatableChangeListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAnimatableChangeListener(AnimatableChangeListener l) {
		// TODO Auto-generated method stub
		
	}
	
}



