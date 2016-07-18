package org.scec.vtk.plugins.GISHazusEventsPlugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.utils.components.ColorWellButton;
import org.scec.vtk.plugins.utils.components.GradientColorChooser;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.actors.AppendActors;

import javax.swing.JDialog;


import com.google.common.collect.Lists;


/**
* Created July 21,2011
* Builds and handles events for GIS NorhtRidge SHP files.
* Adapted from PolBoundGUI
*
* @author Miguel Villasana
* 
*/

class GISHazusEventsPluginGUI extends JPanel implements TableModelListener, ActionListener, ChangeListener {
	private static final int REGION_AMT = 200;
 
    private FilledBoundaryCluster 		currentBoundary;
    private ArrayList<FilledBoundaryCluster> 		polArray;
    private int 		numOfBoundaries;     
	
	private static final long serialVersionUID = 1L;
	private ArrayList<String> subgroupNames;
	protected ArrayList<JCheckBox> 		checkBoxes;
	private Events	bTrace;
    protected BoundSectionsTableModel tableModel;
    protected BoundSectionsTableModel tableModel2;
    private BoundSectionsTable table;
    protected BoundaryTableModel[] boundaryTableModel = new BoundaryTableModel[REGION_AMT];
    private BoundaryTable[] boundaryTable = new BoundaryTable[REGION_AMT];
    protected int[] boundaryRowOrder = new int[REGION_AMT];
    protected int[] boundaryGroupOrder = new int[REGION_AMT];
    protected int[] boundaryRowSize = new int[REGION_AMT];
    protected int[] boundaryStartIndex = new int[REGION_AMT];
    private int boundaryRowOrderCounter = 0;
    private JTabbedPane boundTabbedPane = new JTabbedPane();
	private ColorWellButton colorButton;
    private GradientColorChooser gradientColor;
    private JButton legendButton;
    private JPanel legendDialog;
    private JDialog dialogLegend;
   	private JButton apply;
   	private JFileChooser saveFile;
   	private BufferedImage myImage;
   	private Graphics2D g2;
   	private File file;
   	private JPanel buttonPanel;
 	private JButton save;
 	private JButton info;
    private JSlider transparencySlider;
    
    /*The name for the tab is defined here and should be the same in the <like_earthquake> category
    in the XML file "QuakeEvents.xml" any new earthquakes that are added to the XML code with the
    same like earthquake defined below will automatically show up in the Hazus Plugin under the
    correct tab and be able to show the correct Hazus information.*/
    private String[] likeEarthquakes = {"Northridge", "Loma Prieta", "Next Like-Earthquake"};
    private int rowClicked;
    JTabbedPane groupsTabbedPane;
    private ArrayList<SetUpNewHazusTab> tabList;
    private ArrayList<TableModel> tableModelList;
    private int selectedEventRow;

	private JLabel legendColor ;		
	private JLabel legendColor1 ;		
	private JLabel legendColor2 ;	
	private JLabel legendColor3 ;
	private JLabel legendColor4 ;		
	private JLabel legendColor5 ;
	private JLabel legendColor6 ;		
	private JLabel legendColor7 ;
	private JLabel legendColor8 ;
	private JLabel legendColor9 ;
	private ArrayList<JLabel> legendColorList;
	JLabel note;
	PluginActors hazusPluginActors;

