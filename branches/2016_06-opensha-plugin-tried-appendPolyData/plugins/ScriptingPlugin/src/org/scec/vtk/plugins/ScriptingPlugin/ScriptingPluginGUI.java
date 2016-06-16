package org.scec.vtk.plugins.ScriptingPlugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import javax.media.MediaLocator;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;
import org.scec.vtk.plugins.utils.components.AddButton;
import org.scec.vtk.plugins.utils.components.PauseButton;
import org.scec.vtk.plugins.utils.components.PlayButton;
import org.scec.vtk.plugins.utils.components.RemoveButton;
import org.scec.vtk.plugins.utils.components.StopButton;
import org.scec.vtk.tools.Prefs;


import vtk.vtkActor;
import vtk.vtkAnimationCue;
import vtk.vtkAnimationScene;
import vtk.vtkCamera;
import vtk.vtkCardinalSpline;
import vtk.vtkCellArray;
import vtk.vtkGlyph3D;
import vtk.vtkMath;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;
import vtk.vtkTubeFilter;
import vtk.vtkUnsignedCharArray;

//TODO: UI and cleanup class del temp files
public class ScriptingPluginGUI extends JPanel implements ActionListener, MouseListener{

	private JPanel scriptingPluginSubPanelUpper;
	private JFileChooser fc = new JFileChooser();
	protected File movieFile;
	private MediaLocator m;
	private AddButton addScriptingPluginButton;
	private RemoveButton removeScriptingPluginButton;
	private PlayButton playScriptingPluginButton;
	private PauseButton pauseScriptingPluginButton;
	private JButton renderButton;
	private JButton renderPauseButton;
	private StopButton stopScriptingPluginButton;
	ArrayList<vtkCamera> framePoints = new ArrayList<>();
	vtkPoints pointsToMoveCameraOnPosition = new vtkPoints();
	vtkPoints pointsToMoveCameraOnFocalPoint = new vtkPoints();
	vtkPoints pointsToMoveCameraOnViewUp = new vtkPoints();
	vtkActor profile = new vtkActor();
	vtkActor glyph = new vtkActor();
	public static ArrayList<vtkUnsignedCharArray> imagePixelData = new ArrayList<vtkUnsignedCharArray>(); 
	//private JpegImagesToMovie jpegToImages = new JpegImagesToMovie();
	//private Vector imagesToConvert = new Vector<>();
	ArrayList<Integer> inputPtIndex = new ArrayList<Integer>();
	vtkActor focusActor = new vtkActor();
	boolean resetScene = false;
	vtkAnimationScene scene = new vtkAnimationScene();
	CueAnimator cb = new CueAnimator();

	//end time line frame #
	JTextField endTime = new JTextField();
	//frame rate
	JTextField noOfFrames = new JTextField();
	//timeline view table
	DefaultTableModel model = new DefaultTableModel();
	JTable table = new JTable(model);
	int selectedRow=0,selectedCol=1;
	private double tickrate;

	Boolean included=false;
	private boolean rendering;
	private boolean play=false;
	private boolean stop=true;
	private ArrayList<EQCatalog> catlogs = new ArrayList<>();
	JScrollPane timelineTableContainer = new JScrollPane(table);

