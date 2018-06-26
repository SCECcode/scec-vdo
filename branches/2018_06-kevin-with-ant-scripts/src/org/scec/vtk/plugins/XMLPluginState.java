package org.scec.vtk.plugins;

import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.opensha.commons.util.ExceptionUtils;

public abstract class XMLPluginState implements PluginState, Cloneable {
	
	protected Element stateEl;

	@Override
	public abstract void load();
	
	/**
	 * Called when the XML representation is changed externally. Note that the reference may change as well,
	 */
	public abstract void refreshState();

	@Override
	public void toXML(Element stateEl) {
		Iterator<Attribute> attIt = this.stateEl.attributeIterator();
		while (attIt.hasNext())
			stateEl.add(attIt.next());
		Iterator<Element> elemIt = this.stateEl.elementIterator();
		while (elemIt.hasNext())
			stateEl.add(elemIt.next());
	}

	@Override
	public void fromXML(Element stateEl) {
		this.stateEl = stateEl;
		refreshState();
	}

	@Override
	public PluginState deepCopy() {
		XMLPluginState o;
		try {
			o = (XMLPluginState) this.clone();
		} catch (CloneNotSupportedException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		o.stateEl = stateEl.createCopy();
		return o;
	}

}
