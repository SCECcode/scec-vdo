package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import javax.swing.JPanel;

import org.jdom.Element;

/**
 * This interface guarantees some basic access to all types of filters and
 * provides some common error messages.
 *
 * Created on Feb 28, 2005
 * 
 * @author P. Powers
 * @version $Id: CatalogFilter.java 3163 2009-07-14 21:45:20Z armstrong $
 */
public interface CatalogFilter {

    /** Generic filter error message. */
    public static final String ERROR = "Filter error";

    /** Empty result set message */
    public static final String ERROR_NO_RESULT = "Filter produced no results";
    
    /** Loading source file message */
    public static final String FILTER_LOADING_SRC = "Filter: loading source catalog";
    
    /** Processing filter message */
    public static final String FILTER_PROCESSING = "Filter: processing";
    
    /** Filter completed message */
    public static final String FILTER_FINISHED = "Filter finished";

    /**
     * Processes a given <code>CatalogAccessor</code> against a filter. Returns
     * an array of valid event indices.
     * 
     * @param catalog to process
     * @return array of event indices 
     */
    public int[] process(CatalogAccessor catalog);
    
    /**
     * Returns the <code>JPanel</code> associated with a filter.
     * 
     * @return the panel
     */
    public JPanel getPanel();
    
    /**
     * Returns a JDOM filter <code>Element</code> that can be used to store filter
     * settings or a catalogs filter history.
     * 
     * @return an <code>Element</code> that describes filter constraints
     */
    public Element getFilterAttributes();
}
