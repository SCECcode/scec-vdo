package org.scec.geo3d.commons.opensha.tree.builders;

import javax.swing.JPanel;

import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;

public interface FaultSectionInfoViewier {
	
	/**
	 * Returns a panel displaying information for the given fault, or null if
	 * no such panel can be provided for the given fault.
	 * 
	 * @return
	 */
	public JPanel getInfoPanel(AbstractFaultSection fault);

}
