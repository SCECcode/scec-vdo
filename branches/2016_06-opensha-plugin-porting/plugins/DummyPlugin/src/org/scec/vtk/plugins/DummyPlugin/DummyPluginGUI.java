package org.scec.vtk.plugins.DummyPlugin;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

/* It's usually cleaner to have a separate class to handle the GUI,
 * but you have to have a way for the GUI to make things happen in the non-GUI class.
 */

public class DummyPluginGUI implements ItemListener {

	private JPanel mainPanel;
	private JCheckBox checkbox;
	private DummyPlugin db;
	
	public DummyPluginGUI(DummyPlugin d) {
		this.db = d;
		mainPanel = new JPanel();
		checkbox = new JCheckBox("Sphere");
		checkbox.addItemListener(this);
		mainPanel.add(checkbox);
	}

	public JPanel getPanel() {
		return mainPanel;
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource()==checkbox) {
			if (e.getStateChange()==ItemEvent.SELECTED) {
				//Turn on the sphere
				this.db.loadSphere();
			} else {
				//Turn it off
				this.db.unloadSphere();
			}
		}
		
	}
	
	
}
