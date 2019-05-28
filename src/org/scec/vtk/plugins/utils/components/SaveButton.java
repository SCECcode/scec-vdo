package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;


import org.scec.vtk.tools.Prefs;


/**
 * <code>SaveButton</code> is an icon-based <code>JButton</code> typically used for saving
 * <i>ScecVideo</i> object properties to file.
 *
 * Created on Feb 22, 2005
 * 
 * @author P. Powers
 * @version $Id: SaveButton.java 70 2005-05-19 08:36:21Z pmpowers $
 */
public class SaveButton extends JButton {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating a "save" action.
     * Button is disabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public SaveButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/saveIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/saveIconDis.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.setEnabled(false);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }
}
