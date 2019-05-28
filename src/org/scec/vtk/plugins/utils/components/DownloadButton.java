
package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.scec.vtk.tools.Prefs;


/**
 * <code>DownloadButton</code> is an icon-based JButton typically used for unloading
 * (from memory) <i>ScecVideo</i> objects displayed in a list or table.
 *
 *
 * Created on Mar 21, 2005
 * 
 * @author P. Powers
 * @version $Id: DownloadButton.java 70 2005-05-19 08:36:21Z pmpowers $
 */
public class DownloadButton extends JButton {
    
    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating an "download" action.
     * Button is disabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public DownloadButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/dnLoadIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/dnLoadIconDis.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.setEnabled(false);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }

}
