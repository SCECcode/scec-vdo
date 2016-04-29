package org.scec.vtk.plugins.SurfacePlugin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.scec.vtk.plugins.SurfacePlugin.Component.GoogleStaticMapsURLGenerator;
import org.scec.vtk.plugins.SurfacePlugin.Component.LatLonBoundingBox;
import org.scec.vtk.plugins.SurfacePlugin.Component.LoadedFilesProperties;
import org.scec.vtk.plugins.SurfacePlugin.Component.WMSLayer;
import org.scec.vtk.plugins.SurfacePlugin.Component.WMSService;
import org.scec.vtk.plugins.SurfacePlugin.Component.WMSStyle;
import org.scec.vtk.plugins.SurfacePlugin.Component.WMSUrlGenerator;
import org.scec.vtk.tools.Prefs;





public class MapSetCreatePluginGUI extends JFrame implements ActionListener, DocumentListener {

	private ImagePluginGUI parent;
	private static final long serialVersionUID = 1L;
	private JPanel instructionsPanel = new JPanel();
	private JPanel centerPanel = new JPanel();
	private JPanel topPanel = new JPanel();
	//private JPanel bottomPanel = new JPanel();
	private JPanel surfaceFilePanel = new JPanel();
	private JPanel surfaceCoordPanel = new JPanel();
	private JPanel surfaceLabelPanel = new JPanel();
	private JPanel surfaceCoordLabelPanel = new JPanel();
	private JPanel surfacePanel = new JPanel();
	private JPanel imageFilePanel = new JPanel();
	private JPanel imageCoordPanel = new JPanel();
	private JPanel imageLabelPanel = new JPanel();
	private JPanel imageCoordLabelPanel = new JPanel();
	private JPanel imagePanel = new JPanel();
	private JPanel addFilePanel = new JPanel();
	private JPanel buttonsPanel = new JPanel();
	private JPanel bottomPanel = new JPanel(new BorderLayout());
	private JPanel mainPanel = new JPanel();
	private JLabel iconLabel = new JLabel();
	private JLabel instructions = new JLabel("Enter the address of a surface(topography) or an image file to display.");
	protected JTextField surfaceFilePath = new JTextField("",30);
	protected JTextField imageFilePath = new JTextField("",30);
	protected JTextField imageRightLatitude = new JTextField("32",6); 
	protected JTextField imageLeftLatitude = new JTextField("43",6); 
	protected JTextField imageUpperLongitude = new JTextField("-125",6); 
	protected JTextField imageLowerLongitude = new JTextField("-114",6);
	protected JTextField imageAltitude = new JTextField("0.0",6);
//	protected JTextField surfaceRightLatitude = new JTextField("32.5",6); // default settings used for the California topography map 
//	protected JTextField surfaceLeftLatitude = new JTextField("42",6); 
//	protected JTextField surfaceUpperLongitude = new JTextField("-124.4",6); 
//	protected JTextField surfaceLowerLongitude = new JTextField("-114.1",6); 
	protected JTextField surfaceRightLatitude = new JTextField("32",6); 
	protected JTextField surfaceLeftLatitude = new JTextField("43",6); 
	protected JTextField surfaceUpperLongitude = new JTextField("-125",6); 
	protected JTextField surfaceLowerLongitude = new JTextField("-114",6); 
	protected JTextField surfaceAltitude = new JTextField("0.0",6);
	//private JLabel imageCoordinates = new JLabel("Image ");
	//private JLabel surfaceCoordinates = new JLabel("Surface ");
	private JLabel imageUpperLeftCorner = new JLabel("Upper left corner (lat,long): ");
	private JLabel imageLowerRightCorner = new JLabel("Lower right corner (lat,long): ");
	private JLabel imageAltitudeLabel = new JLabel("Altitude, in km:  ");
	private JLabel surfaceUpperLeftCorner = new JLabel("Upper left corner (lat,long): ");
	private JLabel surfaceLowerRightCorner = new JLabel("Lower right corner (lat,long): ");
	private JLabel surfaceAltitudeLabel = new JLabel("Altitude, in km:  ");
	private JLabel selectSurface = new JLabel("Surface(topography):  ");
	private JLabel selectImage = new JLabel("Image:    ");
	//private DefaultListModel surfaceListModel = new DefaultListModel();
	//private DefaultListModel imageListModel = new DefaultListModel();
	//private JList displayedImageList;
	//private JList displayedSurfaceList;
	private JButton displayButton = new JButton("Display");
	private JButton saveDisplayButton = new JButton("Save & Display");
	private JButton cancelButton = new JButton("Cancel");
	private JButton browseSurfaceButton = new JButton("...");
	private JButton browseImageButton = new JButton("...");
	private JLabel surfaceScaleLabel = new JLabel("Scale Factor: ");
	protected JTextField surfaceScale = new JTextField("1.0");

	private boolean surfaceFile = false;
	private boolean imageFile = false;

	
	private double[] surfaceData = new double[5];
	private double[] imageData = new double[5];

