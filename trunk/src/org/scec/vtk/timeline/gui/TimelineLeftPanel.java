package org.scec.vtk.timeline.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.opensha.commons.util.ExceptionUtils;
import org.scec.vtk.plugins.AnimatableChangeListener;
import org.scec.vtk.plugins.AnimatablePlugin;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.plugins.utils.components.FreezeButton;
import org.scec.vtk.plugins.utils.components.PauseButton;
import org.scec.vtk.plugins.utils.components.PlayButton;
import org.scec.vtk.plugins.utils.components.RemoveKeyFramesButton;
import org.scec.vtk.plugins.utils.components.RenderButton;
import org.scec.vtk.plugins.utils.components.ShowButton;
import org.scec.vtk.plugins.utils.components.StopButton;
import org.scec.vtk.timeline.AnimationTimeListener;
import org.scec.vtk.timeline.CueAnimator;
import org.scec.vtk.timeline.CueAnimatorListener;
import org.scec.vtk.timeline.KeyFrame;
import org.scec.vtk.timeline.RangeKeyFrame;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.timeline.TimelinePluginChangeListener;
import org.scec.vtk.timeline.VisibilityKeyFrame;
import org.scec.vtk.timeline.camera.CameraAnimator.SplineType;
import org.scec.vtk.timeline.render.ImageSequenceRenderer;
import org.scec.vtk.timeline.render.Renderer;
import org.scec.vtk.timeline.render.GIFSequence;

import com.google.common.base.Preconditions;

class TimelineLeftPanel extends JPanel implements TimelinePluginChangeListener, AnimationTimeListener {
	
	private Timeline timeline;
	private TimelinePanel tp;
	
	private TimePanel timePanel;
	private CameraPanel cameraPanel;
	private Map<Plugin, PluginPanel> pluginPanels;
	
	private double curTime;
	public double maxTime;
	public double fps;
	
	static final int panelWidth = 270;
	
	TimelineLeftPanel(Timeline timeline, TimelinePanel tp) {
		setLayout(null); // manual placement
		this.timeline = timeline;
		this.tp = tp;
		
		updateSize();
		
		timeline.addTimelinePluginChangeListener(this);
		timeline.addAnimationTimeListener(this);
		
		this.timePanel = new TimePanel();
		animationTimeChanged(0d);
		
		this.cameraPanel = new CameraPanel();
		
		rebuildPluginPanels();
	}
	
	private void rebuildPluginPanels() {
		removeAll();
		revalidate();
		add(timePanel);
		timePanel.setBounds(0, 0, panelWidth, TimelinePanel.headerHeight);
		add(cameraPanel);
		cameraPanel.setBounds(0, TimelinePanel.headerHeight, panelWidth, TimelinePanel.cameraHeight);
		if (pluginPanels == null) {
			pluginPanels = new HashMap<>();
		}
		// TODO synchronize on timeline?
		HashSet<PluginPanel> unusedPanels = new HashSet<TimelineLeftPanel.PluginPanel>(pluginPanels.values());
		for (int index=0; index<timeline.getNumPlugins(); index++) {
			Plugin plugin = timeline.getPluginAt(index);
			PluginPanel p = pluginPanels.get(plugin);
			if (p == null) {
				// new plugin
				p = new PluginPanel(timeline.getPluginAt(index));
			} else {
				// existing plugin
				unusedPanels.remove(p);
			}
			int pluginY = tp.yForPluginIndex(index);
			add(p);
			p.setBounds(0, pluginY, panelWidth, TimelinePanel.heightPerPlugin);
//			System.out.println("Adding plugin "+plugin.getMetadata().getName()+" at "+pluginY);
		}
		for (PluginPanel unused : unusedPanels) {
			unused.removeListeners();
			Preconditions.checkNotNull(pluginPanels.remove(unused.plugin));
		}
		updateSize();
		repaint();
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
		timePanel.timeLabel.setText("  "+label);
		timePanel.timeLabel.repaint();
	}
	
	@Override
	public void animationBoundsChanged(double maxTime) {}
	
	private static void setButtonSize(JButton button, Dimension size) {
		button.setPreferredSize(size);
		button.setSize(size);
		button.setMaximumSize(size);
		button.setMinimumSize(size);
	}
	
	private static Dimension iconButtonSize = new Dimension(25, 25);
	private static Dimension keyButtonSize = new Dimension(18, 25);
	private static Dimension settingsButtonSize = new Dimension(90, 25);
	
	private class TimePanel extends JPanel implements ActionListener, CueAnimatorListener {
		
		private PlayButton playButton;
		private PauseButton pauseButton;
		private StopButton stopButton;
		private RenderButton renderButton;
		private JButton settingsButton;
		private TimelineSettingsPanel settingsPanel;
		private JLabel timeLabel;
		
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
			
