package org.scec.vtk.commons.opensha.gui.dist;

import java.util.HashMap;

import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.tree.AbstractFaultNode;

public interface VisibleFaultSurfacesProvider {
	
	public HashMap<AbstractFaultSection, Surface3D> getVisibleSurfaces();
	
	public AbstractFaultNode getNode(AbstractFaultSection fault);
	
	public AbstractFaultSection getFault(int id);

}
