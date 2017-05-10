package org.scec.vtk.timeline.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.opensha.commons.util.FileNameComparator;

public class RenderBatchTests {

	public static void main(String[] args) throws IOException {
		File inDir = new File("/home/kevin/vdo/render_tests/input_sequence");
		File outDir = inDir.getParentFile();
		
		double fps = 30;
		
		List<BufferedImage> images = new ArrayList<>();
		
		File[] files = inDir.listFiles();
		Arrays.sort(files, new FileNameComparator());
		
		for (File file : files) {
			if (!file.getName().endsWith(".png"))
				continue;
			images.add(ImageIO.read(file));
		}
		
		System.out.println("Loaded "+images.size()+" frames");
		int width = images.get(0).getWidth();
		int height = images.get(0).getHeight();
		
		List<Renderer> renderers = new ArrayList<>();
		List<String> fileNames = new ArrayList<>();
		
		int[] qualities = { 100, 90, 80, 70, 60, 50 };
		
		for (int quality : qualities) {
			renderers.add(new H264Renderer(quality));
			fileNames.add("h264_q"+quality);
			renderers.add(new MP4JPEGSequenceRenderer(quality));
			fileNames.add("jpeg_seq_q"+quality);
		}
		renderers.add(new MP4PNGSequenceRenderer());
		fileNames.add("png_seq");
		
		for (int i=0; i<renderers.size(); i++) {
			Renderer r = renderers.get(i);
			if (r instanceof AbstractThreadedRenderer)
				((AbstractThreadedRenderer)r).setThreadLocalProcessing(true);
			File outputFile = new File(outDir, fileNames.get(i)+"."+r.getExtension());
			
			System.out.println("Processing "+outputFile.getName());
			
			r.init(outputFile, width, height, fps, images.size());
			
			for (BufferedImage img : images)
				r.processFrame(img);
			
			r.finalize();
		}
	}

}
