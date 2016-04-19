package org.scec.vtk.plugins.ScriptingPlugin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.MediaLocator;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

import org.scec.vtk.drawingTools.DrawingToolsPlugin;
import org.scec.vtk.drawingTools.DrawingToolsTable;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.utils.components.AddButton;
import org.scec.vtk.plugins.utils.components.ColorButton;
import org.scec.vtk.plugins.utils.components.EditButton;
import org.scec.vtk.plugins.utils.components.PlayButton;
import org.scec.vtk.plugins.utils.components.RemoveButton;
import org.scec.vtk.plugins.utils.components.ShowButton;
import org.scec.vtk.plugins.utils.components.StopButton;
import org.scec.vtk.tools.JpegImagesToMovie;
import org.scec.vtk.tools.Prefs;


import com.sun.media.jfxmedia.Media;

import sun.java2d.pipe.RenderBuffer;
import vtk.vtkActor;
import vtk.vtkAnimationCue;
import vtk.vtkAnimationScene;
import vtk.vtkCamera;
import vtk.vtkCameraInterpolator;
import vtk.vtkCameraRepresentation;
import vtk.vtkCardinalSpline;
import vtk.vtkCellArray;
import vtk.vtkCellPicker;
import vtk.vtkCommand;
import vtk.vtkGlyph3D;
import vtk.vtkJPEGWriter;
import vtk.vtkMath;
import vtk.vtkParametricFunctionSource;
import vtk.vtkParametricSpline;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkRenderWindow;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkRenderer;
import vtk.vtkSphereSource;
import vtk.vtkTubeFilter;
import vtk.vtkUnsignedCharArray;
import vtk.vtkWindowToImageFilter;

//TODO: UI and cleanup class del temp files
public class ScriptingPluginGUI extends JPanel implements ActionListener{

	class CueAnimator
	{

		void StartCue()
		{
			System.out.println("*** IN StartCue " );
			this.TimerCount = 0;
			camold = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();
		}

		void Tick()
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

		void EndCue()
		{
			System.out.println("*** IN EndCue " );
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
	};
	class CueAnimatorRender
	{

		void StartCue()
		{
			System.out.println("*** IN StartCue " );
			this.TimerCount = 0;
			camold = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();
			imagePixelData = new ArrayList<>();
		}

		void Tick()
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

