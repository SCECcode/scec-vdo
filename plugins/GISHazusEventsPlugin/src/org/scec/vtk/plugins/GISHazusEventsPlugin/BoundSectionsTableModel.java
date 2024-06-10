package org.scec.vtk.plugins.GISHazusEventsPlugin;

import java.awt.Color;
import java.util.ArrayList;

import org.scec.vtk.plugins.utils.AbstractLibraryModel;


public class BoundSectionsTableModel extends AbstractLibraryModel {

	private final static String []columnNames = { "Show/Hide","Color", "Region"};
    private boolean isSelectedRow[];
    private Color traceColor[];
    private static Color DEFAULT_COLOR = new Color(255, 255, 255);
    private ArrayList continentsList;
    private boolean[] inMem = null;
    private GISHazusEventsPluginGUI eventsGUI;
    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>BoundSectionsTableModel</code>.
     */
    public BoundSectionsTableModel(ArrayList  continentsList, GISHazusEventsPluginGUI eventsGUI) {
    	this.continentsList = continentsList;
    	
    	this.eventsGUI = eventsGUI;
    	isSelectedRow = new boolean[continentsList.size()];
    	inMem = new boolean[continentsList.size()];
    	traceColor = new Color[continentsList.size()];
    	for(int i=0; i<isSelectedRow.length; ++i)  {
    		isSelectedRow[i] = false;
    		inMem[i] = false;
    		traceColor[i] = DEFAULT_COLOR;
       	}
    		
    }
    
	public int getColumnCount() {
		return columnNames.length;
	}
	
	public Class getColumnClass(int col) {
		if(col==0) return Boolean.class;
		else if(col==1) return Color.class;
		else  return String.class;
    }
	

    public int getRowCount() {
        return this.continentsList.size();
    }

      public String getColumnName(int col) {
          return columnNames[col];
      }

      public Object getValueAt(int row, int col) {
    	  if(col==0) return Boolean.valueOf(isSelectedRow[row]);
    	  else if(col==1) return this.traceColor[row];
    	  else if(col == 3) return this.inMem[row];
    	  else  return this.continentsList.get(row);
      }
      
      /*
       * Don't need to implement this method unless your table's
       * editable.
       */
      public boolean isCellEditable(int row, int col) {
          //Note that the data/cell address is constant,
          //no matter where the cell appears onscreen.
          if (col != 2) return true;
          else return false;
      }
  
      /*
       * Don't need to implement this method unless your table's
       * data can change.
       */
      public void setValueAt(Object value, int row, int col) {	
    	  
    	  
    	  if(col==1)  { // TODO change the color of the continent outline
    		  this.traceColor[row] = (Color)value;
    		  eventsGUI.setColor(row, traceColor[row],eventsGUI.groupsTabbedPane.getSelectedIndex());
    		  return;    	  
    	  }
    	  
    	  // select/deselect display of the boundary group
    	  if(col==0) {
    		  boolean currVal = ((Boolean)value).booleanValue();
        	  boolean prevVal = isSelectedRow[row];
    		  this.isSelectedRow[row] = currVal;
    		  if(prevVal==true && currVal==false) { // remove the continent from view
    			  System.out.println("Set value at called");
    			  eventsGUI.predefinedSubGroup(row, currVal);
    		  }
    		 if(prevVal==false && currVal==true){ // add continent to the view
    			 eventsGUI.predefinedSubGroup(row, currVal);
    		 }   
    	  }

 		 if(col==3){
 			 inMem[row] = (Boolean)value;
 		 }
    	  fireTableCellUpdated(row, col);
      }
}