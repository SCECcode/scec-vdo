# Native VTK regression checks

These standalone checks use Swing and the bundled VTK libraries without opening a render window.
Run from the repository root with a JDK compatible with the OpenSHA library in `lib/opensha.jar`.
The commands below use the bundled Linux libraries; adjust the library path for other platforms.

```sh
ant -Drelease=21 build
mkdir -p build/test-classes
javac -cp 'classes:lib/*' -d build/test-classes \
    tests/org/scec/vtk/commons/opensha/surfaces/LineSurfaceGeneratorTest.java \
    tests/org/scec/vtk/commons/opensha/gui/EventManagerThreadingTest.java

LD_LIBRARY_PATH="$PWD/vtkLibs/linux_64bit${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}" \
    java -Djava.awt.headless=true -Djava.library.path=vtkLibs/linux_64bit \
    -cp 'build/test-classes:classes:lib/*' \
    org.scec.vtk.commons.opensha.surfaces.LineSurfaceGeneratorTest

LD_LIBRARY_PATH="$PWD/vtkLibs/linux_64bit${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}" \
    java -Djava.awt.headless=true -Djava.library.path=vtkLibs/linux_64bit \
    -cp 'build/test-classes:classes:lib/*' \
    org.scec.vtk.commons.opensha.gui.EventManagerThreadingTest
```

`LineSurfaceGeneratorTest` covers supplied and fixed colors, matching initial colors followed by recoloring,
independent outline/interior widths (defaults 2/1), editor refresh after programmatic changes, opacity,
unique grid edges, outline/trace modes, thin grids,
compound surfaces, parent-based and bounded bundlers, bundle clearing, picking registration, visibility,
settings rebuilds, and tree replacement. Both bundled and standalone coloring paths are exercised. It also
builds the VTK cell lookup before appending more lines to check the picking-cache invalidation path.

`EventManagerThreadingTest` checks that native updates occur on the Swing event thread, color batches render
once, superseded calculations cannot write stale colors, failures unlock the UI, and animations and tree
replacement preserve those guarantees.

These checks do not exercise screen rendering or actual mouse interaction; inspect the line styles in the
viewer as well when changing the rendering pipeline.

## GeoJSON subsectioning

The builder checks below use Swing without native VTK or a render window. A temporary preferences directory
keeps the remembered input directory separate from the viewer's normal preferences.

```sh
javac -cp 'classes:lib/*' -d build/test-classes \
    tests/org/scec/vtk/plugins/opensha/nshm/GeoJSONFaultBuilderTest.java
java -Djava.awt.headless=true -Djava.util.prefs.userRoot=/tmp/vdo-subsection-test-prefs \
    -cp 'build/test-classes:classes:lib/*' \
    org.scec.vtk.plugins.opensha.nshm.GeoJSONFaultBuilderTest
```

This checks the OpenSHA defaults, button availability, generation and tree notifications, parent IDs and
state metadata, repeated generation from the original sections, fixed-length limits, invalid settings,
empty input, and Reload restoring the original sections. The modal dialog itself requires a viewer check.
