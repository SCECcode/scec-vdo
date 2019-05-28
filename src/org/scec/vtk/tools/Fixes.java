package org.scec.vtk.tools;

import java.io.File;

public class Fixes {
	public static String fixDirectory(String dirName){
    	String str = "";
 
    	if( !dirName.contains("/")){
    		//Then the fileName is a windows path
    		return dirName.replace("\\", File.separator);
    	}else{
    		//The filename is a linux one
    		return dirName.replace("/", File.separator);
    	}
    	
    }
}
