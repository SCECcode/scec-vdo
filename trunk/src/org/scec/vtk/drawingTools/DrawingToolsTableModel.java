package org.scec.vtk.drawingTools;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.scec.vtk.plugins.utils.AbstractLibraryModel;

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
