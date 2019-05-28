package org.scec.vtk.tools.picking;

import java.awt.event.MouseEvent;

import vtk.vtkCellPicker;

/**
 * Interface to handle picked actors.
 * 
 * @author Kevin
 *
 * @param <E>
 */
public interface PickHandler<E> {
	
	public void actorPicked(PickEnabledActor<E> actor, E reference, vtkCellPicker picker, MouseEvent e);
	
}
