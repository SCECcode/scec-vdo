package org.scec.vtk.tools;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Preference class that holds several default size and color values
 * as well as some system specific settings (e.g. UI, command keys, file
 * separator) for convenience. <br/><br/>
 * 
 * <font color=red">
 * TODO: store project localization/coordinate conversion information<br/>
 * TODO: build preference interface along with regional project data stores<br/>
 * TODO: need to take inventory of how objects are lit - check globeBox text and grid
 * </font>
 * 
 * Created on Feb 21, 2005
 * 
 * @author P. Powers
 * @version $Id: Prefs.java 4189 2012-07-26 15:46:58Z hoogstra $
 */
public class Prefs {
    // TODO SJD consider reading these values from a properties file, or using the Spring framework    
    // appearance constants
    /** The default ambient color used to construct a <code>Material</code> */
    public static final Color  DEFAULT_MATERIAL_AMBIENT = new Color(0.3f, 0.3f, 0.3f);
    /** The default emissive color used to construct a <code>Material</code> */
    public static final Color  DEFAULT_MATERIAL_EMISSIVE = new Color(0.0f, 0.0f, 0.0f);
    /** The default diffuse color used to construct a <code>Material</code> */
    public static final Color  DEFAULT_MATERIAL_DIFFUSE = new Color(0.8f, 0.8f, 0.8f);
    /** The default specular color used to construct a <code>Material</code> */
    public static final Color  DEFAULT_MATERIAL_SPECULAR = new Color(0.0f, 0.0f, 0.0f);
    /** The default shininess value used to construct a <code>Material</code> */
    public static final float  DEFAULT_MATERIAL_SHININESS = 100.0f;    

    // TODO update with geo3d/SV class association
    private static Preferences prefs = Preferences.userNodeForPackage(Prefs.class);

    // default preference fields
    private static String defaultLibLocation;
    
    private static int defaultStriping_r = 240;
    private static int defaultStriping_g = 240;
    private static int defaultStriping_b = 255;
    
    private static int defaultPluginWidth = 300;
    private static int defaultPluginHeight = 650;
    
    private static int defaultMainWidth = 720;
    private static int defaultMainHeight = 780;
    //private static int defaultTotalWidth = defaultMainWidth + defaultPluginWidth;
    
    private static int os;
    private static int    platformActionKey;
    private static String platformFileSeparator;
    private static Insets platformIconInset;
    public static final int OSX = 1;
    public static final int WINDOWS = 2;
    public static final float Unrecorded_Depth_Flag = 10.991234f; //Use this value to flag unrecorded depth in catalogs
    private static boolean initialized = false;
    
    /**
     * Initializes platform specific values.
     */
    public static void init() {
        
        // initialize platform values
        //osx = (System.getProperty("os.name").equals("Mac OS X")) ? true : false;
    	
        String system = System.getProperty("os.name");
        if (system.equals("Mac OS X")) {
        	os = OSX;
        } else if (system.indexOf("Windows")!=-1){
        	os = WINDOWS;
        }
    	platformActionKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        platformFileSeparator = File.separator;
        if (os==OSX) {
        	platformIconInset = new Insets(3,4,3,4);
        } else {
        	platformIconInset = new Insets(3,4,2,3);
        }
        // initialize default library location
        defaultLibLocation = 
        	System.getProperty("user.home") + platformFileSeparator + ".scec_vdo";
        initialized = true;
    }
    
    public static boolean isInitialized() {
    	return initialized;
    }
    
    /**
     * Sets the standard plugin panel width.
     * 
     * @param width of plugin panel
     */
    public static void setPluginWidth(int width) {
        prefs.putInt("PLUGIN_WIDTH", width);
    }
    
    /**
     * Returns the standard plugin panel width.
     * 
     * @return width of plugin panel
     */
    public static int getPluginWidth() {
        return prefs.getInt("PLUGIN_WIDTH", defaultPluginWidth);
    }
    
    /**
     * Sets the standard plugin panel height.
     * 
     * @param height of plugin panel
     */
    public static void setPluginHeight(int height) {
        prefs.putInt("PLUGIN_HEIGHT", height);
    }
    
    /**
     * Returns the standard plugin panel height.
     * 
     * @return height of plugin panel
     */
    public static int getPluginHeight() {
        return prefs.getInt("PLUGIN_HEIGHT", defaultPluginHeight);
    }

    public static void setMainHeight(int height) {
    	prefs.putInt("MAIN_HEIGHT",height);
    }
    
    public static int getMainHeight() {	
    	return prefs.getInt("MAIN_HEIGHT", defaultMainHeight);
    }
    
    public static void setMainWidth(int width) {
    	prefs.putInt("MAIN_WIDTH", width);
    }
    
    public static int getMainWidth() {
    	return prefs.getInt("MAIN_WIDTH", defaultMainWidth);
    }
    
    public static void setTotalWidth(int width) {
    	prefs.putInt("MAIN_WIDTH", width - defaultPluginWidth);
    }
    
    public static int getTotalWidth() {
    	return prefs.getInt("MAIN_WIDTH", defaultMainWidth) +
    		prefs.getInt("PLUGIN_WIDTH", defaultPluginWidth);
    }
    /**
     * Sets the <i>ScecVideo</i> plugin data library location.
     * 
     * @param location path to data library directory
     */
    public static void setLibLoc(String location) {
        prefs.put("LIB",location);
    }
        
    /**
     * Returns the <i>ScecVideo</i> plugin data library location.
     * 
     * @return the data library directory path
     */
    public static String getLibLoc() {
//    	System.out.println("libloc"+ prefs.get("LIB",defaultLibLocation));
//    	System.out.flush();
        return prefs.get("LIB", defaultLibLocation);
    }
    
    public static String getDefaultLocation()
    {
    	return defaultLibLocation;
    }
    
    /**
     * Sets the background striping color used in plugin tables.
     * 
     * @param color to set
     */
    public static void setStripingColor(Color color) {
        prefs.putInt("STRIPE_COLOR_R", color.getRed());
        prefs.putInt("STRIPE_COLOR_G", color.getGreen());
        prefs.putInt("STRIPE_COLOR_B", color.getBlue());
    }
    
    /**
     * Returns the background striping color used in plugin tables.
     * 
     * @return the background striping color
     */
    public static Color getStripingColor() {
        int r = prefs.getInt("STRIPE_COLOR_R", defaultStriping_r);
        int g = prefs.getInt("STRIPE_COLOR_G", defaultStriping_g);
        int b = prefs.getInt("STRIPE_COLOR_B", defaultStriping_b);
        Color c = new Color(r, g, b);
        return c;
    }
    
    /**
     * As yet unused method to move the <i>ScecVideo</i> data library location
     */
    public static void moveLib() {
        // TODO implement with preference panel
    }

    /**
     * Returns whether platform is Apple "OS X".
     * 
     * @return whether OS X
     */
    public static int getOS() {
        return os;
    }
    
    /**
     * Returns the action (menu shortcut key) for the current platrform.
     * 
     * @return the platform shortcut key.
     */
    public static int getActionKey() {
        return platformActionKey;
    }
    
    /**
     * Returns the file-separator for the current platform.
     * 
     * @return the platform file separator.
     */
    public static String getFS() {
        return platformFileSeparator;
    }
    
    
    /**
     * Returns the <code>Insets</code> that optimize icon button appearance
     * depending on users platform. Could be considered needless customization.
     * 
     * @return the platform specific icon insets.
     */
    public static Insets getIconInset() {
        return platformIconInset;
    }
}
