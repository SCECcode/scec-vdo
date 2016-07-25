package org.scec.vtk.politicalBoundaries;

import javax.swing.JPanel;

import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.plugins.SurfacePlugin.SurfacePluginState;

public class PoliticalBoundariesPlugin extends ActionPlugin implements StatefulPlugin {
	PoliticalBoundariesGUI pBGUI;
	private PluginState state;

	public PoliticalBoundariesPlugin() {
		// this.metadata = new PluginInfo("Grids", "Grids", "David & Genia",
		// "1.0");
	}

	public JPanel createGUI() {
		this.pBGUI = new PoliticalBoundariesGUI(this.getPluginActors());
		JPanel pbGUIPanel = this.pBGUI.loadAllRegions();
		this.pBGUI.addPoliticalBoundaryActors();
		return pbGUIPanel;
	}

	public PoliticalBoundariesGUI getGraticuleGUI() {
		return pBGUI;
	}

	@Override
	public PluginState getState() {
		// TODO Auto-generated method stub
		if(state==null)
			state = new PoliticalBoundariesPluginState(this.pBGUI);
		return state;
	}
}
