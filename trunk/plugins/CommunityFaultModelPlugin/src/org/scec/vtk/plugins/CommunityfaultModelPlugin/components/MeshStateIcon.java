package org.scec.vtk.plugins.CommunityfaultModelPlugin.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

import org.scec.vtk.plugins.CommunityfaultModelPlugin.*;

/**
 * This class handles drawing of icons that represent a Java3D object's mesh state.
 * A mesh state is some combination of lines connecting vertices and transparant, partially
 * transparant, or filled polygons.
 * 
 * Created on Jan 30, 2005
 * 
 * @author P. Powers
 * @version $Id: MeshStateIcon.java 2109 2008-07-08 20:35:22Z ihrig $
 */
public class MeshStateIcon implements Icon {

    private final int DEFAULT_ICON_SIZE = 11;
    private int width = this.DEFAULT_ICON_SIZE;
    private int height = this.DEFAULT_ICON_SIZE;
    
    /** Specifies mesh-only fault appearance */
    public static final int MESH_NO_FILL = 0;
    /** Specifies mesh with transparant fill fault appearance */
    public static final int MESH_TRANS_FILL = 1;
    /** Specifies mesh with solid fill fault appearance */
    public static final int MESH_SOLID_FILL = 2;
    /** Specifies transparant fill only fault appearance */
    public static final int NO_MESH_TRANS_FILL = 3;
    /** Specifies solid fill only fault appearance */
    public static final int NO_MESH_SOLID_FILL = 4;
    
    private int mesh;
    
    private static Color border = Color.GRAY;
    private static Color bgLight = new Color(230,230,230);
    private static Color bgMid = new Color(170,170,170);
    private static Color bgDark = new Color(110,110,110);
    private static Color meshLine = new Color(30,30,30);
    private static Color disabledColor = new Color(1.0f,1.0f,1.0f,0.5f);
    
    /**
     * Constructs a new mesh state <code>Icon</code> with a given mesh state.
     * 
     * @param meshState initial icon appearance
     */
    public MeshStateIcon(int meshState) {
        this.mesh = meshState;
    }
    
    /**
     * Constructs a new mesh state <code>Icon</code> of a given size (width=height) and with a 
     * given mesh state.
     *  
     * @param meshState initial icon appearance
     * @param iconSize initial icon size
     */
    public MeshStateIcon(int meshState, int iconSize) {
        setIconDimensions(iconSize, iconSize);
        new MeshStateIcon(meshState);
    }

    /**
     * Constructs a new mesh state <code>Icon</code> of a given width and height and with a 
     * given mesh state.
     * 
     * @param meshState initial icon appearance
     * @param iconWidth initial icon width
     * @param iconHeight initial icon height
     */
    public MeshStateIcon(int meshState, int iconWidth, int iconHeight) {
        setIconDimensions(iconWidth, iconWidth);
        new MeshStateIcon(meshState);
    }

    /**
     * Sets/changes the mesh state of this icon.
     * 
     * @param meshState to set
     */
    public void setMeshState(int meshState) {
        this.mesh = meshState;
    }
    
    /**
     * Sets the width and height of the icon.
     * 
     * @param iconWidth width to set in pixels
     * @param iconHeight height to set in pixels
     */
    public void setIconDimensions(int iconWidth, int iconHeight) {
        this.width = iconWidth;
        this.height = iconHeight;
    }

    /**
     * Return's the icon's width.
     * 
     * @see javax.swing.Icon#getIconWidth()
     */
    public int getIconWidth() {
        return this.width;
    }

    /**
     * Returns the icon's height.
     * 
     * @see javax.swing.Icon#getIconHeight()
     */
    public int getIconHeight() {
        return this.height;
    }

    /**
     * Draw the icon at the specified location.
     * 
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        
        ((Graphics2D)g).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(border);
        g.drawRect(x, y, getIconWidth(), getIconHeight());
        
        // set background for option combinations
        if (this.mesh == MESH_NO_FILL) {
            g.setColor(bgLight);
        } else if (this.mesh == MESH_TRANS_FILL || this.mesh == NO_MESH_TRANS_FILL){
            g.setColor(bgMid);
        } else {
            g.setColor(bgDark);
        }
        
        g.fillRect(x+1, y+1, getIconWidth()-1, getIconHeight()-1);
        
        // set mesh for option combinations
        if (this.mesh == MESH_NO_FILL || this.mesh == MESH_TRANS_FILL || this.mesh == MESH_SOLID_FILL) {
            int evenWidth = 1;
            if (getIconWidth() % 2 == 1) evenWidth = 1;
            int middle = (getIconWidth() / 2);
            int  i = 1;
            int[] xs = {                 x+i, x+middle,  x+middle+evenWidth, x+getIconWidth()-i };
            int[] ys = { y+getIconHeight()-i,      y+i, y+getIconHeight()-i,                y+i };
            g.setColor(meshLine);
            g.drawPolyline(xs, ys, 4);            
        }
        
        // lastly, if owner is disabled, draw a transparent rectangle to give 
        // a disabled appearance to color well
        if (!c.isEnabled()) {
            g.setColor(disabledColor);
            g.fillRect(x, y, this.width+1, this.height+1);
            
        }
    }
}
