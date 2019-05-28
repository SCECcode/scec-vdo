package org.scec.vtk.grid;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * A GraticulePreset is a series of lat/lon points that coorespond to a grid
 * that can define a certain region (i.e. Southern California, Northern California,
 * etc...).
 * 
 * @author punihaol
 *
 */
public class GraticulePreset 
{
	private String name;
	private int lowerLatitude;
	private int upperLatitude;
	private int rightLongitude;
	private int leftLongitude;

	public GraticulePreset(int lowerLatitude, int upperLatitude, 
						   int lowerLongitude, int upperLongitude)
	{
		this.lowerLatitude = lowerLatitude;
		this.upperLatitude = upperLatitude;
		this.rightLongitude = lowerLongitude;
		this.leftLongitude = upperLongitude;
	}
	
	public GraticulePreset(File file)
	{
		load(file);
	}
	
	public GraticulePreset()
	{
		
	}
	
	/**
	 * Loads the lat/lon points from the file to the preset.
	 * 
	 * @param filename - a text file specifying the lat/lons of this preset
	 */
	public void load(File file)
	{
		try
		{	
			name = file.getName().replace(".grat", "");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			StringTokenizer tokens;
			
			while (reader.ready())
			{
				line = reader.readLine();
				
				if (line.contains("upper-latitude"))
				{
					tokens = new StringTokenizer(line,"=");
					tokens.nextToken();
					upperLatitude = new Integer(tokens.nextToken().trim()).intValue();
				}
				else if (line.contains("lower-latitude"))
				{
					tokens = new StringTokenizer(line,"=");
					tokens.nextToken();
					lowerLatitude = new Integer(tokens.nextToken().trim()).intValue();
				}
				else if (line.contains("right-longitude"))
				{
					tokens = new StringTokenizer(line,"=");
					tokens.nextToken();
					rightLongitude = new Integer(tokens.nextToken().trim()).intValue();
				}
				else if (line.contains("left-longitude"))
				{
					tokens = new StringTokenizer(line,"=");
					tokens.nextToken();
					leftLongitude = new Integer(tokens.nextToken().trim()).intValue();
				}
			}
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	public int getUpperLatitude()
	{
		return upperLatitude;
	}
	
	public int getLowerLatitude()
	{
		return lowerLatitude;
	}
	
	public int getRightLongitude()
	{
		return rightLongitude;
	}
	
	public int getLeftLongitude()
	{
		return leftLongitude;
	}
	
	public String getName()
	{
		return name;
	}
}

