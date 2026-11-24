package org.scec.vtk.commons.opensha.surfaces;

import java.util.ArrayList;

import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

import vtk.vtkActor;

/** Actors belonging to one fault, with optional support for a fixed color. */
public class FaultSectionActorList extends ArrayList<vtkActor> {
	private final AbstractFaultSection fault;
	private final boolean colorUpdateEnabled;

	public FaultSectionActorList(AbstractFaultSection fault) {
		this(fault, true);
	}

	public FaultSectionActorList(AbstractFaultSection fault, boolean colorUpdateEnabled) {
		this.fault = fault;
		this.colorUpdateEnabled = colorUpdateEnabled;
	}

	public String getInfo() {
		return fault.getInfo();
	}

	public AbstractFaultSection getFault() {
		return fault;
	}

	/** Whether unbundled actors follow subsequent colorer changes. Bundled lists use their point ranges instead. */
	public boolean isColorUpdateEnabled() {
		return colorUpdateEnabled;
	}
}
