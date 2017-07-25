package org.scec.vtk.main;

import java.io.File;

public class Help {
	  public Help(){
		  
		  String url;
		  if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Political Boundaries")) {
				url = "http://scecvdo.usc.edu/manual/UserGuide.html#PoliticalBoundaries";
				}
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Graticule")) {
				url = "http://scecvdo.usc.edu/manual/UserGuide.html#Graticule";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Drawing Tools")) {
				url = "http://scecvdo.usc.edu/manual/UserGuide.html#DrawingTools";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("ShakeMap Plugin")) {
				url = "http://scecvdo.usc.edu/manual/UserGuide.html#ShakeMap";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Surface Plugin")) {
				url = "http://scecvdo.usc.edu/manual/UserGuide.html#Surface";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Earthquake Catalog Plugin")) {
				url = "http://scecvdo.usc.edu/manual/UserGuide.html#EarthquakeCatalog";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Legend Plugin")) {
				url = "http://scecvdo.usc.edu/manual/UserGuide.html#Legend";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Earthquake Simulators")) {
				url = "http://scecvdo.usc.edu/manual/UserGuide.html#Simulators";
			  }
			  else{
					url = "http://scecvdo.usc.edu/manual/UserGuide.html";
			  }
		  
		String os = System.getProperty("os.name").toLowerCase();
	    Runtime rt = Runtime.getRuntime();
		try{
		    if (os.indexOf( "win" ) >= 0) {
		        rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);
		    }
		    else if (os.indexOf( "mac" ) >= 0) {
		    	
		        rt.exec( "open " + url);
	            }
		    else if (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0) {
		        String[] browsers = {"firefox", "mozilla", "epiphany", "konqueror",
		       			             "netscape","opera","links","lynx"};
		        StringBuffer cmd = new StringBuffer();
		        for (int i=0; i<browsers.length; i++)
		            cmd.append( (i==0  ? "" : " || " ) + browsers[i] +" \"" + url + "\" ");
		        rt.exec(new String[] { "sh", "-c", cmd.toString() });
		        } 
	            else {
	                return;
	           }
	       }
		catch (Exception e){
		    return;
	       }
	      return;
	}
}