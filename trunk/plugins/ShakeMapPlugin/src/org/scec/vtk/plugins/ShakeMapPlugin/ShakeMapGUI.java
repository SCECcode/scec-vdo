package org.scec.vtk.plugins.ShakeMapPlugin;

import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.opensha.commons.util.cpt.CPT;
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
				ShakeMap shakeMap = new ShakeMap(new CPT(), Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Extra/colors.cpt");
				if(!shakeMapsList.contains(shakeMap))
				{
					shakeMap.loadFromFile(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Chino_Hills.txt","shake");
					shakeMapsList.add(shakeMap);
					shakeMap.drawPolygonMap();
					vtkActor actor = new vtkActor();
					actor.SetMapper(shakeMap.getMapper());
					actorList.add(actor);
					pluginActors.addActor(actor);
					Info.getMainGUI().updateRenderWindow();
				}
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
