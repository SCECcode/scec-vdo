package org.scec.vtk.politicalBoundaries;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.scec.vtk.drawingTools.DrawingTool;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.utils.components.CheckAllTable;
import org.scec.vtk.plugins.utils.components.CheckAllTable.ControlPanel;
import org.scec.vtk.plugins.utils.components.ColorButton;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;
import org.scec.vtk.plugins.utils.components.TreeNode;
import org.scec.vtk.politicalBoundaries.PoliticalBoundariesFileParser.PresetLocationGroup;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;
import org.scec.vtk.tools.actors.AppendActors;

import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkCellArray;
import vtk.vtkConeSource;
import vtk.vtkDoubleArray;
import vtk.vtkGlyph3D;
import vtk.vtkLabelPlacementMapper;
import vtk.vtkLine;
import vtk.vtkPointSetToLabelHierarchy;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkStringArray;

public class PoliticalBoundariesGUI implements ActionListener {
	private JPanel mainPanel;														//Main panel of the plugin. Contains the CheckAllTable.
	private ArrayList<vtkActor> actorPoliticalBoundariesSegments;					//List of vtkActors of all country, continent, and region boundaries. A vtkActor handle the 3D visualization of the object
	private ArrayList<String> allSubRegionNames;									//allSubRegionNames contains all country names. Unfortunately this is necessary because of the way vtkActors are saved above.
	Dimension dMainPanel;															//Dimensions for main panel.
	public static vtkActor mainFocusReginActor = new vtkActor();						
	PluginActors pluginActors = new PluginActors();									//Contains actors of country and continent boundaries.
	AppendActors appendActors = new AppendActors();									//Contains actors of landmarks
	private ColorButton colorDrawingToolsButton;									//Color button
	private SingleColorChooser colorChooser;										//Color chooser dialog
	
	private Object[][] regionTableData = {{Boolean.FALSE, "Africa", Color.white},				//Data for continents
										{Boolean.FALSE, "Asia", Color.white},
										{Boolean.FALSE, "Europe", Color.white},
										{Boolean.FALSE, "North America", Color.white},
										{Boolean.FALSE, "Oceania", Color.white},
										{Boolean.FALSE, "South America", Color.white},
										{Boolean.FALSE, "United States", Color.white}};
	
	private String[][] regionFileNames = {{"United States","us_complete.txt"},					//All data for countries/regions are in text files.
									{"Africa","africa.txt"}, 
									{"Asia", "asia.txt"},
									{"Europe", "europe.txt"},
									{"North America", "north_america.txt"},
									{"Oceania", "oceania.txt"},
									{"South America", "south_america.txt"}};
	
	TreeNode<CheckAllTable> root;														//All CheckAllTables are part of the root tree to enable navigation between CheckAllTables.
	PoliticalBoundariesFileParser fileParser;											//Helps with file parsing. TODO::Put text file parsing inside this file.
	ArrayList<DrawingTool> allActiveDrawings;											//Contains all landmark drawings - vtk labels and pins. Does not contain country/continent boundaries.
	ArrayList<DrawingTool> countyDrawings;	
	
	public PoliticalBoundariesGUI(PluginActors pluginActors){
		this.pluginActors = pluginActors;												//Plugin actors handle the display of vtk objects
		this.pluginActors.addActor(appendActors.getAppendedActor());
		
		createMainPanel();																//Create main panel. All CheckAllTables must be contained in the same parent panel to enable navigation between tables.
		
		fileParser = new PoliticalBoundariesFileParser();							
		allActiveDrawings = new ArrayList<DrawingTool>();
		countyDrawings = new ArrayList<DrawingTool>();
		this.actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
		this.allSubRegionNames = new ArrayList<String>();
		
	}
	
	/**
	 * Create MainPanel which contains CheckAllTables.
	 */
	public void createMainPanel() {
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setName("Political Boundaries");
		dMainPanel = new Dimension(Prefs.getPluginWidth(),Prefs.getPluginHeight());
		mainPanel.setPreferredSize(dMainPanel);
		mainPanel.setOpaque(false);
	}

