package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;
import org.opensha.sha.simulators.utils.SimulatorUtils;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;

public class EQSlipRateColorer extends CPTBasedColorer implements EQSimsEventListener, ParameterChangeListener {
	
	private static CPT createEQSimsCPT() {
		CPT cpt = new CPT();
		
		float delta = 40f / 6f;
		
		cpt.add(new CPTVal(0f, new Color(0, 0, 255),
				delta * 1f, new Color(0, 255, 255)));
		cpt.add(new CPTVal(delta * 1f, new Color(0, 255, 255),
				delta * 2f, new Color(0, 255, 0)));
		cpt.add(new CPTVal(delta * 2f, new Color(0, 255, 0),
				delta * 3f, new Color(255, 255, 0)));
		cpt.add(new CPTVal(delta * 3f, new Color(255, 255, 0),
				delta * 4f, new Color(255, 127, 0)));
		cpt.add(new CPTVal(delta * 4f, new Color(255, 127, 0),
				delta * 5f, new Color(255, 0, 0)));
		cpt.add(new CPTVal(delta * 5f, new Color(255, 0, 0),
				delta * 6f, new Color(255, 0, 255)));
		
		cpt.setBelowMinColor(new Color(0, 0, 255));
		cpt.setAboveMaxColor(new Color(255, 0, 255));
		cpt.setNanColor(new Color(127, 127, 127));
		
		return cpt;
	}
	
	private enum PlotType {
		FAULT_SLIP("Fault Slip Rate"),
		SOLUTION_SLIP("Solution Slip Rate"),
		RATIO("Ratio");
		
		private String name;
		private PlotType(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	private static final String PLOT_TYPE_PARAM_NAME = "Plot Type";
	private static final PlotType PLOT_TYPE_DEFAULT = PlotType.FAULT_SLIP;
	private EnumParameter<PlotType> plotTypeParam;
	
	private ParameterList params;
	
	private CPT slipCPT;
	private CPT ratioCPT;
	
	private HashMap<Integer, Double> solutionSlips = new HashMap<Integer, Double>();
	
	public EQSlipRateColorer() {
		super(createEQSimsCPT(), false);
		slipCPT = (CPT) getCPT().clone();
		try {
			ratioCPT = GMT_CPT_Files.UCERF3_RATIOS.instance();
		} catch (IOException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		ratioCPT = ratioCPT.rescale(-3, 3);
		
		params = new ParameterList();
		
		plotTypeParam = new EnumParameter<EQSlipRateColorer.PlotType>(PLOT_TYPE_PARAM_NAME,
				EnumSet.allOf(PlotType.class), PLOT_TYPE_DEFAULT, null);
		plotTypeParam.addParameterChangeListener(this);
		params.addParameter(plotTypeParam);
	}

	@Override
	public String getName() {
		return "Slip Rate (mm/yr)";
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == plotTypeParam) {
			switch (plotTypeParam.getValue()) {
			case FAULT_SLIP:
				setCPT(slipCPT, false);
				break;
			case SOLUTION_SLIP:
				setCPT(slipCPT, false);
				break;
			case RATIO:
				setCPT(ratioCPT, true);
				break;

			default:
				break;
			}
			fireColorerChangeEvent();
		}
	}

	@Override
	public void setEvents(List<? extends SimulatorEvent> events) {
		solutionSlips.clear();
		
		if (events == null)
			return;
		
		double rate = 1d / SimulatorUtils.getSimulationDurationYears(events);
		
		for (SimulatorEvent event : events) {
			int[] ids = event.getAllElementIDs();
			if (ids == null)
				continue;
			double[] slips = event.getAllElementSlips();
			for (int i=0; i<ids.length; i++) {
				int id = ids[i];
				double slip = slips[i];
				
				Double prevSlip = solutionSlips.get(id);
				if (prevSlip == null)
					prevSlip = 0d;
				
				prevSlip += slip * rate;
				solutionSlips.put(id, prevSlip);
			}
		}
		
		fireColorerChangeEvent();
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		PlotType plt = plotTypeParam.getValue();
		if (plt == PlotType.FAULT_SLIP)
			return fault.getSlipRate();
		
		if (solutionSlips.isEmpty())
			return Double.NaN;
		
		Double solSlip = solutionSlips.get(fault.getId());
		if (solSlip == null)
			solSlip = 0d;
		
		solSlip *= 1e3; // mm to m
		
		if (plt == PlotType.SOLUTION_SLIP)
			return solSlip;
		
		// this means ratio
		return solSlip / fault.getSlipRate();
	}

	@Override
	public void setGeometry(List<SimulatorElement> elements) {}

}