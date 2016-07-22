package org.scec.vtk.plugins.ShakeMapPlugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.ShakeMapPlugin.Component.ShakeMap;
import org.scec.vtk.plugins.SurfacePlugin.SurfaceTableModel;
import org.scec.vtk.tools.Prefs;

import vtk.vtkActor;


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
	static final String openSHAMapURL = "http://zero.usc.edu/gmtData/1468263306257/map_data.txt"; //custom shakemaps which may be uploaded to this link

	private JPanel shakeMapLibraryPanel = new JPanel();
	JPanel panel1 = new JPanel();
	private JPanel panesPanel = new JPanel();
	private JPanel usgsDownloads = new JPanel();
	private JTabbedPane tabbedPane = new JTabbedPane();
	JPanel bottomPane = new JPanel();
	
	private ArrayList<JCheckBox> checkBoxList; //for the local files in ShakeMapPlugin directory
	private ArrayList<ShakeMap> shakeMapsList;
	private PluginActors pluginActors;
	private ArrayList<vtkActor> actorList;
	
	//for the usgs download option
	private ButtonGroup calChooser = new ButtonGroup();
	private JRadioButton nc = new JRadioButton("Northern California");
	private JRadioButton sc = new JRadioButton("Southern California");
	JTextField eventIdBox = new JTextField("Enter Event ID");
	JButton downloadUSGSButton = new JButton("Download USGS Shake Map");
	
	//Table and transparency for preset shakemaps
	String[] header = {"Name", "List Index"};
	public SurfaceTableModel surfaceTableModel = new SurfaceTableModel(header);
	public JTable surfaceTable = new JTable(surfaceTableModel);
	private JSlider transparencySlider = new JSlider(); 
	
	
	public ShakeMapGUI(PluginActors pluginActors) {
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
					tempCheckbox.setName(tempName);
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
		
		bottomPane.setLayout(new GridLayout(2,0,0,15));
		bottomPane.add(panesPanel);
		bottomPane.add(sliderPanel);
		
		
		//Checkboxes for the USGS Table
		usgsDownloads.setLayout(new GridLayout(0,2)); //2 per row
		//Initialize all the preset files in the data/ShakeMapPlugin directory
		File usgsDirectory = new File(dataPath + "/" + moreMaps);
		if (usgsDirectory.isDirectory()) {
			// List files in the directory and process each
			File files[] = usgsDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					String tempName = files[i].getName();
					JCheckBox tempCheckbox = new JCheckBox(tempName);
					tempCheckbox.setName(tempName);
					tempCheckbox.addItemListener(this);
					usgsDownloads.add(tempCheckbox);
					checkBoxList.add(tempCheckbox); //add the JCheckBox to the list
					shakeMapsList.add(null); //for now, initialize to null
					actorList.add(null);
				}
			}
		}
		
		
		JPanel USGSPanel = new JPanel();
		USGSPanel.setLayout(new FlowLayout());
		calChooser.add(nc);
		calChooser.add(sc);
		USGSPanel.add(new JLabel("Select Region:"));
		USGSPanel.add(nc);
		USGSPanel.add(sc);
		eventIdBox.setPreferredSize(new Dimension(200,40));
		USGSPanel.add(eventIdBox);
		USGSPanel.add(downloadUSGSButton);
		
		downloadUSGSButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String id = eventIdBox.getText();
				if(id.length() > 0){
					String network = "";
					if(nc.isSelected()){
						network = "nc";
					}else if(sc.isSelected()){
						network = "sc";
					}
					if(network.length() > 0){
						USGSShakeMapDownloader smd = new USGSShakeMapDownloader(network, id);
						String d = smd.downloadShakeMap(id+".txt");
						System.out.println(d);
						if(d.length() <= 0){
							System.out.println("Failure");
							JOptionPane.showMessageDialog(shakeMapLibraryPanel,
								    "File not found on USGS site.");
						}else{
							System.out.println("Loaded!");
							showNewUSGSMap(id+".txt");
							addNewUSGSCheckBox(id+".txt");
						}
					}else{
						System.out.println("Make a location selection!");
						JOptionPane.showMessageDialog(shakeMapLibraryPanel,
							    "Select A Region (Northern or Southern California");
					}
				}else{
					System.out.println("Enter an earthquake ID!");
					JOptionPane.showMessageDialog(shakeMapLibraryPanel,
						    "Enter an earthquake ID.");
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
		tabbedPane.addTab("Saved Maps", new JScrollPane(usgsDownloads));
		tabbedPane.addTab("Download USGS Map", USGSPanel);
		JPanel shaPanel = new JPanel(new FlowLayout());
		shaPanel.add(openSHAButton);
		tabbedPane.addTab("OpenSHA", shaPanel);
		
		shakeMapLibraryPanel.setLayout(new BorderLayout());
		shakeMapLibraryPanel.add(tabbedPane, BorderLayout.NORTH);
		shakeMapLibraryPanel.add(bottomPane, BorderLayout.SOUTH);
		this.add(shakeMapLibraryPanel);
	}



	public JPanel getPanel() {
		// TODO Auto-generated method stub
		return this;
	}

	/*
	 * This immediately shows the map after downloading it
	 * from the USGS website
	 */
	private void showNewUSGSMap(String filename){
		ShakeMap shakeMap = new ShakeMap(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Extra/colors.cpt");
		shakeMap.loadFromFileToGriddedGeoDataSet(dataPath + "/" + moreMaps + "/" + filename);
		shakeMap.setActor(shakeMap.builtPolygonSurface());
		actorList.add(shakeMap.getActor());
		pluginActors.addActor(shakeMap.getActor());
		shakeMapsList.add(shakeMap);
		Info.getMainGUI().updateRenderWindow();
	}
	
	//appends a CheckBox after downloading a new USGS
	//shake map
	private void addNewUSGSCheckBox(String fileName){
		JCheckBox tempCheckbox = new JCheckBox(fileName);
		tempCheckbox.setName(fileName);
		tempCheckbox.setSelected(true);
		tempCheckbox.addItemListener(this);
		usgsDownloads.add(tempCheckbox); //add it to GUI
		checkBoxList.add(tempCheckbox); //add it to data
	}
	
	//What happens whenever a check box is selected
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		Object src = e.getSource();
		for(int i=0; i<checkBoxList.size(); i++){
			if(src == checkBoxList.get(i)){
				if (e.getStateChange()==ItemEvent.SELECTED) {
					if(shakeMapsList.get(i) == null){ //if it has never been selected before, load the file
						ShakeMap shakeMap = new ShakeMap(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Extra/colors.cpt");
						if(checkBoxList.get(i).getName().equals("openSHA.txt")){
							//The file format for data from openSHA files are a little different.
							//Open declaration of method for more details
							shakeMap.loadOpenSHAFileToGriddedGeoDataSet(dataPath + "/" + checkBoxList.get(i).getName());
						}else{
							File f = new File(dataPath + "/" + checkBoxList.get(i).getName());
							if(f.exists())
								shakeMap.loadFromFileToGriddedGeoDataSet(dataPath + "/" + checkBoxList.get(i).getName());			
							else
								shakeMap.loadFromFileToGriddedGeoDataSet(dataPath + "/" + moreMaps + "/" + checkBoxList.get(i).getName());
						}
						shakeMap.setActor(shakeMap.builtPolygonSurface());
						actorList.set(i, shakeMap.getActor());
						pluginActors.addActor(shakeMap.getActor());
						shakeMapsList.set(i, shakeMap);
					}else{
						shakeMapsList.get(i).getActor().SetVisibility(1);
					}
					//add to table
					surfaceTableModel.addRow(new Object[]{checkBoxList.get(i).getName(), i});
				}else{ //checkbox is unselected
					shakeMapsList.get(i).getActor().SetVisibility(0);
					for(int j=0; j<surfaceTableModel.getRowCount(); j++){
						if(checkBoxList.get(i).getName().equals(surfaceTableModel.getValueAt(j, 0))){
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
		} else {
			this.transparencySlider.setEnabled(false);
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
