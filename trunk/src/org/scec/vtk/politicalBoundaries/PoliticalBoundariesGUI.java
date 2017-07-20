package org.scec.vtk.politicalBoundaries;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;


import org.netlib.util.booleanW;
import org.apache.commons.math3.exception.NoDataException;
import org.jpedal.utils.sleep;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.scec.vtk.commons.opensha.tree.events.ColorChangeListener;
import org.scec.vtk.drawingTools.DisplayAttributes;
import org.scec.vtk.drawingTools.DrawingTool;
import org.scec.vtk.drawingTools.DrawingToolsGUI;
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

import com.sun.corba.se.impl.orbutil.graph.Node;
import com.sun.xml.internal.bind.v2.model.core.ID;

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
	private JPanel politicalBoundaryMainPanel;
	private JPanel tablePanel;
	private ArrayList<vtkActor> actorPoliticalBoundariesSegments;
	private ArrayList<String> allSubRegionNames;
	Dimension dMainPanel;
	public static vtkActor mainFocusReginActor = new vtkActor();
	PluginActors pluginActors = new PluginActors();
	AppendActors appendActors = new AppendActors();
	private ColorButton colorDrawingToolsButton;
	private SingleColorChooser colorChooser;
	private DrawingToolsGUI gui;
	
	private Object[][] regionTableData = {{Boolean.FALSE, "Africa", Color.white},
										{Boolean.FALSE, "Asia", Color.white},
										{Boolean.FALSE, "Europe", Color.white},
										{Boolean.FALSE, "North America", Color.white},
										{Boolean.FALSE, "Oceania", Color.white},
										{Boolean.FALSE, "South America", Color.white},
										{Boolean.FALSE, "United States", Color.white}};
	
	private String[][] regionFileNames = {{"United States","us_complete.txt"},
									{"Africa","africa.txt"}, 
									{"Asia", "asia.txt"},
									{"Europe", "europe.txt"},
									{"North America", "north_america.txt"},
									{"Oceania", "oceania.txt"},
									{"South America", "south_america.txt"}};
	
	TreeNode<CheckAllTable> root;
	PoliticalBoundariesFileParser fileParser;
	ArrayList<DrawingTool> allActiveDrawings;
		
	
	public PoliticalBoundariesGUI(PluginActors pluginActors){
		//Plugin actors handle the display of vtk objects
		this.pluginActors = pluginActors;
		this.pluginActors.addActor(appendActors.getAppendedActor());
		createMainPanel();
		
		fileParser = new PoliticalBoundariesFileParser();
		allActiveDrawings = new ArrayList<DrawingTool>();
		//Upper panel contains regions
		this.tablePanel = new JPanel();
		this.tablePanel.setLayout(new BoxLayout(this.tablePanel, BoxLayout.Y_AXIS));
		this.tablePanel.setOpaque(false);
		
		//List of "actors" 
		this.actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
		allSubRegionNames = new ArrayList<String>();
		
	}
	
	public void createMainPanel() {
		//Main panel contains tablePanel	
		politicalBoundaryMainPanel = new JPanel(new BorderLayout());
		politicalBoundaryMainPanel.setName("Political Boundaries");
		dMainPanel = new Dimension(Prefs.getPluginWidth(),Prefs.getPluginHeight());
		politicalBoundaryMainPanel.setPreferredSize(dMainPanel);
		politicalBoundaryMainPanel.setOpaque(false);
	}

	/**
	 * Populates table tree with all tables and subtables
	 * @return
	 */
	public JPanel loadAllRegions() {
	    String name = "Regions";
	    //Create root table of the plugin - the regions table;
		CheckAllTable regionTable = new CheckAllTable(regionTableData, name, checkNextTableListener);
		root = new TreeNode<CheckAllTable>(regionTable);
		//Add root table to the panel;
		tablePanel.add(regionTable, BorderLayout.PAGE_START);
		this.politicalBoundaryMainPanel.add(tablePanel);
		//Add subregions and landmarks
		for (int i = 0; i < regionFileNames.length; i++) {
			ArrayList<String> subRegions = loadRegion(regionFileNames[i][1], false);
			CheckAllTable subRegionTable = setUpTable(subRegions, regionFileNames[i][0], subRegionListener, new ColorListener(false));
			TreeNode<CheckAllTable> subRegionNode = root.addChild(subRegionTable);

			if(subRegionTable.getTitle() == "United States") {
				subRegionNode.data.getTable().getModel().setValueAt(true, 4, 0);
				loadLandmarks("California", subRegionNode);
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
			subRegionTable.addControlColumn(forwardClickListener, ">", subRegionNode);
		}
		regionTable.addControlColumn(forwardClickListener, ">", root);
		regionTable.addColorButton(new ColorListener(true));

		return this.politicalBoundaryMainPanel;
	}

	
	private void loadLandmarks(String groupName, TreeNode<CheckAllTable> subRegionNode) {
		ArrayList<String> landmarks = fileParser.loadLandmarkGroups(groupName);
		CheckAllTable landmarksTable = setUpTable(landmarks, groupName, checkNextTableListener, new ColorListener(true));
		TreeNode<CheckAllTable> landmarksNode = subRegionNode.addChild(landmarksTable);
		landmarksTable.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		for (int j = 0; j < landmarks.size(); j++) {
			final PresetLocationGroup landmarkData = fileParser.loadLandmarkData(landmarks.get(j));
			CheckAllTable landmarkDataTable = setUpTable(landmarkData.locationNames, landmarks.get(j), new LandmarkListener(landmarkData), new ColorListener(false));
			landmarksNode.addChild(landmarkDataTable);
		}
		landmarksTable.addControlColumn(forwardClickListener, ">", landmarksNode);
	}

	 
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
	public void removeDrawingTool(DrawingTool drawingTool) {
		appendActors.getAppendedActor().RemovePart(drawingTool.getActorPin());
		appendActors.getAppendedActor().RemovePart(drawingTool.getActorText());
	}

	
	public DrawingTool addDrawingTool(DrawingTool drawingTool, String text){
		if (drawingTool.getActorPin()!= null) {
			appendActors.addToAppendedPolyData(drawingTool.getActorPin());
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
		locData.put("fontSize", "15");
		//AttributesData.add(locData);
		drawingTool.setAttributes(locData);
	//	drawingToolsArray.add(drawingTool);
		return drawingTool;
	}

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
    		
    		if (column == 0) {
    			TableModel model = (TableModel) e.getSource();
    			String landmarkName = (String) model.getValueAt(row, column+1);
    			Boolean checked = (Boolean) model.getValueAt(row, column);
    			for(int k = 0; k < landmarkData.locationNames.size(); k++) {
    				if (landmarkData.locationNames.get(k).equals(landmarkName)) {
    					System.out.println(landmarkData.locationNames.get(k));
    					if (checked) {
    						if(!allActiveDrawings.contains(landmarkData.locations.get(k))) {
    							allActiveDrawings.add(addDrawingTool(landmarkData.locations.get(k), ""));
    							//addDrawingTool(landmarkData.locations.get(k), "");
    						}
    						else {
    							setVisibility(landmarkData.locations.get(k), 1); 
    						}
    					}
    					else {
    						removeDrawingTool(landmarkData.locations.get(k));
    						allActiveDrawings.remove(landmarkData.locations.get(k));
    						//setVisibility(landmarkData.locations.get(k), 0);
    					}
    					break;
    				}
    				
    			}
    		}
    		Info.getMainGUI().updateRenderWindow();
    	}
    }

    
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
    			tablePanel.remove(targetTable);
    			currentTableNode.parent.data.renderTableHeader();
    			tablePanel.add(currentTableNode.parent.data);
    			tablePanel.revalidate(); 
    			tablePanel.repaint();
    		}
    		
    	}
    };
	
    MouseAdapter forwardClickListener = new MouseAdapter() {
    	public void mousePressed(MouseEvent e) {
    		JTable target = (JTable)e.getSource();
    		JViewport vp = (JViewport) target.getParent();
    		JScrollPane sp = (JScrollPane) vp.getParent();
    		final CheckAllTable targetTable = (CheckAllTable) sp.getParent();
    		TreeNode<CheckAllTable> currentTableNode = findTableNode(root, targetTable);
    		int row = target.getSelectedRow();
    		int col = target.columnAtPoint(e.getPoint());
    		if (col != 0) {
    			if (e.getClickCount() == 2 || col == targetTable.getTable().getColumnCount()-1) {
    				final String subTableName = (String)target.getValueAt(row, 1);
    				for (TreeNode<CheckAllTable> node : currentTableNode) {
    					if (node.data.getTitle().equals(subTableName)) {
    						tablePanel.remove(targetTable);
    						tablePanel.add(node.data);
    					}
    				}
    				tablePanel.revalidate(); 
    				tablePanel.repaint();
    			}
    		}
    	}
    };
	
	TableModelListener checkNextTableListener = new TableModelListener() {
		@Override
		public void tableChanged(final TableModelEvent e) {
			final int row = e.getFirstRow();
			final int column = e.getColumn();
			if (column == 0) {
						TableModel model = (TableModel) e.getSource();
						String subTableName = (String) model.getValueAt(row, column+1);
						System.out.println("subtable name: " + subTableName);
						final Boolean checked = (Boolean) model.getValueAt(row, column);
						final TreeNode<CheckAllTable> nextTableNode = findTableNodeByTitle(root, subTableName);
						for (int i = 0 ; i < nextTableNode.data.getTable().getRowCount(); i++) {
							nextTableNode.data.getTable().setValueAt(checked, i, 0);
						}   
				}
			}
		};
	
	TableModelListener subRegionListener = new TableModelListener() {
		@Override
		public void tableChanged(TableModelEvent e) {
			int row = e.getFirstRow();
			int column = e.getColumn();
			if (column == 0) {
				TableModel model = (TableModel) e.getSource();
				String subRegionName = (String) model.getValueAt(row, column+1);
				Boolean checked = (Boolean) model.getValueAt(row, column);
				if (allSubRegionNames.contains(subRegionName)) {
					vtkActor actor = actorPoliticalBoundariesSegments.get(allSubRegionNames.indexOf(subRegionName));
					if (checked) {
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
    	 	final ControlPanel cp = (ControlPanel) target.getParent();
    	 	final CheckAllTable targetPanel = (CheckAllTable) cp.getParent();
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
							if (allSubRegionNames.contains(subTableName)) {
								actorPoliticalBoundariesSegments.get(allSubRegionNames.indexOf(subTableName)).GetProperty().SetColor(Info.convertColor(newColor));
								table.getModel().setValueAt(newColor ,table.convertRowIndexToModel(i), 2);
							}
							if (allActiveDrawings != null) {
								for (int k = 0; k < allActiveDrawings.size(); k++) {
									if (subTableName.equals(allActiveDrawings.get(k).getTextString())) {
										allActiveDrawings.get(k).setColor(newColor);
										setColor(allActiveDrawings.get(k), newColor);
									}
								}
							}
						}
						else {
				    	 	final TreeNode<CheckAllTable> nextTableNode = findTableNodeByTitle(root, subTableName);
				    	 	for (int j = 0 ; j < nextTableNode.data.getTable().getRowCount(); j++) {
								String nextTableName = (String) nextTableNode.data.getTable().getModel().getValueAt(j, 1);
								if (allSubRegionNames.contains(nextTableName)) {
									actorPoliticalBoundariesSegments.get(allSubRegionNames.indexOf(nextTableName)).GetProperty().SetColor(Info.convertColor(newColor));
								}
								if (allActiveDrawings != null) {
									for (int k = 0; k < allActiveDrawings.size(); k++) {
										if (nextTableName.equals(allActiveDrawings.get(k).getTextString())) {
											allActiveDrawings.get(k).setColor(newColor);
											setColor(allActiveDrawings.get(k), newColor);
										}
									}
								}
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
		Object src = e.getSource();
		if (src == this.colorDrawingToolsButton){
			if (this.colorChooser == null) {
				this.colorChooser = new SingleColorChooser(colorDrawingToolsButton);
			}
			Color newColor = this.colorChooser.getColor();
			if (newColor != null) {
				for(int j =0;j<actorPoliticalBoundariesSegments.size();j++)
				{
					actorPoliticalBoundariesSegments.get(j).GetProperty().SetColor(Info.convertColor(newColor));
				}
			}
			Info.getMainGUI().updateRenderWindow();
		}
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
