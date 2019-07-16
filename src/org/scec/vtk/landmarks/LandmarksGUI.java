package org.scec.vtk.landmarks;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

//import org.scec.vtk.drawingTools.DefaultLocationsGUI.PresetLocationGroup;
import org.scec.vtk.drawingTools.DisplayAttributes;
import org.scec.vtk.drawingTools.DrawingTool;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.utils.components.CheckAllTable;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;
import org.scec.vtk.tools.actors.AppendActors;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.DBFReaderJGeom;
import oracle.spatial.util.ShapefileReaderJGeom;
import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkConeSource;
import vtk.vtkGlyph3D;
import vtk.vtkLabelPlacementMapper;
import vtk.vtkPointSetToLabelHierarchy;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkStringArray;


public class LandmarksGUI {
	private static String dataPath = Info.getMainGUI().getRootPluginDir()+File.separator+"GISLocationPlugin"+File.separator;
	ArrayList<PresetLocationGroup> presetLocationGroups = new ArrayList<PresetLocationGroup>();
	private String selectedInputFile;
	private DisplayAttributes displayAttributes;
	ArrayList<String> citypop = new ArrayList<String>();
	ArrayList<String> landmarkNames;
	CheckAllTable landmarkGroupsTable;
	CheckAllTable landmarkTable;
	private JPanel landmarksMainPanel;
	Dimension dMainPanel;
	AppendActors appendActors = new AppendActors();
	
	public LandmarksGUI(PluginActors pluginActors) {
		pluginActors.addActor(appendActors.getAppendedActor());
		landmarkNames = new ArrayList<String>();
		landmarksMainPanel = new JPanel(new GridLayout(0,1));
		parseTitles(dataPath);
		landmarkGroupsTable = new CheckAllTable(landmarkNames, "Landmarks");
		landmarksMainPanel.add(landmarkGroupsTable);
		//landmarkGroupsTable.addControlColumn(mouseListener, ">");
		dMainPanel = new Dimension(Prefs.getPluginWidth(),Prefs.getPluginHeight());
		landmarksMainPanel.setPreferredSize(dMainPanel);
	};
	
	public JPanel loadLandmarks() {
		return landmarksMainPanel;
	}

	private void parseTitles(String dataPath) {
		File dataDirectory = new File(dataPath);
		if (dataDirectory.isDirectory()) {
			// List files in the directory and process each
			File files[] = dataDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile() && files[i].getName().endsWith(".shp") || 
						files[i].getName().endsWith(".txt") 
						&& !files[i].getName().equals("CA_Counties.txt") 
						&& !files[i].getName().contains("popdensity.txt") 
						&& !files[i].getName().contains("CA_Cities")) {
					PresetLocationGroup group = new PresetLocationGroup();
					group.file = files[i];
					String tempName = files[i].getName();
					tempName = tempName.substring(0, tempName.lastIndexOf("."));
					tempName = tempName.replace('_', ' ');
					group.name = tempName;
					presetLocationGroups.add(group);
					landmarkNames.add(tempName);
				}
			}
		}
