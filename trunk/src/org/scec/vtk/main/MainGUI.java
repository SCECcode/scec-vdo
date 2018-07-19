package org.scec.vtk.main;

import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.scec.vtk.commons.legend.LegendItem;
import org.scec.vtk.grid.ViewRange;
import org.scec.vtk.main.Help;
import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainMenu;
import org.scec.vtk.main.Wizard;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.PluginActorsChangeListener;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.politicalBoundaries.PoliticalBoundariesGUI;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.timeline.gui.TimelineGUI;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.plugins.Plugins;

import com.google.common.base.Preconditions;

import vtk.vtkActor;
import vtk.vtkCamera;
import vtk.vtkCellPicker;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkSphereSource;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public  class MainGUI extends JFrame implements  ChangeListener, PluginActorsChangeListener{
	private final int BORDER_SIZE = 10;
	//	private static JFrame frame ;
	private static vtkJoglPanelComponent  renderWindow;
	//pluginTabPane contains tabs with all plugins
	public static JTabbedPane pluginTabPane;
	//Create Main Panel/ Main panel contains toolBar and VTK rendered 3D image; 
	public JPanel mainPanel;
	
	public JFrame wizFrame;
	public JFrame helpFrame;
	
	private Dimension canvasSize = new Dimension();
	private int xCenter = BORDER_SIZE / 2;
	private int yCenter = BORDER_SIZE / 2;
	private ViewRange viewRange;
	private static final Logger log = Logger.getLogger(MainGUI.class);

	public  Map<String, String> tabMap = new HashMap<String, String>();
	// In the static constructor we load in the native code.
	// The libraries must be in your path to work.
	static {
		if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
			for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
				if (!lib.IsLoaded()) {
					System.out.println(lib.GetLibraryName() + " not loaded");
				}
			}
		}
		vtkNativeLibrary.DisableOutputWindow(null);
	}
	private static File getCWD;

	public MainMenu mainMenu;
	//pluginGUIPanel contains searchBar and pluginTabPane
	private JPanel pluginGUIPanel;
	private JPanel helpPanel;
	//pluginGUIScrollPane contains the scroll bar.
	private JScrollPane pluginGUIScrollPane;
	//pluginSplitPane splits mainPanel and pluginGUIPanel
	private JSplitPane pluginSplitPane;
	private int xeBorder=0;
	private int ysBorder=0;
	private vtkActor tempGlobeScene = new vtkActor();
	private boolean gridDisplay = true;
