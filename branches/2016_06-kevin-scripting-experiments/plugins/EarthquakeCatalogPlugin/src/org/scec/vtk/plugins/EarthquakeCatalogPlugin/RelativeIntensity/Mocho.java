package org.scec.vtk.plugins.EarthquakeCatalogPlugin.RelativeIntensity;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * Mocho makes the Molchan diagrams.
 * @author Lauren Schenkman and Elisa Suarez
 */

//Mocho is the class that makes the Molchan diagrams in the Rel Intensity panel 

public class Mocho {
	private RelativeIntensityGUI owner;
	public Mocho(RelativeIntensityGUI owner){
		this.owner = owner;
	}
	public JPanel createGraph(float[][] data, String title, boolean weighted) {
		// this gets the binning data, and sets it to weighted unless said otherwise
		//String type = "map-area";
	/*	System.out.println("GRAPH!!");
		for(int i=0; i<data.length; i++){
			for(int j=0; j<data[0].length; j++){
				System.out.print(data[i][j]+" ");
			}
			System.out.println();
		}*/
		
		JFreeChart chart = createChart(createXYDataSet(data, weighted), weighted, title); //create new instance of chart that plugs in the above array of values into the method createChart
		
		return new ChartPanel(chart);
	
	}
	
	/* this gets the data from Molchan Tools and makes it into an array so that 
	 * it can be graphed using JFree Chart
	 * It converts the data into a matrix with 3 columns; nu, tau_one (not weighted), tau_two (weighted)
	 */
	
	private XYDataset createXYDataSet(float[][] data, boolean weighted) {
		//weighted = false;
		XYSeries series = new XYSeries("Molchan Trajectory");
		int i;
		int rows = data.length;
		if (!weighted){
			
			for(i=0; i < rows; i++){
				series.add(data[i][1], data[i][0]);
			
			}
		}
		else{
			for(i=0; i<rows; i++ ){
				series.add(data[i][2], data[i][0]);
				
			}
		}
		/*
		 * This sorts the data according whether it is weighted or unweighted
		 */
		
		XYSeries series2= getConfident();
		XYSeries seriesDiag = makeDiagonal();
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		
		
		dataset.addSeries(series);
		dataset.addSeries(series2);
		dataset.addSeries(seriesDiag);
		return dataset;
	}
	
	private XYSeries getConfident() {
		/*
		 * Makes the 98% Confidence Interval Line. It finds the number of 
		 * target earthquakes and graphs the calculated array from the Confidence
		 * Interval class 
		 */
		XYSeries series = new XYSeries("98% Confidence Interval");
		int i;
		int rows = owner.getTargetCatalog().getNumEvents();
		double[]nuForCI = new double[rows];
			for (i=1;i<=rows;i++){
				nuForCI[i-1]= ((double)rows-i)/((double)rows);
			
			}
		//int j;
		double[] tauArray = ConfidenceInterval.computeInterval(rows);
		int a;
			for(a=0; a<rows; a++ ){
			series.add(tauArray[a], nuForCI[a]);
			
		} 
			
		return series;	
	}
	private XYSeries makeDiagonal(){
		/*
		 * Makes the null hypothesis line. Is just a line going from (0,1) to (1,0)
		 */
		XYSeries series = new XYSeries("Null Hypothesis");
		int i;
		int rows = owner.getTargetCatalog().getNumEvents();
		//double[]diagonal = new double[rows];
		for (i=1; i<=rows;i++){
			series.add((double)i/rows, 1-((double)i/rows));
		}
		return series;
	}
	
	 
	
		private JFreeChart createChart(XYDataset dataset, boolean weighted, String title){
		//String title;
		JFreeChart chart;
		
	
		if(!weighted){
			chart = ChartFactory.createScatterPlot(
					title, 
					"Map-area fraction of space covered by alarm", 
					"Miss rate", 
					dataset, 
					PlotOrientation.VERTICAL,
					true,
					false,
					false);
		} 
		else {
			chart = ChartFactory.createScatterPlot(
					title, 
					"Intensity-weighted-area fraction of space covered by alarm", 
					"Miss rate", 
					dataset, 
					PlotOrientation.VERTICAL,
					true,
					false,
					false);
			//makes the titles according to if the data is weighted or unweighted
		}
		
		chart.setBackgroundPaint(Color.white);
		chart.getXYPlot().getDomainAxis().setRange(0, 1);
		chart.getXYPlot().getRangeAxis().setRange(0, 1);
		//makes it so the graph axes both go from 0 to 1
		chart.getTitle().setFont(new Font("Arial", Font.PLAIN, 13));
		XYPlot plot = (XYPlot)chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		
		Ellipse2D.Float circle = new Ellipse2D.Float(-5, -5,10,10);
		
		//sets chart characteristics
		
		XYItemRenderer r = plot.getRenderer();
		if(r instanceof XYLineAndShapeRenderer){
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setShape(circle);
			renderer.setShapesVisible(true);
			renderer.setLinesVisible(true);
			renderer.setShapesFilled(false);
			r.setSeriesVisibleInLegend(true);
			//draws circles to mark the points on the molchan diagrams
		}
		
		return chart;
	}
	
	
}



