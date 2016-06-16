package org.scec.vtk.plugins.EarthquakeCatalogPlugin.RelativeIntensity;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginGUI;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.CatalogTable;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.CatalogTableModel;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;

public class RelativeIntensityGUI extends JPanel implements ActionListener, FocusListener{
	private static final long serialVersionUID = 1L;

	private RelativeIntensity parent;
	private EarthquakeCatalogPluginGUI eqGUI;

	private JTextField northLatField	= new JTextField("38.3", 5);
	private JTextField southLatField	= new JTextField("32", 5);
	private JTextField westLonField		= new JTextField("-123", 5);
	private JTextField eastLonField		= new JTextField("-115", 5);

	private JTextField binDimField		= new JTextField("0.1", 1);
	public JCheckBox binningOnBox			= new JCheckBox("Turn Binning On");
	private JCheckBox marginErrorBox	= new JCheckBox("Use Margin of Error");

	private JComboBox inputEqMenu		= new JComboBox();
	private JComboBox targetEqMenu		= new JComboBox();

	private JCheckBox epicenterBox		= new JCheckBox("Display Target Epicenter");
	private JCheckBox exagerationBox	= new JCheckBox("Turn Vertical Exaggeration On");

	private JButton	applyButton			= new JButton("Apply");
	private JButton weightedButton		= new JButton("Weighted Molchan");
	private JButton unweightedButton	= new JButton("Unweighted Molchan");

	private String margErrorTitle		= "Without Margin of Error";

	public RelativeIntensityGUI(RelativeIntensity newParent, Component parent){ 
		this.parent = newParent;
		this.eqGUI = (EarthquakeCatalogPluginGUI)parent; 

		this.setName("Relative Intensity");
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setMaximumSize(new Dimension(350, 350));
		
		this.add(getTitlePanel());
		this.add(Box.createRigidArea(new Dimension(0,5)));
		this.add(getExtentsPanel());
		this.add(Box.createRigidArea(new Dimension (0, 10)));
		this.add(getBinDimPanel());
		this.add(makebottomPanel());
		this.add(getCheckboxPanel());
		this.add(getButtonPanel());
	}