			settingsButton = new JButton("Settings");
			settingsButton.addActionListener(this);
			add(settingsButton);
			setButtonSize(settingsButton, settingsButtonSize);
			
			timeLabel = new JLabel();
			add(timeLabel);
			
			final TimePanel parent = this;
			
			timeLabel.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						// show go to time dialog
						String timeStr = JOptionPane.showInputDialog(parent, "Go to time",
								TimelinePanel.timeMinorLabelFormat.format(curTime).replaceAll("s", "")+"");
						if (timeStr != null) {
							try {
								double time = Double.parseDouble(timeStr);
								timeline.activateTime(time);
								
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(parent, "Could not parse time: "+timeStr,
										"Error Parsing Time", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
				
			});
			
			setButtonsEnabled();
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
				stopButton.setEnabled(true); //changed to false.. originally true
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
				if (animator != null)
					Preconditions.checkState(animator.isRendering());
				else
					animator = new CueAnimator(timeline, this);
				if (!animator.render())
					// canceled
					animator = null;
			} else if (e.getSource() == playButton) {
				if (animator == null)
					animator = new CueAnimator(timeline, this);
				animator.play(curTime);
				pauseButton.requestFocus();
			} else if (e.getSource() == pauseButton) {
				Preconditions.checkNotNull(animator);
				animator.pause();
				runWhenStopped(new Runnable() {
					
					@Override
					public void run() {
						setButtonsEnabled();
					}
				});
				playButton.requestFocus();
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
				playButton.requestFocus();
				
			} else if (e.getSource() == settingsButton) {
				if (settingsPanel == null)
					settingsPanel = new TimelineSettingsPanel();
				else
					settingsPanel.updateFromTimeline();
				int ret = JOptionPane.showConfirmDialog(
						this, settingsPanel, "Render settings", JOptionPane.OK_CANCEL_OPTION);
				if (ret == JOptionPane.OK_OPTION) {
					try {
						settingsPanel.updateTimeline();
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(this, e1.getMessage(), "Error Updating Settings",
								JOptionPane.ERROR_MESSAGE);
					}
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
			resetState();
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
			resetState();
			
		}
		
		public void resetState() {
			for (Plugin plugin: timeline.plugins) {
				timeline.setDisplayed(plugin, true);
			}
		}
		
	}
	
	private class TimelineSettingsPanel extends JPanel implements ItemListener {
		
		private JTextField timeField;
		private JTextField frameRateField;
		private JComboBox<SplineType> splineBox;
		private JComboBox<Renderer> renderBox;
		
		private JPanel renderSettingsPanel;
		
		private ViewerSizePanel sizePanel;
		
		public TimelineSettingsPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			JLabel timeLabel = new JLabel("Total time (s):  ");
			timeField = new JTextField(10);

			addRow(timeLabel, timeField);
			
			JLabel frameRateLabel = new JLabel("Frames Per Second:  ");
			frameRateField = new JTextField(10);
			addRow(frameRateLabel, frameRateField);
			
			JLabel splineLabel = new JLabel("Camera Spline Type:  ");
			splineBox = new JComboBox<SplineType>(SplineType.values());
			addRow(splineLabel, splineBox);
			
			add(new JSeparator(JSeparator.HORIZONTAL));
			
			JLabel renderLabel = new JLabel("Renderer:  ");
			renderBox = new JComboBox<Renderer>();
			renderBox.addItemListener(this);
			addRow(renderLabel, renderBox);
			renderSettingsPanel = new JPanel();
			renderSettingsPanel.setLayout(new CardLayout());
			renderSettingsPanel.setMinimumSize(new Dimension(200, 30));
			for (Renderer r : timeline.getAvailableRenderers()) {
				JComponent renderPanel = r.getSettingsComponent();
				if (renderPanel == null) {
					renderPanel = new JPanel();
					renderPanel.setPreferredSize(new Dimension(200, 30));
					renderPanel.setMinimumSize(renderPanel.getPreferredSize());
					renderPanel.setSize(renderPanel.getPreferredSize());
				}
				renderSettingsPanel.add(renderPanel, r.getName());
			}
			add(renderSettingsPanel);
			
			add(new JSeparator(JSeparator.HORIZONTAL));
			
			sizePanel = new ViewerSizePanel(timeline);
			add(sizePanel);
			
			updateFromTimeline();
		}
		