	private AppendActors appendHazusActor;

    
	public GISHazusEventsPluginGUI(PluginActors pluginActors) {
		super(); 
		hazusPluginActors=pluginActors;
		bTrace = new Events();	
		appendHazusActor = new AppendActors();
		bTrace.setAppendActors(appendHazusActor);
		
		for(int i = 0; i < boundaryRowOrder.length; i++)
		{
			boundaryGroupOrder[i] = -1;
			boundaryRowOrder[i] = -1;
			boundaryRowSize[i] = -1;
			boundaryStartIndex[i] = -1;
		}
		numOfBoundaries = 0;
		subgroupNames = bTrace.buildBoundaryNames();
		checkBoxes = new ArrayList<JCheckBox>(numOfBoundaries);
		// sets layout of the whole gui, which is a JPanel itself
		this.setLayout(new BorderLayout()); 
		this.setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()));
		
		/************
		*Upper Panel*
		************/
		// subgroups tab
		groupsTabbedPane = new JTabbedPane();
		groupsTabbedPane.setBorder(BorderFactory.createEmptyBorder(15,10,10,10));
		groupsTabbedPane.setPreferredSize(new Dimension(400, 240));
		tabList = new ArrayList<SetUpNewHazusTab>();
		groupsTabbedPane.getSelectedIndex();
		tableModelList = new ArrayList<TableModel>();
		for (String quake : likeEarthquakes){
			SetUpNewHazusTab newTab = new SetUpNewHazusTab();
			tabList.add(newTab);
			ArrayList<String> continentList = new ArrayList<String>();
			//ArrayList<String> continentList2 = new ArrayList<String>();
			for(int i =0; i<subgroupNames.size(); i++){
				if (quake.equalsIgnoreCase(bTrace.getLikeEarthquake(i))){
					continentList.add(subgroupNames.get(i));
				}
				else if (bTrace.getEventName(i).equalsIgnoreCase("Import")){
					continentList.add(subgroupNames.get(i));
				}
			}
			groupsTabbedPane.add(newTab.getTab(), quake);
			tableModel = new BoundSectionsTableModel(continentList, this);
			table = new BoundSectionsTable(this, tableModel);
			table.getModel().addTableModelListener(this);
			newTab.getSubGroupPanel().add(table);
			tableModelList.add(tableModel);
		}
		
		add(groupsTabbedPane, BorderLayout.PAGE_START);
		gradientColor = new GradientColorChooser(this);
		Color[] purpleGradient = bTrace.getPurpleGradient();
		colorButton = new ColorWellButton(purpleGradient[0], purpleGradient[9], 74, 16);
		colorButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e) {
            	// check needed
            	Color[] purpleGradient = bTrace.getPurpleGradient();
            	Color[] newColor = gradientColor.getColors(purpleGradient[0], purpleGradient[9]);
            	if(newColor!= null){
    			colorButton.setColor(newColor[0], newColor[1]);
    			Color[] newGradient = bTrace.setColorGradient(colorButton.getColor1(), colorButton.getColor2());    			
    			for(int i = 0; i < REGION_AMT; i++)
    				setColor(i, newGradient);
            }
            }
		});

		  JPanel colorHolder = new JPanel();
	        colorHolder.add(new JLabel("Legend Gradient"));
	        colorHolder.add(colorButton);
	        add(colorHolder, BorderLayout.SOUTH);
