package org.scec.vtk.plugins.ProgressBar;

import java.awt.Cursor;

import org.opensha.sha.gui.infoTools.CalcProgressBar;


//Object that can be attached to any function/constructor call that 
//produces a Loading Screen when things are being loaded. 
//Does not account for outside Mouse Clicks, clicking multiple times, etc. 
//@Joses

public class ProgressBar {
	CalcProgressBar progress;
	//Circular waiting cursor
	private Cursor waitCursor;
	
	
	//Default Constructor, loadingString represents the title of the 
	//Loading Screen you would want 
	public ProgressBar(String loadingString) {
		waitCursor = new Cursor(Cursor.WAIT_CURSOR);
		progress = new CalcProgressBar(loadingString, "Please Wait");
	}
	
	
	//is Called before function call where User will wait for the
	//program to run. 
	public void runProgressBar() {
		progress.setCursor(waitCursor);
		progress.setVisible(true);
		progress.setIndeterminate(true);
		//Attempts to keep it in the front. 
		progress.toFront();
	}
	
	//is Called after function call where we remove the Loading screen 
	//and call dispose. 
	public void stopProgressBar() {
		progress.toFront();
		progress.setVisible(false);	
		progress.dispose();
		
	}

}
