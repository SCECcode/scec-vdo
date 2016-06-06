package org.scec.geo3d.library.wgcep.surfaces.pickBehavior;

import java.awt.event.MouseEvent;

import javax.media.j3d.Node;

import org.scec.geo3d.library.wgcep.surfaces.FaultSectionActorList;

public interface PickHandler {
	
	/**
	 * Called when the user clicks on a fault section shape 3D
	 * 
	 * @param faultShape the fault that was clicked
	 * @param clickCount number of clicks
	 */
	public void faultPicked(FaultSectionActorList faultShape, MouseEvent mouseEvent);
	
	/**
	 * Called when the user clicks on an object that is not a fault section shape
	 * @param node
	 * @param mouseEvent
	 */
	public void otherPicked(Node node, MouseEvent mouseEvent);
	
	/**
	 * Called when the user clicks, but no fault shapes were clicked on
	 * @param mouseEvent 
	 */
	public void nothingPicked(MouseEvent mouseEvent);

}
