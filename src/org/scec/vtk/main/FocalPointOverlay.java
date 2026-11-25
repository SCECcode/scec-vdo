package org.scec.vtk.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.Timer;
import javax.swing.plaf.LayerUI;

import vtk.rendering.vtkComponent;

/**
 * A screen-space marker for the camera's pivot during mouse navigation.
 * Movie and PNG exports capture the canvas inside this layer directly.
 */
final class FocalPointOverlay extends LayerUI<JComponent> {
	private static final long serialVersionUID = 1L;
	private static final int IDLE_TIMEOUT_MS = 600;
	private static final int MOUSE_BUTTONS = InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK
			| InputEvent.BUTTON3_DOWN_MASK;
	private static final Color INK = new Color(255, 255, 255, 160);
	private static final Color OUTLINE = new Color(0, 0, 0, 145);
	private static final BasicStroke INK_STROKE = new BasicStroke(1f);
	private static final BasicStroke OUTLINE_STROKE = new BasicStroke(3f);
	private static final Path2D CROSSHAIR = buildCrosshair();

	private final JLayer<JComponent> layer;
	private final Timer idleTimer;
	private boolean enabled = true;
	private Point2D.Double focalPosition;
	private int dragButton;

	FocalPointOverlay(vtkComponent<? extends JComponent> viewer) {
		JComponent canvas = viewer.getComponent();
		layer = new JLayer<>(canvas, this);
		// Clear after wheel zoom and if the system never delivers a drag release event.
		idleTimer = new Timer(IDLE_TIMEOUT_MS, event -> setFocalPosition(null));
		idleTimer.setRepeats(false);

		MouseAdapter listener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				endDrag();
				int button = event.getButton();
				if (enabled && button >= MouseEvent.BUTTON1 && button <= MouseEvent.BUTTON3)
					dragButton = InputEvent.getMaskForButton(button);
			}

			@Override
			public void mouseDragged(MouseEvent event) {
				if (dragButton != 0 && (event.getModifiersEx() & MOUSE_BUTTONS) == dragButton)
					showMarker(viewer);
				else
					hideMarker();
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				if (event.getWheelRotation() != 0)
					showMarker(viewer);
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				endDrag();
			}

			@Override
			public void mouseMoved(MouseEvent event) {
				// An unpressed move also ends a drag if the button release was missed.
				if (dragButton != 0)
					endDrag();
			}

			@Override
			public void mouseExited(MouseEvent event) {
				// The canvas still receives drag and release events after the pointer leaves.
				if (dragButton == 0)
					hideMarker();
			}
		};
		// The viewer has already installed its listeners, so read the camera after it handles the event.
		canvas.addMouseListener(listener);
		canvas.addMouseMotionListener(listener);
		canvas.addMouseWheelListener(listener);
		canvas.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				endDrag();
			}
		});
	}

	JLayer<JComponent> getLayer() {
		return layer;
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (!enabled)
			endDrag();
	}

	private void showMarker(vtkComponent<?> viewer) {
		if (!enabled)
			return;
		Point2D.Double position = projectFocalPoint(viewer);
		if (position == null) {
			hideMarker();
		} else {
			setFocalPosition(position);
			idleTimer.restart();
		}
	}

	private void endDrag() {
		dragButton = 0;
		hideMarker();
	}

	private void hideMarker() {
		idleTimer.stop();
		setFocalPosition(null);
	}

	private static Point2D.Double projectFocalPoint(vtkComponent<?> viewer) {
		var lock = viewer.getVTKLock();
		lock.lock();
		try {
			int[] size = viewer.getRenderWindow().GetSize();
			if (size[0] <= 0 || size[1] <= 0)
				return null;
			var renderer = viewer.getRenderer();
			double[] focalPoint = renderer.GetActiveCamera().GetFocalPoint();
			renderer.SetWorldPoint(focalPoint[0], focalPoint[1], focalPoint[2], 1d);
			renderer.WorldToDisplay();
			double[] display = renderer.GetDisplayPoint();
			// Convert from device pixels and VTK's lower-left origin to Swing's upper-left origin.
			double x = display[0] / size[0];
			double y = 1d - display[1] / size[1];
			return x >= 0d && x <= 1d && y >= 0d && y <= 1d ? new Point2D.Double(x, y) : null;
		} finally {
			lock.unlock();
		}
	}

	private void setFocalPosition(Point2D.Double position) {
		if (position == null && focalPosition == null)
			return;
		focalPosition = position;
		layer.repaint();
	}

	@Override
	public void paint(Graphics graphics, JComponent component) {
		super.paint(graphics, component);
		if (focalPosition == null)
			return;
		Graphics2D g = (Graphics2D)graphics.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.translate(roundToPixel(focalPosition.x * component.getWidth()),
					roundToPixel(focalPosition.y * component.getHeight()));
			g.setStroke(OUTLINE_STROKE);
			g.setColor(OUTLINE);
			g.draw(CROSSHAIR);
			g.setStroke(INK_STROKE);
			g.setColor(INK);
			g.draw(CROSSHAIR);
		} finally {
			g.dispose();
		}
	}

	private static long roundToPixel(double coordinate) {
		// A half-pixel center can land just above or below the tie because of projection roundoff.
		return Math.round(coordinate + 1e-6);
	}

	private static Path2D buildCrosshair() {
		Path2D path = new Path2D.Double();
		double halfGap = 4.5;
		double reach = halfGap + 9;
		path.moveTo(-reach, 0);
		path.lineTo(-halfGap, 0);
		path.moveTo(halfGap, 0);
		path.lineTo(reach, 0);
		path.moveTo(0, -reach);
		path.lineTo(0, -halfGap);
		path.moveTo(0, halfGap);
		path.lineTo(0, reach);
		return path;
	}
}
