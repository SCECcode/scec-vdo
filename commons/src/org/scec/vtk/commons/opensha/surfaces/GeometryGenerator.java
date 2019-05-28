package org.scec.vtk.commons.opensha.surfaces;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.Container2DImpl;
import org.opensha.commons.data.Named;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.surfaces.events.GeometrySettingsChangeListener;
import org.scec.vtk.commons.opensha.surfaces.events.GeometrySettingsChangedEvent;
import org.scec.vtk.tools.Transform;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;
import org.scec.vtk.tools.picking.PointPickEnabledActor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkUnsignedCharArray;

/**
 * This interface generates a Java3D <code>BranchGroup</code> from an OpenSHA <code>EvenlyGriddedSurfaceAPI</code>
 * object.
 * 
 * @author kevin
 *
 */
public abstract class GeometryGenerator implements Named {
	
	private String name;
	
	private PickHandler<AbstractFaultSection> pickHandler;
	
	public GeometryGenerator(String name) {
		this.name = name;
	}
	
	private List<GeometrySettingsChangeListener> listeners = new ArrayList<GeometrySettingsChangeListener>();
	
	protected FaultActorBundler bundler = new GenericFaultActorBundler(0);
	private boolean bundlerEnabled = true;
	
	/**
	 * Builds a vtkActor containing geometry representing the given surface, and colored with the given colorer.
	 * 
	 * @param surface
	 * @param colorer
	 * @param fault
	 * @return
	 */
	public abstract FaultSectionActorList createFaultActors(Surface3D surface, Color color,
			AbstractFaultSection fault);
	
	protected FaultSectionActorList handleCompound(CompoundSurface surface, Color color, AbstractFaultSection fault) {
		FaultSectionActorList list = new FaultSectionActorList(fault);
		for (RuptureSurface subSurf : surface.getSurfaceList()) {
			FaultSectionActorList sub;
			if (subSurf instanceof EvenlyGriddedSurface)
				sub = createFaultActors((EvenlyGriddedSurface)subSurf, color, fault);
			else
				sub = createFaultActors(subSurf, color, fault);
			list.addAll(sub);
		}
		return list;
	}
	
	/**
	 * Update the color of the given branch group. The branch group supplied will always have been generated
	 * by this geometry generator. Should return true if the color was successfully updated, and false if it
	 * cannot be updated, in which case the geometry will be rebuilt with the new color using
	 * <code>createFaultBranchGroup</code>.
	 * 
	 * @param shakeaMapActor
	 * @param color
	 * @return success
	 */
	public boolean updateColor(FaultSectionActorList actorList, Color color) {
		if (actorList instanceof FaultSectionBundledActorList) {
			FaultSectionBundledActorList bundleList = (FaultSectionBundledActorList)actorList;
			FaultActorBundle bundle = bundleList.getBundle();
//			System.out.println("Entering bundle synchronized block (updateColor)");
			synchronized (bundle) {
//				System.out.println("Inside bundle synchronized block (updateColor)");
				vtkUnsignedCharArray colors = bundle.getColorArray();
				int firstIndex = bundleList.getMyFirstPointIndex();
				int lastIndex = firstIndex + bundleList.getMyNumPointsForColoring() - 1;
				int totNumTuples = colors.GetNumberOfTuples();
//				System.out.println("Updating color for points at index "+firstIndex+" through "+lastIndex);
				for (int index=firstIndex; index<=lastIndex; index++) {
					Preconditions.checkState(index < totNumTuples, "Bad tuple index. index=%s, num tuples=%s", index, totNumTuples);
					double[] orig = colors.GetTuple4(index);
					colors.SetTuple4(index, color.getRed(), color.getGreen(), color.getBlue(), orig[3]); // keep same opacity
				}
				colors.Modified();
				bundle.getActor().Modified();
				bundle.modified();
				if (bundle.getActor().GetVisibility() > 0) {
					bundle.getActor().VisibilityOff();
					bundle.getActor().VisibilityOn();
				}
//				System.out.println("Leaving bundle synchronized block (updateColor)");
			}
//			System.out.println("Left bundle synchronized block (updateColor)");
			return true;
		} else {
			for (vtkActor actor : actorList) {
				actor.GetProperty().SetColor(getColorDoubleArray(color));
				actor.Modified();
			}
			return true;
		}
	}
	
	public static double[] getColorDoubleArray(Color color) {
		return new double[] { (double)color.getRed()/255d, (double)color.getGreen()/255d, (double)color.getBlue()/255d };
	}
	
	
	