//	private ScriptingPlugin scriptingPluginObj;
	
	public Timeline timeline;
	private TimelineGUI timelineGUI;
	
	private JTextField searchBar;
	private JPanel searchBarGUI;
	
	private JToolBar toolBar;
	private JPanel toolBarGUI;

	//default starting cam coordinates
	static double[] camCord = {7513.266063258975,
			-4588.568400980608,
			6246.237592377226,//position
			4375.8873291015625,
			-2496.9269409179688,
			3859.8922119140625,//focalpoint
			-0.45792813113264974,
			0.276911132961531,
			0.8447615350850914};//up

	private vtkActor focalPointActor = new vtkActor();
	
	private ArrayList<PluginActorsChangeListener> actorsChangeListeners = new ArrayList<>();
	
	vtkCamera cam = new vtkCamera();

	public MainGUI() {
		Prefs.init();
		renderWindow = new vtkJoglPanelComponent();
		mainPanel = new JPanel(new BorderLayout());
		renderWindow.getRenderer().SetBackground(0,0,0);
		
		
		// this should enable depth peeling, but doesn't seem to work. at least for Kevin on linux.
		// more info/source: http://www.vtk.org/Wiki/VTK/Depth_Peeling
//		renderWindow.getRenderWindow().SetAlphaBitPlanes(1);
//		renderWindow.getRenderWindow().SetMultiSamples(0);
//		renderWindow.getRenderer().SetUseDepthPeeling(1);
////		renderWindow.getRenderer().SetMaximumNumberOfPeels(100);
////		renderWindow.getRenderer().SetOcclusionRatio(0.1);
//		renderWindow.getRenderer().SetMaximumNumberOfPeels(1000);
//		renderWindow.getRenderer().SetOcclusionRatio(0);
		mainMenu = new MainMenu();
		
		pluginGUIPanel = new JPanel(new BorderLayout());
		helpPanel = new JPanel();
		helpPanel.setBorder(new EmptyBorder(10, 30, 10, 30));
		helpPanel.setLayout(new FlowLayout());
		
//		Icon icon = UIManager.getIcon("OptionPane.questionIcon");
		
		
		JButton helpButton = new JButton();
		try {
			// sets hover text
		helpButton.setToolTipText("Help");
			File file = new File("resources/question3.png");
		    Image img = ImageIO.read(file);
		    img = img.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
		    helpButton.setIcon(new ImageIcon(img));
		  } catch (IOException ex) {
		    System.out.println("help: " + ex);
		  }
		helpButton.setBackground(Color.black);
		helpButton.setOpaque(false);       
		//helpButton.setPreferredSize(new Dimension(50, 30));
		helpPanel.add(helpButton);
		helpPanel.setOpaque(false);
		//helpPanel.setColor(Color.white);
		pluginGUIPanel.add(helpPanel,BorderLayout.PAGE_END);
		pluginGUIPanel.setBorder(BorderFactory.createEmptyBorder());
		mainPanel.setOpaque(true);
		//mainPanel.setBackground(Color.white);
		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		pluginGUIPanel.setOpaque(true);
		//pluginGUIPanel.setBackground(Color.white);
		pluginGUIPanel.setBorder(new EmptyBorder(2, 0, 0, 0));
		
		
		/*
		 * 
		 * Help button action listener. 
		 * This listener aims to open the tutorial and autodirect the user to the correlating section of the tutorial.
		 */
		helpButton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		    	  Help help = new Help();
		      }
		
		});
		
  //HELP BUTTON
		pluginTabPane =  new JTabbedPane();
		//pluginTabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		pluginTabPane.setUI(tabbedPaneUI);
		//Set up all default GUI elements
		Info.setMainGUI(this);
		setUpPluginTabs();
		//setUpSearchBar();
		setUpToolBar();

		pluginSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, mainPanel, pluginGUIScrollPane);
		pluginSplitPane.setOneTouchExpandable(false);
		pluginSplitPane.setBorder(BorderFactory.createEmptyBorder());
		pluginSplitPane.setResizeWeight(1);
		pluginSplitPane.setDividerLocation(0.5);
		
		//Set preferred sizes
		Dimension d = new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight());
		pluginGUIScrollPane.setMinimumSize(d);
		pluginGUIScrollPane.setPreferredSize(d);
		pluginTabPane.setOpaque(true);
		Dimension minimumSize = new Dimension(100, 50);
		mainPanel.setMinimumSize(minimumSize);//new Dimension(Prefs.getMainWidth(), Prefs.getMainHeight()));
		renderWindow.getComponent().setMinimumSize(new Dimension(Prefs.getMainWidth(), Prefs.getMainHeight()));
		timeline = new Timeline();
		timelineGUI = new TimelineGUI(timeline);
		mainMenu.setupTimeline(timeline, timelineGUI);

		cam.SetPosition(camCord[0],camCord[1],camCord[2]);
		cam.SetFocalPoint(camCord[3],camCord[4],camCord[5]);
		cam.SetViewUp(camCord[6],camCord[7],camCord[8]);

		vtkSphereSource focalPoint = new vtkSphereSource();
		focalPoint.SetRadius(20);
		vtkPolyDataMapper focalPointMapper = new vtkPolyDataMapper();
		focalPointMapper.SetInputConnection(focalPoint.GetOutputPort());
		focalPointActor.SetMapper(focalPointMapper);
		renderWindow.getRenderer().AddActor(focalPointActor);
		focalPointActor.VisibilityOff();

		renderWindow.getRenderer().SetActiveCamera(cam);
		renderWindow.getRenderer().ResetCameraClippingRange();
		focalPointActor.SetPosition(renderWindow.getRenderer().GetActiveCamera().GetFocalPoint());
		focalPointActor.Modified();
		renderWindow.getComponent().setFocusable(true);
		renderWindow.getComponent().addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				renderWindow.getRenderer().Render();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					//TODO: change start cam coordinates to be as per the region assigned by default they are set for California
					//reset view position to default 
					if(renderWindow.getRenderer().GetViewProps().IsItemPresent(PoliticalBoundariesGUI.mainFocusReginActor)!=0) {
					
						renderWindow.getRenderer().GetActiveCamera().SetPosition(camCord[0],camCord[1],camCord[2]);
						renderWindow.getRenderer().GetActiveCamera().SetFocalPoint(camCord[3],camCord[4],camCord[5]);
						renderWindow.getRenderer().GetActiveCamera().SetViewUp(camCord[6],camCord[7],camCord[8]);
						
						renderWindow.getRenderer().ResetCameraClippingRange();
						renderWindow.getComponent().repaint();
						
					}
				}
			}
		});

		//mouse event
		final JLabel label = new JLabel(" ", SwingConstants.RIGHT);
		final vtkCellPicker cellPicker = new vtkCellPicker();
		cellPicker.SetTolerance(0.001);
		
		final boolean clickDebug = false;
		renderWindow.getComponent().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// released
				if (focalPointActor.GetVisibility() == 1)
					updateFocalPointLocation();
			}

			public void mousePressed(MouseEvent e) {
				// this is needed otherwise for some reason focus can never be regained by the render window
				// which causes key events to be ignored after focus is lost
				renderWindow.getComponent().requestFocus();
				
				int[] clickPos = renderWindow.getRenderWindowInteractor().GetEventPosition();
				int height = renderWindow.getComponent().getHeight();
				int calcY = (height - e.getY()) - 1;
				
				if (clickDebug) {
					System.out.println("Mouse pressed! "+clickPos[0]+" "+clickPos[1]);
					System.out.println("\tRW Click Pos: "+clickPos[0]+" "+clickPos[1]);
					System.out.println("\tEvent Pos: "+e.getX()+" "+e.getY());
					System.out.println("\tHeight: "+height);
					System.out.println("\tCustom Pos: "+e.getX()+" "+calcY);
				}
				
				int x = clickPos[0];
				int y = clickPos[1];
 
				cellPicker.Pick(x, y, 0, renderWindow.getRenderer());
				if (clickDebug) {
					if (cellPicker.GetActor() != null)
						System.out.println("Actor: "+cellPicker.GetActor().getClass().getName());
					else
						System.out.println("Actor: (null)");
				}
				// if we picked a pick enabled actor, fire off a pick event
				if (cellPicker.GetActor() instanceof PickEnabledActor<?>) {
					PickEnabledActor<?> actor = (PickEnabledActor<?>)cellPicker.GetActor();
					// TODO check to see that the actor belongs to the currently visible plugin, if not ignore pick
					actor.picked(cellPicker, e);
				}
			}
		});

		mainPanel.add(renderWindow.getComponent(), BorderLayout.CENTER);
		try {
			mainMenu.availablePlugins = Plugins.getAvailablePlugins();
			mainMenu.setupPluginMenus();
			addDefaultActors();
			setMainFrame();
			
			//Update the divider location so that the plugin pane doesn't require horizontal scrolling
			pluginSplitPane.setDividerLocation(this.getWidth() - pluginTabPane.getPreferredSize().width - 60);

		} catch (IOException ioe) {
			throw new RuntimeException("Unable to get available plugins", ioe);
		}
		
		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				if (targetDims != null) {
					// we asked for a change
					System.out.println("Resized! "+getWidth()+"x"+getHeight());
					if (targetDims.getWidth() != getWidth() || targetDims.getHeight() != getHeight()) {
						JOptionPane.showMessageDialog(Info.getMainGUI(),
							"Screen is not large enough for selected resolution. Select a lower resolution and try agin."
							+ "\nYou can still render movies larger than screen resolution using the render settings dialog.",
							"Coundn't Resize Window", JOptionPane.ERROR_MESSAGE);
					}
					targetDims = null;
				}
			}
		});
	
		// checks to see if Wizard enabled
		if(MainMenu.Wizard){
			//Wizard GUI to run with main
			wizFrame = new JFrame();
			Wizard wizGui = new Wizard(mainMenu, this);
			wizFrame.getContentPane().add(wizGui);
			wizFrame.setSize(550, 140);
			wizFrame.setLocationRelativeTo(null);
			wizFrame.setVisible(true);
		}

	}
	final Color selectedTabColor = new Color(239,239,239);
    final Color tabBackgroundColor = Color.LIGHT_GRAY;
    final Color tabBorderColor = Color.LIGHT_GRAY;

	BasicTabbedPaneUI tabbedPaneUI = new BasicTabbedPaneUI() {
		private final Insets borderInsets = new Insets(-5, 0, 0, 0);
        @Override
        protected Insets getContentBorderInsets(int tabPlacement) {
            return borderInsets;
        }
        @Override
        protected Insets getTabInsets(int tabPlacement, int tabIndex)  {
        	return new Insets(3, 6, 3, 6);
        }
        @Override
        protected Insets getTabAreaInsets(int tabPlacement) {
        	return new Insets(5, 5, 5, 5);
        }
		@Override protected void paintContentBorder(Graphics g,
                int tabPlacement,
                int selectedIndex) {
		}
		@Override protected void paintTabBorder(
				Graphics g, int tabPlacement, int tabIndex,
				int x, int y, int w, int h, boolean isSelected) {
		}
		@Override protected void paintFocusIndicator(
				Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
				Rectangle iconRect, Rectangle textRect, boolean isSelected) {
		}
		@Override protected void paintContentBorderTopEdge(
				Graphics g, int tabPlacement, int selectedIndex,
				int x, int y, int w, int h) {
			super.paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
			Rectangle selRect = getTabBounds(selectedIndex, calcRect);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setColor(Color.cyan);
			g2.drawLine(selRect.x - 2, y, selRect.x + selRect.width + 2, y);
			g2.dispose();
		}
		@Override protected void paintTabBackground(
				Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
				boolean isSelected) {

			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			int a = isSelected ? 0 : 1;

			GeneralPath shape = new GeneralPath();
			shape.moveTo(x - 3, y + h);
			shape.lineTo(x + 3, y + a);
			shape.lineTo(x + w - 3, y + a);
			shape.lineTo(x + w + 3, y + h);
			shape.closePath();
			g2.setColor(isSelected ? selectedTabColor : tabBackgroundColor);
			g2.fill(shape);

			GeneralPath border = new GeneralPath();
			if (isSelected || tabIndex == 0) {
				border.moveTo(x - 3, y + h - 1);
			} else {
				border.moveTo(x + 3, y + h - 1);
				border.lineTo(x, (y + h - 1) / 2);
			}
			border.lineTo(x + 3, y + a);
			border.lineTo(x + w - 3, y + a);
			border.lineTo(x + w + 3, y + h - 1);

			g2.setColor(tabBorderColor);
			g2.draw(border);
			g2.dispose();
		}
	};
	public void setFocalPointVisible(boolean visible) {
		int newVis = visible ? 1 : 0;
		int curVis = focalPointActor.GetVisibility();
		if (newVis != curVis) {
			focalPointActor.SetVisibility(newVis);
			if (visible)
				updateFocalPointLocation();
			updateRenderWindow();
			renderWindow.getComponent().repaint();
		}
	}
	
	
	public void updateFocalPointLocation() {
		focalPointActor.SetPosition(renderWindow.getRenderer().GetActiveCamera().GetFocalPoint());
		focalPointActor.Modified();
	}
	
	public TimelineGUI getTimelineGUI() {
		return timelineGUI;
	}
	
	public Timeline getTimeline() {
		return timeline;
	}
	
	public static File getRootPluginDir(){
		return  new File( getCWD() + File.separator+ "data");
	}
	
	public static File getCWD(){

		if(getCWD==null)
		{
			getCWD = new File(System.getProperty("user.dir"));
		}
		//System.out.println("user.dir is: " + System.getProperty("user.dir"));
		return getCWD;
	}

	private void addDefaultActors() {
		// render window locks up and won't repaint if there are zero actors. add a blank actor to prevent this
		vtkActor blankActor = new vtkActor();
		renderWindow.getRenderer().AddActor(blankActor);
		
		// TODO these should be treated as plugins that are loaded by default, not hardcoded here
		ArrayList<String> ids =new ArrayList<String>();
		ArrayList<PluginInfo> pluginInfo = new ArrayList<PluginInfo>();
		//politicalBoundaries
		ids.add("org.scec.vdo.politicalBoundaries");
		pluginInfo.add(new PluginInfo());
		pluginInfo.get(0).setId(ids.get(0));
		pluginInfo.get(0).setName("Political Boundaries");
		pluginInfo.get(0).setShortName("Political Boundaries");
		pluginInfo.get(0).setPluginClass("org.scec.vtk.politicalBoundaries.PoliticalBoundariesPlugin");	
		mainMenu.availablePlugins.put(ids.get(0), pluginInfo.get(0));
		mainMenu.activatePlugin(ids.get(0));
		
		//graticule
		ids.add("org.scec.vdo.graticulePlugin");
		pluginInfo.add(new PluginInfo());
		pluginInfo.get(1).setId(ids.get(1));
		pluginInfo.get(1).setName("Graticule");
		pluginInfo.get(1).setShortName("Graticule");
		pluginInfo.get(1).setPluginClass("org.scec.vtk.grid.GraticulePlugin");	
		mainMenu.availablePlugins.put(ids.get(1), pluginInfo.get(1));
		mainMenu.activatePlugin(ids.get(1));

		//draw DrawingTools
		ids.add("org.scec.vdo.drawingToolsPlugin");
		pluginInfo.add(new PluginInfo());
		pluginInfo.get(2).setId(ids.get(2));
		pluginInfo.get(2).setName("Drawing Tools");
		pluginInfo.get(2).setShortName("Drawing Tools");
		pluginInfo.get(2).setPluginClass("org.scec.vtk.drawingTools.DrawingToolsPlugin");	
		mainMenu.availablePlugins.put(ids.get(2), pluginInfo.get(2));
		mainMenu.activatePlugin(ids.get(2));
		pluginTabPane.setSelectedIndex(0);
	}
	
	public boolean getGridDisplayBool() {
		return this.gridDisplay;
	}

	public vtkActor getGrid() {
		return this.tempGlobeScene;
	}

	
	@SuppressWarnings("unused")
	/*
	 * Adds buttons to toolbar, adds toolbar to GUI
	 */
	private void setUpToolBar() {
		toolBar = new JToolBar();
		//toolBar.setLayout(new BorderLayout());
		//toolBar.setSize(new Dimension(200, 20));
		JButton centerImage = new JButton();
		try {
			// sets hover text
			centerImage.setToolTipText("Center Image");
			File file = new File("resources/Center.png");
		    Image img = ImageIO.read(file);
		    img = img.getScaledInstance(15, 15, Image.SCALE_DEFAULT);
		    centerImage.setIcon(new ImageIcon(img));
		  } catch (IOException ex) {
			
		    System.out.println("centerImage: " + ex);
		  }
		centerImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				
				//if(renderWindow.getRenderer().GetViewProps().IsItemPresent(PoliticalBoundariesGUI.mainFocusReginActor)!=) {
					renderWindow.getRenderer().GetActiveCamera().SetPosition(MainGUI.camCord[0], MainGUI.camCord[1],MainGUI.camCord[2]);
					renderWindow.getRenderer().GetActiveCamera().SetFocalPoint(MainGUI.camCord[3], MainGUI.camCord[4],MainGUI.camCord[5]);
					renderWindow.getRenderer().GetActiveCamera().SetViewUp(MainGUI.camCord[6], MainGUI.camCord[7],MainGUI.camCord[8]);
					renderWindow.getRenderer().ResetCameraClippingRange();
					renderWindow.getComponent().repaint();
					
			//}
			  } 
		});
		centerImage.setOpaque(false);
		centerImage.setToolTipText("Center");
		
		
		JButton zoomIn = new JButton();
		
		try {
			// sets hover text
			zoomIn.setToolTipText("Zoom In");
			zoomIn.setOpaque(false);
			File file = new File("resources/zoomIn.png");
		    Image img = ImageIO.read(file);
		    img = img.getScaledInstance(15, 15, Image.SCALE_DEFAULT);
		    zoomIn.setIcon(new ImageIcon(img));
		  } catch (IOException ex) {
		    System.out.println("zoomIn: " + ex);
		  }
		zoomIn.addActionListener(new ActionListener() { 
			  public void actionPerformed(ActionEvent e) { 
			    cam.Zoom(1.2);
			    renderWindow.getRenderer().ResetCameraClippingRange();
			    renderWindow.getComponent().repaint();
			    		
			  } 
		} );
		
		zoomIn.setToolTipText("Zoom In");
		
		JButton zoomOut = new JButton();
		try {
			// sets hover text
			zoomOut.setToolTipText("Zoom Out");
			zoomOut.setOpaque(false);
			File file = new File("resources/zoomOut.png");
		    Image img = ImageIO.read(file);
		    img = img.getScaledInstance(15, 15, Image.SCALE_DEFAULT);
		    zoomOut.setIcon(new ImageIcon(img));
		  } catch (IOException ex) {
		    System.out.println("zoomOut: " +  ex);
		  }
		zoomOut.addActionListener(new ActionListener() { 
			  public void actionPerformed(ActionEvent e) { 
				  cam.Zoom(.8);
				  renderWindow.getRenderer().ResetCameraClippingRange();
				  renderWindow.getComponent().repaint();
			  } 
		} );
		
		zoomOut.setToolTipText("Zoom Out");
		
		JButton save = new JButton();
		
		try {
			// sets hover text
			save.setToolTipText("Save file");
			save.setOpaque(false);
			File file = new File("resources/save.png");
		    Image img = ImageIO.read(file);
		    img = img.getScaledInstance(15, 15, Image.SCALE_DEFAULT);
		    save.setIcon(new ImageIcon(img));
		  } catch (IOException ex) {
		    System.out.println("zoomOut: " +  ex);
		  }
		save.addActionListener(new ActionListener() { 
			  public void actionPerformed(ActionEvent e) {
				 // checks whether file has been created
				 if (mainMenu.isSaved()) {
					 System.out.println("autoSave()");
					 mainMenu.autoSave();
				 }
				 else {
					 // creates new file
					 System.out.println("saveForToolbar");
					 mainMenu.saveForToolbar();
				 }
			  } 
		} );
		
		save.setToolTipText("Save");
		
		JButton open = new JButton();
		
		try {
			// sets hover text
			open.setToolTipText("Open a file");
			open.setOpaque(false);
			File file = new File("resources/open.png");
		    Image img = ImageIO.read(file);
		    img = img.getScaledInstance(15, 15, Image.SCALE_DEFAULT);
		    open.setIcon(new ImageIcon(img));
		  } catch (IOException ex) {
		    System.out.println("zoomOut: " +  ex);
		  }
		open.addActionListener(new ActionListener() { 
			  public void actionPerformed(ActionEvent e) { 
				  mainMenu.openForToolbar();
			  } 
		} );
		
		open.setToolTipText("Open");
		
		
		//adds buttons to toolbar
		toolBar.add(centerImage);
		toolBar.add(zoomIn);
		toolBar.add(zoomOut);
		toolBar.add(save);
		toolBar.add(open);
		toolBar.setOpaque(false);
		
		toolBarGUI = new JPanel(new BorderLayout());
		toolBarGUI.add(toolBar, BorderLayout.CENTER);
		toolBarGUI.setOpaque(false);
		toolBarGUI.setBorder(new EmptyBorder(5, 0, 1, 0));
		
		mainPanel.add(toolBarGUI, BorderLayout.PAGE_START);
	}
	
	private void setUpSearchBar() {
		searchBar = new JTextField();
		searchBarGUI = new JPanel(new BorderLayout());
		searchBarGUI.add(searchBar, BorderLayout.CENTER);
		pluginGUIPanel.add(searchBarGUI, BorderLayout.PAGE_END);
		
		
	}
	
	private void setUpPluginTabs() {
		//pluginTabPane.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		pluginGUIPanel.add(pluginTabPane, BorderLayout.PAGE_START);
		pluginTabPane.addChangeListener((ChangeListener) this);
		pluginTabPane.setBorder(BorderFactory.createEmptyBorder());
		pluginGUIScrollPane = new JScrollPane(pluginGUIPanel);
		pluginGUIScrollPane.setBorder(BorderFactory.createEmptyBorder());
		//		pluginSplitPane = null;
	}
	
	//create frame and tabbed pane in main window
	private void setMainFrame() {	
		this.setTitle("SCEC VDO VTK");
		this.setJMenuBar(mainMenu.getMenuBar());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(new Dimension(Prefs.getTotalWidth(), Prefs.getMainHeight()));
		this.setContentPane(pluginSplitPane);
		//This has to be done here, after the pluginSplitPane has been added to the GUI
		pluginSplitPane.setDividerSize(0);
		this.setLocationRelativeTo(null);
		this.setVisible(true);

	}
	public JPanel getmainFrame() {
		return mainPanel;
	}
	
