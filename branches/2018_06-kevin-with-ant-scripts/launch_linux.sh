#!/bin/bash

LIB="lib"
VTKDIR="vtkLibs/linux_64bit"

export LD_LIBRARY_PATH=${VTKDIR}/:$LD_LIBRARY_PATH

#java -splash:resources/SCEClogo.png -Xms2G -Xmx20G -cp dist/scec_vdo.jar:${VTKDIR}/vtk.jar:${LIB}/EventWebService.jar:${LIB}/gluegen-rt.jar:${LIB}/jcodec-0.2.0.jar:${LIB}/jdom.jar:${LIB}/jmf.jar:${LIB}/jogl-all.jar:${LIB}/log4j-1.2.9.jar:${LIB}/opensha.jar:${LIB}/org-netbeans-swing-outline.jar:${LIB}/sdoutl.jar org.scec.vtk.main.MainGUI
java -splash:resources/SCEClogo.png -Xms2G -Xmx20G -cp classes:${VTKDIR}/vtk.jar:${LIB}/EventWebService.jar:${LIB}/gluegen-rt.jar:${LIB}/jcodec-0.2.0.jar:${LIB}/jdom.jar:${LIB}/jmf.jar:${LIB}/jogl-all.jar:${LIB}/log4j-1.2.9.jar:${LIB}/opensha.jar:${LIB}/org-netbeans-swing-outline.jar:${LIB}/sdoutl.jar org.scec.vtk.main.MainGUI
