package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;

import com.google.common.base.Preconditions;

public class EternalDatasetColorer extends CPTBasedColorer implements ParameterChangeListener {
	
	private static CPT getDefaultCPT() {
		try {
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			cpt.setNanColor(Color.GRAY);
			return cpt.rescale(0d, 1d);
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		
	}
	
	private FileParameter browseParam;
	
	private StringParameter columnParam;
	
	private ButtonParameter rescaleParam;
	
	private ParameterList params;
	
	private double[] curScalars;
	private CSVFile<String> csv;

	public EternalDatasetColorer() {
		super(getDefaultCPT(), false);
		
		params = new ParameterList();
		
		browseParam = new FileParameter("Load CSV File");
		browseParam.setInfo("CSV file should have 1 row for each subsection, plus a header row. "
				+ "Multiple data columns are allowed and will be user selectable. Values must "
				+ "be numerical.");
		browseParam.addParameterChangeListener(this);
		params.addParameter(browseParam);
		
		rescaleParam = new ButtonParameter("CPT Range", "Rescale From Data");
		rescaleParam.addParameterChangeListener(this);
		params.addParameter(rescaleParam);
		
		ArrayList<String> strings = new ArrayList<>();
		strings.add("(none)");
		columnParam = new StringParameter("CSV Column", strings);
		columnParam.addParameterChangeListener(this);
		columnParam.setValue(strings.get(0));
		params.addParameter(columnParam);
	}

	@Override
	public String getName() {
		return "External Dataset Colorer";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		int id = fault.getId();
		if (curScalars != null && id < curScalars.length)
			return curScalars[id];
		return Double.NaN;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == browseParam) {
			loadFile(browseParam.getValue());
			fireColorerChangeEvent();
		} else if (event.getParameter() == columnParam) {
			updateScalars();
			fireColorerChangeEvent();
		} else if (event.getParameter() == rescaleParam) {
			if (curScalars != null) {
				double max = StatUtils.max(curScalars);
				if (isCPTLog()) {
					double minNonZero = Double.POSITIVE_INFINITY;
					for (double val : curScalars)
						if (val > 0)
							minNonZero = Math.min(val, minNonZero);
					
					double logMin = Math.floor(Math.log10(minNonZero));
					double logMax = Math.ceil(Math.log10(max));
					
					setCPT(getCPT().rescale(logMin, logMax), true);
				} else {
					double min = StatUtils.min(curScalars);
					if (min == max)
						max++;
					setCPT(getCPT().rescale(min, max), false);
				}
				fireColorerChangeEvent();
			}
		}
	}
	
	private void loadFile(File file) {
		csv = null;
		curScalars = null;
		
		if (file != null) {
			try {
				csv = CSVFile.readFile(file, true);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),
						"Error loading CSV", JOptionPane.ERROR_MESSAGE);
				return;
			}
			ArrayList<String> headers = new ArrayList<>(csv.getLine(0));
			columnParam.removeParameterChangeListener(this);
			((StringConstraint)columnParam.getConstraint()).setStrings(headers);
			columnParam.setValue(headers.get(0));
			columnParam.getEditor().refreshParamEditor();
			columnParam.addParameterChangeListener(this);
			
			updateScalars();
		}
	}
	
	private void updateScalars() {
		curScalars = null;
		if (csv != null) {
			String header = columnParam.getValue();
			int col = csv.getLine(0).indexOf(header);
			System.out.println("Loading scalars from column "+col);
			Preconditions.checkState(col >= 0);
			curScalars = new double[csv.getNumRows()-1];
			for (int row=1; row<csv.getNumRows(); row++) {
				try {
					curScalars[row-1] = csv.getDouble(row, col);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage(),
							"Error reading CSV data", JOptionPane.ERROR_MESSAGE);
					csv = null;
					curScalars = null;
					break;
				}
			}
			System.out.println("Loaded "+curScalars.length+" scalars");
		}
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

}
