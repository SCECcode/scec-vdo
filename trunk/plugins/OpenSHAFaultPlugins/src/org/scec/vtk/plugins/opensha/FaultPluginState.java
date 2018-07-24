package org.scec.vtk.plugins.opensha;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.dom4j.Element;
import org.opensha.commons.exceptions.ConstraintException;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.commons.util.cpt.CPT;
import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.gui.ColorerPanel;
import org.scec.vtk.commons.opensha.gui.anim.MultiAnimPanel;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.plugins.PluginState;

import com.google.common.base.Preconditions;

public class FaultPluginState implements PluginState {
	
	private static final boolean D = false;
	
	private FaultPluginGUI gui;
	
	/*
	 * state items populated on deepCopy() for fromXML() via captureState() method
	 * all parameters/data are cloned and not references to original
	 */
	// builder/tree
	private ParameterList builderParams;
	private Map<Integer, Boolean> idToVisibleMap;
	private Map<Integer, Color> idToColorMap;
	// colorer
	private FaultColorer colorer;
	private ParameterList colorerParams;
	private CPT cpt;
	private boolean cptLog;
	private boolean legendVisibile;
	// animation
	// TODO store current step
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
		if (D) System.out.println("Loading state. EDT? "+SwingUtilities.isEventDispatchThread());
		// first set any builder params
		if (D) System.out.println("Loading builder params");
		updateParams(gui.getBuilder().getBuilderParams(), builderParams);
		
		// update fault params
		if (D) System.out.println("Loading fault params");
		updateParams(gui.getFaultParams(), faultParams);
		
		if (D) System.out.println("Loading geom gen");
		if (geomGen != gui.getGeomSelect().getSelectedGeomGen())
			gui.getGeomSelect().setSelectedGeomGen(geomGen);
		if (D) System.out.println("Loading geom gen parans");
		updateParams(gui.getGeomSelect().getSelectedGeomGen().getDisplayParams(), geomGenParams);
		
		if (anim != null && gui.getAnimPanel() != null) {
			if (D) System.out.println("Loading anim");
			if (anim != gui.getAnimPanel().getSelectedAnim())
				gui.getAnimPanel().setSelectedAnim(anim);
			waitOnColorerChange();
			if (D) System.out.println("Loading anim params");
			if (gui.getAnimPanel().getSelectedAnim() != null)
				updateParams(gui.getAnimPanel().getSelectedAnim().getAnimationParameters(), animParams);
			waitOnColorerChange();
		}
		
		// update colorer
		if (gui.getColorPanel() != null) {
			if (D) System.out.println("Loading colorer");
			if (colorer != gui.getColorPanel().getSelectedColorer())
				gui.getColorPanel().setSelectedColorer(colorer);
			if (D) System.out.println("Loading colorer params");
			waitOnColorerChange();
			if (colorer != null) {
				updateParams(gui.getColorPanel().getSelectedColorer().getColorerParameters(), colorerParams);
				if (cpt != null && gui.getColorPanel().getSelectedColorer() instanceof CPTBasedColorer) {
					if (D) System.out.println("Loading CPT info");
					CPTBasedColorer cptColor = (CPTBasedColorer) gui.getColorPanel().getSelectedColorer();
					if (!cpt.equals(cptColor.getCPT()) || cptLog != cptColor.isCPTLog()) {
						waitOnColorerChange();
						cptColor.setCPT(cpt, cptLog);
						gui.getColorPanel().cptChangedExternally();
					}
				}
				gui.getColorPanel().setLegendVisible(legendVisibile);
			}
		}
		
