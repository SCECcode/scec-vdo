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

import vtk.vtkActor;

public abstract class AbstractFaultPlugin extends ActionPlugin {
	
//	private PluginInfo metadata;
	private FaultPluginGUI gui;
	
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

}
