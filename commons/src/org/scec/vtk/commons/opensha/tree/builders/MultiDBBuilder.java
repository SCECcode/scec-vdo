package org.scec.vtk.commons.opensha.tree.builders;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import javax.swing.JPanel;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.gui.view.ViewFaultSection;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.AbstractSimpleFaultDataFaultSection;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;

public abstract class MultiDBBuilder implements FaultTreeBuilder, ParameterChangeListener, FaultSectionInfoViewier {
	
	private ParameterList faultParams = PrefDataSection.createPrefDataParams();
	
	private TreeChangeListener l;
	
	protected enum DB_Source {
		UCERF3("UCERF3 DB") {
			@Override
			protected DB_AccessAPI build() {
				return DB_ConnectionPool.getDB3ReadOnlyConn();
			}
		},
		UCERF2("UCERF2 DB") {
			@Override
			protected DB_AccessAPI build() {
				return DB_ConnectionPool.getDB2ReadOnlyConn();
			}
		},
		CACHED("UCERF3 Cached (XML)") {
			@Override
			protected DB_AccessAPI build() {
				return null;
			}
		};
		
		private String name;

		private DB_Source(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
		
		protected abstract DB_AccessAPI build();
	}
	
	protected static final String DB_SELECT_PARAM_NAME = "Fault Database";
	protected static final DB_Source DB_SELECT_DEFAULT = DB_Source.CACHED;
	
	protected EnumParameter<DB_Source> dbSelectParam;
	
	private ParameterList builderParams = new ParameterList();
	
	private HashMap<DB_Source, DB_AccessAPI> dbs = new HashMap<>();
	
	private HashMap<DB_Source, ViewFaultSection> viewFSMap = new HashMap<>();
	
	public MultiDBBuilder() {
		this(DB_SELECT_DEFAULT);
	}
	
	public MultiDBBuilder(DB_Source defaultDB) {
		dbSelectParam = new EnumParameter<MultiDBBuilder.DB_Source>(DB_SELECT_PARAM_NAME, EnumSet.allOf(DB_Source.class), defaultDB, null);
		dbSelectParam.addParameterChangeListener(this);
		builderParams.addParameter(dbSelectParam);
	}
	
	protected DB_AccessAPI getSelectedDB() {
		DB_Source dbName = dbSelectParam.getValue();
		DB_AccessAPI db = dbs.get(dbName);
		if (db == null) {
			db = dbName.build();
			dbs.put(dbName, db);
		}
		return db;
	}
	
	private ViewFaultSection getSelectedViewFS() {
		DB_Source dbName = dbSelectParam.getValue();
		DB_AccessAPI db = getSelectedDB();
		
		if (!viewFSMap.containsKey(dbName)) {
			viewFSMap.put(dbName, new ViewFaultSection(db));
		}
		return viewFSMap.get(dbName);
	}

	@Override
	public ParameterList getBuilderParams() {
		return builderParams;
	}

	@Override
	public ParameterList getFaultParams() {
		return faultParams;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (l != null)
			l.treeChanged(null);
	}

	@Override
	public void setTreeChangeListener(TreeChangeListener l) {
		this.l = l;
	}

	@Override
	public JPanel getInfoPanel(AbstractFaultSection fault) {
		if (!(fault instanceof PrefDataSection))
				return null;
		ViewFaultSection viewFS = getSelectedViewFS();
		FaultSectionSummary summary = ((PrefDataSection)fault).getFaultSectionSummary();
		viewFS.setSelectedFaultSectionNameId(summary.getAsString());
		return viewFS;
	}

}
