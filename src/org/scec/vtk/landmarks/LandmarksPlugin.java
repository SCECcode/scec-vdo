package org.scec.vtk.landmarks;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.scec.vtk.grid.GraticuleGUI;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.politicalBoundaries.PoliticalBoundariesGUI;

import vtk.vtkActor;

public class LandmarksPlugin extends ActionPlugin implements StatefulPlugin{
		LandmarksGUI landmarkGUI;
		public LandmarksPlugin() {
			// this.metadata = new PluginInfo("Grids", "Grids", "David & Genia",
			// "1.0");
		}

		public JPanel createGUI() {
			this.landmarkGUI = new LandmarksGUI(this.getPluginActors());
			JPanel landmarkGUIPanel = this.landmarkGUI.loadLandmarks();
			//this.landmarkGUI.addPoliticalBoundaryActors();
			return landmarkGUIPanel;
		}

		public LandmarksGUI getLandmarksGUI() {
			return landmarkGUI;
		}

		@Override
		public PluginState getState() {
			// TODO Auto-generated method stub
			return null;
		}
	}