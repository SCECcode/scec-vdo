package org.scec.vtk.timeline.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;

import org.scec.vtk.timeline.Timeline;

public class TimelineGUI extends JScrollPane {
	
	private static final int plugin_line_height = 30;
	
	private Timeline timeline;
	
	private CombinedPanel combinedPanel;
	private TimelinePanel centerPanel;
	private TimelineLeftPanel leftPanel;
	
	public TimelineGUI(Timeline timeline) {
		this.timeline = timeline;
		
		centerPanel = new TimelinePanel(timeline);
		leftPanel = new TimelineLeftPanel(timeline, centerPanel);
		
		combinedPanel = new CombinedPanel(leftPanel, centerPanel);
		
		setViewportView(combinedPanel);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		updateSize();
		
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				updateSize();
			}
		});
	}
	
	private class CombinedPanel extends JPanel implements Scrollable {
		
		private JScrollPane centerScroll;
		
		public CombinedPanel(TimelineLeftPanel leftPanel, TimelinePanel centerPanel) {
			setLayout(null);
			
			add(leftPanel);
			centerScroll = new JScrollPane(centerPanel,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			add(centerScroll);
			
			updateSize();
		}
		
		public void updateSize() {
			leftPanel.updateSize();
			centerPanel.updateSize();
			if (getParent() == null)
				return;
			int totWidth = getParent().getWidth();
//			int totHeight = leftPanel.getPreferredSize().height;
			int minHeight = leftPanel.getPreferredSize().height + centerScroll.getHorizontalScrollBar().getHeight();
			int totHeight = getParent().getHeight();
			if (totHeight < minHeight)
				totHeight = minHeight;
			
//			System.out.println("Combined size: "+totWidth+" x "+totHeight);
			
			leftPanel.setBounds(0, 0, TimelineLeftPanel.panelWidth, totHeight);
			centerScroll.setBounds(TimelineLeftPanel.panelWidth, 0, totWidth-TimelineLeftPanel.panelWidth, totHeight);
			
			Dimension mySize = new Dimension(totWidth, totHeight);
			setPreferredSize(mySize);
			setSize(mySize);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
//			if (getParent() != null)
//				return getParent().getPreferredSize();
			return null;
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			return 5;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			return TimelinePanel.heightPerPlugin;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
		
	}
	
	public void updateSize() {
//		leftPanel.updateSize();
//		centerPanel.updateSize();
//		Dimension leftSize = leftPanel.getPreferredSize();
//		Dimension centerSize = centerPanel.getPreferredSize();
//		Dimension totSize = new Dimension((int)(leftSize.getWidth()+centerSize.getWidth()), (int)centerSize.getHeight());
//		centerPanel.setPreferredSize(totSize);
//		centerPanel.setMinimumSize(totSize);
//		centerPanel.setMaximumSize(totSize);
//		centerPanel.setSize(totSize);
//		centerPanel.revalidate();
		
//		System.out.println("Center preferred size: "+centerPanel.getPreferredSize());
//		System.out.println("Center size: "+centerPanel.getSize());
//		if (this.getParent() != null)
//			setPreferredSize(this.getParent().getPreferredSize());
//		centerScroll.setPreferredSize(this.getPreferredSize());
//		centerScroll.revalidate();
//		revalidate();
		combinedPanel.updateSize();
	}

}
