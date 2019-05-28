package org.scec.vtk.plugins.GISHazusEventsPlugin;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class SetUpNewHazusTab {
	private JScrollPane scrollPane;
	private JPanel subGroupPanel;
	private ArrayList<String> quakeNames;
	
	SetUpNewHazusTab(){
		subGroupPanel = new JPanel();
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(subGroupPanel);
		subGroupPanel.setLayout(new BoxLayout(subGroupPanel,BoxLayout.PAGE_AXIS));
		quakeNames = new ArrayList<String>();
	}
	
	public JScrollPane getTab(){
		return scrollPane;
	}
	
	public JPanel getSubGroupPanel(){
		return subGroupPanel;
	}
}