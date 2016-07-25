package org.scec.vtk.plugins.LegendPlugin.Component;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;
import org.scec.vtk.plugins.LegendPlugin.LegendPlugin;
import org.scec.vtk.plugins.LegendPlugin.LegendPluginGUI;
import org.scec.vtk.plugins.utils.components.ImageFileChooser;

import vtk.vtkActor2D;
import vtk.vtkColorTransferFunction;
import vtk.vtkImageMapper;
import vtk.vtkJPEGReader;
import vtk.vtkLookupTable;
import vtk.vtkProp;
import vtk.vtkScalarBarActor;
import vtk.vtkTextActor;

public class LegendModel {
	private static LegendPluginGUI parent;
//	FontDialog dialog = new FontDialog(parent);
	
	private static PluginActors legendActors;
	private static ArrayList<vtkActor2D> legends; // to keep track of indexing when removing actors
	private static LegendPlugin legendPlugin;
	
	public LegendModel() {
//		legendPlugin = new LegendPlugin();
//		legendActors = legendPlugin.getPluginActors();
		legendActors = new PluginActors();
		legends = new ArrayList<vtkActor2D>();
	}
	
	public LegendModel(LegendPluginGUI parent, PluginActors actors)
	{
		this.parent = parent;
		if (legendActors != null)
		{
			actors = legendActors;
		} else {
			legendActors = actors;
			legends = new ArrayList<vtkActor2D>();
		}
	}
	
	public PluginActors getLegendActors()
	{
		return legendActors;
	}
	
	public ArrayList<vtkActor2D> getLegends()
	{
		return legends;
	}
	
	public void addImageLegend() throws IOException, NullPointerException
	{
		ImageFileChooser fileChooser = new ImageFileChooser();
		File filename = null;
		BufferedImage image = null;
		int returnVal = fileChooser.showOpenDialog(parent);
		vtkJPEGReader imageReader = new vtkJPEGReader();
		
	    if(returnVal == JFileChooser.APPROVE_OPTION) 
	    	filename = fileChooser.getSelectedFile();
		
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
		
		if (imageReader != null)
		{
			vtkImageMapper  mapper = new vtkImageMapper();
            mapper.SetInputData(imageReader.GetOutput());
            mapper.SetColorWindow(256.0);
            mapper.SetColorLevel(128.0);
            vtkActor2D  actor2d = new vtkActor2D();
            actor2d.SetMapper(mapper);
            actor2d.SetPosition(400, 600);
            legendActors.addActor(actor2d);
            legends.add(actor2d);
		}
	}
	
	public void addText(String text)
	{
		if (text != null)
		{
			vtkTextActor textActor = new vtkTextActor();
			textActor.SetInput(text);
			textActor.SetPosition(300,300);
			textActor.GetTextProperty().SetFontSize(24);
			textActor.GetTextProperty().SetColor(1.0, 1.0, 1.0);
			legendActors.addActor(textActor);
			legends.add(textActor);
			addToParent(textActor.GetInput());
		}
	}
	
	public vtkScalarBarActor addScalarBar(CPT cpt, String title)
	{
		vtkScalarBarActor scalarBar = new vtkScalarBarActor();
		int minValue = (int)cpt.getMinValue();
		int maxValue = (int)cpt.getMaxValue();
		Color minColor = cpt.getMinColor();
		Color maxColor = cpt.getMaxColor();
		int numTicks = cpt.size()+1;
		
		scalarBar.SetTitle(title);
		scalarBar.SetNumberOfLabels(numTicks);
		
		
		vtkLookupTable hue = new vtkLookupTable();
		hue.SetTableRange(minValue, maxValue);
		hue.SetNumberOfColors(256);
		vtkColorTransferFunction ctf = new vtkColorTransferFunction();
		ctf.SetColorSpaceToRGB();
		for (int i=0; i<cpt.size(); i++)
		{
			CPTVal str = cpt.get(i);
			ctf.AddRGBPoint((float)i/(float)cpt.size(), str.minColor.getRed()/255.0, str.minColor.getGreen()/255.0, str.minColor.getBlue()/255.0);
		}
		ctf.AddRGBPoint(1, maxColor.getRed()/255.0, maxColor.getGreen()/255.0, maxColor.getBlue()/255.0);
		
		int tableSize = (int)maxValue;
		hue.Build();
		for (int i=0; i<256; i++)
		{
			double[] color=ctf.GetColor((double)i/256.0);
			hue.SetTableValue(i, color[0], color[1], color[2], 1);
		}
		
		scalarBar.SetLookupTable(hue);
		scalarBar.SetOrientationToHorizontal();
		scalarBar.SetWidth(0.4);
		scalarBar.SetHeight(0.07);
		scalarBar.SetPosition(0.05, 0.05);
		scalarBar.Modified();
		
		legendActors.addActor(scalarBar);
		legends.add(scalarBar);
		addToParent("Color Gradient");
		Info.getMainGUI().getRenderWindow().GetRenderer().AddActor2D(scalarBar);
		Info.getMainGUI().updateRenderWindow();
		
		return scalarBar;
	}
	
