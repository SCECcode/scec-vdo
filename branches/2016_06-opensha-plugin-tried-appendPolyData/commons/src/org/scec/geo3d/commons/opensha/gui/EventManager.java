package org.scec.geo3d.commons.opensha.gui;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;

import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.geo3d.commons.opensha.faults.AbstractFaultSection;
import org.scec.geo3d.commons.opensha.faults.anim.FaultAnimation;
import org.scec.geo3d.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.geo3d.commons.opensha.faults.colorers.FaultColorer;
import org.scec.geo3d.commons.opensha.gui.anim.AnimationListener;
import org.scec.geo3d.commons.opensha.gui.dist.VisibleFaultSurfacesProvider;
import org.scec.geo3d.commons.opensha.surfaces.ActorBundle;
import org.scec.geo3d.commons.opensha.surfaces.FaultSectionActorList;
import org.scec.geo3d.commons.opensha.surfaces.FaultSectionBundledActorList;
import org.scec.geo3d.commons.opensha.surfaces.GeometryGenerator;
import org.scec.geo3d.commons.opensha.surfaces.events.GeometryGeneratorChangeListener;
import org.scec.geo3d.commons.opensha.surfaces.events.GeometrySettingsChangeListener;
import org.scec.geo3d.commons.opensha.surfaces.events.GeometrySettingsChangedEvent;
import org.scec.geo3d.commons.opensha.surfaces.pickBehavior.FaultSectionPickBehavior;
import org.scec.geo3d.commons.opensha.surfaces.pickBehavior.PickHandler;
import org.scec.geo3d.commons.opensha.tree.AbstractFaultNode;
import org.scec.geo3d.commons.opensha.tree.FaultSectionNode;
import org.scec.geo3d.commons.opensha.tree.events.ColorChangeListener;
import org.scec.geo3d.commons.opensha.tree.events.CustomColorSelectionListener;
import org.scec.geo3d.commons.opensha.tree.events.TreeChangeListener;
import org.scec.geo3d.commons.opensha.tree.events.VisibilityChangeListener;
import org.scec.geo3d.commons.opensha.tree.gui.FaultTreeTable;

import vtk.vtkActor;
import vtk.vtkPanel;
import vtk.vtkRenderer;
import vtk.vtkUnsignedCharArray;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

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
	
	// TODO:
	// everything related to pick behaviour
	
	private static final boolean D = false;
	private static final boolean queue_renders = true;
	
	private vtkPanel rendererPanel;
	
	private HashMap<AbstractFaultSection, FaultSectionActorList> actorsMap =
		new HashMap<AbstractFaultSection, FaultSectionActorList>();
	private vtkRenderer renderer;
	
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
	
