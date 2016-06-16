package org.scec.vtk.drawingTools;

import java.awt.Color;

import javax.swing.table.DefaultTableModel;

public class LabelTableModel extends DefaultTableModel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static int defaultRowCount = 0;		

        public LabelTableModel(Object[][] inData, String[] inColumnNames) {
        	super(inData, inColumnNames);
		}       
        
        public LabelTableModel(String[] inColumnNames) {
        	super(inColumnNames, defaultRowCount);
        }

		/*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */

        public Class getColumnClass(int c) {
        	if(c==1) return Color.class;
        	//if(c==6) return Float.class;
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
        	if(col == 1) {return false; }
        	else {
        		return true;
        	}
        }

    }