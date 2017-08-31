package org.scec.vtk.plugins;

/**
 * Interface for object (plugin, timeline, etc) which can capture their state
 * @author kevin
 *
 */
public interface Stateful {
	
	/**
     * Get the state of the plugin
     * 
     * getState() will always be called after load()
     */
    public PluginState getState();

}
