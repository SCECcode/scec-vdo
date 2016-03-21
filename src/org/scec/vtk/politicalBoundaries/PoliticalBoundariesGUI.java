package org.scec.vtk.politicalBoundaries;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.Segment;

import org.scec.vtk.main.MainGUI;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkGeoAssignCoordinates;
import vtk.vtkGraphMapper;
import vtk.vtkGraphToPolyData;
import vtk.vtkMutableDirectedGraph;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyLine;
import vtk.vtkSphericalTransform;
import vtk.vtkTransformPolyDataFilter;

public class PoliticalBoundariesGUI {
	private JPanel politicalBoundaryMainPanel;
	private JPanel politicalBoundarySubPanel;
	private ArrayList<ArrayList> actorPoliticalBoundariesMain;
	private ArrayList<vtkActor> actorPoliticalBoundariesSegments;
	private ArrayList<JCheckBox> usCheckBoxButtons;
	Dimension d;
	public PoliticalBoundariesGUI(){
		this.politicalBoundaryMainPanel = new JPanel(new BorderLayout());
		
		//this.politicalBoundaryMainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		this.politicalBoundaryMainPanel.setName("Political Boundaries");
		d = new Dimension(Prefs.getPluginWidth(),Prefs.getPluginHeight());
		this.politicalBoundaryMainPanel.setPreferredSize(d);
		this.politicalBoundaryMainPanel.setOpaque(false);
		this.politicalBoundarySubPanel=new JPanel();
		this.politicalBoundarySubPanel.setLayout(new BoxLayout(this.politicalBoundarySubPanel, BoxLayout.Y_AXIS));
        
		this.actorPoliticalBoundariesMain = new ArrayList<ArrayList>();
		this.actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
		
		//loadRegion();
	}
	public JPanel loadRegion()
	{
		
		this.usCheckBoxButtons = new ArrayList<JCheckBox>();
		
		PoliticalBoundariesRegion newBoundaries = new PoliticalBoundariesRegion(); 
		String usBoundariesPath = this.getClass().getResource("resources/sourcefiles/us_complete.txt").getPath();
		ArrayList us_boundaries = (ArrayList<ArrayList>) newBoundaries.buildBoundaries(usBoundariesPath);
		//vtkPolyData us_boundaries = (vtkPolyData) newBoundaries.buildBoundaries(this.getClass().getResource("resources/sourcefiles/us.vtk").getPath());
		ArrayList<String> usStateNames = newBoundaries.getUSStateNames();
		
		
		
	////us_boundaries.size()
				/*for(int j=0;j<us_boundaries.size();j++)
				{
					//state
					vtkCellArray  cellsPolyLine= new vtkCellArray();
					vtkPolyData linesPolyData = new vtkPolyData();
					vtkPoints segmentpointsAll = new vtkPoints();
					ArrayList us_boundariesState = (ArrayList) us_boundaries.get(j);
					vtkGeoAssignCoordinates assign = new vtkGeoAssignCoordinates();
					vtkDoubleArray latitude = new vtkDoubleArray();
					latitude.SetName("latitude");
					vtkDoubleArray	longitude = new vtkDoubleArray();
					longitude.SetName("longitude");
					
					
					createCheckBoxes(usStateNames.get(j));
					int countpts = 0;
					for(int k=0;k<us_boundariesState.size();k++)
					{
						//segments
						vtkPoints segmentpoints = (vtkPoints) us_boundariesState.get(k);
						// Create the polydata where we will store all the geometric data
						
						vtkPolyLine polyLine = new vtkPolyLine();
						polyLine.GetPointIds().SetNumberOfIds(segmentpoints.GetNumberOfPoints());
						//int s = segmentpoints.GetNumberOfPoints();
						for(int i = 0; i <  segmentpoints.GetNumberOfPoints(); i++)
						{
							polyLine.GetPointIds().SetId(i,countpts);
							
							
							double[] pt = segmentpoints.GetPoint(i);
							latitude.InsertNextValue(pt[0]);
					        longitude.InsertNextValue(pt[1]);	
					        
					        segmentpointsAll.InsertNextPoint(segmentpoints.GetPoint(i));
					        countpts++;
						}
						//countpts = countpts + segmentpoints.GetNumberOfPoints()-1;
						cellsPolyLine.InsertNextCell(polyLine);
						//actorBoundary.GetProperty().SetPointSize(20);
					}
					linesPolyData.SetPoints(segmentpointsAll);
					linesPolyData.SetLines(cellsPolyLine);
					
					assign.SetInputData(linesPolyData);
					//assign.set
					assign.SetLatitudeArrayName("latitude");
					assign.SetLongitudeArrayName("longitude");
					
					assign.SetGlobeRadius(Transform.re);
					
					assign.Update();
					 
					
					
					vtkPolyDataMapper mapper = new vtkPolyDataMapper();
					mapper.SetInputConnection(assign.GetOutputPort());
					
					//mapper.SetVertexPointSize(0);
					//mapper.SetEdgeLineWidth(2);
					//mapper.EdgeVisibilityOn();
					
					vtkActor plyOutActor = new vtkActor();
					plyOutActor.SetMapper(mapper);
					//plyOutActor.GetProperty().SetRepresentationToWireframe();
					actorPoliticalBoundariesSegments.add(plyOutActor);
					
					//actorPoliticalBoundariesMain.add(actorPoliticalBoundariesSegments);
				}*/
		/*segmentpoints = us_boundaries.GetPoints();
		
		//vtkPolyData us_boundariesNew = new vtkPolyData();
		for(int i = 0; i <  segmentpoints.GetNumberOfPoints(); i++)
		{
			double[] pt = segmentpoints.GetPoint(i);
			latitude.InsertNextValue(pt[1]);
	        longitude.InsertNextValue(pt[0]);	
	        
		}
		
		assign.SetInputDataObject(us_boundaries);
		//assign.set
		assign.SetLatitudeArrayName("latitude");
		assign.SetLongitudeArrayName("longitude");
		
		assign.SetGlobeRadius(Transform.re);
		
		assign.Update();
		 
		
		
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.SetInputConnection(assign.GetOutputPort());
		
		//mapper.SetVertexPointSize(0);
		//mapper.SetEdgeLineWidth(2);
		//mapper.EdgeVisibilityOn();
		
		vtkActor plyOutActor = new vtkActor();
		plyOutActor.SetMapper(mapper);
		plyOutActor.GetProperty().SetRepresentationToWireframe();
		actorPoliticalBoundariesSegments.add(plyOutActor);*/
		/*for(int i = 0; i <  segmentpoints.GetNumberOfPoints(); i++)
		{
			//connect all edges
			g.AddGraphEdge(countpts, (countpts+1));
			countpts=countpts+1;
		}*/
			////us_boundaries.size()
			/*for(int j=0;j<us_boundaries.size();j++)
			{
				//state
				vtkCellArray  cellsPolyLine= new vtkCellArray();
				vtkPolyData linesPolyData = new vtkPolyData();
				vtkPoints segmentpointsAll = new vtkPoints();
				ArrayList us_boundariesState = (ArrayList) us_boundaries.get(j);
				
				createCheckBoxes(usStateNames.get(j));
				int countpts = 0;
				for(int k=0;k<us_boundariesState.size();k++)
				{
					//segments
					vtkPoints segmentpoints = (vtkPoints) us_boundariesState.get(k);
					// Create the polydata where we will store all the geometric data
					
					vtkPolyLine polyLine = new vtkPolyLine();
					polyLine.GetPointIds().SetNumberOfIds(segmentpoints.GetNumberOfPoints());
					//int s = segmentpoints.GetNumberOfPoints();
					for(int i = 0; i <  segmentpoints.GetNumberOfPoints(); i++)
					{
						polyLine.GetPointIds().SetId(i,countpts);
						segmentpointsAll.InsertNextPoint(segmentpoints.GetPoint(i));
						countpts++;
						
					}
					//countpts = countpts + segmentpoints.GetNumberOfPoints()-1;
					cellsPolyLine.InsertNextCell(polyLine);
					//actorBoundary.GetProperty().SetPointSize(20);
				}
				linesPolyData.SetPoints(segmentpointsAll);
				linesPolyData.SetLines(cellsPolyLine);
				vtkSphericalTransform vts= new vtkSphericalTransform();

				
				vtkTransformPolyDataFilter tpoly2 = new vtkTransformPolyDataFilter();
				tpoly2.SetInputData(linesPolyData);
				tpoly2.SetTransform(vts);


				vtkPolyDataMapper mapperBoundary = new vtkPolyDataMapper();
				mapperBoundary.SetInputConnection(tpoly2.GetOutputPort());
				vtkActor actorBoundary =new vtkActor();
				actorBoundary.SetMapper(mapperBoundary);
				actorBoundary.GetProperty().SetColor(1,0,1);
				actorBoundary.VisibilityOff();
				actorPoliticalBoundariesSegments.add(actorBoundary);
				
				//actorPoliticalBoundariesMain.add(actorPoliticalBoundariesSegments);
			}
		////us_boundaries.size()
		/*for(int j=0;j<us_boundaries.size();j++)
		{
			//state
			vtkCellArray  cellsPolyLine= new vtkCellArray();
			vtkPolyData linesPolyData = new vtkPolyData();
			vtkPoints segmentpointsAll = new vtkPoints();
			ArrayList us_boundariesState = (ArrayList) us_boundaries.get(j);
			
			createCheckBoxes(usStateNames.get(j));
			int countpts = 0;
			for(int k=0;k<us_boundariesState.size();k++)
			{
				//segments
				vtkPoints segmentpoints = (vtkPoints) us_boundariesState.get(k);
				// Create the polydata where we will store all the geometric data
				
				vtkPolyLine polyLine = new vtkPolyLine();
				polyLine.GetPointIds().SetNumberOfIds(segmentpoints.GetNumberOfPoints());
				//int s = segmentpoints.GetNumberOfPoints();
				for(int i = 0; i <  segmentpoints.GetNumberOfPoints(); i++)
				{
					polyLine.GetPointIds().SetId(i,countpts);
					segmentpointsAll.InsertNextPoint(segmentpoints.GetPoint(i));
					countpts++;
					
				}
				//countpts = countpts + segmentpoints.GetNumberOfPoints()-1;
				cellsPolyLine.InsertNextCell(polyLine);
				//actorBoundary.GetProperty().SetPointSize(20);
			}
			linesPolyData.SetPoints(segmentpointsAll);
			linesPolyData.SetLines(cellsPolyLine);
			vtkSphericalTransform vts= new vtkSphericalTransform();

			
			vtkTransformPolyDataFilter tpoly2 = new vtkTransformPolyDataFilter();
			tpoly2.SetInputData(linesPolyData);
			tpoly2.SetTransform(vts);


			vtkPolyDataMapper mapperBoundary = new vtkPolyDataMapper();
			mapperBoundary.SetInputConnection(tpoly2.GetOutputPort());
			vtkActor actorBoundary =new vtkActor();
			actorBoundary.SetMapper(mapperBoundary);
			actorBoundary.GetProperty().SetColor(1,0,1);
			actorBoundary.VisibilityOff();
			actorPoliticalBoundariesSegments.add(actorBoundary);
			
			//actorPoliticalBoundariesMain.add(actorPoliticalBoundariesSegments);
		}
		//add subPanel to main Panel
		*/
		int countpts = 0;
		for(int j=0;j<us_boundaries.size();j++)
		{
			//state
			vtkPoints segmentpointsAll = new vtkPoints();
			ArrayList us_boundariesState = (ArrayList) us_boundaries.get(j);
			
			createCheckBoxes(usStateNames.get(j));
			
			vtkMutableDirectedGraph g = new vtkMutableDirectedGraph();
			vtkGeoAssignCoordinates assign = new vtkGeoAssignCoordinates();
			vtkDoubleArray latitude = new vtkDoubleArray();
			latitude.SetName("latitude");
			vtkDoubleArray	longitude = new vtkDoubleArray();
			longitude.SetName("longitude");
			countpts = 0;
			for(int k=0;k<us_boundariesState.size();k++)
			{
				//segments
				vtkPoints segmentpoints = (vtkPoints) us_boundariesState.get(k);

				for(int i = 0; i <  segmentpoints.GetNumberOfPoints(); i++)
				{
					g.AddVertex();
					double[] pt = segmentpoints.GetPoint(i);
					latitude.InsertNextValue(pt[0]);
			        longitude.InsertNextValue(pt[1]);	
				}
				for(int i = 0; i <  segmentpoints.GetNumberOfPoints(); i++)
				{
					//connect all edges
					g.AddGraphEdge(countpts, (countpts+1));
					countpts=countpts+1;
				}
			}
			
			g.GetVertexData().AddArray(latitude);
			g.GetVertexData().AddArray(longitude);
		
			vtkGraphToPolyData graphToPolyData = new vtkGraphToPolyData();
			graphToPolyData.SetInputData(g);
			graphToPolyData.Update();
				  
		    vtkPolyData pd = graphToPolyData.GetOutput();
			
			assign.SetInputData(pd);
			//assign.set
			assign.SetLatitudeArrayName("latitude");
			assign.SetLongitudeArrayName("longitude");
			
			assign.SetGlobeRadius(Transform.re);
			
			assign.Update();
			 
			
			vtkPolyDataMapper mapper = new vtkPolyDataMapper();
			mapper.SetInputConnection(assign.GetOutputPort());
			
			vtkActor plyOutActor = new vtkActor();
			plyOutActor.SetMapper(mapper);
			actorPoliticalBoundariesSegments.add(plyOutActor);
		}
		//actorPoliticalBoundariesSegments.add(globe);
		//add subPanel to main Panel
		//politicalBoundaryMainPanel.add(politicalBoundarySubPanel);
		  
		politicalBoundaryMainPanel.add(politicalBoundarySubPanel);
		JScrollPane scroller = new JScrollPane(this.politicalBoundarySubPanel);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        //set scroll speed
        JScrollBar bar = scroller.getVerticalScrollBar();
        bar.setBlockIncrement(20);
        bar.setUnitIncrement(20);
        scroller.setVerticalScrollBar(bar);
        scroller.setMinimumSize(d);
		 this.politicalBoundaryMainPanel.add(scroller);
		return this.politicalBoundaryMainPanel;
	}
	private void createCheckBoxes(String checkBoxLabel)
	{
		JCheckBox checkBoxButton = new JCheckBox(checkBoxLabel);
		checkBoxButton.addItemListener(itemListener);
		checkBoxButton.setSelected(true);
		this.usCheckBoxButtons.add(checkBoxButton);
		this.politicalBoundarySubPanel.add(checkBoxButton);
	}
	
	ItemListener itemListener = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    Object source = e.getItemSelectable();
		    
		
		    if (e.getStateChange() == ItemEvent.DESELECTED){
		    for(int i=0;i<usCheckBoxButtons.size();i++)
		    {	
		    	vtkActor actor =	actorPoliticalBoundariesSegments.get(i);
		    	
		    	if (source == usCheckBoxButtons.get(i)) {
		    		MainGUI.updateRenderWindow(actor);
		    		actor.VisibilityOff();
			
		    		break;
		    	}
		    	}
		    }
		    else
		    {
		        for(int i=0;i<usCheckBoxButtons.size();i++)
			    {	
			    	vtkActor actor =	actorPoliticalBoundariesSegments.get(i);
			    	
			    	if (source == usCheckBoxButtons.get(i)) {
			        //...make a note of it...
			    		actor.VisibilityOn();

			    		MainGUI.updateRenderWindow(actor);
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
