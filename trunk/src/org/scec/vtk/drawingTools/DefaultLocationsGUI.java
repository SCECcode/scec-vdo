 package org.scec.vtk.drawingTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.scec.vtk.main.Info;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.DBFReaderJGeom;
import oracle.spatial.util.ShapefileReaderJGeom;



/**
 *	author: Kristy Akullian
 *	author: Jessica McMorris
 *	author: Becky Gallagher
 *	author: Tom Robinson
 */

public class DefaultLocationsGUI extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final String dataPath = Info.getMainGUI().getRootPluginDir()+File.separator+"GISLocationPlugin"+File.separator;

	private DrawingToolsGUI guiparent;
// We took out the ability to search for a city within this tab as we have a better search
// functionality in its own tab. I'll leave this commented out in case at any point in the future
// we decide it is worthwhile to have a search that doesn't require internet access.
//	private JButton searchButton = new JButton("Go!");
//	private JTextField searchCity = new JTextField();
	
	private ArrayList<PresetLocationGroup> presetLocationGroups = new ArrayList<PresetLocationGroup>();
	private String selectedInputFile;
	
	private DisplayAttributes displayAttributes;
	private JPanel centerPanel = new JPanel();
	private JFrame frame = new JFrame();
	private JScrollPane defaultScrollPane = new JScrollPane(centerPanel);

	private DrawingToolsTable drawingToolTable;
	private DrawingToolsTableModel drawingTooltablemodel;
	private int defaultLocationsStartIndex = 0;
	private int popSize = 0;
	private ArrayList<String> ccount = new ArrayList<String>();
	
	
	public DefaultLocationsGUI(DrawingToolsGUI guiparent) {
		this.guiparent = guiparent;
		this.drawingToolTable = guiparent.getTable();
		this.drawingTooltablemodel = drawingToolTable.getLibraryModel();
		this.defaultLocationsStartIndex = this.drawingToolTable.getRowCount();
		// Set main panel layout manager and dimensions
		this.setLayout(new BorderLayout(5,5));
		this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		// North Panel
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));
		
		// Center Panel
		centerPanel.setLayout(new GridLayout(0,2));

		// Build the presets panel automatically
		
		// Check to make sure it exists
		File dataDirectory = new File(dataPath);
		if (dataDirectory.isDirectory()) {
			// List files in the directory and process each
			File files[] = dataDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile() && files[i].getName().endsWith(".shp")) {
					PresetLocationGroup tempGroup = new PresetLocationGroup();
					
					tempGroup.file = files[i];
					
					String tempName = files[i].getName();
					tempName = tempName.substring(0, tempName.lastIndexOf("."));
					tempName = tempName.replace('_', ' ');
					tempGroup.name = tempName;
					
					JPanel tempPanel = new JPanel();
					tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
					JCheckBox tempCheckbox = new JCheckBox();
					tempCheckbox.addActionListener(this);
					tempPanel.add(tempCheckbox);
					tempPanel.add(new JLabel(tempName));
					tempGroup.checkbox = tempCheckbox;
					
					presetLocationGroups.add(tempGroup);
					centerPanel.add(tempPanel);
				}
			}
		} else {
			// If it doesn't exist, display an error instead
			centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
			centerPanel.add(Box.createHorizontalGlue());
			centerPanel.add(new JLabel("Default locations data folder not found."));
			centerPanel.add(Box.createHorizontalGlue());
		}
		
		// Assemble main Default Locations Panel 

		this.add(northPanel, BorderLayout.NORTH);
		this.add(defaultScrollPane,BorderLayout.CENTER);
	}


	private void addBuiltInFiles(Vector<DrawingTool> locations) {
		defaultLocationsStartIndex = this.drawingToolTable.getRowCount();
		for (int i = 0; i < locations.size(); i++) {
			DrawingTool tempLocation = locations.get(i);
			this.guiparent.addDrawingTool(tempLocation);//.addDrawingTool(tempLocation);
			this.drawingToolTable.addDrawingTool(tempLocation);//newObjects);
		}
		Info.getMainGUI().updateRenderWindow();
	}

	private void removeBuiltInFiles(Vector<DrawingTool> locations) {
		if(locations!=null){
			for(int i =0;i<this.drawingToolTable.getRowCount();i++)
			{
				for(int j =0; j < locations.size(); j++)
				{
					//System.out.println(locations.elementAt(j).getTextString() + "," + this.drawingToolTable.getValueAt(i, 0));
					if(locations.elementAt(j).getTextString().equals(this.drawingToolTable.getValueAt(i, 0)))
					{
						//System.out.println("Removing");
						this.drawingToolTable.setRowSelectionInterval(i,i);
						guiparent.removeTextActors();
						defaultLocationsStartIndex=i;
						i--;
						break;
					}
				}
				
			}
			Info.getMainGUI().updateRenderWindow();
		
		}
	}
	
	private Vector<DrawingTool> loadBuiltInFiles() {
	
		/**
		 * Only reads point data
		 */
		//if the dbf file exists, then read from the dbf file for the label names
		String selectedDbfFile = selectedInputFile.replace(".shp", ".dbf");
		File dbf = new File(selectedDbfFile);
		if(dbf.exists()) {
			Vector<DrawingTool> locations = new Vector<DrawingTool>();
			try {
				DBFReaderJGeom dbfFile = new DBFReaderJGeom(selectedDbfFile);
				//obtains the location of name columns in the file
				int nameColumn = 0;
				int fieldsCount = dbfFile.numFields();
				for(int i = 0; i < fieldsCount; i++) {
					String fieldName = dbfFile.getFieldName(i);
					if(fieldName.equalsIgnoreCase("Name_1") || fieldName.equalsIgnoreCase("NameLSAD")) 
						nameColumn = i;
				}
				
				ShapefileReaderJGeom shpFile = new ShapefileReaderJGeom(selectedInputFile);

				int pointCount = shpFile.numRecords();
				
				for(int index = 0; index < pointCount; index++) {		
					//reads the shape bytes in the shapefile at the given index
					byte[] geometryBytes = shpFile.getGeometryBytes(index);			

					//converts the bytes into a JGeometry type
					JGeometry point = ShapefileReaderJGeom.getGeometry(geometryBytes, index);

					//gets the coordinates of all the vertices of the shape
					double[] coordinates = point.getPoint();
					
					//gets the name of the point
					byte[] record = dbfFile.getRecord(index);
					String textStr = dbfFile.getFieldData(nameColumn, record);
										
					DrawingTool tempLocation = new DrawingTool(
							coordinates[1],
							coordinates[0],
							0.0d,
							textStr,
							displayAttributes);
					locations.addElement(tempLocation);
				}
				return locations;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Vector<DrawingTool> locations = new Vector<DrawingTool>();
			//int fileSeparatorIndex = selectedInputFile.lastIndexOf(File.separator);
			//String textStr = selectedInputFile.substring(fileSeparatorIndex + 1);
			try {
				ShapefileReaderJGeom shpFile = new ShapefileReaderJGeom(selectedInputFile);
				int pointCount = shpFile.numRecords();
				for(int index = 0; index < pointCount; index++) {			
					//reads the shape bytes in the shapefile at the given index
					byte[] geometryBytes = shpFile.getGeometryBytes(index);			

					//converts the bytes into a JGeometry type
					JGeometry point = ShapefileReaderJGeom.getGeometry(geometryBytes, index);

					//gets the coordinates of all the vertices of the shape
					double[] coordinates = point.getPoint();

					
					DrawingTool tempLocation = new DrawingTool(
							coordinates[1],
							coordinates[0],
							0.0d,
							""/*textStr*/,
							displayAttributes);
					locations.addElement(tempLocation);
				}
				return locations;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void clearCheckBoxes() {
		for (int i = 0; i < presetLocationGroups.size(); i++) {
			PresetLocationGroup tempGroup = presetLocationGroups.get(i);
			if (tempGroup != null && tempGroup.checkbox != null) {
				tempGroup.checkbox.setSelected(false);
			}
		}
		this.repaint();
	}
	private void showPopUp()
	{
		CitiesDialogBox mdb = new CitiesDialogBox();
		//mdb.toFront();
		final JDialog frame = new JDialog(this.frame, "City Filter", true);
		mdb.d = frame;
		frame.getContentPane().add(mdb);
		frame.pack();
		frame.setVisible(true);
		//frame.setSize(320, 100);
		//frame.setLocation(200, 200);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	popSize = -1;
		    }
		});
		mdb.d.setLocationRelativeTo(frame);
		
		
	}
	private boolean showSchoolsPopUp()
	{
		SchoolsDialogBox mdb = new SchoolsDialogBox();
		//mdb.toFront();
		final JDialog frame = new JDialog(this.frame, "School Filter", true);
		mdb.d = frame;
		frame.getContentPane().add(mdb);
		frame.pack();
		frame.setVisible(true);
		//frame.setSize(320, 100);
		//frame.setLocation(200, 200);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	//popSize = -1;
		    }
		});
		return mdb.success;
	}
	public void readPopInput(String input)
	{
		try{
			popSize = Integer.parseInt(input);
		}catch(NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(null,
				    "Please enter a number",
				    "Number Format Exception",
				    JOptionPane.ERROR_MESSAGE);
		}
	}
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
			for (int i = 0; i < presetLocationGroups.size(); i++) {
				PresetLocationGroup tempGroup = presetLocationGroups.get(i);
				
				if (tempGroup != null && src == tempGroup.checkbox) {
					if (tempGroup.checkbox.isSelected()) {
						selectedInputFile = tempGroup.file.getAbsolutePath();
						if(tempGroup.name.equals("CA Cities"))
						{
							popSize = -1;
							ccount.clear();
							showPopUp();	
							if(popSize != -1)
							{
								tempGroup.locations = loadBuiltInFiles();
		            			String p = dataPath + "CA_Cities_population.txt";
		            			String c = dataPath + "CA_Cities_counties.txt";
		            			ArrayList<String> temp = filterCitiesByCounty(c,ccount);
		            			addFilteredFiles(tempGroup.locations,filterCitiesByPopulation(p,popSize,temp));
							}
							else
							{
								tempGroup.checkbox.setSelected(false);
								popSize = -1;
							}
						}
						else if(tempGroup.name.equals("CA Schools"))
						{
							if(showSchoolsPopUp());
								tempGroup.checkbox.setSelected(false);
						}
						else
						{
							tempGroup.locations = loadBuiltInFiles();
							addBuiltInFiles(tempGroup.locations);
						}
						
					} else {
						removeBuiltInFiles(tempGroup.locations);
					}
				}
			}
	}
	private ArrayList<String> getCounties(String countyFile)
	{
		ArrayList<String> counties = new ArrayList<String>();
		ArrayList<String> buff = new ArrayList<String>();
		try {
			Charset charset = Charset.forName("Cp1252");
			buff = (ArrayList<String>)Files.readAllLines(Paths.get(countyFile), charset);
			for(String s: buff)
			{
				if(s.substring(0, 1).contains("-"))
				{
					counties.add(s.substring(1));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return counties;
	}
	private ArrayList<String> filterCitiesByCounty(String countyFile, ArrayList<String> counties)
	{
		ArrayList<String> filteredCities = new ArrayList<String>();
		ArrayList<String> buff = new ArrayList<String>();
		try {
			Charset charset = Charset.forName("Cp1252");
			buff = (ArrayList<String>)Files.readAllLines(Paths.get(countyFile), charset);
			for(String s: counties)
			{
				String search = "-"+s;
				boolean append = false;
				for(String j: buff)
				{
					if(search.equals(j))
					{
						append = true;
					}
					if(j.contains("-") && !(search.equals(j)) && append)
					{
						break;
					}
					if(append && !(search.equals(j) && !j.contains("-")))
					{
						filteredCities.add(j);
					}
						
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filteredCities;
	}
	private void addFilteredFiles(Vector<DrawingTool> locations,ArrayList<String> cities) {
		defaultLocationsStartIndex = this.drawingToolTable.getRowCount();
		for (int i = 0; i < locations.size(); i++) {
			for(int j = 0; j < cities.size();j++)
			{
				//System.out.println(locations.get(i).getTextString() + "," + cities.get(j));
				if(locations.get(i).getTextString().equals(cities.get(j)))
				{
					//System.out.println(cities.get(j) + " added");
					DrawingTool tempLocation = locations.get(i);
					this.guiparent.addDrawingTool(tempLocation);//.addDrawingTool(tempLocation);
					//ArrayList<DrawingTool> newObjects = new ArrayList<>();
					//newObjects.add(tempLocation);
					this.drawingToolTable.addDrawingTool(tempLocation);//newObjects);
				}
			}		
		}
		Info.getMainGUI().updateRenderWindow();
	}
	private ArrayList<String> filterCitiesByPopulation(String popFile, int desiredPop, ArrayList<String> cities)
	{
		ArrayList<String> filteredCities = new ArrayList<String>();
		ArrayList<String> buff = new ArrayList<String>();
		try {
			Charset charset = Charset.forName("Cp1252");
			buff = (ArrayList<String>)Files.readAllLines(Paths.get(popFile), charset);
			for(String s: cities)
			{
				for(int i = 0; i < buff.size(); i+=2)
				{
					if(Integer.parseInt(buff.get(i+1)) >= desiredPop && buff.get(i).trim().equals(s))
					{
						filteredCities.add(buff.get(i).trim());
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return filteredCities;
	}
	private Vector<DrawingTool> filterSchools(String key, boolean elem, boolean middle, boolean high, boolean other)
	{
		Vector<DrawingTool> result = new Vector<DrawingTool>();
		for(DrawingTool d : loadBuiltInFiles())
		{
			if(d.getTextString().contains(key.toUpperCase()))
			{
				if(elem)
				{
					if(d.getTextString().contains("ELEMENTARY"))
						result.add(d);
				}
				if(middle)
				{
					if(d.getTextString().contains("MIDDLE") || d.getTextString().contains("JUNIOR HIGH"))
						result.add(d);
				}
				if(high)
				{
					if(d.getTextString().contains("HIGH") && !d.getTextString().contains("JUNIOR HIGH"))
						result.add(d);
				}
				if(other)
				{
					result.add(d);
				}
				if(!other && !elem && !middle && !high)
					result.add(d);
			}
		}
		return result;
	}
	public class PresetLocationGroup {
		public Vector<DrawingTool> locations = null;
		public String name			= null;
		public File file			= null;
		public JCheckBox checkbox	= null;
	}
	class CitiesDialogBox extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private DefaultListModel<String> dlm;
		private JList<String> jl;
		private JDialog d; //reference to parent dialog
		private ArrayList<String> counties = new ArrayList<String>();
		CitiesDialogBox() {
			counties = getCounties(dataPath + "CA_Cities_counties.txt");
			Collections.sort(counties);
			//super("");
			//setSize(320, 200);
			//setLocation(200, 200);
			JPanel jp = new JPanel();
			jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));
			
			// fist row
			JPanel aPanel = new JPanel();
			aPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			JLabel sizeLabel = new JLabel("Show all cities with pop. > than:");
			aPanel.add(sizeLabel);
			final JTextField sizeText= new JTextField("0", 12);
			aPanel.add(sizeText);
			
			// second row
			JPanel bPanel = new JPanel();
			bPanel.setLayout(new BoxLayout(bPanel, BoxLayout.PAGE_AXIS));
			JLabel filterLabel = new JLabel("Counties: Default = All Counties");
			bPanel.add(filterLabel);
			
			dlm = new DefaultListModel<String>();
			for(String s:counties)
			{
				dlm.addElement(s);
			}
			jl = new JList<String>(dlm);
			JScrollPane scroll = new JScrollPane(jl);
			scroll.setPreferredSize(new Dimension(250,250));
			bPanel.add(scroll);
			
			// third row
			JButton okButton = new  JButton("Ok");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae){
					try{
						readPopInput(sizeText.getText());
						ListSelectionModel lsm = jl.getSelectionModel();
						int minIndex = lsm.getMinSelectionIndex();
				        int maxIndex = lsm.getMaxSelectionIndex();
				        boolean atLeastOne = false;
				        for (int i = minIndex; i <= maxIndex; i++) {
				            if (lsm.isSelectedIndex(i)) {
				            	atLeastOne = true;
				            	ccount.add(counties.get(i));
				                //System.out.println(counties.get(i) + " selected");
				            }
				        }
				        if(!atLeastOne)
				        {
				        	for(String s: counties)
				        		ccount.add(s);
				        }
						d.dispose();
					}
					catch(NumberFormatException e){
						JOptionPane.showMessageDialog(null,
            				    "Please enter a number",
            				    "Number Format Exception",
            				    JOptionPane.ERROR_MESSAGE);
						sizeText.setText("");
					}
				}		
			});
			
			JButton cancelButton = new  JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent aa){
					popSize = -1;
					d.dispose();
				}
			});

			JPanel okPanel = new JPanel();
			okPanel.setLayout(new FlowLayout(FlowLayout.CENTER));		
			okPanel.add(okButton);
			okPanel.add(cancelButton);

			//add add add 
			jp.add(aPanel);
			jp.add(bPanel);
			jp.add(okPanel);
			add(jp);
			setVisible(true);
		}
	}
	class DisabledItemSelectionModel extends DefaultListSelectionModel {

	    @Override
	    public void setSelectionInterval(int index0, int index1) {
	        super.setSelectionInterval(-1, -1);
	    }
	}
	class SchoolsDialogBox extends JPanel {
		//private static final long serialVersionUID = 1L;
		private JDialog d; //reference to parent dialog
		private DefaultListModel<String> dlm,tbd;
		private JList<String> jl,tba;
		private boolean success = false;
		private Vector<DrawingTool> results = new Vector<DrawingTool>();
		private Vector<DrawingTool> search = new Vector<DrawingTool>();
		SchoolsDialogBox()
		{
			//super("");
			//setSize(320, 200);
			//setLocation(200, 200);
			JPanel jp = new JPanel();
			jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));
			
			// first row
			JPanel aPanel = new JPanel();
			aPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			JLabel sizeLabel = new JLabel("Name of the school:");
			aPanel.add(sizeLabel);
			final JTextField schoolName= new JTextField("", 16);
			aPanel.add(schoolName);
			
			// 2nd row
			JPanel bPanel = new JPanel();
			bPanel.setLayout(new BoxLayout(bPanel, BoxLayout.LINE_AXIS));
			final JCheckBox elementary = new JCheckBox("Elementary School");
			final JCheckBox otherRadio = new JCheckBox("Other");
			final JCheckBox middleRadio = new JCheckBox("Middle School");
			final JCheckBox highRadio = new JCheckBox("High School");
			bPanel.add(elementary);
			bPanel.add(middleRadio);
			bPanel.add(highRadio);
			bPanel.add(otherRadio);
			
			// 3rd row
			JPanel cPanel = new JPanel();
			cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.PAGE_AXIS));
			JLabel filterLabel = new JLabel("Search Results:");
			cPanel.add(filterLabel);
			dlm = new DefaultListModel<String>();
			search = filterSchools("",false,false,false,false);
			for(DrawingTool s: search)
				dlm.addElement(s.getTextString());
			jl = new JList<String>(dlm);
			JScrollPane scroll = new JScrollPane(jl);
			scroll.setPreferredSize(new Dimension(250,250));
			cPanel.add(scroll);
			
			
			// 4th row
			JButton okButton = new  JButton("Search");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae){
					dlm.removeAllElements();
					search = filterSchools(schoolName.getText(),elementary.isSelected(),middleRadio.isSelected(),highRadio.isSelected(),otherRadio.isSelected());
					for(DrawingTool s: search )
						dlm.addElement(s.getTextString());
				}		
			});
			
			JButton cancelButton = new  JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent aa){
					success = true;
					d.dispose();
				}
			});
			
			final JButton displayButton = new  JButton("Display");
			displayButton.setEnabled(false);
			displayButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent aa){
					addBuiltInFiles(results);
					d.dispose();
				}
			});
			
			JButton addButton = new  JButton("Add");
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent aa){
					//System.out.println(search.size());
					ListSelectionModel lsm = jl.getSelectionModel();
					int minIndex = lsm.getMinSelectionIndex();
			        int maxIndex = lsm.getMaxSelectionIndex();
			        for (int i = minIndex; i <= maxIndex; i++) {
			            if (lsm.isSelectedIndex(i)) {
			            	tbd.addElement(search.get(i).getTextString());
			            	results.add(search.get(i));
			            }
			        }
			        if(results.size() > 0)
						displayButton.setEnabled(true);
					else
						displayButton.setEnabled(false);
				
				}
			});
			
			JButton removeButton = new  JButton("Remove");
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent aa){
					if(results.size() > 0)
						displayButton.setEnabled(true);
					else
						displayButton.setEnabled(false);
					ListSelectionModel lsm = tba.getSelectionModel();
					int minIndex = lsm.getMinSelectionIndex();
			        int maxIndex = lsm.getMaxSelectionIndex();
			        for (int i = minIndex; i <= maxIndex; i++) {
			            if (lsm.isSelectedIndex(i)) {
			            	results.remove(i);
			            	tbd.removeElement(i);
			            }
			        }
				
				}
			});

			JPanel okPanel = new JPanel();
			okPanel.setLayout(new FlowLayout(FlowLayout.CENTER));		
			okPanel.add(okButton);
			okPanel.add(addButton);
			okPanel.add(removeButton);
			okPanel.add(cancelButton);
			okPanel.add(displayButton);
			
			// 5th row
			JPanel dPanel = new JPanel();
			dPanel.setLayout(new BoxLayout(dPanel, BoxLayout.PAGE_AXIS));
			JLabel selectLabel = new JLabel("Your Selection");
			dPanel.add(selectLabel);
			tbd = new DefaultListModel<String>();
			tba = new JList<String>(tbd);
			tba.setSelectionModel(new DisabledItemSelectionModel());
			JScrollPane scroll2 = new JScrollPane(tba);
			scroll2.setPreferredSize(new Dimension(250,250));
			dPanel.add(scroll2);

			//add add add 
			jp.add(aPanel);
			jp.add(bPanel);
			jp.add(cPanel);
			jp.add(okPanel);
			jp.add(dPanel);
			add(jp);
			setVisible(true);
		}
		
	}

}
