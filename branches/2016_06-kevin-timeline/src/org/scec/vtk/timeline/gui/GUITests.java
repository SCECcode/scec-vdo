package org.scec.vtk.timeline.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.dom4j.Element;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.PluginInfo;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.timeline.KeyFrame;
import org.scec.vtk.timeline.RangeAnimationListener;
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
		
		timeline.addPlugin(new DummyPlugin(), new PluginActors());
		timeline.addPlugin(new DummyRangePlugin(), new PluginActors());
		timeline.addPlugin(new DummyStatefulPlugin(), new PluginActors());
		timeline.addKeyFrame(0, new VisibilityKeyFrame(0d, new PluginActors(), true));
		timeline.addKeyFrame(0, new VisibilityKeyFrame(5d, new PluginActors(), false));
		timeline.addKeyFrame(1, new RangeKeyFrame(0d, 3d, new DummyState()));
		timeline.addKeyFrame(2, new KeyFrame(1d, new DummyState()));
		timeline.addKeyFrame(2, new KeyFrame(4d, new DummyState()));
		
		final TimelinePanel tl = new TimelinePanel(timeline);
		panel.add(tl, BorderLayout.CENTER);
		
		boolean play = false;
		final long sleepTime = 100;
//		final long sleepTime = 1;
		Thread playThread = new Thread() {
			
			@Override
			public void run() {
				Stopwatch watch = Stopwatch.createStarted();
				while (true) {
					double time = watch.elapsed(TimeUnit.MILLISECONDS)/1000d;
					double curMax = tl.getCurrentTotalTime();
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
		
		frame.setSize(500, 300);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private static class DummyPlugin implements Plugin {

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
		
	}
	
	private static class DummyStatefulPlugin extends DummyPlugin implements StatefulPlugin {

		@Override
		public PluginState getState() {
			return new DummyState();
		}

		@Override
		public void setState(PluginState s) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private static class DummyRangePlugin extends DummyStatefulPlugin implements RangeAnimationListener {

		@Override
		public void rangeStarted() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void rangeEnded() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void rangeTimeChanged(double fractionalTime) {
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
