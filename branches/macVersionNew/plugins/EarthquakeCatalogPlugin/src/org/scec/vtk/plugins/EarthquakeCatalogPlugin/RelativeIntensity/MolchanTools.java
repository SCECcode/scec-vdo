package org.scec.vtk.plugins.EarthquakeCatalogPlugin.RelativeIntensity;


public class MolchanTools{
    
    public MolchanTools() {
    }
    
    public float[][] getMolchanTrajectoryFromRIMap(int[][] riMap, int[][] targetEqks, boolean useMarginOfError, int rows, int columns){
        // Calculate the Molchan trajectory for the given RI map and target eqk catalog; each of these is provided by a 2D array that represents a lat-lon
        //   gridding of the study region.  riMap[][] contains the number of past earthquakes in each box, and targetEqks[][] contains the number
        //   of target eqks in each box.
        // The idea here is to take the map, choose a threshold; any box in riMap[][] with a value higher than the threshold is considered to be an alarm;
        //   any box below the threshold is not an alarm.  Given the derived alarm map, we compute the miss rate and fraction of alarm-space time covered
        //   by alarms.  This gives us one (tau,nu) point.  We repeat this process for all threshold values.  The maximum threshold should be set to the maximum
        //   number of events in any one box (max(riMap[][])) and, in order to make sure that we generate all possible alarm sets from the map, we'll use
        //   a threshold step of 1 and a minimum threshold of 0 (this minimum threshold is guaranteed to yield (tau,nu)=(1,0)).
        
        float[][] nu_tau_weightedTau; // this will hold the Molchan trajectory (tau, nu) points
        boolean[][] alarmMap; // this will be an alarm map derived from the riMap and the chosen threshold
        
        int minThreshold=0;
        int maxThreshold=0;
        int thresholdStep=1;
        
        // get the maximum number of events in any one box in riMap (this will be used as the maxThreshold)
        int numberOfRows = rows;
        int numberOfCols = columns;
        /*       int numberOfRows = riMap.length;
        int numberOfCols = riMap[0].length;*/
        for (int rowCounter=0;rowCounter<numberOfRows;rowCounter++){
            for (int colCounter=0;colCounter<numberOfCols;colCounter++){
                if (riMap[rowCounter][colCounter]>maxThreshold){
                    maxThreshold=riMap[rowCounter][colCounter];
                }
            }
        }
        
        // get the total number of target eqks
        int numberOfTargetEqks=0;
        for (int rowCounter=0;rowCounter<numberOfRows;rowCounter++){
            for (int colCounter=0;colCounter<numberOfCols;colCounter++){
                numberOfTargetEqks+=targetEqks[rowCounter][colCounter];
            }
        }
        
        // Allocate room to store (nu, tau, weighted_tau) values at each threshold step
        nu_tau_weightedTau = new float[maxThreshold+1][3];
        
        // step through the threshold values and generate Molchan trajectory
        for (int threshold = minThreshold;threshold<=maxThreshold;threshold+=thresholdStep){
            alarmMap = this.getAlarmMapFromRIMap(riMap,threshold, useMarginOfError, numberOfRows, numberOfCols); // given this threshold, determine which boxes are alarms
            nu_tau_weightedTau[threshold][0] = this.getNuFromAlarmMap(alarmMap, targetEqks, numberOfTargetEqks, numberOfRows, numberOfCols);
            nu_tau_weightedTau[threshold][1] = this.getTauFromAlarmMap(alarmMap, riMap, false, numberOfRows, numberOfCols); // get the map area tau
            nu_tau_weightedTau[threshold][2] = this.getTauFromAlarmMap(alarmMap, riMap, true, numberOfRows, numberOfCols); // get the intensity-weighted area tau
        }
        return nu_tau_weightedTau;
    }
    
