package org.scec.vtk.grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.utils.components.ColorWellButton;
import org.scec.vtk.plugins.utils.components.IntegerTextField;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;

import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkIntArray;
import vtk.vtkLabelPlacementMapper;
import vtk.vtkLine;
import vtk.vtkPointSetToLabelHierarchy;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkStringArray;

public class GraticuleGUI extends JPanel implements ActionListener{
	protected DisplayAttributes displayAttributes; //From location plugin; contains font color, cone color...
	protected JCheckBox showCompass;
	//protected BranchGroup compassBG;
	protected JPanel compassPanel;
	protected JFormattedTextField latField,lonField;
	private static final long serialVersionUID = 1L;

	private JPanel displayPanel;

	/** Adjustable status field. */
	public static final JLabel status = new JLabel("Status");

	private ButtonGroup displayButtons;

	protected JRadioButton
	firstsceneRadioButton, // check box for dots option
	secondsceneRadioButton, // checkbox for triangle
	noneRadioButton,		// "no grid" option
	customRadioButton;

	protected JTextField customTextBox;
	private JComboBox presetsComboBox;

	private boolean labelsOn = true;
	private JCheckBox latLonLabelsCheckBox = new JCheckBox("Display latitude and longitude labels.", labelsOn);

	//private Switch switchNode;
	private GlobeBox gb1;
	private GlobeBox gb2;
	private GlobeBox gb3;
	private GlobeBox gb4;


	private JPanel
	gridDimensionsPanel,
	NorthPanel,
	EWPanel,
	SouthPanel,
	buttonPanel;

	protected JButton graticuleappsProp_apply; // "Apply" button
	// private JButton graticuleappsProp_view; // "Center View" button

	private ColorWellButton
	colorChooser,
	bckgroundColorChooser;

	private boolean firstTime = true;
	
	private vtkStringArray labels;
	private int labelLatCt=0;

	protected int
	lowerLatitude = 0,
	upperLatitude = 0,
	lowerLongitude = 0,
	upperLongitude = 0;

	protected double gridWidth = 1.0;
	private Color curColor = Color.WHITE;
	private GraticulePresetModel presetModel;
	private ArrayList<GlobeBox> grids;

	private IntegerTextField
	relIntensityProp_extentsNval1 = new IntegerTextField(2, false, 3),
	relIntensityProp_extentsWval1 = new IntegerTextField(3, false, 3),
	relIntensityProp_extentsEval1 = new IntegerTextField(3, false, 3),
	relIntensityProp_extentsSval1 = new IntegerTextField(2, false, 3);

	public int upperLat, lowerLat, upperLon, lowerLon;
	private vtkActor tempGlobeScene=new vtkActor();
	private vtkActor pointActor = new vtkActor();
	vtkActor2D labelActor =new vtkActor2D();
	private boolean gridDisplay=true;
	
	PluginActors pluginActors = new PluginActors();
	
