#!/bin/sh
# Linux launch script used for packaged application with jpackage
# Not to be confused with `launch_linux.sh` which can be used directly from Terminal.
cd "$(dirname "$0")"
export LD_LIBRARY_PATH=$PWD/lib/runtime/lib:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH=$PWD/lib/app/vtkLibs/linux_64bit:$LD_LIBRARY_PATH
bin/SCEC-VDO

