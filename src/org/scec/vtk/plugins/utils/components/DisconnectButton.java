package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.scec.vtk.tools.Prefs;


/**
 * <code>DisconnectButton</code> is an icon-based <code>JButton</code>  used to disconnect
 * from the Internet when a connection is open.
 *<br>
 * Created on July 13, 2010
 * @author Mike Sheehan
 * @Referenced RefreshButton.java
 */
public class DisconnectButton extends JButton {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating a "disconnect" action.
     * Button is disabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public DisconnectButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/disconnectIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/disconnectIconDis.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.setEnabled(false);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }

}