		// now update the tree itself
		waitOnColorerChange();
		if (D) System.out.println("Updating tree itself");
		if (updateTree(gui.getFaultTreeTable().getTreeRoot()))
			gui.getFaultTreeTable().refreshTreeView();
		if (D) System.out.println("DONE Loading state");
	}
	
	private void waitOnColorerChange() {
		try {
			// first wait on any colorer change events
			gui.getEventManager().waitOnCalcThread();
		} catch (InterruptedException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void updateParams(ParameterList to, ParameterList from) {
		if (to == null || from == null)
			return;
		for (Parameter fromParam : from) {
			try {
				Parameter toParam = to.getParameter(fromParam.getName());
				Object fromVal = fromParam.getValue();
				Object toVal = toParam.getValue();
				boolean equals = Objects.equals(fromVal, toVal);
				if (D) {
					System.out.println("Setting param "+fromParam.getName()+". Equals? "+equals);
					if (!equals) {
						System.out.println("\tOrig: "+toVal);
						System.out.println("\tNew: "+fromVal);
					}
				}
				if (equals)
					// do external equals check to avoid any spurious parameterChanged() calls for paramters
					// that don't check equals before firing an event (even though they should)
					continue;
//				System.out.println("From class: "+fromParam.getClass());
//				System.out.println("To class: "+toParam.getClass());
//				System.out.println("From constraint: "+fromParam.getConstraint());
//				System.out.println("To constraint: "+toParam.getConstraint());
				try {
					toParam.setValue(fromParam.getValue());
					toParam.getEditor().refreshParamEditor();
				} catch (ConstraintException e) {
					System.err.println("WARNING: Couldn't set parameter '"+fromParam.getName()+"': "+e.getMessage());
				}
			} catch (ParameterException e) {
				System.err.println("WARNING: No parameter exists named "+fromParam.getName()+", cannot fully restore state");
			}
		}
	}
	
	// XML constants

	private static final String BUILDER_PARAM_LIST_EL_NAME = "BuilderParams";
	private static final String FAULT_PARAM_LIST_EL_NAME = "FaultParams";
	private static final String GEOM_GEM_EL_NAME = "GeometryGenerator";
	private static final String GEOM_GEM_PARAM_LIST_EL_NAME = "GeometryGeneratorParams";
	private static final String ANIM_EL_NAME = "Animation";
	private static final String ANIM_PARAM_LIST_EL_NAME = "AnimationParams";
	private static final String COLORER_EL_NAME = "Colorer";
	private static final String COLORER_PARAM_LIST_EL_NAME = "ColorerParams";
	private static final String TREE_EL_NAME = "FaultTree";
	private static final String TREE_NODE_EL_NAME = "Node";

	@Override
	public void toXML(Element stateEl) {
		if (D) System.out.println("toXML: capturing tree");
		captureState();
		if (D) System.out.println("toXML: done capturing tree");
		
		if (builderParams != null && !builderParams.isEmpty()) {
			if (D) System.out.println("toXML: capturing builder params");
			paramListToXML(stateEl, builderParams, BUILDER_PARAM_LIST_EL_NAME);
		}
		
		if (faultParams != null && !faultParams.isEmpty()) {
			if (D) System.out.println("toXML: capturing fault params");
			paramListToXML(stateEl, faultParams, BUILDER_PARAM_LIST_EL_NAME);
		}
		
		Element geomGenEl = stateEl.addElement(GEOM_GEM_EL_NAME);
		if (D) System.out.println("toXML: capturing geometry generator");
		geomGenEl.addAttribute("Name", geomGen.getName());
		if (geomGenParams != null)
			paramListToXML(geomGenEl, geomGenParams, GEOM_GEM_PARAM_LIST_EL_NAME);
		
		if (anim != null && gui.getAnimPanel() != null && gui.getAnimPanel().getSelectedAnim() != null) {
			if (D) System.out.println("toXML: capturing animation");
			if (D) System.out.println("Loading anim");
			Element animEl = stateEl.addElement(ANIM_EL_NAME);
			animEl.addAttribute("Name", anim.getName());
			
			if (animParams != null)
				paramListToXML(animEl, animParams, ANIM_PARAM_LIST_EL_NAME);
		}
		
		if (gui.getColorPanel() != null) {
			if (D) System.out.println("toXML: capturing colorer");
			Element colorerEl = stateEl.addElement(COLORER_EL_NAME);
			if (colorer == null) {
				colorerEl.addAttribute("Name", ColorerPanel.COLORER_SELECTOR_CUSTOM);
			} else {
				colorerEl.addAttribute("Name", colorer.getName());
				if (colorerParams != null)
					paramListToXML(colorerEl, colorerParams, COLORER_PARAM_LIST_EL_NAME);
				if (cpt != null && gui.getColorPanel().getSelectedColorer() instanceof CPTBasedColorer) {
					cpt.toXMLMetadata(colorerEl);
					colorerEl.addAttribute("CPTLog", cptLog+"");
				}
				colorerEl.addAttribute("LegendVisible", legendVisibile+"");
			}
		}
		
		if (D) System.out.println("toXML: capturing tree");
		Element treeEl = stateEl.addElement(TREE_EL_NAME);
		for (Integer id : idToColorMap.keySet()) {
			Element nodeEl = treeEl.addElement(TREE_NODE_EL_NAME);
			nodeEl.addAttribute("ID", id+"");
			Color color = idToColorMap.get(id);
			Boolean visible = idToVisibleMap.get(id);
			nodeEl.addAttribute("Color", color.getRGB()+"");
			nodeEl.addAttribute("Visible", visible.toString());
		}
		
		if (D) System.out.println("toXML: done");
	}
	
	private static void paramListToXML(Element rootEl, ParameterList paramList, String elName) {
		Element paramsEl = rootEl.addElement(elName);
		for (Parameter<?> param : paramList)
			param.toXMLMetadata(paramsEl);
	}

	@Override
	public void fromXML(Element stateEl) {
		if (D) System.out.println("fromXML: capturing state");
		captureState();
		if (D) System.out.println("fromXML: done capturing state");
		
		Element builderParamsEl = stateEl.element(BUILDER_PARAM_LIST_EL_NAME);
		if (builderParamsEl != null) {
			if (D) System.out.println("fromXML: loading builder params");
			paramListFromXML(builderParamsEl, builderParams);
		}
		
		Element faultParamsEl = stateEl.element(FAULT_PARAM_LIST_EL_NAME);
		if (faultParamsEl != null) {
			if (D) System.out.println("fromXML: loading fault params");
			paramListFromXML(faultParamsEl, faultParams);
		}
		
		Element geomGenEl = stateEl.element(GEOM_GEM_EL_NAME);
		if (D) System.out.println("fromXML: loading geometry generator");
		String geomGenName = geomGenEl.attributeValue("Name");
		for (GeometryGenerator geomGen : gui.getGeomSelect().getAllGeomGens()) {
			if (geomGen.getName().equals(geomGenName)) {
				this.geomGen = geomGen;
				geomGenParams = cloneParamList(geomGen.getDisplayParams());
				Element geomGenParamsEl = geomGenEl.element(GEOM_GEM_PARAM_LIST_EL_NAME);
				if (geomGenParamsEl != null)
					paramListFromXML(geomGenParamsEl, geomGenParams);
				break;
			}
		}
		
		Element animEl = stateEl.element(ANIM_EL_NAME);
		if (animEl != null) {
			if (D) System.out.println("fromXML: loading animation");
			String animName = animEl.attributeValue("Name");
			MultiAnimPanel animPanel = gui.getAnimPanel();
			for (FaultAnimation anim : animPanel.getAnimations()) {
				if (anim.getName().equals(animName)) {
					this.anim = anim;
					animParams = cloneParamList(anim.getAnimationParameters());
					Element animParamsEl = animEl.element(ANIM_PARAM_LIST_EL_NAME);
					if (animParamsEl != null)
						paramListFromXML(animParamsEl, animParams);
					break;
				}
			}
		}
		
		Element colorerEl = stateEl.element(COLORER_EL_NAME);
		if (colorerEl != null) {
			if (D) System.out.println("fromXML: loading colorer");
			String colorerName = colorerEl.attributeValue("Name");
			if (colorerName.equals(ColorerPanel.COLORER_SELECTOR_CUSTOM)) {
				colorer = null;
			} else {
				for (FaultColorer colorer : gui.getColorPanel().getColorers()) {
					if (colorer.getName().equals(colorerName)) {
						this.colorer = colorer;
						colorerParams = cloneParamList(colorer.getColorerParameters());
						Element colorerParamsEl = colorerEl.element(COLORER_PARAM_LIST_EL_NAME);
						if (colorerParamsEl != null)
							paramListFromXML(colorerParamsEl, colorerParams);
						Element cptEl = colorerEl.element(CPT.XML_METADATA_NAME);
						if (cptEl != null) {
							Preconditions.checkState(colorer instanceof CPTBasedColorer);
							cpt = CPT.fromXMLMetadata(cptEl);
							cptLog = Boolean.parseBoolean(colorerEl.attributeValue("CPTLog"));
						}
						legendVisibile = Boolean.parseBoolean(colorerEl.attributeValue("LegendVisible"));
						break;
					}
				}
			}
		}
		
		Element treeEl = stateEl.element(TREE_EL_NAME);
		if (D) System.out.println("fromXML: loading tree");
		for (Element nodeEl : XMLUtils.getSubElementsList(treeEl, TREE_NODE_EL_NAME)) {
			Integer id = Integer.parseInt(nodeEl.attributeValue("ID"));
			int rgb = Integer.parseInt(nodeEl.attributeValue("Color"));
			Color color = new Color(rgb);
			Boolean visible = Boolean.parseBoolean(nodeEl.attributeValue("Visible"));
			idToColorMap.put(id, color);
			idToVisibleMap.put(id, visible);
		}
		if (D) System.out.println("fromXML: done");
	}
	
	private static void paramListFromXML(Element paramListEl, ParameterList params) {
		boolean success = ParameterList.setParamsInListFromXML(params, paramListEl);
		if (!success)
			System.err.println("Warning: failed to set one or more parameters from XML");
	}
	
	private void clearState() {
		if (D) System.out.println("Clearing state");
		builderParams = null;
		idToVisibleMap = null;
		idToColorMap = null;
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
		if (D) System.out.println("Capturing state");
		builderParams = cloneParamList(gui.getBuilder().getBuilderParams());
		idToVisibleMap = new HashMap<>();
		idToColorMap = new HashMap<>();
		captureTree(gui.getFaultTreeTable().getTreeRoot(), idToVisibleMap, idToColorMap);
		if (gui.getColorPanel() != null) {
			colorer = gui.getColorPanel().getSelectedColorer();
			if (colorer != null) {
				colorerParams = cloneParamList(colorer.getColorerParameters());
				if (colorer instanceof CPTBasedColorer) {
					cpt = ((CPTBasedColorer)colorer).getCPT();
					if (cpt != null)
						cpt = (CPT)cpt.clone();
					cptLog = ((CPTBasedColorer)colorer).isCPTLog();
				}
			}
			legendVisibile = gui.getColorPanel().isLegendVisible();
		}
		if (gui.getAnimPanel() != null) {
			anim = gui.getAnimPanel().getSelectedAnim();
			if (anim != null)
				animParams = cloneParamList(anim.getAnimationParameters());
		}
		geomGen = gui.getGeomSelect().getSelectedGeomGen();
		geomGenParams = cloneParamList(geomGen.getDisplayParams());
		faultParams = cloneParamList(gui.getFaultParams());
		if (D) System.out.println("DONE Capturing state");
	}
	
	private static ParameterList cloneParamList(ParameterList params) {
		if (params == null)
			return null;
		return (ParameterList)params.clone();
	}
	
	/**
	 * Recursively captures all AbstractFaultNode data into the given map. Assumes (and checks) that ID field
	 * of tree nodes are unique
	 * @param map
	 * @param node
	 * @param userDataToIDMap2 
	 */
	private static void captureTree(DefaultMutableTreeNode node, Map<Integer, Boolean> visibilityMap,
			Map<Integer, Color> colorMap) {
//		System.out.println("Capture: "+node);
		if (node instanceof FaultSectionNode) {
			FaultSectionNode faultNode = (FaultSectionNode)node;
			Integer id = faultNode.getFault().getId();
			Preconditions.checkState(!visibilityMap.containsKey(id), "Duplicate ID entry in tree: "+id);
			visibilityMap.put(id, faultNode.isVisible());
			colorMap.put(id, faultNode.getColor());
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
			Integer id = faultNode.getFault().getId();
			if (idToColorMap.containsKey(id)) {
//				System.out.println("Udating "+faultNode.getName());
				Color color = idToColorMap.get(id);
				boolean visible = idToVisibleMap.get(id);
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
		o.idToVisibleMap = idToVisibleMap;
		o.idToColorMap = idToColorMap;
		o.colorer = colorer;
		o.colorerParams = colorerParams;
		o.cpt = cpt;
		o.cptLog = cptLog;
		o.legendVisibile = legendVisibile;
		o.anim = anim;
		o.animParams = animParams;
		o.geomGen = geomGen;
		o.geomGenParams = geomGenParams;
		o.faultParams = faultParams;
		
		return o;
	}

}
