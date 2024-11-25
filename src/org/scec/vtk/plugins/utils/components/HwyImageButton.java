
package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;


import org.scec.vtk.tools.Prefs;

/**
 * <code>AddButton</code> is an icon-based JButton typically used for adding
 * <i>ScecVideo</i> objects to a list or table.
 *
 * Created on Feb 22, 2005
 * 
 * @author P. Powers
 * @version $Id: HwyImageButton.java 385 2006-07-13 22:37:08Z chumrick $
 */
public class HwyImageButton extends JButton {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating an "add" action.
     * Button is enabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public HwyImageButton(ActionListener listener, String tip, String iconName) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/hwyImage" + iconName + "Icon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/hwyImage" + iconName + "Icon.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }
    
    public HwyImageButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/hwyImageIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/hwyImageIcon.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }
}
