package org.scec.vtk.plugins.opensha.nshm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.earthquake.faultSysSolution.modules.PolygonFaultGridAssociations;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.GeoJSONFaultReader;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.GeoJSONFaultReader.GeoSlipRateRecord;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.faultSurface.GeoJSONFaultSection;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.tree.FaultCategoryNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;

public class GeoJSONFaultBuilder implements FaultTreeBuilder, ParameterChangeListener {
	
	private FileParameter loadParam;
	private FileParameter slipRatesParam;
	
	private ParameterList params;
	
	private ParameterList faultParams = PrefDataSection.createPrefDataParams();

	private TreeChangeListener l;
	
	private List<GeoJSONFaultSection> sects;
	
	public GeoJSONFaultBuilder() {
		params = new ParameterList();
		
		loadParam = new FileParameter("Load Fault Sections GeoJSON");
		loadParam.addParameterChangeListener(this);
		params.addParameter(loadParam);
		
		slipRatesParam = new FileParameter("Load/Map (Kludgy) Slip Rates GeoDB");
		slipRatesParam.addParameterChangeListener(this);
		params.addParameter(slipRatesParam);
	}

	@Override
	public ParameterList getBuilderParams() {
		return params;
	}

	@Override
	public ParameterList getFaultParams() {
		return faultParams;
	}

	@Override
	public void setTreeChangeListener(TreeChangeListener l) {
		this.l = l;
	}

	@Override
	public void buildTree(DefaultMutableTreeNode root) {
		if (sects == null || sects.isEmpty())
			return;
		boolean hasStates = false;
		for (GeoJSONFaultSection sect : sects) {
			if (sect.getProperty("State") != null || sect.getProperty("PrimState") != null) {
				hasStates = true;
				break;
			}
		}
		if (hasStates) {
			List<String> states = new ArrayList<>();
			String defaultState = "Other";
			Map<String, List<GeoJSONFaultSection>> statesMap = new HashMap<>();
			for (GeoJSONFaultSection sect : sects) {
				String state = sect.getProperty("State", null);
				if (state == null || state.isBlank())
					state = sect.getProperty("PrimState", null);
				if (state == null || state.isBlank())
					state = defaultState;
				if (statesMap.containsKey(state)) {
					statesMap.get(state).add(sect);
				} else {
					if (!state.equals(defaultState))
						states.add(state);
					List<GeoJSONFaultSection> sects = new ArrayList<>();
					sects.add(sect);
					statesMap.put(state, sects);
				}
			}
			Collections.sort(states);
			if (statesMap.containsKey(defaultState))
				// add at end
				states.add(defaultState);
			for (String state : states) {
				FaultCategoryNode stateNode = new FaultCategoryNode(state);
				for (FaultSection sect : statesMap.get(state)) {
					PrefDataSection fault = new PrefDataSection(sect);
					
					// add it to the tree
					FaultSectionNode faultNode = new FaultSectionNode(fault);
					stateNode.add(faultNode);
				}
				root.add(stateNode);
			}
		} else {
			for (FaultSection sect : sects) {
				PrefDataSection fault = new PrefDataSection(sect);
				
				// add it to the tree
				FaultSectionNode faultNode = new FaultSectionNode(fault);
				root.add(faultNode);
			}
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {
		if (e.getParameter() == loadParam) {
			sects = null;
			File sectsFile = loadParam.getValue();
			System.out.println("Loading fault sections from: "+sectsFile.getAbsolutePath());
			try {
//				sects = GeoJSONFaultReader.readFaultSections(sectsFile, false);
				sects = GeoJSONFaultReader.readFaultSections(sectsFile);
			} catch (IOException e1) {
				throw ExceptionUtils.asRuntimeException(e1);
			}
			if (slipRatesParam.getValue() != null) {
				slipRatesParam.removeParameterChangeListener(this);
				slipRatesParam.setValue(null);
				slipRatesParam.getEditor().refreshParamEditor();
				slipRatesParam.addParameterChangeListener(this);
			}
		} else if (e.getParameter() == slipRatesParam) {
			File slipRateFile = slipRatesParam.getValue();
			if (sects != null && slipRateFile != null) {
				System.out.println("Loading slip rate data from: "+slipRateFile.getAbsolutePath());
				try {
					Map<Integer, List<GeoSlipRateRecord>> slipRates = GeoJSONFaultReader.readGeoDB(slipRateFile);
					GeoJSONFaultReader.testMapSlipRates(sects, slipRates, Double.NaN, null);
				} catch (IOException e1) {
					throw ExceptionUtils.asRuntimeException(e1);
				}
			}
		}
		l.treeChanged(null);
	}

}
