package org.scec.vtk.plugins.LegendPlugin;

import java.io.IOException;

import javax.swing.JPanel;

import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.plugins.ShakeMapPlugin.ShakeMapPluginState;

public class LegendPlugin extends ActionPlugin implements StatefulPlugin {

	private LegendPluginGUI legendGUI;
	private PluginState state;
	
	public LegendPlugin() {}

	@Override
	protected JPanel createGUI() throws IOException {
		this.legendGUI = new LegendPluginGUI(this);
		return this.legendGUI;
	}
	
	public LegendPluginGUI getLegendGUI()
	{
		return this.legendGUI;
	}
	
	
	public void unload()
	{
		legendGUI.unload();
		legendGUI = null;
	}

	@Override
	public PluginState getState() {
		if(state==null)
			state = new LegendPluginState(this.legendGUI);
		return state;
	}
	
	
	
}
