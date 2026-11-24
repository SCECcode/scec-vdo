package org.scec.vtk.main;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.scec.vtk.tools.CameraNavigation;
import org.scec.vtk.tools.Transform;

import vtk.vtkCamera;
import vtk.vtkGenericOpenGLRenderWindow;
import vtk.vtkNativeLibrary;
import vtk.rendering.vtkAbstractComponent;

/** Exercises VTK's event forwarder without opening a window. Requires a display for Swing. */
public class GlobePanInteractionTest {
	private static final double TOLERANCE = 1e-8;

	private static final class Viewer extends vtkAbstractComponent<JPanel> {
		private final JPanel canvas = new JPanel();
		private int renders;

		Viewer() {
			super(new vtkGenericOpenGLRenderWindow());
			canvas.setSize(600, 400);
			setSize(1200, 800); // Simulate a high-DPI window.
			windowInteractor.SetEnableRender(false);
			windowInteractor.Enable();
		}

		@Override
		public JPanel getComponent() {
			return canvas;
		}

		@Override
		public void Render() {
			renders++;
		}
	}

	private static void check(boolean condition, String message) {
		if (!condition)
			throw new AssertionError(message);
	}

	private static boolean isOnSurface(vtkCamera camera) {
		double[] focal = camera.GetFocalPoint();
		return Vector3D.distance(new Vector3D(focal), new Vector3D(Transform.projectToEarthSurface(focal))) < TOLERANCE;
	}

	private static void setCamera(vtkCamera camera, double focalRadius) {
		camera.SetPosition(Transform.re + 1800, -700, 300);
		camera.SetFocalPoint(focalRadius, 0, 0);
		camera.SetViewUp(0, 0, 1);
		camera.SetClippingRange(1, 30000);
	}

	private static MouseEvent event(Viewer viewer, int id, int mods, int x, int y, int button) {
		return new MouseEvent(viewer.getComponent(), id, 0, mods, x, y, 1, false, button);
	}

	private static void drag(Viewer viewer, boolean shift, boolean releaseShiftDuringDrag) {
		var forwarder = viewer.getInteractorForwarder();
		int button = InputEvent.BUTTON2_DOWN_MASK;
		int mods = button | (shift ? InputEvent.SHIFT_DOWN_MASK : 0);
		forwarder.mousePressed(event(viewer, MouseEvent.MOUSE_PRESSED, mods, 270, 120, MouseEvent.BUTTON2));
		for (int i = 1; i <= 8; i++) {
			int dragMods = releaseShiftDuringDrag && i > 4 ? button : mods;
			forwarder.mouseDragged(event(viewer, MouseEvent.MOUSE_DRAGGED, dragMods, 270 + 8 * i, 120 - 5 * i,
					MouseEvent.NOBUTTON));
		}
		forwarder.mouseReleased(event(viewer, MouseEvent.MOUSE_RELEASED, 0, 334, 80, MouseEvent.BUTTON2));
	}

	private static void verifyPan(Viewer viewer, int[] changes, boolean shift, boolean expectedGlobe,
			boolean releaseShiftDuringDrag) {
		vtkCamera camera = viewer.getRenderer().GetActiveCamera();
		setCamera(camera, Transform.re);
		Vector3D direction = new Vector3D(camera.GetDirectionOfProjection());
		double distance = camera.GetDistance();
		int previousChanges = changes[0];
		int previousRenders = viewer.renders;
		drag(viewer, shift, releaseShiftDuringDrag);
		check(camera.GetFocalPoint()[1] < -1d, "Dragging right should move the pivot left");
		check(Math.abs(camera.GetDistance() - distance) < TOLERANCE, "Pan changed camera distance");
		check(isOnSurface(camera) == expectedGlobe, "Wrong pan mode");
		if (expectedGlobe) {
			check(changes[0] == previousChanges + 8 && viewer.renders == previousRenders + 8,
					"Globe pan should update and render once per drag event");
			double[] f = camera.GetFocalPoint();
			double equatorSquared = Transform.re * Transform.re;
			double poleSquared = Transform.rp * Transform.rp;
			Vector3D normal = new Vector3D(f[0] / equatorSquared, f[1] / equatorSquared, f[2] / poleSquared)
					.normalize();
			Vector3D newDirection = new Vector3D(camera.GetDirectionOfProjection());
			check(Math.abs(direction.getX() - normal.dotProduct(newDirection)) < TOLERANCE,
					"Globe pan changed the camera's local tilt");
		} else {
			check(Vector3D.distance(direction, new Vector3D(camera.GetDirectionOfProjection())) < TOLERANCE,
					"Regular pan changed the view direction");
			check(changes[0] == previousChanges, "Regular pan invoked the globe callback");
		}
	}