//	@SuppressWarnings("unused")
//	public JPanel makebuttonPanel1() {
//		searchBarGUI = new JPanel();
//		searchBar = new JButton("?");
//		searchBarGUI.add(searchBar, BorderLayout.CENTER);
//		pluginGUIPanel.add(searchBarGUI, BorderLayout.PAGE_END);
//		
//		return mainPanel;
	//}
	//viewRange
	public void setViewRange(ViewRange viewRange) {
		this.viewRange = viewRange;
	}

	public ViewRange getViewRange() {
		return this.viewRange;
	}

	public void addPluginGUI(String id, String title, JComponent gui) {
		
		if(pluginTabPane.indexOfTab(title) != -1)
			return;
		if (!mainMenu.isPluginActive(id) && id !="org.scec.vdo.politicalBoundaries" && id !="org.scec.vdo.graticulePlugin"
				&& id != "org.scec.vdo.drawingToolsPlugin" && id !="org.scec.vdo.landmarksPlugin") {
			//Logger
			log.debug("Cannot add gui for inactive plugin " + id);
			return;
		}
		
		//System.out.println("id: "+ id);
		//System.out.println("title: "+ title);
		
		//everytime a plugin is added we're gonna put it in our map.
		tabMap.put(id,title);

		isPluginGuiShowing();

		// Create a new plugin tab
		JPanel allPanel = new JPanel();
		allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.PAGE_AXIS));
		allPanel.setBorder(BorderFactory.createEmptyBorder());
		allPanel.add(gui);
		allPanel.setOpaque(true);
	    allPanel.setFocusable (false);
		allPanel.add(Box.createVerticalGlue());
		//allPanel.add(new JPanel());
		JScrollPane pluginTab = new JScrollPane(allPanel);
		pluginTab.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		pluginTab.setBorder(BorderFactory.createEmptyBorder());
		//pluginTab.setOpaque(true);
		//pluginTab.setColor(Color.white);
		pluginTab.setName(id);
		pluginTab.setOpaque(true);
		pluginTab.setBackground(Color.black);
		// Add the tab to the tab panel
		
		pluginTabPane.addTab(title, pluginTab);
		if(id !="org.scec.vdo.politicalBoundaries" && id !="org.scec.vdo.graticulePlugin" && id != "org.scec.vdo.drawingToolsPlugin" && id != "org.scec.vdo.landmarksPlugin")
			pluginTabPane.setTabComponentAt(pluginTabPane.getTabCount() -1, new ButtonTabComponent(pluginTabPane, id));
		else
			pluginTabPane.setTabComponentAt(pluginTabPane.getTabCount() -1,null);
		pluginTabPane.setSelectedIndex(pluginTabPane.indexOfComponent(pluginTab));

