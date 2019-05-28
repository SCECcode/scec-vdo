package org.scec.vtk.commons.opensha.gui.dist;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.opensha.commons.geo.LocationUtils;
import org.opensha.refFaultParamDb.calc.sectionDists.FaultSectDistCalculator;
import org.opensha.refFaultParamDb.calc.sectionDists.FaultSectDistRecord;
import org.opensha.refFaultParamDb.calc.sectionDists.SmartSurfaceFilter;
import org.opensha.refFaultParamDb.calc.sectionDists.SurfaceFilter;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.Surface3D;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;

public class DistanceCalc {
	
	private VisibleFaultSurfacesProvider surfaceProv;
	
	private ArrayList<NamedDistRecord> records;
	private HashMap<Integer, AbstractFaultSection> idFaultMap;
	
	private static final DecimalFormat distFormat = new DecimalFormat("0.00");
	
	public DistanceCalc(VisibleFaultSurfacesProvider surfaceProv) {
		this.surfaceProv = surfaceProv;
	}
	
	public void updateFaultConnections(double maxDist) throws InterruptedException {
		HashMap<AbstractFaultSection, Surface3D> surfaceMap = surfaceProv.getVisibleSurfaces();
		
		ArrayList<EvenlyGriddedSurface> surfaces = new ArrayList<EvenlyGriddedSurface>();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		idFaultMap = new HashMap<Integer, AbstractFaultSection>();
		
		for (AbstractFaultSection fs : surfaceMap.keySet()) {
			Surface3D surface = surfaceMap.get(fs);
			if (surface instanceof EvenlyGriddedSurface) {
				ids.add(fs.getId());
				surfaces.add((EvenlyGriddedSurface)surface);
				idFaultMap.put(fs.getId(), fs);
			}
		}

		System.out.println("Calculating for " + ids.size() + " sections");
		
		if (ids.size() == 0)
			return;

		double faultDisc = LocationUtils.linearDistance(surfaces.get(0).get(0, 0), surfaces.get(0).get(1, 0));
		
		// first we filter out faults that are obviously far away from each other
		//		faultSects.getFaultSection(index)
		FaultSectDistCalculator distCalc = new FaultSectDistCalculator(true, surfaces, ids);
		double cornerMidptDistFilter = maxDist * 5d;
		int outlineModulus = (int)((1 / (faultDisc / 4)));
		int internalModulus = (int)(1 / (faultDisc / 5));
		double filterDist = maxDist * 1.75;
		System.out.println("faultDisc: " + faultDisc + " maxDist: " + maxDist + " cornerMidptDistFilter: " + cornerMidptDistFilter);
		System.out.println("outlineModulus: " + outlineModulus + " internalModulus: " + internalModulus + " filterDist: " + filterDist);
		SurfaceFilter filter = new SmartSurfaceFilter(outlineModulus, internalModulus, cornerMidptDistFilter);
		distCalc.createPairings(filter, filterDist);
		System.out.println("Pair time: " + distCalc.getPairTimeSecs());

		// get available processors
		int threads = Runtime.getRuntime().availableProcessors();
		System.out.println("calculating distances with " + threads + " threads");
		// do the calculation
		distCalc.calcDistances(threads);
		System.out.println("Calc time: " + distCalc.getCalcTimeSecs());

		records = new ArrayList<NamedDistRecord>();
		for (FaultSectDistRecord record : distCalc.getRecords().values()) {
			if (record.getMinDist() <= maxDist) {
				AbstractFaultSection fault1 = idFaultMap.get(record.getID1());
				AbstractFaultSection fault2 = idFaultMap.get(record.getID2());
				String name1 = fault1.getName();
				String name2 = fault2.getName();
				String recName = distFormat.format(record.getMinDist()) + " KM: " + name1 + " => " + name2;
				records.add(new NamedDistRecord(record, recName, fault1, fault2));
			}
		}

//		ArrayList<String> allowedStrings = new ArrayList<String>();
//		allowedStrings.add(FAULT_CONNECTIONS_PARAM_DEFAULT);
//		allowedStrings.addAll(faultDistNames);
//
//		constraint.setStrings(allowedStrings);
//
//		faultConnectionsParam.getEditor().setParameter(faultConnectionsParam);
//		exportFaultConnButton.setEnabled(records.size() > 0);
	}
	
	public void sortByDistance() {
		sortRecords(new NamedRecordDistComparator());
	}
	
	public void sortBySlip() {
		HashMap<Integer, Double> slipMap = new HashMap<Integer, Double>();
		for (AbstractFaultSection fault : idFaultMap.values()) {
			
			slipMap.put(fault.getId(), fault.getSlipRate());
		}
		sortRecords(new NamedRecordAveQuantityComparator(slipMap, false));
	}
	
	public void sortRecords(Comparator<NamedDistRecord> comparator) {
		if (records == null)
			return;
		System.out.print("sorting...");
		Collections.sort(records, comparator);
		System.out.println("DONE!");
	}

	public ArrayList<NamedDistRecord> getRecords() {
		return records;
	}
	
	public void clear() {
		records = null;
		idFaultMap = null;
	}

}
