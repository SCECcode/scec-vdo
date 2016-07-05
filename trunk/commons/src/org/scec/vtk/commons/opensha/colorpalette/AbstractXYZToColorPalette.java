package org.scec.vtk.commons.opensha.colorpalette;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.data.xyz.XYZ_DataSet;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.imr.ScalarIMR;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.ShapefileReaderJGeom;

public abstract class AbstractXYZToColorPalette implements GeoDataSet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	CPT cpt;
	GriddedRegion region;
	boolean latitudeX;
	//gridded geo data set
	GriddedGeoDataSet griddedGeoDataSet;
	public AbstractXYZToColorPalette(CPT cpt,GriddedRegion region, boolean latitudeX) {

		this.cpt=cpt;
		this.region = region;
		this.latitudeX = latitudeX;
		griddedGeoDataSet = new GriddedGeoDataSet(region, latitudeX);
	}

	public GriddedGeoDataSet getGriddedGeoDataSet()
	{
		return griddedGeoDataSet;
	}

	public AbstractXYZToColorPalette(CPT cpt,String filePath) {

		this.cpt=cpt;

		//get the colors file to be used as CPT
		loadCPTFromFile((filePath));

	}

	//loading existing catalog file
	public CPT loadCPTFromFile(String dataPath)
	{
		try {
			cpt = CPT.loadFromFile(new File(dataPath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cpt;
	}
	//calculates color based on CPT
	public Color calcColor(double value) {
		Color pointColor = cpt.getColor((float) value);
		return pointColor;
	}

	abstract public void loadFromGeoDataSet(GeoDataSet ds, ScalarIMR imr);
	abstract public void loadFromFile(String dP,String type);
	abstract public void drawPolygonMap();

	//TODO load gridded geo data set which also extends geodataset
}
