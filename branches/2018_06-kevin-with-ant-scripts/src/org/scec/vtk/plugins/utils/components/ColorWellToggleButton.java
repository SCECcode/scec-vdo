package org.scec.vtk.plugins.utils.components;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.JToggleButton;


/**
 * This class represents toggle-style buttons with <code>ColorWell</code> icons. 
 *
 * Created on Feb 12, 2005
 * 
 * @author P. Powers
 * @version $Id: ColorWellToggleButton.java 20 2005-05-04 19:44:40Z pmpowers $
 */
public class ColorWellToggleButton extends JToggleButton {

    private static final long serialVersionUID = 1L;
	private static final int MARGIN = 4;
    private ColorWellIcon buttonIcon;
    
    /**
     * Constructs a new button of a specified width and height and with
     * a <code>ColorWell</code> icon initialized to a specified color.
     * 
     * @param c initial color
     * @param width icon width
     * @param height icon height
     */
    public ColorWellToggleButton(Color c, int width, int height) {
        super();
        this.buttonIcon = makeIcon(c,c,width,height);
        setIcon(this.buttonIcon);
        setMargin(new Insets(MARGIN,MARGIN,MARGIN,MARGIN));
    }
    
    /**
     * Constructs a new button of a specified width and height and with
     * a <code>ColorWell</code> icon initialized to a specified color.
     * 
     * @param c1 initial color for left side of gradient
     * @param c2 initial color for right side of gradient
     * @param width icon width
     * @param height icon height
     */
    public ColorWellToggleButton(Color c1, Color c2, int width, int height) {
        super();
        this.buttonIcon = makeIcon(c1,c2,width,height);
        setIcon(this.buttonIcon);
        setMargin(new Insets(MARGIN,MARGIN,MARGIN,MARGIN));
    }
        
    /**
     * Set this button's icon to a single color.
     * 
     * @param c color to set
     */
    public void setColor(Color c) {
        setColor(c,c);
    }
    
    /**
     * Set this button's icon to a gradient.
     * 
     * @param c1 color for left side of gradient
     * @param c2 color for right side of gradient
     */
    public void setColor(Color c1, Color c2) {
        this.buttonIcon.setColor(c1,c2);
        repaint();
    }

    private ColorWellIcon makeIcon(Color c1, Color c2, int width, int height) {
        ColorWellIcon icon = new ColorWellIcon(c1 ,c2, width-(2*MARGIN), height-(2*MARGIN), 0);
        return icon;
    }
    
}
