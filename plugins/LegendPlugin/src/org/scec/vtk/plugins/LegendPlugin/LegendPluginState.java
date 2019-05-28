package org.scec.vtk.plugins.LegendPlugin;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JCheckBox;

import org.dom4j.Element;
import org.scec.vtk.commons.legend.LegendItem;
import org.scec.vtk.commons.legend.LegendUtils;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.ShakeMapPlugin.ShakeMapPluginState;
import org.scec.vtk.plugins.ShakeMapPlugin.Component.ShakeMap;

import vtk.vtkActor2D;
import vtk.vtkScalarBarActor;
import vtk.vtkTextActor;

public class LegendPluginState implements PluginState {

	private LegendPluginGUI parent;

	//Values in these arrays will be saved to xml file
	private ArrayList<String> imagePath;
	private ArrayList<double[]> coordinates;
	private ArrayList<String> text;
	private ArrayList<String> font;
	private ArrayList<Integer> fontSize;
	private ArrayList<double[]> color;
	private ArrayList<Integer> visibility;
	
	LegendPluginState(LegendPluginGUI parent)
	{
		this.parent = parent;
		imagePath = new ArrayList<String>();
		coordinates = new ArrayList<double[]>();
		visibility = new ArrayList<Integer>();
		text = new ArrayList<String>();
		font = new ArrayList<String>();
		fontSize = new ArrayList<Integer>();
		color = new ArrayList<double[]>();
	}
	
	//Gets the latest details. This function is called
	//in toXML()
	public void copyLatestCatalogDetails()
	{
		imagePath.clear();
		coordinates.clear();
		text.clear();
		font.clear();
		fontSize.clear();
		color.clear();
		visibility.clear();
		
		for(int i=0; i<parent.getLegendModel().size(); i++)
		{
			LegendItem legend = parent.getLegendModel().getElementAt(i);
			if(legend != null)
			{ 
				if (legend.getActor() instanceof vtkActor2D)
				{
					imagePath.add(legend.getImagePath());
				}
				else if (legend.getActor() instanceof vtkTextActor)
				{
					text.add(legend.getTitle());
					font.add(((vtkTextActor)legend.getActor()).GetTextProperty().GetFontFamilyAsString());
					fontSize.add(((vtkTextActor)legend.getActor()).GetTextProperty().GetFontSize());
					color.add(((vtkTextActor)legend.getActor()).GetTextProperty().GetColor());
				}
				visibility.add(legend.getActor().GetVisibility());
				coordinates.add(legend.getActor().GetPosition());
				
				
			}
			else
			{
				visibility.add(0);
				coordinates.add(null);
				imagePath.add(null);
				text.add(null);
				font.add(null);
				fontSize.add(null);
				color.add(null);
			}
		}
	}
	
	
	@Override
	public void load() 
	{
		for(int i=0; i<parent.getLegendModel().getSize(); i++)
		{	
			if(visibility.isEmpty())
				break;
			if(parent.getLegendModel().get(i) != null)
			{			
				if(i<visibility.size())
				{ 
					parent.getLegendModel().get(i).getActor().SetVisibility(visibility.get(i));
				} 
				else
				{
					parent.getLegendModel().get(i).getActor().SetVisibility(0);
				}
			}
		}
	}
	
