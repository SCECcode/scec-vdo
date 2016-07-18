package org.scec.vtk.plugins.SurfacePlugin;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.SurfacePlugin.Component.LoadedFilesProperties;


public class SurfacePluginState implements PluginState{


	private SurfacePluginGUI parent;
	private ArrayList<Double> transparency;
	ArrayList<Integer> visibility;
	private ArrayList<String> dispName;
	private ArrayList<String> filePath;
	private ArrayList<String> filePathgeo;
	private ArrayList<Integer> fromWeb;
	private ArrayList<Surface> surfaceArray;



	SurfacePluginState(SurfacePluginGUI parent)
	{

		this.parent = parent;
		dispName = new ArrayList<>();
		filePath = new ArrayList<>();
		surfaceArray = new ArrayList<>();
		filePathgeo = new ArrayList<>();
		transparency = new ArrayList<>();
		visibility = new ArrayList<>();
		fromWeb = new ArrayList<>();


	}

	void copyLatestCatalogDetails()
	{
		fromWeb.clear();
		dispName.clear();
		filePath.clear();
		surfaceArray.clear();
		filePathgeo.clear();
		transparency.clear();
		visibility.clear();

		for (Surface surf : SurfacePluginGUI.surfaceArray)
		{
			surfaceArray.add(surf);
			String imagename = surf.getImageInfo().getImageName();
			String file = surf.getImageInfo().getFilename();
			dispName.add(imagename);
			filePath.add(new File(file).getAbsolutePath());
			if(surf.getGeoSurfaceInfo() == null)
			{
				//fromWeb.add(0);
			}
			else
			{
				String fileGeo = surf.getGeoSurfaceInfo().getFilename();
				filePathgeo.add(new File(fileGeo).getAbsolutePath());
				//fromWeb.add(1);
			}
			//create local copies of display attributes
			transparency.add(surf.getSurfaceActor().GetProperty().GetOpacity());
			visibility.add(surf.getVisibility());
		}
	}

	@Override
	public void load() {
		// call methods to update based on the properties captured //might also want to put swing invoke and wait
		int i=0;
		for (Surface surf : surfaceArray)
		{
			surf.setDisplayName(dispName.get(i));
			System.out.println(transparency.get(i));
			System.out.println(visibility.get(i));
			parent.setTransparency(surf, transparency.get(i));
			parent.setVisibility(surf,i,visibility.get(i));
			Info.getMainGUI().updateRenderWindow();
			i++;
		}
	}

	private void createElement(Element stateEl) {
		int i=0;
		for (Surface surf : SurfacePluginGUI.surfaceArray)
		{
			//System.out.println(filePathgeo.get(i));
			if (filePathgeo.size() != 0)
			{
				stateEl.addElement( "Surfaces" )
				.addAttribute( "dispName", dispName.get(i))
				.addAttribute( "filePath", filePath.get(i))
				.addAttribute("GeographicSurfaceInfo", filePathgeo.get(i))
				//.addAttribute("fromWeb", Integer.toString(fromWeb.get(i)))
				.addAttribute( "transparency", Double.toString(transparency.get(i)))
				.addAttribute( "visibility",(visibility.get(i).toString()));
			}
			else
			{
				stateEl.addElement( "Surfaces" )
				.addAttribute( "dispName", dispName.get(i))
				.addAttribute( "filePath", filePath.get(i))
				.addAttribute("GeographicSurfaceInfo", "")
				//.addAttribute("fromWeb", Integer.toString(fromWeb.get(i)))
				.addAttribute( "transparency", Double.toString(transparency.get(i)))
				.addAttribute( "visibility",(visibility.get(i).toString()));

			}

			i++;
		}
	}

	@Override
	public void toXML(Element stateEl) {
		copyLatestCatalogDetails();
		createElement(stateEl);
	}

	@Override
	public void fromXML(Element stateEl) {

		for ( Iterator i = stateEl.elementIterator( "Surfaces" ); i.hasNext(); ) 
		{
			Element e = (Element) i.next();
			dispName.add(e.attributeValue("dispName"));
			filePath.add(e.attributeValue("filePath"));
			filePathgeo.add(e.attributeValue("GeographicSurfaceInfo"));
			//fromWeb.add(Integer.parseInt(e.attributeValue("fromWeb")));
			transparency.add(Double.parseDouble(e.attributeValue("transparency")));	          
			visibility.add(Integer.parseInt(e.attributeValue("visibility")));	            

			System.out.println(e.attributeValue("filePath"));
			// read the catalog file

		}
		addSurfaceToTable(dispName, filePath);

		for (Surface suf : surfaceArray)
		{
			System.out.println("Surface ARRAY: " + suf.getImageInfo().getImageName());
		}

	}

	@Override
	public PluginState deepCopy() {
		SurfacePluginState state = new SurfacePluginState(parent);
		state.copyLatestCatalogDetails();
		return state;
	}

