package org.scec.vtk.plugins.utils.components;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.scec.vtk.tools.Prefs;



public class AddButton extends JButton {
    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>JButton</code> with an icon indicating an "add" action.
     * Button is enabled by default.
     * 
     * @param listener button's event listener
     * @param tip tool tip to set for button
     */
    public AddButton(ActionListener listener, String tip, String iconName) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/add" + iconName + "Icon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/add" + iconName + "IconDis.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }
    
    public AddButton(ActionListener listener, String tip) {
        super();
        ImageIcon icon    = new ImageIcon(this.getClass().getResource("resources/img/addIcon.png"));
        ImageIcon iconDis = new ImageIcon(this.getClass().getResource("resources/img/addIconDis.png"));
        this.setMargin(Prefs.getIconInset());
        this.setDisabledIcon(iconDis);
        this.setIcon(icon);
        this.addActionListener(listener);
        this.setToolTipText(tip);
    }
}
