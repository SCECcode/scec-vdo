package org.scec.vtk.plugins.GISHazusEventsPlugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.scec.vtk.plugins.utils.components.ColorWellIcon;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;
import org.scec.vtk.tools.Prefs;



/**
 * customize the JTable according to our needs
 * @author vipingupta
 *
 */
public class BoundSectionsTable extends JTable implements MouseListener {
	private static final long serialVersionUID = 1L;
	protected BoundSectionsTableModel model;
	private GISHazusEventsPluginGUI owner;
	/**
	 * @param dm
	 */
	public BoundSectionsTable(GISHazusEventsPluginGUI pbg, TableModel dm) {
		super(dm);
		this.owner = pbg;
		this.model = (BoundSectionsTableModel)dm;
		addMouseListener(this);
		getTableHeader().setReorderingAllowed(false);
		

		TableColumn col0 = getColumnModel().getColumn(0);
		col0.setCellRenderer(new BoundGroupCheckBoxRenderer());
		col0.setPreferredWidth(35);
        col0.setMaxWidth(65);
		
		TableColumn col1 = getColumnModel().getColumn(1);
		col1.setCellRenderer(new ColorIconRenderer());
		col1.setPreferredWidth(35);
        col1.setMaxWidth(65);
        
        TableColumn col2 = getColumnModel().getColumn(2);
		col2.setCellRenderer(new BoundGroupStringRenderer());
	}
	
	public void mouseClicked(MouseEvent event) {		
		Point p = event.getPoint();
        int row = rowAtPoint(p);
        int column = columnAtPoint(p); // This is the view column!
        if(column==1)  { // change the color
        	if(!(Boolean)model.getValueAt(row, 3))
        		model.setValueAt(true, row, 0);
        	Color color = new SingleColorChooser(owner).getColor();
        	if(color!=null) getModel().setValueAt(color, row, column);
        }

	}

	public void mousePressed(MouseEvent e) {
	}
	
	public void mouseReleased(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}
	
}

// display the color icon to display the color used to draw the fault section
class ColorIconRenderer extends DefaultTableCellRenderer   {
	private static final long serialVersionUID = 1L;
	private ColorWellIcon colorIcon = new ColorWellIcon(Color.WHITE, 11, 11, 2);
	
	public ColorIconRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	   public Component getTableCellRendererComponent(
               JTable table, Object color,
               boolean isSelected, boolean hasFocus,
               int row, int column) {

           setIcon(this.colorIcon);
           setDisabledIcon(this.colorIcon);
           Color temp2 = (Color)color;
           this.colorIcon.setColor(temp2);
           
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
 * Custom renderer class draws <code>String</code> objects.
 *
 * Created on Jan 30, 2005
 * 
 * @author P. Powers
     * @version $Id: BoundSectionsTable.java 1840 2006-12-23 07:21:33Z rapp $
 */
class BoundGroupStringRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;
	/**
     * Constructs a new <code>StringRenderer</code>.
     */
    public BoundGroupStringRenderer() {
        super();
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
    	BoundSectionsTable hwtb = (BoundSectionsTable)table;
    	this.setText((String)(hwtb.model.getValueAt(row, column)));
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            if ((row % 2) == 0) {
                this.setBackground(Prefs.getStripingColor());
            } else {
                setBackground(table.getBackground());
            }
        }                       
        return this;
    }
}


/**
 * Custom renderer class draws <code>JCheckBox</code> objects for table cells. This
 * class includes a number of methods that are overridden for performance
 * reasons as specified in <code>TableCellRenderer</code>.
 * 
 * Created on Feb 27, 2005
 * 
 * @author P. Powers
 * @version $Id: BoundSectionsTable.java 1840 2006-12-23 07:21:33Z rapp $
 */
class BoundGroupCheckBoxRenderer extends JCheckBox implements TableCellRenderer {

    // this class includes a number of methods that are overridden for performance
    // reasons as specified in TableCellRenderer
    
    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>CheckBoxRenderer</code>.
     */
    public BoundGroupCheckBoxRenderer() {
        super();
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
                  
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            if ((row % 2) == 0) {
                setBackground(Prefs.getStripingColor());
            } else {
                setBackground(table.getBackground());
            }
        }     
       BoundSectionsTable hwtb = (BoundSectionsTable)table;
       boolean displayed = (Boolean)hwtb.model.getValueAt(row, 0);
        this.setSelected(displayed);
        return this;          
    }
}