	private void createElement(Element stateEl)
	{
		for(int i=0; i<parent.getLegendModel().getSize(); i++)
		{
			LegendItem legend = parent.getLegendModel().getElementAt(i);
			if(legend != null)
			{
				if (legend.getActor() instanceof vtkTextActor)
				{
					Element propertyEl = stateEl.addElement("TextLegend");
					propertyEl.addElement("legendName").addText(legend.toString());
					propertyEl.addElement("text").addText(((vtkTextActor)legend.getActor()).GetInput());
					propertyEl.addElement("font-family").addText(((vtkTextActor)legend.getActor()).GetTextProperty().GetFontFamilyAsString());
					propertyEl.addElement("font-size").addText("" + ((vtkTextActor)legend.getActor()).GetTextProperty().GetFontSize());
					propertyEl.addElement("colorR").addText("" + ((vtkTextActor)legend.getActor()).GetTextProperty().GetColor()[0]);
					propertyEl.addElement("colorG").addText("" + ((vtkTextActor)legend.getActor()).GetTextProperty().GetColor()[1]);
					propertyEl.addElement("colorB").addText("" + ((vtkTextActor)legend.getActor()).GetTextProperty().GetColor()[2]);
					propertyEl.addElement("x-coordinate").addText("" + legend.getActor().GetPosition()[0]);
					propertyEl.addElement("y-coordinate").addText("" + legend.getActor().GetPosition()[1]);
					propertyEl.addElement("visibility").addText("" + legend.getActor().GetVisibility());
				} 
				else if (legend.getActor() instanceof vtkActor2D)
				{
					//Check if legend has a filepath before writing information to XML
					if(legend.getImagePath() != null)
					{
						Element propertyEl = stateEl.addElement("ImageLegend");
						propertyEl.addElement("legendName").addText(legend.toString());
						propertyEl.addElement("imagePath").addText(legend.getImagePath());
						propertyEl.addElement("x-coordinate").addText("" + legend.getActor().GetPosition()[0]);
						propertyEl.addElement("y-coordinate").addText("" + legend.getActor().GetPosition()[1]);
						propertyEl.addElement("visibility").addText("" + legend.getActor().GetVisibility());
					}
				}
			}
		}
	}
	

	@Override
	public void toXML(Element stateEl) 
	{
		copyLatestCatalogDetails();
		createElement(stateEl);
	}

	public void showLegends()
	{
		int i = 0;
		//load text legends
		for(i = 0; i<text.size(); i++)
		{
			Color c = new Color((float)color.get(i)[0], (float)color.get(i)[1], (float)color.get(i)[2]); 
			LegendItem legendItem = LegendUtils.buildTextLegend(parent.getPlugin(), text.get(i), font.get(i), fontSize.get(i).intValue(), c, coordinates.get(i)[0], coordinates.get(i)[1]);
			parent.addLegend(legendItem);
		}
		for (int j = 0; j<imagePath.size(); j++)
		{
			File file = new File(imagePath.get(j));
			try {
				LegendItem legendItem = LegendUtils.buildImageLegend(parent.getPlugin(), file, coordinates.get(i)[0], coordinates.get(i)[1]);
				parent.addLegend(legendItem);
			} catch (IOException e) {
				e.printStackTrace();
			}
			i++;
		}
		Info.getMainGUI().updateRenderWindow();
	}
	
	@Override
	public void fromXML(Element stateEl) 
	{
		for ( Iterator i = stateEl.elementIterator( "TextLegend" ); i.hasNext(); ) 
		{
			Element e = (Element) i.next();
			text.add(e.elementText("text"));
			font.add(e.elementText("font-family"));
			fontSize.add(Integer.parseInt(e.elementText("font-size")));
			double [] textColor = new double[3];
			textColor[0] = Double.parseDouble(e.elementText("colorR"));
			textColor[1] = Double.parseDouble(e.elementText("colorG"));
			textColor[2] = Double.parseDouble(e.elementText("colorB"));
			color.add(textColor);
			double [] coord = new double[2];
			coord[0] = Double.parseDouble(e.elementText("x-coordinate"));
			coord[1] = Double.parseDouble(e.elementText("y-coordinate"));
			coordinates.add(coord);
			visibility.add(Integer.parseInt(e.elementText("visibility")));
		}
		
		for ( Iterator i = stateEl.elementIterator( "ImageLegend" ); i.hasNext();)
		{
			Element e = (Element) i.next();
			imagePath.add(e.elementText("imagePath"));
			double [] coord = new double[2];
			coord[0] = Double.parseDouble(e.elementText("x-coordinate"));
			coord[1] = Double.parseDouble(e.elementText("y-coordinate"));
			coordinates.add(coord);
			visibility.add(Integer.parseInt(e.elementText("visibility")));
		}
		showLegends();
	}

	@Override
	public PluginState deepCopy() {
		LegendPluginState state = new LegendPluginState(parent);
		state.copyLatestCatalogDetails();
		return state;
	}


}
