package org.scec.vtk.plugins.LegendPlugin.Component;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.scec.vtk.plugins.LegendPlugin.LegendPluginGUI;
import org.scec.vtk.plugins.utils.components.ImageFileChooser;

import vtk.vtkJPEGReader;

public class LegendModel {
	private LegendPluginGUI parent;
//	FontDialog dialog = new FontDialog(parent);
	
	public LegendModel(LegendPluginGUI parent)
	{
		this.parent = parent;
	}
	
	public vtkJPEGReader addImageLegend() throws IOException, NullPointerException
	{
		ImageFileChooser fileChooser = new ImageFileChooser();
		File filename = null;
		BufferedImage image = null;
		int returnVal = fileChooser.showOpenDialog(parent);
		vtkJPEGReader imageReader = new vtkJPEGReader();
		
	    if(returnVal == JFileChooser.APPROVE_OPTION) 
	    	filename = fileChooser.getSelectedFile();
	    else
	    	return null;
		
		try
		{
			image = ImageIO.read(filename);
		}
		catch(IOException e)
		{
			throw new IOException();
		}
		
		try
		{
			
			imageReader.SetFileName(filename.getAbsolutePath());
			imageReader.Update();			
		}
		catch(NullPointerException e)
		{
			throw new NullPointerException();
		}
		
		return imageReader;
	}
}
