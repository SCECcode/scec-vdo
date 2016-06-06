package org.scec.vtk.plugins;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdom.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;

import vtk.vtkActor;

public abstract class ActionPlugin implements Plugin, StatefulPlugin, ClickablePlugin {
	
	private JComponent gui;
	protected PluginInfo metadata;
	
	/**
	 * Save the plugin metadata, create the gui
	 */
	public void initialize(PluginInfo metadata) {
		setMetadata(metadata);
	}
	
	/**
	 * Get the id from the stored metadata
	 */
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
     * @throws UnsupportedOperationException Always
     */
    public Element getState() {
    	throw new UnsupportedOperationException();
    }
    
    /**
     * @throws UnsupportedOperationException Always
     */
    public void setState(Element s) {
    	throw new UnsupportedOperationException();
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
    final public void load() throws IOException {
    	this.gui = createGUI();
    }
    
    /**
     * Add the plugin GUI to the main window
     */
	final public void activate() {
		Info.getMainGUI().addPluginGUI(metadata.getId(), metadata.getShortName(), this.gui);
	}
	
	/**
	 * Remove the plugin GUI from the main window
	 */
	final public void passivate() {
		Info.getMainGUI().removePluginGUI(metadata.getId());
	}
	

	/**
	 * Destroy the GUI
	 */
	 public void unload() {
		gui = null;
	}
	
	/**
	 * Does nothing
	 */
	public void setClickableEnabled(boolean enable) {
		/* Do nothing */
	}
}
