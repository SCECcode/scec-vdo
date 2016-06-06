package org.scec.geo3d.library.wgcep.gui;

import java.awt.Color;
import java.util.Enumeration;
import java.util.HashMap;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import javax.vecmath.Point3d;

import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.geo3d.library.wgcep.faults.AbstractFaultSection;
import org.scec.geo3d.library.wgcep.faults.anim.FaultAnimation;
import org.scec.geo3d.library.wgcep.faults.colorers.ColorerChangeListener;
import org.scec.geo3d.library.wgcep.faults.colorers.FaultColorer;
import org.scec.geo3d.library.wgcep.gui.anim.AnimationListener;
import org.scec.geo3d.library.wgcep.gui.dist.VisibleFaultSurfacesProvider;
import org.scec.geo3d.library.wgcep.surfaces.GeometryGenerator;
import org.scec.geo3d.library.wgcep.surfaces.events.GeometryGeneratorChangeListener;
import org.scec.geo3d.library.wgcep.surfaces.events.GeometrySettingsChangeListener;
import org.scec.geo3d.library.wgcep.surfaces.events.GeometrySettingsChangedEvent;
import org.scec.geo3d.library.wgcep.surfaces.pickBehavior.FaultSectionPickBehavior;
import org.scec.geo3d.library.wgcep.surfaces.pickBehavior.PickHandler;
import org.scec.geo3d.library.wgcep.tree.AbstractFaultNode;
import org.scec.geo3d.library.wgcep.tree.FaultSectionNode;
import org.scec.geo3d.library.wgcep.tree.events.ColorChangeListener;
import org.scec.geo3d.library.wgcep.tree.events.CustomColorSelectionListener;
import org.scec.geo3d.library.wgcep.tree.events.TreeChangeListener;
import org.scec.geo3d.library.wgcep.tree.events.VisibilityChangeListener;
import org.scec.geo3d.library.wgcep.tree.gui.FaultTreeTable;

import com.google.common.base.Preconditions;

