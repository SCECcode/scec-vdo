package org.scec.vtk.tools;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkCamera;
import vtk.vtkRenderer;

public final class CameraNavigation {
	private CameraNavigation() {}

	/** Move the camera pivot onto the Earth's surface. Returns false if there is no usable surface target. */
	public static boolean snapFocalPointToSurface(vtkCamera camera) {
		double[] eye = camera.GetPosition();
		double[] focal = camera.GetFocalPoint();
		double[] surface = Transform.intersectEarthSurface(eye, focal);
		if (surface != null) {
			// The camera keeps looking along the same ray, so only its pivot and distance to it change.
			camera.SetFocalPoint(surface);
			return true;
		}

		surface = Transform.projectToEarthSurface(focal);
		if (surface == null)
			return false;
		// Translate the whole camera when the sightline misses Earth so the viewing angle and distance stay the same.
		double[] translatedEye = new double[] {
				eye[0] + (surface[0] - focal[0]), eye[1] + (surface[1] - focal[1]), eye[2] + (surface[2] - focal[2])
		};
		if (Transform.isInsideEarth(translatedEye) && !Transform.isInsideEarth(eye)) {
			// Keep the eye in place and aim at the projected target if translating would take it below the surface.
			if (Math.hypot(Math.hypot(surface[0] - eye[0], surface[1] - eye[1]), surface[2] - eye[2]) <= 1e-8)
				return false;
		} else {
			camera.SetPosition(translatedEye);
		}
		camera.SetFocalPoint(surface);
		return true;
	}

	/** Fit the scene bounds and move the pivot along the new sightline to the Earth surface. */
	public static void fitViewToSurface(vtkRenderer renderer, double[] bounds) {
		renderer.ResetCamera(bounds);
		snapFocalPointToSurface(renderer.GetActiveCamera());
	}

	/** Pan by a drag in VTK display pixels, with positive y pointing up. */
	public static boolean globePan(vtkRenderer renderer, double dx, double dy) {
		if (dx == 0d && dy == 0d)
			return false;
		vtkCamera camera = renderer.GetActiveCamera();
		boolean snapped = false;
		if (!Transform.isOnEarthSurface(camera.GetFocalPoint())) {
			snapped = snapFocalPointToSurface(camera);
			if (!snapped)
				return false;
		}

		Vector3D focal = new Vector3D(camera.GetFocalPoint());
		// Use the same depth as VTK's planar pan. Moving the camera opposite the drag makes the scene follow the mouse.
		renderer.SetWorldPoint(focal.getX(), focal.getY(), focal.getZ(), 1d);
		renderer.WorldToDisplay();
		double[] display = renderer.GetDisplayPoint();
		renderer.SetDisplayPoint(display[0] - dx, display[1] - dy, display[2]);
		renderer.DisplayToWorld();
		double[] world = renderer.GetWorldPoint();
		if (!Double.isFinite(world[3]) || world[3] == 0d)
			return snapped;
		Vector3D planarFocal = new Vector3D(world[0] / world[3], world[1] / world[3], world[2] / world[3]);

		// Rotate the screen plane onto the local tangent plane to preserve the drag length at any camera tilt.
		Vector3D normal = surfaceNormal(focal);
		Vector3D viewNormal = new Vector3D(camera.GetViewPlaneNormal());
		Rotation screenToSurface = new Rotation(viewNormal, normal);
		Vector3D tangent = screenToSurface.applyTo(planarFocal.subtract(focal));
		double[] projected = Transform.projectToEarthSurface(focal.add(tangent).toArray());
		if (projected == null)
			return snapped;

		Vector3D next = new Vector3D(projected);
		// Carry the eye offset and view-up with the surface normal, preserving the tilt and distance to the pivot.
		Rotation rotation = new Rotation(normal, surfaceNormal(next));
		double[] eye = camera.GetPosition();
		double[] nextEye = next.add(rotation.applyTo(new Vector3D(eye).subtract(focal))).toArray();
		if (Transform.isInsideEarth(nextEye) && !Transform.isInsideEarth(eye))
			return snapped;
		camera.SetPosition(nextEye);
		camera.SetFocalPoint(projected);
		camera.SetViewUp(rotation.applyTo(new Vector3D(camera.GetViewUp())).toArray());
		return true;
	}

	private static Vector3D surfaceNormal(Vector3D point) {
		double equatorSquared = Transform.re * Transform.re;
		double poleSquared = Transform.rp * Transform.rp;
		return new Vector3D(point.getX() / equatorSquared, point.getY() / equatorSquared,
				point.getZ() / poleSquared).normalize();
	}
}
