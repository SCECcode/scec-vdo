package org.scec.vtk.main;

import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.timeline.gui.TimelineGUI;
import org.scec.vtk.timeline.gui.ViewerSizePanel;

import vtk.vtkActor;
import vtk.vtkActorCollection;
import vtk.vtkAppendPolyData;
import vtk.vtkDoubleArray;
import vtk.vtkOBJExporter;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.rendering.jogl.vtkJoglPanelComponent;

import com.google.common.base.Preconditions;


public class MainMenu implements ActionListener, ItemListener{

	private MenuBar menuBar;
	private Menu fileMenu;
	private MenuItem fileOpen;
	private MenuItem saveItem;
	private MenuItem appExit;
	private Menu windowMenu ;
	private Timeline timeline;
	private TimelineGUI timelineGUI;
	private JFrame timelineFrame;
	private CheckboxMenuItem timelineItem;
	private MenuItem saveItemVTK;
	private MenuItem saveItemOBJ;
	private MenuItem resizeWindow;
	private ViewerSizePanel sizePanel;
	private CheckboxMenuItem focalPointItem;

	static Map<String, PluginInfo> availablePlugins = new HashMap<String, PluginInfo>();
	// TODO why are these static?
	static Map<String, Plugin> loadedPlugins = new HashMap<String, Plugin>();
	static Map<Plugin, PluginActors> pluginActors = new HashMap<>();
	static Map<String, Plugin> activePlugins = new HashMap<String, Plugin>();
	private static Map<String, CheckboxMenuItem> pluginMenuItems = new HashMap<String, CheckboxMenuItem>();
	private static  Logger log = Logger.getLogger(MainGUI.class);

