package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.io.*;
import java.util.*;
import java.awt.*;

import org.apache.log4j.Logger;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPlugin;
//import org.scec.geo3d.plugins.MagFreqPlugin.MagFreqPluginGUI;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginGUI;
import org.scec.vtk.plugins.utils.DataImport;
import org.scec.vtk.plugins.utils.components.ObjectInfoDialog;
import org.scec.vtk.tools.Prefs;

/**
 * This class imports flat files to the <i>ScecVideo</i> catalog source library. Source 
 * files for each import are assumed to be composed of ascii text in 11, 13, 14,
 *  or 16 columns with values for:
 * <blockquote><b>
 * CuspID Year Month Day Hour Minute Second Longitude Latitude Depth 
 * Magnitude [ [ HorizontalError VerticalError] [ Strike Dip Rake] ] 
 * </b></blockquote>
 * Make sure txt file is space-delimited and not tab delimited.
 * Source files are also assumed to be sorted by increasing date.
 *
 * Created on Feb 14, 2005
 * 
 * @author P. Powers
 * @author Addie Beseda (small change to the way num_eqs is determined by the program)
 * @version $Id: SourceCatalog.java 4155 2012-07-20 20:12:51Z davesmith $
 */
public class SourceCatalog extends CatalogAccessor implements DataImport {
    
    private Logger log = Logger.getLogger(SourceCatalog.class);

    // internal Calendar used for eq_time (Date) creation
    private GregorianCalendar cal = new GregorianCalendar();
    private String name; //default name of the catalog
        
    /**
     * Constructs a new, empty <code>SourceCatalog</code> initialized with the given 
     * parent <code>Component</code>. This object needs to be
     * further initialized with the <code>processFile()</code> method.
     * 
     * @param parent to set
     */
    public SourceCatalog(Component parent) {
        super(parent);
    }
    
    /**
     * Creates a <code>SourceCatalog</code> object with a given parent from a given
     * source file (attribute file in the <i>ScecVideo</i> library).
     * 
     * @param parent
     * @param catalog
     */
    public SourceCatalog(Component parent, File catalog) {
        super(parent);
        if (!readAttributeFile(catalog)) {
            setStatus(DataImport.ERROR_OBJECT_LOAD);
        }
    }
    
    /**
     * Sets whether this source catalog's data  should be loaded
     * or released.
     * 
     * @see org.scec.geo3d.plugins.utils.DataAccessor#setInMemory(boolean)
     */
    public void setInMemory(boolean load) {
        if (load) {
            // importers may have already loaded data arrays; bypass by checking one
            if (this.eq_id == null) {
                readDataFile();
            }
        } else {
            clearArrays();
        }
        super.setInMemory(load);
    }

