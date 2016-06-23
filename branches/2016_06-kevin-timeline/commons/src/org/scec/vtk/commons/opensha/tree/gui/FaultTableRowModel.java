package org.scec.vtk.commons.opensha.tree.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.netbeans.swing.outline.RowModel;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.tree.AbstractFaultNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultSectionInfoViewier;
import org.scec.vtk.commons.opensha.tree.events.CustomColorSelectionListener;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;

public class FaultTableRowModel implements RowModel, MouseListener {
	
	private static final String[] colNames = { "Color", "Visible" };
	private static final Class<?>[] colClasses = { Color.class, Boolean.class };
	
	private CustomColorSelectionListener customColorListener;
	
	private FaultTreeTable table;
	
	private JFrame infoFrame;
	private FaultSectionInfoViewier infoViewer;
	
	public FaultTableRowModel(FaultTreeTable table) {
		this(table, null);
	}
	
	public FaultTableRowModel(FaultTreeTable table, FaultSectionInfoViewier infoViewer) {
		this.table = table;
		this.infoViewer = infoViewer;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return colClasses[col];
	}

	@Override
	public int getColumnCount() {
		return colNames.length;
	}

	@Override
	public String getColumnName(int col) {
		return colNames[col];
	}
	
	public void setCustomColorListener(CustomColorSelectionListener customColorListener) {
		this.customColorListener = customColorListener;
	}

	@Override
	public Object getValueFor(Object node, int col) {
		AbstractFaultNode fnode = AbstractFaultNode.checkCast(node);
		switch(col) {
		case 0:
			return fnode.getColor();
		case 1:
			return fnode.isVisible();
		}
		return null;
	}

	@Override
	public boolean isCellEditable(Object node, int col) {
//		if (col == 1)
			return true;
//		return false;
	}

	@Override
	public void setValueFor(Object node, int col, Object value) {
		//System.out.println("setValueFor called: " + node + ", "+col+", "+value);
		AbstractFaultNode fnode = AbstractFaultNode.checkCast(node);
		if (value instanceof Boolean)
			fnode.setVisible((Boolean)value);
		else if (value instanceof Color)
			fnode.setColor((Color)value);
	}

	@Override
	public void mouseClicked(MouseEvent mouseevent) {
		int col = table.columnAtPoint(mouseevent.getPoint());
		
		if (infoViewer != null && col == 0 && mouseevent.getClickCount() == 2) {
			// double clicked on a fault
			
			int row = table.rowAtPoint(mouseevent.getPoint());
			AbstractFaultNode node = table.nodeForRow(row);
			if (node instanceof FaultSectionNode) {
				AbstractFaultSection fault = ((FaultSectionNode)node).getFault();
				JPanel panel = infoViewer.getInfoPanel(fault);
				if (panel != null) {
					if (infoFrame == null) {
						infoFrame = new JFrame();
						infoFrame.getContentPane().setLayout(new BorderLayout());
					}
					infoFrame.getContentPane().removeAll();
					infoFrame.getContentPane().add(panel, BorderLayout.CENTER);
					infoFrame.pack();
					infoFrame.validate();
					infoFrame.setVisible(true);
					infoFrame.toFront();
				}
			}
			
		} else if (col == 1) { // color is col 1 now because it's with the table's numbering system
			int LastRow = table.rowAtPoint(mouseevent.getPoint());
			int rowCount = table.getSelectedRowCount();
			int selectedRow = table.getSelectedRow();

			/*System.out.println("LastROW: "+LastRow);
			System.out.println("rowCount: "+rowCount);
			System.out.println("SelectedRow: "+selectedRow);*/
			
			SingleColorChooser chooser = new SingleColorChooser(table);
			
			Color newColor = chooser.getColor();
			if (newColor != null) {
				for(int i = selectedRow; i<=LastRow; i++)
				{
				AbstractFaultNode node = table.nodeForRow(i);
				fireCustomColorSelectedEvent();
				table.setValueAt(newColor, i, col);
				node.setVisible((Boolean)true);
				}
			}
		}
	}
	
	private void fireCustomColorSelectedEvent() {
		if (customColorListener != null)
			customColorListener.customColorSelected();
	}

	@Override
	public void mouseEntered(MouseEvent mouseevent) {}

	@Override
	public void mouseExited(MouseEvent mouseevent) {}

	@Override
	public void mousePressed(MouseEvent mouseevent) {}

	@Override
	public void mouseReleased(MouseEvent mouseevent) {}

}