	/**
	 * @returns the main panel to be displayed in the plugin.
	 *  Populates table tree with all tables and children tables.
	 * 
	 */
	public JPanel loadAllRegions() {
	    String name = "Regions";
		CheckAllTable regionTable = new CheckAllTable(regionTableData, name, checkNextTableListener);		
		root = new TreeNode<CheckAllTable>(regionTable);													//Root of the tree contains the regionTable - the continents and the U.S.
		mainPanel.add(regionTable, BorderLayout.PAGE_START);
		for (int i = 0; i < regionFileNames.length; i++) {													
			ArrayList<String> subRegions = loadRegion(regionFileNames[i][1], false);
			CheckAllTable subRegionTable = setUpTable(subRegions, regionFileNames[i][0], subRegionListener, new ColorListener(false));
			TreeNode<CheckAllTable> subRegionNode = root.addChild(subRegionTable);							//Add subRegion tables as children of the root node. subRegions include countries and states.

			if(subRegionTable.getTitle() == "United States") {
				subRegionNode.data.getTable().getModel().setValueAt(true, 4, 0);							//Make California visible by default. TODO::Un-hardcode this value and search for default instead.
				loadLandmarks("California", subRegionNode);												//Add landmark tables as children of the subRegion nodes. 
			}
			if(subRegionTable.getTitle() == "North America") {	
				loadLandmarks("Mexico", subRegionNode);
				
			}
			if(subRegionTable.getTitle() == "South America") {
				loadLandmarks("Chile", subRegionNode);
			}
			if(subRegionTable.getTitle() == "Oceania") {
				loadLandmarks("Indonesia", subRegionNode);
				loadLandmarks("New Zealand", subRegionNode);
			}
			if(subRegionTable.getTitle() == "Asia") {
				loadLandmarks("Japan", subRegionNode);
			}
			subRegionTable.addControlColumn(forwardClickListener, ">", subRegionNode);						//Control column to implement navigation. Can also be used for loading new data.
		}
		regionTable.addControlColumn(forwardClickListener, ">", root);
		regionTable.addColorButton(new ColorListener(true));												//Add color button. Color listener is set to true, which means it changes the color of its direct children as well as its own color.
		return this.mainPanel;
	}

	/**
	 * 
	 * @param groupName
	 * @param subRegionNode
	 * 
	 * 
	 * Loads landmarks and add them to the table tree
	 * 
	 */
	private void loadLandmarks(String groupName, TreeNode<CheckAllTable> subRegionNode) {
		ArrayList<String> landmarks = fileParser.loadLandmarkGroups(groupName);												//Use fileParser to parse landmark data based on the group name.
		CheckAllTable landmarksTable = setUpTable(landmarks, groupName, checkNextTableListener, new ColorListener(true));	//Create table to display all landmark groups for the subRegion
		TreeNode<CheckAllTable> landmarksNode = subRegionNode.addChild(landmarksTable);										//Add the table as child of the subRegion table.
		landmarksTable.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (int j = 0; j < landmarks.size(); j++) {																		
			final PresetLocationGroup landmarkData = fileParser.loadLandmarkData(landmarks.get(j));		
			CheckAllTable landmarkDataTable = setUpTable(landmarkData.locationNames, landmarks.get(j), new LandmarkListener(landmarkData), new ColorListener(false));		//Create table to display landmarks in landmark group.
			landmarksNode.addChild(landmarkDataTable);																		//Add table as child.
		}
		landmarksTable.addControlColumn(forwardClickListener, ">", landmarksNode);
	}

	/**
	 * 
	 * @param tableData
	 * @param title
	 * @param tableListener
	 * @param colorListener
	 * @return table
	 * 
	 * Create new CheckAllTable with listeners and buttons added.
	 * 
	 */
	public CheckAllTable setUpTable(ArrayList<String> tableData, String title, TableModelListener tableListener, ActionListener colorListener) {
		title = title.replace('_', ' ');
		CheckAllTable table = new CheckAllTable(tableData, title, tableListener);
		table.getTable().getTableHeader().addMouseListener(backClickListener);
		table.addColorButton(colorListener);
		return table;
	}
	
