package org.scec.vtk.plugins.opensha.ucerf3Rups.colorers;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemRupSet;
import org.opensha.sha.earthquake.faultSysSolution.FaultSystemSolution;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.surfaces.LineSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.params.ColorParameter;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import com.google.common.collect.Lists;

import vtk.vtkCellPicker;

public class MultiFaultRupColorer extends CPTBasedColorer implements PickHandler<AbstractFaultSection>,
UCERF3RupSetChangeListener, ParameterChangeListener {
	
	private static CPT getDefaultCPT() {
		CPT cpt;
		try {
			cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		cpt = cpt.rescale(-10, -2);
		cpt.setNanColor(Color.GRAY);
		return cpt;
	}
	
	private static CPT getRICPT() {
		CPT cpt;
		try {
			cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
		} catch (IOException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		cpt = cpt.reverse();
		cpt = cpt.rescale(2, 5);
		cpt.setNanColor(Color.GRAY);
		return cpt;
	}
	
	private HashSet<Integer> includeIDs = new HashSet<Integer>();
	private HashSet<Integer> excludeIDs = new HashSet<Integer>();
	
	private FaultSystemSolution sol;
	
	private enum CompareType {
		RATE("Rupture Rate"),
		NUM_SECTIONS("Num Sections"),
		RUP_ID("Rupture ID");
		
		private String name;
		private CompareType(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	private static final String DISPLAY_PARAM_NAME = "Display Rups";
	private BooleanParameter displayParam;
	
	private static final String MAX_RUPS_PARAM_NAME = "Max # Rups to Display";
	private static final Integer MAX_RUPS_DEFAULT = 1000;
	private static final Integer MAX_RUPS_MIN = 1;
	private static final Integer MAX_RUPS_MAX = Integer.MAX_VALUE;
	private IntegerParameter maxRupsParam;
	
	private static final String OR_PARAM_NAME = "Selection OR";
	private BooleanParameter orParam;
	
	private static final String EXCLUDE_PARAM_NAME = "Selection Exclude";
	private BooleanParameter excludeParam;
	
	private static final String PARENT_SECT_PARAM_NAME = "Select Parent Sects";
	private BooleanParameter parentSectParam;
	
	private static final String COMP_PARAM_NAME = "Sort By";
	private EnumParameter<CompareType> compParam;
	
	private static final String REVERSED_PARAM_NAME = "Reversed";
	private BooleanParameter reversedParam;
	
	private ParameterList params = new ParameterList();
	
	private LineSurfaceGenerator lineGen;
	
	private HashMap<Integer, Double> asDiscrRates = new HashMap<Integer, Double>();
	private HashMap<Integer, Double> rates = new HashMap<Integer, Double>();
	
	private static final String HIGHLIGHT_COLOR_PARAM_NAME = "Highlight Color";
	private static final Color HIGHLI_COLOR_DEFAULT = Color.WHITE;
	private ColorParameter highlightColorParam;
	
	private static final String HIDE_NAN_PARAM_NAME = "Hide NaN's?";
	private BooleanParameter hideNansParam;
	
	private DoubleParameter magMinParam;
	private double min = 0d;
	private double max = 10d;
	private DoubleParameter magMaxParam;
	
	private DoubleParameter minRateParam;
	private double min_rate_min = 0d;
	private double min_rate_max = 1d;
	
	private BooleanParameter riParam;
	private BooleanParameter asDiscrParam;
	
	private JFileChooser exportChoose;
	private ButtonParameter exportParam;

	public MultiFaultRupColorer() {
		super(getDefaultCPT(), true);
		
		displayParam = new BooleanParameter(DISPLAY_PARAM_NAME, false);
		displayParam.addParameterChangeListener(this);
		params.addParameter(displayParam);
		
		maxRupsParam = new IntegerParameter(MAX_RUPS_PARAM_NAME, MAX_RUPS_MIN, MAX_RUPS_MAX, MAX_RUPS_DEFAULT);
		maxRupsParam.addParameterChangeListener(this);
		params.addParameter(maxRupsParam);
		
		orParam = new BooleanParameter(OR_PARAM_NAME, false);
		orParam.addParameterChangeListener(this);
		params.addParameter(orParam);
		
		parentSectParam = new BooleanParameter(PARENT_SECT_PARAM_NAME, false);
		parentSectParam.addParameterChangeListener(this);
		params.addParameter(parentSectParam);
		
		compParam = new EnumParameter<CompareType>(COMP_PARAM_NAME,
				EnumSet.allOf(CompareType.class), CompareType.RATE, null);
		compParam.addParameterChangeListener(this);
		params.addParameter(compParam);
		
		reversedParam = new BooleanParameter(REVERSED_PARAM_NAME, false);
		reversedParam.addParameterChangeListener(this);
		params.addParameter(reversedParam);
		
		highlightColorParam = new ColorParameter(HIGHLIGHT_COLOR_PARAM_NAME, HIGHLI_COLOR_DEFAULT);
		highlightColorParam.addParameterChangeListener(this);
		params.addParameter(highlightColorParam);
		
		excludeParam = new BooleanParameter(EXCLUDE_PARAM_NAME, false);
		excludeParam.addParameterChangeListener(this);
		params.addParameter(excludeParam);
		
		hideNansParam = new BooleanParameter(HIDE_NAN_PARAM_NAME, false);
		hideNansParam.addParameterChangeListener(this);
		params.addParameter(hideNansParam);
		
		magMinParam = new DoubleParameter("Min Mag", 0d, 10d);
		magMinParam.setValue(min);
		magMinParam.addParameterChangeListener(this);
		params.addParameter(magMinParam);
		magMaxParam = new DoubleParameter("Max Mag", 0d, 10d);
		magMaxParam.setValue(max);
		magMaxParam.addParameterChangeListener(this);
		params.addParameter(magMaxParam);
		
		minRateParam = new DoubleParameter("Min Rup Rate", min_rate_min, min_rate_max);
		minRateParam.setValue(min_rate_min);
		minRateParam.setInfo("Min rate to display for stacked ruptures");
		minRateParam.addParameterChangeListener(this);
		params.addParameter(minRateParam);
		
		riParam = new BooleanParameter("Recurrence Intervals", false);
		riParam.addParameterChangeListener(this);
		params.addParameter(riParam);
		
		asDiscrParam = new BooleanParameter("As Discretized", false);
		asDiscrParam.addParameterChangeListener(this);
		params.addParameter(asDiscrParam);
		
		exportParam = new ButtonParameter("CSV File", "Export");
		exportParam.addParameterChangeListener(this);
		params.addParameter(exportParam);
		
		lineGen = new LineSurfaceGenerator();
		lineGen.setSize(2);
	}

	@Override
	public String getName() {
		return "Multi Fault Rup Rates";
	}

	@Override
	public Color getColor(AbstractFaultSection fault) {
		int id = fault.getId();
		if (!parentSectParam.getValue() && includeIDs.contains(id))
			return highlightColorParam.getValue();
		if (excludeIDs.contains(id)) {
			Color origColor = highlightColorParam.getValue();
			return new Color(255 - origColor.getRed(), 255 - origColor.getGreen(), 255 - origColor.getBlue());
		}
		double value = getValue(fault);
		if (hideNansParam.getValue() && Double.isNaN(value) && rates != null && !rates.isEmpty())
			return null;
		return super.getColorForValue(value);
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		Double rate = rates.get(fault.getId());
		if (rate == null)
			return Double.NaN;
		if (riParam.getValue())
			return 1d / rate;
		if (asDiscrParam.getValue())
			return asDiscrRates.get(fault.getId());
		return rate;
	}
	
	private void update() {
//		bg.removeAllChildren(); // TODO
		rates.clear();
		asDiscrRates.clear();
		double totRate = 0;
		int totNumRups = 0;
		updateParentBGDisplay();
		
		if (sol == null)
			return;
		
		List<Rupture> rups = new ArrayList<Rupture>();
		HashSet<Integer> rupIDs = new HashSet<Integer>();
		
		double minMag = magMinParam.getValue();
		double maxMag = magMaxParam.getValue();
		for (int sect : includeIDs) {
			List<Integer> rupsForSect;
			if (parentSectParam.getValue())
				rupsForSect = sol.getRupSet().getRupturesForParentSection(sect);
			else
				rupsForSect = sol.getRupSet().getRupturesForSection(sect);
			for (int rupID : rupsForSect) {
				if (rupIDs.contains(rupID))
					continue;
				double mag = sol.getRupSet().getMagForRup(rupID);
				if (mag < minMag || mag > maxMag)
					continue;
				List<Integer> sects = sol.getRupSet().getSectionsIndicesForRup(rupID);
				boolean match = true;
				if (!orParam.getValue()) {
					// logical AND
					if (parentSectParam.getValue()) {
						HashSet<Integer> rupParents = new HashSet<Integer>();
						for (int sect2 : sects) {
							int parent = sol.getRupSet().getFaultSectionData(sect2).getParentSectionId();
							if (!rupParents.contains(parent))
								rupParents.add(parent);
						}
						for (int parent : includeIDs) {
							if (!rupParents.contains(parent)) {
								match = false;
								break;
							}
						}
					} else {
						for (int sect2 : includeIDs) {
							if (sect == sect2)
								continue;
							if (!sects.contains(sect2)) {
								match = false;
								break;
							}
						}
					}
				}
				for (int sect2 : excludeIDs) {
					if (sects.contains(sect2)) {
						match = false;
						break;
					}
				}
				if (match) {
					Rupture rup = new Rupture(rupID, sects, sol.getRateForRup(rupID));
					rupIDs.add(rupID);
					rups.add(rup);
					totRate += rup.rate;
					totNumRups++;
					for (int sectID : sects) {
						Double sectRate = rates.get(sectID);
						Double sectAsDiscrRate = asDiscrRates.get(sectID);
						if (sectRate == null) {
							sectRate = 0d;
							sectAsDiscrRate = 0d;
						}
						sectRate += rup.rate;
						sectAsDiscrRate += 1d;
						rates.put(sectID, sectRate);
						asDiscrRates.put(sectID, sectAsDiscrRate);
					}
				}
			}
		}
		
		double asDiscrScale = totRate / (double)totNumRups;
		for (int sectID : asDiscrRates.keySet())
			asDiscrRates.put(sectID, asDiscrScale * asDiscrRates.get(sectID));
		
		Collections.sort(rups);
		
		if (reversedParam.getValue())
			Collections.reverse(rups);
		
		fireColorerChangeEvent();
		
		int maxRups = maxRupsParam.getValue();
		
		if (displayParam.getValue()) {
			double minRate = minRateParam.getValue();
			if (minRate > 0) {
				List<Rupture> newRups = Lists.newArrayList();
				for (Rupture rup : rups)
					if (rup.rate >= minRate)
						newRups.add(rup);
				rups = newRups;
			}
			
			if (rups.size() > maxRups) {
				JOptionPane.showMessageDialog(null, "Only the first "+maxRups+" ruptures are " +
						"being displayed", "WARNING: Too many ruptures", JOptionPane.WARNING_MESSAGE);
				rups = rups.subList(0, maxRups);
			}
			
			double alt = 3;
			if (parentSectParam.getValue())
				alt += 3;
			for (Rupture rup : rups)
				display(rup, alt++);
			
			displayParam.getEditor().refreshParamEditor();
			if (!displayParam.getValue()) {
				displayParam.removeParameterChangeListener(this);
				displayParam.setValue(true);
				displayParam.getEditor().refreshParamEditor();
				displayParam.addParameterChangeListener(this);
			}
		}
	}
	
	private class Rupture implements Comparable<Rupture> {
		private int id;
		private List<Integer> indices;
		private double rate;

		public Rupture(int id, List<Integer> indices, double rate) {
			this.id = id;
			this.indices = indices;
			this.rate = rate;
		}

		@Override
		public int compareTo(Rupture o) {
			switch (compParam.getValue()) {
			case RATE:
				return -Double.compare(rate, o.rate);
			case NUM_SECTIONS:
				return Double.compare((double)indices.size(), (double)o.indices.size());
			case RUP_ID:
				return Double.compare((double)id, (double)o.id);
			default:
				throw new IllegalStateException("unknown comp type: "+compParam.getValue());
			}
		}
	}
	
	private void display(Rupture rup, double alt) {
		double rate = rup.rate;
		if (riParam.getValue())
			rate = 1d/rate;
		Color color = getCPT().getColor((float)Math.log10(rate));
		
		List<Integer> indices = Lists.newArrayList(rup.indices);
		Collections.sort(indices);
		
		// TODO
//		BranchGroup subBG = buildBG(indices, alt, 2f, color);
//		
//		this.bg.addChild(subBG);
	}
		
//	private BranchGroup buildBG(List<Integer> sects, double alt, float thickness, Color color) {
//		BranchGroup subBG = buildBG();
//		Map<Integer, LocationList> parentSectTraces = Maps.newHashMap();
//		
//		for (int sectID : sects) {
//			FaultSectionPrefData sect = sol.getRupSet().getFaultSectionData(sectID);
//			FaultTrace trace = sect.getFaultTrace();
//			Integer parentID = sect.getParentSectionId();
//			LocationList parentTrace = parentSectTraces.get(parentID);
//			if (parentTrace == null) {
//				parentTrace = new LocationList();
//				parentSectTraces.put(parentID, parentTrace);
//			}
//			
//			int start = 0;
//			if (!parentTrace.isEmpty() && parentTrace.get(parentTrace.size()-1).equals(trace.get(0)))
//				// don't duplicate points
//				start = 1;
//			
//			for (int i=start; i<trace.size(); i++)
//				parentTrace.add(trace.get(i));
//		}
//		
//		for (LocationList trace : parentSectTraces.values()) {
//			BranchGroup bg = buildBG(trace, alt, thickness, color);
//			
//			subBG.addChild(bg);
//		}
//		
//		return subBG;
//	}
	
	private void updateParentBGDisplay() {
		// TODO
//		if (parentHighlightBG != null && parentHighlightBG.isLive()) {
//			parentHighlightBG.detach();
//		}
//		
//		if (parentSectParam.getValue() && sol != null && !includeIDs.isEmpty()) {
//			ArrayList<Integer> sects = new ArrayList<Integer>();
//			HashSet<Integer> parents = new HashSet<Integer>(includeIDs);
//			for (int sectIndex=0; sectIndex<sol.getRupSet().getNumSections(); sectIndex++) {
//				if (parents.contains(sol.getRupSet().getFaultSectionData(sectIndex).getParentSectionId()))
//					sects.add(sectIndex);
//			}
//			parentHighlightBG = buildBG(sects, 1, 4f, highlightColorParam.getValue());
//					
//			this.bg.addChild(parentHighlightBG);
//		}
	}
	
	// TODO
//	private BranchGroup buildBG(LocationList locs, double alt, float width, Color color) {
//		ArrayList<Point3f> points = new ArrayList<Point3f>();
//		
//		for (int i=0; i<locs.size(); i++) {
//			Location loc = locs.get(i);
//			points.add(LatLongToPoint.plotPoint3f(loc.getLatitude(), loc.getLongitude(), alt));
//		}
//		
//		
//		LineArray la = new LineArray((points.size()-1)*2, LineArray.COORDINATES);
//		int cnt = 0;
//		for (int i=1; i<points.size(); i++) {
//			la.setCoordinate(cnt++, points.get(i-1));
//			la.setCoordinate(cnt++, points.get(i));
//		}
//		
//		BranchGroup bg = buildBG();
//		
////		ColoringAttributes ca = new ColoringAttributes(new Color3f(color), ColoringAttributes.SHADE_FLAT);
//		ColoringAttributes ca = new ColoringAttributes(new Color3f(color), ColoringAttributes.FASTEST);
//		ca.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
//		ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
//		
//		Appearance lapp = new Appearance();
//		lapp.setColoringAttributes(ca);
//		LineAttributes latt = new LineAttributes(width, LineAttributes.PATTERN_SOLID, false);
//		lapp.setLineAttributes(latt);
//		
//		FaultSectionShape3D shape = new FaultSectionShape3D(la,lapp, null);
//		bg.addChild(shape);
//		
//		return bg;
//	}

	@Override
	public void actorPicked(PickEnabledActor<AbstractFaultSection> actor,
			AbstractFaultSection reference, vtkCellPicker picker, MouseEvent e) {
		
		if (reference instanceof PrefDataSection) {
			
			PrefDataSection fault = (PrefDataSection)reference;
			if (e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown()) {
				System.out.println("shift down");
				int id;
				
				if (parentSectParam.getValue())
					id = fault.getFaultSection().getParentSectionId();
				else
					id = fault.getId();
				
				if (excludeParam.getValue()) {
					id = fault.getId();
					// only sub section ids
					if (excludeIDs.contains(id))
						excludeIDs.remove(id);
					else
						excludeIDs.add(id);
				} else {
					if (includeIDs.contains(id)) {
						includeIDs.remove(id);
					} else {
						if (e.isControlDown()) {
							// adding
							if (!includeIDs.contains(id))
								includeIDs.add(id);
						} else {
							includeIDs.clear();
							excludeIDs.clear();
							includeIDs.add(id);
						}
					}
				}
				update();
			}
		}
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.sol = null;
		update();
		if (sol == null && rupSet != null) {
			// just build a fake solution
			double[] fakeRates = new double[rupSet.getNumRuptures()];
			double rate = 1d/fakeRates.length;
			for (int r=0; r<fakeRates.length; r++)
				fakeRates[r] = rate;
			sol = new FaultSystemSolution(rupSet, fakeRates);
		}
		this.sol = sol;
		update();
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		Parameter<?> param = event.getParameter();
		if (param == compParam || param == reversedParam || param == maxRupsParam || param == orParam) {
			update();
		} else if (param == parentSectParam) {
			if (parentSectParam.getValue()) {
				// this means it wasn't parent before, but now it is. we can convert if we have a rup set
				if (sol == null) {
					includeIDs.clear();
					excludeIDs.clear();
				} else {
					HashSet<Integer> newSectIDs = new HashSet<Integer>();
					
					for (int sectID : includeIDs) {
						int parentID = sol.getRupSet().getFaultSectionData(sectID).getParentSectionId();
						if (!newSectIDs.contains(parentID))
							newSectIDs.add(parentID);
					}
					
					includeIDs = newSectIDs;
				}
			} else {
				// going from parent to individual, can't convert
				includeIDs.clear();
				excludeIDs.clear();
			}
			update();
		} else if (param == highlightColorParam) {
			updateParentBGDisplay();
			fireColorerChangeEvent();
		} else if (param == displayParam) {
			update();
//			displayParam.getEditor().refreshParamEditor();
		} else if (param == hideNansParam) {
			fireColorerChangeEvent();
		} else if (param == magMinParam || param == magMaxParam) {
			update();
		} else if (param == minRateParam) {
			update();
		} else if (param == riParam) {
			if (riParam.getValue()) {
				setCPT(getRICPT(), true);
			} else {
				setCPT(getDefaultCPT(), true);
			}
			update();
		} else if (param == asDiscrParam) {
			fireColorerChangeEvent();
		} else if (param == exportParam && rates != null) {
			if (exportChoose == null)
				exportChoose = new JFileChooser();
			int ret = exportChoose.showSaveDialog(null);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = exportChoose.getSelectedFile();
				CSVFile<String> csv = new CSVFile<String>(true);
				csv.addLine("Section ID", "Multi Fault Participation Rate");
				for (Integer id : rates.keySet())
					csv.addLine(id+"", rates.get(id)+"");
				try {
					csv.writeToFile(file);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.toString(), "Error saving file", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

}
