package org.scec.vtk.main;

import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.plaf.basic.BasicButtonUI;

import org.apache.log4j.Logger;
import org.scec.vtk.plugins.ClickablePlugin;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.plugins.ScriptingPlugin.ScriptingPlugin;
import org.scec.vtk.plugins.ScriptingPlugin.ScriptingPluginGUI;
import org.scec.vtk.drawingTools.DrawingToolsGUI;
import org.scec.vtk.drawingTools.DrawingToolsPlugin;
import org.scec.vtk.grid.GlobeBox;
import org.scec.vtk.grid.GraticuleGUI;
import org.scec.vtk.grid.GraticulePlugin;
import org.scec.vtk.grid.ViewRange;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.politicalBoundaries.PoliticalBoundariesGUI;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.plugins.Plugins;

import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkActorCollection;
import vtk.vtkAxesActor;
import vtk.vtkCamera;
import vtk.vtkCanvas;
import vtk.vtkCellPicker;
import vtk.vtkGlobeSource;
import vtk.vtkNativeLibrary;
import vtk.vtkObject;
import vtk.vtkPanel;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkTextActor;
import vtk.vtkTextActor3D;
import vtk.vtkTransform;
import vtk.vtkVectorText;

public  class MainGUI extends JFrame implements ChangeListener{
	private final int BORDER_SIZE = 10;
//	private static JFrame frame ;
	private static vtkCanvas  renderWindow;
	private static JTabbedPane pluginTabPane;
	//Create Main Panel
	private static JPanel mainPanel;
	private static JPanel vtkPanel;
	private JPanel all = null;
	private Dimension canvasSize = new Dimension();
	private int xCenter = BORDER_SIZE / 2;
	private int yCenter = BORDER_SIZE / 2;
	private ViewRange viewRange;
	private static final Logger log = Logger.getLogger(MainGUI.class);
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

	static MainMenu mainMenu;
	
	public static PoliticalBoundariesGUI pbGUI ;

	private static JPanel pluginGUIPanel;
	private static JScrollPane pluginGUIScrollPane;
	private static JSplitPane pluginSplitPane;
	private static boolean alreadyResized;
	private static Object id;

	private int xeBorder=0;
	private int ysBorder=0;
	private GraticuleGUI gridGUI;
	GraticulePlugin gridPlugin;
	private ViewRange viewrange;
	private static ArrayList<vtkActor> allActors = new ArrayList<vtkActor>();
	private static ArrayList<vtkActor> allTextActors = new ArrayList<vtkActor>();
	 vtkActor tempGlobeScene = new vtkActor();
	 vtkActor2D labelActor =new vtkActor2D();
	 vtkActor pointActor = new 
			    vtkActor();
	private boolean gridDisplay = true;
	private DrawingToolsGUI drawingTool;
	private DrawingToolsPlugin drawingToolPlugin;
	private double[] pointerPosition;
	private ScriptingPlugin scriptingPluginObj;
	public MainGUI() {
		
		/*vtkTransform transform = new  vtkTransform();
		  transform.Translate(-1.0, -1.5, 0.0);
		vtkAxesActor axes = new vtkAxesActor();
		  // The axes are positioned with a user transform
		  axes.SetUserTransform(transform);

		  // the actual text of the axis label can be changed:
		 axes.SetXAxisLabelText("");
		  axes.SetYAxisLabelText("");
		  axes.SetZAxisLabelText("");
		 axes.SetScale(0.2);
		 renderWindow.GetRenderer().AddActor(axes);
		 //renderWindow.GetRenderer().ResetCamera(axes.GetBounds());
		 */
		
	    //add modules like CFM, regions    
		
		 
	    //cfm
	    
	    
	    
	    
		//creates main window  
        
        
        //add ui classes 
        //createMenu();
        
		/*
		 * The GUI hierarchy is:
		 * 	This class (MainGUI) extends JFrame
		 * 		pluginSplitPane
		 * 			mainPanel - separate Panel to hold renderWindow so we can handle it if all the plugin tabs are closed
		 * 				renderWindow
		 * 			pluginGUIScrollPane, so the pluginGUI scrolls if needed
		 * 				pluginGUIPanel, the panel containing all the plugin GUI tabs
		 * 					pluginTabPane, the pane with all the plugin GUI tabs.  New tabs are added here.
		 * 				
 		 */
		
		
		renderWindow = new vtkCanvas ();
		vtkCamera camera = new vtkCamera();
		renderWindow.GetRenderer().SetActiveCamera(camera);
		mainPanel = new JPanel(new BorderLayout());
		vtkPanel = new JPanel(new BorderLayout());
		vtkPanel.add(renderWindow,BorderLayout.CENTER);
		mainPanel.add(vtkPanel, BorderLayout.CENTER);
		
		//renderWindow.setFocusable(true);
		renderWindow.GetRenderer().SetBackground(0,0,0);
		
		mainMenu = new MainMenu();
		pluginGUIPanel = new JPanel();
		pluginTabPane =  new JTabbedPane();
		
		Info.setMainGUI(this);
		setUpPluginTabs();
		
		pluginSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, mainPanel, pluginGUIScrollPane);
		pluginSplitPane.setOneTouchExpandable(false);
		pluginSplitPane.setResizeWeight(1);
		pluginSplitPane.setDividerLocation(0.5);

