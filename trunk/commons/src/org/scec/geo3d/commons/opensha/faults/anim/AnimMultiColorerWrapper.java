package org.scec.geo3d.commons.opensha.faults.anim;

import java.awt.Color;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.util.cpt.CPT;
import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;
import org.scec.geo3d.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.geo3d.commons.opensha.faults.colorers.FaultColorer;

public class AnimMultiColorerWrapper extends CPTBasedColorer {
	
	private FaultColorer colorer;
	private String name;
	
	public AnimMultiColorerWrapper(String name, FaultColorer colorer) {
		super(null, false);
		this.name = name;
		setAnimColorer(colorer);
	}
	
	public void setAnimColorer(FaultColorer colorer) {
		this.colorer = colorer;
		fireColorerChangeEvent();
	}

	@Override
	public String getName() {
		return name;
	}
	
	private boolean isCPTBased() {
		return colorer instanceof CPTBasedColorer;
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		throw new UnsupportedOperationException("can't get get value for custom multi colorer");
	}

	@Override
	public CPT getCPT() {
		if (isCPTBased())
			return ((CPTBasedColorer)colorer).getCPT();
		return null;
	}

	@Override
	public void setCPT(CPT cpt) {
		if (isCPTBased())
			((CPTBasedColorer)colorer).setCPT(cpt);
	}

	@Override
	public boolean isCPTLog() {
		if (isCPTBased())
			return ((CPTBasedColorer)colorer).isCPTLog();
		return false;
	}

	@Override
	public void setCPTLog(boolean newCPTLog) {
		if (isCPTBased())
			((CPTBasedColorer)colorer).setCPTLog(newCPTLog);
	}

	@Override
	public Color getColor(AbstractFaultSection fault) {
		return colorer.getColor(fault);
	}

	@Override
	public ParameterList getColorerParameters() {
		return colorer.getColorerParameters();
	}

}
