
package org.scec.vtk.plugins.utils.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.scec.vtk.plugins.utils.DataAccessor;

import org.scec.vtk.tools.Prefs;
    
/**
 * Custom renderer class draws <code>String</code> objects.
 *
 * Created on Jan 30, 2005
 * 
 * @author P. Powers
 * @version $Id: StringRenderer.java 385 2006-07-13 22:37:08Z chumrick $
 */
public class StringRenderer extends DefaultTableCellRenderer {
    
    private static final long serialVersionUID = 1L;
    private boolean accessor = true;
	/**
     * Constructs a new <code>StringRenderer</code>.
     */
    public StringRenderer() {
        super();
        setHorizontalAlignment(SwingConstants.LEFT);
    }
    public StringRenderer(boolean acc)
    {
    	super();
    	this.accessor = acc;
    	setHorizontalAlignment(SwingConstants.LEFT);
    }
    
    /**
     * Required method of custom cell renderers that gets called to 
     * render <code>String</code> cells.
     * 
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(
            JTable table, Object object,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        
        if(accessor)
        {
        	this.setText(((DataAccessor)object).getDisplayName());
        }
        // we don't rely on selected state b/c disabled text renders
        // diferently with striped background
        if(accessor)
        {
	        if (!((DataAccessor)object).isInMemory()) {
	            this.setForeground(Color.GRAY);
	        } else {
	            this.setForeground(table.getForeground());
	        }
        }
        this.setForeground(table.getForeground());

        
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(Color.WHITE);
        } else {
        	setForeground(Color.BLACK);
            if ((row % 2) == 0) {
                this.setBackground(Prefs.getStripingColor());
            } else {
                this.setBackground(table.getBackground());
            }
        }
                    
        return this;
    }
}
