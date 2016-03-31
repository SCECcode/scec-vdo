package org.scec.vtk.plugins.CommunityfaultModelPlugin.components;

import java.awt.*;

import org.scec.vtk.plugins.utils.AbstractLibraryModel;

/**
 * This class maintains <code>Fault3D</code> data for display in a <code>FaultTable</code> and a
 * Java3D scenegraph. It provides several methods to modify fault specific attributes that keep
 * object attribute files in sync with the corresponding table display.
 * 
 * Created on Jan 30, 2005
 * 
 * @author P. Powers
 * @version $Id: FaultTableModel.java 2109 2008-07-08 20:35:22Z ihrig $
 */
public class FaultTableModel extends AbstractLibraryModel {
    
    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new, empty <code>FaultDataModel</code>.
     */
    public FaultTableModel() {
        // empty constructor
    }

    /**
     * Sets the color of a <code>Fault3D</code>. Method is aborted if previous value
     * is <code>null</code> - implying a multi-colored geometry.
     * 
     * @param c the color to set
     * @param row (fault) to be changed
     */
    public void setColorForRow(Color c, int row) {
        // check that row/fault isInMemory and color is non-null
        if (getColorForRow(row) != null) {
            ((FaultAccessor)getObjectAtRow(row)).setColor(c);
            fireTableCellUpdated(row, 1);
        }
    }
    
    /**
     * Sets the color of each <code>Fault3D</code> in the specified rows.
     * 
     * @param c the color to set
     * @param rows (faults) to be set/changed
     * 
     */
    public void setColorForRows(Color c, int[] rows) {
        for (int i=0; i<rows.length; i++) {
            setColorForRow(c, rows[i]);
        }
    }
    
    /**
     * Returns the color of a <code>FaultAccessor</code> at a given <code>row</code>.
     * 
     * @param row to get color for
     * @return the color of the object at <code>row</code>
     */
    public Color getColorForRow(int row) {
        return ((FaultAccessor)getObjectAtRow(row)).getColor();
    }
    
    /**
     * Sets the mesh/appearance state of a <code>Fault3D</code> at a particular row.
     * Possible values are:
     * <pre>
     * MESH_NO_FILL
     * MESH_TRANS_FILL
     * MESH_SOLID_FILL
     * NO_MESH_TRANS_FILL
     * NO_MESH_SOLID_FILL
     * </pre>
     * 
     * @see org.scec.geo3d.plugins.CommunityFaultModelPlugin.components.MeshStateIcon
     * @param meshState the appearance style to use
     * @param row (fault) to set
     */
    public void setMeshStateForRow(int meshState, int row) {
    //    if (getLoadedStateForRow(row)) {
            ((FaultAccessor)getObjectAtRow(row)).setMeshState(meshState);
            fireTableCellUpdated(row, 2);
    //    }
    }
    
    /**
     * Sets the mesh/appearance state of particular rows. 
     * Possible values are:
     * <pre>
     * MESH_NO_FILL
     * MESH_TRANS_FILL
     * MESH_SOLID_FILL
     * NO_MESH_TRANS_FILL
     * NO_MESH_SOLID_FILL
     * </pre>
     * 
     * @see org.scec.geo3d.plugins.CommunityFaultModelPlugin.components.MeshStateIcon
     * @param meshState the appearance style to use
     * @param rows (faults) to set
     */
    public void setMeshStateForRows(int meshState, int[] rows) {
        for (int i=0; i<rows.length; i++) {
            setMeshStateForRow(meshState,rows[i]);
        }
    }
    
    /**
     * Toggles the mesh state of a fault at a particular <code>row</code>,
     * advancing it one further value.
     * 
     * @param row (fault) to change
     */
    public void toggleMeshStateForRow(int row) {
        int meshState = ((FaultAccessor)getObjectAtRow(row)).getMeshState();
        setMeshStateForRow((meshState+1)%5, row);
    }
    
    /**
     * Toggles the mesh state of the fault at all <code>rows</code>,
     * advancing them one further value.
     * 
     * @param rows (faults) to change
     */
    public void toggleMeshStateForRows(int[] rows) {
        // adjust mesh state for all based on mesh state of the first selection
        int meshState = MeshStateIcon.MESH_NO_FILL;
        for (int i=0; i<rows.length; i++) {
            if (getLoadedStateForRow(rows[i])) {
                meshState = getMeshStateForRow(rows[i]);
                break;
            }
        }
        for (int i=0; i<rows.length; i++) {
            setMeshStateForRow((meshState+1)%5, rows[i]);
        }
    }
    
    /**
     * Returns the mesh sate of a <code>FaultAccessor</code> at a given <code>row</code>.
     * 
     * @param row to get mesh state for
     * @return the mesh state value
     * @see MeshStateIcon
     */
    public int getMeshStateForRow(int row) {
        return ((FaultAccessor)getObjectAtRow(row)).getMeshState();
    }
    
    /**
     * Returns the number of columns in this/data model/table. Required method 
     * when extending <code>AbstractTableModel</code>.
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 4;
    }
    
}

