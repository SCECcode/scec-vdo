package org.scec.vtk.plugins.SurfacePlugin.Component;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

public class WMSLayer {
	
	String name;
	String title;
	LatLonBoundingBox box;
	ArrayList<WMSStyle> styles = new ArrayList<WMSStyle>();
	
	public WMSLayer(String name, String title, LatLonBoundingBox box, ArrayList<WMSStyle> styles) {
		this.name = name;
		this.title = title;
		this.box = box;
		this.styles = styles;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LatLonBoundingBox getBox() {
		return box;
	}

	public void setBox(LatLonBoundingBox box) {
		this.box = box;
	}

	public ArrayList<WMSStyle> getStyles() {
		return styles;
	}

	public void setStyles(ArrayList<WMSStyle> styles) {
		this.styles = styles;
	}
	
	public String toString() {
		return this.getName();
	}
	
	public static WMSLayer fromXML(Element layer) {
//		Namespace namespace = Namespace.getNamespace("http://www.opengis.net/wms");
		//		String name = layer.getChild("Name").getValue();
		String name = WMSService.getElement(layer, "Name").getValue();
//		String title = layer.getChild("Title").getValue();
		String title = WMSService.getElement(layer, "Title").getValue();
		
		LatLonBoundingBox box;
		if (layer.getChild("LatLonBoundingBox") != null)
			box = LatLonBoundingBox.fromXML(WMSService.getElement(layer, "LatLonBoundingBox"));
//			box = LatLonBoundingBox.fromXML(layer.getChild("LatLonBoundingBox"));
		else if (layer.getChild("BoundingBox") != null)
			box = LatLonBoundingBox.fromXML(WMSService.getElement(layer, "BoundingBox"));
//			box = LatLonBoundingBox.fromXML(layer.getChild("BoundingBox"));
		else
			box = new LatLonBoundingBox(-90, -180, 90, 180); // assume global
		
//		List<Element> styleList = layer.getChildren("Style");
		List<Element> styleList = WMSService.getElements(layer, "Style");
		
		ArrayList<WMSStyle> styles = new ArrayList<WMSStyle>();
		
		for (Element syle : styleList) {
			styles.add(WMSStyle.fromXML(syle));
		}
		
		return new WMSLayer(name, title, box, styles);
	}

}
