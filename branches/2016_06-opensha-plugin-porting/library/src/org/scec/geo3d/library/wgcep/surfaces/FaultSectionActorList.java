package org.scec.geo3d.library.wgcep.surfaces;

import java.util.ArrayList;

import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;

import vtk.vtkActor;

/**
 * Inner class designed to allow pick behaviors to discern instances of FaultSection objects.
 * Wrapping a Shape3D in this way ensures future compatibility if pick behaviors
 * are added to other SCEC-VIDEO objects.
 * THIS IS JUST COPIED FROM FaultAccessor class in Fault3DPlugin. Ideally, it can be put in some
 * UTILS package.
 */
public class FaultSectionActorList extends ArrayList<vtkActor> {
	private AbstractFaultSection fault;
	
    public FaultSectionActorList(AbstractFaultSection fault) {
        this.fault = fault;
    }
    
    /**
     * Returns the display name of this <code>FaultShape3D</code>.
     * 
     * @return the display name of this fault
     */
    public String getInfo() {
        return fault.getInfo();
    }
    
    public AbstractFaultSection getFault() {
    	return fault;
    }
}