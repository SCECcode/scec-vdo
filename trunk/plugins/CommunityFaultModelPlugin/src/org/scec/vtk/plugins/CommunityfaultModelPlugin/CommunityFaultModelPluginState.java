package org.scec.vtk.plugins.CommunityfaultModelPlugin;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.Fault3D;

public class CommunityFaultModelPluginState implements PluginState
{
	private CommunityFaultModelGUI parent;
	private ArrayList<Fault3D> faults;
	private ArrayList<Color> color1;
	private ArrayList<Integer> meshState;
	ArrayList<Integer> visibility;
	private ArrayList<String> dispName;
	private ArrayList<String> filePath;
	private ArrayList<String> citation;
	private ArrayList<String> reference;
	private ArrayList<String> notes;
	
	CommunityFaultModelPluginState	(CommunityFaultModelGUI parent)
	{
		
		this.parent = parent;
		faults = new ArrayList<Fault3D>();
		dispName =new ArrayList<>();
		filePath =new ArrayList<>();
		color1 =new ArrayList<>();
		meshState =new ArrayList<>();
		visibility =new ArrayList<>();
		citation =new ArrayList<>();
		reference =new ArrayList<>();
		notes =new ArrayList<>();
	}
	void copyLatestCatalogDetials()
	{
		faults.clear();
		dispName.clear();
		filePath.clear();
		color1.clear();
		meshState.clear();
		visibility.clear();
		citation.clear();
		reference.clear();
		notes.clear();
		for (int row =0;row<parent.faultTable.getRowCount();row++)
		{
			Fault3D fault = (Fault3D) parent.faultTable.getModel().getValueAt(row,0);
			faults.add(fault);
			dispName.add(fault.getDisplayName());
			filePath.add(fault.getSourceFile());
			color1.add(fault.getColor());
			meshState.add(fault.getMeshState());
			visibility.add(fault.getFaultActor().GetVisibility());
			citation.add(fault.getCitation());
			reference.add(fault.getReference());
			notes.add(fault.getNotes());
		}
	}
	@Override
	public void load() {
		int i=0;
		for (Fault3D fault : faults)
		{
			fault.setDisplayName(dispName.get(i));
			fault.setAttributeFile(filePath.get(i));
			fault.setCitation(citation.get(i));
			fault.setReference(reference.get(i));
			fault.setNotes(notes.get(i));
			parent.setColor(fault, color1.get(i));
			parent.faultTable.getLibraryModel().setColorForRow(color1.get(i), i);
			parent.setMeshState(fault,meshState.get(i));
			parent.faultTable.getLibraryModel().setMeshStateForRow(meshState.get(i), i);
			parent.setVisibility(fault, visibility.get(i));
			System.out.println(fault.getFaultActor().GetVisibility());
			i++;
		}
		Info.getMainGUI().updateRenderWindow();
	}

	private void createElement(Element stateEl) {
		int i=0;
		for (Fault3D eqc : faults)
		{
			stateEl.addElement( "CFM" )
					.addAttribute( "dispName", dispName.get(i))
					.addAttribute( "filePath", filePath.get(i))
					.addAttribute( "color1", Integer.toString(color1.get(i).getRGB()))
					.addAttribute( "meshState",Integer.toString(meshState.get(i)))
					.addAttribute( "citation",(citation.get(i)))
					.addAttribute( "reference",(reference.get(i)))
					.addAttribute( "notes",(notes.get(i)))
					.addAttribute( "visibility",(visibility.get(i).toString()));
			i++;
		}
	}
	
	@Override
	public void toXML(Element stateEl) {
		copyLatestCatalogDetials();
		createElement(stateEl);
	}

	@Override
	public void fromXML(Element stateEl) {
		 ArrayList<File> file = new ArrayList<>();
		for ( Iterator i = stateEl.elementIterator( "CFM" ); i.hasNext(); ) {
            Element e = (Element) i.next();
            dispName.add(e.attributeValue("dispName"));
            filePath.add(e.attributeValue("filePath"));
            color1.add(Color.decode(e.attributeValue("color1")));
            visibility.add(Integer.parseInt(e.attributeValue("visibility")));
            meshState.add(Integer.parseInt(e.attributeValue("meshState")));
            citation.add((e.attributeValue("citation")));
            reference.add((e.attributeValue("reference")));
            notes.add((e.attributeValue("notes")));
            System.out.println(e.attributeValue("filePath"));
           
            file.add(new File(filePath.get(filePath.size()-1)));
        }
		parent.addFaultsFromFile(file.toArray(new File[file.size()]));
		for (int row =0;row<parent.faultTable.getRowCount();row++)
		{
			Fault3D fault = (Fault3D) parent.faultTable.getModel().getValueAt(row,0);
			faults.add(fault);
		}
		load();
	}

	@Override
	public PluginState deepCopy() {
		CommunityFaultModelPluginState state = new CommunityFaultModelPluginState(parent);
		state.copyLatestCatalogDetials();
		return state;
	}

}
