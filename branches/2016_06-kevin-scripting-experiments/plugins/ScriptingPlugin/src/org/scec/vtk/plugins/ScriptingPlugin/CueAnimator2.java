package org.scec.vtk.plugins.ScriptingPlugin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.MediaLocator;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.tools.JpegImagesToMovie;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import vtk.vtkAnimationScene;
import vtk.vtkUnsignedCharArray;

public class CueAnimator2 {
	
	private vtkAnimationScene scene;
	private boolean rendering;
	private boolean renderPaused;
	
	private List<RenderStepListener> stepListeners = new ArrayList<>();
	
	private List<vtkUnsignedCharArray> imagePixelData;
	private int renderWidth;
	private int renderHeight;
	private MediaLocator movieFile;
	private double renderFPS;
	
	public CueAnimator2(vtkAnimationScene scene) {
		this.scene = scene;
	}
	
	public void setRendering(MediaLocator movieFile, double renderFPS) {
		this.movieFile = movieFile;
		this.renderFPS = renderFPS;
		rendering = true;
	}
	
	public void addRenderStepListener(RenderStepListener l) {
		stepListeners.add(l);
	}
	
	/**
	 * Called when animation is started
	 */
	public void startCue() {
		for (RenderStepListener l : stepListeners)
			l.renderStarted();
		
		if (rendering) {
			imagePixelData = new ArrayList<>();
			int[] renderSize = MainGUI.getRenderWindow().GetRenderWindow().GetSize();
			renderWidth =  renderSize[0];
			renderHeight = renderSize[1];
		}
	}
	
	public void endCue() {
		for (RenderStepListener l : stepListeners)
			l.renderStopped();
		
		if (rendering) {
			rendering = false;
			writeAnimationToFile();
		}
	}
	
	public void tick() {
		while (rendering && renderPaused) {
			// wait for user to unpause render
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Throwables.propagate(e);
			}
		}
		
		double startTime = scene.GetStartTime();
		double curTime = scene.GetAnimationTime();
		double endTime = scene.GetEndTime();
		
		System.out.print("Tick curTime="+curTime);
		
		for (RenderStepListener l : stepListeners)
			l.renderFrameToBeProcessed(startTime, curTime, endTime);
		
		if (rendering) {
			Preconditions.checkNotNull(imagePixelData, "set to rendering after render started? pixel data null");
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						int[] renderSize = MainGUI.getRenderWindow().GetRenderWindow().GetSize();
						int width =  renderSize[0];
						int height = renderSize[1];
						Preconditions.checkState(width == renderWidth, "Render width changed during rendering");
						Preconditions.checkState(height == renderHeight, "Render height changed during rendering");
						vtkUnsignedCharArray vtkPixelData = new vtkUnsignedCharArray();
						MainGUI.getRenderWindow().GetRenderWindow().GetPixelData(0, 0, width, height,
								1, vtkPixelData);
						imagePixelData.add(vtkPixelData);
					}
				});
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		}
		System.out.print("End tick curTime="+curTime);
	}
	
	private void writeAnimationToFile() {
		Preconditions.checkNotNull(movieFile, "No movie file specified");
		JpegImagesToMovie jpegToImages = new JpegImagesToMovie();
		List<File> imagesToConvert = new ArrayList<>();
		File tempDir = Files.createTempDir();
		for(int i =0;i<imagePixelData.size();i++){
			File file = new File(tempDir, "frame_"+i+".jpg");

			vtkUnsignedCharArray vtkPixelData = imagePixelData.get(i);
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
				ImageIO.write(bufImage, "jpg", file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			imagesToConvert.add(file);
		}
		jpegToImages.doIt(renderWidth, renderHeight, renderFPS, imagesToConvert, movieFile);
		try {
			FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			Throwables.propagate(e);
		}
		System.out.println("*** Finished Generating jpgs " );
	}

}
