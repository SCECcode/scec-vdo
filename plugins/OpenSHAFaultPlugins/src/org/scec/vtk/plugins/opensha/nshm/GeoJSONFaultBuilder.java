package org.scec.vtk.plugins.opensha.nshm;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.impl.ParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.earthquake.faultSysSolution.modules.PolygonFaultGridAssociations;
import org.opensha.sha.earthquake.faultSysSolution.util.SubSectionBuilder;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.GeoJSONFaultReader;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.GeoJSONFaultReader.GeoDBSlipRateRecord;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.faultSurface.GeoJSONFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.AseismicityColorer;
import org.scec.vtk.commons.opensha.faults.colorers.CouplingCoefficientColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.tree.FaultCategoryNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;
import org.scec.vtk.tools.RememberedFileParameter;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.opensha.FaultPluginGUI;
import org.scec.vtk.plugins.opensha.ucerf3Rups.FaultSectionsChangeListener;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.DistinctSectColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.StiffnessColorer;

public class GeoJSONFaultBuilder implements FaultTreeBuilder, ParameterChangeListener {
	
	private RememberedFileParameter loadParam;
	private ButtonParameter reloadButton;
	private ButtonParameter subsectionButton;

	private final IntegerParameter minPerFaultParam = new IntegerParameter("Minimum Subsections Per Fault",
			1, Integer.MAX_VALUE, Integer.valueOf(SubSectionBuilder.MIN_SUB_SECTS_PER_FAULT_DEFAULT));
	private final DoubleParameter ddwFractParam = new DoubleParameter("DDW Fraction",
			SubSectionBuilder.DOWN_DIP_FRACT_DEFAULT);
	private final DoubleParameter fixedLengthParam = new DoubleParameter("Fixed Length", "km",
			SubSectionBuilder.MAX_LEN_DEFAULT);
	private final ParameterList subsectionParams = new ParameterList();
//	private RememberedFileParameter slipRatesParam;
	
	private ParameterList params;
	
	private ParameterList faultParams = PrefDataSection.createPrefDataParams();

	private ArrayList<FaultColorer> colorers;
	private ArrayList<FaultSectionsChangeListener> sectChangeListeners;

	private TreeChangeListener l;
	
	private List<GeoJSONFaultSection> sects;
	// Rebuild from the file's sections each time, rather than subdividing the previous result again.
	private List<GeoJSONFaultSection> loadedSects;
	
	public GeoJSONFaultBuilder(Plugin plugin) {
		params = new ParameterList();
		
		loadParam = new RememberedFileParameter("Load Fault Sections GeoJSON",
				GeoJSONFaultBuilder.class, "faultSectionsDirectory");
		loadParam.addParameterChangeListener(this);
		params.addParameter(loadParam);
		
		reloadButton = new ButtonParameter("Reload GeoJSON File?", "Reload");
		reloadButton.addParameterChangeListener(this);
		reloadButton.getEditor().setEnabled(false);
		params.addParameter(reloadButton);

		subsectionButton = new ButtonParameter("Subsection Faults", "Subsection...");
		subsectionButton.addParameterChangeListener(this);
		subsectionButton.getEditor().setEnabled(false);
		params.addParameter(subsectionButton);

		minPerFaultParam.setDefaultValue(SubSectionBuilder.MIN_SUB_SECTS_PER_FAULT_DEFAULT);
		ddwFractParam.setDefaultValue(SubSectionBuilder.DOWN_DIP_FRACT_DEFAULT);
		fixedLengthParam.setDefaultValue(SubSectionBuilder.MAX_LEN_DEFAULT);
		minPerFaultParam.setInfo("Minimum number of subsections for each loaded fault, regardless of length.");
		ddwFractParam.setInfo("Maximum subsection length as a fraction of down-dip width; NaN or 0 disables this limit.");
		fixedLengthParam.setInfo("Maximum subsection length in kilometers; NaN or 0 disables this limit.");
		subsectionParams.addParameter(minPerFaultParam);
		subsectionParams.addParameter(ddwFractParam);
		subsectionParams.addParameter(fixedLengthParam);
		
//		slipRatesParam = new RememberedFileParameter("Load/Map (Kludgy) Slip Rates GeoDB",
//				GeoJSONFaultBuilder.class, "slipRatesDirectory");
//		slipRatesParam.addParameterChangeListener(this);
//		params.addParameter(slipRatesParam);
		
		sectChangeListeners = new ArrayList<>();
		colorers = FaultPluginGUI.createDefaultColorers();
		colorers.add(new AseismicityColorer());
		colorers.add(new CouplingCoefficientColorer());
		StiffnessColorer stiff = new StiffnessColorer(plugin.getPluginActors());
		colorers.add(stiff);
		sectChangeListeners.add(stiff);
		
		DistinctSectColorer distinctColorer = new DistinctSectColorer();
		colorers.add(distinctColorer);
		sectChangeListeners.add(distinctColorer);
	}
	
