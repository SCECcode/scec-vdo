package org.scec.vtk.plugins.LegendPlugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.scec.vtk.commons.legend.LegendItem;
import org.scec.vtk.commons.legend.LegendUtils;
import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.PluginActorsChangeListener;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;
import org.scec.vtk.plugins.LegendPlugin.Component.FontDialog;
import org.scec.vtk.plugins.utils.components.ColorButton;
import org.scec.vtk.plugins.utils.components.DataFileChooser;
import org.scec.vtk.plugins.utils.components.ImageFileChooser;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;

import com.google.common.base.Preconditions;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import java.awt.event.MouseListener;

import vtk.vtkActor2D;
import vtk.vtkColorTransferFunction;
import vtk.vtkImageMapper;
import vtk.vtkImageResize;
import vtk.vtkJPEGReader;
import vtk.vtkLookupTable;
import vtk.vtkProp;
import vtk.vtkScalarBarActor;
import vtk.vtkTextActor;

public class LegendPluginGUI extends JPanel implements ActionListener, ChangeListener, ListSelectionListener,
PluginActorsChangeListener {

	private static final long serialVersionUID = 1L;
	private JButton displayButton, moveLeftButton, moveRightButton, moveUpButton, moveDownButton;
	private JButton imageButton, textButton, removeButton, createButton, editButton;
	private SingleColorChooser colorChooser = new SingleColorChooser(this);

	private JSlider transparencySlider;
	private JTextField scaleField;
	private JList<LegendItem> legendSelectList;
	private DefaultListModel<LegendItem> model;

	final float SCALE = 0.5f;

	public CreateLegendsGUI createLeg;
	
	private LegendPlugin plugin;
	private PluginActors legendActors;
	
	private ImageFileChooser chooser;

	public LegendPluginGUI(LegendPlugin plugin)
	{
		super();
		this.plugin = plugin;
		this.legendActors = plugin.getPluginActors();
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
//		scaleField = new JTextField("1.0");
//		scaleField.addActionListener(this);
//		JPanel scalePanel = new JPanel();
//		scalePanel.setLayout(new GridLayout(1,2));
//		scalePanel.setBorder(BorderFactory.createTitledBorder("Scale"));
//		scalePanel.add(scaleField);

		displayButton = new JButton("Display");
		displayButton.setEnabled(false);
		displayButton.addActionListener(this);
		moveLeftButton = new JButton("Move Left");
		moveLeftButton.setEnabled(false);
		moveLeftButton.addActionListener(this);
		
		moveRightButton = new JButton("Move Right");
		moveRightButton.setEnabled(false);
		moveRightButton.addActionListener(this);
		
		moveUpButton = new JButton("Move Up");
		moveUpButton.setEnabled(false);
		moveUpButton.addActionListener(this);
		
		moveDownButton = new JButton("Move Down");
		moveDownButton.setEnabled(false);
		moveDownButton.addActionListener(this);

		JPanel upperButtonPanel = new JPanel();
		upperButtonPanel.setLayout(new BoxLayout(upperButtonPanel,BoxLayout.X_AXIS));
		upperButtonPanel.setBorder(new EmptyBorder(5,0,0,0));
		upperButtonPanel.add(displayButton);
		upperButtonPanel.add(moveLeftButton);
		upperButtonPanel.add(moveRightButton);
		upperButtonPanel.add(moveUpButton);
		upperButtonPanel.add(moveDownButton);

		model = new DefaultListModel<>();
		legendSelectList = new JList<>(model);
		legendSelectList.addListSelectionListener(this);
		JScrollPane listScroll = new JScrollPane(legendSelectList);
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel,BoxLayout.Y_AXIS));
		listPanel.setBorder(BorderFactory.createTitledBorder("Legends"));
		listPanel.add(upperButtonPanel);
		listPanel.add(listScroll);

		imageButton = new JButton("Add Image");
		imageButton.setEnabled(true);
		imageButton.addActionListener(this);
		createButton = new JButton("Create Legend");
		createButton.setEnabled(true);
		createButton.addActionListener(this);
		textButton = new JButton("Add Text");
		textButton.addActionListener(this);
		textButton.setEnabled(true);
		editButton = new JButton("Edit Text");
		editButton.addActionListener(this);
		editButton.setEnabled(false);
		
		removeButton = new JButton("Remove");
		removeButton.addActionListener(this);
		removeButton.setEnabled(false);
		JPanel lowerButtonPanel = new JPanel();
		lowerButtonPanel.setLayout(new BoxLayout(lowerButtonPanel,BoxLayout.X_AXIS));
		lowerButtonPanel.setBorder(new EmptyBorder(5,0,0,0));
		lowerButtonPanel.add(imageButton);
		lowerButtonPanel.add(createButton);
		lowerButtonPanel.add(textButton);
		lowerButtonPanel.add(editButton);
		lowerButtonPanel.add(removeButton);
		listPanel.add(lowerButtonPanel);
		
