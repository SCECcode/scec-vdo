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

import vtk.vtkActor;

/*
 * So far, the attributes the Shake Map stores is the name of the file 
 * which stores the data, and the opacity (transparency) of the shake map
 */
public class ShakeMapPluginState implements PluginState{
	
	private ShakeMapGUI parent;

	//Values in these arrays will be saved to xml file
	private ArrayList<String> filePath;
	private ArrayList<Double> transparency;
	
	//These values are not saved in a xml file, 
	//but are used in the load() function
	private ArrayList<Integer> visibility;
	private ArrayList<Integer> selectedIndexes;
	
	ShakeMapPluginState(ShakeMapGUI parent)
	{
		this.parent = parent;
		filePath = new ArrayList<String>();
		transparency = new ArrayList<Double>();
		visibility = new ArrayList<Integer>();
		selectedIndexes = new ArrayList<Integer>();
	}

	//Gets the latest details. This function is called
	//in toXML()
	void copyLatestCatalogDetails()
	{

		filePath.clear();
		transparency.clear();
		selectedIndexes.clear();

		for (JCheckBox box : parent.getCheckBoxList())
		{
			filePath.add(box.getName());
		}
		
		int shakeIndex = 0;
		for(ShakeMap shake: parent.getShakeMapsList()){
			if(shake != null){ 
				transparency.add(shake.getActor().GetProperty().GetOpacity());
				visibility.add(shake.getActor().GetVisibility());
				selectedIndexes.add(shakeIndex);
			}
			else{
				transparency.add(null);
				visibility.add(0);
			}
			shakeIndex++;
		}
	}

	/*
	 * Whenever a new state key frame (the red key frame) for the Shake Map
	 * plugin is added to the timeline, this function is called.
	 */
	@Override
	public void load() {
		// call methods to update based on the properties captured //might also want to put swing invoke and wait
		for(int i=0; i<parent.getShakeMapsList().size(); i++){	
			if(visibility.isEmpty())
				break;
			if(parent.getShakeMapsList().get(i) != null){			
				parent.getShakeMapsList().get(i).getActor().SetVisibility(visibility.get(i));
				parent.getCheckBoxList().get(i).setSelected(visibility.get(i)==1);
			}
		}
		for(int i: selectedIndexes){
			parent.getShakeMapsList().get(i).getActor().GetProperty().SetOpacity(transparency.get(i));
		}		
	}

	private void createElement(Element stateEl) {
		int i=0;
		for (JCheckBox box : parent.getCheckBoxList())
		{
			if(box.isSelected()){
				stateEl.addElement( "ShakeMaps" )
				.addAttribute( "filePath", filePath.get(i))
				.addAttribute( "transparency", Double.toString(parent.getShakeMapsList().get(i).getActor().GetProperty().GetOpacity()));
			}

			i++;
		}
	}

	//Writes to a xml file
	@Override
	public void toXML(Element stateEl) {
		copyLatestCatalogDetails();
		createElement(stateEl);
	}
	
	/*
	 * Display the maps after reading the data from the xml files.
	 * -filenames: list of files which contain the map data
	 * -transparentValues: list of opacity (transparency) values
	 */
	public void showMaps(ArrayList<String> filenames, ArrayList<Double> transparentValues){
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

	//Reads the file path and the transparency value from the xml file
	@Override
	public void fromXML(Element stateEl) {
		for ( Iterator i = stateEl.elementIterator( "ShakeMaps" ); i.hasNext(); ) 
		{
			Element e = (Element) i.next();
			filePath.add(e.attributeValue("filePath"));
			transparency.add(Double.parseDouble(e.attributeValue("transparency")));	          
		}
		showMaps(filePath, transparency);
	}

	@Override
	public PluginState deepCopy() {
		ShakeMapPluginState state = new ShakeMapPluginState(parent);
		state.copyLatestCatalogDetails();
		return state;
	}


}
