package org.scec.vtk.plugins.opensha;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.dom4j.Element;
import org.opensha.commons.exceptions.ConstraintException;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.util.cpt.CPT;
import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.plugins.PluginState;

import com.google.common.base.Preconditions;

public class FaultPluginState implements PluginState {
	
	private FaultPluginGUI gui;
	
	/*
	 * state items populated on deepCopy() for fromXML() via captureState() method
	 * all parameters/data are cloned and not references to original
	 */
	// builder/tree
	private ParameterList builderParams;
	private Map<Object, Boolean> userDataToVisibleMap;
	private Map<Object, Color> userDataToColorMap;
	// colorer
	private FaultColorer colorer;
	private ParameterList colorerParams;
	private CPT cpt;
	private boolean cptLog;
	// animation
	private FaultAnimation anim;
	private ParameterList animParams;
	// geometry generator
	private GeometryGenerator geomGen;
	private ParameterList geomGenParams;
	// fault params
	private ParameterList faultParams;
	
	public FaultPluginState(FaultPluginGUI gui) {
		this.gui = gui;
	}

	@Override
	public void load() {
		// first set any builder params
		updateParams(gui.getBuilder().getBuilderParams(), builderParams);
		
		// update fault params
		updateParams(gui.getFaultParams(), faultParams);
		
		if (geomGen != gui.getGeomSelect().getSelectedGeomGen())
			gui.getGeomSelect().setSelectedGeomGen(geomGen);
		updateParams(gui.getGeomSelect().getSelectedGeomGen().getDisplayParams(), geomGenParams);
		
		if (anim != null && gui.getAnimPanel() != null) {
			if (anim != gui.getAnimPanel().getSelectedAnim())
				gui.getAnimPanel().setSelectedAnim(anim);
			if (gui.getAnimPanel().getSelectedAnim() != null)
				updateParams(gui.getAnimPanel().getSelectedAnim().getAnimationParameters(), animParams);
		}
		
		// update colorer
		if (colorer != gui.getColorPanel().getSelectedColorer())
			gui.getColorPanel().setSelectedColorer(colorer);
		if (cpt != null && gui.getColorPanel().getSelectedColorer() instanceof CPTBasedColorer) {
			CPTBasedColorer cptColor = (CPTBasedColorer) gui.getColorPanel().getSelectedColorer();
			if (!cpt.equals(cptColor.getCPT()) || cptLog != cptColor.isCPTLog()) {
				cptColor.setCPT(cpt, cptLog);
				gui.getColorPanel().cptChangedExternally();
			}
		}
		updateParams(gui.getColorPanel().getSelectedColorer().getColorerParameters(), colorerParams);
		
		// now update the tree itself
		if (updateTree(gui.getFaultTreeTable().getTreeRoot()))
			gui.getFaultTreeTable().refreshTreeView();
	}
	
	private static void updateParams(ParameterList to, ParameterList from) {
		if (to == null || from == null)
			return;
		for (Parameter fromParam : from) {
			try {
				Parameter toParam = to.getParameter(fromParam.getName());
				try {
					toParam.setValue(fromParam.getValue());
				} catch (ConstraintException e) {
					System.err.println("WARNING: Couldn't set parameter '"+fromParam.getName()+"': "+e.getMessage());
				}
			} catch (ParameterException e) {
				System.err.println("WARNING: No parameter exists named "+fromParam.getName()+", cannot fully restore state");
			}
		}
	}

	@Override
	public void toXML(Element stateEl) {
		// TODO
	}

	@Override
	public void fromXML(Element stateEl) {
		// TODO
	}
	
	private void clearState() {
		builderParams = null;
		userDataToVisibleMap = null;
		userDataToColorMap = null;
		colorer = null;
		colorerParams = null;
		cpt = null;
		anim = null;
		animParams = null;
		geomGen = null;
		geomGenParams = null;
		faultParams = null;
	}
	
