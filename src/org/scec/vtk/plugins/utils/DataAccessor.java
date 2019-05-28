package org.scec.vtk.plugins.utils;

import java.io.File;


/**
 * This interface defines the basic methods that a <i>ScecVideo</i> object must implement
 * to set and retrieve data. Implementors such as <code>AbstractDataAccessor</code>
 * provide much of the functionality required to communicate with the ScecVideo
 * object library. This interface also guarantees that an object can be displayed or
 * hidden, and that the backing data file for large objects such as catalogs or
 * faults can be loaded or released from memory.
 *
 * Created on Feb 16, 2005
 * 
 * @author P. Powers
 * @version $Id: DataAccessor.java 3396 2010-07-15 18:37:27Z skohn $
 */
public interface DataAccessor extends Comparable {
	
    /**
     * Reads binary <i>ScecVideo</i> object data from a file.
     * 
     * @return whether operation was successful
     */
    public boolean readDataFile();
    
    /**
     * Writes binary <i>ScecVideo</i> object data to object library.
     * 
     * @return whether operation was successful
     */
    public boolean writeDataFile();
    
    /**
     * Reads <i>ScecVideo</i> object attribute data from a file.
     * 
     * @param file to read from
     * @return whether operation was successful
     */
    public boolean readAttributeFile(File file);
    
    /**
     * Writes <i>ScecVideo</i> object attribute data to a file.
     *      
     * @return whether operation was successful
     */
    public boolean writeAttributeFile();

    /**
     * Specifies if a <i>ScecVideo</i> object's data arrays and Java3D graphic
     * representations should be filled or cleared.
     * 
     * @param load whether to load or clear data arrays
     */
    public void setInMemory(boolean load);
    
    /**
     * Returns whether a <i>ScecVideo</i> object's data arrays and Java3D graphic
     * representations are loaded or not.
     * 
     * @return whether data loaded or not
     */
    public boolean isInMemory();
    
    /**
     * Specifies whether a <i>ScecVideo</i> object should be visible or not.
     * 
     * @param show whether to show or hide an object
     */
    public void setDisplayed(boolean show);
    
    /**
     * Returns if this <i>ScecVideo</i> object is displayed
     * 
     * @return whether visible or hidden
     */
    public boolean isDisplayed();
    
    /**
     * Returns the display name (not actual file name, although the two may be
     * similar) of a <i>ScecVideo</i> object.
     * 
     * @return the object's name
     */
    public String getDisplayName();

    /**
     * Sets the name of this <i>ScecVideo</i> object as it will be displayed in any GUIs.
     * 
     * @param name to set
     */
    public void setDisplayName(String name);
    
    /**
     * Returns a reference to a <i>ScecVideo</i> object's attribute file. An object's
     * attribute file is unique and can be used to test for equality of two objects.
     * 
     * @return attribute file reference
     */
    public File getAttributeFile();
    
    /**
     * Returns a library-relative path to an object's attribute file.
     * 
     * @return attribute file path
     */
    public String getAttributeFileLibPath();
    
    /**
     * Returns a reference to a <i>ScecVideo</i> object's data file.
     * 
     * @return data file reference
     */
    public File getDataFile();

    /**
     * Returns the citation-style reference for this <i>ScecVideo</i> object.
     * 
     * @return the object's citation
     */
    public String getCitation();
    
    /**
     * Sets the citation-style reference of this <i>ScecVideo</i> object.
     * 
     * @param citation to set
     */
    public void setCitation(String citation);
    
    /**
     * Returns the full reference of this <i>ScecVideo</i> object.
     * 
     * @return the object's reference
     */
    public String getReference();

    /**
     * Sets the full reference of this <i>ScecVideo</i> object.
     * 
     * @param reference to set
     */
    public void setReference(String reference);
    
    /**
     * Returns any additional notes or comments about this <i>ScecVideo</i> object.
     * 
     * @return the object's notes
     */
    public String getNotes();
    
    /**
     * Sets any additional notes or comments about this <i>ScecVideo</i> object.
     * 
     * @param notes to set
     */
    public void setNotes(String notes);

    /**
     * Returns whether this object is equivalent to another. Implementors should
     * be able to compare <i>ScecVideo</i> data objects as well as files.
     * 
     * @param object
     * @return whether objects are equal (have same source)
     */
    public boolean equals(Object object);
}
