package org.scec.vtk.tools.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.PluginInfo;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class Plugins {
	public static final String PLUGIN_DESCRIPTOR_SCHEMA = "plugin.xsd";
	public static final String PLUGIN_DESCRIPTOR = "plugin.xml";
	public static final String PLUGIN_PROPERTIES = "plugin.properties";
	public static String DEFAULT_PLUGIN_DIRECTORY = MainGUI.getCWD()+File.separator+"plugins";
	
	private static final Logger log = Logger.getLogger(Plugins.class);
	
	private Plugins() {}
	
	public static Map<String, PluginInfo> getAvailablePlugins() throws IOException {
		return getAvailablePlugins(new File(DEFAULT_PLUGIN_DIRECTORY));
	}
	
	public static Map<String, PluginInfo> getAvailablePlugins(File pluginDirectory) throws IOException {
		
		if (!pluginDirectory.exists() || !pluginDirectory.isDirectory()) {
			throw new IOException("Plugin directory "+pluginDirectory+" not found");
		}
		
		Map<String, PluginInfo> availablePlugins = new HashMap<String, PluginInfo>();
		
		// List all files in the plugin directory and load plugins from
		// directories and jar files
		File[] fds = pluginDirectory.listFiles();
		for (int i=0; i<fds.length; i++) {
			
			File fd = fds[i];
			
			if (fd.isDirectory()) {
				
				// Plugin directories should have a plugin.xml file
				File pluginxml = new File(fd, PLUGIN_DESCRIPTOR);
				if (pluginxml.exists() && pluginxml.isFile()) {
					List<PluginInfo> plugins = null;
					
					try {
						FileInputStream fis = new FileInputStream(pluginxml);
						plugins = parseDescriptor(fis);
						fis.close();
					} catch (IOException ioe) {
						IOException e = new IOException(
								"Unable to load plugin from directory: "+fd.getName());
						e.initCause(ioe);
						throw e;
					}
					
					// Try to load properties
					Properties props = new Properties();
					File pluginprops = new File(fd, PLUGIN_PROPERTIES);
					if (pluginprops.exists()) {
						FileInputStream in = new FileInputStream(pluginprops);
						props.load(in);
						in.close();
					}
					
					// Add the descriptors to our list
					for (PluginInfo plugin : plugins) {
						log.debug("Found plugin: "+plugin.getName()+" ("+plugin.getId()+")");
						plugin.setProperties(props);
						availablePlugins.put(plugin.getId(), plugin);
					}
				}
				
			} else if (fd.isFile()) {
				
				// Jars should have .jar extensions
				if (fd.getName().endsWith(".jar")) {
					
					try {
						// Jars should have a plugin.xml entry
						JarFile jar = new JarFile(fd);
						ZipEntry pluginxml = jar.getEntry(PLUGIN_DESCRIPTOR);
						if (pluginxml != null) {
							List<PluginInfo>plugins = parseDescriptor(jar.getInputStream(pluginxml));
							
							// Try to load properties
							Properties props = new Properties();
							ZipEntry pluginprops = jar.getEntry(PLUGIN_PROPERTIES);
							if (pluginprops != null) {
								props.load(jar.getInputStream(pluginprops));
							}
							
							// Add the descriptors to our list
							for (PluginInfo plugin : plugins) {
								log.debug("Found plugin: "+plugin.getName()+" ("+plugin.getId()+")");
								plugin.setProperties(props);
								availablePlugins.put(plugin.getId(), plugin);
							}
						}
						jar.close();
					} catch (IOException ioe) {
						IOException e = new IOException("Unable to load plugin from jar: "+fd.getName());
						e.initCause(ioe);
						throw e;
					}
				}
			}
		}
		
		return availablePlugins;
	}
	
	private static List<PluginInfo> parseDescriptor(InputStream is) throws IOException {
		try {
			PluginDescriptorParser parser = new PluginDescriptorParser();
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(parser);
			reader.setEntityResolver(parser);
			reader.setErrorHandler(parser);
			reader.setFeature("http://apache.org/xml/features/validation/schema", true);
			reader.parse(new InputSource(is));
			return parser.getPlugins();
		} catch (Exception e) {
			IOException ioe = new IOException("Unable to parse plugin descriptor");
			ioe.initCause(e);
			throw ioe;
		}
	}
	
	private static class PluginDescriptorParser extends DefaultHandler {
		private LinkedList<PluginInfo> plugins = null;
		private PluginInfo current = null;
		
		public PluginDescriptorParser() {
			plugins = new LinkedList<PluginInfo>();
		}
		
		public List<PluginInfo> getPlugins() {
			return plugins;
		}
		
		public void startElement(String uri, String localName, String qname, Attributes attributes) 
		throws SAXException {
			if ("plugin".equals(qname)) {
				current = new PluginInfo();
				current.setId(attributes.getValue("id"));
				current.setName(attributes.getValue("name"));
				current.setShortName(attributes.getValue("short-name"));
				current.setCreator(attributes.getValue("creator"));
				current.setVersion(attributes.getValue("version"));
				current.setPluginClass(attributes.getValue("plugin-class"));
			} else if ("menu-item".equals(qname)) {
				current.setMenu(true);
				current.setMenuName(attributes.getValue("menu"));
				current.setSubmenuName(attributes.getValue("submenu"));
			}
		}
		
		public void endElement(String uri, String localName, String qname)
		throws SAXException
		{
			if ("plugin".equals(qname)) {
				plugins.add(current);
				current = null;
			}
		}
		
		public InputSource resolveEntity(String publicID, String systemID)
		throws IOException, SAXException {
			
			if (systemID.endsWith(PLUGIN_DESCRIPTOR_SCHEMA)) {
		    	URL url = Plugins.class.getResource(PLUGIN_DESCRIPTOR_SCHEMA);
		    	return new InputSource(url.openStream());
			} else {
				return null;
		    }
		}
		
		public void error(SAXParseException exception) throws SAXException {
			throw exception;
		}
		
		public void fatalError(SAXParseException exception) throws SAXException {
			throw exception;
		}
		
		public void warning(SAXParseException exception) throws SAXException {
			exception.printStackTrace();
		}
	}
}
