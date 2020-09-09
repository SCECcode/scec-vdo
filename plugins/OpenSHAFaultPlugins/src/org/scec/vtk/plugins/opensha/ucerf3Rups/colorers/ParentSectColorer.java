package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.io.IOException;
import java.util.Map;

import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultSection;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;

import com.google.common.collect.Maps;

public class ParentSectColorer extends CPTBasedColorer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<Integer, Double> idValMap = Maps.newHashMap();

	private static final String NAME = "Parent Section";
	
	private static CPT getDefaultCPT() {
		CPT cpt;
		try {
			cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		cpt = cpt.rescale(0, 1);
		cpt.setNanColor(Color.GRAY);
		return cpt;
	}
	
	public ParentSectColorer() {
		super(getDefaultCPT(), false);
	}

	@Override
	public synchronized double getValue(AbstractFaultSection fault) {
		if (fault instanceof PrefDataSection) {
			PrefDataSection prefFault = (PrefDataSection)fault;
			FaultSection pref = prefFault.getFaultSection();
			Integer parentID = pref.getParentSectionId();
			if (idValMap.containsKey(parentID))
				return idValMap.get(parentID);
			Double val = Math.random();
			idValMap.put(parentID, val);
			return val;
		}
		return Double.NaN;
	}

	@Override
	public String getName() {
		return NAME;
	}

}
