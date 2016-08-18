package org.scec.vtk.drawingTools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;

import oracle.net.jdbc.TNSAddress.AddressList;
import vtk.vtkProp;

public class DrawingToolsPluginState implements PluginState {

	private DrawingToolsGUI parent;
	private DefaultLocationsGUI presets;
	private ArrayList<DrawingTool> drawingTools;
	private ArrayList<Color> color1;
	ArrayList<Integer> visibility;
	private ArrayList<String> textString;
	private ArrayList<String> countyFilePath;
	//TODO position 
	//private ArrayList<Boolean> isHighway;
	private ArrayList<HashMap<String, String>> attributes;
	private ArrayList<DrawingTool> highwayTools;
	private ArrayList<String> highwayTextString;
	private ArrayList<Color> highwayColor1;
	private ArrayList<Integer> highwayVisibility;
	private ArrayList<HashMap<String, String>> highwayAttributes;
	private  ArrayList<String> highwayFilePath;
	private boolean dt;
	private ArrayList<String> dispName;
	private ArrayList<String> highwayDispName;

	DrawingToolsPluginState(DrawingToolsGUI parent)
	{
		this.parent = parent;
		presets = new DefaultLocationsGUI(parent);
		drawingTools = new ArrayList<DrawingTool>();
		textString =new ArrayList<>();
		dispName =new ArrayList<>();
		countyFilePath =new ArrayList<>();
		color1 =new ArrayList<>();
		visibility =new ArrayList<>();
		attributes =new ArrayList<>();

		highwayTools = new ArrayList<DrawingTool>();
		highwayDispName =new ArrayList<>();
		highwayTextString =new ArrayList<>();
		highwayColor1 =new ArrayList<>();
		highwayVisibility =new ArrayList<>();
		highwayAttributes =new ArrayList<>();
		highwayFilePath =new ArrayList<>();
	}
	private void copyLatestDetials() {
		drawingTools.clear();
		textString.clear();
		dispName.clear();
		color1.clear();
		visibility.clear();
		//	isHighway.clear();
		attributes.clear();

		for (int row =0;row<parent.getDrawingToolTable().getRowCount();row++)
		{

			DrawingTool dt = (DrawingTool) parent.getDrawingToolArray().get(row);
			drawingTools.add(dt);
			dispName.add(dt.getDisplayName());
			textString.add(dt.getTextString());
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
		highwayTextString.clear();
		highwayColor1.clear();
		highwayVisibility.clear();
		highwayAttributes.clear();
		highwayFilePath.clear();
		highwayDispName.clear();
		for (int row =0;row<parent.getHighwayToolTable().getRowCount();row++)
		{

			DrawingTool dh = (DrawingTool) parent.getHighwayToolsArray().get(row);
			highwayTools.add(dh);
			highwayDispName.add(dh.getDisplayName());
			highwayTextString.add(dh.getTextString());
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
			parent.setText(dr,textString.get(i));
			dr.setAttributes(attributes.get(i));
			parent.setAttributes( dr,attributes.get(i));
			parent.setColor(dr, color1.get(i));
			parent.setVisibility(dr, visibility.get(i));
			i++;
		}
		//		parent.getDefaultLocation().loadCounties(dt);
		
		for (i=0;i<parent.getHighwayToolsArray().size();i++)
		{
			DrawingTool dr = parent.getHighwayToolsArray().get(i);
			dr.setDisplayName(highwayDispName.get(i));
            dr.setAttributes(highwayAttributes.get(i));
			dr.setSourceFile(highwayFilePath.get(i));
			//parent.setAttributes( dr,highwayAttributes.get(i));
			parent.setColor(dr, highwayColor1.get(i));
			parent.setVisibility(dr, highwayVisibility.get(i));
			System.out.println(dr.getAttributes().get("Lon"));
			
		}
		Info.getMainGUI().updateRenderWindow();
	}

	private void createElement(Element stateEl) {
		int i=0;
		for (DrawingTool eqc : drawingTools)
		{
			Element propertyEl = stateEl.addElement( "DrawingTool" );
			propertyEl.addElement( "dispName").addText(dispName.get(i));
			propertyEl.addElement( "textString").addText( textString.get(i));
			propertyEl.addElement( "type").addText( "Text");
			propertyEl.addElement( "color1").addText( Integer.toString(color1.get(i).getRGB()));
			propertyEl.addElement( "Lat").addText(attributes.get(i).get("Lat"));
			propertyEl.addElement( "Lon").addText(attributes.get(i).get("Lon"));
			propertyEl.addElement( "Alt").addText(attributes.get(i).get("Alt"));
			propertyEl.addElement( "pinH").addText(attributes.get(i).get("pinH"));
			propertyEl.addElement( "pinR").addText(attributes.get(i).get("pinR"));
			propertyEl.addElement( "fontSize").addText(attributes.get(i).get("fontSize"));
			propertyEl.addElement( "visibility").addText((visibility.get(i).toString()));
			i++;
		}
		//highway
		i=0;
		for (DrawingTool eqc : highwayTools)
		{
			Element propertyEl = stateEl.addElement( "DrawingTool" );
			propertyEl.addElement( "dispName").addText( highwayDispName.get(i));
			propertyEl.addElement( "textString").addText( highwayTextString.get(i));
			propertyEl.addElement( "type").addText( "Highway");
			propertyEl.addElement( "filepath").addText( highwayFilePath.get(i));
			propertyEl.addElement( "color1").addText( Integer.toString(highwayColor1.get(i).getRGB()));
			propertyEl.addElement( "Lat").addText(highwayAttributes.get(i).get("Lat"));
			propertyEl.addElement( "Lon").addText(highwayAttributes.get(i).get("Lon"));
			propertyEl.addElement( "Alt").addText(highwayAttributes.get(i).get("Alt"));
			propertyEl.addElement( "visibility").addText((highwayVisibility.get(i).toString()));
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
		textString.clear();
		color1.clear();
		visibility.clear();
		attributes.clear();
		//highways
		highwayTools.clear();
		highwayTextString.clear();
		highwayColor1.clear();
		highwayVisibility.clear();
		highwayAttributes.clear();
		highwayFilePath.clear();
		highwayDispName.clear();
		int found;
		for ( Iterator i = stateEl.elementIterator( "DrawingTool" ); i.hasNext(); ) {
			Element e = (Element) i.next();
			if(e.elementText("type").contains("Text"))
			{
				dispName.add(e.elementText("dispName"));
				textString.add(e.elementText("textString"));
				color1.add(Color.decode(e.elementText("color1")));
				visibility.add(Integer.parseInt(e.elementText("visibility")));
				HashMap<String,String> locData = new HashMap<String, String>();
				locData.put("Lat", e.elementText("Lat")); 
				locData.put("Lon", e.elementText("Lon"));
				locData.put("Alt", e.elementText("Alt"));
				locData.put("pinH", e.elementText("pinH"));
				locData.put("pinR", e.elementText("pinR"));
				locData.put("fontSize", e.elementText("fontSize"));
				attributes.add(locData);
				System.out.println(e.elementText("type"));
				found=0;
				for(int k = 0;k<parent.getDrawingToolArray().size();k++)
				{
					if(parent.getDrawingToolArray().get(k).getDisplayName().equalsIgnoreCase(e.elementText("dispName")))
					{
						found=1;
						drawingTools.add(parent.getDrawingToolArray().get(k));
					}
				}
				if(found==0){
					DrawingTool drawingToolObj = parent.addDrawingTool(new DrawingTool(), e.elementText("textString"));
					parent.getDrawingToolTable().addDrawingTool(drawingToolObj);
					drawingTools.add(drawingToolObj);
				}
			}
			else 
				if(e.elementText("type").contains("Highway"))
				{
					highwayDispName.add(e.elementText("dispName"));
					highwayTextString.add(e.elementText("textString"));
					highwayColor1.add(Color.decode(e.elementText("color1")));
					highwayVisibility.add(Integer.parseInt(e.elementText("visibility")));
					highwayFilePath.add(e.elementText("filepath"));
					HashMap<String,String> locData = new HashMap<String, String>();
					locData.put("Lat", e.elementText("Lat")); 
					locData.put("Lon", e.elementText("Lon"));
					locData.put("Alt", e.elementText("Alt"));
					highwayAttributes.add(locData);
					System.out.println(e.elementText("type"));
					parent.getDefaultLocation().setSelectedInputFile(highwayFilePath.get(0));
					if(!parent.getDefaultLocation().presetLocationGroups.get(0).checkbox.isSelected())
					{
						parent.getDefaultLocation().loadHighways();
					}
					
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
