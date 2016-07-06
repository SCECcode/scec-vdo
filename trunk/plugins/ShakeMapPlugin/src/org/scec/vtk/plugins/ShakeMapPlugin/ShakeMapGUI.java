package org.scec.vtk.plugins.ShakeMapPlugin;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.ShakeMapPlugin.Component.ShakeMap;

import vtk.vtkActor;



public class ShakeMapGUI extends JPanel implements ItemListener{


	private static final long serialVersionUID = 1L;

	private JPanel shakeMapLibraryPanel;
	JCheckBox checkBox;
	ArrayList<ShakeMap> shakeMapsList;
	PluginActors pluginActors;
	ShakeMap shakeMap;
	private ArrayList<vtkActor> actorList;
	
	public ShakeMapGUI(PluginActors pluginActors) {
		shakeMapLibraryPanel = new JPanel();
		checkBox = new JCheckBox("Chino Hills");
		checkBox.addItemListener(this);
		shakeMapLibraryPanel.add(checkBox);
		this.pluginActors = pluginActors;
		this.add(shakeMapLibraryPanel);
		shakeMapsList = new ArrayList<ShakeMap>();
		actorList = new ArrayList<>();
		
		shakeMap = new ShakeMap(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Extra/colors.cpt");
		
	}



	public JPanel getPanel() {
		// TODO Auto-generated method stub
		return this;
	}



	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		Object src = e.getSource();
		if(src == checkBox)
		{
			if (e.getStateChange()==ItemEvent.SELECTED) {
				if(!shakeMapsList.contains(shakeMap))
				{
					shakeMap.loadFromFileToGriddedGeoDataSet(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Chino_Hills.txt");
					shakeMapsList.add(shakeMap);
					shakeMap.setActor(shakeMap.builtPolygonSurface());
					actorList.add(shakeMap.getActor());
					pluginActors.addActor(shakeMap.getActor());
				}
				else
				{
					shakeMapsList.get(0).getActor().SetVisibility(1);
				}
			}
			else
			{
				shakeMapsList.get(0).getActor().SetVisibility(0);
			}
			Info.getMainGUI().updateRenderWindow();
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
