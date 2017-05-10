package org.scec.vtk.timeline.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;

import org.jcodec.common.Codec;
import org.jcodec.common.MuxerTrack;
import org.jcodec.common.VideoCodecMeta;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Rational;
import org.jcodec.common.model.Size;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.muxer.MP4Muxer;

import com.google.common.base.Preconditions;

public abstract class AbstractMP4Renderer extends AbstractThreadedRenderer {
	
	private static final boolean D = true;

	private int width;
	private int height;

	private SeekableByteChannel ch;
	private MP4Muxer muxer;
	private MuxerTrack outTrack;
	
	private int frameNo;
	private int timestamp;
	private Rational fps;

	@Override
	protected void doInit(File outputFile, int width, int height, double fps, int count) throws IOException {
		// force to be even, required for h264
		this.width = width - width % 2;
		this.height = height - height % 2;
		if (D) System.out.println("Creating h.264 encoder, size="+this.width+"x"+this.height+" (orig "+width+"x"+height+")");
		this.fps = getRationalFPS(fps);
		if (D) System.out.println("Input fps="+fps+". Rational: "+this.fps.getNum()+"/"+this.fps.getDen());
		//		enc = new SequenceEncoder8Bit(NIOUtils.writableChannel(outputFile), r);
		//		enc.getEncoder().
		this.ch = NIOUtils.writableChannel(outputFile);
		this.timestamp = 0;
		this.frameNo = 0;
		this.outTrack = null;

		// Muxer that will store the encoded frames
		muxer = MP4Muxer.createMP4Muxer(ch, Brand.MP4);
	}
	
	protected int getWidth() {
		return width;
	}
	
	protected int getHeight() {
		return height;
	}
	
	protected abstract ColorSpace getColorSpace();
	
	protected abstract Codec getCodec();

	@Override
	protected final void doProcessFrame(BufferedImage img) throws IOException {
		if (D) System.out.print("Processing frame "+frameNo+"...");
		if (outTrack == null) {
			Size size = new Size(img.getWidth(), img.getHeight());
			outTrack = muxer.addVideoTrack(getCodec(), new VideoCodecMeta(size, getColorSpace()));
		}
		
		Preconditions.checkState(img.getHeight() >= height, img.getWidth() >= width);
		if (img.getHeight() > height || img.getWidth() > width)
			// crop
			img = img.getSubimage(0, 0, width, height);
		
		// Add packet to video track
		outTrack.addFrame(createPacket(img, timestamp, fps.getNum(), fps.getDen(), frameNo));

		timestamp += fps.getDen();
		frameNo++;
		if (D) System.out.println("DONE");
	}
	
	protected abstract Packet createPacket(BufferedImage img, long pts, int timescale, long duration, long frameNo)
			throws IOException;

	@Override
	protected void doFinalize() throws IOException {
		if (D) System.out.print("Finalizing...");
		// Write MP4 header and finalize recording
		muxer.finish();
		NIOUtils.closeQuietly(ch);
		if (D) System.out.println("DONE");
	}
	
	private static double rational_accuracy = 0.000001;
	private static Rational getRationalFPS(double value) {
		Preconditions.checkState(value > 0);

		int n = (int) Math.floor(value);
		value -= n;

		if (value < rational_accuracy)
		{
			return new Rational(n, 1);
		}

		if (1 - rational_accuracy < value)
		{
			return new Rational((n + 1), 1);
		}

		// The lower fraction is 0/1
		int lower_n = 0;
		int lower_d = 1;

		// The upper fraction is 1/1
		int upper_n = 1;
		int upper_d = 1;

		while (true)
		{
			// The middle fraction is (lower_n + upper_n) / (lower_d + upper_d)
			int middle_n = lower_n + upper_n;
			int middle_d = lower_d + upper_d;

			if (middle_d * (value + rational_accuracy) < middle_n)
			{
				// real + error < middle : middle is our new upper
				upper_n = middle_n;
				upper_d = middle_d;
			}
			else if (middle_n < (value - rational_accuracy) * middle_d)
			{
				// middle < real - error : middle is our new lower
				lower_n = middle_n;
				lower_d = middle_d;
			}
			else
			{
				// Middle is our best fraction
				return new Rational((n * middle_d + middle_n), middle_d);
			}
		}
	}

	@Override
	public final String getExtension() {
		return "mp4";
	}

}
