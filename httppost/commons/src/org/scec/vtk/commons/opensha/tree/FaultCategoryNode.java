package org.scec.vtk.commons.opensha.tree;

import java.awt.Color;

public class FaultCategoryNode extends AbstractFaultNode {

	private String name;
	private String info;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// user data object must be unique for saving/loading state, so we can't just pass in the String which could be a duplicate
	private static class CategoryUserObject {
		private String name;
		
		public CategoryUserObject(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public FaultCategoryNode(String name) {
		this(name, null);
	}
	
	public FaultCategoryNode(String name, String info) {
		super(new CategoryUserObject(name));
		
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
