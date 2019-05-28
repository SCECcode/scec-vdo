package org.scec.vtk.plugins.CommunityfaultModelPlugin.components;

import java.io.*;
import java.util.*;
import java.awt.*;

import org.apache.log4j.Logger;
import org.apache.log4j.chainsaw.Main;
import org.scec.vtk.main.MainGUI;

import org.scec.vtk.plugins.utils.DataAccessor;
import org.scec.vtk.plugins.utils.DataImport;
import org.scec.vtk.plugins.utils.components.ChoiceDialog;
import org.scec.vtk.plugins.utils.components.ObjectInfoDialog;
import org.scec.vtk.tools.Prefs;
import org.scec.vtk.tools.Transform;
import org.scec.vtk.tools.convert;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkGraphToPolyData;
import vtk.vtkMath;
import vtk.vtkMutableDirectedGraph;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphericalTransform;
import vtk.vtkTransformPolyDataFilter;
import vtk.vtkTriangle;
import vtk.vtkTriangleStrip;
import org.scec.vtk.plugins.CommunityfaultModelPlugin.*;


/**
 * Instances of this class process TSurf (GOCAD *.ts) files into attribute (*.flt)
 * and binary object data (*.dat) files. Once
 * complete (or error aborted), processing methods return true (or false) and properties
 * of each file are available via accessor methods.
 * 
 * Created on Jan 30, 2005
 * 
 * @author P. Powers
 * @version $Id: TSurfImport.java 3253 2010-05-20 18:46:50Z kmilner $
 */
public class TSurfImport implements DataImport {

	private Logger log = Logger.getLogger(TSurfImport.class);

	private File[] filesIn = null;
	private ArrayList<Fault3D> faultsOut = null;
	private Component owner = null;

	// utility fields
	private Fault3D fault = null;

	private String importID = null;
	private String tmpDisplayName = null;
	private String tmpDatFile = null;
	private String tmpAttFile = null;
	private String tmpCitation;
	private String tmpReference;
	private String tmpNotes;

	// conversion constants (these are for SoCal)
	private double spheroid = 20;
	private double zone = 11;



	/**
	 * Constructs a new import object for TSurf (*.ts) files.
	 * 
	 * @param files array of files to be processed
	 * @param parent <code>Component</code> used for <code>JOptionPane</code> ownership
	 */
	public TSurfImport(Component parent, File[] files) {
		// parent is used as owner for dialogs and to access  
		this.owner = parent;
		this.filesIn = files;
	}

	public ArrayList<Fault3D> processFiles() {
		return processFiles(true,null);
	}

	//****************************************
	//     PUBLIC UTILITY METHODS
	//****************************************

