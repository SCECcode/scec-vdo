package org.scec.vtk.commons.opensha.geoDataSet;

import java.awt.Color;

import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.cpt.CPT;
import org.scec.vtk.tools.Transform;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolygon;
import vtk.vtkUnsignedCharArray;
import vtk.vtkVertexGlyphFilter;

public class GeoDataSetGeometryGenerator {

	/**
	 * Builds a polygon surface for the given GriddedGeoDataSet and CPT file
	 * @param dataset
	 * @param cpt
	 * @return
	 */
	public static vtkActor buildPolygonSurface(GriddedGeoDataSet dataset, CPT cpt) {
		vtkPoints pts = new vtkPoints();
		// Add the polygon to a list of polygons
		vtkCellArray polygons = new vtkCellArray();
		vtkUnsignedCharArray colors =new vtkUnsignedCharArray();
		vtkPolyData polydata =new vtkPolyData();
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		vtkActor actor =  new vtkActor();
		
		colors.SetNumberOfComponents(3);
		colors.SetName ("Colors");
		int ptCount=0;
		int width = (int) Math.floor(( dataset.getMaxLon() - dataset.getMinLon()) / dataset.getRegion().getLonSpacing())+1 ;
		
		for(int j = 0; j < dataset.getRegion().getNodeCount()-width-1; j++)
		{
			//prevents polys from getting wrapped around
			if((j+1) % width != 0){
				//Convert latitude and longitude to a point in the 3d world
				Location loc = dataset.getLocation(j+width);
				double[] currentPoint = Transform.transformLatLonHeight(loc.getLatitude(), loc.getLongitude(), loc.getDepth());
				pts.InsertNextPoint(currentPoint);
				Color color3 = cpt.getColor((float) dataset.get(loc));
				colors.InsertNextTuple3(color3.getRed(), color3.getGreen(), color3.getBlue());

				loc = dataset.getLocation(j+width+1);
				currentPoint = Transform.transformLatLonHeight(loc.getLatitude(), loc.getLongitude(), loc.getDepth());
				pts.InsertNextPoint(currentPoint);
				color3 = cpt.getColor((float) dataset.get(loc));
				colors.InsertNextTuple3(color3.getRed(), color3.getGreen(), color3.getBlue());

				loc = dataset.getLocation(j+1);
				currentPoint = Transform.transformLatLonHeight(loc.getLatitude(), loc.getLongitude(), loc.getDepth());
				pts.InsertNextPoint(currentPoint);
				color3 = cpt.getColor((float) dataset.get(loc));
				colors.InsertNextTuple3(color3.getRed(), color3.getGreen(), color3.getBlue());

				loc = dataset.getLocation(j);
				currentPoint = Transform.transformLatLonHeight(loc.getLatitude(), loc.getLongitude(), loc.getDepth());
				pts.InsertNextPoint(currentPoint);
				color3 = cpt.getColor((float) dataset.get(loc));
				colors.InsertNextTuple3(color3.getRed(), color3.getGreen(), color3.getBlue());

				// Create the polygon
				vtkPolygon polygon =new vtkPolygon();
				polygon.GetPointIds().SetNumberOfIds(4); //make a quad

				polygon.GetPointIds().SetId(0, ptCount);
				polygon.GetPointIds().SetId(1, ptCount+1);
				polygon.GetPointIds().SetId(2, ptCount+2);
				polygon.GetPointIds().SetId(3, ptCount+3);

				polygons.InsertNextCell(polygon);
				ptCount+=4;
			}
		}



		polydata.SetPoints(pts);
		polydata.SetPolys(polygons);
		polydata.GetPointData().AddArray(colors);

		
		mapper.SetInputData(polydata);
		mapper.ScalarVisibilityOn();
		mapper.SetScalarModeToUsePointFieldData();
		mapper.SelectColorArray("Colors");
		
		actor.SetMapper(mapper);
		actor.GetProperty().SetAmbient(1);
		actor.GetProperty().SetSpecular(0);
		actor.GetProperty().SetDiffuse(0);
		return actor; 
	}

	/**
	 * Builds a point cloud for the given GeoDataSet and CPT file, can be used for irregularly spaced data
	 * @param dataset
	 * @param cpt
	 * @param pointSize
	 * @return
	 */
	public static vtkActor buildPointSurface(GeoDataSet dataset, CPT cpt, double pointSize) {
		vtkPoints pts = new vtkPoints();
		vtkUnsignedCharArray colors =new vtkUnsignedCharArray();
		vtkPolyData polydata =new vtkPolyData();
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		vtkActor actor =  new vtkActor();
		
		colors.SetNumberOfComponents(3);
		colors.SetName ("Colors");

		for(int j = 0; j < dataset.getLocationList().size(); j++)
		{

			//Convert latitude and longitude to a point in the 3d world
			Location loc = dataset.getLocation(j);
			double[] currentPoint = Transform.transformLatLonHeight(loc.getLatitude(), loc.getLongitude(), loc.getDepth());
			pts.InsertNextPoint(currentPoint);
			Color color3 = cpt.getColor((float) dataset.get(loc));
			colors.InsertNextTuple3(color3.getRed(), color3.getGreen(), color3.getBlue());
		}
		polydata.SetPoints(pts);
		polydata.GetPointData().AddArray(colors);
		//points
		vtkVertexGlyphFilter vertexGlyphFilter = new vtkVertexGlyphFilter();
		vertexGlyphFilter.AddInputData(polydata);
		vertexGlyphFilter.Update();

		mapper.SetInputConnection(vertexGlyphFilter.GetOutputPort());
		mapper.ScalarVisibilityOn();
		mapper.SetScalarModeToUsePointFieldData();
		mapper.SelectColorArray("Colors");
		
		actor.SetMapper(mapper);
		actor.GetProperty().SetAmbient(1);
		actor.GetProperty().SetSpecular(0);
		actor.GetProperty().SetDiffuse(0);
		actor.GetProperty().SetPointSize(pointSize);
		return actor; // TODO
	}

