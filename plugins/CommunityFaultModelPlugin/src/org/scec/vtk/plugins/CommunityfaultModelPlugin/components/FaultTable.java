package org.scec.vtk.plugins.CommunityfaultModelPlugin.components;

import java.util.*;
import java.util.List;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.table.*;


import org.scec.vtk.plugins.utils.components.ColorWellIcon;
import org.scec.vtk.tools.Prefs;

import javax.swing.*;
import javax.swing.colorchooser.*;
import javax.swing.event.*;

import vtk.vtkActor;

import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.*;


/**
 * This class extends <code>JTable</code> to display a list of faults
 * available for viewing in a Java3D scenegraph. The table is coupled to an 
 * underlying <code>FaultDataModel</code>
 * that manages fault data (e.g. loading, saving, attribute modification).
 * 
 * Created on Jan 30, 2005
 * 
 * @author P. Powers
 * @version $Id: FaultTable.java 4543 2013-07-18 16:30:17Z jeremypc $
 */
public class FaultTable extends JTable implements ChangeListener {
    
    private static final long serialVersionUID = 1L;

	// owner (default visibility for nested components)
    Component tableOwner;
    
    // table access fields
    private FaultTableModel tableModel;
    private ListSelectionModel selModel;

    /**
     * Constructs a new <code>FaultTable</code> with the specified owner, i.e <code>FaultGUI</code>.
     *
     * @param owner parent <code>Container</code> that is registered for various event notifications
     */
    public FaultTable(Component owner) {
        super();
        this.tableOwner = owner;
        this.init();
    }
    
 
    //****************************************
    //     PUBLIC UTILITY METHODS
    //****************************************

    /**
     * (Re)loads all <code>Fault3D</code>s in <i>ScecVideo</i> data store.
     */
    public void loadLibrary() {

        // clear current list
        this.tableModel.clear();
        
        // process import group directories
        File dataDir = new File(
                Prefs.getLibLoc() + 
                File.separator + CommunityFaultModelPlugin.dataStoreDir);
 
        FileFilter filter = new FileFilter() {
            public boolean accept(File f) {
                if (f.getName().endsWith(".flt")) return true;
                return false; 
            }
        };
        
        File[] faultDirs = dataDir.listFiles();
        List faultList = new ArrayList();
        for (int i=0; i<faultDirs.length; i++) {
            if (faultDirs[i].isDirectory()) {
                List files = Arrays.asList(faultDirs[i].listFiles(filter));
                if (files.size() == 0) {
                    // prune empty directories
                    deleteDir(faultDirs[i]);
                } else {
                    faultList.addAll(files);
                }
            }
        }
        
        for (int i=0; i<faultList.size(); i++) {
            Fault3D fault = new Fault3D((File)faultList.get(i));
            addFault(fault);
        }
        
    }

    /**
     * Method adds an <code>ArrayList</code> of <code>FaultAccessor</code>s
     * to this table.
     * 
     * @param faults array of <code>FaultAccessor</code>s to be loaded
     */
    public void addFaults(ArrayList faults) {
        this.tableModel.addObjects(faults);
    }

    /**
     * Adds a fault to the table.
     * 
     * @param fault to add
     */
    public void addFault(Fault3D fault) {
        this.tableModel.addObject(fault);
    }
    
    /**
     * Returns the data model associated with this table.
     * 
     * @return data model
     */
    public FaultTableModel getLibraryModel() {
        return this.tableModel;
    }
           
    /**
     * Sets selected rows/faults based on array of fault files provided.
     * 
     * @param objects <code>DatatAccessor</code>s to be selected
     */
    public void setSelected(ArrayList objects) {        
        
        // clear current selection
        clearSelection();
                
        // select faults
        this.selectionModel.setValueIsAdjusting(true);
        for (int i=0; i<objects.size(); i++) {
        	FaultAccessor obj = (FaultAccessor)objects.get(i);
            if (!obj.isInMemory()) {
            	obj.setIndex(i);
            	obj.setInMemory(true);
            }
            int sel = this.tableModel.indexOf(obj);
            this.selectionModel.addSelectionInterval(sel,sel);
            
            //makes set selected actually set the check box and not just highlight
            loadSelection(objects);

        }
        this.selectionModel.setValueIsAdjusting(false);
    }

    public void loadSelection(ArrayList objects){
    	FaultTableModel libModel = FaultTable.this.getLibraryModel();
    	FaultAccessor obj;

    	for(int i =0;i<libModel.getRowCount();i++){
    		obj = (FaultAccessor)this.tableModel.getObjectAtRow(i);
    		obj.setDisplayed(false);
    	}
    		
    	
    	for(int j=0;j<objects.size();j++){
    		obj = (FaultAccessor)objects.get(j);
    		obj.setDisplayed(true);
    	}
    }

    /**
     * Returns an <code>ArrayList</code> of currently selected <code>DataAccessors</code>.
     * 
     * @return selected <code>DataAccessor</code>s
     */
    public ArrayList<Fault3D> getSelected() {
    	FaultTableModel libModel = FaultTable.this.getLibraryModel();
        ArrayList<Fault3D> selectedObjects = new ArrayList<>();
        int[] selectedRows = getSelectedRows();
        for (int i=0; i<libModel.getRowCount(); i++) {
        	for(int j=0;j<selectedRows.length;j++)
        	{
        		if(i==selectedRows[j])
        			selectedObjects.add((Fault3D)this.tableModel.getObjectAtRow(i));
        	}
        }
        return selectedObjects;
    }
    
