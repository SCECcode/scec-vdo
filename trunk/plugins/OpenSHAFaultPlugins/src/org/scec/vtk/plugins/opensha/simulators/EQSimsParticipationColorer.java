package org.scec.vtk.plugins.opensha.simulators;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.iden.ElementIden;
import org.opensha.sha.simulators.iden.FaultIDIden;
import org.opensha.sha.simulators.iden.MagRangeRuptureIdentifier;
import org.opensha.sha.simulators.iden.SectionIDIden;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;
import org.opensha.sha.simulators.utils.SimulatorUtils;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.SimulatorElementFault;
import org.scec.vtk.tools.picking.PickEnabledActor;
import org.scec.vtk.tools.picking.PickHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.kevin.simulators.MFDCalc;
import vtk.vtkCellPicker;

public class EQSimsParticipationColorer extends CPTBasedColorer implements EQSimsEventListener, ParameterChangeListener,
PickHandler<AbstractFaultSection> {
	
	private DoubleParameter magMinParam;
	private static final double MIN_MAG_DEFAULT = 6.5;
	private static final double MAX_MAG_DEFAULT = 10d;
	private DoubleParameter magMaxParam;
	private double minMag = MIN_MAG_DEFAULT;
	private double maxMag = MAX_MAG_DEFAULT;
	
	private double minEventMag, maxEventMag;
	
	private BooleanParameter probabilityParam;
	private DoubleParameter probDurationParam;
	
	private FileParameter csvComparisonParam;
	private List<DiscretizedFunc> comparisonCumMFDs;
	private HashMap<Integer, Double> comparisonRates;
	
	private enum PlotType {
		CATALOG("Catalog Rate"),
		UCERF3("UCERF3 Rate"),
		RATIO("Ratio");
		
		private String name;
		private PlotType(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	private static final String PLOT_TYPE_PARAM_NAME = "Plot Type";
	private static final PlotType PLOT_TYPE_DEFAULT = PlotType.CATALOG;
	private EnumParameter<PlotType> plotTypeParam;
	
	private ParameterList params;
	
	private List<? extends SimulatorEvent> events;
	private HashMap<Integer, SimulatorEvent> eventsMap;
	protected HashMap<Integer, Double> rates;
	
	private List<SimulatorElement> elements;
	private int minElemIndex = Integer.MAX_VALUE;
	private int maxElemIndex = -1;
	private int minSectIndex = Integer.MAX_VALUE;
	private int maxSectIndex = -1;
	private int myNumSects = -1;
	
	private static CPT getDefaultRateCPT() {
		try {
			CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance();
			cpt = cpt.rescale(-6, -2);
			cpt.setNanColor(Color.GRAY);
			return cpt;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private static CPT getDefaultRatioCPT() {
		try {
			CPT cpt = GMT_CPT_Files.UCERF3_RATIOS.instance();
			cpt = cpt.rescale(-3, 3d);
			cpt.setNanColor(Color.GRAY);
			return cpt;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private CPT rateCPT;
	private boolean rateCPTLog;
	private CPT ratioCPT = getDefaultRatioCPT();
	private boolean ratioCPTLog = true;

	public EQSimsParticipationColorer() {
		super(getDefaultRateCPT(), true);
		rateCPT = getCPT();
		rateCPTLog = isCPTLog();
		
		params = new ParameterList();
		magMinParam = new DoubleParameter("Min Mag", 0d, 10d);
		magMinParam.setValue(MIN_MAG_DEFAULT);
		magMinParam.addParameterChangeListener(this);
		params.addParameter(magMinParam);
		magMaxParam = new DoubleParameter("Max Mag", 0d, 10d);
		magMaxParam.setValue(MAX_MAG_DEFAULT);
		magMaxParam.addParameterChangeListener(this);
		params.addParameter(magMaxParam);
		
		probabilityParam = new BooleanParameter("Probabilities", false);
		probabilityParam.addParameterChangeListener(this);
		probabilityParam.setInfo("If selected, Poisson probabilities with the given duration. Otherwise annualized rates");
		params.addParameter(probabilityParam);
		
		probDurationParam = new DoubleParameter("Duration", 1d/365.25, 100000d, "Years");
		probDurationParam.setValue(50);
		probDurationParam.addParameterChangeListener(this);
		params.addParameter(probDurationParam);
		
		csvComparisonParam = new FileParameter("U3 CSV For Comparison");
		csvComparisonParam.addParameterChangeListener(this);
		params.addParameter(csvComparisonParam);
		
		plotTypeParam = new EnumParameter<PlotType>(PLOT_TYPE_PARAM_NAME,
				EnumSet.allOf(PlotType.class), PLOT_TYPE_DEFAULT, null);
		plotTypeParam.addParameterChangeListener(this);
		plotTypeParam.getEditor().setEnabled(false);
		params.addParameter(plotTypeParam);
	}

	@Override
	public String getName() {
		return "Simulator Participation Rates";
	}

	@Override
	public double getValue(AbstractFaultSection fault) {
		return getValue(fault.getId());
	}
	
	public double getValue(int id) {
		double simRate, u3Rate;
		switch (plotTypeParam.getValue()) {
		case CATALOG:
			simRate = getSimulator(id);
			return getProbIfApplicable(simRate);
		case UCERF3:
			u3Rate = getUCER3(id);
			return getProbIfApplicable(u3Rate);
		case RATIO:
			simRate = getSimulator(id);
			u3Rate = getUCER3(id);
			return simRate/u3Rate;

		default:
			throw new IllegalStateException("Unknown plot type!");
		}
	}
	
	private double getSimulator(int id) {
		if (events == null)
			return Double.NaN;
		if (rates == null) {
			synchronized (this) {
				if (rates == null)
					updateCache();
			}
		}
		Double rate = rates.get(id);
		if (rate == null)
			return 0d;
		return rate;
	}
	
	private double getUCER3(int id) {
		if (comparisonCumMFDs == null)
			return Double.NaN;
		if (comparisonRates == null) {
			synchronized (this) {
				if (comparisonRates == null)
					updateComparisonCache();
			}
		}
		if (comparisonRates == null)
			// if still null then there's an error matching up the file with the elements
			return Double.NaN;
		return comparisonRates.get(id);
	}
	
	private double getProbIfApplicable(double rate) {
		if (probabilityParam.getValue()) {
			double duration = probDurationParam.getValue();
			double prob = 1d - Math.exp(-rate*duration);
			return prob;
		}
		return rate;
	}

	@Override
	public void setEvents(List<? extends SimulatorEvent> events) {
		this.events = events;
		eventsMap = Maps.newHashMap();
		if (events == null || events.isEmpty()) {
			minEventMag = 0;
			maxEventMag = 0;
		} else {
			minEventMag = Double.MAX_VALUE;
			maxEventMag = 0d;
			
			for (SimulatorEvent e : events) {
				double mag = e.getMagnitude();
				if (mag < minEventMag)
					minEventMag = mag;
				if (mag > maxEventMag)
					maxEventMag = mag;
				eventsMap.put(e.getID(), e);
			}
		}
		rates = null;
	}
	
	double getMinEventMag() {
		return minEventMag;
	}

	double getMaxEventMag() {
		return maxEventMag;
	}

	void clearCache() {
		rates = null;
		comparisonRates = null;
	}
	
	synchronized void updateCache() {
		if (events == null)
			return;
		
		rates = new HashMap<Integer, Double>();
		
		if (events.isEmpty())
			return;
		
		double years = SimulatorUtils.getSimulationDurationYears(events);
		double rate = 1d/years; // each event happens once in the simulation
		
		System.out.println("Updating participation rates! Time span: "+years+" years. Rate/event: "+rate);
		System.out.println("Num events: "+events.size());
		
		int eventCount = 0;
		for (SimulatorEvent e : events) {
			double mag = e.getMagnitude();
			if (!isWithinMagRange(mag))
				continue;
			
			for (Integer elementID : e.getAllElementIDs()) {
				Double val = rates.get(elementID);
				if (val == null)
					val = rate;
				else
					val += rate;
				rates.put(elementID, val);
			}
			eventCount++;
		}
		System.out.println("Found "+eventCount+" events in the given range");
	}
	
	synchronized void updateComparisonCache() {
		if (comparisonCumMFDs == null || elements == null)
			return;
		if (!validateComparisonCSV())
			return;
		comparisonRates = Maps.newHashMap();
		for (SimulatorElement elem : elements) {
			int sectID = elem.getSectionID() - minSectIndex;
			DiscretizedFunc mfd = comparisonCumMFDs.get(sectID);
			double val = mfd.getInterpolatedY_inLogYDomain(minMag);
			comparisonRates.put(elem.getID(), val);
		}
	}
	
	boolean isWithinMagRange(double mag) {
		return mag >= minMag && mag < maxMag;
	}
	
	private void safeDisablePlotType() {
		plotTypeParam.getEditor().setEnabled(false);
		if (plotTypeParam.getValue() != PlotType.CATALOG)
			plotTypeParam.setValue(PlotType.CATALOG);
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == magMinParam || event.getParameter() == magMaxParam) {
			minMag = magMinParam.getValue();
			maxMag = magMaxParam.getValue();
			clearCache();
			fireColorerChangeEvent();
		} else if (event.getParameter() == probabilityParam || event.getParameter() == probDurationParam) {
			probDurationParam.getEditor().setEnabled(probabilityParam.getValue());
			fireColorerChangeEvent();
		} else if (event.getParameter() == csvComparisonParam) {
			comparisonCumMFDs = null;
			File csvFile = csvComparisonParam.getValue();
			if (csvFile == null) {
				safeDisablePlotType();
				return;
			}
			CSVFile<String> csv;
			try {
				csv = CSVFile.readFile(csvFile, true);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error opening CSV", JOptionPane.ERROR_MESSAGE);
				safeDisablePlotType();
				return;
			}
			final int startCol = 3;
			List<Double> mags = new ArrayList<>();
			comparisonCumMFDs = new ArrayList<>();
			for (int col=startCol; col<csv.getNumCols(); col++)
				mags.add(Double.parseDouble(csv.get(0, col)));
			for (int row=1; row<csv.getNumRows(); row++) {
				int index = Integer.parseInt(csv.get(row, 0));
				Preconditions.checkState(index == comparisonCumMFDs.size(), "File out of order!");
				DiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
				for (int i=0; i<mags.size(); i++)
					func.set(mags.get(i), Double.parseDouble(csv.get(row, startCol+i)));
				comparisonCumMFDs.add(func);
			}
			plotTypeParam.getEditor().setEnabled(true);
		} else if (event.getParameter() == plotTypeParam) {
			if (plotTypeParam.getValue() == PlotType.CATALOG || plotTypeParam.getValue() == PlotType.UCERF3) {
				if (event.getOldValue().equals(PlotType.RATIO)) {
					// switching from ratio, store any custom ratio settings
					ratioCPT = getCPT();
					ratioCPTLog = isCPTLog();
					setCPT(rateCPT, rateCPTLog);
				}
			} else {
				// switching to ratio, store any current rate settings
				rateCPT = getCPT();
				rateCPTLog = isCPTLog();
				setCPT(ratioCPT, ratioCPTLog);
			}
			fireColorerChangeEvent();
		}
	}
	
	protected List<? extends SimulatorEvent> getEvents() {
		return events;
	}
	
	protected SimulatorEvent getEvent(int id) {
		return eventsMap.get(id);
	}

	@Override
	public ParameterList getColorerParameters() {
		return params;
	}

	@Override
	public void setGeometry(List<SimulatorElement> elements) {
		this.elements = elements;
		minElemIndex = Integer.MAX_VALUE;
		maxElemIndex = -1;
		minSectIndex = Integer.MAX_VALUE;
		maxSectIndex = -1;
		if (elements != null) {
			for (SimulatorElement elem : elements) {
				int e = elem.getID();
				if (e < minElemIndex)
					minElemIndex = e;
				if (e > maxElemIndex)
					maxElemIndex = e;
				int s = elem.getSectionID();
				if (s < 0)
					continue;
				if (s < minSectIndex)
					minSectIndex = s;
				if (s > maxSectIndex)
					maxSectIndex = s;
			}
			myNumSects = (maxSectIndex - minSectIndex) + 1;
		} else {
			myNumSects = -1;
		}
	}
	
	private boolean validateComparisonCSV() {
		if (myNumSects != comparisonCumMFDs.size()) {
			JOptionPane.showMessageDialog(null, "Section count mismatch. CSV has "+comparisonCumMFDs.size()
			+", geom file has "+myNumSects, "Can't use comparison CSV", JOptionPane.ERROR_MESSAGE);
			csvComparisonParam.setValue(null);
			csvComparisonParam.getEditor().refreshParamEditor();
			return false;
		}
		return true;
	}

	@Override
	public void actorPicked(PickEnabledActor<AbstractFaultSection> actor, AbstractFaultSection reference,
			vtkCellPicker picker, MouseEvent e) {
		int clickCount = e.getClickCount();
		// return if we don't have events, or it's not a double click
		if (events == null || events.isEmpty() || clickCount < 2 || e.getButton() != MouseEvent.BUTTON1
				|| !(reference instanceof SimulatorElementFault))
			return;
		
		List<Double> recurrenceMags = Lists.newArrayList(6d, 6.5d, 7d, 7.5d);
		if (!recurrenceMags.contains(magMinParam.getValue()))
			recurrenceMags.add(magMinParam.getValue());
		Collections.sort(recurrenceMags);
		
		SimulatorElementFault elementFault = (SimulatorElementFault)reference;
		
		SimulatorElement element = elementFault.getElement();
		
		List<String> names = Lists.newArrayList();
		List<List<? extends SimulatorEvent>> eventLists = Lists.newArrayList();
		List<DiscretizedFunc> comparisonMFDs = Lists.newArrayList();
		
		int elemID = element.getID();
		List<? extends SimulatorEvent> matches = new ElementIden(elemID+"", elemID).getMatches(events);
		if (!matches.isEmpty()) {
			names.add("Elem "+elemID);
			eventLists.add(matches);
			comparisonMFDs.add(null);
		}
		
		int sectID = element.getSectionID();
		if (sectID >= 0) {
			matches = new SectionIDIden(sectID+"", elements, sectID).getMatches(events);
			if (!matches.isEmpty()) {
				names.add("Sect "+sectID+": "+element.getSectionName());
				eventLists.add(matches);
				if (comparisonCumMFDs != null) {
					System.out.println("Min: "+minSectIndex);
					System.out.println("Max: "+maxSectIndex);
					System.out.println("File: "+comparisonCumMFDs.size());
					if (validateComparisonCSV())
						comparisonMFDs.add(comparisonCumMFDs.get(sectID-minSectIndex));
					else
						comparisonMFDs.add(null);
				} else {
					comparisonMFDs.add(null);
				}
			}
		}
		int faultID = element.getFaultID();
		if (faultID >= 0) {
			matches = new FaultIDIden(sectID+"", elements, faultID).getMatches(events);
			if (!matches.isEmpty()) {
				names.add("Parent "+faultID);
				eventLists.add(matches);
				comparisonMFDs.add(null);
			}
		}
		
		if (names.isEmpty())
			return;
		
		GraphWindow graph = null;
		
		double duration = events.get(events.size()-1).getTimeInYears() - events.get(0).getTimeInYears();
		
		double eventMinMag = Double.POSITIVE_INFINITY;
		for (SimulatorEvent event : events)
			if (event.getMagnitude() < eventMinMag)
				eventMinMag = event.getMagnitude();
		double minMag = Math.floor(eventMinMag);
		if (minMag > 5d)
			minMag = 5d;
		double deltaMag = 0.1;
		int numMag = (int)((9d - minMag)/deltaMag + 0.5);
		Preconditions.checkState(numMag > 1, "Bad numMag="+numMag+", minMag="+minMag);
		minMag += 0.5*deltaMag;
		
		for (int i=0; i<names.size(); i++) {
			String name = names.get(i);
			List<? extends SimulatorEvent> events = eventLists.get(i);
			
			// calc MFD
			IncrementalMagFreqDist mfd = MFDCalc.calcMFD(events, null, duration, minMag, numMag, deltaMag);
			mfd.setName("Incremental MFD");
			mfd.setInfo(" ");
			EvenlyDiscretizedFunc cumMFD = mfd.getCumRateDistWithOffset();
			cumMFD.setName("Cumulative MFD");
			cumMFD.setInfo(" ");
			
			ArrayList<XY_DataSet> funcs = new ArrayList<>();
			ArrayList<PlotCurveCharacterstics> chars = new ArrayList<>();
			funcs.add(mfd);
			chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));
			funcs.add(cumMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			
			DiscretizedFunc compMFD = comparisonMFDs.get(i);
			if (compMFD != null) {
				funcs.add(compMFD);
				compMFD.setName("U3 Comparison");
				chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
			}
			
			PlotSpec spec = new PlotSpec(funcs, chars, name+" MFD", "Magnitude", "Participation Rate (1/yr)");
			spec.setLegendVisible(true);
			
			if (graph == null)
				graph = new GraphWindow(spec, false);
			else
				graph.addTab(spec);
			
			graph.setYLog(true);
			graph.setAxisRange(6, 9, 1e-10, 1e-1);
			
			// now recurrence intervals
			for (double riMag : recurrenceMags) {
				List<? extends SimulatorEvent> riEvents = new MagRangeRuptureIdentifier(riMag, 10d).getMatches(events);
				if (riEvents.size() < 3)
					continue;
				
				double[] ris = new double[riEvents.size()-1];
				for (int r=0; r<riEvents.size()-1; r++)
					ris[r] = riEvents.get(r+1).getTimeInYears() - riEvents.get(r).getTimeInYears();
				
				double meanRI = StatUtils.mean(ris);
				
				double riDelta = 5d;
				if (meanRI > 300)
					riDelta = 10;
				if (meanRI > 1000)
					riDelta = 50;
				
				HistogramFunction hist = HistogramFunction.getEncompassingHistogram(StatUtils.min(ris), StatUtils.max(ris), riDelta);
				for (double ri : ris)
					hist.add(ri, 1d);
				
				funcs = new ArrayList<>();
				chars = new ArrayList<>();
				funcs.add(hist);
				hist.setName("M≥"+(float)riMag+" RI Dist");
				chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));
				
				DefaultXY_DataSet meanLine = new DefaultXY_DataSet();
				meanLine.set(meanRI, 0d);
				meanLine.set(meanRI, hist.getMaxY());
				meanLine.set(meanRI, hist.getMaxY()*1.25);
				meanLine.setName("Mean RI: "+(float)meanRI);
				funcs.add(meanLine);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
				
				if (compMFD != null && riMag >= compMFD.getMinX() && riMag <= compMFD.getMaxX()) {
					double rate = compMFD.getInterpolatedY(riMag);
					double compRI = 1d/rate;
					
					DefaultXY_DataSet compLine = new DefaultXY_DataSet();
					compLine.set(compRI, 0d);
					compLine.set(compRI, hist.getMaxY());
					compLine.set(compRI, hist.getMaxY()*1.25);
					compLine.setName("U3 Comparison RI: "+(float)compRI);
					funcs.add(compLine);
					chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
				}
				
				spec = new PlotSpec(funcs, chars, "M≥"+(float)riMag+" RI", "Time (years)", "Number");
				spec.setLegendVisible(true);
				
				graph.addTab(spec);
			}
		}
		
		graph.setSelectedTab(0);
		graph.setVisible(true);
	}

}