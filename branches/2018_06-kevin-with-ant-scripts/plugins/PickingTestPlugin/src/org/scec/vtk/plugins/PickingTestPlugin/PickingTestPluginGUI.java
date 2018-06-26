package org.scec.vtk.plugins.PickingTestPlugin;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/* It's usually cleaner to have a separate class to handle the GUI,
 * but you have to have a way for the GUI to make things happen in the non-GUI class.
 */

public class PickingTestPluginGUI implements ItemListener {

	private JPanel mainPanel;
	private JCheckBox displayCheck;
	private JCheckBox bundleCheck;
	private PickingTestPlugin plugin;
	
	public PickingTestPluginGUI(PickingTestPlugin plugin) {
		this.plugin = plugin;
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		displayCheck = new JCheckBox("Display");
		displayCheck.addItemListener(this);
		bundleCheck = new JCheckBox("Bundle Actors");
		bundleCheck.addItemListener(this);
		mainPanel.add(displayCheck);
		mainPanel.add(bundleCheck);
	}

	public JPanel getPanel() {
		return mainPanel;
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == displayCheck) {
			if (displayCheck.isSelected()) {
				if (bundleCheck.isSelected())
					plugin.displayBundled();
				else
					plugin.display();
			} else {
				plugin.clear();
			}
		}
		if (e.getSource() == bundleCheck) {
			if (displayCheck.isSelected()) {
				plugin.clear();
				if (bundleCheck.isSelected())
					plugin.displayBundled();
				else
					plugin.display();
			}
		}
		
	}
	
	
}
