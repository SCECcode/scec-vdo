package org.scec.vtk.plugins.utils.components;

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.scec.vtk.tools.Prefs;

import org.scec.vtk.plugins.utils.DataAccessor;

/**
 * Custom renderer class draws <code>JCheckBox</code> objects for table cells. This
 * class includes a number of methods that are overridden for performance
 * reasons as specified in <code>TableCellRenderer</code>.
 * 
 * Created on Feb 27, 2005
 * 
 * @author P. Powers
 * @version $Id: CheckBoxRenderer.java 385 2006-07-13 22:37:08Z chumrick $
 */
public class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

    // this class includes a number of methods that are overridden for performance
    // reasons as specified in TableCellRenderer
    
    private static final long serialVersionUID = 1L;
    private boolean accessor = true;

	/**
     * Constructs a new <code>CheckBoxRenderer</code>.
     */
    public CheckBoxRenderer() {
        super();
    }
    public CheckBoxRenderer(boolean acc) {
    	super();
    	this.accessor = acc;
    }
    /**
     * Required method of custom cell renderers that gets called to render 
     * <code>JCheckBox</code> table cells.
     * 
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(
            JTable table, Object object,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        if(accessor)
        	this.setEnabled(((DataAccessor)object).isInMemory());
        
        this.setEnabled(true);
        
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            if ((row % 2) == 0) {
                setBackground(Prefs.getStripingColor());
            } else {
                setBackground(table.getBackground());
            }
        }
        
        if(accessor)
        {
        boolean b = ((DataAccessor)object).isDisplayed();
        this.setSelected(b);
        }
        return this;
        
    }
    
    /**
     * Overridden for performance reasons.
     * 
     * @see javax.swing.table.DefaultTableCellRenderer
     * @see javax.swing.JComponent#firePropertyChange(java.lang.String, boolean, boolean)
     */
    public void firePropertyChange(String propertyName, boolean oldValue,  boolean newValue) {
        // overridden for performance
    }
    
    /**
     * Overridden for performance reasons.
     * 
     * @see javax.swing.table.DefaultTableCellRenderer
     * @see javax.swing.JComponent#firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
     */
    protected void firePropertyChange(String propertyName,  Object oldValue,  Object newValue) {
        // overridden for performance
    }
    
    /**
     * Overridden for performance reasons.
     * 
     * @see javax.swing.table.DefaultTableCellRenderer
     * @see javax.swing.JComponent#isOpaque()
     */
    public boolean isOpaque() {
        return true;
    }
    
    /**
     * Overridden for performance reasons.
     * 
     * @see javax.swing.table.DefaultTableCellRenderer
     * @see javax.swing.JComponent#repaint(long, int, int, int, int)
     */
    public void repaint(long tm,  int x,  int y,  int width,  int height) {
        // overridden for performance
    }
    
    /**
     * Overridden for performance reasons.
     * 
     * @see javax.swing.table.DefaultTableCellRenderer
     * @see javax.swing.JComponent#repaint(java.awt.Rectangle)
     */
    public void repaint(Rectangle r) {
        // overridden for performance
    }
    
    /**
     * Overridden for performance reasons.
     * 
     * @see javax.swing.table.DefaultTableCellRenderer
     * @see javax.swing.JComponent#revalidate()
     */
    public void revalidate() {
        // overridden for performance
    }

    /**
     * Overridden for performance reasons.
     * 
     * @see javax.swing.table.DefaultTableCellRenderer
     * @see java.awt.Container#validate()
     */
    public void validate() {
        // overridden for performance
    }
}
