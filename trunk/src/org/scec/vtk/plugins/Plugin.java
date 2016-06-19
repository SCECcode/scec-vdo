package org.scec.vtk.plugins;

import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkActor;


/**
 * This is the interface that all plugins must implement.
 * 
 * The state change for a plugin goes like this:
 * 
 * initialize, (load, (activate, passivate)*, unload)*
 * 
 * @author Scott Callaghan
 * @author P.Powers
 * @author Gideon Juve
 * @version $Id: Plugin.java 3160 2009-07-14 18:38:29Z armstrong $
 */
public interface Plugin {
	
	/**
	 * @return The unique identifier for this plugin 
	 */
	public String getId();
	
	/**
	 * This method gets called when the plugin is instantiated.
	 * @param pluginActors TODO
	 */
    public void initialize(PluginInfo metadata, PluginActors pluginActors);
    
    /**
     * This method gets called when a plugin is loaded. It will
     * always be called before activate, passivate, and unload.
     * 
     * Load is a good place to create GUIs and BranchGroups.
     * @throws IOException 
     */
    public void load() throws IOException;
    
    /**
     * This method is called when the plugin is selected in
     * the menu. If the plugin does not have a menu item this
     * method will never be called.
     * 
     * load() will always be called before activate().
     */
    public void activate();
    
    /**
     * This method gets called when the plugin is un-selected
     * in the menu. If the plugin does not have a menu item
     * this method will never be called.
     */
    public void passivate();
    
    /**
     * This method gets called when the plugin is removed from
     * the session. It should cause the plugin to remove all guis
     * and branch groups that are attached to the scene.
     * 
     * passivate() is always called before unload().
     */
    public void unload();
}