	/**
	 * Add vtkActors
	 */
	public void addPoliticalBoundaryActors() {
		ArrayList<vtkActor> actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
		actorPoliticalBoundariesSegments = getPoliticalBoundaries();
		if(actorPoliticalBoundariesSegments.size()>0) {
			for(int j =0;j<actorPoliticalBoundariesSegments.size();j++) {
				vtkActor pbActor = actorPoliticalBoundariesSegments.get(j);
				pluginActors.addActor(pbActor);
			}
		}
	}
	
	/**
	 * @param drawingTool
	 * 
	 * Remove vtk pin and labels.
	 */
	public void removeDrawingTool(DrawingTool drawingTool) {
		appendActors.getAppendedActor().RemovePart(drawingTool.getActorPin());
		appendActors.getAppendedActor().RemovePart(drawingTool.getActorText());
	}

	/**
	 * @param drawingTool
	 * @param text
	 * @return
	 * 
	 * Creates vtk actors for a drawing tool and adds it to allActiveDrawings
	 * 
	 */
	public DrawingTool addDrawingTool(DrawingTool drawingTool, String text){
		if (drawingTool.getActorPin()!= null) {
			appendActors.addToAppendedPolyData(drawingTool.getActorPin());
			if (drawingTool.getActorText() != null) {
				appendActors.addToAppendedPolyData(drawingTool.getActorText());
			}
			appendActors.getAppendedActor().Modified();
			return drawingTool;
		}
		
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
		
		final vtkActor actorPin = new vtkActor();
		actorPin.SetMapper(pm);

		vtkPolyData temp = new vtkPolyData();
		temp.SetPoints(labelPoints);
		temp.GetPointData().AddArray(labels);

		vtkPointSetToLabelHierarchy pointSetToLabelHierarchyFilter =new vtkPointSetToLabelHierarchy();
		pointSetToLabelHierarchyFilter.SetInputData(temp);
		pointSetToLabelHierarchyFilter.GetTextProperty().SetJustificationToLeft();
		pointSetToLabelHierarchyFilter.SetLabelArrayName("labels");
		//pointSetToLabelHierarchyFilter.SetInputConnection(pinSource.GetOutputPort());
		pointSetToLabelHierarchyFilter.Update();
		pointSetToLabelHierarchyFilter.GetTextProperty().SetFontSize(15);

		vtkLabelPlacementMapper cellMapper = new vtkLabelPlacementMapper();
		cellMapper.SetInputConnection(pointSetToLabelHierarchyFilter.GetOutputPort());

		final vtkActor2D actor = new vtkActor2D();
		actor.SetMapper(cellMapper);

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
		locData.put("fontSize", "15");
		//AttributesData.add(locData);
		drawingTool.setAttributes(locData);
	//	drawingToolsArray.add(drawingTool);
		return drawingTool;
	}
	
