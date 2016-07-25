package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opensha.commons.data.NameIDPairing;
import org.opensha.commons.data.NamedComparator;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.RectangularElement;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.iden.MagRangeRuptureIdentifier;
import org.opensha.sha.simulators.iden.RuptureIdentifier;
import org.opensha.sha.simulators.parsers.EQSIMv06FileReader;
import org.opensha.sha.simulators.parsers.RSQSimFileReader;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;
import org.scec.vtk.commons.opensha.faults.AbstractFaultIDComparator;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.colorers.SlipRateColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.SimulatorElementFault;
import org.scec.vtk.commons.opensha.tree.FaultCategoryNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;
import org.scec.vtk.tools.Prefs;

import com.google.common.base.Preconditions;

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
	}
	
	private static final String INPUT_SELECTOR_PARAM_NAME = "Input";
	private static final String INPUT_SELECTOR_FROM_FILE = "(Select File)";
	private StringParameter inputParam;
	
	private static final String EVENT_SELECTOR_PARAM_NAME = "Simulator Event File";
	private FileParameter eventFileParam = new FileParameter(EVENT_SELECTOR_PARAM_NAME);
	
	private static final String EVENT_MIN_MAG_PARAM_NAME = "Min Event Mag To Load";
	private DoubleParameter eventMinMagParam;
	
	private ParameterList builderParams = new ParameterList();
	
	private JFileChooser chooser;
	
	private TreeChangeListener l;
	
	private EQSimsEventAnimColorer faultAnim;
	private ArrayList<FaultColorer> colorers;
	
	private ArrayList<EQSimsEventListener> eventListeners = new ArrayList<EQSimsEventListener>();
	
	private File dataDir;
	
	private List<SimulatorElement> elements;
	
	public EQSimsBuilder() {
		dataDir = new File(Prefs.getDefaultLocation() + File.separator + "data" + File.separator + "EQSims");
		if (!dataDir.exists())
			dataDir.mkdirs();
		
		ArrayList<String> strings = new ArrayList<String>();
		for (String name : hardcodedInputs) {
			strings.add(name);
		}
		strings.add(INPUT_SELECTOR_FROM_FILE);
		
		inputParam = new StringParameter(INPUT_SELECTOR_PARAM_NAME, strings, strings.get(0));
		inputParam.addParameterChangeListener(this);
		builderParams.addParameter(inputParam);
		eventFileParam.addParameterChangeListener(this);
		eventFileParam.setShowHiddenFiles(true);
		builderParams.addParameter(eventFileParam);
		eventMinMagParam = new DoubleParameter(EVENT_MIN_MAG_PARAM_NAME, -10d, 10d, new Double(5d));
		builderParams.addParameter(eventMinMagParam);
		
		colorers = new ArrayList<FaultColorer>();
		EQSlipRateColorer slipColorer = new EQSlipRateColorer();
		eventListeners.add(slipColorer);
		colorers.add(slipColorer);
		EQSimParticipationColorer particColor = new EQSimParticipationColorer();
		eventListeners.add(particColor);
		colorers.add(particColor);
		colorers.add(new EQSimsDepthColorer());
//		EQSimTimeDepParticColorer timeDepParticColor = new EQSimTimeDepParticColorer();
//		eventListeners.add(timeDepParticColor);
//		colorers.add(timeDepParticColor);
//		EQSimMultiFaultRupColorer multiFault = new EQSimMultiFaultRupColorer();
//		eventListeners.add(multiFault);
//		colorers.add(multiFault);
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
	
	private File getCustomFile() {
		if (chooser == null) {
			chooser = new JFileChooser();
			chooser.setFileHidingEnabled(false);
		}
		int retVal = chooser.showOpenDialog(null);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			return file;
		}
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
	
	private File getCacheFile(String fName) throws IOException {
		// first see if it's cached
		File file = new File(dataDir.getAbsolutePath() + File.separator + fName);
		if (file.exists())
			return file;
		// if we got this far, we need to download and cache it
		URL url = new URL("http://opensha.usc.edu/data/simulators/"+fName);
		downloadURL(url, file);
		return file;
	}

	@Override
	public void buildTree(DefaultMutableTreeNode root) {
		String input = inputParam.getValue();
		
		elements = null;
		File file;
		
		if (input.equals(INPUT_SELECTOR_FROM_FILE)) {
			file = getCustomFile();
			if (file == null)
				return;
			// set directory for loading events file
			if (eventFileParam.getValue() == null)
				eventFileParam.setDefaultInitialDir(file.getParentFile());
		} else {
			try {
				file = getCacheFile(input);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			elements = loadGeometry(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
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
		
		FaultCategoryNode faultRoot = new FaultCategoryNode(file.getName());
		
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
	
	public EQSimsEventAnimColorer getFaultAnimation() {
		if (faultAnim == null) {
			faultAnim = new EQSimsEventAnimColorer();
			eventListeners.add(faultAnim);
		}
		return faultAnim;
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
		if (event.getSource() == inputParam) {
			fireTreeChangeEvent();
		} else if (event.getSource() == eventFileParam) {
			// TODO
			File outFile = eventFileParam.getValue();
			if (outFile == null || elements == null) {
				fireNewEvents(null);
			} else if (elements != null) {
				try {
					List<RuptureIdentifier> rupIdens = new ArrayList<>();
					rupIdens.add(new MagRangeRuptureIdentifier(eventMinMagParam.getValue(), 10d));
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
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	private List<SimulatorElement> loadGeometry(File file) throws IOException {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".flt") || name.endsWith(".in"))
			return RSQSimFileReader.readGeometryFile(file, 11, 'S'); // TODO make selectable
		return EQSIMv06FileReader.readGeometryFile(file);
	}
	
}