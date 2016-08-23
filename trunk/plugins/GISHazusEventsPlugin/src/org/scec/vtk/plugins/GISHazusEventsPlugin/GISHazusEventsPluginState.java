package org.scec.vtk.plugins.GISHazusEventsPlugin;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableModel;

import org.dom4j.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.tools.actors.AppendActors;
import org.w3c.dom.NodeList;

import oracle.net.aso.b;

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
	//private ArrayList<Float> legendMaxList;
	//ArrayList<EventAttributes> eventList;
	private int numLines = 0;
	private int numFiles = 0;
	String sImportedFilePath, sImportedFilePath1,  sImportedFilePath2, sImportedFilePath3, sImportedFilePath4;
	private int groupCount = 0;
	private int numBounds = 0;
	public String[] names;
	public int[] groupSize;

	//private Color[] purpleGradient;

	private ArrayList<FilledBoundaryCluster> allBounds;
	private AppendActors segmentActors;
	//private NodeList nodeList;

	private ArrayList<Integer> selected;
	private ArrayList<TableModel> tableModelList;
	private int[] tabIndex;
	private BoundaryTableModel[][] boundaryTableModel;
	GISHazusEventsPluginState(GISHazusEventsPluginGUI parent)
	{
		this.parent = parent;
		transparency =0;
		gradient = new Color[2];
		tabIndex = new int[3];//likeEQ size
		boundaryTableModel = new BoundaryTableModel[3][this.parent.REGION_AMT];
		events = null;
		selected = new ArrayList<Integer>();
		//legendMaxList = new ArrayList<Float>();
		//events = new Events();
		tableModelList = new  ArrayList<>();
		segmentActors = new AppendActors();
		currentBoundary = new FilledBoundaryCluster(segmentActors);
		populationCategory = new ArrayList<Float>();
		//eventList = new ArrayList<>();
		//purpleGradient = new Color[Events.NUM_POP_CATEGORY];
		allBounds = new ArrayList<FilledBoundaryCluster>();
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
		//System.out.println(this.parent.bTrace.getpo);
		boundaryTableModel = this.parent.boundaryTableModel;
		//currentBoundary = this.parent.bTrace.currentBoundary;
		tableModelList = this.parent.getTableModleList();
		//this.parent.tableModel2.getValueAt(0, 0);
		populationCategory = this.parent.bTrace.populationCategory;
		ralph = this.parent.bTrace.ralph;
		event = this.parent.bTrace.event;
		//legendMaxList = this.parent.bTrace.getLegendMax();
		//eventList = this.parent.bTrace.eventList;
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
		//purpleGradient = this.parent.bTrace.getPurpleGradient();
		allBounds = this.parent.bTrace.allBounds;
		segmentActors = this.parent.bTrace.segmentActors;
		//nodeList = this.parent.bTrace.nodeList;
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
		//this.parent.bTrace.legendMaxList = legendMaxList;
		//this.parent.bTrace.eventList = eventList;
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
		//this.parent.bTrace.purpleGradient = purpleGradient;
		this.parent.bTrace.allBounds = allBounds;
		//this.parent.bTrace.segmentActors = segmentActors;
		//this.parent.bTrace.nodeList = nodeList;
		this.parent.selected = selected;
		this.parent.boundaryTableModel = boundaryTableModel;
//		for(int j =0;j<tabIndex.length;j++)
		//{
		for (int i = 0; i<boundaryTableModel.length; i++){
			
			for (int j = 0; j<boundaryTableModel[i].length; j++){
				if(boundaryTableModel[i][j]!=null)
				{
					this.parent.getTableModleList().get(i).setValueAt(true, j, 0);		
					//this.parent.drawEvent(j,i);
				}
			}
		}
		parent.setTransparency(transparency);
		parent.updateColorButton(gradient[0], gradient[1]);
		Info.getMainGUI().updateRenderWindow();


	}

	private void createElement(Element stateEl)
	{
		Element  propertyEl = stateEl.addElement( "HAZUS" );
		propertyEl.addElement("color1").addText("" + gradient[0].getRGB());
		propertyEl.addElement("color2").addText("" + gradient[1].getRGB());
		propertyEl.addElement("transparency").addText("" + transparency);
		propertyEl.addElement("numLines").addText("" + numLines);
		propertyEl.addElement("numFiles").addText("" + numFiles);
		propertyEl.addElement("GroupTabs").addText("" + this.parent.groupsTabbedPane.getTabCount());
		//tabList.add(this.parent.groupsTabbedPane.)
		for (int i = 0; i<boundaryTableModel.length; i++){
			Element tabEl = propertyEl.addElement("Tab").addText("" + i);	
			for (int j = 0; j<boundaryTableModel[i].length; j++){
				
				Element boundsEl = tabEl.addElement("Bounds");
				if(boundaryTableModel[i][j]!=null)
				{
					boundsEl.addElement("SubBoundSize").addText(Integer.toString(boundaryTableModel[i][j].getRowCount()));
					for (int k = 0; k<boundaryTableModel[i][j].getRowCount(); k++){
						//isDisplayed
						Element subBoundsEl = boundsEl.addElement("SubBounds");
						subBoundsEl.addElement("Name").addText(boundaryTableModel[i][j].getValueAt(k, 2).toString());
						subBoundsEl.addElement("IsDisplayed").addText(boundaryTableModel[i][j].getValueAt(k, 0).toString());
						subBoundsEl.addElement("Color").addText(Integer.toString(((Color)boundaryTableModel[i][j].getValueAt(k, 1)).getRGB()));
					}
				}
			}
		}
			//		for(int i=0;i<eventList.size();i++)
			//		{
			//			Element eventAttEl = propertyEl.addElement("EventList");
			//			eventAttEl.addElement("SHPFile").addText((eventList.get(i).getSHPFile()==null)?"null":eventList.get(i).getSHPFile());
			//			eventAttEl.addElement("EventName").addText(eventList.get(i).getEventName());
			//			eventAttEl.addElement("ID").addText(eventList.get(i).getID());
			//			eventAttEl.addElement("LegendTitle").addText((eventList.get(i).getLegendTitle()==null)?"null":eventList.get(i).getLegendTitle());
			//			eventAttEl.addElement("DBFFile").addText((eventList.get(i).getDBFFile()==null)?"null":eventList.get(i).getDBFFile());
			//			eventAttEl.addElement("LikeEarthquake").addText((eventList.get(i).getLikeEarthquake()==null)?"null":eventList.get(i).getLikeEarthquake());
			//
			//			eventAttEl.addElement("Column").addText((eventList.get(i).getColumn()==null)?"null":eventList.get(i).getColumn());
			//		}
//			for(int num:selected)
//			{
//				propertyEl.addElement("id").addText("" + num);
//				//sub bounds of hazus upper tab
//				for(int i=0;i<allBounds.size();i++)
//				{
//					Element allBoundsEl = propertyEl.addElement("allBounds");
//					allBoundsEl.addElement("name").addText(allBounds.get(i).getName());
//					allBoundsEl.addElement("isDisplayed").addText(Boolean.toString(allBounds.get(i).isDisplayed()));
//				}
//
//			}

		}
		@Override
		public void toXML(Element stateEl) {
			copyDetails();
			createElement(stateEl);
		}

		@Override
		public void fromXML(Element stateEl) {
		
			int cti=0,ctj=0,ctk=0;
			for ( Iterator i = stateEl.elementIterator( "HAZUS" ); i.hasNext(); ) {
				Element e = (Element) i.next();
				cti=0;
				transparency = Float.parseFloat(e.elementText("transparency"));
				gradient[0] = Color.decode(e.elementText("color1"));
				gradient[1]= Color.decode(e.elementText("color2"));
				numFiles= Integer.parseInt(e.elementText("numFiles"));
				numLines= Integer.parseInt(e.elementText("numLines"));

				for ( Iterator j = e.elementIterator("Tab"); j.hasNext(); ) {
					Element eTab = (Element) j.next();
					ctj=0;
					for ( Iterator k = eTab.elementIterator("Bounds"); k.hasNext(); ) {
						Element eBounds = (Element) k.next();
						if(eBounds.element("SubBoundSize")!=null){
						int subBoundSize = Integer.parseInt(eBounds.element("SubBoundSize").getText());
						FilledBoundaryCluster[] b = new FilledBoundaryCluster[subBoundSize];	
						ctk=0;
						for ( Iterator l = eBounds.elementIterator("SubBounds"); l.hasNext(); ) {
							Element eSubBounds = (Element) l.next();
							b[ctk] = new FilledBoundaryCluster();
							b[ctk].setName(eSubBounds.element("Name").getText());
							b[ctk].setDisplayed(Boolean.parseBoolean(eSubBounds.element("IsDisplayed").getText()));
							b[ctk].setColor(Color.decode(eSubBounds.element("Color").getText()));
							
//							boundaryTableModel[cti][ctj].setValueAt(, ctk, 2);
//							boundaryTableModel[cti][ctj].setValueAt(, ctk, 0);
//							boundaryTableModel[cti][ctj].setValueAt(, ctk, 1);
							ctk++;
						}
						boundaryTableModel[cti][ctj]= new BoundaryTableModel(b);
						selected.add(ctj);
						}
						ctj++;
					}
					cti++;
					
				}
//				for ( Iterator k = e.elementIterator("EventList"); k.hasNext(); ) {
//					Element eSubEventAtt = (Element) k.next();
//					event = new EventAttributes();
//					event.setDBFFile(eSubEventAtt.elementText("DBFFile").equals("null")?null:eSubEventAtt.elementText("DBFFile"));
//					event.setEventName(eSubEventAtt.elementText("EventName"));
//					event.setID(eSubEventAtt.elementText("ID"));
//					event.setLegendTitle(eSubEventAtt.elementText("LegendTitle").equals("null")?null:eSubEventAtt.elementText("LegendTitle"));
//					event.setLikeEarthquake(eSubEventAtt.elementText("LikeEarthquake").equals("null")?null:eSubEventAtt.elementText("LikeEarthquake"));
//					event.setSHPFile(eSubEventAtt.elementText("SHPFile").equals("null")?null:eSubEventAtt.elementText("SHPFile"));
//					event.setColumn(eSubEventAtt.elementText("Column").equals("null")?null:eSubEventAtt.elementText("Column"));
//					eventList.add(event);
//				}
//				for ( Iterator j = e.elementIterator("id"); j.hasNext(); ) {
//					Element eSub = (Element) j.next();
//					selected.add(Integer.parseInt(eSub.getText()));
//				}

			}
			//		for(int i = 0;i < selected.size();i++)
			//		{
			//			this.parent.drawEvent(selected.get(i));
			//		}

		}

		@Override
		public PluginState deepCopy() {
			GISHazusEventsPluginState state = new GISHazusEventsPluginState(parent);
			state.copyDetails();
			return state;
		}

	}
