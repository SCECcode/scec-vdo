package org.scec.vtk.plugins.EarthquakeCatalogPlugin;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.CatalogAccessor;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.CatalogTable;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.ComcatResourcesDialog;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQCatalog;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.EQPickBehavior;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.Earthquake;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.FocalMechIcons;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.FocalMechRenderer;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components.SourceCatalog;
import org.scec.vtk.plugins.utils.components.AddButton;
import org.scec.vtk.plugins.utils.components.ColorWellButton;
import org.scec.vtk.plugins.utils.components.EditButton;
import org.scec.vtk.plugins.utils.components.EndButton;
import org.scec.vtk.plugins.utils.components.GradientColorChooser;
import org.scec.vtk.plugins.utils.components.HelpButton;
import org.scec.vtk.plugins.utils.components.NewObjButton;
import org.scec.vtk.plugins.utils.components.ObjectInfoDialog;
import org.scec.vtk.plugins.utils.components.PauseButton;
import org.scec.vtk.plugins.utils.components.PlayButton;
import org.scec.vtk.plugins.utils.components.ReferenceButton;
import org.scec.vtk.plugins.utils.components.RemoveButton;
import org.scec.vtk.plugins.utils.components.SaveButton;
import org.scec.vtk.plugins.utils.components.StopButton;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.picking.PickHandler;

import javafx.animation.Animation;

import vtk.vtkActor;
import vtk.vtkAnimationScene;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkGlyph3D;
import vtk.vtkMapper;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnsignedCharArray;
import vtk.vtkVertexGlyphFilter;

