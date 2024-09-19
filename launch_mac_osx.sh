#!/bin/bash

if [ "$(uname -m)" == "arm64" ]; then
  export DYLD_LIBRARY_PATH=vtkLibs/macosx_arm:$DYLD_LIBRARY_PATH
else
  export DYLD_LIBRARY_PATH=vtkLibs/macosx_64bit:$DYLD_LIBRARY_PATH
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd $DIR
./.do_launch.sh