//		PresetLocationGroup tempGroup = presetLocationGroups.get(6);
//	//	System.out.println(tempGroup.name);
//	//	System.out.println(tempGroup.locations);
//		selectedInputFile = tempGroup.file.getAbsolutePath();
//		tempGroup.locations = loadBuiltInFiles(selectedInputFile);
////		for (int i = 0; i < tempGroup.locations.size(); i++) {
////			System.out.println(tempGroup.locations.get(i).getTextString());
////		}
//		landmarkGroupsTable = new CheckAllTable(landmarkNames, "Landmarks");
//		landmarksMainPanel.add(landmarkGroupsTable);
//		addBuiltInFiles(tempGroup.locations);
		
	}
	private class PresetLocationGroup {
		public Vector<DrawingTool> locations = null;
		public String name			= null;
		public File file			= null;
		public JCheckBox checkbox	= null;
	}
	public DrawingTool addDrawingTool(DrawingTool drawingTool, String text){
		double[] pt= {Transform.calcRadius(37),37,-120};
		if(drawingTool.getTextString()!=null) {
			text = drawingTool.getTextString();
			drawingTool.setDisplayName(drawingTool.getTextString());
			pt[0]= pt[0]+drawingTool.getaltitude();
			pt[1]= drawingTool.getLatitude();
			pt[2]= drawingTool.getLongitude();
		}
		
		//text as label facing camera
		vtkStringArray labels =new vtkStringArray();
		labels.SetName("labels");
		labels.SetNumberOfValues(1);
		labels.SetValue(0, text);
		vtkPoints labelPoints = new vtkPoints();
		labelPoints.InsertNextPoint(Transform.customTransform(pt));

		//create a pin near the text to mark the location 
		vtkPolyData pinPolydata = new  vtkPolyData();
		pinPolydata.SetPoints(labelPoints);

		// Use sphere as glyph source.
		vtkConeSource conePin = new vtkConeSource();
		conePin.SetRadius(3);
		conePin.SetHeight(10);
		conePin.SetDirection(-Transform.customTransform(pt)[0],-Transform.customTransform(pt)[1],-Transform.customTransform(pt)[2]);
		conePin.SetResolution(10);

		vtkGlyph3D glyphPoints = new vtkGlyph3D();
		glyphPoints.SetInputData(pinPolydata);
		glyphPoints.SetSourceConnection(conePin.GetOutputPort());

		vtkPolyDataMapper pm = new vtkPolyDataMapper();
		pm.SetInputConnection(glyphPoints.GetOutputPort());

		vtkPolyData temp = new vtkPolyData();
		temp.SetPoints(labelPoints);
		temp.GetPointData().AddArray(labels);

		vtkPointSetToLabelHierarchy pointSetToLabelHierarchyFilter =new vtkPointSetToLabelHierarchy();
		pointSetToLabelHierarchyFilter.SetInputData(temp);
		pointSetToLabelHierarchyFilter.GetTextProperty().SetJustificationToLeft();
		pointSetToLabelHierarchyFilter.SetLabelArrayName("labels");
		//pointSetToLabelHierarchyFilter.SetInputConnection(pinSource.GetOutputPort());
		pointSetToLabelHierarchyFilter.Update();
		pointSetToLabelHierarchyFilter.GetTextProperty().SetFontSize(21);

		vtkLabelPlacementMapper cellMapper = new vtkLabelPlacementMapper();
		cellMapper.SetInputConnection(pointSetToLabelHierarchyFilter.GetOutputPort());

		vtkActor2D actor = new vtkActor2D();
		actor.SetMapper(cellMapper);

		vtkActor actorPin = new vtkActor();
		actorPin.SetMapper(pm);

		if(drawingTool.getTextString()==null){
			drawingTool = new DrawingTool(pt[1],pt[2],pt[0],text,null,Color.white,actorPin,actor);
			drawingTool.setDisplayName(text +" -"); 
		}
		else{
			drawingTool.setActors(actorPin,actor);
		}

		//first pin then label
		appendActors.addToAppendedPolyData(actorPin);
		appendActors.addToAppendedPolyData(actor);
		appendActors.getAppendedActor().Modified();
		HashMap<String,String> locData = new HashMap<String, String>();
		locData.put("Lat", String.format("%.1f", pt[1])); 
		locData.put("Lon", String.format("%.1f", pt[2]));
		locData.put("Alt", "0");
		locData.put("pinH", "10");
		locData.put("pinR", "3");
		locData.put("fontSize", "21");
		//AttributesData.add(locData);
		drawingTool.setAttributes(locData);
	//	drawingToolsArray.add(drawingTool);
		return drawingTool;
	}
	private void addBuiltInFiles(Vector<DrawingTool> locations) {
		for (int i = 0; i < locations.size(); i++) {
			DrawingTool tempLocation = locations.get(i);
			addDrawingTool(tempLocation, "Text");
		}
		Info.getMainGUI().updateRenderWindow();
	}
	private Vector<DrawingTool> loadBuiltInFiles(String selectedInputFile) {
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
					tempLocation.setSourceFile(selectedInputFile);
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
					tempLocation.setSourceFile(selectedInputFile);
				}
				return locations;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	MouseAdapter mouseListener = new MouseAdapter() {
		  public void mousePressed(MouseEvent e) {
			JTable target = (JTable)e.getSource();
			int row = target.getSelectedRow();
			int col = target.columnAtPoint(e.getPoint());
		    if (e.getClickCount() == 2 || col == 2) {
		      String subRegionName = (String)target.getValueAt(row, 1);
		     // changeToSubTable(subRegionName, regionTable, subRegionTables);
		    }
		  }
	};

}