	private JLabel surfaceSourceLabel = new JLabel("Load Surface(topography) From: ");
	private JRadioButton surfaceDiskButton = new JRadioButton("Disk");
	private JRadioButton surfaceWebButton = new JRadioButton("Internet");
	private JLabel surfacePixelLabel = new JLabel("Pixel Spacing (1/2 min): ");
	private JTextField surfacePixelField = new JTextField("1",5);
	private JPanel surfaceSourcePanel = new JPanel(new BorderLayout());
	private JPanel surfaceSourceChoosePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	private boolean webSurface = false;
	private String surfaceServerURL = "http://scecdata.usc.edu/topo/cgi-bin/topo.pl";
	
	private JLabel imageSourceLabel = new JLabel("Load Image From: ");
	private JRadioButton imageDiskButton = new JRadioButton("Disk");
	private JRadioButton imageWebButton = new JRadioButton("Internet");
	private JLabel imagePixelLabel = new JLabel("Pixels/Degree: ");
	private JTextField imagePixelField = new JTextField("240",5);
	private JPanel imageSourcePanel = new JPanel(new BorderLayout());
	private JPanel imageSourceChoosePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	private boolean webImage = false;
	
	private JPanel imageLoadPanel = new JPanel(new BorderLayout());
	private JPanel imageWebLayerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	
	private JProgressBar downloadProgressBar = new JProgressBar(0,100);
	
	private JCheckBox reDownloadBox = new JCheckBox("Delete and Re-Download Internet Content?", false);
	private JCheckBox differentImageBox = new JCheckBox("Different Image Coordinates?", false);
	
	private File groupFile;
	private Document groupDoc;
	String surfaceError = "";
	
	WMSService wms = null;
	private String loadedFilePath;
	private String filePath;
	public static final double optimalSurfacePoints = 691200.0;
	public static final double optimalImagePoints = 1843200.0;
	private String imageError;
	private static final String wms_url = "http://www.nasa.network.com/wms";
	
