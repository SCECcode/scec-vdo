@echo off

:: Use JRE bundled with Eclipse. You may need to update the version in JAVA_HOME.
set JAVA_HOME=%USERPROFILE%\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_22.0.1.v20240426-1149\jre
:: Use locally packaged JRE for distribution.
::set JAVA_HOME=%CD%\jre
set JAVA=%JAVA_HOME%\bin\java.exe
set VTK_LIBS=%CD%\vtkLibs\windows_64bit
set PATH=%PATH%;%JAVA_HOME%\bin;%VTK_LIBS%


echo Launching SCEC-VDO...

:: Define classpath to include all jar files inside of the 'lib' dir and the 'classes' dir.
::CLASSPATH=`find lib/ -maxdepth 1 -mindepth 1 -name '*.jar' | tr '\n' ':'`
set CLASSPATH=classes;lib\EventWebService.jar;lib\jcodec-0.2.0.jar;lib\jdom.jar;lib\jmf.jar;lib\jogamp-fat.jar;lib\jxlayer.jar;lib\log4j-1.2.9.jar;lib\opensha.jar;lib\org-netbeans-swing-outline.jar;lib\sdoutl.jar;lib\useit-fast.jar;lib\vtk-9.1.0.jar;

echo === CLASSPATH ===
echo %CLASSPATH%
echo =================
echo

echo Detecting memory requirements...
:: maxmimum memory in gigabytes. should be close to, but not over, total memory available
:: can set externally with VDO_MEM_GB environmental variable.
:: Set in PowerShell via ` $env:VDO_MEM_GB = 60 ` to use 60 GB.
:: Unset via ` Remove-Item Env:VDO_MEM_GB `
if not "%VDO_MEM_GB%"=="" (
	echo Using global environmental variable VDO_MEM_GB
	set MEM_GIGS=%VDO_MEM_GB%
) else (
	echo VDO_MEM_GB environmental variable is not set, will automatically detect maximum memory as 80%% of total system memory
	for /f "delims=" %%a in ('.get_memory.bat') do set /a "MEM_GIGS=(%%a * 80) / 100"
)

echo ===== JAVA ======
echo Launching with: %JAVA%
%JAVA% -version
echo =================

::"%JAVA%" -splash:resources\SCECVDOlogo.png -Xms1G -Xmx%MEM_GIGS%G -cp %CLASSPATH% org.scec.vtk.main.MainGUI
"%JAVA%" -splash:resources\SCECVDOlogo.png -Xms1G -Xmx20G -cp %CLASSPATH% org.scec.vtk.main.MainGUI

