package org.scec.vtk.commons.shapes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.geo.Location;
import org.scec.vtk.tools.Transform;

import com.google.common.base.Preconditions;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

public class Boundary {
	
	private vtkActor actor;
	private vtkPoints pts;
	private vtkCellArray lines;
	private vtkPolyData polyData;
	
	private Color color;
	private String name;
	private double lineWidth;
	private double opacity;
	
	public Boundary() {
		this("", Color.WHITE, 1d);
	}
	
	public Boundary(String name, Color color, double lineWidth) {
		this(name, color, null, lineWidth);
	}
	
	public Boundary(String name, Color color, List<Location> locs, double lineWidth) {
		this.name = name;
		this.color = color;
		this.lineWidth = lineWidth;
		this.opacity = 1d;
		
		actor = new vtkActor();
		pts = new vtkPoints();
		lines = new vtkCellArray();
		polyData = new vtkPolyData();
		polyData.SetPoints(pts);
		polyData.SetLines(lines);
		
		if (locs != null && !locs.isEmpty())
			addSegment(locs);
		
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.SetInputData(polyData);
		
		actor.SetMapper(mapper);
		actor.GetProperty().SetColor(getColorDoubleArray(color));
		actor.GetProperty().SetLineWidth(lineWidth);
		actor.GetProperty().SetOpacity(opacity);
		actor.Modified();
	}
	
	private static double[] getColorDoubleArray(Color color) {
		return new double[] { (double)color.getRed()/255d, (double)color.getGreen()/255d,
				(double)color.getBlue()/255d };
	}
	
	public void addSegment(List<Location> locs) {
		addSegment(locs, false);
	}
	
	public void addSegment(List<Location> locs, boolean close) {
		Preconditions.checkArgument(locs.size() > 1);
		int startIndex = pts.GetNumberOfPoints();
		
		if (close && !locs.get(0).equals(locs.get(locs.size()-1))) {
			// close it
			locs = new ArrayList<>(locs);
			locs.add(locs.get(0));
		}
		
		vtkLine line = new vtkLine();
		for (int i=0; i<locs.size(); i++) {
			Location loc = locs.get(i);
			double[] pt = Transform.transformLatLonHeight(loc.getLatitude(), loc.getLongitude(), -loc.getDepth());
			pts.InsertNextPoint(pt);
			line.GetPointIds().SetId(i, startIndex+i);
		}
		Preconditions.checkState((pts.GetNumberOfPoints() - startIndex) == locs.size());
		
		lines.InsertNextCell(line);
		
		lines.Modified();
		pts.Modified();
		polyData.Modified();
		actor.Modified();
	}
	
	public void setWidth(double width) {
		actor.GetProperty().SetLineWidth(width);
		this.lineWidth = width;
		actor.Modified();
	}
	
	public double getWidth() {
		return lineWidth;
	}
	
	public void setOpacity(double opacity) {
		actor.GetProperty().SetOpacity(opacity);
		this.opacity = opacity;
		actor.Modified();
	}
	
	public double getOpacity() {
		return opacity;
	}
	
	public void setColor(Color color) {
		actor.GetProperty().SetColor(getColorDoubleArray(color));
		this.color = color;
		actor.Modified();
	}
	
	public Color getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public vtkActor getActor() {
		return actor;
	}

}