public class EarthquakeCatalogPluginGUI extends JPanel implements
ActionListener,
ChangeListener,
ListSelectionListener,
TableModelListener,
MouseListener
{

	/**
	 * The primary GUI for the <code>EarthquakeCatalogPlugin</code>. This class handles most data and
	 * selection events that affect the appearance of GUI buttons and as such is registered
	 * as a listener for tables and lists contained within this user interface.
	 *
	 * Created on Feb 3, 2005
	 *
	 * @author P. Powers
	 * @author Ifraz Haqque  (added Animation Functionality)
	 * @author Addie Beseda  (added Cow Easter Egg display property) -- 
	 * @author Justin Perez  (added some focal mechanism disc functionality)
	 * @author Joshua Garcia (added some focal mechanism disc color changing functionality)
	 * @author Ryan Lacey    (added space-time functionality)
	 * @version $Id: EQCatalogGUI.java 4543 2013-07-18 16:30:17Z jeremypc $
	 */

	private PluginActors pluginActors;

	private static ArrayList<Earthquake> earthquakes = new ArrayList<Earthquake>();
	private static final long serialVersionUID = 1L;

	private static final String NO_VALUE = " -- ";
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##0.0");
	private static  final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");

	private static int a_l	= GridBagConstraints.LINE_START;       // anchor left
	private static int a_c	= GridBagConstraints.CENTER;           // anchor center
	private static int a_r	= GridBagConstraints.LINE_END;         // anchor right
	private static int f	= GridBagConstraints.NONE;               // fill none

	// catalog library panel accessible components
	private JPanel			libraryPanel;
	//protected CatalogTable	catalogTable;
	//private SourceList   	sourceList;

	private AddButton		newDiskSourceButton;

	private AddButton		newInternetSourceButton;
	private NewObjButton	newFromLibraryButton;
	private ReferenceButton referenceButton;
	private SaveButton		exportLibraryCatButton;
	private EditButton		editLibraryCatButton;
	private RemoveButton	remLibraryCatButton;
	private HelpButton		helpButton;


	// catalog properties panel adjustable-value components
	private JPanel propsExtentsPanel;
	private JLabel catProp_dateFromVal;
	private JLabel catProp_dateToVal;
	private JLabel catProp_extentsNval;
	private JLabel catProp_extentsWval;
	private JLabel catProp_extentsEval;
	private JLabel catProp_extentsSval;
	private JLabel catProp_minDepthVal;
	private JLabel catProp_maxDepthVal;
	private JLabel catProp_minMagVal;
	private JLabel catProp_maxMagVal;
	private JLabel catProp_numEventsVal;
	private JLabel catProp_sourceVal;

	// catalog properties panel adjustable-value components BETA: Animation
	public JPanel propsAnimationPanel;
	private JLabel catProp_dateFromVal1;
	private JLabel catProp_dateToVal1;
	private JLabel catProp_extentsNval1;
	private JLabel catProp_extentsWval1;
	private JLabel catProp_extentsEval1;
	private JLabel catProp_extentsSval1;
	private JLabel catProp_minDepthVal1;
	private JLabel catProp_maxDepthVal1;
	private JLabel catProp_minMagVal1;
	private JLabel catProp_maxMagVal1;
	private JLabel catProp_numEventsVal1;
	public JCheckBox catProp_loop;
	public JTextField catProp_duration;
	private JLabel catProp_animation;
	public JRadioButton catProp_relative;
	public JRadioButton catProp_static;
	public PlayButton catProp_playButton;
	public PauseButton catProp_pauseButton;
	public StopButton catProp_stopButton;
	public EndButton catProp_endButton;
	public JPanel controlPanel;
	private int animationStyle;
	private JCheckBox animationDisplayTimeCheckbox = new JCheckBox();

	// display properties panel adjustable components
	private JPanel          propsDisplayPanel;
	// Geometry:
	private JLabel          dispProp_geometry;				// "Geometry:" label
	private JRadioButton    dispProp_geomPoint;				// "Point" radio button
	private JRadioButton    dispProp_geomSphere;			// "Sphere" radio button
	// Magnitude scaling:
	private JLabel          dispProp_scaling;				// "Magnitude scaling:" label
	private JSlider			dispProp_slider;				// Additional scaling options

	// Color:
	private JLabel          dispProp_color;					// "Color:" label
	private ColorWellButton dispProp_colGradientButton;				// Color button
	private JLabel			lowerGradientLabel;
	private JLabel			higherGradientLabel;
	// Apply gradient to:
	private JLabel          dispProp_gradient;				// "Apply gradient to:" label
	private JRadioButton    dispProp_gradDepth;				// "Depth" radio button
	private JRadioButton    dispProp_gradMag;				// "Magnitude" radio button
	// Use RecentEQ coloring:
	private JLabel			dispProp_recenteqcolor;			// "Use recent earthquake coloring:" label
	private JCheckBox		dispProp_recentCheckBox;		// Recent color checkbox

	// Use DiscreteEQ coloring:
	private JLabel			dispProp_discreteeqcolor;			// "Use discrete coloring:" label
	public JCheckBox		dispProp_discreteCheckBox;		// Discrete color checkbox

	// Use focal mechanism:
	private JLabel          dispProp_focalmechanism;			// "Use focal mechanism:" label
	private JRadioButton 	dispProp_focalNone;				// "None" radio button
	private JRadioButton	dispProp_focalBall;				// "Ball:" radio button
	private JComboBox       dispProp_focalBallDropDownBox;	// Ball style drop down box
	private JRadioButton	dispProp_focalDisc;				// "Disc:" radio button
	private JLabel 			dispProp_focalCompLabel;				// "Compression" label
	private ColorWellButton dispProp_focalCompColButton;			// Compression color well
	private JLabel 			dispProp_focalExtLabel;				// "Extension" label
	private ColorWellButton dispProp_focalExtColButton;			// Extension color well

	// Earthquake Transparency
	private JSlider transparencySlider;
	private  JLabel transLabel;

	//Space-time panel - Ryan Lacey / Marshall Rogers-Martinez 2011
	public JPanel spaceTimePanel;

	private JRadioButton timeButton;
	private JRadioButton depthButton;
	public static boolean timeButtonSelected;
	public static boolean depthButtonSelected;

	private JLabel depthSliderLabel;
	private JSlider depthSlider;
	public static int depthSliderValue = 0;

	private JLabel depthModLabel;
	private JTextField depthModField;
	public static int depthModifier = 250;
	private JButton applyDepthSetButton;

	private JButton getInfoButton;

	private JTabbedPane propsTabbedPane;

	// adjustable status field.
	public static JLabel status = new JLabel("Status");

	// accessory windows and dialogs
	private GradientColorChooser colorChooser;
	protected ObjectInfoDialog srcInfoDialog;

	public static JProgressBar progbar;
	public static JLabel progLabel;
	private Animation a;

	public static boolean bIsDiscreteColors = false;


	public static boolean bIsUnrecordedDepthFound = false;

	private JButton btnStatistics = new JButton("Open Earthquake Statistics");
	private JPanel statsPanel = new JPanel();


	//Max & Min Dates
	public static  Date maxDate, minDate;
	public static long maxDateMill, minDateMill;
	public static  long timeDifference;


	private CatalogTable catalogTable;

	private ComcatResourcesDialog netSourceDialog;

	ArrayList<vtkActor> earthquakePointActorList = new ArrayList<>();

	public CatalogAccessor catalogAcc;

	private EQPickBehavior pickHandler;

	private EarthquakeCatalogPlugin parentPlugin;
	public static ArrayList<EQCatalog> eqCatalogs = new ArrayList<>();


	// init data store
	static {
		String sourceStore =
				Prefs.getLibLoc() +
				File.separator + EarthquakeCatalogPlugin.dataStoreDir +
				File.separator + "source" +
				File.separator + "data";
		String displayStore =
				Prefs.getLibLoc() +
				File.separator + EarthquakeCatalogPlugin.dataStoreDir +
				File.separator + "display" +
				File.separator + "data";
		File file;
		if (!(file = new File(sourceStore)).exists()) file.mkdirs();
		if (!(file = new File(displayStore)).exists()) file.mkdirs();
	}

	public static ArrayList<Earthquake> getEarthquakes() {
		return earthquakes;
	}
	/**
	 * Constructs a new <code>CatalogGUI</code>. This constructor builds a custom
	 * <code>JPanel</code> to allow user control of earthquake catalog data.
	 * @throws IOException 
	 *
	 */

	public  EarthquakeCatalogPluginGUI(EarthquakeCatalogPlugin plugin){
		super();
		this.parentPlugin = plugin;
		pluginActors = plugin.getPluginActors();
		pickHandler = new EQPickBehavior();
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()));
		setName("Earthquake Catalog Plugin");

		JPanel upperPane = getLibraryPanel();
		upperPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		// creates and adds extents, animation, display, and ri gui's to the lower pane
		this.propsTabbedPane = new JTabbedPane();
		this.propsTabbedPane.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
		this.propsTabbedPane.add(getPropsExtentsPanel());
		this.propsTabbedPane.add(getPropsAnimationPanel());
		this.propsTabbedPane.add(getSpaceTimePanel());
		this.propsTabbedPane.add(getPropsDisplayPanel());


		// assemble lower pane
		JPanel lowerPane = new JPanel();
		lowerPane.setLayout(new BoxLayout(lowerPane,BoxLayout.PAGE_AXIS));
		lowerPane.add(this.propsTabbedPane);
		lowerPane.add(getStatusPanel());

		add(upperPane, BorderLayout.CENTER);
		add(lowerPane, BorderLayout.PAGE_END);
		// now load any data
		try {
			this.catalogTable.loadCatalogs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public PluginActors getPluginActors() {
		return pluginActors;
	}
	public void setPluginActors(PluginActors p) {
		pluginActors = p;
	}

	/**
	 * Returns a handle to the single instance of a <code>SourceInfoDialog</code> that is maintained
	 * by this plugin. This permits other components of this plugin to add and edit source catalog
	 * information.
	 *
	 * @return the info editing dialog
	 */
	public ObjectInfoDialog getSourceInfoDialog() {
		if (this.srcInfoDialog == null) {
			this.srcInfoDialog = new ObjectInfoDialog(this);
		}
		return this.srcInfoDialog;
	}

	public ObjectInfoDialog getSourceInfoDialogSC(SourceCatalog sc) {
		if (this.srcInfoDialog == null) {
			this.srcInfoDialog = new ObjectInfoDialog(this);
		}
		return this.srcInfoDialog;
	}

	/**
	 * Utility method that makes the display properties panel active.
	 */
	public void switchToDisplayPanel() {
		this.propsTabbedPane.setSelectedComponent(this.propsDisplayPanel);
	}

	/**
	 * Utility method to set properties of the display attributes panel using info
	 * from the given catalog. Implementation requires that this method be
	 * public.
	 *
	 * @param catalog source of display attribute info
	 */
	public void setDisplayPanel(EQCatalog catalog) {

		// display buttons are set to respond to user actions and are
		// en/disabled in reponse to ActionEvents. Since programmatic
		// selections also trigger events, it is important to set geometry
		// buttons last as they weild the most control over access to other
		// components.

		// set values first for components that do not change access to others
		//this.dispProp_scaleMenu.setSelectedIndex(catalog.getScaling());
		this.dispProp_colGradientButton.setColor(catalog.getColor1(), catalog.getColor2());
		this.dispProp_focalCompColButton.setColor(catalog.getDiscCompColor());
		this.dispProp_focalExtColButton.setColor(catalog.getDiscExtColor());
		this.dispProp_recentCheckBox.setSelected(catalog.getRecentEQColoring() == 1);
		//this.dispProp_discreteCheckBox.setSelected(catalog.getDiscreteEQColoring() == 1);
		if (catalog.getApplyGradientTo() == EQCatalog.GRADIENT_APPLY_DEPTH) {
			this.dispProp_gradDepth.setSelected(true);
		} else {
			this.dispProp_gradMag.setSelected(true);
		}
		// get a copy of the variable that knows what focal to display
		int selectedFocalDisplay = catalog.getFocalDisplay();
		// Determine which of the three possible selections to set as true
		switch(selectedFocalDisplay) {
		case EQCatalog.FOCAL_NONE:
			this.dispProp_focalNone.setSelected(true); // check the None box
			this.dispProp_focalBall.setSelected(false);
			this.dispProp_focalDisc.setSelected(false);
			break;

		case EQCatalog.FOCAL_BALL:
			this.dispProp_focalBall.setSelected(true); // check the Ball box
			this.dispProp_focalDisc.setSelected(false);
			this.dispProp_focalNone.setSelected(false);
			break;

		case EQCatalog.FOCAL_DISC:
			this.dispProp_focalDisc.setSelected(true); // Check the Disc box
			this.dispProp_focalBall.setSelected(false);
			this.dispProp_focalNone.setSelected(false);
			break;

		default:
			this.dispProp_focalNone.setSelected(true); // Check the None box by default
			break;
		}

		// do focal mechs
		this.dispProp_focalBallDropDownBox.setSelectedIndex(catalog.getFocalMech());

		// lastly enable and set geometry
		// -- using doClick simplifies updating panel selections and enabled components
		//    since it always fires an event. setSelected() will not fire an event if
		//    the component is already selected.
		setGeometryEnabled(true);
		if (catalog.getGeometry() == EQCatalog.GEOMETRY_SPHERE) {
			this.dispProp_geomSphere.doClick();
		} /*else if (catalog.getGeometry() == EQCatalog.GEOMETRY_COW){
				this.dispProp_geomCow.doClick();
			} */else{
				//this.dispProp_geomPoint.doClick();
			}
	}

	/**
	 * Method centralizes button enabling based on selections. An object's state
	 * may change without changing a selection so a means to alter button state's
	 * is needed outside of event handlers.
	 */
	public void processTableSelectionChange() {
		int selectedRow = this.catalogTable.getSelectedRow();
		if (selectedRow != -1) {
			this.newFromLibraryButton.setEnabled(true);
			this.exportLibraryCatButton.setEnabled(true);
			this.editLibraryCatButton.setEnabled(true);
			this.remLibraryCatButton.setEnabled(true);
		} else {
			this.newFromLibraryButton.setEnabled(false);
			this.exportLibraryCatButton.setEnabled(false);
			this.editLibraryCatButton.setEnabled(false);
			this.remLibraryCatButton.setEnabled(false);
		}
		setAttributePanels();
	}

	//****************************************
	//     PRIVATE & DEFAULT GUI METHODS
	//****************************************


	private JPanel getLibraryPanel() {

		// set up panel
		this.libraryPanel = new JPanel(new BorderLayout());
		this.libraryPanel.setName("Library");
		this.libraryPanel.setOpaque(false);

		// set up scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// set up table
		this.catalogTable = new CatalogTable(this, getPluginActors());
		scroller.setViewportView(this.catalogTable);
		scroller.getViewport().setBackground(this.catalogTable.getBackground());

		this.libraryPanel.add(scroller,BorderLayout.CENTER);
		this.libraryPanel.add(getLibraryBar(),BorderLayout.PAGE_END);

		return this.libraryPanel;
	}

	/**
	 * Creates the row of buttons that appears below the list of earthquake catalogs
	 * @return JPanel
	 */
	private JPanel getLibraryBar() {

		this.newDiskSourceButton	= new AddButton(this, "Import new catalog from disk", "Folder");
		this.newInternetSourceButton= new AddButton(this, "Import new catalog from Internet", "Earth");
		this.newFromLibraryButton	= new NewObjButton(this, "Filter a selected catalog");

		this.referenceButton		= new ReferenceButton(this, "Documentation");

		this.exportLibraryCatButton	= new SaveButton(this, "Export a catalog to disk");
		this.editLibraryCatButton	= new EditButton(this, "Edit catalog name or metadata");
		this.remLibraryCatButton	= new RemoveButton(this, "Remove catalog from SCEC-VDO");
		this.helpButton				= new HelpButton(this, "Help");

		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar,BoxLayout.LINE_AXIS));
		bar.setOpaque(false);
		bar.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		bar.add(this.newDiskSourceButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(this.newInternetSourceButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(this.newFromLibraryButton);
		bar.add(Box.createHorizontalGlue());
		bar.add(this.referenceButton);
		bar.add(Box.createHorizontalGlue());
		bar.add(this.exportLibraryCatButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(this.editLibraryCatButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(this.remLibraryCatButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(helpButton);

		return bar;
	}

	/**
	 * Creates the Extents Panel
	 * @return JPanel
	 */
	private JPanel getPropsExtentsPanel() {

		// set up panel
		this.propsExtentsPanel = new JPanel(new GridBagLayout());
		this.propsExtentsPanel.setName("Extents");
		this.propsExtentsPanel.setOpaque(false);

		// init value fields
		Font boldFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);
		Dimension latDim = new Dimension(40,16);
		Dimension lonDim = new Dimension(48,16);
		Dimension valDim = new Dimension(120,16);

		this.catProp_extentsNval = new JLabel(NO_VALUE);
		this.catProp_extentsNval.setFont(boldFont);
		this.catProp_extentsNval.setPreferredSize(latDim);
		this.catProp_extentsNval.setHorizontalAlignment(SwingConstants.RIGHT);
		this.catProp_extentsWval = new JLabel(NO_VALUE);
		this.catProp_extentsWval.setFont(boldFont);
		this.catProp_extentsWval.setPreferredSize(lonDim);
		this.catProp_extentsWval.setHorizontalAlignment(SwingConstants.RIGHT);
		this.catProp_extentsEval = new JLabel(NO_VALUE);
		this.catProp_extentsEval.setFont(boldFont);
		this.catProp_extentsEval.setPreferredSize(lonDim);
		this.catProp_extentsEval.setHorizontalAlignment(SwingConstants.RIGHT);
		this.catProp_extentsSval = new JLabel(NO_VALUE);
		this.catProp_extentsSval.setFont(boldFont);
		this.catProp_extentsSval.setPreferredSize(latDim);
		this.catProp_extentsSval.setHorizontalAlignment(SwingConstants.RIGHT);
		this.catProp_minDepthVal = new JLabel(NO_VALUE);
		this.catProp_minDepthVal.setFont(boldFont);
		this.catProp_minDepthVal.setPreferredSize(valDim);
		this.catProp_maxDepthVal = new JLabel(NO_VALUE);
		this.catProp_maxDepthVal.setFont(boldFont);
		this.catProp_maxDepthVal.setPreferredSize(valDim);
		this.catProp_minMagVal = new JLabel(NO_VALUE);
		this.catProp_minMagVal.setFont(boldFont);
		this.catProp_minMagVal.setPreferredSize(valDim);
		this.catProp_maxMagVal = new JLabel(NO_VALUE);
		this.catProp_maxMagVal.setFont(boldFont);
		this.catProp_maxMagVal.setPreferredSize(valDim);
		this.catProp_numEventsVal = new JLabel(NO_VALUE);
		this.catProp_numEventsVal.setFont(boldFont);
		this.catProp_numEventsVal.setPreferredSize(valDim);
		this.catProp_dateFromVal = new JLabel(NO_VALUE);
		this.catProp_dateFromVal.setFont(boldFont);
		this.catProp_dateFromVal.setPreferredSize(valDim);
		this.catProp_dateToVal = new JLabel(NO_VALUE);
		this.catProp_dateToVal.setFont(boldFont);
		this.catProp_dateToVal.setPreferredSize(valDim);
		this.catProp_sourceVal =    new JLabel(NO_VALUE);
		this.catProp_sourceVal.setFont(boldFont);
		this.catProp_sourceVal.setPreferredSize(valDim);

		boolean borders = false;
		if (borders) {
			this.catProp_extentsNval.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_extentsWval.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_extentsEval.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_extentsSval.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_minDepthVal.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_maxDepthVal.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_minMagVal.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_maxMagVal.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_numEventsVal.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_dateFromVal.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_dateToVal.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_sourceVal.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		}

		// set constant labels
		JLabel catProp_extentsN = new JLabel("(\u00b0N)");
		catProp_extentsN.setForeground(Color.GRAY);
		JLabel catProp_extentsW = new JLabel("(\u00b0W)");
		catProp_extentsW.setForeground(Color.GRAY);
		JLabel catProp_extentsE = new JLabel("(\u00b0E)");
		catProp_extentsE.setForeground(Color.GRAY);
		JLabel catProp_extentsS = new JLabel("(\u00b0S)");
		catProp_extentsS.setForeground(Color.GRAY);
		JLabel catProp_extents_to = new JLabel("to");
		JLabel catProp_minDepth = new JLabel("Depth (km) Min:");
		JLabel catProp_maxDepth = new JLabel("Max:");
		JLabel catProp_minMag = new JLabel("Magnitude Min:");
		JLabel catProp_maxMag = new JLabel("Max:");
		JLabel catProp_dateFrom = new JLabel("From:");
		JLabel catProp_dateTo = new JLabel("To:");
		JLabel catProp_numEvents = new JLabel("Total EQ's:");
		JLabel catProp_citation = new JLabel("Citation:");

		// assemble lat lon panel which spans 3 columns in extents panel
		JPanel catProp_latLonPanel = new JPanel(new GridBagLayout());
		catProp_latLonPanel.setOpaque(false);
		catProp_latLonPanel.add(this.catProp_extentsNval,  new GridBagConstraints( 2, 0, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
		catProp_latLonPanel.add(     catProp_extentsN,     new GridBagConstraints( 3, 0, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));
		catProp_latLonPanel.add(this.catProp_extentsWval,  new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
		catProp_latLonPanel.add(     catProp_extentsW,     new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));
		catProp_latLonPanel.add(     catProp_extents_to,   new GridBagConstraints( 2, 1, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
		catProp_latLonPanel.add(this.catProp_extentsEval,  new GridBagConstraints( 4, 1, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
		catProp_latLonPanel.add(     catProp_extentsE,     new GridBagConstraints( 5, 1, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));
		catProp_latLonPanel.add(this.catProp_extentsSval,  new GridBagConstraints( 2, 2, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
		catProp_latLonPanel.add(     catProp_extentsS,     new GridBagConstraints( 3, 2, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));

		// assemble catalog properties panel
		this.propsExtentsPanel.add(     catProp_latLonPanel,  new GridBagConstraints( 0, 0, 2, 1, 0.0, 0.0, a_c, f, new Insets(0,0,0,0), 0, 0 ));

		this.propsExtentsPanel.add(     catProp_minDepth,     new GridBagConstraints( 0, 1, 1, 1, 0.5, 0.0, a_r, f, new Insets(10,0,0,0), 0, 0 ));
		this.propsExtentsPanel.add(this.catProp_minDepthVal,  new GridBagConstraints( 1, 1, 1, 1, 0.5, 0.0, a_l, f, new Insets(10,10,0,0), 0, 0 ));
		this.propsExtentsPanel.add(     catProp_maxDepth,     new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, a_r, f, new Insets(0,0,0,0), 0, 0 ));
		this.propsExtentsPanel.add(this.catProp_maxDepthVal,  new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0, a_l, f, new Insets(0,10,0,0), 0, 0 ));

		this.propsExtentsPanel.add(     catProp_minMag,       new GridBagConstraints( 0, 3, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));
		this.propsExtentsPanel.add(this.catProp_minMagVal,    new GridBagConstraints( 1, 3, 1, 1, 0.0, 0.0, a_l, f, new Insets(8,10,0,0), 0, 0 ));
		this.propsExtentsPanel.add(     catProp_maxMag,       new GridBagConstraints( 0, 4, 1, 1, 0.0, 0.0, a_r, f, new Insets(0,0,0,0), 0, 0 ));
		this.propsExtentsPanel.add(this.catProp_maxMagVal,    new GridBagConstraints( 1, 4, 1, 1, 0.0, 0.0, a_l, f, new Insets(0,10,0,0), 0, 0 ));

		this.propsExtentsPanel.add(     catProp_dateFrom,     new GridBagConstraints( 0, 5, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));
		this.propsExtentsPanel.add(this.catProp_dateFromVal,  new GridBagConstraints( 1, 5, 1, 1, 0.0, 0.0, a_l, f, new Insets(8,10,0,0), 0, 0 ));
		this.propsExtentsPanel.add(     catProp_dateTo,       new GridBagConstraints( 0, 6, 1, 1, 0.0, 0.0, a_r, f, new Insets(0,0,0,0), 0, 0 ));
		this.propsExtentsPanel.add(this.catProp_dateToVal,    new GridBagConstraints( 1, 6, 1, 1, 0.0, 0.0, a_l, f, new Insets(0,10,0,0), 0, 0 ));

		this.propsExtentsPanel.add(     catProp_numEvents,    new GridBagConstraints( 0, 7, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));
		this.propsExtentsPanel.add(this.catProp_numEventsVal, new GridBagConstraints( 1, 7, 1, 1, 0.0, 0.0, a_l, f, new Insets(8,10,0,0), 0, 0 ));

		this.propsExtentsPanel.add(     catProp_citation,       new GridBagConstraints( 0, 8, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,8,0), 0, 0 ));
		this.propsExtentsPanel.add(this.catProp_sourceVal,    new GridBagConstraints( 1, 8, 1, 1, 0.0, 0.0, a_l, f, new Insets(8,10,8,0), 0, 0 ));

		return this.propsExtentsPanel;
	}

	private JPanel getSpaceTimePanel() {

		this.spaceTimePanel = new JPanel(new GridBagLayout());

		this.spaceTimePanel.setName("Space-Time");/*Treatise on Space-Time and Depth Properties of Seismological Events*/
		this.spaceTimePanel.setOpaque(false);

		//Orientation Radio Buttons
		JLabel displayLabel = new JLabel("Display by: ");

		depthButton = new JRadioButton("Depth");
		timeButton = new JRadioButton("Time");

		timeButton.setOpaque(false);
		depthButton.setOpaque(false);

		depthButton.addActionListener(this);
		timeButton.addActionListener(this);

		ButtonGroup displayGroup = new ButtonGroup();
		displayGroup.add(depthButton);
		displayGroup.add(timeButton);

		// User input field
		depthModLabel = new JLabel("Range: ");
		depthModField = new JTextField(10);

		applyDepthSetButton = new JButton("Apply Range");
		applyDepthSetButton.addActionListener(this);

		//Slider for depth movement
		depthSliderLabel = new JLabel("Move Vertically (km): ");
		depthSlider = new JSlider(-depthModifier * 2, depthModifier * 2, 0);
		depthSlider.addChangeListener(this);
		depthSlider.setPaintLabels(true);
		depthSlider.setMajorTickSpacing(depthModifier);
		depthSlider.setOpaque(false);

		//Info button for time-axis input in LocationPlugin		
		getInfoButton = new JButton("Get Info");

		getInfoButton.addActionListener(this);

		//defaults
		depthButtonSelected = true;
		depthButton.setSelected(true);
		depthModField.setEnabled(false);
		depthSlider.setEnabled(false);
		applyDepthSetButton.setEnabled(false);
		getInfoButton.setDefaultCapable(true);

		this.spaceTimePanel.add(     displayLabel,        new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, f, new Insets(5,0,0,0), 0, 0 ));
		this.spaceTimePanel.add(     depthButton,         new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, f, new Insets(5,0,0,0), 0, 0 ));
		this.spaceTimePanel.add(     timeButton,          new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, f, new Insets(5,0,0,0), 0, 0 ));  
		this.spaceTimePanel.add(     depthModLabel,       new GridBagConstraints( 0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, f, new Insets(25,0,0,0), 0, 0 )); 
		this.spaceTimePanel.add(     depthModField,       new GridBagConstraints( 1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, f, new Insets(25,0,0,0), 0, 0 )); 
		this.spaceTimePanel.add(     depthSliderLabel,    new GridBagConstraints( 0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, f, new Insets(25,0,0,0), 0, 0 ));
		this.spaceTimePanel.add(     depthSlider,         new GridBagConstraints( 1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, f, new Insets(25,0,0,0), 0, 0 ));
		this.spaceTimePanel.add(     getInfoButton,       new GridBagConstraints( 1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, f, new Insets(65,35,0,0), 0, 0 ));		
		this.spaceTimePanel.add(     applyDepthSetButton, new GridBagConstraints( 0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,     f, new Insets(65,0,0,0), 0, 0 ));

		return this.spaceTimePanel;
	}

	/**
	 * Creates Animation Panel
	 * @return JPanel
	 */
	private JPanel getPropsAnimationPanel() {

		// set up panel
		this.propsAnimationPanel = new JPanel(new GridBagLayout());

		//JPanel controlPanel = new JPanel(new FlowLayout());
		this.propsAnimationPanel.setName("Animation");
		this.propsAnimationPanel.setOpaque(false);

		// init value fields
		Font boldFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);
		Dimension latDim = new Dimension(40,16);
		Dimension lonDim = new Dimension(48,16);
		Dimension valDim = new Dimension(120,16);

		this.catProp_extentsNval1 = new JLabel(NO_VALUE);
		this.catProp_extentsNval1.setFont(boldFont);
		this.catProp_extentsNval1.setPreferredSize(latDim);
		this.catProp_extentsNval1.setHorizontalAlignment(SwingConstants.RIGHT);
		this.catProp_extentsWval1 = new JLabel(NO_VALUE);
		this.catProp_extentsWval1.setFont(boldFont);
		this.catProp_extentsWval1.setPreferredSize(lonDim);
		this.catProp_extentsWval1.setHorizontalAlignment(SwingConstants.RIGHT);
		this.catProp_extentsEval1 = new JLabel(NO_VALUE);
		this.catProp_extentsEval1.setFont(boldFont);
		this.catProp_extentsEval1.setPreferredSize(lonDim);
		this.catProp_extentsEval1.setHorizontalAlignment(SwingConstants.RIGHT);
		this.catProp_extentsSval1 = new JLabel(NO_VALUE);
		this.catProp_extentsSval1.setFont(boldFont);
		this.catProp_extentsSval1.setPreferredSize(latDim);
		this.catProp_extentsSval1.setHorizontalAlignment(SwingConstants.RIGHT);
		this.catProp_minDepthVal1 = new JLabel(NO_VALUE);
		this.catProp_minDepthVal1.setFont(boldFont);
		this.catProp_minDepthVal1.setPreferredSize(valDim);
		this.catProp_maxDepthVal1 = new JLabel(NO_VALUE);
		this.catProp_maxDepthVal1.setFont(boldFont);
		this.catProp_maxDepthVal1.setPreferredSize(valDim);
		this.catProp_minMagVal1 = new JLabel(NO_VALUE);
		this.catProp_minMagVal1.setFont(boldFont);
		this.catProp_minMagVal1.setPreferredSize(valDim);
		this.catProp_maxMagVal1 = new JLabel(NO_VALUE);
		this.catProp_maxMagVal1.setFont(boldFont);
		this.catProp_maxMagVal1.setPreferredSize(valDim);
		this.catProp_numEventsVal1 = new JLabel(NO_VALUE);
		this.catProp_numEventsVal1.setFont(boldFont);
		this.catProp_numEventsVal1.setPreferredSize(valDim);
		this.catProp_dateFromVal1 = new JLabel(NO_VALUE);
		this.catProp_dateFromVal1.setFont(boldFont);
		this.catProp_dateFromVal1.setPreferredSize(valDim);
		this.catProp_dateToVal1 = new JLabel(NO_VALUE);
		this.catProp_dateToVal1.setFont(boldFont);
		this.catProp_dateToVal1.setPreferredSize(valDim);
		this.catProp_loop =    new JCheckBox();
		this.catProp_loop.setEnabled(false);
		this.catProp_loop.setOpaque(false);
		this.catProp_duration = new JTextField(10);
		this.catProp_duration.setEnabled(false);
		this.catProp_duration.setOpaque(false);
		EarthquakeCatalogPluginGUI.progbar = new JProgressBar(JProgressBar.HORIZONTAL);
		EarthquakeCatalogPluginGUI.progbar.setMinimum(0);
		EarthquakeCatalogPluginGUI.progbar.setPreferredSize(valDim);
		EarthquakeCatalogPluginGUI.progLabel = new JLabel("Earthquake Information");
		EarthquakeCatalogPluginGUI.progLabel.setFont(boldFont);
		EarthquakeCatalogPluginGUI.progLabel.setPreferredSize(new Dimension(120,16));

		this.catProp_playButton = new PlayButton(this, "Play");
		this.catProp_pauseButton = new PauseButton(this, "Pause");
		this.catProp_stopButton = new StopButton(this, "Stop");
		this.catProp_endButton = new EndButton(this, "End");

		boolean borders = false;
		if (borders) {
			this.catProp_extentsNval1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_extentsWval1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_extentsEval1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_extentsSval1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_minDepthVal1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_maxDepthVal1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_minMagVal1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_maxMagVal1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_numEventsVal1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_dateFromVal1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_dateToVal1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_loop.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			this.catProp_duration.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			EarthquakeCatalogPluginGUI.progbar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			EarthquakeCatalogPluginGUI.progLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		}

		// set constant labels
		JLabel catProp_extentsN = new JLabel("(\u00b0N)");
		catProp_extentsN.setForeground(Color.GRAY);
		JLabel catProp_extentsW = new JLabel("(\u00b0W)");
		catProp_extentsW.setForeground(Color.GRAY);
		JLabel catProp_extentsE = new JLabel("(\u00b0E)");
		catProp_extentsE.setForeground(Color.GRAY);
		JLabel catProp_extentsS = new JLabel("(\u00b0S)");
		catProp_extentsS.setForeground(Color.GRAY);
		JLabel catProp_extents_to = new JLabel("to");
		//JLabel catProp_minDepth = new JLabel("Depth (km) Min:");
		//JLabel catProp_maxDepth = new JLabel("Max:");
		JLabel catProp_minMag = new JLabel("Magnitude Min:");
		JLabel catProp_maxMag = new JLabel("Max:");
		JLabel catProp_dateFrom = new JLabel("From:");
		JLabel catProp_dateTo = new JLabel("To:");
		JLabel catProp_numEvents = new JLabel("Total EQ's:");
		JLabel catProp_loopLabel = new JLabel("Loop Animation:");
		JLabel catProp_duration = new JLabel("Duration (secs):");

		// animation buttons and labels
		this.catProp_animation = new JLabel("Animation Type:");

		this.catProp_relative = new JRadioButton("True Time");
		this.catProp_relative.setOpaque(false);

		this.catProp_static = new JRadioButton("Equal Event Time");
		this.catProp_static.setOpaque(false);

		ButtonGroup catProp_animButGrp = new ButtonGroup();
		catProp_animButGrp.add(this.catProp_relative);
		catProp_animButGrp.add(this.catProp_static);

		// assemble lat lon panel which spans 3 columns in extents panel
		JPanel catProp_latLonPanel = new JPanel(new GridBagLayout());
		catProp_latLonPanel.setOpaque(false);
		catProp_latLonPanel.add(this.catProp_extentsNval1,  new GridBagConstraints( 2, 0, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
		catProp_latLonPanel.add(     catProp_extentsN,     new GridBagConstraints( 3, 0, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));
		catProp_latLonPanel.add(this.catProp_extentsWval1,  new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
		catProp_latLonPanel.add(     catProp_extentsW,     new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));
		catProp_latLonPanel.add(     catProp_extents_to,   new GridBagConstraints( 2, 1, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
		catProp_latLonPanel.add(this.catProp_extentsEval1,  new GridBagConstraints( 4, 1, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
		catProp_latLonPanel.add(     catProp_extentsE,     new GridBagConstraints( 5, 1, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));
		catProp_latLonPanel.add(this.catProp_extentsSval1,  new GridBagConstraints( 2, 2, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
		catProp_latLonPanel.add(     catProp_extentsS,     new GridBagConstraints( 3, 2, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));

		// assemble catalog properties panel
		//this.propsAnimationPanel.add(     catProp_latLonPanel,  new GridBagConstraints( 0, 0, 2, 1, 0.0, 0.0, a_c, f, new Insets(0,0,0,0), 0, 0 ));

		//this.propsAnimationPanel.add(     catProp_minDepth,     new GridBagConstraints( 0, 1, 1, 1, 0.5, 0.0, a_r, f, new Insets(10,0,0,0), 0, 0 ));
		//this.propsAnimationPanel.add(this.catProp_minDepthVal1,  new GridBagConstraints( 1, 1, 1, 1, 0.5, 0.0, a_l, f, new Insets(10,10,0,0), 0, 0 ));
		//this.propsAnimationPanel.add(     catProp_maxDepth,     new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, a_r, f, new Insets(0,0,0,0), 0, 0 ));
		//this.propsAnimationPanel.add(this.catProp_maxDepthVal1,  new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0, a_l, f, new Insets(0,10,0,0), 0, 0 ));


		this.propsAnimationPanel.add(     catProp_minMag,       new GridBagConstraints( 0, 3, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));
		this.propsAnimationPanel.add(this.catProp_minMagVal1,    new GridBagConstraints( 1, 3, 1, 1, 0.0, 0.0, a_l, f, new Insets(8,10,0,0), 0, 0 ));
		this.propsAnimationPanel.add(     catProp_maxMag,       new GridBagConstraints( 0, 4, 1, 1, 0.0, 0.0, a_r, f, new Insets(0,0,0,0), 0, 0 ));
		this.propsAnimationPanel.add(this.catProp_maxMagVal1,    new GridBagConstraints( 1, 4, 1, 1, 0.0, 0.0, a_l, f, new Insets(0,10,0,0), 0, 0 ));

		this.propsAnimationPanel.add(     catProp_dateFrom,     new GridBagConstraints( 0, 5, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));
		this.propsAnimationPanel.add(this.catProp_dateFromVal1,  new GridBagConstraints( 1, 5, 1, 1, 0.0, 0.0, a_l, f, new Insets(8,10,0,0), 0, 0 ));
		this.propsAnimationPanel.add(     catProp_dateTo,       new GridBagConstraints( 0, 6, 1, 1, 0.0, 0.0, a_r, f, new Insets(0,0,0,0), 0, 0 ));
		this.propsAnimationPanel.add(this.catProp_dateToVal1,    new GridBagConstraints( 1, 6, 1, 1, 0.0, 0.0, a_l, f, new Insets(0,10,0,0), 0, 0 ));

		this.propsAnimationPanel.add(     catProp_numEvents,    new GridBagConstraints( 0, 7, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));
		this.propsAnimationPanel.add(this.catProp_numEventsVal1, new GridBagConstraints( 1, 7, 1, 1, 0.0, 0.0, a_l, f, new Insets(8,10,0,0), 0, 0 ));

		this.propsAnimationPanel.add(     catProp_loopLabel,       new GridBagConstraints( 0, 8, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));
		//this.propsAnimationPanel.add(this.catProp_loop,    new GridBagConstraints( 1, 8, 1, 1, 0.0, 0.0, a_l, f, new Insets(8,10,0,0), 0, 0 ));

		JPanel loopAndDispDatePanel = new JPanel();
		loopAndDispDatePanel.setLayout(new BoxLayout(loopAndDispDatePanel, BoxLayout.X_AXIS));
		loopAndDispDatePanel.add(this.catProp_loop);
		loopAndDispDatePanel.add(Box.createHorizontalStrut(40));
		loopAndDispDatePanel.add(new JLabel("Display Date and Time:"));
		loopAndDispDatePanel.add(Box.createHorizontalStrut(10));
		loopAndDispDatePanel.add(this.animationDisplayTimeCheckbox);
		animationDisplayTimeCheckbox.setSelected(true);

		this.propsAnimationPanel.add(loopAndDispDatePanel, new GridBagConstraints( 1, 8, 1, 1, 0.0, 0.0, a_l, f, new Insets(8,10,0,0), 0, 0 ));

		this.propsAnimationPanel.add(this.catProp_animation,    new GridBagConstraints( 0, 9, 1, 1, 0.3, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));
		this.propsAnimationPanel.add(this.catProp_relative,   new GridBagConstraints( 1, 9, 1, 1, 0.7, 0.0, a_l, f, new Insets(8,10,0,0), 0, 0 ));
		this.propsAnimationPanel.add(this.catProp_static,  new GridBagConstraints( 1, 10, 1, 1, 0.0, 0.0, a_l, f, new Insets(0,10,0,0), 0, 0 ));

		this.propsAnimationPanel.add(     catProp_duration,       new GridBagConstraints( 0, 11, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));
		this.propsAnimationPanel.add(this.catProp_duration,       new GridBagConstraints( 1, 11, 1, 1, 0.0, 0.0, a_l, f, new Insets(8,10,0,0), 0, 0 ));

		controlPanel = new JPanel(new GridBagLayout());
		controlPanel.setOpaque(false);
		controlPanel.add(this.catProp_playButton,  new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
		controlPanel.add(this.catProp_stopButton,     new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));
		controlPanel.add(this.catProp_endButton,     new GridBagConstraints( 2, 1, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));

		this.propsAnimationPanel.add(controlPanel,  new GridBagConstraints( 0, 13, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));

		//this.propsAnimationPanel.add(animLabel,       new GridBagConstraints( 1, 13, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));
		this.propsAnimationPanel.add(EarthquakeCatalogPluginGUI.progbar,    new GridBagConstraints( 1, 13, 1, 1, 0.0, 0.0, a_l, f, new Insets(8,10,0,0), 0, 0 ));
		this.propsAnimationPanel.add(EarthquakeCatalogPluginGUI.progLabel,       new GridBagConstraints( 0, 14, 120, 1, 0.0, 0.0, a_c, f, new Insets(8,20,8,0), 200, 0 ));
		this.catProp_playButton.setEnabled(true);
		return this.propsAnimationPanel;
	}


	/**
	 * Creates Display Panel
	 * @return JPanel
	 */
	@SuppressWarnings("unchecked")
	protected JPanel getPropsDisplayPanel() {

		// set up panel
		this.propsDisplayPanel = new JPanel(new GridBagLayout());
		this.propsDisplayPanel.setName("Display");
		this.propsDisplayPanel.setOpaque(false);

		// GEOMETRY
		this.dispProp_geometry = new JLabel("Geometry:");

		this.dispProp_geomPoint = new JRadioButton("Point");
		this.dispProp_geomPoint.addActionListener(this);
		this.dispProp_geomPoint.setOpaque(false);

		this.dispProp_geomSphere = new JRadioButton("Sphere");
		this.dispProp_geomSphere.addActionListener(this);
		this.dispProp_geomSphere.setSelected(true);
		this.dispProp_geomSphere.setOpaque(false);

		/*this.dispProp_geomCow = new JRadioButton("Cow");
			this.dispProp_geomCow.addActionListener(this);
			this.dispProp_geomCow.setOpaque(false);*/

		ButtonGroup dispProp_geomButGrp = new ButtonGroup();
		dispProp_geomButGrp.add(this.dispProp_geomPoint);
		dispProp_geomButGrp.add(this.dispProp_geomSphere);
		//dispProp_geomButGrp.add(this.dispProp_geomCow);

		// MAGNITUDE SCALING
		this.dispProp_scaling = new JLabel("Magnitude scaling:");


		//Transparency
		this.dispProp_slider=new JSlider(1,10,5);
		this.dispProp_slider.setMajorTickSpacing(2);
		this.dispProp_slider.setMinorTickSpacing(1);
		//transparencySlider.setPaintLabels(true);
		this.dispProp_slider.setPaintTicks(true);
		this.dispProp_slider.addChangeListener(this);
		this.dispProp_slider.setSnapToTicks(true);


		//Transparency
		transparencySlider=new JSlider(0,100,100);
		transparencySlider.setMajorTickSpacing(20);
		transparencySlider.setMinorTickSpacing(10);
		transparencySlider.setPaintLabels(true);
		transparencySlider.setPaintTicks(true);
		transparencySlider.addChangeListener(this);
		transparencySlider.setSnapToTicks(true);
		transLabel=new JLabel("Earthquake Transparency: ");


		// FOCAL MECHANISM
		this.dispProp_focalmechanism = new JLabel("Use focal mechanism:");

		// NONE
		this.dispProp_focalNone = new JRadioButton("None", true);
		this.dispProp_focalNone.addActionListener(this);
		this.dispProp_focalNone.setOpaque(false);

		// BALL
		this.dispProp_focalBall = new JRadioButton("Ball:");
		this.dispProp_focalBall.addActionListener(this);
		this.dispProp_focalBall.setOpaque(false);

		Integer[] focalIcons = {
				new Integer(FocalMechIcons.BLUE_GREY),
				new Integer(FocalMechIcons.BLUE_YELLOW),
				new Integer(FocalMechIcons.GREEN_GREY),
				new Integer(FocalMechIcons.RED_YELLOW),
				new Integer(FocalMechIcons.RED_GREY),
				new Integer(FocalMechIcons.ORANGE_YELLOW)};

		this.dispProp_focalBallDropDownBox = new JComboBox(focalIcons);
		this.dispProp_focalBallDropDownBox.setOpaque(false);

		this.dispProp_focalBallDropDownBox.addActionListener(this);
		this.dispProp_focalBallDropDownBox.setRenderer(new FocalMechRenderer());
		//this.dispProp_focalBallDropDownBox.setPreferredSize(menuSize);

		// DISC
		this.dispProp_focalDisc = new JRadioButton("Disc:");
		this.dispProp_focalDisc.addActionListener(this);
		this.dispProp_focalDisc.setOpaque(false);

		this.dispProp_focalCompLabel = new JLabel("Compression");
		this.dispProp_focalCompColButton = new ColorWellButton(Color.RED, 16, 16);
		this.dispProp_focalCompColButton.addActionListener(this);

		this.dispProp_focalExtLabel = new JLabel("Extension");
		this.dispProp_focalExtColButton = new ColorWellButton(Color.YELLOW, 16, 16);
		this.dispProp_focalExtColButton.addActionListener(this);

		ButtonGroup dispProp_focalButtonGroup = new ButtonGroup();
		dispProp_focalButtonGroup.add(this.dispProp_focalNone);
		dispProp_focalButtonGroup.add(this.dispProp_focalBall);
		dispProp_focalButtonGroup.add(this.dispProp_focalDisc);

		// COLOR
		this.dispProp_color = new JLabel("Set Color or Gradient:");

		this.dispProp_colGradientButton = new ColorWellButton(Color.BLUE, Color.ORANGE, 74, 16);
		this.dispProp_colGradientButton.addActionListener(this);
		this.lowerGradientLabel = new JLabel("Smaller");
		this.higherGradientLabel = new JLabel("Bigger");
		this.higherGradientLabel.setVisible(false);
		this.lowerGradientLabel.setVisible(false);

		// discrete coloring
		this.dispProp_discreteeqcolor = new JLabel("Use Discrete Coloring");

		this.dispProp_discreteCheckBox = new JCheckBox();
		//this.dispProp_discreteCheckBox.setOpaque(true);
		this.dispProp_discreteCheckBox.addActionListener(this);
		dispProp_discreteCheckBox.setEnabled(true);
		dispProp_discreteCheckBox.setSelected(false);


		// RECENTEQCOLOR
		this.dispProp_recenteqcolor = new JLabel("Use recent EQ coloring:");

		this.dispProp_recentCheckBox = new JCheckBox();
		this.dispProp_recentCheckBox.setOpaque(false);
		this.dispProp_recentCheckBox.addActionListener(this);

		// GRADIENT
		this.dispProp_gradient = new JLabel("Apply gradient to:");

		this.dispProp_gradDepth = new JRadioButton("Depth");
		this.dispProp_gradDepth.addActionListener(this);
		this.dispProp_gradDepth.setOpaque(false);

		this.dispProp_gradMag = new JRadioButton("Magnitude");
		this.dispProp_gradMag.addActionListener(this);
		this.dispProp_gradMag.setSelected(true);
		this.dispProp_gradMag.setOpaque(false);

		ButtonGroup dispProp_gradButtonGroup = new ButtonGroup();
		dispProp_gradButtonGroup.add(this.dispProp_gradMag);
		dispProp_gradButtonGroup.add(this.dispProp_gradDepth);


		assemblePropsDispPanel(false);


		return this.propsDisplayPanel;
	}

	protected void assemblePropsDispPanel(boolean showCowOption) {
		// Assemble display properties panel
		this.propsDisplayPanel.setVisible(false);
		this.propsDisplayPanel.removeAll();
		this.propsDisplayPanel.add(this.dispProp_geometry,		new GridBagConstraints( 0, 0, 1, 1, 0.3, 0.0, a_r, f, new Insets( 0, 0,0,0), 0, 0 ));

		// had to hack this up to get it to display correctly on Mac OS X
		JPanel geometryPanel = new JPanel();
		geometryPanel.setLayout(new BoxLayout(geometryPanel, BoxLayout.X_AXIS));
		geometryPanel.add(this.dispProp_geomPoint);
		geometryPanel.add(Box.createHorizontalStrut(10));
		geometryPanel.add(this.dispProp_geomSphere);
		this.propsDisplayPanel.add(geometryPanel,		new GridBagConstraints( 1, 0, 1, 1, 0.7, 0.0, a_l, f, new Insets( 0,10,0,0), 0, 0 ));
	
		int offset = 0;
	
		this.propsDisplayPanel.add(this.dispProp_scaling,		new GridBagConstraints( 0, 2+offset, 1, 1, 0.0, 0.0, a_r, f, new Insets(10, 0,0,0), 0, 0 ));
		//this.propsDisplayPanel.add(this.dispProp_scaleMenu,		new GridBagConstraints( 1, 2+offset, 1, 1, 0.0, 0.0, a_l, f, new Insets(10,10,0,0), 0, 0 ));
		this.propsDisplayPanel.add(this.dispProp_slider,		new GridBagConstraints( 1, 2+offset, 1, 1, 0.0, 0.0, a_r, f, new Insets(10,20,0,10), 0, 0 ));

		if (Prefs.getOS() == Prefs.OSX) {
		} else if (Prefs.getOS() == Prefs.WINDOWS) {
		} else {
		}

		//Add new transparency slider here
		this.propsDisplayPanel.add(transLabel,new GridBagConstraints( 0, 4+offset, 1, 1, 0.0, 0.0, a_c, f, new Insets(0, 0,0,0), 0, 0 ));
		this.propsDisplayPanel.add(this.transparencySlider,		new GridBagConstraints( 1, 4+offset, 2, 1, 0.0, 0.0, a_c, f, new Insets(0, 0,0,0), 0, 0 ));
		offset++;

		this.propsDisplayPanel.add(this.dispProp_color,			new GridBagConstraints( 0, 8+offset, 1, 1, 0.0, 0.0, a_l, f, new Insets(10, 10,0,0), 0, 0 ));
		this.propsDisplayPanel.add(this.dispProp_colGradientButton,		new GridBagConstraints( 1, 8+offset, 1, 1, 0.0, 0.0, a_l, f, new Insets(10,0,0,0), 0, 0 ));
		this.propsDisplayPanel.add(this.lowerGradientLabel,       new GridBagConstraints(1, 9+offset, 1, 1, 0.0, 0.0, a_l, f, new Insets(0,0,0,0), 0, 0));
		this.propsDisplayPanel.add(this.higherGradientLabel,      new GridBagConstraints(1, 9+ offset, 1, 1, 0.0, 0.0, a_r, f, new Insets(0, 0, 0, 0), 103, 0));

		// had to hack this up to get it to display correctly on Mac OS X
		this.propsDisplayPanel.add(this.dispProp_gradient,		new GridBagConstraints( 0, 10+offset, 1, 1, 0.0, 0.0, a_r, f, new Insets(10, 0,0,0), 0, 0 ));// had to hack this up to get it to display correctly on Mac OS X
		JPanel gradientPanel = new JPanel();
		gradientPanel.setLayout(new BoxLayout(gradientPanel, BoxLayout.X_AXIS));
		gradientPanel.add(this.dispProp_gradMag);
		gradientPanel.add(Box.createHorizontalStrut(10));
		gradientPanel.add(this.dispProp_gradDepth);
		this.propsDisplayPanel.add(gradientPanel,		new GridBagConstraints( 1, 10+offset, 1, 1, 0.0, 0.0, a_l, f, new Insets(10,10,0,0), 0, 0 ));
		//        this.propsDisplayPanel.add(this.dispProp_gradMag,		new GridBagConstraints( 1, 9+offset, 1, 1, 0.0, 0.0, a_l, f, new Insets(10,10,0,0), 0, 0 ));
		//        this.propsDisplayPanel.add(this.dispProp_gradDepth,		new GridBagConstraints( 1, 9+offset, 1, 1, 0.0, 0.0, a_r, f, new Insets(10,0,0,0), 55, 0 ));

		this.propsDisplayPanel.add(this.dispProp_recenteqcolor,	new GridBagConstraints( 0, 11+offset, 1, 1, 0.0, 0.0, a_r, f, new Insets(10, 10,0,0), 0, 0 ));
		this.propsDisplayPanel.add(this.dispProp_recentCheckBox,new GridBagConstraints( 1, 11+offset, 1, 1, 0.0, 0.0, a_l, f, new Insets(10,10,0,0), 0, 0 ));

		this.propsDisplayPanel.add(this.dispProp_discreteeqcolor,	new GridBagConstraints( 1, 8+offset, 1, 1, 0.0, 0.0, a_r, f, new Insets(10, 0,0,0), 0, 0 ));
		this.propsDisplayPanel.add(this.dispProp_discreteCheckBox,new GridBagConstraints( 1, 8+offset, 1, 1, 0.0, 0.0, a_c, f, new Insets(10,0,0,0), 0, 0 ));

		//this.propsDisplayPanel.add(this.dispProp_apply,			new GridBagConstraints( 1, 11+offset, 1, 1, 0.0, 0.0, a_r, f, new Insets( 0, 0,4,4), 0, 0 ));

		this.propsDisplayPanel.repaint();
	}

	private JPanel getStatusPanel() {
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
		statusPanel.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
		statusPanel.add(status);
		statusPanel.add(Box.createHorizontalGlue());
		return statusPanel;
	}

	private void disableDisplayPanelComponents() {
		setGeometryEnabled(true);
		setMagScaleEnabled(true);
		setColorEnabled(true);
		setRecentEQColorEnabled(false);
		setDiscreteEQColorEnabled(false);
		setGradApplyEnabled(false);
		setFocalMechEnabled(false);

	}

	private void setGeometryEnabled(boolean enable) {
		this.dispProp_geometry.setEnabled(enable);
		this.dispProp_geomPoint.setEnabled(enable);
		this.dispProp_geomSphere.setEnabled(enable);
		
	}
	private void setMagScaleEnabled(boolean enable) {
		this.dispProp_scaling.setEnabled(enable);
		this.dispProp_slider.setEnabled(enable);
	}

	private void setColorEnabled(boolean enable) {
		this.dispProp_color.setEnabled(enable);
		this.dispProp_colGradientButton.setEnabled(enable);
		this.propsDisplayPanel.add(this.lowerGradientLabel,       new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0, a_l, f, new Insets(0,0,0,0), 0, 0));
		this.lowerGradientLabel.setEnabled(enable);
		this.higherGradientLabel.setEnabled(enable);

	}
	private void setRecentEQColorEnabled(boolean enable) {
		this.dispProp_recenteqcolor.setEnabled(enable);
		this.dispProp_recentCheckBox.setEnabled(enable);
	}
	private void setDiscreteEQColorEnabled(boolean enable) {
		this.dispProp_discreteeqcolor.setEnabled(enable);
		this.dispProp_discreteCheckBox.setEnabled(enable);
	}
	private void setGradApplyEnabled(boolean enable) {
		this.dispProp_gradient.setEnabled(enable);
		this.dispProp_gradDepth.setEnabled(enable);
		this.dispProp_gradMag.setEnabled(enable);
	}
	private void setFocalMechEnabled(boolean enable) {
		this.dispProp_focalmechanism.setEnabled(enable);
		this.dispProp_focalNone.setEnabled(enable);
		this.dispProp_focalBall.setEnabled(enable);
		this.dispProp_focalDisc.setEnabled(enable);
		setFocalBallGUIEnabled(enable);
		setFocalDiscGUIEnabled(enable);
	}
	private void setFocalDiscGUIEnabled(boolean enable) {
		this.dispProp_focalCompColButton.setEnabled(enable);
		this.dispProp_focalCompLabel.setEnabled(enable);
		this.dispProp_focalExtColButton.setEnabled(enable);
		this.dispProp_focalExtLabel.setEnabled(enable);
	}
	private void setFocalBallGUIEnabled(boolean enable) {
		this.dispProp_focalBallDropDownBox.setEnabled(enable);
	}

	private void setExtentsPanel(CatalogAccessor catalog) {
		if (catalog != null) {
			this.catProp_dateFromVal.setText(DATE_FORMAT.format(catalog.getMinDate()));
			this.catProp_dateToVal.setText(DATE_FORMAT.format(catalog.getMaxDate()));
			this.catProp_extentsNval.setText(DECIMAL_FORMAT.format(catalog.getMaxLatitude()));
			this.catProp_extentsWval.setText(DECIMAL_FORMAT.format(catalog.getMinLongitude()));
			this.catProp_extentsEval.setText(DECIMAL_FORMAT.format(catalog.getMaxLongitude()));
			this.catProp_extentsSval.setText(DECIMAL_FORMAT.format(catalog.getMinLatitude()));
			this.catProp_minDepthVal.setText(DECIMAL_FORMAT.format(catalog.getMinDepth()));
			this.catProp_maxDepthVal.setText(DECIMAL_FORMAT.format(catalog.getMaxDepth()));
			this.catProp_minMagVal.setText(DECIMAL_FORMAT.format(catalog.getMinMagnitude()));
			this.catProp_maxMagVal.setText(DECIMAL_FORMAT.format(catalog.getMaxMagnitude()));
			this.catProp_numEventsVal.setText(String.valueOf(catalog.getNumEvents()));
			this.catProp_sourceVal.setText(catalog.getCitation());

			minDate = catalog.getMinDate();
			maxDate  = catalog.getMaxDate();
			maxDateMill = maxDate.getTime();
			minDateMill = minDate.getTime();

			timeDifference = maxDate.getTime() - minDate.getTime();
		} else {
			this.catProp_dateFromVal.setText(NO_VALUE);
			this.catProp_dateToVal.setText(NO_VALUE);
			this.catProp_extentsNval.setText(NO_VALUE);
			this.catProp_extentsWval.setText(NO_VALUE);
			this.catProp_extentsEval.setText(NO_VALUE);
			this.catProp_extentsSval.setText(NO_VALUE);
			this.catProp_minDepthVal.setText(NO_VALUE);
			this.catProp_maxDepthVal.setText(NO_VALUE);
			this.catProp_minMagVal.setText(NO_VALUE);
			this.catProp_maxMagVal.setText(NO_VALUE);
			this.catProp_numEventsVal.setText(NO_VALUE);
			this.catProp_sourceVal.setText(NO_VALUE);
		}
	}

	private void setAnimationPanel(CatalogAccessor catalog) {
		if (catalog != null) {
			this.catProp_dateFromVal1.setText(DATE_FORMAT.format(catalog.getMinDate()));
			this.catProp_dateToVal1.setText(DATE_FORMAT.format(catalog.getMaxDate()));
			this.catProp_extentsNval1.setText(DECIMAL_FORMAT.format(catalog.getMaxLatitude()));
			this.catProp_extentsWval1.setText(DECIMAL_FORMAT.format(catalog.getMinLongitude()));
			this.catProp_extentsEval1.setText(DECIMAL_FORMAT.format(catalog.getMaxLongitude()));
			this.catProp_extentsSval1.setText(DECIMAL_FORMAT.format(catalog.getMinLatitude()));
			this.catProp_minDepthVal1.setText(DECIMAL_FORMAT.format(catalog.getMinDepth()));
			this.catProp_maxDepthVal1.setText(DECIMAL_FORMAT.format(catalog.getMaxDepth()));
			this.catProp_minMagVal1.setText(DECIMAL_FORMAT.format(catalog.getMinMagnitude()));
			this.catProp_maxMagVal1.setText(DECIMAL_FORMAT.format(catalog.getMaxMagnitude()));
			this.catProp_numEventsVal1.setText(String.valueOf(catalog.getNumEvents()));
			this.catProp_loop.setEnabled(true);
			this.catProp_duration.setEnabled(true);
			this.catProp_duration.setText("10");
			this.catProp_playButton.setEnabled(true);
			this.catProp_stopButton.setEnabled(false);
			this.catProp_endButton.setEnabled(false);
			this.catProp_relative.setSelected(true);
			this.catProp_relative.setEnabled(true);
			this.catProp_static.setEnabled(true);
		} else {
			this.catProp_dateFromVal1.setText(NO_VALUE);
			this.catProp_dateToVal1.setText(NO_VALUE);
			this.catProp_extentsNval1.setText(NO_VALUE);
			this.catProp_extentsWval1.setText(NO_VALUE);
			this.catProp_extentsEval1.setText(NO_VALUE);
			this.catProp_extentsSval1.setText(NO_VALUE);
			this.catProp_minDepthVal1.setText(NO_VALUE);
			this.catProp_maxDepthVal1.setText(NO_VALUE);
			this.catProp_minMagVal1.setText(NO_VALUE);
			this.catProp_maxMagVal1.setText(NO_VALUE);
			this.catProp_numEventsVal1.setText(NO_VALUE);
			this.catProp_loop.setEnabled(false);
			this.catProp_duration.setEnabled(false);
			this.catProp_relative.setEnabled(false);
			this.catProp_static.setEnabled(false);
			this.catProp_playButton.setEnabled(false);
		}
	}

	private void clearAttributePanels() {
		setExtentsPanel(null);
		setAnimationPanel(null);
		this.propsTabbedPane.setEnabledAt(1, false);
		disableDisplayPanelComponents();
	}

	// Main method used to convert catalog object into collection of individual earthquake
	// objects.  Once the conversion is completed, the ArrayList of earthquake objects
	// is set to a static variable in Geo3DInfo for sharing purposes.  The collection can
	// then be retrieved by simply calling Geo3dInfo.getEarthquakes()
	// This method is called whenever a catalog is selected from the EQCatalogGUI Library
	// Panel

	//get earthquakes also filters earthquakes through user defined polygons - Ryan Berti 2008

	public void getEarthquakes(CatalogAccessor cat){
		this.catalogAcc = cat;
		Earthquake eqs;
		//FocalEQ focEqs;
		ArrayList<Earthquake> eqList = new ArrayList<Earthquake>();
		cat.readDataFile();
		new vtkCellArray();
		for (int i=0; i<cat.getNumEvents();i++){
			//				if(ROIFilter && !isContained(cat.getEq_latitude(i),cat.getEq_longitude(i))){//checks for ROIfilter, then checks if the eq is contained in any stored ROI polygons
			//					continue;//skips eq if ROIfilter is on and epicenter is outside of polygons
			//				}
			/*else{
					if (cat.getDataScope() == EQCatalog.DATA_SCOPE_FOCAL_PROB) {
						focEqs = new FocalEQ(cat.getEq_depth(i),cat.getEq_id(i),cat.getEq_latitude(i),
								cat.getEq_longitude(i),cat.getEq_magnitude(i),cat.getEq_time(i),
								cat.getEq_strike(i), cat.getEq_dip(i), cat.getEq_rake(i),
								cat.getEQ_nodal(i), cat.getEQ_probability(i));

						compColor= dispProp_focalCompColButton.getColor1	();
						extColor = dispProp_focalExtColButton.getColor1();

						Color3f compColor3f = new Color3f(compColor);
						Color3f extColor3f = new Color3f(extColor);

						focEqs.setColor1(compColor3f);
						focEqs.setColor2(extColor3f);
						eqList.add(focEqs);
					} else if (cat.getDataScope() == EQCatalog.DATA_SCOPE_FOCAL || cat.getDataScope() == EQCatalog.DATA_SCOPE_UNCERT_FOCAL) {
						eqs = new FocalEQ(cat.getEq_depth(i),cat.getEq_id(i),cat.getEq_latitude(i),
								cat.getEq_longitude(i),cat.getEq_magnitude(i),cat.getEq_time(i),
								cat.getEq_strike(i), cat.getEq_dip(i), cat.getEq_rake(i));
						focEqs = new FocalEQ(cat.getEq_depth(i),cat.getEq_id(i),cat.getEq_latitude(i),
								cat.getEq_longitude(i),cat.getEq_magnitude(i),cat.getEq_time(i),
								cat.getEq_strike(i), cat.getEq_dip(i), cat.getEq_rake(i));

						compColor = dispProp_focalCompColButton.getColor1();
						extColor = dispProp_focalExtColButton.getColor1();

						Color3f compColor3f = new Color3f(compColor);
						Color3f extColor3f = new Color3f(extColor);

						focEqs.setColor1(compColor3f);
						focEqs.setColor2(extColor3f);
						eqList.add(focEqs);
					} else {*/
			//plot the earthquakes as spheres with radius as magnitude	
			Date time;
			if(cat.getEq_time(i)!=null)
			{time = cat.getEq_time(i);}
			else
				time=new Date();
			eqs = new Earthquake(cat.getEq_depth(i),cat.getEq_magnitude(i),cat.getEq_latitude(i),
					cat.getEq_longitude(i),time);
			//						if (cat.getDataScope() == EQCatalog.DATA_SCOPE_UNCERT || cat.getDataScope() == EQCatalog.DATA_SCOPE_UNCERT_FOCAL) {
			//							eqs.setEq_xy_error(cat.getEq_xy_error(i));
			//							eqs.setEq_z_error(cat.getEq_z_error(i));
			//						}
			eqList.add(eqs);

		}
		this.setEarthquakes(eqList);

	}

	private static void setEarthquakes(ArrayList<Earthquake> eqList) {
		// TODO Auto-generated method stub
		earthquakes = eqList;
	}

	private void setAttributePanels() {
		// get the current selection
		CatalogAccessor catalog = null;
		EQCatalog cat = this.catalogTable.getSelectedValue();
		catalog = this.catalogTable.getSelectedValue();
	
		// if there is no catalog, clear panels and abort
		if (catalog == null) {
			clearAttributePanels();
			return;
		}

		setExtentsPanel(catalog);
		getEarthquakes(catalog);
		setAnimationPanel(catalog);

		if (cat != null) {
			setAnimationColor(cat.getColor1(),cat.getColor2());
			setAnimationScaling(cat.getScaling());
			setAnimationStyle(cat.getFocalDisplay());
		}

		if (a != null){
			this.propsAnimationPanel.remove(controlPanel);
			this.propsAnimationPanel.repaint();
			controlPanel = new JPanel(new GridBagLayout());
			controlPanel.setOpaque(false);
			this.catProp_playButton.setEnabled(true);
			this.catProp_endButton.setEnabled(false);
			this.catProp_stopButton.setEnabled(false);
			controlPanel.add(this.catProp_playButton,  new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, a_r, f, new Insets(4,0,0,0), 0, 0 ));
			controlPanel.add(this.catProp_stopButton,     new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));
			controlPanel.add(this.catProp_endButton,     new GridBagConstraints( 2, 1, 1, 1, 0.0, 0.0, a_l, f, new Insets(4,3,0,0), 0, 0 ));
			this.propsAnimationPanel.add(controlPanel,  new GridBagConstraints( 0, 13, 1, 1, 0.0, 0.0, a_r, f, new Insets(8,0,0,0), 0, 0 ));
			this.catProp_relative.setEnabled(true);
			this.catProp_static.setEnabled(true);
			this.catProp_duration.setEnabled(true);
		}

		if (catalog instanceof EQCatalog) {
			if (catalog.isInMemory()) {
				setDisplayPanel((EQCatalog)catalog);
				this.propsTabbedPane.setEnabledAt(1, true);
				setAnimationPanel((EQCatalog)catalog);
			} else {
				disableDisplayPanelComponents();
				this.propsTabbedPane.setEnabledAt(1, false);
				this.propsTabbedPane.setSelectedIndex(0);
			}
		}
	}

	private void runObjectInfoDialog(CatalogAccessor catalog) {
		ObjectInfoDialog oid = getSourceInfoDialog();
		oid.showInfo(catalog, "Edit Catalog Information");
		if (oid.windowWasCancelled()) return;
		setExtentsPanel(catalog);
		catalog.writeAttributeFile();
	}


	//****************************************
	//     EVENT HANDLERS
	//****************************************

	/**
	 * Required event-handler method.
	 *
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {
	}

	/**
	 * Required event-handler method that processes user interaction.
	 *
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {

		Object src = e.getSource();
		if (src == this.catalogTable.getSelectionModel()) {
			if (e.getValueIsAdjusting()) return;
			processTableSelectionChange();
		}
	}

	/**
	 * Required event-handler method that processes user interaction.
	 *
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if(src == dispProp_slider){
			//earthquake magnitude scaling
			//setPropertyChange(true, EQCatalog.CHANGE_SIZE_SLIDER);
			int scale = dispProp_slider.getValue();
			EQCatalog cat = this.catalogTable.getSelectedValue();
			cat.setScaling(scale);
			System.out.println(scale);
			setMagnitudeScale(cat,scale);
		}
		if(src == transparencySlider)
		{
			EQCatalog cat = this.catalogTable.getSelectedValue();
			int transparencyVal = (int) transparencySlider.getValue();
			cat.setTransparency(transparencyVal);
			//System.out.println(transparency);
			setTransparency(cat,transparencyVal);
		}
		if (src == depthSlider) {
			depthSliderValue = depthSlider.getValue();
			updateDisplay();
		}

	}
	
	public void setTransparency(EQCatalog cat, int transparencyVal) {
		// TODO Auto-generated method stub
		double transparency = (transparencyVal*255/100);
		
		ArrayList<Earthquake> eqList = cat.getSelectedEqList();
		for(int i =0;i<eqList.size();i++)
		{
			Earthquake eq = eqList.get(i);
			animateEarthquakeOpacity(i, eq, cat, (int)transparency);
		}
		Info.getMainGUI().updateRenderWindow();
	}
	public void setMagnitudeScale(EQCatalog cat,int scale) {
		// TODO Auto-generated method stub
		double[] scaleMenuItems = {0.05,0.1,0.2,0.5,1.0,2.0,3.0,4.0,5.0,6.0};

		double scaleSet = 0.05;
		switch(scale)
		{
		case 1:scaleSet =scaleMenuItems[0];break;
		case 2:scaleSet =scaleMenuItems[1];break;
		case 3:scaleSet =scaleMenuItems[2];break;
		case 4:scaleSet =scaleMenuItems[3];break;
		case 5:scaleSet =scaleMenuItems[4];break;
		case 6:scaleSet =scaleMenuItems[5];break;
		case 7:scaleSet =scaleMenuItems[6];break;
		case 8:scaleSet =scaleMenuItems[7];break;
		case 9:scaleSet =scaleMenuItems[8];break;
		case 10:scaleSet =scaleMenuItems[9];break;
		}
		
		
		ArrayList<Earthquake> eqList = cat.getSelectedEqList();
		vtkActor actorPointsOld = (vtkActor) cat.getActors().get(0);
		vtkActor actorSpheresOld = (vtkActor) cat.getActors().get(1);

		vtkDoubleArray radi = new vtkDoubleArray();
		radi.SetName("radi");

		vtkMapper mapperPoints = actorPointsOld.GetMapper();
		vtkMapper mapperSphere = actorSpheresOld.GetMapper();

		vtkVertexGlyphFilter vertexGlyphFilter =new vtkVertexGlyphFilter();
		vertexGlyphFilter=(vtkVertexGlyphFilter) actorPointsOld.GetMapper().GetInputAlgorithm();//.GetOutputDataObject(0);

		vtkGlyph3D glyphPoints = new vtkGlyph3D();
		glyphPoints = (vtkGlyph3D) actorSpheresOld.GetMapper().GetInputAlgorithm();

		vtkPolyData inputData = new vtkPolyData();
		inputData = (vtkPolyData) vertexGlyphFilter.GetInput();
		//int lastIndex = eqList.indexOf(eq);

		new vtkPoints();
		radi = (vtkDoubleArray) inputData.GetPointData().GetArray("radi");
		//double stepSize = (cat.getMaxMagnitude()-cat.getMinMagnitude())/cat.gradientDivisions;
		for(int i =0;i<eqList.size();i++)
		{
			Earthquake eq = eqList.get(i);
			radi.SetTuple1(i,eq.getEq_magnitude()*scaleSet);
		}
		radi.Modified();
		inputData.GetPointData().AddArray(radi);
		vertexGlyphFilter.SetInputData(inputData);
		vertexGlyphFilter.Update();
		mapperPoints.SetInputConnection(vertexGlyphFilter.GetOutputPort());


		glyphPoints.SetInputData(inputData);
		mapperSphere.SetInputConnection(glyphPoints.GetOutputPort());

		actorPointsOld.SetMapper(mapperPoints);
		actorSpheresOld.SetMapper(mapperSphere);
		cat.getActors().set(0,actorPointsOld);
		cat.getActors().set(1,actorSpheresOld);
		updateActorsAndRender(cat);

	}
	private void updateActorsAndRender(EQCatalog cat) {
		// TODO make this not disgusting please. it's not a branch group anymore. and it should at leass
		for (vtkActor actor : cat.getActors())
			pluginActors.addActor(actor);
		MainGUI.updateRenderWindow();
	}

	public void setAnimationColor(Color c1, Color c2){
	}

	private void setAnimationScaling(int size){
	}

	private void setAnimationStyle(int type){
		animationStyle=type;
	}

	private int getAnimationStyle(){
		return animationStyle;
	}

	/**
	 * Required event-handler method that processes user interaction.
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	private String getReferenceText() {
		URL fileURL = EarthquakeCatalogPlugin.class.getResource("Documentation/References/EQreference.txt");
		StringBuffer readme = new StringBuffer();
		try
		{
			BufferedReader inStream = new BufferedReader(new InputStreamReader(fileURL.openStream()));
			String readLine = inStream.readLine();
			while(readLine != null)
			{
				readme.append(readLine + "\n" );
				readLine = inStream.readLine();
			}
			inStream.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return readme.toString();
	}

	public ComcatResourcesDialog getComcatResourceDialog()
	{
		return this.netSourceDialog;
	}

	//if catalogs opacity has to be changed
	public static List<vtkActor> animateEarthquakeOpacity(int lastIndex,Earthquake eq,EQCatalog cat,int opacity)
	{
		//also called on every animation tick
		ArrayList<Earthquake> eqList = cat.getSelectedEqList();
		vtkActor actorPointsOld = (vtkActor) cat.getActors().get(0);
		vtkActor actorSpheresOld = (vtkActor) cat.getActors().get(1);
		vtkActor actorPointsNew = new vtkActor(); 
		vtkActor actorSpheresNew = new vtkActor();

		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
		colors.SetName("colors");
		colors.SetNumberOfComponents(4);
		colors.SetNumberOfTuples(eqList.size());

		vtkMapper mapperPoints = actorPointsOld.GetMapper();
		vtkMapper mapperSphere = actorSpheresOld.GetMapper();

		vtkVertexGlyphFilter vertexGlyphFilter =new vtkVertexGlyphFilter();
		vertexGlyphFilter=(vtkVertexGlyphFilter) actorPointsOld.GetMapper().GetInputAlgorithm();//.GetOutputDataObject(0);

		vtkGlyph3D glyphPoints = new vtkGlyph3D();
		glyphPoints = (vtkGlyph3D) actorSpheresOld.GetMapper().GetInputAlgorithm();

		vtkPolyData inputData = new vtkPolyData();
		inputData = (vtkPolyData) vertexGlyphFilter.GetInput();
		//int lastIndex = eqList.indexOf(eq);

		colors = (vtkUnsignedCharArray) inputData.GetPointData().GetArray("colors");

		double[] val = colors.GetTuple4(lastIndex);
		colors.SetTuple4(lastIndex, val[0],val[1],val[2],opacity);

		colors.Modified();

		inputData.GetPointData().AddArray(colors);
		vertexGlyphFilter.SetInputData(inputData);
		vertexGlyphFilter.Update();
		mapperPoints.SetInputConnection(vertexGlyphFilter.GetOutputPort());


		glyphPoints.SetInputData(inputData);
		mapperSphere.SetInputConnection(glyphPoints.GetOutputPort());

		actorPointsNew.SetMapper(mapperPoints);
		actorSpheresNew.SetMapper(mapperSphere);
		return cat.getActors();
	}
	public void actionPerformed(ActionEvent e) {

		Object src = e.getSource();
		new vtkAnimationScene();
		if (src == newInternetSourceButton){
			if (this.netSourceDialog == null) {
				this.netSourceDialog = new ComcatResourcesDialog(this);
			}
			ComcatResourcesDialog dialog = this.netSourceDialog;

			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);

		}
		if (src == this.helpButton){
			JOptionPane.showMessageDialog(this,
					"Click on any catalog to highlight it. \n" +
							"Once it is highlighted, click the checkbox \n" +
							"to display your catalog. \n \n" +
							"To import an earthquake catalog from file on \n" +
							"your computer, acquire one from an online \n" +
							"network, or filter one out of an existing catalog,\n" +
							"click the appropriate button on the left.\n \n",
							"Need Help?",
							JOptionPane.PLAIN_MESSAGE);


		}
		if (src == catProp_playButton)
		{
			//ascending order as per the time
//			EQCatalog cat = this.catalogTable.getSelectedValue();
//			//ArrayList<Earthquake> earthquakeList = 	cat.getSelectedEqList();
//			if(this.parentPlugin instanceof StatefulPlugin)
//			{
//				PluginState state = ((StatefulPlugin)this.parentPlugin).getState().deepCopy();
//				state.load();
//			}
			//Info.getMainGUI().GetScriptingPlugin().addEarthquakeListForAniamtion(cat,true);
		}

		//display panel buttons
		if (src == this.dispProp_geomPoint) {
			EQCatalog cat = this.catalogTable.getSelectedValue();
			cat.setGeometry(0);
			setCatalogVisible(cat,cat.getGeometry(),true);
		}
		else if (src == this.dispProp_geomSphere) {

			EQCatalog cat = this.catalogTable.getSelectedValue();
			cat.setGeometry(1);
			setCatalogVisible(cat,cat.getGeometry(),true);
		}
		else if(src==this.dispProp_colGradientButton)
		{
			if (this.colorChooser == null) {
				this.colorChooser = new GradientColorChooser(this);
			}
			Color[] newColor = this.colorChooser.getColors(
					this.dispProp_colGradientButton.getColor1(),
					this.dispProp_colGradientButton.getColor2());
			if (newColor != null) {
				this.dispProp_colGradientButton.setColor(newColor[0], newColor[1]);
				if (newColor[0].equals(newColor[1])) {
					setGradApplyEnabled(false);
					this.higherGradientLabel.setVisible(false);
					this.lowerGradientLabel.setVisible(false);
				} else {
					setGradApplyEnabled(true);
					this.higherGradientLabel.setVisible(true);
					this.lowerGradientLabel.setVisible(true);
				}
			}
			EQCatalog cat = this.catalogTable.getSelectedValue();
			setColGradient(cat,newColor);
			
		}

		/**
		 * Custom renderer class draws focal mechanism icons with color labels.
		 *
		 * Created on Feb 8, 2005
		 *
		 */
		class FocalMechRenderer extends BasicComboBoxRenderer {

			private static final long serialVersionUID = 1L;

			FocalMechRenderer() {
				super();
				setHorizontalAlignment(SwingConstants.CENTER);
			}

			/**
			 * Required cell renderer method.
			 *
			 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
			 */
			public Component getListCellRendererComponent(
					JList list, Object pattern,
					int index, boolean isSelected,
					boolean cellHasFocus) {

				int id = ((Integer)pattern).intValue();

				setText(null);
				// workaround: setDisabledIcon() has no effect on icon showing when combobox is closed
				// (alternatively we could override paintIcon())
				/*if (!EarthquakeCatalogPluginGUI.this.isFocalMenuEnabled()) {
					setIcon(FocalMechIcons.getLargeIconDisabled(id));
				} else {
				 */	setIcon(FocalMechIcons.getLargeIcon(id));
				 //}

				 if (isSelected) {
					 setBackground(list.getSelectionBackground());
				 } else {
					 setBackground(list.getBackground());
				 }

				 return this;
			}
		}}

	
	public void setCatalogVisible(EQCatalog cat, int geometry,boolean visible) {
		// TODO Auto-generated method stub
		vtkActor actorPoints = (vtkActor) cat.getActors().get(0);
		vtkActor actorSpheres = (vtkActor) cat.getActors().get(1);
		System.out.println(geometry);
		System.out.println(visible);
		if(geometry==0)
		{
			if(visible)
			{
				actorPoints.VisibilityOn();
				actorSpheres.VisibilityOff();
			}
			else
			{
				actorPoints.VisibilityOff();
				actorSpheres.VisibilityOff();
			}
		}
		else if(geometry==1 )
		{
			if(visible)
			{
				actorPoints.VisibilityOff();
				actorSpheres.VisibilityOn();
			}
			else
			{
				actorPoints.VisibilityOff();
				actorSpheres.VisibilityOff();
			}
		}
	
		updateActorsAndRender(cat);
	}
	public void setColGradient(EQCatalog cat,Color[] newColor) {
		// TODO Auto-generated method stub
		if(newColor!=null){
		cat.setGradColor1(newColor[0]);
		cat.setGradColor2(newColor[1]);
		cat.initGradientAppearance();
		ArrayList<Earthquake> eqList = cat.getSelectedEqList();
		vtkActor actorPointsOld = (vtkActor) cat.getActors().get(0);
		vtkActor actorSpheresOld = (vtkActor) cat.getActors().get(1);

		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
		colors.SetName("colors");
		colors.SetNumberOfComponents(4);
		colors.SetNumberOfTuples(eqList.size());

		vtkMapper mapperPoints = actorPointsOld.GetMapper();
		vtkMapper mapperSphere = actorSpheresOld.GetMapper();

		vtkVertexGlyphFilter vertexGlyphFilter =new vtkVertexGlyphFilter();
		vertexGlyphFilter=(vtkVertexGlyphFilter) actorPointsOld.GetMapper().GetInputAlgorithm();//.GetOutputDataObject(0);

		vtkGlyph3D glyphPoints = new vtkGlyph3D();
		glyphPoints = (vtkGlyph3D) actorSpheresOld.GetMapper().GetInputAlgorithm();

		vtkPolyData inputData = new vtkPolyData();
		inputData = (vtkPolyData) vertexGlyphFilter.GetInput();
		//int lastIndex = eqList.indexOf(eq);

		colors = (vtkUnsignedCharArray) inputData.GetPointData().GetArray("colors");

		//double stepSize = (cat.getMaxMagnitude()-cat.getMinMagnitude())/cat.gradientDivisions;
		for(int i =0;i<eqList.size();i++)
		{
			Earthquake eq = eqList.get(i);
			// Color based on magnitude
			int ind= (int) ( Math.floor( Math.floor(eq.getEq_magnitude()))-cat.getMinMagnitude());
			if(ind<0)
				ind=0;

			colors.SetTuple4(i, cat.gradientColors[ind].getRed(),cat.gradientColors[ind].getGreen(),cat.gradientColors[ind].getBlue(),255);
		}
		colors.Modified();

		inputData.GetPointData().AddArray(colors);
		vertexGlyphFilter.SetInputData(inputData);
		vertexGlyphFilter.Update();
		mapperPoints.SetInputConnection(vertexGlyphFilter.GetOutputPort());


		glyphPoints.SetInputData(inputData);
		mapperSphere.SetInputConnection(glyphPoints.GetOutputPort());

		actorPointsOld.SetMapper(mapperPoints);
		actorSpheresOld.SetMapper(mapperSphere);
		cat.getActors().set(0,actorPointsOld);
		cat.getActors().set(1,actorSpheresOld);
		updateActorsAndRender(cat);
		}
	}
	public void mouseClicked(MouseEvent e) {
	}
	public void mousePressed(MouseEvent arg0) {
	}
	public void mouseReleased(MouseEvent arg0){
	}
	public void mouseEntered(MouseEvent arg0) {
	}
	public void mouseExited(MouseEvent arg0)  {
	}

	public CatalogTable getCatalogTable() {
		return catalogTable;
	}

	public JRadioButton getDispProp_focalBall() {
		return dispProp_focalBall;
	}

	public void setDispProp_focalBall(JRadioButton dispProp_focalBall) {
		this.dispProp_focalBall = dispProp_focalBall;
	}

	public JRadioButton getDispProp_focalDisc() {
		return dispProp_focalDisc;
	}

	public void setDispProp_focalDisc(JRadioButton dispProp_focalDisc) {
		this.dispProp_focalDisc = dispProp_focalDisc;
	}

	public JRadioButton getDispProp_focalNone() {
		return dispProp_focalNone;
	}

	public void setDispProp_focalNone(JRadioButton dispProp_focalNone) {
		this.dispProp_focalNone = dispProp_focalNone;
	}

	public void updateDisplay() {

		/*EQCatalog cat = this.catalogTable.getSelectedValue();
			EQStats.setCatalog(cat);
			cat.updateDisplay();
			CatalogAccessor catalog = null;
			catalog = this.catalogTable.getSelectedValue();
			getEarthquakes(catalog);

			cat.updateDisplay();
			getEarthquakes(catalog);*/
	}

	/**
	 * This gets called from EarthquakeCatalogPlugin and turns all the
	 * earthquakes' pickability off or on, depending if the tab is
	 * currently selected.
	 * @param enable
	 */
	public void setPickable(boolean enable) {
		/*EQCatalog cat;
			CatalogTableModel tableModel;
			tableModel = catalogTable.getTableModel();
			ArrayList<Earthquake> catalogEQ;
			Earthquake eq;

			for(int i=0; i<catalogTable.getRowCount(); i++){
				cat = (EQCatalog)tableModel.getObjectAtRow(i);
				catalogEQ = cat.getDisplayedEQs();
				for(int j=0; j<catalogEQ.size(); j++){
					eq = catalogEQ.get(j);
					eq.setPickable(enable);
				}
			}*/
	}

	public void setRenderProtect(boolean on, boolean auto) {
		if (on) {
			if (auto) {
				catProp_playButton.setEnabled(false);
				catProp_pauseButton.setEnabled(false);
			} else {
				catProp_playButton.setEnabled(true);
				catProp_pauseButton.setEnabled(true);
			}
			catProp_stopButton.setEnabled(false);
			catProp_endButton.setEnabled(false);
		} else {
			catProp_playButton.setEnabled(true);
			catProp_stopButton.setEnabled(false);
			catProp_endButton.setEnabled(true);
		}
	}

	public boolean getAnimationDisplayTime() {
		return animationDisplayTimeCheckbox.isSelected();
	}
	public void setAnimationDisplayTime(boolean selected){
		animationDisplayTimeCheckbox.setSelected(selected);
	}
	public boolean getAnimationType(){
		//true = true time
		//false = equal event time
		if(catProp_relative.isSelected())
			return true;
		else
			return false;
	}

	public void setAnimationType(boolean type){
		//true = true time
		//false = equal event time
		if(type){
			catProp_relative.setSelected(true);
		}else{
			catProp_static.setSelected(true);
		}
	}
	public ArrayList<EQCatalog> getCatalogs() {
		// TODO Auto-generated method stub
		return eqCatalogs;
	}
	public PickHandler<EQCatalog> getPickHandler() {
		// TODO Auto-generated method stub
		return pickHandler;
	}
	

}
