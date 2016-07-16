package org.scec.vtk.plugins.GISHazusEventsPlugin;

import javax.swing.JPanel;

import org.scec.vtk.plugins.ActionPlugin;

/**
 * Created on July 21, 2011
 * 
 * @author Scott Callaghan, Lewis Nerenberg, Araceli Billoch, Christine Kahn, Amy Lim
 *
 */
public class GISHazusEventsPlugin extends ActionPlugin {
	
	GISHazusEventsPluginGUI gui;
	
	public GISHazusEventsPlugin() {
//		this.metadata = new PluginInfo("Political Boundaries", "Political Boundaries", "Scott", "1.1", "Outlines & Boundaries");
	}
	
	protected JPanel createGUI() {
		gui = new GISHazusEventsPluginGUI(getPluginActors());
		return gui;
	}
		
	public void unload(){
		
	}
}