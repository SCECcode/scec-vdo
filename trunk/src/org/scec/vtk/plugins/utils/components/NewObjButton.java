package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;


import org.scec.vtk.tools.Prefs;

/**
 * <code>NewCatButton</code> is an icon-based <code>JButton</code> typically used to prompt
 * user to create a new catalog by querying an existing one.
 *
 * Created on Feb 22, 2005
 * 
 * @author P. Powers
 * @version $Id: NewObjButton.java 397 2006-07-17 18:42:15Z pack $
 */
public class NewObjButton extends JButton {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating an "new catalog" action.
     * Button is disabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public NewObjButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/addFromCatIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/addFromCatIconDis.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.setEnabled(false);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }

}
