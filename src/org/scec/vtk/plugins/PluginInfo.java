package org.scec.vtk.plugins;

import java.util.Properties;

/**
 * This class is a container for a plugin's metadata.
 * 
 * Created on May 20, 2004
 * 
 * @author Scott Callaghan
 * @author Gideon Juve <juve@usc.edu>
 * @version $Id: PluginInfo.java 2965 2008-08-07 02:28:58Z juve $
 */
public class PluginInfo implements Comparable<PluginInfo>{

	private String id;
	private String name;
	private String shortName;
	private String creator;
	private String version;
	private String pluginClass;
	private boolean menu;
	private String menuName;
	private String submenuName;
	private Properties properties;

	public PluginInfo() {
	}

	/**
	 * @deprecated This information should be provided through the plugin
	 *             descriptor file now
	 */
	public PluginInfo(String name, String shortName, String creator,
			String version, String submenu) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated This information should be provided through the plugin
	 *             descriptor file now
	 */
	public PluginInfo(String name, String shortName, String creator,
			String version) {
		throw new UnsupportedOperationException();
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPluginClass() {
		return pluginClass;
	}

	public void setPluginClass(String pluginClass) {
		this.pluginClass = pluginClass;
	}

	public boolean hasMenu() {
		return menu;
	}

	public void setMenu(boolean menu) {
		this.menu = menu;
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	public String getSubmenuName() {
		return submenuName;
	}

	public void setSubmenuName(String submenuName) {
		this.submenuName = submenuName;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
/**
 * Sort the plugins by their menu sub menu and shortname so that they can be put in sorted order in the menubar
 */
	public int compareTo(PluginInfo other) {
		// first make some intermediary variable so the rest of the method is clearer
		String otherMenuName = other.getMenuName();
		String otherSubmenuName = other.getSubmenuName();
		String otherShortName = other.getShortName();

		// If their primary menus are equal
		if ( ! menuName.equals(otherMenuName) ) {
			// The menus are different sort by the MenuName
			return menuName.compareTo(otherMenuName);
		}

		// If they are both not in a sub menu
		if ( submenuName == null && otherSubmenuName == null ) {
			// Then we want them sorted by their shortName
			return shortName.compareTo(otherShortName);
		}

		// If one plugin is without a sub menu make is less than the other
		if (submenuName == null) {
			return +1;
		} else if (otherSubmenuName == null) {
			return -1;
		}

		// If both plugins have sub menus compare by their short names
		if (submenuName.equals(otherSubmenuName)) {
			// they are in the same menu and sub menu so compare by name
			return shortName.compareTo(otherShortName);
		}

		//They are in different sub menus so sort by sub menu
		return submenuName.compareTo(otherSubmenuName);

	}

	public Plugin newInstance(PluginActors actors) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Class clazz = PluginInfo.class.getClassLoader().loadClass(pluginClass);
		Plugin plugin = (Plugin) clazz.newInstance();
		plugin.initialize(this, actors);
		return plugin;
	}
}

