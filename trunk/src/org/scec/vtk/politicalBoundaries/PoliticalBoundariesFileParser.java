package org.scec.vtk.politicalBoundaries;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.scec.vtk.drawingTools.DisplayAttributes;
import org.scec.vtk.drawingTools.DrawingTool;
import org.scec.vtk.drawingTools.DefaultLocationsGUI.PresetLocationGroup;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.utils.components.CheckAllTable;
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

public class PoliticalBoundariesFileParser {
	
	public class PresetLocationGroup {
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
		parseHighwayFiles();
	}
	
	private PresetLocationGroup parseHighwayFiles() {
		PresetLocationGroup highwayGroup = new PresetLocationGroup();
		String selectedFile = Info.getMainGUI().getRootPluginDir()+File.separator+"GISLocationPlugin"+File.separator+"California_Interstates.txt";
		File highwaysFile = new File(selectedFile);
		String temp[] = new String[2];
		String nameOfSegment="";
		vtkPoints linePts;
		vtkCellArray cells = new vtkCellArray();
		vtkPolyData polyData;
		vtkPolyDataMapper mapper;
		vtkActor actor = new vtkActor();
		double [] p = null;
		highwayGroup.locationNames = new ArrayList<String>();
		highwayGroup.locations = new ArrayList<DrawingTool>();
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
					segmentPoints = new ArrayList<vtkPoints>();

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
					highwayGroup.locations.add(highway);
					highwayGroup.locationNames.add(nameOfSegment);
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
		highwayGroup.name = "California Interstates";
		presetLocationGroups.add(highwayGroup);
		return highwayGroup; 
	}
	
	public ArrayList<String> loadLandmarkGroups(String groupname) {
		ArrayList<String> groupNames = new ArrayList<String>();
		for (int i = 0; i < presetLocationGroups.size(); i++) {
			if (groupname.equals("California")) {
				if (presetLocationGroups.get(i).name.contains("CA") || presetLocationGroups.get(i).name.contains("California") || presetLocationGroups.get(i).name.contains("LA")) {
					groupNames.add(presetLocationGroups.get(i).name);
				}
			}
			else if (presetLocationGroups.get(i).name.contains(groupname))
				groupNames.add(presetLocationGroups.get(i).name);
		}
		return groupNames;
	}

	public PresetLocationGroup loadLandmarkData(String groupName) {
		for (int i = 0; i < presetLocationGroups.size(); i++) {
			PresetLocationGroup tempGroup = presetLocationGroups.get(i);
			if (tempGroup.name.equals(groupName)) {
				if (tempGroup.name.equals("California Interstates")) {
					tempGroup = parseHighwayFiles();
				}
				
				else {
					String selectedInputFile = tempGroup.file.getAbsolutePath();
					tempGroup = loadBuiltInFiles(selectedInputFile, tempGroup);
				}
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
					String name = tempLocation.getTextString().replace('_', ' ');
					group.locationNames.add(name);
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
					String name = tempLocation.getTextString().replace('_', ' ');
					group.locationNames.add(name);
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
