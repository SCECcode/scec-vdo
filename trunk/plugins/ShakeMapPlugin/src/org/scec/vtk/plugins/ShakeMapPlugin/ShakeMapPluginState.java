package org.scec.vtk.plugins.ShakeMapPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JCheckBox;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.ShakeMapPlugin.Component.ShakeMap;

public class ShakeMapPluginState implements PluginState{
	
	private ShakeMapGUI parent;


	private ArrayList<String> filePath;
	private ArrayList<Double> transparency;
	private ArrayList<String> dispName;
	
	ShakeMapPluginState(ShakeMapGUI parent)
	{
		this.parent = parent;
		filePath = new ArrayList<>();
		transparency = new ArrayList<>();
		dispName = new ArrayList<>();
	}

	void copyLatestCatalogDetails()
	{

		filePath.clear();
		transparency.clear();
		dispName.clear();

		for (JCheckBox box : parent.getCheckBoxList())
		{
			filePath.add(box.getName());
			dispName.add(box.getName());
		}
		
		for(ShakeMap shake: parent.getShakeMapsList()){
			if(shake != null) 
				transparency.add(shake.getActor().GetProperty().GetOpacity());
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
				.addAttribute( "filePath", filePath.get(i))
				.addAttribute( "dispName", dispName.get(i))
				.addAttribute( "transparency", Double.toString(parent.getShakeMapsList().get(i).getActor().GetProperty().GetOpacity()));
			}

			i++;
		}
	}

	@Override
	public void toXML(Element stateEl) {
		copyLatestCatalogDetails();
		createElement(stateEl);
	}
	
	public void showMaps(ArrayList<String> filenames, ArrayList<String> displayNames, ArrayList<Double> transparentValues){
		File presetDirectory = new File(parent.dataPath);
		File savedDirectory = new File(parent.dataPath+"/"+parent.moreMaps);
		//Add everyting to one list
		ArrayList<File> files = new ArrayList<File>();
		//add presets first
		for(File f: presetDirectory.listFiles())
			if(f.isFile())
				files.add(f);
		//add saved maps next
		//for now, there's no subdirectories, so just add all
		files.addAll(Arrays.asList(savedDirectory.listFiles()));
		
		int transparencyIndex = 0; //only increments if a file is loaded
		for(String chosenFile: filenames){
			// List files in the directory and process each
			for (int i = 0; i < files.size(); i++) {
				if (files.get(i).getName().equals(chosenFile)) {
					//set visiblity true
					ShakeMap shakeMap = new ShakeMap(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Extra/colors.cpt");
					if(parent.getCheckBoxList().get(i).getName().equals("openSHA.txt")){
						//The file format for data from openSHA files are a little different.
						//Open declaration of method for more details
						shakeMap.loadOpenSHAFileToGriddedGeoDataSet(parent.dataPath + "/" + parent.getCheckBoxList().get(i).getName());
					}else{
						File f = new File(parent.dataPath + "/" + parent.getCheckBoxList().get(i).getName());
						if(f.exists())
							shakeMap.loadFromFileToGriddedGeoDataSet(parent.dataPath + "/" + parent.getCheckBoxList().get(i).getName());	
						else
							shakeMap.loadFromFileToGriddedGeoDataSet(parent.dataPath + "/" + parent.moreMaps + "/" + parent.getCheckBoxList().get(i).getName());			
					}
					shakeMap.setActor(shakeMap.builtPolygonSurface());
					shakeMap.getActor().GetProperty().SetOpacity(transparentValues.get(transparencyIndex));
					parent.getActorList().set(i, shakeMap.getActor());
					parent.getPluginActors().addActor(shakeMap.getActor());
					parent.getShakeMapsList().set(i, shakeMap);
					parent.getCheckBoxList().get(i).setSelected(true);
					Info.getMainGUI().updateRenderWindow();
					transparencyIndex++;
					break;
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
			dispName.add(e.attributeValue("dispName"));
			transparency.add(Double.parseDouble(e.attributeValue("transparency")));	          

//			System.out.println(e.attributeValue("filePath"));
			// read the catalog file
		}
		showMaps(filePath, dispName, transparency);
	}

	@Override
	public PluginState deepCopy() {
		ShakeMapPluginState state = new ShakeMapPluginState(parent);
		state.copyLatestCatalogDetails();
		return state;
	}


}
