package org.scec.vtk.drawingTools;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.scec.vtk.grid.GraticuleGUI;
import org.scec.vtk.plugins.ActionPlugin;

import vtk.vtkActor;

public class DrawingToolsPlugin extends ActionPlugin {
		DrawingToolsGUI gratPanel;

		public DrawingToolsPlugin() {
			// this.metadata = new PluginInfo("Grids", "Grids", "David & Genia",
			// "1.0");
		}

		public JPanel createGUI() {
			this.gratPanel = new DrawingToolsGUI(this);
			return this.gratPanel;
		}

		public DrawingToolsGUI getGraticuleGUI() {
			return gratPanel;
		}

		@Override
		public ArrayList<vtkActor> getActors() {
			// TODO Auto-generated method stub
			return null;
		}
	}