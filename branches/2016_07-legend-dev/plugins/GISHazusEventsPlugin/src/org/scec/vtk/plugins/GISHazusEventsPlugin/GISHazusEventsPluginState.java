package org.scec.vtk.plugins.GISHazusEventsPlugin;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.tools.actors.AppendActors;
import org.w3c.dom.NodeList;

public class GISHazusEventsPluginState implements PluginState {
	private GISHazusEventsPluginGUI parent;
	private float transparency;
	private Color[] gradient;
	private Events events;
	private boolean import1;
	private boolean import2;
	private boolean import3;
	private boolean import4;
	private FilledBoundaryCluster currentBoundary;
	private ArrayList<Float> populationCategory;
    private File ralph;
    private EventAttributes event;
    private ArrayList<Float> legendMaxList;
	ArrayList<EventAttributes> eventList;
    private int numLines = 0;
    private int numFiles = 0;
    String sImportedFilePath, sImportedFilePath1,  sImportedFilePath2, sImportedFilePath3, sImportedFilePath4;
    private int groupCount = 0;
	private int numBounds = 0;
	public String[] names;
	public int[] groupSize;
	
	private Color[] purpleGradient = new Color[Events.NUM_POP_CATEGORY];
	
	private ArrayList<FilledBoundaryCluster> allBounds = new ArrayList<FilledBoundaryCluster>();
	private AppendActors segmentActors;
	private NodeList nodeList;
	
	private ArrayList<Integer> selected;
	GISHazusEventsPluginState(GISHazusEventsPluginGUI parent)
	{
		this.parent = parent;
		transparency =0;
		gradient = new Color[2];
		events = null;
		selected = new ArrayList<Integer>();
	}
	public void copyDetails()
	{
		selected.clear();
		transparency = (float)(parent.getTransparencySlider().getValue()) / 100.0f;
		gradient[0] = parent.colorButton.getColor1();
		gradient[1] = parent.colorButton.getColor2();
		import1 = this.parent.bTrace.bIsImport;
		import2 = this.parent.bTrace.bIsImport1;
		import3 = this.parent.bTrace.bIsImport2;
		import4 = this.parent.bTrace.bIsImport3;
		currentBoundary = this.parent.bTrace.currentBoundary;
		populationCategory = this.parent.bTrace.populationCategory;
		ralph = this.parent.bTrace.ralph;
		event = this.parent.bTrace.event;
		legendMaxList = this.parent.bTrace.getLegendMax();
		eventList = this.parent.bTrace.eventList;
		numLines = this.parent.bTrace.numLines;
		numFiles = this.parent.bTrace.numFiles;
		sImportedFilePath = this.parent.bTrace.sImportedFilePath;
		sImportedFilePath1 = this.parent.bTrace.sImportedFilePath1;
		sImportedFilePath2 = this.parent.bTrace.sImportedFilePath2;
		sImportedFilePath3 = this.parent.bTrace.sImportedFilePath3;
		sImportedFilePath4 = this.parent.bTrace.sImportedFilePath4;
		groupCount = this.parent.bTrace.groupCount;
		numBounds = this.parent.bTrace.numBounds;
		names = this.parent.bTrace.names;
		groupSize = this.parent.bTrace.groupSize;
		purpleGradient = this.parent.bTrace.getPurpleGradient();
		allBounds = this.parent.bTrace.allBounds;
		segmentActors = this.parent.bTrace.segmentActors;
		nodeList = this.parent.bTrace.nodeList;
		selected = this.parent.selected;
	}
	@Override
	public void load() {
		this.parent.bTrace.bIsImport = import1;
		this.parent.bTrace.bIsImport1 = import2;
		this.parent.bTrace.bIsImport2 = import3;
		this.parent.bTrace.bIsImport3 = import4;
		this.parent.bTrace.currentBoundary = currentBoundary;
		this.parent.bTrace.populationCategory = populationCategory;
		this.parent.bTrace.ralph = ralph;
		this.parent.bTrace.event = event;
		this.parent.bTrace.legendMaxList = legendMaxList;
		this.parent.bTrace.eventList = eventList;
		this.parent.bTrace.numLines = numLines;
		this.parent.bTrace.numFiles = numFiles;
		this.parent.bTrace.sImportedFilePath = sImportedFilePath;
		this.parent.bTrace.sImportedFilePath1 = sImportedFilePath1;
		this.parent.bTrace.sImportedFilePath2 = sImportedFilePath2;
		this.parent.bTrace.sImportedFilePath3 = sImportedFilePath3;
		this.parent.bTrace.sImportedFilePath4 = sImportedFilePath4;
		this.parent.bTrace.groupCount = groupCount;
		this.parent.bTrace.numBounds = numBounds;
		this.parent.bTrace.names = names;
		this.parent.bTrace.groupSize = groupSize;
		this.parent.bTrace.purpleGradient = purpleGradient;
		this.parent.bTrace.allBounds = allBounds;
		this.parent.bTrace.segmentActors = segmentActors;
		this.parent.bTrace.nodeList = nodeList;
		this.parent.selected = selected;
		for(int i = 0;i < selected.size();i++)
		{
			this.parent.drawEvent(selected.get(i));
		}
		parent.setTransparency(transparency);
		parent.updateColorButton(gradient[0], gradient[1]);
		Info.getMainGUI().updateRenderWindow();
		

	}
	
	private void createElement(Element stateE1)
	{
		for(int num:selected)
		{
			stateE1.addElement("HAZUS").addElement("id","" + num);
		}
		
	}
	@Override
	public void toXML(Element stateEl) {
		copyDetails();
		createElement(stateEl);
	}

	@Override
	public void fromXML(Element stateEl) {
		for ( Iterator i = stateEl.elementIterator( "HAZUS" ); i.hasNext(); ) {
            Element e = (Element) i.next();
            selected.add(Integer.parseInt(e.attributeValue("id")));
		}
		for(int i = 0;i < selected.size();i++)
		{
			this.parent.drawEvent(selected.get(i));
		}

	}

	@Override
	public PluginState deepCopy() {
		GISHazusEventsPluginState state = new GISHazusEventsPluginState(parent);
		state.copyDetails();
		return state;
	}

}
