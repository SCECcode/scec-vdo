package org.scec.vtk.drawingTools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;


import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.utils.DataAccessor;
import org.scec.vtk.plugins.utils.components.AddButton;
import org.scec.vtk.plugins.utils.components.ColorButton;
import org.scec.vtk.plugins.utils.components.EditButton;
import org.scec.vtk.plugins.utils.components.RemoveButton;
import org.scec.vtk.plugins.utils.components.ShowButton;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;

import vtk.vtkTextActor3D;

public class DrawingToolsGUI extends JPanel implements ActionListener, ListSelectionListener, TableModelListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel drawingToolSubPanelLower;
	private ShowButton showDrawingToolsButton;
	private ColorButton colorDrawingToolsButton;
	private SingleColorChooser colorChooser;
	private AddButton addDrawingToolsButton;
	private RemoveButton remDrawingToolsButton;
	private DrawingToolsTable drawingToolTable;
	private EditButton editDrawingToolsButton;
	
	private DisplayAttributes displayAttributes;
	private DefaultLocationsGUI defaultLocations;
	private static String[] columnNames = {"Show","",
			"Label",
			"Size",
			"Lat",
			"Lon",
		"Alt"};
	public LabelTableModel labelModel = new LabelTableModel(columnNames);
	private JPanel drawingToolSubPanelLowest;
	
	
	public DrawingToolsGUI(DrawingToolsPlugin plugin){
		
		//setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()));
		setName("Drawing Tools");
		this.drawingToolTable = new DrawingToolsTable(this);
		new DrawingToolsTableModel();
		
		defaultLocations = new DefaultLocationsGUI(plugin, this);
		JScrollPane drawingToolSubPanelUpper = new JScrollPane();
		drawingToolSubPanelUpper.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		drawingToolSubPanelUpper.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		drawingToolSubPanelUpper.setPreferredSize(new Dimension(200, 100));
		drawingToolSubPanelUpper.setViewportView(defaultLocations);

		displayAttributes = new DisplayAttributes();
		displayAttributes.latField.addActionListener(this);
		displayAttributes.lonField.addActionListener(this);
		displayAttributes.altField.addActionListener(this);
		displayAttributes.rotateXField.addActionListener(this);
		displayAttributes.rotateYField.addActionListener(this);
		displayAttributes.rotateZField.addActionListener(this);
		displayAttributes.fontSizeField.addActionListener(this);
		
		JPanel displayPanel = new JPanel();
		displayPanel.setLayout(new BoxLayout(displayPanel,BoxLayout.Y_AXIS));
		displayPanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
	
		this.drawingToolSubPanelLowest = new JPanel(new BorderLayout(0,0));
	    this.drawingToolSubPanelLowest.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));    
		this.drawingToolSubPanelLowest.add(displayAttributes);
		displayPanel.add(drawingToolSubPanelUpper);//upper level
		displayPanel.add(getDrawingToolLibraryPanel());//mid level
		displayPanel.add(this.drawingToolSubPanelLowest);//lowest level
		add(displayPanel);
	}
	public DrawingToolsTable getTable()
	{
		return this.drawingToolTable;
	}
	private JPanel getDrawingToolLibraryPanel() {

	    // set up panel
	    this.drawingToolSubPanelLower = new JPanel(new BorderLayout(0,0));
	    this.drawingToolSubPanelLower.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	    this.drawingToolSubPanelLower.setName("Library");
	    this.drawingToolSubPanelLower.setOpaque(false);
	    
	    // set up scroll pane
	    JScrollPane scroller = new JScrollPane();
	    scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	   
	    scroller.setViewportView(this.drawingToolTable);
	    scroller.getViewport().setBackground(this.drawingToolTable.getBackground());
	    this.drawingToolSubPanelLower.add(scroller,BorderLayout.CENTER);
	    this.drawingToolSubPanelLower.add(getDrawingToolLibraryBar(),BorderLayout.PAGE_END);
	    
	    
	    return this.drawingToolSubPanelLower;
	}
	private JPanel getDrawingToolLibraryBar() {

	    this.showDrawingToolsButton = new ShowButton(this, "Toggle visibility of selected Text(s)");
	    this.colorDrawingToolsButton = new ColorButton(this, "Change color of selected Text(s)");
	    //this.meshDrawingToolsButton = new MeshButton(this, "Toggle mesh state of selected DrawingTool(s)s");
	    this.editDrawingToolsButton = new EditButton(this, "Edit DrawingTool information");
	    this.addDrawingToolsButton = new AddButton(this, "Add new Text");
	    this.remDrawingToolsButton = new RemoveButton(this, "Remove selected Text(s)");

	    JPanel bar = new JPanel();
	    bar.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	    bar.setLayout(new BoxLayout(bar,BoxLayout.LINE_AXIS));
	    bar.setOpaque(true);
	    int buttonSpace = 3;

	    bar.add(this.showDrawingToolsButton);
	    bar.add(Box.createHorizontalStrut(buttonSpace));
	    bar.add(this.colorDrawingToolsButton);
	    bar.add(Box.createHorizontalStrut(buttonSpace));
	    //bar.add(this.meshDrawingToolsButton);
	   // bar.add(Box.createHorizontalStrut(buttonSpace));
	    bar.add(this.editDrawingToolsButton);
	    bar.add(Box.createHorizontalGlue());
	    bar.add(Box.createHorizontalStrut(buttonSpace));
	    bar.add(Box.createHorizontalGlue());
	    //bar.add(this.savDrawingToolsButton);
	   // bar.add(this.editDrawingToolsButton);
	    //bar.add(Box.createHorizontalStrut(buttonSpace));
	    bar.add(this.addDrawingToolsButton);
	    bar.add(Box.createHorizontalStrut(buttonSpace));
	    bar.add(this.remDrawingToolsButton);

	    return bar;
	}
	
	   public DrawingTool addDrawingTool(DrawingTool drawingTool){
		   String text = "Text";
		   double[] pt= {Transform.calcRadius(37),37,-120};
		   ArrayList<DataAccessor> a = this.drawingToolTable.getLibraryModel().getAllObjects();
		   if(drawingTool.getTextString()==null)
		     {
			   drawingTool = new DrawingTool(pt[1],pt[2],pt[0],text,null);
		       drawingTool.setDisplayName(text +" -"+ Integer.toString(a.size()+1)); 
		     }
		   else
		   {
			   text = drawingTool.getTextString();
			   drawingTool.setDisplayName(drawingTool.getTextString() +" -"+ Integer.toString(a.size()+1));
			   pt[0]= pt[0]+drawingTool.getaltitude();
			   pt[1]= drawingTool.getLatitude();
			   pt[2]= drawingTool.getLongitude();
		   }
	    	 //this text is not facing the camera
		    vtkTextActor3D actor= new vtkTextActor3D();
		    actor.SetInput(text);
		    actor.GetTextProperty().SetFontSize(20);
	    	actor.SetPosition( Transform.customTransform(pt));
	    	actor.GetTextProperty().SetColor(1,0,0);
	    	
	    	DrawingTool.getMasterFaultBranchGroup().add(actor);
	        Info.getMainGUI().updateActors(DrawingTool.getMasterFaultBranchGroup());
			return drawingTool;
	    	 
	    }
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		DrawingToolsTableModel drawingTooltablemodel = this.drawingToolTable.getLibraryModel();
	    if (src == this.showDrawingToolsButton) {
	    	int[] selectedRows =  this.drawingToolTable.getSelectedRows();
	    	ArrayList<vtkTextActor3D> actors = DrawingTool.getMasterFaultBranchGroup();
            for(int i =0;i<selectedRows.length;i++)
            {
            	vtkTextActor3D actor = actors.get(selectedRows[i]-i);
            	if(actor.GetVisibility() == 1)
            		actor.SetVisibility(0);
            	else
            		actor.SetVisibility(1);
            }
            Info.getMainGUI().updateRenderWindow();
	    }
	    else if (src == this.editDrawingToolsButton) {
	        runObjectInfoDialog(this.drawingToolTable.getSelectedRows());
	    }
	    else if (src == this.addDrawingToolsButton) {
	    	 DrawingTool drawingToolObj = new DrawingTool();
	    	 drawingToolObj = addDrawingTool(drawingToolObj);
	    	 ArrayList<DrawingTool> newObjects = new ArrayList<>();
	    	 newObjects.add(drawingToolObj);
	    	 this.drawingToolTable.addDrawingTool(newObjects);
	    	 
	    }
	    if (src == this.remDrawingToolsButton) {
	    	int[] selectedRows = this.drawingToolTable.getSelectedRows();
	    	int delete = drawingTooltablemodel.deleteObjects(
	                this.drawingToolTable,
	                selectedRows);
	        if (delete == JOptionPane.NO_OPTION ||
	                delete == JOptionPane.CLOSED_OPTION) {
	        }
	        else
	        {
	        	//remove actors
	            ArrayList<vtkTextActor3D> actors = DrawingTool.getMasterFaultBranchGroup();
	            ArrayList<vtkTextActor3D> removedActors = new ArrayList<>();
	            for(int i =0;i<selectedRows.length;i++)
	            {
	            	vtkTextActor3D actor = actors.get(selectedRows[i]-i);
	            	removedActors.add(actor);
	            	DrawingTool.getMasterFaultBranchGroup().remove(selectedRows[i]-i);
	            }
	            Info.getMainGUI().removeActors(removedActors);
	        }
	    }
	    if (src == this.displayAttributes.latField || src == this.displayAttributes.lonField || src == this.displayAttributes.altField ) {
	    	int[] selectedRows =  this.drawingToolTable.getSelectedRows();
	    	ArrayList<vtkTextActor3D> actors = DrawingTool.getMasterFaultBranchGroup();
	        for(int i =0;i<selectedRows.length;i++)
	        {
	        	vtkTextActor3D actor = actors.get(selectedRows[i]-i);
	        	double[] pt= {Transform.calcRadius(Double.parseDouble((String) this.displayAttributes.latField.getText()))+Double.parseDouble((String) this.displayAttributes.altField.getText()),
	        			Double.parseDouble((String) this.displayAttributes.latField.getText()),
	        			Double.parseDouble((String) this.displayAttributes.lonField.getText())};
	        	actor.SetPosition(Transform.customTransform(pt));
	        }
	        Info.getMainGUI().updateActors(DrawingTool.getMasterFaultBranchGroup());
	    }
	    if (src == this.displayAttributes.rotateXField || src == this.displayAttributes.rotateYField || src == this.displayAttributes.rotateZField ) {
	    	int[] selectedRows =  this.drawingToolTable.getSelectedRows();
	    	ArrayList<vtkTextActor3D> actors = DrawingTool.getMasterFaultBranchGroup();
	        for(int i =0;i<selectedRows.length;i++)
	        {
	        	vtkTextActor3D actor = actors.get(selectedRows[i]-i);
	        	actor.SetOrigin(actor.GetCenter());
	        	actor.RotateX(Double.parseDouble((String) this.displayAttributes.rotateXField.getText()));
	        	actor.RotateY(Double.parseDouble((String) this.displayAttributes.rotateYField.getText()));
	        	actor.RotateZ(Double.parseDouble((String) this.displayAttributes.rotateZField.getText()));
	        }
	        Info.getMainGUI().updateActors(DrawingTool.getMasterFaultBranchGroup());
	    }
	    if (src == this.displayAttributes.fontSizeField ) {
	    	int[] selectedRows =  this.drawingToolTable.getSelectedRows();
	    	ArrayList<vtkTextActor3D> actors = DrawingTool.getMasterFaultBranchGroup();
	        for(int i =0;i<selectedRows.length;i++)
	        {
	        	vtkTextActor3D actor = actors.get(selectedRows[i]-i);
	        	actor.GetTextProperty().SetFontSize(Integer.parseInt((String) this.displayAttributes.fontSizeField.getText()));
	        }
	        Info.getMainGUI().updateActors(DrawingTool.getMasterFaultBranchGroup());
	    }
	    if (src == this.colorDrawingToolsButton) {
	        if (this.colorChooser == null) {
	            this.colorChooser = new SingleColorChooser(this);
	        }
	        Color newColor = this.colorChooser.getColor();
	        if (newColor != null) {
	        	double[] color = {newColor.getRed()/Info.rgbMax,newColor.getGreen()/Info.rgbMax,newColor.getBlue()/Info.rgbMax};
	            int[] selectedRows = this.drawingToolTable.getSelectedRows();
	            ArrayList<vtkTextActor3D> actors = DrawingTool.getMasterFaultBranchGroup();
		        for(int i =0;i<selectedRows.length;i++)
		        {
		        	vtkTextActor3D actor =actors.get(selectedRows[i]);
	            	//only between 0 and 1;
	            	actor.GetTextProperty().SetColor(color);
	    	    }
	            Info.getMainGUI().updateActors(DrawingTool.getMasterFaultBranchGroup());
	        }
	    }
	    
	}

	
	private void runObjectInfoDialog(int[] objects) {
		ArrayList<vtkTextActor3D> actors = DrawingTool .getMasterFaultBranchGroup();
	     String displayTextInput = JOptionPane.showInputDialog(
	    		 this.drawingToolTable,
	                "Change text:",
	                "Set Drawing Text",
	                JOptionPane.QUESTION_MESSAGE);
	        if (displayTextInput == null) return;
	        else
	        {
	        	for(int i =0;i<objects.length;i++)
	        	{
	    	    	vtkTextActor3D actor = actors.get(objects[i]);
	    	    	actor.SetInput(displayTextInput);
	        		Info.getMainGUI().updateActors(DrawingTool.getMasterFaultBranchGroup());
	        	}
	        }
		}
	
	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void valueChanged(ListSelectionEvent e) {
		 Object src = e.getSource();
		    if (e.getValueIsAdjusting()) return;

		   if (src == this.drawingToolTable.getSelectionModel()) {
		        processTableSelectionChange();
		    }
	}
	public void processTableSelectionChange() {
		  int[] selectedRows = this.drawingToolTable.getSelectedRows();
		    if (selectedRows.length > 0) {
		        this.remDrawingToolsButton.setEnabled(true);
		        this.editDrawingToolsButton.setEnabled(true);
		        this.colorDrawingToolsButton.setEnabled(true);
		        if (this.drawingToolTable.getLibraryModel().allAreLoaded(selectedRows)) {
		            enablePropertyEditButtons(true);
		        } else if (this.drawingToolTable.getLibraryModel().noneAreLoaded(selectedRows)) {
		            enablePropertyEditButtons(true);
		        } else {
		            enablePropertyEditButtons(true);
		        }
		    } else {
		        enablePropertyEditButtons(false);
		        this.remDrawingToolsButton.setEnabled(false);
		        this.editDrawingToolsButton.setEnabled(false);
		        this.colorDrawingToolsButton.setEnabled(false);
		    }

		   
	}
	private void enablePropertyEditButtons(boolean enable) {
		this.showDrawingToolsButton.setEnabled(enable);
	}
	

}