//		if(id.equals("org.scec.vdo.plugins.ScriptingPlugin") )
//			scriptingPluginObj = (ScriptingPlugin) mainMenu.getActivePlugins().get(id);		

		SwingUtilities.updateComponentTreeUI(this);
		pluginTabPane.setUI(tabbedPaneUI);
		pluginTabPane.repaint();
		//pluginTabPane.set
	}

	public boolean removePluginGUI(String id) {

		if (!mainMenu.isPluginActive(id)) {
			log.warn("Cannot remove GUI for inactive plugin " + id);
			return false;
		}

		boolean removed = false;
		isPluginGuiShowing();

		// Find the plugin gui to remove
		for (int j = 0; j < pluginTabPane.getTabCount(); j++) {
			Component c = pluginTabPane.getComponentAt(j);
			if (c.getName() != null && c.getName().equals(id)) {

				// Remove the gui
				pluginTabPane.removeTabAt(j);

				// If it was the last gui, remove the split pane
				if (pluginTabPane.getTabCount() == 0) {
					updateCanvasSize();
					SwingUtilities.updateComponentTreeUI(this);
				}

				removed = true;
			}
		}

		// I think this means that if we remove all the plugins
		// and the plugin split pane goes away, and we are
		// showing the navigation map HUD, then we need to
		// update the HUD.
		/*if (showNavMap && (showing != isPluginGuiShowing())) {
			viewPlatform.setPlatformGeometry(getHUDGeometry());
			you.setRedDot(keyBehv.getFocalPoint());
		}*/

		return removed;
	}



	private boolean isPluginGuiShowing() {
		return (pluginTabPane.getTabCount() > 0);
	}


	//update renderwindow and focus on actor
	public static void updateRenderWindow(vtkActor actor) {
		renderWindow.Render();
		renderWindow.getRenderer().ResetCamera(actor.GetBounds());
		//renderWindow.repaint(); 
	}
	//just update renderwindow
	public static void updateRenderWindow() {
		//updateActors(getActorToAllActors());
		renderWindow.Render();
		renderWindow.getComponent().repaint();
	}

	public static vtkJoglPanelComponent  getRenderWindow() {
		return renderWindow;
	}

	public void removeSplitPane() {
		// TODO Auto-generated method stub
		remove(pluginSplitPane);
		pluginSplitPane = null;
		setContentPane(mainPanel);
		updateCanvasSize();
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	public void updateCanvasSize() {
		canvasSize = getSize();
		xCenter = (int) canvasSize.getWidth() / 2;
		yCenter = (int) canvasSize.getHeight() / 2;
		xeBorder = (int) canvasSize.getWidth() - BORDER_SIZE;
		ysBorder = (int) canvasSize.getHeight() - BORDER_SIZE;
	}


	/**
	 * When tabs are changed, TODO change whether the pickability of objects
	 * belonging to a tab are turned on or off. If a tab is currently selected
	 * allow objects to be pickable. All other tabs should have their objects' 
	 */
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();

		if (!(source instanceof JTabbedPane)) {
			log.warn("Event source was not a JTabbedPane!: "
					+ source.getClass().getName());
			return;
		}

		// Get selected component
		JTabbedPane pane = (JTabbedPane) source;
		Component c = pane.getSelectedComponent();

		// If nothing is selected then return. This happens
		// when the tabbed pane is removed
		if (c == null) {
			return;
		}

		// Get the id of the selected plugin
		String selectedPlugin = c.getName();
	}



	// button component to put inside the tabs to make the close button
	public class ButtonTabComponent extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JTabbedPane pane;
		private String pluginID;
		private Icon icon = new ImageIcon("data/MiscImages/closeTabButton.png");
		private Icon redIcon = new ImageIcon("data/MiscImages/closeTabButtonRed_1.png");

		public ButtonTabComponent(final JTabbedPane pane, String id) {    	
			//unset default FlowLayout' gaps
			super(new FlowLayout(FlowLayout.RIGHT, 0, 0));

			if (pane == null) {
				throw new NullPointerException("TabbedPane is null");
			}
			this.pane = pane;
	
			setOpaque(false);

			pluginID = id;

			//make JLabel read titles from JTabbedPane
			
			JLabel label = new JLabel() {
				public String getText() {
					int i = pane.indexOfTabComponent(ButtonTabComponent.this);
					if (i != -1) {
						return pane.getTitleAt(i);
					}
					return null;
				}
			};

			add(label);
			//add more space between the label and the button
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			//tab button
			JButton button = new TabButton();
			add(button);
			//add more space to the top of the component
			setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}

		private class TabButton extends JButton implements ActionListener {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public TabButton() {
				int size = 13;
				setPreferredSize(new Dimension(size, size));
				setToolTipText("close this tab");
				//Make the button looks the same for all Laf's
				setUI(new BasicButtonUI());
				//Make it transparent
				setContentAreaFilled(false);
				//No need to be focusable
				setFocusable(false);
				setBorder(BorderFactory.createEtchedBorder());
				setBorderPainted(false);

				setIcon(icon);
				setRolloverEnabled(true);

				//we use the same listener for all buttons
				addMouseListener(buttonMouseListener);
				//Close the proper tab by clicking the button
				addActionListener(this);
			}

			public void actionPerformed(ActionEvent e) {
				Map<String, Plugin> loadedPlugins = mainMenu.getLoadedPluginsAsMap();
				
				int i = pane.indexOfTabComponent(ButtonTabComponent.this);
				if (i != -1 && loadedPlugins.containsKey(pluginID)) {
					//System.out.println("*******PluginID to be removed: " + pluginID);
					timeline.removePlugin(loadedPlugins.get(pluginID));
					pane.remove(i);
					mainMenu.updateMenu(pluginID);
				}
				//if (loadedPlugins.size() == 0)
				//removeSplitPane();
			}

		}

		private final MouseListener buttonMouseListener = new MouseAdapter(){
		
		
			public void mouseEntered(MouseEvent e) {
				Component component = e.getComponent();
				if (component instanceof AbstractButton) {
					AbstractButton button = (AbstractButton) component;
					//button.setBorderPainted(true);
					button.setIcon(redIcon);
				}
			}
		
			public void mouseExited(MouseEvent e) {
				Component component = e.getComponent();
				if (component instanceof AbstractButton) {
					AbstractButton button = (AbstractButton) component;
					//button.setBorderPainted(false);
					button.setIcon(icon);
				}
			}
		};
		};
	

	private MenuShiftDetector shiftDetector = new MenuShiftDetector();

	/**
	 * This class is used to detect if shift is held down, used with the StayOpenCheckBoxMenuItem
	 * to keep the menu displayed if shift is held down
	 * 
	 * @author kevin
	 *
	 */
	private class MenuShiftDetector implements MenuKeyListener, MouseListener {

		private boolean shiftDown = false;

		public MenuShiftDetector() {
		}


		public void menuKeyTyped(MenuKeyEvent e) {}


		public void menuKeyPressed(MenuKeyEvent e) {
			shiftDown = e.isShiftDown();
		}


		public void menuKeyReleased(MenuKeyEvent e) {
			shiftDown = e.isShiftDown();
		}


		public void mouseClicked(MouseEvent e) {
			shiftDown = e.isShiftDown();
		}


		public void mousePressed(MouseEvent e) {
			shiftDown = e.isShiftDown();
		}


		public void mouseReleased(MouseEvent e) {
			shiftDown = e.isShiftDown();
		}


		public void mouseEntered(MouseEvent e) {
			shiftDown = e.isShiftDown();
		}


		public void mouseExited(MouseEvent e) {
			shiftDown = e.isShiftDown();
		}

	}


	// Used to keep the Display panel up when selecting and deselecting plugins
	public class StayOpenCheckBoxMenuItem extends CheckboxMenuItem {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @see MenuItem#JCheckBoxMenuItem()
		 */
		public StayOpenCheckBoxMenuItem() {
			super();
			//		    super.addKeyListener(this);
			//super.addMenuKeyListener(shiftDetector);
		}

		/**
		 * @see JCheckBoxMenuItem#JCheckBoxMenuItem(Action)
		 */
		public StayOpenCheckBoxMenuItem(Action a) {
			//super(a);
			//		    super.addKeyListener(this);
			//super.addMenuKeyListener(shiftDetector);
		}

		/**
		 * @see JCheckBoxMenuItem#JCheckBoxMenuItem(Icon)
		 */
		public StayOpenCheckBoxMenuItem(Icon icon) {
			//super(icon);
			//		    super.addKeyListener(this);
			//super.addMenuKeyListener(shiftDetector);
		}

		/**
		 * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String)
		 */
		public StayOpenCheckBoxMenuItem(String text) {
			super(text);
			//		    super.addKeyListener(this);
			//super.addMenuKeyListener(shiftDetector);
		}

		/**
		 * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String, boolean)
		 */
		public StayOpenCheckBoxMenuItem(String text, boolean selected) {
			super(text, selected);
			//		    super.addKeyListener(this);
			//super.addMenuKeyListener(shiftDetector);
		}

		/**
		 * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String, Icon)
		 */
		public StayOpenCheckBoxMenuItem(String text, Icon icon) {
		}

		/**
		 * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String, Icon, boolean)
		 */
		public StayOpenCheckBoxMenuItem(String text, Icon icon, boolean selected) {
		}

	}

