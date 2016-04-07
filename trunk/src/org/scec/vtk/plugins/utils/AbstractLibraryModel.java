package org.scec.vtk.plugins.utils;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.scec.vtk.main.MainGUI;


/**
 * This class provides a skeletal framework for managing and displaying a
 * group of <code>DataAccessor</code> objects in a table (one object per row).
 * Objects are stored in a <code>Vector</code> and subclasses must provide
 * implementation of:
 * <pre>
 * getColumnCount()
 * </pre>
 * This class provides several utility methods to add, remove, and retreive information
 * about <code>DataAccessor</code> objects.<br/>
 * <br/>
 * Since a data model acts as a delegate for <i>ScecVideo</i> objects, subclasses should
 * include any methods that modify the display properties of objects that are reflected
 * in icons, text, etc... displayed in the table. This ensures that the table always
 * reflects the current state of each object.<br/>
 * <br/>
 * <i>Note: For the time being no call is made to update display name strings in tables via
 * data models. It appears that after an <code>ObjectInfoDialog</code> closes, another full
 * rendering pass of the table is made and no <code>fireTable...</code> call is necessary.
 * This behavior should be verified across other platforms</i>.
 * 
 * Created on Feb 27, 2005
 * 
 * @author P. Powers
 * @version $Id: AbstractLibraryModel.java 2549 2008-07-24 15:52:05Z juve $
 */
public abstract class AbstractLibraryModel extends AbstractTableModel {

    private Logger log = Logger.getLogger(AbstractLibraryModel.class);
    
    /** <code>Vector</code> of <code>DataAccessor</code>s */
    protected Vector<DataAccessor> data = new Vector<DataAccessor>();
        
    /**
     * Always returns a <code>DataAccessor.class</code>; model subclasses should
     * implement methods to change <code>DataAccessor</code> fields and
     * <code>TableCellRenderer</code>'s to display them. Required method from
     * <code>AbstractTableModel</code>.
     * 
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    public Class<DataAccessor> getColumnClass(int col) {
        return DataAccessor.class;
    }
    
    /**
     * Returns the {@link DataAccessor} at a specific location in
     * this data model/table. Required method when extending 
     * <code>AbstractTableModel</code>. Cell renderers are responsible
     * for getting object properties. 
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
        return getObjectAtRow(row);
    }
    
    /**
     * Returns the number of rows in this data model/table. Required method 
     * when extending <code>AbstractTableModel</code>.
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return this.data.size();
    }
       
    /**
     * Removes all objects from model.
     */
    public void clear() {
        this.data.clear();
        fireTableDataChanged();
    }
    
    /**
     * Sets the visiblity of an object on a particular row <i>if</i> it is loaded.
     * 
     * @param show true=show; false=hide
     * @param row (object) to set
     */
    public void setVisibilityForRow(boolean show, int row) {
        if (getLoadedStateForRow(row) && getVisibilityForRow(row) != show) {
            getObjectAtRow(row).setDisplayed(show);
            fireTableCellUpdated(row, 0);
        }
    }

    /**
     * Sets the visiblity of objects on <code>rows</code>.
     * 
     * @param show true=show; false=hide
     * @param rows (objects) to set
     */
    public void setVisibilityForRows(boolean show, int[] rows) {
        for (int i=0; i<rows.length; i++) {
            setVisibilityForRow(show, rows[i]);
        }
        
    }
    
    /**
     * Toggles the visiblity of an object on a particular row.
     * 
     * @param row (object) to set
     */
    public void toggleVisibilityForRow(int row) {
        setVisibilityForRow(!getObjectAtRow(row).isDisplayed(), row);
    }
    
    /**
     * Toggles the visibility of the objects in each row based on the visibility
     * of the first selected and loaded object. First call to method will cause selected
     * objects to have uniform visibility settings; successive calls will toggle
     * visibility.
     * 
     * @param rows (objects) to set
     */
    public void toggleVisibilityForRows(int[] rows) {
        // adjust visibility of all based on visibility of first loaded object
        boolean show = true;
        for (int i=0; i<rows.length; i++) {
            if (getLoadedStateForRow(rows[i])) {
                show = !getVisibilityForRow(rows[i]);
                break;
            }
        }
        for (int i=0; i<rows.length; i++) {
            setVisibilityForRow(show, rows[i]);
        }
    }
    
    /**
     * Returns whether the <i>ScecVideo</i> object at <code>row</code>
     * is displayed or hidden.
     * 
     * @param row (fault) to be queried
     * @return visibility state (true=visible; false=hidden)
     */
    public boolean getVisibilityForRow(int row) {
        return getObjectAtRow(row).isDisplayed();
    }
    
    /**
     * Returns references to the objects that are currently visible in the
     * Java3D scenegraph.
     * 
     * @return list of visible objects
     */
    public ArrayList<DataAccessor> getVisibleObjects() {
        ArrayList<DataAccessor> visibleObjects = new ArrayList<DataAccessor>();
        for (int i=0; i<getRowCount(); i++) {
            if (getVisibilityForRow(i)) {
                visibleObjects.add(getObjectAtRow(i));
            }
        }
        return visibleObjects;
    }
    
