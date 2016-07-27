package org.scec.vtk.plugins.DummyPlugin;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.tools.Transform;

import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;

/* This is an example plugin.  Its GUI contains a checkbox which will turn on and off a sphere, centered at USC.
 * Hopefully this helps to illustrate how to create a plugin in SCEC-VDO.
 */

//Basically all plugins (any with a GUI) will extend ActionPlugin, which takes care of loading and unloading the GUI
public class DummyPlugin extends ActionPlugin {
	private boolean sphereLoaded = false;
	private vtkSphereSource sphere = null;
	private vtkActor actor = null;
	
	public DummyPlugin() {
	}
	
	private void createSphere() {
		//Create the sphere
		sphere = new vtkSphereSource();
		//Set the size
		sphere.SetRadius(20.0);
		//Put at USC
		double[] coords = {34.0192, -118.286};
		double[] xyzCoords = Transform.transformLatLon(coords[0], coords[1]);
		sphere.SetCenter(xyzCoords);
		//Create mapper and actor
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.SetInputConnection(sphere.GetOutputPort());
		actor = new vtkActor();
		actor.SetMapper(mapper);
	}
		
	public void loadSphere() {
		if (sphereLoaded==true) {
			return;
		}
		if (sphere==null) {
			createSphere();
		}
		System.out.println("Loading sphere.");
		actor.SetVisibility(1);
		actor.Modified();
		// will only add if not already present
		getPluginActors().addActor(actor);
		MainGUI.updateRenderWindow();
		sphereLoaded = true;
	}
	
	public void unloadSphere() {
		if (sphereLoaded==false) {
			return;
		}
		System.out.println("Unloading sphere.");
		actor.SetVisibility(0);
		MainGUI.updateRenderWindow();
		sphereLoaded = false;
	}

	@Override
	//This method creates the JPanel, which is returned to main to display
	protected JPanel createGUI() throws IOException {
		DummyPluginGUI dpg = new DummyPluginGUI(this);
		return dpg.getPanel();
	}

	
}
