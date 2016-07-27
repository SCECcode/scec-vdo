package org.scec.vtk.plugins;

/**
 * Interface which allows the Timeline GUI to listen for changes to an AnimatablePlugin's isAnimatable() state.
 * @author kevin
 *
 */
public interface AnimatableChangeListener {
	
	/**
	 * AnimatablePlugins call this method on all listeners when their animatable state changes
	 * @param plugin
	 * @param isAnimatable
	 */
	public void animatableChanged(AnimatablePlugin plugin, boolean isAnimatable);

}
