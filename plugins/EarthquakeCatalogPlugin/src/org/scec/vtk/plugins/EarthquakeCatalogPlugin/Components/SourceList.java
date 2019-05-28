package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPlugin;
import org.scec.vtk.tools.Prefs;



/**
 * This class maintains the list of all source catalogs available to the
 * <code>EQCatalogPlugin</code>. It uses a generic <code>DefaultListModel</code>
 * for data and handles the tasks of list creation and deletion.
 * 
 * Created on Feb 24, 2005
 * 
 * @author P. Powers
 * @version $Id: SourceList.java 1178 2006-06-06 23:28:00Z tlrobins $
 */
public class SourceList extends JList {

    private static final long serialVersionUID = 1L;

	// owner, data source, and data file
    private Component listOwner;
    
    // list access fields
    private ListSelectionModel selModel;
    private DefaultListModel listModel;
        
   /**
     * Constructs a new <code>SourceList</code> with a given owner (for listener addition).
     * 
     * @param owner object that is registered to for notifications of changes to list
     * contents and selection
     */
    public SourceList(Container owner) {
        super();
        this.listOwner = owner;
        init();
    }
    
    //****************************************
    //     PUBLIC UTILITY METHODS
    //****************************************

    public DefaultListModel getDefaultListModel()
    {
    	return this.listModel;
    }
    
    /**
     * (Re)loads all <code>SourceCatalog</code>s in <i>ScecVideo</i> data store.
     */
    public void loadSourceCatalogs() {

        // clear current list
        this.listModel.clear();
        
        // read source directory filtering for catalaogs
        File dir = new File(
                Prefs.getLibLoc() + 
                File.separator + EarthquakeCatalogPlugin.dataStoreDir +
                File.separator + "source");
        
        File[] cats = dir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                if (f.getName().endsWith(".cat")) return true;
                return false;
            }
        });
        
        for (int i=0; i<cats.length; i++) {
            SourceCatalog source = new SourceCatalog(this.listOwner, cats[i]);
            addCatalog(source);
        }
    }
    
    /**
     * Adds a <code>SourceCatalog</code> to this list.
     * 
     * @param catalog to add
     */
    public void addCatalog(SourceCatalog catalog) {
        this.listModel.addElement(catalog);
    }
    
    /**
     * Deletes a <code>CatalogAccessor</code> from this list. Method will
     * delete attribute files and data backing store. Always asks for confirmation
     * 
     * @param catalog object to be deleted
     */
    public void deleteCatalog(CatalogAccessor catalog) {
    	deleteCatalog(catalog, true);
    }
    
    /**
     * Deletes a <code>CatalogAccessor</code> from this list. Method will
     * delete attribute files and data backing store. Option to confirm whether
     * or not you wish to delete it.
     * 
     * @param catalog object to be deleted
     * @param confirmDelete confirm you want to delete it
     */
    public void deleteCatalog(CatalogAccessor catalog, boolean confirmDelete) {
    	if (confirmDelete) {
            int delete = JOptionPane.showConfirmDialog(
                    this.listOwner,
                    "Are you sure you want to delete the selected source catalog?\n" +
                    "(All attribute and catalog data will be deleted)",
                    "Delete Source Catalog",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (delete == JOptionPane.NO_OPTION ||
                delete == JOptionPane.CLOSED_OPTION) return;
    	}       
        catalog.getAttributeFile().delete();
        catalog.getDataFile().delete();
        this.listModel.removeElement(catalog);
    }

    //****************************************
    //     PRIVATE METHODS
    //****************************************

    private void init() {
        // Init selection model and register GUI as listener for button enabling.
        this.selModel = getSelectionModel();
        this.selModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.selModel.addListSelectionListener((ListSelectionListener)this.listOwner);
        // Init list model
        this.listModel = new DefaultListModel();
        setModel(this.listModel);
        // Init renderer
        setCellRenderer(new SourceListRenderer());
    }
         
    
    //****************************************
    //     CELL RENDERERS
    //****************************************
    
    /**
     * Custom renderer class draws <code>String</code> objects.
     *
     * Created on Jan 30, 2005
     * 
     */
    private class SourceListRenderer extends DefaultListCellRenderer {
        
        private static final long serialVersionUID = 1L;
		// icons
        private ImageIcon catIcon_none = new ImageIcon(
                SourceList.class.getResource("resources/img/sourceIcon_none.png"));
        private ImageIcon catIcon_err = new ImageIcon(
                SourceList.class.getResource("resources/img/sourceIcon_err.png"));
        private ImageIcon catIcon_fm = new ImageIcon(
                SourceList.class.getResource("resources/img/sourceIcon_fm.png"));
        private ImageIcon catIcon_err_fm = new ImageIcon(
                SourceList.class.getResource("resources/img/sourceIcon_fm_err.png"));

        SourceListRenderer() {
            super();
        }
        
        /**
         * Required method of custom cell renderers that gets called to render 
         * <code>SourceList</code> items.
         * 
         * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(
                JList list, Object catalog, int index,
                boolean isSelected, boolean hasFocus) {
            
            switch (((SourceCatalog)catalog).getDataScope()) {
                case CatalogAccessor.DATA_SCOPE_UNCERT:
                    setIcon(this.catIcon_err);
                    break;
                case CatalogAccessor.DATA_SCOPE_FOCAL:
                    setIcon(this.catIcon_fm);
                    break;
                case CatalogAccessor.DATA_SCOPE_UNCERT_FOCAL:
                    setIcon(this.catIcon_err_fm);
                case CatalogAccessor.DATA_SCOPE_FOCAL_PROB:
                	setIcon(this.catIcon_err_fm);
                    break;
                default:
                    setIcon(this.catIcon_none);
            }
            setIconTextGap(8);
            
            String catname = ((SourceCatalog)catalog).toString().trim();
            catname = (catname.equals("")) ? "-- no name --" : catname;
            setText(catname);
            setBorder(BorderFactory.createEmptyBorder(3,7,3,7));
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                if ((index % 2) == 0) {
                    setBackground(Prefs.getStripingColor());
                } else {
                    setBackground(list.getBackground());
                }
            }
            
            return this;
        }
    }

}