//	        add(hazusOptions, BorderLayout.PAGE_START);
	  
		legendButton = new JButton("Show Legend");
		legendButton.addActionListener(this);
		legendButton.setEnabled(false);
		
		legendColorList = new ArrayList<JLabel>();
		legendColorList.add(legendColor);
		legendColorList.add(legendColor1);
		legendColorList.add(legendColor2);
		legendColorList.add(legendColor3);
		legendColorList.add(legendColor4);
		legendColorList.add(legendColor5);
		legendColorList.add(legendColor6);
		legendColorList.add(legendColor7);
		legendColorList.add(legendColor8);
		legendColorList.add(legendColor9);
		
		colorHolder.add(legendButton);
		//Transparency Panel being added to GUI
		colorHolder.add(getTransparencyPanel());

        
	}

	public class MyChangeAction implements ChangeListener{
	    public void stateChanged(ChangeEvent ce){
	    	//thickness = slider value
	    	int value = 0;
	    	//get array list of all boundaries
	    	ArrayList<FilledBoundary> boundaries = bTrace.getAllBoundaries();
	    	//set width of all plate boundaries
	    	for(FilledBoundary boundary: boundaries)
	    	  boundary.setLineApperance(bTrace.getAllBoundaries().get(0).getColor(),(float)value);
	    }
	  }
	

	private JPanel getTransparencyPanel() {
		// transparency slider
		transparencySlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		transparencySlider.addChangeListener(this);
		transparencySlider.setMajorTickSpacing(10);
		transparencySlider.setMinorTickSpacing(5);
		transparencySlider.setPaintLabels(true);
		transparencySlider.setPaintTicks(true);
		transparencySlider.setToolTipText("Set transparency level "
				+ "(0 = Opaque; 100 = Transparent)");
		transparencySlider.setPreferredSize(new Dimension(350, 80));
		transparencySlider.setEnabled(false);

		// Transparency panel
		JPanel transparencyPanel = new JPanel();
		transparencyPanel.setBorder(BorderFactory
				.createTitledBorder("Transparency"));
		transparencyPanel.add(transparencySlider);

		return transparencyPanel;
	}
	
	private void createLowerPanel(String tabName, int row, int startIndex){
		//BoxLayout is a class that guides the placement of the checkboxes as you add them		
		boundTabbedPane.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
		FilledBoundaryCluster[] b = new FilledBoundaryCluster[numOfBoundaries-startIndex];
		for(int i = startIndex; i < numOfBoundaries; i++)
		{
			if(polArray.get(i) == null)
			{
				polArray.remove(i);
			}
			b[i-startIndex] = polArray.get(i);
		}
		boundaryTableModel[row] = null;
		boundaryTableModel[row] = new BoundaryTableModel(b);
		boundaryTable[row] = null;
		boundaryTable[row] = new BoundaryTable(this, boundaryTableModel[row]);
		boundaryTable[row].getModel().addTableModelListener(this);
		JPanel polLibraryPanel = new JPanel();
		polLibraryPanel.setLayout(new BoxLayout(polLibraryPanel, BoxLayout.Y_AXIS));
		polLibraryPanel.add(boundaryTable[row]);		
		
		
		for(int i =startIndex; i<numOfBoundaries; i++){
			checkBoxes.add(new JCheckBox(polArray.get(i).getName().replace('_', ' ')));
		}	
		
		JScrollPane scroller = new JScrollPane(polLibraryPanel);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        //set scroll speed
        JScrollBar bar = scroller.getVerticalScrollBar();
        bar.setBlockIncrement(20);
        bar.setUnitIncrement(20);
        scroller.setVerticalScrollBar(bar);
        boundTabbedPane.add(scroller, tabName);
        add(boundTabbedPane, BorderLayout.CENTER);  


	}
	
	
	/***********************
	* GUI building methods *
	***********************/	

	public int getNumContinents() {
		return this.tableModel.getRowCount();
	}
	
	public boolean isContinentSelected(int i) {
		return ((Boolean)tableModel.getValueAt(i,0)).booleanValue();
	}
	public boolean isContinentLoaded(int i) {
		return ((Boolean)tableModel.getValueAt(i,3)).booleanValue();
	}
	public void setContinentSelected(int i) {
		tableModel.setValueAt(new Boolean(true),i,0);
	}
	public void setContinentLoaded(int i){
		tableModel.setValueAt(new Boolean(true),i,0);
		tableModel.setValueAt(new Boolean(false),i,0);
	}
	
	/********************
	* Subgroup methods  *
	********************/
	/** 
	 *Turns on a predefined set of boundaries
	 */
	public void predefinedSubGroup(int subgroupNum, boolean isSelected) {
		System.out.println(subgroupNum);
		int index = this.boundaryRowOrder[subgroupNum];
		if(index != -1)
		{
			boolean[] shown = new boolean[this.boundaryRowSize[index]];
			for(int i = boundaryStartIndex[subgroupNum]; i < boundaryRowSize[index] + boundaryStartIndex[subgroupNum]; i++){
				shown[i-boundaryStartIndex[subgroupNum]] = true;
			}
			toggleSubGroup(shown, isSelected, subgroupNum);
		}
	}
	
