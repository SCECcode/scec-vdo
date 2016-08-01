package org.scec.vtk.drawingTools;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ListSelectionModel;

import org.dom4j.Element;
import org.scec.vtk.drawingTools.DefaultLocationsGUI.PresetLocationGroup;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.CommunityFaultModelPluginState;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.Fault3D;

import vtk.vtkActor;
import vtk.vtkProp;

public class DrawingToolsPluginState implements PluginState {

	private DrawingToolsGUI parent;
	private DefaultLocationsGUI presets;
	private ArrayList<DrawingTool> drawingTools;
	private ArrayList<Color> color1;
	ArrayList<Integer> visibility;
	private ArrayList<String> dispName;
	private ArrayList<String> countyFilePath;
	//TODO position 
	//private ArrayList<Boolean> isHighway;
	private ArrayList<HashMap<String, String>> attributes;
	private ArrayList<DrawingTool> highwayTools;
	private ArrayList<String> highwayDispName;
	private ArrayList<Color> highwayColor1;
	private ArrayList<Integer> highwayVisibility;
	private ArrayList<HashMap<String, String>> highwayAttributes;
	private  ArrayList<String> highwayFilePath;
	private boolean dt;

	DrawingToolsPluginState(DrawingToolsGUI parent)
	{
		this.parent = parent;
		presets = new DefaultLocationsGUI(parent);
		drawingTools = new ArrayList<DrawingTool>();
		dispName =new ArrayList<>();
		countyFilePath =new ArrayList<>();
		color1 =new ArrayList<>();
		visibility =new ArrayList<>();
		attributes =new ArrayList<>();

		highwayTools = new ArrayList<DrawingTool>();
		highwayDispName =new ArrayList<>();
		highwayColor1 =new ArrayList<>();
		highwayVisibility =new ArrayList<>();
		highwayAttributes =new ArrayList<>();
		highwayFilePath =new ArrayList<>();
	}
	private void copyLatestDetials() {
		drawingTools.clear();
		dispName.clear();
		//filePath.clear();
		color1.clear();
		visibility.clear();
		//	isHighway.clear();
		attributes.clear();
		for (int row =0;row<parent.getDrawingToolTable().getRowCount();row++)
		{

			DrawingTool dt = (DrawingTool) parent.getDrawingToolArray().get(row);
			drawingTools.add(dt);
			dispName.add(dt.getTextString());
			countyFilePath.add(dt.getSourceFile());
			System.out.println(dt.getTextString());
			color1.add(dt.getColor());
			System.out.println(dt.getTextString());
			HashMap<String,String> locData = new HashMap<String, String>();
			locData.put("Lat", dt.getAttributes().get("Lat")); 
			locData.put("Lon", dt.getAttributes().get("Lon"));
			locData.put("Alt", dt.getAttributes().get("Alt"));
			locData.put("pinH", dt.getAttributes().get("pinH"));
			locData.put("pinR", dt.getAttributes().get("pinR"));
			locData.put("fontSize", dt.getAttributes().get("fontSize"));
			attributes.add(locData);
			System.out.println(locData.get("Lon"));
			visibility.add(((vtkProp) dt.getActorPin()).GetVisibility());
			//isHighway.add(false);
			System.out.println(visibility.get(row));
		}

		//highways
		highwayTools.clear();
		highwayDispName.clear();
		highwayColor1.clear();
		highwayVisibility.clear();
		highwayAttributes.clear();
		highwayFilePath.clear();
		for (int row =0;row<parent.getHighwayToolTable().getRowCount();row++)
		{

			DrawingTool dh = (DrawingTool) parent.getHighwayToolsArray().get(row);
			highwayTools.add(dh);
			highwayDispName.add(dh.getTextString());
			highwayFilePath.add(dh.getSourceFile());
			System.out.println(dh.getTextString());
			highwayColor1.add(dh.getColor());
			System.out.println(dh.getTextString());
			HashMap<String,String> locData = new HashMap<String, String>();
			locData.put("Lat", dh.getAttributes().get("Lat")); 
			locData.put("Lon", dh.getAttributes().get("Lon"));
			locData.put("Alt", dh.getAttributes().get("Alt"));
			highwayAttributes.add(locData);
			System.out.println(locData.get("Lon"));
			highwayVisibility.add(((vtkProp) dh.getActorPin()).GetVisibility());
			//isHighway.add(false);
			System.out.println(highwayVisibility.get(row));
		}
		dt = parent.getDefaultLocation().dt;

	}
	@Override
	public void load() {
		// TODO Auto-generated method stub
		int i=0;
		//parent.clearTable();
		for (DrawingTool dr : drawingTools)
		{
			//DrawingTool drawingToolObj = parent.addDrawingTool(dr, dispName.get(i));
			//parent.getDrawingToolTable().addDrawingTool(drawingToolObj);
			dr.setDisplayName(dispName.get(i));
			parent.setText(dr,dispName.get(i));
			dr.setAttributes(attributes.get(i));
			parent.setAttributes( dr,attributes.get(i));
			parent.setColor(dr, color1.get(i));
			parent.setVisibility(dr, visibility.get(i));
			i++;
		}
//		parent.getDefaultLocation().loadCounties(dt);
		i=0;
		for (DrawingTool dr : highwayTools)
		{
			dr.setDisplayName(highwayDispName.get(i));
			dr.setAttributes(highwayAttributes.get(i));
			dr.setSourceFile(highwayFilePath.get(i));
			//parent.setAttributes( dr,highwayAttributes.get(i));
			parent.setColor(dr, highwayColor1.get(i));
			parent.setVisibility(dr, highwayVisibility.get(i));
			System.out.println(dr.getAttributes().get("Lon"));
			i++;
		}
		Info.getMainGUI().updateRenderWindow();
	}

