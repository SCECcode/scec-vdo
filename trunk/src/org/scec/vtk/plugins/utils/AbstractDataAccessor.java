package org.scec.vtk.plugins.utils;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.scec.vtk.tools.Fixes;
import org.scec.vtk.tools.Prefs;

/**
 * This abstract class provides basic or empty implementations of most <code>DataAccessor</code>
 * methods in addition to several utility and accessor methods for class fields. For
 * objects with data backing stores, subclasses should generally provide
 * implementations of:
 * <pre>
 * readDataFile()
 * writeDataFile()
 * setObjectInMemory()
 * isObjectInMemory()
 * </pre>
 * If an object  type is non-displayable, empty implementations of:
 * <pre>
 * setDisplayed
 * isDisplayed
 * </pre>
 * are provided. Subclasses should also override and call the following methods to
 * save/add additional object attributes:
 * <pre>
 * newDocument()
 * newDocument(DataAccessor parent)
 * readAttributeFile(File file)
 * </pre>
 *
 * Created on Feb 14, 2005
 * 
 * @author P. Powers
 * @version $Id: AbstractDataAccessor.java 3396 2010-07-15 18:37:27Z skohn $
 */
public abstract class AbstractDataAccessor implements DataAccessor {

    private Logger log = Logger.getLogger(AbstractDataAccessor.class);
    
    private String objectClass;
    private String sourceFile;
    protected File attsFile;
    protected File dataFile;
    protected String displayName="";
    private String sourceCite;
    private String sourceRef;
    private String objNotes;
    public boolean infoAdded=false;
    
    private boolean display = false;
    
    /** XML <code>Element</code> for object info access */
    protected Element object_info;

    /** The <code>org.jdom.Document</code> for this object, available to subclasses for
     * customization if necessary.
     */
    protected Document objectDocument;
    
        
    /**
     * Creates a new, empty attribute file <code>Document</code>. At the <code>AbstractDataAccessor</code>
     * level, an object's attributes consist of object type, file locations, references, and user
     * notes. Concrete subclass overrides should initially call this method.
     * 
     * @return whether operation completed successfully
     */
    protected boolean newDocument() {
        SAXBuilder parser = new SAXBuilder();
        URL dataDoc = AbstractDataAccessor.class.getResource("components/resources/xml/object_template.xml");
        try {
            this.objectDocument = parser.build(dataDoc);
            this.object_info = this.objectDocument.getRootElement().getChild("object_info");
        }
        catch (Exception e) {
            log.debug("problem parsing XML");
            return false;
        }
        return true;
    }    
    
    /**
     * Empty implementation provided in case object doesn't have data store.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#readDataFile()
     */
    public boolean readDataFile() {
        return false;
    }
    
    /**
     * Empty implementation provided in case object doesn't have data store.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#writeDataFile()
     */
    public boolean writeDataFile() {
        return false;
    }
    
    /**
     * Empty implementation provided in case object doesn't have data store.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#setInMemory(boolean)
     */
    public void setInMemory(boolean load) {
        // empty implementation
    }
    
    /**
     * Empty implementation provided in case object doesn't have data store.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#isInMemory()
     */
    public boolean isInMemory() {
        return true;
    }
    
    /**
     * Basic functionality for setting whether an object is displayed. Subclasses should
     * override: Perform such tasks as adding or removing display representation of objects
     * to/from a Java3D scenegraph and then call <code>super()</code> to set display
     * value field. 
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#setDisplayed(boolean)
     */
    public void setDisplayed(boolean show) {
        this.display = show;
    }
    
    /**
     * Returns whether an object is displayed or not.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#isDisplayed()
     */
    public boolean isDisplayed() { 
        return this.display;
    }
    
