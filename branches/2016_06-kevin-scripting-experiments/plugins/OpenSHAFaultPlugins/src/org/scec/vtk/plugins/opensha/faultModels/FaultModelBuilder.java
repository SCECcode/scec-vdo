package org.scec.vtk.plugins.opensha.faultModels;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.data.NamedComparator;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultModelSummaryDB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.scec.geo3d.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.geo3d.commons.opensha.tree.FaultCategoryNode;
import org.scec.geo3d.commons.opensha.tree.FaultSectionNode;
import org.scec.geo3d.commons.opensha.tree.builders.MultiDBBuilder;

public class FaultModelBuilder extends MultiDBBuilder {
	
//	private ConnectionPointsDisplayPanel connsDisplay;

	public FaultModelBuilder() {
		super();
//		this.connsDisplay = connsDisplay;
	}

	public FaultModelBuilder(String defaultDB) {
		super(defaultDB);
	}

	@Override
	public void buildTree(DefaultMutableTreeNode root) {
		DB_AccessAPI db = getSelectedDB();
		
//		connsDisplay.setDB(db);
		
		FaultModelSummaryDB_DAO faultModelSummaryDB_DAO = new FaultModelSummaryDB_DAO(db);
		FaultModelDB_DAO faultModelDB_DAO = new FaultModelDB_DAO(db);
		PrefFaultSectionDataDB_DAO faultSectionDB_DAO = new PrefFaultSectionDataDB_DAO(db);
		
		// cache everything
		faultSectionDB_DAO.getAllFaultSectionPrefData();
		
		ArrayList<FaultModelSummary> summaries = faultModelSummaryDB_DAO.getAllFaultModels();
//		
		ArrayList<ArrayList<Integer>> sectionsList = new ArrayList<ArrayList<Integer>>();
		
		for (FaultModelSummary fm : summaries) {
			sectionsList.add(faultModelDB_DAO.getFaultSectionIdList(fm.getFaultModelId()));
		}
		
		for (int i=0; i<summaries.size(); i++) {
			FaultModelSummary fm = summaries.get(i);
			ArrayList<Integer> sections = sectionsList.get(i);
			
			ArrayList<Integer> common = new ArrayList<Integer>();
			ArrayList<Integer> unique = (ArrayList<Integer>)sections.clone();
			
			for (int j=0; j<summaries.size(); j++) {
				if (i == j)
					continue;
				ArrayList<Integer> otherSections = sectionsList.get(j);
				for (int otherID : otherSections) {
					if (sections.contains(otherID) && !common.contains(otherID)) {
						common.add(otherID);
						unique.remove((Integer)otherID);
					}
				}
			}
			
			FaultCategoryNode fmNode = new FaultCategoryNode(fm.getFaultModelName() + " ("+fm.getFaultModelId()+")");
			
			FaultCategoryNode uniqueNode = new FaultCategoryNode("Unique to this model");
			addAll(uniqueNode, unique, faultSectionDB_DAO);
			fmNode.add(uniqueNode);
			
			FaultCategoryNode commonNode = new FaultCategoryNode("Common other models");
			common.removeAll(unique);
			addAll(commonNode, common, faultSectionDB_DAO);
			fmNode.add(commonNode);
			
			root.add(fmNode);
		}
	}
	
	private static void addAll(FaultCategoryNode fmNode, ArrayList<Integer> secIDs,
			PrefFaultSectionDataDB_DAO faultSectionDB_DAO) {
		ArrayList<FaultSectionNode> faultNodes = new ArrayList<FaultSectionNode>();
		for (int secID : secIDs) {
			FaultSectionPrefData prefData = faultSectionDB_DAO.getFaultSectionPrefData(secID);
			PrefDataSection fault = new PrefDataSection(prefData);
			
			// add it to the tree
			FaultSectionNode faultNode = new FaultSectionNode(fault);
			faultNodes.add(faultNode);
		}
		// we do it this way to sort them before adding
		Collections.sort(faultNodes, new NamedComparator());
		for (FaultSectionNode faultNode : faultNodes) {
			fmNode.add(faultNode);
		}
	}

}
