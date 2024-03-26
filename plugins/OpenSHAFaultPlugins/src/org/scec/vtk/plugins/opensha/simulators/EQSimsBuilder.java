package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Cursor;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.data.NamedComparator;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.iden.CatalogLengthLoadIden;
import org.opensha.sha.simulators.iden.LogicalAndRupIden;
import org.opensha.sha.simulators.iden.MagRangeRuptureIdentifier;
import org.opensha.sha.simulators.iden.RuptureIdentifier;
import org.opensha.sha.simulators.parsers.EQSIMv06FileReader;
import org.opensha.sha.simulators.parsers.RSQSimFileReader;
import org.scec.vtk.commons.opensha.faults.AbstractFaultIDComparator;
import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.DipColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.colorers.RakeColorer;
import org.scec.vtk.commons.opensha.faults.colorers.StrikeColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.SimulatorElementFault;
import org.scec.vtk.commons.opensha.tree.FaultCategoryNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;
import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.tools.Prefs;


public class EQSimsBuilder implements FaultTreeBuilder, ParameterChangeListener {
	
	/*
	 * ProgressBar represents a class that may be implemented elsewhere in its own 
	 * package so that any file may use it. 
	 */
	public class ProgressBar {
		CalcProgressBar progress;
		private Cursor waitCursor;
		
		//Default Constructor, loadingString represents the title of the 
		//Loading Screen you would want 
		public ProgressBar(String titleString, String loadingString) {
			waitCursor = new Cursor(Cursor.WAIT_CURSOR);
			progress = new CalcProgressBar(titleString, loadingString);
		}
		public void runProgressBar() {
			progress.setCursor(waitCursor);
			progress.setVisible(true);
			progress.setIndeterminate(true); 
			//progress.setAlwaysOnTop(true);

		}
		public void stopProgressBar() {
			progress.toFront();
			progress.setVisible(false);	
			progress.dispose();
		}
		public void changeLoadingMessage(String loadingString) {
			progress.setProgressMessage(loadingString);
		}

	}
	
	private static ArrayList<String> hardcodedInputs = new ArrayList<String>();
	static {
		// this is where you should add new files. only put the filename. the files should be stored
		// on opensha.usc.edu in /var/www/html/data/simulators/<filename>
		// and accessible at: http://opensha.usc.edu/data/simulators/<filename>
		hardcodedInputs.add("ALLCAL2_1-7-11_Geometry.dat");
		hardcodedInputs.add("ALLCAL_Ward_Geometry.dat");
		hardcodedInputs.add("NCAL2a_Ward_Geometry.dat");
		hardcodedInputs.add("NCAL3a_Ward_Geometry.dat");
		hardcodedInputs.add("NCAL4a_Ward_Geometry.dat");
		hardcodedInputs.add("UCERF3.D3.1.1km.tri.2.flt");
		hardcodedInputs.add("zfault_Deepen.in");
	}
	
	//Filtering Tools Drop Down Location
	private static ArrayList<String> filteringOptions = new ArrayList<String>();
	static {
		//Helps filter out for 2019 Grand Challenge. 
		filteringOptions.add("San Andreas, San Jacinto, Elsinore, Hayward (UCERF3 catalog data)");
		filteringOptions.add("San Andreas, San Jacinto, Elsinore, Hayward (RSQSim catalog data (zfault))");
	}
	private StringParameter filterOptions;
	String buttonchosen = "";
	List<? extends SimulatorEvent> events;
	
	
	/*
	 * Initialization of all buttons/drop downs. 
	 */
	private static final String EVENT_MIN_MAG_PARAM_NAME = "Min Event Mag To Load";
	private DoubleParameter eventMinMagParam;
	
	private static final String MAX_CAT_DURATION_PARAM_NAME = "Max Catalog Years To Load";
	private DoubleParameter catDurationParam;
	
	private static final String GEOM_PRESET_PARAM_NAME = "Geometry File Presets";
	private static final String GEOM_PRESET_NONE = "(none selected)";
	private StringParameter geomPresetParam;
	
