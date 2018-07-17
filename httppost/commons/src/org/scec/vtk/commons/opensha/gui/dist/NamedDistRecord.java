package org.scec.vtk.commons.opensha.gui.dist;

import org.opensha.commons.data.Named;
import org.opensha.refFaultParamDb.calc.sectionDists.FaultSectDistRecord;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

public class NamedDistRecord implements Named {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FaultSectDistRecord record;
	private String name;
	private AbstractFaultSection fault1;
	private AbstractFaultSection fault2;
	
	public NamedDistRecord(FaultSectDistRecord record, String name, AbstractFaultSection fault1, AbstractFaultSection fault2) {
		this.record = record;
		this.name = name;
		this.fault1 = fault1;
		this.fault2 = fault2;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}
	
	public FaultSectDistRecord getRecord() {
		return record;
	}

	public AbstractFaultSection getFault1() {
		return fault1;
	}

	public AbstractFaultSection getFault2() {
		return fault2;
	}

}
