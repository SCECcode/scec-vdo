package org.scec.vtk.timeline.gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.dom4j.Element;
import org.scec.vtk.plugins.AnimatableChangeListener;
import org.scec.vtk.plugins.AnimatablePlugin;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.timeline.KeyFrame;
import org.scec.vtk.timeline.RangeKeyFrame;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.timeline.VisibilityKeyFrame;

import com.google.common.base.Stopwatch;

public class GUITests {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		final Timeline timeline = new Timeline();
		timeline.setLive(false);
		
		DummyPlugin normal = new DummyPlugin("Normal Plugin");
		DummyAnimatablePlugin animatable = new DummyAnimatablePlugin("Animatable Plugin");
		DummyStatefulPlugin stateful = new DummyStatefulPlugin("Stateful Plugin");
		
		timeline.addPlugin(normal, new PluginActors());
		timeline.addPlugin(animatable, new PluginActors());
		timeline.addPlugin(stateful, new PluginActors());
		timeline.addKeyFrame(0, new VisibilityKeyFrame(0d, new PluginActors(), true));
		timeline.addKeyFrame(0, new VisibilityKeyFrame(5d, new PluginActors(), false));
		timeline.addKeyFrame(1, new RangeKeyFrame(0d, 3d, new DummyState(), animatable));
		timeline.addKeyFrame(2, new KeyFrame(1d, new DummyState()));
		timeline.addKeyFrame(2, new KeyFrame(4d, new DummyState()));
		
		final TimelineGUI gui = new TimelineGUI(timeline);
		panel.add(gui, BorderLayout.CENTER);
		
		boolean play = false;
		final long sleepTime = 100;
//		final long sleepTime = 1;
		Thread playThread = new Thread() {
			
			@Override
			public void run() {
				Stopwatch watch = Stopwatch.createStarted();
				while (true) {
					double time = watch.elapsed(TimeUnit.MILLISECONDS)/1000d;
					double curMax = timeline.getMaxTime();
//					System.out.println("curMax="+curMax+" s");
					if (curMax > 0) {
						double fract = time/curMax;
						time = curMax*(fract - Math.floor(fract));
						
//						System.out.println("Setting with time="+time);
						
						System.out.println("Rendering "+time);
						timeline.activateTime(time);
					}
					
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		};
		if (play)
			playThread.start();
		
		frame.setContentPane(panel);
		
		frame.setSize(800, 300);
		frame.setVisible(true);
		gui.updateSize();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private static class DummyPlugin implements Plugin {
		
		private PluginInfo metadata;
		
		public DummyPlugin(String name) {
			metadata = new PluginInfo();
			metadata.setName(name);
		}

		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void initialize(PluginInfo metadata, PluginActors pluginActors) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void load() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void activate() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void passivate() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void unload() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public PluginInfo getMetadata() {
			return metadata;
		}
		
	}
	
	private static class DummyStatefulPlugin extends DummyPlugin implements StatefulPlugin {

		public DummyStatefulPlugin(String name) {
			super(name);
		}

		@Override
		public PluginState getState() {
			return new DummyState();
		}

	
		
	}
	
	private static class DummyAnimatablePlugin extends DummyStatefulPlugin implements AnimatablePlugin {

		public DummyAnimatablePlugin(String name) {
			super(name);
		}

		@Override
		public void animationStarted() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void animationEnded() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void animationTimeChanged(double fractionalTime) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isAnimatable() {
			return true;
		}

		@Override
		public void addAnimatableChangeListener(AnimatableChangeListener l) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeAnimatableChangeListener(AnimatableChangeListener l) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private static class DummyState implements PluginState {

		@Override
		public void load() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void toXML(Element stateEl) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void fromXML(Element stateEl) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public PluginState deepCopy() {
			return this;
		}
		
	}

}
