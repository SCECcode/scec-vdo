package org.scec.vtk.plugins.opensha;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.opensha.commons.util.ExceptionUtils;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundler;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;

import vtk.vtkActor;

public abstract class AbstractFaultPlugin extends ActionPlugin implements StatefulPlugin {
	
//	private PluginInfo metadata;
	private FaultPluginGUI gui;
	private FaultPluginState state;
	
	/**
	 * Build the Fault GUI. This will only be called once, and will be called before the
	 * branch group is added to the scene.
	 * 
	 * @return
	 */
	protected abstract FaultPluginGUI buildGUI() throws Exception;
	
	protected static void setBundlerInGeomGens(List<GeometryGenerator> geomGens, FaultActorBundler bundler) {
		for (GeometryGenerator geomGen : geomGens)
			geomGen.setFaultActorBundler(bundler);
	}

	@Override
	protected JComponent createGUI() throws IOException {
		if (gui == null) {
			try {
				gui = buildGUI();
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		return gui;
	}

	@Override
	public PluginState getState() {
		if (state == null)
			state = new FaultPluginState(gui);
		return state;
	}

	@Override
	public void setState(PluginState s) {
		// TODO Auto-generated method stub
	}

}
