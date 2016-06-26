package org.scec.vtk.timeline;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.media.MediaLocator;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.tools.JpegImagesToMovie;

import vtk.vtkAnimationCue;
import vtk.vtkAnimationScene;
import vtk.vtkUnsignedCharArray;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class CueAnimator {
	
	private Timeline timeline;
	private vtkAnimationScene scene;
	private vtkAnimationCue cue;
	
	private CueAnimatorListener listener;
	
	private boolean rendering;
	private int renderWidth;
	private int renderHeight;
	private File outputFile;
	private List<vtkUnsignedCharArray> renderedFrames;
	
	private Thread playThread;
	
	// static for persistance of current directory
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
		if (renderedFrames == null || renderedFrames.isEmpty()) {
			synchronized (chooser) {
				int ret = chooser.showSaveDialog(null);
				if (ret == JFileChooser.APPROVE_OPTION) {
					outputFile = chooser.getSelectedFile();
					if (!outputFile.getName().toLowerCase().endsWith(".mov"))
						outputFile = new File(outputFile.getParentFile(), outputFile.getName()+".mov");
					
					rendering = true;
					scene.SetModeToSequence();
					scene.SetFrameRate(30); // TODO make selectable
					
					int[] renderSize = MainGUI.getRenderWindow().GetRenderWindow().GetSize();
					renderWidth =  renderSize[0];
					renderHeight = renderSize[1];
					
					renderedFrames = new ArrayList<>();
					
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
		if (time == scene.GetEndTime())
			time = 0d; // TODO this isn't working
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
			timeline.activateTime(animTime);
			int[] renderSize = MainGUI.getRenderWindow().GetRenderWindow().GetSize();
			int width =  renderSize[0];
			int height = renderSize[1];
			Preconditions.checkState(width == renderWidth && height == renderHeight,
					"Render canvas size changed during render");
			vtkUnsignedCharArray vtkPixelData = new vtkUnsignedCharArray();
			MainGUI.getRenderWindow().GetRenderWindow().GetPixelData(0, 0, width, height,
					1, vtkPixelData);
			renderedFrames.add(vtkPixelData);
		}
	}
	
	public void tickCue() {
		final double animTime = scene.GetAnimationTime();
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
		
		private vtkUnsignedCharArray vtkPixelData;
		private File outputFile;
		private CalcProgressBar progressBar;
		
		public ImageWriteCallable(vtkUnsignedCharArray vtkPixelData, File outputFile, CalcProgressBar progressBar) {
			this.vtkPixelData = vtkPixelData;
			this.outputFile = outputFile;
			this.progressBar = progressBar;
		}

		@Override
		public void run() {
			BufferedImage bufImage = new BufferedImage(renderWidth, renderHeight,
					BufferedImage.TYPE_INT_RGB);
			int[] rgbArray = new int[(renderWidth) * (renderHeight)];
			int index, r, g, b;
			double[] rgbFloat;
			// bad performance because one has to get the values out of the vtk find a workaround jpeg writer
			// data structure tuple by tuple (instead of one "copyToArray") ...
			for (int y = 0; y < renderHeight; y++) {
				for (int x = 0; x < renderWidth; x++) {
					index = ((y * (renderWidth + 1)) + x);
					rgbFloat = vtkPixelData.GetTuple3(index);
					r = (int) rgbFloat[0];
					g = (int) rgbFloat[1];
					b = (int) rgbFloat[2];
					// vtk window origin: bottom left, Java image origin: top left
					rgbArray[((renderHeight -1 - y) * (renderWidth)) + x] =
							((r << 16) + (g << 8) + b);
				}
			}
			bufImage.setRGB(0, 0, renderWidth, renderHeight, rgbArray, 0, renderWidth);
			try {
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
//				ImageIO.write(bufImage, "jpg", out);
				JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(out);
				JPEGEncodeParam encParam = enc.getDefaultJPEGEncodeParam(bufImage);
				encParam.setQuality(1f, true);
				enc.setJPEGEncodeParam(encParam);
				enc.encode(bufImage);
				out.close();
			} catch (IOException e) {
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
		if (rendering) {
			written = 0;
			number = renderedFrames.size();
			File tempDir = Files.createTempDir();
			System.out.println("Writing "+renderedFrames.size()+" frames to "+tempDir.getAbsolutePath());
			final CalcProgressBar progressBar = new CalcProgressBar("Rendering", "Writing frames"); // TODO
			List<File> imageFiles = Lists.newArrayList();
			ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			List<Future<?>> futures = Lists.newArrayList();
			for(int i =0;i<renderedFrames.size();i++){
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
				File file = new File(tempDir, "Capture" + i + ".jpg");				
				vtkUnsignedCharArray vtkPixelData = renderedFrames.get(i);
				imageFiles.add(file);
				futures.add(executor.submit(new ImageWriteCallable(vtkPixelData, file, progressBar)));
			}
			renderedFrames = null;
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
