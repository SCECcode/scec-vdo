package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.JTextArea;

import org.opensha.commons.util.ExceptionUtils;
import org.scec.vtk.main.Info;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginGUI;
import org.scec.vtk.tools.Transform;

import com.sun.glass.events.WindowEvent;

import gov.usgs.earthquake.event.EventQuery;
import gov.usgs.earthquake.event.EventWebService;
import gov.usgs.earthquake.event.Format;
import gov.usgs.earthquake.event.JsonEvent;
import gov.usgs.earthquake.event.OrderBy;
import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;

public class ComcatResourcesDialog  extends JDialog implements ActionListener {
		
		private static final long serialVersionUID = 1L;
		
		private EarthquakeCatalogPluginGUI parent;

		private JLabel latLabel  = new JLabel("Lat:");
		private JLabel lonLabel  = new JLabel("Lon:");
		private JLabel depLabel  = new JLabel("Dep:");
		private JLabel magLabel  = new JLabel("Mag:");
		private JLabel minLabel1 = new JLabel("Min");
		private JLabel maxLabel1 = new JLabel("Max");
		private JLabel minLabel2 = new JLabel("Min");
		private JLabel maxLabel2 = new JLabel("Max");
		private JLabel dateStartLabel = new JLabel("Start Date:");
		private JLabel dateEndLabel   = new JLabel("End Date:");
		//private JLabel srcLabel    	  = new JLabel("Src:");
		//private JLabel eventTypeLabel = new JLabel("Type:");
		private JLabel maxEventsLabel = new JLabel("Max EQs:");

		private JTextField latMinField = new JTextField("32");
		private JTextField latMaxField = new JTextField("43");
		private JTextField lonMinField = new JTextField("-125");
		private JTextField lonMaxField = new JTextField("-114");
		private JTextField depMinField = new JTextField();
		private JTextField depMaxField = new JTextField();
		private JTextField magMinField = new JTextField();
		private JTextField magMaxField = new JTextField();
		private JTextField dateStartField = new JTextField("2016/04/01");
		private JTextField dateEndField   = new JTextField("2016/04/02");
		private JTextField maxEventsField = new JTextField();
		private JButton importButton = new JButton("Import");
		//explanations and references for each source catalog
		//private JTextArea srcExplainText = new JTextArea();
//		private JLabel srcSCSNLabel = new JLabel();
//		private JLabel srcDS2000Label = new JLabel();
//		private JLabel srcSHLK2003Label = new JLabel();
//		private JLabel srcHAUK2003Label = new JLabel();
//		private JLabel srcHAUK2004Label = new JLabel();
		
		/*private String[] eventTypeList = {
				"All Events", //default request is All Events
				"Local",
				"Regional",
				"Teleseism",
				"Quarry Blast",
				"Nuclear Blast",
				"Sonic Boom",
				"Uknown Event"};
		private JComboBox eventTypePulldown = new JComboBox(eventTypeList);
		
		private String[] sourceNetList = {
				"SCSN (default)",
				"DS2000",
				"SHLK2003",
				"HAUK2003",
				"HAUK2004"
				};
		public JComboBox sourceNetPulldown = new JComboBox(sourceNetList);*/
		
		
		private String defaultName="";

		//private ArrayList<vtkActor> masterEarthquakeCatalogBranchGroup; //to keep actors
		private ArrayList<Earthquake> masterEarthquakeCatalogsList = new ArrayList<>(); //to keep earthquakeInfo in memory
		
