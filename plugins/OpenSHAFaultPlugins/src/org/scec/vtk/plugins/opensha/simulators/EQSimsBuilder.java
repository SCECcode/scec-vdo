package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.data.NamedComparator;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.FileParameter;
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
import org.scec.vtk.commons.opensha.surfaces.params.ColorParameter;
import org.scec.vtk.commons.opensha.tree.FaultCategoryNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.tools.Prefs;

public class EQSimsBuilder implements FaultTreeBuilder, ParameterChangeListener {
	
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
	}
	
	private static final String GEOM_PRESET_PARAM_NAME = "Geometry File Presets";
	private static final String GEOM_PRESET_NONE = "(none selected)";
	private StringParameter geomPresetParam;
	
	private static final String GEOM_FILE_SELECTOR_PARAM_NAME = "Geometry File";
	private FileParameter geomFileParam = new FileParameter(GEOM_FILE_SELECTOR_PARAM_NAME);
	
	// no longer working
//	private static final String LOAD_PARAM_NAME = "Load Catalogs";
//	private static final String LOAD_PARAM_BUTTON_NAME = "Browse";
//	private ButtonParameter loadParam;
	
	private static final String EVENT_SELECTOR_PARAM_NAME = "Simulator Event File";
	private FileParameter eventFileParam = new FileParameter(EVENT_SELECTOR_PARAM_NAME);
	
	private static final String EVENT_MIN_MAG_PARAM_NAME = "Min Event Mag To Load";
	private DoubleParameter eventMinMagParam;
	
	private static final String MAX_CAT_DURATION_PARAM_NAME = "Max Catalog Years To Load";
	private DoubleParameter catDurationParam;
	
	private ParameterList builderParams = new ParameterList();
	
	private TreeChangeListener l;
	
	private EQSimsEventSlipAnim eventSlipAnim;
	private ArrayList<FaultColorer> colorers;
	private ArrayList<FaultAnimation> animations;
	
	private ArrayList<EQSimsEventListener> eventListeners = new ArrayList<EQSimsEventListener>();
	
	private File dataDir;
	
	private List<SimulatorElement> elements;
	
	public EQSimsBuilder() {
		dataDir = new File(Prefs.getDefaultLocation() + File.separator + "data" + File.separator + "EQSims");
		if (!dataDir.exists())
			dataDir.mkdirs();
		
		ArrayList<String> strings = new ArrayList<String>();
		strings.add(GEOM_PRESET_NONE);
		for (String name : hardcodedInputs) {
			strings.add(name);
		}
		
		//Min Event Mag input area
		eventMinMagParam = new DoubleParameter(EVENT_MIN_MAG_PARAM_NAME, -10d, 10d, new Double(7d));
		builderParams.addParameter(eventMinMagParam);
		//Max Catalog Yrs Loaded input area
		catDurationParam = new DoubleParameter(MAX_CAT_DURATION_PARAM_NAME, 0d, Double.POSITIVE_INFINITY, new Double(0d));
		builderParams.addParameter(catDurationParam);
		
		geomPresetParam = new StringParameter(GEOM_PRESET_PARAM_NAME, strings, GEOM_PRESET_NONE);
		geomPresetParam.addParameterChangeListener(this);
		builderParams.addParameter(geomPresetParam);
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
		builderParams.addParameter(geomFileParam);
		eventFileParam.addParameterChangeListener(this);
		eventFileParam.setShowHiddenFiles(true);
		builderParams.addParameter(eventFileParam);
		//Added button to load catalogs from RSQSimServer.
//		loadParam = new ButtonParameter(LOAD_PARAM_NAME, LOAD_PARAM_BUTTON_NAME);
//		loadParam.addParameterChangeListener(this);
//		builderParams.addParameter(loadParam);

		
		colorers = new ArrayList<FaultColorer>();
		EQSlipRateColorer slipColorer = new EQSlipRateColorer();
		eventListeners.add(slipColorer);
		colorers.add(slipColorer);
		EQSimsParticipationColorer particColor = new EQSimsParticipationColorer();
		eventListeners.add(particColor);
		colorers.add(particColor);
		colorers.add(new EQSimsDepthColorer());
		EQSimsPatchScalarColorer patchColorer = new EQSimsPatchScalarColorer();
		colorers.add(patchColorer);
		eventListeners.add(patchColorer);
//		EQSimTimeDepParticColorer timeDepParticColor = new EQSimTimeDepParticColorer();
//		eventListeners.add(timeDepParticColor);
//		colorers.add(timeDepParticColor);
//		EQSimMultiFaultRupColorer multiFault = new EQSimMultiFaultRupColorer();
//		eventListeners.add(multiFault);
//		colorers.add(multiFault);
		colorers.add(new StrikeColorer());
		colorers.add(new DipColorer());
		colorers.add(new RakeColorer());
		EQSimsSubSectDASColorer dasColorer = new EQSimsSubSectDASColorer();
		colorers.add(dasColorer);
		eventListeners.add(dasColorer);
		EQSimsDroughtColorer droughtColorer = new EQSimsDroughtColorer();
		colorers.add(droughtColorer);
		eventListeners.add(droughtColorer);
		
		animations = new ArrayList<>();
		EQSimsEventAnimColorer eventAnim = new EQSimsEventAnimColorer();
		//Me
		EQSimsAnimDroughtColorerTemp eventDrought  = new EQSimsAnimDroughtColorerTemp();
		animations.add(eventDrought);
		eventListeners.add(eventDrought);
		
		animations.add(eventAnim);
		eventListeners.add(eventAnim);
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
		// TODO Auto-generated method stub
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
//		progress.setModal(true);
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
		if (geomFile == null)
			return;
		
//		if (input.equals(GEOM_PRESET_NONE)) {
//			file = getCustomFile();
//			if (file == null)
//				return;
//			// set directory for loading events file
//			if (eventFileParam.getValue() == null)
//				eventFileParam.setDefaultInitialDir(file.getParentFile());
//		} else {
////			try {
//				if (isFileCached(input)) {
//					file = getCachePath(input);
//				} else {
//					// load asynchronous
//					fireNewGeometry(null);
//					downloadGeomAsynchronous(input); // will fire new tree change event when done
//					return;
//				}
////			} catch (IOException e) {
////				throw ExceptionUtils.asRuntimeException(e);
////			}
//		}
		try {
			elements = loadGeometry(geomFile);
		} catch (Exception e) {
			throw ExceptionUtils.asRuntimeException(e);
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
		} else if (event.getSource() == geomFileParam) {
			//Loading Screen
			CalcProgressBar progress = new CalcProgressBar("Loading Events", "Please Wait");
			//progress.runProgressBar();
			progress.getMousePosition();
			progress.setVisible(true);
			progress.setIndeterminate(true);
			
			File file = geomFileParam.getValue();
			if (file != null && eventFileParam.getValue() == null)
				eventFileParam.setDefaultInitialDir(file.getParentFile());
			fireNewGeometry(null);
			fireTreeChangeEvent();
			
			//End
			progress.toFront();
			progress.setVisible(false);	
			progress.dispose();
		} else if (event.getSource() == eventFileParam) {
			File outFile = eventFileParam.getValue();
			if (outFile == null || elements == null) {
				fireNewEvents(null);
			} else if (elements != null) {
				loadEvents(outFile);
			}
//		} else if (event.getSource() == loadParam) {
//			final EQSimsCatalogQuery EQSimQueryFrame = new EQSimsCatalogQuery();
//			EQSimQueryFrame.downloadButton.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					//Create a new folder in the .scecvdo folder and download to that folder
//					HashMap<String, ArrayList<URL>> downloadURLs = EQSimQueryFrame.getDownloadURLs();
//					for (Entry<String, ArrayList<URL>> entry : downloadURLs.entrySet()) {
//						String title = entry.getKey();
//						ArrayList<URL> urls = entry.getValue();
//						File catalogDir = new File(dataDir + File.separator + title);
//						if (!catalogDir.exists())
//							catalogDir.mkdirs();
//						for (int i = 0; i < urls.size(); i++) {
//							String[] fileName = urls.get(i).getFile().split("/");
//							File file = new File(catalogDir.getAbsolutePath() + File.separator + fileName[fileName.length - 1]);
//							if (!file.exists()) {
//								try {
//									downloadURL(urls.get(i), file);
//								} catch (IOException e1) {
//									e1.printStackTrace();
//								}
//							}
//						}
//						File outFile = catalogDir; //outFile contains all downloaded files.
//						//Work with the outFile. Copied from above.
//						if (outFile == null || elements == null) {
//							fireNewEvents(null);
//						} else if (elements != null) {
//							loadEvents(outFile);
//						}
//					}
//				}
//			});
		}
	}

	private void loadEvents(File outFile) {
		try {
			eventSlipAnim.setInitialDir(outFile.getParentFile());
			List<RuptureIdentifier> rupIdens = new ArrayList<>();
			RuptureIdentifier loadIden = new MagRangeRuptureIdentifier(eventMinMagParam.getValue(), Double.POSITIVE_INFINITY);
			if (catDurationParam.getValue() > 0d)
				loadIden = new LogicalAndRupIden(loadIden, new CatalogLengthLoadIden(catDurationParam.getValue()));
			rupIdens.add(loadIden);
			List<? extends SimulatorEvent> events;
			CalcProgressBar progress = new CalcProgressBar("Reading Events File", "Loading events...");
			progress.setIndeterminate(true);
			if (outFile.isDirectory() || outFile.getName().endsWith("List")) {
				System.out.println("Detected RSQSim output file/dir");
				events = RSQSimFileReader.readEventsFile(outFile, elements, rupIdens);
			} else {
				events = EQSIMv06FileReader.readEventsFile(outFile, elements, rupIdens);
			}
			System.out.println("Done reading events file!");
			fireNewEvents(events);
			progress.setVisible(false);
			progress.dispose();
			System.out.println("Done loadEvents()");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<SimulatorElement> loadGeometry(File file) throws IOException {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".flt") || name.endsWith(".in"))
			return RSQSimFileReader.readGeometryFile(file, 11, 'S'); // TODO make selectable
		return EQSIMv06FileReader.readGeometryFile(file);
	}
	
}