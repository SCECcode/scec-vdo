package org.scec.vtk.main;

import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
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
import org.scec.vtk.grid.GlobeBox;
import org.scec.vtk.grid.MakeGrids;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.politicalBoundaries.PoliticalBoundariesGUI;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.plugins.Plugins;

import vtk.vtkActor;
import vtk.vtkAxesActor;
import vtk.vtkCamera;
import vtk.vtkGlobeSource;
import vtk.vtkNativeLibrary;
import vtk.vtkPanel;
import vtk.vtkPolyDataMapper;
import vtk.vtkTransform;

public  class MainGUI extends JFrame implements ChangeListener{
	private final int BORDER_SIZE = 10;
	private static JFrame frame ;
	private static vtkPanel renderWindow;
	private static JTabbedPane pluginTabPane;
	//Create Main Panel
	private static JPanel mainPanel;
	private JPanel all = null;
	private Dimension canvasSize = new Dimension();
	private int xCenter = BORDER_SIZE / 2;
	private int yCenter = BORDER_SIZE / 2;
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
	private static  JSplitPane pluginSplitPane;
	private static boolean alreadyResized;
	private static Object id;

	private int xeBorder=0;
	private int ysBorder=0;

	private static ArrayList<vtkActor> allActors = new ArrayList<vtkActor>();
	
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
        
		renderWindow = new vtkPanel();
		vtkCamera camera = new vtkCamera();
		renderWindow.GetRenderer().SetActiveCamera(camera);
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout());
		renderWindow.setFocusable(true);
		mainPanel.add(renderWindow);
		mainMenu = new MainMenu();
		pluginGUIPanel = new JPanel();
		pluginTabPane =   new JTabbedPane();
		
		setUpPluginTabs();
		addDefaultActors();
		Info.setMainGUI(this);
		
        try {
        	mainMenu.availablePlugins = Plugins.getAvailablePlugins();
        	mainMenu.setupPluginMenus();
			setMainFrame();
			
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to get available plugins", ioe);
		}
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
	public static void updateActors(ArrayList<vtkActor> allCFMActors)
	{
		
	    if(allCFMActors.size()>0){
	    	//loading form previous import
	    for(int i =0;i<allCFMActors.size();i++)
	    {
	    	renderWindow.GetRenderer().AddActor(allCFMActors.get(i));
	    	
	    }
	    //updateRenderWindow();
	    updateRenderWindow(allCFMActors.get(allCFMActors.size()-1));
	    }
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
		
		addPluginGUI("org.scec.vdo.politicalBoundaries","Political Boundaries",pbGUI.loadRegion());
	
		
		//regions - us
		
    
     ArrayList<ArrayList> actorPoliticalBoundariesMain = new ArrayList<ArrayList>();
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
	
	 //draw Grid
	 MakeGrids mgrids = new MakeGrids();
	 
	 ArrayList<GlobeBox> gbs = mgrids.getGlobeBox();
	 vtkActor tempGlobeScene = new vtkActor();
	 vtkPolyDataMapper tempMapper = (vtkPolyDataMapper) (gbs.get(0)).globeScene; 
	 tempGlobeScene.SetMapper(tempMapper);
	 renderWindow.GetRenderer().AddActor(tempGlobeScene);
		 
	renderWindow.GetRenderer().ResetCamera(tempGlobeScene.GetBounds());
	
	}
	
	private void setUpPluginTabs() {
		pluginGUIPanel.add(pluginTabPane);
		pluginTabPane.addChangeListener((ChangeListener) this);
		pluginTabPane.setBorder(BorderFactory.createEtchedBorder());
		pluginGUIScrollPane = new JScrollPane(pluginGUIPanel);
		pluginSplitPane = null;
	}
	//create frame and tabbed pane in main window
	private void setMainFrame()
	{
		
		mainPanel.add(pluginGUIPanel);
		frame = new JFrame("Scec VDO VTK");
		frame.setMenuBar(mainMenu.getMenuBar());     
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
	}
	
	public void addPluginGUI(String id, String title, JPanel gui) {

		if (!mainMenu.isPluginActive(id) && id !="org.scec.vdo.politicalBoundaries") {
			//Logger
			log.debug("Cannot add gui for inactive plugin " + id);
			return;
		}

		boolean showing = isPluginGuiShowing();

		//Dimension d = new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight());
		//all.setMinimumSize(d);
		//pluginGUIScrollPane.setMinimumSize(d);
		//pluginGUIScrollPane.setPreferredSize(d);


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
		pluginTabPane.setTabComponentAt(pluginTabPane.getTabCount() -1, new ButtonTabComponent(pluginTabPane, id));
		pluginTabPane.setSelectedIndex(pluginTabPane.getTabCount() - 1);		

		// If the split pane was removed, re-add it
		/*if (pluginSplitPane == null) {
			pluginSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					false, all, pluginGUIScrollPane);
			((JSplitPane) pluginSplitPane).setOneTouchExpandable(false);
			((JSplitPane) pluginSplitPane).setDividerSize(0);
			((JSplitPane) pluginSplitPane).setResizeWeight(1);
			((JSplitPane) pluginSplitPane).setDividerLocation(0.5);
			remove(all);
			setContentPane((Container) pluginSplitPane);
		}*/

		// Note: pluginTabPane.getGraphics() will return null if pluginSplitPane
		// has not been created
		//.update(pluginTabPane.getGraphics()); // forces it to show
		pluginTabPane.repaint();
		// the new tab

		/*if (!alreadyResized) {
			((JSplitPane) pluginSplitPane).setDividerLocation(this.getWidth()
					- pluginTabPane.getPreferredSize().width - 60);
			alreadyResized = true;
		}*/

		// I think this means that if we enable the first plugin
		// and the plugin split pane gets created, and we are
		// showing the navigation map HUD, then we need to
		// update the HUD.
		/*if (showNavMap && (showing != isPluginGuiShowing())) {
			viewPlatform.setPlatformGeometry(getHUDGeometry());
			you.setRedDot(keyBehv.getFocalPoint());
		}*/

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

	

	//update renderwindow and focus on actor
	public static void updateRenderWindow(vtkActor actor)
	{
		renderWindow.GetRenderer().ResetCamera(actor.GetBounds());
		renderWindow.repaint(); 
	}
	//just update renderwindow
	public static void updateRenderWindow()
	{
		updateActors(getActorToAllActors());
		renderWindow.repaint();
	}
	 
	public static vtkPanel getRenderWindow() {
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
				//super(text, icon);
				//		    super.addKeyListener(this);
				//super.addMenuKeyListener(shiftDetector);
			}

			/**
			 * @see JCheckBoxMenuItem#JCheckBoxMenuItem(String, Icon, boolean)
			 */
			public StayOpenCheckBoxMenuItem(String text, Icon icon, boolean selected) {
				//super(text, icon, selected);
				//		    super.addKeyListener(this);
				//super.addMenuKeyListener(shiftDetector);
			}

			/**
			 * Overridden to reopen the menu.
			 *
			 * @param pressTime the time to "hold down" the button, in milliseconds
			 */
			
			/*public void doClick(int pressTime) {
				super.doClick(pressTime);
				if (shiftDetector.shiftDown)
					MenuSelectionManager.defaultManager().setSelectedPath(path);
			}*/
		}
	
	
	
	

}
