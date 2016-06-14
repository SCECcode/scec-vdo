package org.scec.geo3d.commons.opensha.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class FaultTablePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JScrollPane tablePane;
	
	public FaultTablePanel(JTable table) {
		super(new BorderLayout());
		tablePane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
////		tablePane.setPreferredSize(new Dimension(300));
//		tablePane.setMaximumSize(new Dimension(1000, 400));
		
		this.add(tablePane, BorderLayout.CENTER);
	}

}
