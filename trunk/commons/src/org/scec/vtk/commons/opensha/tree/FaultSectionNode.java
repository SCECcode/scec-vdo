package org.scec.vtk.commons.opensha.tree;

import java.awt.Color;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.tree.events.ColorChangeListener;
import org.scec.vtk.commons.opensha.tree.events.VisibilityChangeListener;

public class FaultSectionNode extends AbstractFaultNode {
	
	private ColorChangeListener colorChangeListener;
	private VisibilityChangeListener visibilityChangeListener;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private AbstractFaultSection fault;
	
	public FaultSectionNode(AbstractFaultSection fault) {
		super(fault);
		
		this.fault = fault;
	}
	
	@Override
	public String getName() {
		return fault.getName();
	}
	
	public String getInfo() {
		return fault.getInfoHTML();
	}
	
	public AbstractFaultSection getFault() {
		return fault;
	}

	public void setColorChangeListener(ColorChangeListener colorChangeListener) {
		this.colorChangeListener = colorChangeListener;
	}

	public void setVisibilityChangeListener(
			VisibilityChangeListener visibilityChangeListener) {
		this.visibilityChangeListener = visibilityChangeListener;
	}

	@Override
	public void setColor(Color color) {
		super.setColor(color);
		if (visibilityChangeListener != null)
			colorChangeListener.colorChanged(fault, color);
	}

	@Override
	public void setVisible(boolean visible) {
		boolean changed = visible != this.isVisible();
		super.setVisible(visible);
		if (visibilityChangeListener != null && changed)
			visibilityChangeListener.visibilityChanged(fault, visible);
	}

}
