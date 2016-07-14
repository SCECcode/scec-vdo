package org.scec.vtk.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scec.vtk.main.Info;

import vtk.vtkActor;
import vtk.vtkProp;

public class PluginActors {
	
	private HashSet<vtkProp> actors;
	
	// used to notify the GUI of changes to actors. plugins can't add actors directly to the gui
	private List<PluginActorsChangeListener> listeners = new ArrayList<>();
	
	public PluginActors() {
		this.actors = new HashSet<>();
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
		for (vtkProp actor : actors)
			removeActor(actor);
	}
	
	/**
	 * 
	 * @return an unmodifiable view of the current actors
	 */
	public Set<vtkProp> getActors() {
		return Collections.unmodifiableSet(actors);
	}
	
	/**
	 * Remove all actors from any listeners without removing them from the actors group
	 */
	public void visibilityOff() {
		for (vtkProp actor : actors)
			for (PluginActorsChangeListener l : listeners)
				l.actorRemoved(actor);
	}
	
	/**
	 * Re-add all actors to any listeners
	 */
	public void visibilityOn() {
		for (vtkProp actor : actors)
			for (PluginActorsChangeListener l : listeners)
				l.actorAdded(actor);
	}
	
	/**
	 * Creates a deep copy of an actor
	 */
	public void deepCopy(PluginActors source) {
		
		for (vtkProp actor : source.getActors())
		{
			if (actor instanceof vtkActor)
			{
				vtkActor copy = new vtkActor();
				copy.SetVisibility(actor.GetVisibility());
				copy.SetPickable(actor.GetPickable());
				copy.SetDragable(actor.GetDragable());
				copy.SetUseBounds(actor.GetUseBounds());
				copy.SetAllocatedRenderTime(actor.GetAllocatedRenderTime(), null);
				copy.SetEstimatedRenderTime(actor.GetEstimatedRenderTime());
				copy.SetRenderTimeMultiplier(actor.GetRenderTimeMultiplier());
			
				for (int i=0; i<actor.GetNumberOfConsumers(); i++)
				{
					copy.AddConsumer(actor.GetConsumer(i));
				}
				copy.SetPropertyKeys(actor.GetPropertyKeys());
				copy.SetDebug(actor.GetDebug());
				copy.SetReferenceCount(actor.GetReferenceCount());
				copy.SetMapper(((vtkActor) actor).GetMapper());
				copy.GetProperty().SetColor(((vtkActor) actor).GetProperty().GetColor());
				copy.GetProperty().SetOpacity(((vtkActor) actor).GetProperty().GetOpacity());
				copy.GetProperty().SetTexture(0,((vtkActor) actor).GetProperty().GetTexture(0));
				copy.SetPosition(((vtkActor) actor).GetPosition());
				System.out.println(copy.GetProperty().GetColor()[0]);
				addActor(copy);
			}
		}
		
		Info.getMainGUI().updateRenderWindow();
	}

}
