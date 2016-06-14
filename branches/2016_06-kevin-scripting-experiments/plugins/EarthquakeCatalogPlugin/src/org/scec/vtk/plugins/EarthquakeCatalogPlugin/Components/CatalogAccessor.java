package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
//import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPlugin;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginGUI;
import org.scec.vtk.plugins.utils.AbstractDataAccessor;
import org.scec.vtk.plugins.utils.DataImport;
import org.scec.vtk.tools.Prefs;


/**
 * This subclass of <code>AbstractDataAccessor</code> provides the bulk of the framework
 * necessary to deal with an earthquake catalog. Concrete subclasses need only implement
 * constructors and custom i/o methods and fields as necessary.
 *
 * Created on Feb 16, 2005
 * 
 * @author P. Powers
 * @version $Id: CatalogAccessor.java 3890 2011-07-21 17:27:22Z rlacey $
 */
public abstract class CatalogAccessor extends AbstractDataAccessor {
// TODO:  SJD remove GUI elements (swing) to make this potentially more (re)usable.
	
    private Logger log = Logger.getLogger(CatalogAccessor.class);
    
    /** Value indicating a basic catalog. */
    public static final int DATA_SCOPE_NO_EXTRAS    = 11;
    /** Value indicating that catalog contains horizontal and vertical uncertainties. */
    public static final int DATA_SCOPE_UNCERT       = 13;
    /** Value indicating that catalog contains strike, dip, and rake of focal mechanisms. */
    public static final int DATA_SCOPE_FOCAL        = 14;
    /** Value indicating that catalog contains both focal mechanism and uncertainty data. */
    public static final int DATA_SCOPE_UNCERT_FOCAL = 16;
    /** Value indicating that catalog contains focal mechanism data and fault plane probability data*/
    public static final int DATA_SCOPE_FOCAL_PROB	= 18;
    
    // display formatters
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy  (HH:mm:ss)");

    // data attributes
    protected int   numEvents;
    private int   dataScope;
    private float minLatitude;
    private float maxLatitude;
    private float minLongitude;
    private float maxLongitude;
    private float minDepth;
    private float maxDepth;
    private float minMagnitude;
    private float maxMagnitude;
    private Date  minDate;
    private Date  maxDate;
        
    /** Catalog owner (plugin GUI) used to access gui sub-components/dialogs. */
    protected Component owner;

    // JDOM accessor objects
    /** XML <code>Element</code> for access to object attribute <code>Element</code>s */
    protected Element objectAttributes;
    /** XML <code>Element</code> for data attribute access (extents, stats, etc.) */
    protected Element dataAttributes;
    
    // data fields
    /** Array of earthquake event ID's. Initialized to <code>null</code> */
    protected int[]   eq_id = null;
    /** Array of earthquake event times. Initialized to <code>null</code> */
    protected Date[]  eq_time = null;
    /** Array of earthquake event latitudes. Initialized to <code>null</code> */
    protected float[] eq_latitude = null;
    /** Array of earthquake event longitudes. Initialized to <code>null</code> */
    protected float[] eq_longitude = null;
    /** Array of earthquake event depths. Initialized to <code>null</code> */
    protected float[] eq_depth = null;
    /** Array of earthquake event magnitudes. Initialized to <code>null</code> */
    protected float[] eq_magnitude = null;
    /** Array of earthquake event horizontal errors. Initialized to <code>null</code> */
    protected float[] eq_xy_error = null;
    /** Array of earthquake event veritcal errors. Initialized to <code>null</code> */
    protected float[] eq_z_error = null;
    /** Array of earthquake focal mechanism strikes. Initialized to <code>null</code> */
    protected int[]   eq_strike = null;
    /** Array of earthquake event focal mechanism dips. Initialized to <code>null</code> */
    protected int[]   eq_dip = null;
    /** Array of earthquake event focal mechanism rakes. Initialized to <code>null</code> */
    protected int[]   eq_rake = null;
    /** Array telling which is the preferred plane of a focal mechanism.  Initialized to <code>null<code> */
    protected int[]	  eq_nodal = null;
    /** Array of probababilities that the preferred plane is in fact the real fault plane.  Initialized to <code>null<code> */
    protected float[] eq_probability = null;
    
