package org.scec.vtk.plugins.utils;

/**
 * Basic interface for <i>ScecVideo</i> data importing. At present this class
 * only provides several static fields for import status messages.
 *
 * Created on Feb 14, 2005
 * 
 * @author P. Powers
 * @version $Id: DataImport.java 20 2005-05-04 19:44:40Z pmpowers $
 */
public interface DataImport {

    /** Error initializing import process message. */
    public static final String ERROR_IMPORT_INIT = "Error: Import initialization problem";

    /** Error loading <i>ScecVideo</i> object message */
    public static final String ERROR_OBJECT_LOAD = "Error: Failed to load object";

    /** Error reading file message. */
    public static final String ERROR_FILE_READ = "Error: File read problem";

    /** Error writing file message. */
    public static final String ERROR_FILE_WRITE = "Error: File write problem";
    
    /** Incorrect file format message. */
    public static final String ERROR_FILE_FORMAT = "Error: Incorrect file format";

    /** Import started message. */
    public static final String IMPORT_START = "Importing file(s)";
    
    /** Import complete message. */
    public static final String IMPORT_END = "Import complete";
    
    /** Import cancelled message. */
    public static final String IMPORT_CANCEL = "Import cancelled";
    
    /** Import failed message. */
    public static final String IMPORT_FAILED = "Import failed: ";
    
}

