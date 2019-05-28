package org.scec.vtk.main;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class pngRenderer {
	
	//Makes an image of the map portion of the screen
	public static void pngRender(File file, MainGUI maingui) throws IOException{
		BufferedImage image = new BufferedImage(MainGUI.getRenderWindow().getComponent().getWidth(), 
				MainGUI.getRenderWindow().getComponent().getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = image.createGraphics();

		RenderingHints rh = new RenderingHints(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics2D.setRenderingHints(rh);

		maingui.getRenderWindow().getComponent().paint(graphics2D);
		ImageIO.write(image,"png", file);
	}
}