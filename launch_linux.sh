#!/bin/bash
# When bundling for distribution, set JAVA_HOME to packaged JRE

export LD_LIBRARY_PATH=vtkLibs/linux_64bit:$LD_LIBRARY_PATH
#export JAVA_HOME=jre/linux_64bit

if [[ -e $JAVA_HOME/lib/amd64 ]];then
	export LD_LIBRARY_PATH=$JAVA_HOME/lib/amd64:$LD_LIBRARY_PATH
	#export JAVA_HOME=jre/linux_arm
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd "$DIR"
./.do_launch.sh

