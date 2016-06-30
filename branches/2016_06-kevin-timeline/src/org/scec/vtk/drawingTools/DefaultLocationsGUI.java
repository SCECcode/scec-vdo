 package org.scec.vtk.drawingTools;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.scec.vtk.main.Info;
import org.scec.vtk.tools.actors.AppendActors;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.DBFReaderJGeom;
import oracle.spatial.util.ShapefileReaderJGeom;
import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkObject;
import vtk.vtkProp;



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
	private int popSize = 0;
	
	
	public DefaultLocationsGUI(DrawingToolsGUI guiparent) {
		this.guiparent = guiparent;
		this.drawingToolTable = guiparent.getTable();
		this.drawingTooltablemodel = drawingToolTable.getLibraryModel();
		this.defaultLocationsStartIndex = this.drawingToolTable.getRowCount();
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
		defaultLocationsStartIndex = this.drawingToolTable.getRowCount();
		for (int i = 0; i < locations.size(); i++) {
			DrawingTool tempLocation = locations.get(i);
			//TODO: Some method that generates a list of acceptable cities to be drawn based on population
			this.guiparent.addDrawingTool(tempLocation);//.addDrawingTool(tempLocation);
			//ArrayList<DrawingTool> newObjects = new ArrayList<>();
			//newObjects.add(tempLocation);
			this.drawingToolTable.addDrawingTool(tempLocation);//newObjects);
		}
		Info.getMainGUI().updateRenderWindow();
	}

	private void removeBuiltInFiles(Vector<DrawingTool> locations) {
		//System.out.println(defaultLocationsStartIndex);
		if(locations!=null){
			for(int i =0;i<this.drawingToolTable.getRowCount();i++)
			{
				for(int j =0; j < locations.size(); j++)
				{
					//System.out.println(locations.elementAt(j).getTextString() + "," + this.drawingToolTable.getValueAt(i, 0));
					if(locations.elementAt(j).getTextString().equals(this.drawingToolTable.getValueAt(i, 0)))
					{
						//System.out.println("Removing");
						this.drawingToolTable.setRowSelectionInterval(i,i);
						guiparent.removeTextActors();
						defaultLocationsStartIndex=i;
						i--;
						break;
					}
				}
				
			}
			Info.getMainGUI().updateRenderWindow();
		
		}
	}
	
	private Vector<DrawingTool> loadBuiltInFiles() {
	
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
						if(tempGroup.name.equals("CA Cities"))
						{
							String s = "";
							int cancel = -1;
							while(s.equals(""))
							{
								s = (String)JOptionPane.showInputDialog(
	                                null,
	                                "Enter Population Size to filter by:\n"
	                                + "Default: 0",
	                                "Population Filter",JOptionPane.PLAIN_MESSAGE,null,null, "0");
							 	//Check if a string was returned
								
				            	if ((s != null) && (s.length() > 0)) {
				            		try{
				            			popSize = Integer.parseInt(s);
				            			//TODO: there should be a method to filter locations
				            		}catch(NumberFormatException ex)
				            		{
				            			JOptionPane.showMessageDialog(null,
				            				    "Please enter a number",
				            				    "Number Format Exception",
				            				    JOptionPane.ERROR_MESSAGE);
				            			s= "";
				            		}
				            		tempGroup.locations = loadBuiltInFiles();
			            			String p = dataPath + "CA_Cities_population.txt";
			            			//System.out.println(p);
									//addBuiltInFiles(tempGroup.locations);
			            			addFilteredFiles(tempGroup.locations,filterCitiesByPopulation(p,popSize));
				            	}
				            	else
				            	{
				            		tempGroup.checkbox.setSelected(false);
				            		break;
				            	}
							}
						}
						else
						{
							tempGroup.locations = loadBuiltInFiles();
							addBuiltInFiles(tempGroup.locations);
						}
				//		System.out.println(tempGroup.name );
					//	System.out.println(selectedInputFile );
						
					} else {
						removeBuiltInFiles(tempGroup.locations);
					}
				}
			}
	}
	private void addFilteredFiles(Vector<DrawingTool> locations,ArrayList<String> cities) {
		defaultLocationsStartIndex = this.drawingToolTable.getRowCount();
		for (int i = 0; i < locations.size(); i++) {
			for(int j = 0; j < cities.size();j++)
			{
				//System.out.println(locations.get(i).getTextString() + "," + cities.get(j));
				if(locations.get(i).getTextString().equals(cities.get(j)))
				{
					//System.out.println(cities.get(j) + " added");
					DrawingTool tempLocation = locations.get(i);
					//TODO: Some method that generates a list of acceptable cities to be drawn based on population
					this.guiparent.addDrawingTool(tempLocation);//.addDrawingTool(tempLocation);
					//ArrayList<DrawingTool> newObjects = new ArrayList<>();
					//newObjects.add(tempLocation);
					this.drawingToolTable.addDrawingTool(tempLocation);//newObjects);
				}
			}		
		}
		Info.getMainGUI().updateRenderWindow();
	}
	private ArrayList<String> filterCitiesByPopulation(String popFile, int desiredPop)
	{
		ArrayList<String> filteredCities = new ArrayList<String>();
		ArrayList<String> buff = new ArrayList<String>();
		try {
			Charset charset = Charset.forName("Cp1252");
			buff = (ArrayList<String>)Files.readAllLines(Paths.get(popFile), charset);
			for(int i = 0; i < buff.size(); i+=2)
			{
				if(Integer.parseInt(buff.get(i+1)) >= desiredPop)
				{
					filteredCities.add(buff.get(i).trim());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return filteredCities;
	}
	public class PresetLocationGroup {
		public Vector<DrawingTool> locations = null;
		public String name			= null;
		public File file			= null;
		public JCheckBox checkbox	= null;
	}
}
