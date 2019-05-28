package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;


import org.scec.vtk.tools.Prefs;;


/**
 * <code>ShowButton</code> is an icon-based <code>JButton</code> used for showing or hiding
 * <i>ScecVideo</i> objects.
 *
 * Created on Feb 22, 2005
 * 
 * @author P. Powers
 * @version $Id: ShowButton.java 70 2005-05-19 08:36:21Z pmpowers $
 */
public class HazardButton extends JButton {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating a "show" action.
     * Button is disabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public HazardButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/hazardIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/hazardIcon.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.setEnabled(false);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }
}