package org.scec.vtk.commons.opensha.tree.builders;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
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
	
	protected static final String DB_SELECT_PARAM_NAME = "Fault Database";
	public static final String DB_SELECT_UCERF2 = "UCERF2";
	public static final String DB_SELECT_UCERF3 = "UCERF3 (development)";
	protected static final String DB_SELECT_DEFAULT = DB_SELECT_UCERF3;
	
	protected StringParameter dbSelectParam;
	
	private ParameterList builderParams = new ParameterList();
	
	private HashMap<String, DB_AccessAPI> dbs = new HashMap<String, DB_AccessAPI>();
	
	private HashMap<String, ViewFaultSection> viewFSMap = new HashMap<String, ViewFaultSection>();
	
	public MultiDBBuilder() {
		this(DB_SELECT_DEFAULT);
	}
	
	public MultiDBBuilder(String defaultDB) {
		ArrayList<String> values = new ArrayList<String>();
		values.add(DB_SELECT_UCERF3);
		values.add(DB_SELECT_UCERF2);
		
		dbSelectParam = new StringParameter(DB_SELECT_PARAM_NAME, values, defaultDB);
		dbSelectParam.addParameterChangeListener(this);
		builderParams.addParameter(dbSelectParam);
	}
	
	protected DB_AccessAPI getSelectedDB() {
		String dbName = dbSelectParam.getValue();
		DB_AccessAPI db = dbs.get(dbName);
		if (db == null) {
			if (dbName.equals(DB_SELECT_UCERF2))
				db = DB_ConnectionPool.getDB2ReadOnlyConn();
			else if (dbName.equals(DB_SELECT_UCERF3))
				db = DB_ConnectionPool.getDB3ReadOnlyConn();
			else
				throw new RuntimeException("Unkown database selected: " + dbName);
			dbs.put(dbName, db);
		}
		return db;
	}
	
	private ViewFaultSection getSelectedViewFS() {
		String dbName = dbSelectParam.getValue();
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
