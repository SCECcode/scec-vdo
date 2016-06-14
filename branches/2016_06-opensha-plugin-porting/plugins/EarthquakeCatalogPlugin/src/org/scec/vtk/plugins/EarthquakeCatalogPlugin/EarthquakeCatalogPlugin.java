package org.scec.vtk.plugins.EarthquakeCatalogPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.PluginInfo;

import vtk.vtkActor;

public class EarthquakeCatalogPlugin extends ActionPlugin {

	
	EarthquakeCatalogPluginGUI f3DGui;
    private boolean guidisplayed = false;
	private PluginInfo metadata;
	
	
    /**
     * Static field for location of fault data in <i>ScecVideo</i> data library.
     */
    public static String dataStoreDir = "EQCatalogStore";
       
    /**
     * Constructs a new <code>Fault3DPlugin</code> with appropriate metadata.
     */
    public EarthquakeCatalogPlugin() {
        //this.metadata = new PluginInfo("Earthquake Catalog Plugin", "Earthquake Catalog Plugin", "P. Powers", "0.1", "EQCatalog");
        
    }
    
    /**
     * Overrides createGUI() in ActionPlugin
     * @see org.scec.geo3d.plugins.ActionPlugin#createGUI()
     */
    public JPanel createGUI() {
  
		f3DGui = new EarthquakeCatalogPluginGUI(this);
		
    	guidisplayed = true;
    	setActors();
        return f3DGui;
    }
    public void unload()
	{

       /* Info.getMainGUI().removeActors(f3DGui.getMasterFaultBranchGroup());
        f3DGui.getMasterFaultBranchGroup().clear();
		super.unload();
		f3DGui=null;*/
	}
    
    public void setActors()
    {
    	//ArrayList<vtkActor> allCFMActors = new ArrayList<vtkActor>();
    	if(guidisplayed){
  
    	//List loadedRows = f3DGui.faultTable.getLibraryModel().getAllObjects();

    		/*for(int i = 0; i < loadedRows.size(); i++)
		{
			FaultAccessor fa = (FaultAccessor)loadedRows.get(i);
			fa.readDataFile();
			//allCFMActors.add(fa.getFaultBranch());
			fa.setFaultBranch(fa.getFaultBranch());
		}
		if(loadedRows.size()>0)
			Info.getMainGUI().updateActors(getActors());*/
    	}
		
    }
    public ArrayList<vtkActor> getActors()
    {
    	ArrayList<vtkActor> actorMasterFaultBranchGroup = new ArrayList<>();//= f3DGui.getMasterFaultBranchGroup();
    	/*if(actorMasterFaultBranchGroup == null)
    		actorMasterFaultBranchGroup = new ArrayList<vtkActor>();*/
    	return actorMasterFaultBranchGroup;
    }
}
