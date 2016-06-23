package org.scec.vtk.commons.opensha.tree;

import java.awt.Color;

public class FaultCategoryNode extends AbstractFaultNode {

	private String name;
	private String info;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FaultCategoryNode(String name) {
		this(name, null);
	}
	
	public FaultCategoryNode(String name, String info) {
		super(name);
		
		this.name = name;
		this.info = info;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public void setInfo(String info) {
		this.info = info;
	}
	
	@Override
	public String getInfo() {
		return info;
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (super.children != null) {
			for (Object child : super.children) {
				AbstractFaultNode node = AbstractFaultNode.checkCast(child);
				node.setVisible(visible);
			}
		}
		super.setVisible(visible);
	}

	@Override
	public void setColor(Color color) {
		for (Object child : super.children) {
			AbstractFaultNode node = AbstractFaultNode.checkCast(child);
			node.setColor(color);
		}
		super.setColor(color);
	}
}