    /**
     * Returns whether any objects in data model are visible.
     * 
     * @return true if at least one object is visible/painted/checked
     */
    public boolean anyAreVisible() {
        for (int i=0; i<this.getRowCount(); i++) {
            if (getObjectAtRow(i).isDisplayed()) return true;
        }
        return false;
    }

    /**
     * Sets whether an object's <code>DataAccessor</code> backing data store is 
     * loaded (such that object can now be displayed in Java3D scenegraph).
     * 
     * @param load whether to load or clear data arrays
     * @param row to act on
     */
    public void setLoadedStateForRow(boolean load, int row) {
        (getObjectAtRow(row)).setInMemory(load);
        fireTableRowsUpdated(row, row);
    }
    
    /**
     * Sets whether the data backing stores of the <code>DataAccessor</code>s at the
     * specified rows are loaded (such they can now be displayed in Java3D
     * scenegraph).
     * 
     * @param load whether to load or clear data arrays
     * @param rows to act on
     */
    public void setLoadedStateForRows(boolean load, int[] rows) {
        for (int i=0; i<rows.length; i++) {
            if (getLoadedStateForRow(rows[i]) != load) {
                setLoadedStateForRow(load, rows[i]);
            }
        }
    }
    
    /**
     * Returns whether the <i>ScecVideo</i> object at <code>row</code>
     * is loaded and can be displayed.
     * 
     * @param row (fault) to be queried
     * @return loaded state
     */
    public boolean getLoadedStateForRow(int row) {
        return getObjectAtRow(row).isInMemory();
    }
    
    /**
     * Returns references to the objects that are currently loaded and
     * displayable Java3D scenegraph.
     * 
     * @return list of loaded objects
     */
    public ArrayList<DataAccessor> getLoadedObjects() {
        ArrayList<DataAccessor> loadedObjects = new ArrayList<DataAccessor>();
        for (int i=0; i<getRowCount(); i++) {
            if (getLoadedStateForRow(i)) {
                loadedObjects.add(getObjectAtRow(i));
            }
        }
        return loadedObjects;
    }
    /**
     * Returns references to all objects that are displayable.
     * Used for Fault3DPlugin getState function (for saving
     * state information)
     * 
     * @return list of displayable objects
     */
    public ArrayList<DataAccessor> getAllObjects() {
        ArrayList<DataAccessor> loadedObjects = new ArrayList<DataAccessor>();
        for (int i=0; i<getRowCount(); i++) {
                loadedObjects.add(getObjectAtRow(i));
        }
        return loadedObjects;
    }
    /**
     * Returns whether any objects in data model are loaded (data in memory).
     * 
     * @return true if at least one object's backing store is loaded and the object
     * is displayable
     */
    public boolean anyAreLoaded() {
        for (int i=0; i<this.getRowCount(); i++) {
            if (getObjectAtRow(i).isInMemory()) return true;
        }
        return false;
    }
    
    /**
     * Returns whether any objects in the given rows are loaded (data in memory).
     * 
     * @param selectedRows table rows to check
     * @return true if at least one object's backing store is loaded and the object
     * is displayable
     */
    public boolean anyAreLoaded(int[] selectedRows) {
        for (int i=0; i<selectedRows.length; i++) {
            if (getObjectAtRow(selectedRows[i]).isInMemory()) return true;
        }
        return false;
    }
    
    /**
     * Returns whether all objects in the given rows are loaded (data in memory).
     * 
     * @param selectedRows table rows to check
     * @return true if all objects' backing stores are loaded and the objects
     * are displayable
     */
    public boolean allAreLoaded(int[] selectedRows) {
        for (int i=0; i<selectedRows.length; i++) {
            if (getObjectAtRow(selectedRows[i]).isInMemory()) continue;
            return false;
        }
        return true;        
    }
    
    /**
     * Returns whether any objects in the given rows are not loaded (data not in memory).
     * 
     * @param selectedRows table rows to check
     * @return true if all objects' backing stores are not loaded and the objects
     * are not displayable
     */
    public boolean noneAreLoaded(int[] selectedRows) {
        for (int i=0; i<selectedRows.length; i++) {
            if (!getObjectAtRow(selectedRows[i]).isInMemory()) continue;
            return false;
        }
        return true;
    }
    
    /**
     * Empty implementation provided because a <code>LibraryModel</code> is backed
     * by a 1D array of {@link DataAccessor}s. Rather than setting <i>ScecVideo</i> object
     * properties on a cell by cell basis, developers should write methods to
     * directly alter individual object properties and then call
     * {@link javax.swing.table.AbstractTableModel#fireTableRowsUpdated(int, int)}, 
     * {@link javax.swing.table.AbstractTableModel#fireTableDataChanged()}, or 
     * {@link javax.swing.table.AbstractTableModel#fireTableCellUpdated(int, int)}.
     * 
     * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt(Object value, int row, int col) {
        // empty implementation
    }
    
    /**
     * Saves the current display properties of all loaded <code>DataAccessor</code>s.
     * 
     */
    public void saveDisplayProperties() {
        for (int i=0; i<this.getRowCount(); i++) {
            getObjectAtRow(i).writeAttributeFile();
        }
    }
    