	private JPanel makebottomPanel() {
		JPanel bottomPanel = new JPanel();

		// setup combo boxes:
		CatalogTableModel ctm = eqGUI.getCatalogTable().getLibraryModel();
		for(int i = 0; i < ctm.getRowCount(); i++){
			EQCatalog eqCat = (EQCatalog)ctm.getObjectAtRow(i);
			inputEqMenu.addItem(eqCat.toString());
			targetEqMenu.addItem(eqCat.toString());
		}
		
		// input panel:
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
		inputPanel.add(new JLabel("Select Input EQ Catalog:"));
		inputPanel.add(Box.createRigidArea(new Dimension(40,0)));
		inputPanel.add(inputEqMenu);
		inputPanel.add(Box.createRigidArea(new Dimension(5,0)));
		
		// target panel:
		JPanel targetPanel = new JPanel();
		targetPanel.setLayout(new BoxLayout(targetPanel, BoxLayout.LINE_AXIS));
		targetPanel.add(new JLabel("Select Target EQ Epicenter:"));
		targetPanel.add(Box.createRigidArea(new Dimension(26,0)));
		targetPanel.add(targetEqMenu);
		targetPanel.add(Box.createRigidArea(new Dimension(5,0)));

		// assemble bottom panel:
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));
		bottomPanel.add(Box.createRigidArea(new Dimension(0,20)));
		bottomPanel.add(inputPanel);
		bottomPanel.add(Box.createRigidArea(new Dimension(0,10)));
		bottomPanel.add(targetPanel);
		bottomPanel.add(Box.createRigidArea(new Dimension(0,10)));
		bottomPanel.setMaximumSize(new Dimension(350,300));

		inputEqMenu.addActionListener(this);
		targetEqMenu.addActionListener(this); 
		
		return bottomPanel;
	}

	private JPanel getExtentsPanel() {
		JPanel extentsPanel = new JPanel();

		// north/south panel:
		JPanel nsPanel = new JPanel();
		nsPanel.setLayout(new BoxLayout(nsPanel, BoxLayout.LINE_AXIS));
		nsPanel.add(new JLabel("Min Lat:"));
		nsPanel.add(Box.createRigidArea(new Dimension(5,0)));
		nsPanel.add(southLatField);
		nsPanel.add(Box.createRigidArea(new Dimension(20,0)));
		nsPanel.add(new JLabel("Max Lat:"));
		nsPanel.add(Box.createRigidArea(new Dimension(5,0)));
		nsPanel.add(northLatField);
		nsPanel.add(Box.createRigidArea(new Dimension(20,0)));

		// east/west panel:
		JPanel ewPanel = new JPanel();
		ewPanel.setLayout(new BoxLayout(ewPanel, BoxLayout.LINE_AXIS));
		ewPanel.add(new JLabel("Min Lon:"));
		ewPanel.add(Box.createRigidArea(new Dimension(5,0)));
		ewPanel.add(westLonField);
		ewPanel.add(Box.createRigidArea(new Dimension(20,0)));
		ewPanel.add(new JLabel("Max Lon:"));
		ewPanel.add(Box.createRigidArea(new Dimension(5,0)));
		ewPanel.add(eastLonField);
		ewPanel.add(Box.createRigidArea(new Dimension(20,0)));

		// assemble extents panel
		extentsPanel.setLayout(new BoxLayout(extentsPanel, BoxLayout.Y_AXIS));
		extentsPanel.add(nsPanel);
		extentsPanel.add(Box.createRigidArea(new Dimension(10,10)));
		extentsPanel.add(ewPanel);
		extentsPanel.add(Box.createRigidArea(new Dimension(0,10)));
		extentsPanel.setMaximumSize(new Dimension(300,100));
		
		southLatField.addFocusListener(this);
		northLatField.addFocusListener(this);
		westLonField.addFocusListener(this);
		eastLonField.addFocusListener(this);

		return extentsPanel;
	}

	private JPanel getTitlePanel(){
		JPanel titlePanel = new JPanel(new GridLayout(1, 1, 10, 10));
		titlePanel.add(new JLabel("Bin Extents:"));
		return titlePanel;
	}

	private JPanel getBinDimPanel(){
		JPanel binDimPanel = new JPanel();
		
		binDimPanel.setLayout(new BoxLayout(binDimPanel, BoxLayout.LINE_AXIS));
		binDimPanel.add(new JLabel("Bin Dimension(degs):"));
		binDimPanel.add(Box.createRigidArea(new Dimension(5,10)));
		binDimPanel.add(binDimField);
		binDimPanel.add(Box.createRigidArea(new Dimension(5,0)));
		binDimPanel.add(binningOnBox);
		binDimPanel.add(Box.createRigidArea(new Dimension(5,0)));
		binDimPanel.add(marginErrorBox);
		binDimPanel.setMaximumSize(new Dimension (400, 15));

		binDimField.addFocusListener(this);
		marginErrorBox.addActionListener(this);
		binningOnBox.addActionListener(this);
		
		binningOnBox.setEnabled(false);

		return binDimPanel;
	}

	private JPanel getButtonPanel(){
		JPanel buttonPanel = new JPanel();
		
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(applyButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(20,0)));
		buttonPanel.add(weightedButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(20,0)));
		buttonPanel.add(unweightedButton);

		applyButton.addActionListener(this);
		weightedButton.addActionListener(this);
		unweightedButton.addActionListener(this);
		
		weightedButton.setEnabled(true);
		unweightedButton.setEnabled(true);

		return buttonPanel;
	}

	private JPanel getCheckboxPanel(){
		JPanel checkboxPanel = new JPanel();

		checkboxPanel.add(epicenterBox);
		checkboxPanel.add(Box.createRigidArea(new Dimension(5,0)));
		checkboxPanel.add(exagerationBox);
		checkboxPanel.add(Box.createRigidArea(new Dimension(5,0)));

		epicenterBox.addActionListener(this);
		exagerationBox.addActionListener(this);
		
		epicenterBox.setEnabled(false);
		exagerationBox.setEnabled(false);

		return checkboxPanel;
	}
	
	/**
	 * Updates the ComboBox, which displays all the earthquake catalogs currently available 
	 * in the upper earthquake gui panel
	 *
	 */
	public void updateCombo() {
		// The CatalogTableModel holds all the EQCatalog entries
		CatalogTableModel ctm = eqGUI.getCatalogTable().getLibraryModel();

		EQCatalog eqCat;
		if(ctm != null){
			// Remove all the entries in the combobox if there are any
			if(inputEqMenu != null){
				inputEqMenu.removeAllItems();
				targetEqMenu.removeAllItems();
				// Add the updated list to the combobox
				for(int i=0; i<ctm.getRowCount(); i++){
					eqCat = (EQCatalog)ctm.getObjectAtRow(i);
					inputEqMenu.addItem(eqCat.toString());
					targetEqMenu.addItem(eqCat.toString());
				}
			}
		}
	}

	public String getMaxLat()		{ return northLatField.getText(); }
	public String getMinLat()		{ return southLatField.getText(); }
	public String getMaxLon() 		{ return eastLonField.getText(); }
	public String getMinLon()		{ return westLonField.getText(); }
	public String getBinDimension()	{ return binDimField.getText(); }

	/**
	 * Retreive selected catalog for binning
	 * @return
	 */
	public EQCatalog getSourceCatalog(){
		int selectedIndex = inputEqMenu.getSelectedIndex();	
		CatalogTableModel ctm = eqGUI.getCatalogTable().getLibraryModel();
		EQCatalog eqCat;

		eqCat = (EQCatalog)ctm.getObjectAtRow(selectedIndex);

		return eqCat;
	}

	/**
	 * Retreive selected catalog for binning
	 * @return
	 */
	public EQCatalog getTargetCatalog(){
		int selectedIndex = targetEqMenu.getSelectedIndex();
		CatalogTableModel ctm = eqGUI.getCatalogTable().getLibraryModel();
		EQCatalog eqCat;

		eqCat = (EQCatalog)ctm.getObjectAtRow(selectedIndex);

		return eqCat;
	}

	private void displayRIMap() {
		makeRIMap();
	
		if(exagerationBox.isSelected()){
			parent.hideGrid();
			parent.showGrid(100);
		}
		if (epicenterBox.isSelected()) {
			parent.showTargetEQs();
		}
	
		binningOnBox.setEnabled(true);
		exagerationBox.setEnabled(true);
		epicenterBox.setEnabled(true);
	
		applyButton.setEnabled(false);
	}

	/**
	 * Creates the RI Map for display after the Apply button is clicked
	 *
	 */
	private void makeRIMap(){

		//If the grid exists, remove it so a new one can be made
		if(parent.gridExists()){
			parent.hideGrid();
		}

		//Assign EQ catalogs
		EQCatalog source = getSourceCatalog();
		EQCatalog target = getTargetCatalog();

		//Check if the catalogs are loaded into memory, if not, load them
		if(!source.isInMemory()){
			source.setInMemory(true);
			CatalogTable ct = eqGUI.getCatalogTable();
			ct.getLibraryModel().setLoadedStateForRow(
					true, ct.getLibraryModel().indexOf(source));
		}
		if(!target.isInMemory()){
			target.setInMemory(true);
			CatalogTable ct = eqGUI.getCatalogTable();
			ct.getLibraryModel().setLoadedStateForRow(
					true, ct.getLibraryModel().indexOf(target));
		}

		//Put the earthquakes into bins
		parent.plotEQs();
	}


	private void displayMolchanGraph(boolean weighted) {
		displayRIMap();
	
		float[][] data = parent.getMolchanTrajectory();
	
		Mocho molchanGraph = new Mocho(this);
	
		String title = "Latitude: " + getMinLat() + " to " + getMaxLat() + ";" +
		"Longitude: " + getMinLon()+ " to " + getMaxLon() + ";\n" +
		"Source Min Mag: " + getSourceCatalog().getMinMagnitude() + ";" +
		"Target Min Mag: " + getTargetCatalog().getMinMagnitude() + ";\n" +
		"Input Dates: "  + DateFormat.getDateInstance(DateFormat.MEDIUM).format(getSourceCatalog().getMinDate()) + " to " +
						   DateFormat.getDateInstance(DateFormat.MEDIUM).format(getSourceCatalog().getMaxDate()) + ";\n" +
		"Target Dates: " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(getTargetCatalog().getMinDate()) + " to " +
		                   DateFormat.getDateInstance(DateFormat.MEDIUM).format(getTargetCatalog().getMaxDate()) + ";";	
	
		JPanel mochoPanel = new JPanel();
		JFrame dialog = new JFrame((weighted ? "Weighted" : "Unweighted") + " Molchan Diagram " + margErrorTitle);
		mochoPanel.setLayout(new BoxLayout(mochoPanel, BoxLayout.PAGE_AXIS));
		mochoPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		mochoPanel.add(Box.createRigidArea(new Dimension(0,20)));
		mochoPanel.add(molchanGraph.createGraph(data, title, weighted));
		mochoPanel.add(Box.createRigidArea(new Dimension(0,20)));
		
		JPanel mButtonPanel = new JPanel();
		mButtonPanel.setLayout(new BoxLayout(mButtonPanel, BoxLayout.LINE_AXIS));
		mButtonPanel.setMaximumSize(new Dimension(600,100));
	
		dialog.add(mochoPanel);
		dialog.pack();
		dialog.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if (src == applyButton) { // "Apply" button
			displayRIMap();
		} 
		else if (src == weightedButton) { // "Weighted Molchan" buton
			displayMolchanGraph(true);
		} 
		else if (src == unweightedButton) { // "Unweighted Molchan"
			displayMolchanGraph(false);
		} 
		else if (src == binningOnBox){ // "Turn binning on" checkbox
			if(binningOnBox.isSelected()){
				parent.showGrid();
				if (epicenterBox.isSelected())
					parent.showTargetEQs();
				exagerationBox.setEnabled(true);
				epicenterBox.setEnabled(true);
			} else {
				parent.hideGrid();
				if (epicenterBox.isSelected())
					parent.hideTargetEQs();
				exagerationBox.setEnabled(false);
				epicenterBox.setEnabled(false);
			}
		}
		else if (src == epicenterBox){ // "Display Target EQ Epicenter" checkbox
			if (epicenterBox.isSelected())
				parent.showTargetEQs();
			else
				parent.hideTargetEQs();
		}
		else if (src == exagerationBox){ // "Turn Verticle Exageration On" checkbox
			if (binningOnBox.isSelected()){
				if (exagerationBox.isSelected()){
					parent.hideGrid();
					parent.showGrid(100);
				} else {
					parent.hideGrid();
					parent.showGrid(0);
				}
			}
		}
		else if (src == marginErrorBox){ // "Use Margin of Error" checkbox
			if (marginErrorBox.isSelected()){
				parent.setUseMarginOfError(true);
				margErrorTitle = "With Margin of Error"; 			
			} else {
				parent.setUseMarginOfError(false);
				margErrorTitle = "Without Margin of Error"; 
			}
		}
		else if (src == targetEqMenu || src == inputEqMenu) {
			applyButton.setEnabled(true);
		}
	}

	public void focusGained(FocusEvent arg0) {
		applyButton.setEnabled(true);
	}

	public void focusLost(FocusEvent arg0) {
		applyButton.setEnabled(true);
	}
}
