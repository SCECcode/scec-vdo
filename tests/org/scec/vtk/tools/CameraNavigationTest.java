package org.scec.vtk.tools;

import vtk.vtkCamera;
import vtk.vtkNativeLibrary;

/** Standalone headless check of focal-point snapping with a native VTK camera. */
public class CameraNavigationTest {
	private static final double TOLERANCE_KM = 1e-8;

	private static void check(boolean condition, String message) {
		if (!condition)
			throw new AssertionError(message);
	}

	private static void assertVector(double[] expected, double[] actual, String message) {
		check(actual != null, message + " missing");
		for (int i = 0; i < 3; i++)
			check(Math.abs(expected[i] - actual[i]) <= TOLERANCE_KM, message + " differs on axis " + i);
	}

	private static void assertSurface(double[] point) {
		assertVector(Transform.projectToEarthSurface(point), point, "Point on surface");
	}

	private static void snapAlongSightline(vtkCamera camera) {
		double[] surface = Transform.transformLatLon(37.5, -117.6);
		double[] eye = Transform.transformLatLonHeight(38.3, -118.2, 2000);
		// The focal point is in front of the ellipsoid. The ray must extend past it to hit the surface.
		double[] focal = new double[3];
		for (int i = 0; i < 3; i++)
			focal[i] = 0.5 * (surface[i] + eye[i]);
		camera.SetPosition(eye);
		camera.SetFocalPoint(focal);
		camera.SetViewUp(0, 0, 1);
		double[] direction = camera.GetDirectionOfProjection();
		check(CameraNavigation.snapFocalPointToSurface(camera), "Forward hit failed");
		assertVector(surface, camera.GetFocalPoint(), "Sightline hit");
		assertVector(eye, camera.GetPosition(), "Camera position");
		assertVector(direction, camera.GetDirectionOfProjection(), "Viewing direction");
		assertVector(new double[] { 0, 0, 1 }, camera.GetViewUp(), "View up");
	}

	private static void snapMissedSightline(vtkCamera camera) {
		double[] eye = { Transform.re + 100, -500, 0 };
		camera.SetPosition(eye);
		camera.SetFocalPoint(eye[0], -200, 0);
		double distance = camera.GetDistance();
		double[] direction = camera.GetDirectionOfProjection();
		check(CameraNavigation.snapFocalPointToSurface(camera), "Miss fallback failed");
		assertSurface(camera.GetFocalPoint());
		assertVector(direction, camera.GetDirectionOfProjection(), "Fallback viewing direction");
		check(Math.abs(camera.GetDistance() - distance) <= TOLERANCE_KM, "Fallback changed distance");
		check(!Transform.isInsideEarth(camera.GetPosition()), "Fallback buried camera");
	}

	private static void keepCameraAboveSurface(vtkCamera camera) {
		double[] eye = { Transform.re + 10, 0, 0 };
		camera.SetPosition(eye);
		camera.SetFocalPoint(Transform.re + 20, 100, 0);
		check(CameraNavigation.snapFocalPointToSurface(camera), "Oblique fallback failed");
		assertSurface(camera.GetFocalPoint());
		assertVector(eye, camera.GetPosition(), "Eye when translation would place it underground");
	}

	private static void rayEdgeCases() {
		double[] equator = { Transform.re, 0, 0 };
		double[] northPole = { 0, 0, Transform.rp };
		double[] center = { 0, 0, 0 };
		assertVector(northPole, Transform.projectToEarthSurface(new double[] { 0, 0, 6500 }), "Pole projection");
		assertVector(northPole, Transform.intersectEarthSurface(new double[] { 0, 0, 6500 }, center), "Pole hit");
		assertVector(equator, Transform.intersectEarthSurface(center, equator), "Hit from inside Earth");
		assertVector(equator, Transform.intersectEarthSurface(new double[] { Transform.re, -100, 0 }, equator),
				"Tangent hit");
		check(Transform.intersectEarthSurface(equator, new double[] { Transform.re + 1, 0, 0 }) == null,
				"Ray pointed away from Earth should miss");
		check(Transform.intersectEarthSurface(equator, equator) == null, "Zero direction should miss");
		check(Transform.projectToEarthSurface(center) == null, "Origin should have no radial projection");
	}

	public static void main(String[] args) {
		rayEdgeCases();
		vtkNativeLibrary.LoadNativeLibraries(vtkNativeLibrary.vtkCommonCore, vtkNativeLibrary.vtkCommonMath,
				vtkNativeLibrary.vtkCommonTransforms, vtkNativeLibrary.vtkCommonDataModel,
				vtkNativeLibrary.vtkCommonExecutionModel, vtkNativeLibrary.vtkRenderingCore);
		vtkCamera camera = new vtkCamera();
		try {
			snapAlongSightline(camera);
			snapMissedSightline(camera);
			keepCameraAboveSurface(camera);
		} finally {
			camera.Delete();
		}
		System.out.println("PASS: ellipsoid intersections, camera snap, and missed-sightline fallbacks");
	}
}