	public ArrayList<FaultColorer> getColorers() {
		return colorers;
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
		if (sects == null || sects.isEmpty()) {
			fireSectsNullEvent();
			return;
		}
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
		fireSectsChangedEvent();
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {
		if (e.getParameter() == subsectionButton) {
			showSubsectionOptions();
			return;
		}
		if (e.getParameter() == loadParam || e.getParameter() == reloadButton) {
			sects = null;
			loadedSects = null;
			File sectsFile = loadParam.getValue();
			System.out.println("Loading fault sections from: "+sectsFile.getAbsolutePath());
			try {
//				sects = GeoJSONFaultReader.readFaultSections(sectsFile, false);
				loadedSects = GeoJSONFaultReader.readFaultSections(sectsFile);
				sects = loadedSects;
				loadParam.rememberLoadedFile(sectsFile);
			} catch (IOException e1) {
				throw ExceptionUtils.asRuntimeException(e1);
			}
//			if (slipRatesParam.getValue() != null) {
//				slipRatesParam.removeParameterChangeListener(this);
//				slipRatesParam.setValue(null);
//				slipRatesParam.getEditor().refreshParamEditor();
//				slipRatesParam.addParameterChangeListener(this);
//			}
//		} else if (e.getParameter() == slipRatesParam) {
//			File slipRateFile = slipRatesParam.getValue();
//			if (sects != null && slipRateFile != null) {
//				System.out.println("Loading slip rate data from: "+slipRateFile.getAbsolutePath());
//				try {
//					Map<Integer, List<GeoDBSlipRateRecord>> slipRates = GeoJSONFaultReader.readGeoDB(slipRateFile);
//					GeoJSONFaultReader.testMapSlipRates(sects, slipRates, Double.NaN, null);
//					slipRatesParam.rememberLoadedFile(slipRateFile);
//				} catch (IOException e1) {
//					throw ExceptionUtils.asRuntimeException(e1);
//				}
//			}
		}
		l.treeChanged(null);
		reloadButton.getEditor().setEnabled(loadParam.getValue() != null);
		reloadButton.getEditor().refreshParamEditor();
		subsectionButton.getEditor().setEnabled(loadedSects != null && !loadedSects.isEmpty());
	}

	private void showSubsectionOptions() {
		if (loadedSects == null || loadedSects.isEmpty())
			return;
		ParameterListEditor editor = new ParameterListEditor(subsectionParams);
		editor.setTitle("Subsectioning Options");
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(editor, BorderLayout.CENTER);
		panel.add(new JLabel("<html>Use NaN or 0 to disable either length limit.<br>"
				+ "If both limits are positive, the shorter limit is used.</html>"), BorderLayout.SOUTH);
		while (true) {
			int choice = JOptionPane.showConfirmDialog(subsectionButton.getEditor().getComponent(), panel,
					"Subsection Faults", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (choice != JOptionPane.OK_OPTION)
				return;
			try {
				buildSubsections();
				return;
			} catch (RuntimeException exception) {
				JOptionPane.showMessageDialog(subsectionButton.getEditor().getComponent(), exception.getMessage(),
						"Could Not Build Subsections", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void buildSubsections() {
		Integer minPerFault = minPerFaultParam.getValue();
		Double ddwFract = ddwFractParam.getValue();
		Double fixedLength = fixedLengthParam.getValue();
		if (minPerFault == null || minPerFault < 1)
			throw new IllegalArgumentException("Minimum subsections per fault must be at least 1.");
		if (ddwFract == null || !(Double.isNaN(ddwFract) || (Double.isFinite(ddwFract) && ddwFract >= 0)))
			throw new IllegalArgumentException("DDW fraction must be nonnegative and finite, or NaN to disable it.");
		if (fixedLength == null || !(Double.isNaN(fixedLength) || (Double.isFinite(fixedLength) && fixedLength >= 0)))
			throw new IllegalArgumentException("Fixed length must be nonnegative and finite, or NaN to disable it.");
		if (!(ddwFract > 0 || fixedLength > 0))
			throw new IllegalArgumentException("Set a positive DDW fraction or fixed length.");

		List<FaultSection> built = SubSectionBuilder.buildSubSects(loadedSects, minPerFault, ddwFract, fixedLength);
		List<GeoJSONFaultSection> subsections = new ArrayList<>(built.size());
		for (FaultSection subsection : built)
			subsections.add(subsection instanceof GeoJSONFaultSection jsonSection ? jsonSection
					: GeoJSONFaultSection.fromFaultSection(subsection));
		// Keep the previous display intact if validation or subsection generation fails.
		sects = subsections;
		l.treeChanged(null);
	}

	private void fireSectsNullEvent() {
		for (FaultSectionsChangeListener l : sectChangeListeners)
			l.setFaultSections(null);
	}
	
	private void fireSectsChangedEvent() {
		for (FaultSectionsChangeListener l : sectChangeListeners)
			l.setFaultSections(sects);
	}

}
