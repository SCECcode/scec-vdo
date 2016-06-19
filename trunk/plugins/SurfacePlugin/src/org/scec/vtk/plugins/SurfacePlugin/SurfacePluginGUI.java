package org.scec.vtk.plugins.SurfacePlugin;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JPanel;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.SurfacePlugin.Component.LoadedFilesProperties;
import org.scec.vtk.tools.Transform;

import com.google.common.base.Throwables;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkFloatArray;
import vtk.vtkJPEGReader;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkTexture;
import vtk.vtkTriangleStrip;

public class SurfacePluginGUI {

	private ImagePluginGUI ipg;
	private JPanel allPanel = new JPanel();
	private double scaleFactor;
	private ArrayList<double[]> data;
	private Vector<LoadedFilesProperties> displayedImageSurfaceVector = new Vector<LoadedFilesProperties>();
	protected Vector<String> displayedImageInfoVector = new Vector<String>();
	protected Vector<String> displayedSurfaceInfoVector = new Vector<String>();
	private int longIncrements, latIncrements, horizontalItems;
	private double w, e, n, s;
	private double altitude;
	private String filename;
	private BufferedReader demReader;
	
	private PluginActors surfaceActors;

	public SurfacePluginGUI(PluginActors surfaceActors){
		this.surfaceActors = surfaceActors;
		//ip = new ImagePlugin(this);
		ipg = new ImagePluginGUI(this);

		allPanel.setLayout(new GridLayout(2,1,6,6));
		allPanel.add(ipg.getPanel());	
	}


	public JPanel getPanel() {
		// TODO Auto-generated method stub
		return allPanel;
	}


	public void setScaleFactor(double newScale) {
		// TODO Auto-generated method stub
		scaleFactor = newScale;
	}

	private void loadData(GeographicSurfaceInfo si) throws IOException {
		filename = si.getFilename();
		double[] ul = si.getUpperRight();
		n = ul[0];
		w = ul[1];
		double[] lr = si.getLowerLeft();
		s = lr[0];
		e = lr[1];
		altitude = lr[2];
		demReader = new BufferedReader(new FileReader(filename)); //This file needs to be in matrix form
		String line = demReader.readLine();
		StringTokenizer dataLine = new StringTokenizer(line);
		//use the first line to count horizontal increments	
		horizontalItems = dataLine.countTokens();
		longIncrements = horizontalItems - 1;
		double[] lineHeights;
		int j;
		//this takes all the z values and puts them into an ArrayList of double arrays,
		//giving a matrix of heights
		while(line!=null) {
			dataLine = new StringTokenizer(line);
			lineHeights = new double[horizontalItems]; //the z values
			j = 0;
			while(dataLine.hasMoreTokens()) {
				lineHeights[j] = scaleFactor * Double.parseDouble(dataLine.nextToken());
				j++;
			}
			data.add(lineHeights);
			line = demReader.readLine();
		}
		demReader.close();
	}
	private vtkPolyData createSurface(GeographicSurfaceInfo si) {
		//create surface mesh
		latIncrements = data.size()-1;
		si.setVertSteps(latIncrements);
		si.setHorizSteps(longIncrements);

		int numVertices = latIncrements*(2*longIncrements+2);

		vtkFloatArray textureCoordinates =new vtkFloatArray();
		textureCoordinates.SetNumberOfComponents(2);
		textureCoordinates.SetNumberOfTuples(numVertices);
		textureCoordinates.SetName("TextureCoordinates");

		vtkPoints pts = new vtkPoints();
		double latStep = (n-s)/(double)latIncrements;
		double longStep = (e-w)/(double)longIncrements;
		double[] firstLineOfData = new double[horizontalItems];
		double[] secondLineOfData = new double[horizontalItems];
		double lon,lat;int pointIndex=0,stripIndex=0;

		vtkCellArray cells = new vtkCellArray();
		for (int i=0; i<latIncrements; i++) {
			//we fill the Triangle strip array by going bottom, top, bottom, top, so we need z values
			//from two different latitudes.  Each latitude corresponds to one of the double arrays
			//in the data ArrayList, so we need to pull down 2 of them.  Also, the ArrayList is stored
			//so that index 0 is the northernmost data, but we step through the TriangleStripArray from
			//south to north, hence the latIncrements-i.
			vtkTriangleStrip triangleStrip = new vtkTriangleStrip();
			//number of vertices are twice 
			triangleStrip.GetPointIds().SetNumberOfIds(longIncrements*2);

			new vtkLine();
			stripIndex=0;
			firstLineOfData = (double[])data.get(latIncrements-i-1);
			secondLineOfData = (double[])data.get(latIncrements-i);

			for (int j=0; j<longIncrements; j++) {
				try {
					double[] xForm = new double[3];
					//Point 1
					double height =  (secondLineOfData[j]/200.0+altitude);
					lat =(s+i*latStep);
					lon =(w+j*longStep);
					xForm = Transform.transformLatLonHeight(lat, lon, height);
					float longRatio=(float)j/(float)longIncrements;
					float lati=i;
					if(j>longIncrements)
						longRatio=1;
					if(i>lati)
						lati=latIncrements;
					textureCoordinates.InsertTuple2(pointIndex,longRatio, lati/(float)latIncrements);
					//textureCoordinates.InsertTuple2(num,(float)((lon-w)/(e-w)), (float)((lat-s)/(n-s)));//, xForm[2]);
					pts.InsertPoint(pointIndex,xForm);

					//Point 2
					height = (firstLineOfData[j]/200.0+altitude);
					lat = (s+(i+1)*latStep);
					lon =  (w+j*longStep);
					xForm = Transform.transformLatLonHeight(lat, lon, height);//Transform.customTransform(latlon);
					pts.InsertPoint(pointIndex+1,xForm);
					textureCoordinates.InsertTuple2( pointIndex+1, longRatio,(lati+1)/(float)latIncrements);

					triangleStrip.GetPointIds().SetId(stripIndex,pointIndex);	
					triangleStrip.GetPointIds().SetId(stripIndex+1,pointIndex+1);
					pointIndex+=2;
					stripIndex+=2;
				} catch (Exception ex) {
					System.out.println("Exception " + ex);
					System.out.println("i=" + i + " j=" + j);
				}

			}
			cells.InsertNextCell(triangleStrip);
		}

		vtkPolyData triangleStripPolydata =new vtkPolyData();
		triangleStripPolydata.SetPoints(pts);
		triangleStripPolydata.SetStrips(cells);
		triangleStripPolydata.GetPointData().SetTCoords(textureCoordinates);

		return triangleStripPolydata;
	}
	public void display(GeographicSurfaceInfo si,ImageInfo ii) { //creates the surface
		data = new ArrayList<double[]>();

		try {
			loadData(si);
		} catch (IOException e) {
			Throwables.propagate(e);
		}
		//texture file
		vtkJPEGReader jPEGReader = new vtkJPEGReader();

		jPEGReader.SetFileName(ii.getFilename());

		vtkPolyData polydata = new vtkPolyData(); 
		polydata = createSurface(si);
		// Create an actor and mapper

		// Apply the texture
		vtkTexture texture = new vtkTexture();
		texture.SetInputConnection(jPEGReader.GetOutputPort());


		//Create a mapper and actor
		vtkPolyDataMapper mapper =new vtkPolyDataMapper();
		mapper.SetInputData(polydata);
		//mapper.ScalarVisibilityOff();
		vtkActor actor = new vtkActor();
		actor.SetMapper(mapper);
		actor.SetTexture(texture);
		actor.GetProperty().SetOpacity(0.5);
		// actor.GeneralTextureTransform();
		//actor.GetProperty().SetRepresentationToWireframe();
		surfaceActors.addActor(actor);
		Info.getMainGUI().updateRenderWindow(actor);
	}

