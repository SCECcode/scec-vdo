package org.scec.vtk.plugins.SurfacePlugin;

import java.awt.Container;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JPanel;

import org.jfr.examples.JpedalLabel;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.SurfacePlugin.Component.LoadedFilesProperties;
import org.scec.vtk.tools.Transform;

import com.google.common.base.Throwables;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkDataSetMapper;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkTriangleStrip;

public class SurfacePluginGUI {
	
	private ImagePluginGUI ipg;
	private JPanel allPanel = new JPanel();
	private double scaleFactor;
	private ArrayList<double[]> data;
	private ArrayList<vtkActor> surfaceActors = new ArrayList<vtkActor>();
	private Vector<LoadedFilesProperties> displayedImageSurfaceVector = new Vector<LoadedFilesProperties>();
	protected Vector<String> displayedImageInfoVector = new Vector<String>();
	protected Vector<String> displayedSurfaceInfoVector = new Vector<String>();
	private int longIncrements, latIncrements, horizontalItems;
	private double w, e, n, s;
	private double altitude;
	private String filename;
	private BufferedReader demReader;

	public SurfacePluginGUI(){
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
		latIncrements = data.size()-1;
		si.setVertSteps(latIncrements);
		si.setHorizSteps(longIncrements);
		
		int[] strips = new int[latIncrements];
		for (int i=0; i<latIncrements; i++) {
			strips[i] = 2*longIncrements + 2; //number in each strip
		}
		
		int numVertices = latIncrements*(2*longIncrements+2);
		
		vtkTriangleStrip triangleStrip = new vtkTriangleStrip();
		//numVertices, TriangleStripArray.COORDINATES | TriangleStripArray.TEXTURE_COORDINATE_2, strips);
		
		triangleStrip.GetPointIds().SetNumberOfIds(numVertices);
		//Point3d[][] pt = new Point3d[latIncrements][2*longIncrements+2]; //all the points will be stored here
		vtkPoints pts = new vtkPoints();
		double latStep = (n-s)/(double)latIncrements;
		double longStep = (e-w)/(double)longIncrements;
		double[] firstLineOfData = new double[horizontalItems];
		double[] secondLineOfData = new double[horizontalItems];
		for (int i=0; i<latIncrements; i++) {
			//we fill the Triangle strip array by going bottom, top, bottom, top, so we need z values
			//from two different latitudes.  Each latitude corresponds to one of the double arrays
			//in the data ArrayList, so we need to pull down 2 of them.  Also, the ArrayList is stored
			//so that index 0 is the northernmost data, but we step through the TriangleStripArray from
			//south to north, hence the latIncrements-i.
			firstLineOfData = (double[])data.get(latIncrements-i-1);
			secondLineOfData = (double[])data.get(latIncrements-i);
			for (int j=0; j<longIncrements+1; j++) {
				try {
					//take all the points and put them in a whopping bit 2-D array
					//pt[i][2*j] = LatLongToPoint.plotPoint(s+i*latStep, w+j*longStep, secondLineOfData[j]/200.0+altitude);
					double[] xForm = new double[3];
					double[] latlon = new double[3];
					int num = i*(2*longIncrements+2)+2*j;
					
					latlon[0] = Transform.calcRadius(s+i*latStep) + (secondLineOfData[j]/200.0+altitude);
	                 //latitude;
	                latlon[1] = (s+i*latStep);
	                 //longitude;
	                latlon[2] =  (w+j*longStep);
	                 
	                xForm = Transform.customTransform(latlon);
	                 
					pts.InsertPoint(num,xForm);
					//pt[i][2*j+1] = LatLongToPoint.plotPoint(s+(i+1)*latStep, w+j*longStep, firstLineOfData[j]/200.0+altitude);
					
					latlon[0] = Transform.calcRadius(s+(i+1)*latStep) + (firstLineOfData[j]/200.0+altitude);
	                 //latitude;
	                latlon[1] = (s+(i+1)*latStep);
	                 //longitude;
	                latlon[2] =  (w+j*longStep);
	                 
	                xForm = Transform.customTransform(latlon);
	                 
					pts.InsertPoint(num+1,xForm);
					
					triangleStrip.GetPointIds().SetId(num,num);
					triangleStrip.GetPointIds().SetId(num+1,num+1);

				} catch (Exception ex) {
					System.out.println("Exception " + ex);
					System.out.println("i=" + i + " j=" + j);
				}
			}
		}

		//copy all the points into the TriangleStripArray
		//t.setCoordinates(0, pts);
		//t.setCapability(TriangleStripArray.ALLOW_TEXCOORD_READ);
		//t.setCapability(TriangleStripArray.ALLOW_TEXCOORD_WRITE);

		
		vtkCellArray cells = new vtkCellArray();
		cells.InsertNextCell(triangleStrip);
			 
		vtkPolyData triangleStripPolydata =new vtkPolyData();
		triangleStripPolydata.SetPoints(pts);
		triangleStripPolydata.SetStrips(cells);
			 		
		
		return triangleStripPolydata;
	}
	public void display(GeographicSurfaceInfo si) { //creates the surface
		data = new ArrayList<double[]>();
		
		//bg = new BranchGroup();
		//bg.setCapability(BranchGroup.ALLOW_DETACH);
		try {
			loadData(si);
		} catch (IOException e) {
			Throwables.propagate(e);
		}
		vtkPolyData polydata = new vtkPolyData(); 
		polydata = createSurface(si);
			  // Create an actor and mapper
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
			  mapper.SetInputData(polydata);
						 
			 vtkActor actor = new vtkActor();
			  actor.SetMapper(mapper);
			  actor.GetProperty().SetRepresentationToWireframe();
		surfaceActors.add(actor);
		Info.getMainGUI().updateActors(surfaceActors);
		//Info.getMainGUI().updateRenderWindow();
		/*TriangleStripArray t = createSurface(si);
		t.setCapability(TriangleStripArray.ALLOW_TEXCOORD_READ);
		t.setCapability(TriangleStripArray.ALLOW_TEXCOORD_WRITE);
		t.setCapability(TriangleStripArray.ALLOW_COUNT_READ);
		t.setCapability(TriangleStripArray.ALLOW_COORDINATE_READ);
		si.setData(t);
		ColoringAttributes ca = new ColoringAttributes(new Color3f(1.0f, 1.0f, 1.0f), ColoringAttributes.SHADE_GOURAUD);
		Appearance a = new Appearance();
				a.setColoringAttributes(ca);
				a.setPolygonAttributes(
							new PolygonAttributes(
									PolygonAttributes.POLYGON_LINE, //makes it a mesh
									PolygonAttributes.CULL_NONE,
									0));
		Shape3D sh = new Shape3D(t, a);
		bg.addChild(sh);
		parent.registerSurface(si);
		si.setBranchGroup(bg);
		Geo3dInfo.getMainWindow().getPluginBranchGroup().addChild(bg);*/
		//add image to  
	}
	
	public void display(ImageInfo ii) {	//This is the method to display an image
		/*bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		bg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		
		Point3d ul = ii.getUpperLeft();
		Point3d lr = ii.getLowerRight();
		
		n = ul.x;
		w = ul.y;
		s = lr.x;
		e = lr.y;
		altitude = lr.z;
		
		Appearance appearance = createAppearance(ii);
		appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
		appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		TriangleStripArray shape;
		//This sees if any part of a surface overlaps with this image.  If it does, the SurfaceInfo
		//object is returned;  if not, it's null
		GeographicSurfaceInfo si = parent.getSurface(new Point3d(n, w, 0), new Point3d(s, e, 0));
		ii.setAttachedSurface(si); //so it can be detatched later
		if (si==null) { //no surface
			shape = createDefaultSurface(); //creates a surface with a point every degree of lat or long
		} else {
			si.setImage(ii);
			shape = setUpCurrentSurface(si); //uses the already existing surface
		}
		
		sh = new Shape3D(shape, appearance);
		sh.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		sh.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		bg.addChild(sh);
		ii.setBranchGroup(bg);
		Geo3dInfo.getPluginBranchGroup().addChild(bg);*/
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
			display(lfp.getGeoInfo());
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