    // are data arrays loaded or set to null
    private boolean objectInMemory = false;
    
    /** Static parser available to catalogs */
    protected static SAXBuilder parser = new SAXBuilder();
    //public ArrayList eqList = new ArrayList();
    //private Earthquake eqs;
    /**
     * Constructs a new <code>CatalogAccessor</code> with the given parent.
     * 
     * @param parent <code>Component</code> to set as owner
     */
    public CatalogAccessor(Component parent) {
        this.owner = parent;
    }
 
    //****************************************
    //  PROTECTED & PUBLIC UTILITY METHODS
    //****************************************

    /**
     * Constructs a new catalog document/file. The no-argument constructor assumes that
     * a new source file is being built/imported.
     * 
     * @see org.scec.geo3d.plugins.utils.AbstractDataAccessor#newDocument()
     */
    protected boolean newDocument() {
        // add attribute component
        if (super.newDocument()) {
            URL attsDoc = CatalogAccessor.class.getResource("resources/xml/catalog_template.xml");
            try {
                Document temp = parser.build(attsDoc);
                this.objectAttributes = (Element)temp.getRootElement().getChild("object_attributes").detach();
                this.dataAttributes = this.objectAttributes.getChild("data");
                this.objectDocument.getRootElement().addContent(this.objectAttributes);
            }
            catch (Exception e) {
                log.debug("problem parsing XML");
                return false;
            }
            return true;
        }
        return false;
    }
    
    
                
    /**
     * Reads/loads catalog object attribute information from a given <i>ScecVideo</i>
     * library file.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#readAttributeFile(java.io.File)
     */
    public boolean readAttributeFile(File file) {
        if (!(super.readAttributeFile(file))) return false;
        this.objectAttributes = this.objectDocument.getRootElement().getChild("object_attributes");
        this.dataAttributes = this.objectAttributes.getChild("data");
        // load fields
        try {
            this.numEvents     = this.dataAttributes.getAttribute("num_events").getIntValue();
            this.dataScope     = this.dataAttributes.getAttribute("scope").getIntValue();
            this.minLatitude   = this.dataAttributes.getChild("latitude").getAttribute("min").getFloatValue();
            this.maxLatitude   = this.dataAttributes.getChild("latitude").getAttribute("max").getFloatValue();
            this.minLongitude  = this.dataAttributes.getChild("longitude").getAttribute("min").getFloatValue();
            this.maxLongitude  = this.dataAttributes.getChild("longitude").getAttribute("max").getFloatValue();
            this.minDepth      = this.dataAttributes.getChild("depth").getAttribute("min").getFloatValue();
            this.maxDepth      = this.dataAttributes.getChild("depth").getAttribute("max").getFloatValue();
            this.minMagnitude  = this.dataAttributes.getChild("magnitude").getAttribute("min").getFloatValue();
            this.maxMagnitude  = this.dataAttributes.getChild("magnitude").getAttribute("max").getFloatValue();
            this.minDate       = DATE_FORMAT.parse(this.dataAttributes.getChild("date").getAttributeValue("min"));
            this.maxDate       = DATE_FORMAT.parse(this.dataAttributes.getChild("date").getAttributeValue("max"));
        }
        catch (Exception e) {
            log.debug("problem reading XML");
            EarthquakeCatalogPluginGUI.status.setText(DataImport.ERROR_OBJECT_LOAD);
            return false;
        }
        return true;        
    }
    

