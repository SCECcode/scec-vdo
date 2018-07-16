package org.scec.vtk.plugins.ShakeMapPlugin;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opensha.commons.util.cpt.CPT;
import org.scec.vtk.commons.legend.LegendItem;
import org.scec.vtk.commons.legend.LegendUtils;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.ShakeMapPlugin.Component.ShakeMap;
import org.scec.vtk.plugins.SurfacePlugin.SurfaceTableModel;
import org.scec.vtk.tools.Prefs;

import com.lowagie.text.Font;

import vtk.vtkActor;
import vtk.vtkActor2D;


/*
 * This class reads the .xyz files from USGS.
 * Whenever you donwload a new .xyz file
 * 	1. Remove the header
 * 	2. Convert it to a .txt file
 * 	3. Move it to the data/ShakeMapPlugin directory
 * The files in the data/ShakeMapPlugin directory will automatically 
 * be loaded when the ShakeMap plugin is opened in the program.
 */
public class ShakeMapGUI extends JPanel implements ItemListener, ChangeListener, ListSelectionListener, TableModelListener{

	private static final long serialVersionUID = 1L;
	
	static final String dataPath = Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin"; //path to directory with local folders
	static final String moreMaps = "More_USGS_Maps"; //the extra maps that the user may download
	static final String openSHAFile = "openSHA.txt";
	
	//custom shakemaps which apparently can be uploaded to this link (from what is heard, i don't really know)
	static final String openSHAMapURL = "http://zero.usc.edu/gmtData/1468263306257/map_data.txt"; 

	private JPanel shakeMapLibraryPanel = new JPanel();
	JPanel panel1 = new JPanel();
	private JPanel loadFilePanel = new JPanel();
	private JTabbedPane tabbedPane = new JTabbedPane();
	JPanel bottomPane = new JPanel();
	
	//Though they're separate lists, the values rely on each other.
	//Make sure the indexes match up for each shake map and actor
	private ArrayList<JCheckBox> checkBoxList; //for the local files in ShakeMapPlugin directory
	private ArrayList<ShakeMap> shakeMapsList; //the shake map corresponding with the check box
	private PluginActors pluginActors;         
	private ArrayList<vtkActor> actorList;     //actor corresponding with the shake map
	
	//browse and load file button
	JButton browse = new JButton("Load File");
	
	//for the usgs download option
	
	JTextField eventIdBox = new JTextField("Enter Shakemap XML Link");
	JButton downloadUSGSButton = new JButton("Download USGS Shake Map");
	
	//Table and transparency for preset shakemaps
	String[] header = {"Name", "List Index"};
	public SurfaceTableModel surfaceTableModel = new SurfaceTableModel(header);
	public JTable surfaceTable = new JTable(surfaceTableModel);
	private JSlider transparencySlider = new JSlider(); 
	
	//Different tabs
	JPanel tab2 = new JPanel();
	
	private JCheckBox legendCheckBox;
	private LegendItem legend;
	private Plugin plugin;
	
