package org.scec.vtk.timeline.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JComponent;

public class ImageSequenceRenderer extends AbstractThreadedRenderer {
	
	private static final boolean D = true;
	
	private String extension;
	private String name;
	private QualitySlider qualitySilder;
	
	private File outputDir;
	private String prefix;
	private int digits;
	private int index;
	
	private ImageWriter writer;
	private ImageWriteParam writeParam;
	
	public static ImageSequenceRenderer getPNG() {
		return new ImageSequenceRenderer("png", "PNG Sequence", false, 0);
	}
	
	public static ImageSequenceRenderer getJPEG() {
		return new ImageSequenceRenderer("jpg", "JPEG Sequence", true, 90);
	}

	ImageSequenceRenderer(String extension, String name, boolean showQuality, int defaultQuality) {
		this.extension = extension;
		this.name = name;
		if (showQuality)
			qualitySilder = new QualitySlider(defaultQuality);
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
			writeParam.setCompressionQuality((float)qualitySilder.getValue()/100f);
		}
	}
	
	private String getNumStr(int index) {
		String numStr = index+"";
		while (numStr.length() < digits)
			numStr = "0"+numStr;
		return numStr;
	}
	
	@Override
	protected void doProcessFrame(BufferedImage img) throws IOException {
		File outputFile = new File(outputDir, prefix+"_"+getNumStr(index)+"."+extension);
		if (D) System.out.print("Writing frame "+index+" to "+outputFile.getName());
		ImageOutputStream outputStream = new FileImageOutputStream(outputFile);	
		writer.setOutput(outputStream);
		IIOImage outputImage = new IIOImage(img, null, null);
		writer.write(null, outputImage, writeParam);
		index++;
		if (D) System.out.println("DONE");	 
	}

	@Override
	protected void doFinalize() throws IOException {
		writer.dispose();
	}

}

