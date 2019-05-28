
package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;


import org.scec.vtk.tools.Prefs;

/**
 * <code>PlayButton</code> is an icon-based <code>JButton</code> typically used to start
 * playing an animation.
 *
 * Created on June 21, 2005
 * 
 * @author I. Haqque
 * @version $Id: PlayButton.java 147 2005-06-27 18:09:09Z haqque $
 */
public class RenderButton extends JButton {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating a "play" action.
     * Button is disabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public RenderButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/renderIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/renderIconDis.png"));
        
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.setEnabled(false);
        this.addActionListener(listener);
        this.setToolTipText(tip);
        	
    }

}
