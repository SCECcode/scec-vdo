package org.scec.geo3d.library.wgcep.tree.builders;

import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.param.ParameterList;
import org.scec.geo3d.library.wgcep.tree.events.TreeChangeListener;

public interface FaultTreeBuilder {
	
	public ParameterList getBuilderParams();
	
	public ParameterList getFaultParams();
	
	public void setTreeChangeListener(TreeChangeListener l);
	
	public void buildTree(DefaultMutableTreeNode root);

}
