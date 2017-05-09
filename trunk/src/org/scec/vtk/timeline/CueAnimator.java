package org.scec.vtk.timeline;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.media.MediaLocator;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.tools.JpegImagesToMovie;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import vtk.vtkAnimationCue;
import vtk.vtkAnimationScene;

public class CueAnimator {
	
	private static final boolean D = false;
	
	private Timeline timeline;
	private vtkAnimationScene scene;
	private vtkAnimationCue cue;
	
	private CueAnimatorListener listener;
	
	private boolean rendering;
	private int renderWidth;
	private int renderHeight;
	private File outputFile;
	
	// rendered frames which have not yet been processed
	private Deque<BufferedImage> framesDeque;
	
	private Thread playThread;
	
	// static for persistence of current directory
	private static JFileChooser chooser = new JFileChooser();
	
	public CueAnimator(Timeline timeline, CueAnimatorListener listener) {
		this.timeline = timeline;
		this.listener = listener;
		
		scene = new vtkAnimationScene();
		scene.SetStartTime(0d);
		scene.SetEndTime(timeline.getMaxTime());
		
		cue = new vtkAnimationCue();
		cue.SetStartTime(0);
		cue.SetEndTime(timeline.getMaxTime());
		
		cue.AddObserver("StartAnimationCueEvent", this, "startCue");
		cue.AddObserver("EndAnimationCueEvent", this, "endCue");
		cue.AddObserver("AnimationCueTickEvent", this, "tickCue");
		
		scene.AddCue(cue);
	}
	
	public void render() {
		if (framesDeque == null || framesDeque.isEmpty()) {
			synchronized (chooser) {
				int ret = chooser.showSaveDialog(null);
				if (ret == JFileChooser.APPROVE_OPTION) {
					outputFile = chooser.getSelectedFile();
					if (!outputFile.getName().toLowerCase().endsWith(".mov"))
						outputFile = new File(outputFile.getParentFile(), outputFile.getName()+".mov");
					
					rendering = true;
					scene.SetModeToSequence();
					scene.SetFrameRate(timeline.getFamerate());
					
					JComponent component = MainGUI.getRenderWindow().getComponent();
					renderWidth =  component.getWidth();
					renderHeight = component.getHeight();
					
					framesDeque = new ArrayDeque<>();
					
					playThread = new PlayThread();
					playThread.start();
				}
			}
		} else {
			Preconditions.checkState(rendering);
			Preconditions.checkNotNull(outputFile);
			// resume
			playThread = new PlayThread();
			playThread.start();
		}
		
	}
	
	public boolean isRendering() {
		return rendering;
	}
	
	public boolean isScenePlaying() {
		// either render or play
		return scene.IsInPlay() == 1;
	}
	
	public boolean isThreadAlive() {
		return playThread != null && playThread.isAlive();
	}
	
	public void play(double time) {
		rendering = false;
		// if within 1% of end start over
		// this happens when playing as it skips frames as needed while playing live
		if (DataUtils.getPercentDiff(time, scene.GetEndTime()) < 1d)
			time = 0d;
		scene.SetModeToRealTime();
		scene.SetAnimationTime(time);
		
		playThread = new PlayThread();
		playThread.start();
	}
	
	private class PlayThread extends Thread {
		
		public void run(){
			//Info.getMainGUI().getRenderWindow().GetRenderer().SetActiveCamera(cb.camold);
			if(scene.IsInPlay()==1)
				scene.Stop();
			scene.Play();
			scene.Stop();
			System.out.println("Thread done");
			listener.animationPaused(isRendering());
		}
		
	}
	
	public void pause() {
		if (isScenePlaying())
			scene.Stop();
	}
	
	public void startCue() {
		if (D) System.out.println("startCue");
		listener.animationStarted(isRendering());
	}
	
	private class TickPlayRunnable implements Runnable {
		private double animTime;
		public TickPlayRunnable(double animTime) {
			this.animTime = animTime;
		}
		@Override
		public void run() {
			timeline.activateTime(animTime);
		}
	}
	
