package org.scec.vtk.grid;

import javax.swing.JPanel;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.plugins.ShakeMapPlugin.ShakeMapPluginState;


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
public class GraticulePlugin extends ActionPlugin implements StatefulPlugin{
	
	GraticuleGUI gratPanel;
	private PluginState state;
	
	public GraticulePlugin() {
		// this.metadata = new PluginInfo("Grids", "Grids", "David & Genia",
		// "1.0");
	}

	public JPanel createGUI() {
		Info.getMainGUI().setViewRange(new ViewRange());
		//draw Grid
		GraticulePreset preset = GraticuleGUI.getGraticlePreset();
		Info.getMainGUI().setViewRange(new ViewRange(preset.getLowerLatitude(), preset.getUpperLatitude(), preset.getLeftLongitude(), preset.getRightLongitude()));
		
		this.gratPanel = new GraticuleGUI(this.getPluginActors());
		gratPanel.makeGrids(gratPanel.getGlobeBox(preset, 1.0),true);
		return this.gratPanel;
	}

	public GraticuleGUI getGraticuleGUI() {
		return gratPanel;
	}

	@Override
	public PluginState getState() {
		// TODO Auto-generated method stub
		if(state==null)
			state = new GraticulePluginState(this.gratPanel);
		return state;
	}

	// TODO
//	public Element getState() {
//		Element root = new Element("graticulePlugin");
//		Element graticule = new Element("Graticule");
//		graticule.setAttribute("on", "true");
//		/*if (Info.getMainGUI().getGridDisplayBool()) {
//			graticule.setAttribute("on", "true");
//		} else {
//			graticule.setAttribute("on", "false");
//		}*/
//		graticule.setAttribute("upperLat",
//				String.valueOf(gratPanel.upperLatitude));
//		graticule.setAttribute("lowerLat",
//				String.valueOf(gratPanel.lowerLatitude));
//		graticule.setAttribute("upperLon",
//				String.valueOf(gratPanel.upperLongitude));
//		graticule.setAttribute("lowerLon",
//				String.valueOf(gratPanel.lowerLongitude));
//		String gridWidth = String.valueOf(gratPanel.getGridWidth());
//		if (gratPanel.noneRadioButton.isSelected()) {
//			gridWidth = "0.0";
//		}
//		 graticule.setAttribute("gridWidth", gridWidth);
//		root.addContent(graticule);
//		return root;
//	}

	// TODO
//	public void setState(Element e) {
//		Element root = e.getChild("graticulePlugin");
//		Element graticule = root.getChild("Graticule");
//		if (graticule.getAttributeValue("on").equals("true")) {
//		}
//		if (Double.parseDouble(graticule.getAttributeValue("gridWidth")) == 1.0) {
//			gratPanel.firstsceneRadioButton.setSelected(true);
//		} else if (Double.parseDouble(graticule.getAttributeValue("gridWidth")) == 0.1) {
//			gratPanel.secondsceneRadioButton.setSelected(true);
//		}
//		// else if(Double.parseDouble(graticule.getAttributeValue("gridWidth"))
//		// == 0.75)
//		// {
//		// gratPanel.quadrangleRadioButton.setSelected(true);
//		// }
//		else if (Double.parseDouble(graticule.getAttributeValue("gridWidth")) == 0.0) {
//			gratPanel.noneRadioButton.setSelected(true);
//		} else {
//			gratPanel.customRadioButton.setSelected(true);
//			gratPanel.customTextBox.setText(graticule
//					.getAttributeValue("gridWidth"));
//		}
//		int upperLat, lowerLat, upperLon, lowerLon;
//		upperLat = Integer.parseInt(graticule.getAttributeValue("upperLat"));
//		lowerLat = Integer.parseInt(graticule.getAttributeValue("lowerLat"));
//		upperLon = Integer.parseInt(graticule.getAttributeValue("upperLon"));
//		lowerLon = Integer.parseInt(graticule.getAttributeValue("lowerLon"));
//
//		if (!graticule.getAttributeValue("gridWidth").equals("0.0")) {
//			gratPanel
//					.makeNewGrid(upperLat, lowerLat, lowerLon, upperLon, Double
//							.parseDouble(graticule
//									.getAttributeValue("gridWidth")));
//		}
//		// ActionEvent e1 = new ActionEvent(gratPanel.graticuleappsProp_apply,
//		// 0, "apply");
//		// gratPanel.actionPerformed(e1);
//		root.removeChild("Graticule");
//	}

}