	private void createElement(Element stateEl) {
		int i=0;
		for (DrawingTool eqc : drawingTools)
		{
			stateEl.addElement( "DrawingTool" )
			.addAttribute( "dispName", dispName.get(i))
			.addAttribute( "type", "Text")
			.addAttribute( "color1", Integer.toString(color1.get(i).getRGB()))
			.addAttribute( "Lat",attributes.get(i).get("Lat"))
			.addAttribute( "Lon",attributes.get(i).get("Lon"))
			.addAttribute( "Alt",attributes.get(i).get("Alt"))
			.addAttribute( "pinH",attributes.get(i).get("pinH"))
			.addAttribute( "pinR",attributes.get(i).get("pinR"))
			.addAttribute( "fontSize",attributes.get(i).get("fontSize"))
			.addAttribute( "visibility",(visibility.get(i).toString()));
			i++;
		}
		//highway
		i=0;
		for (DrawingTool eqc : highwayTools)
		{
			stateEl.addElement( "DrawingTool" )
			.addAttribute( "dispName", highwayDispName.get(i))
			.addAttribute( "type", "Highway")
			.addAttribute( "filepath", highwayFilePath.get(i))
			.addAttribute( "color1", Integer.toString(highwayColor1.get(i).getRGB()))
			.addAttribute( "Lat",highwayAttributes.get(i).get("Lat"))
			.addAttribute( "Lon",highwayAttributes.get(i).get("Lon"))
			.addAttribute( "Alt",highwayAttributes.get(i).get("Alt"))
			.addAttribute( "visibility",(highwayVisibility.get(i).toString()));
			i++;
		}
	}

	@Override
	public void toXML(Element stateEl) {
		copyLatestDetials();
		createElement(stateEl);
	}

	@Override
	public void fromXML(Element stateEl) {
		//clear any previous data
		drawingTools.clear();
		dispName.clear();
		color1.clear();
		visibility.clear();
		attributes.clear();
		for ( Iterator i = stateEl.elementIterator( "DrawingTool" ); i.hasNext(); ) {
			Element e = (Element) i.next();
			if(e.attributeValue("type").contains("Text"))
			{
				dispName.add(e.attributeValue("dispName"));
				color1.add(Color.decode(e.attributeValue("color1")));
				visibility.add(Integer.parseInt(e.attributeValue("visibility")));
				HashMap<String,String> locData = new HashMap<String, String>();
				locData.put("Lat", e.attributeValue("Lat")); 
				locData.put("Lon", e.attributeValue("Lon"));
				locData.put("Alt", e.attributeValue("Alt"));
				locData.put("pinH", e.attributeValue("pinH"));
				locData.put("pinR", e.attributeValue("pinR"));
				locData.put("fontSize", e.attributeValue("fontSize"));
				attributes.add(locData);
				System.out.println(e.attributeValue("type"));
				DrawingTool drawingToolObj = parent.addDrawingTool(new DrawingTool(), e.attributeValue("dispName"));
				parent.getDrawingToolTable().addDrawingTool(drawingToolObj);
				drawingTools.add(drawingToolObj);
			}
			else 
				if(e.attributeValue("type").contains("Highway"))
				{
					highwayDispName.add(e.attributeValue("dispName"));
					highwayColor1.add(Color.decode(e.attributeValue("color1")));
					highwayVisibility.add(Integer.parseInt(e.attributeValue("visibility")));
					highwayFilePath.add(e.attributeValue("filepath"));
					HashMap<String,String> locData = new HashMap<String, String>();
					locData.put("Lat", e.attributeValue("Lat")); 
					locData.put("Lon", e.attributeValue("Lon"));
					locData.put("Alt", e.attributeValue("Alt"));
					highwayAttributes.add(locData);
					System.out.println(e.attributeValue("type"));
//					DrawingTool drawingToolObj =new DrawingTool(
//							Double.parseDouble(locData.get("Lat")),
//							Double.parseDouble(locData.get("Lon")),
//							Double.parseDouble(locData.get("Alt")),
//							e.attributeValue("dispName"),
//							null,
//							Color.WHITE,
//							new vtkActor(),
//							null
//						); 
//					parent.addHighways(drawingToolObj);
//					parent.getHighwayToolTable().addDrawingTool(drawingToolObj);
//					highwayTools.add(drawingToolObj);
					parent.getDefaultLocation().setSelectedInputFile(highwayFilePath.get(0));
					parent.getDefaultLocation().loadHighways();
				}

		}
	}

	@Override
	public PluginState deepCopy() {
		DrawingToolsPluginState state = new DrawingToolsPluginState(parent);
		state.copyLatestDetials();
		return state;
	}


}
