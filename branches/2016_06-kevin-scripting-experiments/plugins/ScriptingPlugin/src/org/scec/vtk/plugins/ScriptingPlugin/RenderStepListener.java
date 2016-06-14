package org.scec.vtk.plugins.ScriptingPlugin;

public interface RenderStepListener {
	
    /**
     * Called when a render operation is started
     */
    public void renderStarted();
    
    /**
     * Called when a render operation is stopped (completed or cancelled)
     */
    public void renderStopped();
    
    /**
     * Called directly before rendering the given frame
     * @param startTime animation start time
     * @param curTime current animation time
     * @param endTime animation end time
     */
    public void renderFrameToBeProcessed(double startTime, double curTime, double endTime);

}
