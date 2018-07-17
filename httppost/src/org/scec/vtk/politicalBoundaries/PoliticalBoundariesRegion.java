package org.scec.vtk.politicalBoundaries;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.scec.vtk.main.MainGUI;

import jdk.nashorn.api.scripting.URLReader;
import vtk.vtkCellArray;
import vtk.vtkDataReader;
import vtk.vtkGenericDataObjectReader;
import vtk.vtkPoints;
import vtk.vtkPolyData;

public class PoliticalBoundariesRegion  {
		vtkPoints currentBoundary = new vtkPoints();
		vtkCellArray vtkVertices = new vtkCellArray();
		Color traceColor1 = new Color(1.0f,1.0f,1.0f); // white
		ArrayList<String> usStateNames = new ArrayList<>();
		
		/*Complete Highway Source File. Segments are not known to be in a particular order, other 
		 * than all belonging to one highway
		 * File format: "segment" [highway name]
		 * 				 latitude coordinate, longitude coordinate
		 * 				 latitude coordinate, longitude coordinate etc.
		 * Note that a blank line will cause everything after it in the file to be ignored(!)
		 */
			private int groupCount = 0;
			private int numBounds = 0;
			public String[] names;
			public int[] groupSize;
			ArrayList allBounds,indSegments;
			
			
			/*public vtkPolyData buildBoundaries(String file) 
			{
				allBounds = null;
				allBounds = new ArrayList();
				indSegments = new ArrayList();
				usStateNames = new ArrayList<String>();
				
//				URL filename = PoliticalBoundary.class.getResource(file);
				File filename = new File(file);//"PoliticalBoundariesPlugin" + File.separator +file);
				ArrayList<Double> lat= new ArrayList<Double>(), lg= new ArrayList<Double>();
				String temp[] = new String[2];
					
				String nameOfSegment=""; 
		        
				
					vtkGenericDataObjectReader reader = new vtkGenericDataObjectReader();
					reader.SetFileName(filename.getAbsolutePath());
					reader.Update();
					vtkPolyData pd = new vtkPolyData();
					pd = reader.GetPolyDataOutput();
					//vtkPoints pts = 
				return pd;
				
			}*/
			
		public ArrayList buildBoundaries(String usBoundariesPath) {
			allBounds = null;
			allBounds = new ArrayList();
			indSegments = new ArrayList();
			usStateNames = new ArrayList<String>();
			
//			URL filename = PoliticalBoundary.class.getResource(file);
//			File filename = new File(file);//"PoliticalBoundariesPlugin" + File.separator +file);
			ArrayList<Double> lat= new ArrayList<Double>(), lg= new ArrayList<Double>();
			String temp[] = new String[2];
				
			String nameOfSegment=""; 
	        
			try {
				BufferedReader inStream =
//					new BufferedReader(new FileReader(filename));
					new BufferedReader(new FileReader(usBoundariesPath));
				String line = inStream.readLine();
				line = inStream.readLine();
				StringTokenizer dataLine = new StringTokenizer(line);
				temp[0] = dataLine.nextToken();	
				temp[1] = dataLine.nextToken();
				/*process first line */
				if (temp[0].equals("segment")){
					if(temp[1]!=null){
						nameOfSegment= temp[1];
						usStateNames.add(nameOfSegment);
					}
					else
						System.out.println("first boundary name missing");
				}
				else
					System.out.println("File does not start with \"segment\", see expected format");
				/* finished with first line */
				line = inStream.readLine();
				
				while (line!=null){
					dataLine = new StringTokenizer(line);
					temp[0] = dataLine.nextToken();	temp[1] = dataLine.nextToken();		
					//Upon reaching "segment", add to the current highway all the coordinates just
					//read in and check to see if the next segment belongs to a new highway
					if (temp[0].equals("segment"))
					{
						if (lat.size()>0) {		//i.e. if there is data to submit		
							addBoundarySegment(lat.size(), lat, lg);	
							lat.clear();
							lg.clear();
						}
						else 
						{
							System.out.println("segment contains no coordinates");		
						}
						if( !nameOfSegment.equals(temp[1]) )
						{ //i.e. if the next segment is in a different highway
							
							//currentBoundary.setName(nameOfSegment);
							allBounds.add(indSegments);
							numBounds++;
							nameOfSegment = temp[1];
							usStateNames.add(nameOfSegment);
							indSegments = new ArrayList();			
						}	
					}
					else{ 		//if the first token is not "segment", it must be data.
						lat.add(Double.parseDouble(temp[0]));
						lg.add(Double.parseDouble(temp[1]));
					}	
					line = inStream.readLine();
				}
				/*Process the last segment*/					
				if (lat.size()>0)						
					addBoundarySegment(lat.size(), lat, lg );	
				else 
					System.out.println("0 vertices found");		
				numBounds++;
			}		
			catch (Exception e) {
				System.out.println(e.getMessage());//prints "null" to console
			}
			allBounds.add(indSegments);
			numBounds++;
			return allBounds;
		}
		
		/**Adapted from display method in caTrace.java
		 * Takes in Vectors for latitude and longitude points
		 * Converts each segment's set of points into the requisite 3D objects.
		 * 
		 * @param vertices number of x,y points
		 * @param x set of all x points Latitude
		 * @param y set of all y points Longitude
		 */
		public static double PIBy2 = Math.PI /2;
		private void addBoundarySegment(int vertices, ArrayList<Double> x, ArrayList<Double> y) {
			
			float Latitude[]= new float[x.size()];
			float Longitude[]= new float[y.size()];
			currentBoundary = new vtkPoints();
			for(int i=0; i<x.size();i++)
			{
				Latitude[i]= x.get(i).floatValue();
				Longitude[i]= y.get(i).floatValue();
				
				 double[] latlon = new double[3];

	             //radiusAtPoint 	= calcRadius(latitude) + altitude;
	             latlon[0] = Latitude[i];//MainGUI.calcRadius(Latitude[i]);
	             // Phi= deg2rad(latitude);
	             latlon[1] = Longitude[i];//Math.toRadians(Latitude[i]);
	             //Theta= deg2rad(longitude);
	             latlon[2] = 0.0;//Math.toRadians(Longitude[i]);
	             //INVERT IMAGE
	             //latlon[2] = -1 * latlon[2];
	     		//END INVERT IMAGE
	             

	             double[] vertex =latlon;
				currentBoundary.InsertNextPoint(vertex);
			}
			//currentBoundary.addSegment(Latitude,Longitude);
			indSegments.add(currentBoundary);
		}
		public ArrayList<String> getUSStateNames()
		{
			return usStateNames;
		}
}

