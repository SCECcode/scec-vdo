package org.scec.geo3d.library.wgcep.surfaces;

import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Shape3D;

import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;

/**
 * Inner class designed to allow pick behaviors to discern instances of FaultSection objects.
 * Wrapping a Shape3D in this way ensures future compatibility if pick behaviors
 * are added to other SCEC-VIDEO objects.
 * THIS IS JUST COPIED FROM FaultAccessor class in Fault3DPlugin. Ideally, it can be put in some
 * UTILS package.
 */
public class FaultSectionShape3D extends Shape3D {
	private AbstractFaultSection fault;
	
    public FaultSectionShape3D(GeometryArray ga, Appearance app, AbstractFaultSection fault) {
        super(ga, app);
        ga.setCapability(GeometryArray.ALLOW_COUNT_READ);
        ga.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        ga.setCapability(GeometryArray.ALLOW_FORMAT_READ);
        ga.setCapability(Geometry.ALLOW_INTERSECT);
        this.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
        app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
        this.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
        this.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        this.fault = fault;
    }
    
  
    public void hLight(){
    	//faultSection.hLight();
    }
    
    public void unhLight(){
    	//faultSection.unhLight();
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