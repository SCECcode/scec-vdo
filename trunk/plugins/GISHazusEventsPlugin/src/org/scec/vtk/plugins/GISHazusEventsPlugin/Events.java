package org.scec.vtk.plugins.GISHazusEventsPlugin;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.scec.vtk.main.Info;
import org.scec.vtk.tools.actors.AppendActors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.DBFReaderJGeom;
import oracle.spatial.util.ShapefileReaderJGeom;


/**
* Created July 21,2011
* Builds and handles events for GIS NorhtRidge SHP files.
* Adapted from PolBoundGUI
*
* @author Miguel Villasana
*/


public class Events {    
	private FilledBoundaryCluster currentBoundary;
	private static final int NUM_POP_CATEGORY = 12;
	boolean bIsImport = false;
	boolean bIsImport1 = false;
	boolean bIsImport2 = false;
	boolean bIsImport3 = false;
	
    JFileChooser OpenShapeFile = new JFileChooser();
    ArrayList<Float> populationCategory;
    File ralph;
    EventAttributes event;
	private ArrayList<Float> legendMaxList;
	private ArrayList<EventAttributes> eventList;
    private int numLines = 0;
    private int numFiles = 0;
    String sImportedFilePath, sImportedFilePath1,  sImportedFilePath2, sImportedFilePath3, sImportedFilePath4;
    public static float maxPop = 0;
//    Parser Tparser = new Parser();

		private int groupCount = 0;
		private int numBounds = 0;
		public String[] names;
		public int[] groupSize;
		
		private Color[] purpleGradient = new Color[NUM_POP_CATEGORY];
		
