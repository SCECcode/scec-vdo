package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.awt.Component;

import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginGUI;
import org.scec.vtk.plugins.utils.AbstractLibraryModel;

public class CatalogTableModel extends AbstractLibraryModel {
    
    private static final long serialVersionUID = 1L;

    private Component parent;
	/**
     * Constructs a new, empty <code>CatalogLibraryModel</code>.
     */
    public CatalogTableModel(Component parent) {
        this.parent=parent;
    }
        
    /**
     * Returns the number of columns in this/data model/table.
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
    	if(parent instanceof EarthquakeCatalogPluginGUI)
    		return 4;
    	else
    		return 1;
    }
}
