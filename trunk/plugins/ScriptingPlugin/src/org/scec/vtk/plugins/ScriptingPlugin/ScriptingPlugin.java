package org.scec.vtk.plugins.ScriptingPlugin;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.scec.vtk.drawingTools.DrawingToolsGUI;
import org.scec.vtk.plugins.ActionPlugin;

import vtk.vtkActor;

public class ScriptingPlugin  extends ActionPlugin {
	ScriptingPluginGUI gratPanel;

	public ScriptingPlugin() {
		// this.metadata = new PluginInfo("Grids", "Grids", "David & Genia",
		// "1.0");
	}

	public JPanel createGUI() {
		this.gratPanel = new ScriptingPluginGUI(this);
		return this.gratPanel;
	}

	public ScriptingPluginGUI getScriptingPluginGUI() {
		return gratPanel;
	}

	public ArrayList<vtkActor> getActors() {
		// TODO Auto-generated method stub
		return null;
	}
}
