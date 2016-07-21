package org.scec.vtk.plugins.LegendPlugin;

import java.io.IOException;

import javax.swing.JPanel;

import org.scec.vtk.plugins.ActionPlugin;

public class LegendPlugin extends ActionPlugin {

	private LegendPluginGUI legendGUI;
	
	public LegendPlugin() {}

	@Override
	protected JPanel createGUI() throws IOException {
		this.legendGUI = new LegendPluginGUI(getPluginActors());
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
	
	
	
}