    /**
     * Adds a <code>DataAccessor</code> to data array/vector. This method checks if the object
     * is already loaded, sorts the data array alphabetically after loading it, and fires a
     * table change event.
     * 
     * @param object <code>DataAccessor</code> to be loaded
     */
    public void addObject(DataAccessor object) {        
        if (!this.contains(object)) {
            this.data.add(object);
            sort(this.data);
            fireTableDataChanged();
        }
    }
    
    /**
     * Adds all <code>DataAccessor</code> elements of a <code>List</code> to data
     * array/vector. This method checks if each object is already loaded,
     * sorts the data array alphabetically after loading non-duplicates,
     * and then fires a table change event. Objects in list must be of type
     * <code>DataAccessor</code>.
     * 
     * @param objects <code>DataAccessor</code>s to be loaded
     */
    public void addObjects(List objects) {
        if (objects.size() > 0) {
            for (int i=0; i<objects.size(); i++) {
                try {
                    DataAccessor obj = (DataAccessor)objects.get(i);
                    if (!this.contains(obj)) {
                        this.data.add(obj);
                    }
                } catch (ClassCastException e) {
                    log.debug("Class cast exception while adding objects");
                }
            }
            
            sort(this.data);
            
            fireTableDataChanged();
        }
    }
    
    /**
     * The point of this method is to eliminate warnings that
     * occur when you try to use generics with Collections.sort()
     */
    private void sort(List<DataAccessor> list) {
    	DataAccessor[] a = list.toArray(new DataAccessor[0]);
    	Arrays.sort(a);
    	ListIterator<DataAccessor> i = list.listIterator();
    	for(int j=0; j<a.length; j++) {
    		i.next();
    		i.set(a[j]);
    	}
	}
    
    /**
     * Deletes the specified <code>DataAccessor</code>'s from this table.
     * Method will delete attribute files and data backing store.
     * 
     * @param owner used for confirm dialog positioning
     * @param rows specifying objects for deletion
     * @return 
     */
    public int deleteObjects(Component owner, int[] rows) {
        // remove group from list
        int delete = JOptionPane.showConfirmDialog(
                owner,
                "Are you sure you want to delete the selected object(s)?\n" +
                "(All attribute and backing data will be deleted)",
                "Delete Object(s)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (delete == JOptionPane.NO_OPTION ||
            delete == JOptionPane.CLOSED_OPTION) return delete; 
        for (int i=0; i<rows.length; i++) {
        	if(getObjectAtRow(rows[i]-i).getAttributeFile()!=null)
        		getObjectAtRow(rows[i]-i).getAttributeFile().delete();
        	if(getObjectAtRow(rows[i]-i).getDataFile()!=null)
        		getObjectAtRow(rows[i]-i).getDataFile().delete();
            removeObjectAtRow(rows[i]-i);
        }
		return delete;
    }
    
    /**
     * Removes the <code>DataAccessor</code>s at a specified row in  the
     * data model/table. This method also removes the object from the Java3D
     * scenegraph if necessary.
     * 
     * @param row corresponding to the objecct to be removed/unloaded
     */
    public void removeObjectAtRow(int row) {
        DataAccessor obj = this.getObjectAtRow(row);
        if (obj.isDisplayed()) obj.setDisplayed(false);
        this.data.removeElementAt(row);
        fireTableRowsDeleted(row, row);
    }
    
    /**
     * Given an object source file, method finds and returns matching object from
     * data model. Returns <code>null</code> if object is not present.
     * 
     * @param objSource file reference to an object's attribute file
     * @return the matching object from data model or <code>null</code> if nonexistent
     */
    public DataAccessor findObject(File objSource) {
        for (int i=0; i<getRowCount(); i++) {
            if (getObjectAtRow(i).equals(objSource)) {
                return getObjectAtRow(i);
            }
        }
        return null;
    }
    
    /**
     * Tests whether a particular <code>DataAccessor</code> is present in the
     * current data model/table.
     * 
     * @param object to be checked for
     * @return whether object is already registered
     */
    public boolean contains(DataAccessor object) {
        for (int i=0; i<getRowCount(); i++) {
            if (getObjectAtRow(i).equals(object)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the position of a particular <code>DataAccessor</code>
     * in this data model/table.
     * 
     * @param object the object in question
     * @return position in data model
     */
    public int indexOf(Object object) {
        for (int i=0; i<this.data.size(); i++) {
            if (getObjectAtRow(i).equals(object)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Returns a refence to the <code>DataAccessor</code> object at a 
     * specified row.
     * 
     * @param row for which a object is to be returned
     * @return the <code>DataAccessor</code> object
     */
    public DataAccessor getObjectAtRow(int row) {
        return (DataAccessor)this.data.get(row);
    }
    
}

