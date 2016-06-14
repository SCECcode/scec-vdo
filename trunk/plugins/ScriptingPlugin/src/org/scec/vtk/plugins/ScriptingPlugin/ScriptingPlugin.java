package org.scec.vtk.plugins.ScriptingPlugin;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.scec.vtk.drawingTools.DrawingToolsGUI;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.ActionPlugin;

import vtk.vtkActor;

public class ScriptingPlugin  extends ActionPlugin {
	private ScriptingPluginGUI gratPanel;

	public ScriptingPlugin() {
		// this.metadata = new PluginInfo("Grids", "Grids", "David & Genia",
		// "1.0");
	}

	public JPanel createGUI() {
		this.setGratPanel(new ScriptingPluginGUI(this));
		return this.getGratPanel();
	}

	public ScriptingPluginGUI getScriptingPluginGUI() {
		return getGratPanel();
	}

	 public void unload()
		{
		 for(int i =0;i<Info.getMainGUI().getmainFrame().getComponentCount();i++)
		
        {
        	if((Info.getMainGUI().getmainFrame().getComponent(i).getName()==getGratPanel().timelineTableContainer.getName()))
        	{
        		Info.getMainGUI().getmainFrame().remove(i);
        	}
        }
		 Info.getMainGUI().getmainFrame().repaint();
			super.unload();
			setGratPanel(null);
		}
	public ArrayList<vtkActor> getActors() {
		// TODO Auto-generated method stub
		return null;
	}

	public ScriptingPluginGUI getGratPanel() {
		return gratPanel;
	}

	public void setGratPanel(ScriptingPluginGUI gratPanel) {
		this.gratPanel = gratPanel;
	}
}