	public ShakeMapGUI(Plugin plugin, PluginActors pluginActors) {
		this.plugin = plugin;
		
		//First check if More_USGS_Maps directory exists...
		//Otherwise, make that directory
		File f = new File(dataPath+"/"+moreMaps);
		if(!(f.exists()))
			f.mkdirs();	

		checkBoxList = new ArrayList<JCheckBox>();
		this.pluginActors = pluginActors;
		shakeMapsList = new ArrayList<ShakeMap>();
		actorList = new ArrayList<>();
		
		//Make checkboxes of all the presets
		JPanel presets = new JPanel();
		presets.setLayout(new GridLayout(0,2)); //2 per row
		//Initialize all the preset files in the data/ShakeMapPlugin directory
		File dataDirectory = new File(dataPath);
		if (dataDirectory.isDirectory()) {
			// List files in the directory and process each
			File files[] = dataDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					String tempName = files[i].getName();
					JCheckBox tempCheckbox = new JCheckBox(tempName);
					tempCheckbox.setName(files[i].getPath()); //set it to the ENTIRE file path, not just the file name
					tempCheckbox.addItemListener(this);
					presets.add(tempCheckbox);
					checkBoxList.add(tempCheckbox); //add the JCheckBox to the list
					shakeMapsList.add(null); //for now, initialize to null
					actorList.add(null);
				}
			}
		}
		
		surfaceTable.setPreferredScrollableViewportSize(new Dimension(350, 70));
		surfaceTable.getSelectionModel().addListSelectionListener(this);
		surfaceTableModel.addTableModelListener(this);
		
		JPanel panesPanel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(surfaceTable);
		panesPanel.setLayout(new GridLayout(1,2,10,10));
		panesPanel.add(scrollPane);
		
		panel1.setLayout(new GridLayout(1,0,0,15));
		panel1.add(new JScrollPane(presets));
		
		transparencySlider.setMajorTickSpacing(10);
		transparencySlider.setMinorTickSpacing(5);
		transparencySlider.setPaintLabels(true); 
		transparencySlider.setPaintTicks(true);
		transparencySlider.addChangeListener(this);
		transparencySlider.setEnabled(false);
		JPanel sliderPanel = new JPanel(new BorderLayout());
		sliderPanel.add(new JLabel("Transparency"), BorderLayout.NORTH);
		sliderPanel.add(transparencySlider, BorderLayout.CENTER);
		
		legendCheckBox = new JCheckBox("Add Legend");
		legendCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				JCheckBox cb = (JCheckBox)e.getSource();
				if (cb.isSelected()) {
					addLegendScalarBar();
				} else {
					removeLegend();
				}
			}
		});
		legendCheckBox.setEnabled(false);
		sliderPanel.add(legendCheckBox, BorderLayout.SOUTH);
		
		bottomPane.setLayout(new GridLayout(2,0,0,15));
		bottomPane.add(panesPanel);
		bottomPane.add(sliderPanel);
		
		browse.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				loadShakeMapFile();
			}
		});
		
		//Checkboxes for the USGS Table
		loadFilePanel.setLayout(new GridLayout(0,2)); //2 per row
		//Initialize all the usgs files in the data/ShakeMapPlugin/More_USGS_Maps directory
		File usgsDirectory = new File(dataPath + "/" + moreMaps);
		if (usgsDirectory.isDirectory()) {
			// List files in the directory and process each
			File files[] = usgsDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
//					System.out.println(files[i].getPath());
//					System.out.println(files[i].getName());
					String tempName = files[i].getName();
					JCheckBox tempCheckbox = new JCheckBox(tempName);
					tempCheckbox.setName(files[i].getPath()); //set it to the ENTIRE file path, not just the file name
					tempCheckbox.addItemListener(this);
					loadFilePanel.add(tempCheckbox);
					checkBoxList.add(tempCheckbox); //add the JCheckBox to the list
					shakeMapsList.add(null); //for now, initialize to null
					actorList.add(null);
				}
			}
		}
		
