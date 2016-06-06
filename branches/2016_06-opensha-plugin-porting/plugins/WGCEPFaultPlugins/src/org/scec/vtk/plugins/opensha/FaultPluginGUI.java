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
import org.scec.geo3d.library.wgcep.faults.anim.FaultAnimation;
import org.scec.geo3d.library.wgcep.faults.colorers.AseismicityColorer;
import org.scec.geo3d.library.wgcep.faults.colorers.CouplingCoefficientColorer;
import org.scec.geo3d.library.wgcep.faults.colorers.DipColorer;
import org.scec.geo3d.library.wgcep.faults.colorers.FaultColorer;
import org.scec.geo3d.library.wgcep.faults.colorers.RakeColorer;
import org.scec.geo3d.library.wgcep.faults.colorers.SlipRateColorer;
import org.scec.geo3d.library.wgcep.faults.colorers.StrikeColorer;
import org.scec.geo3d.library.wgcep.gui.ColorerPanel;
import org.scec.geo3d.library.wgcep.gui.EventManager;
import org.scec.geo3d.library.wgcep.gui.FaultTablePanel;
import org.scec.geo3d.library.wgcep.gui.GeometryTypeSelectorPanel;
import org.opensha.commons.param.editor.impl.GriddedParameterListEditor;
import org.scec.geo3d.library.wgcep.gui.anim.AnimationPanel;
import org.scec.geo3d.library.wgcep.gui.anim.MultiAnimPanel;
import org.scec.geo3d.library.wgcep.gui.dist.DistancesPanel;
import org.scec.geo3d.library.wgcep.surfaces.GeometryGenerator;
import org.scec.geo3d.library.wgcep.surfaces.LineSurfaceGenerator;
import org.scec.geo3d.library.wgcep.surfaces.PointSurfaceGenerator;
import org.scec.geo3d.library.wgcep.surfaces.PolygonSurfaceGenerator;
import org.scec.geo3d.library.wgcep.surfaces.pickBehavior.FaultSectionPickBehavior;
import org.scec.geo3d.library.wgcep.surfaces.pickBehavior.PickHandler;
import org.scec.geo3d.library.wgcep.tree.builders.FaultSectionInfoViewier;
import org.scec.geo3d.library.wgcep.tree.builders.FaultTreeBuilder;
import org.scec.geo3d.library.wgcep.tree.gui.FaultTreeTable;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.tools.Prefs;

public class FaultPluginGUI extends JSplitPane {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FaultTreeTable table;
	private ColorerPanel colorPanel;         
	private GeometryTypeSelectorPanel geomPanel;
	private GriddedParameterListEditor faultParamEditor;
	
	protected static ArrayList<GeometryGenerator> createDefaultGeomGens() {
		ArrayList<GeometryGenerator> geomGens = new ArrayList<GeometryGenerator>();
		
		// TODO
//		geomGens.add(new PointSurfaceGenerator());
		geomGens.add(new LineSurfaceGenerator());
//		geomGens.add(new PolygonSurfaceGenerator());
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
	
	private FaultSectionPickBehavior pickBehavior;
	
	private JXLayer<JComponent> jxLayer;
	private LockableUI lockUI;
	
	public FaultPluginGUI(	FaultTreeBuilder builder,
							ArrayList<FaultColorer> colorers,
							ArrayList<GeometryGenerator> geomGens,
							Color defaultColor) {
		this(builder, colorers, geomGens, defaultColor, null);
	}
	
	private static boolean hasAnimColorer(ArrayList<FaultAnimation> faultAnims) {
		for (FaultAnimation anim : faultAnims) {
			if (anim.getFaultColorer() != null)
				return true;
		}
		return false;
	}

	public FaultPluginGUI(	FaultTreeBuilder builder,
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
//		this.add(settingsPanel);
		
//		pickBehavior = new FaultSectionPickBehavior(Geo3dInfo.getRenderEnabledCanvas(), masterBranchGroup);
//		BoundingSphere appBounds = new BoundingSphere(new Point3d(), 30000.0);
//		pickBehavior.setSchedulingBounds(appBounds);
//		masterBranchGroup.addChild(pickBehavior);
		

		//	pluginBranchGroup.addChild(faultBranchGroup);

		em = new EventManager( MainGUI.getRenderWindow(), table, colorPanel, geomPanel,
				builder.getFaultParams(), defaultColor, pickBehavior, lockUI, pickBehavior);

		if (faultAnims != null && faultAnims.size() > 0) {
			if (faultAnims.size() == 1) {
				FaultAnimation faultAnim = faultAnims.get(0);
				AnimationPanel animPanel = new AnimationPanel(faultAnim, em);
				settingsPanel.addTab("Animation", null,
						wrapInScrollPane(animPanel), "Animate by "+faultAnim.getName());
			} else {
				MultiAnimPanel animPanel = new MultiAnimPanel(faultAnims, em, colorPanel);
				settingsPanel.addTab("Animation", null,
						wrapInScrollPane(animPanel), "Fault Animations");
			}
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
	
	public void setClickableEnabled(boolean enable) {
		// TODO
//		pickBehavior.setEnable(enable);
	}
	
	public void setPickHandler(PickHandler pickHandler) {
//		pickBehavior.setPickHandler(pickHandler);
	}
	
	public JXLayer<JComponent> getJXLayer() {
		return jxLayer;
	}
	
	public GeometryTypeSelectorPanel getGeomSelect() {
		return geomPanel;
	}

}

