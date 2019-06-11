package org.scec.vtk.plugins.opensha.faultModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.data.NamedComparator;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultModelSummaryDB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.tree.FaultCategoryNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.MultiDBBuilder;

import scratch.UCERF3.enumTreeBranches.FaultModels;

public class FaultModelBuilder extends MultiDBBuilder {
	
//	private ConnectionPointsDisplayPanel connsDisplay;

	public FaultModelBuilder() {
		super();
	}

	public FaultModelBuilder(DB_Source defaultDB) {
		super(defaultDB);
	}

	@Override
	public void buildTree(DefaultMutableTreeNode root) {
		DB_AccessAPI db = getSelectedDB();
		
//		connsDisplay.setDB(db);
		
		if (db == null) {
			HashSet<Integer> common = null;
			
			FaultModels[] fms = { FaultModels.FM3_1, FaultModels.FM3_2 };
			List<List<FaultSectionPrefData>> sectsList = new ArrayList<>();
			
			for (FaultModels fm : fms) {
				List<FaultSectionPrefData> sects = fm.fetchFaultSections();
				
				List<Integer> ids = new ArrayList<>();
				for (FaultSectionPrefData sect : sects) {
					ids.add(sect.getSectionId());
				}
				
				if (common == null)
					common = new HashSet<>(ids);
				else
					common.retainAll(ids);
				
				sectsList.add(sects);
			}
			
			for (int f = 0; f < fms.length; f++) {
				FaultModels fm = fms[f];
				List<FaultSectionPrefData> sects = sectsList.get(f);
				
				// ID numbers must be unique in the tree, but there are duplicates in each fault model
				// add the fault model number, plus a bunch of zeros, to each ID number
				String addIntStr = "";
				for (char c : fm.name().toCharArray())
					if (Character.isDigit(c))
						addIntStr += c;
				addIntStr += "000000";
				int idAdd = Integer.parseInt(addIntStr);
//				System.out.println("ID add: "+idAdd);
				
				FaultCategoryNode fmNode = new FaultCategoryNode(fm.getName());
				
				List<FaultSectionNode> commonNodes = new ArrayList<>();
				List<FaultSectionNode> uniqueNodes = new ArrayList<>();
				
				for (FaultSectionPrefData sect : sects) {
					int origID = sect.getSectionId();
					sect.setSectionId(origID+idAdd);
					PrefDataSection fault = new PrefDataSection(sect);
					
					// add it to the tree
					FaultSectionNode faultNode = new FaultSectionNode(fault);
					if (common.contains(origID))
						commonNodes.add(faultNode);
					else
						uniqueNodes.add(faultNode);
				}
				// we do it this way to sort them before adding
				Collections.sort(commonNodes, new NamedComparator());
				Collections.sort(uniqueNodes, new NamedComparator());
				
				FaultCategoryNode uniqueNode = new FaultCategoryNode("Unique to this model");
				for (FaultSectionNode faultNode : uniqueNodes)
					uniqueNode.add(faultNode);
				FaultCategoryNode commonNode = new FaultCategoryNode("Common other models");
				for (FaultSectionNode faultNode : commonNodes)
					commonNode.add(faultNode);
				
				fmNode.add(uniqueNode);
				fmNode.add(commonNode);
				
				root.add(fmNode);
			}
		} else {
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
	}
	
	private static void addAll(FaultCategoryNode fmNode, List<Integer> secIDs,
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
