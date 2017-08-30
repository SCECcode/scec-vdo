package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.commons.util.ListUtils;
import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.commons.opensha.surfaces.LineSurfaceGenerator;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.opensha.AbstractFaultPlugin;
import org.scec.vtk.plugins.opensha.FaultPluginGUI;

public class EQSimsPlugin extends AbstractFaultPlugin {
	
	@Override
	protected FaultPluginGUI buildGUI() {
		EQSimsBuilder builder = new EQSimsBuilder();
		
		ArrayList<FaultColorer> colorers = builder.getColorers();
		ArrayList<GeometryGenerator> geomGens = FaultPluginGUI.createDefaultGeomGens();
		setBundlerInGeomGens(geomGens, new EQSimsFaultSectionBundler());
		
		int lineIndex = ListUtils.getIndexByName(geomGens, LineSurfaceGenerator.NAME);
		if (lineIndex > 0) {
			GeometryGenerator lines = geomGens.remove(lineIndex);
			geomGens.add(0, lines);
		}
		
		ArrayList<FaultAnimation> faultAnims = builder.getAnimations();
		
		FaultPluginGUI gui = new FaultPluginGUI(this, builder, colorers, geomGens, Color.GRAY, faultAnims);
//		gui.addDistTab(); // TODO?
		
		return gui;
	}
}