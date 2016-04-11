package org.scec.vtk.plugins.CommunityfaultModelPlugin;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.jdom.Element;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.ActionPlugin;
import org.scec.vtk.plugins.PluginInfo;

import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.FaultAccessor;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.components.FaultTableModel;
import vtk.vtkActor;

/**
 * <i>ScecVideo</i> plugin for the display of 3-dimensional fault representations.
 * 
 * Created on Jan 30, 2005
 * 
 * Status: functional
 * Comments:
 * <ul>
 *      <li>has list of todos (see Peter)</li>
 * </ul>
 * 
 * @author P. Powers
 * @version $Id: Fault3DPlugin.java 2071 2008-07-03 15:39:24Z rberti $
 */
public class CommunityFaultModelPlugin extends ActionPlugin {
    
    // TODO  can pluginInfo be made static and then use the plugin name as the data 
    // repository directory name eg "Fault3DPlugin"
    CommunityFaultModelGUI f3DGui;
    private boolean guidisplayed = false;
	
	
    /**
     * Static field for location of fault data in <i>ScecVideo</i> data library.
     */
    public static String dataStoreDir = "Fault3DStore";
       
    /**
     * Constructs a new <code>Fault3DPlugin</code> with appropriate metadata.
     */
    public CommunityFaultModelPlugin() {
        //this.metadata = new PluginInfo("Community Fault Model (CFM)", "Community Fault Model (CFM)", "P. Powers", "0.1", "Faults");
        
    }
    
    /**
     * Overrides createGUI() in ActionPlugin
     * @see org.scec.geo3d.plugins.ActionPlugin#createGUI()
     */
    public JPanel createGUI() {
    	f3DGui = new CommunityFaultModelGUI();
    	guidisplayed = true;
    	setActors();
        return f3DGui;
    }
    public void unload()
	{

        Info.getMainGUI().removeActors(f3DGui.getMasterFaultBranchGroup());
        f3DGui.getMasterFaultBranchGroup().clear();
		super.unload();
		f3DGui=null;
	}
    
    public void setActors()
    {
    	//ArrayList<vtkActor> allCFMActors = new ArrayList<vtkActor>();
    	if(guidisplayed){
  
    	List loadedRows = f3DGui.faultTable.getLibraryModel().getAllObjects();

		for(int i = 0; i < loadedRows.size(); i++)
		{
			FaultAccessor fa = (FaultAccessor)loadedRows.get(i);
			fa.readDataFile();
			//allCFMActors.add(fa.getFaultBranch());
			fa.setFaultBranch(fa.getFaultBranch());
		}
		if(loadedRows.size()>0)
			Info.getMainGUI().updateActors(getActors());
    	}
		
    }
    public ArrayList<vtkActor> getActors()
    {
    	ArrayList<vtkActor> actorMasterFaultBranchGroup = f3DGui.getMasterFaultBranchGroup();
    	if(actorMasterFaultBranchGroup == null)
    		actorMasterFaultBranchGroup = new ArrayList<vtkActor>();
    	return actorMasterFaultBranchGroup;
    }
    public Element getState()
    {
    	Element root = new Element("fault3DPlugin");
    	if(guidisplayed){
    		root.setAttribute("displayed","true");
    		List loadedRows = f3DGui.faultTable.getLibraryModel().getAllObjects();

    		for(int i = 0; i < loadedRows.size(); i++)
    		{
    			Element fault = new Element("fault");
    			FaultAccessor fa = (FaultAccessor)loadedRows.get(i);
    			//FaultTableModel ftm = f3DGui.faultTable.getLibraryModel().getLoadedObjects();
    			if(fa.isInMemory())
    			{
    				fault.setAttribute("loaded", "true");
    			}
    			else
    			{
    				fault.setAttribute("loaded", "false");
    			}
    			if(fa.isDisplayed())
    			{
    				fault.setAttribute("displayed", "true");
    			}
    			else
    			{
    				fault.setAttribute("displayed", "false");
    			}
    			Color color = fa.getColor();
    			if(color != null)
    			{
    				int rgb = color.getRGB();
    				String rgbColor = String.valueOf(rgb);
    				fault.setAttribute("color", rgbColor);
    			}else{
    				fault.setAttribute("color", "NULL");
    			}
    			//Gets mesh state of fault
    			int meshState = fa.getMeshState();
    			String meshStateString = String.valueOf(meshState);
    			fault.setAttribute("meshState", meshStateString);

    			fault.setText(fa.getDisplayName());

    			root.addContent(fault);
    		}
    	}else{
    		root.setAttribute("displayed","false");
    	}

    	return root;
    }
    
    public void setState(Element e)
    {
    	Element root = e.getChild("fault3DPlugin");
    	if(root.getAttributeValue("displayed").equals("true")){
    		ListSelectionModel lsm = f3DGui.faultTable.getSelectionModel();
    		FaultTableModel ftm = f3DGui.faultTable.getLibraryModel();

    		int numChildren = root.getContentSize();

    		for(int i = 0; i < numChildren; i++)
    		{
    			Element fault = root.getChild("fault");

    			if(fault != null)
    			{
    				int row = -1;

    				for(int j = 0; j < ftm.getRowCount(); j++)
    				{
    					if(ftm.getObjectAtRow(j).getDisplayName().equals(fault.getText()))
    					{
    						row = j;
    						break;
    					}
    				}    		
    				if(row >= 0)
    				{
    					lsm.setSelectionInterval(row, row);
    					if(fault.getAttributeValue("displayed").equals("true"))
    					{
    						ActionEvent e2 = new ActionEvent(f3DGui.showFaultsButton, 0, f3DGui.showFaultsButton.toString());
    						f3DGui.actionPerformed(e2);
    					}
    					if(!fault.getAttributeValue("color").equals(null))
    					{
    						String newColor = fault.getAttributeValue("color");
    						int rgb = Integer.parseInt(newColor);
    						Color c = new Color(rgb);
    						ftm.setColorForRow(c, i);
    					}
    					if(!fault.getAttributeValue("meshState").equals(null))
    					{
    						String meshStateString = fault.getAttributeValue("meshState");
    						int meshState = Integer.parseInt(meshStateString);
    						ftm.setMeshStateForRow(meshState, i);
    					}

    				}

    				root.removeChild("fault");
    			}
    		}
    	}
    }    
    
    /*public void setClickableEnabled(boolean enable){
    	if(f3DGui!=null) f3DGui.setPickable(enable);
    }*/
}
