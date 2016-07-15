package org.scec.vtk.plugins.SurfacePlugin;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginState;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.plugins.PluginState;

import vtk.vtkActor;

public class SurfacePlugin extends ActionPlugin implements StatefulPlugin {

	public static String dataStoreDir = "SurfaceStore";
	SurfacePluginGUI spGui;
	private boolean guidisplayed = false;
	private PluginState state;
	
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
	
	@Override
	public PluginState getState() {
		// TODO Auto-generated method stub
		if(state==null)
			state = new SurfacePluginState(this.spGui);
		return state;
	}

}
