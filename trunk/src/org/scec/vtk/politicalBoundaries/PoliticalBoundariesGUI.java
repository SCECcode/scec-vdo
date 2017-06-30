package org.scec.vtk.politicalBoundaries;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.utils.components.CheckAllTable;
import org.scec.vtk.plugins.utils.components.ColorButton;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

public class PoliticalBoundariesGUI implements ActionListener{
	private JPanel politicalBoundaryMainPanel;
	private JTabbedPane politicalBoundarySubPanelLowerTab;
	private JPanel politicalBoundarySubPanelLower;
	private JPanel politicalBoundarySubPanelUpper;
	//private ArrayList<ArrayList> actorPoliticalBoundariesMain;
	private ArrayList<vtkActor> actorPoliticalBoundariesSegments;
	private ArrayList<JCheckBox> lowerCheckBoxButtons;
	private ArrayList<JCheckBox> upperCheckBoxButtons;
	private ArrayList<String> allSubRegionNames;
	Dimension dMainPanel,dSubPanel;
	public static vtkActor mainFocusReginActor = new vtkActor();
	PluginActors pluginActors = new PluginActors();
	private ColorButton colorDrawingToolsButton;
	private SingleColorChooser colorChooser;
	private Object[][] regionTableData = {{Boolean.FALSE, "Africa"},
										{Boolean.FALSE, "Asia"},
										{Boolean.FALSE, "Europe"},
										{Boolean.FALSE, "North America"},
										{Boolean.FALSE, "Oceania"},
										{Boolean.FALSE, "South America"},
										{Boolean.TRUE, "United States"}};
	
	public PoliticalBoundariesGUI(PluginActors pluginActors){
		//Plugin actors are something. TODO: Explain this
		this.pluginActors = pluginActors;
		
		//Main panel contains upper and lower panels
		politicalBoundaryMainPanel = new JPanel(new GridLayout(0,1));
		politicalBoundaryMainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		politicalBoundaryMainPanel.setName("Political Boundaries");
		dMainPanel = new Dimension(Prefs.getPluginWidth(),Prefs.getPluginHeight());
		dSubPanel = new Dimension(Prefs.getPluginWidth(),100);
		politicalBoundaryMainPanel.setPreferredSize(dMainPanel);
		politicalBoundaryMainPanel.setOpaque(false);
		
		//Lower panel contains countries/states in region
		politicalBoundarySubPanelLower = new JPanel();
		politicalBoundarySubPanelLowerTab = new JTabbedPane();
		
		//Upper panel contains regions
		this.politicalBoundarySubPanelUpper=new JPanel();
		this.politicalBoundarySubPanelUpper.setLayout(new BoxLayout(this.politicalBoundarySubPanelUpper, BoxLayout.Y_AXIS));
		
		//List of "actors" TODO: Explain this
		this.actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
		
		allSubRegionNames = new ArrayList<String>();
		
		//Add color button for changing map color
		colorDrawingToolsButton = new ColorButton(this, "Change color of selected Text(s)");
		colorDrawingToolsButton.setEnabled(true);
		politicalBoundarySubPanelUpper.add(colorDrawingToolsButton);
	}

	/**
	 * Loads all tables with default regions/countries in upper and lower panels
	 * @return
	 */
	
