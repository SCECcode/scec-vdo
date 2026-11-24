package org.scec.vtk.main;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import org.scec.vtk.tools.CameraNavigation;

import vtk.vtkRenderer;
import vtk.rendering.vtkAbstractEventInterceptor;
import vtk.rendering.vtkComponent;

/** Uses the stock VTK mouse handling except during a globe pan drag. */
final class GlobePanInteraction extends vtkAbstractEventInterceptor {
	private final vtkComponent<? extends Component> viewer;
	private final Consumer<vtkRenderer> cameraChanged;
	private boolean globePanDefault;
	private boolean preserveFocalDepth;
	private boolean globeDrag;
	private int lastX;
	private int lastY;

	GlobePanInteraction(vtkComponent<? extends Component> viewer, Consumer<vtkRenderer> cameraChanged) {
		this.viewer = viewer;
		this.cameraChanged = cameraChanged;
	}

	void setGlobePanDefault(boolean globePanDefault) {
		this.globePanDefault = globePanDefault;
	}

	void setPreserveFocalDepth(boolean enabled) {
		preserveFocalDepth = enabled;
	}

	@Override
	public boolean mousePressed(MouseEvent event) {
		if (event.getButton() == MouseEvent.BUTTON2) {
			// Latch the mode for this drag so a modifier change cannot jump between camera behaviors mid-drag.
			globeDrag = globePanDefault != event.isShiftDown();
			lastX = event.getX();
			lastY = event.getY();
		}
		return false;
	}

	@Override
	public boolean mouseReleased(MouseEvent event) {
		if (event.getButton() == MouseEvent.BUTTON2)
			globeDrag = false;
		return false;
	}

	@Override
	public boolean mouseDragged(MouseEvent event) {
		if (!globeDrag || (event.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) == 0)
			return false;
		int dx = event.getX() - lastX;
		int dy = lastY - event.getY();
		lastX = event.getX();
		lastY = event.getY();
		var lock = viewer.getVTKLock();
		lock.lock();
		try {
			int[] size = viewer.getRenderWindow().GetSize();
			Component canvas = viewer.getComponent();
			if (size[0] <= 0 || size[1] <= 0 || canvas.getWidth() <= 0 || canvas.getHeight() <= 0)
				return true;
			// Swing reports logical pixels, while VTK projects into the render window's pixel dimensions.
			double displayDX = (double)dx * size[0] / canvas.getWidth();
			double displayDY = (double)dy * size[1] / canvas.getHeight();
			vtkRenderer renderer = viewer.getRenderer();
			if (!CameraNavigation.globePan(renderer, displayDX, displayDY, preserveFocalDepth))
				return true;
			cameraChanged.accept(renderer);
		} finally {
			lock.unlock();
		}
		viewer.Render();
		return true;
	}
}
