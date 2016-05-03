package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

//import javax.media.j3d.Texture;
import javax.swing.ImageIcon;

//import com.sun.j3d.utils.image.TextureLoader;

/**
 * This class wraps static focal mechanism <code>ImageIcon</code> objects so that
 * list and table renderers do not recreate image icons each time they're called.
 * This class also manages the textures that are used to paint <i>ScecVideo</i>
 * earthquakes.
 *
 * Created on Feb 9, 2005
 * 
 * @author P. Powers
 * @version $Id: FocalMechIcons.java 1624 2006-07-13 23:50:36Z pack $
 */

public class FocalMechIcons {
    
//    /** No focal mechanism value. */
//    public static final int NONE           = 0;
    /** Blue-grey focal mechanism value. */
    public static final int BLUE_GREY      = 0;
    /** Blue-yellow focal mechanism value. */
    public static final int BLUE_YELLOW    = 1;
    /** Green-grey focal mechanism value. */
    public static final int GREEN_GREY     = 2;
    /** Red-yellow focal mechanism value. */
    public static final int RED_YELLOW     = 3;
    /** Red-grey focal mechanism value. */
    public static final int RED_GREY       = 4;
    /** Orange-yellow focal mechanism value. */
    public static final int ORANGE_YELLOW  = 5;

    
    private static Class FM = FocalMechIcons.class;

    /** BEGIN: DO NOT REMOVE THESE UNUSED FIELD **/
    /** These fields are used when getDeclaredField.get(null) is called on the FocalMechIcons class
     * for such operations as providing the proper name for the icons in the earthquake catalog gui
     * on the display tab.  
     */
    
    protected static FocalIcon blu_gry_12_reg = new FocalIcon("blu_gry_12_reg");
    protected static FocalIcon blu_gry_12_dis = new FocalIcon("blu_gry_12_dis");
    protected static FocalIcon blu_gry_16_reg = new FocalIcon("blu_gry_16_reg");
    protected static FocalIcon blu_gry_16_dis = new FocalIcon("blu_gry_16_dis");
    
    protected static FocalIcon blu_yel_12_reg = new FocalIcon("blu_yel_12_reg");
    protected static FocalIcon blu_yel_12_dis = new FocalIcon("blu_yel_12_dis");
    protected static FocalIcon blu_yel_16_reg = new FocalIcon("blu_yel_16_reg");
    protected static FocalIcon blu_yel_16_dis = new FocalIcon("blu_yel_16_dis");
    
    protected static FocalIcon grn_gry_12_reg = new FocalIcon("grn_gry_12_reg");
    protected static FocalIcon grn_gry_12_dis = new FocalIcon("grn_gry_12_dis");
    protected static FocalIcon grn_gry_16_reg = new FocalIcon("grn_gry_16_reg");
    protected static FocalIcon grn_gry_16_dis = new FocalIcon("grn_gry_16_dis");
    
    protected static FocalIcon red_yel_12_reg = new FocalIcon("red_yel_12_reg");
    protected static FocalIcon red_yel_12_dis = new FocalIcon("red_yel_12_dis");
    protected static FocalIcon red_yel_16_reg = new FocalIcon("red_yel_16_reg");
    protected static FocalIcon red_yel_16_dis = new FocalIcon("red_yel_16_dis");
    
    protected static FocalIcon red_gry_12_reg = new FocalIcon("red_gry_12_reg");
    protected static FocalIcon red_gry_12_dis = new FocalIcon("red_gry_12_dis");
    protected static FocalIcon red_gry_16_reg = new FocalIcon("red_gry_16_reg");
    protected static FocalIcon red_gry_16_dis = new FocalIcon("red_gry_16_dis");
    
    protected static FocalIcon org_yel_12_reg = new FocalIcon("org_yel_12_reg");
    protected static FocalIcon org_yel_12_dis = new FocalIcon("org_yel_12_dis");
    protected static FocalIcon org_yel_16_reg = new FocalIcon("org_yel_16_reg");
    protected static FocalIcon org_yel_16_dis = new FocalIcon("org_yel_16_dis");
    /** END: DO NOT REMOVE THESE UNUSED FIELD **/
    
