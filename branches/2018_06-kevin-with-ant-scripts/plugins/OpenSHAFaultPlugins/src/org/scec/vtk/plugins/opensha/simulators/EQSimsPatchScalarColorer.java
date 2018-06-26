package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.SimulatorElementFault;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

public class EQSimsPatchScalarColorer extends CPTBasedColorer implements ParameterChangeListener, EQSimsEventListener {
	
	private enum CPTPreset {
		RATIO("Ratio", false) {
			@Override
			CPT build() {
				CPT cpt;
				try {
					cpt = GMT_CPT_Files.GMT_POLAR.instance();
				} catch (IOException e) {
					throw ExceptionUtils.asRuntimeException(e);
				}
				cpt.setNanColor(Color.GRAY);
				cpt = cpt.rescale(0d, 2d);
				return cpt;
			}
		},
		SPECTRUM("Max Spectrum", false) {
			@Override
			CPT build() {
				CPT cpt;
				try {
					cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
				} catch (IOException e) {
					throw ExceptionUtils.asRuntimeException(e);
				}
				cpt.setNanColor(Color.GRAY);
				cpt = cpt.rescale(0d, 2d);
				return cpt;
			}
		},
		LOG_SPECTRUM("Log10 Spectrum", true) {
			@Override
			CPT build() {
				CPT cpt;
				try {
					cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
				} catch (IOException e) {
					throw ExceptionUtils.asRuntimeException(e);
				}
				cpt.setNanColor(Color.GRAY);
				cpt = cpt.rescale(-1, 1);
				return cpt;
			}
		};
		
		private String name;
		private CPT cpt;
		private boolean isLog10;
		private CPTPreset(String name, boolean isLog10) {
			this.name = name;
			this.isLog10 = isLog10;
		}
		
		abstract CPT build();
		
		CPT get() {
			if (cpt == null)
				cpt = build();
			return cpt;
		}
		
		boolean isLog10() {
			return isLog10;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private EnumParameter<CPTPreset> cptParam;
	private static final CPTPreset CPT_PRESET_DEFAULT = CPTPreset.SPECTRUM;
	
	private FileParameter fileParam;
	
	private ButtonParameter scaleToDataParam;
	
	private ParameterList params;
	
	private int minPatchIndex = -1;
	private int numPatches = -1;
	private List<Double> data;

	public EQSimsPatchScalarColorer() {
		super(CPT_PRESET_DEFAULT.get(), CPT_PRESET_DEFAULT.isLog10());
		
		params = new ParameterList();
		
		fileParam = new FileParameter("Input File");
		fileParam.setInfo("Load a text file where every line contains one scalar value, indexed by patch index.");
		fileParam.addParameterChangeListener(this);
		fileParam.getEditor().setEnabled(false);
		params.addParameter(fileParam);
		
		cptParam = new EnumParameter<CPTPreset>("CPT Preset", EnumSet.allOf(CPTPreset.class), CPT_PRESET_DEFAULT, null);
		cptParam.addParameterChangeListener(this);
		params.addParameter(cptParam);
		
		scaleToDataParam = new ButtonParameter("CPT Range", "Scale to Data");
		scaleToDataParam.addParameterChangeListener(this);
		scaleToDataParam.getEditor().setEnabled(false);
		params.addParameter(scaleToDataParam);
	}

	@Override
	public String getName() {
		return "Patch Scalar File Loader";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		if (data == null || !(fault instanceof SimulatorElementFault))
			return Double.NaN;
		int index = ((SimulatorElementFault)fault).getElement().getID() - minPatchIndex;
		if (index >= data.size()) {
			System.out.println("Bad patch index! Have "+data.size()+", index="+index);
			return Double.NaN;
		}
		return data.get(index);
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == fileParam) {
			data = null;
			File file = fileParam.getValue();
			if (file != null) {
				try {
					for (String line : Files.readLines(file, Charset.defaultCharset())) {
						if (data == null)
							data = new ArrayList<>();
						line = line.trim();
						if (!line.isEmpty()) {
							double val = Double.parseDouble(line);
							data.add(val);
						}
					}
					if (data.size() != numPatches) {
						JOptionPane.showMessageDialog(null, "Each line should contain one scalar value, one for each patch."
								+"\n\nExpected "+numPatches+", file has "+data.size(),
								"Malformed data file", JOptionPane.ERROR_MESSAGE);
						data = null;
					}
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
					data = null;
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Each line should contain one scalar value.\n\n"+e.getMessage(),
							"Malformed data file", JOptionPane.ERROR_MESSAGE);
					data = null;
				}
				if (data == null) {
					// failed load
					fileParam.setValue(null);
				}
			}
			scaleToDataParam.getEditor().setEnabled(data != null);
			fireColorerChangeEvent();
		} else if (event.getParameter() == cptParam) {
			CPTPreset preset = cptParam.getValue();
			setCPT(preset.get(), preset.isLog10());
			fireColorerChangeEvent();
		} else if (event.getParameter() == scaleToDataParam) {
			Preconditions.checkNotNull(data);
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			for (double val : data) {
				min = Math.min(min, val);
				max = Math.max(max, val);
			}
			if (isCPTLog()) {
				min = Math.log10(min);
				max = Math.log10(max);
			}
			CPT cpt = getCPT().rescale(min, max);
			setCPT(cpt, isCPTLog());
			fireColorerChangeEvent();
		}
	}

	@Override
	public void setEvents(List<? extends SimulatorEvent> events) {}

	@Override
	public void setGeometry(List<SimulatorElement> elements) {
		minPatchIndex = -1;
		numPatches = -1;
		
		if (elements != null && !elements.isEmpty()) {
			minPatchIndex = Integer.MAX_VALUE;
			numPatches = elements.size();
			for (SimulatorElement elem : elements)
				if (elem.getID() < minPatchIndex)
					minPatchIndex = elem.getID();
		}
		fileParam.getEditor().setEnabled(numPatches > 0);
	}

}
