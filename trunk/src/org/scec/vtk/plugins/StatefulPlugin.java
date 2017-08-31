package org.scec.vtk.plugins;

/**
 * Interface for all plugins which have state that needs
 * to be saved.
 * 
 * The methods of this interface are called by the state
 * vector code to save and restore the session.
 *
 * @author Gideon Juve <juve@usc.edu>
 */
public interface StatefulPlugin extends Plugin, Stateful {
    
}