//	private PickHandler defaultPickHandler;
//	private FaultSectionPickBehavior pickBehavior;
	
	private LockableUI panelLock;
	private Thread currentCalcThread;
	
	
	public EventManager(		vtkPanel rendererPanel,
								FaultTreeTable table,
								ColorerPanel colorerPanel,
								GeometryTypeSelectorPanel geomPanel,
								ParameterList faultParams,
								Color defaultFaultColor,
								FaultSectionPickBehavior pickBehavior,
								LockableUI panelLock,
								FaultSectionPickBehavior pick) {
		this.rendererPanel = rendererPanel;
		this.renderer = rendererPanel.GetRenderer();
		this.defaultFaultColor = defaultFaultColor;
//		this.pickBehavior = pickBehavior;
//		this.defaultPickHandler = pickBehavior.getPickHandler();
		this.panelLock = panelLock;
		
		this.colorerPanel = colorerPanel;
		if (colorerPanel != null) {
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
	private FaultSectionActorList getBuildActors(AbstractFaultSection fault) {
		FaultSectionActorList actors = actorsMap.get(fault);
		if (actors == null) {
			if (D) System.out.println("Building actors for fault: '"+fault.getName()+"'");
			FaultSectionNode node = nodes.get(fault);
			RuptureSurface surface = getBuildSurface(fault);
			if (surface == null)
				return null;
			Color color = node.getColor();
			if (color == null)
				color = defaultFaultColor;
			GeometryGenerator geomGen = getGeomGen(fault);
			actors = geomGen.createFaultActors(surface, color, fault);
			actorsMap.put(fault, actors);
//			if (actors instanceof FaultSectionBundledActorList) {
//				ActorBundle bundle = ((FaultSectionBundledActorList)actors).getBundle();
//				synchronized (bundle) {
//					bundle.modified();
//				}
//			}
		}
		return actors;
	}
	
	private void unCacheBranch(AbstractFaultSection fault) {
		if (D) System.out.println("UnCaching BG for fault: '"+fault.getName()+"'");
		FaultSectionActorList actors = actorsMap.remove(fault);
		for (vtkActor actor : actors) {
			if (isActorDisplayed(actor))
				renderer.RemoveActor(actor);
		}
		if (actors instanceof FaultSectionBundledActorList) {
			vtkActor actor = ((FaultSectionBundledActorList)actors).getBundle().getActor();
			if (isActorDisplayed(actor))
				renderer.RemoveActor(actor);
		}
		updateViewer();
	}
	
	private void rebuildAllVisibleFaults() {
		if (D) System.out.println("Rebuilding all visible faults");
		for (FaultSectionNode node : nodes.values()) {
			AbstractFaultSection fault = node.getFault();
			FaultSectionActorList actors = actorsMap.get(fault);
			if (actors != null) {
				unCacheBranch(fault);
				if (node.isVisible()) {
					displayFault(fault, false);
				}
			} else if (node.isVisible()) {
				// it is visible, but not built yet
				getBuildActors(fault);
				displayFault(fault, false);
			}
		}
		updateViewer();
		System.gc();
	}
	
	private boolean isActorDisplayed(vtkActor actor) {
		Stopwatch watch = null;
		if (D) watch = Stopwatch.createStarted();
		boolean present = renderer.GetActors().IsItemPresent(actor) != 0;
		if (D) watch.stop();
		if (D) System.out.println("Took "+watch.elapsed(TimeUnit.MILLISECONDS)+" ms detect if present");
		return present;
	}
	
	private void displayFault(AbstractFaultSection fault, boolean updateViewer) {
		FaultSectionActorList actors = getBuildActors(fault);
		if (D) System.out.println("Displaying fault: '"+fault.getName()+"'");
		for (vtkActor actor : actors) {
			actor.SetVisibility(1);
			if (isActorDisplayed(actor)) {
				// it's already in there, update it
				actor.Modified();
			} else {
				// add it
				renderer.AddActor(actor);
			}
		}
		if (actors instanceof FaultSectionBundledActorList) {
			FaultSectionBundledActorList bundleList = (FaultSectionBundledActorList)actors;
			ActorBundle bundle = bundleList.getBundle();
			synchronized (bundle) {
				vtkActor actor = bundle.getActor();
				Preconditions.checkNotNull(actor);
				actor.SetVisibility(1); // always visible in bundled view
				setBundledOpacity(bundleList, true);
				if (isActorDisplayed(actor))
					actor.Modified();
				else
					renderer.AddActor(actor);
			}
		}
		if (updateViewer)
			updateViewer();
	}
	
	private long lastRequestedRender = Long.MIN_VALUE;
	private long lastCompletedRender = Long.MIN_VALUE;
//	private boolean upToDate = true;
	
	private class RenderRunnable implements Runnable {
		
		@Override
		public void run() {
			synchronized (RenderRunnable.class) {
				if (lastCompletedRender > lastRequestedRender)
					return;
				Stopwatch watch;
				if (D) watch = Stopwatch.createStarted();
				long myRenderTime = System.nanoTime();
				rendererPanel.Render();
				rendererPanel.repaint();
				if (D) watch.stop();
				if (D) System.out.println("Took "+watch.elapsed(TimeUnit.MILLISECONDS)+" ms to render");
				if (myRenderTime > lastCompletedRender)
					lastCompletedRender = myRenderTime;
			}
		}
	}
	
	private synchronized void updateViewer() {
//		upToDate = false;
		long curTime = System.nanoTime();
		if (curTime > lastRequestedRender)
			lastRequestedRender = curTime;
		// queue the render and avoid duplicates
		Runnable updateRunnable = new RenderRunnable();
		if (queue_renders)
			SwingUtilities.invokeLater(updateRunnable);
		else
			updateRunnable.run();
	}
	
	private void hideFault(AbstractFaultSection fault) {
		FaultSectionActorList actors = actorsMap.get(fault);
		for (vtkActor actor : actors) {
			actor.SetVisibility(0);
			actor.Modified(); // TODO needed?
		}
		if (actors instanceof FaultSectionBundledActorList) {
			FaultSectionBundledActorList bundleList = (FaultSectionBundledActorList)actors;
			ActorBundle bundle = bundleList.getBundle();
			synchronized (bundle) {
				vtkActor actor = bundle.getActor();
				Preconditions.checkNotNull(actor);
				actor.SetVisibility(1); // always visible in bundled view
				setBundledOpacity(bundleList, false);
				actor.Modified();
			}
		}
		updateViewer();
	}
	
	// synchroinzed externally
	private void setBundledOpacity(FaultSectionBundledActorList bundleList, boolean visible) {
		vtkUnsignedCharArray colors = bundleList.getColorArray();
		double opacity;
		if (visible)
			opacity = bundleList.getMyOpacity();
		else
			opacity = 0;
		int totNumTuples = colors.GetNumberOfTuples();
		for (int index=0; index<totNumTuples; index++) {
			double[] orig = colors.GetTuple4(index);
			colors.SetTuple4(index, orig[0], orig[1], orig[2], opacity); // keep same color
		}
		bundleList.getBundle().modified();
	}

	@Override
	public void geometryGeneratorChanged(GeometryGenerator geomGen) {
		// this is called when the geometry generator is changed
		
		if (this.geomGen != null)
			this.geomGen.clearBundles();
		
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
		FaultSectionActorList actors = actorsMap.get(fault);
		if (actors != null) {
			if (D) System.out.println("Trying to update color for fault: '"+fault.getName()+"'");
			boolean success;
			try {
				success = getGeomGen(fault).updateColor(actors, newColor);
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
				displayFault(fault, true);
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
				try {
					panelLock.setLocked(false);
				} catch (NullPointerException e) {
					System.out.println("TODO: NPE unlocking panel");
				}
				if (D) System.out.println("Called setLocked(false)...still locked? "+panelLock.isLocked()+"");
			}
		});
	}

	@Override
	public synchronized void colorerChanged(final FaultColorer newColorer) {
		// this is called when the fault colorer is changed
		// TODO
//		if (pickBehavior != null)
//			pickBehavior.setColorer(null);
		
		// if it's null, then they switched to custom and we don't have to do anything
		if (newColorer == null) {
//			if (pickBehavior != null)
//				pickBehavior.setPickHandler(defaultPickHandler);
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
//						if (pickBehavior != null
//								&& newColorer instanceof PickHandler)
//							pickBehavior
//									.setPickHandler((PickHandler) newColorer);
						if (D)
							System.out.println("Colorer changed");
						for (FaultSectionNode node : nodes.values()) {
							Color color = newColorer.getColor(node.getFault());
							if (color == null || !color.equals(node.getColor()))
								node.setColor(color);
						}
					} finally {
						unlockGUI();
//						if (pickBehavior != null)
//							pickBehavior.setColorer(newColorer);
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
			displayFault(fault, true);
		} else {
			// hide the fault
			hideFault(fault);
		}
	}

	@Override
	public void treeChanged(TreeNode newRoot) {
		// this is called when the entire tree is rebuilt
		
		if (D) System.out.println("Tree changed!");
		for (AbstractFaultSection fault : actorsMap.keySet())
			unCacheBranch(fault);
		if (geomGen != null)
			geomGen.clearBundles();
		nodes.clear();
		idSectionsMap.clear();
		actorsMap.clear();
		surfaces.clear();
		System.gc();
		FaultColorer colorer;
		if (colorerPanel == null)
			colorer = null;
		else
			colorer = colorerPanel.getSelectedColorer();
//		if (pickBehavior != null)
//			pickBehavior.setColorer(colorer);
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

}
