package org.scec.geo3d.commons.opensha.tree;

import java.awt.Color;

import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.data.Named;

public abstract class AbstractFaultNode extends DefaultMutableTreeNode implements Named {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Color color;
	private boolean visible = false;
	
	public AbstractFaultNode(Object obj) {
		super(obj);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public abstract String getInfo();
	
	public static AbstractFaultNode checkCast(Object node) {
		if (node == null)
			return null;
		if (!(node instanceof AbstractFaultNode))
			throw new ClassCastException("Object of type '"+node.getClass().getName()
					+"' cannot be cast to AbstractFaultNode");
		return (AbstractFaultNode)node;
	}
}
