package org.scec.vtk.politicalBoundaries;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.dom4j.Element;
import org.scec.vtk.plugins.PluginState;

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


		for (JCheckBox box: parent.getLowerCheckBoxButtons())
		{
			if (box.isSelected())
			{
				filePath.add(box.getText());
			}
		}

	}

	@Override
	public void load() {
		// call methods to update based on the properties captured //might also want to put swing invoke and wait

//		for(int j= 0; j < parent.getPoliticalBoundarySubPanelLowerTab().getTabCount(); j++)
//		{
//			JScrollPane sp = (JScrollPane)  parent.getPoliticalBoundarySubPanelLowerTab().getComponentAt(j);
//			JViewport vp = (JViewport) sp.getComponent(0);
//			JPanel p = (JPanel) vp.getComponent(0);
//			unselectLowerCheckBox(p);
//
//			for(int k = 0; k < filePath.size(); k++)
//			{	
//				if(containComponent(p, filePath.get(k)))
//				{
//					int segIndex = findIndex(p, filePath.get(k));
//					int actorIndex = parent.getLowerCheckBoxButtons().indexOf(p.getComponent(segIndex));//
//					vtkActor actor = parent.getPoliticalBoundaries().get(actorIndex);
//					actor.VisibilityOn();
//					JCheckBox comp = (JCheckBox) p.getComponent(segIndex);
//					comp.setSelected(true);
//
//				}
//
//			}
//		}

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