	/**
	 * Returns the display parameters for this generator
	 * 
	 * @return
	 */
	public abstract ParameterList getDisplayParams();
	
	public void addPlotSettingsChangeListener(GeometrySettingsChangeListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public boolean removePlotSettingsChangeListener(GeometrySettingsChangeListener listener) {
		return listeners.remove(listener);
	}
	
	protected void firePlotSettingsChangeEvent() {
		clearBundles();
		GeometrySettingsChangedEvent e = new GeometrySettingsChangedEvent(this);
		for (GeometrySettingsChangeListener l : listeners) {
			l.geometrySettingsChanged(e);
		}
	}
	
	public static double[] getPointForLoc(Location loc) {
		// TODO
//		return new Point3f(LatLongToPoint.plotPoint3f(loc.getLatitude(),loc.getLongitude(),-loc.getDepth()));
		return Transform.transformLatLonHeight(loc.getLatitude(), loc.getLongitude(), -loc.getDepth());
	}
	
	protected static Container2DImpl<double[]> cacheSurfacePoints(EvenlyGriddedSurface surface) {
		Container2DImpl<double[]> points = new Container2DImpl<double[]>(surface.getNumRows(), surface.getNumCols());
		
		for (int row=0; row<surface.getNumRows(); row++) {
			for (int col=0; col<surface.getNumCols(); col++) {
				points.set(row, col, getPointForLoc(surface.get(row, col)));
			}
		}
		
		return points;
	}
	
//	protected static boolean updateColorAttributesForNode(Node node, Color3f color3f) {
//		if (node != null && node instanceof FaultSectionShape3D) {
//			FaultSectionShape3D shape = (FaultSectionShape3D)node;
//			shape.getAppearance().getColoringAttributes().setColor(color3f);
//			return true;
//		}
//		return false;
//	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public void setFaultActorBundler(FaultActorBundler bundler) {
		this.bundler = bundler;
	}
	
	public FaultActorBundler getFaultActorBundler() {
		return bundler;
	}
	
	public boolean isBundlerEnabled() {
		return bundlerEnabled;
	}
	
	public void setBundlerEneabled(boolean bundlerEnabled) {
		boolean rebuild = bundlerEnabled != this.bundlerEnabled;
		this.bundlerEnabled = bundlerEnabled;
		if (rebuild) {
			clearBundles();
			firePlotSettingsChangeEvent();
		}
	}
	
	public void clearBundles() {
		if (bundler != null)
			bundler.clearBundles();
	}
	
	public void setPickHandler(PickHandler<AbstractFaultSection> pickHandler) {
		this.pickHandler = pickHandler;
	}
	
	public PickHandler<AbstractFaultSection> getPickHandler() {
		return pickHandler;
	}
	
	public static class PointArray {
		
		private double[][] points;
		
		public PointArray(double[]... points) {
			this.points = points;
		}
		
		public int size() {
			return points.length;
		}
		
		public double[][] get() {
			return points;
		}
		
		public double[] get(int index) {
			return points[index];
		}
	}
	
	public enum GeometryType {
		LINE,
		POLYGON;
	}
	
	/**
	 * Dynamically adds points to the vtkPoints as needed without duplicating points used in multiple lines/polygons for a single fault
	 * 
	 * Note - multiple faults can have duplicate points, those are kept separate
	 * @author kevin
	 *
	 */
	private class PointOrganizer {
		
		private vtkPoints points;
		private vtkUnsignedCharArray colorsArray;
		private Map<double[], Integer> pointsIndexMap;
		
		double r, g, b, a;
		
		public PointOrganizer(vtkPoints points, vtkUnsignedCharArray colorsArray, Color color) {
			this.points = points;
			this.colorsArray = colorsArray;
			pointsIndexMap = Maps.newHashMap();
			if (colorsArray != null) {
				r = color.getRed();
				g = color.getGreen();
				b = color.getBlue();
				a = 0d; // always starts as invisible for bundles, set visible when displayed
			}
		}
		
		public int getIndex(double[] point) {
			Integer index = pointsIndexMap.get(point);
			if (index == null) {
				// new point
				index = points.GetNumberOfPoints();
				points.InsertNextPoint(point);
				if (colorsArray != null)
					colorsArray.InsertNextTuple4(r, g, b, a);
				pointsIndexMap.put(point, index);
			}
			return index;
		}
	}
	
