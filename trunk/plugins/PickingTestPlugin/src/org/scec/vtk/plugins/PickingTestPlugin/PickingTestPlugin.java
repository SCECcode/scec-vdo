package org.scec.vtk.plugins.PickingTestPlugin;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundle;
import org.scec.vtk.commons.opensha.surfaces.FaultActorBundler;
import org.scec.vtk.commons.opensha.surfaces.FaultSectionActorList;
import org.scec.vtk.commons.opensha.surfaces.FaultSectionBundledActorList;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.commons.opensha.surfaces.PolygonSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator.GeometryType;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator.PointArray;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.tools.Transform;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;
import org.scec.vtk.tools.picking.PointPickEnabledActor;

import com.google.common.base.Preconditions;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkCellPicker;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;

public class PickingTestPlugin extends ActionPlugin implements FaultActorBundler {
	
	private GriddedRegion reg;
	private NoBundlePickHandler noBundlePick;
	private BundlePickHandler bundlePick;
	private PolygonSurfaceGenerator polyGen;
	
	public PickingTestPlugin() {
		reg = new GriddedRegion(new Location(32, -125), new Location(43, -114), 0.2, null);
		noBundlePick = new NoBundlePickHandler();
		bundlePick = new BundlePickHandler();
	}
	
//	private void createSphere() {
//		//Create the sphere
//		sphere = new vtkSphereSource();
//		//Set the size
//		sphere.SetRadius(20.0);
//		//Put at USC
//		double[] coords = {34.0192, -118.286};
//		double[] xyzCoords = Transform.transformLatLon(coords[0], coords[1]);
//		sphere.SetCenter(xyzCoords);
//		//Create mapper and actor
//		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
//		mapper.SetInputConnection(sphere.GetOutputPort());
//		actor = new vtkActor();
//		actor.SetMapper(mapper);
//	}
//		
//	public void loadSphere() {
//		if (sphereLoaded==true) {
//			return;
//		}
//		if (sphere==null) {
//			createSphere();
//		}
//		System.out.println("Loading sphere.");
//		actor.SetVisibility(1);
//		actor.Modified();
//		// will only add if not already present
//		getPluginActors().addActor(actor);
//		MainGUI.updateRenderWindow();
//		sphereLoaded = true;
//	}
//	
//	public void unloadSphere() {
//		if (sphereLoaded==false) {
//			return;
//		}
//		System.out.println("Unloading sphere.");
//		actor.SetVisibility(0);
//		MainGUI.updateRenderWindow();
//		sphereLoaded = false;
//	}
	
	public void clear() {
		getPluginActors().clearActors();
	}
	
	public void display() {
		System.out.println("Displaying "+reg.getNodeCount()+" actors (no bundle)");
		
		LocationList nodeList = reg.getNodeList();
		
		polyGen = new PolygonSurfaceGenerator();
		polyGen.setBundlerEneabled(false);
		polyGen.setPickHandler(noBundlePick);
		
		for (int n=0; n<reg.getNodeCount(); n++) {
			FaultSectionActorList actorList = polyGen.createFaultActors(GeometryType.POLYGON, getPoly(nodeList.get(n)), regColor, 0.7, null);
			getPluginActors().addActor(actorList.get(0));
		}
	}
	
	private FaultActorBundle bundle;

	@Override
	public synchronized FaultActorBundle getBundle(AbstractFaultSection fault) {
		if (bundle == null)
			bundle = new FaultActorBundle();
		return bundle;
	}

	@Override
	public synchronized void clearBundles() {
		bundle = null;
	}
	
	private Map<AbstractFaultSection, FaultSectionBundledActorList> sectMap;
	
	private static class FakeFaultSection extends AbstractFaultSection {

		public FakeFaultSection(String name, int id) {
			super(name, id);
		}

