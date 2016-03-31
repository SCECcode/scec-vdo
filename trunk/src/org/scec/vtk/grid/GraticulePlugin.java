package org.scec.vtk.grid;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.jdom.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.ActionPlugin;

import vtk.vtkActor;


/**
 * This plugin displays the color-coded basin depths under Southern California.
 * 
 * Status: functional with problems Comments:
 * <ul>
 * <li>gui doesn't work right</li>
 * <li>color-coding by depth</li>
 * <li>'work' code should be in separate class from base plugin class</li>
 * <li>perhaps this should be rolled into a more generic 3D surface plugin</li>
 * </ul>
 * 
 * Created on May 4, 2005
 * 
 * @author Tommy Morbitzer
 * @author Bonnie Gurry
 * 
 * @version $Id: GraticulePlugin.java 4873 2014-07-25 15:28:52Z sellsted $
 */
public class GraticulePlugin extends ActionPlugin {
	GraticuleGUI gratPanel;

	public GraticulePlugin() {
		// this.metadata = new PluginInfo("Grids", "Grids", "David & Genia",
		// "1.0");
	}

	public JPanel createGUI() {
		this.gratPanel = new GraticuleGUI(this);
		return this.gratPanel;
	}

	public GraticuleGUI getGraticuleGUI() {
		return gratPanel;
	}

	public Element getState() {
		Element root = new Element("graticulePlugin");
		Element graticule = new Element("Graticule");
		graticule.setAttribute("on", "true");
		/*if (Info.getMainGUI().getGridDisplayBool()) {
			graticule.setAttribute("on", "true");
		} else {
			graticule.setAttribute("on", "false");
		}*/
		graticule.setAttribute("upperLat",
				String.valueOf(gratPanel.upperLatitude));
		graticule.setAttribute("lowerLat",
				String.valueOf(gratPanel.lowerLatitude));
		graticule.setAttribute("upperLon",
				String.valueOf(gratPanel.upperLongitude));
		graticule.setAttribute("lowerLon",
				String.valueOf(gratPanel.lowerLongitude));
		System.out.println("tits");
		String gridWidth = String.valueOf(gratPanel.getGridWidth());
		if (gratPanel.noneRadioButton.isSelected()) {
			gridWidth = "0.0";
		}
		 graticule.setAttribute("gridWidth", gridWidth);
		root.addContent(graticule);
		return root;
	}

	public void setState(Element e) {
		Element root = e.getChild("graticulePlugin");
		Element graticule = root.getChild("Graticule");
		if (graticule.getAttributeValue("on").equals("true")) {
		}
		if (Double.parseDouble(graticule.getAttributeValue("gridWidth")) == 1.0) {
			gratPanel.firstsceneRadioButton.setSelected(true);
		} else if (Double.parseDouble(graticule.getAttributeValue("gridWidth")) == 0.1) {
			gratPanel.secondsceneRadioButton.setSelected(true);
		}
		// else if(Double.parseDouble(graticule.getAttributeValue("gridWidth"))
		// == 0.75)
		// {
		// gratPanel.quadrangleRadioButton.setSelected(true);
		// }
		else if (Double.parseDouble(graticule.getAttributeValue("gridWidth")) == 0.0) {
			gratPanel.noneRadioButton.setSelected(true);
		} else {
			gratPanel.customRadioButton.setSelected(true);
			gratPanel.customTextBox.setText(graticule
					.getAttributeValue("gridWidth"));
		}
		int upperLat, lowerLat, upperLon, lowerLon;
		upperLat = Integer.parseInt(graticule.getAttributeValue("upperLat"));
		lowerLat = Integer.parseInt(graticule.getAttributeValue("lowerLat"));
		upperLon = Integer.parseInt(graticule.getAttributeValue("upperLon"));
		lowerLon = Integer.parseInt(graticule.getAttributeValue("lowerLon"));

		if (!graticule.getAttributeValue("gridWidth").equals("0.0")) {
			gratPanel
					.makeNewGrid(upperLat, lowerLat, lowerLon, upperLon, Double
							.parseDouble(graticule
									.getAttributeValue("gridWidth")));
		}
		// ActionEvent e1 = new ActionEvent(gratPanel.graticuleappsProp_apply,
		// 0, "apply");
		// gratPanel.actionPerformed(e1);
		root.removeChild("Graticule");
	}

	public ArrayList<vtkActor> getActors() {
		// TODO Auto-generated method stub
		return null;
	}

}
