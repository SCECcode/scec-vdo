package org.scec.vtk.drawingTools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ListSelectionModel;

import org.dom4j.Element;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.CommunityFaultModelPluginState;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.Fault3D;

import vtk.vtkProp;

public class DrawingToolsPluginState implements PluginState {

	private DrawingToolsGUI parent;
	private ArrayList<DrawingTool> drawingTool;
	private ArrayList<Color> color1;
	ArrayList<Integer> visibility;
	private ArrayList<String> dispName;
	private ArrayList<String> filePath;
	//TODO position 
	private ArrayList<Boolean> isHighway;
	private ArrayList<HashMap<String, String>> attributes;

	DrawingToolsPluginState(DrawingToolsGUI parent)
	{
		this.parent = parent;
		drawingTool = new ArrayList<DrawingTool>();
		dispName =new ArrayList<>();
		filePath =new ArrayList<>();
		color1 =new ArrayList<>();
		visibility =new ArrayList<>();
		isHighway = new ArrayList<>();
		attributes =new ArrayList<>();
	}
	private void copyLatestDetials() {
		drawingTool.clear();
		dispName.clear();
		filePath.clear();
		color1.clear();
		visibility.clear();
		isHighway.clear();
		for (int row =0;row<parent.getDrawingToolTable().getRowCount();row++)
		{
			
			DrawingTool dt = (DrawingTool) parent.getDrawingToolArray().get(row);
			drawingTool.add(dt);
			dispName.add(dt.getTextString());
			filePath.add(dt.getSourceFile());
			System.out.println(dt.getTextString());
			color1.add(dt.getColor());
			attributes.add(dt.getAttributes());
			visibility.add(((vtkProp) dt.getActorPin()).GetVisibility());
			isHighway.add(false);
			System.out.println(visibility.get(row));
		}
		
	}
	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toXML(Element stateEl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromXML(Element stateEl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PluginState deepCopy() {
		DrawingToolsPluginState state = new DrawingToolsPluginState(parent);
		state.copyLatestDetials();
		return state;
	}
	

}
