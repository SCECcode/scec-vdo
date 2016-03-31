package org.scec.vtk.plugins.utils.components;

import javax.swing.JFileChooser;

import org.scec.vtk.tools.filters.ExampleFileFilter;


public class ImageFileChooser extends JFileChooser
{
	private static final long serialVersionUID = 1L;
	
	ExampleFileFilter filter;
    
	public ImageFileChooser()
	{
		super();
		filter = new ExampleFileFilter();
		filter.addExtension("jpg");
	    filter.addExtension("gif");
	    filter.addExtension("bmp");
	    filter.addExtension("tiff");
	    filter.addExtension("png");
	    filter.setDescription("Image Files");
	    this.setFileFilter(filter);
	    this.setVisible(true);
	}
	
}