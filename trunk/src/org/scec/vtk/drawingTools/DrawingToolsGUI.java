package org.scec.vtk.drawingTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.utils.AbstractLibraryModel;
import org.scec.vtk.plugins.utils.components.AddButton;
import org.scec.vtk.plugins.utils.components.ColorButton;
import org.scec.vtk.plugins.utils.components.EditButton;
import org.scec.vtk.plugins.utils.components.MeshButton;
import org.scec.vtk.plugins.utils.components.RemoveButton;
import org.scec.vtk.plugins.utils.components.ShowButton;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;

import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkTextActor3D;
import vtk.vtkVectorText;

public class DrawingToolsGUI extends JPanel implements ActionListener{

	private JPanel drawingToolSubPanelUpper;
	private ArrayList<vtkActor> actorDrawingToolSegments;
	private JPanel drawingToolSubPanelLower;
	private ShowButton showDrawingToolsButton;
	private ColorButton colorDrawingToolsButton;
	private AddButton addDrawingToolsButton;
	private RemoveButton remDrawingToolsButton;
	private DrawingToolsTable DrawingToolTable;
	//private DrawingToolTable drawingToolObj;
	private boolean loaded = false;
	
	public DrawingToolsGUI(DrawingToolsPlugin plugin){
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()));
		setName("Drawing Tools");
		
		Dimension dSubPanel = new Dimension(Prefs.getPluginWidth(),100);
		
		this.drawingToolSubPanelUpper=new JPanel();
		this.drawingToolSubPanelUpper.setLayout(new BoxLayout(this.drawingToolSubPanelUpper, BoxLayout.Y_AXIS));
		add(this.drawingToolSubPanelUpper);
		
		//this.drawingToolSubPanelLower=new JPanel();
		//this.drawingToolSubPanelLower.setLayout(new BoxLayout(this.drawingToolSubPanelLower, BoxLayout.Y_AXIS));
		add(getDrawingToolLibraryPanel());
		
		this.actorDrawingToolSegments = new ArrayList<vtkActor>();
		//todo add actors to the render window once
		loaded = true;
	}
	private JPanel getDrawingToolLibraryPanel() {

	    // set up panel
	    this.drawingToolSubPanelLower = new JPanel(new BorderLayout());
	    this.drawingToolSubPanelLower.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	    this.drawingToolSubPanelLower.setName("Library");
	    this.drawingToolSubPanelLower.setOpaque(false);

	    // set up scroll pane
	    JScrollPane scroller = new JScrollPane();
	    scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	    // set up table
	    this.DrawingToolTable = new DrawingToolsTable(this);
	    scroller.setViewportView(this.DrawingToolTable);
	    scroller.getViewport().setBackground(this.DrawingToolTable.getBackground());
	    this.drawingToolSubPanelLower.add(scroller,BorderLayout.CENTER);
	    this.drawingToolSubPanelLower.add(getDrawingToolLibraryBar(),BorderLayout.PAGE_END);

	    return this.drawingToolSubPanelLower;
	}
	private JPanel getDrawingToolLibraryBar() {

	    this.showDrawingToolsButton = new ShowButton(this, "Toggle visibility of selected Text(s)");
	    this.colorDrawingToolsButton = new ColorButton(this, "Change color of selected Text(s)");
	    //this.meshDrawingToolsButton = new MeshButton(this, "Toggle mesh state of selected DrawingTool(s)s");
	    //this.editDrawingToolsButton = new EditButton(this, "Edit DrawingTool information");
	    this.addDrawingToolsButton = new AddButton(this, "Add new Text");
	    this.remDrawingToolsButton = new RemoveButton(this, "Remove selected Text(s)");

	    JPanel bar = new JPanel();
	    bar.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
	    bar.setLayout(new BoxLayout(bar,BoxLayout.LINE_AXIS));
	    bar.setOpaque(true);
	    int buttonSpace = 3;

	    bar.add(this.showDrawingToolsButton);
	    bar.add(Box.createHorizontalStrut(buttonSpace));
	    bar.add(this.colorDrawingToolsButton);
	    bar.add(Box.createHorizontalStrut(buttonSpace));
	    //bar.add(this.meshDrawingToolsButton);
	   // bar.add(Box.createHorizontalStrut(buttonSpace));
	    //bar.add(this.editDrawingToolsButton);
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
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object src = e.getSource();
		DrawingToolsTableModel drawingTooltablemodel = this.DrawingToolTable.getLibraryModel();
	    if (src == this.showDrawingToolsButton) {
	    	int[] selectedRows =  this.DrawingToolTable.getSelectedRows();
	    }
	    if (src == this.addDrawingToolsButton) {
	    	 DrawingTool drawingToolObj = new DrawingTool();
	    	 drawingToolObj.setDisplayName("New Text");
	    	 drawingToolObj.setInMemory(true);
	    	ArrayList newObjects = new ArrayList<>();
	    	vtkVectorText newText = new vtkVectorText();
	    	newText.SetText( "cursor:" );
	    	 //newText.GetTextProperty().SetFontSize ( 12 );
	    	// Create a mapper and actor
	    	  vtkPolyDataMapper mapper =new vtkPolyDataMapper();
	    	  mapper.SetInputConnection(newText.GetOutputPort());
	    	  
	    	  vtkActor actor = new vtkActor();
	    	  actor.SetMapper(mapper);
	    	  actor.GetProperty().SetColor(1.0, 0.0, 0.0);
	    	  actor.SetScale(30,30,30);
	    	  actor.RotateY(90);
	    	  actor.RotateZ(90);
	    	  actor.RotateX(30);
	    	 double[] pt= {Transform.calcRadius(37),37,-120};
	    	 actor.SetPosition( Transform.customTransform(pt));
	    	 actor.GetProperty().SetColor (1,0,0);
	    	 newObjects.add(drawingToolObj);
	    	 this.DrawingToolTable.addDrawingTool(newObjects);
	    	 drawingToolObj.getMasterFaultBranchGroup().add(actor);
	    	 Info.getMainGUI().updateTextActors(drawingToolObj.getMasterFaultBranchGroup());
	    }
	}

}
