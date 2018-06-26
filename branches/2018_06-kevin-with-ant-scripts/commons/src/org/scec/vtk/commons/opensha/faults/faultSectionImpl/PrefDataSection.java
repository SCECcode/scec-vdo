package org.scec.vtk.commons.opensha.faults.faultSectionImpl;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.opensha.sha.faultSurface.SimpleFaultData;
import org.scec.vtk.commons.opensha.faults.AbstractSimpleFaultDataFaultSection;
import org.scec.vtk.commons.opensha.faults.attributeInterfaces.AseismicityFaultSection;
import org.scec.vtk.commons.opensha.faults.attributeInterfaces.CouplingCoefficientFaultSection;
import org.scec.vtk.commons.opensha.faults.params.AseismicityParam;

public class PrefDataSection extends AbstractSimpleFaultDataFaultSection
implements AseismicityFaultSection, CouplingCoefficientFaultSection{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FaultSectionPrefData prefData;
	
	public PrefDataSection(FaultSectionPrefData prefData) {
		this(prefData.getName(), prefData);
	}
	
	public PrefDataSection(String name, FaultSectionPrefData prefData) {
		super(name, prefData.getSectionId());
		this.prefData = prefData;
	}

	@Override
	protected SimpleFaultData getSimpleFaultData(ParameterList faultRepresentationParams) {
		checkHasParam(faultRepresentationParams, AseismicityParam.NAME);
		BooleanParameter aseisParam = (BooleanParameter)faultRepresentationParams.getParameter(AseismicityParam.NAME);
		return prefData.getSimpleFaultData(aseisParam.getValue());
	}

	@Override
	public double getAvgDip() {
		return prefData.getAveDip();
	}

	@Override
	public double getAvgRake() {
		return prefData.getAveRake();
	}

	@Override
	public double getAvgStrike() {
		return prefData.getFaultTrace().getAveStrike();
	}

	@Override
	public double getSlipRate() {
		return prefData.getOrigAveSlipRate();
	}
	
	public FaultSectionSummary getFaultSectionSummary() {
		return new FaultSectionSummary(prefData.getSectionId(), prefData.getSectionName());
	}
	
	public FaultSectionPrefData getFaultSection() {
		return prefData;
	}
	
	public static ParameterList createPrefDataParams() {
		ParameterList params = AbstractSimpleFaultDataFaultSection.createSimpleFaultDataParams();
		
		params.addParameter(new AseismicityParam());
		
		return params;
	}

	@Override
	public double getCouplingCoeff() {
		return getFaultSection().getCouplingCoeff();
	}

	@Override
	public double getAseismicSlipFactor() {
		return getFaultSection().getAseismicSlipFactor();
	}

}
