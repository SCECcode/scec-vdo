package org.scec.vtk.drawingTools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.utils.components.AddButton;
import org.scec.vtk.plugins.utils.components.ColorButton;
import org.scec.vtk.plugins.utils.components.EditButton;
import org.scec.vtk.plugins.utils.components.RemoveButton;
import org.scec.vtk.plugins.utils.components.ShowButton;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;
import org.scec.vtk.tools.actors.AppendActors;

import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkConeSource;
import vtk.vtkGlyph3D;
import vtk.vtkLabelPlacementMapper;
import vtk.vtkPointSetToLabelHierarchy;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkStringArray;

public class DrawingToolsGUI extends JPanel implements ActionListener, ListSelectionListener, TableModelListener{

	private static final long serialVersionUID = 1L;
	private JPanel drawingToolSubPanelLower;
	private ShowButton showDrawingToolsButton;
	private ColorButton colorDrawingToolsButton;
	private SingleColorChooser colorChooser;
	private AddButton addDrawingToolsButton;
	private RemoveButton remDrawingToolsButton;
	private DrawingToolsTable drawingToolTable;
	private DrawingToolsTable highwayToolTable;
	private EditButton editDrawingToolsButton;

	private DisplayAttributes displayAttributes;
	private JPanel drawingToolSubPanelLowest;

	AppendActors appendActors = new AppendActors();

	private int numText = 0;
	private Vector<DrawingTool> drawingToolsArray ;
	

