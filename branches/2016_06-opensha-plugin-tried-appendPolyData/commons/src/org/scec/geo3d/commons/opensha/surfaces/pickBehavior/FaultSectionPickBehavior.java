package org.scec.geo3d.commons.opensha.surfaces.pickBehavior;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import org.scec.geo3d.commons.opensha.faults.colorers.FaultColorer;
import org.scec.geo3d.commons.opensha.surfaces.FaultSectionActorList;

import com.google.common.base.Preconditions;

public class FaultSectionPickBehavior {

	// TODO port over
//	private PickCanvas pickCanvas;
//	private PickResult pickResult;
//	private PickResult prev=null;
//	
//	private PickHandler pickHandler;
//	
//	private PickHandler defaultPickHandler;
//
//	/**
//	 * Constructs a new picking behavior.
//	 * 
//	 * @param canvas the <code>Canvas3D</code> where picking will occur
//	 * @param bg the <code>BranchGroup</code> to which the behavior is applied
//	 */
//	public FaultSectionPickBehavior(Canvas3D canvas, BranchGroup bg) {
//		this(canvas, bg, null);
//	}
//	
//	/**
//	 * Constructs a new picking behavior.
//	 * 
//	 * @param canvas the <code>Canvas3D</code> where picking will occur
//	 * @param bg the <code>BranchGroup</code> to which the behavior is applied
//	 * @param the {@link PickHandler} to handle events
//	 */
//	public FaultSectionPickBehavior(Canvas3D canvas, BranchGroup bg, PickHandler pickHandler) {
//		Preconditions.checkNotNull(canvas, "Canvas can't be null!");
//		Preconditions.checkNotNull(bg, "BranchGroup can't be null!");
//		this.pickCanvas = new PickCanvas(canvas, bg);
//		this.pickCanvas.setTolerance(5.0f);
//		this.pickCanvas.setMode(PickTool.GEOMETRY);
//		
//		this.pickHandler = pickHandler;
//		this.defaultPickHandler = new NameDispalyPickHandler();
//	}
//
//
//	/**
//	 * Required method of a <code>Behavior</code>.
//	 * 
//	 * @see javax.media.j3d.Behavior#initialize()
//	 */
//	public void initialize() {
//		wakeupOn(new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED));
//	}
//
//	/**
//	 * Required method of a <code>Behavior</code> that processes intersection of a user
//	 * click point and scenegraph objects.
//	 * 
//	 * @see javax.media.j3d.Behavior#processStimulus(java.util.Enumeration)
//	 */
//	@Override
//	public void processStimulus(Enumeration criteria) {
//		if (pickHandler == null && defaultPickHandler == null)
//			return;
//		
////		System.out.println("Handling click!");
//
//		WakeupCriterion wakeup;
//		AWTEvent[] events;
//		int eventId;
//		while (criteria.hasMoreElements()) {
//
//			// cycle through list of criteria for wakeup
//			wakeup = (WakeupCriterion) criteria.nextElement();
//			if (wakeup instanceof WakeupOnAWTEvent) {
//				// process possible series of events comprising AWTEvent
//				events = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
//				for (int i = 0; i < events.length; i++) {
//					// determine if mouse was pressed/clicked
//					eventId = events[i].getID();
//					if (eventId == MouseEvent.MOUSE_PRESSED && events[i] instanceof MouseEvent) {
//						MouseEvent mouseEvent = (MouseEvent)events[i];
//						// make sure it was button 1
//						if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
//							// get coords for pickCanvas
//							int x = mouseEvent.getX();
//							int y = mouseEvent.getY();
//							this.pickCanvas.setShapeLocation(x, y);
//							try{
//								this.pickResult = this.pickCanvas.pickClosest();
//							}catch(NullPointerException e){
//								this.pickResult=null;
//							}
//							if (this.pickResult != null){
//								if(prev!=null)
//									prev=null;
//								if (this.pickResult.getObject() instanceof FaultSectionActor) {
//									FaultSectionActor faultShape = (FaultSectionActor)this.pickResult.getObject();
//									if (defaultPickHandler != null)
//										defaultPickHandler.faultPicked(faultShape, mouseEvent);
//									if (pickHandler != null)
//										pickHandler.faultPicked(faultShape, mouseEvent);
//								} else {
//									Node node = pickResult.getObject();
//									if (defaultPickHandler != null)
//										defaultPickHandler.otherPicked(node, mouseEvent);
//									if (pickHandler != null)
//										pickHandler.otherPicked(node, mouseEvent);
//								}
//
//								prev=this.pickResult;
//							} else {
////								System.out.println("was null");
//								if(prev!=null)
//									prev=null;
//								if (defaultPickHandler != null)
//									defaultPickHandler.nothingPicked(mouseEvent);
//								if (pickHandler != null)
//									pickHandler.nothingPicked(mouseEvent);
//							}
//						}
//					}
//				}
//			}
//		}
//		wakeupOn(new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED));
//	}
//	
//	public void setPickHandler(PickHandler pickHandler) {
//		this.pickHandler = pickHandler;
//	}
//	
//	public PickHandler getPickHandler() {
//		return this.pickHandler;
//	}
//
//	public PickHandler getDefaultPickHandler() {
//		return defaultPickHandler;
//	}
//
//	public void setDefaultPickHandler(PickHandler defaultPickHandler) {
//		this.defaultPickHandler = defaultPickHandler;
//	}
//
//	public Node cloneNode(boolean forceDuplicate) {
//		FaultSectionPickBehavior bpb = new FaultSectionPickBehavior(
//				this.pickCanvas.getCanvas(), this.pickCanvas.getBranchGroup());
//		bpb.duplicateNode(this, forceDuplicate);
//		return bpb;
//	}
//	
//	public void setColorer(FaultColorer colorer) {
//		if (defaultPickHandler instanceof NameDispalyPickHandler) {
//			((NameDispalyPickHandler)defaultPickHandler).setColorer(colorer);
//		}
//	}
//
//public void duplicateNode(Node n, boolean forceDuplicate) {
// if (!(n instanceof FaultSectionPickBehavior)) {
//                System.err.println("Trying to duplicate node which is not a FaultSectionPickBehavior node.");
//           return;
//      }
// FaultSectionPickBehavior copyFrom = (FaultSectionPickBehavior)n;
//       PickCanvas pc = new PickCanvas(copyFrom.pickCanvas.getCanvas(), new BranchGroup());
//    pc.setTolerance(10.0f);
//    pc.setMode(PickTool.GEOMETRY);
//       this.pickCanvas = pc;
//}

}
