package org.scec.vtk.plugins.SurfacePlugin.Component;

import org.jdom.Element;

public class WMSStyle {
	
	private String name;
	private String title;
	
	public WMSStyle(String name) {
		this(name, name);
	}
	
	public WMSStyle(String name, String title) {
		this.name = name;
		this.title = title;
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
	
	public String toString() {
		return this.getName();
	}
	
	public static WMSStyle fromXML(Element style) {
//		String name = style.getChild("Name").getValue();
		String name = WMSService.getElement(style, "Name").getValue();
//		String title = style.getChild("Title").getValue();
		String title = WMSService.getElement(style, "Title").getValue();
		
		return new WMSStyle(name, title);
	}

}
