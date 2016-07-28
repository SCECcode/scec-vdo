package org.scec.vtk.commons.opensha.geoDataSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.util.cpt.CPT;
import vtk.vtkActor;

public class XYZToColorPalette {

	private static final long serialVersionUID = 1L;
	CPT cpt;
	GriddedRegion region;
	boolean latitudeX;
	GeoDataSet geoDataSet; //regular dataset
	//gridded geo data set
	GriddedGeoDataSet griddedGeoDataSet;

	public XYZToColorPalette(CPT cpt,GriddedRegion region, boolean latitudeX) {
		//directly pass region
		this.cpt=cpt;
		this.region = region;
		this.latitudeX = latitudeX;
		griddedGeoDataSet = new GriddedGeoDataSet(region, latitudeX);
	}
	public XYZToColorPalette(String filePath) {
		//get the colors file to be used as CPT
		loadCPTFromFile((filePath));
	}
	

	public GriddedGeoDataSet getGriddedGeoDataSet()
	{
		return griddedGeoDataSet;
	}

	//loading existing catalog file
	public void loadCPTFromFile(String dataPath)
	{
		try {
			cpt = CPT.loadFromFile(new File(dataPath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	 public void loadFromFileToGriddedGeoDataSet(String dP)
	 {
			System.out.println("Loading file...");
			File file = new File(dP);
			try {
				 this.griddedGeoDataSet = GriddedGeoDataSet.loadXYZFile(file, 1, 0, -1, 2); 
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }
	 
	 /*
	  * In these files, the latitude is the first column,
	  * not the longitude
	  */
	 public void loadOpenSHAFileToGriddedGeoDataSet(String dP){
		 	System.out.println("Loading file...");
			File file = new File(dP);
			try {
				 this.griddedGeoDataSet = GriddedGeoDataSet.loadXYZFile(file, 0, 1, -1, 2); 
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }
	 
	 public vtkActor builtPolygonSurface()
	 {
		 return GeoDataSetGeometryGenerator.buildPolygonSurface(griddedGeoDataSet, cpt);
	 }
	 
	 public CPT getCPT()
	 {
		 return cpt;
	 }
}