		//Set preferred sizes
		Dimension d = new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight());
		pluginGUIScrollPane.setMinimumSize(d);
		pluginGUIScrollPane.setPreferredSize(d);
		Dimension minimumSize = new Dimension(100, 50);
		mainPanel.setMinimumSize(minimumSize);//new Dimension(Prefs.getMainWidth(), Prefs.getMainHeight()));
		renderWindow.setMinimumSize(new Dimension(Prefs.getMainWidth(), Prefs.getMainHeight()));

		renderWindow.GetRenderer().AddActor(tempGlobeScene);
		renderWindow.GetRenderer().AddActor(pointActor);
		renderWindow.GetRenderer().AddActor(labelActor);
		renderWindow.GetRenderWindow().SetPointSmoothing(35);
		renderWindow.GetRenderWindow().PointSmoothingOn();
		addDefaultActors();
		
		//mouse event
		final vtkCellPicker cellPicker = new vtkCellPicker();
		 
	    // Show the point on the sphere the mouse is hovering over in the status bar
		renderWindow.addMouseListener(new MouseAdapter()
	    {
	    	 //public void mouseClicked(MouseEvent e)
		      //{
	    		 public void mousePressed(MouseEvent e) {
	                  if (e.getButton() == MouseEvent.BUTTON3) {
	     
	        // The call to Pick needs to be surrounded by lock and unlock to prevent crashes.
	                	  renderWindow.lock();
	        int pickSucceeded = cellPicker.Pick(e.getX(), renderWindow.getHeight()-e.getY()-1,
	            0.0, renderWindow.GetRenderer());
	        renderWindow.unlock();
	 
	        if (pickSucceeded == 0)
	        {
	        	//gets points position 
	          double[] p = cellPicker.GetPickPosition();
	          setPointerPosition(cellPicker.GetPickPosition());
	          System.out.println("Position: " + p[0] + ", " + p[1] + ", " + p[2]);
	          //framePoints.add(p);
	        }
		    	  }
	      }
	    });
        try {
        	mainMenu.availablePlugins = Plugins.getAvailablePlugins();
        	mainMenu.setupPluginMenus();
			setMainFrame();
			//Update the divider location so that the plugin pane doesn't require horizontal scrolling
			pluginSplitPane.setDividerLocation(this.getWidth() - pluginTabPane.getPreferredSize().width - 60);
			
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to get available plugins", ioe);
		}
	}

	public void setPointerPosition(double[] ds)
	{
		pointerPosition = ds;
	}
	public double[] getPointerPosition()
	{
		return pointerPosition;
	}
	public static File getRootPluginDir(){
		return  new File( getCWD() + File.separator+ "data");
	}
	public static File getCWD(){
		
		if(getCWD==null)
		{
			getCWD=new File(System.getProperty("user.dir"));
		}
		//System.out.println("user.dir is: " + System.getProperty("user.dir"));
		return getCWD;
	}
	/*public void updateActors(ArrayList<vtkActor> allCFMActors)
	{
		
	    if(allCFMActors.size()>0){
	    	//loading form previous import
	    for(int i =0;i<allCFMActors.size();i++)
	    {
	    	renderWindow.GetRenderer().AddActor(allCFMActors.get(i));
	    	
	    }
	    }
	    updateRenderWindow();
	}*/
	public void addActors(ArrayList allActors)
	{
		vtkPropCollection  renderedActors = renderWindow.GetRenderer().GetViewProps();
		//System.out.println(renderWindow.GetRenderer().GetViewProps().GetNumberOfItems());
		if(allActors.size()>0){
	    		
	    		for(int j =0;j<allActors.size();j++)
			    {
	    			renderWindow.GetRenderer().AddActor((vtkProp) allActors.get(j));
			    }
		}
	    updateRenderWindow();
	    //System.out.println(renderWindow.GetRenderer().GetViewProps().GetNumberOfItems());
	   
	}
	public void removeActors(ArrayList allActors)
	{
		vtkPropCollection renderedActors = renderWindow.GetRenderer().GetViewProps();
			if(allActors.size()>0){
				for(int j =0;j<allActors.size();j++)
			    {
					if(renderedActors.IsItemPresent((vtkObject) allActors.get(j))!=0)
					{
						 renderWindow.GetRenderer().GetViewProps().RemoveItem((vtkObject) allActors.get(j));
					}
					else{	System.out.println("no actor found to remove");}
			    	
			    }
		}
	    updateRenderWindow();
	    //renderWindow.GetRenderer().ResetCamera(allTextActors.get(allTextActors.size()-1).GetBounds());
	}
	/*public static void addActorsToAllActors(ArrayList<vtkActor> ar)
	{
		allActors.addAll(ar);
	}
	
	public static void addActorToAllActors(vtkActor ar)
	{
		allActors.add(ar);
	}*/
	public static ArrayList<vtkActor> getActorToAllActors()
	{
		return allActors;
	}
	
	private void addDefaultActors()
	{
		pbGUI = new PoliticalBoundariesGUI();
		
		addPluginGUI("org.scec.vdo.politicalBoundaries","Political Boundaries",pbGUI.loadAllRegions());
	
		
		//regions - us
		
    
    // ArrayList<ArrayList> actorPoliticalBoundariesMain = new ArrayList<ArrayList>();
	 ArrayList<vtkActor> actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
	 actorPoliticalBoundariesSegments = pbGUI.getPoliticalBoundaries();
	 
	 if(actorPoliticalBoundariesSegments.size()>0){
	 
		// actorPoliticalBoundariesSegments = actorPoliticalBoundariesMain.get(i);
		 for(int j =0;j<actorPoliticalBoundariesSegments.size();j++)
		 {
			 vtkActor pbActor = actorPoliticalBoundariesSegments.get(j);
			 renderWindow.GetRenderer().AddActor(pbActor);
			 //if(j==4)
				//updateRenderWindow(pbActor);
		 }
	 
	 }
	
	 setViewRange(new ViewRange());
	 //draw Grid
	 gridGUI = new GraticuleGUI(gridPlugin);
	 addPluginGUI("org.scec.vdo.graticulePlugin","Graticule",gridGUI);
	 makeGrids(gridGUI.getGlobeBox(1.0));
	
	 
	//draw DrawingTools
	drawingTool = new DrawingToolsGUI(drawingToolPlugin);
	addPluginGUI("org.scec.vdo.drawingToolsPlugin","Drawing Tools",drawingTool);
		
	}
	
	public void makeGrids(ArrayList<GlobeBox> gbs)
	{

		 vtkPolyDataMapper tempMapper = (vtkPolyDataMapper) (gbs.get(0)).globeScene; 
		 tempGlobeScene.SetMapper(tempMapper);
		 if(gbs.get(0).getLineColor() != null)
			 tempGlobeScene.GetProperty().SetColor(Info.convertColor(gbs.get(0).getLineColor()));
		 else
			 tempGlobeScene.GetProperty().SetColor(1,1,1);
		 tempGlobeScene.Modified();
		 //renderWindow.GetRenderer().Render();
		 
		 vtkPolyData  plyGrid = tempMapper.GetInput();
		 

		 pointActor.SetMapper(gbs.get(0).ptMapper);
		 pointActor.Modified();
		 labelActor.SetMapper(gbs.get(0).labelMapperLat);
		 labelActor.Modified();
		renderWindow.GetRenderer().ResetCamera(tempGlobeScene.GetBounds());
	}
	public void toggleGridDisplay() {
		if (!this.gridDisplay) {
			tempGlobeScene.VisibilityOn();
			gridDisplay = true;
		} else {
			tempGlobeScene.VisibilityOff();
			gridDisplay = false;
		}
	}
	public boolean getGridDisplayBool() {
		return this.gridDisplay;
	}

	public vtkActor getGrid() {
		// TODO Auto-generated method stub
		return this.tempGlobeScene;
	}
	
	
	private void setUpPluginTabs() {
		pluginGUIPanel.add(pluginTabPane);
		pluginTabPane.addChangeListener((ChangeListener) this);
		pluginTabPane.setBorder(BorderFactory.createEtchedBorder());
		pluginGUIScrollPane = new JScrollPane(pluginGUIPanel);
//		pluginSplitPane = null;
	}
	//create frame and tabbed pane in main window
	private void setMainFrame()
	{	
		this.setTitle("SCEC VDO VTK");
		this.setMenuBar(mainMenu.getMenuBar());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(new Dimension(Prefs.getTotalWidth(), Prefs.getMainHeight()));
		this.setContentPane(pluginSplitPane);
		//This has to be done here, after the pluginSplitPane has been added to the GUI
		pluginSplitPane.setDividerSize(0);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}
	public JPanel getmainFrame()
	{
		return mainPanel;
	}
	//viewRange
	private void setViewRange(ViewRange viewRange) {
		this.viewRange = viewRange;
	}

	public ViewRange getViewRange() {
		return this.viewRange;
	}
	
	public void addPluginGUI(String id, String title, JComponent gui) {

		if (!mainMenu.isPluginActive(id) && id !="org.scec.vdo.politicalBoundaries" && id !="org.scec.vdo.graticulePlugin"
				&& id != "org.scec.vdo.drawingToolsPlugin") {
			//Logger
			log.debug("Cannot add gui for inactive plugin " + id);
			return;
		}

		boolean showing = isPluginGuiShowing();

		// Create a new plugin tab
		JPanel allPanel = new JPanel();
		allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.PAGE_AXIS));
		allPanel.add(gui);
		allPanel.add(Box.createVerticalGlue());
		allPanel.add(new JPanel());
		JScrollPane pluginTab = new JScrollPane(allPanel);
		pluginTab.setName(id);		

		// Add the tab to the tab panel
		pluginTabPane.addTab(title, pluginTab);
		if(id !="org.scec.vdo.politicalBoundaries" && id !="org.scec.vdo.graticulePlugin" && id != "org.scec.vdo.drawingToolsPlugin")
			pluginTabPane.setTabComponentAt(pluginTabPane.getTabCount() -1, new ButtonTabComponent(pluginTabPane, id));
		else
			pluginTabPane.setTabComponentAt(pluginTabPane.getTabCount() -1,null);
		
		if(id.equals("org.scec.vdo.plugins.ScriptingPlugin") )
			scriptingPluginObj = (ScriptingPlugin) mainMenu.getActivePlugins().get(id);		

		pluginTabPane.repaint();
		
		SwingUtilities.updateComponentTreeUI(this);
	}

	public boolean removePluginGUI(String id) {

		if (!mainMenu.isPluginActive(id)) {
			log.warn("Cannot remove GUI for inactive plugin " + id);
			return false;
		}

		boolean removed = false;
		boolean showing = isPluginGuiShowing();

		// Find the plugin gui to remove
		for (int j = 0; j < pluginTabPane.getTabCount(); j++) {
			Component c = pluginTabPane.getComponentAt(j);
			if (c.getName() != null && c.getName().equals(id)) {

				// Remove the gui
				pluginTabPane.removeTabAt(j);

				// If it was the last gui, remove the split pane
				if (pluginTabPane.getTabCount() == 0) {
					//remove(pluginSplitPane);
					alreadyResized = false;
					//pluginSplitPane = null;
					//setContentPane(all);
					
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
		// TODO Auto-generated method stub
		return (pluginTabPane.getTabCount() > 0);
	}
//update political boundaries
	public  void updatePoliticalBoundaries()
	{
		ArrayList<vtkActor> actorPoliticalBoundariesSegments = new ArrayList<vtkActor>();
		 actorPoliticalBoundariesSegments = pbGUI.getPoliticalBoundaries();
		 
		 if(actorPoliticalBoundariesSegments.size()>0){
		 
			// actorPoliticalBoundariesSegments = actorPoliticalBoundariesMain.get(i);
			 for(int j =0;j<actorPoliticalBoundariesSegments.size();j++)
			 {
				 vtkActor pbActor = actorPoliticalBoundariesSegments.get(j);
				 renderWindow.GetRenderer().AddActor(pbActor);
			 }
		 
		 }
	}
	

	//update renderwindow and focus on actor
	public static void updateRenderWindow(vtkActor actor)
	{
		renderWindow.Render();
		renderWindow.GetRenderer().ResetCamera(actor.GetBounds());
		//renderWindow.repaint(); 
	}
	//just update renderwindow
	public static void updateRenderWindow()
	{
		//updateActors(getActorToAllActors());
		renderWindow.Render();
		//renderWindow.repaint();
	}
	 
	public static vtkCanvas  getRenderWindow() {
		return renderWindow;
	}


	

	

	public void removeSplitPane() {
		// TODO Auto-generated method stub
		remove(pluginSplitPane);
		alreadyResized = false;
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
	 * pickability off.
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

		// Update all loaded, clickable plugins
		for (Plugin p : mainMenu.loadedPlugins.values()) {
			if (p instanceof ClickablePlugin) {
				ClickablePlugin cp = (ClickablePlugin) p;
				if (cp.getId().equals(selectedPlugin)) {
					cp.setClickableEnabled(true);
				} else {
					cp.setClickableEnabled(false);
				}
			}
		}
	}
	/*public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void componentResized(ComponentEvent e) {
		/*if (showNavMap) {
			viewPlatform.setPlatformGeometry(getHUDGeometry());
			you.setRedDot(keyBehv.getFocalPoint());
		}*/
		/*updateCanvasSize();
	}
	public void componentShown(ComponentEvent e) {
	}*/

	
	
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
	        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	    }
	 
	    private class TabButton extends JButton implements ActionListener {
	        /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public TabButton() {
	            int size = 17;
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
	            	pane.remove(i);
	            	mainMenu.updateMenu(pluginID);
	            }
	    		//if (loadedPlugins.size() == 0)
	    			//removeSplitPane();
	        }
	 
	    }
	 
	    private final MouseListener buttonMouseListener = new MouseAdapter() {
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
	}

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
			private MenuElement[] path;

			/*{
				getModel().addChangeListener(new ChangeListener() {

					
					public void stateChanged(ChangeEvent e) {
						if (getModel().isArmed() && isShowing()) {
							path = MenuSelectionManager.defaultManager().getSelectedPath();
						}
					}
				});
			}*/

			/**
			 * @see JCheckBoxMenuItem#JCheckBoxMenuItem()
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

		public ScriptingPluginGUI GetScriptingPlugin() {
			// TODO Auto-generated method stub
			if(scriptingPluginObj==null || scriptingPluginObj.getGratPanel()==null)
			{
				mainMenu.activatePlugin("org.scec.vdo.plugins.ScriptingPlugin");	
			}
			return scriptingPluginObj.getScriptingPluginGUI();
		}

	
	
	
	

}
