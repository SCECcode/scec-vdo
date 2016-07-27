package org.scec.vtk.tools.picking;

import java.awt.event.MouseEvent;

import vtk.vtkActor;
import vtk.vtkCellPicker;

/**
 * This extends vtkActor by adding picking capabilities via a <code>PickHandler</code> and a reference.
 * If a <code>PickEnabledActor</code> is picked, then the <code>picked</code> method will be called
 * which will notify its <code>PickHandler</code> and pass along it's reference. The reference object
 * allows a plugin to determine which object was picked, as the actor otherwise contians no information
 * on what created it.
 * 
 * @author Kevin
 *
 * @param <E>
 */
public class PickEnabledActor<E> extends vtkActor {
	
	protected PickHandler<E> handler;
	private E reference;

	public PickEnabledActor(PickHandler<E> handler, E reference) {
		super();
		
		this.SetPickable(1);
		
		this.handler = handler;
		this.reference = reference;
	}
	
	public void picked(vtkCellPicker picker, MouseEvent e) {
		if (handler == null)
			return;
		handler.actorPicked(this, reference, picker, e);
	}

}
