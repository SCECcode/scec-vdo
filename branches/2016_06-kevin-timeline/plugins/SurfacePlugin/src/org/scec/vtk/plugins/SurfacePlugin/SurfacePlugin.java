package org.scec.vtk.plugins.SurfacePlugin;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.scec.vtk.plugins.ActionPlugin;

import vtk.vtkActor;

public class SurfacePlugin extends ActionPlugin {

	public static String dataStoreDir = "SurfaceStore";
	SurfacePluginGUI spGui;
	private boolean guidisplayed = false;
	
	public SurfacePlugin() {
	}

	protected JPanel createGUI() {
		spGui = new SurfacePluginGUI(getPluginActors());
		guidisplayed = true;
		return spGui.getPanel();
	}
	
	public void unload(){
		//if(spGui.getImageMasterBranchGroup() != null)
			//spGui.getImageMasterBranchGroup().detach();
		spGui.unloadPlugin();
	}

}
