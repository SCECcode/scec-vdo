package org.scec.vtk.plugins;

import org.jdom.Element;

/**
 * Interface for all plugins which have state that needs
 * to be saved.
 * 
 * The methods of this interface are called by the state
 * vector code to save and restore the session.
 *
 * @author Gideon Juve <juve@usc.edu>
 */
public interface StatefulPlugin extends Plugin
{
	/**
     * Get the state of the plugin as a DOM Element.
     * This method is called when the user chooses to
     * save the session from the menu.
     * 
     * getState() will always be called after load()
     */
    public Element getState();
    
    /**
     * Set the state of the plugin given a DOM Element.
     * This method is called when the user chooses to
     * start VDO from a saved session or restore a saved
     * session from the menu.
     * 
     * setState() will always be called after load()
     */
    public void setState(Element s);
}

