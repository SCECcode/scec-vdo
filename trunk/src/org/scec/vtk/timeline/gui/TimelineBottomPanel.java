package org.scec.vtk.timeline.gui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.scec.vtk.timeline.AnimationTimeListener;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.timeline.render.RenderStatusListener;
import org.scec.vtk.timeline.render.Renderer;

public class TimelineBottomPanel extends JPanel implements AnimationTimeListener, RenderStatusListener {
	
	private static final int play_max = 100;
	private static final int play_min = 0;
	private JProgressBar playProgress;
	private JProgressBar renderProgress;
	
	private static final String renderingText = "Rendering";
	private static final String finalizeText = "Finalizing Render";
	private static final String renderDoneText = "Render Complete";
	
	private double maxTime;
	
	public TimelineBottomPanel(Timeline timeline) {
		setLayout(new GridLayout(1, 2));
		
		timeline.addAnimationTimeListener(this);
		for (Renderer r : timeline.getAvailableRenderers())
			r.setRenderStatusListener(this);
		
		maxTime = timeline.getMaxTime();
		
		playProgress = buildProgress(play_min, play_max, "Playback", Color.BLUE);
		renderProgress = buildProgress(0, 1, "", Color.RED);
		
		add(playProgress);
		add(renderProgress);
	}
	
	private static JProgressBar buildProgress(int min, int max, String text, Color color) {
		JProgressBar progress = new JProgressBar(min, max);
		progress.setValue(min);
		progress.setString(text);
		progress.setStringPainted(true);
		progress.setForeground(color);
		return progress;
	}

	@Override
	public void animationTimeChanged(double curTime) {
		double fract = curTime/maxTime;
		int p = (int)Math.round(fract*play_max);
		playProgress.setValue(p);
	}

	@Override
	public void animationBoundsChanged(double maxTime) {
		this.maxTime = maxTime;
	}

	@Override
	public void frameProcessed(int index, int count) {
		// index is 0-based
		renderProgress.setString(renderingText+" Frame "+(index+1)+"/"+count);
		if (count != renderProgress.getMaximum())
			renderProgress.setMaximum(count);
		renderProgress.setValue(index+1);
	}

	@Override
	public void finalizeStarted() {
		renderProgress.setIndeterminate(true);
		renderProgress.setString(finalizeText);
	}

	@Override
	public void finalizeProgress(int current, int total) {
		renderProgress.setString(finalizeText+" "+(current+1)+"/"+total);
		if (total != renderProgress.getMaximum())
			renderProgress.setMaximum(total);
		renderProgress.setValue(current+1);
	}

	@Override
	public void finalizeCompleted() {
		renderProgress.setString(renderDoneText);
		renderProgress.setIndeterminate(false);
		renderProgress.setValue(renderProgress.getMaximum());
	}

}