		public  ComcatResourcesDialog(JPanel parent) {
			
			super();
			
			this.parent = (EarthquakeCatalogPluginGUI)parent;
			
			this.setName("Network Sources");
			 this.setSize(500,220);
		   
			
			BorderLayout layout = new BorderLayout();
			this.setLayout(layout);
			
			
			
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			// Set dimensions of text fields in pixels (width, height)
			
			// Lower Panel Row 1
			latMinField.setPreferredSize(new Dimension(50, 20));
			latMinField.setMinimumSize(new Dimension(50, 20));
			latMaxField.setPreferredSize(new Dimension(50, 20));
			latMaxField.setMinimumSize(new Dimension(50, 20));
			magMinField.setPreferredSize(new Dimension(35, 20));
			magMinField.setMinimumSize(new Dimension(35, 20));
			magMaxField.setPreferredSize(new Dimension(35, 20));
			magMaxField.setMinimumSize(new Dimension(35, 20));
			dateStartField.setPreferredSize(new Dimension(75, 20));
			dateStartField.setMinimumSize(new Dimension(75, 20));
			
			// Lower Panel Row 2
			lonMinField.setPreferredSize(new Dimension(50, 20));
			lonMinField.setMinimumSize(new Dimension(50, 20));
			lonMaxField.setPreferredSize(new Dimension(50, 20));
			lonMaxField.setMinimumSize(new Dimension(50, 20));
			depMinField.setPreferredSize(new Dimension(35, 20));
			depMinField.setMinimumSize(new Dimension(35, 20));
			depMaxField.setPreferredSize(new Dimension(35, 20));
			depMaxField.setMinimumSize(new Dimension(35, 20));
			dateEndField.setPreferredSize(new Dimension(75, 20));
			dateEndField.setMinimumSize(new Dimension(75, 20));
			
			// Lower Panel Row 3
			/*sourceNetPulldown.setMinimumSize(new Dimension(104, 20));
			sourceNetPulldown.setMinimumSize(new Dimension(104, 20));
			
			eventTypePulldown.setMinimumSize(new Dimension(74, 20));
			eventTypePulldown.setMinimumSize(new Dimension(74, 20));*/
			
			maxEventsField.setMinimumSize(new Dimension(75, 20));
			maxEventsField.setMinimumSize(new Dimension(75, 20));
			
			importButton.addActionListener(this);
			// Layout grid components, anchor contents of each grid box
			// Add insets to separate fields, (top, left, bottom, right)
			
			// Top row (0)
			c.gridx = 1;
			c.gridy = 0;
			c.anchor = GridBagConstraints.SOUTH;
			c.insets = new Insets(2,4,0,4);
			this.add(minLabel1, c);
			c.gridx = 2;
			c.gridy = 0;
			c.anchor = GridBagConstraints.SOUTH;
			c.insets = new Insets(2,4,0,4);
			this.add(maxLabel1, c);
			c.gridx = 4;
			c.gridy = 0;
			c.anchor = GridBagConstraints.SOUTH;
			c.insets = new Insets(2,4,0,4);
			this.add(minLabel2, c);
			c.gridx = 5;
			c.gridy = 0;
			c.anchor = GridBagConstraints.SOUTH;
			c.insets = new Insets(2,4,0,4);
			this.add(maxLabel2, c);
			

			// Second row (1)
			c.gridx = 0;
			c.gridy = 1;
			c.anchor = GridBagConstraints.EAST;
			c.insets = new Insets(0,0,0,2);
			this.add(latLabel, c);
			c.gridx = 1;
			c.gridy = 1;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(2,0,2,0);
			this.add(latMinField, c);
			c.gridx = 2;
			c.gridy = 1;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(2,4,2,0);
			this.add(latMaxField, c);
			c.gridx = 3;
			c.gridy = 1;
			c.anchor = GridBagConstraints.EAST;
			c.insets = new Insets(0,4,0,2);
			this.add(magLabel, c);
			c.gridx = 4;
			c.gridy = 1;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(2,0,2,0);
			this.add(magMinField, c);
			c.gridx = 5;
			c.gridy = 1;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(2,4,2,0);
			this.add(magMaxField, c);
			c.gridx = 6;
			c.gridy = 1;
			c.anchor = GridBagConstraints.EAST;
			c.insets = new Insets(0,4,0,2);
			this.add(dateStartLabel, c);
			c.gridx = 7;
			c.gridy = 1;
			c.anchor = GridBagConstraints.EAST;
			c.insets = new Insets(2,0,2,0);
			this.add(dateStartField, c);

			
			// Third row (2)
			c.gridx = 0;
			c.gridy = 2;
			c.anchor = GridBagConstraints.EAST;
			c.insets = new Insets(0,0,0,2);
			this.add(lonLabel, c);
			c.gridx = 1;
			c.gridy = 2;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(2,0,2,0);
			this.add(lonMinField, c);
			c.gridx = 2;
			c.gridy = 2;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(2,4,2,0);
			this.add(lonMaxField, c);
			c.gridx = 3;
			c.gridy = 2;
			c.anchor = GridBagConstraints.EAST;
			c.insets = new Insets(0,4,0,2);
			this.add(depLabel, c);
			c.gridx = 4;
			c.gridy = 2;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(2,0,2,0);
			this.add(depMinField, c);
			c.gridx = 5;
			c.gridy = 2;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(2,4,2,0);
			this.add(depMaxField, c);
			c.gridx = 6;
			c.gridy = 2;
			c.anchor = GridBagConstraints.EAST;
			c.insets = new Insets(0,4,0,2);
			this.add(dateEndLabel, c);
			c.gridx = 7;
			c.gridy = 2;
			c.anchor = GridBagConstraints.EAST;
			c.insets = new Insets(2,0,2,0);
			this.add(dateEndField, c);
			
			
			// Fourth row (3)
			/*c.gridx = 0;
			c.gridy = 3;
			c.anchor = GridBagConstraints.EAST;
			c.insets = new Insets(0,0,0,2);
			this.add(srcLabel, c);
			c.gridx = 1;
			c.gridy = 3;
			c.gridwidth = 2;
			c.insets = new Insets(2,0,2,0);
			this.add(sourceNetPulldown, c);
			c.gridx = 2;
			c.gridy = 3;
			this.add(eventTypeLabel, c);
			c.gridx = 4;
			c.gridy = 3;
			c.gridwidth = 2;
			c.insets = new Insets(2,0,2,0);
			//this.add(eventTypePulldown, c);*/
			c.gridx = 0;
			c.gridy = 3;
			c.anchor = GridBagConstraints.EAST;
			c.insets = new Insets(0,0,0,2);
			this.add(maxEventsLabel, c);
			c.gridx = 1;
			c.gridy = 3;
			c.gridwidth = 2;
			c.insets = new Insets(2,0,2,0);
			this.add(maxEventsField, c);
			
			//sourceNetPulldown.addActionListener(this);
			
			maxEventsField.setText("10000");
			
			c.gridx = 1;
			c.gridy = 4;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.anchor = GridBagConstraints.NORTHWEST;
			this.add(importButton,c);
			//this.add(srcExplainText, c);
			//srcExplainText.setLineWrap(true);
			//srcExplainText.setColumns(50);
			//srcExplainText.getPreferredSize();
			/*srcExplainText.setBackground(null);
			srcExplainText.setEnabled(false);
			srcExplainText.setDisabledTextColor(Color.black);
			srcExplainText.setText(						
					"SCSN: Default Southern California Catalog \n" +
					"Earthquakes located by the Southern California Seismic Network \n" +
					"1932-present \n" +
					"Includes global data provided by the NEIC");
		}*/
		}
		
