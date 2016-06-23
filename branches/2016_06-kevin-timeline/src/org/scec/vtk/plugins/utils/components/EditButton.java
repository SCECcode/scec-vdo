package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.scec.vtk.tools.Prefs;


public class EditButton extends JButton {

	    private static final long serialVersionUID = 1L;

		/**
	     * Constructs a new <code>JButton</code> with an icon indicating an "edit" action.
	     * Button is disabled by default.
	     * 
	     * @param listener button's event listener
	     * @param tip tool tip to set for button
	     */
	    public EditButton(ActionListener listener, String tip) {
	        super();
	        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/editIcon.png"));
	        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/editIconDis.png"));
	        this.setMargin(Prefs.getIconInset());
	        this.setDisabledIcon(iconDis);
	        this.setIcon(icon);
	        this.setEnabled(false);
	        this.addActionListener(listener);
	        this.setToolTipText(tip);
	    }

	}