	public MainMenu(){
		//Creates the main menu bar.
		menuBar = new MenuBar();
		setupFileMenu();

		// manually add Display menu so that it is second from the left
		Menu displayMenu = new Menu();
		displayMenu.setLabel("Display");
		displayMenu.setName("Display");
		focalPointItem = new CheckboxMenuItem("Focal Point", false);
		Menu trainingMenu = getCreateSubMenu(displayMenu, "Training");
		trainingMenu.add(focalPointItem);
		focalPointItem.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				Info.getMainGUI().setFocalPointVisible(focalPointItem.getState());
			}
		});
		menuBar.add(displayMenu);
		

		setupWindowMenu();
	}


	private void setupWindowMenu() {
		// TODO Auto-generated method stub
		windowMenu = new Menu();
		windowMenu.setLabel("Window");
		windowMenu.setName("Window");
	
		
		
		resizeWindow = new MenuItem("Resize render window");
		windowMenu.add(resizeWindow);
		menuBar.add(windowMenu);
		windowMenu.addActionListener(this);
		this.resizeWindow.addActionListener(this);
	}


	public MenuBar getMenuBar()
	{
		return menuBar;
	}

	public void setupTimeline(Timeline timeline, TimelineGUI timelineGUI) {
		Preconditions.checkState(this.timeline == null, "Timeline already initialized!");
		this.timeline = timeline;
		this.timelineGUI = timelineGUI;

		Menu menu = getMenuByName("Render");

		// If the menu was not found, then create it. A plugin could use this menu name which is why we check ahead of time
		if (menu == null) {
			menu = new Menu();
			menu.setLabel("Render");
			menu.setName("Render");
			menuBar.add(menu);
		}

		timelineItem = new CheckboxMenuItem("Timeline");
		timelineItem.setName("Timeline");
		timelineItem.addItemListener(this);

		menu.add(timelineItem);
	}

	private void setupFileMenu() {
		//File menu - save and open a scene.
		fileMenu = new Menu("File");
		fileOpen = new MenuItem("Open...");
		saveItem = new MenuItem("Save state...");
		saveItemVTK = new MenuItem("Save as VTK...");
		saveItemOBJ = new MenuItem("Save as OBJ...");
		appExit = new MenuItem("Quit");

		fileMenu.addActionListener(this);
		this.saveItem.addActionListener(this);
		this.saveItemVTK.addActionListener(this);
		this.saveItemOBJ.addActionListener(this);
		this.appExit.addActionListener(this);
		this.fileOpen.addActionListener(this);

		this.fileMenu.add(fileOpen);
		this.fileMenu.add(saveItem);
		this.fileMenu.add(saveItemVTK);
		this.fileMenu.add(saveItemOBJ);
		this.fileMenu.addSeparator();
		this.fileMenu.add(appExit);

		this.menuBar.add(fileMenu);
	}

	public void quit() {
		System.exit(0);
	}

	public void saveObj(File file)
	{
		vtkActorCollection actorlist =  Info.getMainGUI().getRenderWindow().getRenderer().GetActors();
		if(actorlist.GetNumberOfItems()>0){
			System.out.println(actorlist.GetNumberOfItems());
			vtkOBJExporter objExporter = new vtkOBJExporter();
			objExporter.SetFilePrefix(file.getPath()+".obj"); 
			objExporter.SetRenderWindow(Info.getMainGUI().getRenderWindow().getRenderWindow());
			objExporter.Write();
			System.out.println("done");
		}
	}
	
	public void saveVTKObj(File file)
	{

		vtkJoglPanelComponent renderWindow = Info.getMainGUI().getRenderWindow();

		vtkActorCollection actorlist = renderWindow.getRenderer().GetActors();
		if(actorlist.GetNumberOfItems()>0){
			System.out.println(actorlist.GetNumberOfItems());
			vtkPolyDataWriter objExporter = new vtkPolyDataWriter();
			objExporter.SetFileName(file.getPath()+".vtk"); 
			vtkAppendPolyData  mainData = new vtkAppendPolyData ();

			for(int i = 0; i <actorlist.GetNumberOfItems();i++)
			{
				vtkActor pbActor = (vtkActor) actorlist.GetItemAsObject(i);

				if(pbActor.GetVisibility() == 1)
				{
					vtkPolyDataMapper gmapper = (vtkPolyDataMapper) pbActor.GetMapper();
					if(gmapper!=null){
					vtkPolyData pd  = new vtkPolyData();
					pd.SetPoints(gmapper.GetInput().GetPoints());
					pd.SetLines(gmapper.GetInput().GetLines());
					pd.SetPolys(gmapper.GetInput().GetPolys());
					mainData.AddInputData(pd);
					mainData.Update();
					}
				}
			}
			objExporter.SetInputConnection(mainData.GetOutputPort());
			objExporter.Write();
			System.out.println("done");
		}
		
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
		vtkJoglPanelComponent renderWindow = MainGUI.getRenderWindow();
		renderWindow.getRenderer().AddActor(actor);
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
			JFileChooser chooser = new JFileChooser();
			int ret = chooser.showOpenDialog(Info.getMainGUI());
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				SAXReader reader = new SAXReader();
				try {
					Document document = reader.read(file.getPath());
					Element root = document.getRootElement();
					// iterate through child elements of root
					Vector<PluginInfo> pluginDescriptors = new Vector<PluginInfo>(
							availablePlugins.values());
					for(PluginInfo pluginDescriptor:pluginDescriptors) {
						//System.out.println(pluginDescriptor.getName());
						for ( Iterator i = root.elementIterator(pluginDescriptor.getName().replace(' ' ,'-')); i.hasNext(); ) {
							Element pluginNameElement = (Element) i.next();
							try {
								if(!activePlugins.containsKey(pluginDescriptor.getId()))
									activatePlugin(pluginDescriptor.getId());
								Plugin plugin = activePlugins.get(pluginDescriptor.getId());
								if (plugin instanceof StatefulPlugin) {
									((StatefulPlugin)plugin).getState().fromXML(pluginNameElement);

									((StatefulPlugin)plugin).getState().load();
								}
							} catch (Exception e1) {
								System.err.println("WARNING: Error loading plugin state from XML: "+pluginDescriptor.getName());
								e1.printStackTrace();
							}
						}
					}
					//open timeline state
					Element pluginNameElement = root.element("Timeline-Plugin");
					timeline.getState().fromXML(pluginNameElement);
					timeline.getState().load();
					timelineGUI.getTimeLinePanel().timelinePluginsChanged();
				} catch (DocumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			//	openVTKObj();
		}
		else if(eventSource == saveItemVTK)
		{
			JFileChooser chooser = new JFileChooser();
			int ret = chooser.showSaveDialog(Info.getMainGUI());
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				saveVTKObj(file);
			}
		}
		else if(eventSource == saveItemOBJ)
		{
			JFileChooser chooser = new JFileChooser();
			int ret = chooser.showSaveDialog(Info.getMainGUI());
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				saveObj(file);
			}
		}
		else if(eventSource == saveItem)
		{
			JFileChooser chooser = new JFileChooser();
			int ret = chooser.showSaveDialog(Info.getMainGUI());
			if (ret == JFileChooser.APPROVE_OPTION) {
				Document document = DocumentHelper.createDocument();
				Element root = document.addElement("root");
				File file = chooser.getSelectedFile();
				String destinationData =  file.getPath();//Prefs.getLibLoc() + File.separator;

				Vector<Plugin> pluginDescriptors = new Vector<Plugin>(
						loadedPlugins.values());
				for(Plugin pluginDescriptor:pluginDescriptors)
				{

					Plugin plugin = activePlugins.get(pluginDescriptor.getId());
					if (plugin instanceof StatefulPlugin) {
						Element pluginNameElement = root.addElement(pluginDescriptor.getMetadata().getName().replace(' ','-'));
						//((StatefulPlugin)plugin).getState().deepCopy().toXML(pluginNameElement);
						((StatefulPlugin)plugin).getState().toXML(pluginNameElement);

					}
				}
				//save timeline state
				Element pluginNameElement = root.addElement("Timeline-Plugin");
				timeline.getState().toXML(pluginNameElement);
				saveXMLFile(document, root, destinationData);
			}
			else {
				System.out.println("Unhandled event");
			}
		} else if(eventSource == resizeWindow) {
			if (sizePanel == null)
				sizePanel = new ViewerSizePanel(null); // null means this isn't render mode, but rather actual size mode
			int val = JOptionPane.showConfirmDialog(
					Info.getMainGUI(), sizePanel, "Resizve Viewer", JOptionPane.OK_CANCEL_OPTION);
			if (val == JOptionPane.OK_OPTION) {
				Dimension dims = sizePanel.getCurDims();
				Info.getMainGUI().resizeViewer(dims.width, dims.height);
			}
		}
	}

	private void saveXMLFile(Document document,Element root,String destinationData) {
		// TODO Auto-generated method stub
		XMLWriter writer = null;
		try {
			writer = new XMLWriter(
					new FileWriter( destinationData)
					);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			writer.write( document );

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		// Pretty print the document to System.out
		//		OutputFormat format = OutputFormat.createPrettyPrint();
		//		try {
		//			writer = new XMLWriter( System.out, format );
		//		} catch (UnsupportedEncodingException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//		try {
		//			writer.write( document );
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
	}


	//plugin menu and events to load and unload plugins
	public void itemStateChanged(ItemEvent e) {
		// for checkboxitem menu
		Object eventSource = e.getSource();
		if (eventSource == timelineItem) {
			setTimelineVisible(timelineItem.getState());
		} else if (eventSource instanceof CheckboxMenuItem) {
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

	public void setTimelineVisible(boolean visible) {
		// TODO panel in main gui?
		if (timelineFrame == null) {
			timelineFrame = new JFrame();
			timelineFrame.setTitle("Timeline");
			timelineFrame.setContentPane(timelineGUI);
			timelineFrame.setSize(1000, 300);
			// catch window close events to update check box
			timelineFrame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent e) {
					timelineItem.setState(false);
				}

			});
		}
		timelineFrame.setVisible(visible);
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

	public boolean isPluginActive(String id) {
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

	private Menu getMenuByName(String menuName) {
		// Try to find the menu
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			Menu candidate = menuBar.getMenu(i);
			if (candidate != null
					&& candidate.getName().equalsIgnoreCase(menuName)) {
				return candidate;
			}
		}
		return null;
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
			Menu menu = getMenuByName(menuName);

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
				Menu submenu = getCreateSubMenu(menu, submenuName);

				// Add the item to the submenu
				submenu.add(mi);

			}
		}
	}

	private Menu getCreateSubMenu(Menu menu, String submenuName) {
		// Try to find the submenu
		Menu submenu = null;
		for (int i = 0; i < (menu).getItemCount(); i++) {
			MenuItem candidate = menu.getItem(i);
			if (candidate != null && candidate instanceof Menu) {
				if (((Menu) candidate).getName().equalsIgnoreCase(
						submenuName)) {
					return (Menu) candidate;
				}
			}
		}

		// If the submenu was not found, add it
		submenu = new Menu();
		submenu.setLabel(submenuName);
		submenu.setName(submenuName);
		menu.add(submenu);
		
		return submenu;
	}

	public void activatePlugin(String id) {
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
				if(mi!=null)
					mi.setState(true);

				// Activate plugin
				Plugin plugin = loadedPlugins.get(id);
				getActivePlugins().put(id, plugin);

				//System.out.println("**************** Loaded plugins: " + loadedPlugins);

				plugin.activate();
				timeline.addPlugin(plugin, pluginActors.get(plugin));


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
				timeline.removePlugin(loadedPlugins.get(id));

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

		if (loadedPlugins != null && loadedPlugins.containsKey(id)) {
			log.warn("Plugin " + id + " already loaded");
			return;
		}

		if (availablePlugins.containsKey(id)) {
			try {
				PluginInfo info = availablePlugins.get(id);
				log.debug("Loading plugin " + id);
				PluginActors actors = new PluginActors();
				actors.addActorsChangeListener(Info.getMainGUI());
				Plugin plugin = info.newInstance(actors);
				pluginActors.put(plugin, actors);
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

	List<PluginActors> getActivatedPluginActors() {
		ArrayList<PluginActors> actorsList = new ArrayList<>();
		for (Plugin plugin : getActivePlugins().values()) {
			PluginActors actors = pluginActors.get(plugin);
			actorsList.add(actors);
		}
		return actorsList;
	}

}