//		JScrollPane loadFileScroll = new JScrollPane(loadFilePanel);
		
		JPanel USGSPanel = new JPanel();
		USGSPanel.setLayout(new FlowLayout());
		//calChooser.add(nc);
		//calChooser.add(sc);
		JButton usgsLink = new JButton("Open USGS Website");
		final JTextField usgsURL = new JTextField("http://earthquake.usgs.gov/data/shakemap/");

		usgsLink.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					openWebpage(new URL(usgsURL.getText()));
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		USGSPanel.add(new JLabel("Visit website, locate shakemap, and enter URL of Shakemap's XML file"));
		
		//USGSPanel.add(nc);
		//USGSPanel.add(sc);
		eventIdBox.setPreferredSize(new Dimension(200,40));
		USGSPanel.add(eventIdBox);
		USGSPanel.add(downloadUSGSButton);
		USGSPanel.add(usgsURL);
		USGSPanel.add(usgsLink);
		
		eventIdBox.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				eventIdBox.setText("");
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				if(eventIdBox.getText() == "" || eventIdBox.getText() == " ")
				{
					eventIdBox.setText("Enter XML Link");
				}
			}
		});
		
		downloadUSGSButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String id = eventIdBox.getText();
				if(id.length() > 0)
				{
					USGSShakeMapDownloader smd = new USGSShakeMapDownloader(id);
					String d = smd.downloadShakeMap(id+".txt");
					if(d.length() <= 0)
					{
						System.out.println("Failure");
						JOptionPane.showMessageDialog(shakeMapLibraryPanel,
								    "File not found on USGS site.");
					}
					else
					{
						System.out.println("Loaded!");
						System.out.println("showing new map: " + dataPath + "/" + moreMaps + "/" + d +".txt");
						showNewMap(dataPath + "/" + moreMaps + "/" + d+".txt", "mmi");
						addNewCheckBox(d+".txt", dataPath + "/" + moreMaps + "/" + d+".txt");
					}
				}
				else
				{
					System.out.println("Enter an earthquake ID!");
					JOptionPane.showMessageDialog(shakeMapLibraryPanel,
							"Enter link to Shakemap XML");
				}
			}			
		});
		
		
		JButton openSHAButton = new JButton("Download OpenSHA File");
		openSHAButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					URL openSHA = new URL(openSHAMapURL);
					Files.copy(openSHA.openStream(), Paths.get(dataPath+"/openSHA.txt"), StandardCopyOption.REPLACE_EXISTING);
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					JOptionPane.showMessageDialog(shakeMapLibraryPanel,
						    "There is no file on OpenSHA right now.");
				}
			}		
		});
		
		tabbedPane.setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()/2));
		tabbedPane.addTab("Presets", panel1);
//		JPanel tab2 = new JPanel(); //it's global now
		tab2.setLayout(new BorderLayout());	
		tab2.add(loadFilePanel, BorderLayout.NORTH);
		tab2.add(browse, BorderLayout.SOUTH);
		tabbedPane.addTab("Load Map", tab2);
		tabbedPane.addTab("Download USGS Map", USGSPanel);
		JPanel shaPanel = new JPanel(new FlowLayout());
		shaPanel.add(openSHAButton);
