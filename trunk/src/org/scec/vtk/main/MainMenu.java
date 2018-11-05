package org.scec.vtk.main;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.politicalBoundaries.PoliticalBoundariesGUI;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.timeline.gui.TimelineGUI;
import org.scec.vtk.timeline.gui.ViewerSizePanel;
import org.scec.vtk.tools.Prefs;
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
import vtk.vtkXMLPolyDataWriter;
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
	private JMenuItem screenShot;
	private JMenuItem publishVTP;
	private JMenuItem resizeWindow;
	private JMenuItem tutorial;
	private JMenuItem wizardActivation;
	private JMenuItem escapeWindow; 
	private ViewerSizePanel sizePanel;
	private JCheckBoxMenuItem focalPointItem;
	private String currFileName;
	static public Boolean Wizard = true; // default value for new users
	static public JMenu helpMenu;
	static public Boolean saved = false;
	public JFrame helpFrame;
	private JFrame wizFrame;
	private MainGUI main;
	
	Map<String, PluginInfo> availablePlugins = new HashMap<String, PluginInfo>();
	Map<String, Plugin> loadedPlugins = new HashMap<String, Plugin>();
	Map<Plugin, PluginActors> pluginActors = new HashMap<>();
	Map<String, Plugin> activePlugins = new HashMap<String, Plugin>(); 
	Map<String, JCheckBoxMenuItem> pluginMenuItems;
	private static  Logger log = Logger.getLogger(MainGUI.class);

	@SuppressWarnings("deprecation")
	public MainMenu(){
		getState();
		currFileName = "";
		menuBar = new JMenuBar(); //Creates the main menu bar.
		setupFileMenu();
		pluginMenuItems = new HashMap<>();
		JMenu displayMenu = new JMenu(); // manually add Display menu so that it is second from the left

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


	@SuppressWarnings("deprecation")
	private void setupWindowMenu() {
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
	/*
	 * setupTimeline(Timeline timeline, TimelineGUI timelineGUI)
	 * 
	 * @param: Timeline timeline: Receives timeline
	 * @param: TimelineGUI timelineGUI: Receives timeline GUI. 
	 */
	
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
		saveItemVTK = new JMenuItem("Export as VTK...");
		saveItemOBJ = new JMenuItem("Export as OBJ...");
		screenShot = new JMenuItem("Save as image");
		publishVTP = new JMenuItem("Publish");
		appExit = new JMenuItem("Quit");

		fileMenu.addActionListener(this);
		this.saveItem.addActionListener(this);
		this.saveItemVTK.addActionListener(this);
		this.saveItemOBJ.addActionListener(this);
		this.screenShot.addActionListener(this);
		this.publishVTP.addActionListener(this);
		this.appExit.addActionListener(this);
		this.fileOpen.addActionListener(this);
		this.fileMenu.add(fileOpen);
		this.fileMenu.add(saveItem);
		this.fileMenu.add(saveItemVTK);
		this.fileMenu.add(saveItemOBJ);
		this.fileMenu.add(screenShot);
		this.fileMenu.addSeparator();
		this.fileMenu.add(publishVTP);
		this.fileMenu.addSeparator();
		this.fileMenu.add(appExit);
		this.menuBar.add(fileMenu);
	}

	public void quit() {
		System.exit(0);
	}
	
	
	//save(): The saving function at the File button.  
	 
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
	
	
	// saveForToolbar(): saves new file, leaves out wizard functionality
	 
	public void saveForToolbar() {
		saveCurrState();
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
		
		if(activePlugins.isEmpty())
			System.out.println("activePlugins is empty");
		
		if(pluginDescriptors.isEmpty())
			System.out.println("pluginDescriptors.isEmpty");
		
		for(Plugin pluginDescriptor:pluginDescriptors)
		{

			Plugin plugin = activePlugins.get(pluginDescriptor.getId());
			
			System.out.println("active plugins in autoSave: " + plugin.getId());
			System.out.println("pluginDescriptor: " + pluginDescriptor.getId());
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
	
	private static File getStatusFile() {
		File libDir = new File(Prefs.getLibLoc());
		Preconditions.checkState(libDir.exists() || libDir.mkdir());
		return new File(libDir, "SCEC-VDO_STATUS.xml");
	}
	
	public boolean getState(){
		File statusFile = getStatusFile();
		try {
			if (!statusFile.exists())
				initStatusFile(statusFile);
			SAXReader reader = new SAXReader();
			Document document = reader.read(statusFile);
			Element root = document.getRootElement();
			Element wizardEl = root.element("Wizard");
			String data = wizardEl.getText().trim();
			if (data.equalsIgnoreCase("true")) {
				Wizard = true;
				return true;
			} else if (data.equalsIgnoreCase("false")) {
				Wizard = false;
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private static OutputFormat format = OutputFormat.createPrettyPrint();
	
	private void initStatusFile(File statusFile) throws IOException {
		System.out.println("Initializing status file: "+statusFile.getAbsolutePath());
		Document doc = DocumentHelper.createDocument();
		
		Element root = doc.addElement("SCECVDO");
		Element wizardEl = root.addElement("Wizard");
		wizardEl.setText(Wizard+"");
		
		writeXML(doc, statusFile);
	}
	
	private static void writeXML(Document document, File file) throws IOException {
		XMLWriter writer = new XMLWriter(new FileWriter(file), format);
		writer.write(document);
		writer.close();
	}
	
	/*
	 * updateWizard(Boolean wiz): 
	 * @param Boolean wiz: Receives the wizard 
	 * 
	 */
	public void updateWizard(Boolean wiz){
		File statusFile = getStatusFile();
		try{
			if (!statusFile.exists())
				initStatusFile(statusFile);
			SAXReader reader = new SAXReader();
			Document document = reader.read(statusFile);
			Element root = document.getRootElement();
			Element wizardEl = root.element("Wizard");
			wizardEl.setText(Wizard+"");
			writeXML(document, statusFile);
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * open(): Opens a project file (File button)
	 */
	public void open(){
		
		JFileChooser chooser = new JFileChooser();
		MainGUI.class.getConstructors();
		int ret = chooser.showOpenDialog(Info.getMainGUI());
		if (ret == JFileChooser.CANCEL_OPTION) {
			Info.getMainGUI().wizFrame.setVisible(true);
			}
		if (ret == JFileChooser.APPROVE_OPTION) {
			if(isSaved())
				autoSave();
			unloadAllPlugins();
			File file = chooser.getSelectedFile();
			currFileName = file.getPath();
			SAXReader reader = new SAXReader();
			try {
				Document document = reader.read(file.getPath());
				Element root = document.getRootElement();
				saved = true;
				// iterate through child elements of root
				Vector<PluginInfo> pluginDescriptors = new Vector<PluginInfo>(
						availablePlugins.values());
				for(PluginInfo pluginDescriptor:pluginDescriptors) {
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
	 * openForToolbar(): l.opens new file, leaves out wizard functionality
	 */
	public void openForToolbar() {
		JFileChooser chooser = new JFileChooser();
		MainGUI.class.getConstructors();
		int ret = chooser.showOpenDialog(Info.getMainGUI());
		if (ret == JFileChooser.APPROVE_OPTION) {
			if(isSaved())
				autoSave();
			unloadAllPlugins();
			File file = chooser.getSelectedFile();
			currFileName = file.getPath();
			SAXReader reader = new SAXReader();
			try {
				Document document = reader.read(file.getPath());
				Element root = document.getRootElement();
				//System.out.println("document.toString(): " + document.toString());
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
	
	public void saveVTPObj(String title)
	{

		vtkJoglPanelComponent renderWindow = Info.getMainGUI().getRenderWindow();

		vtkActorCollection actorlist = renderWindow.getRenderer().GetActors();
		if(actorlist.GetNumberOfItems()>0){
			System.out.println(actorlist.GetNumberOfItems());
			vtkXMLPolyDataWriter objExporter = new vtkXMLPolyDataWriter();
			objExporter.SetFileName(System.getProperty("user.home") + File.separator + ".scec_vdo/tmp/" + title + ".vtp"); 
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
	
	public void savePNG(File file) throws IOException {
		pngRenderer.pngRender(file, Info.getMainGUI());
	}

	//@Override
	public void actionPerformed(ActionEvent e) {
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
			saveCurrState();
		} 
		
		else if(eventSource == resizeWindow) {
			if (sizePanel == null)
				sizePanel = new ViewerSizePanel(null); // null means this isn't render mode, but rather actual size mode
			int val = JOptionPane.showConfirmDialog(
					Info.getMainGUI(), sizePanel, "Resize Viewer", JOptionPane.OK_CANCEL_OPTION);
			if (val == JOptionPane.OK_OPTION) {
				Dimension dims = sizePanel.getCurDims();
				Info.getMainGUI().resizeViewer(dims.width, dims.height);
			}
			
		}
		
		else if (eventSource == publishVTP) //provides input window for publishing information, writes xml and vtp files and sends them to server, then displays link to project
		{
			UIManager.put("OptionPane.minimumSize",new Dimension(500,300)); 
			JTextField title = new JTextField();
			title.setBorder(BorderFactory.createLineBorder(Color.black, 1));

			JTextField author = new JTextField();
			author.setBorder(BorderFactory.createLineBorder(Color.black, 1));

			JTextField server = new JTextField("http://scecvdo.usc.edu/viewer/publish.php");
			server.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			server.setEnabled(false);

			JTextArea description = new JTextArea(20, 20);
			description.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			description.setFont(title.getFont());
			

			Object[] message = {
			    "Title:", title,
			    "Author:", author,
			    "Server:", server,		
			//	"Username:", username,
			//	"Password:", password,
			    "Description", description
			};

			int val = JOptionPane.showConfirmDialog(null, message, "Publish To Web Server", JOptionPane.OK_CANCEL_OPTION);
			
			if(val != JOptionPane.OK_OPTION) //publishing has been canceled
			{
				return;
			}
			
			try 
			{
				String[] arr = title.getText().split("[~#@*+%{}<>\\[\\]|\"\\_^]", 2); //check if title has invalid characters (will be used in URL),
			    if(arr.length > 1)
			    {
			    	JLabel words = new JLabel("Invalid characters in Title");
					Object[] confirmation = {words};
					JOptionPane.showMessageDialog(null, confirmation, "Error", JOptionPane.ERROR_MESSAGE);
					return;
			    }
				
				
				//set up the XML document with title, author, date, description info
				 org.jdom.Element rootElement = new org.jdom.Element("model");
				 org.jdom.Document doc = new org.jdom.Document(rootElement);
				 
				 org.jdom.Element xmlTitle = new org.jdom.Element("title");
				 xmlTitle.setText(title.getText());
				 
				 org.jdom.Element xmlAuthor = new org.jdom.Element("author");
				 xmlAuthor.setText(author.getText());
				 
				 DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				 Date date = new Date();
				//System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
				 org.jdom.Element xmlDate = new org.jdom.Element("date");
				 xmlDate.setText(dateFormat.format(date));
				 
				 org.jdom.Element xmlRegion = new org.jdom.Element("region");
				 xmlRegion.setText("California");
				 
				 org.jdom.Element xmlDescription = new org.jdom.Element("description");
				 xmlDescription.setText(description.getText());
				 
				 //add elements to xml
				 doc.getRootElement().addContent(xmlTitle);
				 doc.getRootElement().addContent(xmlAuthor);
				 doc.getRootElement().addContent(xmlDate);
				 doc.getRootElement().addContent(xmlRegion);
				 doc.getRootElement().addContent(xmlDescription);
				 
				 org.jdom.output.XMLOutputter outter = new XMLOutputter();
				 outter.setFormat(Format.getPrettyFormat());
				 
				 //write XML file to tmp folder
				 //File publishXml = new File(System.getProperty("user.home") + File.separator + ".scec_vdo/tmp/publish.xml");

				 final String projectTitle = title.getText().replaceAll("\\s",""); //get rid of spaces in title for URL
				 File publishXml = new File(System.getProperty("user.home") + File.separator + ".scec_vdo/tmp/" + projectTitle + ".xml");

				 if(!publishXml.exists()) //if tmp directory doesn't exist, create it
				 {
					 Files.createDirectories(Paths.get(System.getProperty("user.home") + File.separator + ".scec_vdo/tmp/"));
				 }
				 
				 saveVTPObj(projectTitle); //save vtp file to tmp folder
				 
				 Writer out = new FileWriter(publishXml);
				 outter.output(doc, out);
				 
				 
				 UIManager.put("OptionPane.minimumSize",new Dimension(100,100)); 
				 JLabel words = new JLabel("Project successfully published online!");
				 Object[] confirmation = {words};
				 
				 //if both files are successfully transferred
				 if(transferFile(new File(System.getProperty("user.home") + File.separator + ".scec_vdo" + File.separator + "tmp" + File.separator + projectTitle + ".vtp"), server.getText())
				  && transferFile(new File(System.getProperty("user.home") + File.separator + ".scec_vdo" + File.separator + "tmp" + File.separator + projectTitle + ".xml"), server.getText()))
				 {
					 	//create clickable hyperlink in window if publishing is successful that directs user to link of their project
					    JLabel label = new JLabel();
					    label.setFont(label.getFont().deriveFont(16.0f));
					    Font font = label.getFont();

					
					    //create css from the label's font
					    StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
					    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
					    style.append("font-size:" + font.getSize() + "pt;");
					    
					    //html content
					    JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" //
					            + "Project successfully published online! \n<a href=\"http://google.com/\">View Project Here</a>" //
					            + "</body></html>");
					
					    //handle link events
					    ep.addHyperlinkListener(new HyperlinkListener()
					    {
					        @Override
					        public void hyperlinkUpdate(HyperlinkEvent e)
					        {
					            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
					            {
					            	String urlString = "http://scecvdo.usc.edu/viewer/?fileURL=objects/" + projectTitle + ".vtp&model=" + projectTitle;
					                try {
					                    Desktop.getDesktop().browse(new URL(urlString).toURI());
					                } catch (Exception er) {
					                    er.printStackTrace();
					                }
					            }
					        }
					    });
					    ep.setEditable(false);
					    ep.setBackground(label.getBackground());
					
					    // show
					    JOptionPane.showMessageDialog(null, ep, "", JOptionPane.INFORMATION_MESSAGE);
				 }
				 else //if there are errors in file transfer
				 {
					 words.setText("Error publishing project");
					 JOptionPane.showMessageDialog(null, confirmation, "Error", JOptionPane.ERROR_MESSAGE);
				 }
					
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else if(eventSource == screenShot)
		{
			JFileChooser chooser = new JFileChooser();
			int ret = chooser.showSaveDialog(Info.getMainGUI());
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				try{
					String fName = file.getAbsolutePath();

					if(!fName.endsWith(".png") ) {
						file = new File(fName + ".png");
					}
					savePNG(file);
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
					System.out.println("Error");
				}
			}
		}


		/*
		 *Function for calling the userGuide 
		 */
		
		else if(eventSource == tutorial) {
			String url = "http://scecvdo.usc.edu/manual/UserGuide.html";
				String os = System.getProperty("os.name").toLowerCase();
			    Runtime rt = Runtime.getRuntime();
				try{
				    if (os.indexOf( "win" ) >= 0) {
				        rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);
				    }
				    else if (os.indexOf( "mac" ) >= 0) {

				        rt.exec( "open " + url);
			            }
				    else if (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0) {
				        String[] browsers = {"firefox", "mozilla", "epiphany", "konqueror",
				       			             "netscape","opera","links","seamonkey", "galeon", "kazehakase","lynx"};
				        StringBuffer cmd = new StringBuffer();
				        for (int i=0; i<browsers.length; i++)
				            cmd.append( (i==0  ? "" : " || " ) + browsers[i] +" \"" + url + "\" ");
				        rt.exec(new String[] { "sh", "-c", cmd.toString() });
				        } 
			            else {
			                return;
			           }
			       }
				catch (Exception e1){
				    return;
			       }
			      return;
			}
		else if(eventSource == wizardActivation){
			
			frame = new JFrame ();
			/*
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
			*/
			JOptionPane.showMessageDialog(frame,  "You set Wizard to display upon launching SCEC-VDO");
			//Wizard GUI to run with main

			wizFrame = new JFrame();
			Wizard wizGui = new Wizard(this,main);
			wizFrame.getContentPane().add(wizGui);
			wizFrame.setSize(550, 140);
			wizFrame.setLocationRelativeTo(null);
			wizFrame.setVisible(true);
			
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
	
	
	private boolean transferFile(File f, String urlText)
	{
		 final String CrLf = "\r\n";

	        URLConnection conn = null;
	        OutputStream os = null;
	        InputStream is = null;

	        try {
	            URL url = new URL(urlText);
	            conn = url.openConnection();
	            conn.setDoOutput(true);
	            
	            InputStream imgIs = new FileInputStream(f.getAbsolutePath());
	            		//MainMenu.class.getClassLoader().getResourceAsStream("tester.txt");
	            
	            //System.out.println("size: " + imgIs.available());
	              
	            byte[] imgData = new byte[imgIs.available()];
	            
	            imgIs.read(imgData);

	            String message1 = "";
	            message1 += "-----------------------------4664151417711" + CrLf;
	            message1 += "Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + f.getName() + "\""
	                    + CrLf;
	            message1 += "Content-Type: text/plain" + CrLf;
	            message1 += CrLf;

	            // the image is sent between the messages in the multipart message.

	            String message2 = "";
	            message2 += CrLf + "-----------------------------4664151417711--"
	                    + CrLf;

	            conn.setRequestProperty("Content-Type",
	                    "multipart/form-data; boundary=---------------------------4664151417711");
	            // might not need to specify the content-length when sending chunked
	            // data.
	            conn.setRequestProperty("Content-Length", String.valueOf((message1
	                    .length() + message2.length() + imgData.length)));

	            //System.out.println("open os");
	            os = conn.getOutputStream();

	           // System.out.println(message1);
	            os.write(message1.getBytes());

	            // SEND THE File
	            int index = 0;
	            int size = 1024;
	            do {
	                //System.out.println("write:" + index);
	                if ((index + size) > imgData.length) {
	                    size = imgData.length - index;
	                }
	                os.write(imgData, index, size);
	                index += size;
	            } while (index < imgData.length);	            
	            
	            System.out.println("written:" + index);

	           // System.out.println(message2);
	            os.write(message2.getBytes());
	            os.flush();

	           // System.out.println("open input stream");
	            is = conn.getInputStream();

	            char buff = 512;
	            int len = 0;
	            byte[] data = new byte[buff];
	            
	            do {
	               // System.out.println("READ");
	                len = is.read(data);
	              //  System.out.println("len: " + len);

	                if (len > 0) {
	                    System.out.println(new String(data, 0, len));
	                }
	            } while (len > 0);

	            System.out.println("DONE");
	        } catch (Exception ef) {
	            ef.printStackTrace();
	            return false;
	        } finally {
	            System.out.println("Close connection");
	            try {
	                os.close();
	            } catch (Exception ef) {
	            }
	            try {
	                is.close();
	            } catch (Exception ef) {
	            }
	            try {

	            } catch (Exception ef) {
	            }
	        }
	        
	        return true;
	        
	}

	private void saveXMLFile(Document document, Element root, String destinationData) {
		
//		System.out.println("document.toString() inside saveXMLFile(): " + document.toString());
		XMLWriter writer = null;
		try {
//			System.out.println("destinationData: " + destinationData);
//			System.out.println("root.asXML() inside saveXMLFile(): " + root.asXML());
			OutputFormat format = OutputFormat.createPrettyPrint();
			writer = new XMLWriter(new FileWriter( destinationData), format);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			writer.write( document );

			writer.close();
		} catch (IOException e) {
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
				// this is a dirty kludgey way to do this, bad interns
				// TODO do it right with some sort of configuration file
				if (candidate.getName().equalsIgnoreCase("GIS Hazus Events")
					//	|| candidate.getName().equalsIgnoreCase("ShakeMap")
//						|| candidate.getName().equals("Training")
						) {
					candidate.setVisible(false);;
				}
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
	
	private void unloadAllPlugins(){
		ArrayList<String> plugIds = new ArrayList<String>();
		
		for(Plugin plug: loadedPlugins.values()){
			if( (!plug.getId().equals("org.scec.vdo.graticulePlugin")) && (!plug.getId().equals("org.scec.vdo.politicalBoundaries"))
					&& (!plug.getId().equals("org.scec.vdo.drawingToolsPlugin")) ){
				plugIds.add(plug.getId());
			}	
		}
		
		for(int i = 0; i < plugIds.size(); i ++ ){
			Info.getMainGUI().timeline.removePlugin(loadedPlugins.get(plugIds));
			String tabName = Info.getMainGUI().tabMap.get(plugIds.get(i));
			System.out.println("tabName: " + tabName);
			int tabIndex = Info.getMainGUI().pluginTabPane.indexOfTab(tabName);
			System.out.println("tabIndex: "+tabIndex);
			System.out.println("pluginTabPane.getTabCount(): "+ Info.getMainGUI().pluginTabPane.getTabCount());
			
			loadedPlugins.remove(plugIds);
			if( tabIndex != -1)
				Info.getMainGUI().pluginTabPane.remove(tabIndex);
			
			updateMenu(plugIds.get(i));
		}
		
	}
	
	public void updateMenu(String id){
		JCheckBoxMenuItem mi = pluginMenuItems.get(id);
		mi.setState(false);

		// Passivate plugin
		Plugin plugin = loadedPlugins.get(id);
		if(plugin != null){
			plugin.unload();
			plugin.passivate();
		}
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
	
	public void saveCurrState()
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
				try {
					if (plugin instanceof StatefulPlugin) { //what plug ins are not Stateful plugins??
						System.out.println("Stateful plug-in #" + stateCntr + ": " + plugin.toString()); //debugging stateful plugins 
						stateCntr++;
						Element pluginNameElement = root.addElement(pluginDescriptor.getMetadata().getName().replace(' ','-'));
						//((StatefulPlugin)plugin).getState().deepCopy().toXML(pluginNameElement);
						((StatefulPlugin)plugin).getState().toXML(pluginNameElement);
					}
				}
				catch(Exception e2)
				{
					System.out.println("ERROR: " + plugin.toString());
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
	}
}