		@Override
		public Surface3D createSurface(ParameterList faultRepresentationParams) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double getSlipRate() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getAvgRake() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getAvgStrike() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getAvgDip() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	public void displayBundled() {
		System.out.println("Displaying bundled!");
		
		sectMap = new HashMap<>();
		
		LocationList nodeList = reg.getNodeList();
		
		polyGen = new PolygonSurfaceGenerator();
		polyGen.setBundlerEneabled(true);
		polyGen.setFaultActorBundler(this);
		polyGen.setPickHandler(bundlePick);
		clearBundles();
		
		for (int n=0; n<reg.getNodeCount(); n++) {
			FakeFaultSection sect = new FakeFaultSection("Sect "+n, n);
			
			FaultSectionActorList actorList = polyGen.createFaultActors(GeometryType.POLYGON, getPoly(nodeList.get(n)), regColor, 0.7, sect);
			Preconditions.checkState(actorList instanceof FaultSectionBundledActorList);
			sectMap.put(sect, (FaultSectionBundledActorList)actorList);
		}
		for (FaultSectionBundledActorList actorList : sectMap.values())
			actorList.getBundle().setVisible(actorList, true);
		PointPickEnabledActor<AbstractFaultSection> actor = getBundle(null).getActor();
		actor.Modified();
		actor.SetVisibility(1);
		actor.SetPickable(1);
		getPluginActors().addActor(actor);
	}
	
	private List<PointArray> getPoly(Location loc) {
		double spacing = reg.getSpacing();
		Location ll = loc;
		double lat = ll.getLatitude();
		double lon = ll.getLongitude();
		Location ul = new Location(lat+spacing, lon);
		Location ur = new Location(lat+spacing, lon+spacing);
		Location lr = new Location(lat, lon+spacing);
		
		List<PointArray> polygons = new ArrayList<>();
		
		double[][] points = new double[4][];
		
		points[0] = GeometryGenerator.getPointForLoc(ll);
		points[1] = GeometryGenerator.getPointForLoc(ul);
		points[2] = GeometryGenerator.getPointForLoc(ur);
		points[3] = GeometryGenerator.getPointForLoc(lr);
		
		polygons.add(new PointArray(points));
		return polygons;
	}
	
	private static final Color pickedColor = Color.RED;
	private static final double[] pickedColorArray = GeometryGenerator.getColorDoubleArray(pickedColor);
	private static final Color regColor = Color.BLUE;
	private static final double[] regColorArray = GeometryGenerator.getColorDoubleArray(regColor);
	
	private class NoBundlePickHandler implements PickHandler<AbstractFaultSection> {
		
		private vtkActor prev = null;

		@Override
		public synchronized void actorPicked(PickEnabledActor<AbstractFaultSection> actor, AbstractFaultSection reference, vtkCellPicker picker,
				MouseEvent e) {
			System.out.println("Picked!");
			
			actor.GetProperty().SetColor(pickedColorArray);
			actor.Modified();
			if (prev != null) {
				prev.GetProperty().SetColor(regColorArray);
				prev.Modified();
			}
			prev = actor;
			MainGUI.updateRenderWindow();
		}
		
	}
	
	private class BundlePickHandler implements PickHandler<AbstractFaultSection> {
		private FaultSectionBundledActorList prev = null;

		@Override
		public synchronized void actorPicked(PickEnabledActor<AbstractFaultSection> actor, AbstractFaultSection reference, vtkCellPicker picker,
				MouseEvent e) {
			FaultSectionBundledActorList list = sectMap.get(reference);
			if (list != null) {
				System.out.println("Picked!");
				polyGen.updateColor(list, pickedColor);
				if (prev != null)
					polyGen.updateColor(prev, regColor);
				prev = list;
				
				MainGUI.updateRenderWindow();
			}
		}
		
	}

	@Override
	//This method creates the JPanel, which is returned to main to display
	protected JPanel createGUI() throws IOException {
		PickingTestPluginGUI dpg = new PickingTestPluginGUI(this);
		return dpg.getPanel();
	}
}