    /**
     * Returns the large form of a focal mechanism icon (16x16 pixels).
     *  
     * @param pattern value that identifies focal mechanism colors
     * @return a focal mechanism icon
     */
    public static FocalIcon getLargeIcon(int pattern) {
        String name = getStringForPattern(pattern) + "_16_reg";
        FocalIcon icon;
        try {
            icon = (FocalIcon)FM.getDeclaredField(name).get(null);
        }
        catch (Exception e) {
            // an error is thrown in OS X if command+click selection is performed
            // in a JComboBox; return value at top of list
            return blu_gry_16_reg;
        }
        return icon;
    }
    
    /**
     * Returns the large, disabled form of a focal mechanism icon (16x16 pixels).
     * 
     * @param pattern value that identifies focal mechanism colors
     * @return a focal mechanism icon
     */
    public static FocalIcon getLargeIconDisabled(int pattern) {
        String name = getStringForPattern(pattern) + "_16_dis";
        FocalIcon icon;
        try {
            icon = (FocalIcon)FM.getDeclaredField(name).get(null);
        }
        catch (Exception e) {
            // an error is thrown in OS X if command+click selection is performed
            // in a JComboBox; return value at top of list
            return blu_gry_16_dis;
        }
        return icon;
    }

    /**
     * Returns the small form of a focal mechanism icon (12x12 pixels).
     * 
     * @param pattern value that identifies focal mechanism colors
     * @return a focal mechanism icon
     */
    public static FocalIcon getSmallIcon(int pattern) {
        String name = getStringForPattern(pattern) + "_12_reg";
        FocalIcon icon;
        try {
            icon = (FocalIcon)FM.getDeclaredField(name).get(null);
        }
        catch (Exception e) {
            return blu_gry_12_reg;
        }
        return icon;
    }

    /**
     * Returns the small, disabled form of a focal mechanism icon (12x12 pixels).
     * 
     * @param pattern value that identifies focal mechanism colors
     * @return a focal mechanism icon
     */
    public static FocalIcon getSmallIconDisabled(int pattern) {
        String name = getStringForPattern(pattern) + "_12_dis";
        FocalIcon icon;
        try {
            icon = (FocalIcon)FM.getDeclaredField(name).get(null);
        }
        catch (Exception e) {
            return blu_gry_12_dis;
        }
        return icon;
    }

    /**
     * Given a focal mechanism pattern, this method returns the associated
     * Java3D <code>Texture</code>.
     * 
     * @param pattern value that identifies focal mechanism colors
     * @return a focal mechanism <code>Texture</code>
     */
   /* public static Texture getTexture(int pattern) {
        String textureName = 
            "resources/img/focal_" +
            getStringForPattern(pattern) +
            "_tex.png";
        TextureLoader loader = new TextureLoader(
                FocalMechIcons.class.getResource(textureName),
                "RGB",
                null);
        return loader.getTexture();
    }*/
    

    
    
    // translate pattern numbers to strings that can be used to
    // access fields and filenames
    protected static String getStringForPattern(int pattern) {
        switch(pattern) {
            case 0: return "blu_gry";
            case 1: return "blu_yel";
            case 2: return "grn_gry";
            case 3: return "red_yel";
            case 4: return "red_gry";
            case 5: return "org_yel";
            default: return "red_yel";
        }
    }

    /**
     * Nested class is an <code>ImageIcon</code>.
     *
     * Created on Feb 9, 2005
     * 
     */
    protected static class FocalIcon extends ImageIcon {
        
    	protected static final long serialVersionUID = 1L;

		/**
         * Constructs a new focal mechanism icon.
         * 
         * @param name string that identifies focal mechanism colors
         */
    	protected FocalIcon(String name) {
            super(FocalMechIcons.class.getResource("resources/img/focal_" + name + ".png"));
        }        
    }
}
