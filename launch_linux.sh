#!/bin/bash

export LD_LIBRARY_PATH=vtkLibs/linux_64bit:$LD_LIBRARY_PATH

if [[ -e $JAVA_HOME/lib/amd64 ]];then
	export LD_LIBRARY_PATH=$JAVA_HOME/lib/amd64:$LD_LIBRARY_PATH
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd $DIR
./.do_launch.sh
