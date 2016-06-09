package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.commons.util.ListUtils;
import org.scec.geo3d.library.wgcep.faults.anim.FaultAnimation;
import org.scec.geo3d.library.wgcep.faults.colorers.FaultColorer;
import org.scec.geo3d.library.wgcep.surfaces.GeometryGenerator;
import org.scec.geo3d.library.wgcep.surfaces.LineSurfaceGenerator;
import org.scec.vtk.plugins.opensha.AbstractFaultPlugin;
import org.scec.vtk.plugins.opensha.FaultPluginGUI;

public class EQSimsPlugin extends AbstractFaultPlugin {
	
	@Override
	protected FaultPluginGUI buildGUI() {
		EQSimsBuilder builder = new EQSimsBuilder();
		
		ArrayList<FaultColorer> colorers = builder.getColorers();
		FaultAnimation faultAnim = builder.getFaultAnimation();
		ArrayList<GeometryGenerator> geomGens = FaultPluginGUI.createDefaultGeomGens();
		
		int lineIndex = ListUtils.getIndexByName(geomGens, LineSurfaceGenerator.NAME);
		if (lineIndex > 0) {
			GeometryGenerator lines = geomGens.remove(lineIndex);
			geomGens.add(0, lines);
		}
		
		ArrayList<FaultAnimation> faultAnims = new ArrayList<FaultAnimation>();
		faultAnims.add(faultAnim);
		
		FaultPluginGUI gui = new FaultPluginGUI(builder, colorers, geomGens, Color.GRAY, faultAnims);
//		gui.addDistTab(); // TODO?
		
		return gui;
	}
}