public class EventManager implements
GeometrySettingsChangeListener,
GeometryGeneratorChangeListener,
ColorChangeListener,
ColorerChangeListener,
CustomColorSelectionListener,
VisibilityChangeListener,
TreeChangeListener,
ParameterChangeListener,
VisibleFaultSurfacesProvider,
AnimationListener {
	
	private static final boolean D = false;
	
	private HashMap<AbstractFaultSection, BranchGroup> branches =
		new HashMap<AbstractFaultSection, BranchGroup>();
	private BranchGroup masterBG;
	private BranchGroup faultBG;
	
	private HashMap<AbstractFaultSection, RuptureSurface> surfaces =
		new HashMap<AbstractFaultSection, RuptureSurface>();
	
	private HashMap<AbstractFaultSection, FaultSectionNode> nodes =
		new HashMap<AbstractFaultSection, FaultSectionNode>();
	
	private HashMap<Integer, AbstractFaultSection> idSectionsMap =
		new HashMap<Integer, AbstractFaultSection>();
	
	private ColorerPanel colorerPanel;
	
	private GeometryGenerator geomGen;
	
	private ParameterList faultParams;
	
	private Color defaultFaultColor;
	
	private PickHandler defaultPickHandler;
	private FaultSectionPickBehavior pickBehavior;
	
	private LockableUI panelLock;
	private Thread currentCalcThread;
	
	
	public EventManager(		BranchGroup masterBG,
								FaultTreeTable table,
								ColorerPanel colorerPanel,
								GeometryTypeSelectorPanel geomPanel,
								ParameterList faultParams,
								Color defaultFaultColor,
								FaultSectionPickBehavior pickBehavior,
								LockableUI panelLock,
								FaultSectionPickBehavior pick) {
		this.masterBG = masterBG;
		this.defaultFaultColor = defaultFaultColor;
		this.pickBehavior = pickBehavior;
		this.defaultPickHandler = pickBehavior.getPickHandler();
		this.panelLock = panelLock;
		
		// this one is just for the faults themselves
		faultBG = new BranchGroup();
		faultBG.setCapability(BranchGroup.ALLOW_DETACH);
		faultBG.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		faultBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		faultBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		masterBG.addChild(faultBG);
		
		this.colorerPanel = colorerPanel;
		if (colorerPanel != null){
			colorerPanel.addColorerChangeListener(this);
			colorerPanel.setFaultTable(table);
		}
		
		table.addTreeChangeListener(this);
		table.getRowModel().setCustomColorListener(this);
		treeChanged(table.getTreeRoot());
		
		geomGen = geomPanel.getSelectedGeomGen();
		for (GeometryGenerator geomGen : geomPanel.getAllGeomGens()) {
			geomGen.addPlotSettingsChangeListener(this);
		}
		geomPanel.addGeometryGeneratorChangeListener(this);
		
		if (faultParams != null) {
			for (Parameter<?> param : faultParams) {
				param.addParameterChangeListener(this);
			}
			this.faultParams = faultParams;
		}
	}
	
	/**
	 * This returns the GriddedSurfaceAPI for the given fault. If it's not cached,
	 * the surface is generated and cached.
	 * 
	 * @return
	 */
	private RuptureSurface getBuildSurface(AbstractFaultSection fault) {
		RuptureSurface surface = surfaces.get(fault);
		
		if (surface == null) {
			if (D) System.out.println("Building surface for fault: '"+fault.getName()+"'");
			surface = fault.createSurface(faultParams);
			Preconditions.checkNotNull(surface, "Surface cannot be null!");
			surfaces.put(fault, surface);
		}
		
		return surface;
	}
	
	/**
	 * Gets the geometry generator specific to this fault. This will normally be the global one, unless
	 * the fault has it's own custom geometry generator.
	 * 
	 * @param fault
	 * @return
	 */
	private GeometryGenerator getGeomGen(AbstractFaultSection fault) {
		GeometryGenerator geomGen = fault.getCustomGeometryGenerator();
		if (geomGen == null)
			geomGen = this.geomGen;
		return geomGen;
	}
	
	/**
	 * This returns the BranchGroup for the given surface. If it's not cached, the BranchGroup
	 * is generated and cached.
	 * 
	 * @param fault
	 * @return
	 */
	private BranchGroup getBuildBG(AbstractFaultSection fault) {
		BranchGroup bg = branches.get(fault);
		if (bg == null) {
			if (D) System.out.println("Building BG for fault: '"+fault.getName()+"'");
			FaultSectionNode node = nodes.get(fault);
			RuptureSurface surface = getBuildSurface(fault);
			if (surface == null)
				return null;
			Color color = node.getColor();
			if (color == null)
				color = defaultFaultColor;
			GeometryGenerator geomGen = getGeomGen(fault);
			bg = geomGen.createFaultActor(surface, color, fault);
			branches.put(fault, bg);
		}
		return bg;
	}
	
	private void unCacheBranch(AbstractFaultSection fault) {
		if (D) System.out.println("UnCaching BG for fault: '"+fault.getName()+"'");
		BranchGroup bg = branches.remove(fault);
		if (bg != null && bg.isLive())
			bg.detach();
	}
	
	private void rebuildAllVisibleFaults() {
		if (D) System.out.println("Rebuilding all visible faults");
		for (FaultSectionNode node : nodes.values()) {
			AbstractFaultSection fault = node.getFault();
			BranchGroup bg = branches.get(fault);
			if (bg != null) {
				unCacheBranch(fault);
				if (node.isVisible()) {
					displayFault(fault);
				}
			} else if (node.isVisible()) {
				// it is visible, but not built yet
				getBuildBG(fault);
				displayFault(fault);
			}
		}
		System.gc();
	}
	
	private void displayFault(AbstractFaultSection fault) {
		BranchGroup bg = getBuildBG(fault);
		
		
		bg.detach();
		if (D) System.out.println("Displaying fault: '"+fault.getName()+"'");
		if (bg != null && !bg.isLive()){
//			Preconditions.checkState(masterBG.getParent() != null, "Master BG is detached!");
//			Preconditions.checkState(faultBG.getParent() != null, "Fault BG is detached!");
			
//			//These getParent() calls were a quick fix to a problem we were having with the branchgroups
//			//For some reason they would not be added even though we added it in the load() function and elsewhere
//			//Can be buggy, sometimes it says it throws an exception saying it can only add a branch group as a child
//			if(pickBehavior.getParent() == null){
//				masterBG.addChild(pickBehavior); //this is necessary to get the click and shift click functionality of the inversion plugin
//				Geo3d.getMainWindow().getPluginBranchGroup().addChild(masterBG);
//			}
//			
//			if(masterBG.getParent() == null){
//				Geo3d.getMainWindow().getPluginBranchGroup().addChild(masterBG);
//			}
//			
//			if(faultBG.getParent() == null){
//				masterBG.addChild(faultBG);
//			}
			
			faultBG.addChild(bg);
		}
	}
	
	private void hideFault(AbstractFaultSection fault) {
		BranchGroup bg = branches.get(fault);
		if (bg != null && bg.isLive())
			bg.detach();
	}

	@Override
	public void geometryGeneratorChanged(GeometryGenerator geomGen) {
		// this is called when the geometry generator is changed
		
		if (D) System.out.println("Geometry Generator changed");
		this.geomGen = geomGen;
		rebuildAllVisibleFaults();
	}
	
	@Override
	public void geometrySettingsChanged(GeometrySettingsChangedEvent e) {
		// this is called when one of the geometry generator's params is changed
		
		if (D) System.out.println("Geometry Settings changed");
		rebuildAllVisibleFaults();
	}

	@Override
	public void colorChanged(AbstractFaultSection fault, Color newColor) {
		// this is called when the color of an individual fault changes
		
		if (newColor == null) {
			hideFault(fault);
			return;
		}
		
		if (D) System.out.println("Color changed for fault: '"+fault.getName()+"'");
		BranchGroup bg = branches.get(fault);
		if (bg != null) {
			if (D) System.out.println("Trying to update color for fault: '"+fault.getName()+"'");
			boolean success;
			try {
				success = getGeomGen(fault).updateColor(bg, newColor);
			} catch (Exception e) {
				System.out.println("Warning: excpetion updating color: "+e);
				success = false;
			}
			if (!success) {
				if (D) System.out.println("WARNING: couldn't update color, rebuilding");
				unCacheBranch(fault);
			}
			FaultSectionNode node = nodes.get(fault);
			if (node.isVisible()) {
				displayFault(fault);
			}
		}
	}
	
	private void lockGUI() {
		if (D) System.out.println("Calling setLocked(true)...already locked? "+panelLock.isLocked()+"");
		panelLock.setLocked(true);
		if (D) System.out.println("after lock: locked? "+panelLock.isLocked());
	}
	
	private void unlockGUI() {
		if (D) System.out.println("before unlock: locked? "+panelLock.isLocked());
		// we do it this way so that it is enabled in the AWT event queue, which prevents deadlock (hopefully)
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				panelLock.setLocked(false);
				if (D) System.out.println("Called setLocked(false)...still locked? "+panelLock.isLocked()+"");
			}
		});
	}

	@Override
	public void colorerChanged(final FaultColorer newColorer) {
		// this is called when the fault colorer is changed
		if (pickBehavior != null)
			pickBehavior.setColorer(null);
		
		// if it's null, then they switched to custom and we don't have to do anything
		if (newColorer == null) {
			if (pickBehavior != null)
				pickBehavior.setPickHandler(defaultPickHandler);
		} else {
			// this can take a while, depending, so lets do it threaded and lock the UI
			
			// if the thread is already going, lets make sure that it has fully exited.
			if (currentCalcThread != null && currentCalcThread.isAlive()) {
				try {
					currentCalcThread.join();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			
			lockGUI();
			currentCalcThread = new Thread() {

				@Override
				public void run() {
					try {
						if (pickBehavior != null
								&& newColorer instanceof PickHandler)
							pickBehavior
									.setPickHandler((PickHandler) newColorer);
						if (D)
							System.out.println("Colorer changed");
						for (FaultSectionNode node : nodes.values()) {
							Color color = newColorer.getColor(node.getFault());
							if (color == null || !color.equals(node.getColor()))
								node.setColor(color);
						}
					} finally {
						unlockGUI();
						if (pickBehavior != null)
							pickBehavior.setColorer(newColorer);
					}
				}
				
			};
			currentCalcThread.start();
		}
	}
	
	public void waitOnCalcThread() throws InterruptedException {
		if (currentCalcThread != null && currentCalcThread.isAlive()) {
			currentCalcThread.join();
		}
	}
	
	@Override
	public void customColorSelected() {
		// this is called when the user selects a custom color
		
		if (D) System.out.println("Custom colorer selected");
		if (colorerPanel != null)
			colorerPanel.setSelectedColorer(null);
	}

	@Override
	public void visibilityChanged(AbstractFaultSection fault,
			boolean newVisibility) {
		// this is called when the visibility of a fault changes
		
		if (D) System.out.println("Visibility changed for fault: '"+fault.getName()+"' (visible="+newVisibility+")");
		if (newVisibility) {
			// make the fault visible
			displayFault(fault);
		} else {
			// hide the fault
			hideFault(fault);
		}
	}

	@Override
	public void treeChanged(TreeNode newRoot) {
		// this is called when the entire tree is rebuilt
		
		if (D) System.out.println("Tree changed!");
		faultBG.removeAllChildren();
		nodes.clear();
		idSectionsMap.clear();
		branches.clear();
		surfaces.clear();
		System.gc();
		FaultColorer colorer;
		if (colorerPanel == null)
			colorer = null;
		else
			colorer = colorerPanel.getSelectedColorer();
		if (pickBehavior != null)
			pickBehavior.setColorer(colorer);
		processAllChildren(newRoot, colorer);
		
		rebuildAllVisibleFaults();
		if (D) System.out.println("Found "+nodes.size()+" fault nodes");
	}
	
	private void processAllChildren(TreeNode node, FaultColorer colorer) {
		Enumeration<TreeNode> children = node.children();
		while (children.hasMoreElements()) {
			TreeNode child = children.nextElement();
			if (child instanceof FaultSectionNode) {
				FaultSectionNode faultNode = (FaultSectionNode)child;
				AbstractFaultSection fault = faultNode.getFault();
				if (colorer == null)
					faultNode.setColor(defaultFaultColor);
				else
					faultNode.setColor(colorer.getColor(fault));
				faultNode.setColorChangeListener(this);
				faultNode.setVisibilityChangeListener(this);
				nodes.put(fault, faultNode);
				idSectionsMap.put(fault.getId(), fault);
			}
			processAllChildren(child, colorer);
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		// this is called when a fault parameter changes
		
		if (faultParams != null && faultParams.containsParameter(event.getParameterName())) {
			if (D) System.out.println("Fault parameter changed");
			surfaces.clear();
			rebuildAllVisibleFaults();
		}
	}

	@Override
	public HashMap<AbstractFaultSection, RuptureSurface> getVisibleSurfaces() {
		HashMap<AbstractFaultSection, RuptureSurface> visSurfaces =
			new HashMap<AbstractFaultSection, RuptureSurface>();
		
		for (AbstractFaultSection fault : nodes.keySet()) {
			AbstractFaultNode node = nodes.get(fault);
			if (node.isVisible()) {
				RuptureSurface surface = surfaces.get(fault);
				if (surface == null)
					throw new RuntimeException("Surfaces is displayed but null?");
				visSurfaces.put(fault, surface);
			}
		}
		
		return visSurfaces;
	}

	@Override
	public AbstractFaultNode getNode(AbstractFaultSection fault) {
		return nodes.get(fault);
	}
	
	@Override
	public AbstractFaultSection getFault(int id) {
		return idSectionsMap.get(id);
	}

	@Override
	public void animationRangeChanged(FaultAnimation anim) {
		FaultColorer colorer = anim.getFaultColorer();
		if (anim.getNumSteps() > 0 && colorer != null) {
			if (colorerPanel.getSelectedColorer() != colorer)
				colorerPanel.setSelectedColorer(colorer);
		}
	}

	@Override
	public void animationStepChanged(FaultAnimation anim) {
		FaultColorer colorer = anim.getFaultColorer();
		if (colorer != null)
			if (colorerPanel.getSelectedColorer() != colorer)
				colorerPanel.setSelectedColorer(colorer);
		for (AbstractFaultSection fault : nodes.keySet()) {
			AbstractFaultNode node = nodes.get(fault);
			
			Boolean newVisibility = anim.getFaultVisibility(fault);
			// if the visibliity is set and it's false, but this fault is visible, hide it
			if (newVisibility != null && !newVisibility && node.isVisible())
				node.setVisible(false);
			// update the color if applicable
			if (colorer != null) {
				Color newColor = colorer.getColor(fault);
				if (!newColor.equals(node.getColor()))
					node.setColor(newColor);
			}
			// set it visible if we're supposed to
			if (newVisibility != null && newVisibility)
				node.setVisible(true);
		}
	}
	
	public BranchGroup getMasterBG() {
		return masterBG;
	}

}
