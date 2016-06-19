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
		this.tableModel = new DrawingToolsTableModel();
		this.tableModel.addTableModelListener((TableModelListener)this.tableOwner);
		setModel(this.tableModel);
		// set to monitor mouse clicks
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				// Get column and row values for X and Y clicked
				DrawingToolsTableModel libModel = DrawingToolsTable.this.getLibraryModel();
				DrawingToolsGUI gui = (DrawingToolsGUI)DrawingToolsTable.this.tableOwner;
				int col = DrawingToolsTable.this.getColumnModel().getColumnIndexAtX(e.getX());
				int row = e.getY() / getRowHeight();

				if (col == 0) {
					if(!libModel.getLoadedStateForRow(row)){
						libModel.setLoadedStateForRow(true, row);
						gui.processTableSelectionChange();
					}
					//libModel.toggleVisibilityForRow(row);
				} /*else if (col == 1 && libModel.getColorForRow(row) != null) {
                	// restrict showing color chooser to clicks that fall on non-null color wells
                	Color newColor = null;//((CommunityFaultModelGUI)FaultTable.this.tableOwner).getColorChooser().getColor();
                	if (newColor != null) {
                		libModel.setColorForRows(newColor, getSelectedRows());
                	}
                } else if (col == 2) {
                	libModel.toggleMeshStateForRow(row);
                }*/
			}
		});

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
	public void addDrawingTool(ArrayList<DrawingTool> drawringTool) {
		this.tableModel.addObjects(drawringTool);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub

	}

	public ArrayList<DrawingTool> getSelected() {
		// TODO Auto-generated method stub
		DrawingToolsTableModel libModel = DrawingToolsTable.this.getLibraryModel();
		ArrayList<DrawingTool> selectedObjects = new ArrayList<>();
		for (int i=0; i<libModel.getRowCount(); i++) {
			if(libModel.getLoadedStateForRow(i)){
				selectedObjects.add((DrawingTool)this.tableModel.getObjectAtRow(i));
			}
		}
		return selectedObjects;
	}
}