package org.scec.vtk.plugins.opensha;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.impl.GriddedParameterListEditor;
import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.AseismicityColorer;
import org.scec.vtk.commons.opensha.faults.colorers.CouplingCoefficientColorer;
import org.scec.vtk.commons.opensha.faults.colorers.DipColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.colorers.RakeColorer;
import org.scec.vtk.commons.opensha.faults.colorers.SlipRateColorer;
import org.scec.vtk.commons.opensha.faults.colorers.StrikeColorer;
import org.scec.vtk.commons.opensha.gui.ColorerPanel;
import org.scec.vtk.commons.opensha.gui.EventManager;
import org.scec.vtk.commons.opensha.gui.FaultTablePanel;
import org.scec.vtk.commons.opensha.gui.GeometryTypeSelectorPanel;
import org.scec.vtk.commons.opensha.gui.anim.AnimationPanel;
import org.scec.vtk.commons.opensha.gui.anim.MultiAnimPanel;
import org.scec.vtk.commons.opensha.gui.dist.DistancesPanel;
import org.scec.vtk.commons.opensha.surfaces.GeometryGenerator;
import org.scec.vtk.commons.opensha.surfaces.LineSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.PointSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.PolygonSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.pickBehavior.FaultSectionPickBehavior;
import org.scec.vtk.commons.opensha.tree.builders.FaultSectionInfoViewier;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.gui.FaultTreeTable;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.picking.PickHandler;

public class FaultPluginGUI extends JSplitPane {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FaultTreeBuilder builder;
	private FaultTreeTable table;
	private ColorerPanel colorPanel;         
	private MultiAnimPanel animPanel;
	private GeometryTypeSelectorPanel geomPanel;
	private GriddedParameterListEditor faultParamEditor;
	
	private FaultPluginState state;
	
	public static ArrayList<GeometryGenerator> createDefaultGeomGens() {
		ArrayList<GeometryGenerator> geomGens = new ArrayList<GeometryGenerator>();
		
		geomGens.add(new LineSurfaceGenerator());
		geomGens.add(new PointSurfaceGenerator());
		geomGens.add(new PolygonSurfaceGenerator());
		return geomGens;
	}
	
	public static ArrayList<FaultColorer> createDefaultNonSlipColorers() {
		ArrayList<FaultColorer> colorers = new ArrayList<FaultColorer>();
		
		colorers.add(new StrikeColorer());
		colorers.add(new DipColorer());
		colorers.add(new RakeColorer());
		return colorers;
	}
	
	public static ArrayList<FaultColorer> createDefaultColorers() {
		ArrayList<FaultColorer> colorers = createDefaultNonSlipColorers();
		
		colorers.add(0, new SlipRateColorer());
		
		return colorers;
	}
	
	public static ArrayList<FaultColorer> createPrefDataColorers(boolean includeSlip) {
		ArrayList<FaultColorer> colorers = createDefaultNonSlipColorers();
		
		if (includeSlip)
			colorers.add(0, new SlipRateColorer());
		colorers.add(new AseismicityColorer());
		colorers.add(new CouplingCoefficientColorer());
		
		return colorers;
	}
	
	private JTabbedPane settingsPanel = new JTabbedPane();
	
	private EventManager em;
	
	private FaultSectionPickBehavior pickhandler;
	
	private JXLayer<JComponent> jxLayer;
	private LockableUI lockUI;
	
	public FaultPluginGUI(	PluginActors pluginActors,
							FaultTreeBuilder builder,
							ArrayList<FaultColorer> colorers,
							ArrayList<GeometryGenerator> geomGens,
							Color defaultColor) {
		this(pluginActors, builder, colorers, geomGens, defaultColor, null);
	}
	
	private static boolean hasAnimColorer(ArrayList<FaultAnimation> faultAnims) {
		for (FaultAnimation anim : faultAnims) {
			if (anim.getFaultColorer() != null)
				return true;
		}
		return false;
	}

