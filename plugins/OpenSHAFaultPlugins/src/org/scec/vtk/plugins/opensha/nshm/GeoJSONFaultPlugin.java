package org.scec.vtk.plugins.opensha.nshm;

import java.awt.Color;
import java.util.ArrayList;

import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.commons.opensha.surfaces.LineSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.LineSurfaceGenerator.LineColoring;
import org.scec.vtk.plugins.opensha.AbstractFaultPlugin;
import org.scec.vtk.plugins.opensha.FaultPluginGUI;

public class GeoJSONFaultPlugin extends AbstractFaultPlugin {

	@Override
	protected FaultPluginGUI buildGUI() throws Exception {
//		ConnectionPointsDisplayPanel connsPanel = new ConnectionPointsDisplayPanel();
		GeoJSONFaultBuilder builder = new GeoJSONFaultBuilder(this);
		ArrayList<FaultColorer> colorers = builder.getColorers();
		ArrayList<GeometryGenerator> geomGens = FaultPluginGUI.createDefaultGeomGens();
		for (GeometryGenerator generator : geomGens) {
			if (generator instanceof LineSurfaceGenerator lines) {
				lines.setLineColoring(LineColoring.FIXED_OUTLINE);
				lines.setSize(1d);
				lines.setInteriorSize(1d);
				lines.setFixedLineColor(Color.WHITE);
			}
		}
		
		FaultPluginGUI gui = new FaultPluginGUI(this, builder, colorers, geomGens, Color.GRAY);
		// TODO
//		gui.addDistTab();
//		gui.addTab("Section Connections", connsPanel);
//		FaultZonesPanel zonesPanel = new FaultZonesPanel(gui.getEventManager());
//		gui.addTab("Zone Polygons", zonesPanel);
		return gui;
	}

}
