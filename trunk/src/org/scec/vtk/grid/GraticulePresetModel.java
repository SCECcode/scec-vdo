package org.scec.vtk.grid;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import org.scec.vtk.main.MainGUI;

public class GraticulePresetModel 
{
	private Vector<GraticulePreset> grats;
	
	public GraticulePresetModel()
	{
		grats = new Vector<GraticulePreset>();
		load();
	}
	
	private void load()
	{
		//System.out.println(GraticuleGUI.class.getResource("resources/").getPath());
//		File dir = new File(GraticuleGUI.class.getResource("resources/").getPath());
		File dir = null;
		try {
			dir = new File(GraticuleGUI.class.getResource("resources/").toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File[] files = dir.listFiles();
		
		for (File f : files)
		{
			if (f.getName().endsWith(".grat"))
			{
				GraticulePreset preset = new GraticulePreset(f);
				grats.add(preset);
			}
		}
	}
	
	public GraticulePreset getPreset(int i)
	{
		return grats.get(i);
	}
	
	public String[] getAllNames()
	{
		String[] names = new String[grats.size()];
		
		for (int i = 0; i < names.length; i++)
		{
			names[i] = grats.get(i).getName();
		}
		
		return names;
	}
}