	/**
	 * 
	 * @param filename
	 * @param isSelected
	 * @return
	 * 
	 * Loads boundaries from the text files and creates vtk actors to display them. 
	 * 
	 */
	public ArrayList<String> loadRegion(String filename, boolean isSelected)
	{
		//subRegions are displayed in the lower panel of this plugin
		PoliticalBoundariesRegion subRegions = new PoliticalBoundariesRegion(); 
		//Path to subRegion data files
		String sourcePath = Info.getMainGUI().getRootPluginDir() + File.separator + "PoliticalBoundaries/sourcefiles/"+filename;
		//List of boundaries pulled from data in sourcePath
		ArrayList<ArrayList> boundaries = (ArrayList<ArrayList>) subRegions.buildBoundaries(sourcePath);//this.getClass().getResource("resources/sourcefiles/"+filename));
		//vtkPolyData us_boundaries = (vtkPolyData) newBoundaries.buildBoundaries(this.getClass().getResource("resources/sourcefiles/us.vtk").getPath());
		
		// This function says .getUSStateNames but actually works for all regions
		ArrayList<String> subRegionNames = subRegions.getUSStateNames();
		for (int i = 0; i < subRegionNames.size(); i++) {
			allSubRegionNames.add(subRegionNames.get(i));
			subRegionNames.set(i, subRegionNames.get(i).replace('_', ' '));

		}
		vtkLine line = new vtkLine();
		int countpts = 0;
		for(int j = 0; j < boundaries.size(); j++) {
			//Cast boundaries to arrayList of an ambiguous object type. Default type is vtkPoint.
			ArrayList<?> vtkBoundaries = (ArrayList<?>) boundaries.get(j);
			vtkDoubleArray latitude = new vtkDoubleArray();
			latitude.SetName("latitude");
			vtkDoubleArray	longitude = new vtkDoubleArray();
			longitude.SetName("longitude");
			vtkPoints boundary = new vtkPoints();
			vtkCellArray lines = new vtkCellArray();
			vtkPolyData linesPolyData = new vtkPolyData();
			countpts = 0;
			for(int k=0;k< vtkBoundaries.size();k++)
			{
				//segments
				vtkPoints segmentpoints = (vtkPoints) vtkBoundaries.get(k);

				for(int i = 0; i <  segmentpoints.GetNumberOfPoints(); i++)
				{
					double[] pt = segmentpoints.GetPoint(i);
					boundary.InsertNextPoint(Transform.transformLatLon(pt[0],pt[1]));	
				}
				for(int i = 0; i <  segmentpoints.GetNumberOfPoints()-1; i++)
				{
					//connect all edges
					line.GetPointIds().SetId(0, countpts);
					line.GetPointIds().SetId(1, countpts+1);
					lines.InsertNextCell(line);
					countpts=countpts+1;
				}
				countpts=countpts+1;
			}

			linesPolyData.SetPoints(boundary);
			linesPolyData.SetLines(lines);
			vtkPolyDataMapper mapper = new vtkPolyDataMapper();
			mapper.SetInputData(linesPolyData);
			//mapper.SetInputConnection(assign.GetOutputPort());

			vtkActor plyOutActor = new vtkActor();
			plyOutActor.SetMapper(mapper);
			plyOutActor.GetProperty().SetColor(1,1,1);
			if(isSelected && j == 4)
				{plyOutActor.VisibilityOn();
					mainFocusReginActor  = plyOutActor;
				}
			else
				plyOutActor.VisibilityOff();
			actorPoliticalBoundariesSegments.add(plyOutActor);
		}
		return subRegionNames;
	}

    //Search criteria for searching treenode
    private TreeNode<CheckAllTable> findTableNode(TreeNode<CheckAllTable> searchNode, final CheckAllTable targetTable) {
    	Comparable<CheckAllTable> searchCriteria = new Comparable<CheckAllTable>() {
			@Override
			public int compareTo(CheckAllTable table) {
				if (table == null)
					return 1;
				boolean nodeOk = (table == targetTable);
				return nodeOk ? 0 : 1;
			}
		};
		return searchNode.findTreeNode(searchCriteria);
    }
    
    //Search criteria for searching treenode by title of CheckAllTable
    private TreeNode<CheckAllTable> findTableNodeByTitle(TreeNode<CheckAllTable> searchNode, final String title) {
    	Comparable<CheckAllTable> searchCriteria = new Comparable<CheckAllTable>() {
			@Override
			public int compareTo(CheckAllTable table) {
				if (table == null)
					return 1;
				boolean nodeOk = (table.getTitle() == title);
				return nodeOk ? 0 : 1;
			}
		};
		return searchNode.findTreeNode(searchCriteria);
    }
    
