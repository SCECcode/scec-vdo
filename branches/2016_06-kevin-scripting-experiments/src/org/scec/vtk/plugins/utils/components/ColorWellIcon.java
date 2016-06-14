package org.scec.vtk.plugins.utils.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * This class handles drawing of icons that represent selected colors and gradients.
 * Icons can be drawn with or without a border; width and height values represent the
 * outer dimensions of the <code>Icon</code> when drawn, e.g. if a button has a border, the actual
 * colored part of the icon will be smaller than if specified without a border.
 * 
 * Created on Jan 30, 2005
 *
 * @author P. Powers
 * @version $Id: ColorWellIcon.java 20 2005-05-04 19:44:40Z pmpowers $
 */
public class ColorWellIcon implements Icon {
    
    private static Color disabledColor = new Color(1.0f,1.0f,1.0f,0.5f);
    
    private int width;
    private int height;
    private int colorWidth;
    private int colorHeight;
    
    private Color color1 = Color.BLACK;
    private Color color2 = Color.WHITE;
    private int inset = 0;

    
    /**
     * Constructs a new color well <code>Icon</code> of a given width and height, with a 
     * given color, and with a given border inset. The inset specifies the amount of space
     * (in pixels) between the border and the colored part of the icon. Use an inset value
     * of 0 or less to specify no border.
     * 
     * @param iconColor icon color
     * @param iconWidth icon width
     * @param iconHeight icon height
     * @param iconInset pixels between border and colored part of icon
     */
    public ColorWellIcon(Color iconColor, int iconWidth, int iconHeight, int iconInset) {
        setInset(iconInset);
        setDimensions(iconWidth, iconHeight);
        setColor(iconColor,iconColor);
    }
    
    /**
     * Constructs a new gradient color well <code>Icon</code> of a given width and height, with
     * given colors, and with a given border inset. The inset specifies the amount of space
     * (in pixels) between the border and the colored part of the icon. Use an inset value
     * of 0 or less to specify no border.
     * 
     * @param iconColor1 color for left side of gradient
     * @param iconColor2 color for right side of gradient
     * @param iconWidth icon width
     * @param iconHeight icon height
     * @param iconInset pixels between border and colored part of icon
     */
    public ColorWellIcon(Color iconColor1, Color iconColor2, int iconWidth, int iconHeight, int iconInset) {
        setInset(iconInset);
        setColor(iconColor1, iconColor2);
        setDimensions(iconWidth,iconHeight);
    }
    
    /**
     * Sets the color for this icon.
     * 
     * @param c color to set
     */
    public void setColor(Color c) {
        setColor(c,c);
    }

    /**
     * Sets the colors for this gradient icon.
     * 
     * @param c1 color to set on left side of gradient
     * @param c2 color to set on right side of gradient
     */
    public void setColor(Color c1, Color c2) {
        this.color1 = c1;
        this.color2 = c2;
    }
    
    /**
     * Returns a 2 item array of Colors shown in this button.
     * 
     * @return a two-color array
     */
    public Color[] getColor() {
        return new Color[] {this.color1, this.color2};
    }
    
    /**
     * Sets the width and height values of an icon's border and color display area.
     * 
     * @param iconWidth outer width of icon
     * @param iconHeight outer height of icon
     */
    public void setDimensions(int iconWidth, int iconHeight) {
        this.width = iconWidth;
        this.height = iconHeight;
        if (this.inset > 0) {
            this.colorWidth = iconWidth - (2 * this.inset);
            this.colorHeight = iconHeight - (2 * this.inset);
        } else {
            this.colorWidth = iconWidth;
            this.colorHeight = iconHeight;
        }
    }
    
    /**
     * Sets the space (in pixels) between this icon's border and the colored part of the icon.
     * 
     * @param iconInset amount of space in pixels (int = 1 or more)
     */
    public void setInset(int iconInset) {
        this.inset = iconInset;
    }
    
    /**
     * Returns the icon's width.
     * 
     * @see javax.swing.Icon#getIconWidth()
     */
    public int getIconWidth() {
        return (this.inset > 0) ? this.width : this.width+1;
    }

    /**
     * Returns the icon's height.
     * 
     * @see javax.swing.Icon#getIconHeight()
     */
    public int getIconHeight() {
        return (this.inset > 0) ? this.height : this.height+1;
    }
    
    /**
     *  Draw the icon at the specified location.
     * 
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {

        int X = x + this.inset;
        int Y = y + this.inset;
        
        // draw border
        if (this.inset > 0) {
            g.setColor(Color.GRAY);
            g.drawRect(x, y, this.width, this.height);
        }
        /*if(this.color1==null)
        	this.color1=Color.black;
        if(this.color2==null)
        	this.color2=Color.black;*/
        // draw color box/rect
        if (this.color1.equals(this.color2)) {
            // draw solid color icon
            g.setColor(this.color1.darker());
            g.drawRect(X, Y, this.colorWidth, this.colorHeight);
            g.setColor(this.color1);
            g.fillRect(X+1, Y+1, this.colorWidth-1, this.colorHeight-1);
            
        } else {
            // draw gradient icon
            g.setColor(Color.DARK_GRAY);
            g.drawRect(X, Y, this.colorWidth, this.colorHeight);
            GradientPaint gp = new GradientPaint(
                    X, Y,
                    this.color1,
                    this.colorWidth, Y,
                    this.color2);
            ((Graphics2D)g).setPaint(gp);
            g.fillRect(X+1, Y+1, this.colorWidth-1, this.colorHeight-1);
        }
        
        // lastly, if owner is disabled, draw a transparent rectangle to give 
        // a disabled appearance to color well
        if (!c.isEnabled()) {
            g.setColor(disabledColor);
            g.fillRect(x, y, this.width+1, this.height+1);   
        }
    }
}
