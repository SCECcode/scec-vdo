package org.scec.vtk.tools.picking;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import vtk.vtkCellPicker;

/**
 * Extends <code>PickEnabledActor</code> for the case where a single actor contains multiple pickable objects.
 * Point IDs should be registered with this class along with the reference data to identify what was picked.
 * 
 * @author Kevin
 *
 * @param <E>
 */
public class PointPickEnabledActor<E> extends PickEnabledActor<E> {
	
	private Map<Integer, E> pointIDreferenceMap = new HashMap<>();

	public PointPickEnabledActor(PickHandler<E> handler) {
		super(handler, null);
	}
	
	public void registerPointID(int pointID, E reference) {
		pointIDreferenceMap.put(pointID, reference);
	}
	
	public E unregisterPointID(int pointID) {
		return pointIDreferenceMap.remove(pointID);
	}
	
	public int getNumRegisteredPoints() {
		return pointIDreferenceMap.size();
	}

	@Override
	public void picked(vtkCellPicker picker, MouseEvent e) {
		if (handler == null)
			return;
		Integer pointID = picker.GetPointId();
		E reference = pointIDreferenceMap.get(pointID);
		handler.actorPicked(this, reference, picker, e);
	}

}