    /**
     * Imports a given flat columnar-data file into the <i>ScecVideo</i> library.
     * 
     * @param datafile to import
     * @return whether <code>datafile</code> was successfully imported
     */
    public boolean processFile(File datafile) {
    	return this.processFile(datafile, true);
    }
    public boolean processFile(File datafile, boolean displayAddCatInfo) {
        setStatus(DataImport.IMPORT_START);
        
        // create new xml object document; shortcircuit on error
        if (!newDocument()) {
            setStatus(DataImport.ERROR_IMPORT_INIT);
            return false;
        }
        
        int columns;
        int num_eqs;
        try {
            // set up file reader
            BufferedReader inStream = new BufferedReader(new FileReader(datafile));
            String line = inStream.readLine();
            StringTokenizer data = new StringTokenizer(line);
            
            // determine data scope
            columns = data.countTokens();
            if (!(columns == CatalogAccessor.DATA_SCOPE_NO_EXTRAS ||
            		columns == CatalogAccessor.DATA_SCOPE_UNCERT ||
            		columns == CatalogAccessor.DATA_SCOPE_FOCAL ||
            		columns == CatalogAccessor.DATA_SCOPE_UNCERT_FOCAL ||
            		columns == CatalogAccessor.DATA_SCOPE_FOCAL_PROB)) {
            	setStatus(DataImport.ERROR_FILE_FORMAT);
            	return false;
            }
            setDataScope(columns);
            
            /* initialize arrays based on catalog number of lines
             * ...value of num_eqs changed by Addie july 01
             * to avoid going through the file two times in a row, you may need to change
             * the way the array methods are implemented, since the methods assume you
             * know the number of earthquakes before you have parsed the rest of the file.
             */
            
            int count = 0; 
            BufferedReader countReader = new BufferedReader(new FileReader(datafile));
            while (countReader.ready()){
            	countReader.readLine();
            	if (line != "") { count++; } //don't include blank lines, if the program has any
            }
            num_eqs = count;
            
            setNumEvents(num_eqs);
            initializeArrays(num_eqs);
            
            // load arrays
            int index = 0;
            while (line != null) {
                loadArrays(index, line);
                index++;
                line = inStream.readLine();
            }

        }
        catch (Exception e) {
            log.debug("problem reading binary data file");
            setStatus(DataImport.ERROR_FILE_READ);
            return false;
        }

        // validate output filename strings
        String filename = confirmOutputFile(
                (datafile.getName()).substring(0,datafile.getName().lastIndexOf(".")));
        if (filename == null) {
            setStatus(DataImport.IMPORT_CANCEL);
            return false;
        }
        
        // set object_info fields
        setObjectClass(this.getClass().toString());
        setSourceFile(datafile.getPath());
        setAttributeFile( 
                File.separator + EarthquakeCatalogPlugin.dataStoreDir +
                File.separator + "source" +
                File.separator + filename + ".cat");
        setDataFile(
                File.separator + EarthquakeCatalogPlugin.dataStoreDir +
                File.separator + "source" +
                File.separator + "data" +
                File.separator + filename + ".dat");
        
        // prompt for display name, citation, reference, and notes and abort on cancel
        
        ObjectInfoDialog oid;
        
//        if(owner instanceof EQCatalogGUI)
        /*	oid = ((EarthquakeCatalogPluginGUI)this.owner).getSourceInfoDialogSC(this);
//        else
//        	oid = ((MagFreqPluginGUI)this.owner).getSourceInfoDialog();
        
        if (displayAddCatInfo) {
        	oid.showInfo(this, "Add Catalog Information");
        	if (oid.windowWasCancelled()) return false;
        } else {
        }
*/
        // run minmax which sets extents fields
        runMinMax();
        
        //write xml attribute and object files; shortcircuit on error
        if (!(writeAttributeFile() && writeDataFile())) {
            setStatus(DataImport.ERROR_FILE_WRITE);
            return false;
        }
                
        setStatus(DataImport.IMPORT_END);
       // ((EarthquakeCatalogPluginGUI)this.owner).getSourceList().addCatalog(this);
        setInMemory(true);
        return true;
        
    }
    
    
    public void setNameCorrectly(ObjectInfoDialog oid) {
		// TODO Auto-generated method stub
    	name = oid.getDisplayName();
        this.setDisplayName(name);
        System.out.println("setting display name to: " + name);
        this.setCitation("");
        this.setReference("");
        this.setNotes("");
		
	}

	/**
     * parses individual catalog lines to values for data arrays
     */
    private void loadArrays(int idx, String eqdata) {
        StringTokenizer st = new StringTokenizer(eqdata);
        this.eq_id[idx]        = Integer.parseInt(st.nextToken());
        this.cal.set(Integer.parseInt(st.nextToken()),
                Integer.parseInt(st.nextToken())-1,
                Integer.parseInt(st.nextToken()),
                Integer.parseInt(st.nextToken()),
                Integer.parseInt(st.nextToken()),
                Integer.parseInt(st.nextToken()));
        this.eq_time[idx]      = this.cal.getTime();        
        this.eq_longitude[idx] = Float.parseFloat(st.nextToken());
        this.eq_latitude[idx]  = Float.parseFloat(st.nextToken());
        this.eq_depth[idx]     = Float.parseFloat(st.nextToken());
      //this finds the eq that have unrecorded depth
        if( this.eq_depth[idx] >=99.0)
        {
        	this.eq_depth[idx] = Prefs.Unrecorded_Depth_Flag;
        }
        this.eq_magnitude[idx] = Float.parseFloat(st.nextToken());
        
        if (getDataScope() == DATA_SCOPE_UNCERT || getDataScope() == DATA_SCOPE_UNCERT_FOCAL || getDataScope() == DATA_SCOPE_FOCAL_PROB) {
            this.eq_xy_error[idx]  = Float.parseFloat(st.nextToken());
            this.eq_z_error[idx]   = Float.parseFloat(st.nextToken());
        }
        if (getDataScope() == DATA_SCOPE_FOCAL || getDataScope() == DATA_SCOPE_UNCERT_FOCAL || getDataScope() == DATA_SCOPE_FOCAL_PROB) {
            this.eq_strike[idx]    = Integer.parseInt(st.nextToken());
            this.eq_dip[idx]       = Integer.parseInt(st.nextToken());
            this.eq_rake[idx]      = Integer.parseInt(st.nextToken());
        }
        if (getDataScope() == DATA_SCOPE_FOCAL_PROB) {
        	this.eq_nodal[idx]       = Integer.parseInt(st.nextToken());
        	this.eq_probability[idx] = Float.parseFloat(st.nextToken());
        }
    }
    /**
     * Sets default name of catalog
     */
    public void setName(String name){
    	this.name=name;
    }
}
