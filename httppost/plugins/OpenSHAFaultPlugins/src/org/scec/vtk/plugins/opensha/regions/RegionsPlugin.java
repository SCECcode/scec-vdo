package org.scec.vtk.plugins.opensha.regions;

import java.awt.Color;
import java.util.ArrayList;

import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.plugins.opensha.AbstractFaultPlugin;
import org.scec.vtk.plugins.opensha.FaultPluginGUI;

public class RegionsPlugin extends AbstractFaultPlugin {

	@Override
	protected FaultPluginGUI buildGUI() throws Exception {
		RegionsBuilder builder = new RegionsBuilder();
		ArrayList<FaultColorer> colorers = null;
		ArrayList<GeometryGenerator> geomGens = FaultPluginGUI.createDefaultGeomGens();
		
		FaultPluginGUI gui = new FaultPluginGUI(this, builder, colorers, geomGens, Color.WHITE);
		
		return gui;
	}

}
