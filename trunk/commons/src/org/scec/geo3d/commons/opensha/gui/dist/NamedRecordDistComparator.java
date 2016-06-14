package org.scec.geo3d.commons.opensha.gui.dist;

import java.util.Comparator;

import org.opensha.refFaultParamDb.calc.sectionDists.FaultSectDistRecord;
import org.opensha.refFaultParamDb.calc.sectionDists.RecordIDsComparator;

public class NamedRecordDistComparator implements Comparator<NamedDistRecord> {

	private RecordIDsComparator idCompare = new RecordIDsComparator();

	@Override
	public int compare(NamedDistRecord o1, NamedDistRecord o2) {
		FaultSectDistRecord record1 = o1.getRecord();
		FaultSectDistRecord record2 = o2.getRecord();
		if (record1.getMinDist() < record2.getMinDist())
			return -1;
		else if (record1.getMinDist() > record2.getMinDist())
			return 1;
		// they're equal
		
		return idCompare.compare(record1, record2);
	}

}
