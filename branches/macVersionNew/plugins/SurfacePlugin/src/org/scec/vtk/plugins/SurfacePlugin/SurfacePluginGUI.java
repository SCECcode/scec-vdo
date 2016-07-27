package org.scec.vtk.plugins.SurfacePlugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.SurfacePlugin.Component.LoadedFilesProperties;
import org.scec.vtk.plugins.utils.components.AddButton;
import org.scec.vtk.plugins.utils.components.RemoveButton;
import org.scec.vtk.plugins.utils.components.ShowButton;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;

import com.google.common.base.Throwables;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkFloatArray;
import vtk.vtkJPEGReader;
import vtk.vtkPNGReader;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolygon;
import vtk.vtkTexture;
import vtk.vtkTriangleStrip;

public class SurfacePluginGUI extends JPanel implements ActionListener,ChangeListener,ListSelectionListener, TableModelListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel mainPanel = new JPanel();
	private JPanel topPanel = new JPanel();
	private JPanel bottomPanel = new JPanel();
	private JPanel sliderPanel = new JPanel();
	private JPanel panesPanel = new JPanel();
	private JPanel defaultPanel = new JPanel();
	private JPanel checkboxPanel = new JPanel();
	private JScrollPane defaultScrollPane = new JScrollPane(checkboxPanel);
	private JLabel defaultSurfaces = new JLabel("Default Surfaces:");
	private JCheckBox wm=new JCheckBox("World Map");
	private JCheckBox sc=new JCheckBox("Southern California Map");
	private JCheckBox cm=new JCheckBox("California Map");
	private JCheckBox jm=new JCheckBox("Japan Map");
	private JCheckBox nz=new JCheckBox("New Zealand Map");
	private JCheckBox im=new JCheckBox("Indonesia Map");
	private JCheckBox hm=new JCheckBox("Haiti Map");
	private JCheckBox mm=new JCheckBox("Mexico Map");
	private JCheckBox sa=new JCheckBox("South America Map");
	private JCheckBox cd=new JCheckBox("California DEM");
	private JCheckBox cdc=new JCheckBox("California DEM Colored");

	//private ButtonGroup bg=new ButtonGroup();

	private JLabel addRemLabel = new JLabel("Add / Remove ");
	protected RemoveButton   remISButton;
	private AddButton   newISButton;

	protected ShowButton showImageButton;
	private JSlider transparencySlider = new JSlider(); 

	String[] columnNames ={
			"Visible",
			"Image(s)",
			"Surface(s)"
	};
	public SurfaceTableModel surfaceTableModel = new SurfaceTableModel(columnNames);
	public JTable surfaceTable = new JTable(surfaceTableModel);
	public static Vector<Surface> surfaceArray = new Vector<Surface>();
	private MapSetCreatePluginGUI mscpg;
	private PluginActors surfaceActors;
	private String filename;
	private double n;
	private double w;
	private double s;
	private double e;
	private double altitude;
	private BufferedReader demReader;
	private int horizontalItems;
	private int longIncrements;
	private double scaleFactor;
	private ArrayList<double[]> data = new ArrayList<double[]>();
	private int latIncrements;
	public SurfacePluginGUI(PluginActors surfaceActors) {
		// TODO Auto-generated constructor stub
		this.surfaceActors = surfaceActors;
		newISButton = new AddButton(this,"Add a new surface/image");
		remISButton = new RemoveButton(this,"Remove selected surface/image");
		showImageButton = new ShowButton(this,"Toggle visibility of selected image(s)");


		surfaceTable.setLayout(new GridLayout(1,3));
		surfaceTable.setPreferredScrollableViewportSize(new Dimension(350, 70));
		surfaceTable.getColumnModel().getColumn(0).setMaxWidth(116);
		surfaceTable.getColumnModel().getColumn(1).setMaxWidth(116);
		surfaceTable.getColumnModel().getColumn(2).setMaxWidth(118);
		surfaceTable.getSelectionModel().addListSelectionListener(this);
		surfaceTableModel.addTableModelListener(this);

		JScrollPane scrollPane = new JScrollPane(surfaceTable);

		panesPanel.setLayout(new GridLayout(1,2,10,10));
		panesPanel.add(scrollPane);

		topPanel.add(panesPanel, BorderLayout.CENTER);

		JPanel bar = new JPanel();
		bar.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
		bar.setLayout(new BoxLayout(bar,BoxLayout.LINE_AXIS));
		bar.setOpaque(true);
		int buttonSpace = 3;

		bar.add(this.showImageButton);
		bar.add(Box.createHorizontalGlue());
		bar.add(addRemLabel);
		bar.add(this.newISButton);
		bar.add(Box.createHorizontalStrut(buttonSpace));
		bar.add(this.remISButton);
		bar.add(Box.createHorizontalStrut(buttonSpace));
		newISButton.setEnabled(true);
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(sliderPanel, BorderLayout.NORTH);
		bottomPanel.add(bar, BorderLayout.SOUTH);

		transparencySlider.setMajorTickSpacing(10);
		transparencySlider.setMinorTickSpacing(5);
		transparencySlider.setPaintLabels(true); 
		transparencySlider.setPaintTicks(true);
		transparencySlider.addChangeListener(this);
		transparencySlider.setEnabled(false);

		sliderPanel.setLayout(new BorderLayout());
		sliderPanel.add(new JLabel("Transparency"),BorderLayout.NORTH);
		sliderPanel.add(transparencySlider,BorderLayout.CENTER);

		wm.addActionListener(this); wm.setName("wm"); 
		sc.addActionListener(this); sc.setName("sc"); 
		cm.addActionListener(this); cm.setName("cm"); 
		jm.addActionListener(this); jm.setName("jm"); 
		nz.addActionListener(this); nz.setName("nz"); 
		im.addActionListener(this); im.setName("im"); 
		hm.addActionListener(this); hm.setName("hm"); 
		mm.addActionListener(this); mm.setName("mm"); 
		sa.addActionListener(this); sa.setName("sa"); 
		cd.addActionListener(this); cd.setName("cd"); 
		cdc.addActionListener(this); cdc.setName("cdc"); 
		
		defaultPanel.add(defaultSurfaces);
		defaultPanel.setLayout(new BoxLayout(defaultPanel,BoxLayout.Y_AXIS));
		defaultPanel.setPreferredSize(new Dimension(300,120));
		defaultScrollPane.setPreferredSize(new Dimension(300,120));
		checkboxPanel.add(cm);
		checkboxPanel.add(sc);
		checkboxPanel.add(cd);
		checkboxPanel.add(cdc);
		checkboxPanel.add(wm);
		checkboxPanel.add(mm);
		checkboxPanel.add(sa);
		checkboxPanel.add(jm);
		checkboxPanel.add(im);
		checkboxPanel.add(nz);
		checkboxPanel.add(hm);
		checkboxPanel.setLayout(new GridLayout(0,2));
		defaultPanel.add(defaultScrollPane);


		mainPanel.setLayout(new BorderLayout(5, 5));
		mainPanel.add(topPanel, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		mainPanel.add(defaultPanel,BorderLayout.NORTH);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		mainPanel.setPreferredSize(new Dimension(Prefs.getPluginWidth(), Prefs.getPluginHeight()/2));
		add(mainPanel);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		Object src = e.getSource();
		if(src == transparencySlider)
		{
			double transparency = ((double)(transparencySlider.getValue())/100.0);
			ListSelectionModel model = surfaceTable.getSelectionModel();
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
				int row = model.getMinSelectionIndex();
				setTransparency(surfaceArray.get(row),transparency);
			}
			Info.getMainGUI().updateRenderWindow();
		}

	}
	public void valueChanged(ListSelectionEvent e) {

		Object src = e.getSource();
		this.surfaceTable.getModel();
		if (e.getValueIsAdjusting()) return;

		if (src == this.surfaceTable.getSelectionModel()) {
			processTableSelectionChange();
		}
	}

	public void setScaleFactor(double newScale) {
		// TODO Auto-generated method stub
		scaleFactor = newScale;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object source = e.getSource();
		LoadedFilesProperties lfp;

		if (source == newISButton) {
			mscpg = new MapSetCreatePluginGUI(this);
		}
		if (source == cm) {
			if(cm.isSelected()){
				double imageData[] = new double[5];
				imageData[0] = 42.2;
				imageData[1] = 32.1;
				imageData[2] = -113.4;
				imageData[3] = -124.5;
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="CaliforniaFull"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";
				System.out.println("Loaded File Path: "+loadedFilePath);
				lfp = new LoadedFilesProperties(Info.getMainGUI().getRootPluginDir()+File.separator+"Maps"+File.separator+imageTemp+imageExt, imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this);
//				CheckBoxList.add(cm);
			}
			else
			{
				removePresetObject(cm);
			}
			System.out.println("surfaceArray list is " + surfaceArray);
		}
		if (source == wm) {
			//mscpg = new MapSetCreatePluginGUI(this);
			if(wm.isSelected()){
				double imageData[] = new double[5];
				imageData[0] = 90;
				imageData[1] = -90;
				imageData[2] = 180;
				imageData[3] = -180;
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="world.topo.bathy.200410.3x5400x2700";//image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";
				System.out.println("Loaded File Path: "+loadedFilePath);
				lfp = new LoadedFilesProperties(Info.getMainGUI().getRootPluginDir()+File.separator+"Maps"+File.separator+imageTemp+imageExt, imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this);
			}
			else
			{
				removePresetObject(wm);
			}
		}
		if (source == sc) {
			//mscpg = new MapSetCreatePluginGUI(this);
			if(sc.isSelected()){
				double imageData[] = new double[5];
				imageData[0] = 36;
				imageData[1] = 32.5;
				imageData[2] = -114;
				imageData[3] = -122;
				imageData[4] = 0;

				String surfaceTemp="-";
				String imageTemp="largesocal";//image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";
				System.out.println("Loaded File Path: "+loadedFilePath);
				lfp = new LoadedFilesProperties(Info.getMainGUI().getRootPluginDir()+File.separator+"Maps"+File.separator+imageTemp+imageExt, imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this);
			}
			else
			{
				removePresetObject(sc);
			}
		}
		if (source == jm) {
			//mscpg = new MapSetCreatePluginGUI(this);
			if(jm.isSelected()){
				double imageData[] = new double[5];
				imageData[0] = 46.9; //Latitude Max
				imageData[1] = 26.2; //Latitude Min
				imageData[2] = 147; //Longitude Max
				imageData[3] = 127.1; //Longitude Min
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="Japan"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";
				System.out.println("Loaded File Path: "+loadedFilePath);
				lfp = new LoadedFilesProperties(Info.getMainGUI().getRootPluginDir()+File.separator+"Maps"+File.separator+imageTemp+imageExt, imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this);
			}
			else
			{
				removePresetObject(jm);
			}
		}
		if (source == nz) {
			//mscpg = new MapSetCreatePluginGUI(this);
			if(nz.isSelected()){
				double imageData[] = new double[5];
				imageData[0] = -34.3; //Latitude Max
				imageData[1] = -47.5; //Latitude Min
				imageData[2] = 179.4; //Longitude Max
				imageData[3] = 165.4; //Longitude Min

				String surfaceTemp="-";
				String imageTemp="NewZealand"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";
				System.out.println("Loaded File Path: "+loadedFilePath);
				lfp = new LoadedFilesProperties(Info.getMainGUI().getRootPluginDir()+File.separator+"Maps"+File.separator+imageTemp+imageExt, imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this);
			}
			else
			{
				removePresetObject(nz);
			}
		}
		if (source == im) {
			//mscpg = new MapSetCreatePluginGUI(this);
			if(im.isSelected()){
				double imageData[] = new double[5];
				imageData[0] = 9.4; //Latitude Max
				imageData[1] = -12.0; //Latitude Min
				imageData[2] = 148.4; //Longitude Max
				imageData[3] = 93.8; //Longitude Min
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="Indonesia"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";
				System.out.println("Loaded File Path: "+loadedFilePath);
				lfp = new LoadedFilesProperties(Info.getMainGUI().getRootPluginDir()+File.separator+"Maps"+File.separator+imageTemp+imageExt, imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this);
			}
			else
			{
				removePresetObject(im);
			}
		}
		if (source == hm) {
			//if changing map files or adding files please update switch case below as per file name so as to uncheck if preset is removed
			if(hm.isSelected()){
				double imageData[] = new double[5];
				imageData[0] = 20.5; //Latitude Max
				imageData[1] = 17.5; //Latitude Min
				imageData[2] = -68.5; //Longitude Max
				imageData[3] = -74.5; //Longitude Min
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="Haiti"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";
				System.out.println("Loaded File Path: "+loadedFilePath);
				lfp = new LoadedFilesProperties(Info.getMainGUI().getRootPluginDir()+File.separator+"Maps"+File.separator+imageTemp+imageExt, imageData,"-",null,null,false,loadedFilePath);

				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this);
			}
			else
			{
				removePresetObject(hm);
			}
		}
		if (source == mm) {
			//mscpg = new MapSetCreatePluginGUI(this);
			if(mm.isSelected()){
				double imageData[] = new double[5];
				imageData[0] = 35.4; //Latitude Max
				imageData[1] = 12.8; //Latitude Min
				imageData[2] = -82.9; //Longitude Max
				imageData[3] = -119; //Longitude Min
				imageData[4] = -8; //Altitude

				String surfaceTemp="-";
				String imageTemp="Mexico"; //image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";
				System.out.println("Loaded File Path: "+loadedFilePath);
				lfp = new LoadedFilesProperties(Info.getMainGUI().getRootPluginDir()+File.separator+"Maps"+File.separator+imageTemp+imageExt, imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this);
			}
			else
			{
				removePresetObject(mm);
			}
		}
		if (source == sa) {
			//mscpg = new MapSetCreatePluginGUI(this);
			if(sa.isSelected()){
				double imageData[] = new double[5];
				imageData[0] = 14.7; //Latitude Max 6.3
				imageData[1] = -57; //Latitude Min -58
				imageData[2] = -36; //Longitude Max
				imageData[3] = -82.4; //Longitude Min -83
				imageData[4] = -5; //Altitude

				String surfaceTemp="-";
				String imageTemp="SouthAmerica";//image name
				String imageExt=".jpg"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";
				System.out.println("Loaded File Path: "+loadedFilePath);
				lfp = new LoadedFilesProperties(Info.getMainGUI().getRootPluginDir()+File.separator+"Maps"+File.separator+imageTemp+imageExt, imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this);
			}
			else
			{
				removePresetObject(sa);
			}
		}
		if (source == cd) {
			//mscpg = new MapSetCreatePluginGUI(this);
			if(cd.isSelected()){
				double imageData[] = new double[5];
				imageData[0] = 42;
				imageData[1] = 32.5;
				imageData[2] = -114;
				imageData[3] = -124.5;
				imageData[4] = 0; //Altitude

				String surfaceTemp="-";
				String imageTemp="CaliforniaDEM"; //image name
				String imageExt=".png"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";
				System.out.println("Loaded File Path: "+loadedFilePath);
				lfp = new LoadedFilesProperties(Info.getMainGUI().getRootPluginDir()+File.separator+"Maps"+File.separator+imageTemp+imageExt, imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this);
			}
			else
			{
				removePresetObject(cd);
			}
		}
		if (source == cdc) {
			//mscpg = new MapSetCreatePluginGUI(this);
			if(cdc.isSelected()){
				double imageData[] = new double[5];
				imageData[0] = 42;
				imageData[1] = 32.5;
				imageData[2] = -114.131477;
				imageData[3] = -124.409641;
				imageData[4] = 0; //Altitude

				String surfaceTemp="-";
				String imageTemp="CAlDEM_new"; // image name
				String imageExt=".png"; //image file extension type
				String loadedFilePath=Info.getMainGUI().getRootPluginDir() + File.separator + SurfacePlugin.dataStoreDir+ File.separator + "data" +File.separator + surfaceTemp + "_" + imageTemp + ".xml";
				System.out.println("Loaded File Path: "+loadedFilePath);
				lfp = new LoadedFilesProperties(Info.getMainGUI().getRootPluginDir()+File.separator+"Maps"+File.separator+imageTemp+imageExt, imageData,"-",null,null,false,loadedFilePath);
				mscpg = new MapSetCreatePluginGUI(imageTemp, imageData);
				mscpg.createImage(lfp, this);
			}
			else
			{
				removePresetObject(cdc);
			}
		}
		else if (source == remISButton) {
			ListSelectionModel model = surfaceTable.getSelectionModel();
			while (model.getMinSelectionIndex() >= 0) {
				int row = model.getMinSelectionIndex();
				JCheckBox tempCheckBox = uncheckPreset(row);
				if(tempCheckBox!=null && tempCheckBox.isSelected())
					tempCheckBox.setSelected(false);
				surfaceTableModel.removeRow(row);
				surfaceActors.removeActor(surfaceArray.get(row).getSurfaceActor());
				surfaceArray.remove(row);

			}
			Info.getMainGUI().updateRenderWindow();
		}
		else if (source == showImageButton) {
			ListSelectionModel model = surfaceTable.getSelectionModel();
			for(int i =model.getMinSelectionIndex();i<=model.getMaxSelectionIndex();i++) {
				int row = i;//model.getMinSelectionIndex();
				int v =(surfaceArray.get(row).getSurfaceActor().GetVisibility()==0)?1:0;
				setVisibility(surfaceArray.get(row),row, v);
				
			}
			Info.getMainGUI().updateRenderWindow();
		}
	}

	private void removePresetObject(JCheckBox uncheckedCheckBox)
	{
		ListSelectionModel model = surfaceTable.getSelectionModel();
		for(int i =0;i<surfaceTableModel.getRowCount();i++)
		{
			JCheckBox tempCheckBox = uncheckPreset(i);
			if(tempCheckBox!=null && tempCheckBox == uncheckedCheckBox)
			{
				surfaceTableModel.removeRow(i);
				surfaceActors.removeActor(surfaceArray.get(i).getSurfaceActor());
				surfaceArray.remove(i);
			}
		}
		Info.getMainGUI().updateRenderWindow();
	}
	
