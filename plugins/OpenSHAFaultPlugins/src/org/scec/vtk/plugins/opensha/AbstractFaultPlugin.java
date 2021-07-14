package org.scec.vtk.plugins.opensha;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.opensha.commons.util.ExceptionUtils;
import org.scec.vtk.commons.opensha.gui.anim.MultiAnimPanel;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundler;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.AnimatableChangeListener;
import org.scec.vtk.plugins.AnimatablePlugin;
import org.scec.vtk.plugins.PluginState;

public abstract class AbstractFaultPlugin extends ActionPlugin implements AnimatablePlugin {
	
//	private PluginInfo metadata;
	private FaultPluginGUI gui;
	private FaultPluginState state;
	
	private MultiAnimPanel animPanel;
	
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
				animPanel = gui.getAnimPanel();
				if (animPanel != null)
					animPanel.setPlugin(this);
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		return gui.getJXLayer();
	}

	@Override
	public PluginState getState() {
		if (state == null)
			state = new FaultPluginState(gui);
		return state;
	}

	@Override
	public void animationStarted() {
		if (animPanel != null)
			animPanel.animationStarted();
	}

	@Override
	public void animationEnded() {
		if (animPanel != null)
			animPanel.animationEnded();
	}

	@Override
	public void animationTimeChanged(double fractionalTime) {
		if (animPanel != null)
			animPanel.animationTimeChanged(fractionalTime);
	}

	@Override
	public boolean isAnimatable() {
		return animPanel != null && animPanel.isAnimatable();
	}

	@Override
	public void addAnimatableChangeListener(AnimatableChangeListener l) {
		if (animPanel != null)
			animPanel.addAnimatableChangeListener(l);
	}

	@Override
	public void removeAnimatableChangeListener(AnimatableChangeListener l) {
		if (animPanel != null)
			animPanel.removeAnimatableChangeListener(l);
	}

	

}
