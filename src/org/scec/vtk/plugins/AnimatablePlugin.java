package org.scec.vtk.plugins;

public interface AnimatablePlugin extends StatefulPlugin {
	
	/**
	 * Called by the Timeline when an animation keyframe begins
	 */
	public void animationStarted();
	
	/**
	 * Called by the Timeline when an animation keyframe ends
	 */
	public void animationEnded();
	
	/**
	 * Called by the Timeline to set the current animation time. This value is fractional between 0 and 1, 0 being the
	 * beginning of the animation and 1 being the end.
	 * @param fractionalTime
	 */
	public void animationTimeChanged(double fractionalTime);
	
	/**
	 * 
	 * @return true if an animation is currently ready, or false if nothing animatable has been loaded in the plugin
	 */
	public boolean isAnimatable();
	
	/**
	 * Should add the AnimatableChangeListener to an internal listener list. any time the isAnimatable() state changes, notify
	 * all listeners that have been added through this method. 
	 * @param l
	 */
	public void addAnimatableChangeListener(AnimatableChangeListener l);
	
	/**
	 * Should remove the AnimatableChangeListener to an internal listener list.
	 * @param l
	 */
	public void removeAnimatableChangeListener(AnimatableChangeListener l);

}
