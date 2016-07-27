package org.scec.vtk.main;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.InputStreamReader;
import java.util.Map;

import javax.swing.SwingUtilities;

public class MainSplashScreen  extends Frame implements ActionListener {
	    static void renderSplashFrame(Graphics2D g, int frame) {
	        final String[] comps = {"...","..."};
	        g.setComposite(AlphaComposite.Clear);
	        g.fillRect(120,140,200,40);
	        g.setPaintMode();
	        g.setColor(Color.BLACK);
	        g.drawString("Loading "+comps[(frame/5)%2]+"...", 120, 150);
	    }
	    public MainSplashScreen() {
	        super("");
	        //main gui loads after the splash screen
	        MainGUI maingui = new MainGUI();
	        
	        final SplashScreen splash = SplashScreen.getSplashScreen();
	        if (splash == null) {
	            System.out.println("SplashScreen.getSplashScreen() returned null");
	            return;
	        }
	        Graphics2D g = splash.createGraphics();
	        if (g == null) {
	            System.out.println("g is null");
	            return;
	        }
	        for(int i=0; i<100; i++) {
	            renderSplashFrame(g, i);
	            splash.update();
	            try {
	                Thread.sleep(90);
	            }
	            catch(InterruptedException e) {
	            }
	        }
	        splash.close();
	        setVisible(true);
	        toFront();
	        
	    }
	    public void actionPerformed(ActionEvent ae) {
	        System.exit(0);
	    }
	    
		//Main function to run GUI
		public static void main(String s[]) {
		    SwingUtilities.invokeLater(new Runnable() {
		      public void run() {
		    	  
		    	  MainSplashScreen mss = new MainSplashScreen();
		      }
		    });
		  }
}
