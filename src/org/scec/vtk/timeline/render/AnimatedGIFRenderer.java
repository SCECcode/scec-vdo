package org.scec.vtk.timeline.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scec.vtk.timeline.CueAnimator;

import com.google.common.base.Preconditions;

public class AnimatedGIFRenderer extends AbstractThreadedRenderer {
	
	private static String NAME = "Animated GIF";
	private static String EXT = "gif";
	
	private ImageWriter writer;
	private ImageWriteParam imageWriteParam;
	private IIOMetadata imageMetaData;
	private FileImageOutputStream output;
	
	private JCheckBox loopCheck;
//	private ColorTableSlider colorTableSlider;
	private JPanel settingsPanel;
	
	public AnimatedGIFRenderer() {
		loopCheck = new JCheckBox("Loop continuously?", true);
//		colorTableSlider = new ColorTableSlider(color_table_values[color_table_values.length-1]);
//		
		settingsPanel = new JPanel();
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
		settingsPanel.add(loopCheck);
		settingsPanel.add(new JLabel("NOTE: FPS should be an even divisor of 100"));
//		settingsPanel.add(colorTableSlider);
	}

	@Override
	public JComponent getSettingsComponent() {
		return settingsPanel;
//		return loopCheck;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getExtension() {
		return EXT;
	}
	
	public void setLoop(boolean loop) {
		loopCheck.setSelected(loop);
		loopCheck.invalidate();
	}

	@Override
	protected void doInit(File outputFile, int width, int height, double fps, int count) throws IOException {
		Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix(EXT);
		Preconditions.checkArgument(iter.hasNext(), "No GIF image writers available!");
		writer = iter.next();
		
		imageWriteParam = writer.getDefaultWriteParam();
		ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(CueAnimator.IMAGE_TYPE);

		imageMetaData = writer.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

		String metaFormatName = imageMetaData.getNativeMetadataFormatName();

		IIOMetadataNode root = (IIOMetadataNode)imageMetaData.getAsTree(metaFormatName);

		IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
		
		// in hundreths of a second
		int delayTime = (int)(100d/fps);

		graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
		graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
		graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
		graphicsControlExtensionNode.setAttribute("delayTime", delayTime+"");
		graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");
		
//		IIOMetadataNode localColorTableNode = getNode(root, "LocalColorTable");
//		localColorTableNode.setAttribute("sizeOfLocalColorTable", colorTableSlider.getValue()+"");
//		localColorTableNode.setAttribute("sortFlag", "FALSE");

		IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
		commentsNode.setAttribute("CommentExtension", "Created by SCEC-VDO");

		IIOMetadataNode appEntensionsNode = getNode(root, "ApplicationExtensions");

		IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

		child.setAttribute("applicationID", "NETSCAPE");
		child.setAttribute("authenticationCode", "2.0");

		int loop = loopCheck.isSelected() ? 0 : 1;

		child.setUserObject(new byte[]{ 0x1, (byte) (loop & 0xFF), (byte)((loop >> 8) & 0xFF)});
		appEntensionsNode.appendChild(child);

		imageMetaData.setFromTree(metaFormatName, root);

		output = new FileImageOutputStream(outputFile);
		writer.setOutput(output);
		writer.prepareWriteSequence(imageMetaData);
	}
	
	/**
	 * Returns an existing child node, or creates and returns a new child node (if 
	 * the requested node does not exist).
	 * 
	 * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child node.
	 * @param nodeName the name of the child node.
	 * 
	 * @return the child node, if found or a new node created with the given name.
	 */
	private static IIOMetadataNode getNode(
			IIOMetadataNode rootNode,
			String nodeName) {
		int nNodes = rootNode.getLength();
		for (int i = 0; i < nNodes; i++) {
			if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName)
					== 0) {
				return((IIOMetadataNode) rootNode.item(i));
			}
		}
		IIOMetadataNode node = new IIOMetadataNode(nodeName);
		rootNode.appendChild(node);
		return(node);
	}

	@Override
	protected void doProcessFrame(BufferedImage img) throws IOException {
		writer.writeToSequence(new IIOImage(img, null, imageMetaData), imageWriteParam);
	}

	@Override
	protected void doFinalize() throws IOException {
		writer.endWriteSequence();
		output.close();
	}
	
