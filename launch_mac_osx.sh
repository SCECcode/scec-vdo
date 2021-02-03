#!/bin/bash

export DYLD_LIBRARY_PATH=vtkLibs/macosx_64bit:$DYLD_LIBRARY_PATH

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd $DIR
./.do_launch.sh vtkLibs/macosx_64bit/vtk.jar
