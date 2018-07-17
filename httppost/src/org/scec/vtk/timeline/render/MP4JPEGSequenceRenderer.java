package org.scec.vtk.timeline.render;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.JComponent;

import org.jcodec.common.Codec;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Packet.FrameType;
import org.jcodec.containers.mp4.MP4Packet;

public class MP4JPEGSequenceRenderer extends AbstractMP4Renderer {
	
	private ImageWriter writer;
	private ImageWriteParam writeParam;
	
	private QualitySlider qualitySilder;

	public MP4JPEGSequenceRenderer() {
		this(90);
	}
	
	public MP4JPEGSequenceRenderer(int quality) {
		qualitySilder = new QualitySlider(quality);
	}

	@Override
	public JComponent getSettingsComponent() {
		return qualitySilder;
	}

	@Override
	public String getName() {
		return "MPEG-4, JPEG";
	}

	@Override
	protected Codec getCodec() {
		return Codec.JPEG;
	}

	@Override
	protected ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}

	@Override
	protected void doInit(File outputFile, int width, int height, double fps, int count) throws IOException {
		super.doInit(outputFile, width, height, fps, count);
		
		writer = ImageIO.getImageWritersByFormatName("jpg").next();
		writeParam = writer.getDefaultWriteParam();
		writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		writeParam.setCompressionQuality((float)qualitySilder.getValue()/100f);
	}

	@Override
	protected Packet createPacket(BufferedImage img, long pts, int timescale, long duration, long frameNo)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    
	    ImageOutputStream outputStream = new MemoryCacheImageOutputStream(out);
		writer.setOutput(outputStream);
		IIOImage outputImage = new IIOImage(img, null, null);
		writer.write(null, outputImage, writeParam);
		outputStream.flush();
	    ByteBuffer buf = ByteBuffer.wrap(out.toByteArray());
	    
	    return MP4Packet.createMP4Packet(
	    		buf, pts, timescale, duration, frameNo, FrameType.KEY, null, (int)frameNo, frameNo, 0);
	}
}
