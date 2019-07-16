package org.scec.vtk.drawingTools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;

import vtk.vtkProp;

public class DrawingToolsPluginState implements PluginState {

	private DrawingToolsGUI parent;
	private ArrayList<DrawingTool> drawingTools;
	private ArrayList<Color> color1;
	ArrayList<Integer> visibility;
	private ArrayList<String> textString;
	private ArrayList<HashMap<String, String>> attributes;
	private ArrayList<String> dispName;

	DrawingToolsPluginState(DrawingToolsGUI parent)
	{
		this.parent = parent;
		drawingTools = new ArrayList<DrawingTool>();
		textString = new ArrayList<>();
		dispName = new ArrayList<>();
		color1 = new ArrayList<>();
		visibility = new ArrayList<>();
		attributes = new ArrayList<>();

	}
	private void copyLatestDetials() {
		drawingTools.clear();
		textString.clear();
		dispName.clear();
		color1.clear();
		visibility.clear();
		attributes.clear();

		for (int row =0;row<parent.getDrawingToolTable().getRowCount();row++)
		{

			DrawingTool dt = (DrawingTool) parent.getDrawingToolArray().get(row);
			drawingTools.add(dt);
			dispName.add(dt.getDisplayName());
			textString.add(dt.getTextString());
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
			System.out.println(visibility.get(row));
		}

	}
	@Override
	public void load() {
		int i=0;
		for (DrawingTool dr : drawingTools){
			dr.setDisplayName(dispName.get(i));
			parent.setText(dr,textString.get(i));
			dr.setAttributes(attributes.get(i));
			parent.setAttributes( dr,attributes.get(i));
			parent.setColor(dr, color1.get(i));
			parent.setVisibility(dr, visibility.get(i));
			i++;
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
		}
	}

	@Override
	public PluginState deepCopy() {
		DrawingToolsPluginState state = new DrawingToolsPluginState(parent);
		state.copyLatestDetials();
		return state;
	}
}
