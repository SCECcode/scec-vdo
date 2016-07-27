package org.scec.vtk.grid;


import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.SurfacePlugin.Component.LoadedFilesProperties;


public class GraticulePluginState implements PluginState{

	private GraticuleGUI parent;
	private String buttonSelection = "";
	private double gridWidth = 0;
	private Color gridColor = Color.DARK_GRAY;
	private Color backgroundColor = Color.BLACK;
	private boolean gridDisplay = true;
	private int lowerLatitude = 0;
	private int upperLatitude = 0;
	private int lowerLongitude = 0;
	private int upperLongitude = 0;
	private boolean latLongLabelDisplay = true;

	GraticulePluginState(GraticuleGUI parent)
	{
		this.parent = parent;
	}

	void copyLatestCatalogDetails()
	{
		if(parent.getFirstSceneRadioButton().isSelected()){
			buttonSelection = "1.0 degrees";
		}else if(parent.getSecondSceneRadioButton().isSelected()){
			buttonSelection = "0.1 degrees";
		}else if(parent.getNoneRadioButton().isSelected()){
			buttonSelection = "No Grid";
		}else{
			buttonSelection = "Custom (degrees)";
		}
		gridWidth = parent.getGridWidth();
		latLongLabelDisplay = parent.isLabelsOn();
		gridDisplay = parent.getGridDisplayBool();
		gridColor = parent.getGridColor();
		backgroundColor = parent.getBackgroundColor();
		lowerLatitude = parent.getMinLat();
		upperLatitude = parent.getMaxLat();
		lowerLongitude = parent.getMinLon();
		upperLongitude = parent.getMaxLon();
	}

	@Override
	public void load() {
		showGrid(buttonSelection, gridWidth, latLongLabelDisplay, gridDisplay, gridColor, backgroundColor,
				lowerLatitude, upperLatitude, lowerLongitude, upperLongitude);
	}

	private void createElement(Element stateEl) {
		
		stateEl.addElement("GraticuleGrid")
		.addAttribute("gridChoice", buttonSelection)
		.addAttribute("gridWidth", Double.toString(gridWidth))
		.addAttribute("labels", Boolean.toString(latLongLabelDisplay))
		.addAttribute("display", Boolean.toString(gridDisplay))
		.addAttribute("gridColor", Integer.toString(gridColor.getRGB()))
		.addAttribute("backgroundColor", Integer.toString(backgroundColor.getRGB()))
		.addAttribute("lowLat", Integer.toString(lowerLatitude))
		.addAttribute("upLat", Integer.toString(upperLatitude))
		.addAttribute("lowLon", Integer.toString(lowerLongitude))
		.addAttribute("upLon", Integer.toString(upperLongitude));
	}
	
	public void showGrid(String buttonSelection, double gridWidth, boolean labelDisplay, boolean gridDisplay, Color gridColor, 
			Color backgroundColor, int lowerLatitude, int upperLatitude, int lowerLongitude, int upperLongitude){
		
		ArrayList<GlobeBox> gb = parent.makeNewGrid(upperLatitude, lowerLatitude, lowerLongitude, upperLongitude, gridWidth);
		parent.makeGrids(gb, labelDisplay);
		parent.setGridColor2(gridColor);
		parent.setBackgroundColor(backgroundColor);
		parent.graphVisibilityOn();
		if(gridDisplay == false){
			parent.graphVisibilityOff();
		}
		parent.getCustomTextField().setEnabled(false);
		if(buttonSelection.equals("1.0 degrees")){
			parent.getFirstSceneRadioButton().setSelected(true);
		}else if(buttonSelection.equals("0.1 degrees")){
			parent.getSecondSceneRadioButton().setSelected(true);
		}else if(buttonSelection.equals("No Grid")){
			parent.getNoneRadioButton().setSelected(true);
		}else{
			parent.getCustomRadioButton().setSelected(true);
			parent.getCustomTextField().setEnabled(true);
			parent.getCustomTextField().setText(Double.toString(gridWidth));
		}
		parent.setMaxLatField(upperLatitude);
		parent.setMinLatField(lowerLatitude);
		parent.setMaxLonField(upperLongitude);
		parent.setMinLonField(lowerLongitude);
		parent.setLabelsDisplayed(labelDisplay);
		parent.setGridWidth(gridWidth);
		Info.getMainGUI().updateRenderWindow();
	}

	@Override
	public void toXML(Element stateEl) {
		copyLatestCatalogDetails();
		createElement(stateEl);
	}

	@Override
	public void fromXML(Element stateEl) {
		for ( Iterator i = stateEl.elementIterator( "GraticuleGrid" ); i.hasNext(); ) 
		{
			Element e = (Element) i.next();
			buttonSelection = e.attributeValue("gridChoice");
			gridWidth = Double.parseDouble(e.attributeValue("gridWidth"));
			latLongLabelDisplay = Boolean.parseBoolean(e.attributeValue("labels"));
			gridDisplay = Boolean.parseBoolean(e.attributeValue("display"));
			gridColor = new Color(Integer.parseInt(e.attributeValue("gridColor")));
			backgroundColor = new Color(Integer.parseInt(e.attributeValue("backgroundColor")));
			lowerLatitude = Integer.parseInt(e.attributeValue("lowLat"));
			upperLatitude = Integer.parseInt(e.attributeValue("upLat"));
			lowerLongitude = Integer.parseInt(e.attributeValue("lowLon"));
			upperLongitude = Integer.parseInt(e.attributeValue("upLon"));
		}
	}

	@Override
	public PluginState deepCopy() {
		GraticulePluginState state = new GraticulePluginState(parent);
		state.copyLatestCatalogDetails();
		return state;
	}


}
