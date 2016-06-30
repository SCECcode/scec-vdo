package org.scec.vtk.plugins.EarthquakeCatalogPlugin.RelativeIntensity;

/*
 * This is the class that actually takes lat/lon region the user entered, divides
 * it into bins, and then counts the number of earthquakes in these separate bins.
 */

import java.awt.Component;

import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;


public class RelativeIntensity {
	//private Component parent;
	private double maxLat, minLat, maxLon, minLon;
	private int[][] riGrid; 
	private int[][] targetGrid;
	private double sizeOfBin;
	private int numOfBinsLat, numOfBinsLon;
	private EQCatalog sourceCatalog, targetCatalog;
	//private ArrayList sourceCat, targetCat;
	//private int[] sourceFilter, targetFilter;
	private RelativeIntensityGUI gui;
	private MolchanTools mt;
	private boolean useMarginOfError;
	//private double sMinMag, tMinMag;
	private boolean gridExists=false;
	private boolean gridIsOn=false;
	//private TarImage ti;
	
	public RelativeIntensity(Component parent){
		//this.parent = parent;
		gui = new RelativeIntensityGUI(this, parent);
		mt = new MolchanTools();
		useMarginOfError = false;
	}
	
	public RelativeIntensityGUI getGUI(){
		return gui;
	}
	
	public void plotEQs(){
		// Get input values from GUI
		maxLat = Double.parseDouble(gui.getMaxLat());
		minLat = Double.parseDouble(gui.getMinLat());
		maxLon = Double.parseDouble(gui.getMaxLon());
		minLon = Double.parseDouble(gui.getMinLon());
		sizeOfBin = Double.parseDouble(gui.getBinDimension());
		
		// Assign the correct catalogs
		sourceCatalog = gui.getSourceCatalog();
		targetCatalog = gui.getTargetCatalog();
			
		// Calculate number of bins 
		numOfBinsLat = (int)Math.round((maxLat - minLat)/sizeOfBin);
		numOfBinsLon = (int)Math.round((maxLon - minLon)/sizeOfBin);
		
		// Put source and target earthquakes into their coresponding bin
		riGrid = binEQs(sourceCatalog);
		targetGrid = binEQs(targetCatalog);

		displayGrid();
	}

	public int[][] binEQs(EQCatalog catalog){
		int binNumLat, binNumLon;
		int i;

		int[][] grid = new int[numOfBinsLat+1][numOfBinsLon+1];

		for(i=0; i<catalog.getNumEvents(); i++){
			// Check if any of the earthquakes fall outside of the lat or lon boundries
			if(!(catalog.getEq_latitude(i)<minLat || 
				 catalog.getEq_longitude(i)<minLon || 
				 catalog.getEq_latitude(i)>maxLat || 
				 catalog.getEq_longitude(i)>maxLon)){
				
				// Turn the lat and lon into an integer value
				int lat_translated = (int)Math.floor(catalog.getEq_latitude(i) * (1.0f/sizeOfBin));
				int lon_translated = (int)Math.floor(catalog.getEq_longitude(i) * (1.0f/sizeOfBin));
				
				// Turn the minLat and minLon into an interger value
				int minLat_translated = (int)(minLat * 1.0f / sizeOfBin);
				int minLon_translated = (int)(minLon * 1.0f / sizeOfBin);
				
				// Subtract the lat and lon from the minimums to get the bin location
				binNumLat = lat_translated - minLat_translated;
				binNumLon = lon_translated - minLon_translated;
				
				grid[binNumLat][binNumLon]++;
			}
		}
		
		return grid;
	}
	
	/**
	 * This function is used to pass values into the MolchanTools, which returns an MolchanTrajectory.
	 * @return 2D float array
	 */
	public float[][] getMolchanTrajectory(){
		float[][] results;
		
		results = mt.getMolchanTrajectoryFromRIMap(riGrid, targetGrid, useMarginOfError, (int)numOfBinsLat, (int)numOfBinsLon);
		return results;
	}

	/**
	 * Returns Relative Intensity Grid
	 * @return 2D int array
	 */
	public int[][] getRiGrid(){
		return riGrid;
	}
	
	/**
	 * Returns the target source grid
	 * @return 2D int array
	 */
	public int[][] getTargetGrid(){
		return targetGrid;
	}

	/**
	 * Prints the values in the RI Grid
	 *
	 */
	public void printGrid() {
		for(int i=0;i<numOfBinsLat;i++){
			for(int j=0; j<numOfBinsLon; j++){
				System.out.print(riGrid[i][j]+" ");
			}
			System.out.println("");
		}
	}

	/**
	 * Creates a new ShowGrid object to display the binned source catalog
	 *
	 */
	public void displayGrid(){
		gridExists=true;
		gridIsOn=true;
	//	gridDisplay = new ShowGrid(riGrid, (int)numOfBinsLat, (int)numOfBinsLon, minLat, minLon, sizeOfBin);
		//Info.get.getPluginBranchGroup().addChild(gridDisplay);
		gui.binningOnBox.setSelected(true);
		
	}

	/**
	 * Displays the grid if it already exists
	 *
	 */
	public void showGrid() {
		if(gridExists && !gridIsOn);{
			//Geo3dInfo.getPluginBranchGroup().addChild(gridDisplay);
			gridIsOn=true;
		}
	}
	
	/**
	 * Displays the grid if it already exists with a maximum altitude specified
	 * @param maxAlt
	 */
	public void showGrid(int maxAlt) {
		if(gridExists && !gridIsOn);{
			//gridDisplay.changeAlt(maxAlt);
			//Geo3dInfo.getPluginBranchGroup().addChild(gridDisplay);
			gridIsOn=true;
		}
	}

	/**
	 * Turns off grid
	 *
	 */
	public void hideGrid() {
		if(gridExists && gridIsOn){
			//gridDisplay.detach();
			gridIsOn=false;
		}
	}
	
	/**
	 * Sets whether margin of error is used for the molchan tools
	 * @param selected
	 */
	public void setUseMarginOfError(boolean selected)
	{
		useMarginOfError = selected;
	}
	
	/**
	 * Displays target earthquakes using a star
	 *
	 */
	public void showTargetEQs() {
		int len = targetCatalog.getNumEvents();
		double[] lat = new double[len];
		double[] lon = new double[len];
		for(int i=0; i<len; i++){
			lat[i]=targetCatalog.getEq_latitude(i);
			lon[i]=targetCatalog.getEq_longitude(i);
		}
		//targetEQs.add(new TarImage(targetCatalog.getEq_latitude(i),targetCatalog.getEq_longitude(i)));
		//ti = new TarImage(lat,lon,len);
	}

	/**
	 * Turns off the target earthquake display
	 *
	 */
	public void hideTargetEQs() {
		//if (ti != null)
			//ti.removeImages();
		//else
			System.out.println("Error! Trying to hide taret EQs but they're not displayed!");
	}
}


