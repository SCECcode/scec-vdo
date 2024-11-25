package org.scec.vtk.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scec.vtk.commons.legend.LegendItem;
import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;

import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkProp;

public class PluginActors {
	
	private HashSet<vtkProp> actors;
	private HashSet<LegendItem> legends;
	
	// used to notify the GUI of changes to actors. plugins can't add actors directly to the gui
	private List<PluginActorsChangeListener> listeners = new ArrayList<>();
	
	public PluginActors() {
		this.actors = new HashSet<>();
		this.legends = new HashSet<>();
	}
	
	public void addActorsChangeListener(PluginActorsChangeListener l) {
		listeners.add(l);
	}
	
	public synchronized void addActor(vtkProp actor) {
		if (!actors.contains(actor)) {
			actors.add(actor);
			for (PluginActorsChangeListener l : listeners)
				l.actorAdded(actor);
		}
	}
	
	public synchronized void removeActor(vtkProp actor) {
		if (actors.contains(actor)) {
			actors.remove(actor);
			for (PluginActorsChangeListener l : listeners)
				l.actorRemoved(actor);
		}
	}
	
	public boolean containsActor(vtkProp actor) {
		return actors.contains(actor);
	}
	
	public synchronized void clearActors() {
		for (vtkProp actor : new ArrayList<>(actors))
			removeActor(actor);
	}
	
	public synchronized void addLegend(LegendItem legend) {
		if (!legends.contains(legend)) {
			legends.add(legend);
			for (PluginActorsChangeListener l : listeners)
				l.legendAdded(legend);
		}
	}
	
	public synchronized void removeLegend(LegendItem legend) {
		if (legends.contains(legend)) {
			legends.remove(legend);
			for (PluginActorsChangeListener l : listeners)
				l.legendRemoved(legend);
		}
	}
	
	public boolean containsLegend(LegendItem legend) {
		return legends.contains(legend);
	}
	
	public synchronized void clearLegends() {
		for (LegendItem legend : new ArrayList<>(legends))
			removeLegend(legend);
	}
	
	/**
	 * 
	 * @return an unmodifiable view of the current actors
	 */
	public Set<vtkProp> getActors() {
		return Collections.unmodifiableSet(actors);
	}
	
	/**
	 * 
	 * @return an unmodifiable view of the current legends
	 */
	public Set<LegendItem> getLegends() {
		return Collections.unmodifiableSet(legends);
	}
	
	/**
	 * Remove all actors from any listeners without removing them from the actors group
	 */
	public void visibilityOff() {
		for (vtkProp actor : actors)
			for (PluginActorsChangeListener l : listeners)
				l.actorRemoved(actor);
		for (LegendItem legend : legends)
			for (PluginActorsChangeListener l : listeners)
				l.legendRemoved(legend);
	}
	
	/**
	 * Re-add all actors to any listeners
	 */
	public void visibilityOn() {
		for (vtkProp actor : actors)
			for (PluginActorsChangeListener l : listeners)
				l.actorAdded(actor);
		for (LegendItem actor : legends)
			for (PluginActorsChangeListener l : listeners)
				l.legendAdded(actor);
	}
	
	/**
	 * Creates a deep copy of an actor
	 */
	public void deepCopy(PluginActors source) {
		
		for (vtkProp prop : source.getActors())
		{
			if (prop instanceof vtkActor)
			{
				vtkActor actor = (vtkActor)prop;
				vtkActor copy = new vtkActor();
				copy.SetVisibility(prop.GetVisibility());
				copy.SetPickable(prop.GetPickable());
				copy.SetDragable(prop.GetDragable());
				copy.SetUseBounds(prop.GetUseBounds());
				copy.SetAllocatedRenderTime(prop.GetAllocatedRenderTime(), null);
				copy.SetEstimatedRenderTime(prop.GetEstimatedRenderTime());
				copy.SetRenderTimeMultiplier(prop.GetRenderTimeMultiplier());
			
				for (int i=0; i<prop.GetNumberOfConsumers(); i++)
				{
					copy.AddConsumer(prop.GetConsumer(i));
				}
				copy.SetPropertyKeys(prop.GetPropertyKeys());
				copy.SetDebug(prop.GetDebug());
				copy.SetReferenceCount(prop.GetReferenceCount());
				copy.SetMapper(actor.GetMapper());
				copy.GetProperty().SetColor(actor.GetProperty().GetColor());
				copy.GetProperty().SetOpacity(actor.GetProperty().GetOpacity());
				copy.SetTexture(actor.GetTexture());
				copy.SetPosition(actor.GetPosition());
				System.out.println(copy.GetProperty().GetColor()[0]);
				addActor(copy);
			}
		}
		
		MainGUI.updateRenderWindow();
	}

}
