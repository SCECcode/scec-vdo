package org.scec.vtk.plugins.EarthquakeCatalogPlugin.Components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPlugin;
import org.scec.vtk.plugins.EarthquakeCatalogPlugin.EarthquakeCatalogPluginGUI;
import org.scec.vtk.tools.Prefs;

import gov.usgs.earthquake.event.EventQuery;
import gov.usgs.earthquake.event.EventWebService;
import gov.usgs.earthquake.event.Format;
import gov.usgs.earthquake.event.JsonEvent;
import gov.usgs.earthquake.event.OrderBy;

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
	private JLabel timeStartLabel = new JLabel("Start Time (UTC):");
	private JLabel timeEndLabel   = new JLabel("End Time (UTC):");
	//private JLabel srcLabel    	  = new JLabel("Src:");
	//private JLabel eventTypeLabel = new JLabel("Type:");
	private JLabel maxEventsLabel = new JLabel("Max EQs:");

	private JTextField latMinField = new JTextField("32");
	private JTextField latMaxField = new JTextField("43");
	private JTextField lonMinField = new JTextField("-125");
	private JTextField lonMaxField = new JTextField("-114");
	private JTextField depMinField = new JTextField();
	private JTextField depMaxField = new JTextField();
	private JTextField magMinField = new JTextField("0.0");
	private JTextField magMaxField = new JTextField("9.0");
	private JTextField dateStartField = new JTextField("2019/07/01");
	private JTextField dateEndField   = new JTextField("2019/07/01");
	
	private JTextField timeStartField = new JTextField("00:00:00");
	private JTextField timeEndField = new JTextField("23:59:59");
	
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
	//for catalog import tracking
	private int count =1;

	//private ArrayList<vtkActor> masterEarthquakeCatalogBranchGroup; //to keep actors
	private ArrayList<Earthquake> masterEarthquakeCatalogsList = new ArrayList<>(); //to keep earthquakeInfo in memory
	public  ComcatResourcesDialog() {
		
	}

	public  ComcatResourcesDialog(JPanel parent) {

		super();

		this.parent = (EarthquakeCatalogPluginGUI)parent;

		this.setName("Network Sources");
		this.setSize(550,220);


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
		timeStartField.setPreferredSize(new Dimension(75, 20));
		timeStartField.setMinimumSize(new Dimension(75, 20));

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
		timeEndField.setPreferredSize(new Dimension(75, 20));
		timeEndField.setMinimumSize(new Dimension(75, 20));


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
		c.gridx = 8;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(2,4,2,0);
		this.add(timeStartLabel, c);
		c.gridx = 9;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0,4,0,2);
		this.add(timeStartField, c);


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
		c.gridx = 8;
		c.gridy = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(2,4,2,0);
		this.add(timeEndLabel, c);
		c.gridx = 9;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0,4,0,2);
		this.add(timeEndField, c);


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
		String timeStart;
		String timeEnd;

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
		boolean timeStartSet = false;
		boolean timeEndSet = false;

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
		boolean sameDate = false;

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
			if (Pattern.matches("[0-9]{4}:[0-9]{2}/[0-9]{2}", dateStart) &&
					Pattern.matches("[0-9]{4}/[0-9]{2}/[0-9]{2}", dateEnd)) {
				DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
				try {
					Date start = df.parse(dateStart);
					Date end = df.parse(dateEnd);
					if (start.compareTo(end) > 0)
						otherErrors += "    Start date must be on or before end date\n";
					else if(start.compareTo(end) == 0)
					{
						sameDate = true;
					}
				} catch (ParseException e) {
					formatErrors += "    Start or end date formatted incorrectly\n";
				}
			}
		} else {
			otherErrors += "    Start and end date must be set\n";
		}
		
		//////////
		// Time //
		//////////
		timeStart = timeStartField.getText();
		timeEnd   = timeEndField.getText();

		if (timeStart.length() > 0) {
			if (Pattern.matches("[0-9]{2}:[0-9]{2}:[0-9]{2}", timeStart)
					)
				timeStartSet = true;
			else
				formatErrors += "    Start date\n" +
						"        Absolute: YYYY/MM/DD\n" +
						"        Relative: -#s, -#m, -#h, or -#d\n";
		}
		if (timeEnd.length() > 0) {
			if (Pattern.matches("[0-9]{2}:[0-9]{2}:[0-9]{2}", timeEnd)
					)
				timeEndSet = true;
			else
				formatErrors += "    End date\n" +
						"        Absolute: YYYY/MM/DD\n" +
						"        Relative: +#s, +#m, +#h, or +#d\n";
		}
		if ((timeStartSet && !timeEndSet) || (!timeStartSet && timeEndSet))
			otherErrors += "    Start and end dates must both be valid\n";
		if (timeStartSet && timeEndSet) {
			if (Pattern.matches("[0-9]{2}:[0-9]{2}:[0-9]{2}", timeStart) &&
					Pattern.matches("[0-9]{2}:[0-9]{2}:[0-9]{2}", timeEnd)) {
				DateFormat df = new SimpleDateFormat("HH:mm:ss");
				try {
					Date start = df.parse(timeStart);
					Date end = df.parse(timeEnd);
					
					if (sameDate && start.compareTo(end) > 0)
						otherErrors += "    Start time must be before or at end time\n";
				} catch (ParseException e) {
					formatErrors += "    Start or end time formatted incorrectly\n";
				}
			}
		} else {
			otherErrors += "    Start and end time must be set\n";
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

		return true;
	}

	public void readFromComcatDataFile(EQCatalog cat, String filePath)
	{
		JSONParser parser = new JSONParser();
		JSONArray jsonArray = null;
		try {
			JSONObject jsonObj = (JSONObject) parser.parse(new FileReader(
					filePath));
			jsonArray = (JSONArray) jsonObj.get(cat.getDisplayName());
		} catch (IOException | org.json.simple.parser.ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(jsonArray!=null){
		float min_dep =    5.0f;
		float max_dep = -600.0f;
		float min_mag =   10.0f;
		float max_mag =    0.0f;
		int index=0;
		cat.initializeArrays(jsonArray.size());

		Date startDate;
		Date endDate;
		
		masterEarthquakeCatalogsList.clear();

		JsonEvent je = new JsonEvent((JSONObject) jsonArray.get(0));
		
		startDate=je.getTime();
		endDate=je.getTime();
		double maxLat=je.getLatitude().doubleValue();
		double minLat=je.getLatitude().doubleValue();
		double maxLon=je.getLongitude().doubleValue();
		double minLon=je.getLongitude().doubleValue();
		for (Object o : jsonArray) {
			JsonEvent event  = new JsonEvent((JSONObject) o);
			double depth=0,mag=0,lon=0,lat=0;
			if(event.getMag()!=null)
				mag= event.getMag().doubleValue();
			if(event.getDepth()!=null)
				depth= -event.getDepth().doubleValue();
			if(event.getLatitude()!=null)
				lat= event.getLatitude().doubleValue();
			if(event.getLongitude()!=null)
				lon= event.getLongitude().doubleValue();
			if(event.getDepth()!=null){
				if (depth <= min_dep) min_dep = (float) depth;
				if (depth >= max_dep) max_dep = (float) depth;
			}
			if(event.getMag()!=null){

				if (mag <= min_mag) min_mag = (float) mag;
				if (mag >= max_mag) max_mag = (float) mag;
			}
			
			if (lat <= minLat) minLat = (float) lat;
			if (lat >= maxLat) maxLat = (float) lat;
			
			if (lon <= minLon) minLon = (float) lon;
			if (lon >= maxLon) maxLon = (float) lon;
			
			cat.setEq_depth(index++, (float)depth);
			if(event.getTime().before(startDate))
				startDate = event.getTime();

			if(event.getTime().after(endDate))
				endDate = event.getTime();
			Earthquake eq = new Earthquake(-depth,mag,lat,lon, event.getTime(),jsonArray.size());
			
			if(!masterEarthquakeCatalogsList.contains(eq))
				masterEarthquakeCatalogsList.add(eq);
		}
		//parent.getCatalogTable().addCatalog(cat);
		//setting minimas and maximas
		if(jsonArray.size()!=0)
		{
			//cat.setComcatQuery(query);
			cat.setMaxMagnitude((float)max_mag);
			cat.setMinMagnitude((float)min_mag);
			cat.setMinDepth((float)-max_dep);
			cat.setMaxDepth((float)-min_dep);
			cat.setMinDate(startDate);
			cat.setMaxDate(endDate);
			cat.setNumEvents(jsonArray.size());
			cat.setMaxLatitude((float)maxLat);
			cat.setMinLatitude((float)minLat);
			cat.setMaxLongitude((float)maxLon);
			cat.setMinLongitude((float)minLon);
			cat.addComcatEqList();
		}
			System.out.println("no events found");
		
		}
		else{
			System.out.println("no events found");
		}
	}

	public void getComcatData(double minDepth,double maxDepth,double minMagnitude,double maxMagnitude,double minLat,double maxLat,double minLon,double maxLon,String startTime,String endTime,int limit)
	{
		EventWebService service = null;

		//masterEarthquakeCatalogBranchGroup = new ArrayList<vtkActor>();
		try {
			//call usgs service to obtain earthquake catalog
			service = new EventWebService(new URL("https://earthquake.usgs.gov/fdsnws/event/1/"));
		} catch (MalformedURLException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}

		int queryLimit = Integer.min(20000, limit);

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

		query.setLimit(queryLimit);

		query.setOrderBy(OrderBy.TIME_ASC);

		Date startDate = null,endDate=null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			startDate = sdf.parse(startTime);
			query.setStartTime(startDate);
			endDate = sdf.parse(endTime);
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
		
		System.out.println("Fetched "+events.size()+" events");

		//Do not make a catalog if there are no events. 
		if (events.size() ==0) {
			JOptionPane.showMessageDialog(this, "No events found. Please change your query.");
		} else {
			
			if (limit > queryLimit && events.size() >= queryLimit) {
				// need to stich together multiple calls
				System.out.println("Stitching together multiple calls");
				while (events.size() < limit) {
					query.setOffset(events.size()+1);
					try {
						//get the events from the query
						List<JsonEvent> subEvents = service.getEvents(query);
						events.addAll(subEvents);
						System.out.println("Fetched "+subEvents.size()+" events ("+events.size()+" in total)");
						if (subEvents.size() < queryLimit)
							break;
					} catch (Exception e) {
						throw ExceptionUtils.asRuntimeException(e);
					}
				}
				System.out.println("Fetched "+events.size()+" total events");
			}

			JSONObject obj = new JSONObject();
			JSONArray catalogList = new JSONArray();
			masterEarthquakeCatalogsList.clear();

			float min_dep =    5.0f;
			float max_dep = -600.0f;
			float min_mag =   10.0f;
			float max_mag =    0.0f;
			EQCatalog cat = new EQCatalog(parent);
			int index=0;
			cat.initializeArrays(events.size());
			cat.setDisplayName("_Imported_Catalog_" + count);
			count ++;
			for (JsonEvent event : events) {
				//plot the earthquakes as spheres with radius as magnitude
				//				double[] xForm = new double[3];
				double depth=0,mag=0,lon=0,lat=0;

				if(event.getMag()!=null)
					mag= event.getMag().doubleValue();
				if(event.getDepth()!=null)
					depth= -event.getDepth().doubleValue();
				if(event.getLatitude()!=null)
					lat= event.getLatitude().doubleValue();
				if(event.getLongitude()!=null)
					lon= event.getLongitude().doubleValue();
				if(event.getDepth()!=null){
					if (depth <= min_dep) min_dep = (float) depth;
					if (depth >= max_dep) max_dep = (float) depth;
				}
				if(event.getMag()!=null){

					if (mag <= min_mag) min_mag = (float) mag;
					if (mag >= max_mag) max_mag = (float) mag;
				}

				cat.setEq_depth(index++, (float)depth);

				Earthquake eq = new Earthquake(-depth,mag,lat,lon, event.getTime(),limit);
				if(!masterEarthquakeCatalogsList.contains(eq))
					masterEarthquakeCatalogsList.add(eq);

				//create a json object
				catalogList.add(event);
			}

			//write json object to file
			obj.put(cat.getDisplayName(), catalogList);
			writeToJSONFile(cat,obj);


			parent.getCatalogTable().addCatalog(cat);

			if (events.size() != 0) {

				//setting minimas and maximas
				//		if(events.size()!=0)
				//		{
				cat.setComcatQuery(query);
				cat.setMaxMagnitude((float)max_mag);
				cat.setMinMagnitude((float)min_mag);
				cat.setMinDepth((float)-max_dep);
				cat.setMaxDepth((float)-min_dep);
				cat.setMinDate(startDate);
				cat.setMaxDate(endDate);
				cat.setNumEvents(events.size());
				cat.setMaxLatitude((float)maxLat);
				cat.setMinLatitude((float)minLat);
				cat.setMaxLongitude((float)maxLon);
				cat.setMinLongitude((float)minLon);
				cat.addComcatEqList();
			}
		}
	}

	private void writeToJSONFile(EQCatalog cat,JSONObject obj) {
		//save in user's local directory
		String destinationData = Prefs.getLibLoc() + File.separator + EarthquakeCatalogPlugin.dataStoreDir +
				File.separator + "display" + File.separator + "data"+File.separator+
				cat.getDisplayName()+"-"+System.currentTimeMillis()+".json";
		//System.out.println("dd:"+destinationData);
		try (FileWriter file = new FileWriter(destinationData)) {
			file.write(obj.toJSONString());
			System.out.println("Successfully Copied JSON Object to File...");
			cat.setComcatFilePathString(destinationData);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				
				String startPeriod =  dateStartField.getText() + "T" + timeStartField.getText() + "Z";
				String endPeriod =  dateEndField.getText() + "T" + timeEndField.getText() + "Z";
				
				CalcProgressBar progress = new CalcProgressBar("Loading Catalog", "Please Wait");
				progress.setVisible(true);
				progress.setIndeterminate(true);
				//Attempts to keep it in the front. 
				progress.toFront();
				//Catalog calling. 
				getComcatData(depthMin,depthMax, magMin,magMax, Double.parseDouble(latMinField.getText()),Double.parseDouble(latMaxField.getText()), 
						Double.parseDouble(lonMinField.getText()), Double.parseDouble(lonMaxField.getText()), startPeriod, endPeriod,Integer.parseInt(maxEventsField.getText()));
				progress.setVisible(false);	
				progress.dispose();
				
				//Closes the window automatically 
				super.dispose();
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