//		tabbedPane.addTab("OpenSHA", shaPanel);
		
		shakeMapLibraryPanel.setLayout(new BorderLayout());
		shakeMapLibraryPanel.add(tabbedPane, BorderLayout.NORTH);
		shakeMapLibraryPanel.add(bottomPane, BorderLayout.SOUTH);
		this.add(shakeMapLibraryPanel);
	}



	public JPanel getPanel() {
		// TODO Auto-generated method stub
		return this;
	}
	
	
	public void addMap(String name, String filepath, String scaleChoice){
		showNewMap(filepath, scaleChoice);
		addNewCheckBox(name, filepath);
	}

	/*
	 * This immediately shows the map after loading it
	 */
	private void showNewMap(String path, String scaleChoice){
		String colorFile = "";
		System.out.println("file path: " + path);
		if(scaleChoice.equals("pga")){
			colorFile = "colors_pga.cpt";
		}else if(scaleChoice.equals("pgv")){
			colorFile = "colors_pgv.cpt";
		}else if(scaleChoice.equals("mmi")){
			colorFile = "colors.cpt"; //mmi format by default
		}
		ShakeMap shakeMap = new ShakeMap(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Extra/" + colorFile, scaleChoice);
		try{ //try loading a usgs-format shakemap
			shakeMap.loadFromFileToGriddedGeoDataSet(path); 
		}catch(Exception e){ //otherwise, try loading  the opensha-form shakemap
			shakeMap.loadOpenSHAFileToGriddedGeoDataSet(path); 
		}
		shakeMap.setActor(shakeMap.builtPolygonSurface());
		shakeMap.getActor().GetProperty().SetOpacity(0.5);
		actorList.add(shakeMap.getActor());
		pluginActors.addActor(shakeMap.getActor());
		shakeMapsList.add(shakeMap);
//		parameterList.add(scaleChoice);
		Info.getMainGUI().updateRenderWindow();
	}
	
	//appends a CheckBox after loading a new shake map
	private void addNewCheckBox(String checkBoxName, String filepath){
		JCheckBox tempCheckbox = new JCheckBox(checkBoxName);
		tempCheckbox.setName(filepath);
		tempCheckbox.setSelected(true);
		tempCheckbox.addItemListener(this);
		loadFilePanel.add(tempCheckbox); //add it to GUI
		checkBoxList.add(tempCheckbox); //add it to data
		surfaceTableModel.addRow(new Object[]{checkBoxName, checkBoxList.size()-1});
		tab2.revalidate(); //make gui display new checkbox
	}
	
	/*
	 * Lets the user load a shakemap file that
	 * is saved on their computer
	 */
	private void loadShakeMapFile(){
		JFileChooser chooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter(
	        "Text files", "txt", "xyz");
	    chooser.setFileFilter(filter);
//	    chooser.setCurrentDirectory(new File(dataPath+"/"+moreMaps));
	    int returnVal = chooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	try{
	    		//add new JOption Pane for user to choose mmi, pga, and pgv
	    		String[] choices = { "mmi", "pga", "pgv" };
	    	    String scaleChoice = (String) JOptionPane.showInputDialog(shakeMapLibraryPanel, "Choose a scale",
	    	        "Choose a scale", JOptionPane.QUESTION_MESSAGE, null, // Use
	    	                                                                        // default
	    	                                                                        // icon
	    	        choices, // Array of choices
	    	        choices[1]); // Initial choice
	    	    if(scaleChoice==null)
	    	    	scaleChoice = "mmi";	    
	    	    addMap(chooser.getSelectedFile().getName(), chooser.getSelectedFile().toString(), scaleChoice);
	    	}catch(NumberFormatException e){
	    		e.printStackTrace();
	    		JOptionPane.showMessageDialog(shakeMapLibraryPanel,
					    "This is an invalid shakeMap file.");
	    	}catch(IllegalArgumentException e){
	    		e.printStackTrace();
	    		JOptionPane.showMessageDialog(shakeMapLibraryPanel,
					    "This is an invalid shakeMap file.");
	    	}
	    }
	}
	
	private void addLegendScalarBar() {
		ListSelectionModel model = surfaceTable.getSelectionModel();
		int idx = (int) surfaceTableModel.getValueAt(model.getMinSelectionIndex(), 1);
		if (idx != -1)
		{
			ShakeMap shakeMap = shakeMapsList.get(idx);		
		
			PluginActors actors = plugin.getPluginActors();
			vtkActor2D prevActor = null;
			double x = 0.05;
			double y = 0.05;
			if (legend != null) {
				// duplicate - first remove old one but keep same position/size
				prevActor = legend.getActor();
				double[] position = prevActor.GetPosition();
				x = position[0];
				y = position[1];
				actors.removeLegend(legend);
			}
			CPT cpt = shakeMap.getCPT();
			legend = LegendUtils.buildColorBarLegend(plugin, cpt, "ShakeMap Scale (MMI)", x, y);
			if (prevActor != null) {
				// set size
				vtkActor2D newActor = legend.getActor();
				newActor.SetWidth(prevActor.GetWidth());
				newActor.SetHeight(prevActor.GetHeight());
				// set color
				newActor.GetProperty().SetColor(prevActor.GetProperty().GetColor());
			}
			actors.addLegend(legend);
			MainGUI.updateRenderWindow();
		}
	}
	
	private void removeLegend() {
		if (legend != null) {
			plugin.getPluginActors().removeLegend(legend);
			MainGUI.updateRenderWindow();
		}
	}
	
	//What happens whenever a check box is selected
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		Object src = e.getSource();
		for(int i=0; i<checkBoxList.size(); i++){
			if(src == checkBoxList.get(i)){
				File f = new File(checkBoxList.get(i).getName());
				if (e.getStateChange()==ItemEvent.SELECTED) {
					
					if(shakeMapsList.get(i) == null){ //if it has never been selected before, load the file
						ShakeMap shakeMap = new ShakeMap(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Extra/colors.cpt", "mmi"); //load mmi by default
						if(f.getName().equals("openSHA.txt")){
							//The file format for data from openSHA files are a little different.
							//Open declaration of method for more details
							shakeMap.loadOpenSHAFileToGriddedGeoDataSet(f.getPath());
						}else{
							shakeMap.loadFromFileToGriddedGeoDataSet(f.getPath());			
						}
						shakeMap.setActor(shakeMap.builtPolygonSurface());
						actorList.set(i, shakeMap.getActor());
						pluginActors.addActor(shakeMap.getActor());
						shakeMapsList.set(i, shakeMap);
						shakeMapsList.get(i).getActor().GetProperty().SetOpacity(0.5);
					}else{
						shakeMapsList.get(i).getActor().SetVisibility(1);
					}
					//add to table
					surfaceTableModel.addRow(new Object[]{f.getName(), i});
				}else{ //checkbox is unselected
					shakeMapsList.get(i).getActor().SetVisibility(0);
					for(int j=0; j<surfaceTableModel.getRowCount(); j++){
						if(f.getName().equals(surfaceTableModel.getValueAt(j, 0))){
							System.out.println("list index is " + surfaceTableModel.getValueAt(j, 1));
							surfaceTableModel.removeRow(j);
						}
					}
				}
				Info.getMainGUI().updateRenderWindow();
			}
		}
	}

	//Removes all the maps from the GUI
	public void unloadPlugin()
	{

		for(vtkActor actor:actorList)
		{
			pluginActors.removeActor(actor);
		}
		Info.getMainGUI().updateRenderWindow();
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
		
	}


	//Checks if a value in the table is being modified
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		Object src = e.getSource();
		this.surfaceTable.getModel();
		if (e.getValueIsAdjusting()) return;
		
		if (src == this.surfaceTable.getSelectionModel()) {
			processTableSelectionChange();
		}
	}

	//Enables the transparency slider if a map is selected in
	//the table. Otherwise, the slider is disabled
	public void processTableSelectionChange() {
		int[] selectedRows = this.surfaceTable.getSelectedRows();
		if (selectedRows.length > 0) {
			this.transparencySlider.setEnabled(true);
			this.legendCheckBox.setEnabled(true);
			
			/*
			double shakeMapOpacity = shakeMapsList.get(selectedRows[0]).getActor().GetProperty().GetOpacity() * 100; //set transparency slider value to the shakemap's current opacity
			this.transparencySlider.setValue((int) shakeMapOpacity);
			*/
		} else {
			this.transparencySlider.setEnabled(false);
			this.legendCheckBox.setEnabled(false);
		}
	}

	//Whenever the transparency is changed
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		Object src = e.getSource();
		if(src == transparencySlider)
		{
			double transparency = ((double)(transparencySlider.getValue())/100.0);
			ListSelectionModel model = surfaceTable.getSelectionModel();
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
				int idx = (int) surfaceTableModel.getValueAt(i, 1);
				shakeMapsList.get(idx).getActor().GetProperty().SetOpacity(transparency);
			}
			Info.getMainGUI().updateRenderWindow();
		}
	}

	//Opens the uri in the user's default browser
	public static void openWebpage(URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

	public static void openWebpage(URL url) {
	    try {
	        openWebpage(url.toURI());
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	    }
	}

	//GETTERS and SETTERS
	public ArrayList<JCheckBox> getCheckBoxList() {
		return checkBoxList;
	}

	public void setCheckBoxList(ArrayList<JCheckBox> checkBoxList) {
		this.checkBoxList = checkBoxList;
	}

	public ArrayList<ShakeMap> getShakeMapsList() {
		return shakeMapsList;
	}

	public void setShakeMapsList(ArrayList<ShakeMap> shakeMapsList) {
		this.shakeMapsList = shakeMapsList;
	}

	public PluginActors getPluginActors() {
		return pluginActors;
	}

	public void setPluginActors(PluginActors pluginActors) {
		this.pluginActors = pluginActors;
	}

	public ArrayList<vtkActor> getActorList() {
		return actorList;
	}

	public void setActorList(ArrayList<vtkActor> actorList) {
		this.actorList = actorList;
	}

}
	