    //Counties are a pain and everything county related should be rewritten properly.
    class LandmarkListener implements TableModelListener{
    	private PresetLocationGroup landmarkData;
    	public LandmarkListener(PresetLocationGroup landmarkData) {
    		super();
			this.landmarkData = landmarkData;
		}
    	@Override
    	public void tableChanged(TableModelEvent e) {
    		int row = e.getFirstRow();
    		int column = e.getColumn();
    		TableModel model = (TableModel) e.getSource();
    		if (column == 0) {
    			String landmarkName = (String) model.getValueAt(row, 1);
    			Color landmarkColor = (Color) model.getValueAt(row, 2);
    			Boolean checked = (Boolean) model.getValueAt(row, column);
    			for(int k = 0; k < landmarkData.locationNames.size(); k++) {
    				if (landmarkData.locationNames.get(k).equals(landmarkName)) {
    					if (checked) {
    						if(!allActiveDrawings.contains(landmarkData.locations.get(k))) {
    							allActiveDrawings.add(addDrawingTool(landmarkData.locations.get(k), ""));
    							if (landmarkData.counties != null) {
    								if (landmarkData.counties[k] != null) {
    									countyDrawings.add(addDrawingTool(landmarkData.counties[k], ""));
    								}
    							}
    						}
    						else {
    							setVisibility(landmarkData.locations.get(k), 1);
    							if (landmarkData.counties != null) {
    								if (landmarkData.counties[k] != null) {
    									setVisibility(landmarkData.counties[k], 1);
    								}
    							}
    						}
    						setColor(landmarkData.locations.get(k), landmarkColor);
//    						if (landmarkData.counties != null) {
//								if (landmarkData.counties[k] != null) {
//									setColor(landmarkData.counties[k], landmarkColor);
//								}
//							}
    					}
    					else {
    						if(allActiveDrawings.contains(landmarkData.locations.get(k))) {
    							setVisibility(allActiveDrawings.get(allActiveDrawings.indexOf(landmarkData.locations.get(k))), 0);
    						}
    						if (landmarkData.counties != null) {
								if (landmarkData.counties[k] != null) {
		    						if(countyDrawings.contains(landmarkData.counties[k])) {
		    							setVisibility(countyDrawings.get(countyDrawings.indexOf(landmarkData.counties[k])), 0);
		    						}
								}
    						}
    						
    					}
    					break;
    				}
    				
    			}
    		}
    		Info.getMainGUI().updateRenderWindow();
    	}
    }
    
    /**
     *  Listens for clicks on the header of the checkbox column. Can be copied and used in other plugins.
     *  This listener is the default to navigate to the parent table.
     */
    MouseAdapter backClickListener = new MouseAdapter() {
    	public void mouseClicked(MouseEvent e) {
    		//Getting path to checkalltable based on table header click
    		JTable target = ((JTableHeader)e.getSource()).getTable();
    		JViewport vp = (JViewport) target.getParent();
    	 	JScrollPane sp = (JScrollPane) vp.getParent();
    	 	final CheckAllTable targetTable = (CheckAllTable) sp.getParent();
    	 	TreeNode<CheckAllTable> currentTableNode = findTableNode(root, targetTable);
    		int col = target.columnAtPoint(e.getPoint());
    		// clear search bar 
    		targetTable.clearSearchBar();
    		if (col == 0) {
    			mainPanel.remove(targetTable);
    			currentTableNode.parent.data.renderTableHeader();
    			mainPanel.add(currentTableNode.parent.data);
    			mainPanel.revalidate(); 
    			mainPanel.repaint();
    		}
    		
    	}
    };
	
    /**
     *  Listens for clicks on the control column. Can be copied and used in other plugins.
     *  This listener is the default to navigate to a child table. 
     */
    MouseAdapter forwardClickListener = new MouseAdapter() {
    	public void mousePressed(MouseEvent e) {
    		JTable target = (JTable)e.getSource();
    		JViewport vp = (JViewport) target.getParent();
    		JScrollPane sp = (JScrollPane) vp.getParent();
    		final CheckAllTable targetTable = (CheckAllTable) sp.getParent();
    		TreeNode<CheckAllTable> currentTableNode = findTableNode(root, targetTable);
    		int row = target.getSelectedRow();
    		int col = target.columnAtPoint(e.getPoint());
    		targetTable.clearSearchBar();
    		if (col != 0) {
    			if (e.getClickCount() == 2 || col == targetTable.getTable().getColumnCount()-1) {
    				final String subTableName = (String)target.getValueAt(row, 1);
    				for (TreeNode<CheckAllTable> node : currentTableNode) {
    					if (node.data.getTitle().equals(subTableName)) {
    						mainPanel.remove(targetTable);
    						mainPanel.add(node.data);
    						node.data.renderTableHeader();
    					}
    				}
    				mainPanel.revalidate(); 
    				mainPanel.repaint();
    			}
    		}
    	}
    };
	