	public void addSurfaceToTable(ArrayList<String> pictureNames, ArrayList<String> filepath)
	{
		LoadedFilesProperties lfp;
		MapSetCreatePluginGUI mscpg;
		for(int i = 0 ; i<pictureNames.size(); i++){

			if (pictureNames.get(i).equals("CaliforniaFull")) {

				parent.setCheckBox("cm", true);

				double imageData[] = new double[5];
				imageData[0] = 42.2;
				imageData[1] = 32.1;
				imageData[2] = -113.4;
				imageData[3] = -124.5;
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="CaliforniaFull"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";

				lfp = new LoadedFilesProperties(filepath.get(i), imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this.parent);

			}
			else if (pictureNames.get(i).equals("world.topo.bathy.200410.3x5400x2700")) {

				parent.setCheckBox("wm", true);
				double imageData[] = new double[5];
				imageData[0] = 90;
				imageData[1] = -90;
				imageData[2] = 180;
				imageData[3] = -180;
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="world.topo.bathy.200410.3x5400x2700";//image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";

				lfp = new LoadedFilesProperties(filepath.get(i), imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this.parent);

			}
			else if (pictureNames.get(i).equals("largesocal")) {
				parent.setCheckBox("sc", true);
				double imageData[] = new double[5];
				imageData[0] = 36;
				imageData[1] = 32.5;
				imageData[2] = -114;
				imageData[3] = -122;
				imageData[4] = 0;

				String surfaceTemp="-";
				String imageTemp="largesocal";//image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";

				lfp = new LoadedFilesProperties(filepath.get(i), imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this.parent);

			}
			else if (pictureNames.get(i).equals("Japan")) {
				parent.setCheckBox("jm", true);
				double imageData[] = new double[5];
				imageData[0] = 46.9; //Latitude Max
				imageData[1] = 26.2; //Latitude Min
				imageData[2] = 147; //Longitude Max
				imageData[3] = 127.1; //Longitude Min
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="Japan"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";

				lfp = new LoadedFilesProperties(filepath.get(i), imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this.parent);

			}
			else if (pictureNames.get(i).equals("NewZealand")) {
				parent.setCheckBox("nz", true);
				double imageData[] = new double[5];
				imageData[0] = -34.3; //Latitude Max
				imageData[1] = -47.5; //Latitude Min
				imageData[2] = 179.4; //Longitude Max
				imageData[3] = 165.4; //Longitude Min

				String surfaceTemp="-";
				String imageTemp="NewZealand"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";

				lfp = new LoadedFilesProperties(filepath.get(i), imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this.parent);

			}
			else if (pictureNames.get(i).equals("Indonesia")) {
				parent.setCheckBox("im", true);
				double imageData[] = new double[5];
				imageData[0] = 9.4; //Latitude Max
				imageData[1] = -12.0; //Latitude Min
				imageData[2] = 148.4; //Longitude Max
				imageData[3] = 93.8; //Longitude Min
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="Indonesia"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";

				lfp = new LoadedFilesProperties(filepath.get(i), imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this.parent);

			}
			else if (pictureNames.get(i).equals("Haiti")) {
				parent.setCheckBox("hm", true);
				double imageData[] = new double[5];
				imageData[0] = 20.5; //Latitude Max
				imageData[1] = 17.5; //Latitude Min
				imageData[2] = -68.5; //Longitude Max
				imageData[3] = -74.5; //Longitude Min
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="Haiti"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";

				lfp = new LoadedFilesProperties(filepath.get(i), imageData,"-",null,null,false,loadedFilePath);

				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this.parent);

			}
			else if (pictureNames.get(i).equals("Mexico")) {
				parent.setCheckBox("mm", true);
				double imageData[] = new double[5];
				imageData[0] = 35.4; //Latitude Max
				imageData[1] = 12.8; //Latitude Min
				imageData[2] = -82.9; //Longitude Max
				imageData[3] = -119; //Longitude Min
				imageData[4] = -8; //Altitude

				String surfaceTemp="-";
				String imageTemp="Mexico"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";

				lfp = new LoadedFilesProperties(filepath.get(i), imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this.parent);

			}
			else if (pictureNames.get(i).equals("SouthAmerica")) {
				parent.setCheckBox("sa", true);
				double imageData[] = new double[5];
				imageData[0] = 14.7; //Latitude Max 6.3
				imageData[1] = -57; //Latitude Min -58
				imageData[2] = -36; //Longitude Max
				imageData[3] = -82.4; //Longitude Min -83
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="SouthAmerica";//image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";

				lfp = new LoadedFilesProperties(filepath.get(i), imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this.parent);

			}
			else if (pictureNames.get(i).equals("CaliforniaDEM")) {
				parent.setCheckBox("cd", true);
				double imageData[] = new double[5];
				imageData[0] = 42;
				imageData[1] = 32.5;
				imageData[2] = -114;
				imageData[3] = -124.5;
				imageData[4] = 0; //Altitude

				String surfaceTemp="-";
				String imageTemp="CaliforniaDEM"; //image name
				String imageExt=".png"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";

				lfp = new LoadedFilesProperties(filepath.get(i), imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this.parent);

			}
			else if (pictureNames.get(i).equals("CAlDEM_new")) {
				parent.setCheckBox("cdc", true);
				double imageData[] = new double[5];
				imageData[0] = 42;
				imageData[1] = 32.5;
				imageData[2] = -114.131477;
				imageData[3] = -124.409641;
				imageData[4] = 0; //Altitude

				String surfaceTemp="-";
				String imageTemp="CAlDEM_new"; // image name
				String imageExt=".png"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";

				lfp = new LoadedFilesProperties(filepath.get(i), imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this.parent);

			}


			Info.getMainGUI().updateRenderWindow();

		}
	}


}