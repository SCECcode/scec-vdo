package org.scec.vtk.plugins.utils.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * 
 * @author Prad Tantiwuttipong
 * 
 * CheckAllTable is meant to be the standard panel for displaying searchable tables 
 * in plugins. This class extends a JPanel that by default contains a search bar, select/deselect
 * buttons, and a JTable. The Political Boundaries plugin contains a full example of this table panel.
 * 
 *
 */

public class CheckAllTable extends JPanel {
	private static final long serialVersionUID = 1L;
	private Object[][] DATA;  								//All data is stored as a 2D array of Java objects.
	private String TITLE;									//Each table contains a title, displayed in the header
    private static final int CHECK_COL = 0;					//CHECK_COL decides which column checkboxes are in.
    private static final int COLOR_COL = 2;					//COLOR_COL decides which column colors are saved.
    private DataModel dataModel;
    private JTable table;
    private DefaultListSelectionModel selectionModel;
    ControlPanel controlPanel;								//controlPanel contains select/deselect all buttons, search bar, and an optional color button
    int hoverRow = 0;										//hoverRow used for hover effect
    int hoverColumn = 0;									//hoverColumn used for hover effect		

    private Filter filter;									//filter used for searching
    JTextField searchBar;									//searchBar implements a search bar
    
    
    
    
    /**
     * @param data
     * @param title
     * @param tableListener
     * 
     * Constructor takes ArrayList<String> that is converted into Object[][] and saved in DATA.
     * tableListener is the default listener for table changes.
     * 
     */
    public CheckAllTable(ArrayList<String> data, String title, TableModelListener tableListener) {
    	super(new BorderLayout());											//All CheckAllTables use a BorderLayout
    	DATA =new Object[data.size()][3];
    	for (int i = 0; i < data.size(); i++) {
			DATA[i][0] = Boolean.FALSE;				
			DATA[i][1] = data.get(i);
			DATA[i][2] = Color.white;
		}
    	initTable(title, tableListener);
    }
    
    /**
     * @param data
     * @param title
     * @param tableListener
     * 
     * Constructor takes Object[][] that is saved in DATA.
     * 
     */
    public CheckAllTable(Object[][] data, String title, TableModelListener tableListener) {
        super(new BorderLayout());
        DATA = data;
        initTable(title, tableListener);
    }
    
