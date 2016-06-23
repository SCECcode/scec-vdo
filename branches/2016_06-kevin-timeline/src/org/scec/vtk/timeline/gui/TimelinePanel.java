package org.scec.vtk.timeline.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;

import javax.swing.JPanel;

import org.scec.vtk.timeline.KeyFrame;
import org.scec.vtk.timeline.KeyFrameList;
import org.scec.vtk.timeline.RangeKeyFrame;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.timeline.VisibilityKeyFrame;

public class TimelinePanel extends JPanel implements MouseListener {
	
	private int pixelsPerSecond = 100;
	private double secondsPerPixel = 1d/(double)pixelsPerSecond;
	
	private double curTime;
	
	// static sizes/font for the ticks/labels
	public static final int headerHeight = 30;
	private static final double majorTickInterval = 1d;
	private static final double minorTickInterval = 0.1d;
	private static final int majorTickHeight = 16;
	private static final int minorTickHeight = 8;
	private static final DecimalFormat timeLabelFormat = new DecimalFormat("0s");
	private static final Font timeLabelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	private static final int timeLabelLowerPad = 2;
	
	private static final Color lineColor = Color.BLACK;
	private static final Color tickColor = Color.BLACK;
	private static final Color pluginDividerColor = Color.LIGHT_GRAY;
	
	public static final int heightPerPlugin = 30;
	private static final int keyWidth = 5; // prefer odd for perfect alignment
	private static final int keyHeight = 15; // prefer odd for perfect alignment
	private static final int halfKeyWidth = keyWidth/2;
	private static final int keyYRelative = (heightPerPlugin-keyHeight)/2;
	private static final int halfKeyHeight = keyHeight/2;
	private static final int halfPluginHeight = heightPerPlugin/2;
	
	private static final Color keyOutlineColor = Color.BLACK;
	
	private Timeline timeline;
	
	public TimelinePanel(Timeline timeline) {
		this.timeline = timeline;
		
		System.out.println("height: "+heightPerPlugin);
		System.out.println("keyWidth: "+keyWidth);
		System.out.println("keyHeight: "+keyHeight);
		System.out.println("halfKeyWidth: "+halfKeyWidth);
		System.out.println("keyYRelative: "+keyYRelative);
		System.out.println("halfKeyHeight: "+halfKeyHeight);
		System.out.println("halfPluginHeight: "+halfPluginHeight);
		
		this.addMouseListener(this);
	}
	
	@Override
	protected synchronized void paintComponent(Graphics g) {
		super.paintComponent(g);
		int panelHeight = getHeight();
		int panelWidth = getWidth();
		
		double curMaxTime = getCurrentTotalTime();
		
		// draw line at current time
		g.setColor(lineColor);
		int curTimePixel = getPixelX(curTime, panelWidth);
//		System.out.println("Paint with time: "+curTime+", pixel="+curTimePixel);
		g.drawLine(curTimePixel, 0, curTimePixel, panelHeight);
		
		// draw header
		drawTicks(g, majorTickInterval, majorTickHeight, panelWidth, curMaxTime, true);
		drawTicks(g, minorTickInterval, minorTickHeight, panelWidth, curMaxTime, false);
		
		int y = headerHeight;
		
		g.setColor(pluginDividerColor);
		g.drawLine(0, y, panelWidth, y);
		
		for (int index=0; index < timeline.getNumPlugins(); index++) {
			KeyFrameList keys = timeline.getKeysForPlugin(index);
			for (KeyFrame key : keys)
				drawKeyRect(g, key, y, panelWidth);
			
			y += heightPerPlugin;
			g.setColor(pluginDividerColor);
			g.drawLine(0, y, panelWidth, y);
		}
	}
	
	private static Color fillColorForKey(KeyFrame key) {
		if (key instanceof VisibilityKeyFrame) {
			VisibilityKeyFrame v = (VisibilityKeyFrame)key;
			if (v.isVisible())
				return Color.BLUE;
			else
				return Color.GRAY;
		}
		if (key instanceof RangeKeyFrame)
			return Color.GREEN;
		return Color.RED;
	}
	
	private void drawKeyRect(Graphics g, KeyFrame key, int pluginY, int panelWidth) {
		// pluginY is the top of this plugin's area
		Color fillColor = fillColorForKey(key);
		Color outlineColor = keyOutlineColor; // TODO selection
		double time = key.getStartTime();
		int x = getPixelX(time, panelWidth) - halfKeyWidth;
		int y = pluginY + keyYRelative;
		g.setColor(fillColor);
		g.fillRect(x, y, keyWidth, keyHeight);
		g.setColor(outlineColor);
		g.drawRect(x, y, keyWidth, keyHeight);
		
		if (key instanceof RangeKeyFrame) {
			double endTime = ((RangeKeyFrame)key).getEndTime();
			int x2 = getPixelX(endTime, panelWidth) - halfKeyWidth;
			// draw line connecting
			int middleY = pluginY + halfPluginHeight;
			g.drawLine(x+keyWidth, middleY, x2, middleY);
			// draw right rectangle
			g.setColor(fillColor);
			g.fillRect(x2, y, keyWidth, keyHeight);
			g.setColor(outlineColor);
			g.drawRect(x2, y, keyWidth, keyHeight);
		}
	}
	
	private void drawTicks(Graphics g, double interval, int tickHeight, int panelWidth, double maxTime, boolean label) {
		if (label)
			g.setFont(timeLabelFont);
		g.setColor(tickColor);
		for (double time=0; time<=maxTime; time+=interval) {
			int x = getPixelX(time, panelWidth);
			g.drawLine(x, 0, x, tickHeight);
			if (label)
				g.drawString(timeLabelFormat.format(time), x, headerHeight-timeLabelLowerPad);
		}
	}
	
	private int getPixelX(double time, int panelWidth) {
		int pixel = (int)(time * pixelsPerSecond + 0.5);
		if (pixel >= panelWidth)
			pixel = panelWidth-1;
		return pixel;
	}
	
	public double getCurrentTotalTime() {
		return getWidth()*secondsPerPixel;
	}
	
	public void setCurTime(double curTime) {
		this.curTime = curTime;
		this.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println(e.getPoint());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
