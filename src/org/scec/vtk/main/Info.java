package org.scec.vtk.main;

import java.awt.Color;

public class Info {
	private static MainGUI mainGUI;
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
}