	public DrawingToolsGUI(PluginActors pluginActors){
		pluginActors.addActor(appendActors.getAppendedActor());

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()));
		setName("Drawing Tools");
		this.drawingToolTable = new DrawingToolsTable(this);
		this.highwayToolTable = new DrawingToolsTable(this);

		JScrollPane drawingToolSubPanelUpper = new JScrollPane();
		drawingToolSubPanelUpper.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		drawingToolSubPanelUpper.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		drawingToolSubPanelUpper.setPreferredSize(new Dimension(200, 100));
		

		displayAttributes = new DisplayAttributes();
		displayAttributes.latField.addActionListener(this);
		displayAttributes.lonField.addActionListener(this);
		displayAttributes.altField.addActionListener(this);
		displayAttributes.coneHeightField.addActionListener(this);
		displayAttributes.coneBaseRadiusField.addActionListener(this);
		displayAttributes.fontSizeField.addActionListener(this);


		JPanel displayPanel = new JPanel();
		displayPanel.setLayout(new BoxLayout(displayPanel,BoxLayout.Y_AXIS));
		displayPanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));

		this.drawingToolSubPanelLowest = new JPanel(new BorderLayout(0,0));
		this.drawingToolSubPanelLowest.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));    
		this.drawingToolSubPanelLowest.add(displayAttributes);
		displayPanel.add(getDrawingToolLibraryPanel());//mid level
		displayPanel.add(this.drawingToolSubPanelLowest);//lowest level			
		add(displayPanel);
		drawingToolsArray = new Vector<DrawingTool>();

		this.drawingToolTable.addMouseListener(new MouseAdapter(){ 

			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 1){
					DrawingToolsTable target = (DrawingToolsTable)e.getSource();
					int i = target.getSelectedRow();
					DrawingTool dr = drawingToolsArray.get(i);
					displayAttributes.latField.setText(dr.getAttributes().get("Lat"));
					displayAttributes.lonField.setText(dr.getAttributes().get("Lon"));
					displayAttributes.altField.setText(dr.getAttributes().get("Alt"));
					displayAttributes.coneHeightField.setText(dr.getAttributes().get("pinH"));
					displayAttributes.coneBaseRadiusField.setText(dr.getAttributes().get("pinR"));
					displayAttributes.fontSizeField.setText(dr.getAttributes().get("fontSize"));
				}
				if (e.getClickCount() == 2) { //double click text in table to highlight it
					DrawingToolsTable target = (DrawingToolsTable)e.getSource();
					int i = target.getSelectedRow();
					DrawingTool dr = drawingToolsArray.get(i);
					vtkProp actor = (vtkProp) dr.getActorText();
					vtkProp actorPin = (vtkProp) dr.getActorPin();
					double rbg[] = ((vtkActor) actorPin).GetProperty().GetColor();
					if(actor != null)
					{
						if(rbg[0] == 1.0 && rbg[1] == 1.0 && rbg[2] == 0.0){
							//make text back to white
							((vtkActor) actorPin).GetProperty().SetColor(1.0,1.0,1.0); //sets color to white
							((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetColor(1.0,1.0,1.0);
						}else{
							((vtkActor) actorPin).GetProperty().SetColor(1.0,1.0,0.0); //sets color to yellow
							((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetColor(1.0,1.0,0.0);
						}
					}
					
					MainGUI.updateRenderWindow();
				}
				if (e.getClickCount() == 3) { // if triple click, open edit dialog box
					runObjectInfoDialog();
				}
			}
		});

	}

	public DrawingToolsTable getDrawingToolTable()
	{
		return this.drawingToolTable;
	}

	public AppendActors getDrawingToolActors()
	{
		return this.appendActors;
	}
	public Vector<DrawingTool> getDrawingToolArray()
	{
		return this.drawingToolsArray;
	}
	private JPanel getDrawingToolLibraryPanel() {

		// set up panel
		this.drawingToolSubPanelLower = new JPanel();
		this.drawingToolSubPanelLower.setLayout(new BoxLayout(this.drawingToolSubPanelLower, BoxLayout.PAGE_AXIS));
		this.drawingToolSubPanelLower.setName("Library");
		this.drawingToolSubPanelLower.setOpaque(false);

		// set up scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setPreferredSize(new Dimension(Prefs.getPluginWidth()-10, 200));
		scroller.setViewportView(this.drawingToolTable);
		scroller.getViewport().setBackground(this.drawingToolTable.getBackground());

		this.drawingToolSubPanelLower.add(new JLabel("Drawings"));
		this.drawingToolSubPanelLower.add(scroller);
		this.drawingToolSubPanelLower.add(getDrawingToolLibraryBar(),BorderLayout.PAGE_END);

		return this.drawingToolSubPanelLower;
	}
	
	private JPanel getDrawingToolLibraryBar() {

		this.showDrawingToolsButton = new ShowButton(this, "Toggle visibility of selected Text");
		this.colorDrawingToolsButton = new ColorButton(this, "Change color of selected Text");
		this.editDrawingToolsButton = new EditButton(this, "Edit Text");
		this.addDrawingToolsButton = new AddButton(this, "Add new Text");
		this.remDrawingToolsButton = new RemoveButton(this, "Remove selected Text");

		JPanel bar = new JPanel();
		bar.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		bar.setLayout(new BoxLayout(bar,BoxLayout.LINE_AXIS));
		bar.setOpaque(true);
		int buttonSpace = 3;

		bar.add(this.showDrawingToolsButton);
		bar.add(Box.createHorizontalStrut(buttonSpace));
		bar.add(this.colorDrawingToolsButton);
		bar.add(Box.createHorizontalStrut(buttonSpace));
		bar.add(this.editDrawingToolsButton);
		bar.add(Box.createHorizontalGlue());
		bar.add(Box.createHorizontalStrut(buttonSpace));
		bar.add(Box.createHorizontalGlue());

		bar.add(this.addDrawingToolsButton);
		bar.add(Box.createHorizontalStrut(buttonSpace));
		bar.add(this.remDrawingToolsButton);

		return bar;
	}
	
	//grouping actor 
	public DrawingTool addDrawingTool(DrawingTool drawingTool, String text){
	
		//individual text as actors
		double[] pt= {Transform.calcRadius(37),37,-120};
		//ArrayList<DataAccessor> a = this.drawingToolTable.getLibraryModel().getAllObjects();

		if(drawingTool.getTextString()!=null)
		{
			text = drawingTool.getTextString();
			drawingTool.setDisplayName(drawingTool.getTextString());
			pt[0]= pt[0]+drawingTool.getaltitude();
			pt[1]= drawingTool.getLatitude();
			pt[2]= drawingTool.getLongitude();
			//drawingTool.setDisplayName(text);
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
		vtkConeSource conePin = new vtkConeSource();
		conePin.SetRadius(5);
		conePin.SetHeight(10);
		conePin.SetDirection(-Transform.customTransform(pt)[0],-Transform.customTransform(pt)[1],-Transform.customTransform(pt)[2]);
		conePin.SetResolution(10);

		vtkGlyph3D glyphPoints = new vtkGlyph3D();
		glyphPoints.SetInputData(pinPolydata);
		glyphPoints.SetSourceConnection(conePin.GetOutputPort());

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
		pointSetToLabelHierarchyFilter.GetTextProperty().SetFontSize(21);


		vtkLabelPlacementMapper cellMapper = new vtkLabelPlacementMapper();
		cellMapper.SetInputConnection(pointSetToLabelHierarchyFilter.GetOutputPort());


		vtkActor2D actor = new vtkActor2D();
		actor.SetMapper(cellMapper);

		vtkActor actorPin = new vtkActor();
		actorPin.SetMapper(pm);

		if(drawingTool.getTextString()==null)
		{
			drawingTool = new DrawingTool(pt[1],pt[2],pt[0],text,null,Color.white,actorPin,actor);
			drawingTool.setDisplayName(text +" ("+ numText++ + ")"); 
		}
		else
		{
			drawingTool.setActors(actorPin,actor);
		}

		//first pin then label
		appendActors.addToAppendedPolyData(actorPin);
		appendActors.addToAppendedPolyData(actor);
		appendActors.getAppendedActor().Modified();
		HashMap<String,String> locData = new HashMap<String, String>();
		locData.put("Lat", String.format("%.1f", pt[1])); 
		locData.put("Lon", String.format("%.1f", pt[2]));
		locData.put("Alt", "0");
		locData.put("pinH", "10");
		locData.put("pinR", "5");
		locData.put("fontSize", "21");
		drawingTool.setAttributes(locData);
		drawingToolsArray.add(drawingTool);
		return drawingTool;

	}
	
	public void setVisibility(DrawingTool dr, Integer visible) {
		vtkActor2D actor = ((vtkActor2D) dr.getActorText());
		vtkActor actorPin = (vtkActor) dr.getActorPin();
		
		if (actor != null && actorPin != null) {
		actor.SetVisibility(visible);
		actorPin.SetVisibility(visible);
		}
		else if(actorPin!=null && actor == null)
		{
			actorPin.SetVisibility(visible);
		}
	}
	
	public void setColor(DrawingTool dr, Color newColor) {
		vtkActor2D actor = ((vtkActor2D) dr.getActorText());
		vtkActor actorPin = (vtkActor) dr.getActorPin();
		
		double[] color = {newColor.getRed()/Info.rgbMax,newColor.getGreen()/Info.rgbMax,newColor.getBlue()/Info.rgbMax};
		if (actor != null && actorPin != null) {
			((vtkPointSetToLabelHierarchy) (actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetColor(color);
		actorPin.GetProperty().SetColor(color);
		}
		else if(actorPin!=null && actor == null)
		{
			actorPin.GetProperty().SetColor(color);
		}
	}
	
	public void setLatLon(DrawingTool dr)
	{
		vtkProp actor = ((vtkActor2D) dr.getActorText());
		vtkProp actorPin = (vtkActor) dr.getActorPin();
		if (actor != null && actorPin != null) {
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
		}
	}
	
	public void setConeHtRadius(DrawingTool dr)
	{
		vtkProp actor = ((vtkActor2D) dr.getActorText());
		vtkProp actorPin = (vtkActor) dr.getActorPin();
		if (actor != null && actorPin != null) {
		double coneHeight = (Double.parseDouble( this.displayAttributes.coneHeightField.getText())==0)?1:Double.parseDouble( this.displayAttributes.coneHeightField.getText());
		double coneRadius = (Double.parseDouble( this.displayAttributes.coneBaseRadiusField.getText())==0)?1:Double.parseDouble( this.displayAttributes.coneBaseRadiusField.getText());

		vtkGlyph3D glyphPoints = new vtkGlyph3D();
		glyphPoints = (vtkGlyph3D) ((vtkActor) actorPin).GetMapper().GetInputAlgorithm();

		vtkPolyData temp = new vtkPolyData();
		temp = (vtkPolyData) (glyphPoints).GetInput();

		vtkConeSource conePin = new vtkConeSource();
		conePin.SetRadius(coneRadius);
		conePin.SetHeight(coneHeight);
		conePin.SetDirection(-temp.GetPoint(0)[0],-temp.GetPoint(0)[1],-temp.GetPoint(0)[2]);
		conePin.SetResolution(10);

		glyphPoints.SetSourceConnection(conePin.GetOutputPort());
		}
	}
	
	public void setFontSize(DrawingTool dr)
	{
		vtkProp actor = ((vtkActor2D) dr.getActorText());
		if (actor != null)
			((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetFontSize(Integer.parseInt((String) this.displayAttributes.fontSizeField.getText()));
	}
	
	public void setAttributes(DrawingTool dr, HashMap<String,String> attributes) {
		this.displayAttributes.latField.setText(attributes.get("Lat"));
		this.displayAttributes.lonField.setText(attributes.get("Lon"));
		this.displayAttributes.altField.setText(attributes.get("Alt"));
		this.displayAttributes.coneBaseRadiusField.setText(attributes.get("pinR"));
		this.displayAttributes.coneHeightField.setText(attributes.get("pinH"));
		this.displayAttributes.fontSizeField.setText(attributes.get("fontSize"));
		setLatLon(dr);
		setConeHtRadius(dr);
		setFontSize(dr);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		this.drawingToolTable.getLibraryModel();
		if (src == this.showDrawingToolsButton) {
			ListSelectionModel model = this.drawingToolTable.getSelectionModel();
			vtkProp actorPin = null;
			if (!model.isSelectionEmpty())
			{
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
				//int row = model.getMinSelectionIndex();
				DrawingTool dr = drawingToolsArray.get(i);

				vtkProp actor = null;
				actor = ((vtkActor2D) dr.getActorText());
				actorPin = (vtkActor) dr.getActorPin();
				if (actor != null && actorPin != null) {
					if(actor.GetVisibility() == 1 && actorPin.GetVisibility() == 1)
					{setVisibility(dr, 0) ;}
					else
					{setVisibility(dr, 1) ;}
				}
			}
			}
			Info.getMainGUI().updateRenderWindow();
		}
		else if (src == this.editDrawingToolsButton) {		
			runObjectInfoDialog();
		}
		else if (src == this.addDrawingToolsButton) {
			DrawingTool drawingToolObj = addDrawingTool(new DrawingTool(), "Text");
			this.drawingToolTable.addDrawingTool(drawingToolObj);
			MainGUI.updateRenderWindow();
		}
		if (src == this.remDrawingToolsButton) {
			removeTextActors();
		}
		if (src == this.displayAttributes.latField || src == this.displayAttributes.lonField || src == this.displayAttributes.altField ) {
			ListSelectionModel model = this.drawingToolTable.getSelectionModel();
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
				DrawingTool dr = drawingToolsArray.get(i);
				if ((vtkActor) dr.getActorPin() != null)
				{
					setLatLon(dr);
					dr.getAttributes().put("Lat", this.displayAttributes.latField.getText());
					dr.getAttributes().put("Lon", this.displayAttributes.lonField.getText());
					dr.getAttributes().put("Alt", this.displayAttributes.altField.getText());
				}
			}
			MainGUI.updateRenderWindow();
		}
		if (src == this.displayAttributes.coneHeightField || src == this.displayAttributes.coneBaseRadiusField) {
			ListSelectionModel model = this.drawingToolTable.getSelectionModel();
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {

				DrawingTool dr = drawingToolsArray.get(i);
				vtkProp actorPin =(vtkActor) dr.getActorPin();
				if (actorPin != null)
				{
					setConeHtRadius(dr);
					dr.getAttributes().put("pinH", this.displayAttributes.coneHeightField.getText());
					dr.getAttributes().put("pinR", this.displayAttributes.coneBaseRadiusField.getText());
				}
			}
			MainGUI.updateRenderWindow();
		}
		if (src == this.displayAttributes.fontSizeField ) {
			ListSelectionModel model = this.drawingToolTable.getSelectionModel();
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
					DrawingTool dr = drawingToolsArray.get(i);
				if ((vtkActor) dr.getActorPin() != null)
				{
					setFontSize(dr);
				}
				dr.getAttributes().put("fontSize", this.displayAttributes.fontSizeField.getText());
			}
			MainGUI.updateRenderWindow();
		}
		if (src == this.colorDrawingToolsButton) {
			if (this.colorChooser == null) {
				this.colorChooser = new SingleColorChooser(this);
			}
			Color newColor = this.colorChooser.getColor();
			if (newColor != null) {
				ListSelectionModel model = this.drawingToolTable.getSelectionModel();
				if(!model.isSelectionEmpty())
				{
					for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
						DrawingTool dr = drawingToolsArray.get(i);
						dr.setColor(newColor);
						setColor(dr,  newColor);
						Info.getMainGUI().updateRenderWindow();
					}
				}
			}
			MainGUI.updateRenderWindow();
		}
	}

	public void removeTextActors() {
		DrawingToolsTableModel drawingTooltablemodel = this.drawingToolTable.getLibraryModel();
		ListSelectionModel model = this.drawingToolTable.getSelectionModel();
		while (model.getMinSelectionIndex() >= 0) {
			int row = model.getMinSelectionIndex();
			DrawingTool dr = drawingToolsArray.get(row);
			appendActors.getAppendedActor().RemovePart(dr.getActorPin());
			appendActors.getAppendedActor().RemovePart(dr.getActorText());
			drawingTooltablemodel.removeRow(row);
			drawingToolsArray.remove(row);
		}
		enablePropertyEditButtons(false);
		Info.getMainGUI().updateRenderWindow();
	}
	
	public void clearTable(){
		DrawingToolsTableModel drawingTooltablemodel = this.drawingToolTable.getLibraryModel();
		int rowIndex = drawingToolTable.getRowCount();
		System.out.println("rowIndex = " + rowIndex);
		while((--rowIndex) >= 0)
		{
			DrawingTool dr = drawingToolsArray.get(rowIndex);
			appendActors.getAppendedActor().RemovePart(dr.getActorPin());
			appendActors.getAppendedActor().RemovePart(dr.getActorText());
			drawingTooltablemodel.removeRow(rowIndex);
			drawingToolsArray.remove(rowIndex);
		}
	}
	
	private void runObjectInfoDialog() {
		String displayTextInput = JOptionPane.showInputDialog(
				this.drawingToolTable,
				"Change text:",
				"Set Drawing Text",
				JOptionPane.QUESTION_MESSAGE);
		if (displayTextInput == null) return;
		else
		{
			ListSelectionModel model = this.drawingToolTable.getSelectionModel();
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
				//int row = model.getMinSelectionIndex();

				DrawingTool dr = drawingToolsArray.get(i);
				if ((vtkActor) dr.getActorPin() != null)
				{
					setText(dr,displayTextInput);
					this.drawingToolTable.setValueAt(displayTextInput,i, 0);
					MainGUI.updateRenderWindow();
				}
			}
		}
	}

	public void setText(DrawingTool dr, String displayTextInput) {
		vtkProp actor = dr.getActorText();
		dr.setTextString(displayTextInput);
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
	}
	
	@Override
	public void tableChanged(TableModelEvent e) {

	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		Object src = e.getSource();
		if (e.getValueIsAdjusting()) return;
	
		if (src == this.drawingToolTable.getSelectionModel()) {
			processTableSelectionChange(this.drawingToolTable.getSelectedRows());
			enablePropertyEditButtons(true);
		}
	}
	
	public void processTableSelectionChange(int[] selectedRows) { 
		if (selectedRows.length > 0) { // If row is selected, enable the color, visibility toggle, and remove buttons
			this.remDrawingToolsButton.setEnabled(true);
			this.colorDrawingToolsButton.setEnabled(true);
			this.showDrawingToolsButton.setEnabled(true);

		} else { // If no rows on the table are selected, disable text boxes and all buttons except for add
			enablePropertyEditButtons(false);
			this.remDrawingToolsButton.setEnabled(false);
			this.showDrawingToolsButton.setEnabled(false);
			this.colorDrawingToolsButton.setEnabled(false);
		}
	}
	
	private void enablePropertyEditButtons(boolean enable) {
		this.editDrawingToolsButton.setEnabled(enable);
		this.displayAttributes.coneBaseRadiusField.setEnabled(enable);
		this.displayAttributes.coneHeightField.setEnabled(enable);
		this.displayAttributes.latField.setEnabled(enable);
		this.displayAttributes.lonField.setEnabled(enable);
		this.displayAttributes.altField.setEnabled(enable);
		this.displayAttributes.fontSizeField.setEnabled(enable);
	}
}
