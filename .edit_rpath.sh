#!/bin/bash
# Update rpath in JNI libraries to search for dynamic libs in same directory.
# This is necessary to load VTK when packaging releases.

# Directory containing your JNI libs and dylibs
LIB_DIR="$1"

# Update each jnilib's rpath
for jnilib in "$LIB_DIR"/*.jnilib; do
  echo "Updating rpath for $jnilib"
  install_name_tool -add_rpath "@loader_path" "$jnilib"
done

