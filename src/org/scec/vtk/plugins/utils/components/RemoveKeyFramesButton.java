package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.scec.vtk.tools.Prefs;

public class RemoveKeyFramesButton extends JButton {

	/**
	 * Constructs a new <code>JButton</code> that removes all keyframes from plugin
	 * 
	 * @param listener button's event listener
     * @param tip tool tip to set for button
	 */
	private static final long serialVersionUID = 1L;

	public RemoveKeyFramesButton(ActionListener listener, String tip)
	{
		super();
		ImageIcon icon = new ImageIcon(this.getClass().getResource("resources/img/trashIcon.png"));
		this.setMargin(Prefs.getIconInset());
		this.setIcon(icon);
		this.setEnabled(true);
		this.addActionListener(listener);
		this.setToolTipText(tip);
	}
}