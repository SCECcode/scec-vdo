package org.scec.vtk.politicalBoundaries;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;

import vtk.vtkActor;



public class PoliticalBoundariesPluginState implements PluginState {
	private PoliticalBoundariesGUI parent;
	private ArrayList<String> filePath;
	private ArrayList<String> upperCheckBox;


	PoliticalBoundariesPluginState(PoliticalBoundariesGUI parent)
	{
		this.parent = parent;
		filePath = new ArrayList<>();
		upperCheckBox = new ArrayList<>();
	}

	void copyLatestCatalogDetails()
	{
		filePath.clear();
		upperCheckBox.clear();

		for (JCheckBox box: parent.getLowerCheckBoxButtons())
		{
			if (box.isSelected())
			{
				System.out.println("lower check box button name: " + box.getText());
				filePath.add(box.getText());
			}
		}

		for (JCheckBox box: parent.getUpperCheckBoxButtons())
		{
//			if (box.isSelected()){
				System.out.println("upper check box button name: " + box.getText());
				upperCheckBox.add(box.getText());
//			}
		}
	}

	@Override
	public void load() {
		// call methods to update based on the properties captured //might also want to put swing invoke and wait
		//no properties to set

	}

	private void createElement(Element stateEl) {

		System.out.println("createElement");
		int index = 0;

		for (int i = 0; i < filePath.size() ; i++)
		{
			if (index < upperCheckBox.size()){
				stateEl.addElement( "PoliticalBoundaries" )
				.addAttribute( "filePath", filePath.get(i))
				.addAttribute("upperCheckBox", upperCheckBox.get(index));
			}
			else{
				stateEl.addElement( "PoliticalBoundaries" )
				.addAttribute( "filePath", filePath.get(i))
				.addAttribute("upperCheckBox", "");
			}
			index++;
		}

	}

	@Override
	public void toXML(Element stateEl) {
		System.out.println("toXML");
		copyLatestCatalogDetails();
		createElement(stateEl);
	}

	@Override
	public void fromXML(Element stateEl) {
		int counter = 0;
		filePath.clear();
		upperCheckBox.clear();

		for ( Iterator i = stateEl.elementIterator( "PoliticalBoundaries" ); i.hasNext(); ) 
		{
			Element e = (Element) i.next();
			filePath.add(e.attributeValue("filePath"));
			String uC = e.attributeValue("upperCheckBox");
			if (!uC.equals(""))
				upperCheckBox.add(uC);
			counter++;
			System.out.println("counter: " + counter);

		}

		for (int k = 0; k < filePath.size(); k++)
		{ // printing out filePath Array list
			System.out.println("filepath(" + k + "): "+ filePath.get(k));
		}

		for (int j = 0; j < upperCheckBox.size(); j++)
		{ // printing out filePath Array list
			System.out.println("upperCheckBox(" + j + "): " + upperCheckBox.get(j));
		}
		updateCheckBoxes();
	}

	@Override
	public PluginState deepCopy() {
		PoliticalBoundariesPluginState state = new PoliticalBoundariesPluginState(parent);
		state.copyLatestCatalogDetails();
		return state;
	}

	public void updateCheckBoxes()
	{
		System.out.println("update Check Box");
		unselectAllCheckBoxes();


		for (int i = 0; i < upperCheckBox.size(); i++ ){
			//			System.out.println("tab count: " + parent.getPoliticalBoundarySubPanelLowerTab().getTabCount());

			for(int j = 0; j < parent.getPoliticalBoundarySubPanelLowerTab().getTabCount(); j++)
			{
				parent.getUpperCheckBoxButtons().get(returnIndexUpperCheckBox(upperCheckBox.get(i))).setSelected(true);


				if(parent.getPoliticalBoundarySubPanelLowerTab().getTitleAt(j).equals(upperCheckBox.get(i)))
				{
					System.out.println("lower tab title: " + parent.getPoliticalBoundarySubPanelLowerTab().getTitleAt(j) );
					System.out.println("upper checkbox: " + upperCheckBox.get(i) );

					JScrollPane sp = (JScrollPane)  parent.getPoliticalBoundarySubPanelLowerTab().getComponentAt(j);
					JViewport vp = (JViewport) sp.getComponent(0);
					JPanel p = (JPanel) vp.getComponent(0);
					unselectLowerCheckBox(p);


					//					System.out.println("component count: " + p.getComponentCount());
					for(int k = 0; k < filePath.size(); k++)
					{	
						//						if(!parent.getLowerCheckBoxButtons().contains(filePath.get(k)))
						//							break;
						System.out.println("filePath.get k is : " + filePath.get(k) + " and k is " + k + " filepath size is: " + filePath.size());
						if(containComponent(p, filePath.get(k)))
						{
							System.out.println("it CONTAINS!");
							int segIndex = findIndex(p, filePath.get(k));
							System.out.println("segIndex: " + segIndex);
							vtkActor actor = parent.getActorPoliticalBoundariesSegments().get(segIndex);
							actor.VisibilityOn();
							System.out.println("selecting this checkBox: " + p.getComponent(segIndex).getName());
							JCheckBox comp = (JCheckBox) p.getComponent(segIndex);
							comp.setSelected(true);

						}

					}
					parent.getPoliticalBoundarySubPanelLowerTab().setSelectedIndex(j);

					//politicalBoundarySubPanelLowerTab.addTab(politicalBoundarySubPanelLowerTab.getTitleAt(j), sp);

				}

			}

		}

		//Info.getMainGUI().updatePoliticalBoundaries();
		Info.getMainGUI().updateRenderWindow();
		System.out.println("done updating checkboxes");
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

		System.out.println("number of components: " + doesItContain.length);
		for( int i = 0; i < doesItContain.length; i++)
		{
			//			System.out.println(doesItContain[i].getName());
			//			System.out.println("*number of components: " + doesItContain.length);
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

	public void unselectAllCheckBoxes()
	{
		System.out.println("-----------------------------------------unselectAllCheckBoxes---------------------------------------------------------------");
		for(int i=0; i < parent.getUpperCheckBoxButtons().size(); i++)
		{	

			for(int j= 0; j < parent.getPoliticalBoundarySubPanelLowerTab().getTabCount(); j++)
			{

				JScrollPane sp = (JScrollPane)  parent.getPoliticalBoundarySubPanelLowerTab().getComponentAt(j);
				JViewport vp = (JViewport) sp.getComponent(0);
				JPanel p = (JPanel) vp.getComponent(0);

				for(int k = 0; k < p.getComponentCount(); k++)
				{	
					vtkActor actor = parent.getActorPoliticalBoundariesSegments().get(k);
					actor.VisibilityOff();
					JCheckBox comp = (JCheckBox) p.getComponent(k);
					comp.setSelected(false);

				}

			}
			parent.getUpperCheckBoxButtons().get(i).setSelected(false);
		}
	}



	public void unselectLowerCheckBox(JPanel p)
	{
//		System.out.println("***************************************************************************unselectLowerCheckBoxes");
		for(int k=0;k<p.getComponentCount();k++)
		{	

			int segIndex = parent.getLowerCheckBoxButtons().indexOf(p.getComponent(k));
			vtkActor actor = parent.getActorPoliticalBoundariesSegments().get(segIndex);
			actor.VisibilityOff();
			parent.getLowerCheckBoxButtons().get(segIndex).setSelected(false);

		}
	}
}
