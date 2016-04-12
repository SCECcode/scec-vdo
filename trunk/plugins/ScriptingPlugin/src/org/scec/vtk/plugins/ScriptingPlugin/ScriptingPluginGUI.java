package org.scec.vtk.plugins.ScriptingPlugin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.BoxLayout;
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
import org.scec.vtk.tools.Prefs;

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

/*class vtkTimerCallback
{
  private int TimerCount = 0; //start time = 0
  public vtkActor actor;
  public vtkRenderWindowInteractor iren;
  public vtkRenderer ren;
  public vtkCamera cam;
  vtkPoints points = new vtkPoints();
  vtkCameraRepresentation cameraRep = new vtkCameraRepresentation();
  vtkCameraInterpolator interpolator = new vtkCameraInterpolator();
	void Execute()
  {
		++this.TimerCount;
		if(this.TimerCount<points.GetNumberOfPoints()){
    cam.SetPosition(points.GetPoint(TimerCount));
    cam.SetFocalPoint(actor.GetPosition());
    System.out.println(this.TimerCount);
    System.out.println(cam.GetPosition()[0]);
    System.out.println(cam.GetPosition()[1]);
    System.out.println(cam.GetPosition()[2]);
    iren.GetRenderWindow().Render();	
		}
    //cameraRep.InitializePath();
    //cameraRep.AddCameraToPath();
	  
    //interpolator.InterpolateCamera(0, cam);
  }
}*/


/*class vtkAnimationCueObserver extends vtkCommand
{

 void Execute()
    {
      if(this.Animator!=null && this.Renderer!=null)
        {
        vtkAnimationCue info=
          static_cast<vtkAnimationCue::AnimationCueInfo *>(calldata);
        switch(event)
          {
          case vtkCommand::StartAnimationCueEvent:
            this.Animator.StartCue();
            break;
          case vtkCommand::EndAnimationCueEvent:
            this.Animator.EndCue();
            break;
          case vtkCommand::AnimationCueTickEvent:
            this.Animator.Tick(info);
            break;
          }
        }
      //if(this->RenWin!=0)
        //{
        this.RenWin.Render();
       // }
    }
 
  vtkRenderer Renderer;
  vtkRenderWindow RenWin;
  CueAnimator Animator;

  vtkAnimationCueObserver()
    {
     /* this.Renderer=0;
      this.Animator=0;
      this.RenWin=0;*/
    //}
//};*/
public class ScriptingPluginGUI extends JPanel implements ActionListener{

	/*class CueAnimator
	{

	  void StartCue()
	    {
	      System.out.println("*** IN StartCue " );
	      this.TimerCount = 0;
	      cam = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();
	      
	      //ren.Render();
	    }
	 
	  void Tick()
	    {
		  //System.out.println("*** IN timer" );
	      TimerCount +=
	        ((info.GetAnimationTime() -
	                             info.GetStartTime())/
	         (info.GetEndTime()-info.GetStartTime())) + 1;
		  //++this.TimerCount;
	      if(this.TimerCount<points.GetNumberOfPoints())
	      {
	    	    cam.SetPosition(points.GetPoint(TimerCount));
	    	    //cam.SetFocalPoint(actor.GetPosition());
	    	    System.out.println(this.TimerCount);
	    	    System.out.println(cam.GetPosition()[0]);
	    	    System.out.println(cam.GetPosition()[1]);
	    	    System.out.println(cam.GetPosition()[2]);
	    	    
	    	    Info.getMainGUI().updateRenderWindow();
	    	    //ren.Render();
	    	}
	      ren.Render();
	    }
	 
	  void EndCue()
	    {
	     // (void)ren;
	      // don't remove the actor for the regression image.
//	      ren->RemoveActor(this->Actor);
		  System.out.println("*** IN EndCue " );
	      //this.Cleanup();
	    }

	  public vtkRenderWindowInteractor iren;
	  public vtkRenderer ren;
	  public vtkCamera cam;
	  vtkPoints points = new vtkPoints();
	  public vtkActor actor;
	  int TimerCount = 0;
	  vtkAnimationCue info = new vtkAnimationCue();
	  void Cleanup()
	    {
	      /*if(this->SphereSource!=0)
	        {
	        this->SphereSource->Delete();
	        this->SphereSource=0;
	        }
	 
	      if(this->Mapper!=0)
	        {
	        this->Mapper->Delete();
	        this->Mapper=0;
	        }
	      if(this->Actor!=0)
	        {
	        this->Actor->Delete();
	        this->Actor=0;
	        }
	    }
	};*/
	
