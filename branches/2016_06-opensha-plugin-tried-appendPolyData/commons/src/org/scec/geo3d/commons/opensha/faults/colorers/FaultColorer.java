package org.scec.geo3d.commons.opensha.faults.colorers;

import java.awt.Color;

import org.opensha.commons.data.Named;
import org.opensha.commons.param.ParameterList;
import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;

/**
 * This interface returns a color for each point on an OpenSHA <code>EvenlyGriddedSurfaceAPI</code>.
 * This is used to color faults for 3D display.
 * 
 * @author kevin
 *
 */
public interface FaultColorer extends Named {
	
	public Color getColor(AbstractFaultSection fault);
	
	public ParameterList getColorerParameters();
	
	public void setColorerChangeListener(ColorerChangeListener l);

}
