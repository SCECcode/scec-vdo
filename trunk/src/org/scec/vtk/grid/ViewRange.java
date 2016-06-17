package org.scec.vtk.grid;


public class ViewRange {
	
	int lowerLatitude;
	int upperLatitude;
	int leftLongitude;
	int rightLongitude;
	boolean preset = true;
	
	public ViewRange()
	{
		this(32, 43, -125, -114);
	}

	public ViewRange(int lowerLatitude, int upperLatitude, int leftLongitude, int rightLongitude) 
	{
		this.lowerLatitude = lowerLatitude;
		this.upperLatitude = upperLatitude;
		this.leftLongitude = leftLongitude;
		this.rightLongitude = rightLongitude;
	}

	public String getLowerLatitudeAsString() 
	{
		return String.valueOf(this.lowerLatitude);
	}

	public String getUpperLatitudeAsString() 
	{
		return String.valueOf(this.upperLatitude);
	}

	public String getLeftLongitudeAsString() 
	{
		return String.valueOf(this.leftLongitude);
	}

	public String getRightLongitudeAsString() 
	{
		return String.valueOf(this.rightLongitude);
	}

	public int getLowerLatitude() {
		return this.lowerLatitude;
	}
	
	public int getUpperLatitude() {
		return this.upperLatitude;
	}

	public int getLeftLongitude() {
		return this.leftLongitude;
	}
	
	public int getRightLongitude() {
		return this.rightLongitude;
	}
	
	public void setLowerLatitude(int lowerLatitude) {
		this.lowerLatitude = lowerLatitude;
	}
	
	public void setUpperLatitude(int upperLatitude) {
		this.upperLatitude = upperLatitude;
	}

	public void setLeftLongitude(int leftLongitude) {
		this.leftLongitude = leftLongitude;
	}
	
	public void setRightLongitude(int rightLongitude) {
		this.rightLongitude = rightLongitude;
	}
	
	public boolean isPreset() {
		return preset;
	}
	
	public void setPreset(boolean preset) {
		this.preset = preset;
	}

}
