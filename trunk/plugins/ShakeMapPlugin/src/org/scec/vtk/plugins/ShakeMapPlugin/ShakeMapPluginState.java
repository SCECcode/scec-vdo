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
	private ArrayList<String> mapParameter; //mmi, pga, or pgv
	
	//These values are not saved in a xml file, 
	//but are used in the load() function
	private ArrayList<Integer> visibility;
	private ArrayList<Integer> selectedIndexes;
	private ArrayList<Double> setTransparencies;
	
	
	// Initialize the GUI element and lists needed to store values for individual elements
	ShakeMapPluginState(ShakeMapGUI parent)
	{
		this.parent = parent;
		filePath = new ArrayList<String>();
		transparency = new ArrayList<Double>();
		mapParameter = new ArrayList<String>();
		visibility = new ArrayList<Integer>();
		selectedIndexes = new ArrayList<Integer>();
		setTransparencies = new ArrayList<Double>();
	}

	// Gets the latest details from existing elements. 
	// This function is called in toXML()
	void copyLatestCatalogDetails()
	{
		filePath.clear();
		transparency.clear();
		selectedIndexes.clear();
		mapParameter.clear();
		
		int shakeIndex = 0;
		for(ShakeMap shake: parent.getShakeMapsList()){
			if(shake != null){ 
				visibility.add(shake.getActor().GetVisibility());
				selectedIndexes.add(shakeIndex);
				setTransparencies.add(shake.getActor().GetProperty().GetOpacity());
			}
			else{
				visibility.add(0);
				setTransparencies.add(null);
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
		int v_size = visibility.size();
		for(int i=0; i<parent.getShakeMapsList().size(); i++){	
			if(visibility.isEmpty())
				break;
			if(parent.getShakeMapsList().get(i) != null){			
				if(i<visibility.size()){ 
					parent.getShakeMapsList().get(i).getActor().SetVisibility(visibility.get(i));
					parent.getCheckBoxList().get(i).setSelected(visibility.get(i)==1);
				}else{ //maybe map was not loaded yet at that time frame
					parent.getShakeMapsList().get(i).getActor().SetVisibility(0);
					parent.getCheckBoxList().get(i).setSelected(false);
				}
			}
		}
		for(int i: selectedIndexes){
			parent.getShakeMapsList().get(i).getActor().GetProperty().SetOpacity(setTransparencies.get(i));
		}		
	}

	private void createElement(Element stateEl) {
		int i=0;
		for (JCheckBox box : parent.getCheckBoxList())
		{
			if(box.isSelected()){
				Element propertyEl = stateEl.addElement( "ShakeMaps" );
				propertyEl.addElement("filePath").addText(box.getName());
				propertyEl.addElement("transparency").addText(Double.toString(parent.getShakeMapsList().get(i).getActor().GetProperty().GetOpacity()));
				propertyEl.addElement("parameter").addText(parent.getShakeMapsList().get(i).getParameter());
			}

			i++;
		}
	}

	//Writes to a XML file after fetching all the current element details and creating element tags.
	@Override
	public void toXML(Element stateEl) {
		copyLatestCatalogDetails();
		createElement(stateEl);
	}
	
	/*
	 * Display the maps after reading the data from the xml files.
	 * -filepaths: list of files which contain the map data
	 * -transparentValues: list of opacity (transparency) values
	 */
	public void showMaps(ArrayList<String> filepaths, ArrayList<Double> transparentValues){
		int i = 0;
		for(String path:filepaths){
			boolean checked = false;
			int checkBoxIndex = 0;
			for(JCheckBox box: parent.getCheckBoxList()){
				//if path one of the checkBoxes, activate the checkbox
				if(path.equals(box.getName())){
					ShakeMap shakeMap = new ShakeMap(Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin/Extra/colors.cpt", mapParameter.get(i));
					File f = new File(parent.getCheckBoxList().get(checkBoxIndex).getName());
					if(f.getName().equals("openSHA.txt")){
						//The file format for data from openSHA files are a little different.
						//Open declaration of method for more details
						shakeMap.loadOpenSHAFileToGriddedGeoDataSet(f.getPath());
					}else{
						shakeMap.loadFromFileToGriddedGeoDataSet(f.getPath());	
					}
					shakeMap.setActor(shakeMap.builtPolygonSurface());
					shakeMap.getActor().GetProperty().SetOpacity(transparentValues.get(i));
					parent.getActorList().set(checkBoxIndex, shakeMap.getActor());
					parent.getPluginActors().addActor(shakeMap.getActor());
					parent.getShakeMapsList().set(checkBoxIndex, shakeMap);
					parent.getCheckBoxList().get(checkBoxIndex).setSelected(true);
					i++;
					checked = true;
					break;
				}
				checkBoxIndex++;
			}
			if(!checked){
				File f = new File(path);
				parent.addMap(f.getName(), path, mapParameter.get(i));
				int latestIndex = parent.getShakeMapsList().size()-1;
				parent.getShakeMapsList().get(latestIndex).getActor().GetProperty().SetOpacity(transparentValues.get(i));
				i++;
			}
		}
		Info.getMainGUI().updateRenderWindow();
	}
	
	//Reads the file path and the transparency value from the xml file
	@Override
	public void fromXML(Element stateEl) {
		for ( Iterator i = stateEl.elementIterator( "ShakeMaps" ); i.hasNext(); ) 
		{
			Element e = (Element) i.next();
			filePath.add(e.elementText("filePath"));
			transparency.add(Double.parseDouble(e.elementText("transparency")));	 
			mapParameter.add(e.elementText("parameter"));
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
