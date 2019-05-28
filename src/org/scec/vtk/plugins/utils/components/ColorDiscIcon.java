package org.scec.vtk.plugins.utils.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;


import javax.swing.Icon;

/**
 * This class handles drawing of icons that represent selected colors for focal mechanism discs.
 * Icons are drawn using an arc that outlines a rectangle.  Two arcs creating semicircles represent
 * the two colors representing compression and extension of the focal disc. 
 * 
 * Created on Jan 30, 2005
 *
 * @author J. Garcia, code based on P. Powers ColorWellIcon
 * @see org.scec.geo3d.plugins.utils.components.ColorWellIcon
 * 
 */
public class ColorDiscIcon implements Icon {
    
    private static Color disabledColor = new Color(1.0f,1.0f,1.0f,0.5f);
    
    private int width;
    private int height;
    private int colorWidth;
    private int colorHeight;
    private int compStartAngle = 90; // angle to start from to draw arc for the compression color
    private int extStartAngle = 270; // angle to start from to draw arc for the extension color
    private int relativeEndAngle = 180; // how much to add to the start angle to finish the arc
    
    private Color compColor = Color.RED;
    private Color extColor = Color.YELLOW;
    private int inset = 0;

    /**
     * Constructs a new focal mechanism color disc <code>Icon</code> of a given width and height, with
     * given colors, and with a given border inset. The inset specifies the amount of space
     * (in pixels) between the border and the colored part of the icon. Use an inset value
     * of 0 or less to specify no border.
     * 
     * @param inCompColor compression color
     * @param inExtColor extension color
     * @param iconWidth icon width
     * @param iconHeight icon height
     * @param iconInset pixels between border and colored part of icon
     */
    public ColorDiscIcon(Color inCompColor, Color inExtColor, int iconWidth, int iconHeight, int iconInset) {
        setInset(iconInset);
        setColor(inCompColor, inExtColor);
        setDimensions(iconWidth,iconHeight);
    }
    
    /**
     * Sets the colors for this gradient icon.
     * 
     * @param inCompColor color to set on left side of gradient
     * @param inExtColor color to set on right side of gradient
     */
    public void setColor(Color inCompColor, Color inExtColor) {
        this.compColor = inCompColor;
        this.extColor = inExtColor;
    }
    
    /**
     * Returns a 2 item array of Colors shown in this button.
     * 
     * @return a two-color array
     */
    public Color[] getColor() {
        return new Color[] {this.compColor, this.extColor};
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
            g.setColor(Color.BLACK);
            g.drawArc(x, y, this.width, this.height, compStartAngle, relativeEndAngle);
            g.drawArc(x, y, this.width, this.height, extStartAngle, relativeEndAngle);
        }
        
        // draw main innear focal disc area
    	g.setColor(this.compColor);
        g.drawArc(X, Y, this.colorWidth, this.colorHeight,compStartAngle,relativeEndAngle);
        g.fillArc(X+1, Y+1, this.colorWidth-1, this.colorHeight-1,compStartAngle,relativeEndAngle);
        
        g.setColor(this.extColor);
        g.drawArc(X, Y, this.colorWidth, this.colorHeight,extStartAngle,relativeEndAngle);
        g.fillArc(X+1, Y+1, this.colorWidth-1, this.colorHeight-1,extStartAngle,relativeEndAngle);
        
        // lastly, if owner is disabled, draw a transparent rectangle to give 
        // a disabled appearance to color well
        if (!c.isEnabled()) {
            g.setColor(disabledColor);
            g.fillArc(x, y, this.width+1, this.height+1,compStartAngle,relativeEndAngle);
            g.fillArc(x, y, this.width+1, this.height+1,extStartAngle,relativeEndAngle);
        }
    }
}