    /** 
     * Compares this <code>DataAccessor</code> to another based their respective
     * display names; used for sorting.
     * 
     * @throws ClassCastException if object is not of type <code>DataAccessor</code>
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) throws ClassCastException {
        if (!(obj instanceof DataAccessor)) {
            throw new ClassCastException();
        }
        int compare = 0;
        try {
			compare = getDisplayName().compareTo(((DataAccessor)obj).getDisplayName());
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
        return compare;
    }

    /**
     * Determines whether this <i>ScecVideo</i> object is equal to another on the basis of
     * their respective unique attribute files. If <code>obj</code> is not a
     * <code>DataAccessor</code> or a <code>File</code> a class cast exceptioon is
     * thrown.
     * 
     * @throws ClassCastException if object is not of type <code>DataAccessor</code>
     * or <code>File</code>
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) throws ClassCastException {
        if (obj instanceof DataAccessor) {
            if (((DataAccessor)obj).getAttributeFile().equals(getAttributeFile())) {
                return true;
            }
            return false;
        } else if (obj instanceof File) {
            if (((File)obj).equals(getAttributeFile())) {
                return true;
            }
            return false;
        }
        throw new ClassCastException();
    }
        
    /**
     * Parses a <i>ScecVideo</i> object's attribute file and loads basic information common
     * to all <i>ScecVideo</i> objects (<code>DataAccessor</code>s). Concrete subclass overrides
     * should initially call this method to save this information along with
     * more detailed attributes particular to that object's class.<br/>
     * <br/>
     * The data are in XML format.
     * 
     * @see #writeAttributeFile()
     * @see org.scec.geo3d.plugins.utils.DataAccessor#readAttributeFile(java.io.File)
     */
    public boolean readAttributeFile(File file) {
        SAXBuilder parser = new SAXBuilder();
        try {
            this.objectDocument = parser.build(file);
            this.object_info = this.objectDocument.getRootElement().getChild("object_info");
        }
        catch (Exception e) {
            log.debug("problem parsing XML");
            return false;
        }
        // load common metadata fields
        
        this.objectClass    = this.object_info.getChild("class").getText();
        this.sourceFile     = Fixes.fixDirectory(this.object_info.getChild("source_file").getText());
        this.attsFile       = new File(Fixes.fixDirectory(Prefs.getLibLoc() +
                              this.object_info.getChild("attribute_file").getText()));
        this.setDataFile(Fixes.fixDirectory(this.object_info.getChild("data_file").getText()));
        this.displayName    = this.object_info.getChild("display_name").getText();
        this.sourceRef      = this.object_info.getChild("source_info").getChild("reference").getText();
        this.sourceCite     = this.object_info.getChild("source_info").getChild("citation").getText();
        this.objNotes       = this.object_info.getChild("notes").getText();
        return true;
    }
    /**
     * Writes a <i>ScecVideo</i> object's attribute file to disk. The data are in XML format.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#writeAttributeFile()
     */
    public boolean writeAttributeFile() {
        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            BufferedWriter xmlOut = new BufferedWriter(new FileWriter(this.attsFile));
            outputter.output(this.objectDocument, xmlOut);
            xmlOut.close();
        }
        catch (Exception e) {
            log.debug("problem writing XML");
            return false;
        }
        return true;
    }
        
    /**
     * Decodes a JDOM <code>Element</code> containing RGB color information and returns
     * a <code>Color</code>.
     * 
     * @param element the <code>Element</code> to decode
     * @return the decoded color
     * @see AbstractDataAccessor#writeColorElement(Element, Color)
     */
    public Color readColorElement(Element element) {
        Color c = null;
        if (element.getAttributeValue("r") != "null") {
            try {
                c = new Color(
                    element.getAttribute("r").getFloatValue(),
                    element.getAttribute("g").getFloatValue(),
                    element.getAttribute("b").getFloatValue());
            }
            catch (Exception e) {
                log.debug("problem parsing XML");
                return c;
            }
        }
        return c;
    }
    
    /**
     * Utility method that adds RGB color information to a JDOM <code>Element</code>.
     * 
     * @param element the <code>Element</code> to add info to
     * @param color the color to add
     * @see AbstractDataAccessor#readColorElement(Element)
     */
    public void writeColorElement(Element element, Color color) {
        if (color != null) {
            float[] colors = color.getRGBColorComponents(new float[3]);
            element.getAttribute("r").setValue(String.valueOf(colors[0]));
            element.getAttribute("g").setValue(String.valueOf(colors[1]));
            element.getAttribute("b").setValue(String.valueOf(colors[2]));
        } else {
            element.getAttribute("r").setValue("null");
            element.getAttribute("g").setValue("null");
            element.getAttribute("b").setValue("null");            
        }
    }
    
    /**
     * Returns the display name of this object.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.displayName;
    }
    
    /**
     * Sets the classname of this <i>ScecVideo</i> object. This is
     * generally the name of the plugin object this data represents.
     * 
     * @param classname name of plugin-wrapped object class
     */
    public void setObjectClass(String classname) {
        this.objectClass = classname;
        this.object_info.getChild("class").setText(classname);
    }
    
    /**
     * Returns the name of the plugin object class this data represents.
     * 
     * @return the class name
     */
    public String getObjectClass() {
        return this.objectClass;
    }
        
    /**
     * Sets the source filename that contains the original data for this
     * <i>ScecVideo</i> object.
     * 
     * @param filename name (full path) of file
     */
    public void setSourceFile(String filename) {
        this.sourceFile = filename;
        this.object_info.getChild("source_file").setText(filename);
    }
    
    /**
     * Returns the full path of the current <i>ScecVideo</i> object source file.
     * 
     * @return pathname of source file
     */
    public String getSourceFile() {
        return this.sourceFile;
    }
    
    /**
     * Sets the path and name of the attribute output file for this ScecVideo
     * object.
     * 
     * @param filename the attributes file to set
     */
    public void setAttributeFile(String filename) {
        this.attsFile = new File(Prefs.getLibLoc() + filename);
        this.object_info.getChild("attribute_file").setText(filename);
    }

    /**
     * Returns a reference to the attributes file for
     * this <i>ScecVideo</i> object.
     * 
     * @return the attribute file of this object
     */
    public File getAttributeFile() {
        return this.attsFile;
    }

    /**
     * Returns a library-relative path to a ScecVideo object's attribute file.
     * 
     * @return the attribute file path
     */
    public String getAttributeFileLibPath() {
        return this.object_info.getChild("attribute_file").getText();
    }
    
    /**
     * Sets the path and name of the binary data output file for this
     * <i>ScecVideo</i> object.
     * 
     * @param filename the binary data file to set
     */
    public void setDataFile(String filename) {
        this.dataFile = new File(Prefs.getLibLoc() + filename);
        this.object_info.getChild("data_file").setText(filename);
    }

    /**
     * Returns a reference to the binary data file for 
     * this <i>ScecVideo</i> object.
     * 
     * @return the data file of this object
     */
    public File getDataFile() {
        return this.dataFile;
    }

    /**
     * Sets the name of this <i>ScecVideo</i> object as it will appear in any GUIs.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#setDisplayName(java.lang.String)
     */
    public void setDisplayName(String name) {
        this.displayName = name;
        this.object_info.getChild("display_name").setText(name);
    }

    /**
     * Returns the name of this <i>ScecVideo</i> object as is will appear
     * in any GUIs.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#getDisplayName()
     */
    public String getDisplayName() {
        return this.displayName;
    }
    
    /**
     * Sets any additional notes or coments about this <i>ScecVideo</i> object.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#setNotes(java.lang.String)
     */
    public void setNotes(String notes) {
        this.objNotes = notes;
        this.object_info.getChild("notes").setText(notes);
    }
    
    /**
     * Returns any additional notes or comments about this <i>ScecVideo</i> object.
     *
     * @see org.scec.geo3d.plugins.utils.DataAccessor#getNotes()
     */
    public String getNotes() {
        return this.objNotes;
    }

    /**
     * Returns a citation style reference (e.g <i>Hauksson(2003)</i>) for this
     * <i>ScecVideo</i> object.
     *
     * @see org.scec.geo3d.plugins.utils.DataAccessor#getCitation()
     */
    public String getCitation() {
        return this.sourceCite;
    }
    
    /**
     * Sets the a citation style reference (e.g <i>Hauksson(2003)</i>) for this
     * <i>ScecVideo</i> object.
     *
     * @see org.scec.geo3d.plugins.utils.DataAccessor#setCitation(java.lang.String)
     */
    public void setCitation(String citation) {
        this.sourceCite = citation;
        this.object_info.getChild("source_info").getChild("citation").setText(citation);
    }
    
    /**
     * Returns the full reference for this <i>ScecVideo</i> object.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#getReference()
     */
    public String getReference() {
        return this.sourceRef;
    }
    
    /**
     * Sets the full reference for this <i>ScecVideo</i> object.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#setReference(java.lang.String)
     */
    public void setReference(String reference) {
        this.sourceRef = reference;
        this.object_info.getChild("source_info").getChild("reference").setText(reference);
    }

}