//	public ScriptingPluginGUI GetScriptingPlugin() {
//		// TODO Auto-generated method stub
//		if(scriptingPluginObj==null || scriptingPluginObj.getGratPanel()==null)
//		{
//			mainMenu.activatePlugin("org.scec.vdo.plugins.ScriptingPlugin");	
//		}
//		return scriptingPluginObj.getScriptingPluginGUI();
//	}

	@Override
	public void actorAdded(vtkProp actor) {
		// called when a plugin adds an actor
		renderWindow.getRenderer().AddActor(actor);
		for (PluginActorsChangeListener l : actorsChangeListeners)
			l.actorAdded(actor);
	}

	@Override
	public void actorRemoved(vtkProp actor) {
		// called when a plugin removes an actor
		renderWindow.getRenderer().RemoveActor(actor);
		for (PluginActorsChangeListener l : actorsChangeListeners)
			l.actorRemoved(actor);
	}

	@Override
	public void legendAdded(LegendItem legend) {
		// called when a plugin adds a legend
		renderWindow.getRenderer().AddActor(legend.getActor());
		for (PluginActorsChangeListener l : actorsChangeListeners)
			l.legendAdded(legend);
	}	

	@Override
	public void legendRemoved(LegendItem legend) {
		// called when a plugin removes a legend
		renderWindow.getRenderer().RemoveActor(legend.getActor());
		for (PluginActorsChangeListener l : actorsChangeListeners)
			l.legendRemoved(legend);
	}
	
	public void addPluginActorsChangeListener(PluginActorsChangeListener listener) {
		actorsChangeListeners.add(listener);
	}
	
	public void removePluginActorsChangeListener(PluginActorsChangeListener listener) {
		actorsChangeListeners.remove(listener);
	}
	
	/**
	 * Used by the legend plugin to get the list of currently displayed legends, useful if a plugin added a legend without
	 * the legend management plugin loaded.
	 * @return
	 */
	public List<LegendItem> getDisplayedLegends() {
		ArrayList<LegendItem> currentLegends = new ArrayList<>();
		for (PluginActors actors : mainMenu.getActivatedPluginActors())
			currentLegends.addAll(actors.getLegends());
		return currentLegends;
	}
	
	private Dimension targetDims;
	
	public void resizeViewer(int width, int height) {
		JComponent comp = renderWindow.getComponent();
		int curWindowWidth = getWidth();
		int curWindowHeight = getHeight();
		int curViewerWidth = comp.getWidth();
		int curViewerHeight = comp.getHeight();
		Preconditions.checkState(curWindowHeight > curViewerHeight);
		Preconditions.checkState(curWindowWidth > curViewerWidth);
		int widthBuffer = curWindowWidth - curViewerWidth;
		int heightBuffer = curWindowHeight - curViewerHeight;
		int newWindowWidth = width + widthBuffer;
		int newWindowHeight = height + heightBuffer;
		System.out.println("Resiging viewer to "+width+"x"+height+". Current: "+curViewerWidth+"x"+curViewerHeight
				+". Buffer: "+widthBuffer+"x"+heightBuffer);
		setSize(newWindowWidth, newWindowHeight);
		comp.setSize(width, height);
		updateRenderWindow();
		//			renderWindow.getComponent().validate();
		int newViewerWidth = comp.getWidth();
		int newViewerHeight = comp.getHeight();
		System.out.println("Resized. New dims: "+newViewerWidth+"x"+newViewerHeight);
		targetDims = new Dimension(newWindowWidth, newWindowHeight);
	}
	
	
	
	public static void main(String[] args) {
		try {
            // Set System L&F
			UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) {
			// handle exception
		}
		catch (ClassNotFoundException e) {
			// handle exception
		}
		catch (InstantiationException e) {
			// handle exception
	    }
	    catch (IllegalAccessException e) {
	       // handle exception
	    }
	//	UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(Color.red, 4));
		UIManager.put("Menu.border", BorderFactory.createEmptyBorder(2, 4, 2, 4));
		UIManager.put("MenuBar.border", BorderFactory.createEmptyBorder(1, 3, 2, 3));
		UIManager.put("MenuItem.border", BorderFactory.createEmptyBorder(2, 4, 2, 4));
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainGUI();
			}
		});
	}
}