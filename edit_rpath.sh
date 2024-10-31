#!/bin/bash

# Directory containing your JNI libs and dylibs
LIB_DIR="dist/vtkLibs/macosx_arm/"

# Update each jnilib's rpath
for jnilib in "$LIB_DIR"/*.jnilib; do
  echo "Updating rpath for $jnilib"
  install_name_tool -add_rpath "@loader_path" "$jnilib"
done

