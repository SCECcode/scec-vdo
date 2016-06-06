package org.scec.geo3d.library.wgcep.gui.dist;

import java.util.HashMap;

import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;
import org.scec.geo3d.library.wgcep.tree.AbstractFaultNode;

public interface VisibleFaultSurfacesProvider {
	
	public HashMap<AbstractFaultSection, RuptureSurface> getVisibleSurfaces();
	
	public AbstractFaultNode getNode(AbstractFaultSection fault);
	
	public AbstractFaultSection getFault(int id);

}
