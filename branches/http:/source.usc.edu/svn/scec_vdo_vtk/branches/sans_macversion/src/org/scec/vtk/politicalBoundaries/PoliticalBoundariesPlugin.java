package org.scec.vtk.politicalBoundaries;

import javax.swing.JPanel;

import org.scec.vtk.plugins.ActionPlugin;

public class PoliticalBoundariesPlugin  extends ActionPlugin {
	PoliticalBoundariesGUI pBGUI;

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
}