	public ScriptingPluginGUI(ScriptingPlugin plugin){
		imagePixelData = new ArrayList<vtkUnsignedCharArray>(); 
		this.framePoints.add(new vtkCamera());
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()));
		setName("Scripting Plugin");

		new Dimension(Prefs.getPluginWidth(),100);

		this.scriptingPluginSubPanelUpper=new JPanel();
		//this.scriptingPluginSubPanelUpper.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
		this.scriptingPluginSubPanelUpper.setLayout(new  BoxLayout(scriptingPluginSubPanelUpper, BoxLayout.PAGE_AXIS));//GridLayout(3, 2, 15, 30));//new BoxLayout(this.scriptingPluginSubPanelUpper,BoxLayout.LINE_AXIS));
		this.scriptingPluginSubPanelUpper.setOpaque(true);
		//this.scriptingPluginSubPanelUpper.setLayout(new BorderLayout());//new BoxLayout(this.scriptingPluginSubPanelUpper, BoxLayout.Y_AXIS));
		this.addScriptingPluginButton = new AddButton(this, "Add new key frame");
		this.addScriptingPluginButton.setEnabled(true);
		this.playScriptingPluginButton = new PlayButton(this, "Play");
		this.playScriptingPluginButton.setEnabled(true);
		this.stopScriptingPluginButton = new StopButton(this, "Stop");
		this.stopScriptingPluginButton.setEnabled(true);
		this.pauseScriptingPluginButton = new PauseButton(this, "Pause");
		this.pauseScriptingPluginButton.setEnabled(true);
		this.renderButton = new JButton("Render");
		this.renderPauseButton = new JButton("Pause Renderer");
		this.renderButton.setEnabled(true);
		this.renderButton.addActionListener(this);
		this.renderPauseButton.setEnabled(true);
		this.renderPauseButton.addActionListener(this);


		endTime = new JTextField(5);
		endTime.setPreferredSize( new Dimension( 150, 20) );
		endTime.setText("15");
		//frame rate
		noOfFrames = new JTextField(5);
		noOfFrames.setPreferredSize( new Dimension( 150, 20 ) );
		noOfFrames.setText("140");
		endTime.addActionListener(this);
		noOfFrames.addActionListener(this);
		JPanel upperHalfPanel = new JPanel();
		upperHalfPanel.setLayout(new  FlowLayout(FlowLayout.CENTER,5,Prefs.getPluginHeight()-450));
		JPanel timelinePropPanel = new JPanel(); 
		timelinePropPanel.setLayout(new  FlowLayout(FlowLayout.CENTER,5,5));
		timelinePropPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		timelinePropPanel.add(new JLabel("End Time(sec)"));
		timelinePropPanel.add(endTime);

		timelinePropPanel.add(new JLabel("Number of Frames"));
		timelinePropPanel.add(noOfFrames);

		JPanel timelineButtonPanel = new JPanel(); 
		timelineButtonPanel.setLayout(new  FlowLayout(FlowLayout.CENTER,5,5));
		timelineButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		//timelinePropPanel.add(Box.createVerticalStrut(buttonSpace));
		timelineButtonPanel.add(new JLabel("Timeline buttons"));
		timelineButtonPanel.add(this.playScriptingPluginButton);
		timelineButtonPanel.add(this.stopScriptingPluginButton);
		timelineButtonPanel.add(this.pauseScriptingPluginButton);
		timelineButtonPanel.add(this.renderButton);
		timelineButtonPanel.add(this.renderPauseButton);

		JPanel timelineKeyFramePanel = new JPanel(); 
		timelineKeyFramePanel.setLayout(new  FlowLayout(FlowLayout.CENTER,5,5));
		timelineKeyFramePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		timelineKeyFramePanel.add(new JLabel("Add or remove key frames"));
		timelineKeyFramePanel.add(this.addScriptingPluginButton);

		this.removeScriptingPluginButton = new RemoveButton(this, "Remove selected key frame");
		this.removeScriptingPluginButton.setEnabled(true);
		timelineKeyFramePanel.add(this.removeScriptingPluginButton);
		// timelinePropPanel.setv.setPreferredSize(new Dimension(Prefs.getPluginWidth(),20));
		//timelinePropPanel.setPreferredSize(new Dimension(Prefs.getPluginWidth(),200));
		//timelineButtonPanel.setPreferredSize(new Dimension(Prefs.getPluginWidth(),200));
		//timelineKeyFramePanel.setPreferredSize(new Dimension(Prefs.getPluginWidth(),200));
		this.scriptingPluginSubPanelUpper.add(upperHalfPanel);
		this.scriptingPluginSubPanelUpper.add(timelinePropPanel);
		this.scriptingPluginSubPanelUpper.add(timelineButtonPanel);
		this.scriptingPluginSubPanelUpper.add(timelineKeyFramePanel);


		scene = new vtkAnimationScene();
		add(this.scriptingPluginSubPanelUpper);
		//timeline view panel
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		timelineTableContainer.setName("timeline");
		timelineTableContainer.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		timelineTableContainer.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		timelineTableContainer.setPreferredSize(new Dimension(Prefs.getPluginWidth(), 100));
		//tableContainer.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		// panel.add(tableContainer, BorderLayout.WEST);
		//add(panel);
		for(int i =0;i<Info.getMainGUI().getmainFrame().getComponentCount();i++)
		{
			if(!(Info.getMainGUI().getmainFrame().getComponent(i).getName()==timelineTableContainer.getName()))
			{
				Info.getMainGUI().getmainFrame().add(timelineTableContainer,BorderLayout.SOUTH);
			}
		}
		table.addMouseListener(this); 
		//swap table values drag and drop
		table.setDragEnabled(true);
		table.setDropMode(DropMode.USE_SELECTION);
		table.setTransferHandler(new TransferHandler(){

			int oldRow,oldCol;
			public int getSourceActions(JComponent c) {
				return DnDConstants.ACTION_COPY_OR_MOVE;
			}

			public Transferable createTransferable(JComponent comp)
			{
				JTable table=(JTable)comp;
				int row=table.getSelectedRow();
				int col=table.getSelectedColumn();
				oldRow = row;oldCol=col;
				String value = (String)table.getModel().getValueAt(row,col);
				StringSelection transferable = new StringSelection(value);
				table.getModel().setValueAt(null,row,col);
				return transferable;
			}
			public boolean canImport(TransferHandler.TransferSupport info){
				if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)){
					return false;
				}

				return true;
			}

			public boolean importData(TransferSupport support) {

				if (!support.isDrop()) {
					return false;
				}

				if (!canImport(support)) {
					return false;
				}

				JTable table=(JTable)support.getComponent();
				DefaultTableModel tableModel=(DefaultTableModel)table.getModel();

				JTable.DropLocation dl = (JTable.DropLocation)support.getDropLocation();

				int row = dl.getRow();
				int col=dl.getColumn();

				String data, exportData;
				try {
					exportData = (String) table.getValueAt(row, col);
					data = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
				} catch (UnsupportedFlavorException e) {
					return false;
				} catch (IOException e) {
					return false;
				}
				vtkCamera tempCam = new vtkCamera();
				tempCam = framePoints.get(oldCol);
				framePoints.set(oldCol, framePoints.get(col));
				System.out.println(framePoints.get(oldCol).GetPosition()[0]);
				framePoints.set(col,tempCam);
				System.out.println(framePoints.get(col).GetPosition()[0]);


				tableModel.setValueAt(data, row, col);
				tableModel.setValueAt(exportData, oldRow, oldCol);

				return true;
			}

		});

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
		//display default number of frames
		model.addRow(new Object[] {""});
		model.addColumn(new String(""));
		table.setValueAt("Camera",0,0);
		for(int i =1;i<=Integer.parseInt(noOfFrames.getText());i++)
		{
			model.addColumn(new String(Integer.toString(i)));
			table.setValueAt("",0,i);
			framePoints.add(new vtkCamera());
		}

	}

	void addLayerToTimeLine()
	{
		int oldColumnCount = model.getColumnCount();
		model.addRow(new Object[] {""});
		/* for(int i =0;i<Integer.parseInt(noOfFrames.getText());i++)
	        {
			  if(i>=oldColumnCount)
	        	{
				  model.addColumn(new String(Integer.toString(i+1)));
				  table.setValueAt("1",model.getRowCount()-1,i);
				  framePoints.add(new vtkCamera());
	        	}
			  else{
	        	table.setValueAt("1",model.getRowCount()-1,i);
	          }
	        }*/
		int numberOfFrames = this.catlogs.get(this.catlogs.size()-1).getSelectedEqList().size();
		table.setValueAt("EQCatalog-"+(this.catlogs.size()-1),model.getRowCount()-1,0);
		for(int i =1;i<=numberOfFrames;i++)
		{
			if(i>=oldColumnCount)
			{
				model.addColumn(new String(Integer.toString(i)));
				table.setValueAt("1",model.getRowCount()-1,i);
				framePoints.add(new vtkCamera());
			}
			else{
				table.setValueAt("1",model.getRowCount()-1,i);
			}
		}
	}
	void updateFramesInTimeLine()
	{
		int oldColumnCount = model.getColumnCount();

		for(int i =0;i<model.getRowCount();i++)
		{
			int j =0;
			for(j =1;j<=Integer.parseInt(noOfFrames.getText());j++)
			{
				if(j>=oldColumnCount)
				{
					model.addColumn(new String(Integer.toString(j+1)));
					table.setValueAt("",i,j);
					framePoints.add(new vtkCamera());
				}
				else
				{
					table.setValueAt(model.getValueAt(i, j),i,j);
				}
			}
			//if frames size has reduced
			if(j==Integer.parseInt(noOfFrames.getText()) && j<oldColumnCount)
			{
				for(int k =j,remCt=0;k<(oldColumnCount);k++,remCt++)
				{
					table.removeColumn(table.getColumnModel().getColumn(k-remCt));
					framePoints.remove(k-remCt);
				}
				model.setColumnCount(Integer.parseInt(noOfFrames.getText()));
			}
		}

	}

	public void addEarthquakeListForAniamtion(EQCatalog cat, Boolean included)
	{
		//this.list = list;
		this.included=included;
		//ArrayList earthquakeList = (ArrayList) list;
		if(!this.catlogs.contains(cat))
		{
			this.catlogs.add(cat);

			if(Double.parseDouble(noOfFrames.getText())<cat.getSelectedEqList().size())
			{
				noOfFrames.setText(Integer.toString(cat.getSelectedEqList().size()));	
			}
			addLayerToTimeLine();
		}
	}



	public void animateSceneWithLayers(double startTime)
	{
		//create splines
		createSplines("position");
		createSplines("focalPoints");
		createSplines("viewUp");
		ArrayList<vtkActor> nw = new ArrayList<>();
		nw.add(profile);
		nw.add(glyph);
		//Info.getMainGUI().addActors(nw);

		//Info.getMainGUI().updateRenderWindow();
		scene = new vtkAnimationScene();
		scene.RemoveAllCues();
		//sequence of frames evenly spaced in the specified Start Time and End Time for the animation
		scene.SetModeToSequence();//SetModeToRealTime();//

		scene.SetLoop(0);//loop once 
		//framerate is 1/tickrate
		//starttime by default is 0. tickrate = (end time-start time)/(no. of frames)
		tickrate = 1/(Double.parseDouble(endTime.getText())/Double.parseDouble(noOfFrames.getText()));
		scene.SetFrameRate(tickrate);
		System.out.println("tick:"+tickrate);
		scene.SetStartTime(0);//startTime);
		scene.SetEndTime(Double.parseDouble(endTime.getText()));

		boolean start,end,onlyEQ=true;
		start=false;
		end=false;
		vtkAnimationCue cue1 = new vtkAnimationCue();
		// Create an Animation Cue.
		for(int i =1;i<framePoints.size();i++)
		{
			vtkCamera cam = framePoints.get(i);

			if((cam.GetPosition()[0]!=0 && cam.GetPosition()[1]!=0 && cam.GetPosition()[2]!=0))
			{
				onlyEQ=false;
				if(!start)
				{
					cue1.SetStartTime(startTime + i*(1/tickrate));
					System.out.println("cue start time: "+ i*(1/tickrate));
					start= true;
					end=false;
				}
				else if(start&&!end)
				{
					cue1.SetEndTime(i*(1/tickrate));
					//scene.SetEndTime(i*(1/tickrate));
					System.out.println("cue end time: "+ i*(1/tickrate));
					//start = false;
					end = false;


					cb = new CueAnimator();
					cb.pointsPosition = pointsToMoveCameraOnPosition;//pos;
					cb.pointsFocalPoint = pointsToMoveCameraOnFocalPoint;//fp;
					cb.pointsViewUp = pointsToMoveCameraOnViewUp;//up
					cb.actor = focusActor;
					cb.cue = cue1;
					double t = cue1.GetEndTime()/scene.GetEndTime();
					cb.ptSize = (int) ((1-t)*(1)+ t*(pointsToMoveCameraOnPosition.GetNumberOfPoints()-1));
					System.out.println("ptSize:"+cb.ptSize);
					cb.camold = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();
					cb.included = included;
					cb.scene = scene;
					if(included)
					{	//cb.earthquakeList = (ArrayList<Earthquake>) list;
						cb.cat = catlogs;
						t = cue1.GetEndTime()/scene.GetEndTime();
						cb.EQPtSize = (int) ((1-t)*(1)+ t*(Integer.parseInt(noOfFrames.getText())-1));

					}


					if(rendering)
					{
						//capture screenshot
						//cb.renderSizeold = Info.getMainGUI().getRenderWindow().GetRenderWindow().GetSize();
						//Info.getMainGUI().getRenderWindow().GetRenderWindow().SetSize(1920,1020);

						cue1.AddObserver("StartAnimationCueEvent", cb, "StartCue");
						cue1.AddObserver("EndAnimationCueEvent", cb, "EndCueCameraAniamtionRender");
						cue1.AddObserver("AnimationCueTickEvent", cb, "TickCameraAniamtionRender");

						//render image files to movie
						try {
							m = new MediaLocator(movieFile.toURL()+ ".mov");
							System.out.println("Writing " + m.getURL());
							cb.m = m;
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} else
					{
						cue1.AddObserver("StartAnimationCueEvent", cb, "StartCue");
						cue1.AddObserver("EndAnimationCueEvent", cb, "EndCue");
						cue1.AddObserver("AnimationCueTickEvent", cb, "TickCameraAniamtion");
					}
					System.out.println("s: "+scene.GetStartTime());
					System.out.println("e: "+scene.GetEndTime());
					scene.AddCue(cue1);
					cue1 = new vtkAnimationCue();
					cue1.SetStartTime((i+1)*(1/tickrate));
				}

			}
		}
		if(included && onlyEQ)
		{
			cue1.SetStartTime(startTime);
			cue1.SetEndTime(scene.GetEndTime());
			cb = new CueAnimator();
			cue1.AddObserver("StartAnimationCueEvent", cb, "StartCueEarthquakeCatalogAniamtion");
			cue1.AddObserver("EndAnimationCueEvent", cb, "EndCue");
			cue1.AddObserver("AnimationCueTickEvent", cb, "TickEarthquakeCatalogAniamtion");
			cb.cue =cue1;
			cb.scene = scene;
			cb.included =included;
			cb.camold = Info.getMainGUI().getRenderWindow().GetRenderer().GetActiveCamera();
			if(included)
			{
				cb.cat = catlogs;
				double t = cue1.GetEndTime()/scene.GetEndTime();
				cb.EQPtSize = (int) ((1-t)*(1)+ t*(Integer.parseInt(noOfFrames.getText())-1));

			}
			System.out.println("s: "+cue1.GetStartTime());
			System.out.println("e: "+cue1.GetEndTime());
			scene.AddCue(cue1);
		}
		//scene.SetStartTime(startTime);
		//scene.SetEndTime(Double.parseDouble(endTime.getText()));
	}
	public void scenePlay(){
		// TODO also disable modifying/adding keyframes here and re-enable later

		new Thread()
		{
			public void run(){
				//Info.getMainGUI().getRenderWindow().GetRenderer().SetActiveCamera(cb.camold);
				if(scene.IsInPlay()==1)
					scene.Stop();
				scene.Play();
				//scene.Stop();
				//System.out.println(scene.GetAnimationTime());

				pauseScriptingPluginButton.setEnabled(true);
				playScriptingPluginButton.setEnabled(true);
				stopScriptingPluginButton.setEnabled(true);
				renderButton.setEnabled(true);
				renderPauseButton.setEnabled(true);
			}
		}.start();
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



		new vtkMath();
		double x=0,y=0,z=0;
		int ct =0;
		vtkPoints inputPoints = new vtkPoints();
		for (int i=0; i<numberOfInputPoints; i++) {
			if(framePoints.get(i).GetPosition()[0]!=0 && framePoints.get(i).GetPosition()[1]!=0 && framePoints.get(i).GetPosition()[2]!=0)
			{
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

				aSplineX.AddPoint(ct, x);
				aSplineY.AddPoint(ct, y);
				aSplineZ.AddPoint(ct, z);
				//aspline.SetXSpline(id0);
				inputPoints.InsertPoint(ct, x, y, z);

				ct++;
			}
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
		int oldInputIndex = 0;
		// Interpolate x, y and z by using the three spline filters and
		// create new points
		double t;
		for (int i=0; i<numberOfOutputPoints; i++) {
			t = (double)(inputPoints.GetNumberOfPoints()-1)/(double)(numberOfOutputPoints-1)*(double)i;

			if(oldInputIndex!=Math.floor(t))
			{
				//then add to inputindex as the range of points changed (segmenting points)
				inputPtIndex.add(i);
				oldInputIndex = (int) Math.floor(t);
			}
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
			profile.Modified();
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
		/*if (src== endTime) {
			scene.SetEndTime(Double.parseDouble(endTime.getText()));
		}*/
		if (src== noOfFrames) {
			//scene.SetFrameRate(Double.parseDouble(noOfFrames.getText()));
			updateFramesInTimeLine();
		}

		if(src == this.addScriptingPluginButton)
		{
			//get table row and column and add key frame there

			System.out.println(selectedRow);
			System.out.println(selectedCol);

			KeyFrame kf = new KeyFrame();
			vtkCamera c = kf.getCamPos();
			vtkCamera c2 = new vtkCamera();
			c2.SetPosition(c.GetPosition()[0], c.GetPosition()[1], c.GetPosition()[2]);
			c2.SetFocalPoint(c.GetFocalPoint()[0], c.GetFocalPoint()[1], c.GetFocalPoint()[2]);
			c2.SetViewUp(c.GetViewUp()[0], c.GetViewUp()[1], c.GetViewUp()[2]);
			framePoints.set(selectedCol,c2);
			int s= framePoints.size();
			System.out.println(framePoints.get(s-1).GetPosition()[0]);
			System.out.println(framePoints.get(s-1).GetPosition()[1]);
			System.out.println(framePoints.get(s-1).GetPosition()[2]);

			resetScene = false;

			//add keyframes for row 1 and column n
			table.setValueAt(new String(Double.toString(c2.GetPosition()[0])+", "+Double.toString(c2.GetPosition()[1])+", "+Double.toString(c2.GetPosition()[2])), selectedRow,selectedCol);    
		}
		else if (src==this.removeScriptingPluginButton)
		{
			//remove key frame value 
			vtkCamera c2 = new vtkCamera();
			framePoints.set(selectedCol,c2);
			table.setValueAt("", selectedRow,selectedCol); 
		}

		else if (src == this.playScriptingPluginButton)
		{
			//			new Thread(){ 
			//				public void run(){
			playScriptingPluginButton.setEnabled(false);
			renderButton.setEnabled(false);
			renderPauseButton.setEnabled(false);
			if(framePoints.size()>=2)
			{
				if(!play && stop){

					animateSceneWithLayers(0);
					scenePlay();

				}
				else if(!play && !stop)
				{
					//resume
					if(Math.round(scene.GetAnimationTime())==Math.round(scene.GetEndTime()))
					{
						animateSceneWithLayers(0);
						scenePlay();
						stop=true;
					}else{
						//resume
						System.out.println("aniamtion time:"+scene.GetAnimationTime());
						animateSceneWithLayers(scene.GetAnimationTime());
						scenePlay();
					}
				}
			}

			//		           }
			//			}.start();
			//
		}
		else if (src == this.pauseScriptingPluginButton)
		{
			pauseScriptingPluginButton.setEnabled(false);
			playScriptingPluginButton.setEnabled(false);
			stop=false;
			play=false;
			if(scene.IsInPlay()==1)
				scene.Stop();
			System.out.println("aniamtion time:"+scene.GetAnimationTime());
			playScriptingPluginButton.setEnabled(true);
			pauseScriptingPluginButton.setEnabled(true);
		}
		else if (src == this.stopScriptingPluginButton)
		{
			pauseScriptingPluginButton.setEnabled(false);
			playScriptingPluginButton.setEnabled(false);
			stopScriptingPluginButton.setEnabled(false);
			stop=true;
			play=false;
			if(scene.IsInPlay()==1)
				scene.Stop();
			System.out.println("aniamtion time:"+scene.GetAnimationTime());
			pauseScriptingPluginButton.setEnabled(true);
			playScriptingPluginButton.setEnabled(true);
			stopScriptingPluginButton.setEnabled(true);
		}
		else if (src == this.renderPauseButton)
		{
			pauseScriptingPluginButton.setEnabled(false);
			playScriptingPluginButton.setEnabled(false);
			stopScriptingPluginButton.setEnabled(false);
			renderPauseButton.setEnabled(false);
			stop=false;
			play=false;
			if(scene.IsInPlay()==1)
				scene.Stop();
			this.rendering = false;
			renderPauseButton.setEnabled(true);
		}
		else if (src == this.renderButton)
		{
			pauseScriptingPluginButton.setEnabled(false);
			playScriptingPluginButton.setEnabled(false);
			stopScriptingPluginButton.setEnabled(false);
			renderButton.setEnabled(false);

			this.rendering = true;
			if(framePoints.size()>=2)
			{
				if(!play && stop){
					int returnVal = fc.showSaveDialog(new JFrame("Save movie"));
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						movieFile = fc.getSelectedFile();
						// it's a movie render
						File tmpDir = new File(Prefs.getLibLoc()+"/tmp/");
						if (!tmpDir.exists())
							tmpDir.mkdir();

						animateSceneWithLayers(0);
						scenePlay();

					}
				}
				else if(!play && !stop)
				{
					//resume
					System.out.println("aniamtion time:"+scene.GetAnimationTime());
					animateSceneWithLayers(scene.GetAnimationTime());
					scenePlay();
				}
			}
			stop=true;
			play=false;

		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		selectedRow = table.rowAtPoint(e.getPoint());
		selectedCol = table.columnAtPoint(e.getPoint());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}