    private boolean[][] getAlarmMapFromRIMap(int[][] riMap, float threshold, boolean useMarginOfError, int rows, int columns) {
        // Given the supplied riMap and the specified threshold, generate an alarm map.
        // The idea here is that if the value in an riMap box is greater than the threshold, it is an alarm and otherwise it is not.
        // One catch is that we may wish to enable a "margin of error" which means that if a target event occurs within one box-length of an alarm, this is 
        //   counted as a hit.  The fairest way to do this is the following: when we choose a threshold, we set an alarm in any box that exceeds the threshold 
        //   as well as its "nearest neighbors" (the 8 boxes surrounding it).  We do it this way to make sure the boxes also are counted in computing tau.

    	int numberOfRows = rows;
    	int numberOfCols = columns;    	
/*        int numberOfRows = riMap.length;
        int numberOfCols = riMap[0].length;*/
        boolean[][] alarmMap = new boolean[numberOfRows][numberOfCols];
        
        
        // initialize all boxes to non-alarms (falses)
        for (int rowCounter=0;rowCounter<numberOfRows;rowCounter++){
            for (int colCounter=0;colCounter<numberOfCols;colCounter++){
                alarmMap[rowCounter][colCounter]=false;
            }
        }
        
        // set alarms according to the threshold; if we're using a margin of error, also declare an alarm in surrounding boxes
        for (int rowCounter=0;rowCounter<numberOfRows;rowCounter++){
            for (int colCounter=0;colCounter<numberOfCols;colCounter++){
                if(riMap[rowCounter][colCounter]>threshold){
                    alarmMap[rowCounter][colCounter]=true;
                    
                    if (useMarginOfError){
                        // Now we declare an alarm in each neighboring box.
                        // B/c we might be at the edge of the study region or in a corner , we might create an ArrayIndexOutOfBoundsException.  We can
                        //   simply catch this exception and ignore it and move onto the next neighbor.
                        try {
                            alarmMap[rowCounter + 1][colCounter + 1] = true;
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                        try {
                            alarmMap[rowCounter + 1][colCounter - 1] = true;
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                        try {
                            alarmMap[rowCounter + 1][colCounter] = true;
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                        try {
                            alarmMap[rowCounter - 1][colCounter + 1] = true;
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                        try {
                            alarmMap[rowCounter - 1][colCounter - 1] = true;
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                        try {
                            alarmMap[rowCounter - 1][colCounter] = true;
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                        try {
                            alarmMap[rowCounter][colCounter + 1] = true;
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                        try {
                            alarmMap[rowCounter][colCounter - 1] = true;
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                    }
                }
            }
        }
        
        return alarmMap;
    }
    
    private float getTauFromAlarmMap(boolean[][] alarmMap, int[][] riMap, boolean useWeightedTau, int rows, int columns){
        // Calculate tau, the fraction of space covered by alarm, from the given alarm map.
        // If we're not using weighted tau, this is simply the number of alarms divided by the total number of boxes;
        // If we want to use intensity-weighted tau, each box will weigh as much as its corresponding RI value.
        
    	int numberOfRows = rows;
    	int numberOfCols = columns;
/*        int numberOfRows = alarmMap.length;
        int numberOfCols = alarmMap[0].length;*/
        
        // if we ARE NOT using weighted-tau, we simply count the number of boxes, count the number of alarms and divide the second by the first
        if (!useWeightedTau){
            int numberOfAlarms=0;
            int numberOfBoxes=0;
            for (int rowCounter=0;rowCounter<numberOfRows;rowCounter++){
                for (int colCounter=0;colCounter<numberOfCols;colCounter++){
                    numberOfBoxes++;
                    if (alarmMap[rowCounter][colCounter]){
                        numberOfAlarms++;
                    }
                }
            }
            float tau = (float)numberOfAlarms/(float)numberOfBoxes;
            return tau;
        }
        // if we ARE  using weighted-tau, each box is given the weight of its corresponding RI value.  So, the "number of boxes" is just the sum over the RI
        //    map and the "number of alarms" is the sum of the RI boxes corresponding to the alarm regions in alarmMap
        else{
            int numberOfAlarms=0;
            int numberOfBoxes=0;
            for (int rowCounter=0;rowCounter<numberOfRows;rowCounter++){
                for (int colCounter=0;colCounter<numberOfCols;colCounter++){
                    numberOfBoxes+=riMap[rowCounter][colCounter];
                    if (alarmMap[rowCounter][colCounter]){
                        numberOfAlarms+=riMap[rowCounter][colCounter];
                    }
                }
            }
            float tau = (float)numberOfAlarms/(float)numberOfBoxes;
            return tau;
        }
    }
    
    private float getNuFromAlarmMap(boolean[][] alarmMap, int[][] targetEqks, int numberOfTargetEqks, int rows, int columns){
        // Calculate nu, the miss rate, from the given alarm map and the target eqk map
        // To do this, we pick the non-alarm boxes and sum the corresponding number of target eqks in these boxes.  This gives us the total number of misses.
        // Then nu is just (the number of misses/number of target eqks).
        
    	int numberOfRows = rows;
    	int numberOfCols = columns;
/*        int numberOfRows = alarmMap.length;
        int numberOfCols = alarmMap[0].length;*/
        
        int numberOfMisses=0;
        for (int rowCounter=0;rowCounter<numberOfRows;rowCounter++){
            for (int colCounter=0;colCounter<numberOfCols;colCounter++){
                if (!alarmMap[rowCounter][colCounter]){
                    numberOfMisses+=targetEqks[rowCounter][colCounter];
                }
            }
        }
        float nu = (float)numberOfMisses/(float)numberOfTargetEqks;
        return nu;
        
    }    

}