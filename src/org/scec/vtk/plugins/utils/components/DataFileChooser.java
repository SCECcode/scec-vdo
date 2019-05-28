package org.scec.vtk.plugins.utils.components;


import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

//import com.google.common.collect.Lists;


/**
 * Custom <code>JFileChooser</code> for selecting data files. Currently this
 * class is only used by importers.
 * 
 * Created on Jan 30, 2005
 * 
 * @author P. Powers
 * @version $Id: DataFileChooser.java 4701 2013-09-23 23:52:01Z kmilner $
 */
public class DataFileChooser extends JFileChooser {

    private static final long serialVersionUID = 1L;

	/** Current file filter extension.  */
    protected ArrayList<String> filterExtension = new ArrayList();
    
    /** Current file filer description. */
    protected String filterDescription = "";
    
    private Filter filter = new Filter();
    private Component parent = null;
    
    
    /**
     * Constructs a new <code>FileChooser</code> with a given parent, title,
     * and multi-file selection policy.
     * 
     * @param component file chooser's parent component / owner
     * @param title for this file chooser window
     * @param multiple whether multiple file selection is permitted
     * 
     * @see javax.swing.JFileChooser
     */
    public DataFileChooser(Component component, String title, boolean multiple) {
    	super();
    	commonSetup(component,title,multiple);
    }
    /**
     * Constructs a new <code>FileChooser</code> with a given parent, title,
     * and multi-file selection policy and current directory.
     * 
     * @param component file chooser's parent component / owner
     * @param title for this file chooser window
     * @param multiple whether multiple file selection is permitted
     * @param directory directory of where to open dialog
     * 
     * @see javax.swing.JFileChooser
     */
    public DataFileChooser(Component component, String title, boolean multiple, File directory) {
    	super(directory);
        commonSetup(component,title,multiple);
    }
    /**
     * Common setup in both generic constructor and directory specified version
     * @param component
     * @param title
     * @param multiple
     * @author rmrobert in the spirit of Doubleday
     */
    private void commonSetup(Component component, String title, boolean multiple){
    	this.parent = component;
        this.setDialogTitle(title);
        this.setMultiSelectionEnabled(multiple);
        this.setFileFilter(this.getAcceptAllFileFilter());
        this.setApproveButtonText("Import");
    }
    
    /**
     * Resets file chooser to "accept-all" state.
     */
    public void reset() {
        this.filterExtension = new ArrayList();
        this.filterDescription = "";
        this.setAcceptAllFileFilterUsed(true);
        this.removeChoosableFileFilter(this.filter);
    }
    
    /**
     * Returns the extension of currently allowable files.
     * 
     * @return allowable file extension
     */
    public ArrayList<String> getCurrentFilter() {
        return this.filterExtension;
    }
    
    /**
     * Sets the extension for allowed files.
     * 
     * @param ext filter extension to set
     * @param desc description of filter for display purposes
     */
    public void setCurrentFilter(String ext, String desc) {
        //setCurrentFilter(new ArrayList().add(ext), desc);
    }
    
    /**
     * Sets the extension for allowed files.
     * 
     * @param ext filter extension to set
     * @param desc description of filter for display purposes
     */
    public void setCurrentFilter(ArrayList<String> ext, String desc) {
        this.setAcceptAllFileFilterUsed(false);
        this.filterExtension = ext;
        this.filterDescription = desc;
        this.setFileFilter(this.filter);
    }
    
    /**
     * Sets this file chooser's title.
     * 
     * @param title to set
     */
    public void setTitle(String title) {
        this.setDialogTitle(title);
    }
    
    /**
     * Convenience method to set title and filter of file chooser.
     * 
     * @param title to set
     * @param ext filter extension to set
     * @param desc description of filter for display purposes
     */
    public void setParams(String title, String ext, String desc) {
        setTitle(title);
        setCurrentFilter(ext, desc);
    }
    
    /**
     * Opens this modal chooser and returns the selected file. Returns <code>null</code>
     * if operation is cancelled.
     * 
     * @return selected file; <code>null</code> if cancelled
     */
    public File getFile() {
        int option = this.showOpenDialog(this.parent);
        if (option == JFileChooser.APPROVE_OPTION) {
            return this.getSelectedFile();
        }
        return null;
    }
    
    /**
     * Opens this modal chooser and returns the selected files. Returns <code>null</code>
     * if operation is cancelled. Method assumes that multiple file selection is enabled.
     * 
     * @return selected file; <code>null</code> if cancelled
     */
    public File[] getFiles() {
        int option = this.showOpenDialog(this.parent);
        if (option == JFileChooser.APPROVE_OPTION) {
            return this.getSelectedFiles();
        }
        return null;
    }
    
     /**
     * Inner concrete subclass of a <code>FileFilter</code> for use with parent class.
     */
    private class Filter extends FileFilter {
                
        /**
         * Accepts or rejects a file for selection.
         * 
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        public boolean accept(File f) {
            // accept directories
        	if (f.isDirectory() || filterExtension.isEmpty())
        		return true;
        	for (String ext : filterExtension)
        		if (f.getName().toLowerCase().endsWith(ext))
        			return true;
            return false;
        }
        
        /**
         * Returns description of filter for display purposes.
         * 
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        public String getDescription() {
            return DataFileChooser.this.filterDescription;
        }
    } 
}

