package org.scec.vtk.timeline.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.scec.vtk.timeline.KeyFrame;
import org.scec.vtk.timeline.KeyFrameList;
import org.scec.vtk.timeline.RangeKeyFrame;

class KeyFrameLabel extends JLabel implements Icon {
	
	private Color fillColor;
	private Color dragColor;
	private Color normalOutlineColor;
	private Color selectedOutlineColor;
	private boolean selected = false;
	private boolean dragging = false;
	
	private TimelinePanel panel;
	private KeyFrame key;
	private KeyFrameList keys;
	
	public KeyFrameLabel(TimelinePanel panel, KeyFrame key, KeyFrameList keys) {
		super();
		setIcon(this);
		this.panel = panel;
		this.key = key;
		this.keys = keys;
		this.fillColor = TimelinePanel.fillColorForKey(key);
		this.normalOutlineColor = TimelinePanel.keyOutlineColor;
		this.selectedOutlineColor = TimelinePanel.keyOutlineSelectedColor;
		this.dragColor = saturate(fillColor);
	}
	
	private static Color saturate(Color c) {
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		
		int saturationSteps = 2;
		
		for (int i=0; i<saturationSteps; i++) {
			r = (int)(0.5d*(r + 255d)+0.5);
			g = (int)(0.5d*(g + 255d)+0.5);
			b = (int)(0.5d*(b + 255d)+0.5);
		}
		
		return new Color(r, g, b);
	}
	
	public KeyFrame getKeyFrame() {
		return key;
	}
	
	public KeyFrameList getKeyFrameList() {
		return keys;
	}
	
	public void dragTo(double time) {
		dragging = true;
		if (time < 0)
			time = 0;
		// time at the location where it's currently drawn
		double locTime = panel.getTime(getX()+TimelinePanel.halfKeyWidth);
		if (time != locTime) {
//			System.out.println("Move to "+time);
			int x = panel.getPixelX(time) - TimelinePanel.halfKeyWidth;
			setLocation(x, getY());
		}
	}
	
	public void finalizeDrag() {
		dragging = false;
		int x = getX() + TimelinePanel.halfKeyWidth;
		double time = panel.getTime(x);
		if (time < 0)
			time = 0;
		key.setStartTime(time);
		System.out.println("Dragged to "+time);
		repaint();
	}
	
	public boolean isDragging() {
		return dragging;
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		// pluginY is the top of this plugin's area
		Color outlineColor;
		if (selected)
			outlineColor = selectedOutlineColor;
		else
			outlineColor = normalOutlineColor;
		Color myFillColor;
		if (dragging)
			myFillColor = dragColor;
		else
			myFillColor = fillColor;
		
		int width = TimelinePanel.keyWidth;
		int height = TimelinePanel.keyHeight;
		
		g.setColor(myFillColor);
		g.fillRect(x, y, width, height);
		g.setColor(outlineColor);
		g.drawRect(x, y, width, height);

		if (key instanceof RangeKeyFrame) {
			int x2 = x + panel.getPixelX(((RangeKeyFrame)key).getDuration());
			// draw line connecting
			int middleY = y + TimelinePanel.halfKeyHeight;
			g.drawLine(x+width, middleY, x2, middleY);
			// draw right rectangle
			g.setColor(myFillColor);
			g.fillRect(x2, y, width, height);
			g.setColor(outlineColor);
			g.drawRect(x2, y, width, height);
		}
	}

	@Override
	public int getIconWidth() {
		if (key instanceof RangeKeyFrame)
			return TimelinePanel.keyWidth + panel.getPixelX(((RangeKeyFrame)key).getDuration())+1;
		return TimelinePanel.keyWidth+1;
	}

	@Override
	public int getIconHeight() {
		return TimelinePanel.keyHeight+1;
	}
	
	public void updateSize() {
		setSize(getIconWidth(), getIconHeight());
	}
	
}
