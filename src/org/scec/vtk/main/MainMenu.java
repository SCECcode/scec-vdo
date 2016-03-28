package org.scec.vtk.main;

import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.scec.vtk.main.MainGUI.StayOpenCheckBoxMenuItem;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.politicalBoundaries.PoliticalBoundariesGUI;
import org.scec.vtk.tools.plugins.Plugins;

import vtk.vtkActor;
import vtk.vtkActorCollection;
import vtk.vtkAlgorithm;
import vtk.vtkAppendPolyData;
import vtk.vtkDataObject;
import vtk.vtkDataSet;
import vtk.vtkDoubleArray;
import vtk.vtkGenericDataObjectWriter;
import vtk.vtkGeoAssignCoordinates;
import vtk.vtkGraph;
import vtk.vtkGraphMapper;
import vtk.vtkGraphWriter;
import vtk.vtkMutableDirectedGraph;
import vtk.vtkOBJExporter;
import vtk.vtkOBJReader;
import vtk.vtkPNGWriter;
import vtk.vtkPanel;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkRenderWindow;
import vtk.vtkSTLWriter;
import vtk.vtkStructuredGrid;
import vtk.vtkStructuredGridWriter;
import vtk.vtkUnstructuredGrid;
import vtk.vtkUnstructuredGridWriter;
import vtk.vtkWindowToImageFilter;
import vtk.vtkXMLPolyDataReader;
import vtk.vtkXMLPolyDataWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class MainMenu implements ActionListener ,ItemListener{
	
	private MenuBar menuBar;
	private Menu fileMenu;
	private MenuItem fileOpen;
	private MenuItem saveItem ;
	private MenuItem appExit;
	
	//not used
	private Menu displayMenu;
	private Menu faults;
	private MenuItem communityFaultModel;
	//
	
	Map<String, PluginInfo> availablePlugins = null;
	static Map<String, Plugin> loadedPlugins = new HashMap<String, Plugin>();
	static Map<String, Plugin> activePlugins = new HashMap<String, Plugin>();
	private static Map<String, CheckboxMenuItem> pluginMenuItems = new HashMap<String, CheckboxMenuItem>();
	private static  Logger log = Logger.getLogger(MainGUI.class);
	public MainMenu(){
	//Creates the main menu bar.
	menuBar = new MenuBar();
		setupFileMenu();
		//setupDisplayMenu();
		
	}
	
	
	public MenuBar getMenuBar()
	{
		return menuBar;
	}
	
	private void setupFileMenu() {
		//File menu - save and open a scene.
		fileMenu = new Menu("File");
		fileOpen = new MenuItem("Open...");
		saveItem = new MenuItem("Save As...");
		appExit = new MenuItem("Quit");
		
		fileMenu.addActionListener(this);
		this.saveItem.addActionListener(this);
		this.appExit.addActionListener(this);
		this.fileOpen.addActionListener(this);

		this.fileMenu.add(fileOpen);
		this.fileMenu.add(saveItem);
		this.fileMenu.addSeparator();
		this.fileMenu.add(appExit);

		this.menuBar.add(fileMenu);
	}
	private void setupDisplayMenu() {
		//Display menu - different plugins
		displayMenu = new Menu("Display");
		//submenu - faults
		faults = new Menu("Faults");
		communityFaultModel = new MenuItem("Community Fault Model");
		
		displayMenu.addActionListener(this);
		this.faults.addActionListener(this);
		this.communityFaultModel.addActionListener(this);

		
		this.faults.add(communityFaultModel);
		this.displayMenu.add(faults);
		//this.faults.addSeparator();
		
		

		this.menuBar.add(displayMenu);
	}
	
	public void quit() {
		System.exit(0);
	}
	
	public void saveVTKObj()
	{
		//vtkOBJExporter objExporter = new vtkOBJExporter();
		//vtkXMLPolyDataWriter objExporter = new vtkXMLPolyDataWriter()
		vtkPanel renderWindow = MainGUI.getRenderWindow();
		//MainGUI.updateRenderWindow();
		vtkRenderWindow renWin = renderWindow.GetRenderWindow();
		/*objExporter.SetFilePrefix("testScene");
		objExporter.SetInput(renWin);
		objExporter.Update();
		objExporter.Write();
		*/
		vtkActorCollection actorlist = renderWindow.GetRenderer().GetActors();
		if(actorlist.GetNumberOfItems()>0){
			System.out.println(actorlist.GetNumberOfItems());
			 vtkPolyDataWriter objExporter = new vtkPolyDataWriter();
			 objExporter.SetFileName("testAll.vtk"); 
			 vtkAppendPolyData  mainData = new vtkAppendPolyData ();
			 //vtkPolyDataMapper maingMapper = new vtkPolyDataMapper();
			 //vtkActor maingActor = new vtkActor();
			for(int i = 0; i <actorlist.GetNumberOfItems();i++)
			{
				 vtkActor pbActor = (vtkActor) actorlist.GetItemAsObject(i);
				 //double[] c = pbActor.GetProperty().GetColor();
				 //vtkDoubleArray dc = new vtkDoubleArray();
				 if(pbActor.GetVisibility() == 1)
				 {
				 vtkPolyDataMapper gmapper = (vtkPolyDataMapper) pbActor.GetMapper();
				 //dc.SetNumberOfComponents(3);
				 //dc.SetName("Colors");
				 //for(int j = 0;j<3;j++)
				 //{
				 //dc.InsertNextTuple3(c[0]*Info.rgbMax, c[1]*Info.rgbMax, c[2]*Info.rgbMax);
				// }
				 /*if(c[0]==0)
				 {
					 System.out.println("here");
				 }*/
				 vtkPolyData pd  = new vtkPolyData();
				 //vtkPolyData pd = gmapper.GetInput();
				 pd.SetPoints(gmapper.GetInput().GetPoints());
				 pd.SetLines(gmapper.GetInput().GetLines());
				 pd.SetPolys(gmapper.GetInput().GetPolys());
				 //pd.GetPointData().SetScalars(dc);
				 mainData.AddInputData(pd);
				 mainData.Update();
				 }
			}
			objExporter.SetInputConnection(mainData.GetOutputPort());
			objExporter.Write();
			System.out.println("done");
		}
		/*ArrayList<ArrayList> actorPoliticalBoundariesMain = new ArrayList<ArrayList>();
		 ArrayList<vtkActor> actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
		actorPoliticalBoundariesSegments = MainGUI.pbGUI.getPoliticalBoundaries();
		 
		 if(actorPoliticalBoundariesSegments.size()>0){
			 for(int j =4;j<5;j++)
			 {
				 vtkActor pbActor = actorPoliticalBoundariesSegments.get(j);
				 vtkPolyDataMapper gmapper = (vtkPolyDataMapper) pbActor.GetMapper();

				
				 objExporter.SetInputData(gmapper.GetInput());
				 //objExporter.Update();
				 objExporter.Write();
			 }
		 }*/
		
	}
	public void openVTKObj()
	{

		 
		vtkPolyDataReader reader =new vtkPolyDataReader();
		  reader.SetFileName("testAll.vtk");
		  reader.Update();
		  //vtkXMLPolyDataReader reader = new vtkXMLPolyDataReader();
		  //reader.SetFileName("Coastlines_Los_Alamos.vtp");
		  //reader.Update();
		  vtkDoubleArray c1 = (vtkDoubleArray) reader.GetOutput().GetPointData().GetScalars("Colors");
			double[] c = c1.GetTuple3(0);
			Color color = new Color((int)c[0], (int) c[1], (int)c[2]); 
			//setColor(color);
			c[0] /= Info.rgbMax;
			c[1] /= Info.rgbMax;
			c[2] /= Info.rgbMax;
			
			vtkPolyData pd = new vtkPolyData();
			pd.SetPoints(reader.GetOutput().GetPoints());
			pd.SetLines(reader.GetOutput().GetLines());
			pd.SetPolys(reader.GetOutput().GetPolys());
			//pd.GetPointData().SetScalars(id0)
		  // Visualize
		  vtkPolyDataMapper mapper =new vtkPolyDataMapper();
		  //mapper.SetInputConnection(reader.GetOutputPort());
		 mapper.SetInputData(pd);
		  vtkActor actor = new vtkActor();
		  actor.SetMapper(mapper);
		  actor.GetProperty().SetColor(c);
		  vtkPanel renderWindow = MainGUI.getRenderWindow();
		  renderWindow.GetRenderer().AddActor(actor);
		  MainGUI.updateRenderWindow(actor);
	}
	
	//@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object eventSource = e.getSource();
		if (eventSource == appExit) {
			quit();
			}
		else if(eventSource == fileOpen)
		{
			openVTKObj();
		}
		else if(eventSource == saveItem)
		{
			saveVTKObj();
		}
		 else {
			System.out.println("Unhandled event");
		}
	}
	
	//plugin menu and events to load and unload plugins
	public void itemStateChanged(ItemEvent e) {
		// for checkboxitem menu
		Object eventSource = e.getSource();
		if (eventSource instanceof CheckboxMenuItem) {
			
	
		CheckboxMenuItem jmi = (CheckboxMenuItem) eventSource;
		if (jmi.getState()) {
			activatePlugin(jmi.getName());
		} else {
			passivatePlugin(jmi.getName());
		}
		}
		 else {
				System.out.println("Unhandled event");
			}
	}
     
   
	
	public static  Map<String, Plugin> getActivePlugins() {
		return activePlugins;
	}
	
	public  Map<String, Plugin> getLoadedPluginsAsMap() {
		return loadedPlugins;
	}

	public void setActivePlugins(Map<String, Plugin> activePlugins) {
		this.activePlugins = activePlugins;
	}
	
	boolean isPluginActive(String id) {
		// TODO Auto-generated method stub
		return getActivePlugins().containsKey(id);
	}
	
	void setupPluginMenus() {
		Vector<PluginInfo> pluginDescriptors = new Vector<PluginInfo>(
				availablePlugins.values());
		Collections.sort(pluginDescriptors);

		for (PluginInfo plugin : pluginDescriptors) {
			addPluginToMenu(plugin);
		}
	}
	private void addPluginToMenu(PluginInfo info) {
		// If the plugin has a menu
		if (info.hasMenu()) {

			// Create a menu item
			CheckboxMenuItem mi = new CheckboxMenuItem(info.getShortName());
			mi.setName(info.getId());
			mi.addItemListener(this);
			// Add to the list
			pluginMenuItems.put(info.getId(), mi);

			final String menuName = info.getMenuName();
			Menu menu = null;

			// Try to find the menu
			for (int i = 0; i < menuBar.getMenuCount(); i++) {
				Menu candidate = menuBar.getMenu(i);
				if (candidate != null
						&& candidate.getName().equalsIgnoreCase(menuName)) {
					menu = candidate;
					break;
				}
			}

			// If the menu was not found, then create it
			if (menu == null) {
				menu = new Menu();
				menu.setLabel(menuName);
				menu.setName(menuName);
				menuBar.add(menu);
			}

			final String submenuName = info.getSubmenuName();
			if (submenuName == null) {

				// If the plugin does not specify a submenu,
				// then add it to the regular menu
				menu.add((CheckboxMenuItem)mi);

			} else {

				// Try to find the submenu
				Menu submenu = null;
				for (int i = 0; i < (menu).getItemCount(); i++) {
					MenuItem candidate = menu.getItem(i);
					if (candidate != null && candidate instanceof Menu) {
						if (((Menu) candidate).getName().equalsIgnoreCase(
								submenuName)) {
							submenu = (Menu) candidate;
							break;
						}
					}
				}

				// If the submenu was not found, add it
				if (submenu == null) {
					submenu = new Menu();
					submenu.setLabel(submenuName);
					submenu.setName(submenuName);
					menu.add(submenu);
				}

				// Add the item to the submenu
				submenu.add(mi);
				
			}
		}
	}

	
	private void activatePlugin(String id) {
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			// If it is not loaded, try to load it
			if (loadedPlugins==null || !loadedPlugins.containsKey(id)) {
				loadPlugin(id);
			}

			log.debug("Activating plugin " + id);
			if (loadedPlugins.containsKey(id)) {

				// Update menu
				CheckboxMenuItem mi = pluginMenuItems.get(id);
				mi.setState(true);

				// Activate plugin
				Plugin plugin = loadedPlugins.get(id);
				getActivePlugins().put(id, plugin);
				
				//System.out.println("**************** Loaded plugins: " + loadedPlugins);

				plugin.activate();
				Info.getMainGUI().updateActors(plugin.getActors());

			} else {
				// Just in case loading fails for some odd reason
				log.warn("Unable to activate plugin: " + id);
			}
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	private void setCursor(Cursor predefinedCursor) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Passivate the selected plugin
	 * 
	 * @param id
	 *            The id of the plugin to passivate
	 */
	private void passivatePlugin(String id) {
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			log.debug("Passivating plugin " + id);
			if (loadedPlugins.containsKey(id)) {

				// Update menu
				updateMenu(id);
				
			} else {
				log.warn("Unknown plugin: " + id);
			}
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	private void loadPlugin(String id) {

		if (loadedPlugins!=null && loadedPlugins.containsKey(id)) {
			log.warn("Plugin " + id + " already loaded");
			return;
		}

		if (availablePlugins.containsKey(id)) {
			try {
				PluginInfo info = availablePlugins.get(id);
				log.debug("Loading plugin " + id);
				Plugin plugin = info.newInstance();
				loadedPlugins.put(id, plugin);
				plugin.load();
			} catch (Exception e) {
				throw new RuntimeException("Unable to load plugin: " + id, e);
			}
		} else {
			log.warn("Unknown plugin " + id);
		}
	}

	private void unloadPlugin(String id) {

		if (!loadedPlugins.containsKey(id)) {
			log.warn("Plugin " + id + " not loaded");
			return;
		}

		log.debug("Unloading plugin " + id);

		Plugin plugin = loadedPlugins.remove(id);
		plugin.unload();
	}
	public static void updateMenu(String id){
		CheckboxMenuItem mi = pluginMenuItems.get(id);
		mi.setState(false);
		
		// Passivate plugin
		Plugin plugin = loadedPlugins.get(id);
		plugin.unload();
		plugin.passivate();
		getActivePlugins().remove(id);
		loadedPlugins.remove(id);
	}


	
}
