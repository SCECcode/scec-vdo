package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.util.IDPairing;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.simulators.RSQSimEvent;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.iden.EventIDsRupIden;
import org.opensha.sha.simulators.iden.RuptureIdentifier;
import org.opensha.sha.simulators.parsers.RSQSimFileReader;
import org.opensha.sha.simulators.utils.RSQSimUtils;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;

public class RSQSimRuptureMappingColorer implements FaultColorer, ParameterChangeListener, UCERF3RupSetChangeListener {
	
	private FileParameter geomFileParam;
	private IntegerParameter eventIDParam;
	private DoubleParameter sectFractParam;
	
	private ParameterList params;
	
	private ColorerChangeListener l;
	
	private List<SimulatorElement> elems;
	private RSQSimEvent event;
	private HashSet<Integer> sects;
	private List<FaultSectionPrefData> subSects;
	private Map<IDPairing, Double> distsCache;
	private Map<Integer, Double> subSectAreas;
	
	public RSQSimRuptureMappingColorer() {
		params = new ParameterList();
		
		geomFileParam = new FileParameter("Geometry File");
		geomFileParam.addParameterChangeListener(this);
		params.addParameter(geomFileParam);
		
		eventIDParam = new IntegerParameter("Event ID", -1);
		eventIDParam.addParameterChangeListener(this);
		params.addParameter(eventIDParam);
		
		sectFractParam = new DoubleParameter("Min Sect Fract", 0d);
		sectFractParam.addParameterChangeListener(this);
		params.addParameter(sectFractParam);
	}

	@Override
	public String getName() {
		return "RSQSim Rupture Mappings";
	}

	@Override
	public Color getColor(AbstractFaultSection fault) {
		if (sects != null && sects.contains(fault.getId()))
			return Color.GREEN;
		return null;
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

	@Override
	public void setColorerChangeListener(ColorerChangeListener l) {
		this.l = l;
	}
	
	private void fireChangeEvent() {
		l.colorerChanged(this);
	}

	@Override
	public String getLegendLabel() {
		return null;
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {
		if (e.getParameter() == geomFileParam) {
			try {
				elems = RSQSimFileReader.readGeometryFile(geomFileParam.getValue(), 11, 'S');
				subSectAreas = new HashMap<>();
				for (SimulatorElement elem : elems) {
					Double prevArea = subSectAreas.get(elem.getSectionID());
					if (prevArea == null)
						prevArea = 0d;
					subSectAreas.put(elem.getSectionID(), prevArea + elem.getArea());
				}
				tryLoad();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getParameter() == eventIDParam) {
			try {
				tryLoad();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getParameter() == sectFractParam) {
			tryBuildRupture();
			fireChangeEvent();
		}
	}
	
	private void tryLoad() throws IOException {
		sects = null;
		
		if (elems != null && eventIDParam.getValue() >= 0 && subSects != null) {
			// load the event
			EventIDsRupIden loadIden = new EventIDsRupIden(eventIDParam.getValue());
			List<RuptureIdentifier> loadIdens = new ArrayList<RuptureIdentifier>();
			loadIdens.add(loadIden);
			System.out.println("Loading events, looking for "+eventIDParam.getValue());
			List<RSQSimEvent> events = RSQSimFileReader.readEventsFile(geomFileParam.getValue().getParentFile(), elems, loadIdens);
			
			if (events.size() == 1) {
				System.out.println("Found it!");
				
				this.event = events.get(0);
				tryBuildRupture();
			} else {
				System.out.println("Not found.");
			}
			
		}
		
		fireChangeEvent();
	}
	
	private void tryBuildRupture() {
		if (event == null || elems == null || subSects == null)
			return;
		int offset = RSQSimUtils.getSubSectIndexOffset(elems, subSects);
		List<List<FaultSectionPrefData>> rupSects = RSQSimUtils.getSectionsForRupture(
				event, offset, subSects, distsCache, sectFractParam.getValue(), subSectAreas);
		this.sects = new HashSet<Integer>();
		for (List<FaultSectionPrefData> parentSects : rupSects)
			for (FaultSectionPrefData sect : parentSects)
				sects.add(sect.getSectionId());
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		if (rupSet == null) {
			subSects = null;
			distsCache = null;
		} else {
			this.subSects = rupSet.getFaultSectionDataList();
			this.distsCache = new HashMap<IDPairing, Double>();
		}
	}

}
