package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.util.EnumSet;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;
import org.scec.geo3d.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.geo3d.commons.opensha.faults.colorers.SlipRateColorer;
import org.scec.geo3d.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;

public class InversionSlipRateColorer extends CPTBasedColorer implements UCERF3RupSetChangeListener,
ParameterChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FaultSystemRupSet rupSet;
	private FaultSystemSolution sol;
	
	private enum SlipRateTypes {
		ORIGINAL("Original Non Reduced"),
		CREEP_REDUCED("Creep Reduced"),
		SUBSEISMOGENIC_REDUCED("Subseismogenic & Creep Reduced"),
		SOLUTION("Solution"),
		SOLUTION_STD_DEV("Solution Std Dev (avg solutions only)");
		
		private String name;
		
		private SlipRateTypes(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private static final String TYPE_PARAM_NAME = "Slip Rate To Plot";
	private EnumParameter<SlipRateTypes> typeParam;
	private ParameterList params;

	private static final String NAME = "Slip Rate (mm/yr)";

	public InversionSlipRateColorer() {
		this(SlipRateColorer.getDefaultCPT(), false);
	}

	public InversionSlipRateColorer(CPT cpt, boolean cptLog) {
		super(cpt, cptLog);
		
		typeParam = new EnumParameter<InversionSlipRateColorer.SlipRateTypes>(TYPE_PARAM_NAME,
				EnumSet.allOf(SlipRateTypes.class), SlipRateTypes.ORIGINAL, null);
		typeParam.addParameterChangeListener(this);
		
		params = new ParameterList();
		params.addParameter(typeParam);
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		if (fault instanceof PrefDataSection) {
			PrefDataSection prefFault = (PrefDataSection)fault;
			FaultSectionPrefData pref = prefFault.getFaultSection();
			switch (typeParam.getValue()) {
			case ORIGINAL:
				return pref.getOrigAveSlipRate();
			case CREEP_REDUCED:
				return pref.getReducedAveSlipRate();
			case SUBSEISMOGENIC_REDUCED:
				if (rupSet != null)
					return rupSet.getSlipRateForSection(pref.getSectionId())*1e3;
			case SOLUTION:
				if (sol != null && sol instanceof InversionFaultSystemSolution)
					return ((InversionFaultSystemSolution)sol).calcSlipRateForSect(pref.getSectionId())*1e3;
			case SOLUTION_STD_DEV:
				if (sol != null && sol instanceof AverageFaultSystemSolution)
					return ((AverageFaultSystemSolution)sol).getRupSet().getSlipRateStdDevForSection(pref.getSectionId())*1e3;
			default:
				break;
			}
		}
		return Double.NaN;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.rupSet = rupSet;
		this.sol = sol;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == typeParam) {
			fireColorerChangeEvent();
		}
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

}
