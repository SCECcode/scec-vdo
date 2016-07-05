package org.scec.vtk.commons.opensha.geoDataSet;

import java.awt.Color;

import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.cpt.CPT;

import vtk.vtkActor;

public class GeoDataSetGeometryGenerator {
	
	/**
	 * Builds a polygon surface for the given GriddedGeoDataSet and CPT file
	 * @param dataset
	 * @param cpt
	 * @return
	 */
	public static vtkActor buildPolygonSurface(GriddedGeoDataSet dataset, CPT cpt) {
		return null; // TODO
	}
	
	/**
	 * Builds a point cloud for the given GeoDataSet and CPT file, can be used for irregularly spaced data
	 * @param dataset
	 * @param cpt
	 * @param pointSize
	 * @return
	 */
	public static vtkActor buildPointSurface(GeoDataSet dataset, CPT cpt, double pointSize) {
		return null; // TODO
	}
	
	/**
	 * Builds a polygon surface where a rectangular "pixel" polygon is created at each location in the given GeoDataSet and CPT file.
	 * This can work with both evenly and irregularly spaced data. If the dataset isn't gridded, the minimum distance in lat/lon between
	 * grid points will be used as the pixel size
	 * @param dataset
	 * @param cpt
	 * @return
	 */
	public static vtkActor buildPixelSurface(GeoDataSet dataset, CPT cpt) {
		double latSpacing, lonSpacing;
		if (dataset instanceof GriddedGeoDataSet) {
			GriddedGeoDataSet gridded = (GriddedGeoDataSet)dataset;
			latSpacing = gridded.getRegion().getLatSpacing();
			lonSpacing = gridded.getRegion().getLonSpacing();
		} else {
			// calculate minimum spacing
			latSpacing = Double.POSITIVE_INFINITY;
			lonSpacing = Double.POSITIVE_INFINITY;
			for (int i=0; i<dataset.size(); i++) {
				Location loc1 = dataset.getLocation(i);
				for (int j=i+1; j<dataset.size(); j++) {
					Location loc2 = dataset.getLocation(j);
					double latDelta = Math.abs(loc1.getLatitude() - loc2.getLatitude());
					double lonDelta = Math.abs(loc1.getLongitude() - loc2.getLongitude());
					latSpacing = Math.min(latSpacing, latDelta);
					lonSpacing = Math.min(lonDelta, lonDelta);
				}
			}
		}
		return buildPixelSurface(dataset, cpt, latSpacing, lonSpacing);
	}
	
	/**
	 * Builds a polygon surface where a rectangular "pixel" polygon is created at each location in the given GeoDataSet and CPT file.
	 * @param dataset
	 * @param cpt
	 * @param latSpacing
	 * @param lonSpacing
	 * @return
	 */
	public static vtkActor buildPixelSurface(GeoDataSet dataset, CPT cpt, double latSpacing, double lonSpacing) {
		double halfLatSpacing = 0.5*latSpacing;
		double halfLonSpacing = 0.5*lonSpacing;
		
		for (int i=0; i<dataset.size(); i++) {
			Location center = dataset.getLocation(i);
			double val = dataset.get(i);
			Color color = cpt.getColor((float)val);
			
			// build pixel
			double lat = center.getLatitude();
			double lon = center.getLongitude();
			double depth = center.getDepth();
			Location topLeft = new Location(lat + halfLatSpacing, lon - halfLonSpacing, depth);
			Location topRight = new Location(lat + halfLatSpacing, lon + halfLonSpacing, depth);
			Location botRight = new Location(lat - halfLatSpacing, lon + halfLonSpacing, depth);
			Location botLeft = new Location(lat - halfLatSpacing, lon - halfLonSpacing, depth);
			
			// TODO build polygon with those 4 corners
		}
		return null; // TODO
	}

}
