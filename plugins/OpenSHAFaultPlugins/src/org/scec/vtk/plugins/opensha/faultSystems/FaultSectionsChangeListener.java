package org.scec.vtk.plugins.opensha.faultSystems;

import java.util.List;

import org.opensha.sha.faultSurface.FaultSection;

public interface FaultSectionsChangeListener {
	
	public void setFaultSections(List<? extends FaultSection> sects);

}
