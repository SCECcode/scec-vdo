package org.scec.vtk.drawingTools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

import org.scec.vtk.plugins.utils.DataAccessor;

public class DrawingToolsTable  extends JTable implements ChangeListener {

	private static final long serialVersionUID = 1L;

	// owner (default visibility for nested components)
	Component tableOwner;

	// table access fields
	private DrawingToolsTableModel tableModel;
	private ListSelectionModel selModel;
	String[] columnNames ={"Labels"};

	/**
	 * Constructs a new <code>FaultTable</code> with the specified owner, i.e <code>FaultGUI</code>.
	 *
	 * @param owner parent <code>Container</code> that is registered for various event notifications
	 */
	public DrawingToolsTable(Component owner) {
		super();
		this.tableOwner = owner;
		this.init();
	}

	private void init() {
		// TODO Auto-generated method stub
		this.tableModel = new DrawingToolsTableModel(columnNames);
		this.tableModel.addTableModelListener((TableModelListener)this.tableOwner);
		setModel(this.tableModel);
	

		// Set up selection model and register GUI as listener for 
		// button en/disabling.
		this.selModel = getSelectionModel();
		this.selModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.selModel.addListSelectionListener((ListSelectionListener)this.tableOwner);

		// visual set up
		this.setTableHeader(null);
		this.setRowHeight(getRowHeight()+4);
		this.setIntercellSpacing(new Dimension(0,0));
		this.setShowGrid(false);
	}

	protected DrawingToolsTableModel getLibraryModel() {
		// TODO Auto-generated method stub
		return this.tableModel;
	}
	public void addDrawingTool(DrawingTool drawringTool) {
		Object[] rowData = { 
				drawringTool.getDisplayName()
		} ;
		this.tableModel.addRow(rowData);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub

	}


}