package org.scec.vtk.main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;

/** Paints a bundled monochrome icon in the component's current foreground color. */
final class ThemedImageIcon implements Icon {
	private final BufferedImage source;
	private final BufferedImage tinted;
	private Color lastColor;

	private ThemedImageIcon(BufferedImage source, int size) {
		this.source = source;
		tinted = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
	}

	static Icon load(String path, int size) {
		try {
			BufferedImage image = ImageIO.read(new File(path));
			if (image == null)
				throw new IOException("Unsupported image format");
			return new ThemedImageIcon(image, size);
		} catch (IOException e) {
			System.err.println("Could not load icon " + path + ": " + e.getMessage());
			return null;
		}
	}

	@Override
	public void paintIcon(Component component, Graphics graphics, int x, int y) {
		Color color = component == null ? null : component.getForeground();
		if (color == null)
			color = Color.BLACK;
		if (!color.equals(lastColor)) {
			Graphics2D g = tinted.createGraphics();
			try {
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g.setComposite(AlphaComposite.Src);
				g.drawImage(source, 0, 0, getIconWidth(), getIconHeight(), null);
				g.setComposite(AlphaComposite.SrcIn);
				g.setColor(color);
				g.fillRect(0, 0, getIconWidth(), getIconHeight());
			} finally {
				g.dispose();
			}
			lastColor = color;
		}
		graphics.drawImage(tinted, x, y, component);
	}

	@Override
	public int getIconWidth() {
		return tinted.getWidth();
	}

	@Override
	public int getIconHeight() {
		return tinted.getHeight();
	}
}