    //****************************************
    //     PRIVATE METHODS
    //****************************************

    private void init() {
        
        // initialize data model and register listeners
        this.tableModel = new FaultTableModel();
        this.tableModel.addTableModelListener((TableModelListener)this.tableOwner);
        setModel(this.tableModel);
        
        // set to monitor mouse clicks
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                
                // Get column and row values for X and Y clicked
                FaultTableModel libModel = FaultTable.this.getLibraryModel();
                CommunityFaultModelGUI gui = (CommunityFaultModelGUI)FaultTable.this.tableOwner;
                int col = FaultTable.this.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / getRowHeight();
                
                if (col == 0) {
                	if(!libModel.getLoadedStateForRow(row)){
                		libModel.setLoadedStateForRow(true, row);
                		gui.processTableSelectionChange();
                	}
                	//libModel.toggleVisibilityForRow(row);
                } else if (col == 1 && libModel.getColorForRow(row) != null) {
                	// restrict showing color chooser to clicks that fall on non-null color wells
                	Color newColor = null;//((CommunityFaultModelGUI)FaultTable.this.tableOwner).getColorChooser().getColor();
                	if (newColor != null) {
                		libModel.setColorForRows(newColor, getSelectedRows());
                	}
                } else if (col == 2) {
                	libModel.toggleMeshStateForRow(row);
                }
            }
        });
               
        // Set up selection model and register GUI as listener for 
        // button en/disabling.
        this.selModel = getSelectionModel();
        this.selModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.selModel.addListSelectionListener((ListSelectionListener)this.tableOwner);
        
        // visual set up
        this.setTableHeader(null);
        this.setRowHeight(getRowHeight()+4);
        this.setIntercellSpacing(new Dimension(0,0));
        this.setShowGrid(false);
  
        TableColumn col2 = getColumnModel().getColumn(1);
        col2.setCellRenderer(new ColorWellRenderer());
        col2.setPreferredWidth(20);
        col2.setMinWidth(20);
        col2.setMaxWidth(20);
        
        TableColumn col3 = getColumnModel().getColumn(2);
        col3.setCellRenderer(new MeshStateRenderer());
        col3.setPreferredWidth(20);
        col3.setMinWidth(20);
        col3.setMaxWidth(20);
        
    }
    
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }

    //****************************************
    //     EVENT HANDLERS
    //****************************************


    /**
     * Required event-handler that processes <code>FaultColorPicker</code> color
     * selection shanges.
     * 
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
        int[] rows = this.getSelectedRows();
        if (rows.length > 0) {
            Color c = ((DefaultColorSelectionModel)e.getSource()).getSelectedColor();
            this.tableModel.setColorForRows(c, rows);
        }
    }
    
    
    //****************************************
    //     CELL RENDERERS
    //****************************************
    
    
    /**
     * Custom renderer class draws <code>ColorWell</code> objects.
     *
     * Created on Jan 30, 2005
     * 
     */
    private class ColorWellRenderer extends DefaultTableCellRenderer {
        
        private static final long serialVersionUID = 1L;
		private ColorWellIcon colorIcon = new ColorWellIcon(Color.WHITE, 11, 11, 2);
        
        ColorWellRenderer() {
            super();
            setHorizontalAlignment(SwingConstants.LEFT);
        }
        
        /**
         * Required method of custom cell renderers that gets called to render 
         * <code>ColorWell</code> table cells.
         * 
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(
                JTable table, Object fault,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            
            FaultAccessor f = (FaultAccessor)fault;
            setEnabled(true);
            
            this.colorIcon.setColor(f.getColor());
            setIcon(this.colorIcon);
            setDisabledIcon(this.colorIcon);
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                if ((row % 2) == 0) {
                    setBackground(Prefs.getStripingColor());
                } else {
                    setBackground(table.getBackground());
                }
            }
            
            return this;
        }
    }
    
    /**
     * Custom renderer class draws <code>MeshState</code> objects.
     *
     * Created on Jan 30, 2005
     * 
     */
    private class MeshStateRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
		MeshStateIcon meshIcon = new MeshStateIcon(MeshStateIcon.MESH_NO_FILL);
        
        MeshStateRenderer() {
            super();
            setHorizontalAlignment(SwingConstants.LEFT);
        }
        
        /**
         * Required method of custom cell renderers that gets called to 
         * render <code>MeshState</code> table cells.
         * 
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(
                JTable table, Object fault,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            
            FaultAccessor f = (FaultAccessor)fault;
            setEnabled(true);
            
            this.meshIcon.setMeshState(f.getMeshState());
            setIcon(this.meshIcon);
            setDisabledIcon(this.meshIcon);
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                if ((row % 2) == 0) {
                    setBackground(Prefs.getStripingColor());
                } else {
                    setBackground(table.getBackground());
                }
            }
            
            return this;
        }
        
    }    
}