	public vtkScalarBarActor addEarthquakeScale(Color colorOne, Color colorTwo, EQCatalog eqc)
	{
		vtkScalarBarActor scalarBar = new vtkScalarBarActor();
		double minValue = (double)eqc.getMinMagnitude();
		double maxValue = (double)eqc.getMaxMagnitude();
		int numTicks = 4;
		String title = eqc.getDisplayName() + " Magnitude Range";
		scalarBar.SetTitle(title);
		scalarBar.SetNumberOfLabels(numTicks);
		
		
		vtkLookupTable hue = new vtkLookupTable();
		hue.SetTableRange(minValue, maxValue);
		hue.SetNumberOfColors(256);
		vtkColorTransferFunction ctf = new vtkColorTransferFunction();
		ctf.SetColorSpaceToRGB();
		for (int i=0; i<2; i++)
		{
			ctf.AddRGBPoint((float)i/(float)2, colorOne.getRed()/255.0, colorOne.getGreen()/255.0, colorOne.getBlue()/255.0);
		}
		ctf.AddRGBPoint(1, colorTwo.getRed()/255.0, colorTwo.getGreen()/255.0, colorTwo.getBlue()/255.0);
		
		hue.Build();
		for (int i=0; i<256; i++)
		{
			double[] color=ctf.GetColor((double)i/256.0);
			hue.SetTableValue(i, color[0], color[1], color[2], 1);
		}
		
		scalarBar.SetLookupTable(hue);
		scalarBar.SetOrientationToHorizontal();
		scalarBar.SetWidth(0.4);
		scalarBar.SetHeight(0.07);
		scalarBar.SetPosition(0.05, 0.05);
		scalarBar.Modified();
		
		legendActors.addActor(scalarBar);
		legends.add(scalarBar);
		addToParent("Color Gradient");
		Info.getMainGUI().getRenderWindow().GetRenderer().AddActor2D(scalarBar);
		Info.getMainGUI().updateRenderWindow();
		
		return scalarBar;
	}
	
	public void moveLegend(int index, double x, double y)
	{
		legends.get(index).SetPosition(legends.get(index).GetPosition()[0]+x, legends.get(index).GetPosition()[1]+y);
		legends.get(index).Modified();
	}
	
	public int getLegendVisibility(int index)
	{
		return legends.get(index).GetVisibility();
	}
	
	public void setLegendVisibility(int index, int visibility)
	{
		legends.get(index).SetVisibility(visibility);
		legends.get(index).Modified();
	}
	
	public void setLegendColor(int index, Color color)
	{
		legends.get(index).GetProperty().SetColor(color.getRed(), color.getGreen(), color.getBlue());
		legends.get(index).Modified();
	}
	
	public void addToParent(String text)
	{
		if (parent != null)
			parent.addToModel(text);
	}
	
	public void removeFromParent(int index)
	{
		if (parent != null)
			parent.removeFromModel(index);
	}
	
	public void removeLegendActor(vtkProp actor)
	{
		int index = legends.indexOf(actor);
		legendActors.removeActor(actor);
		legends.remove(actor);
		removeFromParent(index);
		Info.getMainGUI().getRenderWindow().GetRenderer().RemoveActor2D(actor);
		Info.getMainGUI().updateRenderWindow();
	}
	
	public void removeLegendActor(int index)
	{
		legendActors.removeActor(legends.get(index));
		legends.remove(index);
		removeFromParent(index);
	}
	
	public void removeLegendActors()
	{
		legendActors.clearActors();
		legends.clear();
		Info.getMainGUI().updateRenderWindow();
	}
}
