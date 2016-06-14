package org.scec.geo3d.commons.opensha.tree.gui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeModel;

public class TreeTableCellRenderer extends JTree implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected int visibleRow;
	private JTable table;

	public TreeTableCellRenderer(TreeModel model, JTable table) { 
		super(model); 
		setRootVisible(false);
		this.table = table;
	}

	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, 0, w, table.getHeight());
	}

	public void paint(Graphics g) {
		g.translate(0, -visibleRow * getRowHeight());
		super.paint(g);
	}

	public Component getTableCellRendererComponent(JTable table,
			Object value,boolean isSelected, boolean hasFocus,
			int row, int column) 
	{
		if(isSelected)
			setBackground(table.getSelectionBackground());
		else
			setBackground(table.getBackground());


		visibleRow = row;
		return this;
	}
}