	private void captureState() {
		clearState();
		
		builderParams = cloneParamList(gui.getBuilder().getBuilderParams());
		userDataToVisibleMap = new HashMap<>();
		userDataToColorMap = new HashMap<>();
		captureTree(gui.getFaultTreeTable().getTreeRoot(), userDataToVisibleMap, userDataToColorMap);
		colorer = gui.getColorPanel().getSelectedColorer();
		if (colorer != null) {
			colorerParams = cloneParamList(colorer.getColorerParameters());
			if (colorer instanceof CPTBasedColorer) {
				cpt = (CPT)((CPTBasedColorer)colorer).getCPT().clone();
				cptLog = ((CPTBasedColorer)colorer).isCPTLog();
			}
		}
		if (gui.getAnimPanel() != null) {
			anim = gui.getAnimPanel().getSelectedAnim();
			if (anim != null)
				animParams = cloneParamList(anim.getAnimationParameters());
		}
		geomGen = gui.getGeomSelect().getSelectedGeomGen();
		geomGenParams = cloneParamList(geomGen.getDisplayParams());
		faultParams = cloneParamList(geomGen.getDisplayParams());
	}
	
	private static ParameterList cloneParamList(ParameterList params) {
		if (params == null)
			return null;
		return (ParameterList)params.clone();
	}
	
	/**
	 * Recursively captures all AbstractFaultNode data into the given map. Assumes (and checks) that userObject field
	 * of tree nodes are unique
	 * @param map
	 * @param node
	 */
	private static void captureTree(DefaultMutableTreeNode node, Map<Object, Boolean> visibilityMap,
			Map<Object, Color> colorMap) {
//		System.out.println("Capture: "+node);
		if (node instanceof FaultSectionNode) {
			Object userObject = node.getUserObject();
			FaultSectionNode faultNode = (FaultSectionNode)node;
			Preconditions.checkState(!visibilityMap.containsKey(userObject), "Duplicate user object entry in tree: "+userObject);
			visibilityMap.put(userObject, faultNode.isVisible());
			colorMap.put(userObject, faultNode.getColor());
		}
		for (int i=0; i<node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			if (child instanceof DefaultMutableTreeNode)
				captureTree((DefaultMutableTreeNode)child, visibilityMap, colorMap);
		}
	}
	
	private boolean updateTree(DefaultMutableTreeNode node) {
		boolean changed = false;
//		System.out.println("Update: "+node);
		if (node instanceof FaultSectionNode) {
			FaultSectionNode faultNode = (FaultSectionNode)node;
			if (userDataToColorMap.containsKey(node.getUserObject())) {
//				System.out.println("Udating "+faultNode.getName());
				Color color = userDataToColorMap.get(node.getUserObject());
				boolean visible = userDataToVisibleMap.get(node.getUserObject());
				// event manager is already listening for changes to the following so updates will happen automatically
				// use Objects.equals to handle null cases
				if (!Objects.equals(color, faultNode.getColor())) {
//					System.out.println("Setting color "+faultNode.getName());
					changed = true;
					faultNode.setColor(color);
				}
				if (visible != faultNode.isVisible()) {
//					System.out.println("Setting visibility "+visible+" "+faultNode.getName());
					changed = true;
					faultNode.setVisible(visible);
				}
			}
		}
//		System.out.println("Iterating over "+node.getChildCount()+" children");
		for (int i=0; i<node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
//			System.out.println("Child "+i+": "+child);
			if (child instanceof DefaultMutableTreeNode)
				changed = updateTree((DefaultMutableTreeNode)child) || changed;
		}
		return changed;
	}

	@Override
	public FaultPluginState deepCopy() {
		FaultPluginState o = new FaultPluginState(gui);
		
		captureState();
		// these are never modified, just replaced so no cloning needed
		o.builderParams = builderParams;
		o.userDataToVisibleMap = userDataToVisibleMap;
		o.userDataToColorMap = userDataToColorMap;
		o.colorer = colorer;
		o.colorerParams = colorerParams;
		o.cpt = cpt;
		o.cptLog = cptLog;
		o.anim = anim;
		o.animParams = animParams;
		o.geomGen = geomGen;
		o.geomGenParams = geomGenParams;
		o.faultParams = faultParams;
		
		return o;
	}

}
