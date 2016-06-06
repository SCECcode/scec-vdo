package org.scec.geo3d.library.wgcep.surfaces;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.commons.data.Container2DImpl;
import org.opensha.commons.data.Named;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;
import org.scec.geo3d.library.wgcep.surfaces.events.GeometrySettingsChangeListener;
import org.scec.geo3d.library.wgcep.surfaces.events.GeometrySettingsChangedEvent;
import org.scec.vtk.tools.Transform;

import vtk.vtkActor;

/**
 * This interface generates a Java3D <code>BranchGroup</code> from an OpenSHA <code>EvenlyGriddedSurfaceAPI</code>
 * object.
 * 
 * @author kevin
 *
 */
public abstract class GeometryGenerator implements Named {
	
	private String name;
	
	public GeometryGenerator(String name) {
		this.name = name;
	}
	
	private ArrayList<GeometrySettingsChangeListener> listeners = new ArrayList<GeometrySettingsChangeListener>();
	
	/**
	 * Builds a vtkActor containing geometry representing the given surface, and colored with the given colorer.
	 * 
	 * @param surface
	 * @param colorer
	 * @param fault
	 * @return
	 */
	public abstract vtkActor createFaultActor(RuptureSurface surface, Color color,
			AbstractFaultSection fault);
	
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
	public abstract boolean updateColor(vtkActor actor, Color color);
	
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

}
