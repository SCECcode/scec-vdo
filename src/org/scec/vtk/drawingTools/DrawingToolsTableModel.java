package org.scec.vtk.drawingTools;

import javax.swing.table.DefaultTableModel;

public class DrawingToolsTableModel extends DefaultTableModel {
    
    private static final long serialVersionUID = 1L;

    public DrawingToolsTableModel(String[] columnNames) {
		// TODO Auto-generated constructor stub
    	super(columnNames, 0);
	}
    public boolean isCellEditable(int row, int column){  
        return false;  
    }
	
}