		void EndCue()
		{
			System.out.println("*** IN EndCue " );
			//Rendering movie
			new Thread(new Runnable() 
			{ 
				public void run() 
				{ 

					for(int i =0;i<imagePixelData.size();i++){
						String fileName;
						fileName = Prefs.getLibLoc() + "/tmp/Capture" + i + ".jpeg";
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
		//.getRenderEnabledCanvas();
		int[] renderSize = Info.getMainGUI().getRenderWindow().GetRenderWindow().GetSize();
		int width = renderSize[0];
		int height = renderSize[1];
		vtkUnsignedCharArray vtkPixelData = new vtkUnsignedCharArray();
		int FPS = 15;
	};
	private JPanel scriptingPluginSubPanelUpper;
	private JFileChooser fc = new JFileChooser();
	protected File movieFile;
	private MediaLocator m;
	private AddButton addScriptingPluginButton;
	private PlayButton playScriptingPluginButton;
	private JButton// newButton, 
	renderButton;
	private StopButton stopScriptingPluginButton;

	ArrayList<vtkCamera> framePoints = new ArrayList<>();
	vtkPoints pointsToMoveCameraOnPosition = new vtkPoints();
	vtkPoints pointsToMoveCameraOnFocalPoint = new vtkPoints();
	vtkPoints pointsToMoveCameraOnViewUp = new vtkPoints();
	vtkActor profile = new vtkActor();
	vtkActor glyph = new vtkActor();
	private JpegImagesToMovie jpegToImages = new JpegImagesToMovie();
	private Vector imagesToConvert = new Vector<>();

	vtkActor focusActor = new vtkActor();
	boolean resetScene = false;

	@SuppressWarnings("deprecation")
	public ScriptingPluginGUI(ScriptingPlugin plugin){

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()));
		setName("Scripting Plugin");

		Dimension dSubPanel = new Dimension(Prefs.getPluginWidth(),100);

		this.scriptingPluginSubPanelUpper=new JPanel();
		this.scriptingPluginSubPanelUpper.setLayout(new BoxLayout(this.scriptingPluginSubPanelUpper, BoxLayout.Y_AXIS));
		this.addScriptingPluginButton = new AddButton(this, "Add new Text");
		this.addScriptingPluginButton.setEnabled(true);
		this.playScriptingPluginButton = new PlayButton(this, "Play");
		this.playScriptingPluginButton.setEnabled(true);
		this.stopScriptingPluginButton = new StopButton(this, "Stop");
		this.stopScriptingPluginButton.setEnabled(true);
		this.renderButton = new JButton("Render");
		this.renderButton.setEnabled(true);
		this.renderButton.addActionListener(this);
		this.scriptingPluginSubPanelUpper.add(this.addScriptingPluginButton);
		this.scriptingPluginSubPanelUpper.add(this.playScriptingPluginButton);
		this.scriptingPluginSubPanelUpper.add(this.renderButton);
		add(this.scriptingPluginSubPanelUpper);

		ArrayList<vtkActor> actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
		actorPoliticalBoundariesSegments = Info.getMainGUI().pbGUI.getPoliticalBoundaries();

		if(actorPoliticalBoundariesSegments.size()>0){
			focusActor = actorPoliticalBoundariesSegments.get(4);
		}

		Info.getMainGUI().getRenderWindow().addMouseListener(new MouseAdapter()
		{
			//public void mouseClicked(MouseEvent e)
			//{
			/* public void mousePressed(MouseEvent e) {
		                  if (e.getButton() == MouseEvent.BUTTON3) {
			     double[] pointerPosition = Info.getMainGUI().getPointerPosition();
			     framePoints.add(pointerPosition);
			     System.out.println("Position1: " + pointerPosition[0] + ", " + pointerPosition[1] + ", " + pointerPosition[2]);

			     if(framePoints.size()>=2){
						createSplines();

						//renderer.AddActor(glyph);
						ArrayList<vtkActor> nw = new ArrayList<>();
						nw.add(profile);
						nw.add(glyph);
						Info.getMainGUI().updateActors(nw);
						//profile.VisibilityOff();
						Info.getMainGUI().updateRenderWindow(profile);
						}
			     }
		    		 }*/
		});
	}

	public void createSplines(String interpolateValue)
	{
		int numberOfInputPoints = framePoints.size();//8;
		vtkPoints pointsToMoveCameraOn = new vtkPoints();
		//profile = new vtkActor();
		// One spline for each direction.
		vtkCardinalSpline aSplineX, aSplineY, aSplineZ;
		aSplineX = new vtkCardinalSpline();
		aSplineY = new vtkCardinalSpline();
		aSplineZ = new vtkCardinalSpline();



		/*  Generate random (pivot) points and add the corresponding
		 *  coordinates to the splines.
		 *  aSplineX will interpolate the x values of the points
		 *  aSplineY will interpolate the y values of the points
		 *  aSplineZ will interpolate the z values of the points */
		vtkMath math=new vtkMath();
		double x=0,y=0,z=0;
		vtkPoints inputPoints = new vtkPoints();
		for (int i=0; i<numberOfInputPoints; i++) {
			if(interpolateValue.equals("position"))
			{
				x= framePoints.get(i).GetPosition()[0]; //math.Random(0, 1);
				y = framePoints.get(i).GetPosition()[1]; //math.Random(0, 1);
				z = framePoints.get(i).GetPosition()[2]; //math.Random(0, 1);
			}
			////TODO remove focal points interpolation
			else if(interpolateValue.equals("focalPoints"))
			{
				x= framePoints.get(i).GetFocalPoint()[0]; //math.Random(0, 1);
				y = framePoints.get(i).GetFocalPoint()[1]; //math.Random(0, 1);
				z = framePoints.get(i).GetFocalPoint()[2]; //math.Random(0, 1);
			}
			else if(interpolateValue.equals("viewUp"))
			{
				x= framePoints.get(i).GetViewUp()[0]; //math.Random(0, 1);
				y = framePoints.get(i).GetViewUp()[1]; //math.Random(0, 1);
				z = framePoints.get(i).GetViewUp()[2]; //math.Random(0, 1);
			}	 
			aSplineX.AddPoint(i, x);
			aSplineY.AddPoint(i, y);
			aSplineZ.AddPoint(i, z);
			//aspline.SetXSpline(id0);
			inputPoints.InsertPoint(i, x, y, z);
		} //i loop

		// position draw splines
		if(interpolateValue.equals("position"))
		{
			// The following section will create glyphs for the pivot points
			// in order to make the effect of the spline more clear.

			// Create a polydata to be glyphed.
			vtkPolyData inputData = new vtkPolyData();
			inputData.SetPoints(inputPoints);

			// Use sphere as glyph source.
			vtkSphereSource balls = new vtkSphereSource();
			balls.SetRadius(3);//.01);
			balls.SetPhiResolution(10);
			balls.SetThetaResolution(10);

			vtkGlyph3D glyphPoints = new vtkGlyph3D();
			glyphPoints.SetInputData(inputData);
			glyphPoints.SetSourceConnection(balls.GetOutputPort());

			vtkPolyDataMapper glyphMapper = new vtkPolyDataMapper();
			glyphMapper.SetInputConnection(glyphPoints.GetOutputPort());


			glyph.SetMapper(glyphMapper);
			glyph.GetProperty().SetDiffuseColor(0.0, 1.0, 0.0);
			glyph.GetProperty().SetSpecular(.3);
			glyph.GetProperty().SetSpecularPower(30);

			// Generate the polyline for the spline.
		}
		vtkPolyData profileData = new vtkPolyData();

		// Number of points on the spline
		int numberOfOutputPoints = 400;//50;
		// Interpolate x, y and z by using the three spline filters and
		// create new points
		double t;
		for (int i=0; i<numberOfOutputPoints; i++) {
			t =
					(double)(numberOfInputPoints-1)/(double)(numberOfOutputPoints-1)*(double)i;
			pointsToMoveCameraOn.InsertPoint(i, aSplineX.Evaluate(t), aSplineY.Evaluate(t),
					aSplineZ.Evaluate(t));
		}//i loop
		if(interpolateValue.equals("position"))
		{
			pointsToMoveCameraOnPosition = pointsToMoveCameraOn;
		}
		else if(interpolateValue.equals("focalPoints"))
		{
			pointsToMoveCameraOnFocalPoint = pointsToMoveCameraOn;
		}
		else if(interpolateValue.equals("viewUp"))
		{
			pointsToMoveCameraOnViewUp = pointsToMoveCameraOn;
		}
		// position draw splines
		if(interpolateValue.equals("position"))
		{
			// Create the polyline.
			vtkCellArray lines = new vtkCellArray();
			lines.InsertNextCell(numberOfOutputPoints);
			for (int i=0; i<numberOfOutputPoints; i++) lines.InsertCellPoint(i);

			profileData.SetPoints(pointsToMoveCameraOn);
			profileData.SetLines(lines);

			// Add thickness to the resulting line.
			vtkTubeFilter profileTubes = new vtkTubeFilter();
			profileTubes.SetNumberOfSides(8);
			profileTubes.SetInputData(profileData);

			profileTubes.SetRadius(0.5);//.005);

			vtkPolyDataMapper profileMapper = new vtkPolyDataMapper();
			profileMapper.SetInputConnection(profileTubes.GetOutputPort());


			profile.SetMapper(profileMapper);
			profile.GetProperty().SetColor(1,0,0);
			profile.GetProperty().SetDiffuseColor(1.0, 0.0, 0.0);
			profile.GetProperty().SetSpecular(.3);
			profile.GetProperty().SetSpecularPower(30);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object src = e.getSource();
		vtkAnimationScene scene = new vtkAnimationScene();
		CueAnimator cb = new CueAnimator();
		if(src == this.addScriptingPluginButton)
		{

			KeyFrame kf = new KeyFrame();
			vtkCamera c = kf.getCamPos();
			vtkCamera c2 = new vtkCamera();
			c2.SetPosition(c.GetPosition()[0], c.GetPosition()[1], c.GetPosition()[2]);
			c2.SetFocalPoint(c.GetFocalPoint()[0], c.GetFocalPoint()[1], c.GetFocalPoint()[2]);
			c2.SetViewUp(c.GetViewUp()[0], c.GetViewUp()[1], c.GetViewUp()[2]);
			framePoints.add(c2);
			int s= framePoints.size();
			System.out.println(framePoints.get(s-1).GetPosition()[0]);
			System.out.println(framePoints.get(s-1).GetPosition()[1]);
			System.out.println(framePoints.get(s-1).GetPosition()[2]);
			if(framePoints.size()>=2){
				createSplines("position");
				createSplines("focalPoints");
				createSplines("viewUp");

				//renderer.AddActor(glyph);
				ArrayList<vtkActor> nw = new ArrayList<>();
				nw.add(profile);
				nw.add(glyph);
				Info.getMainGUI().updateActors(nw);
				//profile.VisibilityOff();
				//Info.getMainGUI().updateRenderWindow(profile);
			}
			resetScene = false;
		}
		else if(src == this.playScriptingPluginButton)
		{

			if(framePoints.size()>=2)
			{
				//resetScene = true;
				//scene = new vtkAnimationScene();
				scene.RemoveAllCues();
				//the frame rate affects sequence mode
				scene.SetModeToSequence();//SetModeToRealTime();//

				scene.SetLoop(0);//loop once 
				scene.SetFrameRate(15);
				scene.SetStartTime(3);
				scene.SetEndTime(20);

				// Create an Animation Cue.
				vtkAnimationCue cue1 = new vtkAnimationCue();
				cue1.SetStartTime(5);
				cue1.SetEndTime(13);
				scene.AddCue(cue1);
				cb = new CueAnimator();


				cb.pointsPosition = pointsToMoveCameraOnPosition;
				cb.pointsFocalPoint = pointsToMoveCameraOnFocalPoint;
				cb.pointsViewUp = pointsToMoveCameraOnViewUp;
				cb.actor = focusActor;
				scene.AddObserver("StartAnimationCueEvent", cb, "StartCue");
				scene.AddObserver("EndAnimationCueEvent", cb, "EndCue");
				scene.AddObserver("AnimationCueTickEvent", cb, "Tick");

				cb.camold = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();

				scene.Play();
				scene.Stop();
				Info.getMainGUI().getRenderWindow().GetRenderer().SetActiveCamera(cb.camold);
			}

		}
		else if (src == this.renderButton)
		{
			int returnVal = fc.showSaveDialog(new JFrame("Save movie"));
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				movieFile = fc.getSelectedFile();
				// it's a movie render
				File tmpDir = new File(Prefs.getLibLoc()+"/tmp/");
				if (!tmpDir.exists())
					tmpDir.mkdir();

				if(framePoints.size()>=2)
				{

					scene.RemoveAllCues();
					//the frame rate affects sequence mode
					scene.SetModeToRealTime();//SetModeToSequence();//

					scene.SetLoop(0);//loop once 
					scene.SetFrameRate(15);
					scene.SetStartTime(3);
					scene.SetEndTime(20);

					// Create an Animation Cue.
					vtkAnimationCue cue1 = new vtkAnimationCue();
					cue1.SetStartTime(5);
					cue1.SetEndTime(13);
					scene.AddCue(cue1);
					CueAnimatorRender cb1 = new CueAnimatorRender();

					ArrayList<vtkActor> actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
					actorPoliticalBoundariesSegments = Info.getMainGUI().pbGUI.getPoliticalBoundaries();

					if(actorPoliticalBoundariesSegments.size()>0){
						cb1.actor = actorPoliticalBoundariesSegments.get(4);
					}

					cb1.camold = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();
					cb1.pointsPosition = pointsToMoveCameraOnPosition;
					cb1.pointsFocalPoint = pointsToMoveCameraOnFocalPoint;
					cb1.pointsViewUp = pointsToMoveCameraOnViewUp;
					cue1.AddObserver("StartAnimationCueEvent", cb1, "StartCue");
					cue1.AddObserver("EndAnimationCueEvent", cb1, "EndCue");
					cue1.AddObserver("AnimationCueTickEvent", cb1, "Tick");

					scene.Play();
					scene.Stop();
					//render image files to movie
					try {
						m = new MediaLocator(movieFile.toURL()+ ".mov");
						System.out.println("Writing " + m.getURL());
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					Info.getMainGUI().getRenderWindow().GetRenderer().SetActiveCamera(cb1.camold);
				}
			}

		}
		else	 if(src == this.stopScriptingPluginButton)
		{
			scene.Stop();
		}
	}

}
