package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPlugin;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginGUI;
import org.scec.vtk.plugins.utils.components.CheckBoxRenderer;
import org.scec.vtk.plugins.utils.components.ColorDiscIcon;
import org.scec.vtk.plugins.utils.components.ColorWellIcon;
import org.scec.vtk.plugins.utils.components.StringRenderer;
import org.scec.vtk.tools.Prefs;


public class CatalogTable extends JTable {
	private static final String destinationData = Prefs.getLibLoc() + File.separator + EarthquakeCatalogPlugin.dataStoreDir + File.separator + "display" + File.separator + "data";
	private static final String destinationDisplay = Prefs.getLibLoc() + File.separator + EarthquakeCatalogPlugin.dataStoreDir + File.separator + "display";
	private static final long serialVersionUID = 1L;

	/** This table's owner (GUI). */
	//earthquakeCatalogGUI
    protected Component parent;
    
    // table access fields
    public CatalogTableModel tableModel;
    private ListSelectionModel selModel;
    
    private PluginActors actors;
        
    /**
     * Constructs a new <code>CatalogLibraryTable</code> with the specified owner.
     *
     * @param owner parent <code>Component</code> that is registered for various event notifications
     */
    public CatalogTable(Component owner, PluginActors actors) {
        super();
        this.actors = actors;
        this.parent = owner;
        this.init();
    }
    //directory copy for permanant files people might accidently delete, feel free to make it cleaner jason armstrong
   


 
    //****************************************
    //     PUBLIC UTILITY METHODS
    //****************************************
       
