package org.scec.vtk.plugins.SurfacePlugin;

import javax.swing.table.DefaultTableModel;

public class SurfaceTableModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    public SurfaceTableModel(String[] columnNames) {
		// TODO Auto-generated constructor stub
    	super(columnNames, 0);
	}
    public boolean isCellEditable(int row, int column){  
        return false;  
    }

}
