package org.scec.vtk.tools;

import vtk.vtkCamera;

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
}
