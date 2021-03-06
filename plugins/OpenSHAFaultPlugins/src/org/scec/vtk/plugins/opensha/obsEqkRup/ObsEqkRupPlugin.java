package org.scec.vtk.plugins.opensha.obsEqkRup;

import java.awt.Color;
import java.util.ArrayList;

import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.plugins.opensha.AbstractFaultPlugin;
import org.scec.vtk.plugins.opensha.FaultPluginGUI;

public class ObsEqkRupPlugin extends AbstractFaultPlugin {
	
	@Override
	protected FaultPluginGUI buildGUI() throws Exception {
		ObsEqkRupGeometryGenerator geomGen = new ObsEqkRupGeometryGenerator();
		ObsEqkRupAnim anim = new ObsEqkRupAnim(geomGen);
		ObsEqkRupBuilder builder = new ObsEqkRupBuilder(anim);

		ArrayList<FaultAnimation> faultAnims = new ArrayList<>();
		faultAnims.add(anim);
		ArrayList<FaultColorer> faultColorers = new ArrayList<>();
		faultColorers.add(new RupMagColorer());

		ArrayList<GeometryGenerator> geomGens = new ArrayList<>();
		geomGens.add(geomGen);
//		setBundlerInGeomGens(geomGens, new ParentFaultSectionBundler());
//		setBundlerInGeomGens(geomGens, null);

		FaultPluginGUI gui = new FaultPluginGUI(this, builder, faultColorers, geomGens, Color.GRAY, faultAnims);
//		gui.addDistTab();
//		UCERF3_FaultZonesPanel zonesPanel = new UCERF3_FaultZonesPanel(gui.getEventManager());
//		builder.addRupSetChangeListener(zonesPanel);
//		gui.addTab("Zone Polygons", zonesPanel);
//		builder.setEventManager(gui.getEventManager());

//		builder.setGeometryTypeSelector(gui.getGeomSelect());

		return gui;
	}

}
