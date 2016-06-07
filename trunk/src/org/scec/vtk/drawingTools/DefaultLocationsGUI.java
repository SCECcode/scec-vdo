 package org.scec.vtk.drawingTools;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.media.j3d.BranchGroup;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.scec.vtk.main.Info;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.DBFReaderJGeom;
import oracle.spatial.util.ShapefileReaderJGeom;
import vtk.vtkTextActor3D;



/**
 *	author: Kristy Akullian
 *	author: Jessica McMorris
 *	author: Becky Gallagher
 *	author: Tom Robinson
 */

public class DefaultLocationsGUI extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final String dataPath = Info.getMainGUI().getRootPluginDir()+File.separator+"GISLocationPlugin"+File.separator;

	private DrawingToolsGUI guiparent;
// We took out the ability to search for a city within this tab as we have a better search
// functionality in its own tab. I'll leave this commented out in case at any point in the future
// we decide it is worthwhile to have a search that doesn't require internet access.
//	private JButton searchButton = new JButton("Go!");
//	private JTextField searchCity = new JTextField();
	
	private ArrayList<PresetLocationGroup> presetLocationGroups = new ArrayList<PresetLocationGroup>();
	private String selectedInputFile;
	
	private DisplayAttributes displayAttributes;
	private JPanel centerPanel = new JPanel();
	private JScrollPane defaultScrollPane = new JScrollPane(centerPanel);

	private DrawingToolsTable drawingToolTable;
	private DrawingToolsTableModel drawingTooltablemodel;
	private int defaultLocationsStartIndex = 0;
	
	
	public DefaultLocationsGUI(DrawingToolsPlugin parent, DrawingToolsGUI guiparent) {
		this.guiparent = guiparent;
		this.drawingToolTable = guiparent.getTable();
		this.drawingTooltablemodel = drawingToolTable.getLibraryModel();
		this.defaultLocationsStartIndex = 0;//this.drawingToolTable.getRowCount();
		// Set main panel layout manager and dimensions
		this.setLayout(new BorderLayout(5,5));
		this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		// North Panel
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));
		
		// Center Panel
		centerPanel.setLayout(new GridLayout(0,2));

		// Build the presets panel automatically
		
		// Check to make sure it exists
		File dataDirectory = new File(dataPath);
		if (dataDirectory.isDirectory()) {
			// List files in the directory and process each
			File files[] = dataDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile() && files[i].getName().endsWith(".shp")) {
					PresetLocationGroup tempGroup = new PresetLocationGroup();
					
					tempGroup.file = files[i];
					
					String tempName = files[i].getName();
					tempName = tempName.substring(0, tempName.lastIndexOf("."));
					tempName = tempName.replace('_', ' ');
					tempGroup.name = tempName;
					
					JPanel tempPanel = new JPanel();
					tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
					JCheckBox tempCheckbox = new JCheckBox();
					tempCheckbox.addActionListener(this);
					tempPanel.add(tempCheckbox);
					tempPanel.add(new JLabel(tempName));
					tempGroup.checkbox = tempCheckbox;
					
					presetLocationGroups.add(tempGroup);
					centerPanel.add(tempPanel);
				}
			}
		} else {
			// If it doesn't exist, display an error instead
			centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
			centerPanel.add(Box.createHorizontalGlue());
			centerPanel.add(new JLabel("Default locations data folder not found."));
			centerPanel.add(Box.createHorizontalGlue());
		}
		
		// Assemble main Default Locations Panel 

		this.add(northPanel, BorderLayout.NORTH);
		this.add(defaultScrollPane,BorderLayout.CENTER);
	}


	private void addBuiltInFiles(Vector<DrawingTool> locations) {

		for (int i = 0; i < locations.size(); i++) {
			DrawingTool tempLocation = locations.get(i);
			this.guiparent.addDrawingTool(tempLocation);
			ArrayList<DrawingTool> newObjects = new ArrayList<>();
			newObjects.add(tempLocation);
			this.drawingToolTable.addDrawingTool(newObjects);
		}
	}

	private void removeBuiltInFiles(Vector<DrawingTool> locations) {
		//System.out.println(defaultLocationsStartIndex);
		this.drawingToolTable.setRowSelectionInterval(defaultLocationsStartIndex,locations.size()-1);
		int[] selectedRows = this.drawingToolTable.getSelectedRows() ;
			
			int delete = drawingTooltablemodel.deleteObjects(
	                this.drawingToolTable,
	                selectedRows);
	        if (delete == JOptionPane.NO_OPTION ||
	                delete == JOptionPane.CLOSED_OPTION) {
	        }
	        else
	        {
	        	
	        	//remove actors
	            ArrayList<vtkTextActor3D> actors = DrawingTool.getMasterFaultBranchGroup();
	            ArrayList<vtkTextActor3D> removedActors = new ArrayList<>();
	            for(int i =0;i<selectedRows.length;i++)
	            {
	            	vtkTextActor3D actor = actors.get(selectedRows[i]-i);
	            	removedActors.add(actor);
	            	DrawingTool.getMasterFaultBranchGroup().remove(selectedRows[i]-i);
	            }
	            Info.getMainGUI().removeActors(removedActors);
	        }
	}
	
	private Vector<DrawingTool> loadBuiltInFiles() {
		BranchGroup mainBranchGroup = new BranchGroup();
		mainBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		mainBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		
		/**
		 * Only reads point data
		 */
		//if the dbf file exists, then read from the dbf file for the label names
		String selectedDbfFile = selectedInputFile.replace(".shp", ".dbf");
		File dbf = new File(selectedDbfFile);
		if(dbf.exists()) {
			Vector<DrawingTool> locations = new Vector<DrawingTool>();
			try {
				DBFReaderJGeom dbfFile = new DBFReaderJGeom(selectedDbfFile);
				//obtains the location of name columns in the file
				int nameColumn = 0;
				int fieldsCount = dbfFile.numFields();
				for(int i = 0; i < fieldsCount; i++) {
					String fieldName = dbfFile.getFieldName(i);
					if(fieldName.equalsIgnoreCase("Name_1") || fieldName.equalsIgnoreCase("NameLSAD")) 
						nameColumn = i;
				}
				
				ShapefileReaderJGeom shpFile = new ShapefileReaderJGeom(selectedInputFile);

				int pointCount = shpFile.numRecords();
				
				for(int index = 0; index < pointCount; index++) {		
					//reads the shape bytes in the shapefile at the given index
					byte[] geometryBytes = shpFile.getGeometryBytes(index);			

					//converts the bytes into a JGeometry type
					JGeometry point = ShapefileReaderJGeom.getGeometry(geometryBytes, index);

					//gets the coordinates of all the vertices of the shape
					double[] coordinates = point.getPoint();
					
					//gets the name of the point
					byte[] record = dbfFile.getRecord(index);
					String textStr = dbfFile.getFieldData(nameColumn, record);
										
					DrawingTool tempLocation = new DrawingTool(
							coordinates[1],
							coordinates[0],
							0.0d,
							textStr,
							displayAttributes);
					locations.addElement(tempLocation);
				}
				return locations;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Vector<DrawingTool> locations = new Vector<DrawingTool>();
			//int fileSeparatorIndex = selectedInputFile.lastIndexOf(File.separator);
			//String textStr = selectedInputFile.substring(fileSeparatorIndex + 1);
			try {
				ShapefileReaderJGeom shpFile = new ShapefileReaderJGeom(selectedInputFile);
				int pointCount = shpFile.numRecords();
				for(int index = 0; index < pointCount; index++) {			
					//reads the shape bytes in the shapefile at the given index
					byte[] geometryBytes = shpFile.getGeometryBytes(index);			

					//converts the bytes into a JGeometry type
					JGeometry point = ShapefileReaderJGeom.getGeometry(geometryBytes, index);

					//gets the coordinates of all the vertices of the shape
					double[] coordinates = point.getPoint();

					
					DrawingTool tempLocation = new DrawingTool(
							coordinates[1],
							coordinates[0],
							0.0d,
							""/*textStr*/,
							displayAttributes);
					locations.addElement(tempLocation);
				}
				return locations;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void clearCheckBoxes() {
		for (int i = 0; i < presetLocationGroups.size(); i++) {
			PresetLocationGroup tempGroup = presetLocationGroups.get(i);
			if (tempGroup != null && tempGroup.checkbox != null) {
				tempGroup.checkbox.setSelected(false);
			}
		}
		this.repaint();
	}
	
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
//		if(src == searchButton){
//			searchCities(searchCity.getText());
//		}
//		else {
			for (int i = 0; i < presetLocationGroups.size(); i++) {
				PresetLocationGroup tempGroup = presetLocationGroups.get(i);
				if (tempGroup != null && src == tempGroup.checkbox) {
					if (tempGroup.checkbox.isSelected()) {
						selectedInputFile = tempGroup.file.getAbsolutePath();
						tempGroup.locations = loadBuiltInFiles();
						addBuiltInFiles(tempGroup.locations);
					} else {
						removeBuiltInFiles(tempGroup.locations);
					}
				}
			}
//		}
	}
	
	public class PresetLocationGroup {
		public Vector<DrawingTool> locations = null;
		public String name			= null;
		public File file			= null;
		public JCheckBox checkbox	= null;
	}
}
