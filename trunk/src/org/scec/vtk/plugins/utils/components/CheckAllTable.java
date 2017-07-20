package org.scec.vtk.plugins.utils.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.jfree.data.time.TimeSeriesTableModel;
import org.scec.vtk.main.Info;

import com.lowagie.text.Row;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import jdk.internal.org.objectweb.asm.tree.IntInsnNode;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;


public class CheckAllTable extends JPanel {
	private static final long serialVersionUID = 1L;
	private Object[][] DATA;
	private String TITLE;
    private static final int CHECK_COL = 0;
    private static final int COLOR_COL = 2;
    private DataModel dataModel;
    private JTable table;
    private DefaultListSelectionModel selectionModel;
    private CheckAllTable parentTable;
    private ArrayList<CheckAllTable> childrenTables;
    ControlPanel controlPanel;
    int itsRow =0;
    int itsColumn = 0;
    private TableModelListener tableListener;

    private Filter filter;
    JTextField searchBar;
    
    //Intermediate Table
    public CheckAllTable(ArrayList<String> data, String title, TableModelListener tableListener) {
    	super(new BorderLayout());
    	DATA =new Object[data.size()][3];
    	for (int i = 0; i < data.size(); i++) {
			DATA[i][0] = Boolean.FALSE;
			DATA[i][1] = data.get(i);
			DATA[i][2] = Color.white;
		}
    	initTable(title, tableListener);
    }
    
    public CheckAllTable(Object[][] data, String title, TableModelListener tableListener) {
        super(new BorderLayout());
        DATA = data;
        initTable(title, tableListener);
    }
    public CheckAllTable(ArrayList<String> data, String title) {
    	super(new BorderLayout());
    	DATA =new Object[data.size()][3];
    	for (int i = 0; i < data.size(); i++) {
			DATA[i][0] = Boolean.FALSE;
			DATA[i][1] = data.get(i);
			DATA[i][2] = Color.white;
		}
    	
    	TableModelListener tableListener = new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
    	initTable(title, tableListener);
    }
    public CheckAllTable(Object[][] data, String title) {
        super(new BorderLayout());
        DATA = data;
    	TableModelListener tableListener = new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
        initTable(title, tableListener);
    }

    /**
     * Sets up all initial GUI elements
     * @param title
     * @param tableListener
     */
    private void initTable(String title, TableModelListener tableListener) {
    	TITLE = title;
        this.setLayout(new BorderLayout());
        this.setOpaque(false);
        String[] COLUMN_HEADERS = {" < ", title, ""};
        dataModel = new DataModel(DATA, COLUMN_HEADERS);
        //Add control panel
        controlPanel = new ControlPanel();
        controlPanel.setOpaque(false);
        controlPanel.setBorder(BorderFactory.createEmptyBorder());
        this.add(controlPanel, BorderLayout.NORTH);
        
        childrenTables = new ArrayList<CheckAllTable>();
        
        //prepareRenderer sets a renderer for the whole table style
        table = new JTable(dataModel) {
        	public Component prepareRenderer(
        		TableCellRenderer renderer, int row, int column) {
        		Component c = super.prepareRenderer(renderer, row, column);
        		JComponent jc = (JComponent)c;
        		if (!isRowSelected(row) && column != 2) {
        			c.setBackground(row % 2 == 0 ? getBackground() : new Color(240, 254, 255));
        		}
        		return c;
        	}
        };

        table.getModel().addTableModelListener(tableListener);
        
        selectionModel = (DefaultListSelectionModel) table.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        //Add searching functionality using a filter
        filter = new Filter();
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
        sorter.setRowFilter(filter);
        sorter.setSortable(0, false);
        table.setRowSorter(sorter);
        setTableProperties();
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        this.add(tableScrollPane, BorderLayout.CENTER);
    }
    
    private void setTableProperties() {
    	//Insert properties for rendering 
    	table.getColumnModel().getColumn(0).setMaxWidth(60);
    	table.getColumnModel().getColumn(1).setCellRenderer(textRenderer);
    	table.getColumnModel().getColumn(2).setMaxWidth(0);
    	table.getColumnModel().getColumn(2).setMinWidth(0);
    	table.getColumnModel().getColumn(2).setPreferredWidth(0);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setShowGrid(false);
        table.setRowHeight(25);
        table.setShowHorizontalLines(false);
        table.setPreferredScrollableViewportSize(new Dimension(250, 175));
        //Center table title
        renderTableHeader();  
    }
    
