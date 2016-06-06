package org.scec.vtk.grid;



public class GlobeLayout {
	
	protected double re = 6378.140;				// equatorial radius of the globe
	protected double rp = 6356.755;				// polar radius of the globe
	
	protected double topLat;
	protected double botLat;
	
	protected double leftLon;
	protected double rightLon;
	
	protected double maxDepth;				//(Maximum depth in kilometers)
	
	protected int numLatLinesWithSpacing;
	protected int numLonLinesWithSpacing;
	
	protected double latDegreeSpacing;
	protected double lonDegreeSpacing;
	
	protected int labelFontSize	= 25;
	protected int[] centerOfEarth = {0,0,0};
	
	protected String[] latTexts;
	protected String[] lonTexts;
	
	public double getTopLat(){
		return topLat;
	}
	public double getBotLat(){
		return botLat;
	}
	public double getLeftLon(){
		return leftLon;
	}
	public double getRightLon(){
		return rightLon;
	}
	
	public GlobeLayout(){
		this.topLat 	= 36;		this.botLat	 	= 32;
		this.leftLon 	= -122;		this.rightLon 	= -114;
		this.maxDepth	= 40;	// DO NOT JUST CHANGE THIS NUMBER. if you really need another depth, you must add it as
								// an option in the graticule plugin, and then LEAVE 40 AS THE DEFAULT.
								// If you change it, I will find you! :-)
		
		this.latDegreeSpacing 	= 1;	//*NOTE: 10.JUN.2004 - Not working unless this and lon value are equal (1,1||2,2 etc)
		this.lonDegreeSpacing 	= 1;
		
		commonSetup();
	}
	
	public GlobeLayout(int topLat, int botLat, int leftLon, int rightLon, double degreeSpacing)
	{
		this.topLat 	= topLat;		this.botLat	 	= botLat;
		this.leftLon 	= leftLon;		this.rightLon 	= rightLon;
		this.maxDepth	= 40;	// DO NOT JUST CHANGE THIS NUMBER. if you really need another depth, you must add it as
								// an option in the graticule plugin, and then LEAVE 40 AS THE DEFAULT.
								// If you change it, I will find you! :-)

		this.latDegreeSpacing 	= degreeSpacing;
		this.lonDegreeSpacing 	= degreeSpacing;

		commonSetup();
	}
	
	public GlobeLayout(ViewRange viewRange, double degreeSpacing)
	{
		this(viewRange.getUpperLatitude(), viewRange.getLowerLatitude(),
				viewRange.getLeftLongitude(), viewRange.getRightLongitude(), degreeSpacing);
	}

	private void commonSetup() {
		//Set the bottom latitude and right longitude to the smallest multiple of degreeSpacing that is greater or equal to their original values
		double lat;
		for(lat = topLat, numLatLinesWithSpacing=0; lat > botLat; lat -= latDegreeSpacing,numLatLinesWithSpacing++);
		this.botLat = lat;
		double lon;
		for(lon=leftLon, numLonLinesWithSpacing=0; lon < rightLon; lon += lonDegreeSpacing,numLonLinesWithSpacing++);
		this.rightLon = lon;
		labelFontSize = (int)(latDegreeSpacing*25);
		latTexts = new String[(int)Math.abs(topLat - botLat)];
		lonTexts = new String[(int)Math.abs(rightLon - leftLon)];
		populateStringArray(true,this.topLat);
		populateStringArray(false,this.leftLon);
	}
	
	private void populateStringArray(boolean lat, double startValue)
	{
		//NOTE: this method will only insert as many texts as can fit in the array
		//		(i.e.: it will not generate an out of bounds exception)
		double curValue = startValue;
		if(lat) {
			for(int i=0; i < latTexts.length;i++){latTexts[i] = Double.toString(curValue--);}
		}
		else {
			for(int i=0; i < lonTexts.length;i++){lonTexts[i] = Double.toString(curValue++);}
		}
		
	}
}