    /**
     * Listens for checks on the check column and checks all the boxes for the rows child table.
     * Can be copied and used in other plugins.
     */
	TableModelListener checkNextTableListener = new TableModelListener() {
		@Override
		public void tableChanged(final TableModelEvent e) {
			final int row = e.getFirstRow();
			final int column = e.getColumn();
			if (column == 0) {
						TableModel model = (TableModel) e.getSource();
						String subTableName = (String) model.getValueAt(row, column+1);
						final Boolean checked = (Boolean) model.getValueAt(row, column);
						final TreeNode<CheckAllTable> nextTableNode = findTableNodeByTitle(root, subTableName);
						for (int i = 0 ; i < nextTableNode.data.getTable().getRowCount(); i++) {
							nextTableNode.data.getTable().setValueAt(checked, i, 0);
							nextTableNode.data.getTable().setValueAt(model.getValueAt(row, 2), i, 2);
						}   
				}
			}
		};
	
	/**
	 * Listener specific to this plugin. Turns region and subregion boundaries on and off.
	 */
	TableModelListener subRegionListener = new TableModelListener() {
		@Override
		public void tableChanged(TableModelEvent e) {
			int row = e.getFirstRow();
			int column = e.getColumn();
			if (column == 0) {
				TableModel model = (TableModel) e.getSource();
				String subRegionName = (String) model.getValueAt(row, column+1);
				Boolean checked = (Boolean) model.getValueAt(row, column);
    			Color subRegionColor = (Color) model.getValueAt(row, 2);
				if (allSubRegionNames.contains(subRegionName)) {
					vtkActor actor = actorPoliticalBoundariesSegments.get(allSubRegionNames.indexOf(subRegionName));
					if (checked) {
						actorPoliticalBoundariesSegments.get(allSubRegionNames.indexOf(subRegionName)).GetProperty().SetColor(Info.convertColor(subRegionColor));
						actor.VisibilityOn();
					}
					else {
						actor.VisibilityOff();
					}
				}
				Info.getMainGUI().updateRenderWindow();
			}
		}
	};
	