	public FaultPluginGUI(	PluginActors pluginActors,
							FaultTreeBuilder builder,
							ArrayList<FaultColorer> colorers,
							ArrayList<GeometryGenerator> geomGens,
							Color defaultColor,
							ArrayList<FaultAnimation> faultAnims) {
		super(VERTICAL_SPLIT);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		
		// this allows the entire UI to be locked during long calculations
		lockUI = new LockableUI();
		jxLayer = new JXLayer<JComponent>(this, lockUI);
		
		FaultSectionInfoViewier infoViewer;
		if (builder instanceof FaultSectionInfoViewier)
			infoViewer = (FaultSectionInfoViewier)builder;
		else
			infoViewer = null;
		
		this.builder = builder;
		table = new FaultTreeTable(builder, infoViewer);
		if (faultAnims != null && faultAnims.size() > 0 && hasAnimColorer(faultAnims)) {
			if (colorers == null)
				colorers = new ArrayList<FaultColorer>();
			for (FaultAnimation faultAnim : faultAnims) {
				if (!colorers.contains(faultAnim.getFaultColorer()))
					colorers.add(faultAnim.getFaultColorer());
			}
		}
		if (colorers != null && colorers.size() > 0)
			colorPanel = new ColorerPanel(colorers, colorers.get(0));
		    
		geomPanel = new GeometryTypeSelectorPanel(geomGens);
		if (builder.getFaultParams() != null && builder.getFaultParams().size() > 0)
			faultParamEditor = new GriddedParameterListEditor(builder.getFaultParams());
		
//		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		FaultTablePanel tablePanel = new FaultTablePanel(table);
		
		int tableHeight = 350;
		
		
		ParameterList builderParams = builder.getBuilderParams();
		
		// add builder params
		if (builderParams != null) {
			for (Parameter<?> param : builderParams) {
				JComponent comp = param.getEditor().getComponent();
				comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, comp.getPreferredSize().height));
				topPanel.add(comp);
				tableHeight -= 55;
			}
		}
		tablePanel.setPreferredSize(new Dimension(Prefs.getPluginWidth()-100, tableHeight));
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		topPanel.add(tablePanel);
		
		if (colorPanel != null)
			settingsPanel.addTab("Color", null, wrapInScrollPane(colorPanel), "Fault color settings");
		settingsPanel.addTab("Display Settings", null,
				wrapInScrollPane(geomPanel), "Fault display settings");
		if (faultParamEditor != null)
			settingsPanel.addTab("Fault Settings", null,
					wrapInScrollPane(faultParamEditor), "Fault geometry settings");
		
		setTopComponent(topPanel);
		setBottomComponent(settingsPanel);
		
		pickhandler = new FaultSectionPickBehavior();
		for (GeometryGenerator geomGen : geomGens)
			geomGen.setPickHandler(pickhandler);

		em = new EventManager( pluginActors, table, colorPanel, geomPanel,
				builder.getFaultParams(), defaultColor, pickhandler, lockUI, pickhandler);

		if (faultAnims != null && faultAnims.size() > 0) {
//			if (faultAnims.size() == 1) {
//				FaultAnimation faultAnim = faultAnims.get(0);
//				AnimationPanel animPanel = new AnimationPanel(faultAnim, em);
//				settingsPanel.addTab("Animation", null,
//						wrapInScrollPane(animPanel), "Animate by "+faultAnim.getName());
//			} else {
				animPanel = new MultiAnimPanel(faultAnims, em, colorPanel);
				settingsPanel.addTab("Animation", null,
						wrapInScrollPane(animPanel), "Fault Animations");
//			}
		}
	}
	
	private JScrollPane wrapInScrollPane(Component view) {
		JScrollPane scroll = new JScrollPane(view,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Dimension d = new Dimension(Prefs.getPluginWidth()-20, 200);
		scroll.setPreferredSize(d);
		return scroll;
	}

	protected void addDistTab() {
		// TODO
		System.out.println("TODO: add dist tab");
//		DistancesPanel distPanel = new DistancesPanel(faultBranchGroup, em, em);
//		table.addTreeChangeListener(distPanel);
//		settingsPanel.addTab("Fault Distances", wrapInScrollPane(distPanel));
	}
	
	protected void addTab(String title, JComponent comp) {
		if (!(comp instanceof JScrollPane))
			comp = wrapInScrollPane(comp);
		settingsPanel.addTab(title, comp);
	}
	
	public EventManager getEventManager() {
		return em;
	}
	
	public JXLayer<JComponent> getJXLayer() {
		return jxLayer;
	}
	
	public GeometryTypeSelectorPanel getGeomSelect() {
		return geomPanel;
	}
	
	public FaultPluginState getState() {
		if (state == null) {
			state = new FaultPluginState(this);
		}
		return state;
	}
	
	FaultTreeBuilder getBuilder() {
		return builder;
	}
	
	FaultTreeTable getFaultTreeTable() {
		return table;
	}
	
	ColorerPanel getColorPanel() {
		return colorPanel;
	}
	
	MultiAnimPanel getAnimPanel() {
		return animPanel;
	}
	
	ParameterList getFaultParams() {
		return faultParamEditor.getParameterList();
	}

}