	public static GraticulePreset getGraticlePreset(){
		URL calGridURL = GraticuleGUI.class.getResource("resources/California.grat");
		File calGrid = null;
		try {
			calGrid = new File(calGridURL.toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new GraticulePreset(calGrid);
	}
	
	public void makeGrids(ArrayList<GlobeBox> gbs, boolean labelsOn)
	{

		vtkPolyDataMapper tempMapper = (vtkPolyDataMapper) (gbs.get(0)).globeScene; 
		tempGlobeScene.SetMapper(tempMapper);
		if(gbs.get(0).getLineColor() != null)
			tempGlobeScene.GetProperty().SetColor(Info.convertColor(gbs.get(0).getLineColor()));
		else
			tempGlobeScene.GetProperty().SetColor(1,1,1);
		tempGlobeScene.Modified();
		//renderWindow.GetRenderer().Render();

		tempMapper.GetInput();


		pointActor.SetMapper(gbs.get(0).ptMapper);
		pointActor.Modified();
		if(labelsOn)
			labelActor.SetMapper(gbs.get(0).labelMapperLat);
		else
			labelActor.SetMapper(null);
		labelActor.Modified();
		
		this.pluginActors.addActor(tempGlobeScene);
		this.pluginActors.addActor(pointActor);
		this.pluginActors.addActor(labelActor);
		Info.getMainGUI().getRenderWindow().GetRenderer().ResetCamera(tempGlobeScene.GetBounds());
	}
	public void toggleGridDisplay() {
		if (!this.gridDisplay) {
			tempGlobeScene.VisibilityOn();
			gridDisplay = true;
		} else {
			tempGlobeScene.VisibilityOff();
			gridDisplay = false;
		}
	}
	public boolean getGridDisplayBool() {
		return this.gridDisplay;
	}

	public vtkActor getGrid() {
		// TODO Auto-generated method stub
		return this.tempGlobeScene;
	}
	
	
	
	public ArrayList<GlobeBox> getGlobeBox(GraticulePreset graticule, double spacing){
		upperLat = (graticule.getUpperLatitude());
		lowerLat = (graticule.getLowerLatitude());
		upperLon = (graticule.getLeftLongitude());
		lowerLon = (graticule.getRightLongitude());

		ArrayList<GlobeBox> gbs = makeNewGrid(upperLat, lowerLat,  upperLon, lowerLon,spacing); //double spacing)
		return gbs;
	}
	private ArrayList<GlobeBox> makeGrids(int upperLat, int lowerLat,
			int leftLong, int rightLong, double spacing) {

		// Add label array.
		labels =new vtkStringArray();
		labels.SetName("labels");
		vtkIntArray sizes = new vtkIntArray();
		sizes.SetName("sizes");
		vtkPolyData temp = new vtkPolyData();

		//for latitude lines


		vtkPoints allPoints = new vtkPoints();
		vtkPoints labelPoints = new vtkPoints();
		//vtkPoints lonLabelPoints = new vtkPoints();
		int countPts =0;
		vtkCellArray lines =  new vtkCellArray();
		vtkPolyData linesPolyData =new vtkPolyData();

		vtkDoubleArray lat = new vtkDoubleArray();
		vtkDoubleArray lon = new vtkDoubleArray();
		lat.SetName("latitude");
		lon.SetName("longitude");
		vtkLine line0 = new vtkLine();
		//j-- is spacing 
		//INVERT IMAGE//
		double leftLon  = 1 * rightLong;
		double rightLon = 1 * leftLong; 
		//END IMAGE INVERT//
		int numOfLat = (int) (Math.ceil((upperLat-lowerLat)/spacing)+1);
		numOfLat += (int) (Math.ceil((leftLon-rightLon)/spacing)+1);
		labels.SetNumberOfValues(numOfLat+1);
		sizes.SetNumberOfValues(numOfLat);
		labelLatCt=0;
		int maxDepth = 0; 	
		for(double j = upperLat;j>=lowerLat;j-=spacing,labelLatCt++)
		{

			double[] pt = new double[3];
			pt[0] = Transform.calcRadius(j);
			// Phi= deg2rad(latitude);
			pt[1] = (j);
			//Theta= deg2rad(longitude);
			pt[2] = (leftLon);

			allPoints.InsertNextPoint(Transform.customTransform(pt));
			//Theta= deg2rad(longitude);
			//countPts++;
			for(double k=(leftLon-spacing);k>=rightLon;k-=spacing)
			{
				pt[2] = k;//(rightLon);
				allPoints.InsertNextPoint(Transform.customTransform(pt));
				
				line0.GetPointIds().SetId(0, countPts); // the second 0 is the index of the Origin in linesPolyData's points
				//countPts++;
				line0.GetPointIds().SetId(1, countPts+1); // the second 1 is the index of P0 in linesPolyData's points
				countPts++;
				lines.InsertNextCell(line0);
			}
			countPts++;
			pt[2] = (rightLon);
			if (labelsOn)
			{
				labels.SetValue(labelLatCt, new DecimalFormat("#.######").format(j));
			}
			labelPoints.InsertNextPoint(Transform.customTransform(pt));

		}

		//longitutde lines
		for(double j = leftLon;j>=rightLon;j-=spacing,labelLatCt++)
		{
			double[] pt = new double[3];
			pt[0] = Transform.calcRadius(upperLat);
			// Phi= deg2rad(latitude);
			pt[1] = (upperLat);
			//Theta= deg2rad(longitude);
			pt[2] = (j);

			allPoints.InsertNextPoint(Transform.customTransform(pt));
			if (labelsOn)
			{
				labels.SetValue(labelLatCt, new DecimalFormat("#.######").format(j));
			}

			labelPoints.InsertNextPoint(Transform.customTransform(pt));


			for(double k = (upperLat-spacing);k>=lowerLat;k-=spacing)
			{
				//Theta= deg2rad(longitude);
				pt[0] = Transform.calcRadius(k);
				// Phi= deg2rad(latitude);
				pt[1] = (k);
				allPoints.InsertNextPoint(Transform.customTransform(pt));
				
				line0.GetPointIds().SetId(0, countPts);
				//countPts++;// the second 0 is the index of the Origin in linesPolyData's points
				line0.GetPointIds().SetId(1, countPts+1);
				countPts++;// the second 1 is the index of P0 in linesPolyData's points
				lines.InsertNextCell(line0);
			}
			countPts++;
		}
		linesPolyData.SetPoints(allPoints);
		linesPolyData.SetLines(lines);

		temp.SetPoints(labelPoints);

		// Generate the label hierarchy.
		temp.GetPointData().AddArray(labels);
		vtkPointSetToLabelHierarchy pointSetToLabelHierarchyFilter =new vtkPointSetToLabelHierarchy();
		pointSetToLabelHierarchyFilter.SetInputData(temp);
		pointSetToLabelHierarchyFilter.SetLabelArrayName("labels");
		pointSetToLabelHierarchyFilter.Update();



		vtkPolyDataMapper globeMapper = new vtkPolyDataMapper();
		globeMapper.SetInputData(linesPolyData);

		vtkPolyDataMapper pointMapper = new vtkPolyDataMapper();
		pointMapper.SetInputData(temp);



		vtkLabelPlacementMapper cellMapper = new vtkLabelPlacementMapper();
		cellMapper.SetInputConnection(pointSetToLabelHierarchyFilter.GetOutputPort());

		ArrayList<GlobeBox> gbs = new ArrayList<GlobeBox>(4);
		Color tempColor3f = new Color(102,102,102);
		GlobeLayout gl = new GlobeLayout(upperLat, lowerLat, leftLong, rightLong, spacing);
		gbs.add(new GlobeBox(gl, tempColor3f, true));
		gbs.get(0).globeScene = globeMapper;
		gbs.get(0).labelMapperLat= cellMapper;
		gbs.get(0).ptMapper = pointMapper;  

		return gbs;
	}


	public ArrayList<GlobeBox> makeNewGrid(int upperLat, int lowerLat, int leftLong,
			int rightLong, double spacing) {
		//globeView = Geo3dInfo.getMainWindow();
		//if (!globeView.getGridDisplayBool())
		//globeView.toggleGridDisplay();
		ArrayList<GlobeBox> gbs = makeGrids(upperLat, lowerLat, leftLong, rightLong,
				spacing);
		// globeView.setGlobeBox(gbs[0]);
		return gbs;
		/*globeView.getSwitchNode().removeAllChildren();
				globeView.getSwitchNode().addChild(( gbs.get(0)).drawGlobe());
				globeView.getSwitchNode().addChild(( gbs.get(1)).drawGlobe());
				globeView.getSwitchNode().addChild(( gbs.get(2)).drawGlobe());
				globeView.getSwitchNode().addChild(( gbs.get(3)).drawGlobe());*/
	}
	public  ArrayList<GlobeBox> makeNewGrid(double spacing) {
		//globeView = Geo3dInfo.getMainWindow();
		//if (!globeView.getGridDisplayBool())
		//globeView.toggleGridDisplay();
		ArrayList<GlobeBox> gbs = makeGrids(Integer
				.parseInt(relIntensityProp_extentsNval1.getText()), Integer
				.parseInt(relIntensityProp_extentsSval1.getText()), Integer
				.parseInt(relIntensityProp_extentsWval1.getText()), Integer
				.parseInt(relIntensityProp_extentsEval1.getText()), spacing);
		// globeView.setGlobeBox(gbs[0]);
		if(curColor!=null)
			gbs.get(0).setLineColor(curColor);
		return gbs;
		//grids = gbs;
		/*globeView.getSwitchNode().removeAllChildren();
				globeView.getSwitchNode().addChild(((GlobeBox) gbs.get(0)).drawGlobe());
				globeView.getSwitchNode().addChild(((GlobeBox) gbs.get(1)).drawGlobe());
				globeView.getSwitchNode().addChild(((GlobeBox) gbs.get(2)).drawGlobe());
				globeView.getSwitchNode().addChild(((GlobeBox) gbs.get(3)).drawGlobe());*/
	}
	public GraticuleGUI(PluginActors pluginActors)
	{
		this.pluginActors = pluginActors;
		// Load presets from the central preset file
		//gratBranch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		//gratBranch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		//gratBranch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		//gratBranch.setCapability(BranchGroup.ALLOW_DETACH);

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()));

		makeGridDimensionsPanel();
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new BorderLayout());
		midPanel.add(gridDimensionsPanel, BorderLayout.CENTER);
		midPanel.setBorder(	BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15),
				BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Grid Dimensions"),
						BorderFactory.createEmptyBorder(10, 10, 10, 10))));
		midPanel.setMaximumSize(new Dimension(400, 200));
		add(getGridSettingsPanel());
		add(midPanel);
		add(Box.createVerticalGlue());
		//setUpSwitch();
		//globeView = Geo3dInfo.getMainWindow();
		//makePresetsPanel();
		//makeCompassPanel();
		//add(compassPanel);
		add(makebuttonPanel());
	}

	// ****************************************
	// PRIVATE GUI METHODS
	// ****************************************

	private JPanel getGridSettingsPanel()
	{
		// set up panel
		this.displayPanel = new JPanel(new GridLayout(0, 1, 5, 5));
		this.displayPanel.setName("Display Type");
		this.displayPanel.setOpaque(false);

		this.displayButtons = new ButtonGroup();
		this.firstsceneRadioButton = new JRadioButton("1.0 degrees");
		this.secondsceneRadioButton = new JRadioButton("0.1 degrees");
		this.noneRadioButton = new JRadioButton("No Grid");
		this.customRadioButton = new JRadioButton("Custom (degrees)");
		this.customTextBox = new JTextField(10);
		this.customTextBox.setEnabled(false);

		this.firstsceneRadioButton.addActionListener(this);
		this.secondsceneRadioButton.addActionListener(this);
		this.noneRadioButton.addActionListener(this);
		this.customRadioButton.addActionListener(this);

		this.displayButtons.add(this.noneRadioButton);
		this.displayButtons.add(this.firstsceneRadioButton);
		this.displayButtons.add(this.secondsceneRadioButton);
		this.displayButtons.add(this.customRadioButton);

		this.displayPanel.add(this.noneRadioButton);
		this.displayPanel.add(this.firstsceneRadioButton);
		this.displayPanel.add(this.secondsceneRadioButton);

		this.colorChooser = new ColorWellButton(Color.DARK_GRAY, 16, 16);
		colorChooser.setEnabled(true);
		colorChooser.addActionListener(this);

		this.bckgroundColorChooser = new ColorWellButton(Color.BLACK, 16, 16);
		bckgroundColorChooser.setEnabled(true);
		bckgroundColorChooser.addActionListener(this);

		this.displayPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15),
				BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Grid Settings"),
						BorderFactory.createEmptyBorder(15, 15, 15, 15))));
		Box bx = new Box(0);
		bx.add(this.customRadioButton);
		bx.add(this.customTextBox);
		this.displayPanel.add(bx);

		JLabel userInstructions = new JLabel("Select Grid Color   ");
		Box bx2 = new Box(0);
		bx2.add(userInstructions);
		bx2.add(colorChooser);
		displayPanel.add(bx2);

		JLabel bckColorUserInstructions = new JLabel("Select Background Color   ");
		Box bx3 = new Box(0);
		bx3.add(bckColorUserInstructions);
		bx3.add(bckgroundColorChooser);
		displayPanel.add(bx3);


		// Default
		this.firstsceneRadioButton.setSelected(true);

		presetModel = new GraticulePresetModel();

		presetsComboBox = new JComboBox(presetModel.getAllNames());
		presetsComboBox.addActionListener(this);
		this.displayPanel.add(presetsComboBox);
		this.displayPanel.add(latLonLabelsCheckBox);
		this.displayPanel.setMinimumSize(new Dimension(400, 120));
		this.displayPanel.setMaximumSize(new Dimension(400, 120));
		return this.displayPanel;
	}


	public void makeCompassPanel()
	{
		compassPanel = new JPanel();

		showCompass = new JCheckBox("Show Compass");
		showCompass.addActionListener(this);

		displayAttributes = new DisplayAttributes();

		if (Prefs.getOS() != Prefs.OSX)
		{
			compassPanel.setBorder(	BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16),
					BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Compass Settings"),
							BorderFactory.createEmptyBorder(0, 16, 16, 16))));
		}
		compassPanel.add(displayAttributes);
		compassPanel.add(showCompass);
		compassPanel.add(Box.createHorizontalStrut(2));
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setGroupingUsed(false);
		decimalFormat.setMinimumIntegerDigits(1);
		decimalFormat.setMaximumIntegerDigits(10);
		latField = new JFormattedTextField(decimalFormat);
		latField.setText("34.113");
		latField.addActionListener(this);

		DecimalFormat decimalFormat2 = new DecimalFormat();
		decimalFormat2.setGroupingUsed(false);
		decimalFormat2.setMinimumIntegerDigits(1);
		decimalFormat2.setMaximumIntegerDigits(10);
		lonField = new JFormattedTextField(decimalFormat2);
		lonField.setText("-118.2");
		lonField.addActionListener(this);

		compassPanel.add(new JLabel("Lat"));
		compassPanel.add(latField);
		compassPanel.add(Box.createHorizontalStrut(2));
		compassPanel.add(new JLabel("Lon"));
		compassPanel.add(lonField);
	}

	private void makeGridDimensionsPanel()
	{
		gridDimensionsPanel = new JPanel();

		ViewRange vr= Info.getMainGUI().getViewRange();

		populateFields(vr);

		JLabel relIntensityProp_extentsLat = new JLabel("Lat:");
		JLabel relIntensityProp_extentsLon = new JLabel("Lon:");
		JLabel relIntensityProp_extentsMax = new JLabel("(min)");
		JLabel relIntensityProp_extentsMax2 = new JLabel("(max)");
		relIntensityProp_extentsMax.setForeground(Color.GRAY);
		relIntensityProp_extentsMax2.setForeground(Color.GRAY);
		JLabel relIntensityProp_extentsMin = new JLabel("(min)");
		JLabel relIntensityProp_extentsMin2 = new JLabel("(max)");
		relIntensityProp_extentsMin.setForeground(Color.GRAY);
		relIntensityProp_extentsMin2.setForeground(Color.GRAY);
		JLabel relIntensityProp_Extents_to = new JLabel("to");

		// make the North Panel
		NorthPanel = new JPanel();
		NorthPanel.setLayout(new BoxLayout(NorthPanel, BoxLayout.LINE_AXIS));
		NorthPanel.add(new JLabel("Lat:"));
		NorthPanel.add(Box.createRigidArea(new Dimension(4, 0)));
		NorthPanel.add(relIntensityProp_extentsNval1);
		NorthPanel.add(Box.createRigidArea(new Dimension(4, 0)));
		NorthPanel.add(relIntensityProp_extentsMax2);
		NorthPanel.setMaximumSize(new Dimension(100, 22));

		// make the East-West Panel
		EWPanel = new JPanel();
		EWPanel.setLayout(new BoxLayout(EWPanel, BoxLayout.LINE_AXIS));
		EWPanel.add(new JLabel("Lon:"));
		EWPanel.add(Box.createRigidArea(new Dimension(4, 0)));
		EWPanel.add(relIntensityProp_extentsWval1);
		EWPanel.add(Box.createRigidArea(new Dimension(4, 0)));
		EWPanel.add(relIntensityProp_extentsMax);
		EWPanel.add(Box.createRigidArea(new Dimension(40, 0)));
		EWPanel.add(relIntensityProp_Extents_to);
		EWPanel.add(Box.createRigidArea(new Dimension(40, 0)));
		EWPanel.add(relIntensityProp_extentsLon);
		EWPanel.add(Box.createRigidArea(new Dimension(4, 0)));
		EWPanel.add(relIntensityProp_extentsEval1);
		EWPanel.add(Box.createRigidArea(new Dimension(4, 0)));
		EWPanel.add(relIntensityProp_extentsMin2);
		EWPanel.setMaximumSize(new Dimension(300, 22));

		// make the South Panel
		SouthPanel = new JPanel();

		SouthPanel.setLayout(new BoxLayout(SouthPanel, BoxLayout.LINE_AXIS));
		SouthPanel.add(relIntensityProp_extentsLat);
		SouthPanel.add(Box.createRigidArea(new Dimension(4, 0)));
		SouthPanel.add(relIntensityProp_extentsSval1);
		SouthPanel.add(Box.createRigidArea(new Dimension(4, 0)));
		SouthPanel.add(relIntensityProp_extentsMin);
		SouthPanel.setMaximumSize(new Dimension(100, 22));
		// make the entire NSEW Panel
		gridDimensionsPanel.setLayout(new BoxLayout(gridDimensionsPanel, BoxLayout.PAGE_AXIS));
		gridDimensionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		gridDimensionsPanel.add(NorthPanel);
		NorthPanel.setAlignmentX(CENTER_ALIGNMENT);
		gridDimensionsPanel.add(Box.createRigidArea(new Dimension(0, 6)));
		gridDimensionsPanel.add(EWPanel);
		EWPanel.setAlignmentX(CENTER_ALIGNMENT);
		gridDimensionsPanel.add(Box.createRigidArea(new Dimension(0, 6)));
		gridDimensionsPanel.add(SouthPanel);
		SouthPanel.setAlignmentX(CENTER_ALIGNMENT);
		gridDimensionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
	}

	private JPanel makebuttonPanel() {
		buttonPanel = new JPanel();
		buttonPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		graticuleappsProp_apply = new JButton("Apply"); // "button
		graticuleappsProp_apply.addActionListener(this);
		graticuleappsProp_apply.setActionCommand("apply");

		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

		buttonPanel.add(graticuleappsProp_apply);

		return (buttonPanel);
	}



	public void apply() {
		
		if (latLonLabelsCheckBox.isSelected() != labelsOn)
		{
			labelsOn = latLonLabelsCheckBox.isSelected();
		}
		if (!getGridDisplayBool())
			toggleGridDisplay();

		if (firstsceneRadioButton.isSelected()) {
			gridWidth = 1.0;
			makeGrids(makeNewGrid(gridWidth),labelsOn);
			Info.getMainGUI().updateRenderWindow();
			System.out.println("One degree selected");
			
		} else if (secondsceneRadioButton.isSelected()) {
			gridWidth = 0.1;
			makeGrids(makeNewGrid(gridWidth),labelsOn);
			Info.getMainGUI().updateRenderWindow();
			System.out.println("0.1 degree selected");
		}
		else if (noneRadioButton.isSelected()) {
			if (getGridDisplayBool())
			{
				makeGrids(makeNewGrid(gridWidth),labelsOn);
				toggleGridDisplay();
			}
			System.out.println("No grid selected");
			Info.getMainGUI().updateRenderWindow();
		} else if (customRadioButton.isSelected()) {
			double customGrid = 0;
			try {
				customGrid = Double.parseDouble(customTextBox.getText());
				if (customGrid >= .05) {
					gridWidth = customGrid;
					makeGrids(makeNewGrid(gridWidth),labelsOn);
					Info.getMainGUI().updateRenderWindow();
				} else {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException nfe) {
				customTextBox
				.setText("Please enter a number greater than .05");
				customTextBox.setSelectionStart(0);
				customTextBox.setSelectionEnd(customTextBox.getText()
						.length());
				customTextBox.requestFocus();
			}
		}
		//if(showCompass.isSelected()){
			/*if( compassBG != null){
				compassBG.detach();
			}
			compassBG = new SurfaceCompass(Double.parseDouble(this.latField.getText()),Double.parseDouble(this.lonField.getText()),0,1,displayAttributes).getBranchGroup();
			Geo3dInfo.getMainWindow().getPluginBranchGroup().addChild(compassBG);
		}else if (!showCompass.isSelected()){
			if( compassBG != null){
				compassBG.detach();
			}*/
		//}

		if (latLonLabelsCheckBox.isSelected()!=labelsOn) {
			labelsOn = latLonLabelsCheckBox.isSelected();
			makeGrids(makeNewGrid(gridWidth), labelsOn);
			Info.getMainGUI().updateRenderWindow();
			System.out.println("Checkbox switched from " + !labelsOn + " to " + labelsOn);
			//globeView.getSwitchNode().removeAllChildren();
			/*if (grids != null) {
				for (GlobeBox gb: grids) {
					//gb.setShowLabels(labelsOn);
					//globeView.getSwitchNode().addChild(gb.getGlobeScene());
				}
			}*/
		}
	}

	public void setGridColor(Color color) {
		curColor = color;
		this.colorChooser.setColor(color);
	}

	public void setLabelsDisplayed(boolean displayed) {
		this.latLonLabelsCheckBox.setSelected(displayed);
	}

	public void actionPerformed(ActionEvent arg0) {
		customTextBox.setEnabled(customRadioButton.isSelected());

		if (arg0.getActionCommand() == "apply") {
			apply();
		} else if (arg0.getSource() == this.colorChooser) {
			SingleColorChooser colorChooser = new SingleColorChooser(null);
			Color tempColor = colorChooser.getColor();
			if (tempColor != null) {
				setGridColor(tempColor);
				getGrid().GetProperty().SetColor(Info.convertColor(tempColor));	
				Info.getMainGUI().updateRenderWindow();
			}
		} else if (arg0.getSource() == this.bckgroundColorChooser) {
			SingleColorChooser colorChooser = new SingleColorChooser(null);
			Color tempColor = colorChooser.getColor();
			if (tempColor != null)
			{	
				this.bckgroundColorChooser.setColor(tempColor);
				Info.getMainGUI().getRenderWindow().GetRenderer().SetBackground(Info.convertColor(tempColor));
				Info.getMainGUI().updateRenderWindow();
			}
		}
		else if (arg0.getSource() == presetsComboBox)
		{
			GraticulePreset preset = presetModel.getPreset(presetsComboBox.getSelectedIndex());

			//Info.getMainGUI().makeGrids(makegrids.makeNewGrid(preset.getUpperLatitude(),preset.getLowerLatitude(),
			//preset.getLeftLongitude(),preset.getRightLongitude(),1));
			Info.getMainGUI().updateRenderWindow();
			relIntensityProp_extentsNval1.setText(preset.getUpperLatitude()+"");
			relIntensityProp_extentsSval1.setText(preset.getLowerLatitude()+"");
			relIntensityProp_extentsEval1.setText(preset.getRightLongitude()+"");
			relIntensityProp_extentsWval1.setText(preset.getLeftLongitude()+"");
		}

	}

	public double getGridWidth() {
		return this.gridWidth;
	}

	protected void populateFields(ViewRange range) {
		relIntensityProp_extentsNval1.setText(range.getUpperLatitudeAsString());
		relIntensityProp_extentsSval1.setText(range.getLowerLatitudeAsString());
		relIntensityProp_extentsEval1
		.setText(range.getRightLongitudeAsString());
		relIntensityProp_extentsWval1.setText(range.getLeftLongitudeAsString());
		if (firstTime) { // needed to fix bug in save state...doesn't save
			// initial values if they are never changed without
			// this code
			this.upperLatitude = range.getUpperLatitude();
			this.lowerLatitude = range.getLowerLatitude();

			this.upperLongitude = range.getRightLongitude();
			this.lowerLongitude = range.getLeftLongitude();
			firstTime = false;
		}
	}
}
