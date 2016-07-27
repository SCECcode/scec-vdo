package org.scec.vtk.plugins.CommunityfaultModelPlugin;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.jdom.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.Fault3D;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.FaultAccessor;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.FaultTableModel;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginState;

import vtk.vtkActor;

/**
 * <i>ScecVideo</i> plugin for the display of 3-dimensional fault representations.
 * 
 * Created on Jan 30, 2005
 * 
 * Status: functional
 * Comments:
 * <ul>
 *      <li>has list of todos (see Peter)</li>
 * </ul>
 * 
 * @author P. Powers
 * @version $Id: Fault3DPlugin.java 2071 2008-07-03 15:39:24Z rberti $
 */
public class CommunityFaultModelPlugin extends ActionPlugin implements StatefulPlugin{

	// TODO  can pluginInfo be made static and then use the plugin name as the data 
	// repository directory name eg "Fault3DPlugin"
	CommunityFaultModelGUI f3DGui;
	private boolean guidisplayed = false;
	private PluginState state;


	/**
	 * Static field for location of fault data in <i>ScecVideo</i> data library.
	 */
	public static String dataStoreDir = "Fault3DStore";

	/**
	 * Constructs a new <code>Fault3DPlugin</code> with appropriate metadata.
	 */
	public CommunityFaultModelPlugin() {
		//this.metadata = new PluginInfo("Community Fault Model (CFM)", "Community Fault Model (CFM)", "P. Powers", "0.1", "Faults");

	}

	/**
	 * Overrides createGUI() in ActionPlugin
	 * @see org.scec.geo3d.plugins.ActionPlugin#createGUI()
	 */
	public JPanel createGUI() {
		f3DGui = new CommunityFaultModelGUI(getPluginActors());
		guidisplayed = true;
		setActors();
		return f3DGui;
	}
	public void unload()
	{
		f3DGui.unload();
		super.unload();
		f3DGui=null;
	}

	public void setActors()
	{
		//ArrayList<vtkActor> allCFMActors = new ArrayList<vtkActor>();
		if(guidisplayed){

			List loadedRows = f3DGui.faultTable.getLibraryModel().getAllObjects();

			for(int i = 0; i < loadedRows.size(); i++)
			{
				FaultAccessor fa = (FaultAccessor)loadedRows.get(i);
				fa.readDataFile();
			}

		}
	}

	@Override
	public PluginState getState() {
		if(state==null)
			state = new CommunityFaultModelPluginState(this.f3DGui);
		return state;
	}
}
