package org.scec.vtk.plugins.DummyPlugin;

import org.dom4j.Element;
import org.scec.vtk.plugins.PluginState;
import java.util.Iterator;

public class DummyPluginState implements PluginState{

	private DummyPluginGUI parent;
	private boolean displayed;
	
	DummyPluginState(DummyPluginGUI parent) {
		this.parent = parent;
	}
	
	private void copyLatestDetails() {
		this.displayed = parent.sphereLoaded;
	}
	
	@Override
	public void load() {
		// TODO Auto-generated method stub
		if(displayed)
		{
			parent.db.loadSphere();
			parent.setCheckBox(true);
		}
		else
		{
			parent.db.unloadSphere();
			parent.setCheckBox(false);
		}
	}
	
	private void createElement(Element stateEl) {
		Element propertyEl = stateEl.addElement("DummySphere");
		propertyEl.addElement("display").addText(Boolean.toString(displayed));
	}

	@Override
	public void toXML(Element stateEl) {
		copyLatestDetails();
		createElement(stateEl);
	}

	@Override
	public void fromXML(Element stateEl) {
		for(Iterator i = stateEl.elementIterator("DummySphere"); i.hasNext();)
		{
			Element e = (Element) i.next();
			displayed = Boolean.parseBoolean(e.elementText("display"));
		}
	}

	@Override
	public PluginState deepCopy() {
		DummyPluginState state = new DummyPluginState(parent);
		state.copyLatestDetails();
		return state;
	}
	
}
