package org.scec.vtk.commons.opensha.faults;

import java.util.Comparator;

public class AbstractFaultIDComparator implements
		Comparator<AbstractFaultSection> {

	@Override
	public int compare(AbstractFaultSection o1, AbstractFaultSection o2) {
		Integer id1 = Integer.valueOf(o1.getId());
		return id1.compareTo(o2.getId());
	}

}
