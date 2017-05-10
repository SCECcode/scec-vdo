package org.scec.vtk.timeline.render;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import org.jcodec.common.Codec;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Packet.FrameType;
import org.jcodec.containers.mp4.MP4Packet;

public class MP4PNGSequenceRenderer extends AbstractMP4Renderer {

	@Override
	public JComponent getSettingsComponent() {
		return null;
	}

	@Override
	public String getName() {
		return "MPEG-4, PNG Lossless";
	}

	@Override
	protected Codec getCodec() {
		return Codec.PNG;
	}

	@Override
	protected ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}

	@Override
	protected Packet createPacket(BufferedImage img, long pts, int timescale, long duration, long frameNo)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ImageIO.write(img, "png", out);
	    ByteBuffer buf = ByteBuffer.wrap(out.toByteArray());
	    
	    return MP4Packet.createMP4Packet(
	    		buf, pts, timescale, duration, frameNo, FrameType.KEY, null, (int)frameNo, frameNo, 0);
	}
}
