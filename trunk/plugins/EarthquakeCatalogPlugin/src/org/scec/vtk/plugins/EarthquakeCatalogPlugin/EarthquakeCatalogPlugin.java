package org.scec.vtk.plugins.EarthquakeCatalogPlugin;

import javax.swing.JPanel;

import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;

import vtk.vtkActor;

public class EarthquakeCatalogPlugin extends ActionPlugin implements StatefulPlugin{


	EarthquakeCatalogPluginGUI eQGui;
	private PluginState state;


	/**
	 * Static field for location of fault data in <i>ScecVideo</i> data library.
	 */
	public static String dataStoreDir = "EQCatalogStore";

	/**
	 * Constructs a new <code>Fault3DPlugin</code> with appropriate metadata.
	 */
	public EarthquakeCatalogPlugin() {
		//this.metadata = new PluginInfo("Earthquake Catalog Plugin", "Earthquake Catalog Plugin", "P. Powers", "0.1", "EQCatalog");

	}

	/**
	 * Overrides createGUI() in ActionPlugin
	 * @see org.scec.geo3d.plugins.ActionPlugin#createGUI()
	 */
	public JPanel createGUI() {

		eQGui = new EarthquakeCatalogPluginGUI(this);

		setActors();
		return eQGui;
	}
	public void unload() {
		for (EQCatalog eqc : eQGui.getCatalogs())
			for (vtkActor actor : eqc.getActors())
				getPluginActors().removeActor(actor);
		
		super.unload();
		eQGui=null;
	}

	public void setActors()
	{

	}

	@Override
	public PluginState getState() {
		// TODO Auto-generated method stub
		if(state==null)
			state = new EarthquakeCatalogPluginState(this.eQGui);
		return state;
	}
}