		public void updateFromTimeline() {
			timeField.setText(TimelinePanel.timeMinorLabelFormat.format(timeline.getMaxTime()).replaceAll("s", ""));
			frameRateField.setText((float)timeline.getFamerate()+"");
			splineBox.setSelectedItem(timeline.getCameraSplineType());
			if (renderBox.getItemCount() == 0)
				// renderers don't change, only do this once
				renderBox.setModel(new DefaultComboBoxModel<>(timeline.getAvailableRenderers().toArray(new Renderer[0])));
			renderBox.setSelectedItem(timeline.getRenderer());
			updateRenderPanel();
			sizePanel.updateFromTimeline();
		}
		
		private void updateRenderPanel() {
//			System.out.println("Updating render panel");
			CardLayout cl = (CardLayout)renderSettingsPanel.getLayout();
			cl.show(renderSettingsPanel, renderBox.getItemAt(renderBox.getSelectedIndex()).getName());
			invalidate();
			validate();
		}
		
		public void updateTimeline() {
		    maxTime = Double.parseDouble(timeField.getText());
			System.out.println("maxTime: " + maxTime);

			Preconditions.checkState(maxTime >= TimelinePanel.minorTickInterval,
					"Max time must be >= "+TimelinePanel.minorTickInterval);
			fps = Double.parseDouble(frameRateField.getText());
			System.out.println("fps: " + fps);

			Preconditions.checkState(fps > 1, "Framerate must be >= 1 fps");
			SplineType type = (SplineType) splineBox.getSelectedItem();
			Preconditions.checkNotNull(type, "Null SplineType");
			
			timeline.setMaxTime(maxTime);
			timeline.setFramerate(fps);
			timeline.setCameraSplineType(type);
			timeline.setRenderer(renderBox.getItemAt(renderBox.getSelectedIndex()));
			
			sizePanel.updateTimeline();
		}
		
