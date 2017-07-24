package org.scec.vtk.politicalBoundaries;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.table.TableModel;
import org.dom4j.Element;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.utils.components.CheckAllTable;
import org.scec.vtk.plugins.utils.components.TreeNode;


import vtk.vtkActor;



public class PoliticalBoundariesPluginState implements PluginState {
	private PoliticalBoundariesGUI parent;
	private ArrayList<String> filePath;

	PoliticalBoundariesPluginState(PoliticalBoundariesGUI parent)
	{
		this.parent = parent;
		filePath = new ArrayList<>();
	}

	/*
	 * copyLatestCatalogDetails()
	 * 		This function traverses through the table tree structure
	 * 		If there is a selected piece on the menu, it will be marked
	 * 		as true on TableModel 
	 * 		
	 */
	
	void copyLatestCatalogDetails()
	{
		System.out.println("inside copyLatestCatalogDetails()");
		filePath.clear();
	
		TreeNode<CheckAllTable> treeRoot = parent.root;
		
		for (TreeNode<CheckAllTable> node : treeRoot) {
			CheckAllTable table = node.data;
			TableModel list = table.getTable().getModel();
			for(int i = 0; i < list.getRowCount(); i ++){
				if((Boolean)list.getValueAt(i, 0)){
					filePath.add((String)list.getValueAt(i, 1));
					System.out.println("Name: " + list.getValueAt(i, 1) + " | color:  " + list.getValueAt(i, 2).toString()  );
					Color c = (Color)list.getValueAt(i, 2);
					filePath.add(Integer.toString(c.hashCode()));
					
				}
				
			}
		}

	}

	
	
	private static String createIndent(int depth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}
	
	
	
	public void clear(){

		TreeNode<CheckAllTable> treeRoot = parent.root;
		
		for (TreeNode<CheckAllTable> node : treeRoot) {
			CheckAllTable table = node.data;
			TableModel list = table.getTable().getModel();
			for(int i = 0; i < list.getRowCount(); i ++){
				for(int j = 0 ; j < filePath.size(); j++){
						list.setValueAt(false, i, 0);	
				}	
			}
		}
		
	}
	
	@Override
	public void load() {
		
		clear();
		
		TreeNode<CheckAllTable> treeRoot = parent.root;
		
		for (TreeNode<CheckAllTable> node : treeRoot) {
			CheckAllTable table = node.data;
			TableModel list = table.getTable().getModel();
			for(int i = 0; i < list.getRowCount(); i ++){
				for(int j = 0 ; j < filePath.size(); j++){
					if(((String)list.getValueAt(i, 1)).equalsIgnoreCase(filePath.get(j))){
						list.setValueAt(true, i, 0);
						j++;
						list.setValueAt(new Color(Integer.parseInt(filePath.get(j))), i, 2);
						
						System.out.println("PoliBoun: " + list.getValueAt(i, 1 ) + " Color: " + list.getValueAt(i, 2));
						
					}
					
				}
				
				
			}
		}

	}

	private void createElement(Element stateEl)
	{
		for (int i = 0; i < filePath.size() ; i++)
		{
			stateEl.addElement( "PoliticalBoundaries" )
			.addElement( "filePath").addText(filePath.get(i));
		}

	}

	@Override
	public void toXML(Element stateEl) {
		System.out.println("toXML pol bound state");
		copyLatestCatalogDetails();
		createElement(stateEl);
	}

	@Override
	public void fromXML(Element stateEl) {
		filePath.clear();

		for ( Iterator i = stateEl.elementIterator( "PoliticalBoundaries" ); i.hasNext(); ) 
		{
			Element e = (Element) i.next();
			filePath.add(e.elementText("filePath"));
		}

	}

	@Override
	public PluginState deepCopy() {
		PoliticalBoundariesPluginState state = new PoliticalBoundariesPluginState(parent);
		state.copyLatestCatalogDetails();
		return state;
	}


	public int findIndex(JPanel p, String name){
		Component[] doesItContain = p.getComponents();

		for( int i = 0; i < doesItContain.length; i++)
		{
			if(doesItContain[i].getName().equals(name)){
				return i;			
			}
		}
		return -1;
	}

	public boolean containComponent(JPanel p, String name)
	{
		Component[] doesItContain = p.getComponents();
		for( int i = 0; i < doesItContain.length; i++)
		{
			if(doesItContain[i].getName().equals(name)){
				return true;				
			}
		}
		return false;
	}

	public int returnIndexUpperCheckBox(String name)
	{
		for(int i = 0; i< parent.getUpperCheckBoxButtons().size(); i++)
		{
			if (parent.getUpperCheckBoxButtons().get(i).getText().equals(name))
			{
				return i;
			}
		}

		return -1;
	}


	public void unselectLowerCheckBox(JPanel p)
	{

		for(int k=0;k<p.getComponentCount();k++)
		{	
			int segIndex = parent.getLowerCheckBoxButtons().indexOf(p.getComponent(k));
			vtkActor actor = parent.getPoliticalBoundaries().get(segIndex);
			actor.VisibilityOff();
			parent.getLowerCheckBoxButtons().get(segIndex).setSelected(false);
		}
	}
}
