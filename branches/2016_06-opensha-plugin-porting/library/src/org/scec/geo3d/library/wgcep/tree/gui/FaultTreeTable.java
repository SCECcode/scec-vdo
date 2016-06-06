package org.scec.geo3d.library.wgcep.tree.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.scec.geo3d.library.wgcep.tree.AbstractFaultNode;
import org.scec.geo3d.library.wgcep.tree.builders.FaultSectionInfoViewier;
import org.scec.geo3d.library.wgcep.tree.builders.FaultTreeBuilder;
import org.scec.geo3d.library.wgcep.tree.events.TreeChangeListener;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public class FaultTreeTable extends Outline implements TreeChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FaultTreeBuilder builder;
	
	private DefaultTreeModel treeModel;
	private OutlineModel outlineModel;
	private FaultTableRowModel rowModel;
	
	private ArrayList<TreeChangeListener> treeChangeListeners = new ArrayList<TreeChangeListener>();
	
	private static int colWidth = 50;
	
	public FaultTreeTable(FaultTreeBuilder builder, FaultSectionInfoViewier infoViewer) {
//		super(new FaultTreeTableModel(new JTree(new DefaultTreeModel(null))));
		this.setRootVisible(false);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		this.builder = builder;
		builder.setTreeChangeListener(this);
		builder.buildTree(root);
		
		super.setColumnHidingAllowed(false);
		super.setRowSorter(null);
		super.setAutoCreateRowSorter(false);
		super.setRowSelectionAllowed(false);
		super.setColumnSelectionAllowed(false);
		
		treeModel = new DefaultTreeModel(root);
		rowModel = new FaultTableRowModel(this, infoViewer);
		boolean isLargeModel = true;
		outlineModel = DefaultOutlineModel.createOutlineModel(treeModel, rowModel, isLargeModel);
		
		super.setModel(outlineModel);
		
		super.setDefaultRenderer(Color.class, new ColorIconRenderer());
		
		this.getColumnModel().getColumn(1).setPreferredWidth(colWidth);
		this.getColumnModel().getColumn(1).setWidth(colWidth);
		this.getColumnModel().getColumn(1).setMaxWidth(colWidth);
		this.getColumnModel().getColumn(2).setPreferredWidth(colWidth);
		this.getColumnModel().getColumn(2).setWidth(colWidth);
		this.getColumnModel().getColumn(2).setMaxWidth(colWidth);
		
		super.setRenderDataProvider(new FaultRenderDataProvider());
		
		super.addMouseListener(rowModel);
	}
	
	public void addTreeChangeListener(TreeChangeListener l) {
		this.treeChangeListeners.add(l);
	}
	
	public boolean removeTreeChangeListener(TreeChangeListener l) {
		return treeChangeListeners.remove(l);
	}
	
	private void fireTreeChangeEvent(TreeNode root) {
		for (TreeChangeListener l : treeChangeListeners) {
			l.treeChanged(root);
		}
	}
	
	public void rebuildTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		
		// we do this here to clear out any caches, then GC
		fireTreeChangeEvent(root);
		this.treeModel.setRoot(root);
		System.gc();
		
		builder.buildTree(root);
		this.treeModel.setRoot(root);
		fireTreeChangeEvent(root);
	}
	
	public FaultTableRowModel getRowModel() {
		return rowModel;
	}
	
	public DefaultMutableTreeNode getTreeRoot() {
		return (DefaultMutableTreeNode)treeModel.getRoot();
	}
	
	protected AbstractFaultNode nodeForRow(int row) {
		return AbstractFaultNode.checkCast(getModel().getValueAt(row, 0));
	}
	
	/**
	 * Toggles visibility of selected fault nodes
	 */
	public void toggleSelectedVisibility() {
		int[] indices = getSelectedRows();
		for (int row : indices)
			outlineModel.setValueAt(!(Boolean)(outlineModel.getValueAt(row, 2)), row, 2);
	}
	
	/**
	 * Sets the color for all selected rows
	 * @param color
	 */
	public void setColorForSelected(Color color) {
		int[] indices = getSelectedRows();
		for (int row : indices)
			outlineModel.setValueAt(color, row, 1);
	}
	
	public static void main(String[] args) throws IOException {
//		FaultTreeBuilder builder = new DefModelBuilder();
//		
//		FaultTreeTable table = new FaultTreeTable(builder, null);
//		
//		JFrame frame = new JFrame();
//		frame.setSize(400, 800);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		
//		frame.setContentPane(table);
//		
//		frame.setVisible(true);
	}

	@Override
	public void treeChanged(TreeNode newRoot) {
		rebuildTree();
	}

}
