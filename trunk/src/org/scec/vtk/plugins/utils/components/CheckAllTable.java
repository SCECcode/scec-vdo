package org.scec.vtk.plugins.utils.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;


public class CheckAllTable extends JPanel {
	private static final long serialVersionUID = 1L;
	private Object[][] DATA;
    private static final int CHECK_COL = 0;
    private DataModel dataModel;
    private JTable table;
    private DefaultListSelectionModel selectionModel;
    private TableModelListener tableListener;
    private Filter filter;
    JTextField searchBar;
    
    public CheckAllTable(ArrayList<String> data, String title, TableModelListener tableListener) {
    	super(new BorderLayout());
    	DATA =new Object[data.size()][2];
    	for (int i = 0; i < data.size(); i++) {
			DATA[i][0] = Boolean.FALSE;
			DATA[i][1] = data.get(i);
		}
    	initTable(title, tableListener);
    	
    }
    public CheckAllTable(Object[][] data, String title, TableModelListener tableListener) {
        super(new BorderLayout());
        DATA = data;
        initTable(title, tableListener);
    }
    /**
     * Sets up all initial GUI elements
     * @param title
     * @param tableListener
     */
    private void initTable(String title, TableModelListener tableListener) {
        setLayout(new BorderLayout());
        String[] COLUMN_HEADERS = {" ", title};
        dataModel = new DataModel(DATA, COLUMN_HEADERS);
        //prepareRenderer sets a renderer for the whole table style
        table = new JTable(dataModel) {
        	public Component prepareRenderer(
        		TableCellRenderer renderer, int row, int column) {
        		Component c = super.prepareRenderer(renderer, row, column);
        		JComponent jc = (JComponent)c;
        		if (!isRowSelected(row))
        			c.setBackground(row % 2 == 0 ? getBackground() : new Color(255, 254, 238));
        		if (isRowSelected(row)) 
        			jc.setBorder(new EmptyBorder(0, 0, 0, 0));
        		return c;
        	}
        };
        this.tableListener = tableListener;
        table.getModel().addTableModelListener(tableListener);
        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setShowGrid(false);
        table.setRowHeight(25);
        table.setShowHorizontalLines(false);
        TableCellRenderer rendererFromHeader = table.getTableHeader().getDefaultRenderer();
        JLabel headerLabel = (JLabel) rendererFromHeader;
        headerLabel.setHorizontalAlignment(JLabel.CENTER);
        this.add(new JScrollPane(table), BorderLayout.CENTER);
        ControlPanel controlPanel = new ControlPanel();
        this.add(controlPanel, BorderLayout.PAGE_END);
        table.setPreferredScrollableViewportSize(new Dimension(250, 175));
        selectionModel = (DefaultListSelectionModel) table.getSelectionModel();
        filter = new Filter();
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
        sorter.setRowFilter(filter);
        table.setRowSorter(sorter);
    }
    public Object[][] getData() {
    	return DATA;
    }
    public void setDataModel(Object[][] data, String[] columnHeader) {
    	DataModel newDataModel = new DataModel(DATA, columnHeader);
    	table.setModel(newDataModel);
    	table.getModel().addTableModelListener(tableListener);
    }
    
    public void setCheckBox(boolean value, int row, int column) {
    	table.setValueAt(value, row, column);
    }
    DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
    	Border padding = BorderFactory.createEmptyBorder(0, 0, 0, 0);
    	@Override
    	public Component getTableCellRendererComponent(JTable table,
    			Object value, boolean isSelected, boolean hasFocus,
    			int row, int column) {
    		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
    				row, column);
    		setBorder(BorderFactory.createCompoundBorder(getBorder(), padding));
    		return this;
    	}
    };
    /**
     * 
     * @return - the state of the table
     */
    public JTable getTable() {
    	return table;
    }
    /**
     * DataModel to store the table data
     * @author intern
     *
     */
    private class DataModel extends DefaultTableModel {

        public DataModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == CHECK_COL) {
                return getValueAt(0, CHECK_COL).getClass();
            }
            return super.getColumnClass(columnIndex);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == CHECK_COL;
        }
    }
    /**
     * Contains buttons for select and deselect and has search bar.
     * @author intern
     *
     */
    private class ControlPanel extends JPanel {
        public ControlPanel() {
//           / this.add(new JLabel("Selection:"));
            this.add(new JButton(new SelectionAction("Clear", false)));
            this.add(new JButton(new SelectionAction("Check", true)));
        	KeyListener keyListener = new KeyListener() {
        		@Override
        		public void keyTyped(KeyEvent e) {
        			// TODO Auto-generated method stub
        			
        		}
        		
        		@Override
        		public void keyReleased(KeyEvent e) {
        			// TODO Auto-generated method stub
        			String text = searchBar.getText();
        			filter.swapPrefix(text); 
        			table.getRowSorter().allRowsChanged();
        		}
        		
        		@Override
        		public void keyPressed(KeyEvent e) {
        			// TODO Auto-generated method stub
        			
        		}
        	};
            searchBar = new JTextField(10);
    		searchBar.addKeyListener(keyListener);
    		this.add(searchBar);
        }
    }
    /**
     * Handles selection on the table
     * @author intern
     *
     */
    private class SelectionAction extends AbstractAction {
        boolean value;
        public SelectionAction(String name, boolean value) {
            super(name);
            this.value = value;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < dataModel.getRowCount(); i++) {
                if (selectionModel.isSelectedIndex(i)) {
                    dataModel.setValueAt(value, table.convertRowIndexToModel(i), CHECK_COL);
                }
            }
        }
    }
    /**
     * Handles search filtering 
     * @author intern
     *
     */
    private static class Filter extends RowFilter<TableModel, Integer> {
        private String includePrefix = "";
        @Override
        public boolean include(javax.swing.RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
        	return entry.getStringValue(1).toLowerCase().startsWith(includePrefix);
        }
        // Calling this method changes the filter to allow a different prefix
        public void swapPrefix(String text) {
            this.includePrefix = text.toLowerCase();
        }
    }
}