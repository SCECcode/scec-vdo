package org.scec.vtk.plugins;

import java.io.IOException;

import javax.swing.JComponent;

import org.scec.vtk.main.Info;

public abstract class ActionPlugin implements Plugin {
	
	private JComponent gui;
	protected PluginInfo metadata;
	private PluginActors pluginActors;
	
	/**
	 * Save the plugin metadata, create the gui
	 */
	@Override
	public void initialize(PluginInfo metadata, PluginActors pluginActors) {
		setMetadata(metadata);
		this.pluginActors = pluginActors;
	}
	
	public PluginActors getPluginActors() {
		return pluginActors;
	}
	
	/**
	 * Get the id from the stored metadata
	 */
	@Override
	public String getId() {
		return metadata.getId();
	}
	
	/**
	 * @return Plugin metadata
	 */
    public PluginInfo getMetadata() {
    	return metadata;
    }
    
    /**
     * @param metadata The metadata for this plugin
     */
    public void setMetadata(PluginInfo metadata) {
    	this.metadata = metadata;
    }
    
    /**
     * Called when the plugin is initialized
     * @throws IOException 
     */
    protected abstract JComponent createGUI() throws IOException;
	
    /**
     * Create the GUI
     * @throws IOException 
     */
    @Override
    final public void load() throws IOException {
    	this.gui = createGUI();
    }
    
    /**
     * Add the plugin GUI to the main window
     */
    @Override
	final public void activate() {
		Info.getMainGUI().addPluginGUI(metadata.getId(), metadata.getShortName(), this.gui);
	}
	
	/**
	 * Remove the plugin GUI from the main window
	 */
	@Override
	final public void passivate() {
		Info.getMainGUI().removePluginGUI(metadata.getId());
	}
	

	/**
	 * Destroy the GUI
	 */
	@Override
	 public void unload() {
		gui = null;
	}
}
