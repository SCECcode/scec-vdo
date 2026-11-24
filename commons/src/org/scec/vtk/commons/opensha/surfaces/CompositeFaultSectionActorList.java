package org.scec.vtk.commons.opensha.surfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

/**
 * Keeps component lists intact so their bundle ranges survive grouping. Operations on this list must visit its parts;
 * ordinary actor iteration exposes only the unbundled actors, as it does for other fault actor lists.
 */
public class CompositeFaultSectionActorList extends FaultSectionActorList {
	private final List<FaultSectionActorList> parts = new ArrayList<>();

	public CompositeFaultSectionActorList(AbstractFaultSection fault) {
		super(fault);
	}

	public void addPart(FaultSectionActorList part) {
		if (part != null) {
			parts.add(part);
			addAll(part);
		}
	}

	public List<FaultSectionActorList> getParts() {
		return Collections.unmodifiableList(parts);
	}
}
