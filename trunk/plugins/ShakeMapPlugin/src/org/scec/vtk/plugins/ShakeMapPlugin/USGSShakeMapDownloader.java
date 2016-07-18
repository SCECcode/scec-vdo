package org.scec.vtk.plugins.ShakeMapPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.scec.vtk.main.Info;

/*
 * This class is responsible for loading shake map files from the USGS
 * website. The url consists of the network and the earthquake id. 
 * By specifying both, the user can the required shake map.
 */
public class USGSShakeMapDownloader {

	static final String URLSTART = "http://earthquake.usgs.gov/earthquakes/shakemap";
	static final String URLEND = "download/grid.xyz.zip";
	
	//path to local directory where all the shakemaps are stored
	static final String dataPath = Info.getMainGUI().getCWD()+File.separator+"data/ShakeMapPlugin"; 
	
	private String network; //the network that recorded the earthquake
	private String quakeId; //the earthquake's id
	
	public USGSShakeMapDownloader(String network, String id){
		this.network = network;
		quakeId = id;
	}
	
	/*
	 * Downloads the file from the USGS website and saves it
	 * in the data/ShakeMapPlugin directory
	 * -destinationFile: the filename of the new file
	 */
	public String downloadShakeMap(String destinationFile){
		String header = "";
		try {
			//url for usgs website
			URL usgs = new URL(URLSTART + "/" + network + "/shake/" + quakeId + "/" + URLEND);
			ZipInputStream zipIn = new ZipInputStream(usgs.openStream());
			ZipEntry entry = zipIn.getNextEntry();
			while(entry != null){
				String filePath = dataPath + "/" + destinationFile;

				FileWriter writ = new FileWriter(new File(filePath));
				Scanner sc = new Scanner(zipIn);
				header = sc.nextLine(); //skip the first line
				System.out.println(header); 
				while (sc.hasNextLine()) {
					writ.append(sc.nextLine() + "\n");
				}
				writ.close();
				
				entry = zipIn.getNextEntry();
			}
			zipIn.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return header;
	}
}