	public JPanel loadAllRegions()
	{
		this.upperCheckBoxButtons = new ArrayList<JCheckBox>();
		this.lowerCheckBoxButtons = new ArrayList<JCheckBox>();
	    String title = "Regions";
		CheckAllTable regionTable = new CheckAllTable(regionTableData, title, listenerUpper);
		politicalBoundarySubPanelUpper.add(regionTable, BorderLayout.PAGE_START);
		addPanelToMainPanel(politicalBoundarySubPanelUpper);
		
		//default
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"us_complete.txt","United States",true);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"africa.txt","Africa", false);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"asia.txt","Asia",false);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"europe.txt","Europe",false);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"north_america.txt","North America",false);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"oceania.txt","Oceania",false);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"south_america.txt","South America",false);


		return this.politicalBoundaryMainPanel;

	}
	/**
	 * Add vtkActors to the thingy. TODO: Figure out what vtkActors are
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
	 * Add panel 
	 * @param panel
	 */
	public void addPanelToMainPanel(JPanel panel)	{
		this.politicalBoundaryMainPanel.add(panel);
	}
	/**
	 * Add subPanelLower to the main panel
	 * @param panel
	 * @param filename
	 * @param tabname
	 * @param isSelected
	 */
	public void addTabbedPanelToMainPanel(JPanel panel,String filename,String tabname,boolean isSelected)	{
		panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		ArrayList<String> subRegions = loadRegion(filename,this.lowerCheckBoxButtons,panel, isSelected);
		CheckAllTable subRegionTable = new CheckAllTable(subRegions, tabname, listenerLower);
		panel.add(subRegionTable);
		this.politicalBoundarySubPanelLowerTab.addTab(tabname, subRegionTable);
		this.politicalBoundaryMainPanel.add(this.politicalBoundarySubPanelLowerTab);
	}

	public ArrayList<String> loadRegion(String filename,ArrayList<JCheckBox> jCheckBoxList, JPanel panel, boolean isSelected)
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
	
	/**
	 * Find relevant checkboxes in lower panel based on input in upper panel
	 * @param regionName - Name of a region in the upper panel
	 * @return - segIndexes - A list of relevant political boundaries by their index
	 */
	private ArrayList<Integer> changeLowerPanelCheckBoxes(boolean checked, final String regionName) {
		ArrayList<Integer> segIndexes = new ArrayList<Integer>();
		for (int i = 0; i < regionTableData.length ; i++)	{	
			if (regionName == regionTableData[i][1]) {
				for(int j = 0; j < politicalBoundarySubPanelLowerTab.getTabCount() ; j++) {
					if(politicalBoundarySubPanelLowerTab.getTitleAt(j) == regionTableData[i][1]) {
						CheckAllTable checkTable = (CheckAllTable) politicalBoundarySubPanelLowerTab.getComponentAt(j);
						JTable table = checkTable.getTable();
						for (int k = 0; k < table.getRowCount(); k++) {
							if (allSubRegionNames.contains(table.getValueAt(k, 1))) {
								segIndexes.add(allSubRegionNames.indexOf(table.getValueAt(k, 1)));
							}
							table.setValueAt(checked, k, 0);
						}
						politicalBoundarySubPanelLowerTab.setSelectedIndex(j);
					}
				}
			}
		}
		return segIndexes;
	}

	TableModelListener listenerUpper = new TableModelListener() {
		@Override
		public void tableChanged(TableModelEvent e) {
			int row = e.getFirstRow();
			int column = e.getColumn();
			if (column == 0) {
				TableModel model = (TableModel) e.getSource();
				String regionName = (String) model.getValueAt(row, column+1);
				Boolean checked = (Boolean) model.getValueAt(row, column);
				ArrayList<Integer> segIndexes = changeLowerPanelCheckBoxes(checked, regionName);
				if (checked) {
					for (int i = 0; i < segIndexes.size(); i++) {
						vtkActor actor = actorPoliticalBoundariesSegments.get(segIndexes.get(i));
						actor.VisibilityOn();
					}
				}
				else {
					for (int i = 0; i < segIndexes.size(); i++) {
						vtkActor actor = actorPoliticalBoundariesSegments.get(segIndexes.get(i));
						actor.VisibilityOff();
					}
				}
				Info.getMainGUI().updateRenderWindow();
			}
		}
	};
	
	TableModelListener listenerLower = new TableModelListener() {
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
	  
	public ArrayList<vtkActor> getPoliticalBoundaries()
	{
		return actorPoliticalBoundariesSegments;
	}
	
	public ArrayList<JCheckBox> getLowerCheckBoxButtons()
	{
		return lowerCheckBoxButtons;
	}
	
	public ArrayList<JCheckBox> getUpperCheckBoxButtons()
	{
		return upperCheckBoxButtons;
	}
	
	public JTabbedPane getPoliticalBoundarySubPanelLowerTab()
	{
		return politicalBoundarySubPanelLowerTab;
	}

	
	
}
