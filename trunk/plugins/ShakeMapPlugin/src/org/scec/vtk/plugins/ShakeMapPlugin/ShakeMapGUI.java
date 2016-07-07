package org.scec.vtk.plugins.ShakeMapPlugin;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scec.vtk.drawingTools.DefaultLocationsGUI.PresetLocationGroup;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.ShakeMapPlugin.Component.ShakeMap;

import vtk.vtkActor;



public class ShakeMapGUI extends JPanel implements ItemListener{


	private static final long serialVersionUID = 1L;
	
	String dataPath = Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin"; //path to directory with local folders

	private JPanel shakeMapLibraryPanel;
//	JCheckBox checkBox;
	ArrayList<JCheckBox> defaultList; //for the local files in ShakeMapPlugin directory
	ArrayList<ShakeMap> shakeMapsList;
	PluginActors pluginActors;
//	ShakeMap shakeMap;
	private ArrayList<vtkActor> actorList;
	
	public ShakeMapGUI(PluginActors pluginActors) {
		shakeMapLibraryPanel = new JPanel();
		shakeMapLibraryPanel.setLayout(new GridLayout(0,2)); //2 per row
		
//		checkBox = new JCheckBox("Chino Hills");
//		checkBox.addItemListener(this);
//		shakeMapLibraryPanel.add(checkBox);
		defaultList = new ArrayList<JCheckBox>();
		this.pluginActors = pluginActors;
		this.add(shakeMapLibraryPanel);
		shakeMapsList = new ArrayList<ShakeMap>();
		actorList = new ArrayList<>();
		
//		shakeMap = new ShakeMap(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Extra/colors.cpt");
		
		// Check to make sure it exists
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
					shakeMapLibraryPanel.add(tempCheckbox);
					defaultList.add(tempCheckbox); //add the JCheckBox to the list
					shakeMapsList.add(null); //for now, initialize to null
					actorList.add(null);
				}
			}
		}
		
	}



	public JPanel getPanel() {
		// TODO Auto-generated method stub
		return this;
	}



	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		Object src = e.getSource();
		for(int i=0; i<defaultList.size(); i++){
			if(src == defaultList.get(i)){
				if (e.getStateChange()==ItemEvent.SELECTED) {
					if(shakeMapsList.get(i) == null){
						ShakeMap shakeMap = new ShakeMap(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Extra/colors.cpt");
						shakeMap.loadFromFileToGriddedGeoDataSet(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/" + defaultList.get(i).getName());
						shakeMap.setActor(shakeMap.builtPolygonSurface());
						actorList.set(i, shakeMap.getActor());
						pluginActors.addActor(shakeMap.getActor());
						shakeMapsList.set(i, shakeMap);
					}else
						shakeMapsList.get(i).getActor().SetVisibility(1);
				}else
					shakeMapsList.get(i).getActor().SetVisibility(0);
				Info.getMainGUI().updateRenderWindow();
			}
		}
	}

	public void unloadPlugin()
	{

		for(vtkActor actor:actorList)
		{
			pluginActors.removeActor(actor);
		}
		Info.getMainGUI().updateRenderWindow();
	}

}