//	private static int[] color_table_values = { 2, 4, 8, 16, 32, 64, 128, 256 };
//	
//	public class ColorTableSlider extends JPanel implements ChangeListener {
//		
//		private JSlider slider;
//		private JLabel valueLabel;
//		
//		/**
//		 * Quality slider panel with a slider from 0 to 100, with the given default
//		 * @param defaultVal
//		 */
//		public ColorTableSlider(int defaultVal) {
//			Preconditions.checkState(Ints.contains(color_table_values, defaultVal));
//			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//			
//			JLabel qualityLabel = new JLabel("Num Colors");
//			add(qualityLabel);
//			
//			JPanel sliderPanel = new JPanel();
//			sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));
//			slider = new JSlider(0, color_table_values.length-1, Ints.indexOf(color_table_values, defaultVal));
//			slider.addChangeListener(this);
//			sliderPanel.add(slider);
//			valueLabel = new JLabel("");
//			updateLabel();
//			sliderPanel.add(valueLabel);
//			add(sliderPanel);
//		}
//		
//		/**
//		 * Color table size
//		 * @return
//		 */
//		public int getValue() {
//			return color_table_values[slider.getValue()];
//		}
//		
//		private void updateLabel() {
//			valueLabel.setText(" "+getValue()+" Colors");
//		}
//
//		@Override
//		public void stateChanged(ChangeEvent arg0) {
//			updateLabel();
//		}
//
//	}
	
	private static void mergeGifs(File gif1, File gif2, File outputFile, boolean vertical, boolean loop) throws IOException {
		AnimatedGIFRenderer writer = new AnimatedGIFRenderer();
		writer.setLoop(loop);
		
		ImageReader reader1 = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();
		ImageReader reader2 = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();
		
		ImageInputStream stream1 = ImageIO.createImageInputStream(gif1);
		reader1.setInput(stream1, false);
		ImageInputStream stream2 = ImageIO.createImageInputStream(gif2);
		reader2.setInput(stream2, false);

		int noi = reader1.getNumImages(true);
		Preconditions.checkState(noi == reader2.getNumImages(true));
		
		int width = -1;
		int height = -1;
		
		for (int i=0; i<noi; i++) {
			System.out.println("Frame "+i+"/"+noi);
			BufferedImage img1 = reader1.read(i);
			BufferedImage img2 = reader2.read(i);
			
			if (i == 0) {
				if (vertical) {
					width = img1.getWidth();
					if (img2.getWidth() > img1.getWidth())
						width = img2.getWidth();
					height = img1.getHeight() + img2.getHeight();
				} else {
					height = img1.getHeight();
					if (img2.getHeight() > img1.getHeight())
						height = img2.getHeight();
					width = img1.getWidth() + img2.getWidth();
				}
				
				IIOMetadataNode root = (IIOMetadataNode) reader1.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
		        IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);

		        int delay = Integer.valueOf(gce.getAttribute("delayTime"));
		        double fps = 100d/delay;
		        System.out.println("Delay="+delay+", fps="+fps);
				
				writer.init(outputFile, width, height, fps, noi);
				
				System.out.println("Image1 dimensions: "+img1.getWidth()+"x"+img1.getHeight());
				System.out.println("Image2 dimensions: "+img2.getWidth()+"x"+img2.getHeight());
				System.out.println("Output dimensions: "+width+"x"+height);
			}
			
			BufferedImage combined = new BufferedImage(width, height, CueAnimator.IMAGE_TYPE);
			for (int x=0; x<img1.getWidth(); x++)
				for (int y=0; y<img1.getHeight(); y++)
					combined.setRGB(x, y, img1.getRGB(x, y));
			int offsetX, offsetY;
			if (vertical) {
				offsetX = 0;
				offsetY = img1.getHeight();
			} else {
				offsetX = img1.getWidth();
				offsetY = 0;
			}
			for (int x=0; x<img2.getWidth(); x++)
				for (int y=0; y<img2.getHeight(); y++)
					combined.setRGB(x+offsetX, y+offsetY, img2.getRGB(x, y));
			writer.processFrame(combined);
		}
		writer.finalize();
		System.out.println("DONE");
		stream1.close();
		stream2.close();
	}
	
	public static void main(String[] args) throws IOException {
//		File gif1 = new File("/tmp/saf_san_gorgonio_slip.gif");
//		File gif2 = new File("/tmp/saf_san_gorgonio_vel.gif");
//		File outputFile = new File("/tmp/saf_san_gorgonio_combined.gif");
//		File gif1 = new File("/home/kevin/Documents/2017_AGU/event_526885_slip_fast.gif");
//		File gif2 = new File("/home/kevin/Documents/2017_AGU/event_526885_vel_fast.gif");
//		File outputFile = new File("/home/kevin/Documents/2017_AGU/event_526885_combined_fast.gif");
//		mergeGifs(gif1, gif2, outputFile, true);
		File gif1 = new File("/home/kevin/Documents/2018_SSA/figures/event_1670183_slip_anim.gif");
		File gif2 = new File("/home/kevin/Documents/2018_SSA/figures/event_1670183_vel_anim.gif");
		File outputFileLoop = new File("/home/kevin/Documents/2018_SSA/figures/event_1670183_combined_anim.gif");
//		File outputFileNoLoop = new File("/home/kevin/SCEC/2018_01-VidalePresentation/combined_disagg_flythrough_noloop.gif");
		mergeGifs(gif1, gif2, outputFileLoop, true, true);
//		mergeGifs(gif1, gif2, outputFileNoLoop, true, false);
		System.exit(0);
	}

}