    /**
     * Reads/loads catalog object data from a <i>ScecVideo</i> library file. Location
     * of file will be known from previous call to
     * {@link CatalogAccessor#readAttributeFile(File)}.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#readDataFile()
     */
    public boolean readDataFile() {
        try {
            ObjectInputStream objIn = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(getDataFile())));
            this.eq_id        = (int[])objIn.readObject();
            this.eq_time      = (Date[])objIn.readObject();
            this.eq_latitude  = (float[])objIn.readObject();
            this.eq_longitude = (float[])objIn.readObject();
            this.eq_depth     = (float[])objIn.readObject();
            this.eq_magnitude = (float[])objIn.readObject();
            if (getDataScope() == DATA_SCOPE_UNCERT || getDataScope() == DATA_SCOPE_UNCERT_FOCAL || getDataScope() == DATA_SCOPE_FOCAL_PROB) {
                this.eq_xy_error = (float[])objIn.readObject();
                this.eq_z_error  = (float[])objIn.readObject();
            }
            if (getDataScope() == DATA_SCOPE_FOCAL || getDataScope() == DATA_SCOPE_UNCERT_FOCAL || getDataScope() == DATA_SCOPE_FOCAL_PROB) {
                this.eq_strike = (int[])objIn.readObject();
                this.eq_dip    = (int[])objIn.readObject();
                this.eq_rake   = (int[])objIn.readObject();
            }
            if (getDataScope() == DATA_SCOPE_FOCAL_PROB) {
            	this.eq_nodal       = (int[])objIn.readObject();
            	this.eq_probability = (float[])objIn.readObject();
            }
            objIn.close();
        }
        catch (Exception e) {
            log.debug("problem reading binary data file");
            EarthquakeCatalogPluginGUI.status.setText(DataImport.ERROR_OBJECT_LOAD);
            return false;
        }
        return true;
    }

    
    /**
     * Writes catalog object data to disk/library.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#writeDataFile()
     */
    public boolean writeDataFile() {
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(getDataFile())));
            objOut.writeObject(this.eq_id);
            objOut.writeObject(this.eq_time);
            objOut.writeObject(this.eq_latitude);
            objOut.writeObject(this.eq_longitude);
            objOut.writeObject(this.eq_depth);
            objOut.writeObject(this.eq_magnitude);
            if (getDataScope() == DATA_SCOPE_UNCERT || getDataScope() == DATA_SCOPE_UNCERT_FOCAL || getDataScope() == DATA_SCOPE_FOCAL_PROB) {
                objOut.writeObject(this.eq_xy_error);
                objOut.writeObject(this.eq_z_error);
            }
            if (getDataScope() == DATA_SCOPE_FOCAL || getDataScope() == DATA_SCOPE_UNCERT_FOCAL || getDataScope() == DATA_SCOPE_FOCAL_PROB) {
                objOut.writeObject(this.eq_strike);
                objOut.writeObject(this.eq_dip);
                objOut.writeObject(this.eq_rake);
            }
            if (getDataScope() == DATA_SCOPE_FOCAL_PROB) {
            	objOut.writeObject(this.eq_nodal);
            	objOut.writeObject(this.eq_probability);
            }
            objOut.close();
        }
        catch (Exception e) {
            log.debug("problem writing binary data file");
            EarthquakeCatalogPluginGUI.status.setText(DataImport.ERROR_FILE_WRITE);
            return false;
        }
        return true;
    }

    /**
     * Method prevents immediate overwite of catalog with same name.
     * 
     * @param name to save catalog as
     * @return new unique name
     */
    protected String confirmOutputFile(String name) {
    	// TODO SJD don't directly call GUI from this method; throw an exception and let the GUI decide how to display it.    	
        String filename = 
            Prefs.getLibLoc() + 
            File.separator + EarthquakeCatalogPlugin.dataStoreDir +
            File.separator + "source" +
            File.separator + name + ".cat";
        String newName = name;
        if ((new File(filename)).exists()) {
            JTextField textfield = new JTextField(20);
            textfield.setText(name);
            String message = "A catalog with this name already  \n" +
                             "exists. Change the name below or  \n" +
                             "click OK to overwrite file.\n\n";
            int val = JOptionPane.showConfirmDialog(
                    this.owner,
                    new Object[] {message, textfield},
                    "Warning: Catalog Exists",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                    
            if (val == JOptionPane.CLOSED_OPTION || val == JOptionPane.CANCEL_OPTION) {
                return null;
            }
            newName = (textfield.getText().trim().equals("")) ?
                    "source" : textfield.getText().trim();
        }
        return newName;
    }

    /**
     * Initializes object data arrays to size determined by length of catalog file.
     * 
     * @param length to set data arrays to
     */
    protected void initializeArrays(int length) {
        this.eq_id        = new int[length];
        this.eq_time      = new Date[length];
        this.eq_latitude  = new float[length];
        this.eq_longitude = new float[length];
        this.eq_depth     = new float[length];
        this.eq_magnitude = new float[length];
        if (getDataScope() == DATA_SCOPE_UNCERT || getDataScope() == DATA_SCOPE_UNCERT_FOCAL || getDataScope() == DATA_SCOPE_FOCAL_PROB) {
            this.eq_xy_error  = new float[length];
            this.eq_z_error   = new float[length];
        }
        if (getDataScope() == DATA_SCOPE_FOCAL || getDataScope() == DATA_SCOPE_UNCERT_FOCAL || getDataScope() == DATA_SCOPE_FOCAL_PROB) {
            this.eq_strike    = new int[length];
            this.eq_dip       = new int[length];
            this.eq_rake      = new int[length];
        }
        if (getDataScope() == DATA_SCOPE_FOCAL_PROB) {
        	this.eq_nodal 		= new int[length];
        	this.eq_probability = new float[length];
        }
    }

    /**
     * Sets all object data arrays to null.
     */
    protected void clearArrays() {
        this.eq_id          = null;
        this.eq_time        = null;
        this.eq_latitude    = null;
        this.eq_longitude   = null;
        this.eq_depth       = null;
        this.eq_magnitude   = null;
        this.eq_xy_error    = null;
        this.eq_z_error     = null;
        this.eq_strike      = null;
        this.eq_dip         = null;
        this.eq_rake        = null;
        this.eq_nodal	    = null;
        this.eq_probability = null;
    }
    
    /**
     * Sets the status message in this plugin's GUI.
     * 
     * @param message to set
     */
    protected void setStatus(String message) {
    	EarthquakeCatalogPluginGUI.status.setText(message);
    }

    /**
     * Copies events at given indices from a given catalog.
     * 
     * @param src catalog to clone events from
     * @param srcIndices events to clone
     */
    public void cloneEvents(CatalogAccessor src, int[] srcIndices) {
        setDataScope(src.getDataScope());
        setNumEvents(srcIndices.length);
        initializeArrays(srcIndices.length);
        // do this in batches so that ifs below are not evaluated repeatedly
        for (int i=0; i<srcIndices.length; i++) {
            this.eq_id[i]        = src.eq_id[srcIndices[i]];
            this.eq_time[i]      = src.eq_time[srcIndices[i]];
            this.eq_latitude[i]  = src.eq_latitude[srcIndices[i]];
            this.eq_longitude[i] = src.eq_longitude[srcIndices[i]];
            this.eq_depth[i]     = src.eq_depth[srcIndices[i]];
            this.eq_magnitude[i] = src.eq_magnitude[srcIndices[i]];
        }
        if (src.getDataScope() == DATA_SCOPE_UNCERT || src.getDataScope() == DATA_SCOPE_UNCERT_FOCAL || src.getDataScope() == DATA_SCOPE_FOCAL_PROB) {
            for (int i=0; i<srcIndices.length; i++) {
                this.eq_xy_error[i] = src.eq_xy_error[srcIndices[i]];
                this.eq_z_error[i]  = src.eq_z_error[srcIndices[i]];
            }
        }
        if (src.getDataScope() == DATA_SCOPE_FOCAL || src.getDataScope() == DATA_SCOPE_UNCERT_FOCAL || src.getDataScope() == DATA_SCOPE_FOCAL_PROB) {
            for (int i=0; i<srcIndices.length; i++) {
                this.eq_strike[i] = src.eq_strike[srcIndices[i]];
                this.eq_dip[i]  = src.eq_dip[srcIndices[i]];
                this.eq_rake[i]  = src.eq_rake[srcIndices[i]];
            }
        }
        if (src.getDataScope() == DATA_SCOPE_FOCAL_PROB) {
        	for (int i=0; i<srcIndices.length; i++) {
        		this.eq_nodal[i]	= src.eq_nodal[srcIndices[i]];
        		this.eq_probability[i] = src.eq_probability[srcIndices[i]];
        	}
        }
    }

    /**
     * Scans this catalog and sets minimum and maximum values for latitude, longitude,
     * depth, magnitude, and time.
     */
    public void runMinMax() {
                
        // initial extreme vlaues
        float min_lat =   90.0f;
        float max_lat =  -90.0f;
        float min_lon =  180.0f;
        float max_lon = -180.0f;
        float min_dep =    5.0f;
        float max_dep = -600.0f;
        float min_mag =   10.0f;
        float max_mag =    0.0f;

        // process data arrays
        int events = getNumEvents();
        for (int i=0; i<events; i++) {
            if (this.eq_latitude[i] <= min_lat) min_lat = this.eq_latitude[i];
            if (this.eq_latitude[i] >= max_lat) max_lat = this.eq_latitude[i];
        }
        for (int i=0; i<events; i++) {
            if (this.eq_longitude[i] <= min_lon) min_lon = this.eq_longitude[i];
            if (this.eq_longitude[i] >= max_lon) max_lon = this.eq_longitude[i];
        }
        for (int i=0; i<events; i++) {
            if (this.eq_depth[i] <= min_dep) min_dep = this.eq_depth[i];
            if (this.eq_depth[i] >= max_dep) max_dep = this.eq_depth[i];
        }
        for (int i=0; i<events; i++) {
            if (this.eq_magnitude[i] <= min_mag) min_mag = this.eq_magnitude[i];
            if (this.eq_magnitude[i] >= max_mag) max_mag = this.eq_magnitude[i];
        }
        
        // TODO switch min max lon values if catalog spans +180/-180 longitude
        // set catalog values
        setMinLatitude(min_lat);
        setMaxLatitude(max_lat);
        setMinLongitude(min_lon);
        setMaxLongitude(max_lon);
        setMinDepth(min_dep);
        setMaxDepth(max_dep);
        setMinMagnitude(min_mag);
        setMaxMagnitude(max_mag);
        setMinDate(this.eq_time[0]);
        setMaxDate(this.eq_time[events-1]);
    }
    
    //****************************************
    //     PRIVATE METHODS
    //****************************************


    //****************************************
    //     GETTERS & SETTERS
    //****************************************
    
    /**
     * Returns whether this catalog's data are loaded.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#isInMemory()
     */
    public boolean isInMemory() {
        return this.objectInMemory;
    }
    
    /**
     * Sets whether this catalog's data and display representation should be loaded
     * or released. Method only sets field; concrete subclasses should override and call
     * as different catalogs will have different data loading requirements.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#setInMemory(boolean)
     */
    public void setInMemory(boolean load) {
        this.objectInMemory = load;
    }
        
    /**
     * Returns the value representing what additional earthquake data is included in this catalog.
     * These data are over and above id, time, latitude, longitude, depth, and magnitude. Possible
     * values are :
     * <pre> 
     * DATA_SCOPE_NO_EXTRAS
     * DATA_SCOPE_UNCERT
     * DATA_SCOPE_FOCAL
     * DATA_SCOPE_UNCERT_FOCAL
     * </pre>
     *
     * @return the data component value
     */
    public int getDataScope() {
        return this.dataScope;
    }
    
    /**
     * Sets the value representing what additional earthquake data is included in this catalog.
     * Possible values are:
     * <pre>
     * DATA_SCOPE_NO_EXTRAS
     * DATA_SCOPE_UNCERT
     * DATA_SCOPE_FOCAL
     * DATA_SCOPE_UNCERT_FOCAL
     * DATA_SCOPE_FOCAL_PROB
     * </pre>
     *
     * @param scope the data scope value to set
     */
    public void setDataScope(int scope) {
        this.dataScope = scope;
        this.dataAttributes.getAttribute("scope").setValue(String.valueOf(scope));
    }
        
    /**
     * Returns end date of this catalog.
     *
     * @return the end date
     */
    public Date getMaxDate() {
        return this.maxDate;
    }
    
    /**
     * Sets the end date of this catalog.
     *
     * @param date the date to set
     */
    public void setMaxDate(Date date) {
        this.maxDate = date;
        String s = DATE_FORMAT.format(date);
        //this.dataAttributes.getChild("date").getAttribute("max").setValue(s);

    }
    
    /**
     * Returns the depth of the deepest event in this catalog.
     *
     * @return the depth value
     */
    public float getMaxDepth() {
        return this.maxDepth;
    }
    
    /**
     * Sets the depth of the deepest event in this catalog.
     *
     * @param depth the depth to set
     */
    public void setMaxDepth(float depth) {
        this.maxDepth = depth;
        //this.dataAttributes.getChild("depth").getAttribute("max").setValue(String.valueOf(depth));
    }
    
    /**
     * Returns the latitude of the northernmost event in this catalog.
     *
     * @return the latitude value
     */
    public float getMaxLatitude() {
        return this.maxLatitude;
    }
    
    /**
     * Sets the latitude of the northernmost event in this catalog.
     *
     * @param latitude the latitude to set
     */
    public void setMaxLatitude(float latitude) {
        this.maxLatitude = latitude;
        //this.dataAttributes.getChild("latitude").getAttribute("max").setValue(String.valueOf(latitude));
    }
    
    /**
     * Returns the longitude of the easternmost event in this catalog.
     *
     * @return the longitude value
     */
    public float getMaxLongitude() {
        return this.maxLongitude;
    }
    
    /**
     * Sets the longitude of the easternmost event in this catalog.
     *
     * @param longitude the longitude to set.
     */
    public void setMaxLongitude(float longitude) {
        this.maxLongitude = longitude;
        //this.dataAttributes.getChild("longitude").getAttribute("max").setValue(String.valueOf(longitude));
    }
    
    /**
     * Returns the magnitude of the largest event in this catalog.
     *
     * @return the magnitude value
     */
    public float getMaxMagnitude() {
        return this.maxMagnitude;
    }
    
    /**
     * Sets the magnitude of the largest event in this catalog.
     *
     * @param magnitude the magnitude to set.
     */
    public void setMaxMagnitude(float magnitude) {
        this.maxMagnitude = magnitude;
        //this.dataAttributes.getChild("magnitude").getAttribute("max").setValue(String.valueOf(magnitude));
    }
    
    /**
     * Returns start date of this catalog.
     *
     * @return the date value
     */
    public Date getMinDate() {
        return this.minDate;
    }
    
    /**
     * Sets the start date of this catalog.
     *
     * @param date the date to set
     */
    public void setMinDate(Date date) {
        this.minDate = date;
        String s = DATE_FORMAT.format(date);
        //this.dataAttributes.getChild("date").getAttribute("min").setValue(s);
    }
    
    /**
     * Returns the depth of the shallowest event in this catalog.
     *
     * @return the depth value
     */
    public float getMinDepth() {
        return this.minDepth;
    }
    
    /**
     * Sets the depth of the shallowest event in this catalog.
     *
     * @param depth the depth to set.
     */
    public void setMinDepth(float depth) {
        this.minDepth = depth;
        //this.dataAttributes.getChild("depth").getAttribute("min").setValue(String.valueOf(depth));
    }
    
    /**
     * Returns the latitude of the southernmost event in this catalog.
     *
     * @return the latitude value
     */
    public float getMinLatitude() {
        return this.minLatitude;
    }
    
    /**
     * Sets the latitude of the southernmost event in this catalog.
     *
     * @param latitude the latitude to set.
     */
    public void setMinLatitude(float latitude) {
        this.minLatitude = latitude;
        //this.dataAttributes.getChild("latitude").getAttribute("min").setValue(String.valueOf(latitude));
    }
    
    /**
     * Returns the longitude of the westernmost event in this catalog.
     * 
     * @return the longitude value
     */
    public float getMinLongitude() {
        return this.minLongitude;
    }
    
    /**
     * Sets the longitude of the westernmost event in this catalog.
     *
     * @param longitude the longitude to set.
     */
    public void setMinLongitude(float longitude) {
        this.minLongitude = longitude;
        //this.dataAttributes.getChild("longitude").getAttribute("min").setValue(String.valueOf(longitude));
    }
    
    /**
     * Returns the magnitude of the smallest event in this catalog as a string.
     *
     * @return the magnitude value
     */
    public float getMinMagnitude() {
    	return this.minMagnitude;
    }

    /**
     * Sets the magnitude of the smallest event in this catalog.
     *
     * @param magnitude the magnitude to set
     */
    public void setMinMagnitude(float magnitude) {
    	this.minMagnitude = magnitude;
    	//this.dataAttributes.getChild("magnitude").getAttribute("min").setValue(String.valueOf(magnitude));
    }

    // Get average latitude of earthquakes, approximated by being the average of the maximum and minimum latitudes
    public float getAvgLat () {
    	return ((minLatitude + maxLatitude) / 2);
    }
    // Get average longitude of earthquakes, approximated by being the average of the maximum and minimum longitudes
    public float getAvgLon () {
    	return ((minLongitude + maxLongitude) / 2);
    }

    /**
     * Returns the total number of events in this catalog.
     *
     * @return the number of events
     */
    public int getNumEvents() {
    	return this.numEvents;
    }

    /**
     * Sets the total number of events in this catalog.
     *
     * @param events the number to set.
     */
    public void setNumEvents(int events) {
        this.numEvents = events;
        this.dataAttributes.getAttribute("num_events").setValue(String.valueOf(events));
    }

    /**
     * This is used in Relative Intensity to show the Target EQ epicenters
     * @param index
     * @param depth
     */
    public void setEq_depth(int index, float depth){
    	eq_depth[index] = depth;
    }
    
	public float getEq_depth(int index) {
		return eq_depth[index];
	}

	public int getEq_dip(int index) {
		return eq_dip[index];
	}

	public int getEq_id(int index) {
		return eq_id[index];
	}

	public float getEq_latitude(int index) {
		return eq_latitude[index];
	}

	public float getEq_longitude(int index) {
		return eq_longitude[index];
	}

	public float getEq_magnitude(int index) {
		return eq_magnitude[index];
	}

	public int getEq_rake(int index) {
		return eq_rake[index];
	}

	public int getEq_strike(int index) {
		return eq_strike[index];
	}

	public Date getEq_time(int index) {
		return eq_time[index];
	}

	public float getEq_xy_error(int index) {
		return eq_xy_error[index];
	}

	public float getEq_z_error(int index) {
		return eq_z_error[index];
	}
	
	public int getEQ_nodal(int index) {
		return eq_nodal[index];
	}
	
	public float getEQ_probability(int index){
		return eq_probability[index];
	}
    

}
