package org.scec.vtk.plugins;

import org.dom4j.Element;

public interface PluginState {
	
	/**
	 * Load the state into the plugin GUI and draw any updates to the 3D viewer immediately. Viewer updates must
	 * block. This method is typically not called from the EventDispatchThread so any interaction with a GUI
	 * element should be wrapped in SwingUtilities.invokeAndWait(Runnable);
	 */
	public void load();
	
	/**
	 * Save the state as XML in the given element 
	 * @param stateEl
	 */
	public void toXML(Element stateEl);
	
	/**
	 * Load state information from the given element. Do not display until <code>load()</code> is called as
	 * many keyframes can be loaded at once from an XML state file.
	 * @param stateEl
	 */
	public void fromXML(Element stateEl);
	
	/**
	 * Creates a deep copy of this plugin state, e.g. for use with KeyFrames
	 * @return
	 */
	public PluginState deepCopy();
	
	//public void clear();

}
