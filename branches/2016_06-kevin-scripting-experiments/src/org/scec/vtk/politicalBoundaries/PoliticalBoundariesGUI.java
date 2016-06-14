package org.scec.vtk.politicalBoundaries;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicTabbedPaneUI.TabbedPaneLayout;
import javax.swing.text.Segment;

import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;

import javafx.scene.control.TabPane;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkGeoAssignCoordinates;
import vtk.vtkGraphMapper;
import vtk.vtkGraphToPolyData;
import vtk.vtkLine;
import vtk.vtkMutableDirectedGraph;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyLine;
import vtk.vtkSphericalTransform;
import vtk.vtkTransformPolyDataFilter;

public class PoliticalBoundariesGUI {
	private JPanel politicalBoundaryMainPanel;
	private JTabbedPane politicalBoundarySubPanelLowerTab;
	private JPanel politicalBoundarySubPanelLower;
	private JPanel politicalBoundarySubPanelUpper;
	//private ArrayList<ArrayList> actorPoliticalBoundariesMain;
	private ArrayList<vtkActor> actorPoliticalBoundariesSegments;
	private ArrayList<JCheckBox> lowerCheckBoxButtons;
	private ArrayList<JCheckBox> upperCheckBoxButtons;
	Dimension dMainPanel,dSubPanel;
	public PoliticalBoundariesGUI(){
		this.politicalBoundaryMainPanel = new JPanel(new GridLayout(0,1));

		this.politicalBoundaryMainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		this.politicalBoundaryMainPanel.setName("Political Boundaries");
		dMainPanel = new Dimension(Prefs.getPluginWidth(),Prefs.getPluginHeight());
		dSubPanel = new Dimension(Prefs.getPluginWidth(),100);
		this.politicalBoundaryMainPanel.setPreferredSize(dMainPanel);
		this.politicalBoundaryMainPanel.setOpaque(false);

		this.politicalBoundarySubPanelLowerTab = new JTabbedPane();

		this.politicalBoundarySubPanelLower=new JPanel();
		//this.politicalBoundarySubPanelLower.setLayout(new BoxLayout(this.politicalBoundarySubPanelLower, BoxLayout.Y_AXIS));



		//this.politicalBoundarySubPanelLower.setPreferredSize(dSubPanel);

		this.politicalBoundarySubPanelUpper=new JPanel();
		this.politicalBoundarySubPanelUpper.setLayout(new BoxLayout(this.politicalBoundarySubPanelUpper, BoxLayout.Y_AXIS));
		//this.politicalBoundarySubPanelUpper.setMinimumSize(dSubPanel);
		//this.actorPoliticalBoundariesMain = new ArrayList<ArrayList>();
		this.actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();

		//loadRegion();
	}

