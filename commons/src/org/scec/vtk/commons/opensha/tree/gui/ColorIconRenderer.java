package org.scec.vtk.commons.opensha.tree.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.scec.vtk.plugins.utils.components.ColorWellIcon;

public class ColorIconRenderer extends DefaultTableCellRenderer   {
	public static final Color NULL_COLOR = Color.GRAY;
	
	private static final long serialVersionUID = 1L;
	private static int iconHeight = 11;
	private ColorWellIcon colorIcon = new ColorWellIcon(NULL_COLOR, iconHeight, iconHeight, 2);

	public ColorIconRenderer() {
		setIcon(colorIcon);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object obj, boolean isSelected, boolean hasFocus, int row,
			int column) {
		if(isSelected)
			setBackground(table.getSelectionBackground());
		else
			setBackground(table.getBackground());
		int iconWidth = table.getColumnModel().getColumn(column).getWidth() - 4;
		Color color = (Color)table.getModel().getValueAt(row, column);
		if (color == null)
			color = NULL_COLOR;
		colorIcon.setDimensions(iconWidth, iconHeight);
		colorIcon.setColor(color);
		return this;
	}
}
