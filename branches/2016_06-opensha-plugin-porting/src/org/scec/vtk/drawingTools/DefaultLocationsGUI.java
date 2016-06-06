 package org.scec.vtk.drawingTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.scec.vtk.main.Info;
import org.scec.vtk.tools.Prefs;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.DBFReaderJGeom;
import oracle.spatial.util.ShapefileReaderJGeom;



/**
 *	author: Kristy Akullian
 *	author: Jessica McMorris
 *	author: Becky Gallagher
 *	author: Tom Robinson
 */

public class DefaultLocationsGUI extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final String dataPath = Info.getMainGUI().getRootPluginDir()+File.separator+"GISLocationPlugin"+File.separator;

	private DrawingToolsPlugin parent;
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
	
	public DefaultLocationsGUI(DrawingToolsPlugin parent, DrawingToolsGUI guiparent) {
		this.parent = parent;
		this.guiparent = guiparent;
		
		//this.displayAttributes = parent.getDisplayAttributes();

		// Set main panel layout manager and dimensions
		
		this.setLayout(new BorderLayout(5,5));
		this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		this.setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()));
		
		// Search Panel
//
//		JPanel searchPanel = new JPanel();
//		searchPanel.setLayout(new BorderLayout());

//		searchPanel.add(new JLabel("Search for a City:"), BorderLayout.NORTH);
//		searchPanel.add(searchCity, BorderLayout.CENTER);
//		searchButton.addActionListener(this);
//		searchPanel.add(searchButton, BorderLayout.SOUTH);

		// North Panel
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));
//		northPanel.add(searchPanel);
		
		// Center Panel
		centerPanel.setLayout(new GridLayout(0,2));
		//centerPanel.setPreferredSize(new Dimension(300,300));

		
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

//	private void searchCities(String citySearchString) {
//		String latString = new String();
//		String lonString = new String();
//		ArrayList<String> queryStringArrayList = new ArrayList<String>();
//		
//		StringTokenizer tokenizedQueryString = new StringTokenizer(citySearchString);
//		while(tokenizedQueryString.hasMoreTokens())
//			queryStringArrayList.add(tokenizedQueryString.nextToken());
//
//		try {
//			BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream("Master.txt")));
//			while(input.ready()){
//				String line = input.readLine();
//				StringTokenizer tk = new StringTokenizer(line);
//				latString = tk.nextToken();
//				lonString = tk.nextToken();
//				
//				ArrayList<String> fileStringArrayList = new ArrayList<String>();
//				while(tk.hasMoreTokens()){
//					fileStringArrayList.add(tk.nextToken());
//				}
//
//				if (compareStringArrayList(queryStringArrayList, fileStringArrayList)) {
//					String cityString = "";
//					for (int i = 0; i < fileStringArrayList.size(); i++) {
//						// Get and format the next word
//						String temp = (String)fileStringArrayList.get(i);
//						temp = temp.toLowerCase();
//						temp = temp.substring(0, 1).toUpperCase() + temp.substring(1, temp.length());
//						if (i > 0) cityString += " "; // Add a space between each word
//						cityString += temp;
//					}
//					
//					parent.displayNewLabel(latString,
//							lonString,
//							cityString,
//							displayAttributes);
//				}
//			}
//		} catch (Exception exp) {
//			System.out.println("Bad Input File!");
//		}
//	}
	
	private void addBuiltInFiles(Vector<DrawingTool> locations) {
		//BranchGroup mainBranchGroup = new BranchGroup();
		//mainBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		//mainBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		
		for (int i = 0; i < locations.size(); i++) {
			DrawingTool tempLocation = locations.get(i);
			
			//BranchGroup tempBranchGroup = tempLocation.createBranchGroup();
			//tempLocation.setDisplayed(true);
			//mainBranchGroup.addChild(tempBranchGroup);
			
			//parent.locationBranchesArray.add(tempBranchGroup);
			//parent.locationsArray.add(tempLocation);
			
			//Object[] tempRow = tempLocation.createRow();
			//guiparent.labelModel.addRow(tempRow);
		}
		//parent.locationPluginBranchGroup.addChild(mainBranchGroup);
	}
	
	private void removeBuiltInFiles(Vector<DrawingTool> locations) {
		/*for (int i = 0; i < locations.size(); i++) {
			int index = parent.locationsArray.indexOf((Location)locations.elementAt(i));
			if (index > -1) {
				((Location)parent.locationsArray.elementAt(index)).detatch();
				guiparent.labelModel.removeRow(index);
				parent.locationBranchesArray.remove(index);
				parent.locationsArray.remove(index);
			}
		}*/
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
										
					/*DrawingTool tempLocation = new DrawingTool(
							coordinates[1],
							coordinates[0],
							0.0d,
							textStr,
							displayAttributes);*/
					//locations.addElement(tempLocation);
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