		public boolean processRequest() {
			String formatErrors = "";
			String rangeErrors  = "";
			String otherErrors  = "";

			// must initialize to something:
			double latMin = -90;
			double latMax = 90;
			double lonMin = -180;
			double lonMax = 180;
			double depMin = 0;
			double depMax = 0;
			double magMin = 0;
			double magMax = 0;
			String dateStart;
			String dateEnd;
			
			boolean latMinSet = false;
			boolean latMaxSet = false;
			boolean lonMinSet = false;
			boolean lonMaxSet = false;
			boolean depMinSet = false;
			boolean depMaxSet = false;
			boolean magMinSet = false;
			boolean magMaxSet = false;
			boolean dateStartSet = false;
			boolean dateEndSet = false;
			
			///////////////
			// Latitude //
			///////////////
			
			if (latMinField.getText().length() > 0) {
				try {
					latMin = Double.parseDouble(latMinField.getText());
					if (latMin < -90 || latMin > 90) rangeErrors += "    Latitude minimum\n";
					else latMinSet = true;
				} catch (NumberFormatException e) {
					formatErrors += "    Latitude minimum\n";
				}
			}
			if (latMaxField.getText().length() > 0) {
				try {
					latMax = Double.parseDouble(latMaxField.getText());
					if (latMax < -90 || latMax > 90) rangeErrors += "    Latitude maximum\n";
					else latMaxSet = true;
				} catch (NumberFormatException e) {
					formatErrors += "    Latitude maximum\n";
				}
			}
			if ((latMinSet && !latMaxSet) || (!latMinSet && latMaxSet))
				otherErrors += "    Latitude minimum and maximum must both be valid\n";
			if ((latMinSet && latMaxSet) && (latMin > latMax))
				otherErrors += "    Latitude minimum must be less than maximum\n";
			
			
			///////////////
			// Longitude //
			///////////////
			
			if (lonMinField.getText().length() > 0) {
				try {
					lonMin = Double.parseDouble(lonMinField.getText());
					if (lonMin < -180 || lonMin > 180) rangeErrors += "    Longitude minimum\n";
					else lonMinSet = true;
				} catch (NumberFormatException e) {
					formatErrors += "    Longitude minimum\n";
				}
			}
			if (lonMaxField.getText().length() > 0) {
				try {
					lonMax = Double.parseDouble(lonMaxField.getText());
					if (lonMax < -180 || lonMax > 180) rangeErrors += "    Longitude maximum\n";
					else lonMaxSet = true;
				} catch (NumberFormatException e) {
					formatErrors += "    Longitude maximum\n";
				}
			}
			if ((lonMinSet && !lonMaxSet) || (!lonMinSet && lonMaxSet))
				otherErrors += "    Longitude minimum and maximum must both be valid\n";
			if ((lonMinSet && lonMaxSet) && (lonMin > lonMax))
				otherErrors += "    Longitude minimum must be less than maximum\n";
			
			
			///////////
			// Depth //
			///////////
			
			if (depMinField.getText().length() > 0) {
				try {
					depMin = Double.parseDouble(depMinField.getText());
					if (depMin < 0) rangeErrors += "    Depth minimum\n";
					else depMinSet = true;
				} catch (NumberFormatException e) {
					formatErrors += "    Depth minimum\n";
				}
			}
			if (depMaxField.getText().length() > 0) {
				try {
					depMax = Double.parseDouble(depMaxField.getText());
					if (depMax < 0) rangeErrors += "    Depth maximum\n";
					else depMaxSet = true;
				} catch (NumberFormatException e) {
					formatErrors += "    Depth maximum\n";
				}
			}
			if ((depMinSet && !depMaxSet) || (!depMinSet && depMaxSet))
				otherErrors += "    Depth minimum and maximum must both be valid\n";
			if ((depMinSet && depMaxSet) && (depMin > depMax))
				otherErrors += "    Depth minimum must be less than maximum\n";
			
			
			///////////////
			// Magnitude //
			///////////////
			
			if (magMinField.getText().length() > 0) {
				try {
					magMin = Double.parseDouble(magMinField.getText());
					if (magMin < -180 || magMin > 180) rangeErrors += "    Magnitude minimum\n";
					else magMinSet = true;
				} catch (NumberFormatException e) {
					formatErrors += "    Magnitude minimum\n";
				}
			}
			if (magMaxField.getText().length() > 0) {
				try {
					magMax = Double.parseDouble(magMaxField.getText());
					if (magMax < -180 || magMax > 180) rangeErrors += "    Magnitude maximum\n";
					else magMaxSet = true;
				} catch (NumberFormatException e) {
					formatErrors += "    Magnitude maximum\n";
				}
			}
			if ((magMinSet && !magMaxSet) || (!magMinSet && magMaxSet))
				otherErrors += "    Magnitude minimum and maximum must both be valid\n";
			if ((magMinSet && magMaxSet) && (magMin > magMax))
				otherErrors += "    Magnitude minimum must be less than maximum\n";
			
			
			//////////
			// Date //
			//////////

			dateStart = dateStartField.getText();
			dateEnd   = dateEndField.getText();
			
			if (dateStart.length() > 0 && !dateStart.equals("YYYY/MM/DD")) {
				if (Pattern.matches("[0-9]{4}/[0-9]{2}/[0-9]{2}", dateStart) || 
						Pattern.matches("\\-[0-9]+(s|m|h|d)", dateStart))
					dateStartSet = true;
				else
					formatErrors += "    Start date\n" +
							"        Absolute: YYYY/MM/DD\n" +
							"        Relative: -#s, -#m, -#h, or -#d\n";
			}
			if (dateEnd.length() > 0 && !dateEnd.equals("YYYY/MM/DD")) {
				if (Pattern.matches("[0-9]{4}/[0-9]{2}/[0-9]{2}", dateEnd) || 
						Pattern.matches("\\+[0-9]+(s|m|h|d)", dateEnd))
					dateEndSet = true;
				else
					formatErrors += "    End date\n" +
							"        Absolute: YYYY/MM/DD\n" +
							"        Relative: +#s, +#m, +#h, or +#d\n";
			}
			if ((dateStartSet && !dateEndSet) || (!dateStartSet && dateEndSet))
				otherErrors += "    Start and end dates must both be valid\n";
			if (dateStartSet && dateEndSet) {
				if (Pattern.matches("[0-9]{4}/[0-9]{2}/[0-9]{2}", dateStart) &&
						Pattern.matches("[0-9]{4}/[0-9]{2}/[0-9]{2}", dateEnd)) {
					DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
					try {
						Date start = df.parse(dateStart);
						Date end = df.parse(dateEnd);
						if (start.compareTo(end) >= 0)
							otherErrors += "    Start date must be before end date\n";
					} catch (ParseException e) {
						formatErrors += "    Start or end date formatted incorrectly\n";
					}
				}
			} else {
				otherErrors += "    Start and end date must be set\n";
			}
			
			////////////////
			// Max Events //
			////////////////
			
			int maxEvents = 1000;
			if (maxEventsField.getText().length() > 0) {
				try {
					maxEvents = Integer.parseInt(maxEventsField.getText());
					if (maxEvents <= 0) {
						otherErrors += "    Invalid max number of events\n";
					}
					else if (maxEvents > 10000) {
						int result = JOptionPane.showConfirmDialog(this,
								"A maximum that is too large may result in a long wait.\n" +
								"Are you sure you want to continue?",
							    "Warning",
							    JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.NO_OPTION)
							return false;
					}
				} catch (NumberFormatException e) {
					formatErrors += "    Maximum number of events\n";
				}
			} else {
				otherErrors += "    Must specify maximum number of events\n";
			}
			
			// Display errors:
			String errors = "";
			if (formatErrors.length() > 0)
				errors += "Incorrectly formatted:\n" + formatErrors;
			if (rangeErrors.length() > 0)
				errors += "Out of range:\n" + rangeErrors;
			if (otherErrors.length() > 0)
				errors += "Other errors:\n" + otherErrors;
			
			if (errors != "") {
				JOptionPane.showMessageDialog(this,
						errors,
					    "Format Error",
					    JOptionPane.PLAIN_MESSAGE);
				return false;
			}
			
			String requestString = "";
			//String requestString = "EVENT";
			//String requestString = "ALTLOC";

			if (dateStartSet && dateEndSet)
				requestString += " -t0 " + dateStart + " " + dateEnd;
			else
				return false;
				
			if (latMinSet && latMaxSet)
				requestString += " -lat " + latMin + " " + latMax;
			if (lonMinSet && lonMaxSet)
				requestString += " -lon " + lonMin + " " + lonMax;
			if (depMinSet && depMaxSet)
				requestString += " -depth " + depMin + " " + depMax;
			if (magMinSet && magMaxSet)
				requestString += " -mag " + magMin + " " + magMax;
			
			/*if (sourceNetPulldown.getSelectedIndex() == 0) {
				if (eventTypePulldown.getSelectedIndex() != 0) {
					requestString += " -type "; //default to all events
					switch (eventTypePulldown.getSelectedIndex()) {
					case 1: // Local
						requestString += "le";
						break;
					case 2: // Regional
						requestString += "re";
						break;
					case 3: // Teleseism
						requestString += "ts";
						break;
					case 4: // Quarry Blast
						requestString += "qb";
						break;
					case 5: // Nuclear Blast
						requestString += "nt";
						break;
					case 6: // Sonic Boom
						requestString += "sn";
						break;
					case 7: // Uknown Event
						requestString += "uk";
						break;
					}
				}
			}*/
			
			/*requestString += "";
			switch (sourceNetPulldown.getSelectedIndex()) {
			case 0: // SCSN (default)
				requestString = "EVENT" +  requestString;
				break;
			case 1: // DS2000
				requestString = "ALTLOC" +  requestString + " -source DS2000";
				break;
			case 2: // SHLK2003
				requestString = "ALTLOC" +  requestString + " -source SHLK2003";
				break;
			case 3: // HAUK2003
				requestString = "ALTLOC" +  requestString + " -source HAUK2003";
				break;
			case 4: // HAUK2004
				requestString = "ALTLOC" +  requestString + " -source HAUK2004";
				break;
			}
			
			requestString += " \n";
			
			System.out.println(requestString);*/
			
			/*File tempFile = null;
			try {
				tempFile = File.createTempFile("net_cat_temp", ".dat");
				
				NetSourceParser parser;
				
				if (sourceNetPulldown.getSelectedIndex() == 0)
					parser = new STPParserEVENT();
				else
					parser = new STPParserALTLOC();
				
				client.setMaxEvents(maxEvents);
				client.setRequest(requestString);
				
				NetSource eqdb = new NetSource(client, parser);
				
				eqdb.writeToFile(tempFile);
				
				if (tempFile != null) {
					SourceCatalog newSource = new SourceCatalog(parent);
					newSource.setName(defaultName);
					if (newSource.processFile(tempFile, false)) {
						ListModel list = parent.getSourceList().getModel();
						SourceCatalog srcCat = (SourceCatalog)list.getElementAt(list.getSize()-1);
						parent.generateNewCatalog(srcCat, false);
						parent.getSourceList().deleteCatalog(srcCat, false);
					}
					tempFile.delete();
				}
				
			} catch (Exception e) {
				e.printStackTrace();

				//System.out.println(e.getClass());
				
				if (e.getClass() == NullPointerException.class) {
					String newTimeoutString = JOptionPane.showInputDialog(this,
							"Your request timed out after " + client.getTimeoutSeconds() + " seconds.\n\n" +
							"You may enter a longer timeout below, or enter\n" +
							"'0' for no timeout (at your own risk, SCEC-VDO\n" + 
							"may stop responding) or click cancel to use the\n" +
							"existing timeout value.\n",
						    "Timeout Error",
						    JOptionPane.PLAIN_MESSAGE);
					int newTimeout = -1;
					try {
						newTimeout = Integer.parseInt(newTimeoutString);
						client.setTimeoutSeconds(newTimeout);
					} catch (Exception e1) {
					}
				}
				return false;
			}*/
			return true;
		}
		public void getComcatData(double minDepth,double maxDepth,double minMagnitude,double maxMagnitude,double minLat,double maxLat,double minLon,double maxLon,String startTime,String endTime,int limit)
		{
			 EventWebService service = null;
			 //masterEarthquakeCatalogBranchGroup = new ArrayList<vtkActor>();
				try {
					//call usgs service to obtain earthquake catalog
					service = new EventWebService(new URL("http://earthquake.usgs.gov/fdsnws/event/1/"));
				} catch (MalformedURLException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
				
				//create query url
				EventQuery query = new EventQuery();
				
				//TODO: Preconditions.checkState(minDepth < maxDepth, "Min depth must be less than max depth");
				if(minDepth>0)
				query.setMinDepth(new BigDecimal(minDepth));
				if(maxDepth>0)
				query.setMaxDepth(new BigDecimal(maxDepth));
				if(minMagnitude>0)
				query.setMinMagnitude(new BigDecimal(minMagnitude));
				if(maxMagnitude>0)
				query.setMaxMagnitude(new BigDecimal(maxMagnitude));
				
				query.setMinLatitude(new BigDecimal(minLat));
				query.setMaxLatitude(new BigDecimal(maxLat));
				
				query.setMinLongitude(new BigDecimal(minLon));
				query.setMaxLongitude(new BigDecimal(maxLon));
				
				query.setLimit(limit);
				
				query.setOrderBy(OrderBy.TIME_ASC);
				
				Date startDate,endDate;
				try {
					startDate = new  SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse(startTime);
					query.setStartTime(startDate);
					endDate = new  SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse(endTime);
					query.setEndTime(endDate);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					//print the URL
					System.out.println(service.getUrl(query, Format.GEOJSON));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			List<JsonEvent> events;
			try {
				//get the events from the query
				events = service.getEvents(query);
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
			System.out.println(events);
			ArrayList<vtkActor> masterEarthquakeCatalogBranchGroup = new ArrayList<>();
			for (JsonEvent event : events) {
				//plot the earthquakes as spheres with radius as magnitude
				double[] xForm = new double[3];
				double[] latlon = new double[3];
				double depth=0,mag=0,lon=0,lat=0;
				vtkSphereSource sphereSource = new vtkSphereSource();
				 latlon[0] = Transform.calcRadius(event.getLatitude().doubleValue()) + (-event.getDepth().doubleValue());
                 // Phi= deg2rad(latitude);
                 latlon[1] = (event.getLatitude().doubleValue());
                 //Theta= deg2rad(longitude);
                 latlon[2] =  (event.getLongitude().doubleValue());
                 
                 xForm = Transform.customTransform(latlon);
                 
				sphereSource.SetCenter(xForm[0], xForm[1], xForm[2]);
				if(event.getMag()!=null)
				sphereSource.SetRadius(event.getMag().doubleValue());
				else
				{
					sphereSource.SetRadius(0.1);
				}
		    	
				vtkPolyDataMapper mapperEQCatalog = new vtkPolyDataMapper();
				mapperEQCatalog.SetInputConnection(sphereSource.GetOutputPort());
				
				vtkActor actorEQCatalog = new vtkActor();
				actorEQCatalog.SetMapper(mapperEQCatalog);
				actorEQCatalog.GetProperty().SetColor(1,1,0);
				masterEarthquakeCatalogBranchGroup.add(actorEQCatalog);
				if(event.getMag()!=null)
					mag= event.getMag().doubleValue();
				if(event.getDepth()!=null)
					depth= event.getDepth().doubleValue();
				if(event.getLatitude()!=null)
					lat= event.getLatitude().doubleValue();
				if(event.getLongitude()!=null)
					lon= event.getLongitude().doubleValue();
				Earthquake eq = new Earthquake(depth,mag,lat,lon, startTime, endTime,limit,actorEQCatalog);
				if(!masterEarthquakeCatalogsList.contains(eq))
					masterEarthquakeCatalogsList.add(eq);
			}
			
			
			Info.getMainGUI().updateActors(masterEarthquakeCatalogBranchGroup);
			Info.getMainGUI().updateRenderWindow();
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if (src == this.importButton) {
				//getComcatData(0,0,0,0,32,36,-122,-114,"2016/04/15","2016/04/22");
				if(processRequest()){
					double depthMin=0;
					double depthMax=0;
					double magMin=0;
					double magMax=0;
					if (depMinField.getText().length() > 0) {
						depthMin = Double.parseDouble(depMinField.getText());
					}
					if (depMaxField.getText().length() > 0) {
						depthMin =  Double.parseDouble(depMaxField.getText());
					}
					if (magMinField.getText().length() > 0) {
						magMin = Double.parseDouble(magMinField.getText());
					}
					if (magMaxField.getText().length() > 0) {
						magMax = Double.parseDouble(magMaxField.getText());
					}
				getComcatData(depthMin,depthMax, magMin,magMax, Double.parseDouble(latMinField.getText()),Double.parseDouble(latMaxField.getText()), 
						Double.parseDouble(lonMinField.getText()), Double.parseDouble(lonMaxField.getText()), dateStartField.getText(), dateEndField.getText(),Integer.parseInt(maxEventsField.getText()));
				}
			}
			//Describe catalog from which data is being retrieved
			/*if (src == this.sourceNetPulldown) {
				if (this.sourceNetPulldown.getSelectedIndex() == 0) {
					this.eventTypePulldown.setEnabled(true);
					srcExplainText.setText(
							"SCSN: Default Southern California Catalog \n" +
							"Earthquakes located by the Southern California Seismic Network \n" +
							"1932-present \n" +
							"Includes global data provided by the NEIC"); 
				}	
				else if (this.sourceNetPulldown.getSelectedIndex() == 1) {
					this.eventTypePulldown.setEnabled(false);
					srcExplainText.setText(
							"DS2000: Dinger-Shearer Catalog (relocated) \n" +
							"Southern California seismicity 1975-1998 \n" +
							"locations obtained using source-specific stations terms \n");
				}
				else if (this.sourceNetPulldown.getSelectedIndex() == 2) {
					this.eventTypePulldown.setEnabled(false);
					srcExplainText.setText(
							"SHLK2003: Shearer, Hauksson, Lin, Kilb (relocated) \n" +
							"Southern California event locations computed by the \n" +
							"source-specific stations terms, waveform cross-correlation \n" +
							"and cluster analysis methods, 1984-2003");
				}
				else if (this.sourceNetPulldown.getSelectedIndex() == 3) {
					this.eventTypePulldown.setEnabled(false);
					srcExplainText.setText(
							"HAUK2003: Hauksson (relocated) \n" +
							"3-D earthquake locations \n" +
							"1981-2003 \n");
				}
				else if (this.sourceNetPulldown.getSelectedIndex() == 4) {
					this.eventTypePulldown.setEnabled(false);
					srcExplainText.setText(
							"HAUK2004: Hauksson (relocated) \n" +
							"3-D earthquake locations \n" +
							"1981-2004 \n");
				}  
				else  {
					this.eventTypePulldown.setEnabled(false);
					srcExplainText.setText("");
				}
			}*/
		}
		public ArrayList<Earthquake> getAllEarthquakes()
		{
			return masterEarthquakeCatalogsList;
		}
		/*public void setLatMin(String value) { latMinField.setText(value); }
		public void setLatMax(String value) { latMaxField.setText(value); }
		public void setLonMin(String value) { lonMinField.setText(value); }
		public void setLonMax(String value) { lonMaxField.setText(value); }
		public void setMagMin(String value) { magMinField.setText(value); }
		public void setMagMax(String value) { magMaxField.setText(value); }
		public void setDepMin(String value) { depMinField.setText(value); }
		public void setDepMax(String value) { depMaxField.setText(value); }
		public void setDateStart(String value) { dateStartField.setText(value); }
		public void setDateEnd(String value)   { dateEndField.setText(value);   }
		public void setMaxEvents(String value) { maxEventsField.setText(value); }
		//public void setSourceNet(int value) { sourceNetPulldown.setSelectedIndex(value); }
		//public void setEventType(int value) { eventTypePulldown.setSelectedIndex(value); }
		public void setDefaultName(String name){ this.defaultName=name; }*/
		
		/*public void setServer(String newServer) {
			if (this.client != null) {
				this.client.setServer(newServer);
			}
		}*/
}