	private static final String GEOM_FILE_SELECTOR_PARAM_NAME = "Geometry File";
	private FileParameter geomFileParam = new FileParameter(GEOM_FILE_SELECTOR_PARAM_NAME);
	
	private static final String EVENT_SELECTOR_PARAM_NAME = "Simulator Event File";
	private FileParameter eventFileParam = new FileParameter(EVENT_SELECTOR_PARAM_NAME);
	
	private static final String UTM_ZONE_NAME = "UTM Zone";
	private IntegerParameter utmZoneParam = new IntegerParameter(UTM_ZONE_NAME, 1, 60, (Integer)11);
	
	private static final String UTM_BAND_NAME = "UTM Band";
	private StringParameter utmBandParam = new StringParameter(UTM_BAND_NAME, "N");
	
	//builderParams is the main list for all objects displayed on this plugin.
	private ParameterList builderParams = new ParameterList();
	
	private TreeChangeListener l;
	
	//Color by Faults List
	private ArrayList<FaultColorer> colorers;
	//Animations List
	private ArrayList<FaultAnimation> animations;
	private EQSimsEventSlipAnim eventSlipAnim;

	
	//List of all Possible listeners
	private ArrayList<EQSimsEventListener> eventListeners = new ArrayList<EQSimsEventListener>();
	
	private File dataDir;

	private List<SimulatorElement> elements;
	
	//use for Simulator Event Reloading
	private File reloadFile;
	
	
	public EQSimsBuilder() {
		//File Directory
		dataDir = new File(Prefs.getDefaultLocation() + File.separator + "data" + File.separator + "EQSims");
		if (!dataDir.exists())
			dataDir.mkdirs();
		
		//Adding Geometry Presets
		ArrayList<String> strings = new ArrayList<String>();
		strings.add(GEOM_PRESET_NONE);
		for (String name : hardcodedInputs) {
			strings.add(name);
		}
		//Adding Filtering Options
		ArrayList<String> strings2 = new ArrayList<String>();
		strings2.add(GEOM_PRESET_NONE);
		for (String name : filteringOptions) {
			strings2.add(name);
		}
		
		//Min Event Mag input area
		eventMinMagParam = new DoubleParameter(EVENT_MIN_MAG_PARAM_NAME, -10d, 10d, new Double(7d));
		builderParams.addParameter(eventMinMagParam);
		
		//Max Catalog Yrs Loaded input area
		catDurationParam = new DoubleParameter(MAX_CAT_DURATION_PARAM_NAME, 0d, Double.POSITIVE_INFINITY, new Double(0d));
		builderParams.addParameter(catDurationParam);
		
		//Geometry File Presets
		geomPresetParam = new StringParameter(GEOM_PRESET_PARAM_NAME, strings, GEOM_PRESET_NONE);
		geomPresetParam.addParameterChangeListener(this);
		builderParams.addParameter(geomPresetParam);
		
		//Geometry File
		geomFileParam.addParameterChangeListener(this);
		geomFileParam.setShowHiddenFiles(true);
		
		// little kludge to make it convenient for me, but still defaults to the data dir for everyone else
		File kevinDir = new File("/home/kevin/Simulators/catalogs");
		File defaultDir;
		if (kevinDir.exists())
			defaultDir = kevinDir;
		else
			defaultDir = new File(MainGUI.getCWD(),	"data");
		if (defaultDir.exists())
			geomFileParam.setDefaultInitialDir(defaultDir);
		
		//Geometry file Initilization 
		builderParams.addParameter(geomFileParam);
		
		//Filtering Options Initialization
		filterOptions = new StringParameter("Filtering Tools", strings2, GEOM_PRESET_NONE);
		builderParams.addParameter(filterOptions);
		filterOptions.addParameterChangeListener(this);
		
		//Simulator Event File Initialization 
		builderParams.addParameter(eventFileParam);
		eventFileParam.addParameterChangeListener(this);
		eventFileParam.setShowHiddenFiles(true);
		
		builderParams.addParameter(utmZoneParam);
		builderParams.addParameter(utmBandParam);
			
		/*
		 * Initialization for all Coloring Options
		 */
		colorers = new ArrayList<FaultColorer>();
		
		EQSlipRateColorer slipColorer = new EQSlipRateColorer();
		eventListeners.add(slipColorer);
		colorers.add(slipColorer);
		
		EQSimsParticipationColorer particColor = new EQSimsParticipationColorer();
		eventListeners.add(particColor);
		colorers.add(particColor);
		
		EQSimsDepthColorer depthColor = new EQSimsDepthColorer();
		colorers.add(depthColor);
		
		EQSimsPatchScalarColorer patchColorer = new EQSimsPatchScalarColorer();
		colorers.add(patchColorer);
		eventListeners.add(patchColorer);
		
		StrikeColorer strikeColor = new StrikeColorer();
		colorers.add(strikeColor);
		
		DipColorer dipColor = new DipColorer();
		colorers.add(dipColor);
		
		RakeColorer rakeColor = new RakeColorer();
		colorers.add(rakeColor);
		
		EQSimsSubSectDASColorer dasColorer = new EQSimsSubSectDASColorer();
		colorers.add(dasColorer);
		eventListeners.add(dasColorer);
		
		EQSimsDroughtColorer droughtColorer = new EQSimsDroughtColorer();
		colorers.add(droughtColorer);
		eventListeners.add(droughtColorer);
			
		/*
		 * Initialization of Animation Tabs
		 */
		animations = new ArrayList<>();
		
		EQSimsEventAnimColorer eventAnim = new EQSimsEventAnimColorer();
		animations.add(eventAnim);
		eventListeners.add(eventAnim);
		
		EQSimsAnimDroughtColorer eventDrought  = new EQSimsAnimDroughtColorer();
		animations.add(eventDrought);
		eventListeners.add(eventDrought);
		
		eventSlipAnim = new EQSimsEventSlipAnim();
		animations.add(eventSlipAnim);
		eventListeners.add(eventSlipAnim);
		
	}

