package org.scec.vtk.plugins.opensha.faultModels;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.scec.geo3d.commons.opensha.faults.colorers.AseismicityColorer;
import org.scec.geo3d.commons.opensha.faults.colorers.CouplingCoefficientColorer;
import org.scec.geo3d.commons.opensha.faults.colorers.FaultColorer;
import org.scec.geo3d.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.plugins.opensha.AbstractFaultPlugin;
import org.scec.vtk.plugins.opensha.FaultPluginGUI;

import vtk.vtkActor;

public class FaultModelPlugin extends AbstractFaultPlugin {

	@Override
	protected FaultPluginGUI buildGUI() throws Exception {
//		ConnectionPointsDisplayPanel connsPanel = new ConnectionPointsDisplayPanel();
		FaultModelBuilder builder = new FaultModelBuilder();
		ArrayList<FaultColorer> colorers = FaultPluginGUI.createDefaultColorers();
		colorers.add(new AseismicityColorer());
		colorers.add(new CouplingCoefficientColorer());
//		colorers.add(new OnlyNewColorer(false)); // TODO
		ArrayList<GeometryGenerator> geomGens = FaultPluginGUI.createDefaultGeomGens();
		
		FaultPluginGUI gui = new FaultPluginGUI(builder, colorers, geomGens, Color.GRAY);
		// TODO
//		gui.addDistTab();
//		gui.addTab("Section Connections", connsPanel);
//		FaultZonesPanel zonesPanel = new FaultZonesPanel(gui.getEventManager());
//		gui.addTab("Zone Polygons", zonesPanel);
		return gui;
	}

}
