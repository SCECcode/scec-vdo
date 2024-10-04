#!/bin/bash
# When bundling for distribution, set JAVA_HOME to packaged JRE

if [ "$(uname -m)" == "arm64" ]; then
  export DYLD_LIBRARY_PATH=vtkLibs/macosx_arm:$DYLD_LIBRARY_PATH
  #export JAVA_HOME=jre/macosx_arm/Contents/Home
else
  export DYLD_LIBRARY_PATH=vtkLibs/macosx_64bit:$DYLD_LIBRARY_PATH
  #export JAVA_HOME=jre/macosx_64bit/Contents/Home
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd $DIR
./.do_launch.sh

