package org.scec.vtk.timeline.render;

public interface RenderStatusListener {
	
	public void frameProcessed(int index, int total);
	
	public void finalizeStarted();
	
	public void finalizeProgress(int current, int total);
	
	public void finalizeCompleted();

}