	/**
	 * Initiates import of files specified in constructor. Method converts *.ts
	 * files to *.flt and *.dat files and stores them in the <i>ScecVideo</i>
	 * data library.
	 * 
	 * @param showDialog - Specifies whether we need to show window asking for group name. This parameter 
	 * was added because sometimes we just want to give some default group name and pre-load the CFM surfaces
	 * 
	 * @param groupName - Default Group name when user is not prompted for a group name
	 * 
	 * @return array of *.flt files processed; empty array if process cancelled
	 */
	public ArrayList<Fault3D> processFiles(boolean showDialog, String groupName) {

		// TODO progress bar - requires running import in separate thread
		// TODO check string format for illegal characters
		// TODO focus should be in text field when dialog is made visible

		// set status
		//setStatus(IMPORT_START);

		// initialize output array
		this.faultsOut = new ArrayList<Fault3D>();

		// get existing import group names
		File dataDir = new File(Prefs.getLibLoc() + File.separator + 
				CommunityFaultModelPlugin.dataStoreDir);
		File[] existingImports = dataDir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				return false;
			}
		});
		String[] choices = (existingImports != null) ?
				new String[existingImports.length] : new String[0];
				for (int i=0; i<choices.length; i++) {
					choices[i] = existingImports[i].getName();
				}

				this.importID = groupName;
				if(showDialog) {
					ChoiceDialog nameChooser = new ChoiceDialog(
							this.owner,
							"Create Import Name",
							true,
							"Please provide a group name for this import:",
							"[ e.g. CFMv1, USGS ]",
							choices,
							true);
					nameChooser.setVisible(true);
					this.importID = nameChooser.getInput();
				}    
				// check for cancelled import
				if (this.importID == null) {
					// setStatus(IMPORT_CANCEL);
					// return empty array of files
					return this.faultsOut;
				}

				// create group directory

				File groupDir = new File(
						Prefs.getLibLoc() + 
						File.separator + CommunityFaultModelPlugin.dataStoreDir + 
						File.separator + this.importID +
						File.separator + "data");
				if (!groupDir.exists()) {
					groupDir.mkdirs();
				}
				if(showDialog) {
					// get reference info that will be applied to all imports in group
					ObjectInfoDialog objInfo = ((CommunityFaultModelGUI)this.owner).getSourceInfoDialog();
					objInfo.showInfo("Add import information");
					if (objInfo.windowWasCancelled()) {
						//setStatus(IMPORT_CANCEL);
						return this.faultsOut;
					}
					this.tmpCitation = objInfo.getCitation();
					this.tmpReference = objInfo.getReference();
					this.tmpNotes = objInfo.getNotes();
				}
				// cycle through files
				int numFiles = this.filesIn.length;
				int failureCount = 0;
				for (int i=0; i<numFiles; i++) {
					this.fault = null;
					File f = this.filesIn[i];
					this.fault = (Fault3D)processFile(f);
					if (this.fault != null) {
						this.faultsOut.add(this.fault);
					} else {
						failureCount += 1;
					}
				}

				// if no error has occurred, change status massage
				if (failureCount == 0) {
					//setStatus(IMPORT_END);
				} else {
					// setStatus(IMPORT_FAILED + failureCount + " errors");
				}
				return this.faultsOut;
	}


	/**
	 * Processes an individual TSurf file. Method creates a new <code>Fault3D</code>.
	 * 
	 * @param file to process
	 * @return <i>ScecVideo</i> object
	 */
	public DataAccessor processFile(File file) { 
		Fault3D newFault = new Fault3D();
		initNames(file);

		newFault.setObjectClass(newFault.getClass().getName());
		newFault.setSourceFile(file.getPath());
		newFault.setAttributeFile(this.tmpAttFile);
		newFault.setDataFile(this.tmpDatFile);
		newFault.setDisplayName(this.tmpDisplayName);
		newFault.setCitation(this.tmpCitation);
		newFault.setReference(this.tmpReference);
		newFault.setNotes(this.tmpNotes);

		readSourceFile(file, newFault);
		/*if (!readSourceFile(file, newFault)) {
            //setStatus(ERROR_FILE_READ);
            return null;
        }*/

		if (!(newFault.writeAttributeFile() && newFault.writeDataFile())) {
			// setStatus(ERROR_FILE_WRITE);
			return null;
		}
		newFault.setInMemory(true);
		//newFault.getFaultBranch());
		return newFault;
	}

	private void initNames(File file) {
		String tempName = file.getName().substring(0,file.getName().lastIndexOf("."));
		String fileOutName;
		StringTokenizer st = new StringTokenizer(tempName,"_");

		// set stripped name for future use when exporting data
		// drop 'cfma' or similar and reset displayName
		if (tempName.startsWith("cfma")) {
			fileOutName = tempName.substring(5);
			st.nextToken();
		} else if (tempName.startsWith("cfm")) {
			fileOutName = tempName.substring(4);
			st.nextToken();
		} else if (tempName.startsWith("pre_cfma")) {
			fileOutName = tempName.substring(9);
			st.nextToken();
			st.nextToken();
		} else {
			fileOutName = tempName;
		}

		// set output file names/paths
		this.tmpDatFile = File.separator + CommunityFaultModelPlugin.dataStoreDir +
				File.separator + this.importID +
				File.separator + "data" +
				File.separator + fileOutName + ".dat";
		this.tmpAttFile = File.separator + CommunityFaultModelPlugin.dataStoreDir +
				File.separator + this.importID +
				File.separator + fileOutName + ".flt";

		// process name
		tempName = "";
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			tempName += s.substring(0,1).toUpperCase() + s.substring(1) + " ";
		}
		tempName += "[" + this.importID + "] ";
		this.tmpDisplayName = tempName;
	}

	// cull data from a TSurf(*.ts) file
	private void readSourceFile(File file, Fault3D obj) {

		vtkPoints vertexArray = new vtkPoints();
		Color c = (Color.BLUE);

		// Create an actor
		vtkActor actorassign =new vtkActor();
		vtkCellArray triangles = new vtkCellArray();
		try {
			BufferedReader inStream = new BufferedReader(new FileReader(file));       
			String line;      

			// read lines from TSurf file
			while ((line = inStream.readLine()) != null){
				StringTokenizer data = new StringTokenizer(line);                
				if (data.hasMoreTokens()){
					String temp = data.nextToken();

					// search for and catalog vertices
					if ( temp.equals("VRTX") || temp.equals("PVRTX")) {
						data.nextToken();
						// get coordinates
						double[] latlon1 = getLatLon(
								Double.parseDouble(data.nextToken()), //latitude
								Double.parseDouble(data.nextToken())); //longitude

						double zVal = Double.parseDouble(data.nextToken())/1000;


						double[] x = new double[3];
						x = Transform.transformLatLonHeight(latlon1[0], latlon1[1], zVal);

						vertexArray.InsertNextPoint(x);
						continue;
					}

					// search for and catalog triangle info
					else if ( temp.equals("TRGL")){


						// cull triangle definitions (subtract 1 so that vertex id's match 
						// the array position of individual vertices
						int[] triangle1 = {
								Integer.parseInt(data.nextToken())-1,
								Integer.parseInt(data.nextToken())-1,
								Integer.parseInt(data.nextToken())-1
						};

						vtkTriangle triangle =new vtkTriangle();
						triangle.GetPointIds().SetId(0, triangle1[0]);
						triangle.GetPointIds().SetId(1, triangle1[1]);
						triangle.GetPointIds().SetId(2, triangle1[2]);

						triangles .InsertNextCell(triangle);
						continue;
					}

					// a very few lines start with "ATOM" which acts as an alias to another
					// vertex so cull the referenced vertex from the vertex array.
					else if (temp.equals("ATOM")) {
						data.nextToken();
						int alias = Integer.parseInt(data.nextToken());
						vertexArray.InsertNextPoint(vertexArray.GetPoint(alias));
					}

					// get default color for fault; if no per-triangle color exists
					// this color is assigned to each one. Also, some tsurfs have a color name
					// assigned; if one is encountered assign a default color of Color.RED
					if (temp.startsWith("*solid*color:")) {
						try {
							c = new Color(
									Float.parseFloat(temp.substring(13, temp.length())),
									Float.parseFloat(data.nextToken()),
									Float.parseFloat(data.nextToken()));
							actorassign.GetProperty().SetColor(c.getRed(),c.getGreen(),c.getBlue());
						}
						catch (NumberFormatException nfe) {
							c=Color.lightGray;
							//actorassign.GetProperty().SetColor(Color.lightGray.getRed(),Color.lightGray.getGreen(),Color.lightGray.getBlue());
						}
					}
					else
					{
						// doublecheck that color gets set
						c=Color.lightGray;
						//actorassign.GetProperty().SetColor(Color.lightGray.getRed(),Color.lightGray.getGreen(),Color.lightGray.getBlue());
					}

				}
			}
			inStream.close();
		}

		catch (Exception e) {
			actorassign = null;
		}
		obj.vertices = vertexArray;
		obj.triangles = triangles;
		obj.colors = c;
		obj.graphToPlyData = new vtkPolyData();
		obj.graphToPlyData.SetPoints(obj.vertices); //graphToPolyData.GetOutput();
		obj.graphToPlyData.SetPolys(obj.triangles);
	}


	/** 
	 * converts UTM coords to Lat Lon
	 */
	public double[] getLatLon(double UTMx, double UTMy) {
		// TODO get coordinate conversion localization information
		// from preference file or popup window

		convert c = new convert();
		c.setAllValues(UTMx, UTMy, this.spheroid, this.zone);
		return c.UTMToLatLon(true); // TODO made this northernHemisphere=true to fix error...should double check
	}

}