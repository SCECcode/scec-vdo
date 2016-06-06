package org.scec.vtk.plugins.opensha;

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.opensha.commons.util.ExceptionUtils;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.PluginInfo;

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

	@Override
	public void setClickableEnabled(boolean enable) {
//		System.out.println(metadata.getShortName()+": setClickableEnabled: " + enable);
		if (gui != null)
			gui.setClickableEnabled(enable);
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