	public JPanel loadAllRegions()
	{
		this.upperCheckBoxButtons = new ArrayList<JCheckBox>();
		this.lowerCheckBoxButtons = new ArrayList<JCheckBox>();

		createCheckBoxes("Africa", this.upperCheckBoxButtons, this.politicalBoundarySubPanelUpper, itemListenerUpper,false);
		createCheckBoxes("Asia", this.upperCheckBoxButtons, this.politicalBoundarySubPanelUpper, itemListenerUpper,false);
		createCheckBoxes("Europe", this.upperCheckBoxButtons, this.politicalBoundarySubPanelUpper, itemListenerUpper,false);
		createCheckBoxes("North America", this.upperCheckBoxButtons, this.politicalBoundarySubPanelUpper, itemListenerUpper,false);
		createCheckBoxes("Oceania", this.upperCheckBoxButtons, this.politicalBoundarySubPanelUpper, itemListenerUpper,false);
		createCheckBoxes("South America", this.upperCheckBoxButtons, this.politicalBoundarySubPanelUpper, itemListenerUpper,false);
		createCheckBoxes("United States", this.upperCheckBoxButtons, this.politicalBoundarySubPanelUpper, itemListenerUpper,true);

		addPanelToMainPanel(this.politicalBoundarySubPanelUpper);

		//default
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"us_complete.txt","United States",true);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"africa.txt","Africa",false);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"asia.txt","Asia",false);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"europe.txt","Europe",false);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"north_america.txt","North America",false);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"oceania.txt","Oceania",false);
		addTabbedPanelToMainPanel(politicalBoundarySubPanelLower,"south_america.txt","South America",false);


		return this.politicalBoundaryMainPanel;

	}



	public void addPanelToMainPanel(JPanel panel)
	{
		this.politicalBoundaryMainPanel.add(panel);
		JScrollPane scrollPane =  addScroller(panel);
		this.politicalBoundaryMainPanel.add(scrollPane);
		//this.politicalBoundaryMainPanel.repaint();
	}

	public void addTabbedPanelToMainPanel(JPanel panel,String filename,String tabname,boolean isSelected)
	{
		panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		loadRegion(filename,this.lowerCheckBoxButtons,panel,itemListenerLower,isSelected);

		//this.politicalBoundarySubPanelLowerTab.addTab(tabname, panel);
		JScrollPane scrollPane =  addScroller(panel);
		this.politicalBoundarySubPanelLowerTab.addTab(tabname,scrollPane);
		this.politicalBoundaryMainPanel.add(this.politicalBoundarySubPanelLowerTab);
	}

	public JScrollPane addScroller(JPanel panel)
	{
		JScrollPane scroller = new JScrollPane(panel);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		//set scroll speed
		JScrollBar bar = scroller.getVerticalScrollBar();
		bar.setBlockIncrement(20);
		bar.setUnitIncrement(20);
		scroller.setVerticalScrollBar(bar);
		scroller.setPreferredSize(dSubPanel);
		return scroller;
	}
	public void loadRegion(String filename,ArrayList<JCheckBox> jCheckBoxList, JPanel panel,ItemListener itemListener,boolean isSelected)
	{

		PoliticalBoundariesRegion newBoundaries = new PoliticalBoundariesRegion(); 
		String usBoundariesPath = this.getClass().getResource("resources/sourcefiles/"+filename).getPath();
		//System.out.println(usBoundariesPath);
		ArrayList<ArrayList> us_boundaries = (ArrayList<ArrayList>) newBoundaries.buildBoundaries(usBoundariesPath);
		//vtkPolyData us_boundaries = (vtkPolyData) newBoundaries.buildBoundaries(this.getClass().getResource("resources/sourcefiles/us.vtk").getPath());
		ArrayList<String> usStateNames = newBoundaries.getUSStateNames();
		vtkLine line0 = new vtkLine();
		//System.out.println(usStateNames.size());
		int countpts = 0;
		for(int j=0;j<us_boundaries.size();j++)
		{
			
			ArrayList<?> us_boundariesState = (ArrayList<?>) us_boundaries.get(j);

			if(isSelected && j==4)
				createCheckBoxes(usStateNames.get(j), jCheckBoxList,panel,itemListener,isSelected);
			else
				createCheckBoxes(usStateNames.get(j), jCheckBoxList,panel,itemListener,false);

			
			vtkDoubleArray latitude = new vtkDoubleArray();
			latitude.SetName("latitude");
			vtkDoubleArray	longitude = new vtkDoubleArray();
			longitude.SetName("longitude");
			vtkPoints boundary = new vtkPoints();
			vtkCellArray lines = new vtkCellArray();
			vtkPolyData linesPolyData = new vtkPolyData();
			countpts = 0;
			for(int k=0;k<us_boundariesState.size();k++)
			{
				//segments
				vtkPoints segmentpoints = (vtkPoints) us_boundariesState.get(k);

				for(int i = 0; i <  segmentpoints.GetNumberOfPoints(); i++)
				{
					double[] pt = segmentpoints.GetPoint(i);
					boundary.InsertNextPoint(Transform.transformLatLon(pt[0],pt[1]));	
				}
				for(int i = 0; i <  segmentpoints.GetNumberOfPoints()-1; i++)
				{
					//connect all edges
					line0.GetPointIds().SetId(0, countpts);
					line0.GetPointIds().SetId(1, countpts+1);
					lines.InsertNextCell(line0);
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
				plyOutActor.VisibilityOn();
			else
				plyOutActor.VisibilityOff();
			actorPoliticalBoundariesSegments.add(plyOutActor);
		}

	}
	private void createCheckBoxes(String checkBoxLabel, ArrayList<JCheckBox> jCheckBoxList, JPanel panel,ItemListener itemListener,boolean isSelected)
	{
		JCheckBox checkBoxButton = new JCheckBox(checkBoxLabel);
		checkBoxButton.addItemListener(itemListener);
		checkBoxButton.setSelected(isSelected);
		jCheckBoxList.add(checkBoxButton);
		panel.add(checkBoxButton);
	}

	ItemListener itemListenerUpper = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
			Object source = e.getItemSelectable();


			if (e.getStateChange() == ItemEvent.DESELECTED){
				for(int i=0;i<upperCheckBoxButtons.size();i++)
				{	
					if (source == upperCheckBoxButtons.get(i)) {
						for(int j= 0;j<politicalBoundarySubPanelLowerTab.getTabCount();j++)
						{
							if(politicalBoundarySubPanelLowerTab.getTitleAt(j) == upperCheckBoxButtons.get(i).getText())
							{

								JScrollPane sp = (JScrollPane)  politicalBoundarySubPanelLowerTab.getComponentAt(j);
								JViewport vp = (JViewport) sp.getComponent(0);
								JPanel p = (JPanel) vp.getComponent(0);

								for(int k=0;k<p.getComponentCount();k++)
								{	
									if(lowerCheckBoxButtons.contains(p.getComponent(k)))
									{
										int segIndex = lowerCheckBoxButtons.indexOf(p.getComponent(k));
										vtkActor actor = actorPoliticalBoundariesSegments.get(segIndex);
										actor.VisibilityOff();
										lowerCheckBoxButtons.get(segIndex).setSelected(false);
									}
								}
								//politicalBoundarySubPanelLowerTab.(j);
								politicalBoundarySubPanelLowerTab.setSelectedIndex(j);
							}
						}
						//Info.getMainGUI().updatePoliticalBoundaries();
						Info.getMainGUI().updateRenderWindow();
					}
				}

			}
			else
			{
				for(int i=0;i<upperCheckBoxButtons.size();i++)
				{	
					if (source == upperCheckBoxButtons.get(i)) {
						for(int j= 0;j<politicalBoundarySubPanelLowerTab.getTabCount();j++)
						{
							if(politicalBoundarySubPanelLowerTab.getTitleAt(j) == upperCheckBoxButtons.get(i).getText())
							{

								JScrollPane sp = (JScrollPane)  politicalBoundarySubPanelLowerTab.getComponentAt(j);
								JViewport vp = (JViewport) sp.getComponent(0);
								JPanel p = (JPanel) vp.getComponent(0);

								for(int k=0;k<p.getComponentCount();k++)
								{	
									if(lowerCheckBoxButtons.contains(p.getComponent(k)))
									{
										int segIndex = lowerCheckBoxButtons.indexOf(p.getComponent(k));
										vtkActor actor = actorPoliticalBoundariesSegments.get(segIndex);
										actor.VisibilityOn();
										lowerCheckBoxButtons.get(segIndex).setSelected(true);
									}
								}
								politicalBoundarySubPanelLowerTab.setSelectedIndex(j);
								//politicalBoundarySubPanelLowerTab.addTab(politicalBoundarySubPanelLowerTab.getTitleAt(j), sp);

							}
						}
						//Info.getMainGUI().updatePoliticalBoundaries();
						Info.getMainGUI().updateRenderWindow();
					}
				}


			}
		}
	};

	ItemListener itemListenerLower = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
			Object source = e.getItemSelectable();


			if (e.getStateChange() == ItemEvent.DESELECTED){
				for(int i=0;i<lowerCheckBoxButtons.size();i++)
				{	
					vtkActor actor = actorPoliticalBoundariesSegments.get(i);

					if (source == lowerCheckBoxButtons.get(i)) {
						Info.getMainGUI().updateRenderWindow();
						actor.VisibilityOff();

						break;
					}
				}
			}
			else
			{
				for(int i=0;i<lowerCheckBoxButtons.size();i++)
				{	
					vtkActor actor = actorPoliticalBoundariesSegments.get(i);

					if (source == lowerCheckBoxButtons.get(i)) {
						//...make a note of it...
						actor.VisibilityOn();
						Info.getMainGUI().updateRenderWindow();
						break;
					}
				}
			}
		}
	};

	public void displayCheckboxPanel()
	{

	}
	public ArrayList<vtkActor> getPoliticalBoundaries()
	{
		return actorPoliticalBoundariesSegments;
	}
}
