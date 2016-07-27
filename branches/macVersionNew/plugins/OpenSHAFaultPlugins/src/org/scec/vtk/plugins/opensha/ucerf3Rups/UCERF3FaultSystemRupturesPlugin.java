package org.scec.vtk.plugins.opensha.ucerf3Rups;

import java.awt.Color;
import java.util.ArrayList;

import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.plugins.opensha.AbstractFaultPlugin;
import org.scec.vtk.plugins.opensha.FaultPluginGUI;

public class UCERF3FaultSystemRupturesPlugin extends AbstractFaultPlugin {
	
	@Override
	protected FaultPluginGUI buildGUI() throws Exception {
		//changed the parameter to take a branch group
		UCERF3FaultSystemRupturesBuilder builder = new UCERF3FaultSystemRupturesBuilder();

		ArrayList<FaultAnimation> faultAnims = builder.getFaultAnimations();
		ArrayList<FaultColorer> faultColorers = builder.getFaultColorers();

		ArrayList<GeometryGenerator> geomGens = FaultPluginGUI.createDefaultGeomGens();
		setBundlerInGeomGens(geomGens, new ParentFaultSectionBundler());

		FaultPluginGUI gui = new FaultPluginGUI(this, builder, faultColorers, geomGens, Color.GRAY, faultAnims);
//		gui.addDistTab();
//		UCERF3_FaultZonesPanel zonesPanel = new UCERF3_FaultZonesPanel(gui.getEventManager());
//		builder.addRupSetChangeListener(zonesPanel);
//		gui.addTab("Zone Polygons", zonesPanel);
		builder.setEventManager(gui.getEventManager());

		builder.setGeometryTypeSelector(gui.getGeomSelect());

		return gui;
	}

}
