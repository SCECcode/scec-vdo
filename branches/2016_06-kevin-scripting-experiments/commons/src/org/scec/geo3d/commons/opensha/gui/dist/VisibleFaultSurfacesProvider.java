package org.scec.geo3d.commons.opensha.gui.dist;

import java.util.HashMap;

import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;
import org.scec.geo3d.commons.opensha.tree.AbstractFaultNode;

public interface VisibleFaultSurfacesProvider {
	
	public HashMap<AbstractFaultSection, RuptureSurface> getVisibleSurfaces();
	
	public AbstractFaultNode getNode(AbstractFaultSection fault);
	
	public AbstractFaultSection getFault(int id);

}
