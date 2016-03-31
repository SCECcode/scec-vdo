package org.scec.vtk.plugins.utils.components;

import java.awt.Dimension;
import java.awt.event.ActionListener;

//import javax.swing.ImageIcon;
import javax.swing.JButton;
//import javax.swing.JLabel;


import org.scec.vtk.tools.Prefs;


public class PlotButton extends JButton {

	    private static final long serialVersionUID = 1L;

		/**
	     * Constructs a new <code>JButton</code> with an icon indicating a "remove" action.
	     * Button is disabled by default.
	     * 
	     * @param listener button's event listener
	     * @param tip tool tip to set for button
	     */
	    public PlotButton(ActionListener listener, String tip) {
	        super();
	        this.setText("Plot");
	        this.setMinimumSize(new Dimension(40,20));
	        this.setMargin(Prefs.getIconInset());
	        this.setEnabled(false);
	        this.addActionListener(listener);
	        this.setToolTipText(tip);
	    }
}
