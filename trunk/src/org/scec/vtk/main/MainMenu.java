package org.scec.vtk.main;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
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
import java.util.Map.Entry;
import java.util.Vector;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
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
import org.scec.vtk.politicalBoundaries.PoliticalBoundariesGUI;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.timeline.gui.TimelineGUI;
import org.scec.vtk.timeline.gui.ViewerSizePanel;
import org.scec.vtk.main.Help;

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

	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem fileOpen;
	private JMenuItem saveItem;
	private JMenuItem appExit;
	private JMenu windowMenu ;
	private Timeline timeline;
	private TimelineGUI timelineGUI;
	private JFrame timelineFrame;
	private JFrame frame;
	private JCheckBoxMenuItem timelineItem;
	private JMenuItem saveItemVTK;
	private JMenuItem saveItemOBJ;
	private JMenuItem resizeWindow;
	private JMenuItem tutorial;
	private JMenuItem wizardActivation;
	private JMenuItem escapeWindow; 
	private ViewerSizePanel sizePanel;
	private JCheckBoxMenuItem focalPointItem;
	private String currFileName;
	static public Boolean Wizard;
	static public JMenu helpMenu;
	static public Boolean saved = false;
	public JFrame helpFrame;

	Map<String, PluginInfo> availablePlugins = new HashMap<String, PluginInfo>();
	Map<String, Plugin> loadedPlugins = new HashMap<String, Plugin>();
	
	Map<Plugin, PluginActors> pluginActors = new HashMap<>();
	Map<String, Plugin> activePlugins = new HashMap<String, Plugin>(); //is this data structure accurately describing the current plug ins???
	Map<String, JCheckBoxMenuItem> pluginMenuItems;
	private static  Logger log = Logger.getLogger(MainGUI.class);

	public MainMenu(Object object){
		getState();
		currFileName = "";
		//Creates the main menu bar.
		menuBar = new JMenuBar();
		setupFileMenu();
		pluginMenuItems = new HashMap<>();

		
		// manually add Display menu so that it is second from the left
		
		JMenu displayMenu = new JMenu();
		displayMenu.setLabel("Display");
		displayMenu.setName("Display");
		focalPointItem = new JCheckBoxMenuItem("Focal Point", false);
		JMenu trainingMenu = getCreateSubMenu(displayMenu, "Training");
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
		windowMenu = new JMenu();
		windowMenu.setLabel("Window");
		windowMenu.setName("Window");
	
		
		
		resizeWindow = new JMenuItem("Resize render window");
		windowMenu.add(resizeWindow);
		menuBar.add(windowMenu);
		windowMenu.addActionListener(this);
		this.resizeWindow.addActionListener(this);
	}
	public JMenuBar getMenuBar()
	{
		return menuBar;
	}
	
	//Help button on menu bar
	private void helpMenu() {
		helpMenu = new JMenu();
		helpMenu.setLabel("Help");
		helpMenu.setName("Help");
		tutorial = new JMenuItem("User Guide");
		helpMenu.add(tutorial);
		menuBar.add(helpMenu);
		helpMenu.addActionListener(this);
		this.tutorial.addActionListener(this);
		
		wizardActivation = new JMenuItem("Toggle Wizard");
		helpMenu.add(wizardActivation);
		this.wizardActivation.addActionListener(this);
	
	}
	public void setupTimeline(Timeline timeline, TimelineGUI timelineGUI) {
		Preconditions.checkState(this.timeline == null, "Timeline already initialized!");
		this.timeline = timeline;
		this.timelineGUI = timelineGUI;

		JMenu menu = getMenuByName("Render");

		// If the menu was not found, then create it. A plugin could use this menu name which is why we check ahead of time
		if (menu == null) {
			menu = new JMenu();
			menu.setLabel("Render");
			menu.setName("Render");
			menuBar.add(menu);
		}

		timelineItem = new JCheckBoxMenuItem("Timeline");
		timelineItem.setName("Timeline");
		timelineItem.addItemListener(this);

		menu.add(timelineItem);
		
		//Add help menu in menu bar
		helpMenu();
		
	}

	private void setupFileMenu() {
		//File menu - save and open a scene.
		fileMenu = new JMenu("File");
		fileOpen = new JMenuItem("Open...");
		saveItem = new JMenuItem("Save state...");
		saveItemVTK = new JMenuItem("Save as VTK...");
		saveItemOBJ = new JMenuItem("Save as OBJ...");
		appExit = new JMenuItem("Quit");

		fileMenu.addActionListener(this);
		this.saveItem.addActionListener(this);
		this.saveItemVTK.addActionListener(this);
		this.saveItemOBJ.addActionListener(this);
		this.appExit.addActionListener(this);
		this.fileOpen.addActionListener(this);

		this.fileMenu.add(fileOpen);
		this.fileMenu.add(saveItem);
		//this.fileMenu.add(saveItemVTK);
		//this.fileMenu.add(saveItemOBJ);
		this.fileMenu.addSeparator();
		this.fileMenu.add(appExit);

		this.menuBar.add(fileMenu);
	}

	public void quit() {
		System.exit(0);
	}
	//function for Wizard GUI
	public void save(){
		JFileChooser chooser = new JFileChooser();
		int ret = chooser.showSaveDialog(Info.getMainGUI());
		if (ret == JFileChooser.CANCEL_OPTION) {
			Info.getMainGUI().wizFrame.setVisible(true);
			}
		if (ret == JFileChooser.APPROVE_OPTION) {
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("root");
			File file = chooser.getSelectedFile();
			String destinationData =  file.getPath();//Prefs.getLibLoc() + File.separator;
			currFileName = destinationData;
			saved = true;

			Vector<Plugin> pluginDescriptors = new Vector<Plugin>(
					loadedPlugins.values());
			for(Plugin pluginDescriptor:pluginDescriptors)
			{

				Plugin plugin = activePlugins.get(pluginDescriptor.getId());
				if (plugin instanceof StatefulPlugin) {
					Element pluginNameElement = root.addElement(pluginDescriptor.getMetadata().getName().replace(' ','-'));
					((StatefulPlugin)plugin).getState().toXML(pluginNameElement);

				}
			}
			//save timeline state
			Info.getMainGUI().wizFrame.setVisible(false);
			Element pluginNameElement = root.addElement("Timeline-Plugin");
			timeline.getState().toXML(pluginNameElement);
			saveXMLFile(document, root, destinationData);
		}
		else {
			System.out.println("Unhandled event");
		}
	}
	
	/*
	 * saves new file, leaves out wizard functionality
	 */
	
	public void saveForToolbar() {
		JFileChooser chooser = new JFileChooser();
		int ret = chooser.showSaveDialog(Info.getMainGUI());
		if (ret == JFileChooser.APPROVE_OPTION) {
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("root");
			File file = chooser.getSelectedFile();
			String destinationData =  file.getPath();
			currFileName = destinationData;
			saved = true;

			Vector<Plugin> pluginDescriptors = new Vector<Plugin>(
					loadedPlugins.values());
			for(Plugin pluginDescriptor:pluginDescriptors)
			{

				Plugin plugin = activePlugins.get(pluginDescriptor.getId());
				if (plugin instanceof StatefulPlugin) {
					Element pluginNameElement = root.addElement(pluginDescriptor.getMetadata().getName().replace(' ','-'));
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
	}
	
	/*
	 * Checks whether file has been saved
	 */
	public Boolean isSaved() {
		return saved;
	}
	
	/*
	 * Saves without creating a new file
	 */

	public void autoSave(){
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("root");
		Vector<Plugin> pluginDescriptors = new Vector<Plugin>(
				loadedPlugins.values());
		for(Plugin pluginDescriptor:pluginDescriptors)
		{

			Plugin plugin = activePlugins.get(pluginDescriptor.getId());
			if (plugin instanceof StatefulPlugin) {
				Element pluginNameElement = root.addElement(pluginDescriptor.getMetadata().getName().replace(' ','-'));
				((StatefulPlugin)plugin).getState().toXML(pluginNameElement);

			}
		}
		//save timeline state
		Element pluginNameElement = root.addElement("Timeline-Plugin");
		timeline.getState().toXML(pluginNameElement);
		saveXMLFile(document, root, currFileName);
	}
	
	
	public boolean getState(){
		
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read("src/org/scec/vtk/main/SCEC-VDO_STATUS.xml");
			Element root = document.getRootElement();
			for ( Iterator i = root.elementIterator(); i.hasNext(); ) {
				Element status = (Element) i.next();
				if(status.getName().equalsIgnoreCase("Wizard")){
					if(status.getData().toString().equalsIgnoreCase("True")){
						Wizard = true;
						return true;
					}
					else {
						Wizard = false;
						return false;
					}
				}
			}
		}catch( DocumentException e){
			e.printStackTrace();
		}
		return true;
	}
	
	public  void updateWizard(Boolean wiz){
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read("src/org/scec/vtk/main/SCEC-VDO_STATUS.xml");
			Element root = document.getRootElement();
			for ( Iterator i = root.elementIterator(); i.hasNext(); ) {
				Element status = (Element) i.next();
				if(status.getName().equalsIgnoreCase("Wizard")){
					status.detach();
					if(wiz){
						Element wizTru = root.addElement("Wizard");
						wizTru.setText("True");
					}
					else {
						System.out.println("setting wizard to false? ");
						Element wizTru = root.addElement("Wizard");
						wizTru.setText("False");
						System.out.println("root.asXML() in updateWizard: " + root.asXML());

					}

				}
			}
			saveXMLFile(document, root, "src/org/scec/vtk/main/SCEC-VDO_STATUS.xml");	
		}catch( DocumentException e){
			e.printStackTrace();
		}
		
		
	}
	
	//Function for Wizard GUI
	public void open(){
		JFileChooser chooser = new JFileChooser();
		MainGUI.class.getConstructors();
		int ret = chooser.showOpenDialog(Info.getMainGUI());
		if (ret == JFileChooser.CANCEL_OPTION) {
			Info.getMainGUI().wizFrame.setVisible(true);
			}
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			currFileName = file.getPath();
			SAXReader reader = new SAXReader();
			try {
				Document document = reader.read(file.getPath());
				Element root = document.getRootElement();
				System.out.println("document.toString(): " + document.toString());
				saved = true;
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
				Info.getMainGUI().wizFrame.setVisible(false);
				Element pluginNameElement = root.element("Timeline-Plugin");
				timeline.getState().fromXML(pluginNameElement);
				timeline.getState().load();
				timelineGUI.getTimeLinePanel().timelinePluginsChanged();
			} catch (DocumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	/*
	 * opens new file, leaves out wizard functionality
	 */
	public void openForToolbar() {
		JFileChooser chooser = new JFileChooser();
		MainGUI.class.getConstructors();
		int ret = chooser.showOpenDialog(Info.getMainGUI());
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			currFileName = file.getPath();
			SAXReader reader = new SAXReader();
			try {
				Document document = reader.read(file.getPath());
				Element root = document.getRootElement();
				System.out.println("document.toString(): " + document.toString());
				saved = true;
				// iterate through child elements of root
				Vector<PluginInfo> pluginDescriptors = new Vector<PluginInfo>(
						availablePlugins.values());
				for(PluginInfo pluginDescriptor:pluginDescriptors) {
					//System.out.println(pluginDescriptor.getName());
					for (Iterator i = root.elementIterator(pluginDescriptor.getName().replace(' ' ,'-')); i.hasNext(); ) {
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
			System.out.println("Saving norm file");
			JFileChooser chooser = new JFileChooser(); //making a file
			int ret = chooser.showSaveDialog(Info.getMainGUI()); //is this function accurately describing the image that the user sees???
			if (ret == JFileChooser.APPROVE_OPTION) {//once user hits save -- we start to process the data to a file
				
				Document document = DocumentHelper.createDocument();
				Element root = document.addElement("root");
				File file = chooser.getSelectedFile();
				String destinationData =  file.getPath();//Prefs.getLibLoc() + File.separator;

				Vector<Plugin> pluginDescriptors = new Vector<Plugin>(
						loadedPlugins.values());
				
				int stateCntr= 0; //statecounter 
				
				getLoadedPluginsAsMap(); 
				
				for (Entry<Plugin, PluginActors> entry : pluginActors.entrySet())
				{
				    System.out.println(entry.getKey() + "/" + entry.getValue());
				}
				
				for(Plugin pluginDescriptor:pluginDescriptors)
				{

					Plugin plugin = activePlugins.get(pluginDescriptor.getId());
					if (plugin instanceof StatefulPlugin) { //what plug ins are not Stateful plugins??
						
						
						System.out.println("Stateful plug-in #" + stateCntr + ": " + plugin.toString()); //debugging stateful plugins 
						
						stateCntr++;
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
		System.out.println("done saving");
		} else if(eventSource == resizeWindow) {
			if (sizePanel == null)
				sizePanel = new ViewerSizePanel(null); // null means this isn't render mode, but rather actual size mode
			int val = JOptionPane.showConfirmDialog(
					Info.getMainGUI(), sizePanel, "Resize Viewer", JOptionPane.OK_CANCEL_OPTION);
			if (val == JOptionPane.OK_OPTION) {
				Dimension dims = sizePanel.getCurDims();
				Info.getMainGUI().resizeViewer(dims.width, dims.height);
			}
			
		}
		//**********************************************
		//Function for calling the userGuide
		else if(eventSource == tutorial) {
			helpFrame = new JFrame("SCEC VDO User Guide");
			Help helpGUI = new Help();
			JScrollPane sp = new JScrollPane(helpGUI);
		    helpFrame.getContentPane().add(sp);
		    helpFrame.setSize(550, 500);
		    helpFrame.setLocationRelativeTo(null);
		    helpFrame.setVisible(true);
		    helpFrame.setAlwaysOnTop(true);
			}
		else if(eventSource == wizardActivation){
			
			frame = new JFrame ();
			Boolean toggle = getState();
			if (!toggle) {
				Wizard = true;
				updateWizard(true);
				JOptionPane.showMessageDialog(
						frame,  "You set Wizard to display upon launching SCEC-VDO");
			}
			else {
				Wizard = false;
				updateWizard(false);
				JOptionPane.showMessageDialog(
						frame,  "You set Wizard to not display upon launching SCEC-VDO");
			}
		}
		
		else if(eventSource==escapeWindow)
		{
			if(MainGUI.getRenderWindow().getRenderer().GetViewProps().IsItemPresent(PoliticalBoundariesGUI.mainFocusReginActor)!=0) {
				MainGUI.getRenderWindow().getRenderer().GetActiveCamera().SetPosition(MainGUI.camCord[0], MainGUI.camCord[1],MainGUI.camCord[2]);
				MainGUI.getRenderWindow().getRenderer().GetActiveCamera().SetFocalPoint(MainGUI.camCord[3], MainGUI.camCord[4],MainGUI.camCord[5]);
				MainGUI.getRenderWindow().getRenderer().GetActiveCamera().SetViewUp(MainGUI.camCord[6], MainGUI.camCord[7],MainGUI.camCord[8]);
				
				MainGUI.getRenderWindow().getRenderer().ResetCameraClippingRange();
				MainGUI.getRenderWindow().getComponent().repaint();
		}
		
	}
			
		}
	

	private void saveXMLFile(Document document,Element root,String destinationData) {
		// TODO Auto-generated method stub
		
		System.out.println("document.toString() inside saveXMLFile(): " + document.toString());
		XMLWriter writer = null;
		try {
			System.out.println("destinationData: " + destinationData);
			System.out.println("root.asXML() inside saveXMLFile(): " + root.asXML());
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
				
	}


	//plugin menu and events to load and unload plugins
	public void itemStateChanged(ItemEvent e) {
		// for checkboxitem menu
		Object eventSource = e.getSource();
		if (eventSource == timelineItem) {
			setTimelineVisible(timelineItem.getState());
		} else if (eventSource instanceof JCheckBoxMenuItem) {
			JCheckBoxMenuItem jmi = (JCheckBoxMenuItem) eventSource;
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

	public Map<String, Plugin> getActivePlugins() {
		return activePlugins;
	}
 
	public Map<String, Plugin> getLoadedPluginsAsMap() {
		for (Entry<String, Plugin> entry : loadedPlugins.entrySet())
		{
		    System.out.println(entry.getKey() + "/" + entry.getValue());
		}
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

	private JMenu getMenuByName(String menuName) {
		// Try to find the menu
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			JMenu candidate = menuBar.getMenu(i);
			if (candidate != null)
				if(candidate.getName() != null) {
					if(candidate.getName().equalsIgnoreCase(menuName)){
						return candidate;
					}
			}
		}
		return null;
	}

	private void addPluginToMenu(PluginInfo info) {
		// If the plugin has a menu
		if (info.hasMenu()) {

			// Create a menu item
			JCheckBoxMenuItem mi = new JCheckBoxMenuItem(info.getShortName());
			mi.setName(info.getId());
			mi.addItemListener(this);
			// Add to the list
			pluginMenuItems.put(info.getId(), mi);

			final String menuName = info.getMenuName();
			JMenu menu = getMenuByName(menuName);

			// If the menu was not found, then create it
			if (menu == null) {
				menu = new JMenu();
				menu.setLabel(menuName);
				menu.setName(menuName);
				menuBar.add(menu);
			}

			final String submenuName = info.getSubmenuName();
			if (submenuName == null) {

				// If the plugin does not specify a submenu,
				// then add it to the regular menu
				menu.add((JCheckBoxMenuItem)mi);

			} else {
				JMenu submenu = getCreateSubMenu(menu, submenuName);

				// Add the item to the submenu
				submenu.add(mi);

			}
		}
	}

	private JMenu getCreateSubMenu(JMenu menu, String submenuName) {
		// Try to find the submenu
		JMenu submenu = null;
		for (int i = 0; i < (menu).getItemCount(); i++) {
			JMenuItem candidate = menu.getItem(i);
			if (candidate != null && candidate instanceof JMenu) {
				if (((JMenu) candidate).getName().equalsIgnoreCase(
						submenuName)) {
					return (JMenu) candidate;
				}
			}
		}

		// If the submenu was not found, add it
		submenu = new JMenu();
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
				JCheckBoxMenuItem mi = pluginMenuItems.get(id);
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
	public void updateMenu(String id){
		JCheckBoxMenuItem mi = pluginMenuItems.get(id);
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
