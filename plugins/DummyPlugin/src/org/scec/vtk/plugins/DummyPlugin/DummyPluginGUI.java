package org.scec.vtk.plugins.DummyPlugin;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

/* It's usually cleaner to have a separate class to handle the GUI,
 * but you have to have a way for the GUI to make things happen in the non-GUI class.
 */

public class DummyPluginGUI extends JPanel implements ItemListener {

	private JPanel mainPanel;
	private JCheckBox checkbox;
	protected DummyPlugin db;
	protected boolean sphereLoaded;
	
	public DummyPluginGUI(DummyPlugin d) {
		this.db = d;
		mainPanel = new JPanel();
		checkbox = new JCheckBox("Sphere");
		checkbox.addItemListener(this);
		mainPanel.add(checkbox);
		sphereLoaded = false;
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
				sphereLoaded = true;
			} else {
				//Turn it off
				this.db.unloadSphere();
				sphereLoaded = false;
			}
		}
	}
	
	public void setCheckBox(boolean checked)
	{
		checkbox.setSelected(checked);
	}
}
