package org.scec.vtk.commons.opensha.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opensha.commons.param.editor.impl.GriddedParameterListEditor;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.commons.opensha.surfaces.LineSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.PointSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.events.GeometryGeneratorChangeListener;
import org.scec.vtk.commons.opensha.surfaces.events.GeometrySettingsChangeListener;
import org.scec.vtk.commons.opensha.surfaces.events.GeometrySettingsChangedEvent;

import com.google.common.base.Preconditions;

public class GeometryTypeSelectorPanel extends JPanel implements ItemListener, ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<GeometryGeneratorChangeListener> listeners = new ArrayList<GeometryGeneratorChangeListener>();
	
	private CardLayout cl = new CardLayout();
	private JPanel cards = new JPanel(cl);
	
	private JComboBox<GeometryGenerator> selector;
	private JCheckBox bundleCheck;
	
	private ArrayList<GeometryGenerator> geomGens;
	
	public GeometryTypeSelectorPanel(ArrayList<GeometryGenerator> geomGens) {
		super(new BorderLayout());
		this.geomGens = geomGens;
		
		selector = new JComboBox<>(geomGens.toArray(new GeometryGenerator[0]));
		
		selector.addItemListener(this);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		
		JLabel label = new JLabel("Geometry Type: ");
		label.setFont(new Font(Font.SERIF, Font.BOLD, 14));
		
		topPanel.add(label);
		topPanel.add(selector);
		
		GeometryGenerator selected = getSelectedGeomGen();
		bundleCheck = new JCheckBox("Bundle?", selected.isBundlerEnabled());
		bundleCheck.setEnabled(selected.getFaultActorBundler() != null);
		bundleCheck.setToolTipText("Bundles multiple faults into one VTK actor for better performance. "
				+ "Disabling can fix depth sorting issues with transparency enabled.");
		bundleCheck.addActionListener(this);
		topPanel.add(bundleCheck);
		
		this.add(topPanel, BorderLayout.NORTH);
		
		for (GeometryGenerator geomGen : geomGens) {
			String name = geomGen.toString();
			cards.add(new GriddedParameterListEditor(geomGen.getDisplayParams()), name);
		}
		
		cl.show(cards, selector.getSelectedItem().toString());
		
		this.add(cards, BorderLayout.CENTER);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == selector) {
			GeometryGenerator selected = getSelectedGeomGen();
			cl.show(cards, selected.toString());
			bundleCheck.setSelected(selected.isBundlerEnabled());
			bundleCheck.setEnabled(selected.getFaultActorBundler() != null);
			firePlotSettingsChangeEvent();
		}
	}
	
	public GeometryGenerator getSelectedGeomGen() {
		return (GeometryGenerator)selector.getSelectedItem();
	}
	
	public void setSelectedGeomGen(GeometryGenerator geomGen) {
		Preconditions.checkState(geomGens.contains(geomGen));
		selector.setSelectedItem(geomGen);
	}
	
	public ArrayList<GeometryGenerator> getAllGeomGens() {
		return geomGens;
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);
		
		ArrayList<GeometryGenerator> geomGens = new ArrayList<GeometryGenerator>();
//		geomGens.add(new PointSurfaceGenerator());
		geomGens.add(new LineSurfaceGenerator());
		
		frame.setContentPane(new GeometryTypeSelectorPanel(geomGens));
		frame.setVisible(true);
	}
	
	public void addGeometryGeneratorChangeListener(GeometryGeneratorChangeListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public boolean removeGeometryGeneratorChangeListener(GeometryGeneratorChangeListener listener) {
		return listeners.remove(listener);
	}
	
	protected void firePlotSettingsChangeEvent() {
		for (GeometryGeneratorChangeListener l : listeners) {
			l.geometryGeneratorChanged(getSelectedGeomGen());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bundleCheck) {
			getSelectedGeomGen().setBundlerEneabled(bundleCheck.isSelected());
		}
	}

}
