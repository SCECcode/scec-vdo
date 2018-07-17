package org.scec.vtk.timeline.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import javax.swing.JComponent;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.encode.RateControl;
import org.jcodec.codecs.h264.io.model.SliceType;
import org.jcodec.common.Codec;
import org.jcodec.common.MuxerTrack;
import org.jcodec.common.VideoCodecMeta;
import org.jcodec.common.VideoEncoder.EncodedFrame;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Packet.FrameType;
import org.jcodec.common.model.Picture8Bit;
import org.jcodec.common.model.Rational;
import org.jcodec.common.model.Size;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform8Bit;

import com.google.common.base.Preconditions;

public class H264Renderer extends AbstractMP4Renderer {

	private static final boolean D = true;

	private Picture8Bit toEncode;
	private Transform8Bit transform;
	private H264Encoder encoder;
	private ByteBuffer _out;
	private static final int default_buffer_mult = 12;
	private static final int max_buffer_mult = default_buffer_mult*1000;
	private int bufferMult = default_buffer_mult;
	
	private QualitySlider qualitySlider;

	public H264Renderer() {
		this(100);
	}
	
	public H264Renderer(int quality) {
		qualitySlider = new QualitySlider(quality);
	}
	
	private static final int min_qp = 4; // 0 should work, but crashes. 1-3 start weird. 4 works
	private static final int max_qp = 30; // above this is just garbage
	private static final int qp_delta = max_qp - min_qp;
	
	private static int qualityToQP(int quality) {
		// QP is 0 to 51
		// restrict to 1-30, where 100=1 and 0=30
		
		// convert to 0-1 with 0 highest and 1 lowest
		double q = (100d - quality)/100d;
		double qp = min_qp + q*(double)qp_delta;
		
		return (int)Math.round(qp);
	}
	
//	public static void main(String[] args) {
//		for (int quality=0; quality<=100; quality+=10) {
//			int QP = qualityToQP(quality);
//			if (D) System.out.println("Translated quality="+quality+" to QP="+QP);
//		}
//	}
	
	private class CustomDumbRateControl implements RateControl {
		final int QP;
		
		public CustomDumbRateControl(int QP) {
			this.QP = QP;
		}

		@Override
		public int getInitQp(SliceType sliceType) {
			return QP + (sliceType == SliceType.P ? 6 : 0);
		}

		@Override
		public int getQpDelta() {
			return 0;
		}

		@Override
		public boolean accept(int bits) {
			return true;
		}

		@Override
		public void reset() {
			// Do nothing, remember we are dumb
		}
	}

	@Override
	protected void doInit(File outputFile, int width, int height, double fps, int count) throws IOException {
		super.doInit(outputFile, width, height, fps, count);

		// Allocate a buffer big enough to hold output frames
		this.bufferMult = default_buffer_mult;
		this.toEncode = null;
		buildBuffer();

		// Create an instance of encoder
		int quality = qualitySlider.getValue();
		int QP = qualityToQP(quality);
		if (D) System.out.println("Translated quality="+quality+" to QP="+QP);
		CustomDumbRateControl rc = new CustomDumbRateControl(QP);
		encoder = new H264Encoder(rc);

		// Transform to convert between RGB and YUV
		transform = ColorUtil.getTransform8Bit(ColorSpace.RGB, encoder.getSupportedColorSpaces()[0]);
	}
	
	private void buildBuffer() {
		Preconditions.checkState(bufferMult > 0);
		_out = ByteBuffer.allocate(getWidth() * getHeight() * bufferMult);
	}
	
	@Override
	protected ColorSpace getColorSpace() {
		return encoder.getSupportedColorSpaces()[0];
	}
	
	@Override
	protected Codec getCodec() {
		return Codec.H264;
	}

	@Override
	protected Packet createPacket(BufferedImage img, long pts, int timescale, long duration, long frameNo) {
		Picture8Bit pic = fromBufferedImageRGB8Bit(img);
		if (toEncode == null) {
			toEncode = Picture8Bit.create(pic.getWidth(), pic.getHeight(), encoder.getSupportedColorSpaces()[0]);
		}

		// Perform conversion
		transform.transform(pic, toEncode);

		// Encode image into H.264 frame, the result is stored in '_out' buffer
		_out.clear();
		EncodedFrame ef = null;
		while (ef == null) {
			try {
				ef = encoder.encodeFrame8Bit(toEncode, _out);
			} catch (BufferOverflowException e) {
				int mult = bufferMult;
				int cap = _out.capacity();
				bufferMult *= 2;
				if (bufferMult > max_buffer_mult) {
					System.out.println("Buffer has reached maximum size, try reducing quality");
					throw e;
				}
				buildBuffer();
				if (D) System.out.println("Increased buffer size from dim*"+mult+"="+cap+" to dim*"+bufferMult+"="+_out.capacity());
			}
		}
		ByteBuffer result = ef.getData();

		return Packet.createPacket(result, pts, timescale, duration, frameNo,
				ef.isKeyFrame() ? FrameType.KEY : FrameType.INTER, null);
	}

	@Override
	protected void doFinalize() throws IOException {
		_out = null;
		super.doFinalize();
	}

	/*
	 *  JCoded conversion utility methods
	 */
	private static Picture8Bit fromBufferedImageRGB8Bit(BufferedImage src) {
		Picture8Bit dst = Picture8Bit.create(src.getWidth(), src.getHeight(), ColorSpace.RGB);
		fromBufferedImage8Bit(src, dst);
		return dst;
	}

	private static void fromBufferedImage8Bit(BufferedImage src, Picture8Bit dst) {
		byte[] dstData = dst.getPlaneData(0);

		int off = 0;
		for (int i = 0; i < src.getHeight(); i++) {
			for (int j = 0; j < src.getWidth(); j++) {
				int rgb1 = src.getRGB(j, i);
				dstData[off++] = (byte) (((rgb1 >> 16) & 0xff) - 128);
				dstData[off++] = (byte) (((rgb1 >> 8) & 0xff) - 128);
				dstData[off++] = (byte) ((rgb1 & 0xff) - 128);
			}
		}
	}

	@Override
	public JComponent getSettingsComponent() {
		return qualitySlider;
	}

	@Override
	public String getName() {
		return "MPEG-4, H.246, Lossy";
	}

}
