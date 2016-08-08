package org.scec.vtk.main;

import java.awt.Color;

import vtk.vtkTextActor;

public class Info {
	private static MainGUI mainGUI;
	public static vtkTextActor textDisplayActor= new vtkTextActor();
	public static final double rgbMax = 255.0;
	public static MainGUI getMainGUI() {
		 
		return mainGUI;
		
	}

	public static void setMainGUI(MainGUI mgui) {
		// TODO Auto-generated method stub
		mainGUI = mgui;
	}
	public static double[] convertColor(Color tempColor)
	{
		double[] color = new double[3];
		color[0] = tempColor.getRed()/rgbMax;
		color[1] = tempColor.getGreen()/rgbMax;
		color[2] = tempColor.getBlue()/rgbMax;
		return color;
	}
	public static Color convertColor(double[] tempColor)
	{
		return new Color((float)(tempColor[0]/rgbMax), (float)(tempColor[1]/rgbMax), (float)(tempColor[2]/rgbMax));
	}
	
	public static Color getBackgroundColor() {
		return convertColor(MainGUI.getRenderWindow().getRenderer().GetBackground());
	}
}
