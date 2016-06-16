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

import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkProp;
import vtk.vtkConeSource;
import vtk.vtkGlyph3D;
import vtk.vtkLabelPlacementMapper;
import vtk.vtkObject;
import vtk.vtkPointSetToLabelHierarchy;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkStringArray;

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
	
	private int  numText =0;
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
		  //indivisual text as actors
		   String text = "Text";
		   double[] pt= {Transform.calcRadius(37),37,-120};
		   ArrayList<DataAccessor> a = this.drawingToolTable.getLibraryModel().getAllObjects();
		   if(drawingTool.getTextString()==null)
		     {
			   drawingTool = new DrawingTool(pt[1],pt[2],pt[0],text,null);
		       drawingTool.setDisplayName(text +" -"+ numText++); 
		     }
		   else
		   {
			   text = drawingTool.getTextString();
			   drawingTool.setDisplayName(drawingTool.getTextString() +" -"+ numText++);
			   pt[0]= pt[0]+drawingTool.getaltitude();
			   pt[1]= drawingTool.getLatitude();
			   pt[2]= drawingTool.getLongitude();
		   }

		   //text as label facing camera
		   vtkStringArray labels =new vtkStringArray();
			labels.SetName("labels");
			labels.SetNumberOfValues(1);
			labels.SetValue(0, text);
		   	vtkPoints labelPoints = new vtkPoints();
		   	labelPoints.InsertNextPoint(Transform.customTransform(pt));
		   	
			
        	//create a pin near the text to mark the location 

        	vtkPolyData pinPolydata = new  vtkPolyData();
        	pinPolydata.SetPoints(labelPoints);

        	// Use sphere as glyph source.
        				vtkConeSource balls = new vtkConeSource();
        				balls.SetRadius(5);
        				balls.SetHeight(10);
        				balls.SetDirection(-Transform.customTransform(pt)[0],-Transform.customTransform(pt)[1],-Transform.customTransform(pt)[2]);
        				balls.SetResolution(10);
        				//balls.SetDirection(0,0,-1);
        				vtkGlyph3D glyphPoints = new vtkGlyph3D();
        				glyphPoints.SetInputData(pinPolydata);
        				glyphPoints.SetSourceConnection(balls.GetOutputPort());

        				vtkPolyDataMapper pm = new vtkPolyDataMapper();
        				pm.SetInputConnection(glyphPoints.GetOutputPort());
        	
		   	vtkPolyData temp = new vtkPolyData();
		   	temp.SetPoints(labelPoints);
		   	temp.GetPointData().AddArray(labels);
        	
			vtkPointSetToLabelHierarchy pointSetToLabelHierarchyFilter =new vtkPointSetToLabelHierarchy();
			pointSetToLabelHierarchyFilter.SetInputData(temp);
			pointSetToLabelHierarchyFilter.GetTextProperty().SetJustificationToLeft();
			pointSetToLabelHierarchyFilter.SetLabelArrayName("labels");
			//pointSetToLabelHierarchyFilter.SetInputConnection(pinSource.GetOutputPort());
			pointSetToLabelHierarchyFilter.Update();
			pointSetToLabelHierarchyFilter.GetTextProperty().SetFontSize(Integer.parseInt(displayAttributes.fontSizeField.getText()));
			
			
			vtkLabelPlacementMapper cellMapper = new vtkLabelPlacementMapper();
			cellMapper.SetInputConnection(pointSetToLabelHierarchyFilter.GetOutputPort());
			
		      	
        	vtkActor2D actor = new vtkActor2D();
        	actor.SetMapper(cellMapper);
			
        	vtkActor actorPin = new vtkActor();
        	actorPin.SetMapper(pm);
        	DrawingTool.getMasterFaultBranchGroup().add(actorPin);
	    	DrawingTool.getMasterFaultBranchGroup().add(actor);
	        Info.getMainGUI().addActors(DrawingTool.getMasterFaultBranchGroup());
			return drawingTool;
	    	 
	    }
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		DrawingToolsTableModel drawingTooltablemodel = this.drawingToolTable.getLibraryModel();
	    if (src == this.showDrawingToolsButton) {
	    	int[] selectedRows =  this.drawingToolTable.getSelectedRows();
	    	ArrayList<vtkObject> actors = DrawingTool.getMasterFaultBranchGroup();
            for(int i =0;i<selectedRows.length;i++)
            {
            	vtkProp actor = (vtkProp) actors.get((selectedRows[i]*2)+1);
            	vtkProp actorPin = (vtkProp) actors.get(selectedRows[i]*2);
            	if(actor.GetVisibility() == 1 && actorPin.GetVisibility() == 1)
            		{actor.SetVisibility(0); actorPin.SetVisibility(0);}
            	else
            		{actor.SetVisibility(1); actorPin.SetVisibility(1);}
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
	        	removeTextActors(selectedRows);
	        }
	    }
	    if (src == this.displayAttributes.latField || src == this.displayAttributes.lonField || src == this.displayAttributes.altField ) {
	    	int[] selectedRows =  this.drawingToolTable.getSelectedRows();
	    	ArrayList<vtkObject> actors = DrawingTool.getMasterFaultBranchGroup();
	        for(int i =0;i<selectedRows.length;i++)
	        {
	        	vtkProp actorPin = (vtkProp) actors.get(selectedRows[i]*2);
	        	vtkProp actor = (vtkProp) actors.get((2*selectedRows[i])+1);
	        	vtkPoints labelPoints = new vtkPoints();
			   	labelPoints.InsertNextPoint(Transform.transformLatLonHeight(Double.parseDouble((String) this.displayAttributes.latField.getText()),
	        			Double.parseDouble((String) this.displayAttributes.lonField.getText()),
	        			Double.parseDouble((String) this.displayAttributes.altField.getText())));
			   	
			   	vtkPolyData temp = new vtkPolyData();
		 		temp = (vtkPolyData) ((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetInput();
			   	temp.SetPoints(labelPoints);
			   	
			   	vtkGlyph3D glyphPoints = new vtkGlyph3D();
			   	glyphPoints = (vtkGlyph3D) ((vtkActor) actorPin).GetMapper().GetInputAlgorithm();
				glyphPoints.SetInputData(temp);
			   	
//	        	double[] pt= {Transform.calcRadius(Double.parseDouble((String) this.displayAttributes.latField.getText()))+Double.parseDouble((String) this.displayAttributes.altField.getText()),
//	        			Double.parseDouble((String) this.displayAttributes.latField.getText()),
//	        			Double.parseDouble((String) this.displayAttributes.lonField.getText())};
//	        	((vtkActor2D) actor).SetPosition(Transform.transformLatLonHeight(Double.parseDouble((String) this.displayAttributes.latField.getText()),
//	        			Double.parseDouble((String) this.displayAttributes.lonField.getText()),
//	        			Double.parseDouble((String) this.displayAttributes.altField.getText())));
//	        	((vtkActor) actorPin).SetPosition(Transform.transformLatLonHeight(Double.parseDouble((String) this.displayAttributes.latField.getText()),
//	        			Double.parseDouble((String) this.displayAttributes.lonField.getText()),
//	        			Double.parseDouble((String) this.displayAttributes.altField.getText())));
	        }
	        Info.getMainGUI().addActors(DrawingTool.getMasterFaultBranchGroup());
	    }
	    if (src == this.displayAttributes.rotateXField || src == this.displayAttributes.rotateYField || src == this.displayAttributes.rotateZField ) {
	    	int[] selectedRows =  this.drawingToolTable.getSelectedRows();
	    	ArrayList<vtkObject> actors = DrawingTool.getMasterFaultBranchGroup();
	        for(int i =0;i<selectedRows.length;i++)
	        {
	        	vtkProp actor = (vtkProp) actors.get(selectedRows[i]);
	        	//actor.SetOrigin(actor.GetCenter());
//	        	actor.RotateX(Double.parseDouble((String) this.displayAttributes.rotateXField.getText()));
//	        	actor.RotateY(Double.parseDouble((String) this.displayAttributes.rotateYField.getText()));
//	        	actor.RotateZ(Double.parseDouble((String) this.displayAttributes.rotateZField.getText()));
	        }
	        Info.getMainGUI().addActors(DrawingTool.getMasterFaultBranchGroup());
	    }
	    if (src == this.displayAttributes.fontSizeField ) {
	    	int[] selectedRows =  this.drawingToolTable.getSelectedRows();
	    	ArrayList<vtkObject> actors = DrawingTool.getMasterFaultBranchGroup();
	        for(int i =0;i<selectedRows.length;i++)
	        {
	        	vtkProp actor = (vtkProp) actors.get((selectedRows[i]*2)+1);
	        	((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetFontSize(Integer.parseInt((String) this.displayAttributes.fontSizeField.getText()));
	          }
	        Info.getMainGUI().addActors(DrawingTool.getMasterFaultBranchGroup());
	    }
	    if (src == this.colorDrawingToolsButton) {
	        if (this.colorChooser == null) {
	            this.colorChooser = new SingleColorChooser(this);
	        }
	        Color newColor = this.colorChooser.getColor();
	        if (newColor != null) {
	        	double[] color = {newColor.getRed()/Info.rgbMax,newColor.getGreen()/Info.rgbMax,newColor.getBlue()/Info.rgbMax};
	            int[] selectedRows = this.drawingToolTable.getSelectedRows();
	            ArrayList<vtkObject> actors = DrawingTool.getMasterFaultBranchGroup();
		        for(int i =0;i<selectedRows.length;i++)
		        {
		        	vtkProp actorPin =(vtkProp) actors.get((selectedRows[i]*2));
	            	((vtkActor) actorPin).GetProperty().SetColor(color);
	            	vtkProp actor = (vtkProp) actors.get((selectedRows[i]*2)+1);
		        	((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetColor(color);
		        	
	    	    }
	            Info.getMainGUI().addActors(DrawingTool.getMasterFaultBranchGroup());
	        }
	    }
	    
	}

	
	public void removeTextActors(int[] selectedRows) {
		//remove actors
        ArrayList<vtkObject> actors = DrawingTool.getMasterFaultBranchGroup();
        ArrayList<vtkObject> removedActors = new ArrayList<>();
        for(int i =0;i<selectedRows.length;i++)
        {
        	vtkProp actor = (vtkProp) actors.get(2*(selectedRows[i]-i));
        	vtkProp actorPin = (vtkProp) actors.get((selectedRows[i]-i)*2+1);
        	removedActors.add(actorPin);
        	removedActors.add(actor);
        	DrawingTool.getMasterFaultBranchGroup().remove(2*(selectedRows[i]-i));
        	DrawingTool.getMasterFaultBranchGroup().remove(2*(selectedRows[i]-i));
        }
        Info.getMainGUI().removeActors(removedActors);
		
	}
	private void runObjectInfoDialog(int[] objects) {
		ArrayList<vtkObject> actors = DrawingTool .getMasterFaultBranchGroup();
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
	        		//vtkProp actor = (vtkProp) actors.get(objects[i]);
	        		vtkProp actor = (vtkProp) actors.get(objects[i]*2+1);
	        		 vtkStringArray labels =new vtkStringArray();
	        		 labels.SetName("labels");
			 		 labels.SetNumberOfValues(1);
			 		 labels.SetValue(0, displayTextInput);
			 		 labels.Modified();
			 		vtkPolyData temp = new vtkPolyData();
			 		temp = (vtkPolyData) ((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetInput();
				   	temp.GetPointData().AddArray(labels);
	        		((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).SetLabelArrayName("labels");
	        		((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).Update();
	        		Info.getMainGUI().addActors(DrawingTool.getMasterFaultBranchGroup());
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
