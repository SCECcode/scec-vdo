package org.scec.vtk.plugins.CommunityfaultModelPlugin.components;

import java.io.File;
import java.util.ArrayList;

import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.tools.picking.PickEnabledActor;

import vtk.vtkActor;

public class Fault3D extends FaultAccessor {
    
    /**
     * Constructs a <code>Fault3D</code> from a given source file. Source is a *.flt
     * file that contains fault attributes in XML format.
     * 
     * @param file the source file to be read
     */
    public Fault3D(File file) {
        super();
        readAttributeFile(file);
    }    
    
    /**
     * Creates a new empty Fault3D.
     */
    public Fault3D() {
        super();
        newDocument();
    }


        
}

