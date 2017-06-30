package org.scec.vtk.drawingTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.scec.vtk.drawingTools.DefaultLocationsGUI.PresetLocationGroup;
import org.scec.vtk.main.Info;

public class SelectLocations {
	private static String dataPath = Info.getMainGUI().getRootPluginDir()+File.separator+"GISLocationPlugin"+File.separator;
	ArrayList<PresetLocationGroup> presetLocationGroups = new ArrayList<PresetLocationGroup>();
	private String selectedInputFile;
	private DisplayAttributes displayAttributes;
	ArrayList<String> citypop = new ArrayList<String>();
	
	public SelectLocations() {
		parseTitles(dataPath);
	}
	private void parseTitles(String dataPath) {
		File dataDirectory = new File(dataPath);
		if (dataDirectory.isDirectory()) {
			// List files in the directory and process each
			File files[] = dataDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile() && files[i].getName().endsWith(".shp") || 
						files[i].getName().endsWith(".txt") 
						&& !files[i].getName().equals("CA_Counties.txt") 
						&& !files[i].getName().contains("popdensity.txt") 
						&& !files[i].getName().contains("CA_Cities")) {
					PresetLocationGroup group = new PresetLocationGroup();
					group.file = files[i];
					String tempName = files[i].getName();
					tempName = tempName.substring(0, tempName.lastIndexOf("."));
					tempName = tempName.replace('_', ' ');
					group.name = tempName;
					presetLocationGroups.add(group);
					//System.out.println(group.name);
				}
			}
		}
	}
	private class PresetLocationGroup {
		public Vector<DrawingTool> locations = null;
		public String name			= null;
		public File file			= null;
		public JCheckBox checkbox	= null;
	}
}
