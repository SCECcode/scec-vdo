#!/bin/bash

SRC="/home/kevin/workspace/scec_vdo_vtk"
DEST="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

rsync -a --delete $SRC/classes $DEST
rsync -a --delete $SRC/commons $DEST
rsync -a --delete $SRC/conf $DEST
rsync -a --delete $SRC/lib $DEST

if [[ ! -e $DEST/data ]];then
	mkdir $DEST/data
fi
rsync -a --delete $SRC/data/PoliticalBoundaries $DEST/data
rsync -a --delete $SRC/data/GISLocationPlugin $DEST/data
rsync -a --delete $SRC/data/Grid $DEST/data

rsync -a --delete $SRC/plugins $DEST
rsync -a --delete $SRC/resources $DEST
rsync -a --delete $SRC/src $DEST

if [[ ! -e $DEST/vtkLibs ]];then
        mkdir $DEST/vtkLibs
fi
rsync -a --delete $SRC/vtkLibs/macosx_64bit $DEST/vtkLibs
rsync -a --delete $SRC/vtkLibs/linux_64bit $DEST/vtkLibs
