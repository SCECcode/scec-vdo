package org.scec.geo3d.commons.opensha.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class XMLConverter {
	
	public static org.jdom.Element toJDOM(org.dom4j.Element dom4jElem) throws org.jdom.JDOMException, IOException {
		org.dom4j.Document dom4jDoc = org.dom4j.DocumentHelper.createDocument();
		dom4jDoc.add(dom4jElem);
		String xml = dom4jDoc.asXML();
		
		org.jdom.input.SAXBuilder jdomBuilder = new org.jdom.input.SAXBuilder();
		org.jdom.Document jdomDoc = jdomBuilder.build(new StringReader(xml));
		return jdomDoc.getRootElement();
	}
	
	public static org.dom4j.Element toDom4J(org.jdom.Element jdomElem) throws IOException, org.dom4j.DocumentException {
		org.jdom.output.XMLOutputter jdomOut = new org.jdom.output.XMLOutputter();
		StringWriter sw = new StringWriter();
		jdomOut.output(jdomElem, sw);
		String xml = sw.toString();
		
		org.dom4j.io.SAXReader dom4jReader = new org.dom4j.io.SAXReader();
		org.dom4j.Document dom4jDoc = dom4jReader.read(new StringReader(xml));
		return dom4jDoc.getRootElement();
	}

}
