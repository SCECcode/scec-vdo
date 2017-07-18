package org.scec.vtk.timeline;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.timeline.render.Renderer;

import com.google.common.base.Preconditions;

import vtk.vtkAnimationCue;
import vtk.vtkAnimationScene;
import vtk.vtkRenderWindow;
import vtk.vtkUnsignedCharArray;
import vtk.vtkWindowToImageFilter;

public class CueAnimator {
	
	private static final boolean D = false; 
	
	// if true, uses the component's paint method to render
	// if false, uses VTK's rendering classes
	private static final boolean JAVA_COMPONENT_RENDER = true;
	
	private Timeline timeline;
	private vtkAnimationScene scene;
	private vtkAnimationCue cue;
	
	private CueAnimatorListener listener;
	
	private boolean rendering;
	private int renderWidth;
	private int renderHeight;
	private File outputFile;
	
	private Renderer renderer;
	private Dimension origDimensions;
	
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
	
	public boolean render() {
		if (renderer == null) {
			synchronized (chooser) {
				String ext = timeline.getRenderer().getExtension();
				chooser.setFileFilter(new FileNameExtensionFilter(ext.toUpperCase()+" File", ext));
				int ret = chooser.showSaveDialog(null);
				if (ret == JFileChooser.APPROVE_OPTION) {
					outputFile = chooser.getSelectedFile();
					renderer = timeline.getRenderer();
					if (!outputFile.getName().toLowerCase().endsWith("."+renderer.getExtension()))
						outputFile = new File(outputFile.getParentFile(), outputFile.getName()+"."+ext);
					
					origDimensions = getCurrentSize();
					if (timeline.getRenderDimensions() != null) {
						Dimension dims = timeline.getRenderDimensions();
						forceViewerSize(dims);
					}
					
					Dimension dims = getCurrentSize();
					renderWidth = dims.width;
					renderHeight = dims.height;
					
					if (D) System.out.println("Rendering movie with dimensions "+renderWidth+"x"+renderHeight);
					
					int count = (int)(timeline.getMaxTime()*timeline.getFamerate()+0.5);
					try {
						renderer.init(outputFile, renderWidth, renderHeight, timeline.getFamerate(), count);
					} catch (IOException e) {
						ExceptionUtils.throwAsRuntimeException(e);
					}
					
					rendering = true;
					scene.SetModeToSequence();
					scene.SetFrameRate(timeline.getFamerate());
					
					playThread = new PlayThread();
					playThread.start();
				} else {
					return false;
				}
			}
		} else {
			Preconditions.checkState(rendering);
			Preconditions.checkNotNull(outputFile);
			Preconditions.checkNotNull(renderer);
			
			origDimensions = getCurrentSize();
			if (timeline.getRenderDimensions() != null) {
				Dimension dims = timeline.getRenderDimensions();
				forceViewerSize(dims);
			}
			
			// resume
			playThread = new PlayThread();
			playThread.start();
		}
		return true;
	}
	
	private void forceViewerSize(Dimension dims) {
		System.out.println("Force resizing viewer to "+dims.width+"x"+dims.height);
		JComponent comp = MainGUI.getRenderWindow().getComponent();
		comp.setSize(dims);
		comp.setPreferredSize(dims);
		comp.setMinimumSize(dims);
		comp.setMaximumSize(dims);
		int newViewerWidth = comp.getWidth();
		int newViewerHeight = comp.getHeight();
		System.out.println("Force resized. New dims: "+newViewerWidth+"x"+newViewerHeight);
	}
	