		private JPanel scriptingPluginSubPanelUpper;
		//private ArrayList<vtkActor> actorDrawingToolSegments;
		//private JPanel drawingToolSubPanelLower;
		//private ShowButton showDrawingToolsButton;
		//private ColorButton colorDrawingToolsButton;
		private AddButton addScriptingPluginButton;
		private ShowButton playScriptingPluginButton;
		//private RemoveButton remDrawingToolsButton;
		//private DrawingToolsTable DrawingToolTable;
		//private DrawingToolTable drawingToolObj;
		//private boolean loaded = false;
		//private EditButton editDrawingToolsButton;
		ArrayList<double[]> framePoints = new ArrayList<>();
		vtkPoints pointsToMoveCameraOn = new vtkPoints();
		vtkActor profile = new vtkActor();
		 vtkActor glyph = new vtkActor();
		@SuppressWarnings("deprecation")
		public ScriptingPluginGUI(ScriptingPlugin plugin){
			
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()));
			setName("Scripting Plugin");
			
			Dimension dSubPanel = new Dimension(Prefs.getPluginWidth(),100);
			
			this.scriptingPluginSubPanelUpper=new JPanel();
			this.scriptingPluginSubPanelUpper.setLayout(new BoxLayout(this.scriptingPluginSubPanelUpper, BoxLayout.Y_AXIS));
			this.addScriptingPluginButton = new AddButton(this, "Add new Text");
			this.playScriptingPluginButton = new ShowButton(this, "play");
			this.playScriptingPluginButton.setEnabled(true);
			this.scriptingPluginSubPanelUpper.add(this.addScriptingPluginButton);
			this.scriptingPluginSubPanelUpper.add(this.playScriptingPluginButton);
			add(this.scriptingPluginSubPanelUpper);
			
			//mouse event
			/*final vtkCellPicker cellPicker = new vtkCellPicker();
			 
		    // Show the point on the sphere the mouse is hovering over in the status bar
		    Info.getMainGUI().getRenderWindow().addMouseMotionListener(new MouseAdapter()
		    {
		    	 //public void mouseClicked(MouseEvent e)
			      //{
		    		 public void mousePressed(MouseEvent e) {
		                  if (e.getButton() == MouseEvent.BUTTON3) {
		     
		        // The call to Pick needs to be surrounded by lock and unlock to prevent crashes.
		    	  Info.getMainGUI().getRenderWindow().lock();
		        int pickSucceeded = cellPicker.Pick(e.getX(), Info.getMainGUI().getRenderWindow().getHeight()-e.getY()-1,
		            0.0, Info.getMainGUI().getRenderWindow().GetRenderer());
		        Info.getMainGUI().getRenderWindow().unlock();
		 
		        if (pickSucceeded == 1)
		        {
		          double[] p = cellPicker.GetPickPosition();
		          System.out.println("Position: " + p[0] + ", " + p[1] + ", " + p[2]);
		          framePoints.add(p);
		        }
			    	  }
		      }
		    });*/

		}
		
		public void createSplines()
		{
			 int numberOfInputPoints = framePoints.size();//8;
			 
			    // One spline for each direction.
			    vtkCardinalSpline aSplineX, aSplineY, aSplineZ;
			    aSplineX = new vtkCardinalSpline();
			    aSplineY = new vtkCardinalSpline();
			    aSplineZ = new vtkCardinalSpline();
			    vtkParametricSpline aspline = new vtkParametricSpline();

			/*  Generate random (pivot) points and add the corresponding
			 *  coordinates to the splines.
			 *  aSplineX will interpolate the x values of the points
			 *  aSplineY will interpolate the y values of the points
			 *  aSplineZ will interpolate the z values of the points */
			    vtkMath math=new vtkMath();
			    vtkPoints inputPoints = new vtkPoints();
			    for (int i=0; i<numberOfInputPoints; i++) {
			      double x = framePoints.get(i)[0]; //math.Random(0, 1);
			      double y = framePoints.get(i)[1]; //math.Random(0, 1);
			      double z = framePoints.get(i)[2]; //math.Random(0, 1);
			      aSplineX.AddPoint(i, x);
			      aSplineY.AddPoint(i, y);
			      aSplineZ.AddPoint(i, z);
			      //aspline.SetXSpline(id0);
			      inputPoints.InsertPoint(i, x, y, z);
			    } //i loop
			    aspline.SetPoints(inputPoints);
			    // The following section will create glyphs for the pivot points
			    // in order to make the effect of the spline more clear.

			    // Create a polydata to be glyphed.
			    vtkPolyData inputData = new vtkPolyData();
			    inputData.SetPoints(inputPoints);

			    // Use sphere as glyph source.
			    vtkSphereSource balls = new vtkSphereSource();
			      balls.SetRadius(.01);
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
			    
			    vtkPolyData profileData = new vtkPolyData();

			    
			   /* vtkParametricFunctionSource functionSource = new vtkParametricFunctionSource();
			    	  functionSource.SetParametricFunction(aspline);
			    	  functionSource.Update();*/
			    
			    // Number of points on the spline
			    int numberOfOutputPoints = 400;//functionSource.GetOutput().GetNumberOfPoints();//50;
			    //pointsToMoveCameraOn  = functionSource.GetOutput().GetPoints();
			    // Interpolate x, y and z by using the three spline filters and
			    // create new points
			    double t;
			    for (int i=0; i<numberOfOutputPoints; i++) {
			      t =
			(double)(numberOfInputPoints-1)/(double)(numberOfOutputPoints-1)*(double)i;
			      pointsToMoveCameraOn.InsertPoint(i, aSplineX.Evaluate(t), aSplineY.Evaluate(t),
			    		  aSplineZ.Evaluate(t));
			    }//i loop

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
			      
			      profileTubes.SetRadius(.005);

			    vtkPolyDataMapper profileMapper = new vtkPolyDataMapper();
			      profileMapper.SetInputConnection(profileTubes.GetOutputPort());

			    
			      profile.SetMapper(profileMapper);
			      profile.GetProperty().SetColor(1,0,0);
			      profile.GetProperty().SetDiffuseColor(1.0, 0.0, 0.0);
			      profile.GetProperty().SetSpecular(.3);
			      profile.GetProperty().SetSpecularPower(30);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Object src = e.getSource();
			if(src == this.addScriptingPluginButton)
			{
				//KeyFrame kf = new KeyFrame();
				//double[] campos = kf.getCamPos();
				//framePoints.add(campos);
				
				 vtkActor ac = new vtkActor();
			   	   ArrayList<vtkActor> actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
					 actorPoliticalBoundariesSegments = Info.getMainGUI().pbGUI.getPoliticalBoundaries();
					 
					 if(actorPoliticalBoundariesSegments.size()>0){
					 ac = actorPoliticalBoundariesSegments.get(4);
					 }
				
					
				double[] campos = ac.GetCenter();
				 System.out.println("Position: " + campos[0] + ", " + campos[1] + ", " + campos[2]);
				campos[0] = ac.GetCenter()[0]+2000;
				framePoints.add(campos);
				System.out.println("Position: " + campos[0] + ", " + campos[1] + ", " + campos[2]);
				campos = ac.GetCenter();
				campos[0] = ac.GetCenter()[0]-2000;
				framePoints.add(campos);
				System.out.println("Position: " + campos[0] + ", " + campos[1] + ", " + campos[2]);
				campos = ac.GetCenter();
				campos[1] = ac.GetCenter()[1]+2000;
				framePoints.add(campos);
				System.out.println("Position: " + campos[0] + ", " + campos[1] + ", " + campos[2]);
				campos = ac.GetCenter();
				campos[1] = ac.GetCenter()[1]-2000;
				framePoints.add(campos);
				System.out.println("Position: " + campos[0] + ", " + campos[1] + ", " + campos[2]);
				
				createSplines();
				
				//renderer.AddActor(glyph);
				ArrayList<vtkActor> nw = new ArrayList<>();
				nw.add(profile);
				nw.add(glyph);
				Info.getMainGUI().updateActors(nw);
				profile.VisibilityOff();
				Info.getMainGUI().updateRenderWindow(profile);
			}
			else if(src == this.playScriptingPluginButton)
				{
				vtkRenderWindowInteractor iren = new vtkRenderWindowInteractor();
				if(framePoints.size()>=2)
				{
					
					
					
					
					//Info.getMainGUI().getRenderWindow().GetRenderer().AddActor(profile);
					//Info.getMainGUI().getRenderWindow().GetRenderer().AddActor(glyph);
					//Info.getMainGUI().updateRenderWindow(glyph);
					//iren.SetRenderWindow(Info.getMainGUI().getRenderWindow().GetRenderWindow());
					
				  	//iren.Initialize();
				  		// Sign up to receive TimerEvent
				  		//vtkTimerCallback cb = new vtkTimerCallback();
					 vtkAnimationScene scene = new vtkAnimationScene();
					   //if (argc >= 2 && strcmp(argv[1],"real") == 0)
					   //  {
					     scene.SetModeToRealTime();//SetModeToSequence();//
					    // }
					   //else
					    // {
					     //.repaint();.scene->SetModeToSequence();
					     //}
					   scene.SetLoop(0);//loop once 
					   scene.SetFrameRate(1);
					   scene.SetStartTime(3);
					   scene.SetEndTime(20);

					   
					   System.out.println("start animation");
			             scene.Initialize();
			             double starttime = scene.GetStartTime();
			             double endtime = scene.GetEndTime();
			             double CurrentTime = scene.GetDeltaTime();
			             System.out.println(starttime);
			             System.out.println(endtime);
			             System.out.println(CurrentTime);
			             int TimerCount = 0;
			             
			             vtkPoints points = pointsToMoveCameraOn;
				   	      vtkCamera camold = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();
				   	      vtkActor ac = new vtkActor();
				   	   ArrayList<vtkActor> actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
						 actorPoliticalBoundariesSegments = Info.getMainGUI().pbGUI.getPoliticalBoundaries();
						 
						 if(actorPoliticalBoundariesSegments.size()>0){
						 ac = actorPoliticalBoundariesSegments.get(4);
						 }
			             
			             while (TimerCount<=points.GetNumberOfPoints())//endtime)
			             { ++TimerCount;
			             vtkCamera cam = new vtkCamera();
			   	      
			   	   //System.out.println(CurrentTime);
					if(TimerCount<points .GetNumberOfPoints())
			   	      {
			   	    	    cam.SetPosition(points.GetPoint(TimerCount));
			   	    	    cam.SetFocalPoint(ac.GetCenter());
			   	    	    System.out.println(TimerCount);
			   	    	    System.out.println(cam.GetPosition()[0]);
			   	    	    System.out.println(cam.GetPosition()[1]);
			   	    	    System.out.println(cam.GetPosition()[2]);
			   	    	    //renWin.GetRenderer().ResetCameraClippingRange();
			   	    	    Info.getMainGUI().updateRenderWindow();
			   	    	    //renWin.GetRenderer().ResetCameraClippingRange();
			   	    	   // renWin.GetRenderWindow().Render();
			   	    	 Info.getMainGUI().getRenderWindow().GetRenderer().SetActiveCamera(cam);
			   	    	    //renWin.GetRenderer().GetActiveCamera().UpdateViewport(renWin.GetRenderer());
			   	    	    //;
			   	    	 try {
							Thread.sleep(1 * 100);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			   	    	   // renWin.repaint();
			   	    	}
			             }
			             Info.getMainGUI().getRenderWindow().GetRenderer().SetActiveCamera(camold);
					   /*// Create an Animation Cue.
					   vtkAnimationCue cue1 = new vtkAnimationCue();
					   cue1.SetStartTime(5);
					   cue1.SetEndTime(13);
					   scene.AddCue(cue1);*/
					//CueAnimator cb = new CueAnimator();
					  //cb.actor = ;
					 // cb.cam = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();
					  //cb.points = pointsToMoveCameraOn;
					  //cb.iren = iren;
					  //cb.ren = Info.getMainGUI().getRenderWindow().GetRenderer();
					  //cb.info.SetStartTime(5);
					  //cb.info.SetEndTime(13);
					 // scene.AddObserver("StartAnimationCueEvent", cb, "StartCue");
					  //scene.AddObserver("EndAnimationCueEvent", cb, "EndCue");
					  //scene.AddObserver("AnimationCueTickEvent", cb, "Tick");
					    
					//scene.Play();
					//scene.Stop();
					  //int timerId = iren.CreateRepeatingTimer(100);
					  //iren.Start();//.InvokeEvent("TimerEvent");
				}
				
				else
				{
					 iren.InvokeEvent("EndInteractionEvent");
				}
				}
		}
}
