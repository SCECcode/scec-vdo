package org.scec.vtk.main;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import javax.swing.Icon;

/** A downward arrow landing on a curved surface. */
final class SnapToSurfaceIcon implements Icon {
	private static final int SIZE = 16;
	private static final BasicStroke STROKE = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final Path2D SHAPE = createShape();

	private static Path2D createShape() {
		Path2D path = new Path2D.Double();
		path.moveTo(8, 2);
		path.lineTo(8, 10);
		path.moveTo(5, 7);
		path.lineTo(8, 10);
		path.lineTo(11, 7);
		path.moveTo(2, 14);
		path.quadTo(8, 10, 14, 14);
		return path;
	}

	@Override
	public void paintIcon(Component component, Graphics graphics, int x, int y) {
		Graphics2D g = (Graphics2D)graphics.create();
		try {
			g.translate(x, y);
			g.setColor(component.getForeground());
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setStroke(STROKE);
			g.draw(SHAPE);
		} finally {
			g.dispose();
		}
	}

	@Override
	public int getIconWidth() {
		return SIZE;
	}

	@Override
	public int getIconHeight() {
		return SIZE;
	}
}
