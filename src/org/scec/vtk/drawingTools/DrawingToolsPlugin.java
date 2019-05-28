package org.scec.vtk.drawingTools;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.scec.vtk.grid.GraticuleGUI;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;

import vtk.vtkActor;

public class DrawingToolsPlugin extends ActionPlugin implements StatefulPlugin{
		DrawingToolsGUI gratPanel;
		DrawingToolsPluginState state;
		public DrawingToolsPlugin() {
			// this.metadata = new PluginInfo("Grids", "Grids", "David & Genia",
			// "1.0");
		}

		public JPanel createGUI() {
			this.gratPanel = new DrawingToolsGUI(this.getPluginActors());
			return this.gratPanel;
		}

		public DrawingToolsGUI getGraticuleGUI() {
			return gratPanel;
		}

		@Override
		public PluginState getState() {
			if(state ==null)
			{
				state = new DrawingToolsPluginState(gratPanel);
			}
			return state;
		}
	}