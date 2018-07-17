package org.scec.vtk.plugins.GISHazusEventsPlugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.scec.vtk.plugins.utils.components.SingleColorChooser;
import org.scec.vtk.tools.Prefs;


public class BoundaryTable extends JTable implements ChangeListener,
		MouseListener {
	protected BoundaryTableModel model;

	private static final long serialVersionUID = 1L;

	public BoundaryTable(GISHazusEventsPluginGUI pbg, BoundaryTableModel boundaryTableModel) {
		super(boundaryTableModel);
		model = boundaryTableModel;
		addMouseListener(this);
		setTableHeader(null);
		getColumnModel().getColumn(0).setPreferredWidth(35);
		getColumnModel().getColumn(0).setMaxWidth(35);
		getColumnModel().getColumn(0).setCellRenderer(
				new BoundaryCheckBoxRenderer());
		getColumnModel().getColumn(1).setPreferredWidth(35);
		getColumnModel().getColumn(1).setMaxWidth(35);
		getColumnModel().getColumn(2).setCellRenderer(
				new BoundaryStringRenderer());

	}

	/**
	 * Custom renderer class draws <code>String</code> objects.
	 *
	 * Created on Jan 30, 2005
	 * 
	 * @author P. Powers
	 * @version $Id: BoundaryTable.java 1840 2006-12-23 07:21:33Z rapp $
	 */
	private class BoundaryStringRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		/**
		 * Constructs a new <code>StringRenderer</code>.
		 */
		public BoundaryStringRenderer() {
			super();
			setHorizontalAlignment(SwingConstants.LEFT);
		}

		/**
		 * Required method of custom cell renderers that gets called to 
		 * render <code>String</code> cells.
		 * 
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table,
				Object object, boolean isSelected, boolean hasFocus, int row,
				int column) {
			this.setText(((String) model.getValueAt(row, column)).replace('_', ' '));
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
	 * @version $Id: BoundaryTable.java 1840 2006-12-23 07:21:33Z rapp $
	 */
	public class BoundaryCheckBoxRenderer extends JCheckBox implements
			TableCellRenderer {

		// this class includes a number of methods that are overridden for performance
		// reasons as specified in TableCellRenderer

		private static final long serialVersionUID = 1L;

		/**
		 * Constructs a new <code>CheckBoxRenderer</code>.
		 */
		public BoundaryCheckBoxRenderer() {
			super();
		}

		/**
		 * Required method of custom cell renderers that gets called to render 
		 * <code>JCheckBox</code> table cells.
		 * 
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		//not used
		public Component getTableCellRendererComponent(JTable table,
				Object object, boolean isSelected, boolean hasFocus, int row,
				int column) {

			if (isSelected) {
				setBackground(table.getSelectionBackground());
			} else {
				if ((row % 2) == 0) {
					setBackground(Prefs.getStripingColor());
				} else {
					setBackground(table.getBackground());
				}
			}
			BoundaryTable hwtb = (BoundaryTable) table;
			boolean displayed = hwtb.model.isDisplayed(row);
			this.setSelected(displayed);
			return this;
		}
	}

	public void stateChanged(ChangeEvent e) {
		int[] rows = this.getSelectedRows();
		if (rows.length > 0) {
			Color c = ((DefaultColorSelectionModel) e.getSource()).getSelectedColor();
			this.model.setColorForRows(c, rows);
		}
	}

	public void mouseClicked(MouseEvent e) {

		// Get column and row values for X and Y clicked
		int col = this.getColumnModel().getColumnIndexAtX(e.getX());
		int row = e.getY() / getRowHeight();

		if (col == 1 && model.getColorForRow(row) != null) {
			// restrict showing color chooser to clicks that fall on non-null color wells
			Color tempColor = new SingleColorChooser(this).getColor();
			if (tempColor != null) {
				Color newColor = tempColor;
				if (newColor != null) {
					model.setColorForRows(newColor, getSelectedRows());
				}
			}
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
