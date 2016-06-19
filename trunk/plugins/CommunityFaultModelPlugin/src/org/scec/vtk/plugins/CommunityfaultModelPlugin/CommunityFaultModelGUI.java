package org.scec.vtk.plugins.CommunityfaultModelPlugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;






import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;




import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.utils.DataAccessor;
import org.scec.vtk.plugins.utils.components.AddButton;
import org.scec.vtk.plugins.utils.components.ColorButton;
import org.scec.vtk.plugins.utils.components.DataFileChooser;
import org.scec.vtk.plugins.utils.components.EditButton;
import org.scec.vtk.plugins.utils.components.MeshButton;
import org.scec.vtk.plugins.utils.components.ObjectInfoDialog;
import org.scec.vtk.plugins.utils.components.RemoveButton;
import org.scec.vtk.plugins.utils.components.ShowButton;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;
import org.scec.vtk.tools.Prefs;

import vtk.vtkActor;

import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.Fault3D;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.FaultAccessor;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.FaultTable;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.FaultTableModel;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.Group;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.GroupList;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.TSurfImport;


public class CommunityFaultModelGUI  extends JPanel implements ActionListener, ListSelectionListener, TableModelListener {

	private static final long serialVersionUID = 1L;
	// fault library panel accessible components
	private JPanel         	 faultLibraryPanel;
	protected FaultTable     faultTable;
	protected ShowButton     showFaultsButton;
	protected MeshButton     meshFaultsButton;
	protected ColorButton    colorFaultsButton;
	protected EditButton     editFaultsButton;
	protected AddButton      addFaultsButton;
	protected RemoveButton   remFaultsButton;
	//protected SaveButton   savFaultsButton;

	// group list accessible components
	private JPanel       groupsPanel;
	private GroupList    groupList;
	private AddButton    newGroupButton;
	private RemoveButton delGroupButton;

	// notes panel adjustable components
	private JPanel    propsNotesPanel;
	private JTextArea faultNotes;

	// accessible panels
	private JTabbedPane propsTabbedPane;

	// accessory windows and dialogs
	private DataFileChooser    fileChooser;
	private SingleColorChooser colorChooser;
	private ObjectInfoDialog   srcInfoDialog;

	//private FaultPickBehavior pickBehavior;

	//filter added by Ryan Berti 2008
	private boolean ROIFilter = false;//beta version of ROI filter for fault filteration
	//private ArrayList<RegionWrapper> plist = new ArrayList<RegionWrapper>();//polygon list used to filter vertices
	ArrayList<FaultAccessor> inlist;
	private JButton filterButton = new JButton("Filter through ROIs");

	// init data store
	static {
		String dataStore =
				Prefs.getLibLoc() +
				File.separator + CommunityFaultModelPlugin.dataStoreDir +
				File.separator + "data";
		File f;
		if (!(f = new File(dataStore)).exists()) f.mkdirs();
	}

	//branch group connection to core branch group
	private PluginActors actors;


