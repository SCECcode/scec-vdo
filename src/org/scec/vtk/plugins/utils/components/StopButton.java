
package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;


import org.scec.vtk.tools.Prefs;

/**
 * <code>StopButton</code> is an icon-based <code>JButton</code> used to stop an animation
 * sequence
 *
 * Created on June 21, 2005
 * 
 * @author I. Haqque
 * @version $Id: StopButton.java 147 2005-06-27 18:09:09Z haqque $
 */
public class StopButton extends JButton {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating a "stop" action.
     * Button is disabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public StopButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/stopIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/stopIconDis.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.setEnabled(false);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }

}
