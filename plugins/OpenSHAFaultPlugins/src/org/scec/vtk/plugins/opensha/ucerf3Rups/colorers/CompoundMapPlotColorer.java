package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.faultSurface.FaultTrace;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.SlipRateColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.analysis.CompoundFSSPlots.MapBasedPlot;
import scratch.UCERF3.analysis.CompoundFSSPlots.MapPlotData;

public class CompoundMapPlotColorer extends CPTBasedColorer implements
		ParameterChangeListener {

	private FileParameter fileParam;

	private static final String PLOT_PARAM_DEFAULT = "(none)";
	private StringParameter plotParam;
	
	private BooleanParameter holdCPTParam;

	private ParameterList params;

	private List<MapPlotData> datas;

	private Map<Integer, List<LocationList>> faultsByTraceSize;
	private Map<LocationList, Double> valuesMap;

	public CompoundMapPlotColorer() {
		super(SlipRateColorer.getDefaultCPT(), false);

		params = new ParameterList();

		fileParam = new FileParameter("Map Plot XML File");
		fileParam.addParameterChangeListener(this);
		params.addParameter(fileParam);

		plotParam = new StringParameter("Plot",
				Lists.newArrayList(PLOT_PARAM_DEFAULT));
		plotParam.setValue(PLOT_PARAM_DEFAULT);
		plotParam.addParameterChangeListener(this);
		params.addParameter(plotParam);
		
		holdCPTParam = new BooleanParameter("Keep CPT on Load", false);
		params.addParameter(holdCPTParam);
	}

	@Override
	public String getName() {
		return "Compound Sol Map Plot Loader";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		if (fault instanceof PrefDataSection && faultsByTraceSize != null) {
			FaultSection subSect = ((PrefDataSection) fault)
					.getFaultSection();
			FaultTrace trace = subSect.getFaultTrace();
			// look for identical traces
			List<LocationList> traces = faultsByTraceSize.get(trace.size());
			outerLoop:
			for (LocationList oTrace : traces) {
				for (int i=0; i<trace.size(); i++) {
					if (!oTrace.get(i).equals(trace.get(i)))
						continue outerLoop;
				}
				// it's a match!
				return valuesMap.get(oTrace);
			}
		}
		return Double.NaN;
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == fileParam) {
			loadFile(fileParam.getValue());
		} else if (event.getParameter() == plotParam) {
			for (MapPlotData data : datas) {
				if (data.getFileName().equals(plotParam.getValue())) {
					setCurData(data);
					break;
				}
			}
			fireColorerChangeEvent();
		}
	}

	private void loadFile(File file) {
		try {
			datas = MapBasedPlot.loadPlotData(file);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Error Loading File", JOptionPane.ERROR_MESSAGE);
			datas = null;
		}

		ArrayList<String> strings = Lists.newArrayList();
		if (datas == null) {
			strings.add(PLOT_PARAM_DEFAULT);
			setCurData(null);
		} else {
			for (MapPlotData data : datas)
				strings.add(data.getFileName());
			setCurData(datas.get(0));
		}

		plotParam.removeParameterChangeListener(this);
		((StringConstraint) plotParam.getConstraint()).setStrings(strings);
		plotParam.setValue(strings.get(0));
		plotParam.getEditor().refreshParamEditor();
		plotParam.addParameterChangeListener(this);

		fireColorerChangeEvent();
	}

	private void setCurData(MapPlotData data) {
		faultsByTraceSize = Maps.newHashMap();
		valuesMap = Maps.newHashMap();
		
		if (data == null)
			return;
		
		if (data.getFaults() == null)
			return;
		
		for (int i=0; i<data.getFaults().size(); i++) {
			LocationList trace = data.getFaults().get(i);
			double value = data.getFaultValues()[i];
			Integer numLocs = trace.size();
			List<LocationList> tracesForSize = faultsByTraceSize.get(numLocs);
			if (tracesForSize == null) {
				tracesForSize = Lists.newArrayList();
				faultsByTraceSize.put(numLocs, tracesForSize);
			}
			tracesForSize.add(trace);
			valuesMap.put(trace, value);
		}
		
		if (!holdCPTParam.getValue())
			setCPT(data.getCPT());
	}
}