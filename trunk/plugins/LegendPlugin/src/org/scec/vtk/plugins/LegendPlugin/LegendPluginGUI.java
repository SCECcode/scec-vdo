package org.scec.vtk.plugins.LegendPlugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;
import org.scec.vtk.plugins.LegendPlugin.Component.LegendModel;
import org.scec.vtk.plugins.utils.components.ColorButton;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;

import vtk.vtkActor2D;
import vtk.vtkColorTransferFunction;
import vtk.vtkImageMapper;
import vtk.vtkJPEGReader;
import vtk.vtkLookupTable;
import vtk.vtkScalarBarActor;
import vtk.vtkTextActor;

public class LegendPluginGUI extends JPanel implements ActionListener, ChangeListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;
	private JButton displayButton, moveLeftButton, moveRightButton, moveUpButton, moveDownButton;
	private JButton imageButton, textButton, removeButton, createButton;
	private ColorButton colorButton;
	private SingleColorChooser colorChooser = new SingleColorChooser(this);

	private JSlider transparencySlider;
	private JTextField scaleField;
	private JList<String> legendSelectList;
	private DefaultListModel<String> model;
	private LegendModel legendModel;

	final float SCALE = 0.5f;

	public CreateLegendsGUI createLeg;
	
	private PluginActors legendActors = new PluginActors();
	private ArrayList<vtkActor2D> legends = new ArrayList<vtkActor2D>();
	private int legendCounter = 0;

	public LegendPluginGUI(PluginActors actors)
	{
		super();
		this.legendActors = actors;
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		legendModel = new LegendModel(this);   
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

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

		model = new DefaultListModel();
		legendSelectList = new JList(model);
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
		colorButton = new ColorButton(this, "Colors");
		colorButton.setEnabled(false);
		removeButton = new JButton("Remove");
		removeButton.addActionListener(this);
		removeButton.setEnabled(false);
		JPanel lowerButtonPanel = new JPanel();
		lowerButtonPanel.setLayout(new BoxLayout(lowerButtonPanel,BoxLayout.X_AXIS));
		lowerButtonPanel.setBorder(new EmptyBorder(5,0,0,0));
		lowerButtonPanel.add(imageButton);
		lowerButtonPanel.add(createButton);
		lowerButtonPanel.add(textButton);
		lowerButtonPanel.add(colorButton);
		lowerButtonPanel.add(removeButton);
		listPanel.add(lowerButtonPanel);

		add(listPanel);
	}

	public void valueChanged(ListSelectionEvent e)
	{
		Object source = e.getSource();
		
		if (source == legendSelectList)
		{
			int index = legendSelectList.getSelectedIndex();
			if (index != -1)
			{
				int visibility = legends.get(index).GetVisibility();
				if (visibility == 1)
				{
					displayButton.setText("Hide");
					colorButton.setEnabled(true);
					setMoveButtonsEnabled(true);
					displayButton.setEnabled(true);
					removeButton.setEnabled(true);
				}
				else
				{
					displayButton.setText("Display");
					colorButton.setEnabled(false);
					setMoveButtonsEnabled(false);
					removeButton.setEnabled(false);
				}
			}
		}
	}

	public JList getLegendSelectList() {
		return legendSelectList;
	}

	public void setLegendSelectList(JList legendSelectList) {
		this.legendSelectList = legendSelectList;
	}

	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();

		if (source == imageButton)
		{
			vtkJPEGReader jpeg = null;
			try {
				jpeg = legendModel.addImageLegend();
			} catch (NullPointerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if (jpeg != null)
			{
				vtkImageMapper  mapper = new vtkImageMapper();
	            mapper.SetInputData(jpeg.GetOutput());
	            mapper.SetColorWindow(256.0);
	            mapper.SetColorLevel(128.0);
	            vtkActor2D  actor2d = new vtkActor2D();
	            actor2d.SetMapper(mapper);
	            actor2d.SetPosition(400, 600);
				
				legendActors.addActor(actor2d);
				legends.add(actor2d);
				legendCounter++;
				displayButton.setText("Hide");
				setMoveButtonsEnabled(true);
				displayButton.setEnabled(true);
				removeButton.setEnabled(true);
				model.addElement("Legend #" + legendCounter);
				legendSelectList.setSelectedIndex(model.getSize()-1);
				Info.getMainGUI().updateRenderWindow();
			}	
		}
		else if (source == textButton)
		{
			String text = (String)JOptionPane.showInputDialog(Info.getMainGUI().getContentPane(), "Text Label: ", "Text To Add", JOptionPane.PLAIN_MESSAGE);
			addText(text);
		}
		else if (source == removeButton)
		{
			removeLegendActor();
		}
		else if (source == displayButton)
		{	
			int index = legendSelectList.getSelectedIndex();
			if (index != -1)
			{
				int visibility = legends.get(index).GetVisibility();
				if (visibility == 1)
				{
					legends.get(index).SetVisibility(0);
					displayButton.setText("Display");
				}
				else
				{
					legends.get(index).SetVisibility(1);
					displayButton.setText("Hide");
				}
			}
			
			Info.getMainGUI().updateRenderWindow();
		}
		else if (source == colorButton)
		{
			int index = legendSelectList.getSelectedIndex();
			if (index != -1)
			{
				Color color = colorChooser.getColor();
				if(color != null)
				{
					legends.get(index).GetProperty().SetColor(color.getRed(), color.getGreen(), color.getBlue());
					legends.get(index).Modified();
				}
			}
			
			Info.getMainGUI().updateRenderWindow();
		}
		
		else if(source == moveLeftButton)
		{
			if (model.getElementAt(legendSelectList.getSelectedIndex()).equals("Color Gradiant")){
				moveLegend(-0.05, 0);
			} else {
				moveLegend(-5, 0);
			}
		}
		else if (source == moveRightButton)
		{
			if (model.getElementAt(legendSelectList.getSelectedIndex()).equals("Color Gradiant")){
				moveLegend(0.05, 0);
			} else {
				moveLegend(5, 0);
			}
		}
		else if (source == moveUpButton)
		{
			if (model.getElementAt(legendSelectList.getSelectedIndex()).equals("Color Gradiant")){
				moveLegend(0, 0.05);
			} else {
				moveLegend(0, 5);
			}
		}
		else if (source == moveDownButton)
		{
			if (model.getElementAt(legendSelectList.getSelectedIndex()).equals("Color Gradiant")){
				moveLegend(0, -0.05);
			} else {
				moveLegend(0, -5);
			}
		}
		else if(source == scaleField){
			//Update if the value changes
			updateScale();
		}
		else if(source == createButton){
			createLeg = new CreateLegendsGUI(this,model,legendSelectList,legendModel,scaleField,displayButton,transparencySlider);
			createLeg.setLocation(Info.getMainGUI().getLocation());
			createLeg.setVisible(true);
		}
	}
	
	private void moveLegend(double x, double y)
	{
		int index = legendSelectList.getSelectedIndex();
		legends.get(index).SetPosition(legends.get(index).GetPosition()[0]+x, legends.get(index).GetPosition()[1]+y);
		legends.get(index).Modified();
		Info.getMainGUI().updateRenderWindow();
	}
	
	public void addText(String text)
	{
		vtkTextActor textActor = new vtkTextActor();
		
		if (text != null)
		{
			textActor.SetInput(text);
			textActor.SetPosition(300,300);
			textActor.GetTextProperty().SetFontSize(24);
			textActor.GetTextProperty().SetColor(1.0, 1.0, 1.0);
			
			model.addElement(text);
			legends.add(textActor);
			legendActors.addActor(textActor);
			legendSelectList.setSelectedIndex(model.getSize()-1);
			Info.getMainGUI().updateRenderWindow();
		}
	}
	
	public void addScalarBar(CPT cpt, String title)
	{
		vtkScalarBarActor scalarBar = new vtkScalarBarActor();
		int minValue = (int)cpt.getMinValue();
		int maxValue = (int)cpt.getMaxValue();
		Color minColor = cpt.getMinColor();
		Color maxColor = cpt.getMaxColor();
		int numTicks = cpt.size()+1;
		
		scalarBar.SetTitle(title);
		scalarBar.SetNumberOfLabels(numTicks);
		
		
		vtkLookupTable hue = new vtkLookupTable();
		hue.SetTableRange(minValue, maxValue);
		hue.SetNumberOfColors(256);
		vtkColorTransferFunction ctf = new vtkColorTransferFunction();
		ctf.SetColorSpaceToRGB();
		for (int i=0; i<cpt.size(); i++)
		{
			CPTVal str = cpt.get(i);
			ctf.AddRGBPoint((float)i/(float)cpt.size(), str.minColor.getRed()/255.0, str.minColor.getGreen()/255.0, str.minColor.getBlue()/255.0);
		}
		ctf.AddRGBPoint(1, maxColor.getRed()/255.0, maxColor.getGreen()/255.0, maxColor.getBlue()/255.0);
		
		int tableSize = (int)maxValue;
		hue.Build();
		for (int i=0; i<256; i++)
		{
			double[] color=ctf.GetColor((double)i/256.0);
			hue.SetTableValue(i, color[0], color[1], color[2], 1);
		}
		
		scalarBar.SetLookupTable(hue);
		scalarBar.SetOrientationToHorizontal();
		scalarBar.SetWidth(0.6);
		scalarBar.SetHeight(0.10);
		scalarBar.SetPosition(0.1, 0.1);
		scalarBar.Modified();
		
		legendActors.addActor(scalarBar);
		legends.add(scalarBar);
		model.addElement("Color Gradiant");
		legendSelectList.setSelectedIndex(model.getSize()-1);
		Info.getMainGUI().getRenderWindow().GetRenderer().AddActor2D(scalarBar);
		Info.getMainGUI().updateRenderWindow();
	}
	
	public void addEarthquakeScale(Color colorOne, Color colorTwo, EQCatalog eqc)
	{
		vtkScalarBarActor scalarBar = new vtkScalarBarActor();
		double minValue = (double)eqc.getMinMagnitude();
		double maxValue = (double)eqc.getMaxMagnitude();
		int numTicks = 4;
		String title = eqc.getDisplayName() + " Magnitude Range";
		scalarBar.SetTitle(title);
		scalarBar.SetNumberOfLabels(numTicks);
		
		
		vtkLookupTable hue = new vtkLookupTable();
		hue.SetTableRange(minValue, maxValue);
		hue.SetNumberOfColors(256);
		vtkColorTransferFunction ctf = new vtkColorTransferFunction();
		ctf.SetColorSpaceToRGB();
		for (int i=0; i<2; i++)
		{
			ctf.AddRGBPoint((float)i/(float)2, colorOne.getRed()/255.0, colorOne.getGreen()/255.0, colorOne.getBlue()/255.0);
		}
		ctf.AddRGBPoint(1, colorTwo.getRed()/255.0, colorTwo.getGreen()/255.0, colorTwo.getBlue()/255.0);
		
		hue.Build();
		for (int i=0; i<256; i++)
		{
			double[] color=ctf.GetColor((double)i/256.0);
			hue.SetTableValue(i, color[0], color[1], color[2], 1);
		}
		
		scalarBar.SetLookupTable(hue);
		scalarBar.SetOrientationToHorizontal();
		scalarBar.SetWidth(0.6);
		scalarBar.SetHeight(0.10);
		scalarBar.SetPosition(0.1, 0.1);
		scalarBar.Modified();
		
		legendActors.addActor(scalarBar);
		legends.add(scalarBar);
		model.addElement("Color Gradiant");
		legendSelectList.setSelectedIndex(model.getSize()-1);
		Info.getMainGUI().getRenderWindow().GetRenderer().AddActor2D(scalarBar);
		Info.getMainGUI().updateRenderWindow();
	}
	
	
	public void removeLegendActor()
	{
		int index = legendSelectList.getSelectedIndex();
		if (index != -1)
		{
			legendActors.removeActor(legends.get(index));
			legends.remove(index);
			legendCounter--;
			
			model.remove(legendSelectList.getSelectedIndex());

			if (model.size() >= 1)
			{
				legendSelectList.setSelectedIndex(model.size()-1);
			}
			else
			{
				setMoveButtonsEnabled(false);
				displayButton.setEnabled(false);
				removeButton.setEnabled(false);
			}
			Info.getMainGUI().updateRenderWindow();
		}
	}
	
	private void removeLegendActors()
	{
		for (vtkActor2D actor : legends)
		{
			legendActors.removeActor(actor);
		}
		legends.clear();
		model.clear();
		legendCounter = 0;
		setMoveButtonsEnabled(false);
		displayButton.setEnabled(false);
		removeButton.setEnabled(false);
		Info.getMainGUI().updateRenderWindow();
	}

	public DefaultListModel getModel() {
		return model;
	}

	public void setModel(DefaultListModel model) {
		this.model = model;
	}
	/**
	 * Takes the value in the scale field and apply it to the image
	 */
	private void updateScale(){
		int index = legendSelectList.getSelectedIndex();

		if (index != -1)
		{
			try
			{
				double scaleFactor = new Double(scaleField.getText()).doubleValue();
				System.out.println(scaleFactor);
				if (scaleFactor == 0)
					return; // cannot be 0 or a crash will occur
				
				legends.get(index).SetHeight(legends.get(index).GetHeight()*scaleFactor);
				legends.get(index).SetWidth(legends.get(index).GetWidth()*scaleFactor);
				legends.get(index).Modified();
				legendActors.clearActors();
				legendActors.addActor(legends.get(index));
			}
			catch(NumberFormatException nfe)
			{
				// User put in something that was NaN
			}
			Info.getMainGUI().updateRenderWindow();
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
//		JSlider source = (JSlider)e.getSource();
//
//		if (source == transparencySlider)
//		{
//			
//			for (vtkActor2D actor : legends)
//			{
//				vtkActor2D act = new vtkActor2D
//				
//				actor.GetProperty().SetOpacity(0.5);
//				actor.Modified();
//			}
//		}
//		Info.getMainGUI().updateRenderWindow();
	}

	public void itemStateChanged(ItemEvent e)
	{
	}

	public LegendModel getLegendModel()
	{
		return legendModel;
	}
	
	public void unload()
	{
		this.removeLegendActors();
	}
}
