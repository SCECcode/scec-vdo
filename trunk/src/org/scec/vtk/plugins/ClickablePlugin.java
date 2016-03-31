package org.scec.vtk.plugins;


public interface ClickablePlugin extends Plugin {
	
	/**
     * Sets the plugin so that when something is clicked in 3d space, 
     * if there are conflicts, the plugin that is currently selected
     * will display it's information for it's object.
     * 
     * This method is called with enable=true when the plugin's
     * gui becomes visible and again with enable=false when
     * the plugin's gui is hidden. It may be called with
     * enable=false several times in a row.
     * 
     * This method will always be called after load() and activate().
     */
    public void setClickableEnabled(boolean enable);
}
