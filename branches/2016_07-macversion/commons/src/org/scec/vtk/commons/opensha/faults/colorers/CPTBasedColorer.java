package org.scec.vtk.commons.opensha.faults.colorers;

import java.awt.Color;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.util.cpt.CPT;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

public abstract class CPTBasedColorer implements FaultColorer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ColorerChangeListener l;
	
	private CPT cpt;
	private boolean cptLog;
	
	public CPTBasedColorer(CPT cpt, boolean cptLog) {
		this.cpt = cpt;
		this.cptLog = cptLog;
	}

	public CPT getCPT() {
		return cpt;
	}

	public void setCPT(CPT cpt) {
		this.cpt = cpt;
	}

	public void setCPT(CPT cpt, boolean isLog) {
		this.cpt = cpt;
		this.cptLog = isLog;
	}

	public boolean isCPTLog() {
		return cptLog;
	}

	public void setCPTLog(boolean newCPTLog) {
		if (cptLog != newCPTLog) {
			this.cptLog = newCPTLog;
			if (cptLog) {
				// we "log" the CPT
				this.cpt = cpt.asLog10();
//				if (getCPT().getMinValue() <= 0)
//					throw new InvalidRangeException("Cannot set to log with CPT values < 0");
//				for (CPTVal val : getCPT()) {
//					val.start = (float)Math.log10(val.start);
//					val.end = (float)Math.log10(val.end);
//				}
			} else {
				// we "unlog" the CPT
				this.cpt = cpt.asPow10();
//				for (CPTVal val : getCPT()) {
//					val.start = (float)Math.pow(10, val.start);
//					val.end = (float)Math.pow(10, val.end);
//				}
			}

		}
	}

	@Override
	public Color getColor(AbstractFaultSection fault) {
		return getColorForValue(getValue(fault));
	}
	
	public abstract double getValue(AbstractFaultSection fault);
	
	public Color getColorForValue(double value) {
		if (isCPTLog()) {
			if (value <= 0)
				return getCPT().getBelowMinColor();
			value = Math.log10(value);
		}
		return getCPT().getColor((float)value);
	}
	
	@Override
	public ParameterList getColorerParameters() {
		return null;
	}
	
	public void setColorerChangeListener(ColorerChangeListener l) {
		this.l = l;
	}
	
	protected void fireColorerChangeEvent() {
		if (l != null)
			l.colorerChanged(this);
	}

	public int getParamColCount() {
		return 2;
	}

}