	private class TickRenderRunnable implements Runnable {
		private double animTime;
		public TickRenderRunnable(double animTime) {
			this.animTime = animTime;
		}
		@Override
		public void run() {
//			vtkWindowToImageFilter vtkPixelData = new vtkWindowToImageFilter();
//			vtkPixelData.SetInput(MainGUI.getRenderWindow().getRenderWindow());
//			vtkPixelData.ReadFrontBufferOff();
			timeline.activateTime(animTime);
			JComponent component = MainGUI.getRenderWindow().getComponent();
			int width =  component.getWidth();
			int height = component.getHeight();
			Preconditions.checkState(width == renderWidth && height == renderHeight,
					"Render canvas size changed during render");
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			// call the Component's paint method, using
			// the Graphics object of the image.
			if (D) System.out.println("Render tick, painting from JComponent");
			component.paint( image.getGraphics() );
			synchronized (framesDeque) {
				framesDeque.push(image);
			}
		}
	}
	
	public void tickCue() {
		final double animTime = cue.GetAnimationTime();
		if (D) System.out.println("tickCue: cue.GetAnimationTime()="+animTime
					+", scene.GetAnimationTime()="+scene.GetAnimationTime()+", isInPlay="+scene.IsInPlay());
		Runnable run;
		if (isRendering())
			run = new TickRenderRunnable(animTime);
		else
			run = new TickPlayRunnable(animTime);
		if (SwingUtilities.isEventDispatchThread()) {
			run.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(run);
			} catch (Exception e) {
				ExceptionUtils.asRuntimeException(e);
			}
		}
	}
	
	private class ImageWriteCallable implements Runnable {
		
		private BufferedImage image;
		private File outputFile;
		private CalcProgressBar progressBar;
		
		public ImageWriteCallable(BufferedImage image, File outputFile, CalcProgressBar progressBar) {
			this.image = image;
			this.outputFile = outputFile;
			this.progressBar = progressBar;
		}

		@Override
		public void run() {
			try{
				ImageIO.write(image, "jpg", outputFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			synchronized (written) {
				written++;
				if (progressBar != null) {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							progressBar.updateProgress(written, number, "Writing frames");
//							progressBar.setValue(frame);
						}
					});
				}
			}
		}
		
	}
	
	private Integer written = 0;
	private Integer number = 0;
	
	public void endCue() {
		if (D) System.out.println("endCue");
		if (rendering) {
			written = 0;
			number = framesDeque.size();
			File tempDir = Files.createTempDir();
			System.out.println("Writing "+number+" frames to "+tempDir.getAbsolutePath());
			final CalcProgressBar progressBar = new CalcProgressBar("Rendering", "Writing frames"); // TODO
			List<File> imageFiles = Lists.newArrayList();
			ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			List<Future<?>> futures = Lists.newArrayList();
			int count = 0;
			while (!framesDeque.isEmpty()) {
//				if (progressBar != null) {
//					final int frame = i;
//					SwingUtilities.invokeLater(new Runnable() {
//						
//						@Override
//						public void run() {
//							progressBar.updateProgress(frame, renderedFrames.size(), "Writing frames");
////							progressBar.setValue(frame);
//						}
//					});
//				}
				// TODO just keep images in memory, never write to disk
				File file = new File(tempDir, "Capture" + count++ + ".jpg");				
				BufferedImage image = framesDeque.removeLast();
				imageFiles.add(file);
				futures.add(executor.submit(new ImageWriteCallable(image, file, progressBar)));
			}
			framesDeque = null;
			executor.shutdown();
			// wait on all tasks
			for (Future<?> future : futures)
				try {
					future.get();
				} catch (Exception e1) {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							progressBar.setIndeterminate(false);
							progressBar.setVisible(false);
							progressBar.dispose();
						}
					});
					ExceptionUtils.throwAsRuntimeException(e1);
				}
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					progressBar.setIndeterminate(true);
//					progressBar.setString("Writing Movie File");
//					progressBar.setStringPainted(true);
				}
			});
			JpegImagesToMovie jpeg = new JpegImagesToMovie();
			MediaLocator m;
			try {
				m = new MediaLocator(outputFile.toURI().toURL());
			} catch (MalformedURLException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
			jpeg.doIt(renderWidth, renderHeight, (float)scene.GetFrameRate(), imageFiles, m);
			System.out.println("*** Finished Generating jpgs " );
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					progressBar.setIndeterminate(false);
					progressBar.setVisible(false);
					progressBar.dispose();
				}
			});
			FileUtils.deleteRecursive(tempDir);
		}
		listener.animationFinished(isRendering());
	}

}