	Logger log = Logger.getLogger(MapSetCreatePluginGUI.class);
	
	
	JComboBox mapLayersBox = new JComboBox();
	JComboBox mapStylesBox = new JComboBox();
	JLabel mapLayerLabel = new JLabel("Layer: ");
	JLabel mapStyleLabel = new JLabel("Style: ");
	private String googleName = "Google Static Maps";
	private boolean enableGoogle = false;
	private static final String wms_layer_default = "bmng200407";
	
	
	public MapSetCreatePluginGUI(ImagePluginGUI ipg){
	parent = ipg;
	ImageIcon pic = new ImageIcon("./scecvdo_sm.jpg");
	iconLabel = new JLabel(pic);
	instructionsPanel.setLayout(new GridLayout(1,1,10,10));
	instructionsPanel.add(instructions);
	FlowLayout layout = new FlowLayout();
	layout.setAlignment(FlowLayout.LEFT);
	topPanel.setLayout(new FlowLayout());
	topPanel.add(iconLabel);
	topPanel.add(instructionsPanel);
	surfaceDiskButton.setSelected(true);
	surfaceWebButton.setSelected(false);
	surfaceDiskButton.addActionListener(this);
	surfaceWebButton.addActionListener(this);
	surfacePixelField.setEnabled(false);
	surfaceSourceChoosePanel.add(surfaceSourceLabel);
	surfaceSourceChoosePanel.add(surfaceDiskButton);
	surfaceSourceChoosePanel.add(surfaceWebButton);
	surfaceSourceChoosePanel.add(surfacePixelLabel);
	surfaceSourceChoosePanel.add(surfacePixelField);
	surfaceFilePanel.setLayout(layout);
	surfaceFilePanel.add(selectSurface);
	surfaceFilePanel.add(surfaceFilePath);
	surfaceFilePanel.add(browseSurfaceButton);
	surfaceSourcePanel.add(reDownloadBox, BorderLayout.NORTH);
	surfaceSourcePanel.add(surfaceSourceChoosePanel, BorderLayout.CENTER);
	surfaceSourcePanel.add(surfaceFilePanel, BorderLayout.SOUTH);
	surfaceCoordPanel.setLayout(new GridLayout(4,2,10,10));
	surfaceCoordPanel.add(surfaceLeftLatitude);
	surfaceCoordPanel.add(surfaceUpperLongitude);
	surfaceCoordPanel.add(surfaceRightLatitude);
	surfaceCoordPanel.add(surfaceLowerLongitude);
	surfaceCoordPanel.add(surfaceAltitude);
	surfaceCoordPanel.add(new JLabel());
	surfaceCoordPanel.add(surfaceScale);
	surfaceLabelPanel.setLayout(new GridLayout(4,1,10,10));
	surfaceLabelPanel.add(surfaceUpperLeftCorner);
	surfaceLabelPanel.add(surfaceLowerRightCorner);
	surfaceLabelPanel.add(surfaceAltitudeLabel);
	surfaceLabelPanel.add(surfaceScaleLabel);
	surfaceCoordLabelPanel.setLayout(layout);
	surfaceCoordLabelPanel.add(surfaceLabelPanel);
	surfaceCoordLabelPanel.add(surfaceCoordPanel);
	surfacePanel.setLayout(new BorderLayout());
	surfacePanel.add(surfaceSourcePanel, BorderLayout.NORTH);
	surfacePanel.add(surfaceCoordLabelPanel, BorderLayout.CENTER);
	
	imageDiskButton.setSelected(true);
	imageWebButton.setSelected(false);
	imageDiskButton.addActionListener(this);
	imageWebButton.addActionListener(this);
	imagePixelField.setEnabled(false);
	imageSourceChoosePanel.add(imageSourceLabel);
	imageSourceChoosePanel.add(imageDiskButton);
	imageSourceChoosePanel.add(imageWebButton);
	imageSourceChoosePanel.add(imagePixelLabel);
	imageSourceChoosePanel.add(imagePixelField);
	
	imageFilePanel.add(selectImage);
	imageFilePanel.add(imageFilePath);
	imageFilePanel.add(browseImageButton);
	/*imageWebLayerPanel.add(mapLayerLabel);
	imageWebLayerPanel.add(mapLayersBox);
	imageWebLayerPanel.add(mapStyleLabel);
	imageWebLayerPanel.add(mapStylesBox);
	this.setImageLoadPanel(false);*/
	imageSourcePanel.add(imageSourceChoosePanel, BorderLayout.NORTH);
	imageLoadPanel.add(imageFilePanel, BorderLayout.NORTH);
	imageLoadPanel.add(imageWebLayerPanel, BorderLayout.SOUTH);
	imageSourcePanel.add(imageLoadPanel, BorderLayout.SOUTH);
	
	
	imageCoordPanel.setLayout(new GridLayout(3,2,10,10));
	imageCoordPanel.add(imageLeftLatitude);
	imageCoordPanel.add(imageUpperLongitude);
	imageCoordPanel.add(imageRightLatitude);
	imageCoordPanel.add(imageLowerLongitude);
	imageCoordPanel.add(imageAltitude);
	imageCoordPanel.add(new JLabel());
	imageLabelPanel.setLayout(new GridLayout(3,1,10,10));
	imageLabelPanel.add(imageUpperLeftCorner);
	imageLabelPanel.add(imageLowerRightCorner);
	imageLabelPanel.add(imageAltitudeLabel);
	imageCoordLabelPanel.setLayout(layout);
	imageCoordLabelPanel.add(imageLabelPanel);
	imageCoordLabelPanel.add(imageCoordPanel);
	imagePanel.setLayout(new BorderLayout());
	imagePanel.add(imageSourcePanel, BorderLayout.NORTH);
	imagePanel.add(differentImageBox, BorderLayout.CENTER);
	imagePanel.add(imageCoordLabelPanel, BorderLayout.SOUTH);
	centerPanel.setLayout(new BorderLayout());
	centerPanel.add(surfacePanel, BorderLayout.NORTH);
	centerPanel.add(imagePanel, BorderLayout.SOUTH);
	addFilePanel.setLayout(layout);
	
	differentImageBox.addActionListener(this);
	imageLeftLatitude.setEnabled(false);
	imageUpperLongitude.setEnabled(false);
	imageRightLatitude.setEnabled(false);
	imageLowerLongitude.setEnabled(false);
	imageAltitude.setEnabled(false);
	imageUpperLeftCorner.setEnabled(false);
	imageLowerRightCorner.setEnabled(false);
	imageAltitudeLabel.setEnabled(false);
	
	buttonsPanel.setLayout(new FlowLayout());
	buttonsPanel.add(displayButton);
	buttonsPanel.add(saveDisplayButton);
	buttonsPanel.add(cancelButton);
	mainPanel.setSize(1000,1050);
	mainPanel.setIgnoreRepaint(true);
	
	//setProgressString(" ");
	//setProgressStringPainted(true);
	
	bottomPanel.add(buttonsPanel, BorderLayout.NORTH);
	bottomPanel.add(downloadProgressBar, BorderLayout.SOUTH);

	mainPanel.setLayout(new BorderLayout());
	mainPanel.add(topPanel, BorderLayout.NORTH);
	mainPanel.add(centerPanel, BorderLayout.CENTER);
	mainPanel.add(bottomPanel, BorderLayout.SOUTH);
	
	displayButton.addActionListener(this);
	saveDisplayButton.addActionListener(this);
	cancelButton.addActionListener(this);
	browseSurfaceButton.addActionListener(this);
	browseImageButton.addActionListener(this);

	//displayButton.setEnabled(false);
	saveDisplayButton.setEnabled(false);
	
	//updateRecommendedWebResolutions();
	
	getContentPane().add(mainPanel);
	pack();
	setResizable(false);
	setVisible(true);																			//
	setLocationRelativeTo(null);
	setTitle("Add new: Surface/Image/Preset Surface & Image");
	
	//mapLayersBox.addActionListener(this);
	
	imageRightLatitude.getDocument().addDocumentListener(this);
	imageLeftLatitude.getDocument().addDocumentListener(this);
	imageUpperLongitude.getDocument().addDocumentListener(this);
	imageLowerLongitude.getDocument().addDocumentListener(this);
	surfaceRightLatitude.getDocument().addDocumentListener(this);
	surfaceLeftLatitude.getDocument().addDocumentListener(this);
	surfaceUpperLongitude.getDocument().addDocumentListener(this);
	surfaceLowerLongitude.getDocument().addDocumentListener(this);
	}
	
