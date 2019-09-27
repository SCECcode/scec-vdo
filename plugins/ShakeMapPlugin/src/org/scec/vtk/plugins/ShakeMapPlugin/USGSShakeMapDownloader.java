package org.scec.vtk.plugins.ShakeMapPlugin;

//xml builder stuff
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;

/*
 * This class is responsible for loading shake map files from the USGS
 * website. The url consists of the network and the earthquake id. 
 * By specifying both, the user can download the required shake map.
 */
public class USGSShakeMapDownloader {

	//static final String URLSTART = "http://earthquake.usgs.gov/earthquakes/shakemap";
	
	static final String URLSTART = "http://earthquake.usgs.gov/realtime/product/shakemap";
	static final String URLEND = "download/grid.xyz.zip";
	
	static final String URLTEST = "http://earthquake.usgs.gov/realtime/product/shakemap/nc73027396/nc/1528505370051/download/grid.xml.zip";
	
	//path to local directory where all the downloaded USGS shakemaps are stored
	static final String USGSDataPath = MainGUI.getCWD()+File.separator+"data/ShakeMapPlugin/More_USGS_Maps";
	
	//private String network; //the network that recorded the earthquake
	//private String quakeId; //the earthquake's id
	private String urlNumber;
	
	public USGSShakeMapDownloader(String shakeUrl){
		//this.network = network;
		//quakeId = id;
		urlNumber = shakeUrl;
	}
	
	/*
	 * Downloads the file from the USGS website and saves it
	 * in the data/ShakeMapPlugin directory
	 * -destinationFile: the filename of the new file
	 */
	public String downloadShakeMap(String destinationFile){
		String idName = "";
		try {
			//parse xml file
			URL url = new URL(urlNumber);
			InputStream stream = url.openStream();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(stream);
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("shakemap_grid");
			Node gridData = nList.item(0);
			System.out.println(gridData.getNodeName());
			Element eElement = (Element)gridData;
			
			//get id of earthquake and assign to name that appears on inde list
			String tempName = JOptionPane.showInputDialog(Info.getMainGUI(), "Input the name of this ShakeMap. \nPressing \"Cancel\" will default name to ID Name.");
			
			//If no name shows up, or if there are only whitespaces, we default the ID name. 
			if (tempName == null || tempName.length() ==0 || tempName.matches("\\s*")) {
				idName = eElement.getAttribute("shakemap_id");
			}
			else {
				tempName.trim();
				idName = tempName;
			}
			
			System.out.println("id: " + idName);
			
			String filePath = USGSDataPath + "/" + idName + ".txt";

			//prune unneeded information (only want first 5 of 8 'columns' of grid_data)
			FileWriter writ = new FileWriter(new File(filePath));
			String gridString = eElement.getElementsByTagName("grid_data").item(0).getTextContent();
			
			Scanner scanner = new Scanner(gridString);
			scanner.nextLine(); //skip first line
			while (scanner.hasNextLine()) //go line by line and write first 5 numbers of each line to file 
			{
				String[] line = scanner.nextLine().split("\\s+");
				writ.write(line[0] + " " + line[1] + " " + line[2] + " " +  line[3] + " " + line[4]);
				writ.write("\n");		
			}
			scanner.close();
			writ.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		//return header;
		return idName;
	}
}
