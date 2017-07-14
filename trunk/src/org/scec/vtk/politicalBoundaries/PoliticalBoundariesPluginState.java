package org.scec.vtk.politicalBoundaries;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
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

	void copyLatestCatalogDetails()
	{
		filePath.clear();
	
		TreeNode<CheckAllTable> treeRoot = parent.root;
		
		for (TreeNode<CheckAllTable> node : treeRoot) {
			CheckAllTable table = node.data;
			TableModel list = table.getTable().getModel();
			for(int i = 0; i < list.getRowCount(); i ++){
				if((Boolean)list.getValueAt(i, 0)){
					filePath.add((String)list.getValueAt(i, 1));
					System.out.println("Name: " + list.getValueAt(i, 1) + " | checked:  " + list.getValueAt(i, 0)  );
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
	
	
	
	@Override
	public void load() {
		
		
		TreeNode<CheckAllTable> treeRoot = parent.root;
		
		for (TreeNode<CheckAllTable> node : treeRoot) {
			CheckAllTable table = node.data;
			TableModel list = table.getTable().getModel();
			for(int i = 0; i < list.getRowCount(); i ++){
				for(int j = 0 ; j < filePath.size(); j++){
					if(((String)list.getValueAt(i, 1)).equalsIgnoreCase(filePath.get(j))){
						list.setValueAt(true, i, 0);
					}
				}
				
				
			}
		}

		/*for(int j= 0; j < parent.getPoliticalBoundarySubPanelLowerTab().getTabCount(); j++)
		{
			JScrollPane sp = (JScrollPane)  parent.getPoliticalBoundarySubPanelLowerTab().getComponentAt(j);
			JViewport vp = (JViewport) sp.getComponent(0);
			JPanel p = (JPanel) vp.getComponent(0);
			unselectLowerCheckBox(p);

			for(int k = 0; k < filePath.size(); k++)
			{	
				if(containComponent(p, filePath.get(k)))
				{
					int segIndex = findIndex(p, filePath.get(k));
					int actorIndex = parent.getLowerCheckBoxButtons().indexOf(p.getComponent(segIndex));//
					vtkActor actor = parent.getPoliticalBoundaries().get(actorIndex);
					actor.VisibilityOn();
					JCheckBox comp = (JCheckBox) p.getComponent(segIndex);
					comp.setSelected(true);

				}

			}
		}*/

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
