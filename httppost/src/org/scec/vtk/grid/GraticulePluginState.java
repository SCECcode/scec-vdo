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
		
		Element propertyEl = stateEl.addElement("GraticuleGrid");
		propertyEl.addElement("gridChoice").addText(buttonSelection);
		propertyEl.addElement("gridWidth").addText( Double.toString(gridWidth));
		propertyEl.addElement("labels").addText( Boolean.toString(latLongLabelDisplay));
		propertyEl.addElement("display").addText(Boolean.toString(gridDisplay));
		propertyEl.addElement("gridColor").addText( Integer.toString(gridColor.getRGB()));
		propertyEl.addElement("backgroundColor").addText( Integer.toString(backgroundColor.getRGB()));
		propertyEl.addElement("lowLat").addText( Integer.toString(lowerLatitude));
		propertyEl.addElement("upLat").addText( Integer.toString(upperLatitude));
		propertyEl.addElement("lowLon").addText( Integer.toString(lowerLongitude));
		propertyEl.addElement("upLon").addText( Integer.toString(upperLongitude));
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
			buttonSelection = e.elementText("gridChoice");
			gridWidth = Double.parseDouble(e.elementText("gridWidth"));
			latLongLabelDisplay = Boolean.parseBoolean(e.elementText("labels"));
			gridDisplay = Boolean.parseBoolean(e.elementText("display"));
			gridColor = new Color(Integer.parseInt(e.elementText("gridColor")));
			backgroundColor = new Color(Integer.parseInt(e.elementText("backgroundColor")));
			lowerLatitude = Integer.parseInt(e.elementText("lowLat"));
			upperLatitude = Integer.parseInt(e.elementText("upLat"));
			lowerLongitude = Integer.parseInt(e.elementText("lowLon"));
			upperLongitude = Integer.parseInt(e.elementText("upLon"));
		}
	}

	@Override
	public PluginState deepCopy() {
		GraticulePluginState state = new GraticulePluginState(parent);
		state.copyLatestCatalogDetails();
		return state;
	}


}
