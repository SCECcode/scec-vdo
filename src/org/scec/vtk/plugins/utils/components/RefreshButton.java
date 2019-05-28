package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;


import org.scec.vtk.tools.Prefs;

/**
 * <code>RefreshButton</code> is an icon-based <code>JButton</code>  used to refresh
 * information (catalogs, notes, references etc.) about <i>ScecVideo</i> objects.
 *<br>
 * Created on July 12, 2010
 * @author Mike Sheehan
 * @Referenced EditButton.java
 */
public class RefreshButton extends JButton {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating a "refresh" action.
     * Button is disabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public RefreshButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/refreshIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/refreshIconDis.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.setEnabled(false);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }

}
