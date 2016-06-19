package org.scec.vtk.drawingTools;

import java.util.ArrayList;

import org.scec.vtk.plugins.utils.AbstractDataAccessor;

import vtk.vtkObject;

public class DrawingTool extends AbstractDataAccessor{

	private static ArrayList<vtkObject> masterDrawingToolBranchGroup = new ArrayList<vtkObject>();
	double latitude, longitude,  altitude;
	String textString;
	DisplayAttributes displayAttributes;
	
	public DrawingTool(double latitude, double longitude, double altitude, String textString, DisplayAttributes displayAttributes) {
		// TODO Auto-generated constructor stub
		this.latitude=latitude;
		this.longitude=longitude;
		this.altitude = altitude;
		this.textString = textString;
		this.displayAttributes = displayAttributes;
	}
	public DrawingTool() {
	}

	public double getLatitude()
	{
		return this.latitude;
	}
	public double getLongitude()
	{
		return this.longitude;
	}
	public double getaltitude()
	{
		return this.altitude;
	}
	public String getTextString()
	{
		return this.textString;
	}
	public DisplayAttributes getDisplayAttributes()
	{
		return this.displayAttributes;
	}
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


}
