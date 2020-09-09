package org.scec.vtk.plugins.opensha.simulators;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.utils.RSQSimSubSectionMapper;
import org.opensha.sha.simulators.utils.RSQSimUtils;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.SimulatorElementFault;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;

public class EQSimsSubSectDASColorer extends CPTBasedColorer implements EQSimsEventListener, ParameterChangeListener {
	
	private List<SimulatorElement> elements;
	private RSQSimSubSectionMapper mapper;
	private boolean mapperFail = false;
	
	private EnumParameter<FaultModels> fmParam;

	private static CPT buildCPT() {
		try {
			return GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, 1d);
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	public EQSimsSubSectDASColorer() {
		super(buildCPT(), false);
		
		fmParam = new EnumParameter<FaultModels>("Fault Model", EnumSet.allOf(FaultModels.class), FaultModels.FM3_1, null);
		fmParam.addParameterChangeListener(this);
	}

	@Override
	public String getName() {
		return "Sub Section DAS Colorer";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		if (elements != null && mapper == null && !mapperFail) {
			// try to build it
			try {
				List<? extends FaultSection> subSects = RSQSimUtils.getUCERF3SubSectsForComparison(fmParam.getValue(), DeformationModels.GEOLOGIC);
				mapper = new RSQSimSubSectionMapper(subSects, elements, 0.2);
			} catch (Exception e) {
				System.err.println("Error building mapper:");
				e.printStackTrace();
				mapperFail = true;
			}
		}
		if (mapper == null || !(fault instanceof SimulatorElementFault))
			return Double.NaN;
		SimulatorElement elem = ((SimulatorElementFault)fault).getElement();
		double das = mapper.getElemSubSectDAS(elem).midDAS;
		FaultSection sect = mapper.getMappedSection(elem);
		double len = sect.getFaultTrace().getTraceLength();
		return das/len;
	}

	@Override
	public void setEvents(List<? extends SimulatorEvent> events) {}

	@Override
	public void setGeometry(List<SimulatorElement> elements) {
		this.elements = elements;
		mapperFail = false;
		fireColorerChangeEvent();
	}

	@Override
	public void parameterChange(ParameterChangeEvent arg0) {
		mapper = null;
		mapperFail = false;
		if (elements != null)
			fireColorerChangeEvent();
	}

}
