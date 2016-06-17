package org.scec.vtk.plugins.ScriptingPlugin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.media.MediaLocator;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.opensha.commons.util.FileUtils;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginGUI;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.Earthquake;
import org.scec.vtk.tools.JpegImagesToMovie;
import org.scec.vtk.tools.Prefs;

import com.google.common.io.Files;

import vtk.vtkActor;
import vtk.vtkAnimationCue;
import vtk.vtkAnimationScene;
import vtk.vtkCamera;
import vtk.vtkCameraInterpolator;
import vtk.vtkPoints;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkRenderer;
import vtk.vtkUnsignedCharArray;

public class CueAnimator  {

	int ctFrames=0;
	int endFrame =0;
	int prevFrame =0;
	public boolean included;
	public ArrayList<EQCatalog> cat;
	void StartCue()
	{
		//System.out.println("*** IN StartCue " + scene.GetStartTime() );
		endFrame = (int) (FPS * scene.GetEndTime());
		//System.out.println(endFrame);
		this.TimerCount = 0;
		System.nanoTime();
		camnew = new vtkCamera();
		if(included)
			StartCueEarthquakeCatalogAniamtion();
		if (progressBar != null) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressBar.setString("Animating");
					progressBar.setStringPainted(true);
					progressBar.setMaximum(endFrame);
					progressBar.setValue(0);
				}
			});
		}
	}

	void TickCameraAniamtion()
	{
		//speed interpolation
		double t = scene.GetAnimationTime()/scene.GetEndTime();
		this.TimerCount = (int) ((1-t)*(1)+ t*(pointsPosition.GetNumberOfPoints()-1));
		if (progressBar != null) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressBar.setValue(TimerCount);
				}
			});
		}
		//System.out.println(ctFrames++);
		if(this.TimerCount<(pointsPosition.GetNumberOfPoints()-1))
		{
			//System.out.println(TimerCount);
			camnew.SetPosition(pointsPosition.GetPoint(TimerCount)[0],pointsPosition.GetPoint(TimerCount)[1],pointsPosition.GetPoint(TimerCount)[2]);
			camnew.SetFocalPoint(pointsFocalPoint.GetPoint(TimerCount)[0],pointsFocalPoint.GetPoint(TimerCount)[1],pointsFocalPoint.GetPoint(TimerCount)[2]);  
			camnew.SetViewUp(pointsViewUp.GetPoint(TimerCount)[0],pointsViewUp.GetPoint(TimerCount)[1],pointsViewUp.GetPoint(TimerCount)[2]);
			camnew.OrthogonalizeViewUp();


			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {	
						Info.getMainGUI().updateRenderWindow();//.GetRenderWindow().Render();
						Info.getMainGUI().getRenderWindow().GetRenderWindow().GetRenderers().GetFirstRenderer().SetActiveCamera(camnew);
						Info.getMainGUI().getRenderWindow().GetRenderWindow().GetRenderers().GetFirstRenderer().ResetCameraClippingRange();
					}
				});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(included)
			TickEarthquakeCatalogAniamtion();

		//capture frames depending on rendering fps by default is 30
		//				t = scene.GetAnimationTime()/scene.GetEndTime();
		//				this.TimerCount = (int) ((1-t)*(1)+ t*(endFrame+2)); //need 2 additional frames 
		//				if(TimerCount<endFrame+2 && prevFrame<TimerCount)
		//				{
		//					prevFrame = TimerCount;
		//					System.out.println(TimerCount);
		//				}
	}

	void TickCameraAniamtionRender()
	{

		//speed interpolation
		double t = scene.GetAnimationTime()/scene.GetEndTime();
		this.TimerCount = (int) ((1-t)*(1)+ t*(pointsPosition.GetNumberOfPoints()-1));
		if (progressBar != null) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressBar.setValue(TimerCount);
				}
			});
		}
		//System.out.println(ctFrames++);
		if(this.TimerCount<pointsPosition.GetNumberOfPoints()-1)
		{
			camnew.SetPosition(pointsPosition.GetPoint(TimerCount)[0],pointsPosition.GetPoint(TimerCount)[1],pointsPosition.GetPoint(TimerCount)[2]);
			camnew.SetFocalPoint(pointsFocalPoint.GetPoint(TimerCount)[0],pointsFocalPoint.GetPoint(TimerCount)[1],pointsFocalPoint.GetPoint(TimerCount)[2]);  
			camnew.SetViewUp(pointsViewUp.GetPoint(TimerCount)[0],pointsViewUp.GetPoint(TimerCount)[1],pointsViewUp.GetPoint(TimerCount)[2]);
			camnew.OrthogonalizeViewUp();
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {	
						Info.getMainGUI().getRenderWindow().GetRenderWindow().Render();
						Info.getMainGUI().getRenderWindow().GetRenderWindow().GetRenderers().GetFirstRenderer().SetActiveCamera(camnew);
						Info.getMainGUI().getRenderWindow().GetRenderWindow().GetRenderers().GetFirstRenderer().ResetCameraClippingRange();
					}
				});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(included)
			TickEarthquakeCatalogAniamtion();

		//capture frames depending on rendering fps by default is 30
		t = scene.GetAnimationTime()/scene.GetEndTime();
		this.TimerCount = (int) ((1-t)*(1)+ t*(endFrame+2)); //need 2 additional frames 
		if(TimerCount<endFrame+2 && prevFrame<TimerCount)
		{
			prevFrame = TimerCount;
			//System.out.println(TimerCount);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						int[] renderSize = Info.getMainGUI().getRenderWindow().GetRenderWindow().GetSize();
						int width =  renderSize[0];
						int height = renderSize[1];
						vtkUnsignedCharArray vtkPixelData = new vtkUnsignedCharArray();
						Info.getMainGUI().getRenderWindow().GetRenderWindow().GetPixelData(0, 0, width, height,
								1, vtkPixelData);
						ScriptingPluginGUI.imagePixelData.add(vtkPixelData);
					}
				});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	void StartCueEarthquakeCatalogAniamtion()
	{
		//System.out.println("*** IN StartCue " + scene.GetStartTime() );
		//this.TimerCount = 0;
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {	

					//System.out.println(t);
					if((int)Math.floor(scene.GetAnimationTime())==(int)scene.GetStartTime())
					{
						//System.out.println("EQptSize:"+EQPtSize);
						for(int j=0;j<cat.size();j++)
						{
							earthquakeList = cat.get(j).getSelectedEqList();
							double t = scene.GetStartTime()/scene.GetEndTime();
							TimerCount = (int) ((1-t)*(1)+ t*(earthquakeList.size()-1));

							for(int i=TimerCount;i<earthquakeList.size();i++)
							{
								eq = earthquakeList.get(i);
								EarthquakeCatalogPluginGUI.aniamteEarthquakeOpacity(i,eq,cat.get(j),0);
							}
						}
					}
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//scene.SetStartTime(scene.GetAnimationTime());
	}

	void TickEarthquakeCatalogAniamtion()
	{	
		for( j=0;j<cat.size();j++)
		{
			earthquakeList = cat.get(j).getSelectedEqList();
			double t = scene.GetAnimationTime()/scene.GetEndTime();
			this.TimerCount = (int) ((1-t)*(1)+ t*(earthquakeList.size()-1));
			//System.out.println(TimerCount);
			if(this.TimerCount<earthquakeList.size()-1)
			{
				for(int i=0;i<=TimerCount;i++)
				{
					eq = earthquakeList.get(i);
					EarthquakeCatalogPluginGUI.aniamteEarthquakeOpacity(i,eq,cat.get(j),255);
				}
				//if(this.TimerCount<earthquakeList.size()-1)
				//{
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {	

							//						eq = earthquakeList.get(TimerCount);
							//						 ArrayList eqActorList = EarthquakeCatalogPluginGUI.aniamteEarthquakeOpacity(TimerCount,eq,cat.get(j),255);
							//						
							//						Info.getMainGUI().addActors(eqActorList);
							Info.getMainGUI().getRenderWindow().GetRenderWindow().Render();
						}
					});
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//System.out.println("***  "+scene.GetAnimationTime());
	}

	void EndCue()
	{
		if (progressBar != null) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressBar.setValue(progressBar.getMaximum());
				}
			});
		}
		if(included )//&& ((int)Math.ceil(scene.GetAnimationTime())==(int)scene.GetEndTime()||this.TimerCount>=EQPtSize))
		{
			//showing all the earthquakes previously shown
			System.out.println("*** IN EndCue "+scene.GetAnimationTime());
			for(int j=0;j<cat.size();j++)
			{
				earthquakeList = cat.get(j).getSelectedEqList();
				//double t = scene.GetStartTime()/scene.GetEndTime();
				//this.TimerCount = (int) ((1-t)*(1)+ t*(earthquakeList.size()-1));

				for(int i=0;i<earthquakeList.size()-1;i++)
				{
					eq = earthquakeList.get(i);
					EarthquakeCatalogPluginGUI.aniamteEarthquakeOpacity(i,eq,cat.get(j),255);
				}
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {	
							Info.getMainGUI().updateRenderWindow();
						}
					});
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	void EndCueCameraAniamtionRender()
	{
		//width = 1920;//Info.getMainGUI().getRenderWindow().getWidth();
		//height = 1020;//Info.getMainGUI().getRenderWindow().getHeight();
		//System.out.println(width);
		//System.out.println(height);

		//Info.getMainGUI().getRenderWindow().GetRenderWindow().SetSize(renderSizeold[0],renderSizeold[1]);
		//process images only once
		if (progressBar != null) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					progressBar.setValue(0);
					progressBar.setString("Writing Image Files");
					progressBar.setStringPainted(true);
					progressBar.setMaximum(ScriptingPluginGUI.imagePixelData.size());
				}
			});
		}
		if((int)Math.ceil(scene.GetAnimationTime()) == (int) scene.GetEndTime()){
			System.out.println("*** IN EndCue writing images and processing..." );
			//Rendering movie
			new Thread(new Runnable() 
			{ 
				public void run() 
				{ 
					File tempDir = Files.createTempDir();
					System.out.println("img Size:" + ScriptingPluginGUI.imagePixelData.size());
					for(int i =0;i<ScriptingPluginGUI.imagePixelData.size();i++){
						if (progressBar != null) {
							final int frame = i;
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() {
									progressBar.setValue(frame);
								}
							});
						}
						// TODO just keep images in memory, never write to disk
						File file = new File(tempDir, "Capture" + i + ".jpg");				
						vtkPixelData = ScriptingPluginGUI.imagePixelData.get(i);
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
						imagesToConvert.add(file);
					}
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							progressBar.setIndeterminate(true);
							progressBar.setString("Writing Movie File");
							progressBar.setStringPainted(true);
						}
					});
					jpegToImages.doIt(width,height,FPS,imagesToConvert ,m);
					System.out.println("*** Finished Generating jpgs " );
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							progressBar.setIndeterminate(false);
							progressBar.setString("");
							progressBar.setStringPainted(false);
							progressBar.setValue(0);
						}
					});
					FileUtils.deleteRecursive(tempDir);
				}
			}).start();
		}
	}
	int j;
	int[] renderSizeold ;
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
	//ArrayList<vtkUnsignedCharArray> imagePixelData = new ArrayList<vtkUnsignedCharArray>(); 
	int[] renderSize = Info.getMainGUI().getRenderWindow().GetRenderWindow().GetSize();
	int width = renderSize[0];
	int height = renderSize[1];
	vtkUnsignedCharArray vtkPixelData = new vtkUnsignedCharArray();
	int FPS = 30;
	private JpegImagesToMovie jpegToImages = new JpegImagesToMovie();
	private List<File> imagesToConvert = new ArrayList<>();
	public MediaLocator m;
	public ArrayList<Earthquake> earthquakeList;
	private Earthquake eq;
	protected vtkAnimationCue cue;
	public int ptSize=0;
	public int EQPtSize=0;
	public vtkAnimationScene scene;
	private JProgressBar progressBar;
	
	void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}
}