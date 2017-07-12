package org.scec.vtk.politicalBoundaries;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import org.scec.vtk.drawingTools.DisplayAttributes;
import org.scec.vtk.drawingTools.DrawingTool;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.utils.components.CheckAllTable;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.DBFReaderJGeom;
import oracle.spatial.util.ShapefileReaderJGeom;

public class PoliticalBoundariesFileParser {
	
	class PresetLocationGroup {
		public ArrayList<DrawingTool> locations = null;
		public ArrayList<String> locationNames = null;
		public String name			= null;
		public File file			= null;
	}
	
	ArrayList<PresetLocationGroup> presetLocationGroups;
	private static String landmarksDataPath = Info.getMainGUI().getRootPluginDir()+File.separator+"GISLocationPlugin"+File.separator;
	private DisplayAttributes displayAttributes;
	
	public PoliticalBoundariesFileParser() {
		parseFiles(landmarksDataPath);
	}
	
	private void parseFiles(String dataPath) {
		presetLocationGroups = new ArrayList<PresetLocationGroup>();
		File dataDirectory = new File(dataPath);
		if (dataDirectory.isDirectory()) {
			// List files in the directory and process each
			File files[] = dataDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile() && files[i].getName().endsWith(".shp") || 
						files[i].getName().endsWith(".txt") 
						&& !files[i].getName().equals("CA_Counties.txt") 
						&& !files[i].getName().contains("popdensity.txt") 
						&& !files[i].getName().contains("CA_Cities")
						&& !files[i].getName().contains("California_Interstates")) {
					PresetLocationGroup group = new PresetLocationGroup();
					group.file = files[i];
					String tempName = files[i].getName();
					tempName = tempName.substring(0, tempName.lastIndexOf("."));
					tempName = tempName.replace('_', ' ');
					group.name = tempName;
					presetLocationGroups.add(group);
				}
			}
		}
	}
	
	public ArrayList<String> loadCALandmarkGroups() {
		ArrayList<String> groupNames = new ArrayList<String>();
		for (int i = 0; i < presetLocationGroups.size(); i++) {
			if (presetLocationGroups.get(i).name.contains("CA") || presetLocationGroups.get(i).name.contains("California"))
				groupNames.add(presetLocationGroups.get(i).name);
		}
		return groupNames;
	}
	public ArrayList<String> loadMexicoLandmarkGroups() {
		ArrayList<String> groupNames = new ArrayList<String>();
		for (int i = 0; i < presetLocationGroups.size(); i++) {
			if (presetLocationGroups.get(i).name.contains("Mexico"))
				groupNames.add(presetLocationGroups.get(i).name);
		}
		return groupNames;
	}
	
	public PresetLocationGroup loadLandmarkData(String groupName) {
		for (int i = 0; i < presetLocationGroups.size(); i++) {
			PresetLocationGroup tempGroup = presetLocationGroups.get(i);
			if (tempGroup.name.equals(groupName)) {
				String selectedInputFile = tempGroup.file.getAbsolutePath();
				tempGroup = loadBuiltInFiles(selectedInputFile, tempGroup);
				return tempGroup;
			}
		}
		return new PresetLocationGroup();
	}
	
	private PresetLocationGroup loadBuiltInFiles(String selectedInputFile, PresetLocationGroup group) {
		//if the dbf file exists, then read from the dbf file for the label names
		String selectedDbfFile = selectedInputFile.replace(".shp", ".dbf");
		File dbf = new File(selectedDbfFile);
		group.locations = new ArrayList<DrawingTool>();
		group.locationNames = new ArrayList<String>();
		if(dbf.exists()) {
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
					group.locations.add(tempLocation);
					group.locationNames.add(tempLocation.getTextString());
					tempLocation.setSourceFile(selectedInputFile);
				}
				return group;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
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
					group.locations.add(tempLocation);
					group.locationNames.add(tempLocation.getTextString());
					tempLocation.setSourceFile(selectedInputFile);
				}
				return group;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
