package org.scec.vtk.plugins.ScriptingPlugin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import org.scec.vtk.plugins.utils.components.StopButton;
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
	
		private JPanel scriptingPluginSubPanelUpper;

		private AddButton addScriptingPluginButton;
		private PlayButton playScriptingPluginButton;
		private StopButton stopScriptingPluginButton;

		ArrayList<vtkCamera> framePoints = new ArrayList<>();
		vtkPoints pointsToMoveCameraOnPosition = new vtkPoints();
		vtkPoints pointsToMoveCameraOnFocalPoint = new vtkPoints();
		vtkPoints pointsToMoveCameraOnViewUp = new vtkPoints();
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
			this.addScriptingPluginButton.setEnabled(true);
			this.playScriptingPluginButton = new PlayButton(this, "Play");
			this.playScriptingPluginButton.setEnabled(true);
			this.stopScriptingPluginButton = new StopButton(this, "Stop");
			this.stopScriptingPluginButton.setEnabled(true);
			this.scriptingPluginSubPanelUpper.add(this.addScriptingPluginButton);
			this.scriptingPluginSubPanelUpper.add(this.playScriptingPluginButton);
			add(this.scriptingPluginSubPanelUpper);
			
			
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
				
			}
			else if(src == this.playScriptingPluginButton)
				{
				vtkRenderWindowInteractor iren = new vtkRenderWindowInteractor();
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
					Info.getMainGUI().updateRenderWindow(profile);
					}
				if(framePoints.size()>=2)
				{


					 //the frame rate affects sequence mode
					     scene.SetModeToRealTime();//SetModeToSequence();//

					   scene.SetLoop(0);//loop once 
					   scene.SetFrameRate(5);
					   scene.SetStartTime(3);
					   scene.SetEndTime(20);

					   // Create an Animation Cue.
					   vtkAnimationCue cue1 = new vtkAnimationCue();
					   cue1.SetStartTime(5);
					   cue1.SetEndTime(13);
					   scene.AddCue(cue1);
					   CueAnimator cb = new CueAnimator();
					   
					   	   ArrayList<vtkActor> actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
							 actorPoliticalBoundariesSegments = Info.getMainGUI().pbGUI.getPoliticalBoundaries();
							 
							 if(actorPoliticalBoundariesSegments.size()>0){
								 cb.actor = actorPoliticalBoundariesSegments.get(4);
							 }
					    
					   cb.camold = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();
					   cb.pointsPosition = pointsToMoveCameraOnPosition;
					   cb.pointsFocalPoint = pointsToMoveCameraOnFocalPoint;
					   cb.pointsViewUp = pointsToMoveCameraOnViewUp;
					   cue1.AddObserver("StartAnimationCueEvent", cb, "StartCue");
					   cue1.AddObserver("EndAnimationCueEvent", cb, "EndCue");
					   cue1.AddObserver("AnimationCueTickEvent", cb, "Tick");
					    
					  scene.Play();
					  scene.Stop();
					  Info.getMainGUI().getRenderWindow().GetRenderer().SetActiveCamera(cb.camold);
				}
				 if(src == this.stopScriptingPluginButton)
				{
					scene.Stop();
				}
				else
				{
					 iren.InvokeEvent("EndInteractionEvent");
				}
				}
		}

}
