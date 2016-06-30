package org.scec.vtk.commons.opensha.surfaces;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.Container2DImpl;
import org.opensha.commons.data.Named;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.surfaces.events.GeometrySettingsChangeListener;
import org.scec.vtk.commons.opensha.surfaces.events.GeometrySettingsChangedEvent;
import org.scec.vtk.tools.Transform;
import org.scec.vtk.tools.picking.PickHandler;

import com.google.common.base.Preconditions;

import vtk.vtkActor;
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
	
	/**
	 * Builds a vtkActor containing geometry representing the given surface, and colored with the given colorer.
	 * 
	 * @param surface
	 * @param colorer
	 * @param fault
	 * @return
	 */
	public abstract FaultSectionActorList createFaultActors(RuptureSurface surface, Color color,
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
	 * @param actor
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
//				System.out.println("Leaving bundle synchronized block (updateColor)");
			}
//			System.out.println("Left bundle synchronized block (updateColor)");
			return true;
		} else {
			for (vtkActor actor : actorList)
				actor.GetProperty().SetColor(getColorDoubleArray(color));
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
	
	protected static double[] getPointForLoc(Location loc) {
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
	
	public void clearBundles() {
		bundler.clearBundles();
	}
	
	public void setPickHandler(PickHandler<AbstractFaultSection> pickHandler) {
		this.pickHandler = pickHandler;
	}
	
	public PickHandler<AbstractFaultSection> getPickHandler() {
		return pickHandler;
	}

}
