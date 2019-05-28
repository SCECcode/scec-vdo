package org.scec.vtk.plugins.CommunityfaultModelPlugin.components;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.scec.vtk.plugins.utils.*;
import org.scec.vtk.tools.Prefs;

/**
 * This class defines a <code>Group</code> of <code>Fault3D</code>s that can be saved,
 * displayed, and selected through a <code>GroupList</code>. A <code>Group</code> contains
 * an array of <code>File</code> references to <code>Fault3D</code> objects as well as the
 * XML/JDOM <code>Element</code> that is used to save it in the <i>groups.xml</i> file.
 * <br/><br/>
 * <font color="red">
 * TODO: make a utility component
 * </font>
 * Created on Jan 30, 2005
 * 
 * @author P. Powers
 * @version $Id: Group.java 2109 2008-07-08 20:35:22Z ihrig $
 * 
 * Testing CVS versioning.
 */
public class Group {
    
    private String name;
    private ArrayList objList;
    private Element groupElement;
    
    /**
     * Constructs a new <code>Group</code> with the given name and containg the given
     * objects. Constructor builds the required JDOM <code>Element</code> from the
     * parameters provided.
     * 
     * @param groupName name of the new group
     * @param groupFiles array of <code>DatatAccessor</code>s in this <code>Group</code>
     */
    public Group(String groupName, ArrayList groupFiles) {
        this.name = groupName;
        this.objList = groupFiles;
        // build Element
        this.groupElement = new Element("group");
        this.groupElement.setAttribute("name",this.name);
        ListIterator li = this.objList.listIterator();
        while (li.hasNext()) {
            Element e = new Element("object");
            e.addContent(((DataAccessor)li.next()).getAttributeFileLibPath());
            this.groupElement.addContent(e);
        }
    }
    
    /**
     * Constructs a new <code>Group</code> from the given JDOM <code>Element</code> and
     * <code>AbstractLibraryModel</code>. The constructor pulls references to objects from
     * the library model that match attribute filenames saved in the group. If a group
     * member has been deleted, it's reference is deleted from the group file.
     * 
     * @param element data <code>Element</code> with <code>Group</code> info
     * @param objectLib library data model passed so method can cull objects for group
     */
    public Group(Element element, AbstractLibraryModel objectLib) {
        this.groupElement = element;
        // build name
        this.name = element.getAttributeValue("name");
        // build fault file array
        List faultList = element.getChildren("object");
        ListIterator li = faultList.listIterator();
        this.objList = new ArrayList();
        
        while (li.hasNext()) {
            DataAccessor obj = objectLib.findObject(
                    new File(Prefs.getLibLoc() + ((Element)li.next()).getText()));
            if (obj != null) {
                this.objList.add(obj);
            }
        }
    }
    
    /**
     * Sets the name of this <code>Group</code>.
     * 
     * @param groupName to set
     */
    public void setName(String groupName) {
        this.name = groupName;
    }
    
    /**
     * Returns the name of this <code>Group</code>.
     * 
     * @return the <code>Group</code> name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns the name of the <code>Group</code>. Same as <code>getName()</code>.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.name;
    }
    
    /**
     * Sets the <code>DataAccessor</code> references that make up this <code>Group</code>.
     * 
     * @param objects for this <code>Group</code>
     */
    public void setObjects(ArrayList objects) {
        this.objList = objects;
    }
    
    /**
     * Returns an array of  <code>DataAccessor</code>s
     * in this <code>Group</code>.
     * 
     * @return array of file references
     */
    public ArrayList getObjects() {
        return this.objList;
    }

    /**
     * Returns a JDOM/XML <code>Element</code> of all the information in this <code>Group</code>.
     * 
     * @return JDOM <code>Element</code>
     */
    public Element getGroupElement() {
        return this.groupElement;
    }
}