//		add(scalePanel);
		add(listPanel);
		
		Info.getMainGUI().addPluginActorsChangeListener(this);
		// now add any legends that were created before this plugin was instantiated
		for (LegendItem legend : Info.getMainGUI().getDisplayedLegends())
			legendAdded(legend);
	}

	public void valueChanged(ListSelectionEvent e)
	{
		Object source = e.getSource();
		
		if (source == legendSelectList)
		{
			LegendItem legend = legendSelectList.getSelectedValue();
			if (legend != null) {
				int visibility = legend.getActor().GetVisibility();
				if (visibility == 1)
				{
					displayButton.setText("Hide");
					setMoveButtonsEnabled(true);
					displayButton.setEnabled(true);
					if (legend.getActor() instanceof vtkTextActor) {
						editButton.setEnabled(true);
					} else {
						editButton.setEnabled(false);
					}
					removeButton.setEnabled(true);
				}
				else
				{
					displayButton.setText("Display");
					if (legend.getActor() instanceof vtkTextActor) {
						editButton.setEnabled(false);
					}
					setMoveButtonsEnabled(false);
					removeButton.setEnabled(false);
				}
			}
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();

		if (source == imageButton)
		{
			
			if (chooser == null) {
				chooser = new ImageFileChooser();
				File dataDir = new File(MainGUI.getCWD(),"data");
	
			if(dataDir.exists())
				this.chooser.setCurrentDirectory(dataDir);
				
			
			}

			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				try {
					LegendItem legend = LegendUtils.buildImageLegend(plugin, file, 0d, 0d);
					legendActors.addLegend(legend); // this will trigger a call to legendAdded
					MainGUI.updateRenderWindow();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Error loading image", JOptionPane.ERROR_MESSAGE);
				} catch (NullPointerException e2) {
					JOptionPane.showMessageDialog(this, "That image type is not supported. Please use a .jpg, .png, or .tiff image.", "Error loading image.", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if (source == textButton)
		{
			TextDialogBox textDialog = new TextDialogBox("");
			String text = textDialog.getText();
			
			if (text != null && !text.equals(""))
			{
				Font font = textDialog.getFont();
				Color color = textDialog.getColor();
				LegendItem legend = LegendUtils.buildTextLegend(plugin, text, font, color, 0d, 0d);
				legendActors.addLegend(legend); // this will trigger a call to legendAdded
				MainGUI.updateRenderWindow();
			}
		}
		else if (source == removeButton)
		{
			LegendItem legend = legendSelectList.getSelectedValue();
			if (legend != null) {
				Plugin plugin = legend.getSource();
				plugin.getPluginActors().removeLegend(legend);
				MainGUI.updateRenderWindow();
			}
		}
		else if (source == displayButton)
		{	
			LegendItem legend = legendSelectList.getSelectedValue();
			if (legend != null) {
				vtkActor2D legendActor = legend.getActor();
				int visibility = legendActor.GetVisibility();
				if (visibility == 1) {
					legendActor.SetVisibility(0);
					displayButton.setText("Display");
					enableButtons(false);
				} else {
					legendActor.SetVisibility(1);
					displayButton.setText("Hide");
					enableButtons(true);
				}
				legendActor.Modified();
				MainGUI.updateRenderWindow();
			}
		}
		else if (source == editButton)
		{
			LegendItem legend = legendSelectList.getSelectedValue();
			if (legend != null) {
				vtkTextActor actor = (vtkTextActor)legend.getActor();
				TextDialogBox textDialog = new TextDialogBox(actor.GetInput());
				String text = textDialog.getText();
				Font font = textDialog.getFont();
				Color color = textDialog.getColor();
				actor.SetInput(text);
				actor.GetTextProperty().SetFontFamilyAsString(font.getFamily());
				actor.GetTextProperty().SetFontSize(font.getSize());
				actor.GetProperty().SetColor(color.getRed(), color.getGreen(), color.getBlue());
				actor.Modified();
				legend.setTitle(text);
				MainGUI.updateRenderWindow();
				legendSelectList.updateUI();
			}
		}
		else if(source == moveLeftButton)
		{
			LegendItem legend = legendSelectList.getSelectedValue();
			if (legend != null)
				moveLegend(legend, -5, 0);
		}
		else if (source == moveRightButton)
		{
			LegendItem legend = legendSelectList.getSelectedValue();
			if (legend != null)
				moveLegend(legend, 5, 0);
		}
		else if (source == moveUpButton)
		{
			System.out.println("move up");
			LegendItem legend = legendSelectList.getSelectedValue();
			if (legend != null)
				moveLegend(legend, 0, 5);
		}
		else if (source == moveDownButton)
		{
			LegendItem legend = legendSelectList.getSelectedValue();
			if (legend != null)
				moveLegend(legend, 0, -5);
		}
		else if(source == scaleField){
			//Update if the value changes
			updateScale();
		}
		else if(source == createButton){
			// TODO
			createLeg = new CreateLegendsGUI(scaleField, displayButton, transparencySlider);
			createLeg.setLocation(Info.getMainGUI().getLocation());
			createLeg.setVisible(true);
		}
	}
	
	private void moveLegend(LegendItem legend, double x, double y)
	{
		vtkActor2D actor = legend.getActor();
		
		double[] position = actor.GetPosition();
		if (actor instanceof vtkScalarBarActor)
			// TODO figure out why things are different for scalar bars and remove this hack
			actor.SetPosition(position[0] + x*0.01, position[1] + y*0.01);
		else
			actor.SetPosition(position[0] + x, position[1] + y);
		actor.Modified();
		MainGUI.updateRenderWindow();
	}
	
	/**
	 * Takes the value in the scale field and apply it to the image
	 */
	private void updateScale(){
		LegendItem legend = legendSelectList.getSelectedValue();

		if (legend != null) {
			try {
				double scaleFactor = new Double(scaleField.getText()).doubleValue();
				System.out.println(scaleFactor);
				if (scaleFactor == 0)
					return; // cannot be 0 or a crash will occur
				vtkImageResize resize = new vtkImageResize();
				vtkActor2D actor = legend.getActor();
				resize.SetInputData(((vtkImageMapper)actor.GetMapper()).GetInput());
				resize.SetOutputDimensions((int)(actor.GetHeight()*scaleFactor), (int)(actor.GetWidth()*scaleFactor), 1);
				resize.Update();
				actor.GetMapper().SetInputConnection(resize.GetOutputPort());
//				actor.SetHeight(actor.GetHeight()*scaleFactor);
//				actor.SetWidth(actor.GetWidth()*scaleFactor);
				actor.Modified();
			}
			catch(NumberFormatException nfe)
			{
				// User put in something that was NaN
			}
			MainGUI.updateRenderWindow();
		}
	}
	private void setMoveButtonsEnabled(boolean enabled)
	{
		moveUpButton.setEnabled(enabled);
		moveDownButton.setEnabled(enabled);
		moveLeftButton.setEnabled(enabled);
		moveRightButton.setEnabled(enabled);
	}
	
	public void stateChanged(ChangeEvent e)
	{	
		JSlider source = (JSlider)e.getSource();

		if (source == transparencySlider)
		{
			LegendItem legend = legendSelectList.getSelectedValue();

			if (legend != null) {
				int transparency = source.getValue();
				if (transparency == 0)
					return; // cannot be 0 or a crash will occur
				vtkActor2D actor = legend.getActor();
				actor.GetProperty().SetOpacity(transparency);
				actor.Modified();
				MainGUI.updateRenderWindow();
			}
		}
	}

	public void itemStateChanged(ItemEvent e)
	{
	}
	
	public void unload()
	{
		Info.getMainGUI().removePluginActorsChangeListener(this);
		// clear any custom legends
		legendActors.clearLegends();
		model.clear();
		MainGUI.updateRenderWindow();
	}
	
	public JList<LegendItem> getLegendSelectList()
	{
		return legendSelectList;
	}
	
	public DefaultListModel<LegendItem> getLegendModel()
	{
		return model;
	}
	
	public LegendPlugin getPlugin()
	{
		return plugin;
	}
	
	public void addLegend(LegendItem legend)
	{
		legendActors.addLegend(legend);
		displayButton.setEnabled(true);
		enableButtons(true);
	}
	
	private void enableButtons(boolean visibility)
	{
		setMoveButtonsEnabled(visibility);
		editButton.setEnabled(visibility);
		removeButton.setEnabled(visibility);
	}
	
	@Override
	public void actorAdded(vtkProp actor) {} // do nothing

	@Override
	public void actorRemoved(vtkProp actor) {} // do nothing

	@Override
	public synchronized void legendAdded(LegendItem legend) {
		// called whenever a legend is added
		if (!model.contains(legend))
			model.addElement(legend);
	}

	@Override
	public synchronized void legendRemoved(LegendItem legend) {
		// called whenever a legend is removed
		model.removeElement(legend);
	}
	
	class TextDialogBox {
		private JDialog dialog;
		private JTextArea textField;
		private JPanel upperPanel, lowerPanel;
		private JButton fontButton, okButton;
		private FontDialog fontGUI;
		private String text;
		private JScrollPane scrollPane;
		
		public TextDialogBox(String s)
		{
			dialog = new JDialog();
			dialog.setSize(400, 300);
			dialog.setLocation(500, 500);
			dialog.setModal(true);
			dialog.setTitle("Text To Add");
			dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
			
			upperPanel = new JPanel();
			textField = new JTextArea(12, 30);
			textField.setText(s);
			scrollPane = new JScrollPane(textField);
			
			upperPanel.add(scrollPane);
			
			lowerPanel = new JPanel();
			lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.X_AXIS));
			fontButton = new JButton("Font");
			fontButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fontGUI = new FontDialog(textField);
					fontGUI.setVisible(true);
					if(fontGUI.isVisible()==false) { 
						textField.setFont(fontGUI.getFont());
						if ((fontGUI.getColor()==Color.WHITE)&&(textField.getBackground().equals(new Color(255,255,255)))){
							textField.setForeground(Color.BLACK);
						} else {
							textField.setForeground(fontGUI.getColor());
						}
					}
				}
			});
			
			okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					text = textField.getText();
					dialog.setVisible(false);
					dialog.dispose();
				}
			});
			lowerPanel.add(fontButton);
			lowerPanel.add(okButton);
			dialog.add(upperPanel);
			dialog.add(lowerPanel);
			dialog.pack();  
			dialog.setVisible(true);
		}
		
		public String getText()
		{
			return text;
		}
		
		public Font getFont()
		{
			return textField.getFont();
		}
		
		public Color getColor()
		{
			if (fontGUI != null && fontGUI.getColor() != null) {
				return fontGUI.getColor();
			} else {
				return Color.WHITE;
			}
		}
	}


}
