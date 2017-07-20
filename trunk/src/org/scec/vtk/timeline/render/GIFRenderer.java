package org.scec.vtk.timeline.render;


import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JComponent;

public class GIFRenderer extends AbstractThreadedRenderer {
	
	private static final boolean D = true;
	
	private String extension;
	private String name;
	private QualitySlider qualitySilder;
	
	private File outputDir;
	private String prefix;
	private int digits;
	private int index;
	
	private double maxTime = 15d;
	private double fps = 30;
	private int tFrames;
	private int frameCntr;
	private String GIFoutputFileName;
	private String GIFoutputFilePath;
	
	
	private ImageWriter writer;
	private ImageWriteParam writeParam;
	
	public static Vector<BufferedImage> Frames; //array of IIOImage frames for gif.
	
	
	
	public GIFRenderer(String extension, String name) {
		this.extension = "gif";
		this.name = name;
		tFrames =  (int)(fps*maxTime);
		frameCntr = -1;
		GIFoutputFileName = "GIFName";
		Frames = new Vector<BufferedImage>();
		
	}

	@Override
	public JComponent getSettingsComponent() {
		return qualitySilder;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getExtension() {
		return extension;
	}

	@Override
	protected void doInit(File outputFile, int width, int height, double fps, int count) throws IOException {
		
		GIFoutputFileName = outputFile.getName().toString();
		System.out.println("GIFoutputFileName: " + outputFile.getName());

		GIFoutputFilePath = outputFile.getAbsolutePath().toString();
		System.out.println("GIFoutputFilePath: " + outputFile.getAbsolutePath());

		
		this.digits = ((count+1)+"").length();
		index = 0;
		this.outputDir = outputFile.getParentFile();
		prefix = outputFile.getName();
		if (prefix.toLowerCase().endsWith("."+extension.toLowerCase())) {
			// already has the extension
			prefix = prefix.substring(0, prefix.length()-(extension.length()+1));
		}
		if (prefix.endsWith("_"+getNumStr(0)))
			// they selected the first frame of an old sequence, use that prefix
			prefix = prefix.substring(0, prefix.lastIndexOf("_"));
		if (D) System.out.println("Output dir: "+outputDir.getAbsolutePath());
		if (D) System.out.println("Output prefix: "+prefix);
		
		writer = ImageIO.getImageWritersByFormatName(extension).next();
		writeParam = writer.getDefaultWriteParam();
		if (qualitySilder != null) {
			writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			//writeParam.setCompressionQuality((float)qualitySilder.getValue()/100f); 
		}
	}
	
	private String getNumStr(int index) {
		String numStr = index+"";
		while (numStr.length() < digits)
			numStr = "0"+numStr;
		return numStr;
	}

	
	
	protected void doProcessFrame(BufferedImage img) throws IOException {
		
		
		Frames.add(img);
		
		index++;
		frameCntr++;
		if (D){
			
		}
		if (frameCntr == (int)maxTime*fps){ //multiply the frames per second with maxtime
			
			//a gif is created when the animation is finished 
			System.out.println("Creating gif...");
			GIFSequence.gifFunction(GIFRenderer.Frames, GIFoutputFileName, GIFoutputFilePath);
			Frames.clear();
			//System.out.println("frameCntr: " + frameCntr);
			frameCntr=-1;
			
			
		}
		
			 
	}
	


	public void setExtension(String extension) {
		this.extension = extension;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setQualitySilder(QualitySlider qualitySilder) {
		this.qualitySilder = qualitySilder;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setDigits(int digits) {
		this.digits = digits;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setMaxTime(double maxTime) {
		this.maxTime = maxTime;
	}

	public void setFps(double fps) {
		this.fps = fps;
	}
	

	public void settFrames(int tFrames) {
		this.tFrames = tFrames;
	}

	public void setWriter(ImageWriter writer) {
		this.writer = writer;
	}

	public void setWriteParam(ImageWriteParam writeParam) {
		this.writeParam = writeParam;
	}

	public static void setFrames(Vector<BufferedImage> frames) {
		Frames = frames;
	}

	@Override
	protected void doFinalize() throws IOException {
		writer.dispose();
	}

}