	private void clearForcedVeiwerSize(Dimension origDims) {
		JComponent comp = MainGUI.getRenderWindow().getComponent();
		comp.setPreferredSize(null);
		comp.setMinimumSize(null);
		comp.setMaximumSize(null);
		comp.setSize(origDims);
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
		
		if (rendering && timeline.getRenderDimensions() != null && origDimensions != null)
			clearForcedVeiwerSize(origDimensions);
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
	
	private Dimension getCurrentSize() {
		int width, height;
		if (JAVA_COMPONENT_RENDER) {
			JComponent component = MainGUI.getRenderWindow().getComponent();
			width =  component.getWidth();
			height = component.getHeight();
		} else {
			int[] renderSize = MainGUI.getRenderWindow().getRenderer().GetSize();
			width =  renderSize[0];
			height = renderSize[1];
		}
		return new Dimension(width, height);
	}
	
	private class TickRenderRunnable implements Runnable {
		private double animTime;
		public TickRenderRunnable(double animTime) {
			this.animTime = animTime;
		}
		@Override
		public void run() {
			if (D) System.out.println("Rending frame at "+animTime);
//			vtkWindowToImageFilter vtkPixelData = new vtkWindowToImageFilter();
//			vtkPixelData.SetInput(MainGUI.getRenderWindow().getRenderWindow());
//			vtkPixelData.ReadFrontBufferOff();
			timeline.activateTime(animTime);
			Dimension dims = getCurrentSize();
			Preconditions.checkState(dims.width == renderWidth && dims.height == renderHeight,
					"Render canvas size changed during render");
			BufferedImage image = new BufferedImage(dims.width, dims.height, BufferedImage.TYPE_INT_RGB);
			if (JAVA_COMPONENT_RENDER) {
				// call the Component's paint method, using
				// the Graphics object of the image.
				if (D) System.out.println("Render tick, painting from JComponent");
				MainGUI.getRenderWindow().getComponent().paint(image.getGraphics());
			} else {
				if (D) System.out.println("Render tick, grabbing frame the VTK way");
				vtkRenderWindow rw = MainGUI.getRenderWindow().getRenderWindow();
				vtkUnsignedCharArray data = new vtkUnsignedCharArray();
				int front = 1;
				rw.GetPixelData(0, 0, dims.width-1, dims.height-1, front, data);
				int index = 0;
				boolean nonZero = false;
				for (int y=0; y<dims.height; y++) {
					for (int x=0; x<dims.width; x++) {
						int r = data.GetValue(index++);
						int g = data.GetValue(index++);
						int b = data.GetValue(index++);
						int rgb = r;
						rgb = (rgb << 8) + g;
						rgb = (rgb << 8) + b;
						image.setRGB(x, (dims.height-1)-y, rgb);
						if (rgb > 0)
							nonZero = true;
					}
				}
//				vtkWindowToImageFilter vtkPixelData = new vtkWindowToImageFilter();
//				vtkPixelData.SetInput(MainGUI.getRenderWindow().getRenderWindow());
//				vtkPixelData.ReadFrontBufferOff();
//				vtkPixelData.Update();
//				vtkPixelData.GetOutput().get
				/*
				 vtkWindowToImageFilter vtkPixelData = new vtkWindowToImageFilter();
			vtkPixelData.SetInput(MainGUI.getRenderWindow().getRenderWindow());
			vtkPixelData.ReadFrontBufferOff();
			vtkPixelData.Update();
			if (D) System.out.println("Render tick, adding pixel data");
			renderedFrames.add(vtkPixelData);
			if (D) {
				vtkRenderWindow rw = MainGUI.getRenderWindow().getRenderWindow();
				vtkUnsignedCharArray data = new vtkUnsignedCharArray();
				int front = 1;
				rw.GetPixelData(0, 0, width-1, height-1, front, data);
				BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				int index = 0;
				boolean nonZero = false;
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int r = data.GetValue(index++);
						int g = data.GetValue(index++);
						int b = data.GetValue(index++);
						int rgb = r;
						rgb = (rgb << 8) + g;
						rgb = (rgb << 8) + b;
						img.setRGB(x, y, rgb);
						if (rgb > 0)
							nonZero = true;
					}
				}
				 */
			}
			if (D) System.out.println("Render tick done, processing frame");
			try {
				renderer.processFrame(image);
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e); // TODO exception handling
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
	
	public void endCue() {
		if (D) System.out.println("endCue");
		if (rendering) {
			try {
				renderer.finalize();
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
			if (timeline.getRenderDimensions() != null) {
				clearForcedVeiwerSize(origDimensions);
			}
		}
		listener.animationFinished(isRendering());
	}

}
