package org.scec.vtk.main;

import java.io.File;

public class Help {
	  public Help(){
		  
		  String url;
		  if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Political Boundaries")) {
				File file = new File("userguide_HTML/userGuide.html");
				String filepath = (file.getAbsolutePath());
				String url0 = filepath.replace("\\", "/");
				url = "file:///" + url0 + "#PoliticalBoundaries";
		  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Graticule")) {
				File file = new File("userguide_HTML/userGuide.html");
				String filepath = (file.getAbsolutePath());
				String url0 = filepath.replace("\\", "/");
				url = "file:///" + url0 + "#Graticule";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Drawing Tools")) {
				File file = new File("userguide_HTML/userGuide.html");
				String filepath = (file.getAbsolutePath());
				String url0 = filepath.replace("\\", "/");
				url = "file:///" + url0 + "#DrawingTools";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("ShakeMap Plugin")) {
				File file = new File("userguide_HTML/userGuide.html");
				String filepath = (file.getAbsolutePath());
				String url0 = filepath.replace("\\", "/");
				url = "file:///" + url0 + "#ShakeMap";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Surface Plugin")) {
				File file = new File("userguide_HTML/userGuide.html");
				String filepath = (file.getAbsolutePath());
				String url0 = filepath.replace("\\", "/");
				url = "file:///" + url0 + "#Surface";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Earthquake Catalog Plugin")) {
				File file = new File("userguide_HTML/userGuide.html");
				String filepath = (file.getAbsolutePath());
				String url0 = filepath.replace("\\", "/");
				url = "file:///" + url0 + "#EarthquakeCatalog";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Legend Plugin")) {
				File file = new File("userguide_HTML/userGuide.html");
				String filepath = (file.getAbsolutePath());
				String url0 = filepath.replace("\\", "/");
				url = "file:///" + url0 + "#Legend";
			  }
		  else if (MainGUI.pluginTabPane.getTitleAt(MainGUI.pluginTabPane.getSelectedIndex()).equals("Earthquake Simulators")) {
				File file = new File("userguide_HTML/userGuide.html");
				String filepath = (file.getAbsolutePath());
				String url0 = filepath.replace("\\", "/");
				url = "file:///" + url0 + "#Simulators";
			  }
			  else{
					File file = new File("userguide_HTML/userGuide.html");
					String filepath = (file.getAbsolutePath());
					url = filepath.replace("\\", "/");
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