    public void renderTableHeader() {
        TableCellRenderer rendererFromHeader = table.getTableHeader().getDefaultRenderer();
        JLabel headerLabel = (JLabel) rendererFromHeader;
        headerLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));
        headerLabel.setHorizontalAlignment(JLabel.CENTER);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
    }
    
    public void setDataModel(Object[][] data, String[] columnHeader) {
    	DataModel newDataModel = new DataModel(DATA, columnHeader);
    	table.setModel(newDataModel);
    	table.getModel().addTableModelListener(tableListener);
    	//lol
    }
    
    public void addControlColumn(MouseAdapter mouseListener, String controlSymbol, TreeNode<CheckAllTable> tableNode) {
    	table.addMouseListener(mouseListener);
    	Object[] controlSymbols = new Object[table.getRowCount()];
    	for (int row = 0 ; row < controlSymbols.length; row++) {
    		if (hasSubTable(row, tableNode))
    			controlSymbols[row] = controlSymbol;
    		else
    			controlSymbols[row] = "";
    	}
    	dataModel.addColumn("", controlSymbols);
        table.getColumnModel().getColumn(table.getColumnCount()-1).setCellRenderer(forwardArrowRenderer);
        table.getColumnModel().getColumn(table.getColumnCount()-1).setMaxWidth(30);
        table.addMouseMotionListener(hoverListener);
        setTableProperties();
    }
    
    public boolean hasSubTable(int row, TreeNode<CheckAllTable> tableNode) {
    	for (TreeNode<CheckAllTable> node : tableNode) {
    		if (node.data.getTitle().equals(table.getValueAt(row, 1))) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public void addButtonToControlPanel(JButton button, ActionListener actionListener) {
    	button.addActionListener(actionListener);
    	controlPanel.add(button);
    }
    

    DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
    	Border padding = BorderFactory.createEmptyBorder(0, 10, 0, 0);
    	@Override
    	public Component getTableCellRendererComponent(JTable table,
    			Object value, boolean isSelected, boolean hasFocus,
    			int row, int column) {
    		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
    				row, column);
    		setBorder(padding);
    		table.getModel().getValueAt(row, 2);
    		return this;
    	}
    };
    DefaultTableCellRenderer forwardArrowRenderer = new DefaultTableCellRenderer() {
    	Border padding = BorderFactory.createEmptyBorder(0, 0, 0, 0);
    	@Override
    	public Component getTableCellRendererComponent(JTable table,
    			Object value, boolean isSelected, boolean hasFocus,
    			int row, int column) {
    		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
    				row, column);
    		setBorder(padding);
    		setHorizontalAlignment(JLabel.CENTER);
    		setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
    		if(row == itsRow && column == itsColumn) {
    			this.setForeground(new Color(127, 255, 0));
    		}
    		else {
    			this.setForeground(Color.DARK_GRAY);
    		}
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
    public String getTitle() {
    	return TITLE;
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
    public void addColorButton(ActionListener actionListener) {
    	ColorButton colorDrawingToolsButton = new ColorButton(actionListener, "Change color");
    	colorDrawingToolsButton.setEnabled(true);
    	controlPanel.add(colorDrawingToolsButton, FlowLayout.LEFT);
    }
    public class ControlPanel extends JPanel {
        public ControlPanel() {
        	this.setBorder(BorderFactory.createEmptyBorder());
        	this.setLayout(new FlowLayout(FlowLayout.LEADING));
        	JButton selectButton = new JButton(new SelectionAction("Select", true));
        	JButton deselectButton = new JButton(new SelectionAction("Deselect", false));
        	this.add(selectButton, FlowLayout.LEFT);
            this.add(deselectButton, FlowLayout.LEFT);
        	this.add(Box.createRigidArea(new Dimension(90, 0)));

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
            searchBar = new JTextField(25);
    		searchBar.addKeyListener(keyListener);
        	this.add(new JLabel("Search:"));
    		this.add(searchBar);
        }
    }
    
    public void clearSearchBar() {
    	searchBar.setText("");
    	filter.swapPrefix("");
    	table.getRowSorter().allRowsChanged();
    	return;
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
     * @author Prad 
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

    MouseMotionAdapter hoverListener = new MouseMotionAdapter() {
    	@Override
    	public void mouseMoved(MouseEvent e) {
            JTable aTable =  (JTable)e.getSource();
            itsRow = aTable.rowAtPoint(e.getPoint());
            itsColumn = aTable.columnAtPoint(e.getPoint());
            aTable.repaint();
        }
	};
}