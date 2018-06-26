package org.scec.vtk.commons.opensha.tree.gui;

import java.awt.Color;

import javax.swing.Icon;

import org.netbeans.swing.outline.RenderDataProvider;
import org.scec.vtk.commons.opensha.tree.AbstractFaultNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;

public class FaultRenderDataProvider implements RenderDataProvider {

	@Override
	public Color getBackground(Object arg0) {
		return null;
	}

	@Override
	public String getDisplayName(Object arg0) {
		if (arg0 instanceof AbstractFaultNode) {
			return ((AbstractFaultNode)arg0).getName();
		}
		return null;
	}

	@Override
	public Color getForeground(Object arg0) {
		return null;
	}

	@Override
	public Icon getIcon(Object arg0) {
		return null;
	}

	@Override
	public String getTooltipText(Object arg0) {
		if (arg0 instanceof AbstractFaultNode) {
			return ((AbstractFaultNode)arg0).getInfo();
		}
		return null;
	}

	@Override
	public boolean isHtmlDisplayName(Object arg0) {
		return false;
	}
	

}
