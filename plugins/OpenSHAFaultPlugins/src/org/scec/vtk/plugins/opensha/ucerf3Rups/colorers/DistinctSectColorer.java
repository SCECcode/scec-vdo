package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opensha.commons.param.ParameterList;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemSolution;
import org.opensha.sha.earthquake.faultSysSolution.reports.plots.SectBySectDetailPlots;
import org.opensha.sha.earthquake.faultSysSolution.ruptures.util.SectionDistanceAzimuthCalculator;
import org.opensha.sha.faultSurface.FaultSection;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.FaultSectionsChangeListener;
import org.scec.vtk.plugins.opensha.ucerf3Rups.RupSetChangeListener;

public class DistinctSectColorer implements FaultColorer, FaultSectionsChangeListener, RupSetChangeListener {
	
	private Map<Integer, Color> sectColors;
	private ColorerChangeListener l;

	@Override
	public String getName() {
		return "Distinct Section Colors";
	}

	@Override
	public void setFaultSections(List<? extends FaultSection> sects) {
		buildColors(sects, sects == null || sects.isEmpty() ? null : new SectionDistanceAzimuthCalculator(sects));
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		if (rupSet == null) {
			buildColors(null, null);
			return;
		}
		SectionDistanceAzimuthCalculator distAzCalc = rupSet.getModule(SectionDistanceAzimuthCalculator.class);
		if (distAzCalc == null) {
			distAzCalc = new SectionDistanceAzimuthCalculator(rupSet.getFaultSectionDataList());
			rupSet.addModule(distAzCalc);
		}
		buildColors(rupSet.getFaultSectionDataList(), distAzCalc);
	}

	private void buildColors(List<? extends FaultSection> sects, SectionDistanceAzimuthCalculator distAzCalc) {
		this.sectColors = null;
		if (sects != null && !sects.isEmpty()) {
			Map<Integer, Color> sectColors = new HashMap<>(sects.size());
			List<Color> colors;
			try {
				colors = SectBySectDetailPlots.buildParentSectionColors(sects, distAzCalc);
			} catch (IOException e) {
				e.printStackTrace();
				fireColorerChangeEvent();
				return;
			}
			for (int i=0; i<sects.size(); i++)
				sectColors.put(sects.get(i).getSectionId(), colors.get(i));
			this.sectColors = sectColors;
		}
		fireColorerChangeEvent();
	}

	@Override
	public Color getColor(AbstractFaultSection fault) {
		if (sectColors == null)
			return Color.GRAY;
		Color color = sectColors.get(fault.getId());
		return color == null ? Color.GRAY : color;
	}

	@Override
	public ParameterList getColorerParameters() {
		return null;
	}

	@Override
	public void setColorerChangeListener(ColorerChangeListener l) {
		this.l = l;
	}
	
	protected void fireColorerChangeEvent() {
		if (l != null)
			l.colorerChanged(this);
	}

	@Override
	public String getLegendLabel() {
		return getName();
	}

}
