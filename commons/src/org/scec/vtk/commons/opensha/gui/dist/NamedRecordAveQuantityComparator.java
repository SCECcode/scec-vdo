package org.scec.vtk.commons.opensha.gui.dist;

import java.util.Comparator;
import java.util.Map;

import org.opensha.refFaultParamDb.calc.sectionDists.FaultSectDistRecord;
import org.opensha.refFaultParamDb.calc.sectionDists.RecordDistComparator;

public class NamedRecordAveQuantityComparator implements
		Comparator<NamedDistRecord> {

private Map<Integer, Double> idQuantityMap;
	
	private int mult;
	
	public NamedRecordAveQuantityComparator(Map<Integer, Double> idQuantityMap, boolean smallestFirst) {
		this.idQuantityMap = idQuantityMap;
		
		if (smallestFirst)
			mult = 1;
		else
			mult = -1;
	}
	
	public void setMap(Map<Integer, Double> idQuantityMap) {
		this.idQuantityMap = idQuantityMap;
	}
	
	private RecordDistComparator distCompare = new RecordDistComparator();
	
	private double getAve(FaultSectDistRecord record) {
		double val1 = idQuantityMap.get(record.getID1());
		double val2 = idQuantityMap.get(record.getID2());
		
		return (val1 + val2) * 0.5d;
	}

	@Override
	public int compare(NamedDistRecord o1, NamedDistRecord o2) {
		FaultSectDistRecord record1 = o1.getRecord();
		FaultSectDistRecord record2 = o2.getRecord();
		double avg1 = getAve(record1);
		double avg2 = getAve(record2);
		
		boolean nan1 = Double.isNaN(avg1);
		boolean nan2 = Double.isNaN(avg2);
		
		if (nan1 && nan2)
			return distCompare.compare(record1, record2);
		if (nan1 && !nan2)
			return 1;
		if (!nan1 && nan2)
			return -1;
		
		if (avg1 < avg2)
			return -1 * mult;
		else if (avg1 > avg2)
			return 1 * mult;
		// they're equal
		
		return distCompare.compare(record1, record2);
	}

}
