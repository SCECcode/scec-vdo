package org.scec.vtk.drawingTools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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

import org.scec.vtk.drawingTools.DefaultLocationsGUI.PresetLocationGroup;
import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.utils.DataAccessor;
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
	private DrawingToolsTable highwayToolTable;
	private EditButton editDrawingToolsButton;

	private DisplayAttributes displayAttributes;
	ArrayList<HashMap<String, String>> AttributesData; //will contain a list of attributes(lat, log, cone height, cone width, etc)
	private DefaultLocationsGUI defaultLocations;
	//	private static String[] columnNames = {"Show","",
	//			"Label",
	//			"Size",
	//			"Lat",
	//			"Lon",
	//	"Alt"};
	//	public HighwayTableModel labelModel = new HighwayTableModel(columnNames);
	private JPanel drawingToolSubPanelLowest;


	//	vtkStringArray labels =new vtkStringArray();
	//	vtkPoints conePinPoints = new vtkPoints();
	//	vtkPoints labelPoints = new vtkPoints();

	private PluginActors pluginActors;

	AppendActors appendActors = new AppendActors();

	private int  numText =0;
	private ArrayList<DrawingTool> drawingToolsArray ;

	public DrawingToolsGUI(PluginActors pluginActors){
		this.pluginActors = pluginActors;
		pluginActors.addActor(appendActors.getAppendedActor());

		//setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()));
		setName("Drawing Tools");
		this.drawingToolTable = new DrawingToolsTable(this);
		this.highwayToolTable = new DrawingToolsTable(this);
		defaultLocations = new DefaultLocationsGUI(this);
		JScrollPane drawingToolSubPanelUpper = new JScrollPane();
		drawingToolSubPanelUpper.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		drawingToolSubPanelUpper.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		drawingToolSubPanelUpper.setPreferredSize(new Dimension(200, 100));
		drawingToolSubPanelUpper.setViewportView(defaultLocations);

		displayAttributes = new DisplayAttributes();
		displayAttributes.latField.addActionListener(this);
		displayAttributes.lonField.addActionListener(this);
		displayAttributes.altField.addActionListener(this);
		displayAttributes.coneHeightField.addActionListener(this);
		displayAttributes.coneBaseRadiusField.addActionListener(this);
		//displayAttributes.rotateZField.addActionListener(this);
		displayAttributes.fontSizeField.addActionListener(this);

		AttributesData = new ArrayList<HashMap<String, String>>(); 

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
		drawingToolsArray  = new ArrayList<>();
		//labels.SetName("labels");

		this.drawingToolTable.addMouseListener(new MouseAdapter(){ 

			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 1){
					//					displayAttributes.latField.getText();
					//					displayAttributes.lonField.getText(); //set text to the index

					DrawingToolsTable target = (DrawingToolsTable)e.getSource();
					int i = target.getSelectedRow();
					vtkProp actor = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2+1);
					vtkProp actorPin = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2);


					displayAttributes.latField.setText(AttributesData.get(i).get("Lat"));
					displayAttributes.lonField.setText(AttributesData.get(i).get("Lon"));
					displayAttributes.altField.setText(AttributesData.get(i).get("Alt"));
					displayAttributes.coneHeightField.setText(AttributesData.get(i).get("pinH"));
					displayAttributes.coneBaseRadiusField.setText(AttributesData.get(i).get("pinR"));
					displayAttributes.fontSizeField.setText(AttributesData.get(i).get("fontSize"));
				}
				if (e.getClickCount() == 2) { //double click text in table to highlight it
					DrawingToolsTable target = (DrawingToolsTable)e.getSource();
					int i = target.getSelectedRow();
					vtkProp actor = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2+1);
					vtkProp actorPin = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2);

					double rbg[] = ((vtkActor) actorPin).GetProperty().GetColor();
					if(rbg[0] == 1.0 && rbg[1] == 1.0 && rbg[2] == 0.0){
						//make text back to white
						((vtkActor) actorPin).GetProperty().SetColor(1.0,1.0,1.0); //sets color to white
						((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetColor(1.0,1.0,1.0);
					}else{
						((vtkActor) actorPin).GetProperty().SetColor(1.0,1.0,0.0); //sets color to yellow
						((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetColor(1.0,1.0,0.0);
						// System.out.println("asdas" +  defaultLocations.presetLocationGroups.size());
						for(int i1 = 0; i1 < defaultLocations.presetLocationGroups.size(); i1++)
						{ 
							PresetLocationGroup tempGroup = defaultLocations.presetLocationGroups.get(i1);
							//System.out.println(tempGroup.name);
							if (tempGroup != null && tempGroup.name.equals("CA Cities") && tempGroup.checkbox.isSelected())
							{
								JOptionPane.showMessageDialog(defaultLocations.frame,"City Name: " + (String)target.getValueAt(i,target.getSelectedColumn()) + "\n"
										+"City Population: "  + defaultLocations.getPopulation((String)target.getValueAt(i,target.getSelectedColumn())) + "\n" + "County: " +
										defaultLocations.getCounty((String)target.getValueAt(i,target.getSelectedColumn())) + "\n" + "Population Density: " + defaultLocations.getPopulationDensity((String)target.getValueAt(i,target.getSelectedColumn())) + " people/sq. mile\n",
										"City Information",JOptionPane.DEFAULT_OPTION);
							}
						}
					}
					MainGUI.updateRenderWindow();
				}
			}
		});

	}
	public DrawingToolsTable getDrawingToolTable()
	{
		return this.drawingToolTable;
	}
	public DrawingToolsTable getHighwayToolTable()
	{
		return this.highwayToolTable;
	}
	private JPanel getDrawingToolLibraryPanel() {

		// set up panel
		this.drawingToolSubPanelLower = new JPanel();
		this.drawingToolSubPanelLower.setLayout(new BoxLayout(this.drawingToolSubPanelLower, BoxLayout.PAGE_AXIS));
//		this.drawingToolSubPanelLower.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		this.drawingToolSubPanelLower.setName("Library");
		this.drawingToolSubPanelLower.setOpaque(false);

		// set up scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setPreferredSize(new Dimension(Prefs.getPluginWidth()-10, 200));
		scroller.setViewportView(this.drawingToolTable);
		scroller.getViewport().setBackground(this.drawingToolTable.getBackground());


		// set up scroll pane
		JScrollPane scrollerHighway = new JScrollPane();
		scrollerHighway.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollerHighway.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollerHighway.setPreferredSize(new Dimension(Prefs.getPluginWidth()-10, 200));
		scrollerHighway.setViewportView(this.highwayToolTable);
		scrollerHighway.getViewport().setBackground(this.highwayToolTable.getBackground());
		this.drawingToolSubPanelLower.add(new JLabel("Cities"));
		this.drawingToolSubPanelLower.add(scroller);
		this.drawingToolSubPanelLower.add(new JLabel("Highways"));
		this.drawingToolSubPanelLower.add(scrollerHighway);
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
	//grouping actor 
	public DrawingTool addDrawingTool(DrawingTool drawingTool){
		//indivisual text as actors
		String text = "Text";
		double[] pt= {Transform.calcRadius(37),37,-120};
		//ArrayList<DataAccessor> a = this.drawingToolTable.getLibraryModel().getAllObjects();
		if(drawingTool.getTextString()==null)
		{
			drawingTool = new DrawingTool(pt[1],pt[2],pt[0],text,null);
			drawingTool.setDisplayName(text +" -"+ numText++); 
		}
		else
		{
			text = drawingTool.getTextString();
			drawingTool.setDisplayName(drawingTool.getTextString());
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
		pointSetToLabelHierarchyFilter.GetTextProperty().SetFontSize(Integer.parseInt(displayAttributes.fontSizeField.getText()));


		vtkLabelPlacementMapper cellMapper = new vtkLabelPlacementMapper();
		cellMapper.SetInputConnection(pointSetToLabelHierarchyFilter.GetOutputPort());


		vtkActor2D actor = new vtkActor2D();
		actor.SetMapper(cellMapper);

		vtkActor actorPin = new vtkActor();
		actorPin.SetMapper(pm);

		//first pin then label
		appendActors.addToAppendedPolyData(actorPin);
		appendActors.addToAppendedPolyData(actor);

		HashMap<String,String> locData = new HashMap<String, String>();
		locData.put("Lat", String.format("%.1f", pt[1])); 
		locData.put("Lon", String.format("%.1f", pt[2]));
		locData.put("Alt", "0");
		locData.put("pinH", "10");
		locData.put("pinR", "5");
		locData.put("fontSize", "21");
		AttributesData.add(locData);

		return drawingTool;

	}
	public void addHighway(DrawingTool drawingTool) {
		double[] pt= {Transform.calcRadius(37),37,-120};
		drawingTool.setDisplayName(drawingTool.getTextString());
		pt[0]= pt[0]+drawingTool.getaltitude();
		pt[1]= drawingTool.getLatitude();
		pt[2]= drawingTool.getLongitude();

		HashMap<String,String> locData = new HashMap<String, String>();
		locData.put("Lat", String.format("%.1f", pt[1])); 
		locData.put("Lon", String.format("%.1f", pt[2]));
		locData.put("Alt", "0");
		AttributesData.add(locData);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		DrawingToolsTableModel drawingTooltablemodel = this.drawingToolTable.getLibraryModel();
		if (src == this.showDrawingToolsButton) {
			ListSelectionModel model = this.drawingToolTable.getSelectionModel();
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
				//int row = model.getMinSelectionIndex();

				vtkProp actor = null;
				vtkProp actorPin = null;

				if (defaultLocations.getHighwayActors().size() > 0)
				{
					actor = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i);
				}
				else
				{
					actor = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2+1);
					actorPin = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2);
				}

				if (actor != null && actorPin != null) {
					if(actor.GetVisibility() == 1 && actorPin.GetVisibility() == 1)
					{actor.SetVisibility(0); actorPin.SetVisibility(0);}
					else
					{actor.SetVisibility(1); actorPin.SetVisibility(1);}
				}
				else if (actor != null) {
					if(actor.GetVisibility() == 1)
						actor.SetVisibility(0);
					else
						actor.SetVisibility(1);
				}
			}
			appendActors.getAppendedActor().Modified();
			Info.getMainGUI().updateRenderWindow();
		}
		else if (src == this.editDrawingToolsButton) {
			//			ArrayList<DrawingTool> selectedDrawingToolObjs = this.drawingToolTable.getSelected();
			//			
			runObjectInfoDialog();
		}
		else if (src == this.addDrawingToolsButton) {
			DrawingTool drawingToolObj = new DrawingTool();
			drawingToolObj = addDrawingTool(drawingToolObj);
			//ArrayList<DrawingTool> newObjects = new ArrayList<>();
			//newObjects.add(drawingToolObj);
			this.drawingToolTable.addDrawingTool(drawingToolObj);

			HashMap<String,String> defaultData = new HashMap<String, String>();
			defaultData.put("Lat", "37"); 
			defaultData.put("Lon", "-120");
			defaultData.put("Alt", "0");
			defaultData.put("fontSize", "21");
			AttributesData.add(defaultData);

			MainGUI.updateRenderWindow();
		}
		if (src == this.remDrawingToolsButton) {
			removeTextActors();
		}
		if (src == this.displayAttributes.latField || src == this.displayAttributes.lonField || src == this.displayAttributes.altField ) {
			ListSelectionModel model = this.drawingToolTable.getSelectionModel();
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
				//int row = model.getMinSelectionIndex();
				vtkProp actor = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2+1);
				vtkProp actorPin = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2);

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

				AttributesData.get(i).put("Lat", this.displayAttributes.latField.getText());
				AttributesData.get(i).put("Lon", this.displayAttributes.lonField.getText());
				AttributesData.get(i).put("Alt", this.displayAttributes.altField.getText());
			}
			MainGUI.updateRenderWindow();
		}
		if (src == this.displayAttributes.coneHeightField || src == this.displayAttributes.coneBaseRadiusField) {
			ListSelectionModel model = this.drawingToolTable.getSelectionModel();
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
				vtkProp actorPin = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2);
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

				AttributesData.get(i).put("pinH", this.displayAttributes.coneHeightField.getText());
				AttributesData.get(i).put("pinR", this.displayAttributes.coneBaseRadiusField.getText());
			}
			MainGUI.updateRenderWindow();
		}
		if (src == this.displayAttributes.fontSizeField ) {
			ListSelectionModel model = this.drawingToolTable.getSelectionModel();
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
				//int row = model.getMinSelectionIndex();
				vtkProp actor = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2+1);
				//vtkProp actorPin = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2);

				((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetFontSize(Integer.parseInt((String) this.displayAttributes.fontSizeField.getText()));

				AttributesData.get(i).put("fontSize", this.displayAttributes.fontSizeField.getText());
			}
			MainGUI.updateRenderWindow();
		}
		if (src == this.colorDrawingToolsButton) {
			if (this.colorChooser == null) {
				this.colorChooser = new SingleColorChooser(this);
			}
			Color newColor = this.colorChooser.getColor();
			if (newColor != null) {
				double[] color = {newColor.getRed()/Info.rgbMax,newColor.getGreen()/Info.rgbMax,newColor.getBlue()/Info.rgbMax};
				ListSelectionModel model = this.drawingToolTable.getSelectionModel();
				for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
					//int row = model.getMinSelectionIndex();
					vtkProp actor = null;
					vtkProp actorPin = null;

					if (defaultLocations.getHighwayActors().size() > 0)
					{
						actor = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i);
						((vtkActor) actor).GetProperty().SetColor(color);
						//		 				((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetColor(color);
					}
					else
					{
						actor = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2+1);
						actorPin = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2);
					}
					if (actorPin != null)
					{
						((vtkActor) actorPin).GetProperty().SetColor(color);
						((vtkPointSetToLabelHierarchy) ((vtkActor2D) actor).GetMapper().GetInputAlgorithm()).GetTextProperty().SetColor(color);
					}


				}
				MainGUI.updateRenderWindow();
			}
		}

	}


	public void removeTextActors() {
		//remove actors
		DrawingToolsTableModel drawingTooltablemodel = this.drawingToolTable.getLibraryModel();
		ListSelectionModel model = this.drawingToolTable.getSelectionModel();
		while (model.getMinSelectionIndex() >= 0) {
			int row = model.getMinSelectionIndex();

			vtkProp actor = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(row*2+1);
			vtkProp actorPin = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(row*2);
			appendActors.getAppendedActor().RemovePart(actorPin);
			appendActors.getAppendedActor().RemovePart(actor);
			drawingTooltablemodel.removeRow(row);

			AttributesData.remove(row);
		}
		Info.getMainGUI().updateRenderWindow();
		ArrayList<vtkObject> removedActors = new ArrayList<>();

	}
	public void removeHighways() {
		DrawingToolsTableModel drawingTooltablemodel = this.highwayToolTable.getLibraryModel();
		while (highwayToolTable.getRowCount() > 0)
		{
			vtkProp actor = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(this.highwayToolTable.getRowCount()-1);
			appendActors.getAppendedActor().RemovePart(actor);
			drawingTooltablemodel.removeRow(this.highwayToolTable.getRowCount()-1);
			AttributesData.remove(this.highwayToolTable.getRowCount());
		}
		Info.getMainGUI().updateRenderWindow();
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
				vtkProp actor = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2+1);
				//vtkProp actorPin = (vtkProp) appendActors.getAppendedActor().GetParts().GetItemAsObject(i*2);
				this.drawingToolTable.setValueAt(displayTextInput,i, 0);//= .setDisplayName(displayTextInput);
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
				MainGUI.updateRenderWindow();
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
			enablePropertyEditButtons(true);
		} else {
			enablePropertyEditButtons(false);
			this.remDrawingToolsButton.setEnabled(false);
			this.editDrawingToolsButton.setEnabled(false);
			this.colorDrawingToolsButton.setEnabled(false);
		}


	}
	private void enablePropertyEditButtons(boolean enable) {
		this.showDrawingToolsButton.setEnabled(enable);
		this.displayAttributes.coneBaseRadiusField.setEnabled(enable);
		this.displayAttributes.coneHeightField.setEnabled(enable);
		this.displayAttributes.latField.setEnabled(enable);
		this.displayAttributes.lonField.setEnabled(enable);
		this.displayAttributes.altField.setEnabled(enable);
		this.displayAttributes.fontSizeField.setEnabled(enable);
	}


}
