package org.scec.vtk.plugins.CommunityfaultModelPlugin.components;

import java.awt.Component;
import java.awt.Container;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.*;
import org.scec.vtk.tools.Prefs;

/**
 * This class maintains the list of fault groups recorded for a project. It
 * uses a generic <code>DefaultListModel</code> for data and handles the tasks of
 * list creation and deletion.
 * <br/><br/>
 * <font color="red">
 * TODO: make a utility component
 * </font>
 * Created on Jan 30, 2005
 * 
 * @author P. Powers
 * @version $Id: GroupList.java 4344 2013-07-02 16:08:05Z villasana $
 */
public class GroupList extends JList implements ListSelectionListener {

    private static final long serialVersionUID = 1L;

	private Logger log = Logger.getLogger(GroupList.class);
    
    // owner, data source, and data file
    private Container listOwner;
    private FaultTable faultTable;
    private File groupFile;
    
    // list access fields
    private ListSelectionModel selModel;
    private DefaultListModel listModel;
    
    // XML components
    private Document groupDoc;
    private Element groupDocRoot;
    
    /**
     * Constructs a new <code>GroupList</code> with a given owner (for listener addition)
     * and table data source.
     * 
     * @param owner object that is registered to for notifications of changes to the GroupList
     * contents and selection
     * @param dataTable table with data for <code>Group</code>s
     */
    public GroupList(Container owner, FaultTable dataTable) {
        super();
        this.listOwner = owner;
        this.faultTable = dataTable;
        this.init();
    }
    
    //****************************************
    //     PUBLIC UTILITY METHODS
    //****************************************

    /**
     * Creates a new <code>Group</code> from visible objects in linked table. Prompts
     * user for group name.
     * 
     */
    public void createGroup() {
        
        String groupName = JOptionPane.showInputDialog(
                this.listOwner,
                "Please provide a name for this group:",
                "Set Group ID",
                JOptionPane.QUESTION_MESSAGE);
        if (groupName == null) return;
        
        // check for overwrite of existing group
        Group existingGroup = groupNameExists(groupName);
        
        if (existingGroup != null) {
            int overwrite = JOptionPane.showConfirmDialog(
                    this.listOwner,
                    "Do you want to overwrite an existing group?",
                    "Overwrite Group?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            switch (overwrite) {
                case JOptionPane.CANCEL_OPTION:
                    // abort create
                    return;
                case JOptionPane.CLOSED_OPTION:
                    return;
                case JOptionPane.NO_OPTION:
                    // return to top of createGroup() method 
                    createGroup();
                    return;
                case JOptionPane.YES_OPTION:
                    removeGroup(existingGroup);
            }
        }
        ArrayList faultList = this.faultTable.getLibraryModel().getVisibleObjects();
        Group newGroup = new Group(groupName, faultList);
        this.groupDocRoot.addContent(newGroup.getGroupElement());
        writeGroupFile();
        this.listModel.addElement(newGroup);        
    }
    
    /**
     * Loads <code>Group</code>s from a users groups.xml file.
     * C:\Users\Kameron Johnson\ScecVideoPrefs\Fault3DStore\groups.xml - file being stored
     */
    public void loadGroups() {
        String groupFileName = Prefs.getLibLoc() + 
                               File.separator + CommunityFaultModelPlugin.dataStoreDir + 
                               File.separator + "groups.xml";
        this.groupFile = new File(groupFileName);
        SAXBuilder parser = new SAXBuilder();
        try {
            // Build group document and set root element
            this.groupDoc = this.groupFile.exists() ?
                    parser.build(this.groupFile) : 
                    parser.build(GroupList.class.getResource("resources/xml/groups.xml"));
            this.groupDocRoot = this.groupDoc.getRootElement();
            //set list of groups  
            List list = this.groupDocRoot.getChildren("group");
            if (list.size() > 0) {
                ListIterator li = list.listIterator();
                while (li.hasNext()) {  
                    // create new Group
                    Group g = new Group((Element)li.next(), this.faultTable.getLibraryModel());
                    // add Group to group list
                    this.listModel.addElement(g);       
                }
            }
        }
        catch (Exception e) {
            log.debug("problem parsing XML");
        }
    }
        
    /**
     * Deletes a <code>Group</code> from this <code>GroupList</code>. This method
     * removes group reference from groups.xml but does not delete source files for 
     * group members.
     * 
     * @param group to be deleted
     */
    public void deleteGroup(Group group) {
        
        // prompt for confirmation
        int delete = JOptionPane.showConfirmDialog(
                this.listOwner,
                "Are you sure you want to delete the selected group?\n(Actual data files are not deleted)",
                "Delete Group?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (delete == JOptionPane.NO_OPTION) return;        
        
        removeGroup(group);
        
        // rewrite xml file
        writeGroupFile();
    }

    //****************************************
    //     PRIVATE METHODS
    //****************************************

    private void init() {
        
        // Init selection model and register GUI as listener for button enabling.
        this.selModel = getSelectionModel();
        this.selModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.selModel.addListSelectionListener((ListSelectionListener)this.listOwner);
        this.selModel.addListSelectionListener(this);
        
        // Init list model
        this.listModel = new DefaultListModel();
        setModel(this.listModel);
        
        // Init renderer
        setCellRenderer(new GroupListRenderer());
    }
    
    // removes a group from list and source xml structure
    public void removeGroup(Group group) {
        // remove from list
        this.listModel.removeElement(group);
        // remove from xml source
        ListIterator li = this.groupDocRoot.getChildren("group").listIterator();
        while (li.hasNext()) {
            Element e = (Element)li.next();
            String name = e.getAttributeValue("name");
            if (name.equals(group.getName())) {
                this.groupDocRoot.removeContent(e);
                break;
            }
        }
        // clear table selections
        this.faultTable.clearSelection();
    }

    // writes groups.xml to disk
    private void writeGroupFile() {
        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            BufferedWriter xmlOut = new BufferedWriter(new FileWriter(this.groupFile));
            outputter.output(this.groupDoc, xmlOut);
            xmlOut.close();
        }
        catch (IOException e) {
            log.debug("problem writing XML");
        }
    }
    
    // tests if group name already exists
    private Group groupNameExists(String name) {
        for (int i=0; i<this.listModel.getSize(); i++) {
            Group group = (Group)this.listModel.getElementAt(i);
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }
    
    
    //****************************************
    //     EVENT HANDLERS
    //****************************************
    
    /**
     * Required event-handler method. This method processes <code>GroupList</code> selections
     * and selects items in linked table based on the selection.
     * 
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        if (getSelectedValue() != null) {
            this.faultTable.setSelected(((Group)getSelectedValue()).getObjects());
        } else {
            this.faultTable.clearSelection();
        }
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
    private class GroupListRenderer extends DefaultListCellRenderer {
        
        private static final long serialVersionUID = 1L;

		GroupListRenderer() {
            super();
        }
        
        /**
         * Required method of custom cell renderers that gets called to render 
         * <code>GroupList</code> items.
         * 
         * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(
                JList list, Object group, int index,
                boolean isSelected, boolean hasFocus) {
            
            this.setText(((Group)group).getName());
            this.setBorder(BorderFactory.createEmptyBorder(3,7,3,7));
            
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
