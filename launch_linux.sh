#!/bin/bash
# When bundling for distribution, set JAVA_HOME to packaged JRE

#export JAVA_HOME=jre/linux_64bit
export LD_LIBRARY_PATH=vtkLibs/linux_64bit:$JAVA_HOME/lib:$LD_LIBRARY_PATH

if [[ -e $JAVA_HOME/lib/amd64 ]];then
	#export JAVA_HOME=jre/linux_arm
	export LD_LIBRARY_PATH=$JAVA_HOME/lib/amd64:$JAVA_HOME/lib:$LD_LIBRARY_PATH
fi

# If apt is installed and the package is not already installed, install the package
PACKAGE_NAME="freeglut3-dev"
if command -v apt &> /dev/null && ! dpkg -l | grep -q "^ii  $PACKAGE_NAME"; then
	sudo apt update
	sudo apt install -y $PACKAGE_NAME
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd "$DIR"
./.do_launch.sh