    /**
     * (Re)loads all <code>EQCatalog</code>s in <i>ScecVideo</i> data store.
     * @throws IOException 
     */
    public void loadCatalogs() throws IOException {
      // clear current list
      //load standard EQCatolog.
    	// Hard coded data set created in ScecVDO so that if some one accidently erases it will come back and reload of the software.  Feel free to shorten the file names  Jason Armstrong
            this.tableModel.clear();
            //.dat files to be copied
            
            
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+File.separator +"data"+ File.separator +"filteredCat_000000.dat", destinationData);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+File.separator +"data"+ File.separator +"filteredCat_314784.dat", destinationData);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+File.separator +"data"+ File.separator +"filteredCat_321964.dat", destinationData);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+File.separator +"data"+ File.separator +"filteredCat_343741.dat", destinationData);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+File.separator +"data"+ File.separator +"filteredCat_454569.dat", destinationData);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+File.separator +"data"+ File.separator +"filteredCat_482015.dat", destinationData);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+File.separator +"data"+ File.separator +"filteredCat_501171.dat", destinationData);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+File.separator +"data"+ File.separator +"filteredCat_266570.dat", destinationData);
              
            //.cat files to be copied C:\Documents and Settings\scec12\workspace\scec_vdo_new\conf\ScecVideoDefaults\EQCatalogStore\display\filteredCat_000000.cat
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+ File.separator +"filteredCat_000000.cat", destinationDisplay);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+ File.separator +"filteredCat_314784.cat", destinationDisplay);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+ File.separator +"filteredCat_321964.cat", destinationDisplay);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+ File.separator +"filteredCat_343741.cat", destinationDisplay);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+ File.separator +"filteredCat_454569.cat", destinationDisplay);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+ File.separator +"filteredCat_482015.cat", destinationDisplay);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+ File.separator +"filteredCat_501171.cat", destinationDisplay);
              Copyfiles.copy(Info.getMainGUI().getCWD()+File.separator+"conf"+File.separator +"ScecVideoDefaults"+File.separator + "EQCatalogStore"+File.separator +"display"+ File.separator +"filteredCat_266570.cat", destinationDisplay);
              
        // read source directory filtering for catalogs
        File file = new File(
                Prefs.getLibLoc() + 
                File.separator + EarthquakeCatalogPlugin.dataStoreDir +
                File.separator + "display");
        File[] cats = file.listFiles(new FileFilter() {
            public boolean accept(File f) {
                if (f.getName().endsWith(".cat")) return true;
                return false;
            }
        });
        
        for (int i=0; i<cats.length; i++) {
        	try{
	            EQCatalog cat;
	            cat = new EQCatalog(this.parent, cats[i], actors);
	            cat.setGeometry(EQCatalog.GEOMETRY_SPHERE);
	            addCatalog(cat);
        	}
        	catch (Exception e)
        	{
        		System.out.println("Poorly formatted input");
        	}
        }
    }
    
    /**
     * Adds a catalog to the table.
     * abra cadabra patas de cabra
     * @param catalog to add
     */
    public void addCatalog(EQCatalog catalog) {
        this.tableModel.addObject(catalog);
    }

    /**
     * Returns the data model associated with this library table.
     * 
     * @return the library table data model
     */
    public CatalogTableModel getLibraryModel() {
        return this.tableModel;
    }
    
    /**
     * Returns the selected <code>CatalogAccessor</code> object. Returns
     * null if nothing is selected.
     * 
     * @return the catalog object
     */
    public EQCatalog getSelectedValue() {
        if (getSelectedRow() != -1) {
            return (EQCatalog)this.tableModel.getObjectAtRow(getSelectedRow());
        }
        return null;
    }
    
    /**
     * Selects the given <code>EQCatalog</code> object.
     * my andaconda donmt want none uin l;esd 
     * @param object to select
     */
    public void setSelected(EQCatalog object) {
        // clear current selection
        clearSelection();
        // select faults
        this.selectionModel.setValueIsAdjusting(true);
        int sel = this.tableModel.indexOf(object);
        this.selectionModel.addSelectionInterval(sel,sel);
        this.selectionModel.setValueIsAdjusting(false);
    }

    public void setVisibility(CatalogTableModel libModel, EQCatalog libCat, int row)
    {
    	boolean show = false;
    	  if(libModel.getObjectAtRow(row).isDisplayed())
          {
          	if(libCat.getActors().get(0)!=null){
          	libCat.getActors().get(0).SetVisibility(0);
          	libCat.getActors().get(1).SetVisibility(0);
          	Info.getMainGUI().updateRenderWindow();
          	show = false;
          	}
          }
          else
          {
          	if(libCat.getActors().get(0)!=null){
              	libCat.getActors().get(0).SetVisibility(1);
              	libCat.getActors().get(1).SetVisibility(1);
              	Info.getMainGUI().updateRenderWindow();
              	show = true;
              	}
          }
          
		libModel.setVisibilityForRow(show, row);
    }
    
    //****************************************
    //     PRIVATE METHODS
   
    //****************************************


    private void init() {
        
        // initialize data model
        this.tableModel = new CatalogTableModel(this.parent);
        this.tableModel.addTableModelListener((TableModelListener)this.parent);
        setModel(this.tableModel);
        
        // set to monitor mouse clicks
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            
                // Get column and row values for X and Y clicked
                CatalogTableModel libModel = CatalogTable.this.getLibraryModel();
                int col = CatalogTable.this.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / getRowHeight();
                
    //            if (libModel.getLoadedStateForRow(row) && e.getButton() == MouseEvent.BUTTON1) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (col == 0) {
                    	if(!libModel.getLoadedStateForRow(row)){
                    		libModel.setLoadedStateForRow(true, row);
                    		EarthquakeCatalogPluginGUI gui;
                    		gui = (EarthquakeCatalogPluginGUI)CatalogTable.this.parent;
                    		gui.processTableSelectionChange();
                    		EQCatalog libCat = getSelectedValue();
                    		gui.setAnimationColor(libCat.getColor1(), libCat.getColor2());
                    	}
                    	EQCatalog libCat = getSelectedValue();
                    	//changing visibility of eq catalog on checkbox click
//                        if(libModel.getObjectAtRow(row).isDisplayed())
//                        {
//                        	if(libCat.getActors().get(0)!=null){
//                        	libCat.getActors().get(0).SetVisibility(0);
//                        	libCat.getActors().get(1).SetVisibility(0);
//                        	Info.getMainGUI().updateRenderWindow();
//                        	}
//                        }
//                        else
//                        {
//                        	if(libCat.getActors().get(0)!=null){
//                            	libCat.getActors().get(0).SetVisibility(1);
//                            	libCat.getActors().get(1).SetVisibility(1);
//                            	Info.getMainGUI().updateRenderWindow();
//                            	}
//                        }
//                        libModel.toggleVisibilityForRow(row);
                    	setVisibility(libModel,libCat,row);
                    } 
                    else if (col == 1 || col == 2) {
                        	EarthquakeCatalogPluginGUI gui;
                        	gui = (EarthquakeCatalogPluginGUI)CatalogTable.this.parent;
                        	gui.switchToDisplayPanel();              
                    }
                }
           }
        });
             
        // Set up selection model and register GUI as listener for 
        // button en/disabling.
        this.selModel = getSelectionModel();
        this.selModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.selModel.addListSelectionListener((ListSelectionListener)this.parent);
        
        // visual set up
        this.setTableHeader(null);
        this.setRowHeight(getRowHeight()+4);
        this.setIntercellSpacing(new Dimension(0,0));
        this.setShowGrid(false);
        
        // set renderers --> use custom nested classes because default renderers
        // put things like grey lines around focused text cells, even though
        // they're not editable 
        
        // set/fix column widths and renderers

        TableColumn col1 = getColumnModel().getColumn(0);
        
        if(parent instanceof EarthquakeCatalogPluginGUI)
        {
        	TableColumn col2 = getColumnModel().getColumn(1);        
        	TableColumn col3 = getColumnModel().getColumn(2);
            TableColumn col4 = getColumnModel().getColumn(3);
            
        	col1.setCellRenderer(new CheckBoxRenderer());
        	col1.setPreferredWidth(26);
        	col1.setMinWidth(26);
        	col1.setMaxWidth(26);

        	col2.setCellRenderer(new DisplayStateRenderer());
        	col2.setPreferredWidth(34);
        	col2.setMinWidth(34);
        	col2.setMaxWidth(34);

        	col3.setCellRenderer(new GeometryStateRenderer());
        	col3.setPreferredWidth(16);
        	col3.setMinWidth(16);
        	col3.setMaxWidth(16);

        	col4.setCellRenderer(new StringRenderer());
        }
     
        else
        {
            col1.setCellRenderer(new StringRenderer());
        }

        
        
        
    }

    
    //****************************************
    //     CELL RENDERERS
    //****************************************
    
    /**
     * Custom renderer class draws icons for catalog point or sphere representation.
     *
     * Created on Jan 30, 2005
     * 
     */
    private class GeometryStateRenderer extends DefaultTableCellRenderer {
        
        private static final long serialVersionUID = 1L;
		// icons
        private ImageIcon sphereOn = new ImageIcon(
                SourceList.class.getResource("resources/img/sphereIcon.png"));
        private ImageIcon sphereOff = new ImageIcon(
                SourceList.class.getResource("resources/img/sphereIconDis.png"));
        private ImageIcon pointOn = new ImageIcon(
                SourceList.class.getResource("resources/img/pointIcon.png"));
        private ImageIcon pointOff = new ImageIcon(
                SourceList.class.getResource("resources/img/pointIconDis.png"));
        
        GeometryStateRenderer() {
            super();
        }
    
        /**
         * Required method of custom cell renderers that gets called to render 
         * <code>EQCatalog</code> display state table cells.
         * 
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(
                JTable table, Object catalog,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            
            EQCatalog c = (EQCatalog)catalog;
            setEnabled(c.isInMemory());
            
            if (c.getGeometry() == EQCatalog.GEOMETRY_SPHERE && 
                    c.getFocalDisplay() == EQCatalog.FOCAL_NONE) {
                setIcon(this.sphereOn);
                setDisabledIcon(this.sphereOff);
            } else if (c.getGeometry() == EQCatalog.GEOMETRY_POINT){
                setIcon(this.pointOn);
                setDisabledIcon(this.pointOff);                
            } else {
                setIcon(null);
                setDisabledIcon(null);
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                if ((row % 2) == 0) {
                    setBackground(Prefs.getStripingColor());
                } else {
                    setBackground(table.getBackground());
                }
            }
            
            return this;
        }
    }

    /**
     * Custom renderer class draws <code>ColorWell</code> objects.
     *
     * Created on Jan 30, 2005
     * 
     */
    private class DisplayStateRenderer extends DefaultTableCellRenderer {
        
        private static final long serialVersionUID = 1L;
		private ColorWellIcon colorIcon = new ColorWellIcon(Color.WHITE, 26, 9, 2);
		
		private int inscribedRectDimension = 15;
		private int inset = 2;
		private ColorDiscIcon colorDiscIcon = new ColorDiscIcon(Color.RED, Color.YELLOW, 
																inscribedRectDimension, 
																inscribedRectDimension, 
																inset);
        
        DisplayStateRenderer() {
            super();
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        /**
         * Required method of custom cell renderers that gets called to render 
         * <code>EQCatalog</code> display state table cells.
         * 
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(
                JTable table, Object catalog,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            
            EQCatalog c = (EQCatalog)catalog;
            setEnabled(c.isInMemory());
             
           if (c.getGeometry() == EQCatalog.GEOMETRY_SPHERE && 
            		 c.getFocalDisplay() == EQCatalog.FOCAL_DISC){
            	this.colorDiscIcon.setColor(c.getDiscCompColor(),c.getDiscExtColor());
            	setIcon(this.colorDiscIcon);
            	setDisabledIcon(this.colorDiscIcon);
            }
            else {
                this.colorIcon.setColor(c.getColor1(), c.getColor2());
                setIcon(this.colorIcon);
                setDisabledIcon(this.colorIcon);
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                if ((row % 2) == 0) {
                    setBackground(Prefs.getStripingColor());
                } else {
                    setBackground(table.getBackground());
                }
            }
            
            return this;
        }
    }

	public CatalogTableModel getTableModel() {
		return tableModel;
	}
    
}

