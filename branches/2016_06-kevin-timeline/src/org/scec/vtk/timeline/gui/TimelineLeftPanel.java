package org.scec.vtk.timeline.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opensha.commons.util.ExceptionUtils;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.utils.components.PauseButton;
import org.scec.vtk.plugins.utils.components.PlayButton;
import org.scec.vtk.plugins.utils.components.RenderButton;
import org.scec.vtk.plugins.utils.components.StopButton;
import org.scec.vtk.timeline.AnimationTimeListener;
import org.scec.vtk.timeline.CueAnimator;
import org.scec.vtk.timeline.CueAnimatorListener;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.timeline.TimelinePluginChangeListener;

import com.google.common.base.Preconditions;

public class TimelineLeftPanel extends JPanel implements TimelinePluginChangeListener, AnimationTimeListener {
	
	private Timeline timeline;
	private TimelinePanel tp;
	
	private TimePanel timePanel;
	private CameraPanel cameraPanel;
	private List<PluginPanel> pluginPanels;
	
	private double curTime;
	
	static final int panelWidth = 270;
	
	public TimelineLeftPanel(Timeline timeline, TimelinePanel tp) {
		setLayout(null); // manual placement
		this.timeline = timeline;
		this.tp = tp;
		
		updateSize();
		
		timeline.addTimelinePluginChangeListener(this);
		timeline.addAnimationTimeListener(this);
		
		this.timePanel = new TimePanel();
		add(timePanel);
		timePanel.setBounds(0, 0, panelWidth, TimelinePanel.headerHeight);
		animationTimeChanged(0d);
		
		this.cameraPanel = new CameraPanel();
		add(cameraPanel);
		cameraPanel.setBounds(0, TimelinePanel.headerHeight, panelWidth, TimelinePanel.cameraHeight);
		
		rebuildPluginPanels();
	}
	
	private void rebuildPluginPanels() {
		if (pluginPanels != null) {
			for (PluginPanel panel : pluginPanels)
				remove(panel);
		}
		// TODO synchronize on timeline?
		pluginPanels = new ArrayList<>();
		int maxY = TimelinePanel.cameraMaxY;
		for (int index=0; index<timeline.getNumPlugins(); index++) {
			PluginPanel p = new PluginPanel(timeline.getPluginAt(index));
			int pluginY = tp.yForPluginIndex(index);
			add(p);
			p.setBounds(0, pluginY, panelWidth, TimelinePanel.heightPerPlugin);
			maxY = pluginY + TimelinePanel.heightPerPlugin;
		}
		updateSize();
	}
	
	void updateSize() {
		int height = TimelinePanel.cameraMaxY + timeline.getNumPlugins()*TimelinePanel.heightPerPlugin;
		Dimension size = new Dimension(panelWidth, height);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		revalidate();
	}

	@Override
	public void timelinePluginsChanged() {
		rebuildPluginPanels();
	}

	@Override
	public void animationTimeChanged(double curTime) {
		this.curTime = curTime;
		String label = TimelinePanel.timeMinorLabelFormat.format(curTime);
		timePanel.timeLabel.setText(" "+label);
		timePanel.timeLabel.repaint();
	}
	
	@Override
	public void animationBoundsChanged(double maxTime) {}
	
	private class TimePanel extends JPanel implements ActionListener, CueAnimatorListener {
		
		private PlayButton playButton;
		private PauseButton pauseButton;
		private StopButton stopButton;
		private RenderButton renderButton;
		private JLabel timeLabel;
		
		private Dimension iconButtonSize = new Dimension(25, 25);
		private Dimension textButtonSize = new Dimension(80, 25);
		
		private CueAnimator animator;
		
		public TimePanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			renderButton = new RenderButton(this, "Render");
			add(renderButton);
			setButtonSize(renderButton, iconButtonSize);
			
			playButton = new PlayButton(this, "Play Animation");
			add(playButton);
			setButtonSize(playButton, iconButtonSize);
			
			pauseButton = new PauseButton(this, "Pause Animation");
			add(pauseButton);
			setButtonSize(pauseButton, iconButtonSize);
			
			stopButton = new StopButton(this, "Stop Animation");
			add(stopButton);
			setButtonSize(stopButton, iconButtonSize);
			
			timeLabel = new JLabel();
			add(timeLabel);
			
			setButtonsEnabled();
		}
		
		private void setButtonSize(JButton button, Dimension size) {
			button.setPreferredSize(size);
			button.setSize(size);
			button.setMaximumSize(size);
		}
		
		private void setButtonsEnabled() {
			if (animator == null) {
				// not playing, not rendering
				playButton.setEnabled(true);
				renderButton.setEnabled(true);
				pauseButton.setEnabled(false);
				stopButton.setEnabled(true);
			} else {
				// playing or rendering
				if (animator.isRendering()) {
					playButton.setEnabled(false);
					renderButton.setEnabled(!animator.isScenePlaying());
				} else {
					playButton.setEnabled(!animator.isScenePlaying());
					renderButton.setEnabled(false);
				}
				pauseButton.setEnabled(animator.isScenePlaying());
				stopButton.setEnabled(true);
			}
		}
		
		private void setButtonsEnabledEDT() {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					setButtonsEnabled();
				}
			});
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == renderButton) {
				if (animator != null) {
					Preconditions.checkState(animator.isRendering());
					animator.render();
				} else {
					animator = new CueAnimator(timeline, this);
					animator.render();
				}
			} else if (e.getSource() == playButton) {
				if (animator == null)
					animator = new CueAnimator(timeline, this);
				animator.play(curTime);
			} else if (e.getSource() == pauseButton) {
				Preconditions.checkNotNull(animator);
				animator.pause();
				runWhenStopped(new Runnable() {
					
					@Override
					public void run() {
						setButtonsEnabled();
					}
				});
			} else if (e.getSource() == stopButton) {
				if (animator != null) {
					animator.pause();
					runWhenStopped(new Runnable() {
						
						@Override
						public void run() {
							animator = null;
							timeline.activateTime(0d);
							setButtonsEnabled();
						}
					});
				} else {
					timeline.activateTime(0d);
					setButtonsEnabled();
				}
				
			}
		}
		
		private void runWhenStopped(final Runnable run) {
			new Thread() {
				public void run() {
					while (animator != null && animator.isScenePlaying()) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {}
					}
					try {
						SwingUtilities.invokeAndWait(run);
					} catch (Exception e) {
						ExceptionUtils.throwAsRuntimeException(e);
					}
				}
			}.start();
		}

		@Override
		public void animationStarted(boolean rendering) {
			System.out.println("Started");
			setButtonsEnabledEDT();
		}

		@Override
		public void animationPaused(boolean rendering) {
			System.out.println("Paused");
			setButtonsEnabledEDT();
		}

		@Override
		public void animationFinished(boolean rendering) {
			System.out.println("Finished");
			animator = null;
			setButtonsEnabledEDT();
		}
		
	}
	
	private class CameraPanel extends JPanel {
		
		public CameraPanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			JLabel label = new JLabel("Camera");
			add(label);
			
			// TODO keyframe buttons
		}
		
	}
	
	private class PluginPanel extends JPanel {
		
		public PluginPanel(Plugin plugin) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			JLabel label = new JLabel(plugin.getMetadata().getName());
			add(label);
			
			// TODO keyframe buttons
		}
		
	}

}
