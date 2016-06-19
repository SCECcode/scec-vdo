package org.scec.vtk.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

}
