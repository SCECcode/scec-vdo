package org.scec.geo3d.library.wgcep.faults;

import java.util.Comparator;

public class AbstractFaultIDComparator implements
		Comparator<AbstractFaultSection> {

	@Override
	public int compare(AbstractFaultSection o1, AbstractFaultSection o2) {
		Integer id1 = new Integer(o1.getId());
		return id1.compareTo(o2.getId());
	}

}
