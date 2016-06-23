package org.scec.vtk.plugins.SurfacePlugin.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class WMSService {
	
	private String baseUrl = "";
	private ArrayList<WMSLayer> layers = new ArrayList<WMSLayer>();
	
	private int currentLayer = 0;
	
	public WMSService(String baseUrl, ArrayList<WMSLayer> layers) {
		if (!baseUrl.endsWith("?"))
			baseUrl += "?";
		this.baseUrl = baseUrl;
		this.layers = layers;
	}
	
	public WMSLayer getCurrentLayer() {
		return layers.get(currentLayer);
	}
	
	public ArrayList<WMSLayer> getLayers() {
		return layers;
	}
	
	public static WMSService fromXML(String baseUrl) throws MalformedURLException, JDOMException, IOException {
		String url = baseUrl;
		if (url.contains("?"))
			url += "&";
		else
			url += "?";
//		url += "request=GetCapabilities&mapext=[mapext]&SERVICE=wms";
//		url += "request=GetCapabilities&mapext=[mapext]";
//		url += "request=getCapabilities&service=wms&version=1.1.1";
//		url += "request=GetCapabilities&mapext=%5Bmapext%5D";
		url += "SERVICE=WMS&REQUEST=GetCapabilities";
		System.out.println("URL: "+url);
		
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new URL(url));
		
		Element root = doc.getRootElement();
		Preconditions.checkNotNull(root, "No root element");
		
//		Element main = root.getChild("WMT_MS_Capabilities");
//		Element caps = root.getChild("Capability");
		Element caps = getElement(root, "Capability");
		Preconditions.checkNotNull(caps, "No capability element. Parent children: "+getChildrenNames(root));
//		for (Object desc : Lists.newArrayList(caps.getDescendants()))
//		for (Object desc : caps.getChildren())
//			System.out.println(desc);
//		 caps.getDescendants()
//		Element layersEl = caps.getChild("Layer");
		Element layersEl = getElement(caps, "Layer");
		Preconditions.checkNotNull(layersEl, "No layer element. Parent children: "+getChildrenNames(caps));
		
//		List<Element> layerList = layersEl.getChildren("Layer");
//		List<Element> layerList = getElements(layersEl, "Layer");
//		
//		ArrayList<WMSLayer> layers = new ArrayList<WMSLayer>();
//		
//		for (Element layer : layerList) {
//			layers.add(WMSLayer.fromXML(layer));
//		}
		
		ArrayList<WMSLayer> layers = getLayers(layersEl);
		
		return new WMSService(baseUrl, layers);
	}
	
	private static ArrayList<WMSLayer> getLayers(Element parentEl) {
		List<Element> layerList = getElements(parentEl, "Layer");
		ArrayList<WMSLayer> layers = Lists.newArrayList();
		if (!layerList.isEmpty()) {
			// this is a container, has sub layers
			for (Element layer : layerList) {
				layers.addAll(getLayers(layer));
			}
		} else {
			// this is the layer itself
			layers.add(WMSLayer.fromXML(parentEl));
		}
		return layers;
	}
	
	static Element getElement(Element root, String name) {
		for (Element el : (List<Element>)root.getChildren()) {
			if (el.getName().trim().equals(name.trim()))
				return el;
		}
		return null;
	}
	
	static List<Element> getElements(Element root, String name) {
		List<Element> els = Lists.newArrayList();
		for (Element el : (List<Element>)root.getChildren()) {
			if (el.getName().trim().equals(name.trim()))
				els.add(el);
		}
		return els;
	}
	
	private static String getChildrenNames(Element elem) {
		List<String> names = Lists.newArrayList();
		for (Element e : (List<Element>)elem.getChildren())
			names.add(e.getName());
		return Joiner.on(",").join(names);
	}
	
	public static void main(String args[]) {
		try {
//			WMSService service = WMSService.fromXML("http://wms.jpl.nasa.gov/wms.cgi");
//			WMSService service = WMSService.fromXML("http://disc1.gsfc.nasa.gov/daac-bin/wms_ogc");
//			WMSService service = WMSService.fromXML("http://www.flysask.ca/cgi-bin/public.cgi");
//			WMSService service = WMSService.fromXML("http://gridca.grid.unep.ch/cgi-bin/mapserv?map=/www/geodataportal/htdocs/mod_map/geo_wms.map");
//			WMSService service = WMSService.fromXML("http://www.gebco.net/data_and_products/gebco_web_services/web_map_service/mapserv");
//			WMSService service = WMSService.fromXML("http://disc1.gsfc.nasa.gov/daac-bin/wms_ogc");
//			WMSService service = WMSService.fromXML("http://neowms.sci.gsfc.nasa.gov/wms/wms");
			WMSService service = WMSService.fromXML("http://www.nasa.network.com/wms");
			for (WMSLayer layer : service.getLayers()) {
				System.out.println();
				System.out.println("Layer: " + layer.getName() + ": " + layer.getTitle());
				System.out.println("BBox: " + layer.getBox());
				for (WMSStyle style : layer.getStyles()) {
					System.out.println("Style: " + style.getName() + ": " + style.getTitle());
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
}
