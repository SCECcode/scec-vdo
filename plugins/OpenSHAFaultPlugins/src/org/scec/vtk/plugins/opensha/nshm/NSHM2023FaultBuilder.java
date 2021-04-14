package org.scec.vtk.plugins.opensha.nshm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.GeoJSONFaultReader;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.GeoJSONFaultReader.GeoSlipRateRecord;
import org.opensha.sha.faultSurface.FaultSection;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.tree.FaultCategoryNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;

public class NSHM2023FaultBuilder implements FaultTreeBuilder, ParameterChangeListener {
	
	private FileParameter loadParam;
	private FileParameter slipRatesParam;
	
	private ParameterList params;
	
	private ParameterList faultParams = PrefDataSection.createPrefDataParams();

	private TreeChangeListener l;
	
	private Map<String, List<FaultSection>> sects;
	
	public NSHM2023FaultBuilder() {
		params = new ParameterList();
		
		loadParam = new FileParameter("Load Fault Sections GeoJSON");
		loadParam.addParameterChangeListener(this);
		params.addParameter(loadParam);
		
		slipRatesParam = new FileParameter("Load/Map (Kludgy) Slip Rates");
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
		if (sects == null)
			return;
		List<String> states = new ArrayList<>(sects.keySet());
		Collections.sort(states);
		for (String state : states) {
			FaultCategoryNode stateNode = new FaultCategoryNode(state);
			List<FaultSection> stateSects = sects.get(state);
			for (FaultSection sect : stateSects) {
				PrefDataSection fault = new PrefDataSection(sect);
				
				// add it to the tree
				FaultSectionNode faultNode = new FaultSectionNode(fault);
				stateNode.add(faultNode);
			}
			root.add(stateNode);
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {
		if (e.getParameter() == loadParam) {
			sects = null;
			File sectsFile = loadParam.getValue();
			System.out.println("Loading fault sections from: "+sectsFile.getAbsolutePath());
			try {
				sects = GeoJSONFaultReader.readFaultSections(sectsFile, false);
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
					for (List<FaultSection> stateSects : sects.values())
						GeoJSONFaultReader.testMapSlipRates(stateSects, slipRates, Double.NaN, null);
				} catch (IOException e1) {
					throw ExceptionUtils.asRuntimeException(e1);
				}
			}
		}
		l.treeChanged(null);
	}

}
