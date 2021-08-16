package org.scec.vtk.plugins.opensha.obsEqkRup;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.dom4j.DocumentException;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.observedEarthquake.parsers.UCERF3_CatalogParser;
import org.opensha.sha.faultSurface.PointSurface;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.ObsEqkRupSection;
import org.scec.vtk.commons.opensha.tree.FaultCategoryNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;

import com.google.common.base.Preconditions;

import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.erf.ETAS.association.FiniteFaultMappingData;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;

public class ObsEqkRupBuilder implements FaultTreeBuilder, ParameterChangeListener {
	
	private FileParameter catalogFileParam;
	private FileParameter finiteSurfsFileParam;
	private FileParameter rupSetFileParam;
	private BooleanParameter finiteOnlyParam;
	
	private FaultModels fm;
	private FaultSystemRupSet rupSet;
	
	private TreeChangeListener l;
	private ParameterList params;
	
	private ObsEqkRupAnim anim;
	
	public ObsEqkRupBuilder(ObsEqkRupAnim anim) {
		this.anim = anim;
		
		params = new ParameterList();
		
		catalogFileParam = new FileParameter("Catalog File");
		catalogFileParam.addParameterChangeListener(this);
		params.addParameter(catalogFileParam);
		
		finiteSurfsFileParam = new FileParameter("Finite Surfaces XML File");
		finiteSurfsFileParam.addParameterChangeListener(this);
		params.addParameter(finiteSurfsFileParam);
		
		rupSetFileParam = new FileParameter("Rupture Set File");
		rupSetFileParam.addParameterChangeListener(this);
		params.addParameter(rupSetFileParam);
		
		finiteOnlyParam = new BooleanParameter("Finite Surface Ruptures Only", false);
		finiteOnlyParam.addParameterChangeListener(this);
		params.addParameter(finiteOnlyParam);
		
		File kevinGitDir = new File("/home/kevin/git/ucerf3-etas-launcher/inputs");
		if (kevinGitDir.exists()) {
			// interns: don't remove this. add an else statement if you insist on another default dir
			catalogFileParam.setDefaultInitialDir(kevinGitDir);
			finiteSurfsFileParam.setDefaultInitialDir(kevinGitDir);
			rupSetFileParam.setDefaultInitialDir(kevinGitDir);
		}
	}

	@Override
	public ParameterList getBuilderParams() {
		return params;
	}

	@Override
	public ParameterList getFaultParams() {
		return null;
	}

	@Override
	public void setTreeChangeListener(TreeChangeListener l) {
		this.l = l;
	}

