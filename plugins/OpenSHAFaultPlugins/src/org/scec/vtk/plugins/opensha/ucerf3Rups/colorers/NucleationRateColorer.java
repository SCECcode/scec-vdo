package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultSection;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.surfaces.FaultSectionActorList;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import vtk.vtkActor;
import vtk.vtkCellPicker;

public class NucleationRateColorer extends CPTBasedColorer implements
		UCERF3RupSetChangeListener, ParameterChangeListener, PickHandler<AbstractFaultSection> {
	
	private ParameterList params;
	
	private DoubleParameter magMinParam;
	private double min = 6.5;
	private double max = 10d;
	private DoubleParameter magMaxParam;
	
	private FaultSystemSolution sol;
	
	private static final double cpt_min = 1.0e-6;
	private static final double cpt_max = 1.0e-2;
	
	protected static CPT getDefaultCPT() {
		try {
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			cpt = cpt.rescale(cpt_min, cpt_max);
			return cpt;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public NucleationRateColorer() {
		super(getDefaultCPT(), false);
		setCPTLog(true);
		params = new ParameterList();
		magMinParam = new DoubleParameter("Min Mag", 0d, 10d);
		magMinParam.setValue(min);
		magMinParam.addParameterChangeListener(this);
		params.addParameter(magMinParam);
		magMaxParam = new DoubleParameter("Max Mag", 0d, 10d);
		magMaxParam.setValue(max);
		magMaxParam.addParameterChangeListener(this);
		params.addParameter(magMaxParam);
	}
	
	@Override
	public double getValue(AbstractFaultSection fault) {
		if (sol == null)
			return Double.NaN;
		int sectIndex = fault.getId();
		return sol.calcNucleationRateForSect(sectIndex, min, max);
	}

	@Override
	public String getName() {
		return "Solution Nucleation Rates (events/yr)";
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.sol = sol;
	}
	
	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == magMinParam) {
			double newMin = magMinParam.getValue();
			if (newMin < max) {
				min = newMin;
				fireColorerChangeEvent();
			} else {
				magMinParam.removeParameterChangeListener(this);
				magMinParam.setValue(min);
				magMinParam.addParameterChangeListener(this);
				magMinParam.getEditor().refreshParamEditor();
				JOptionPane.showMessageDialog(null, "Min must be < Max!", "Invalid Range",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (event.getParameter() == magMaxParam) {
			double newMax = magMaxParam.getValue();
			if (newMax > min) {
				max = newMax;
				fireColorerChangeEvent();
			} else {
				magMaxParam.removeParameterChangeListener(this);
				magMaxParam.setValue(max);
				magMaxParam.addParameterChangeListener(this);
				magMaxParam.getEditor().refreshParamEditor();
				JOptionPane.showMessageDialog(null, "Max must be > Min!", "Invalid Range",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void actorPicked(PickEnabledActor<AbstractFaultSection> actor,
			AbstractFaultSection fault, vtkCellPicker picker, MouseEvent e) {
		int clickCount = e.getClickCount();
		// return if we don't have a solution, or it's not a double click
		if (sol == null || clickCount < 2 || e.getButton() != MouseEvent.BUTTON1)
			return;
		
		int faultID = fault.getId();
		
		FaultSection sect = sol.getRupSet().getFaultSectionData(faultID);
		
		GraphWindow graph = null;
		
		for (boolean parent : new boolean[] {false, true}) {
			IncrementalMagFreqDist mfd;
			if (parent)
				mfd = sol.calcNucleationMFD_forParentSect(sect.getParentSectionId(), 5.55d, 8.55d, 31);
			else
				mfd = sol.calcNucleationMFD_forSect(faultID, 5.55d, 8.55d, 31);
			
			mfd.setName("Incremental MFD");
			mfd.setInfo(" ");
			EvenlyDiscretizedFunc cumMFD = mfd.getCumRateDistWithOffset();
			cumMFD.setName("Cumulative MFD");
			cumMFD.setInfo(" ");
			ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<>();
			ArrayList<PlotCurveCharacterstics> chars = new ArrayList<>();
			funcs.add(mfd);
			chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));
			funcs.add(cumMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			
//			MinMaxAveTracker yTrack = new MinMaxAveTracker();
//			for (EvenlyDiscretizedFunc func : funcs)
//				for (Point2D pt : func)
//					if (pt.getY() > 0)
//						yTrack.addValue(pt.getY());
//			
//			double minY = yTrack.getMin()*0.5;
//			double maxY = yTrack.getMax()*2d;
			
			String name;
			if (parent)
				name = sect.getParentSectionName();
			else
				name = fault.getName();
			
			PlotSpec spec = new PlotSpec(funcs, chars, name, "Magnitude", "Nucleation Rate (1/yr)");
			spec.setLegendVisible(true);
			
			if (graph == null)
				graph = new GraphWindow(spec, false);
			else
				graph.addTab(spec);
			
			graph.setYLog(true);
			graph.setAxisRange(6, 9, 1e-10, 1e-1);
		}
		
		graph.setSelectedTab(0);
		graph.setVisible(true);
	}

}