/** 
	 *Turns on a predefined set of boundaries
	 */
	public void setColor(int subgroupNum, Color color) {
		int index = this.boundaryRowOrder[subgroupNum];
		Color color3f = color;
		//make sure the group is actually loaded
		if(index != -1) {
			for(int i = boundaryStartIndex[subgroupNum]; i < boundaryRowSize[boundaryRowOrder[subgroupNum]] + boundaryStartIndex[subgroupNum]; i++){
				polArray.get(i).setColor(color3f);	
			}
			this.paintAll(this.getGraphics());
		}
	}
	
	public void setColor(int subgroupNum, Color[] color) {
		int index = this.boundaryRowOrder[subgroupNum];
		//make sure the group is actually loaded
		if(index != -1) {
			for(int i = boundaryStartIndex[subgroupNum]; i < boundaryRowSize[boundaryRowOrder[subgroupNum]] + boundaryStartIndex[subgroupNum]; i++){
				polArray.get(i).setColor(color[polArray.get(i).getCategory()]);	
			}
			this.paintAll(this.getGraphics());
		}
	}
	
	public void setColorGradient() {

	}
	
	public void toggleSubGroup(boolean[] shown, boolean turnGroupOn, int subgroupNum){
		if(shown.length != this.boundaryRowSize[this.boundaryRowOrder[subgroupNum]])
			System.out.println("Error: Subgroup does not account for all boundaries");
		for(int i = boundaryStartIndex[subgroupNum]; i<shown.length + boundaryStartIndex[subgroupNum];i++){ 
			JCheckBox temp;				
			if(shown[i-boundaryStartIndex[subgroupNum]] == true){
				temp = checkBoxes.get(i);
				if(turnGroupOn){ 
					if(! temp.isSelected()) //only turn on if not already on (and vice versa)
					{
						temp.setSelected(true);
						refreshCheckbox(i);
					}
				}
				else{
					if( temp.isSelected())
					{
						temp.setSelected(false);
						refreshCheckbox(i);
					}
				}
			}		
			else
			{
				temp = checkBoxes.get(i);
				temp.setSelected(false);
				refreshCheckbox(i);
			}
		}	
	}
	public boolean[] getBoundries(){
		boolean[] shown = new boolean[this.numOfBoundaries];
		for(int i=0; i<numOfBoundaries; i++)
		{
			if(polArray.get(i).isDisplayed() == true)
			{
				shown[i] = true;
			}
			else
			{
				shown[i] = false;
			}
		}
		return shown;
	}
	public int getPolSize(){
		return numOfBoundaries;
	}
	public String getBoundryName(int row)
	{
		String hwyName;
		FilledBoundaryCluster tempHighway = polArray.get(row);
		hwyName = tempHighway.getName();
		return hwyName;
	}
	
	public ArrayList<FilledBoundaryCluster> getPolArray()
	{
		return this.polArray;
	}

	
	/******************
	* Event Handling  *
	******************/

	
	public void tableChanged(TableModelEvent e) {
		rowClicked = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = null;
        
//        int selectedPane = groupsTabbedPane.getSelectedIndex();
//    	
//    	String likeEarthquake = likeEarthquakes[selectedPane];
//    	ArrayList<String> eventsLikeList = new ArrayList<String>();
//    	eventsLikeList = bTrace.makeLikeList(likeEarthquake);
//    	String eventName = eventsLikeList.get(rowClicked-1);
//    	rowClicked = bTrace.getIndexByEventName(eventName);
    	
        try
        {
        	model = (BoundaryTableModel)e.getSource();
           	BoundaryTableModel btm = (BoundaryTableModel)e.getSource();
        	 
            Object data = model.getValueAt(rowClicked, column);
            
            if(column==0)
            {
            	int tabNumber = 0;
	            for(int i = 0; i < REGION_AMT; i++)
	            {
	            	if(boundaryTable[i] != null)
	            	{
	            		if(btm.equals(boundaryTable[i].model))
	            		{
	            			tabNumber = i;
	            		}
	            	}
	            }
	            for(int i = 1; i<boundaryRowOrder[tabNumber]; i++){
	            	rowClicked += boundaryRowSize[i-1];
	            }
                if((Boolean)data == true)
                {
                	checkBoxes.get(selectedEventRow).setSelected(true);
                	
                }
                else
                {
                	checkBoxes.get(selectedEventRow).setSelected(false);
                }           	
                refreshCheckbox(selectedEventRow);
             }
        }
        catch(ClassCastException ex1)
        {
        	int selectedPane = groupsTabbedPane.getSelectedIndex();
        	
        	String likeEarthquake = likeEarthquakes[selectedPane];
        	ArrayList<String> eventsLikeList = new ArrayList<String>();
        	eventsLikeList = bTrace.makeLikeList(likeEarthquake);
        	String eventName = eventsLikeList.get(rowClicked);
        	selectedEventRow = bTrace.getIndexByEventName(eventName);
        	
        	try
        	{
        		model = tableModelList.get(selectedPane);
        		BoundSectionsTableModel btm = (BoundSectionsTableModel)e.getSource();
        		Object data = model.getValueAt(rowClicked, column);
        		
        		if(column == 0)
        		{
        			if((Boolean)data)
        			{
        				if((Boolean)btm.getValueAt(rowClicked, 3))//checks to see if it's in memory
        				{
        					//countries already loaded, don't need to do anything
        					transparencySlider.setEnabled(true);
        				}
        				else
        				{
        					int startIndex = 0;
        					int sizeIncrease = 0;
        					if(polArray == null){
        						polArray = bTrace.buildSelectedBoundary(selectedEventRow);
        						
    
        					}
        					else{
        						startIndex = polArray.size();
//        						polArray.addAll(bTrace.buildSelectedBoundary(rowClicked));
        						ArrayList<FilledBoundaryCluster> temp = bTrace.buildSelectedBoundary(selectedEventRow);
  
        						for (int i = 0; i < temp.size(); i++) {
        							polArray.add(temp.get(i));
 
        						}
        					}
        					transparencySlider.setEnabled(true);
        					sizeIncrease = polArray.size() - startIndex;
        					boundaryRowOrder[rowClicked] = boundaryRowOrderCounter;//rowClicked
        					boundaryRowSize[boundaryRowOrderCounter] = sizeIncrease;
        					boundaryGroupOrder[boundaryRowOrderCounter] = rowClicked; //used for save state
        					boundaryStartIndex[rowClicked] = startIndex;//rowClicked
        					boundaryRowOrderCounter++;
        					btm.setValueAt((Boolean)true, rowClicked, 3);//rowClicked
        					numOfBoundaries = polArray.size();
        					createLowerPanel(bTrace.getName(selectedEventRow), rowClicked, startIndex);//rowClicked
        					predefinedSubGroup(rowClicked, true);
        					dialogLegend = new JDialog();
        					buttonPanel = new JPanel();
        					legendDialog = new JPanel();
        					buttonPanel.setSize(250,320);
        					dialogLegend.setSize(420,340);
        					dialogLegend.setLocationRelativeTo(this);
        					legendDialog.setLayout(null);
        
        					buttonPanel.setLayout(new BoxLayout(	buttonPanel,BoxLayout.X_AXIS));
        					dialogLegend.setLayout(new BoxLayout(	dialogLegend.getContentPane(),BoxLayout.Y_AXIS));
        					
        					save = new JButton("Save");
        					save.addActionListener(this);
        					
        					info = new JButton("Info");
        					info.addActionListener(this);
        					
        					buttonPanel.add(save);
        					buttonPanel.add(info);
        					
        					//vtkPolyDataMapper mapper = new vtkPolyDataMapper();
        					//mapper.SetInputData(bTrace.getAppendActor().GetOutput());
        					//vtkActor actor = new vtkActor();
        					//actor.SetMapper(mapper);
//        					actor.GetProperty().EdgeVisibilityOn();
//        					actor.GetProperty().SetEdgeColor(0,1,1);
        					hazusPluginActors.addActor(bTrace.getAppendActor().getAppendedActor());
        					Info.getMainGUI().updateRenderWindow();
        					/*
        				   Font font = new Font("Times New Roman", Font.BOLD, 18);
        				   JLabel rate = new JLabel(bTrace.getName(rowClicked));
        					rate.setFont(font);
        					rate.setBounds(10,10,420,20);
        					legendDialog.add(rate);*/
        				}
        			}
        		}
        	}
        	
        	catch(Exception e1){}
        }
        legendButton.setEnabled(true);
        this.paintAll(this.getGraphics());
	}
	
	protected void refreshCheckbox(int position){
			currentBoundary = polArray.get(position);
		
			if (this.checkBoxes.get(position).isSelected() == true) {
				if (currentBoundary != null) {
					try{
						
					polArray.get(position).setDisplayed(true);
					}catch(Exception e)
					{
						System.out.println("Exception: " + e);
					}
				} else
					System.out.println("null boundary");
			} else {
				
				polArray.get(position).setDisplayed(false);
			}		
			currentBoundary = null;
		}

	
	public void actionPerformed(ActionEvent btnEvt) {
		if(btnEvt.getSource()==legendButton){
			legendDialog.removeAll();
			legendDialog.repaint();
			
			Color[] purpleGradient = bTrace.getPurpleGradient();
			ArrayList<Float> legendMaxList = bTrace.getLegendMax();
			legendDialog.updateUI();
			JLabel title = new JLabel();
			
			/*In order for the title of the legend to match the specific event this if statement is needed. Each dbf file has a particular column
			that the Events class is getting the information from. These column names can help for a few certain things, in this case it helps set
			the right title.*/
			if (bTrace.getLegendTitle(selectedEventRow).equals("Direct Building Economic Loss")){
				title.setText("Building Damage ($)");
			}
			else if (bTrace.getColumnAt(selectedEventRow).equalsIgnoreCase("TotalCount")){
				title.setText("Total Buildings");
			}
			else{
				title.setText("Injuries & Fatalities at " + bTrace.getLegendTitle(selectedEventRow));
			}
			title.setFont(new Font ("helvetica", Font.BOLD, 20));
			title.setForeground(Color.WHITE);
			title.setBounds(10,10,250,30);
			legendDialog.setBackground(Color.BLACK);
			legendDialog.add(title);
			
			/*Gets the number of maxes in legendMaxList. One does not simply use legentMaxList.size() because the number of maxes is the number
			of values not equal to -1.0. The number of maxes is used to set the bounds for the JLabels in the legend.*/
			int numMaxes = 0;
			for (int i = 0; i < 10; i++){
				if (legendMaxList.get(i) != -1.0){
					numMaxes += 1;
				}
			}
			
			ArrayList<JLabel> labels = Lists.newArrayList();
			if (bTrace.getLegendTitle(selectedEventRow).equals("Direct Building Economic Loss")
					|| bTrace.getLegendTitle(selectedEventRow).equals("Building Count")) {
				// economic loss
				labels.add(new JLabel(" $0"));
				labels.add(new JLabel(" $360 to $286,780"));
				labels.add(new JLabel(" $286,780 to $1,015,290"));
				labels.add(new JLabel(" $1,015,290 to $2,659,870"));
				labels.add(new JLabel(" $2,659,870 to $6,415,170"));
				labels.add(new JLabel(" $6,415,170 to $13,014,440"));
				labels.add(new JLabel(" $13,014,440 to $24,217,800"));
				labels.add(new JLabel(" $24,217,800 to $45,846,980"));
				labels.add(new JLabel(" $45,846,980 to $104,812,440"));
				labels.add(new JLabel(" $104,812,440 to $3,063,048,200"));
			} else {
				// injuries
				labels.add(new JLabel(" < 1 Injury or Fatality"));
				labels.add(new JLabel(" 1 to 2"));
				labels.add(new JLabel(" 2 to 3"));
				labels.add(new JLabel(" 3 to 4"));
				labels.add(new JLabel(" 4 to 7"));
				labels.add(new JLabel(" 7 to 11"));
				labels.add(new JLabel(" 11 to 19"));
				labels.add(new JLabel(" 19 to 36"));
				labels.add(new JLabel(" 36 to 83"));
				labels.add(new JLabel(" 83 to 4900"));
			}
			
			/*This for-loop fills in the legend. It kind of goes backwards in that it puts the lower colors in first and fills up to the title with
			the rest of the colors that a specific event requires. boundInc1 and boundInc2 are bounds that are incremented after each loop in
			order to make the legend look right.*/
//			int boundInc1 = 15 + 21 * numMaxes;
//			int boundInc2 = 110;
			int yPos = 45; // will be incremented by 20 for each row
			int xRightExtent = 260; // will be decremented by 10 for each row
			
			// draws from top to bottom, so iteration through in reverse
			for (int i=labels.size(); --i>=0;) {
				JLabel label = labels.get(i);
				label.setBackground(new Color (purpleGradient[i].getRed(), purpleGradient[i].getGreen(), purpleGradient[i].getBlue()));
				label.setForeground(Color.BLACK);
				label.setOpaque(true);
				
				label.setBounds(10, yPos, xRightExtent, 20);
				legendDialog.add(label);
				
				yPos += 20;
				xRightExtent -= 10;
			}
			
//			for (int i = 0; i < 10; i++){
//				if (bTrace.getLegendTitle(selectedEventRow).equals("Direct Building Economic Loss") || bTrace.getLegendTitle(selectedEventRow).equals("Building Count")){
//					System.out.println("poop");
//					economicLabels.get(i).setBackground(new Color (purpleGradient[i].x, purpleGradient[i].y, purpleGradient[i].z));
//					economicLabels.get(i).setForeground(Color.BLACK);
//					economicLabels.get(i).setOpaque(true);
//					if (bTrace.getLegendTitle(selectedEventRow).equals("Building Count")){
//						economicLabels.get(i).setBounds(10, boundInc1+165, boundInc2+20, 20);
//					}
//					else{
//						economicLabels.get(i).setBounds(10, boundInc1, boundInc2+20, 20);
//					}
//					legendDialog.add(economicLabels.get(i));
//				}
//				else{
//					injuryLabels.get(i).setBackground(new Color (purpleGradient[i].x, purpleGradient[i].y, purpleGradient[i].z));
//					injuryLabels.get(i).setForeground(Color.BLACK);
//					injuryLabels.get(i).setOpaque(true);
//					injuryLabels.get(i).setBounds(10, boundInc1, boundInc2, 20);
//					legendDialog.add(injuryLabels.get(i));
//				}
//				
//				boundInc1 -= 20;
//				boundInc2 += 10;
//			}
			
			bTrace.resetLegendMax();
		}
			//Show legend GUI

		else{
			legendColor = new JLabel(String.valueOf(bTrace.populationCategory.get(0)) + " to " + String.valueOf(bTrace.populationCategory.get(1)));		
			legendColor1 = new JLabel(String.valueOf(bTrace.populationCategory.get(1)) + " to " + String.valueOf(bTrace.populationCategory.get(2)));		
			legendColor2 = new JLabel(String.valueOf(bTrace.populationCategory.get(2)) + " to " + String.valueOf(bTrace.populationCategory.get(3)));	
			legendColor3 = new JLabel(String.valueOf(bTrace.populationCategory.get(3)) + " to " + String.valueOf(bTrace.populationCategory.get(4)));
			legendColor4 = new JLabel(String.valueOf(bTrace.populationCategory.get(4)) + " to " + String.valueOf(bTrace.populationCategory.get(5)));		
			legendColor5 = new JLabel(String.valueOf(bTrace.populationCategory.get(5)) + " to " + String.valueOf(bTrace.populationCategory.get(6)));
			legendColor6 = new JLabel(String.valueOf(bTrace.populationCategory.get(6)) + " to " + String.valueOf(bTrace.populationCategory.get(7)));		
			legendColor7 = new JLabel(String.valueOf(bTrace.populationCategory.get(7)) + " to " + String.valueOf(bTrace.populationCategory.get(8)));
			legendColor8 = new JLabel(String.valueOf(bTrace.populationCategory.get(8)) + " to " + String.valueOf(bTrace.populationCategory.get(9)));
			legendColor9 = new JLabel(String.valueOf(bTrace.populationCategory.get(9)) + " to " + Events.maxPop);
		}	
		
		dialogLegend.add(legendDialog);
		dialogLegend.add(buttonPanel);
		legendDialog.setVisible(true);
		dialogLegend.setVisible(true);
		
		if(btnEvt.getSource()==apply){
			dialogLegend.dispose();
		}
		
		if(btnEvt.getSource()==save){
		saveFile = new JFileChooser();
		int returnvalue = saveFile.showSaveDialog(this);
			if(returnvalue==JFileChooser.APPROVE_OPTION){
				file = saveFile.getSelectedFile();
				String filename;
				if(file.toString().contains("."))
					filename = file.toString();
				else
					filename = file.toString() + ".PNG ";
				myImage = new BufferedImage(legendDialog.getWidth(),legendDialog.getHeight(), BufferedImage.TYPE_INT_RGB);
			g2 = myImage.createGraphics();
			legendDialog.setSize(legendDialog.getSize());
			legendDialog.paint(g2);
			
			File outputFile = new File(filename);
			try {
					ImageIO.write(myImage, "png" , outputFile);		
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			}	
		}
		
		if(btnEvt.getSource()==info){
			createWindow();
		}
	}

	private void createWindow() {
		JFrame frame = new JFrame("Help"); 
		JLabel textLabel = new JLabel("<html><h1>How to add legend to the canvas</h1><br>1.Click Save button.<br>2.Open the <b>'Legend Plugin'</b> under Displays->Legends&Labels.<br>3.Click <i>ADD IMAGE</i> button.<br><br<br<br><br><br><br>Dev13 #ThoseBooleansThough",SwingConstants.CENTER); textLabel.setPreferredSize(new Dimension(500, 300)); 
		frame.getContentPane().add(textLabel, BorderLayout.CENTER);
		frame.setLocationRelativeTo(null); 
		frame.pack();
		frame.setVisible(true);

	
		
	}

	// State Changed =
	@Override
	public void stateChanged(ChangeEvent e) {
		// get array list of all boundaries
				ArrayList<FilledBoundary> boundaries = bTrace.getAllBoundaries();
				Object src = e.getSource();
		      
				// if the transparency slider is moved...
				if (src == transparencySlider) {
				
					float transparency = ((float) transparencySlider.getValue()) / 100.0f;
				
					System.out.println("transparency: "+ transparency);
					for (FilledBoundary boundary : boundaries)
					{	

						boundary.setTransparency(transparency);

					}

					
					
				}
		
	}
	
}	