	@Override
	public void buildTree(DefaultMutableTreeNode root) {
		File catalogFile = catalogFileParam.getValue();
		if (catalogFile == null)
			return;
		File finiteSurfsFile = finiteSurfsFileParam.getValue();
		
		
		ObsEqkRupList rups;
		try {
			// load UCERF3 catalog
			rups = UCERF3_CatalogParser.loadCatalog(catalogFile);
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		
		int cnt = 0;
		FaultCategoryNode pointSourcesNode = null;
		if (!finiteOnlyParam.getValue()) {
			pointSourcesNode = new FaultCategoryNode("Point Sources");
			root.add(pointSourcesNode);
		}
		
		FaultCategoryNode finiteSourcesNode = null;
		if (finiteSurfsFile != null && rupSet != null) {
			System.out.println("Loading finite sources");
			try {
				FiniteFaultMappingData.loadRuptureSurfaces(finiteSurfsFile, rups, fm, rupSet);
			} catch (MalformedURLException | DocumentException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
			finiteSourcesNode = new FaultCategoryNode("Finite Sources");
			root.add(finiteSourcesNode);
		}

		ObsEqkRupList displayRups = new ObsEqkRupList();
		for (ObsEqkRupture rup : rups) {
			String name = rup.getEventId()+": M"+magDF.format(rup.getMag())+", "+df.format(new Date(rup.getOriginTime()));
			if (rup.getRuptureSurface() == null || rup.getRuptureSurface() instanceof PointSurface) {
				if (pointSourcesNode == null)
					continue;
				pointSourcesNode.add(new FaultSectionNode(new ObsEqkRupSection(name, cnt++, rup)));
			} else {
				Preconditions.checkNotNull(finiteSourcesNode);
				finiteSourcesNode.add(new FaultSectionNode(new ObsEqkRupSection(name, cnt++, rup)));
			}
			displayRups.add(rup);
		}
		
		anim.setRups(displayRups);
	}
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
	private static DecimalFormat magDF = new DecimalFormat("0.##");

	@Override
	public void parameterChange(ParameterChangeEvent e) {
		if (e.getParameter() == catalogFileParam) {
			l.treeChanged(null);
			rupSetFileParam.setDefaultInitialDir(catalogFileParam.getValue().getParentFile());
			finiteSurfsFileParam.setDefaultInitialDir(catalogFileParam.getValue().getParentFile());
		} else if (e.getParameter() == finiteSurfsFileParam) {
			if (rupSetFileParam.getValue() == null) {
				rupSetFileParam.setDefaultInitialDir(finiteSurfsFileParam.getValue().getParentFile());
				JOptionPane.showMessageDialog(null, "Must Load Rupture Set zip file before finite surfaces can be loaded",
						"Load RupSet File Next", JOptionPane.INFORMATION_MESSAGE);
			} else {
				l.treeChanged(null);
			}
		} else if (e.getParameter() == rupSetFileParam) {
			File rupSetFile = rupSetFileParam.getValue();
			try {
				rupSet = FaultSystemRupSet.load(rupSetFile);
				if (rupSet instanceof InversionFaultSystemRupSet) {
					fm = ((InversionFaultSystemRupSet)rupSet).getFaultModel();
				} else {
					// first try from name
					fm = null;
					for (FaultModels fm : FaultModels.values())
						if (rupSetFile.getName().contains(fm.encodeChoiceString()))
							this.fm = fm;
					if (fm == null) {
						// prompt
						EnumParameter<FaultModels> fmParam = new EnumParameter<FaultModels>("Fault Model", EnumSet.allOf(FaultModels.class),
								FaultModels.FM3_1, null);
						JOptionPane.showMessageDialog(null, fmParam.getEditor(), "Select Fault Model", JOptionPane.QUESTION_MESSAGE);
						fm = fmParam.getValue();
					} else {
						System.out.println("Detected Fault Model: "+fm.getShortName());
					}
				}
			} catch (IOException e1) {
				throw ExceptionUtils.asRuntimeException(e1);
			}
			if (finiteSurfsFileParam.getValue() != null)
				l.treeChanged(null);
			else
				finiteSurfsFileParam.setDefaultInitialDir(rupSetFile.getParentFile());
		} else if (e.getParameter() == finiteOnlyParam) {
			l.treeChanged(null);
		}
	}
	
	public static FaultModels detectFM(FaultSystemRupSet rupSet, File rupSetFile) {
		if (rupSet instanceof InversionFaultSystemRupSet) {
			return ((InversionFaultSystemRupSet)rupSet).getFaultModel();
		} else {
			if (rupSetFile != null) {
				for (FaultModels fm : FaultModels.values())
					if (rupSetFile.getName().contains(fm.encodeChoiceString()))
						return fm;
			}
			// now try by the numbers
			if (rupSet.getNumRuptures() == 253706)
				return FaultModels.FM3_1;
			if (rupSet.getNumRuptures() == 305709)
				return FaultModels.FM3_2;
			// prompt
			EnumParameter<FaultModels> fmParam = new EnumParameter<FaultModels>("Fault Model", EnumSet.allOf(FaultModels.class),
					FaultModels.FM3_1, null);
			JOptionPane.showMessageDialog(null, fmParam.getEditor(), "Select Fault Model", JOptionPane.QUESTION_MESSAGE);
			return fmParam.getValue();
		}
	}

}
