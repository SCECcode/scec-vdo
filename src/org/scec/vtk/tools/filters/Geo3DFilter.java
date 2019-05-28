package org.scec.vtk.tools.filters;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
* The purpose of this class is to ensure that our choosers only show files of extension
* types that Geo3d can handle.
*
* @author Wayne Manselle
*/

public class Geo3DFilter extends FileFilter
{
	public final static String xml = "xml";
	 
	  /**
	   * This method is inherited from the abstract FileFilter class.  In the case of 
	   * the Geo3DFilter, it returns true only if the files in question are directories
	   * or .xml files.
	   * 
	   * @param f The File that is being analyzed
	   */
	  public boolean accept(File f)
	  {
	  	 if(f.isDirectory()) { 
	  	 	return true;
	  	 }
	  	 String fileExt = getExtension(f);
	  	 if (fileExt != null) {
	  	 	if (fileExt.equals(xml)) {
	  	 		return true;
	  	 	} else {
	  	 		return false;
	  	 	}
	  	 } else {
	  	 	return false;
	  	 }
	  }
	  
	  /**
	   * This method is inherited from the abstract FileFilter.  
	   * It returns the description of our Geo3DFilter.
	   * 
	   * @return A string description of the file filter
	   */
	  public String getDescription() {
	  	return "SCEC-VDO XML Files";
	  }
	
	/**
	 * This method files the extension of a given file and 
	 * returns it as a String.
	 * @param f The File to be analyzed
	 * @return a string consisting of the File's extension
	 */
	private String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}


}