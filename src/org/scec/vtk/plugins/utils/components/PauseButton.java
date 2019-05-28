
package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;


import org.scec.vtk.tools.Prefs;

/**
 * <code>PauseButton</code> is an icon-based <code>JButton</code> typically used to pause
 * an animation sequence
 *
 * Created on June 21, 2005
 * 
 * @author I. Haqque
 * @version $Id: PauseButton.java 147 2005-06-27 18:09:09Z haqque $
 */
public class PauseButton extends JButton {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating a "pause" action.
     * Button is disabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public PauseButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/pauseIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/pauseIconDis.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.setEnabled(false);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }

}