		private ArrayList<FilledBoundaryCluster> allBounds = new ArrayList<FilledBoundaryCluster>();
		private AppendActors segmentActors;
		private NodeList nodeList;
	public Events() {
		populationCategory = new ArrayList<Float>();
		purpleGradient[0] = new Color(0.219f, 0.659f, 0.000f);
		purpleGradient[0] = new Color(0.455f, 0.780f, 0.000f);
		purpleGradient[1] = new Color(0.588f, 0.839f, 0.000f);
		purpleGradient[2] = new Color(0.749f, 0.902f, 0.000f);
		purpleGradient[3] = new Color(0.914f, 0.961f, 0.000f);
		purpleGradient[4] = new Color(1.000f, 0.918f, 0.000f);
		purpleGradient[5] = new Color(1.000f, 0.733f, 0.000f);
		purpleGradient[6] = new Color(1.000f, 0.549f, 0.000f);
		purpleGradient[7] = new Color(1.000f, 0.369f, 0.000f);
		purpleGradient[8] = new Color(1.000f, 0.184f, 0.000f);
		purpleGradient[9] = new Color(1.000f, 0.000f, 0.000f);
		
		legendMaxList = new ArrayList<Float>();
		for (int i = 0; i < 10; i++){
			legendMaxList.add(-1.0f);
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document document;
		
		try {
			builder = factory.newDocumentBuilder();
			document = builder.parse(new File(Info.getMainGUI().getRootPluginDir()+File.separator+"GISHazusEventsPlugin"
					+File.separator+"QuakeEvents.xml"));
			eventList = new ArrayList<EventAttributes>();
			nodeList = document.getDocumentElement().getChildNodes();
			
			for (int i = 0; i < nodeList.getLength(); i++){
				Node node = nodeList.item(i);
				
				if (node instanceof Element){
					event = new EventAttributes();
					event.setID(node.getAttributes().getNamedItem("id").getNodeValue());
					
					NodeList childNodes = node.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++){
						Node childNode = childNodes.item(j);
						
						if (childNode instanceof Element){
							String content = childNode.getLastChild().getTextContent().trim();
							if (childNode.getNodeName() == "event_name"){
								event.setEventName(content);
							}
							else if (childNode.getNodeName() == "shape_file"){
								event.setSHPFile(content);
							}
							else if (childNode.getNodeName() ==	"data_file"){
								event.setDBFFile(content);
							}
							else if (childNode.getNodeName() == "column"){
								event.setColumn(content);
							}
							else if (childNode.getNodeName() == "like_earthquake"){
								event.setLikeEarthquake(content);
							}
							else if (childNode.getNodeName() == "time_or_type"){
								event.setLegendTitle(content);
							}
						}
					}
					eventList.add(event);
					numLines++;
					numFiles++;
				}
			}
			
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<FilledBoundary> getAllBoundaries(){
		ArrayList<FilledBoundary> allBoundaries = new ArrayList<FilledBoundary>();
		for(FilledBoundaryCluster c: allBounds){
			ArrayList<FilledBoundary> temp = c.getBoundaries();
			for(FilledBoundary b: temp){
				allBoundaries.add(b);
			}
			
		}
		
		return allBoundaries;
	}
	
	public String getName(int row)
	{
		String name = "";
		name = eventList.get(row).getEventName();
		return name;
	}

	public ArrayList<String> buildBoundaryNames(){
		File filename;
		if (bIsImport)
			filename = new File(sImportedFilePath);
		else if(bIsImport1)
			filename = new File(sImportedFilePath);
		else if(bIsImport2)
			filename = new File(sImportedFilePath);
		else if(bIsImport3)
			filename = new File(sImportedFilePath);
		else
		 filename = new File(Info.getMainGUI().getRootPluginDir()+File.separator+"GISHazusEventsPlugin"+File.separator+"sourcefiles" + File.separator);
		ArrayList<String> files = new ArrayList<String>();
		int i = 0;
		//Find the number of segments in all the files
		if (filename.exists() && filename.isDirectory()) {
			File filelist[] = filename.listFiles();
			for (i = 0; i < filelist.length; i++) {
				if (filelist[i].isFile()) {
					String line = filelist[i].getName();
					if(line.contains("/")){
						line = line.substring(line.lastIndexOf('/'));
					}
					if(!line.substring(0,3).equals("CVS")){
						line = "sourcefiles/"+line;
						files.add(line);
					}
				}
			}
		}
		
		names = new String[files.size()];
		groupSize = new int[files.size()];
		

		//Build the boundaries
		for(i = 0; i < files.size(); i++)
		{
			getBoundaryName(files.get(i));
			groupSize[groupCount] = numBounds;
			groupCount++;
			numBounds = 0;
			currentBoundary = new FilledBoundaryCluster(segmentActors);
		}
		ArrayList<String> quakeList = new ArrayList<String>();
		for(i = 0; i < eventList.size(); i++){
			quakeList.add(eventList.get(i).getEventName());
		}
	
		return quakeList;
	}
	
	private void getBoundaryName(String file){

		File filename;
		if(bIsImport)
			filename = new File(sImportedFilePath);
		else if(bIsImport1)
			filename = new File(sImportedFilePath);
		else if(bIsImport2)
			filename = new File(sImportedFilePath);
		else if(bIsImport3)
			filename = new File(sImportedFilePath);
		else
		 filename = new File(Info.getMainGUI().getRootPluginDir() + File.separator + "GISHazusEventsPlugin"+ File.separator + file);
		String temp[] = new String[2]; 
        
		try {
			BufferedReader inStream =
				new BufferedReader(new FileReader(filename));
			String line = inStream.readLine();
			names[groupCount] = line;
			line = inStream.readLine();
			StringTokenizer dataLine = new StringTokenizer(line);
			temp[0] = dataLine.nextToken();	
			temp[1] = dataLine.nextToken();

		}		
		catch (Exception e) {
			System.out.println(e.getMessage());//prints "null" to console
		}
	}
	/**
	 * Reads the source file and gathers in coordinates for each segment. 
	 * Coords are sent to addHighwaySegment()
	 * 
	 * @return an ArrayList of all Highway objects
	 */
	public ArrayList<FilledBoundaryCluster> buildAllBoundaries(){
		File fileURL;
		if(bIsImport)
			fileURL = new File(sImportedFilePath);
		else if(bIsImport1)
			fileURL = new File(sImportedFilePath);
		else if(bIsImport2)
			fileURL = new File(sImportedFilePath);
		else if(bIsImport3)
			fileURL = new File(sImportedFilePath);
		else
		 fileURL = new File(Info.getMainGUI().getRootPluginDir() + File.separator+ "GISHazusEventsPlugin"+File.separator+"sourcefiles");
		ArrayList<String> files = new ArrayList<String>();
		int i = 0;
		//Find the number of segments in all the files
		try{
			BufferedReader inStream =
				new BufferedReader(new FileReader(fileURL));
			String line = inStream.readLine();
			while(line != null)
			{
				line = "sourcefiles/"+line;
				files.add(line);
				i++;
				line = inStream.readLine();
			}
			currentBoundary = new FilledBoundaryCluster(segmentActors);
		}catch(Exception e){}
		
		names = new String[files.size()];
		groupSize = new int[files.size()];
		//Build the boundaries find me
		for(i = 0; i < files.size(); i++)
		{
			buildBoundaries(files.get(i));
			groupSize[groupCount] = numBounds;
			groupCount++;
			numBounds = 0;
			currentBoundary = new FilledBoundaryCluster(segmentActors);
		}
		
		return allBounds;
	}


	public ArrayList<FilledBoundaryCluster> buildSelectedBoundary(int row)
	{
		ralph = null;
		bIsImport = false;
		bIsImport1 = false;
		bIsImport2 = false;
		bIsImport3 = false;
		int i = 0;
		while (i < numLines){
			
			if (i == row){
				if (i == 0){
					/*do
					{
						int returnValue = OpenShapeFile.showOpenDialog(null);
						if (returnValue==JFileChooser.APPROVE_OPTION) {
							ralph = OpenShapeFile.getSelectedFile();
							sImportedFilePath1 = ralph.toString();

						}
						sImportedFilePath = sImportedFilePath1;
					}while(ralph.equals(null));


					bIsImport = true;
					buildBoundaries(sImportedFilePath);*/
					importFiles();
					buildBoundaries(eventList.get(eventList.size()-1).getSHPFile());
				}
				else{
					buildBoundaries(eventList.get(i).getSHPFile());
				}
			}
			
			i++;
		}

		currentBoundary = new FilledBoundaryCluster(segmentActors);
		return allBounds;
	}
	private void importFiles()
	{
		//TODO:
		event = new EventAttributes();
		event.setID("" + (nodeList.getLength() + 1));
		String s = (String)JOptionPane.showInputDialog(
                Info.getMainGUI(),
                "Enter name of event:",
                JOptionPane.QUESTION_MESSAGE);
		event.setEventName(s);
		int n = JOptionPane.showConfirmDialog(
			    Info.getMainGUI(),
			    "Please enter location of the .shp file you wish to import:",
			    "Import SHF File",
			    JOptionPane.YES_NO_OPTION);
		if(n == JOptionPane.YES_OPTION)
		{
			int returnValue = OpenShapeFile.showOpenDialog(null);
			if (returnValue==JFileChooser.APPROVE_OPTION) {
				String path = OpenShapeFile.getSelectedFile().getPath();
				event.setSHPFile(path);
				sImportedFilePath = path;
				int x = JOptionPane.showConfirmDialog(
					    Info.getMainGUI(),
					    "Please enter location of the .dbf file you wish to import:",
					    "Import DBF File",
					    JOptionPane.YES_NO_OPTION);
				if(x == JOptionPane.YES_OPTION){
					returnValue = OpenShapeFile.showOpenDialog(null);
					if (returnValue==JFileChooser.APPROVE_OPTION) {
						event.setDBFFile(OpenShapeFile.getSelectedFile().getPath());
					}
					//TODO: Show the user what their .dbf file contains
					String z = (String)JOptionPane.showInputDialog(
			                Info.getMainGUI(),
			                "Enter name of column:",
			                JOptionPane.QUESTION_MESSAGE);
					event.setColumn(z);
					event.setLikeEarthquake("Northridge"); //TODO: Allow users to switch this
					String timeOrType = (String)JOptionPane.showInputDialog(
			                Info.getMainGUI(),
			                "Enter legend time or type:",
			                JOptionPane.QUESTION_MESSAGE);
					event.setLegendTitle(timeOrType);
					eventList.add(event);
					numLines++;
					numFiles++;
					bIsImport2 = true;
					
				}
			}
		}
		
	}
	private void buildBoundaries(String file) {
		allBounds = null;
		allBounds = new ArrayList<FilledBoundaryCluster>();

	//---------------------------------------------shapefile-----------------------------------------		
		ShapefileReaderJGeom shpFile;
	
		
		try {
			//setting up to read from dbf file
			int fileEnum = 0;
		String file2 = file.replace("shp", "dbf");
			String dbfFilename;
			if(bIsImport)
				dbfFilename = new String(file2);
			else if(bIsImport1)
				dbfFilename = new String(file2);
			else if(bIsImport2)
				dbfFilename = new String(file2);
			else if(bIsImport3)
				dbfFilename = new String(file2);
			else
			 dbfFilename = new String(Info.getMainGUI().getRootPluginDir() + File.separator + "GISHazusEventsPlugin" + File.separator +file2);
			DBFReaderJGeom dbfFile = new DBFReaderJGeom(dbfFilename);
			int columnIndex = 0;
			for (fileEnum = 0; fileEnum < numFiles; fileEnum++){
				System.out.println(file2 + "," + eventList.get(fileEnum).getDBFFile());
				if(file2.equals(eventList.get(fileEnum).getDBFFile())){
					
					columnIndex = fileEnum;
					System.out.println("Column Index: " + columnIndex);
					break;
				}
			}
			fileEnum += 1;
			
			//obtains the location of population and name columns in the file
			int nameColumn = 0;
			int popColumn = 1;
			int fieldsCount = dbfFile.numFields();
			String fieldName = "poop";
			
			for (int i = 0; i < fieldsCount; i ++){
				fieldName = dbfFile.getFieldName(i);
				System.out.println(":"+fieldName);
				System.out.println(eventList.get(columnIndex).getColumn());
				
				if (fieldName.equalsIgnoreCase("tract"))
					nameColumn = i;
			//	popColumn = 15;
					if(eventList.get(columnIndex).getColumn().equals(fieldName))//columnList.get(columnIndex).equals(fieldName))
						popColumn = i;
			}
			
			//obtains the population records in the order that the dbf file has them
			int[] populationAscendingIndices = new int[dbfFile.numRecords()];
			for(int a = 0; a < populationAscendingIndices.length; a++)
				populationAscendingIndices[a] = a;

			DecimalFormat twoDec = new DecimalFormat("#.##");
			float[] populationRecord = new float[dbfFile.numRecords()];
			for(int b = 0; b < populationRecord.length; b++) {
				byte[] record = dbfFile.getRecord(b);
				String temp = dbfFile.getFieldData(popColumn,record).replace("$", "");
				String floatToParse = temp.replace(",", "");
				float populationDensity = Float.parseFloat(floatToParse);
				populationDensity = Float.valueOf(twoDec.format(populationDensity));
				populationRecord[b] = populationDensity;
			}
			
			//orders the population density records in descending order
			sortDescending(populationAscendingIndices, populationRecord);
			
			for (int i = 0; i < populationRecord.length; i++){
				if (populationCategory.contains(populationRecord[i])){}
				else{
					populationCategory.add(populationRecord[i]);
				}
			}
		    System.out.println(sImportedFilePath);
			//-----------------shape file stuff--------------------
		    if(bIsImport)  
				shpFile = new ShapefileReaderJGeom(sImportedFilePath);
			else if(bIsImport1)
				shpFile = new ShapefileReaderJGeom(sImportedFilePath);
			else if(bIsImport2)
				shpFile = new ShapefileReaderJGeom(sImportedFilePath);
			else if(bIsImport3)
				shpFile = new ShapefileReaderJGeom(sImportedFilePath);
			else
				shpFile = new ShapefileReaderJGeom(Info.getMainGUI().getRootPluginDir() + File.separator + "GISHazusEventsPlugin" + File.separator +file);
			ArrayList<Double> latitude= new ArrayList<Double>(), longitude= new ArrayList<Double>();
			//amount of shapes in the shp file
			int shapeCount = shpFile.numRecords();
			numBounds = shapeCount;
			for(int i = 0; i < shapeCount; i++) {				
				int index = populationAscendingIndices[i];
				
				//reads the shape bytes in the shapefile at the given index
				byte[] geometryBytes = shpFile.getGeometryBytes(index);			

				//converts the bytes into a JGeometry type
				JGeometry shape = ShapefileReaderJGeom.getGeometry(geometryBytes, index);

				//gets the coordinates of all the vertices of the shape
				double[] coordinates = shape.getOrdinatesArray();

				//gets the amount of vertices of the shape
				int verticeCount = shape.getNumPoints();

				//gets the first coordinates of the shape 
				//so that multiple polygons in one "shape" are not connected
				double[] initialCoord = new double[2];
				initialCoord[0] = coordinates[0];
				initialCoord[1] = coordinates[1];
				int initialCoordIndex = 0;
				
				//loops through all the coordinates so that they can all get connected to each other
				for(int coordIndex = 0; coordIndex < coordinates.length; coordIndex = coordIndex + 2) {
					longitude.add(coordinates[coordIndex]);
					latitude.add(coordinates[coordIndex + 1]);
					if(initialCoord[0] == coordinates[coordIndex] && initialCoord[1] == coordinates[coordIndex + 1] && coordIndex != initialCoordIndex) {
						addBoundarySegment(verticeCount, latitude, longitude);
						longitude.clear();
						latitude.clear();
						if(coordIndex != coordinates.length - 2) {
							initialCoord[0] = coordinates[coordIndex + 2];
							initialCoord[1] = coordinates[coordIndex + 3];
							initialCoordIndex = coordIndex + 2;		
						}
					}
				}
				addBoundarySegment(verticeCount, latitude, longitude);
				longitude.clear();
				latitude.clear();
							
				byte[] record = dbfFile.getRecord(index);
				currentBoundary.setName(dbfFile.getFieldData(nameColumn, record) + " ----- " + populationRecord[i] );
				float denominatorConstant = 0.0f;
				
				denominatorConstant = 2000.0f;
				
				for (int j = 0; j < populationCategory.size(); j++) {
					//System.out.println("poop");
					if (populationRecord[i] >= populationCategory.get(j)){
						currentBoundary.setCategory(i);
						
						if (eventList.get(columnIndex).getLegendTitle().equals("Direct Building Economic Loss")||eventList.get(columnIndex).getLegendTitle().equals("Building Count")){
							if (legendMaxList.get(0) == -1 || (legendMaxList.get(0) > 0.0 && populationRecord[i] < legendMaxList.get(0) && populationRecord[i] > 0.0)){
								legendMaxList.set(0, populationRecord[i]);
							}
							
							if ((populationRecord[i]/denominatorConstant) == 0.0){
								currentBoundary.setColor(purpleGradient[0]);
								if (legendMaxList.get(0) < populationRecord[i]){
									legendMaxList.set(0, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 360.00 && populationRecord[i] < 286780.00){
								currentBoundary.setColor(purpleGradient[1]);
								if (legendMaxList.get(1) < populationRecord[i]){
									legendMaxList.set(1, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 286780.00 && populationRecord[i] < 1015290.00){
								currentBoundary.setColor(purpleGradient[2]);
								if (legendMaxList.get(2) < populationRecord[i]){
									legendMaxList.set(2, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 1015290.00 && populationRecord[i] < 2659870.00){
								currentBoundary.setColor(purpleGradient[3]);
								if (legendMaxList.get(3) < populationRecord[i]){
									legendMaxList.set(3, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 2659870.00 && populationRecord[i] < 6415170.00){
								currentBoundary.setColor(purpleGradient[4]);
								if (legendMaxList.get(4) < populationRecord[i]){
									legendMaxList.set(4, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 6415170.00 && populationRecord[i] < 13014440.00){
								currentBoundary.setColor(purpleGradient[5]);
								if (legendMaxList.get(5) < populationRecord[i]){
									legendMaxList.set(5, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 13014440.00 && populationRecord[i] < 24217800.00){
								currentBoundary.setColor(purpleGradient[6]);
								if (legendMaxList.get(6) < populationRecord[i]){
									legendMaxList.set(6, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 24217800.00 && populationRecord[i] < 45846980.00){
								currentBoundary.setColor(purpleGradient[7]);
								if (legendMaxList.get(7) < populationRecord[i]){
									legendMaxList.set(7, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 45846980.00 && populationRecord[i] < 104812440.00){
								currentBoundary.setColor(purpleGradient[8]);
								if (legendMaxList.get(8) < populationRecord[i]){
									legendMaxList.set(8, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 104812440.00 && populationRecord[i] <= 3100000000.00){
								currentBoundary.setColor(purpleGradient[9]);
								if (legendMaxList.get(9) < populationRecord[i]){
									legendMaxList.set(9, populationRecord[i]);
								}
								break;
							}
						}
						else{
							if (populationRecord[i] < 1.0){
								currentBoundary.setColor(purpleGradient[0]);
								if (legendMaxList.get(0) < populationRecord[i]){
									legendMaxList.set(0, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 1.0 && populationRecord[i] < 1.652){
								currentBoundary.setColor(purpleGradient[1]);
								if (legendMaxList.get(1) < populationRecord[i]){
									legendMaxList.set(1, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 1.652 && populationRecord[i] < 2.676){
								currentBoundary.setColor(purpleGradient[2]);
								if (legendMaxList.get(2) < populationRecord[i]){
									legendMaxList.set(2, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 2.676 && populationRecord[i] < 4.305){
								currentBoundary.setColor(purpleGradient[3]);
								if (legendMaxList.get(3) < populationRecord[i]){
									legendMaxList.set(3, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 4.305 && populationRecord[i] < 6.886){
								currentBoundary.setColor(purpleGradient[4]);
								if (legendMaxList.get(4) < populationRecord[i]){
									legendMaxList.set(4, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 6.886 && populationRecord[i] < 11.258){
								currentBoundary.setColor(purpleGradient[5]);
								if (legendMaxList.get(5) < populationRecord[i]){
									legendMaxList.set(5, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 11.258 && populationRecord[i] < 19.363){
								currentBoundary.setColor(purpleGradient[6]);
								if (legendMaxList.get(6) < populationRecord[i]){
									legendMaxList.set(6, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 19.363 && populationRecord[i] < 36.134){
								currentBoundary.setColor(purpleGradient[7]);
								if (legendMaxList.get(7) < populationRecord[i]){
									legendMaxList.set(7, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 36.134 && populationRecord[i] < 82.693){
								currentBoundary.setColor(purpleGradient[8]);
								if (legendMaxList.get(8) < populationRecord[i]){
									legendMaxList.set(8, populationRecord[i]);
								}
								break;
							}
							else if (populationRecord[i] >= 82.693 && populationRecord[i] <= 5000.0){
								currentBoundary.setColor(purpleGradient[9]);
								if (legendMaxList.get(9) < populationRecord[i]){
									legendMaxList.set(9, populationRecord[i]);
								}
								break;
							}
						}
					}
				}
				
				if (allBounds.contains(currentBoundary)){}
				else{
				allBounds.add(currentBoundary);
				}
				currentBoundary = new FilledBoundaryCluster(segmentActors);	
				
			}
		}
		catch (IOException e) {
			System.out.println("Failed to read shapefile.");
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}	
	
	public Color[] setColorGradient(Color c1, Color c2) {
		Color[] gradient = new Color[NUM_POP_CATEGORY];
		for (int i = 0; i < NUM_POP_CATEGORY; i++) {
	          float ratio = (float)i / (float)NUM_POP_CATEGORY;
	          int red = (int)(c2.getRed() * ratio + c1.getRed() * (1 - ratio));
	          int green = (int)(c2.getGreen() * ratio +
	                            c1.getGreen() * (1 - ratio));
	          int blue = (int)(c2.getBlue() * ratio +
	                           c1.getBlue() * (1 - ratio));
	          Color c = new Color(red, green, blue);
	          Color cf = c;
	          gradient[i] = cf;
	          purpleGradient[i] = cf;
		}
		return gradient;
	}
	
	public void sortDescending(int[] indices, float[] array) {
		for(int i = 0; i < array.length; i++) {
			for(int j = i; j < array.length; j++) {
				if(array[i] < array[j]) {
					float temp = array[i];
					array[i] = array[j];
					array[j] = temp;
					
					int temp2 = indices[i];
					indices[i] = indices[j];
					indices[j] = temp2;
				}
			}			
		}
	}

	public static void sortAscending(float[] array) {
		for(int i = 0; i < array.length; i++) {
			for(int j = i; j < array.length; j++) {
				if(array[i] > array[j]) {
					float temp = array[i];
					array[i] = array[j];
					array[j] = temp;
				}
			}			
		}
	}

	/**Adapted from display method in caTrace.java
	 * Takes in Vectors for latitude and longitude points
	 * Converts each segment's set of points into the requisite 3D objects.
	 * 
	 * @param vertices number of x,y points
	 * @param x set of all x points Latitude
	 * @param y set of all y points Longitude
	 */
	private void addBoundarySegment(int vertices, ArrayList<Double> x, ArrayList<Double> y) {
		
		if (x.size() > 0 && y.size() > 0)
		{
			float Latitude[]= new float[x.size()];
			float Longitude[]= new float[y.size()];
			for(int i=0; i<x.size();i++)
			{
				Latitude[i]= x.get(i).floatValue();
				Longitude[i]= y.get(i).floatValue();
			}
			currentBoundary.addSegment(Latitude,Longitude);
		
		}
	}
	
	public ArrayList<Float> getLegendMax(){
		return legendMaxList;
	}
	
	public void resetLegendMax(){
		for (int i = 0; i < 10; i++){
			legendMaxList.set(i, -1.0f);
		}
	}
	
	public String getColumnAt(int index){
		return eventList.get(index).getColumn();
	}
	
	public Color[] getPurpleGradient(){
		return purpleGradient;
	}
	
	public String getEventName(int index){
		return eventList.get(index).getEventName();
	}
	
	public String getLikeEarthquake(int index){
		return eventList.get(index).getLikeEarthquake();
	}
	
	public String getLegendTitle(int index){
		return eventList.get(index).getLegendTitle();
	}
	
	public ArrayList<String> makeLikeList(String likeEarthquakeName){
		ArrayList<String> likeList = new ArrayList<String>();
		int i = 0;
		for (EventAttributes event : eventList){
			if (i == 0){
				likeList.add(null);
			}
			else if (likeEarthquakeName.equalsIgnoreCase(event.getLikeEarthquake())){
				likeList.add(event.getEventName());
			}
			i++;
		}
		
		return likeList;
	}
	
	public int getIndexByEventName(String eventName){
		int index = 0;
		
		for (EventAttributes event : eventList){
			if (event.getEventName().equalsIgnoreCase(eventName)){//eventName.equalsIgnoreCase(event.getEventName())){
				return index;
			}
			
			index++;
		}
		
		return 0;
	}

	public void setAppendActors(AppendActors pluginActors) {
		// TODO Auto-generated method stub
		segmentActors = pluginActors;
	}

	public AppendActors getAppendActor() {
		// TODO Auto-generated method stub
		return currentBoundary.getAppendActor();
	}
}