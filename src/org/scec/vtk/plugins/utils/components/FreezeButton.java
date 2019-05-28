package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.scec.vtk.tools.Prefs;

public class FreezeButton extends JButton {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating a "show" action.
     * Button is disabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public FreezeButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/freezeIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/freezeIconDis.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.setEnabled(false);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }
}