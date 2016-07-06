package org.scec.vtk.plugins.ShakeMapPlugin;

import javax.swing.JPanel;

import org.scec.vtk.plugins.ActionPlugin;

public class ShakeMapPlugin  extends ActionPlugin{


	private ShakeMapGUI gui;
	public ShakeMapPlugin() {
	}

	protected JPanel createGUI() {
		gui = new ShakeMapGUI(getPluginActors());
		return gui.getPanel();
	}

	public void unload(){
		gui.unloadPlugin();
	}

}