	public synchronized FaultSectionActorList createFaultActors(
			GeometryType type, List<PointArray> cellDatas, Color color, double opacity, AbstractFaultSection fault) {
		int myOpacity = (int)(255d*opacity);
		FaultActorBundle currentBundle;
		if (bundler != null && bundlerEnabled && fault != null)
			currentBundle = bundler.getBundle(fault);
		else
			currentBundle = null;
		
		boolean bundle = currentBundle != null;
		
		vtkPolyData polyData;
		vtkPoints pts;
		vtkUnsignedCharArray colors;
		vtkCellArray cells;
		vtkActor actor;
		boolean newBundle = currentBundle == null || !currentBundle.isInitialized();
		Object synchOn = this;
		if (newBundle) {
			polyData = new vtkPolyData();
			pts = new vtkPoints();
			if (bundle) {
				colors = new vtkUnsignedCharArray();
				colors.SetNumberOfComponents(4);
				colors.SetName("Colors");
			} else {
				colors = null;
			}
			cells = new vtkCellArray();
			
			if (bundle) {
				PointPickEnabledActor<AbstractFaultSection> myActor =
						new PointPickEnabledActor<AbstractFaultSection>(getPickHandler());
				actor = myActor;
				currentBundle.initialize(myActor, polyData, pts, colors, cells);
				synchOn = currentBundle;
			} else {
				if (fault == null)
					actor = new vtkActor();
				else
					actor = new PickEnabledActor<AbstractFaultSection>(getPickHandler(), fault);
			}
		} else {
			polyData = currentBundle.getPolyData();
			pts = currentBundle.getPoints();
			colors = currentBundle.getColorArray();
			Preconditions.checkState(colors.GetNumberOfComponents() == 4);
			cells = currentBundle.getCellArray();
			
			actor = currentBundle.getActor();
		}
		int firstIndex;
		synchronized (synchOn) {
			firstIndex = pts.GetNumberOfPoints();
			PointOrganizer organizer = new PointOrganizer(pts, colors, color);
			for (PointArray cell : cellDatas) {
				switch (type) {
				case LINE:
					vtkLine line = new vtkLine();
					for (int i=0; i<cell.size(); i++)
						line.GetPointIds().SetId(i, organizer.getIndex(cell.get(i)));
					cells.InsertNextCell(line);
					break;
				case POLYGON:
					vtkLine polygon = new vtkLine();
					int size = cell.size();
					boolean closed = Arrays.equals(cell.get(0), cell.get(size-1));
					if (closed)
						// vtk polygons are not supposed to be closed
						size--;
					Preconditions.checkState(size >= 3);
					polygon.GetPointIds().SetNumberOfIds(size);
					for (int i=0; i<size; i++)
						polygon.GetPointIds().SetId(i, organizer.getIndex(cell.get(i)));
					cells.InsertNextCell(polygon);
					break;

				default:
					throw new IllegalStateException("Unknkown GeometryType");
				}
			}
			
			if (newBundle) {
				// new bundle
				polyData.SetPoints(pts);
				switch (type) {
				case LINE:
					polyData.SetLines(cells);
					break;
				case POLYGON:
					polyData.SetPolys(cells);
					break;

				default:
					throw new IllegalStateException("Unknkown GeometryType");
				}
				if (bundle)
					polyData.GetPointData().AddArray(colors);
				
				vtkPolyDataMapper mapper = new vtkPolyDataMapper();
				mapper.SetInputData(polyData);
				if (bundle) {
					mapper.ScalarVisibilityOn();
					mapper.SetScalarModeToUsePointFieldData();
					mapper.SelectColorArray("Colors");
				}
				
				actor.SetMapper(mapper);
				setActorProperties(actor, bundle, color, opacity);
			} else {
				if (currentBundle != null)
					currentBundle.modified();
			}
		}
		
		FaultSectionActorList list;
		if (bundle) {
			Preconditions.checkState(pts.GetNumberOfPoints() == colors.GetNumberOfTuples());
			list = new FaultSectionBundledActorList(fault, currentBundle, firstIndex, pts.GetNumberOfPoints()-firstIndex, myOpacity);
		} else {
			list = new FaultSectionActorList(fault);
			list.add(actor);
		}
		
		return list;
	}
	
	/**
	 * Sets common actor properties. Can be extended for custom actor properties when using createFaultActors above
	 * @param actor
	 * @param bundle
	 * @param color
	 * @param opacity
	 */
	protected void setActorProperties(vtkActor actor, boolean bundle, Color color, double opacity) {
		if (bundle) {
//			actor.GetProperty().SetOpacity(0.999); // needed to trick it to using a transparency enabled renderer
		} else {
			actor.GetProperty().SetColor(getColorDoubleArray(color));
			actor.GetProperty().SetOpacity(opacity);
		}
	}

}