		private void addRow(Component... components) {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			for (Component component : components)
				panel.add(component);
			add(panel);
		}

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			updateRenderPanel();
		}
		
	}
	
	private class KeyFrameButton extends JButton {
		public KeyFrameButton(Color color, String toolTip) {
			Color disabledColor = KeyFrameLabel.saturate(color);
			setIcon(new KeyFrameLabel(color));
			setDisabledIcon(new KeyFrameLabel(disabledColor));
			setButtonSize(this, keyButtonSize);
			setToolTipText(toolTip);
		}
	}
	
	private class CameraPanel extends JPanel implements ActionListener {
		
		private KeyFrameButton camKeyButton = new KeyFrameButton(TimelinePanel.cameraKeyColor,
				"Create new camera position KeyFrame");
		private KeyFrameButton camPauseKeyButton = new KeyFrameButton(TimelinePanel.cameraKeyPauseColor,
				"Create new camera pause KeyFrame");
		private RemoveKeyFramesButton removeButton;
		
		public CameraPanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			camKeyButton.addActionListener(this);
			add(camKeyButton);
			
			camPauseKeyButton.addActionListener(this);
			add(camPauseKeyButton);
			
			removeButton = new RemoveKeyFramesButton(this, "Remove All KeyFrames");
			removeButton.setEnabled(true);
			setButtonSize(removeButton, iconButtonSize);
			add(removeButton);
			
			JLabel label = new JLabel("  Camera");
			add(label);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			double time = tp.getRoundedTime(curTime);
			if (e.getSource() == camKeyButton) {
				tp.addCameraKey(time, false);
			} else if (e.getSource() == camPauseKeyButton) {
				tp.addCameraKey(time, true);
			} else if (e.getSource() == removeButton) {
				tp.clearCameraKeys();
			}
		}
		
	}
	
	private class PluginPanel extends JPanel implements ActionListener, AnimatableChangeListener {
		
		private Plugin plugin;
		
		private ShowButton displayButton;
		private Icon displayedIcon;
		private Icon hiddenIcon;
		
		private FreezeButton freezeButton;
		private Icon frozenIcon;
		private Icon unfrozenIcon;
		
		private RemoveKeyFramesButton removeButton;
		
		private KeyFrameButton visiblityOnButton = new KeyFrameButton(TimelinePanel.visibilityKeyOnColor,
				"Create new visibility on KeyFrame");
		private KeyFrameButton visiblityOffButton = new KeyFrameButton(TimelinePanel.visibilityKeyOffColor,
				"Create new visibility off KeyFrame");
		private KeyFrameButton normalKeyButton = new KeyFrameButton(TimelinePanel.normalKeyColor,
				"Create normal KeyFrame, only enabled for stateful plugins");
		private KeyFrameButton rangeKeyButton = new KeyFrameButton(TimelinePanel.rangeKeyColor,
				"Create range KeyFrame, only enabled for animatable plugins that are currently configured "
				+ "to display an animation");
		
		public PluginPanel(Plugin plugin) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			this.plugin = plugin;
			
			displayButton = new ShowButton(this, "Show/Hide entire plugin");
			displayButton.setEnabled(true);
			displayedIcon = displayButton.getIcon();
			hiddenIcon = displayButton.getDisabledIcon();
			setButtonSize(displayButton, iconButtonSize);
			add(displayButton);
			
			freezeButton = new FreezeButton(this, "Freeze plugin to ignore KeyFrames until unfrozen");
			freezeButton.setEnabled(true);
			frozenIcon = freezeButton.getIcon();
			unfrozenIcon = freezeButton.getDisabledIcon();
			freezeButton.setIcon(unfrozenIcon);
			setButtonSize(freezeButton, iconButtonSize);
			add(freezeButton);
			
			removeButton = new RemoveKeyFramesButton(this, "Remove All KeyFrames");
			removeButton.setEnabled(true);
			setButtonSize(removeButton, iconButtonSize);
			add(removeButton);
			
			normalKeyButton.addActionListener(this);
			normalKeyButton.setEnabled(false);
			add(normalKeyButton);
			rangeKeyButton.addActionListener(this);
			rangeKeyButton.setEnabled(false);
			add(rangeKeyButton);
			if (plugin instanceof StatefulPlugin) {
				normalKeyButton.setEnabled(true);
				if (plugin instanceof AnimatablePlugin) {
					AnimatablePlugin animPlugin = (AnimatablePlugin)plugin;
					animPlugin.addAnimatableChangeListener(this);
					animatableChanged(animPlugin, animPlugin.isAnimatable());
				}
			}
			
			visiblityOnButton.addActionListener(this);
			add(visiblityOnButton);
			visiblityOffButton.addActionListener(this);
			add(visiblityOffButton);
			
			JLabel label = new JLabel("  "+plugin.getMetadata().getName());
			label.setToolTipText(plugin.getMetadata().getName());
			int buttonsWidth = 4*keyButtonSize.width;
			Dimension labelSize = new Dimension(panelWidth - (buttonsWidth + 5), TimelinePanel.heightPerPlugin);
			label.setPreferredSize(labelSize);
			label.setMaximumSize(labelSize);
			label.setMinimumSize(labelSize);
			add(label);
		}

		@Override
		public synchronized void actionPerformed(ActionEvent e) {
			double time = tp.getRoundedTime(curTime);
			if (e.getSource() == visiblityOnButton) {
				tp.addPluginKey(plugin, new VisibilityKeyFrame(time, timeline.getActorsForPlugin(plugin), true));
			} else if (e.getSource() == visiblityOffButton) {
				tp.addPluginKey(plugin, new VisibilityKeyFrame(time, timeline.getActorsForPlugin(plugin), false));
			} else if (e.getSource() == normalKeyButton) {
				Preconditions.checkState(plugin instanceof StatefulPlugin);
				StatefulPlugin statePlugin = (StatefulPlugin)plugin;
				PluginState state = statePlugin.getState().deepCopy();
				tp.addPluginKey(plugin, new KeyFrame(time, state));
			} else if (e.getSource() == rangeKeyButton) {
				Preconditions.checkState(TimelinePanel.isAnimatable(plugin));
				AnimatablePlugin animPlugin = (AnimatablePlugin)plugin;
				PluginState state = animPlugin.getState().deepCopy();
				double duration = tp.showDurationDialog(false);
				if (duration > 0)
					tp.addPluginKey(plugin, new RangeKeyFrame(time, time+duration, state, animPlugin));
			} else if (e.getSource() == displayButton) {
				// toggle
				boolean displayed = !timeline.isDisplayed(plugin);
				if (displayed)
					displayButton.setIcon(displayedIcon);
				else
					displayButton.setIcon(hiddenIcon);
				timeline.setDisplayed(plugin, displayed);
				tp.repaint();
			} else if (e.getSource() == freezeButton) {
				// toggle
				boolean frozen = !timeline.isFrozen(plugin);
				if (frozen)
					freezeButton.setIcon(frozenIcon);
				else
					freezeButton.setIcon(unfrozenIcon);
				timeline.setFrozen(plugin, frozen);
				tp.repaint();
			} else if (e.getSource() == removeButton) {
				tp.clearPluginKeys(plugin);
			}
		}

		@Override
		public void animatableChanged(AnimatablePlugin plugin,
				boolean isAnimatable) {
			Preconditions.checkState(plugin == this.plugin);
			rangeKeyButton.setEnabled(isAnimatable);
		}
		
		public void removeListeners() {
			if (plugin instanceof AnimatablePlugin)
				((AnimatablePlugin)plugin).removeAnimatableChangeListener(this);
		}
		
	}

}
