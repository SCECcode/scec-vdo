package org.scec.vtk.plugins.opensha.nshm;

import java.awt.Color;
import java.util.ArrayList;

import org.scec.vtk.commons.opensha.faults.colorers.AseismicityColorer;
import org.scec.vtk.commons.opensha.faults.colorers.CouplingCoefficientColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.plugins.opensha.AbstractFaultPlugin;
import org.scec.vtk.plugins.opensha.FaultPluginGUI;

public class NSHM2023FaultPlugin extends AbstractFaultPlugin {

	@Override
	protected FaultPluginGUI buildGUI() throws Exception {
//		ConnectionPointsDisplayPanel connsPanel = new ConnectionPointsDisplayPanel();
		NSHM2023FaultBuilder builder = new NSHM2023FaultBuilder();
		ArrayList<FaultColorer> colorers = FaultPluginGUI.createDefaultColorers();
		colorers.add(new AseismicityColorer());
		colorers.add(new CouplingCoefficientColorer());
//		colorers.add(new OnlyNewColorer(false)); // TODO
		ArrayList<GeometryGenerator> geomGens = FaultPluginGUI.createDefaultGeomGens();
		
		FaultPluginGUI gui = new FaultPluginGUI(this, builder, colorers, geomGens, Color.GRAY);
		// TODO
//		gui.addDistTab();
//		gui.addTab("Section Connections", connsPanel);
//		FaultZonesPanel zonesPanel = new FaultZonesPanel(gui.getEventManager());
//		gui.addTab("Zone Polygons", zonesPanel);
		return gui;
	}

}
