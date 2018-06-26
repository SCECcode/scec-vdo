package org.scec.vtk.timeline.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.scec.vtk.timeline.AnimationTimeListener;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.timeline.render.RenderStatusListener;
import org.scec.vtk.timeline.render.Renderer;

public class TimelineBottomPanel extends JPanel implements AnimationTimeListener, RenderStatusListener {
	
	private static final int play_max = 100;
	private static final int play_min = 0;
	private JProgressBar playProgress;
	private JProgressBar renderProgress;
	
	private static final String playbackText = "Playback";
	private static final String renderingText = "Rendering";
	private static final String finalizeText = "Finalizing Render";
	private static final String renderDoneText = "Render Complete";
	private boolean rendering = false;
	
	
	private double maxTime;
	
	public TimelineBottomPanel(Timeline timeline) {
		setLayout(new GridLayout(1, 2));
		
		timeline.addAnimationTimeListener(this);
		for (Renderer r : timeline.getAvailableRenderers())
			r.setRenderStatusListener(this);
		
		maxTime = timeline.getMaxTime();
		
		playProgress = buildProgress(play_min, play_max, playbackText, Color.BLUE);
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
	
	private static DecimalFormat timeDF = new DecimalFormat("0.0 s");

	@Override
	public void animationTimeChanged(final double curTime) {
		runOnEDT(new Runnable() {
			
			@Override
			public void run() {
				if (!rendering) {
					renderProgress.setString("");
					renderProgress.setValue(0);
				}
				double fract = curTime/maxTime;
				int p = (int)Math.round(fract*play_max);
				playProgress.setValue(p);
				playProgress.setString(playbackText+": "+timeDF.format(curTime));
			}
		});
	}

	@Override
	public void animationBoundsChanged(double maxTime) {
		this.maxTime = maxTime;
	}
	
	private static DecimalFormat percentDF = new DecimalFormat("0 %");

	@Override
	public void frameProcessed(final int index, final int count) {
		runOnEDT(new Runnable() {
			
			@Override
			public void run() {
				rendering = true;
				// index is 0-based
				int myIndex = index+1;
				double fract = (double)myIndex/(double)count;
				renderProgress.setString(renderingText+": Frame "+myIndex+"/"+count+" ("+percentDF.format(fract)+")");
				if (count != renderProgress.getMaximum())
					renderProgress.setMaximum(count);
				renderProgress.setValue(myIndex);
			}
		});
	}

	@Override
	public void finalizeStarted() {
		runOnEDT(new Runnable() {
			
			@Override
			public void run() {
				rendering = true;
				renderProgress.setIndeterminate(true);
				renderProgress.setString(finalizeText);
			}
		});
	}

	@Override
	public void finalizeProgress(final int current, final int total) {
		runOnEDT(new Runnable() {

			@Override
			public void run() {
				rendering = true;
				double fract = (double)current/(double)total;
				renderProgress.setString(finalizeText+": "+(current+1)+"/"+total+" ("+percentDF.format(fract)+")");
				if (total != renderProgress.getMaximum())
					renderProgress.setMaximum(total);
				renderProgress.setValue(current+1);
			}
		});
	}

	@Override
	public void finalizeCompleted() {
		runOnEDT(new Runnable() {
			
			@Override
			public void run() {
				rendering = false;
				renderProgress.setString(renderDoneText);
				renderProgress.setIndeterminate(false);
				renderProgress.setValue(renderProgress.getMaximum());
			}
		});
		
	}
	
	private void runOnEDT(Runnable run) {
		if (SwingUtilities.isEventDispatchThread())
			run.run();
		else
			try {
//				SwingUtilities.invokeAndWait(run); // can cause deadlock
				SwingUtilities.invokeLater(run);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}
