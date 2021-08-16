package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemSolution;
import org.opensha.sha.faultSurface.FaultSection;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;

import scratch.UCERF3.utils.LastEventData;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoProbabilityModel;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoProbabilityModel;

public class DateLastEventColorer extends CPTBasedColorer implements UCERF3RupSetChangeListener, ParameterChangeListener {
	
	private FaultSystemRupSet rupSet;
	private FaultSystemSolution sol;
	private Map<Integer, List<LastEventData>> data;
	private boolean populated = false;
	private PaleoProbabilityModel paleoProbModel;
	
	private static final double YEARS_PER_MILLI = 1d/((double)(1000l*60l*60l*24l)*365.242);
	
	private CPT intervalCPT;
	private CPT ratioCPT;
	
	enum PlotType {
		INTERVAL("Open Interval (years)"),
		RATIO("Open Interval/Paleo Obs Recurr");
		
		private String name;
		
		private PlotType(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	private EnumParameter<PlotType> plotTypeParam;
	
	private ParameterList params;
	
	private static CPT createIntervalCPT() {
		try {
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			cpt = cpt.rescale(0, Math.log10(15000));
			cpt.setNanColor(Color.GRAY);
			return cpt;
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
	
	private static CPT createRatioCPT() {
		try {
			CPT cpt = GMT_CPT_Files.GMT_POLAR.instance();
			cpt = cpt.rescale(-1, 1);
			cpt.setNanColor(Color.GRAY);
			return cpt;
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	public DateLastEventColorer() {
		super(createIntervalCPT(), true);
		intervalCPT = getCPT();
		ratioCPT = createRatioCPT();
		
		plotTypeParam = new EnumParameter<PlotType>("Plot Type", EnumSet.allOf(PlotType.class), PlotType.INTERVAL, null);
		plotTypeParam.addParameterChangeListener(this);
		
		params = new ParameterList();
		params.addParameter(plotTypeParam);
	}

	@Override
	public String getName() {
		return "Open Interval (years)";
	}

	@Override
	public synchronized double getValue(AbstractFaultSection fault) {
		if (rupSet == null || !(fault instanceof PrefDataSection))
			return Double.NaN;
		if (!populated) {
			if (data == null) {
				try {
					System.out.println("Loading data...");
					data = LastEventData.load();
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			}
			System.out.println("Populating subsects with data...");
			LastEventData.populateSubSects(rupSet.getFaultSectionDataList(), data);
			populated = true;
		}
		FaultSection sect = ((PrefDataSection)fault).getFaultSection();
		long curTime = System.currentTimeMillis();
		long eventTime = sect.getDateOfLastEvent();
		if (eventTime == Long.MIN_VALUE)
			return Double.NaN;
		long timeDiffMillis = curTime - eventTime;
		double diffYears = YEARS_PER_MILLI*timeDiffMillis;
		switch (plotTypeParam.getValue()) {
		case INTERVAL:
			return diffYears;
		case RATIO:
			if (sol == null)
				return Double.NaN;
			if (paleoProbModel == null) {
				try {
					paleoProbModel = UCERF3_PaleoProbabilityModel.load();
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			}
			double paleoRate = sol.calcTotPaleoVisibleRateForSect(sect.getSectionId(), paleoProbModel);
			double paleoRI = 1d/paleoRate;
			return diffYears/paleoRI;
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.rupSet = rupSet;
		this.sol = sol;
		populated = false;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == plotTypeParam) {
			switch (plotTypeParam.getValue()) {
			case INTERVAL:
				setCPT(intervalCPT, true);
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
	public ParameterList getColorerParameters() {
		return params;
	}

}
