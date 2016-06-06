package org.scec.vtk.drawingTools;

import java.io.File;
import java.util.ArrayList;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.utils.AbstractDataAccessor;
import org.scec.vtk.plugins.utils.DataAccessor;
import org.scec.vtk.tools.Transform;

import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkTextActor3D;
import vtk.vtkVectorText;

public class DrawingTool extends AbstractDataAccessor{

	private static ArrayList<vtkActor> masterDrawingToolBranchGroup = new ArrayList<vtkActor>();
	private DrawingToolsTableModel drawingTooltablemodel = new DrawingToolsTableModel();
	private AbstractDataAccessor drawingToolObj; 
	public DrawingTool(double d, double e, double f, String string, DisplayAttributes displayAttributes) {
		// TODO Auto-generated constructor stub
	}
	public DrawingTool()
	{}
	/*@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean readDataFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean writeDataFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean readAttributeFile(File file) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean writeAttributeFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setInMemory(boolean load) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInMemory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDisplayed(boolean show) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDisplayName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public File getAttributeFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttributeFileLibPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getDataFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCitation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCitation(String citation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReference(String reference) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getNotes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNotes(String notes) {
		// TODO Auto-generated method stub
		
	}*/
	 public void setMasterFaultBranchGroup(ArrayList<vtkActor> masterFaultBranchGroup) 
	    {
	    	this.masterDrawingToolBranchGroup = masterFaultBranchGroup;
	    }
	    public static ArrayList<vtkActor> getMasterFaultBranchGroup() 
	    {
	    	return masterDrawingToolBranchGroup;
	    }

	    public ArrayList addDrawingTool(){
	    	 //drawingToolObj.newDocument(); 
	    	 String text = "test text";
	    	 ArrayList a = drawingTooltablemodel.getAllObjects();
	    	 setDisplayName(text +" -"+ Integer.toString(a.size()+1));
	    	 //drawingToolObj.setInMemory(true);
	    	ArrayList newObjects = new ArrayList<>();
	    	vtkVectorText newText = new vtkVectorText();
	    	newText.SetText(text);
	    	 //newText.GetTextProperty().SetFontSize ( 12 );
	    	// Create a mapper and actor
	    	  vtkPolyDataMapper mapper =new vtkPolyDataMapper();
	    	  mapper.SetInputConnection(newText.GetOutputPort());
	    	  
	    	  vtkActor actor = new vtkActor();
	    	  actor.SetMapper(mapper);
	    	  actor.GetProperty().SetColor(1.0, 0.0, 0.0);
	    	  actor.SetScale(30,30,30);
	    	  actor.RotateY(90);
	    	  actor.RotateZ(90);
	    	  actor.RotateX(30);
	    	 double[] pt= {Transform.calcRadius(37),37,-120};
	    	 actor.SetPosition( Transform.customTransform(pt));
	    	 actor.GetProperty().SetColor (1,0,0);
	    	 newObjects.add(this);
	    	 getMasterFaultBranchGroup().add(actor);
	    	 Info.getMainGUI().updateActors(getMasterFaultBranchGroup());
			return newObjects;
	    	 
	    }
}