	/**
	 * this constructor accepts the directory names.
	 * Then this GUI reads that directory and pre-loads the T-Surf file names into the GUI
	 * @param cfmFilesDirectory
	 */
	public CommunityFaultModelGUI(String cfmFilesDirectory, String groupName, PluginActors actors) {
		super();
		initialize(actors);
		//this.groupList.dedeleteGroupgroupName);
		File dir = new File(cfmFilesDirectory);
		// only inlcude files which have .ts extension
		File[] f = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dirName, String name) {
				if(name.endsWith(".ts")) return true;
				return false;
			}
		});
		if (f != null) {
			TSurfImport tsImport = new TSurfImport(this, f);
			ArrayList newObjects = tsImport.processFiles(false, groupName);
			if (newObjects.size() > 0) {
				this.faultTable.addFaults(newObjects);
			}
		}
	}

	private void initialize(PluginActors actors) {
		this.actors = actors;
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(
				Prefs.getPluginWidth(), Prefs.getPluginHeight()));

		// add library to gui
		add(getFaultLibraryPanel(), BorderLayout.CENTER);

		// assemble properties tabbed pane
		this.propsTabbedPane = new JTabbedPane();
		this.propsTabbedPane.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
		this.propsTabbedPane.add(getGroupsPanel());
		this.propsTabbedPane.add(getNotesPanel());

		// assemble lower pane
		JPanel lowerPane = new JPanel();
		lowerPane.setLayout(new BoxLayout(lowerPane,BoxLayout.PAGE_AXIS));
		lowerPane.add(this.propsTabbedPane);

		// add lower pane to gui
		add(lowerPane, BorderLayout.PAGE_END);

		// other initializations


	}


	/**
	 * Constructs a new <code>FaultGUI</code>. This constructor builds a custom
	 * <code>JPanel</code> to allow user control of 3D fault data.
	 *
	 */
	public CommunityFaultModelGUI(PluginActors pluginActors) {
		super();
		initialize(pluginActors);
		// now load any data
		// TODO
		this.faultTable.loadLibrary();
		this.groupList.loadGroups();
	}

	/**
	 * Method centralizes button enabling based on selections. An object's state
	 * may change without changing a selection so a means to alter button state's
	 * is needed outside of event handlers.
	 */
	public void processTableSelectionChange() {
		int[] selectedRows = this.faultTable.getSelectedRows();
		if (selectedRows.length > 0) {
			this.remFaultsButton.setEnabled(true);
			this.editFaultsButton.setEnabled(true);
			if (this.faultTable.getLibraryModel().allAreLoaded(selectedRows)) {
				enablePropertyEditButtons(true);
			} else if (this.faultTable.getLibraryModel().noneAreLoaded(selectedRows)) {
				enablePropertyEditButtons(true);
			} else {
				enablePropertyEditButtons(true);
			}
		} else {
			enablePropertyEditButtons(false);
			this.remFaultsButton.setEnabled(false);
			this.editFaultsButton.setEnabled(false);
		}

		// notes panel
		if (selectedRows.length == 1) {
			setNotesPanel(this.faultTable.getLibraryModel().getObjectAtRow(
					this.faultTable.getSelectedRow()));
		} else {
			setNotesPanel(null);
		}

	}

	//****************************************
	//     PRIVATE GUI METHODS
	//****************************************

	private JPanel getFaultLibraryPanel() {

		// set up panel
		this.faultLibraryPanel = new JPanel(new BorderLayout());
		this.faultLibraryPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		this.faultLibraryPanel.setName("Library");
		this.faultLibraryPanel.setOpaque(false);

		// set up scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// set up table
		this.faultTable = new FaultTable(this);
		scroller.setViewportView(this.faultTable);
		scroller.getViewport().setBackground(this.faultTable.getBackground());
		this.faultLibraryPanel.add(scroller,BorderLayout.CENTER);
		this.faultLibraryPanel.add(getFaultLibraryBar(),BorderLayout.PAGE_END);

		return this.faultLibraryPanel;
	}

	private JPanel getFaultLibraryBar() {

		this.showFaultsButton = new ShowButton(this, "Toggle visibility of selected fault(s)");
		this.colorFaultsButton = new ColorButton(this, "Change color of selected fault(s)");
		this.meshFaultsButton = new MeshButton(this, "Toggle mesh state of selected fault(s)s");
		this.editFaultsButton = new EditButton(this, "Edit fault information");
		this.addFaultsButton = new AddButton(this, "Add/Import new faults");
		this.remFaultsButton = new RemoveButton(this, "Remove selected fault(s)");

		JPanel bar = new JPanel();
		bar.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
		bar.setLayout(new BoxLayout(bar,BoxLayout.LINE_AXIS));
		bar.setOpaque(true);
		int buttonSpace = 3;

		bar.add(this.showFaultsButton);
		bar.add(Box.createHorizontalStrut(buttonSpace));
		bar.add(this.colorFaultsButton);
		bar.add(Box.createHorizontalStrut(buttonSpace));
		bar.add(this.meshFaultsButton);
		bar.add(Box.createHorizontalStrut(buttonSpace));
		//bar.add(this.editFaultsButton);
		bar.add(Box.createHorizontalGlue());
		bar.add(Box.createHorizontalStrut(buttonSpace));
		bar.add(Box.createHorizontalGlue());
		//bar.add(this.savFaultsButton);
		bar.add(this.editFaultsButton);
		bar.add(Box.createHorizontalStrut(buttonSpace));
		bar.add(this.addFaultsButton);
		bar.add(Box.createHorizontalStrut(buttonSpace));
		bar.add(this.remFaultsButton);

		this.filterButton.addActionListener(this);
		bar.add(Box.createHorizontalStrut(buttonSpace));
		bar.add(this.filterButton);

		return bar;
	}

	private JPanel getGroupsPanel() {

		// set up panel
		this.groupsPanel = new JPanel(new BorderLayout());
		//this.groupsPanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		//this.groupsPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		this.groupsPanel.setName("Groups");
		this.groupsPanel.setOpaque(false);

		// set up scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// set up list
		this.groupList = new GroupList(this, this.faultTable);
		scroller.setViewportView(this.groupList);
		scroller.getViewport().setBackground(this.groupList.getBackground());

		// assembly
		this.groupsPanel.add(scroller,BorderLayout.CENTER);
		this.groupsPanel.add(getGroupBar(),BorderLayout.PAGE_END);

		return this.groupsPanel;
	}

	private JPanel getGroupBar() {

		this.newGroupButton = new AddButton(this, "Create a new group from visible faults");
		this.delGroupButton = new RemoveButton(this, "Delete selected group(s)");

		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar,BoxLayout.LINE_AXIS));
		bar.setOpaque(false);
		bar.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		bar.add(Box.createHorizontalGlue());
		bar.add(this.newGroupButton);
		bar.add(Box.createHorizontalStrut(3));
		bar.add(this.delGroupButton);

		return bar;
	}

	private JPanel getNotesPanel() {
		this.propsNotesPanel = new JPanel(new BorderLayout());
		this.propsNotesPanel.setOpaque(false);
		this.propsNotesPanel.setName("Notes");
		JScrollPane notesScrollPane = new JScrollPane();
		notesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		notesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.faultNotes = new JTextArea();
		this.faultNotes.setEditable(false);
		this.faultNotes.setWrapStyleWord(true);
		this.faultNotes.setLineWrap(true);
		notesScrollPane.setViewportView(this.faultNotes);
		this.propsNotesPanel.add(notesScrollPane, BorderLayout.CENTER);
		return this.propsNotesPanel;
	}

	private void setNotesPanel(DataAccessor fault) {
		if (fault != null) {
			this.faultNotes.setText(fault.getNotes());
		} else {
			this.faultNotes.setText("");
		}
	}

	public ObjectInfoDialog getSourceInfoDialog() {
		if (this.srcInfoDialog == null) {
			this.srcInfoDialog = new ObjectInfoDialog(this);
		}
		return this.srcInfoDialog;
	}

	// perhaps this should be static
	private void runObjectInfoDialog(ArrayList objects) {
		ObjectInfoDialog oid = getSourceInfoDialog();
		if (objects.size() == 1) {
			DataAccessor obj = (DataAccessor)objects.get(0);
			oid.showInfo(obj, "Edit Fault Information");
			if (oid.windowWasCancelled()) return;
			setNotesPanel(obj);
			obj.writeAttributeFile();
		} else {
			oid.showInfo("Edit Fault Information");
			if (oid.windowWasCancelled()) return;
			for (int i=0; i<objects.size(); i++) {
				((DataAccessor)objects.get(i)).setCitation(this.srcInfoDialog.getCitation());
				((DataAccessor)objects.get(i)).setReference(this.srcInfoDialog.getReference());
				((DataAccessor)objects.get(i)).setNotes(this.srcInfoDialog.getNotes());
				((DataAccessor)objects.get(i)).writeAttributeFile();
			}
		}
	}

	private void enablePropertyEditButtons(boolean enable) {
		this.showFaultsButton.setEnabled(enable);
		this.meshFaultsButton.setEnabled(enable);
		this.colorFaultsButton.setEnabled(enable);
	}

	//following functions are used by ROIfilter to display only earthquakes encompassed by user regions, Ryan Berti 2008
	//function called when ROIfilter button is pushed
	public boolean setROIFilter(){
		return ROIFilter;
		/*if(!ROIFilter){
		ROIFilter = true;
		System.out.println("ROI filter is on");
		filterButton.setText("Stop Filtering");

		Point3d[] parray;
		plist = Geo3dInfo.getRegions();
		inlist = faultTable.getSelected();
		ArrayList<FaultAccessor> outlist = new ArrayList<FaultAccessor>();
		for(int i=0;i<inlist.size();i++){
			parray = inlist.get(i).getVertices();
			for(int j=0;j<parray.length;j++){
				if(isContained((float)PointToLatLong.WorldPointToLat(parray[j]),(float)PointToLatLong.WorldPointToLong(parray[j]))){
					outlist.add(inlist.get(i));
					break;
				}
			}
		}
		faultTable.setSelected(outlist);
	}else{
		ROIFilter = false;
		System.out.println("ROI filter is off");
		filterButton.setText("Filter through ROIs");
		faultTable.setSelected(inlist);
	}
	return ROIFilter;*/
	}


	//checks if earthquake is in any polygons
	private boolean isContained(float lat, float lon){
		/*Location loc = new Location(lat, lon);
	if(plist.size() != 0){
		for(int i = 0; i < plist.size(); i++){
			RegionWrapper rw = plist.get(i);
			if(rw.getRegion().contains(loc)){//look at Geo3dInfo comments for why lat and long are multiplied by 1000
				return true;
			}
		}
	}*/
		return false;
	}


	//****************************************
	//     EVENT HANDLERS
	//****************************************

	/**
	 * Required event-handler method that processes selection changes to the <code>FaultTable</code>
	 * and <code>GroupList</code> in the gui. Method enables or disables buttons according to
	 * selection.
	 *
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {

		Object src = e.getSource();
		FaultTableModel libModel  = this.faultTable.getLibraryModel();
		if (e.getValueIsAdjusting()) return;

		if (src == this.faultTable.getSelectionModel()) {
			processTableSelectionChange();


		} else if (src == this.groupList.getSelectionModel()) {
			// process events from list of fault groups
			if (this.groupList.getSelectedIndices().length > 0) {
				this.delGroupButton.setEnabled(true);
			} else {
				this.delGroupButton.setEnabled(false);
			}
		}
	}

	/**
	 * Required event-handler method that processes changes to data displayed in the
	 * <code>FaultTable</code> of the gui. Method enables or disables buttons
	 * according to whether any fault data is loaded in the table and/or visible
	 * in the Java3D scenegraph.
	 *
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {

		FaultTableModel libModel  = this.faultTable.getLibraryModel();

		// check if tableModel has at least 1 loaded object(fault)
		// and enable "Save" state button
		//    if (libModel.anyAreLoaded()) {
		//        this.savFaultsButton.setEnabled(true);
		//    } else {
		//        this.savFaultsButton.setEnabled(false);
		//    }

		// check if any active/loaded faults are visible
		// and enable "Create" group button
		if (libModel.anyAreVisible()) {
			this.newGroupButton.setEnabled(true);
		} else {
			this.newGroupButton.setEnabled(false);
		}

	}

	/**
	 * Required event-handler method that processes user interaction with gui buttons.
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		FaultTableModel libModel  = this.faultTable.getLibraryModel();

		Object src = e.getSource();


		/////////////////////////
		// FAULT DISPLAY PANEL //
		/////////////////////////

		if (src == this.showFaultsButton) {
			libModel.setLoadedStateForRows(true, this.faultTable.getSelectedRows());
			processTableSelectionChange();
			//libModel.toggleVisibilityForRows(this.faultTable.getSelectedRows());
			ArrayList<Fault3D> selectedFaults = faultTable.getSelected();
			for (Fault3D fault : selectedFaults) {
				vtkActor actor = fault.getFaultActor();
				int visisble = actor.GetVisibility();
				if (visisble == 0) {
					actor.VisibilityOn();
				}
				else {
					actor.VisibilityOff();
				}
			}
			Info.getMainGUI().updateRenderWindow();
		} else if (src == this.meshFaultsButton) {
			libModel.toggleMeshStateForRows(this.faultTable.getSelectedRows());
		} else if (src == this.colorFaultsButton) {
			if (this.colorChooser == null) {
				this.colorChooser = new SingleColorChooser(this);
			}
			Color newColor = this.colorChooser.getColor();
			if (newColor != null) {
				libModel.setColorForRows(newColor, this.faultTable.getSelectedRows());
				//update mesh color of the actor
				int[] selectedRows = this.faultTable.getSelectedRows();
				ArrayList<Fault3D> selectedFaults = faultTable.getSelected();
				for (Fault3D fault : selectedFaults) {
					vtkActor actor = fault.getFaultActor();
					//only between 0 and 1;
					double[] color = {newColor.getRed()/Info.rgbMax,newColor.getGreen()/Info.rgbMax,newColor.getBlue()/Info.rgbMax};
					actor.GetProperty().SetColor(color);
				}
				MainGUI.updateRenderWindow();
			}
		} else if (src == this.editFaultsButton) {
			runObjectInfoDialog(this.faultTable.getSelected());
		} else if (src == this.addFaultsButton) {
			if (this.fileChooser == null) {
				this.fileChooser = new DataFileChooser(this, "Import Fault Files", true,new File(MainGUI.getRootPluginDir() + File.separator + "Faults"));
			}
			this.fileChooser.setCurrentFilter("ts", "GoCAD (*.ts)");
			File[] f = this.fileChooser.getFiles();
			if (f != null) {
				TSurfImport tsImport = new TSurfImport(this, f);
				ArrayList newObjects = tsImport.processFiles();
				if (newObjects.size() > 0) {
					this.faultTable.addFaults(newObjects);
					int faultTableRows = this.faultTable.getRowCount();
					//reloading as the faults are sorted alphabetically 
					List loadedRows = this.faultTable.getLibraryModel().getAllObjects();
					for(int i = 0; i < loadedRows.size(); i++)
					{
						Fault3D  fault =(Fault3D) loadedRows.get(i);
						System.out.println("Adding "+fault.getDisplayName());
						vtkActor actor = (fault.getFaultActor());
						actors.addActor(actor);
					}
					MainGUI.updateRenderWindow();
				}
			}
		} else if (src == this.remFaultsButton) {
			int[] selectedRows = this.faultTable.getSelectedRows();
			ArrayList<Fault3D> selectedFaults = faultTable.getSelected();
			ArrayList<vtkActor> removedActors = new ArrayList<vtkActor>();
			int delete = libModel.deleteObjects(
					this.faultTable,
					selectedRows);
			if (delete == JOptionPane.OK_OPTION) {
				//remove actors
				for (Fault3D fault : selectedFaults) {
					vtkActor actor = fault.getFaultActor();
					actors.removeActor(actor);
				}
			}
			MainGUI.updateRenderWindow();



		}else if(src == this.filterButton){
			this.setROIFilter();
		}




		/////////////////////////
		// PROPS DISPLAY PANEL //
		/////////////////////////

		else if (src == this.newGroupButton) {
			// create a group from currently selected (visible faults);
			this.groupList.createGroup();


		} else if (src == this.delGroupButton) {
			// delete named group
			this.groupList.deleteGroup((Group)this.groupList.getSelectedValue());
		}


	}



	/*public void setPickable(boolean enable) {
	pickBehavior.setEnable(enable);
}*/

}