	public double[] setSurfaceData() {
		surfaceData = new double[5];
		try{
			surfaceData[0] = Double.parseDouble(this.surfaceLeftLatitude.getText());
			surfaceData[1] = Double.parseDouble(this.surfaceRightLatitude.getText());
			surfaceData[2] = Double.parseDouble(this.surfaceLowerLongitude.getText());
			surfaceData[3] = Double.parseDouble(this.surfaceUpperLongitude.getText());
			surfaceData[4] = Double.parseDouble(this.surfaceAltitude.getText());
		}
		catch(NumberFormatException e)
		{
			
		}
		return surfaceData;
	}
	
	public double[] setImageData() {
		imageData = new double[5];
		if (this.isDifferentImageCoords()) {
			imageData[0] = Double.parseDouble(this.imageLeftLatitude.getText());
			imageData[1] = Double.parseDouble(this.imageRightLatitude.getText());
			imageData[2] = Double.parseDouble(this.imageLowerLongitude.getText());
			imageData[3] = Double.parseDouble(this.imageUpperLongitude.getText());
			imageData[4] = Double.parseDouble(this.imageAltitude.getText());
			return imageData;
		} else {
			imageData = new double[5];
			imageData[0] = Double.parseDouble(this.surfaceLeftLatitude.getText());
			imageData[1] = Double.parseDouble(this.surfaceRightLatitude.getText());
			imageData[2] = Double.parseDouble(this.surfaceLowerLongitude.getText());
			imageData[3] = Double.parseDouble(this.surfaceUpperLongitude.getText());
			imageData[4] = Double.parseDouble(this.surfaceAltitude.getText());
			return imageData;
		}
	}

	
	public boolean isDifferentImageCoords() {
		return differentImageBox.isSelected();
	}
	
	
	public String saveToMemory() {
		int begin;
		int end;
		String surfaceTemp = new String();
		String imageTemp = new String();
		File groupDir = new File(
		            Prefs.getLibLoc() + 
		            File.separator + SurfacePlugin.dataStoreDir + 
		            File.separator + "data");
		    if (!groupDir.exists()) {
		        groupDir.mkdirs();
		    }
		    if(surfaceData != null){
		    	begin = surfaceFilePath.getText().lastIndexOf(File.separator) + 1;
		    	end = surfaceFilePath.getText().length()-1;
		    	if(surfaceFilePath.getText().endsWith(".txt"))
			    	end = surfaceFilePath.getText().indexOf(".txt");
			    else
			    	end = surfaceFilePath.getText().indexOf(".dem");
		    	    try {
						surfaceTemp = surfaceFilePath.getText().substring(begin,end);
					} catch (StringIndexOutOfBoundsException e) {
						surfaceTemp = "-";
					}
		    }
		    else {
		    	surfaceTemp = "-";
		    }
		    if(imageData != null &&  imageFilePath.getText().length() > 0){
//		    	System.out.println(mscg.imageFilePath.getText());
//		    	System.out.flush();
		    	String name = imageFilePath.getText();
		    	begin =name.lastIndexOf(File.separator);
		    	if (name.toLowerCase().contains(".jpg"))
		    		end = name.toLowerCase().indexOf(".jpg");
		    	else if (name.toLowerCase().contains(".png"))
		    		end = name.toLowerCase().indexOf(".png");
		    	else
		    		throw new IllegalStateException("Only JPG/PNG supported");
			    imageTemp = imageFilePath.getText().substring(begin+1,end);
		    }
		    else{
		    	imageTemp = "-";
		    }
		    loadedFilePath = groupDir.toString()+ File.separator + surfaceTemp + "_" + imageTemp + ".xml";			 
		    groupFile = new File(loadedFilePath);
		    if(!groupFile.exists()){
		    	groupDoc = createXML(false);
		    	writeToFile();
		    	return loadedFilePath;
		    }
		    else{
		    	JOptionPane.showMessageDialog(this, "Files already in memory!");
		    	return loadedFilePath;
		    }
	}
	private void writeToFile() {
        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            BufferedWriter xmlOut = new BufferedWriter(new FileWriter(this.groupFile,true));
            outputter.output(this.groupDoc, xmlOut);
            xmlOut.close();
        }
        catch (IOException e) {
//            log.debug("problem writing XML");
        }
    }
	private Document createXML(boolean groupFile){
		Element LoadedFilesRoot = setElements(groupFile);
		Document doc = new Document(LoadedFilesRoot);
		
		return doc;
	}
	private Element setElements(boolean groupFile) {
		Element surfaceImageRoot = new Element("SurfaceImage");
		Element surface = new Element("surface");
		Element image = new Element("image");
		Element group = new Element("group");
		Element imageSourceFile = new Element("imageSourceFile");
		Element surfaceSourceFile = new Element("surfaceSourceFile");
		Element imageCoordinates = new Element("imageCoordinates");
		Element surfaceCoordinates = new Element("surfaceCoordinates");
		if(surfaceData != null){
			surfaceSourceFile.setText(this.surfaceFilePath.getText());
			String coord = surfaceData[0] + " " + surfaceData[1] + " " 
						   + surfaceData[2] + " " + surfaceData[3] + " " + surfaceData[4];
			surfaceCoordinates.setText(coord);
		}
		else{
			surfaceSourceFile.setText("-");
			surfaceCoordinates.setText("-");
		}
		surface.addContent(surfaceSourceFile);
		surface.addContent(surfaceCoordinates);
		if(imageData != null){
			imageSourceFile.setText(this.imageFilePath.getText());
			String coord = imageData[0] + " " + imageData[1] + " " 
						   + imageData[2] + " " + imageData[3] + " " + imageData[4];
			imageCoordinates.setText(coord);
		}
		else{
			imageSourceFile.setText("-");
			surfaceCoordinates.setText("-");
		}
		if(groupFile && filePath!=null){
			group.setText(filePath);
		}
		else
			group.setText("-");
		image.addContent(imageSourceFile);
		image.addContent(imageCoordinates);
		surfaceImageRoot.addContent(surface);
		surfaceImageRoot.addContent(image);
		surfaceImageRoot.addContent(group);
		return surfaceImageRoot;
	}

	private void setProgressIndeterminate(boolean ind) {
		// TODO Auto-generated method stub
		final boolean ind2 = ind;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				downloadProgressBar.setIndeterminate(ind2);
			}
		});
	}

	private void setProgressStringPainted(boolean painted) {
		// TODO Auto-generated method stub
		final boolean painted2 = painted;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				downloadProgressBar.setStringPainted(painted2);
			}
		});
	}

	private void setProgressString(String str) {
		// TODO Auto-generated method stub
		final String str2 = str;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				downloadProgressBar.setString(str2);
			}
		});
	}

	
	private boolean loadSurfaceFromWeb() {
		surfaceError = "";
		
		setProgressString("Downloading Surface...");
		setProgressStringPainted(true);
		setProgressIndeterminate(true);
		
//		String tempPath = Geo3dInfo.getRootPluginDir()+File.separator+"DEMs"+File.separator+"Downloaded"+File.separator;
		String tempPath = Prefs.getLibLoc() + File.separator + SurfacePlugin.dataStoreDir + File.separator + "DEMs" + File.separator;
		File tempDir = new File(tempPath);
		if (!tempDir.isDirectory())
			tempDir.mkdirs();
		double[] region;
		try {
			region = setSurfaceData();
		} catch (NumberFormatException e) {
			setProgressString("The selected range is invalid!");
			setProgressIndeterminate(false);
			e.printStackTrace();
			return false;
		}
		double latN = region[0];
		double latS = region[1];
		double lonE = region[2];
		double lonW = region[3];
		int ndel = Integer.parseInt(surfacePixelField.getText());
		
		double numSurfacePts = Math.abs(latN - latS) * Math.abs(lonE - lonW) * Math.pow(120d / (double)ndel, 2);
		if (numSurfacePts > (optimalSurfacePoints * 4)) {
			System.out.println("Trying to download something too big...");
			setProgressString("Surface is too large, increase Pixel Spacing!");
			setProgressIndeterminate(false);
			return false;
		}
		
		String filename = String.valueOf(latN).replace(".", "p") + "_" + String.valueOf(latS).replace(".", "p") + "_" + String.valueOf(lonW).replace(".", "p") + "_" + String.valueOf(lonE).replace(".", "p") + "_" + ndel + ".txt";
		System.out.println("Downloading topography to: " + tempPath + filename);
		
		File outFile = new File(tempPath + filename);
		
		boolean reDownload = reDownloadBox.isSelected();
		
		if (outFile.exists()) {
			System.out.print("You've already downloaded this...");
			if (reDownload) {
				System.out.print("deleting...");
				reDownload = outFile.delete();
			}
			System.out.println();
		} 
		if (!outFile.exists() || reDownload) {
			URL url;
			try {
				url = new URL(surfaceServerURL + "?latN=" + latN + "&latS=" + latS + "&lonW=" + lonW + "&lonE=" + lonE + "&ndel=" + ndel);
				
				System.out.println("Downloading from: " + surfaceServerURL + "?latN=" + latN + "&latS=" + latS + "&lonW=" + lonW + "&lonE=" + lonE + "&ndel=" + ndel);
				
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				BufferedWriter out;
				
				String line;
				
//				if (in.ready()) {
					line = in.readLine();
					
					if (line.contains("<MOVED>")) {
						surfaceServerURL = line.substring(line.indexOf("<MOVED>") + 7, line.indexOf("</MOVED>"));
						System.out.println("Web service moved to: " + surfaceServerURL);
						return loadSurfaceFromWeb();
					}
					
					if (line.contains("ERROR")) {
						surfaceError = "Surface service suplied incorrect paramaters!";
						System.out.println(surfaceError);
						setProgressString("The selected range is invalid!");
						setProgressIndeterminate(false);
						return false;
					}
					System.out.println(outFile);
					System.out.flush();
					out = new BufferedWriter(new FileWriter(outFile));
					
					out.append(line + "\n");
				
					while ((line = in.readLine()) != null) {
						out.append(line + "\n");
					}
				
					out.flush();
					out.close();
//				}
			} catch (MalformedURLException e) {
				setProgressString("Malformed URL Exception!");
				setProgressIndeterminate(false);
				imageError = "Malformed URL Exception";
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				setProgressString("IO Exception! Check internet connection.");
				setProgressIndeterminate(false);
				imageError = "IO Exception";
				e.printStackTrace();
				return false;
			}
		}
		
		surfaceFilePath.setText(outFile.getAbsolutePath());
		surfaceFile = true;
		
		setProgressIndeterminate(false);
		
		return true;
	}
	private boolean loadImageFromWeb() {
		imageError = "";
		
		setProgressString("Downloading Surface Image...");
		setProgressStringPainted(true);
		setProgressIndeterminate(true);
		
//		String tempPath = Geo3dInfo.getRootPluginDir()+File.separator+"Maps"+File.separator+"Downloaded"+File.separator;
		String tempPath = Prefs.getLibLoc() + File.separator + SurfacePlugin.dataStoreDir + File.separator + "Maps" + File.separator;
		File tempDir = new File(tempPath);
		if (!tempDir.isDirectory())
			tempDir.mkdirs();
		double[] region;
		try {
			region = setImageData();
		} catch (NumberFormatException e) {
			setProgressString("The selected range is invalid!");
			setProgressIndeterminate(false);
			//e.printStackTrace();
			return false;
		}
		double latN = region[0];
		double latS = region[1];
		double lonE = region[2];
		double lonW = region[3];
		int pixelsPerDegree = Integer.parseInt(imagePixelField.getText());
		String layer = "";
		String style = "";
		String filename = String.valueOf(latN).replace(".", "p") + "_" + String.valueOf(latS).replace(".", "p") +
		"_" + String.valueOf(lonW).replace(".", "p") + "_" + String.valueOf(lonE).replace(".", "p") +
		"_" + pixelsPerDegree;
		LatLonBoundingBox box = null;
		boolean useWMS = true;
		if (this.mapLayersBox.getSelectedItem() instanceof WMSLayer) {
			layer = ((WMSLayer)mapLayersBox.getSelectedItem()).getName();
			if (mapStylesBox.getSelectedItem() != null)
				style = ((WMSStyle)mapStylesBox.getSelectedItem()).getName();
			box = ((WMSLayer)mapLayersBox.getSelectedItem()).getBox();
			filename += "_" + layer;
			if (style.length() > 0)
				filename += "_" + style;
		} else {
			useWMS = false;
			filename += "_google";
		}
		filename += ".jpg";
		System.out.println("Downloading Surface Image to: " + tempPath + filename);
		
		File outFile = new File(tempPath + filename);
		
		boolean reDownload = reDownloadBox.isSelected();
		
		if (outFile.exists()) {
			System.out.print("You've already downloaded this...");
			if (reDownload) {
				System.out.print("deleting...");
				reDownload = outFile.delete();
			}
			System.out.println();
		} 
		if (!outFile.exists() || reDownload) {
			URL url;
			try {
				BufferedImage image;
				String type="jpg";
				if (useWMS) {
					//JR: Get IMAGE from jpl by determining the url from the requested coordinates
					//
					WMSUrlGenerator wms = new WMSUrlGenerator(this.wms.getBaseUrl(), layer, style);
					url =  new URL(wms.toWMSURL(latS, latN, lonE, lonW, pixelsPerDegree));
					System.out.println("Downloading image from: " + url);
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				
					if (!conn.getContentType().contains("jpeg")) {
						BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line = "";
						while (in.ready() && !(line = in.readLine()).contains("<ServiceException>"));
						
						imageError = in.readLine();
						
						while (imageError.charAt(0) == ' ') {
							imageError = imageError.substring(1);
						}
						
						System.out.println("WMS ERROR: " + imageError);
						
						setProgressString(imageError);
						setProgressIndeterminate(false);
						
						return false;
					}
					
					image = ImageIO.read(conn.getInputStream());
				}
				else 
				{
					// google
					url = new URL(GoogleStaticMapsURLGenerator.toURL(latS, latN, lonE, lonW, pixelsPerDegree));
					System.out.println("Downloading image from: " + url);
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				
					if (!(conn.getContentType().contains("jpeg")||conn.getContentType().contains("png"))) {
						System.out.println("AHHHHHHHHH!");
						
						setProgressString(imageError);
						setProgressIndeterminate(false);
						
						return false;
					}
					if(conn.getContentType().contains("png"))
					{
						type="png";
					}
					image = ImageIO.read(conn.getInputStream());
				}
				ImageIO.write(image, type, outFile);
			} catch (MalformedURLException e) {
				setProgressString("Malformed URL Exception!");
				setProgressIndeterminate(false);
				imageError = "Malformed URL Exception";
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				setProgressString("IO Exception! Check internet connection or decrease Pixels/Degree");
				setProgressIndeterminate(false);
				imageError = "IO Exception";
				e.printStackTrace();
				return false;
			}
		}
		
		imageFilePath.setText(outFile.getAbsolutePath());
		imageFile = true;
		
		setProgressIndeterminate(false);
		
		return true;
	}
	
	public void createSurface(LoadedFilesProperties temp, ImagePluginGUI ipg) {
		double[] ul = new double[3];
		double[] lr = new double[3];
		try {
			double ulLat = surfaceData[0];
			double lrLat = surfaceData[1];
			double lrLong = surfaceData[2];
			double ulLong =surfaceData[3];
		
			ul[0] = ulLat;
			ul[1] = ulLong;
			ul[2] = surfaceData[4];
			lr[0] = lrLat;
			lr[1] = lrLong;
			lr[2] = surfaceData[4];
			
			if (ulLat <= lrLat || ulLong >= lrLong || ulLat>90 || ulLong<-180 || lrLat<-90 || lrLong>180) {
				JOptionPane.showMessageDialog(this, "Bad coordinates.", "Oops!", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
		
			temp.addGeographicSurfaceInfo(new GeographicSurfaceInfo(temp.getSurfaceFilePath(), ul, lr));
				ipg.getImagePluginGUIParent().display(temp.getGeoInfo());
				System.out.println("ipg == null");
			
			
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Bad data.");
		}
		
	}
	public void setFilePath(String filePath){
		this.filePath = filePath;
	}
	public void createImage(LoadedFilesProperties temp, ImagePluginGUI ipg) {
		double[] ul = new double[3];
		double[] lr = new double[3];
		try {
			double ulLat = imageData[0];
			double lrLat = imageData[1];
			double lrLong = imageData[2];
			double ulLong =imageData[3];
			ul[0] = ulLat;
			ul[1] = ulLong;
			ul[2] = imageData[4];
			lr[0] = lrLat;
			lr[1] = lrLong;
			lr[2] = imageData[4];
			System.out.println(ulLat + " " + ulLong + " " + lrLat + " " + lrLong);
			boolean meshType = false;
//			if (mesh.isSelected()==true) {
//			meshType = true;
//			}
			if (ulLat <= lrLat || ulLong==lrLong || ulLat>90 || ulLong<-180 || lrLat<-90 || lrLong>180) {
				JOptionPane.showMessageDialog(this, "Bad coordinates.", "Oops!", JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (ulLong > lrLong) {
				int option = JOptionPane.showConfirmDialog(this, "Do you want this image across the International Date Line?", "Across hemispheres?", JOptionPane.YES_NO_OPTION);
				if (option==JOptionPane.NO_OPTION) {
					return;
				} else {
					lr[1] = lr[1]+360; //to deal with the fact that the lr.y < ul.y, but that's ok
				}
			}
			
			    temp.addImageInfo(new ImageInfo(temp.getImageFilePath(), ul, lr, meshType));
			    ipg.getImagePluginGUIParent().display(temp.getImgInfo());
			    temp.setShow(true);
			    temp.setPlot(true);
			
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Bad data.");
		}
	}

	
	
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void insertUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object source = e.getSource();
		
		if (source == surfaceDiskButton) {
			surfaceWebButton.setSelected(!surfaceDiskButton.isSelected());
			
			webSurface = surfaceWebButton.isSelected();
			if (webSurface) {
				surfaceFilePath.setEnabled(false);
				surfacePixelField.setEnabled(true);
				browseSurfaceButton.setEnabled(false);
				displayButton.setEnabled(true);
			} else {
				surfaceFilePath.setEnabled(true);
				surfacePixelField.setEnabled(false);
				browseSurfaceButton.setEnabled(true);
				if (!surfaceFile && !imageFile && !webImage)
					displayButton.setEnabled(false);
			}
		} else if (source == surfaceWebButton) {
			surfaceDiskButton.setSelected(!surfaceWebButton.isSelected());
			
			webSurface = surfaceWebButton.isSelected();
			if (webSurface) {
				surfaceFilePath.setEnabled(false);
				surfacePixelField.setEnabled(true);
				browseSurfaceButton.setEnabled(false);
				displayButton.setEnabled(true);
			} else {
				surfaceFilePath.setEnabled(true);
				surfacePixelField.setEnabled(false);
				browseSurfaceButton.setEnabled(true);
				if (!surfaceFile && !imageFile && !webImage)
					displayButton.setEnabled(false);
			}
		}
		else if (source == imageDiskButton) {
			imageWebButton.setSelected(!imageDiskButton.isSelected());
			
			webImage = imageWebButton.isSelected();
			this.setImageLoadPanel(webImage);
			if (webImage) {
				imagePixelField.setEnabled(true);
				displayButton.setEnabled(true);
			} else {
				imagePixelField.setEnabled(false);
				if (!surfaceFile && !imageFile && !webSurface)
					displayButton.setEnabled(false);
			}
		} else if (source == imageWebButton) {
			imageDiskButton.setSelected(!imageWebButton.isSelected());
			
			webImage = imageWebButton.isSelected();
			this.setImageLoadPanel(webImage);
			if (webImage) {
				imagePixelField.setEnabled(true);
				displayButton.setEnabled(true);
			} else {
				imagePixelField.setEnabled(false);
				if (!surfaceFile && !imageFile && !webSurface)
					displayButton.setEnabled(false);
			}
		}
		if(source == displayButton)
		{
			new Thread() {
				public void run() {
					displayButton.setEnabled(false);
					saveDisplayButton.setEnabled(false);
					cancelButton.setEnabled(false);
					
					boolean fail = false;
					if (webSurface) {
						if (!loadSurfaceFromWeb()) {
							fail = true;
						}
					}
					//image file from http://www.nasa.network.com/wms?version=1.1.1&request=GetMap&Layers=bmng200407&format=image/jpeg&BBOX=-125.0,32.0,-114.0,43.0&width=2640&height=2640&SRS=EPSG:4326&Styles=
					//ipg == null
					if (webImage && !fail) {
						if (!loadImageFromWeb()) {
							fail = true;
						}
					}
					
					if (!fail) {
						
						setProgressString("Displaying Surface...");
						setProgressStringPainted(true);
						setProgressIndeterminate(true);
						
						String loadedFilePath;
			
						/*if((surfaceFile) && !imageFile)
						{   
							parent.getImagePluginGUIParent().setScaleFactor(Double.parseDouble(surfaceScale.getText()));
							surfaceData = parent.setSurfaceData();
							loadedFilePath = parent.saveToMemory();
							if(loadedFilePath == null) {
								setProgressString("Couldn't Find File...");
								setProgressIndeterminate(false);
								return;
							}
							LoadedFilesProperties lfp;
							lfp = new LoadedFilesProperties("-", null, surfaceFilePath.getText(),surfaceData,null,false,loadedFilePath);
							lfp.setPlot(true);
							lfp.setShow(true);
							parent.createSurface(lfp,ipg);
							ipg.getImagePluginGUIParent().addSurfaceImage(lfp,ipg);
							dispose();
						}
						else if(!surfaceFile && imageFile)
						{
							ipg.getImagePluginGUIParent().setScaleFactor(Double.parseDouble(surfaceScale.getText()));
							imageData = parent.setImageData();
							loadedFilePath = parent.saveToMemory();
							if(loadedFilePath == null) {
								setProgressString("Couldn't Find File...");
								setProgressIndeterminate(false);
								return;
							}
							LoadedFilesProperties lfp = new LoadedFilesProperties(imageFilePath.getText(), imageData,"-",null,null,false,loadedFilePath);
							lfp.setPlot(true);
							lfp.setShow(true);
							parent.createImage(lfp,ipg.getImagePluginGUIParent());
							ipg.getImagePluginGUIParent().addSurfaceImage(lfp,ipg);
							dispose();
						}*/
						//else
							if(surfaceFile && imageFile)
						{	
							parent.getImagePluginGUIParent().setScaleFactor(Double.parseDouble(surfaceScale.getText()));
							surfaceData = setSurfaceData();
							imageData = setImageData();
							loadedFilePath = saveToMemory();
							if(loadedFilePath == null) {
								setProgressString("Couldn't Find File...");
								setProgressIndeterminate(false);
								return;
							}
							LoadedFilesProperties lfp = new LoadedFilesProperties(imageFilePath.getText(), imageData, surfaceFilePath.getText(), surfaceData, null,false,loadedFilePath);
							lfp.setPlot(true);
							lfp.setShow(true);
							createSurface(lfp,parent);
							createImage(lfp,parent);
							parent.getImagePluginGUIParent().addSurfaceImage(lfp,parent);
							dispose();
						}
					}
					if (fail)  {
						displayButton.setEnabled(true);
						if ((surfaceFile || webSurface) && (imageFile || webImage))
							saveDisplayButton.setEnabled(true);
						cancelButton.setEnabled(true);
					}
				}
			}.start();
		}
	}

	private void setImageLoadPanel(boolean internet) {
		if (internet && wms == null) {
			boolean retry = true;
			while (retry) {
				log.debug("Downloading WMS capabilities XML from: " + wms_url);
				try {
					wms = WMSService.fromXML(wms_url);
					retry = false;
				} catch (Exception e) {
					e.printStackTrace();
					String message = "Error contacting Web Map Service.\n" +
					"This means that the service is down or overloaded,\n" +
					"or you are not connected to the internet.\n" +
					"\nRetry?";
					String title = "WMS Error";
					int ret = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
					if (ret == 0) {
						log.debug("Retrying WMS capabilities download...");
						retry = true;
					} else {
						retry = false;
						this.imageDiskButton.doClick();
						return;
					}
				}
			}
		}
		if (internet) {
			mapLayersBox.removeAllItems();
			if (enableGoogle)
				mapLayersBox.addItem(googleName);
			int index = 0;
			if (wms != null) {
				for (WMSLayer layer : wms.getLayers()) {
					mapLayersBox.addItem(layer);
					if (layer.getName().equals(wms_layer_default))
						if (enableGoogle)
							index = wms.getLayers().indexOf(layer) + 1;
						else
							index = wms.getLayers().indexOf(layer);
				}
			}
			mapLayersBox.setSelectedIndex(index);
		} else if (mapLayersBox.getItemCount() == 0){
			mapLayersBox.removeAllItems();
			mapLayersBox.addItem("< none >");
			mapStylesBox.removeAllItems();
			mapStylesBox.addItem("< none >");
		}
		this.imageFilePanel.setEnabled(!internet);
		this.browseImageButton.setEnabled(!internet);
		this.imageFilePath.setEnabled(!internet);
		this.selectImage.setEnabled(!internet);
		this.imageWebLayerPanel.setEnabled(internet);
		this.mapLayersBox.setEnabled(internet);
		this.mapStylesBox.setEnabled(internet);
		this.mapLayerLabel.setEnabled(internet);
		this.mapStyleLabel.setEnabled(internet);
		this.imageLoadPanel.repaint();
	}
	
}
