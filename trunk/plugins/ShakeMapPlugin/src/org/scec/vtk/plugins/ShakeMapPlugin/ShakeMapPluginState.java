package org.scec.vtk.plugins.ShakeMapPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.ShakeMapPlugin.Component.ShakeMap;

public class ShakeMapPluginState implements PluginState{
	
	private ShakeMapGUI parent;


	private ArrayList<String> filePath;
	

	ShakeMapPluginState(ShakeMapGUI parent)
	{

		this.parent = parent;

		filePath = new ArrayList<>();



	}

	void copyLatestCatalogDetails()
	{

		filePath.clear();


		for (JCheckBox box : parent.getCheckBoxList())
		{
			filePath.add(box.getName());
			System.out.println(box.getName());
		}
	}

	@Override
	public void load() {
		// call methods to update based on the properties captured //might also want to put swing invoke and wait
		int i=0;
		for (JCheckBox surf : parent.getCheckBoxList())
		{
//			String fileName = surf.getName();
			
//			surf.setDisplayName(dispName.get(i));
//			System.out.println(transparency.get(i));
//			System.out.println(visibility.get(i));
//			parent.setTransparency(surf, transparency.get(i));
//			parent.setVisibility(surf,i,visibility.get(i));
//			Info.getMainGUI().updateRenderWindow();
//			i++;
		}
	}

	private void createElement(Element stateEl) {
		int i=0;
		for (JCheckBox box : parent.getCheckBoxList())
		{
			if(box.isSelected()){
				stateEl.addElement( "ShakeMaps" )
				.addAttribute( "filePath", filePath.get(i));
	//			.addAttribute( "transparency", Double.toString(transparency.get(i)));
			}

			i++;
		}
	}

	@Override
	public void toXML(Element stateEl) {
		copyLatestCatalogDetails();
		createElement(stateEl);
	}
	
	public void showMaps(ArrayList<String> filenames){
		File dataDirectory = new File(parent.dataPath);
		for(String chosenFile: filenames){
			// List files in the directory and process each
			int fileIndex = -1;
			File files[] = dataDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if(files[i].isFile()){
					fileIndex++;
					if (files[i].getName().equals(chosenFile)) {
						//set visiblity true
						ShakeMap shakeMap = new ShakeMap(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Extra/colors.cpt");
						if(parent.getCheckBoxList().get(fileIndex).getName().equals("openSHA.txt")){
							//The file format for data from openSHA files are a little different.
							//Open declaration of method for more details
							shakeMap.loadOpenSHAFileToGriddedGeoDataSet(parent.dataPath + "/" + parent.getCheckBoxList().get(fileIndex).getName());
						}else{
							shakeMap.loadFromFileToGriddedGeoDataSet(parent.dataPath + "/" + parent.getCheckBoxList().get(fileIndex).getName());			
						}
						shakeMap.setActor(shakeMap.builtPolygonSurface());
						parent.getActorList().set(fileIndex, shakeMap.getActor());
						parent.getPluginActors().addActor(shakeMap.getActor());
						parent.getShakeMapsList().set(fileIndex, shakeMap);
						parent.getCheckBoxList().get(fileIndex).setSelected(true);
						Info.getMainGUI().updateRenderWindow();
					}
				}
			}
		}
	}

	@Override
	public void fromXML(Element stateEl) {
		for ( Iterator i = stateEl.elementIterator( "ShakeMaps" ); i.hasNext(); ) 
		{
			Element e = (Element) i.next();
			filePath.add(e.attributeValue("filePath"));
//			transparency.add(Double.parseDouble(e.attributeValue("transparency")));	          

//			System.out.println(e.attributeValue("filePath"));
			// read the catalog file
		}
		showMaps(filePath);
	}

	@Override
	public PluginState deepCopy() {
		ShakeMapPluginState state = new ShakeMapPluginState(parent);
		state.copyLatestCatalogDetails();
		return state;
	}


}