	/**
	 * @param targetTable
	 * @param tableName
	 * @param color
	 * 
	 * Helper function to set colors.
	 * 
	 */
	private void setVtkColors(JTable targetTable, String tableName, Color color) {
		if (allSubRegionNames.contains(tableName)) {
			actorPoliticalBoundariesSegments.get(allSubRegionNames.indexOf(tableName)).GetProperty().SetColor(Info.convertColor(color));
		}
		if (allActiveDrawings != null) {
			for (int k = 0; k < allActiveDrawings.size(); k++) {
				if (tableName.equals(allActiveDrawings.get(k).getTextString())) {
					allActiveDrawings.get(k).setColor(color);
					setColor(allActiveDrawings.get(k), color);
				}
			}
		}
	}
	/**
	 * @author intern
	 *
	 * Listens for color button click and changes colors as appropriate.
	 *
	 */
	class ColorListener implements ActionListener {
		private PresetLocationGroup landmarkData;
		private boolean colorNextTable = false;
		public ColorListener(PresetLocationGroup landmarkData) {
			this.landmarkData = landmarkData;
		}
		public ColorListener(boolean colorNextTable) {
			this.colorNextTable = colorNextTable;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			ColorButton target = (ColorButton) e.getSource();
			final JPanel jPanel = (JPanel) target.getParent();
    	 	final ControlPanel controlPanel = (ControlPanel) jPanel.getParent();
    	 	final CheckAllTable targetPanel = (CheckAllTable) controlPanel.getParent();
    	 	JTable table = targetPanel.getTable();
			if (colorChooser == null) {
				colorChooser = new SingleColorChooser(colorDrawingToolsButton);
			}
			Color newColor = colorChooser.getColor();
			if (newColor != null) {
				for (int i = 0; i < table.getModel().getRowCount(); i++) {
					if (table.getSelectionModel().isSelectedIndex(i)) {
						String subTableName = (String) table.getModel().getValueAt(table.convertRowIndexToModel(i), 1);
						table.getModel().setValueAt(newColor, table.convertRowIndexToModel(i), 2);
						if (!colorNextTable) {
							setVtkColors(table, subTableName, newColor);
							if (allSubRegionNames.contains(subTableName)) {
								table.getModel().setValueAt(newColor ,table.convertRowIndexToModel(i), 2);
							}
						}
						else {
				    	 	final TreeNode<CheckAllTable> nextTableNode = findTableNodeByTitle(root, subTableName);
				    	 	for (int j = 0 ; j < nextTableNode.data.getTable().getRowCount(); j++) {
								String nextTableName = (String) nextTableNode.data.getTable().getModel().getValueAt(j, 1);
								setVtkColors(table, nextTableName, newColor);
								nextTableNode.data.getTable().getModel().setValueAt(newColor, nextTableNode.data.getTable().convertRowIndexToModel(j), 2);
							}
						}
					}
				}
			}
			Info.getMainGUI().updateRenderWindow();
		}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
//		Object src = e.getSource();
//		if (src == this.colorDrawingToolsButton){
//			if (this.colorChooser == null) {
//				this.colorChooser = new SingleColorChooser(colorDrawingToolsButton);
//			}
//			Color newColor = this.colorChooser.getColor();
//			if (newColor != null) {
//				for(int j =0;j<actorPoliticalBoundariesSegments.size();j++)
//				{
//					actorPoliticalBoundariesSegments.get(j).GetProperty().SetColor(Info.convertColor(newColor));
//				}
//			}
//			Info.getMainGUI().updateRenderWindow();
//		}
	}
	
	
	public void setColor(DrawingTool dr, Color newColor) {
		vtkActor2D actor = ((vtkActor2D) dr.getActorText());
		vtkActor actorPin = (vtkActor) dr.getActorPin();
		
		double[] color = {newColor.getRed()/Info.rgbMax,newColor.getGreen()/Info.rgbMax,newColor.getBlue()/Info.rgbMax};
		if (actor != null && actorPin != null) {
			((vtkPointSetToLabelHierarchy) (actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetColor(color);
		actorPin.GetProperty().SetColor(color);
		}
		else if(actorPin!=null && actor == null)
		{
			actorPin.GetProperty().SetColor(color);
		}
	}
	
	public void setVisibility(DrawingTool dr, Integer visible) {
		vtkActor2D actor = ((vtkActor2D) dr.getActorText());
		vtkActor actorPin = (vtkActor) dr.getActorPin();
		
		if (actor != null && actorPin != null) {
		actor.SetVisibility(visible);
		actorPin.SetVisibility(visible);
		}
		else if(actorPin!=null && actor == null)
		{
			actorPin.SetVisibility(visible);
		}
	}
	  
	public ArrayList<vtkActor> getPoliticalBoundaries()
	{
		return actorPoliticalBoundariesSegments;
	}
	
	public ArrayList<JCheckBox> getLowerCheckBoxButtons()
	{
		ArrayList<JCheckBox> lowerCheckBoxButtons = new ArrayList<JCheckBox>();
		return lowerCheckBoxButtons;
	}
	
	public ArrayList<JCheckBox> getUpperCheckBoxButtons()
	{
		ArrayList<JCheckBox> upperCheckBoxButtons = new ArrayList<JCheckBox>();
		return upperCheckBoxButtons;
	}
}