//	public void removeAllMaps(){
//		for(int i =0;i<surfaceTableModel.getRowCount();i++)
//		{
//			JCheckBox tempCheckBox = uncheckPreset(i);
//			if(tempCheckBox!=null)
//			{
//				for(int c=0; c<CheckBoxList.size(); c++){
//					if(CheckBoxList.get(c) == tempCheckBox){
//						String name = CheckBoxList.get(c).getName();
//						setCheckBox(CheckBoxList.get(c).getName(), false);
//					}
//				}
//				surfaceTableModel.removeRow(i);
//				surfaceActors.removeActor(surfaceArray.get(i).getSurfaceActor());
//				surfaceArray.remove(i);
//			}
//		}
//		Info.getMainGUI().updateRenderWindow();
//	}
	
	public void removeAllMaps(){
		int numRows = surfaceTableModel.getRowCount();
//		System.out.println("Removing " + numRows + "rows");
		for(int i =0; i<numRows;i++)
		{
			JCheckBox tempCheckBox = uncheckPreset(0);
			setCheckBox(tempCheckBox.getName(), false);
			surfaceTableModel.removeRow(0);
			surfaceActors.removeActor(surfaceArray.get(0).getSurfaceActor());
			surfaceArray.remove(0);
		}
		Info.getMainGUI().updateRenderWindow();
	}
	
	private JCheckBox uncheckPreset(int row)
	{
		String presetImage = (String) surfaceTableModel.getValueAt(row, 1);
		switch (presetImage)
		{
		case "CaliforniaFull":
		{
			return cm;
		}
		case "world.topo.bathy.200410.3x5400x2700":
		{
			return wm;
		}
		case "largesocal":
		{
			return sc;
		}
		case "Japan":
		{
			return jm;
		}
		case "NewZealand":
		{
			return nz;
		}
		case "Haiti":
		{
			return hm;
		}
		case "Mexico":
		{
			return mm;
		}
		case "SouthAmerica":
		{
			return sa;
		}
		case "CaliforniaDEM":
		{
			return cd;
		}
		case "CAlDEM_new":
		{
			return cdc;
		}
		}
		return null;
	}

	public JPanel getPanel() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub

	}


	private void loadData(GeographicSurfaceInfo si) throws IOException {
		filename = si.getFilename();
		double[] ul = si.getUpperRight();
		n = ul[0];
		w = ul[1];
		double[] lr = si.getLowerLeft();
		s = lr[0];
		e = lr[1];
		altitude = lr[2];
		demReader = new BufferedReader(new FileReader(filename)); //This file needs to be in matrix form
		String line = demReader.readLine();
		StringTokenizer dataLine = new StringTokenizer(line);
		//use the first line to count horizontal increments	
		horizontalItems = dataLine.countTokens();
		longIncrements = horizontalItems - 1;
		double[] lineHeights;
		int j;
		//this takes all the z values and puts them into an ArrayList of double arrays,
		//giving a matrix of heights
		while(line!=null) {
			dataLine = new StringTokenizer(line);
			lineHeights = new double[horizontalItems]; //the z values
			j = 0;
			while(dataLine.hasMoreTokens()) {
				lineHeights[j] = scaleFactor * Double.parseDouble(dataLine.nextToken());
				j++;
			}
			data.add(lineHeights);
			line = demReader.readLine();
		}
		demReader.close();
	}
	private vtkPolyData createSurface(GeographicSurfaceInfo si) {
		//create surface mesh
		latIncrements = data.size()-1;
		si.setVertSteps(latIncrements);
		si.setHorizSteps(longIncrements);

		int numVertices = latIncrements*(2*longIncrements+2);

		vtkFloatArray textureCoordinates =new vtkFloatArray();
		textureCoordinates.SetNumberOfComponents(2);
		textureCoordinates.SetNumberOfTuples(numVertices);
		textureCoordinates.SetName("TextureCoordinates");

		vtkPoints pts = new vtkPoints();
		double latStep = (n-s)/(double)latIncrements;
		double longStep = (e-w)/(double)longIncrements;
		double[] firstLineOfData = new double[horizontalItems];
		double[] secondLineOfData = new double[horizontalItems];
		double lon,lat;int pointIndex=0,stripIndex=0;

		vtkCellArray cells = new vtkCellArray();
		for (int i=0; i<latIncrements; i++) {
			//we fill the Triangle strip array by going bottom, top, bottom, top, so we need z values
			//from two different latitudes.  Each latitude corresponds to one of the double arrays
			//in the data ArrayList, so we need to pull down 2 of them.  Also, the ArrayList is stored
			//so that index 0 is the northernmost data, but we step through the TriangleStripArray from
			//south to north, hence the latIncrements-i.
			vtkTriangleStrip triangleStrip = new vtkTriangleStrip();
			//number of vertices are twice 
			triangleStrip.GetPointIds().SetNumberOfIds(longIncrements*2);

			stripIndex=0;
			firstLineOfData = (double[])data.get(latIncrements-i-1);
			secondLineOfData = (double[])data.get(latIncrements-i);

			for (int j=0; j<longIncrements; j++) {
				try {
					double[] xForm = new double[3];
					//Point 1
					double height =  (secondLineOfData[j]/200.0+altitude);
					lat =(s+i*latStep);
					lon =(w+j*longStep);
					xForm = Transform.transformLatLonHeight(lat, lon, height);
					float longRatio=(float)j/(float)longIncrements;
					float lati=i;
					if(j>longIncrements)
						longRatio=1;
					if(i>lati)
						lati=latIncrements;
					textureCoordinates.InsertTuple2(pointIndex,longRatio, lati/(float)latIncrements);
					//textureCoordinates.InsertTuple2(num,(float)((lon-w)/(e-w)), (float)((lat-s)/(n-s)));//, xForm[2]);
					pts.InsertPoint(pointIndex,xForm);

					//Point 2
					height = (firstLineOfData[j]/200.0+altitude);
					lat = (s+(i+1)*latStep);
					lon =  (w+j*longStep);
					xForm = Transform.transformLatLonHeight(lat, lon, height);//Transform.customTransform(latlon);
					pts.InsertPoint(pointIndex+1,xForm);
					textureCoordinates.InsertTuple2( pointIndex+1, longRatio,(lati+1)/(float)latIncrements);

					triangleStrip.GetPointIds().SetId(stripIndex,pointIndex);	
					triangleStrip.GetPointIds().SetId(stripIndex+1,pointIndex+1);
					pointIndex+=2;
					stripIndex+=2;
				} catch (Exception ex) {
					System.out.println("Exception " + ex);
					System.out.println("i=" + i + " j=" + j);
				}

			}
			cells.InsertNextCell(triangleStrip);
		}

		vtkPolyData triangleStripPolydata =new vtkPolyData();
		triangleStripPolydata.SetPoints(pts);
		triangleStripPolydata.SetStrips(cells);
		triangleStripPolydata.GetPointData().SetTCoords(textureCoordinates);

		return triangleStripPolydata;
	}
	//both images and surface information
	public void display(GeographicSurfaceInfo si, ImageInfo ii) {
		new ArrayList<double[]>();

		try {
			loadData(si);
		} catch (IOException e) {
			Throwables.propagate(e);
		}
		// Apply the texture
		vtkTexture texture = new vtkTexture();

		//texture file
		if(ii.getFilename().contains("png"))
		{
			vtkPNGReader pngReader = new vtkPNGReader();

			pngReader.SetFileName(ii.getFilename());
			System.out.println(ii.getFilename());
			pngReader.Update();

			texture.SetInputConnection(pngReader.GetOutputPort());
		}
		else
		{
			vtkJPEGReader jPEGReader = new vtkJPEGReader();

			jPEGReader.SetFileName(ii.getFilename());
			System.out.println(ii.getFilename());
			jPEGReader.Update();


			texture.SetInputConnection(jPEGReader.GetOutputPort());
		}

		vtkPolyData polydata = new vtkPolyData(); 
		polydata = createSurface(si);
		// Create an actor and mapper



		//Create a mapper and actor
		vtkPolyDataMapper mapper =new vtkPolyDataMapper();
		mapper.SetInputData(polydata);
		//mapper.ScalarVisibilityOff();
		vtkActor actor = new vtkActor();
		actor.SetMapper(mapper);
		actor.SetTexture(texture);
		actor.GetProperty().SetOpacity(0.5);
		// actor.GeneralTextureTransform();
		//actor.GetProperty().SetRepresentationToWireframe();
		Surface surface = new Surface(ii, si, actor);
		surfaceArray.add(surface);

		Object[] tempRow = surface.createRow();
		surfaceTableModel.addRow(tempRow);
		surfaceActors.addActor(actor);
		Info.getMainGUI().updateRenderWindow();
	}

	public void display(ImageInfo ii) {	//This is the method to display an image

		System.out.println("Image added");
		//texture file
		vtkTexture texture = new vtkTexture();
		if(ii.getFilename().contains("png"))
		{
			vtkPNGReader pngReader = new vtkPNGReader();

			pngReader.SetFileName(ii.getFilename());
			System.out.println(ii.getFilename());
			pngReader.Update();

			texture.SetInputConnection(pngReader.GetOutputPort());
		}
		else
		{
			vtkJPEGReader jPEGReader = new vtkJPEGReader();

			jPEGReader.SetFileName(ii.getFilename());
			System.out.println(ii.getFilename());
			jPEGReader.Update();


			texture.SetInputConnection(jPEGReader.GetOutputPort());
		}


		double[] upperLeft = ii.getUpperLeft();
		double[] lowerRight = ii.getLowerRight();
		double height = upperLeft[2]; //or lower right (altitude) are same
		double[][] tuple = new double[4][3];
		vtkPoints points = new vtkPoints();
		vtkDoubleArray textureCoordinates = new vtkDoubleArray();
		textureCoordinates.SetName("TextureCoordinates");
		textureCoordinates.SetNumberOfComponents(3);
		vtkCellArray polygons = new vtkCellArray();

		double upperLat = upperLeft[0];
		double lowerLat = lowerRight[0];
		double leftLon = upperLeft[1];
		double rightLon= lowerRight[1];


		// textureCoordinates.SetNumberOfTuples(noOfTuples);
		//creating temp curved surface
		int ptCount =0;//pt ids
		double u ;double v ;//texture uv
		for(double i=upperLat;i>lowerLat;i--)
		{
			for(double j=rightLon;j>leftLon;j--)
			{

				vtkPolygon polygon = new vtkPolygon();
				polygon.GetPointIds().SetNumberOfIds(4);
				points.InsertNextPoint(Transform.transformLatLon(i-1, j));
				u=(leftLon-(j))/(leftLon-rightLon);
				v=(lowerLat-(i-1))/(lowerLat-upperLat);
				u=clampTextureCoord(u);
				v=clampTextureCoord(v);;
				// System.out.println(k+","+u);
				textureCoordinates.InsertNextTuple2(u,v);
				polygon.GetPointIds().SetId(0, ptCount++);

				points.InsertNextPoint(Transform.transformLatLon(i, j));
				u=(leftLon-j)/(leftLon-rightLon);
				v=(lowerLat-i)/(lowerLat-upperLat);
				u=clampTextureCoord(u);
				v=clampTextureCoord(v);
				// System.out.println(k+","+u);
				textureCoordinates.InsertNextTuple2(u,v);
				polygon.GetPointIds().SetId(1, ptCount++);

				points.InsertNextPoint(Transform.transformLatLon(i, j-1));
				u=(leftLon-(j-1))/(leftLon-rightLon);
				v=(lowerLat-i)/(lowerLat-upperLat);
				u=clampTextureCoord(u);
				v=clampTextureCoord(v);
				// System.out.println(k+","+u);
				textureCoordinates.InsertNextTuple2(u,v);
				polygon.GetPointIds().SetId(2, ptCount++);

				points.InsertNextPoint(Transform.transformLatLon(i-1, j-1));
				u=(leftLon-(j-1))/(leftLon-rightLon);
				v=(lowerLat-(i-1))/(lowerLat-upperLat);
				u=clampTextureCoord(u);
				v=clampTextureCoord(v);
				// System.out.println(k+","+u);
				textureCoordinates.InsertNextTuple2(u,v);
				polygon.GetPointIds().SetId(3, ptCount++);
				polygons.InsertNextCell(polygon);
			}
		}

		// Create a PolyData
		vtkPolyData polygonPolyData = new vtkPolyData();
		polygonPolyData.SetPoints(points);
		polygonPolyData.SetPolys(polygons);

		polygonPolyData.GetPointData().SetTCoords(textureCoordinates);
		// Create an actor
		//Create a mapper and actor
		vtkPolyDataMapper mapper =new vtkPolyDataMapper();
		mapper.SetInputData(polygonPolyData);
		//mapper.ScalarVisibilityOff();
		vtkActor actor = new vtkActor();
		actor.SetMapper(mapper);
		actor.SetTexture(texture);
		actor.GetProperty().SetOpacity(0.5);

		Surface surface = new Surface(ii, null, actor);
		surfaceArray.add(surface);

		Object[] tempRow = surface.createRow();
		surfaceTableModel.addRow(tempRow);
		surfaceActors.addActor(actor);
		Info.getMainGUI().updateRenderWindow();
	}

	private double clampTextureCoord(double t)
	{	t=t<0?0:t;
		t=t>1?1:t;
		return t;
	}
	public void processTableSelectionChange() {
		int[] selectedRows = this.surfaceTable.getSelectedRows();
		if (selectedRows.length > 0) {
			this.remISButton.setEnabled(true);
			this.showImageButton.setEnabled(true);
			this.transparencySlider.setEnabled(true);
		} else {
			this.remISButton.setEnabled(false);
			this.showImageButton.setEnabled(false);
			this.transparencySlider.setEnabled(false);
		}
	}

	public void unloadPlugin() {
		// TODO Auto-generated method stub
		for(int i =0;i<surfaceArray.size();i++)
		{
			surfaceActors.removeActor(surfaceArray.get(i).getSurfaceActor());
		}
		surfaceArray.removeAllElements();
		Info.getMainGUI().updateRenderWindow();
	}



	public void setTransparency(Surface surface, double transparency)
	{
		surface.getSurfaceActor().GetProperty().SetOpacity(transparency);
	}
	
	public void setVisibility(Surface surf, int row, int visibility)
	{
		surf.setVisibility(visibility);
		surf.getSurfaceActor().SetVisibility(visibility);
		if(visibility == 0)
		{

			surfaceTableModel.setValueAt("false",row, 0);
			//surfaceTableModel.fireTableCellUpdated(row, 0);
		}
		else
		{	
			surfaceTableModel.setValueAt("true",row, 0);
			//surfaceTableModel.fireTableCellUpdated(row, 0);
		}
		
	}
	
	public void setCheckBox(String nameOFButton, boolean what)
	{
		switch (nameOFButton)
		{
		case "wm" : wm.setSelected(what);
		break;
		case "sc" : sc.setSelected(what);
		break;
		case "cm" : cm.setSelected(what);
		break;
		case "jm" : jm.setSelected(what);
		break;
		case "nz" : nz.setSelected(what);
		break;
		case "im" : im.setSelected(what);
		break;
		case "hm" : hm.setSelected(what);
		break;
		case "mm" : mm.setSelected(what);
		break;
		case "sa" : sa.setSelected(what);
		break;
		case "cd" : cd.setSelected(what);
		break;
		case "cdc" : cdc.setSelected(what);

		}

	}
	
	//GETTERS and SETTERS	
	public Vector<Surface> getSurfaceArray(){
		return surfaceArray;
	}
		
}