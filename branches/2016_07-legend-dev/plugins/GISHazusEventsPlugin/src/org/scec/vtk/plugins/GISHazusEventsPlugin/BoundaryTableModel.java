package org.scec.vtk.plugins.GISHazusEventsPlugin;

import java.awt.Color;

import javax.swing.table.AbstractTableModel;


public class BoundaryTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private FilledBoundaryCluster[] data;

	public BoundaryTableModel(FilledBoundaryCluster[] b) {
		data = b;
	}

	public int getRowCount() {
		return data.length;
	}

	public int getColumnCount() {
		return 3;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return data[rowIndex].isDisplayed();
		} else if (columnIndex == 1) {
			return data[rowIndex].getColor();
		} else if (columnIndex == 2) {
			return data[rowIndex].getName();
		} else {
			return null;
		}
	}

	public boolean isDisplayed(int rowIndex) {
		return data[rowIndex].isDisplayed();
	}

	public Class getColumnClass(int col) {
		return getValueAt(0, col).getClass();
	}

	public boolean isCellEditable(int row, int col) {
		if (col == 0 || col == 1) {
			return true;
		} else {
			return false;
		}
	}

	public void setValueAt(Object value, int row, int col) {
		if (col == 0) {
			data[row].setDisplayed((Boolean) value);
		} else if (col == 1) {
			data[row].setColor((Color) value);
		}
		fireTableCellUpdated(row, col);
	}

	public void setColorForRow(Color c, int row) {
		// check that row/fault isInMemory and color is non-null
		if (data[row] != null) {
			data[row].setColor(c);
			fireTableCellUpdated(row, 1);
		}
	}

	public Color getColorForRow(int row) {
		return data[row].getColor();
	}

	public void setColorForRows(Color c, int[] rows) {
		for (int i = 0; i < rows.length; i++) {
			setColorForRow(c, rows[i]);
		}
	}
}
