 package org.scec.vtk.drawingTools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;


import org.scec.vtk.main.Info;
import org.scec.vtk.tools.Transform;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.DBFReaderJGeom;
import oracle.spatial.util.ShapefileReaderJGeom;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyLine;



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
	
	ArrayList<PresetLocationGroup> presetLocationGroups = new ArrayList<PresetLocationGroup>();
	private String selectedInputFile;
	
	private DisplayAttributes displayAttributes;
	private JPanel centerPanel = new JPanel();
	JFrame frame = new JFrame();
	private JScrollPane defaultScrollPane = new JScrollPane(centerPanel);
	ArrayList<String> citypop = new ArrayList<String>();

	private DrawingToolsTable drawingToolTable;
	private DrawingToolsTable highwayToolTable;
	private DrawingToolsTableModel drawingTooltablemodel;
	private int defaultLocationsStartIndex = 0;
	private int popSize = 0;
	private ArrayList<String> ccount = new ArrayList<String>();
	private ArrayList<vtkActor> highwayActors = new ArrayList<vtkActor>();
	private vtkActor countyActor = new vtkActor();
	private Vector<DrawingTool> highwayList = new Vector<DrawingTool>();
	private Vector<DrawingTool> countyList = new Vector<DrawingTool>();
	private boolean countiesLoaded = false;
	
	public DefaultLocationsGUI(DrawingToolsGUI guiparent) {
		this.guiparent = guiparent;
		this.drawingToolTable = guiparent.getDrawingToolTable();
		this.highwayToolTable = guiparent.getHighwayToolTable();
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
				if (files[i].isFile() && files[i].getName().endsWith(".shp") || files[i].getName().endsWith(".txt") && !files[i].getName().equals("CA_Counties.txt") && !files[i].getName().contains("popdensity.txt") 
						&& !files[i].getName().contains("CA_Cities")) {
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
			this.guiparent.addDrawingTool(tempLocation);//.addDrawingTool(tempLocation);
			this.drawingToolTable.addDrawingTool(tempLocation);//newObjects);
		}
		Info.getMainGUI().updateRenderWindow();
	}

	private void removeBuiltInFiles(Vector<DrawingTool> locations) {
		if(locations!=null){
			this.guiparent.appendActors.removeFromAppendedPolyData(countyActor);
			for(int i =0;i<this.drawingToolTable.getRowCount();i++)
			{
				for(int j =0; j < locations.size(); j++)
				{
					//System.out.println(locations.elementAt(j).getTextString() + "," + this.drawingToolTable.getValueAt(i, 0));
					if(locations.elementAt(j).getTextString().equals(this.drawingToolTable.getValueAt(i, 0)))
					{
						System.out.println("Removing:" + locations.elementAt(j).getTextString());
						this.drawingToolTable.setRowSelectionInterval(i,i);
						guiparent.removeTextActors();
						defaultLocationsStartIndex=i;
						i--;
						break;
					}
				}
				
			}
			if(countiesLoaded)
			{
				this.guiparent.appendActors.addToAppendedPolyData(countyActor);
				System.out.println("Readding");
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
					if(fieldName.equalsIgnoreCase("Name_1") || fieldName.equalsIgnoreCase("NameLSAD") || fieldName.equalsIgnoreCase("FULLNAME")) 
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
					//double[] coordinates = point.getOrdinatesArray();
					
					//gets the name of the point
					byte[] record = dbfFile.getRecord(index);
					String textStr = dbfFile.getFieldData(nameColumn, record);
										
					DrawingTool tempLocation = new DrawingTool(
							coordinates[1],
							coordinates[0],
							0.0d,
							textStr,
							displayAttributes,
							Color.white,
							null,
							null);
					locations.addElement(tempLocation);
					//System.out.println(index);
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
							displayAttributes,
							Color.white,
							null,
							null);
					locations.addElement(tempLocation);
				}
				return locations;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	private vtkActor loadCounties()
	{
		String selectedFile = dataPath + "CA_Counties.txt";
		File highwaysFile = new File(selectedFile);
		ArrayList<vtkPoints> points = new ArrayList<vtkPoints>();
		String name = "";
		vtkCellArray cells = new vtkCellArray();
		int ptCount=0;
		
		try {
			BufferedReader inStream = new BufferedReader(new FileReader(highwaysFile));
			String line = inStream.readLine();
			StringTokenizer dataLine = new StringTokenizer(line);
			name = dataLine.nextToken(":");
			/*process first line */
			
			/*DrawingTool tempLocation = new DrawingTool(
					0,
					0,
					0.0d,
					name,
					displayAttributes);
			highwayList.add(tempLocation);*/
			/* finished with first line */
			line = inStream.readLine();
			vtkPoints linePts =new vtkPoints();
			while (line!=null){
				dataLine = new StringTokenizer(line);
				String coord = "";
				while(coord != null)
				{
					try{
						String latitude = "";
						String longitude = "";
						coord = dataLine.nextToken();
						coord = coord.substring(0,coord.length()-3);
						int i = coord.indexOf(',');
						longitude = coord.substring(0,i);
						latitude = coord.substring(i + 1, coord.length()-1);
						
							double [] p = Transform.transformLatLon(Double.parseDouble(latitude), Double.parseDouble(longitude));
							linePts.InsertNextPoint(p);

						
							
						
					}catch(Exception e)
					{
						break;
					}
					
				}
				if(linePts.GetNumberOfPoints()>0)
				{
					points.add(linePts);
					linePts = new vtkPoints();
				}
				
				line = inStream.readLine();
			}
			inStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 		
		vtkPoints glbPoints = new vtkPoints();
		for(int i = 0;i<points.size();i++)
		{
			vtkPolyLine plyLine = new vtkPolyLine();
			plyLine.GetPointIds().SetNumberOfIds(points.get(i).GetNumberOfPoints());
			for(int j = 0;j<points.get(i).GetNumberOfPoints();j++)
				{
					glbPoints.InsertNextPoint(points.get(i).GetPoint(j));
					plyLine.GetPointIds().SetId(j,ptCount);
					ptCount++;
				}
			cells.InsertNextCell(plyLine);
		}

		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(glbPoints);
		polyData.SetLines(cells);
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.SetInputData(polyData);
		vtkActor actor = new vtkActor();
		actor.SetMapper(mapper);
		
		return actor;
	}
	public void setSelectedInputFile(String filen)
	{
		selectedInputFile =filen;
	}
	public  void loadHighways()
	{
		presetLocationGroups.get(0).checkbox.setSelected(true);
		String selectedFile = selectedInputFile;
		File highwaysFile = new File(selectedFile);
		String temp[] = new String[2];
		String nameOfSegment="";
		vtkPoints linePts;
		vtkCellArray cells = new vtkCellArray();
		vtkPolyData polyData;
		vtkPolyDataMapper mapper;
		vtkActor actor = new vtkActor();
		double [] p = null;
		ArrayList<vtkPoints> segmentPoints = new ArrayList<vtkPoints>();
		int pointCount;
		
		try {
			BufferedReader inStream = new BufferedReader(new FileReader(highwaysFile));
			String line = inStream.readLine();
			StringTokenizer dataLine = new StringTokenizer(line);
			temp[0] = dataLine.nextToken();	
			temp[1] = dataLine.nextToken();
			nameOfSegment = temp[1];
			linePts = new vtkPoints();

			while (line!=null){
				dataLine = new StringTokenizer(line);  
				temp[0] = dataLine.nextToken();	temp[1] = dataLine.nextToken();
				if (!temp[0].equals("segment"))
				{
					p = Transform.transformLatLon(Double.parseDouble(temp[0]), Double.parseDouble(temp[1]));
					linePts.InsertNextPoint(p);
				}
				else if (temp[1].equals(nameOfSegment))
				{
					if (linePts.GetNumberOfPoints() > 0)
					{
						segmentPoints.add(linePts);
						linePts = new vtkPoints();
					}
				}
				else // new segment name
				{
					if (linePts.GetNumberOfPoints() > 0)
					{
						segmentPoints.add(linePts);
						linePts = new vtkPoints();
					}
					vtkPoints globalPoints = new vtkPoints();
					cells = new vtkCellArray();
					pointCount = 0;
					for (int i=0; i<segmentPoints.size(); i++)
					{
						vtkPolyLine interstateLine = new vtkPolyLine();
						interstateLine.GetPointIds().SetNumberOfIds(segmentPoints.get(i).GetNumberOfPoints());
						for (int j=0; j<segmentPoints.get(i).GetNumberOfPoints(); j++)
						{
							globalPoints.InsertNextPoint(segmentPoints.get(i).GetPoint(j));
							interstateLine.GetPointIds().SetId(j, pointCount++);							
						}
						cells.InsertNextCell(interstateLine);
					}
					
					polyData = new vtkPolyData();
					polyData.SetPoints(globalPoints);
					polyData.SetLines(cells);
					mapper = new vtkPolyDataMapper();
					mapper.SetInputData(polyData);
					actor = new vtkActor();
					actor.SetMapper(mapper);
					highwayActors.add(actor);
					segmentPoints = new ArrayList<vtkPoints>();
					this.guiparent.appendActors.addToAppendedPolyData(actor);
					
					defaultLocationsStartIndex = this.highwayToolTable.getRowCount();
					DrawingTool highway = new DrawingTool(
						p[0],
						p[1],
						0.0d,
						nameOfSegment,
						displayAttributes,
						Color.WHITE,
						actor,
						null
					);
					highway.setDisplayName(nameOfSegment);
					highway.setSourceFile(selectedFile);
					this.guiparent.addHighways(highway);
					this.highwayToolTable.addDrawingTool(highway);
					nameOfSegment = temp[1];
				}
				line = inStream.readLine();						
			}
			inStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 		
	}
	
	public void removeHighways()
	{	
		this.guiparent.removeHighways();
		highwayActors.clear();
		Info.getMainGUI().updateRenderWindow();
	}
	public void removeCounties()
	{
		Vector<DrawingTool> copy = new Vector<DrawingTool>();
		for(DrawingTool t: countyList)
		{
			copy.add(t);
		}
		countyList.clear();
		this.guiparent.appendActors.removeFromAppendedPolyData(countyActor);
		removeBuiltInFiles(copy);
		
		Info.getMainGUI().updateRenderWindow();
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
	public void showPopUp()
	{
		MyDialogBox mdb = new MyDialogBox();
		//mdb.toFront();
		final JDialog frame = new JDialog(this.frame, "City Filter", true);
		mdb.d = frame;
		frame.getContentPane().add(mdb);
		frame.pack();
		frame.setVisible(true);
		//frame.setSize(320, 100);
		//frame.setLocation(200, 200);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	popSize = -1;
		    }
		});
		mdb.d.setLocationRelativeTo(frame);
		
		
	}
	public void readPopInput(String input)
	{
		try{
			popSize = Integer.parseInt(input);
		}catch(NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(null,
				    "Please enter a number",
				    "Number Format Exception",
				    JOptionPane.ERROR_MESSAGE);
		}
	}
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		for (int i = 0; i < presetLocationGroups.size(); i++) {
			PresetLocationGroup tempGroup = presetLocationGroups.get(i);
			if (tempGroup != null && src == tempGroup.checkbox) {
				if (tempGroup.checkbox.isSelected()) {
					selectedInputFile = tempGroup.file.getAbsolutePath();
					if(tempGroup.name.equals("CA Cities"))
					{
						popSize = -1;
						ccount.clear();
						showPopUp();	
						if(popSize != -1)
						{
							tempGroup.locations = loadBuiltInFiles();
	            			String p = dataPath + "CA_Cities_population.txt";
	            			String c = dataPath + "CA_Cities_counties.txt";
	            			ArrayList<String> temp = filterCitiesByCounty(c,ccount);
	            			addFilteredFiles(tempGroup.locations,filterCitiesByPopulation(p,popSize,temp));
	            			citypop = filterCitiesByPopulation(p,popSize,temp);
						}
						else
						{
							tempGroup.checkbox.setSelected(false);
							popSize = -1;
						}
					}
					else if (tempGroup.name.equals("California Highways") || tempGroup.name.equals("California Interstates"))
					{
						loadHighways();
						//addBuiltInFiles(highwayList);
						Info.getMainGUI().updateRenderWindow();
					}
					else if(tempGroup.name.equals("CA Counties"))
					{
						countiesLoaded = true;
						int n = JOptionPane.showConfirmDialog(
							    frame,
							    "Load County Labels?",
							    "County Labels",
							    JOptionPane.YES_NO_OPTION);
						countyActor = loadCounties();
						if(n == JOptionPane.YES_OPTION)
						{
							countyList = loadBuiltInFiles();
							addBuiltInFiles(countyList);
						}
						this.guiparent.appendActors.addToAppendedPolyData(countyActor);
						Info.getMainGUI().updateRenderWindow();
					}
					else
					{
						tempGroup.locations = loadBuiltInFiles();
						//System.out.println("Size:" + tempGroup.locations.size());
						addBuiltInFiles(tempGroup.locations);
					}
					
				} else {
					if (tempGroup.name.equals("California Highways") || tempGroup.name.equals("California Interstates")) {
						removeHighways();
					}
					if(tempGroup.name.equals("CA Counties"))
					{
						countiesLoaded = false;
						removeCounties();
					}
					removeBuiltInFiles(tempGroup.locations);
				}
			}
		}
	}	
	private ArrayList<String> getCounties(String countyFile)
	{
		ArrayList<String> counties = new ArrayList<String>();
		ArrayList<String> buff = new ArrayList<String>();
		try {
			Charset charset = Charset.forName("Cp1252");
			buff = (ArrayList<String>)Files.readAllLines(Paths.get(countyFile), charset);
			for(String s: buff)
			{
				if(s.substring(0, 1).contains("-"))
				{
					counties.add(s.substring(1));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return counties;
	}
	public int getPopulation(String cityName)
	{
		int counties = -1;
		ArrayList<String> buff = new ArrayList<String>();
		try {
			Charset charset = Charset.forName("Cp1252");
			buff = (ArrayList<String>)Files.readAllLines(Paths.get(dataPath + "CA_Cities_population.txt"), charset);
			for(int i = 0; i < buff.size(); i+=2)
			{
			//	System.out.println(buff.get(i) + "," + cityName);
				if(buff.get(i).trim().equals(cityName))
				{
					return Integer.parseInt(buff.get(i+1));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return counties;
	}
	public String getCounty(String cityName)
	{
		ArrayList<String> buff = new ArrayList<String>();
		String county = "";
		try {
			Charset charset = Charset.forName("Cp1252");
			buff = (ArrayList<String>)Files.readAllLines(Paths.get(dataPath + "CA_Cities_counties.txt"), charset);
			for(String search : buff)
			{
				if(search.contains("-"))
					county = search.substring(1);
				else
				{
					if(search.trim().equals(cityName))
						break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return county;
	}
	public String getPopulationDensity(String cityName)
	{
		String county = getCounty(cityName);
		ArrayList<String> buff = new ArrayList<String>();
		try {
			Charset charset = Charset.forName("Cp1252");
			buff = (ArrayList<String>)Files.readAllLines(Paths.get(dataPath + "popdensity.txt"), charset);
			for(int i = 0; i < buff.size();i+=2)
			{
				if(buff.get(i).equals(county))
					return buff.get(i+1);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private ArrayList<String> filterCitiesByCounty(String countyFile, ArrayList<String> counties)
	{
		ArrayList<String> filteredCities = new ArrayList<String>();
		ArrayList<String> buff = new ArrayList<String>();
		try {
			Charset charset = Charset.forName("Cp1252");
			buff = (ArrayList<String>)Files.readAllLines(Paths.get(countyFile), charset);
			for(String s: counties)
			{
				String search = "-"+s;
				boolean append = false;
				for(String j: buff)
				{
					if(search.equals(j))
					{
						append = true;
					}
					if(j.contains("-") && !(search.equals(j)) && append)
					{
						break;
					}
					if(append && !(search.equals(j) && !j.contains("-")))
					{
						filteredCities.add(j);
					}
						
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filteredCities;
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
					this.guiparent.addDrawingTool(tempLocation);//.addDrawingTool(tempLocation);
					//ArrayList<DrawingTool> newObjects = new ArrayList<>();
					//newObjects.add(tempLocation);
					this.drawingToolTable.addDrawingTool(tempLocation);//newObjects);
				}
			}		
		}
		Info.getMainGUI().updateRenderWindow();
	}
	private ArrayList<String> filterCitiesByPopulation(String popFile, int desiredPop, ArrayList<String> cities)
	{
		ArrayList<String> filteredCities = new ArrayList<String>();
		ArrayList<String> buff = new ArrayList<String>();
		try {
			Charset charset = Charset.forName("Cp1252");
			buff = (ArrayList<String>)Files.readAllLines(Paths.get(popFile), charset);
			for(String s: cities)
			{
				for(int i = 0; i < buff.size(); i+=2)
				{
					if(Integer.parseInt(buff.get(i+1)) >= desiredPop && buff.get(i).trim().equals(s))
					{
						filteredCities.add(buff.get(i).trim());
					}
				}
			}
			
		} catch (IOException e) {
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
	class MyDialogBox extends JPanel {
		private DefaultListModel dlm;
		private JList jl;
		private JDialog d; //reference to parent dialog
		private ArrayList<String> counties = new ArrayList<String>();
		MyDialogBox() {
			counties = getCounties(dataPath + "CA_Cities_counties.txt");
			Collections.sort(counties);
			//super("");
			//setSize(320, 200);
			//setLocation(200, 200);
			JPanel jp = new JPanel();
			jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));
			
			// fist row
			JPanel aPanel = new JPanel();
			aPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			JLabel sizeLabel = new JLabel("Show all cities with pop. > than:");
			aPanel.add(sizeLabel);
			final JTextField sizeText= new JTextField("0", 12);
			aPanel.add(sizeText);
			
			// second row
			JPanel bPanel = new JPanel();
			bPanel.setLayout(new BoxLayout(bPanel, BoxLayout.PAGE_AXIS));
			JLabel filterLabel = new JLabel("Counties: Default = All Counties");
			bPanel.add(filterLabel);
			
			dlm = new DefaultListModel();
			for(String s: counties)
			{
				dlm.addElement(s);
			}
			jl = new JList(dlm);
			JScrollPane scroll = new JScrollPane(jl);
			scroll.setPreferredSize(new Dimension(250,250));
			bPanel.add(scroll);
			
			// third row
			JButton okButton = new  JButton("Ok");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae){
					try{
						readPopInput(sizeText.getText());
						ListSelectionModel lsm = jl.getSelectionModel();
						int minIndex = lsm.getMinSelectionIndex();
				        int maxIndex = lsm.getMaxSelectionIndex();
				        boolean atLeastOne = false;
				        for (int i = minIndex; i <= maxIndex; i++) {
				            if (lsm.isSelectedIndex(i)) {
				            	atLeastOne = true;
				            	ccount.add(counties.get(i));
				                //System.out.println(counties.get(i) + " selected");
				            }
				        }
				        if(!atLeastOne)
				        {
				        	for(String s: counties)
				        		ccount.add(s);
				        }
						d.dispose();
					}
					catch(NumberFormatException e){
						JOptionPane.showMessageDialog(null,
            				    "Please enter a number",
            				    "Number Format Exception",
            				    JOptionPane.ERROR_MESSAGE);
						sizeText.setText("");
					}
				}		
			});
			
			JButton cancelButton = new  JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent aa){
					popSize = -1;
					d.dispose();
				}
			});

			JPanel okPanel = new JPanel();
			okPanel.setLayout(new FlowLayout(FlowLayout.CENTER));		
			okPanel.add(okButton);
			okPanel.add(cancelButton);

			//add add add 
			jp.add(aPanel);
			jp.add(bPanel);
			jp.add(okPanel);
			add(jp);
			setVisible(true);
		}
	}
	public ArrayList<vtkActor> getHighwayActors()
	{
		return this.highwayActors;
	}
}
