package org.scec.vtk.commons.opensha.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.util.ListUtils;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.gui.dist.VisibleFaultSurfacesProvider;
import org.scec.vtk.commons.opensha.tree.AbstractFaultNode;
import org.scec.vtk.commons.opensha.tree.events.CustomColorSelectionListener;

import com.google.common.base.Preconditions;

public class FaultHighlighter {
	
	private ArrayList<AbstractFaultNode> highlightedNodes = new ArrayList<AbstractFaultNode>();
	private ArrayList<Color> highlightedPrevColors = new ArrayList<Color>();
	private ArrayList<Color> highlightColors = new ArrayList<Color>();
	
	private VisibleFaultSurfacesProvider surfaceProv;
	private CustomColorSelectionListener customColorListener;
	
	public FaultHighlighter(VisibleFaultSurfacesProvider surfaceProv,
			CustomColorSelectionListener customColorListener) {
		this.surfaceProv = surfaceProv;
		this.customColorListener = customColorListener;
	}
	
	public void unHilight() {
		for (int i=0; i<highlightedNodes.size(); i++) {
			rollBackColor(highlightedNodes.get(i), highlightedPrevColors.get(i), highlightColors.get(i));
		}
		highlightedNodes.clear();
		highlightedPrevColors.clear();
		highlightColors.clear();
	}
	
	private void rollBackColor(AbstractFaultNode highlightedNode,
			Color highlightedNodePrevColor, Color highlightColor) {
		if (highlightedNode != null
				&& highlightedNodePrevColor != null
				&& highlightedNode.getColor().equals(highlightColor)) {
			highlightedNode.setColor(highlightedNodePrevColor);
		}
	}
	
	public void highlightFaults(List<Integer> faultIDs, Color color) {
		ArrayList<AbstractFaultSection> faults = new ArrayList<AbstractFaultSection>();
		ArrayList<Color> colors = new ArrayList<Color>();
		
		for (int id : faultIDs) {
			faults.add(surfaceProv.getFault(id));
			colors.add(color);
		}
		highlightFaults(faults, colors);
	}
	
	public void highlightFault(AbstractFaultSection fault, Color color) {
		highlightFaults(ListUtils.wrapInList(fault), ListUtils.wrapInList(color));
	}
	
	public void highlightFaults(List<AbstractFaultSection> faults, List<Color> colors) {
		Preconditions.checkArgument(faults.size() == colors.size(), "faults and colors sizes inconsistant!");
		for (int i=0; i<faults.size(); i++) {
			AbstractFaultSection fault = faults.get(i);
			AbstractFaultNode node = surfaceProv.getNode(fault);
			Color color = colors.get(i);
			
			if (highlightedNodes.contains(node)) {
				node.setColor(color);
				highlightColors.set(highlightedNodes.indexOf(node), color);
			} else {
				highlightedNodes.add(node);
				highlightedPrevColors.add(node.getColor());
				highlightColors.add(color);
				node.setColor(color);
				node.setVisible(true);
			}
		}
		
		customColorListener.customColorSelected();
	}

}
