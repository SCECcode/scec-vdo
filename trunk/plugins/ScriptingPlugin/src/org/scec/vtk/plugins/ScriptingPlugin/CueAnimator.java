package org.scec.vtk.plugins.ScriptingPlugin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.media.MediaLocator;
import javax.swing.SwingUtilities;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginGUI;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.Earthquake;
import org.scec.vtk.tools.JpegImagesToMovie;
import org.scec.vtk.tools.Prefs;

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


	public boolean included;
	public ArrayList<EQCatalog> cat;
	void StartCue()
	{
		//System.out.println("*** IN StartCue " + scene.GetStartTime() );
		this.TimerCount = 0;
		System.nanoTime();
		camnew = new vtkCamera();
		if(included)
			StartCueEarthquakeCatalogAniamtion();
	}

	void TickCameraAniamtion()
	{
		//speed interpolation
		double t = scene.GetAnimationTime()/scene.GetEndTime();
		this.TimerCount = (int) ((1-t)*(1)+ t*(pointsPosition.GetNumberOfPoints()-1));
		if(this.TimerCount<ptSize)
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
	}

	void TickCameraAniamtionRender()
	{

		//speed interpolation
		double t = scene.GetAnimationTime()/scene.GetEndTime();
		this.TimerCount = (int) ((1-t)*(1)+ t*(pointsPosition.GetNumberOfPoints()-1));
		if(this.TimerCount<ptSize)
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
			System.out.println("EQptSize:"+EQPtSize);
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
		if(this.TimerCount<EQPtSize)//earthquakeList.size())
		{
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {	
						
						eq = earthquakeList.get(TimerCount);
						 ArrayList eqActorList = EarthquakeCatalogPluginGUI.aniamteEarthquakeOpacity(TimerCount,eq,cat.get(j),255);
						
						Info.getMainGUI().addActors(eqActorList);
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
		if(included )//&& ((int)Math.ceil(scene.GetAnimationTime())==(int)scene.GetEndTime()||this.TimerCount>=EQPtSize))
		{
			//showing all the earthquakes previously shown
			//System.out.println("*** IN EndCue "+scene.GetAnimationTime());
			for(int j=0;j<cat.size();j++)
			{
				earthquakeList = cat.get(j).getSelectedEqList();
				double t = scene.GetStartTime()/scene.GetEndTime();
				this.TimerCount = (int) ((1-t)*(1)+ t*(earthquakeList.size()-1));
				
			for(int i=0;i<TimerCount;i++)
			{
				eq = earthquakeList.get(i);
				EarthquakeCatalogPluginGUI.aniamteEarthquakeOpacity(i,eq,cat.get(j),255);
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
		if((int)Math.ceil(scene.GetAnimationTime()) == (int) scene.GetEndTime()){
		System.out.println("*** IN EndCue writing images and processing..." );
		//Rendering movie
		new Thread(new Runnable() 
		{ 
			public void run() 
			{ 
				//clean temp dir
				File oldfile = new File(Prefs.getLibLoc() + "/tmp/");
				File[] files = oldfile.listFiles();
				for (File f:files) 
		        {
					if (f.isFile() && f.exists()) 
		            { 
		        	f.delete();
		        	}
		        }
				for(int i =0;i<ScriptingPluginGUI.imagePixelData.size();i++){
					String fileName;
					fileName = Prefs.getLibLoc() + "/tmp/Capture" + i + ".jpg";
					File file = new File(fileName);				
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
					imagesToConvert.add(file.getAbsolutePath());
				}
				jpegToImages.doIt(width,height,FPS,imagesToConvert ,m);
				System.out.println("*** Finished Generating jpgs " );
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
	private Vector imagesToConvert = new Vector<>();
	public MediaLocator m;
	public ArrayList<Earthquake> earthquakeList;
	private Earthquake eq;
	protected vtkAnimationCue cue;
	public int ptSize=0;
	public int EQPtSize=0;
	public vtkAnimationScene scene;
}
