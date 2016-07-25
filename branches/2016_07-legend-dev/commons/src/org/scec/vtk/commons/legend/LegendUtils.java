package org.scec.vtk.commons.legend;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.scec.vtk.plugins.Plugin;

import com.google.common.base.Preconditions;

import vtk.vtkActor2D;
import vtk.vtkColorTransferFunction;
import vtk.vtkImageMapper;
import vtk.vtkImageReader2;
import vtk.vtkJPEGReader;
import vtk.vtkLookupTable;
import vtk.vtkPNGReader;
import vtk.vtkScalarBarActor;
import vtk.vtkTIFFReader;
import vtk.vtkTextActor;

public class LegendUtils {
	
	public static LegendItem buildColorBarLegend(Plugin source, String title, double x, double y,
			Color minColor, double minValue, Color maxColor, double maxValue) {
		CPT cpt = new CPT(minValue, maxValue, minColor, maxColor);
		return buildColorBarLegend(source, cpt, title, x, y);
	}
	
	public static LegendItem buildColorBarLegend(Plugin source, CPT cpt, String title, double x, double y) {
		Preconditions.checkNotNull(cpt, "CPT cannot be null");
		vtkScalarBarActor scalarBar = new vtkScalarBarActor();
		int minValue = (int)cpt.getMinValue();
		int maxValue = (int)cpt.getMaxValue();
		Color minColor = cpt.getMinColor();
		Color maxColor = cpt.getMaxColor();
		int numTicks = cpt.size()+1;
		
		if (title != null && !title.isEmpty())
			scalarBar.SetTitle(title);
		scalarBar.SetNumberOfLabels(numTicks);
		
		
		vtkLookupTable hue = new vtkLookupTable();
		hue.SetTableRange(minValue, maxValue);
		hue.SetNumberOfColors(256);
		vtkColorTransferFunction ctf = new vtkColorTransferFunction();
		ctf.SetColorSpaceToRGB();
		for (int i=0; i<cpt.size(); i++)
		{
			CPTVal str = cpt.get(i);
			ctf.AddRGBPoint((float)i/(float)cpt.size(), str.minColor.getRed()/255.0, str.minColor.getGreen()/255.0, str.minColor.getBlue()/255.0);
		}
		ctf.AddRGBPoint(1, maxColor.getRed()/255.0, maxColor.getGreen()/255.0, maxColor.getBlue()/255.0);
		
		hue.Build();
		for (int i=0; i<256; i++)
		{
			double[] color=ctf.GetColor((double)i/256.0);
			hue.SetTableValue(i, color[0], color[1], color[2], 1);
		}
		
		scalarBar.SetLookupTable(hue);
		scalarBar.SetOrientationToHorizontal();
		scalarBar.SetWidth(0.4);
		scalarBar.SetHeight(0.07);
		scalarBar.SetPosition(x, y);
		scalarBar.Modified();
		
		return new LegendItem(scalarBar, source, title);
	}
	
	public static LegendItem buildTextLegend(Plugin source, String text, int fontSize, Color color, double x, double y) {
		Preconditions.checkNotNull(text, "Text cannot be null when building text legend");
		vtkTextActor textActor = new vtkTextActor();
		textActor.SetInput(text);
		textActor.SetPosition(x, y); // TODO : why different coordinates? figure that out
		textActor.GetTextProperty().SetFontSize(fontSize);
		textActor.GetTextProperty().SetColor(color.getRed()/255d, color.getGreen()/255d, color.getBlue()/255d);
		
		return new LegendItem(textActor, source, text);
	}
	
	public static LegendItem buildImageLegend(Plugin source, File imageFile, double x, double y) throws IOException {
		BufferedImage image = ImageIO.read(imageFile);
		vtkImageReader2 imageReader = new vtkImageReader2();
		if (imageFile.getName().toLowerCase().endsWith(".jpg") || imageFile.getName().toLowerCase().endsWith(".jpeg")) {
			imageReader = new vtkJPEGReader();
		} else if (imageFile.getName().toLowerCase().endsWith(".png")) {
			imageReader = new vtkPNGReader();
		} else if (imageFile.getName().toLowerCase().endsWith(".tiff")) {
			imageReader = new vtkTIFFReader();
		} else {
			throw new NullPointerException();
		}
		
		imageReader.SetFileName(imageFile.getAbsolutePath());
		imageReader.Update();
		
		vtkImageMapper mapper = new vtkImageMapper();
        mapper.SetInputData(imageReader.GetOutput());
        mapper.SetColorWindow(256.0);
        mapper.SetColorLevel(128.0);
        vtkActor2D vtkImage = new vtkActor2D();
        vtkImage.SetMapper(mapper);
        vtkImage.SetPosition(x, y);
        
        return new LegendItem(vtkImage, source, imageFile.getName());
		
	}
}
