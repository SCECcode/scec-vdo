package org.scec.vtk.timeline.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.scec.vtk.timeline.Timeline;

public class TimelineGUI extends JPanel {
	
	private static final int plugin_line_height = 30;
	
	private Timeline timeline;
	
	public TimelineGUI(Timeline timeline) {
		setLayout(new BorderLayout());
		
		this.timeline = timeline;
	}

}