	private static void verifyPanSensitivity(Viewer viewer) {
		var renderer = viewer.getRenderer();
		var camera = renderer.GetActiveCamera();
		double[][] drags = { { 60, 0 }, { 0, 60 }, { 60, 60 } };
		for (int parallel : new int[] { 0, 1 }) {
			camera.SetParallelProjection(parallel);
			for (double[] drag : drags) {
				Vector3D headOn = null;
				for (int tilt : new int[] { 0, 60, 85 }) {
					double angle = Math.toRadians(tilt);
					camera.SetPosition(Transform.re + 1500 * Math.cos(angle), 0, 1500 * Math.sin(angle));
					camera.SetFocalPoint(Transform.re, 0, 0);
					camera.SetViewUp(-Math.sin(angle), 0, Math.cos(angle));
					camera.SetClippingRange(1, 30000);
					check(CameraNavigation.globePan(renderer, drag[0], drag[1]), "Drag did not move the pivot");
					Vector3D point = new Vector3D(camera.GetFocalPoint());
					if (headOn == null)
						headOn = point;
					else
						check(Vector3D.distance(headOn, point) < TOLERANCE, "Pan changed at " + tilt + "° tilt"
								+ " for drag (" + drag[0] + ", " + drag[1] + ")");
				}
			}
		}
	}

	private static void verifyGridFit(Viewer viewer) {
		var renderer = viewer.getRenderer();
		var camera = renderer.GetActiveCamera();
		for (double[] bounds : new double[][] {
				{ -Transform.re, Transform.re, -Transform.re, Transform.re, -Transform.rp, Transform.rp },
				{ Transform.re - 30, Transform.re, -350, 350, -450, 450 }
		}) {
			setCamera(camera, Transform.re);
			renderer.ResetCamera(bounds);
			Vector3D fitEye = new Vector3D(camera.GetPosition());
			Vector3D fitDirection = new Vector3D(camera.GetDirectionOfProjection());
			setCamera(camera, Transform.re);
			CameraNavigation.fitViewToSurface(renderer, bounds);
			check(isOnSurface(camera), "Grid fitting left the pivot off the surface");
			check(Vector3D.distance(fitEye, new Vector3D(camera.GetPosition())) < TOLERANCE,
					"Snapping changed the fitted camera position");
			check(Vector3D.distance(fitDirection, new Vector3D(camera.GetDirectionOfProjection())) < TOLERANCE,
					"Snapping changed the fitted camera direction");
		}
	}

	public static void main(String[] args) throws Exception {
		vtkNativeLibrary.LoadNativeLibraries(vtkNativeLibrary.vtkCommonCore, vtkNativeLibrary.vtkCommonMath,
				vtkNativeLibrary.vtkCommonTransforms, vtkNativeLibrary.vtkCommonDataModel,
				vtkNativeLibrary.vtkCommonExecutionModel, vtkNativeLibrary.vtkRenderingCore,
				vtkNativeLibrary.vtkRenderingOpenGL2, vtkNativeLibrary.vtkRenderingUI, vtkNativeLibrary.vtkInteractionStyle);
		SwingUtilities.invokeAndWait(() -> {
			Viewer viewer = new Viewer();
			int[] changes = { 0 };
			GlobePanInteraction globePan = new GlobePanInteraction(viewer, renderer -> changes[0]++);
			viewer.getInteractorForwarder().setEventInterceptor(globePan);
			verifyPan(viewer, changes, false, false, false);
			verifyPan(viewer, changes, true, true, true);
			globePan.setGlobePanDefault(true);
			viewer.getRenderer().GetActiveCamera().ParallelProjectionOn();
			viewer.getRenderer().GetActiveCamera().SetParallelScale(500);
			verifyPan(viewer, changes, false, true, false);
			verifyPan(viewer, changes, true, false, false);
			setCamera(viewer.getRenderer().GetActiveCamera(), 0d);
			drag(viewer, false, false);
			check(isOnSurface(viewer.getRenderer().GetActiveCamera()), "First globe drag did not snap the pivot");
			viewer.getRenderer().GetActiveCamera().ParallelProjectionOff();
			verifyGridFit(viewer);
			verifyPanSensitivity(viewer);
		});
		System.out.println("PASS: native pan gestures, camera motion, and graticule fitting");
	}
}