	/**
	 * Builds a polygon surface where a rectangular "pixel" polygon is created at each location in the given GeoDataSet and CPT file.
	 * This can work with both evenly and irregularly spaced data. If the dataset isn't gridded, the minimum distance in lat/lon between
	 * grid points will be used as the pixel size
	 * @param dataset
	 * @param cpt
	 * @param skipNaN
	 * @return
	 */
	public static vtkActor buildPixelSurface(GeoDataSet dataset, CPT cpt, boolean skipNaN) {
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
		return buildPixelSurface(dataset, cpt, skipNaN, latSpacing, lonSpacing);
	}

	/**
	 * Builds a polygon surface where a rectangular "pixel" polygon is created at each location in the given GeoDataSet and CPT file.
	 * @param dataset
	 * @param cpt
	 * @param skipNaN
	 * @param latSpacing
	 * @param lonSpacing
	 * @return
	 */
	public static vtkActor buildPixelSurface(GeoDataSet dataset, CPT cpt, boolean skipNaN, double latSpacing, double lonSpacing) {
		
		vtkPoints pts = new vtkPoints();
		// Add the polygon to a list of polygons
		vtkCellArray polygons = new vtkCellArray();
		vtkUnsignedCharArray colors =new vtkUnsignedCharArray();
		vtkPolyData polydata =new vtkPolyData();
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		vtkActor actor =  new vtkActor();
		
		colors.SetNumberOfComponents(3);
		colors.SetName ("Colors");
		int ptCount=0;
		double halfLatSpacing = 0.5*latSpacing;
		double halfLonSpacing = 0.5*lonSpacing;

		for (int i=0; i<dataset.size(); i++) {
			Location center = dataset.getLocation(i);
			double val = dataset.get(i);
			if (skipNaN && Double.isNaN(val))
				continue;
			Color color3 = cpt.getColor((float)val);

			// build pixel
			double lat = center.getLatitude();
			double lon = center.getLongitude();
			double depth = center.getDepth();
			
			Location topLeft = new Location(lat + halfLatSpacing, lon - halfLonSpacing, depth);
			double[] currentPoint = Transform.transformLatLonHeight(topLeft.getLatitude(), topLeft.getLongitude(), topLeft.getDepth());
			pts.InsertNextPoint(currentPoint);
			colors.InsertNextTuple3(color3.getRed(), color3.getGreen(), color3.getBlue());
			
			Location topRight = new Location(lat + halfLatSpacing, lon + halfLonSpacing, depth);
			currentPoint = Transform.transformLatLonHeight(topRight.getLatitude(), topRight.getLongitude(), topRight.getDepth());
			pts.InsertNextPoint(currentPoint);
			colors.InsertNextTuple3(color3.getRed(), color3.getGreen(), color3.getBlue());
			
			Location botRight = new Location(lat - halfLatSpacing, lon + halfLonSpacing, depth);
			currentPoint = Transform.transformLatLonHeight(botRight.getLatitude(), botRight.getLongitude(), botRight.getDepth());
			pts.InsertNextPoint(currentPoint);
			colors.InsertNextTuple3(color3.getRed(), color3.getGreen(), color3.getBlue());
			
			Location botLeft = new Location(lat - halfLatSpacing, lon - halfLonSpacing, depth);
			currentPoint = Transform.transformLatLonHeight(botLeft.getLatitude(), botLeft.getLongitude(), botLeft.getDepth());
			pts.InsertNextPoint(currentPoint);
			colors.InsertNextTuple3(color3.getRed(), color3.getGreen(), color3.getBlue());
			
			//build polygon with those 4 corners but same color val
			vtkPolygon polygon =new vtkPolygon();
			polygon.GetPointIds().SetNumberOfIds(4); //make a quad

			polygon.GetPointIds().SetId(0, ptCount);
			polygon.GetPointIds().SetId(1, ptCount+1);
			polygon.GetPointIds().SetId(2, ptCount+2);
			polygon.GetPointIds().SetId(3, ptCount+3);

			polygons.InsertNextCell(polygon);
			ptCount+=4;
		}
		polydata.SetPoints(pts);
		polydata.SetPolys(polygons);
		polydata.GetPointData().AddArray(colors);

		
		mapper.SetInputData(polydata);
		mapper.ScalarVisibilityOn();
		mapper.SetScalarModeToUsePointFieldData();
		mapper.SelectColorArray("Colors");
		
		actor.SetMapper(mapper);
		actor.GetProperty().SetAmbient(1);
		actor.GetProperty().SetSpecular(0);
		actor.GetProperty().SetDiffuse(0);
		return actor; 
	}

}
