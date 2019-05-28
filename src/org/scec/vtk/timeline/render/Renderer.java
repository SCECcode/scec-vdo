package org.scec.vtk.timeline.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;

public interface Renderer {

	/**
	 * 
	 * @param outputFile output file
	 * @param width width in pixels
	 * @param height height in pixels
	 * @param fps frames per second
	 * @throws IOException
	 */
	public void init(File outputFile, int width, int height, double fps, int count) throws IOException;

	/**
	 * Process the current image in a separate thread
	 * @param img
	 */
	public void processFrame(BufferedImage img) throws IOException;

	public void finalize() throws IOException;
	
	public JComponent getSettingsComponent();
	
	public void setRenderStatusListener(RenderStatusListener l);
	
	/**
	 * Name of this renderer. Should also overwrite toString to return this
	 * @return
	 */
	public String getName();
	
	/**
	 * @return filename extension, without a period
	 */
	public String getExtension();

}