	public void display(ImageInfo ii) {	//This is the method to display an image

		System.out.println("Image added");
	}


	public int addSurfaceImage(LoadedFilesProperties lfp, ImagePluginGUI ipg){
		displayedImageSurfaceVector.addElement(lfp);

		String data = new String();
		String file = new String();
		int begin,end;
		if(lfp.getSurfaceFilePath() != null)
			file = lfp.getSurfaceFilePath();
		else{
			file = lfp.getGeoInfo().getFilename();
			display(lfp.getGeoInfo(),lfp.getImageInfo());
		}
		if(file.equalsIgnoreCase("-")){
			data = "-";
		}
		else{
			if(file.contains("\\")) {
				begin = file.lastIndexOf("\\") + 1;
			}
			else {
				begin = file.lastIndexOf("/") + 1;
			}
			end = file.length()-1;
			if(file.endsWith(".txt")){
				end = file.indexOf(".txt");
			}
			else if(file.endsWith(".dem")){
				end = file.indexOf(".dem");
			}
			data = file.substring(begin,end);
		}
		displayedSurfaceInfoVector.add(data);
		if(lfp.getImageFilePath() != null)
			file = lfp.getImageFilePath();
		else{
			file = lfp.getImgInfo().getFilename();
			display(lfp.getImgInfo());
		}
		if(file.equalsIgnoreCase("-")){
			data = "-";
		}
		else{
			if(file.contains("\\")) {
				begin = file.lastIndexOf("\\") + 1;
			}
			else {
				begin = file.lastIndexOf("/") + 1;
			}
			end = file.length() - 1;
			if(file.endsWith(".jpg")){
				end = file.indexOf(".jpg");
			}
			else if(file.endsWith(".jpeg")){
				end = file.indexOf(".jpeg");
			}
			data = file.substring(begin,end);
		}
		displayedImageInfoVector.add(data);
		ipg.displayedImageList.setListData(displayedImageInfoVector);
		ipg.displayedSurfaceList.setListData(displayedSurfaceInfoVector);
		int index=displayedImageInfoVector.size()-1;
		ipg.displayedSurfaceList.setSelectedIndex(index);
		ipg.displayedImageList.setSelectedIndex(index);
		return index;
	}
}
