package org.scec.vtk.timeline.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.tools.Prefs;

public class RenderSizePanel extends JPanel implements ActionListener, ItemListener, DocumentListener {
	
	private enum SizePreset {
		HD("HD (720p)", 1280, 720),
		FHD("Full HD (1080p)", 1920, 1080),
		FOUR_K("4K UHD", 3840, 2160),
		DEFAULT_VIEW("Default", Prefs.getMainWidth(), Prefs.getMainHeight());
		
		private String label;
		private int width;
		private int height;

		private SizePreset(String label, int width, int height) {
			this.label = label;
			this.width = width;
			this.height = height;
		}
		
		public boolean equals(int width, int height) {
			return width == this.width && height == this.height;
		}
		
		public static SizePreset forSize(int width, int height) {
			for (SizePreset preset : values())
				if (preset.equals(width, height))
					return preset;
			return null;
		}
		
		public static SizePreset forLabel(String label) {
			for (SizePreset preset : values())
				if (preset.label.equals(label))
					return preset;
			return null;
		}
	}
	
	private Timeline timeline;
	
	private JComboBox<String> presetBox;
	private JTextField widthField, heightField;
	private JButton resizeButton;
	
	private static final String CUSTOM_LABEL = "(custom)";
	
	public RenderSizePanel(Timeline timeline) {
		this.timeline = timeline;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		resizeButton = new JButton("Resize Now");
		addRow(new JLabel("Render Resolution    "), resizeButton);
		resizeButton.addActionListener(this);
		List<String> presets = new ArrayList<>();
		for (SizePreset preset : SizePreset.values())
			presets.add(preset.label);
		presets.add(CUSTOM_LABEL);
		presetBox = new JComboBox<String>(presets.toArray(new String[0]));
		presetBox.addItemListener(this);
		addRow(new JLabel("Preset:  "), presetBox);
		widthField = new JTextField(10);
		widthField.getDocument().addDocumentListener(this);
		addRow(new JLabel("Width:  "), widthField);
		heightField = new JTextField(10);
		heightField.getDocument().addDocumentListener(this);
		addRow(new JLabel("Height:  "), heightField);
		
		updateFromWindow();
	}
	
	private void addRow(Component... components) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		for (Component component : components)
			panel.add(component);
		add(panel);
	}
	
	public void updateFromWindow() {
		int width, height;
		if (timeline.isLive()) {
			width = MainGUI.getRenderWindow().getComponent().getWidth();
			height = MainGUI.getRenderWindow().getComponent().getHeight();
		} else {
			width = SizePreset.DEFAULT_VIEW.width;
			height = SizePreset.DEFAULT_VIEW.height;
		}
		
		SizePreset preset = SizePreset.forSize(width, height);
		if (preset == null) {
			presetBox.setSelectedItem(CUSTOM_LABEL);
			updateInternal(width, height);
		} else {
			presetBox.setSelectedItem(preset.label);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int curWidth = Integer.parseInt(widthField.getText());
		int curHeight = Integer.parseInt(heightField.getText());
		Info.getMainGUI().resizeViewer(curWidth, curHeight);
	}
	
	private boolean internalUpdate = false;
	
	private synchronized void updateInternal(int width, int height) {
		internalUpdate = true;
		updateIfNecessary(widthField, width);
		updateIfNecessary(heightField, height);
		internalUpdate = false;
	}

	@Override
	public synchronized void itemStateChanged(ItemEvent e) {
		SizePreset preset = SizePreset.forLabel(presetBox.getItemAt(presetBox.getSelectedIndex()));
		if (preset != null) {
			updateInternal(preset.width, preset.height);
		}
	}
	
	private void updateIfNecessary(JTextField field, int val) {
		String strVal = val+"";
		if (!field.getText().equals(strVal))
			field.setText(strVal);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		textUpdated();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		textUpdated();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		textUpdated();
	}
	
	private void textUpdated() {
		if (internalUpdate)
			return;
		int curWidth = Integer.parseInt(widthField.getText());
		int curHeight = Integer.parseInt(heightField.getText());
		SizePreset preset = SizePreset.forSize(curWidth, curHeight);
		if (preset == null)
			presetBox.setSelectedItem(CUSTOM_LABEL);
		else
			presetBox.setSelectedItem(preset.label);
	}

}