	@Override
	public ParameterList getBuilderParams() {
		return builderParams;
	}

	@Override
	public ParameterList getFaultParams() {
		return null;
	}
	
	private static void downloadURL(URL url, File toFile) throws IOException {
		System.out.println("downloading: "+url.toString()+"\nto: "+toFile.getAbsolutePath());
		InputStream is = url.openStream();
		DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
		FileOutputStream fout = new FileOutputStream(toFile);
		int b = dis.read();
		while (b >= 0) {
			fout.write(b);
			b = dis.read();
		}
		fout.flush();
		fout.close();
		System.out.println("done.");
	}
	
	private File getCachePath(String fName) {
		return new File(dataDir.getAbsolutePath() + File.separator + fName);
	}
	
	private boolean isFileCached(String fName) {
		return getCachePath(fName).exists();
	}
	
	private void downloadGeomAsynchronous(final String fName) {
		final CalcProgressBar progress = new CalcProgressBar(null, "Downloading...",
				"Downloading selected geometry file.This requires an active internet connection.", false);
		progress.setModal(true);
		progress.setIndeterminate(true);
		progress.setVisible(true);
		final File file = getCachePath(fName);
		final Runnable fireRunnable = new Runnable() {
			
			@Override
			public void run() {
				progress.setVisible(false);
				fireTreeChangeEvent();
			}
		};
		Runnable downloadRunnable = new Runnable() {
			
			@Override
			public void run() {
				try {
					URL url = new URL("http://opensha.usc.edu/data/simulators/"+fName);
					downloadURL(url, file);
					// fire tree change event in EDT
					SwingUtilities.invokeLater(fireRunnable);
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			}
		};
		new Thread(downloadRunnable).start();
	}
	
//	private File getCacheFile(String fName) throws IOException {
//		// first see if it's cached
//		File file = new File(dataDir.getAbsolutePath() + File.separator + fName);
//		if (file.exists())
//			return file;
//		// if we got this far, we need to download and cache it
//		URL url = new URL("http://opensha.usc.edu/data/simulators/"+fName);
//		downloadURL(url, file);
//		return file;
//	}
	


	@Override
	public void buildTree(DefaultMutableTreeNode root) {
		File geomFile = geomFileParam.getValue();
		
		elements = null;
		if (geomFile == null) {
			return;
		}
		
		//Catches an incompatible file
		try {
			elements = loadGeometry(geomFile);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(Info.getMainGUI(), "Incompatible File. "
					+ "Please Choose Another (.flt or .in recommended)");
			return;
			//throw ExceptionUtils.asRuntimeException(e);
		}
		
		eventFileParam.setValue(null);
		
		// this is a mapping of all fault IDs to category node for the fault
		HashMap<Integer, FaultCategoryNode> faultNodesMap = new HashMap<Integer, FaultCategoryNode>();
		// this is a mapping of all fault nodes to a list of section nodes;
		HashMap<FaultCategoryNode, ArrayList<SimulatorElementFault>> sectionsMap =
			new HashMap<FaultCategoryNode, ArrayList<SimulatorElementFault>>();
		// this list is for sorting;
		ArrayList<FaultCategoryNode> faultNodes = new ArrayList<FaultCategoryNode>();
		
		for (SimulatorElement element : elements) {
			int secID = element.getSectionID();
			String secName = element.getName();
			
			//Switch is based on SAFS, algorithm remains the same regardless if needed to be unimplemented. 
			switch(buttonchosen) {
				case "San Andreas, San Jacinto, Elsinore, Hayward (UCERF3 catalog data)":
					//Elsinore, Hayward, San Andreas, San Jacinto fault IDs
					if(secName.contains("SanAndreas")||secName.contains("SanJacinto")||secName.contains("Hayward")||secName.contains("Elsinore")) {
					//if ((secID >=512 && secID <=537) || (secID >=819 && secID <=843) || (secID >=1773 && secID <=1974) || (secID>=2154 && secID<=2197)) {
						if (!faultNodesMap.containsKey(secID)) {
							FaultCategoryNode tempNode = new FaultCategoryNode(element.getSectionName());
							faultNodesMap.put(secID, tempNode);
							faultNodes.add(tempNode);
						}
						FaultCategoryNode faultNode = faultNodesMap.get(secID);
						
						if (!sectionsMap.containsKey(faultNode)) {
							sectionsMap.put(faultNode, new ArrayList<SimulatorElementFault>());
						}
						ArrayList<SimulatorElementFault> secsForFault = sectionsMap.get(faultNode);
						SimulatorElementFault secNode = new SimulatorElementFault(element);
						secsForFault.add(secNode);
						
					}
					break;
				case "San Andreas, San Jacinto, Elsinore, Hayward (RSQSim catalog data (zfault))":
					if ((secID >=511 && secID <=536) || (secID >=818 && secID <=842) || (secID >=1772 && secID <=1973) || (secID>=2153 && secID<=2196)) {
					if (!faultNodesMap.containsKey(secID)) {
						FaultCategoryNode tempNode = new FaultCategoryNode(element.getSectionName());
						faultNodesMap.put(secID, tempNode);
						faultNodes.add(tempNode);
					}
					FaultCategoryNode faultNode = faultNodesMap.get(secID);
					
					if (!sectionsMap.containsKey(faultNode)) {
						sectionsMap.put(faultNode, new ArrayList<SimulatorElementFault>());
					}
					ArrayList<SimulatorElementFault> secsForFault = sectionsMap.get(faultNode);
					SimulatorElementFault secNode = new SimulatorElementFault(element);
					secsForFault.add(secNode);
					
				}
					break;
			//Original code parsing. All fault IDs will be added. 
				default:
					if (!faultNodesMap.containsKey(secID)) {
						FaultCategoryNode tempNode = new FaultCategoryNode(element.getSectionName());
						faultNodesMap.put(secID, tempNode);
						faultNodes.add(tempNode);
					}
					FaultCategoryNode faultNode = faultNodesMap.get(secID);
					
					if (!sectionsMap.containsKey(faultNode)) {
						sectionsMap.put(faultNode, new ArrayList<SimulatorElementFault>());
					}
					ArrayList<SimulatorElementFault> secsForFault = sectionsMap.get(faultNode);
					SimulatorElementFault secNode = new SimulatorElementFault(element);
					secsForFault.add(secNode);
					break;
			}
		}
		
		// now sort the fault nodes
		Collections.sort(faultNodes, new NamedComparator());
		FaultCategoryNode faultRoot = new FaultCategoryNode(geomFile.getName());
		
		for (FaultCategoryNode faultNode : faultNodes) {
			ArrayList<SimulatorElementFault> sections = sectionsMap.get(faultNode);
			Collections.sort(sections, new AbstractFaultIDComparator());
			for (SimulatorElementFault section : sections) {
				faultNode.add(new FaultSectionNode(section));
			}
			faultRoot.add(faultNode);
		}
		
		root.add(faultRoot);
		fireNewGeometry(elements);

	}

	@Override
	public void setTreeChangeListener(TreeChangeListener l) {
		this.l = l;
	}
	
	private void fireTreeChangeEvent() {
		if (l != null)
			l.treeChanged(null);
	}
	
	public ArrayList<FaultColorer> getColorers() {
		return colorers;
	}
	
	public ArrayList<FaultAnimation> getAnimations() {
		return animations;
	}
	
	private void fireNewGeometry(List<SimulatorElement> elements) {
		for (EQSimsEventListener l : eventListeners)
			l.setGeometry(elements);
	}
	
	private void fireNewEvents(List<? extends SimulatorEvent> events) {
		for (EQSimsEventListener l : eventListeners)
			l.setEvents(events);
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		//Presets
		if (event.getSource() == geomPresetParam) {
			String preset = geomPresetParam.getValue();
			if (!preset.equals(GEOM_PRESET_NONE)) {
				if (isFileCached(preset)) {
					File file = getCachePath(preset);
					geomFileParam.setValue(file);
					geomFileParam.getEditor().refreshParamEditor();
					fireTreeChangeEvent();
				} else {
					// load asynchronous
					geomFileParam.setValue(null);
					geomFileParam.getEditor().refreshParamEditor();
					downloadGeomAsynchronous(preset); // will fire new tree change event when done
					return;
				}
			}
		}
		//Geometry file upload
		else if (event.getSource() == geomFileParam) {
			ProgressBar progress = new ProgressBar("Loading Geometry File", "Please Wait");
			progress.runProgressBar();
			
			File file = geomFileParam.getValue();
			if (file != null && eventFileParam.getValue() == null)
				eventFileParam.setDefaultInitialDir(file.getParentFile());
		
			fireNewGeometry(null);
			
			final Runnable fireRunnable = new Runnable() {
				
				@Override
				public void run() {
					fireTreeChangeEvent();
					progress.stopProgressBar();
				}
			};
			
			Runnable fileRunnable = new Runnable() {
				
				@Override
				public void run() {
					SwingUtilities.invokeLater(fireRunnable);
				}
			};
			new Thread(fileRunnable).start();
			
			
		} 
		//Simulator Event upload
		else if (event.getSource() == eventFileParam) {
			File outFile = eventFileParam.getValue();
			//for reloading purposes
			if (reloadFile == null) {
			setEventFile(eventFileParam.getValue());
			}		
			if (outFile == null || elements == null) {
				fireNewEvents(null);
			} else if (elements != null) {
				loadEvents(outFile);
			}

		} 
	
		
		//Filtering Options Upload
		else if (event.getSource() == filterOptions) {
			//no chosen geometry file
			if (geomFileParam.getValue() == null) {
				JOptionPane.showMessageDialog(Info.getMainGUI(), "No Geometry File Detected.\n Try again after uploading it.  ");
				return;
			}
			else  {
				if (filterOptions.getValue() == GEOM_PRESET_NONE) {
					buttonchosen = "";
				}
				else if (filterOptions.getValue() == "San Andreas, San Jacinto, Elsinore, Hayward (UCERF3 catalog data)") {
					buttonchosen= "San Andreas, San Jacinto, Elsinore, Hayward (UCERF3 catalog data)";
				}
				else if (filterOptions.getValue() == "San Andreas, San Jacinto, Elsinore, Hayward (RSQSim catalog data (zfault))") {
					buttonchosen = "San Andreas, San Jacinto, Elsinore, Hayward (RSQSim catalog data (zfault))";
				}
			}
			ProgressBar progress = new ProgressBar("Loading Filter" ,"Loading " + buttonchosen);
			progress.runProgressBar();
			
			final Runnable fireRunnable = new Runnable() {
				
				@Override
				public void run() {
					progress.stopProgressBar();
					//reloads the simulator event file if it exists 
					if (reloadFile != null) {
						loadEvents(reloadFile);
					}
				}
			};
			Runnable filteringRunnable = new Runnable() {
				
				@Override
				public void run() {
					fireTreeChangeEvent();
					SwingUtilities.invokeLater(fireRunnable);
					
				}
			};
			new Thread(filteringRunnable).start();
		
		}
	}
	
	private void setEventFile(File outFile) {
		reloadFile = outFile;
	}

	private void loadEvents(File outFile) {
		ProgressBar progress = new ProgressBar("Reading Events File", "Loading events...");
		try {
			eventSlipAnim.setInitialDir(outFile.getParentFile());
			List<RuptureIdentifier> rupIdens = new ArrayList<>();
			RuptureIdentifier loadIden = new MagRangeRuptureIdentifier(eventMinMagParam.getValue(), Double.POSITIVE_INFINITY);
			if (catDurationParam.getValue() > 0d)
				loadIden = new LogicalAndRupIden(loadIden, new CatalogLengthLoadIden(catDurationParam.getValue()));
			rupIdens.add(loadIden);
			progress.runProgressBar();
			
			final Runnable eventRunnable = new Runnable() {
				
				@Override
				public void run() {
					fireNewEvents(events);
					progress.stopProgressBar();
				}
			};
			Runnable simulatorRunnable = new Runnable() {
				
				@Override
				public void run() {
					
					try {
					if (outFile.isDirectory() || outFile.getName().endsWith("List")) {
						System.out.println("Detected RSQSim output file/dir");
						progress.changeLoadingMessage("Detected RSQSim output file/dir");
						events = RSQSimFileReader.readEventsFile(outFile, elements, rupIdens);
					} else {
						events = EQSIMv06FileReader.readEventsFile(outFile, elements, rupIdens);
					}
					}
					catch(Exception e) {
						JOptionPane.showMessageDialog(Info.getMainGUI(), "Incompatible File. "
								+ "Please Choose Another (Files Ending With \"List\" Preferred)");
						progress.stopProgressBar();
						eventFileParam.setValue(null);
						return;
						
					}
					
					System.out.println("Done reading events file!");
					progress.changeLoadingMessage("Done reading events file!");
					SwingUtilities.invokeLater(eventRunnable);		
				}
			};
			new Thread(simulatorRunnable).start();

			System.out.println("Done loadEvents()");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<SimulatorElement> loadGeometry(File file) throws IOException {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".flt") || name.endsWith(".in")) {
			int zone = utmZoneParam.getValue();
			char band = utmBandParam.getValue().charAt(0);
			return RSQSimFileReader.readGeometryFile(file, zone, band);
		}
		return EQSIMv06FileReader.readGeometryFile(file);
	}
	
}