    /**
     * @param data
     * @param title
     * 
     * Constructor has no tableListener. Table does not listen for changes. A tableListener can be
     * added at a later time. 
     * 
     */
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
			}
		};
    	initTable(title, tableListener);
    }
    
    /**
     * @param data
     * @param title
     * 
     */
    public CheckAllTable(Object[][] data, String title) {
        super(new BorderLayout());
        DATA = data;
    	TableModelListener tableListener = new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
			}
		};
        initTable(title, tableListener);
    }

    /**
     * @param title
     * @param tableListener
     * 
     * Creates a JTable, adds tableListener and control panel, and sets default table properties. This function
     * is called every time a new CheckAllTable is created.
     * 
     */
    private void initTable(String title, TableModelListener tableListener) {
    	TITLE = title;
        this.setLayout(new BorderLayout());
        
        String[] COLUMN_HEADERS = {" < ", TITLE, ""};									//First column header is "<" and is used as the back button.
        dataModel = new DataModel(DATA, COLUMN_HEADERS);								//Create a new DataModel to handle all data
        
        controlPanel = new ControlPanel();												//Add ControlPanel.					
        this.add(controlPanel, BorderLayout.NORTH);

        table = new JTable(dataModel) {													//Create the table using dataModel.
        	public Component prepareRenderer(											//prepareRenderer sets GUI properties for the entire table. Currently,
        		TableCellRenderer renderer, int row, int column) {							//it makes the rows alternate in color between white and light blue.
        		Component c = super.prepareRenderer(renderer, row, column);
        		if (!isRowSelected(row) && column != 2) {
        			c.setBackground(row % 2 == 0 ? getBackground() : new Color(240, 254, 255));
        		}
        		return c;
        	}
        };

        table.getModel().addTableModelListener(tableListener);								//Add tableListener to model.
        selectionModel = (DefaultListSelectionModel) table.getSelectionModel();				//selectionModel keeps track of which rows are highlighted
        selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);	//Set table selection mode. This one lets users select multiple rows.
        
        filter = new Filter();																//Create new filter for searching
        
        setTableProperties();																//Set GUI properties of table
        
        JScrollPane tableScrollPane = new JScrollPane(table);								//Add table to a scroll pane to enable scrolling when needed.
        tableScrollPane.setBorder(new EmptyBorder(3, 8, 0, 8));								//Delete border around tableScrollPane
        tableScrollPane.setOpaque(false);
        this.add(tableScrollPane, BorderLayout.CENTER);
        
    }
    
    public void addColumn(ArrayList<String> columnData, String columnTitle) {
    	Object[] cData = new Object[columnData.size()];
    	for (int i = 0; i < columnData.size(); i++) {
    		cData[i] = columnData.get(i);
    	}
    	dataModel.addColumn(columnTitle, cData);
    	table.moveColumn(table.getColumnCount()-1, table.getColumnCount()-2);
    	setTableProperties();
    }
    
    /**
     * Disable checkboxes by setting values in CHECK_COL to an empty string instead of a boolean.
     */
    public void disableCheckboxes() {
    	for (int i = 0; i < table.getRowCount(); i++) {
    		table.getModel().setValueAt("", i, CHECK_COL);
    	}
    }
    
    /**
     * Sets all visual properties for the table
     */
    private void setTableProperties() {
    	table.getColumnModel().getColumn(CHECK_COL).setMaxWidth(60);
    	table.getColumnModel().getColumn(1).setCellRenderer(textRenderer);
    	table.getColumnModel().getColumn(COLOR_COL).setMaxWidth(0);									//Hides color column from table view.
    	table.getColumnModel().getColumn(COLOR_COL).setMinWidth(0);
    	table.getColumnModel().getColumn(COLOR_COL).setPreferredWidth(0);
        table.setIntercellSpacing(new Dimension(0,0));												
        table.setShowGrid(false);																	//Hide default table grid
        table.setRowHeight(25);																
        table.setShowHorizontalLines(false);														//Hide lines between rows
        table.setPreferredScrollableViewportSize(new Dimension(250, 175));

        renderTableHeader();  																		//renderTableHeader recenters the header titles
        
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());		//Get the TableRowSorter of the model
        sorter.setRowFilter(filter);																//Add filter for searching by letter
        sorter.setSortable(CHECK_COL, false);														//Makes checkbox column unsortable
        table.setRowSorter(sorter);
    }
    /**
     * Centers the table headers and changes table header properties
     */
    public void renderTableHeader() {
    	((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer())
        .setHorizontalAlignment(JLabel.CENTER);
        table.getTableHeader().setReorderingAllowed(false);							//Disables reordering of columns
        table.getTableHeader().setResizingAllowed(false);							//Disables resizing of columns
    }
    public void addPopupMenu() {
    	JFrame frame = new JFrame("PopupSample Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Create popup menu, attach popup menu listener
        JPopupMenu popupMenu = new JPopupMenu("Title");
    }
    /**
     * @param mouseListener
     * @param controlSymbol	- String that contains what should be displayed in the controlColumn. Default should be ">".
     * @param tableNode
     * 
     * Adds a control column that enables clicking to the next table or loading the next table. This requires the CheckAllTable
     * to be part of a tree of CheckAllTables. See implementation of the tree in the PoliticalBoundaries plugin.
     * 
     */
    public void addControlColumn(MouseAdapter mouseListener, String controlSymbol, TreeNode<CheckAllTable> tableNode) {
    	table.addMouseListener(mouseListener);																	//Add a listener for the controls
    	Object[] controlSymbols = new Object[table.getRowCount()];												//Creates the column data
    	for (int row = 0 ; row < table.getRowCount(); row++) {												
    		if (hasSubTable(row, tableNode) || controlSymbol.equals("Load"))									//If the table has a next table or can load a new table, add the symbol to the column
    			controlSymbols[row] = controlSymbol;						
    		else
    			controlSymbols[row] = "";
    	}
    	
    	dataModel.addColumn("", controlSymbols);																//Add new column
        table.getColumnModel().getColumn(table.getColumnCount()-1).setCellRenderer(controlColumnRenderer);		//Set renderer for column
        
        if (controlSymbol.equals("Load"))																		//Make the column bigger if "Load" is the controlSymbol.
        	table.getColumnModel().getColumn(table.getColumnCount()-1).setMaxWidth(100);						
        else
            table.getColumnModel().getColumn(table.getColumnCount()-1).setMaxWidth(30);
        
        table.addMouseMotionListener(hoverListener);														//Add hover effect to control column	
        setTableProperties();																				//TableProperties are lost after new column is added, so we reset them here.
    }
    
    MouseAdapter rightClickListener = new MouseAdapter() {
    	public void mouseClicked(MouseEvent e) {
    		if (SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
    			
    		}
    	}
    };
		
    /**
     * Listens for hover on whatever row/column it is attached to.
     */
    MouseMotionAdapter hoverListener = new MouseMotionAdapter() {
    	@Override
    	public void mouseMoved(MouseEvent e) {
            JTable aTable =  (JTable)e.getSource();
            hoverRow = aTable.rowAtPoint(e.getPoint());
            hoverColumn = aTable.columnAtPoint(e.getPoint());
            aTable.repaint();
        }
	};
    
    /**
     * @param row
     * @param tableNode
     * @return
     * 
     * Searches tableNode and returns whether it has children i.e. if there is a next table.
     * 
     */
    public boolean hasSubTable(int row, TreeNode<CheckAllTable> tableNode) {
    	for (TreeNode<CheckAllTable> node : tableNode) {
    		if (node.data.getTitle().equals(table.getValueAt(row, 1))) {
    			return true;
    		}
    	}
    	return false;
    }

    /**
     *   Renderer for column 1 which contains all the strings of data
     */
    DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
    	Border padding = BorderFactory.createEmptyBorder(0, 10, 0, 0);
    	@Override
    	public Component getTableCellRendererComponent(JTable table,
    			Object value, boolean isSelected, boolean hasFocus,
    			int row, int column) {
    		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
    				row, column);
    		setBorder(padding);
    		return this;
    	}
    };
    
    /**
     *    Renderer for the control column
     */
    DefaultTableCellRenderer controlColumnRenderer = new DefaultTableCellRenderer() {
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
    		if(row == hoverRow && column == hoverColumn)							//Controls the hover effect colors
    			this.setForeground(new Color(127, 255, 0));
    		else
    			this.setForeground(Color.DARK_GRAY);
    		return this;
    	}
    };

    /**
     * @return - Returns the table
     */
    public JTable getTable() {
    	return table;
    }
    
    /**
     * @return - Returns the table title
     */
    public String getTitle() {
    	return TITLE;
    }
    
    /**
     * Class that extends the DefaultTableModel and turns CHECK_COL into checkboxes
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
        public boolean isCellEditable(int row, int column) {									//Makes the CHECK_COL uneditable.
            return column == CHECK_COL && !(getValueAt(row, CHECK_COL) instanceof String);
        }
    }
    
    /**
     * @param actionListener
     * 
     * Adds a color button to the controlPanel
     * 
     */
    public void addColorButton(ActionListener actionListener) {													
    	ColorButton colorDrawingToolsButton = new ColorButton(actionListener, "Change color");
    	colorDrawingToolsButton.setEnabled(true);
    	controlPanel.buttonPanel.add(colorDrawingToolsButton, FlowLayout.LEFT);
    }
    
    /**
     * Class that extends JPanel and contains the select/deselect all buttons and the search bar. New buttons can also be added.
     */
    public class ControlPanel extends JPanel {
    	JPanel buttonPanel;
    	JPanel searchPanel;
        public ControlPanel() {
        	this.setBorder(new EmptyBorder(4, 0, -3, 3));
            this.setOpaque(false);
            this.setLayout(new BorderLayout());
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
            buttonPanel.setBorder(new EmptyBorder(4, 1, 4, 0));
        	JButton selectButton = new JButton(new SelectionAction("Select All", true));    	//These buttons use SelectionAction as their action when clicked.
        	JButton deselectButton = new JButton(new SelectionAction("Deselect All", false));
        	buttonPanel.add(selectButton, FlowLayout.LEFT);
            buttonPanel.add(deselectButton, FlowLayout.LEFT);
            this.add(buttonPanel, BorderLayout.LINE_START);

            searchPanel = new JPanel(new BorderLayout());
            searchPanel.setPreferredSize(new Dimension(300, 10));
            searchPanel.setBorder(new EmptyBorder(10, 0, 9, 4));
            searchBar = new JTextField();
    		searchBar.addKeyListener(searchKeyListener);
        	searchPanel.add(new JLabel("Search: "), BorderLayout.LINE_START);
    		searchPanel.add(searchBar, BorderLayout.CENTER);
    		this.add(searchPanel, BorderLayout.LINE_END);
        }
    }
    
    /**
     * Creates a listener for searching
     */
	KeyListener searchKeyListener = new KeyListener() {
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {									//When a key is pressed into the search bar, send the text to the filter.
			String text = searchBar.getText();										
			filter.swapPrefix(text); 
			table.getRowSorter().allRowsChanged();								//Tell the table to change its row view.
		}
		@Override
		public void keyPressed(KeyEvent e) {}
	};
    
	/**
	 * Clears the search bar. Used when changing from CheckAllTable to a different CheckAllTable.
	 */
    public void clearSearchBar() {
    	searchBar.setText("");
    	filter.swapPrefix("");
    	table.getRowSorter().allRowsChanged();
    	return;
    }
    
    /**
     * Handles selection on the table by setting the row's checkbox to the desired value when the button is clicked. 
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
               // if (selectionModel.isSelectedIndex(i)) {										
                    dataModel.setValueAt(value, table.convertRowIndexToModel(i), CHECK_COL);
                //}
            }
        }
    }
    
    /**
     *  Filters the table based on search bar input
     */
    private static class Filter extends RowFilter<TableModel, Integer> {
        private String includePrefix = "";
        @Override
        public boolean include(javax.swing.RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
        	return entry.getStringValue(1).toLowerCase().startsWith(includePrefix);
        }
        public void swapPrefix(String text) {									//Swaps search prefix with new prefix from search bar
            this.includePrefix = text.toLowerCase();
        }
    }
}