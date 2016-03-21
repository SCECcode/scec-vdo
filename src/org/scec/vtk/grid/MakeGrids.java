package org.scec.vtk.grid;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.log4j.chainsaw.Main;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.FaultAccessor;
import org.scec.vtk.tools.Transform;

import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkGeoAssignCoordinates;
import vtk.vtkGeoGraticule;
import vtk.vtkGraphToPolyData;
import vtk.vtkLine;
import vtk.vtkMutableDirectedGraph;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphericalTransform;
import vtk.vtkTransformPolyDataFilter;

public class MakeGrids {

	public MakeGrids(){
		
	}
	public ArrayList<GlobeBox> getGlobeBox(){
		URL calGridURL = MakeGrids.class.getResource("resources/California.grat");
		File calGrid = new File(calGridURL.getPath());
		GraticulePreset graticule = new GraticulePreset(calGrid);
		int upperLat, lowerLat, upperLon, lowerLon;
		upperLat = (graticule.getUpperLatitude());
		lowerLat = (graticule.getLowerLatitude());
		upperLon = (graticule.getLeftLongitude());
		lowerLon = (graticule.getRightLongitude());
		
		ArrayList<GlobeBox> gbs = makeNewGrid(upperLat, lowerLat,  upperLon, lowerLon,1.0); //double spacing)
		return gbs;
	}
	 private ArrayList<GlobeBox> makeGrids(int upperLat, int lowerLat,
	  				int leftLong, int rightLong, double spacing) {

				  //for latitude lines
		
		 
				  vtkPoints allPoints = new vtkPoints();
				  int countPts =0;
				  vtkCellArray lines =  new vtkCellArray();
				  vtkPolyData linesPolyData =new vtkPolyData();
				  
				  vtkDoubleArray lat = new vtkDoubleArray();
				  vtkDoubleArray lon = new vtkDoubleArray();
				  vtkDoubleArray gridName = new vtkDoubleArray();
				  lat.SetName("latitude");
				  lon.SetName("longitude");
				  vtkMutableDirectedGraph graph1 = new vtkMutableDirectedGraph();
				  vtkMutableDirectedGraph graph2 = new vtkMutableDirectedGraph();
						  //j-- is spacing 
				//INVERT IMAGE//
					double leftLon  = 1 * rightLong;
					double rightLon = 1 * leftLong; 
					//END IMAGE INVERT//
				  for(double j = upperLat;j>=lowerLat;j--,countPts+=2)
				  {
					  
					  //double[] pt = new double[3];
					  	//pt[0] = Transform.calcRadius(j);
				         // Phi= deg2rad(latitude);
						//pt[1] = (j);
				         //Theta= deg2rad(longitude);
						//pt[2] = (leftLon);
						
						graph1.AddVertex();
						lat.InsertNextValue(j);
						lon.InsertNextValue( leftLon);
						//allPoints.InsertNextPoint(Transform.customTransform(pt));
						
				         //Theta= deg2rad(longitude);
						//pt[2] = (rightLon);
						graph1.AddVertex();
						lat.InsertNextValue( j);
						lon.InsertNextValue( rightLon);
						/*allPoints.InsertNextPoint(Transform.customTransform(pt));
						vtkLine line0 = new vtkLine();
						line0.GetPointIds().SetId(0, countPts); // the second 0 is the index of the Origin in linesPolyData's points
						line0.GetPointIds().SetId(1, countPts+1); // the second 1 is the index of P0 in linesPolyData's points
						lines.InsertNextCell(line0);*/
						graph1.AddGraphEdge(countPts, countPts+1);
						gridName.InsertNextValue(j);
				  }
				  //longitutde lines
				  for(double j = leftLon;j>=rightLon;j--,countPts+=2)
				  {
					  /*double[] pt = new double[3];
					  	pt[0] = Transform.calcRadius(upperLat);
				         // Phi= deg2rad(latitude);
						pt[1] = (upperLat);
				         //Theta= deg2rad(longitude);
						pt[2] = (j);
						
						allPoints.InsertNextPoint(Transform.customTransform(pt));*/
					  	graph1.AddVertex();
						lat.InsertNextValue( upperLat);
						lon.InsertNextValue( j);
						
						graph1.AddVertex();
						lat.InsertNextValue( lowerLat);
						lon.InsertNextValue( j);
				         //Theta= deg2rad(longitude);
						/*pt[0] = Transform.calcRadius(lowerLat);
				         // Phi= deg2rad(latitude);
						pt[1] = (lowerLat);
						
						allPoints.InsertNextPoint(Transform.customTransform(pt));
						
						vtkLine line0 = new vtkLine();
						line0.GetPointIds().SetId(0, countPts); // the second 0 is the index of the Origin in linesPolyData's points
						line0.GetPointIds().SetId(1, countPts+1); // the second 1 is the index of P0 in linesPolyData's points
						lines.InsertNextCell(line0);*/
						graph1.AddGraphEdge(countPts, countPts+1);
				  }
				  //linesPolyData.SetPoints(allPoints);
				  // linesPolyData.SetLines(lines);
				  graph1.GetVertexData().AddArray(lat);
			        graph1.GetVertexData().AddArray(lon);
			        
			        vtkGraphToPolyData graphToPolyData = new vtkGraphToPolyData();
					graphToPolyData.SetInputData(graph1);
					
					graphToPolyData.Update();
					
					linesPolyData = graphToPolyData.GetOutput();
					
					vtkGeoAssignCoordinates assign = new vtkGeoAssignCoordinates();

					assign.SetInputData(linesPolyData);
					//assign.set
					assign.SetLatitudeArrayName("latitude");
					assign.SetLongitudeArrayName("longitude");
					assign.SetGlobeRadius(Transform.re);
					
					assign.Update();
					
				   vtkPolyDataMapper globeMapper = new vtkPolyDataMapper();
				
					/*vtkSphericalTransform vts= new vtkSphericalTransform();
					vtkTransformPolyDataFilter tpoly21 = new vtkTransformPolyDataFilter();
					tpoly21.SetTransform(vts);
			 		tpoly21.SetInputData(linesPolyData);*/
			 			
					globeMapper.SetInputConnection(assign.GetOutputPort());
				  
				  
	  			ArrayList<GlobeBox> gbs = new ArrayList<GlobeBox>(4);
	  			Color tempColor3f = new Color(1,1,1);
	  			GlobeLayout gl = new GlobeLayout(upperLat, lowerLat, leftLong, rightLong, spacing);
	  			gbs.add(new GlobeBox(gl, tempColor3f, true));
	  			gbs.get(0).globeScene = globeMapper;
	  			//gbs.add(new GlobeBox(tg, gl, tempColor3f, latLonLabelsCheckBox.isSelected()));
	  			/*GlobeLayout g2 = new GlobeLayout(upperLat, lowerLat, leftLong, rightLong, spacing * 2);
	  			gbs.add(new GlobeBox(g2, tempColor3f, true));
	  			gbs.get(1).drawGlobe();
	  			//gbs.add(new GlobeBox(tg, g2, tempColor3f, latLonLabelsCheckBox.isSelected()));
	  			GlobeLayout g3 = new GlobeLayout(upperLat, lowerLat, leftLong, rightLong, spacing * 4);
	  			gbs.add(new GlobeBox(g3, tempColor3f, true));
	  			gbs.get(2).drawGlobe();
	  			//gbs.add(new GlobeBox(tg, g3, tempColor3f, latLonLabelsCheckBox.isSelected()));
	  			GlobeLayout g4 = new GlobeLayout(upperLat, lowerLat, leftLong, rightLong, spacing * 6);
	  			gbs.add(new GlobeBox(g4, tempColor3f, true));
	  			gbs.get(3).drawGlobe();*/
	  			//gbs.add(new GlobeBox(tg, g4, tempColor3f, latLonLabelsCheckBox.isSelected()));
	  			return gbs;
	  		}
		  
		  protected ArrayList<GlobeBox> makeNewGrid(int upperLat, int lowerLat, int leftLong,
					int rightLong, double spacing) {
				//globeView = Geo3dInfo.getMainWindow();
				//if (!globeView.getGridDisplayBool())
					//globeView.toggleGridDisplay();
				ArrayList<GlobeBox> gbs = makeGrids(upperLat, lowerLat, leftLong, rightLong,
						spacing);
				// globeView.setGlobeBox(gbs[0]);
				return gbs;
				/*globeView.getSwitchNode().removeAllChildren();
				globeView.getSwitchNode().addChild(( gbs.get(0)).drawGlobe());
				globeView.getSwitchNode().addChild(( gbs.get(1)).drawGlobe());
				globeView.getSwitchNode().addChild(( gbs.get(2)).drawGlobe());
				globeView.getSwitchNode().addChild(( gbs.get(3)).drawGlobe());*/
			}
}
