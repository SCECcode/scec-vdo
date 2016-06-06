package org.scec.vtk.plugins.ScriptingPlugin;

import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.MediaLocator;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.Earthquake;
import org.scec.vtk.tools.JpegImagesToMovie;
import org.scec.vtk.tools.Prefs;

import vtk.vtkActor;
import vtk.vtkAnimationCue;
import vtk.vtkCamera;
import vtk.vtkCameraInterpolator;
import vtk.vtkPoints;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkRenderer;
import vtk.vtkUnsignedCharArray;

public class CueAnimator {

		
		
		void StartCue()
		{
			System.out.println("*** IN StartCue " );
			this.TimerCount = 0;
			//camold = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();
		}

		void TickCameraAniamtion()
		{
			camnew = new vtkCamera();
			++this.TimerCount;

			if(this.TimerCount<pointsPosition.GetNumberOfPoints())
			{

				camnew.SetPosition(pointsPosition.GetPoint(TimerCount)[0],pointsPosition.GetPoint(TimerCount)[1],pointsPosition.GetPoint(TimerCount)[2]);
				camnew.SetFocalPoint(pointsFocalPoint.GetPoint(TimerCount)[0],pointsFocalPoint.GetPoint(TimerCount)[1],pointsFocalPoint.GetPoint(TimerCount)[2]);  
				camnew.SetViewUp(pointsViewUp.GetPoint(TimerCount)[0],pointsViewUp.GetPoint(TimerCount)[1],pointsViewUp.GetPoint(TimerCount)[2]);
				camnew.OrthogonalizeViewUp();
				Info.getMainGUI().updateRenderWindow();
				Info.getMainGUI().getRenderWindow().GetRenderer().SetActiveCamera(camnew);
				Info.getMainGUI().getRenderWindow().GetRenderer().ResetCameraClippingRange();
			}
		}
		
		void TickCameraAniamtionRender()
		{
			camnew = new vtkCamera();
			++this.TimerCount;

			if(this.TimerCount<pointsPosition.GetNumberOfPoints())
			{
				camnew.SetPosition(pointsPosition.GetPoint(TimerCount)[0],pointsPosition.GetPoint(TimerCount)[1],pointsPosition.GetPoint(TimerCount)[2]);
				camnew.SetFocalPoint(pointsFocalPoint.GetPoint(TimerCount)[0],pointsFocalPoint.GetPoint(TimerCount)[1],pointsFocalPoint.GetPoint(TimerCount)[2]);  
				camnew.SetViewUp(pointsViewUp.GetPoint(TimerCount)[0],pointsViewUp.GetPoint(TimerCount)[1],pointsViewUp.GetPoint(TimerCount)[2]);
				camnew.OrthogonalizeViewUp();
				Info.getMainGUI().updateRenderWindow();
				Info.getMainGUI().getRenderWindow().GetRenderer().SetActiveCamera(camnew);
				Info.getMainGUI().getRenderWindow().GetRenderer().ResetCameraClippingRange();
				//capture screenshot
				int[] renderSize = Info.getMainGUI().getRenderWindow().GetRenderWindow().GetSize();
				int width = renderSize[0];
				int height = renderSize[1];
				vtkUnsignedCharArray vtkPixelData = new vtkUnsignedCharArray();
				Info.getMainGUI().getRenderWindow().GetRenderWindow().GetPixelData(0, 0, width, height,
						1, vtkPixelData);
				imagePixelData.add(vtkPixelData);
			}
		}
		
		void StartCueEarthquakeCatalogAniamtion()
		{
			System.out.println("*** IN StartCue " );
			this.TimerCount = 0;
			//camold = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();
			for(int i =0;i<earthquakeList.size();i++)
			{
				eq = earthquakeList.get(i);
				eq.getEarthquakeCatalogActor().VisibilityOff();
			}
		}
		
		void TickEarthquakeCatalogAniamtion()
		{
			if(this.TimerCount<earthquakeList.size())
			{
				eq = earthquakeList.get(this.TimerCount);
				eq.getEarthquakeCatalogActor().VisibilityOn();
				Info.getMainGUI().updateRenderWindow();
			}
			++this.TimerCount;
		}

		void EndCue()
		{
			System.out.println("*** IN EndCue " );
		}

		void EndCueCameraAniamtionRender()
		{
			System.out.println("*** IN EndCue " );
			//Rendering movie
			new Thread(new Runnable() 
			{ 
				public void run() 
				{ 

					for(int i =0;i<imagePixelData.size();i++){
						String fileName;
						fileName = Prefs.getLibLoc() + "/tmp/Capture" + i + ".jpg";
						File file = new File(fileName);

						vtkPixelData = imagePixelData.get(i);
						BufferedImage bufImage = new BufferedImage(width, height,
								BufferedImage.TYPE_INT_RGB);
						int[] rgbArray = new int[(width) * (height)];
						int index, r, g, b;
						double[] rgbFloat;
						// bad performance because one has to get the values out of the vtk find a workaround jpeg writer
						// data structure tuple by tuple (instead of one "copyToArray") ...
						for (int y = 0; y < height; y++) {
							for (int x = 0; x < width; x++) {
								index = ((y * (width + 1)) + x);
								rgbFloat = vtkPixelData.GetTuple3(index);
								r = (int) rgbFloat[0];
								g = (int) rgbFloat[1];
								b = (int) rgbFloat[2];
								// vtk window origin: bottom left, Java image origin: top left
								rgbArray[((height -1 - y) * (width)) + x] =
										((r << 16) + (g << 8) + b);
							}
						}
						bufImage.setRGB(0, 0, width, height, rgbArray, 0, width);
						try {
							ImageIO.write(bufImage, "jpg", file);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						imagesToConvert.add(file.getAbsolutePath());
					}
					jpegToImages.doIt(Info.getMainGUI().getRenderWindow().getWidth(),Info.getMainGUI().getRenderWindow().getHeight(),FPS,imagesToConvert ,m);
				}
			}).start();
			System.out.println("*** Finished Generating jpgs " );
		}

		
		public vtkRenderWindowInteractor iren;
		public vtkRenderer ren;
		public vtkCamera cam;
		public vtkCamera camnew;
		public vtkCamera camold;
		vtkPoints pointsPosition = new vtkPoints();
		vtkPoints pointsFocalPoint = new vtkPoints();
		vtkPoints pointsViewUp = new vtkPoints();
		public vtkActor actor;
		int TimerCount = 0;
		vtkAnimationCue info = new vtkAnimationCue();
		vtkCameraInterpolator incam = new vtkCameraInterpolator();
		ArrayList<vtkUnsignedCharArray> imagePixelData = new ArrayList<vtkUnsignedCharArray>(); 
		int[] renderSize = Info.getMainGUI().getRenderWindow().GetRenderWindow().GetSize();
		int width = renderSize[0];
		int height = renderSize[1];
		vtkUnsignedCharArray vtkPixelData = new vtkUnsignedCharArray();
		int FPS = 15;
		private JpegImagesToMovie jpegToImages = new JpegImagesToMovie();
		private Vector imagesToConvert = new Vector<>();
		public MediaLocator m;
		public ArrayList<Earthquake> earthquakeList;
		private Earthquake eq;
}
