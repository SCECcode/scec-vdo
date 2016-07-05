package org.scec.vtk.plugins.ShakeMapPlugin.Component;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.XYZ_DataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.imr.ScalarIMR;
import org.scec.vtk.commons.opensha.geoDataSet.AbstractXYZToColorPalette;
import org.scec.vtk.tools.Transform;

import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolygon;
import vtk.vtkUnsignedCharArray;

public class ShakeMap extends AbstractXYZToColorPalette{

	private String dataPath;
	private ArrayList<EQDot> eQDots;
	vtkPolyDataMapper mapper = new vtkPolyDataMapper();
	private int width;

	public ShakeMap(CPT cpt, String fp) {
		super(cpt,fp);
		eQDots = new ArrayList<>();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadFromGeoDataSet(GeoDataSet ds, ScalarIMR imr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadFromFile(String dP,String type) {
		// TODO Auto-generated method stub
		System.out.println("Loading file...");
		int width = 0;
		Double lastLat = 0.0;

		File file = new File(dP);
		if (file.isFile()) 
		{
			setFile(file);

			String tempName = file.getName();
			tempName = tempName.substring(0, tempName.lastIndexOf("."));
			tempName = tempName.replace('_', ' ');
			setName(tempName);



			int j = 0;
			boolean foundWidth = false;

			BufferedReader input = null;
			try {
				input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Parse file and create quad array
			try {
				while(input.ready()) 
				{
					width++;
					j++;

					@SuppressWarnings("unused")
					String temp = "";
					String line = input.readLine();
					StringTokenizer dataLine = new StringTokenizer(line);

					String lonStr = "";
					String latStr = "";
					String mag = "";

					// You can add more types to parse different kinds of files.  What you need is a longitude, latitude and some value to be mapped
					if(type == "shake")
					{
						lonStr = dataLine.nextToken();
						latStr = dataLine.nextToken();
						dataLine.nextToken();
						dataLine.nextToken();
						mag = dataLine.nextToken();	
					}
					if(type == "hazard")
					{
						lonStr = dataLine.nextToken();
						latStr = dataLine.nextToken();
						mag = dataLine.nextToken();
					}

					double magnitude = Double.parseDouble(mag);

					while(dataLine.hasMoreTokens()) 
					{
						temp += dataLine.nextToken() + " ";
					}

					//this finds the width of the shake map (it's a rectangle)
					if(lastLat != Double.parseDouble(latStr) && !foundWidth && j > 1)
					{
						foundWidth = true;
						setWidth(width-1);
					}

					lastLat = Double.parseDouble(latStr);

					EQDot tempDot = new EQDot(Double.parseDouble(latStr), Double.parseDouble(lonStr), magnitude, 1);

					getEQdots().add(tempDot);

				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private ArrayList<EQDot>  getEQdots() {
		// TODO Auto-generated method stub
		return eQDots;
	}

	private void setWidth(int i) {
		// TODO Auto-generated method stub
		this.width = i;
	}

	private void setName(String tempName) {
		// TODO Auto-generated method stub

	}

	private void setFile(File file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawPolygonMap() {
		vtkPoints pts = new vtkPoints();
		// Add the polygon to a list of polygons
		vtkCellArray polygons = new vtkCellArray();
		// TODO Auto-generated method stub
		int eqDotCount=0;
		vtkUnsignedCharArray colors =new vtkUnsignedCharArray();
		colors.SetNumberOfComponents(3);
		colors.SetName ("Colors");
		for(int j = 0; j < getEQdots().size()-getWidth()-1; j++)
		{

			//This checks to see if the current point is the last in the row, prevents lines from getting wrapped around
			if((j+1) % getWidth() != 0){

				//Convert latitude and longitude to a point in the 3d world
				double[] currentPoint = Transform.transformLatLonHeight(getEQdots().get(j+getWidth()).getLat(), getEQdots().get(j+getWidth()).getLng(), getHeight());
				pts.InsertNextPoint(currentPoint);
				//pArray.setCoordinate(v, currentPoint);

				Color color4 = calcColor(getEQdots().get(j+getWidth()).getMag());
				colors.InsertNextTuple3(color4.getRed(), color4.getGreen(), color4.getBlue());
				//pArray.setColor(v, color4);

				currentPoint = Transform.transformLatLonHeight(getEQdots().get(j+getWidth()+1).getLat(), getEQdots().get(j+getWidth()+1).getLng(), getHeight());
				pts.InsertNextPoint(currentPoint);
				//pArray.setCoordinate(eqDotCount+1, currentPoint);

				color4 = calcColor(getEQdots().get(j+getWidth()+1).getMag());
				colors.InsertNextTuple3(color4.getRed(), color4.getGreen(), color4.getBlue());

				currentPoint = Transform.transformLatLonHeight(getEQdots().get(j+1).getLat(), getEQdots().get(j+1).getLng(), getHeight());
				pts.InsertNextPoint(currentPoint);
				//pArray.setCoordinate(eqDotCount+2, currentPoint);

				color4 = calcColor(getEQdots().get(j+1).getMag());
				colors.InsertNextTuple3(color4.getRed(), color4.getGreen(), color4.getBlue());

				currentPoint = Transform.transformLatLonHeight(getEQdots().get(j).getLat(), getEQdots().get(j).getLng(), getHeight());
				pts.InsertNextPoint(currentPoint);
				//pArray.setCoordinate(eqDotCount+3, currentPoint);

				color4 =  calcColor(getEQdots().get(j).getMag());
				colors.InsertNextTuple3(color4.getRed(), color4.getGreen(), color4.getBlue());

				// Create the polygon
				vtkPolygon polygon =new vtkPolygon();
				polygon.GetPointIds().SetNumberOfIds(4); //make a quad
				polygon.GetPointIds().SetId(0, eqDotCount);
				polygon.GetPointIds().SetId(1, eqDotCount+1);
				polygon.GetPointIds().SetId(2, eqDotCount+2);
				polygon.GetPointIds().SetId(3, eqDotCount+3);



				polygons.InsertNextCell(polygon);

				eqDotCount+=4;
			}
		}


		vtkPolyData polydata =new vtkPolyData();
		polydata.SetPoints(pts);
		polydata.SetPolys(polygons);
		polydata.GetPointData().AddArray(colors);


		mapper.SetInputData(polydata);
		mapper.ScalarVisibilityOn();
		mapper.SetScalarModeToUsePointFieldData();
		mapper.SelectColorArray("Colors");
	}

	public vtkPolyDataMapper getMapper()
	{
		return mapper;
	}

	private double getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	private int getWidth() {
		// TODO Auto-generated method stub
		return this.width;
	}

	@Override
	public boolean contains(Location arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GeoDataSet copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double get(Location arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Location getLocation(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocationList getLocationList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getMaxLat() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaxLon() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinLat() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinLon() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int indexOf(Location arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isLatitudeX() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void set(Location arg0, double arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLatitudeX(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void abs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void add(double arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean contains(Point2D arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(double arg0, double arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void exp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void exp(double arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public double get(Point2D arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double get(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double get(double arg0, double arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaxX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaxY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaxZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Point2D getPoint(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Point2D> getPointList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getSumZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Double> getValueList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int indexOf(Point2D arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int indexOf(double arg0, double arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void log() {
		// TODO Auto-generated method stub

	}

	@Override
	public void log10() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pow(double arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scale(double arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(Point2D arg0, double arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(int arg0, double arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(double arg0, double arg1, double arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAll(XYZ_DataSet arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
