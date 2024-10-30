# SCEC-VDO


Researchers and interns at the Southern California Earthquake Center (SCEC) have built a seismic data visualization software tool called the SCEC Virtual Display of Objects (SCEC-VDO). Written in Java with the Swing GUI toolkit to create interactive menus and the Visualization Toolkit (VTK) to render 3D content, SCEC-VDO allows for the visualization of 3D earthquake and fault objects on maps and the creation of images and movies for analysis, presentation, and publication. 

To download latest packaged version of SCEC-VDO visit: [https://strike.scec.org/scecpedia/SCEC_VDO](https://strike.scec.org/scecpedia/SCEC_VDO)


### Importing SCEC-VDO as an Eclipse project

1)   Launch Eclipse<br>
     Click on “Checkout Projects from Git” on start-up window<br>
     (Alternatively, you can go to File -> Import -> Git -> Projects from Git)
2)   Select Clone URI and click next
3)   Enter URI as: https://github.com/SCECcode/scec-vdo
4)   Check the master branch
5)   Specify a local path for the project to be exported and specify a remote name.
6)   Check import an existing project and specify the local path.
7)   Click Finish

### Steps for packaging SCEC-VDO
#### v24.11.0
First we build the SCEC-VDO.jar, bundled with its Manifest and compiled class files.
```ant create-jar```
If you just want to execute SCEC-VDO without packaging, you can use
* Apple Silicon Macs: `ant run-macOS-arm`
* Intel Macs: `ant run-macOS-x86`
* Linux: `ant run-linux`
* Windows: `ant run-windows`

We then package the jar into an application with specified dynamic library path,
external JARs and native libraries, and other resources and data.
Packaging for macOS requires us to create an application folder. We can do so
using Eclipse.

We package the JAR using packr: https://github.com/libgdx/packr
See the following document for detailed packaging instructions: TODO

#### v24.10.0
In each zip file, I add only the platform-specific script files, vtkLibs, and bundled JREs.
JREs available at https://developer.ibm.com/languages/java/semeru-runtimes/downloads/

In each launcher script I also define a JAVA_HOME to use the bundled JRE.
This code is present in the scripts in this repository and just need to be uncommented.

The following document provides support on how I added support for Apple Silicon Macs, how to build shared VTK libraries, and testing across platforms.
[M1 Mac Support Documentation](https://docs.google.com/document/d/16bD83jedZaHi_q-HEjz3SYC8Afct3LJUjrUpepj44Ec/edit?usp=sharing)

<!-- The following links are unavailable due to insufficient permissions.
Previous methods of packaging are unknown.


### Steps for packaging SCEC-VDO on Windows
Please refer to the steps in the following link:
[Package on Windows](https://drive.google.com/file/d/1-obw71GBGWEqQ6OoJglAgLLq6xxs-joy/view?usp=sharing)

### Steps for packaging SCEC-VDO on Mac

Please refer to the steps in the following link:
[Package on Mac](https://drive.google.com/open?id=16KSD43eVX6ebS-5oMUwmDkHriPAMAYzx)
-->

