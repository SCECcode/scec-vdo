package org.scec.vtk.plugins.opensha.obsEqkRup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.TimeBasedFaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.ObsEqkRupSection;

public class ObsEqkRupAnim implements TimeBasedFaultAnimation, ParameterChangeListener {
	
	private ObsEqkRupGeometryGenerator geomGen;
	private ObsEqkRupList rups;
	private int step;
	
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	
	private static final String MIN_YEAR_PARAM_NAME = "Min Year";
	private static final int MIN_YEAR_DEFAULT = 0;
	private IntegerParameter minYearParam;
	
	private static final String MAX_YEAR_PARAM_NAME = "Max Year";
	private static final int MAX_YEAR_DEFAULT = 3000;
	private IntegerParameter maxYearParam;
	
	private static final String MIN_MAG_PARAM_NAME = "Min Mag";
	private static final double MIN_MAG_DEFAULT = 0;
	private DoubleParameter minMagParam;
	
	private long minYearMillis, maxYearMillis;
	private int rupsBeforeMinYear = 0;
	
	private ParameterList animParams;
	
	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	public ObsEqkRupAnim(ObsEqkRupGeometryGenerator geomGen) {
		this.geomGen = geomGen;
		
		animParams = new ParameterList();
		
		minYearParam = new IntegerParameter(MIN_YEAR_PARAM_NAME, MIN_YEAR_DEFAULT, MAX_YEAR_DEFAULT);
		minYearParam.setDefaultValue(MIN_YEAR_DEFAULT);
		minYearParam.setValueAsDefault();
		minYearParam.addParameterChangeListener(this);
		animParams.addParameter(minYearParam);
		
		maxYearParam = new IntegerParameter(MAX_YEAR_PARAM_NAME, MIN_YEAR_DEFAULT, MAX_YEAR_DEFAULT);
		maxYearParam.setDefaultValue(MAX_YEAR_DEFAULT);
		maxYearParam.setValueAsDefault();
		maxYearParam.addParameterChangeListener(this);
		animParams.addParameter(maxYearParam);
		
		minMagParam = new DoubleParameter(MIN_MAG_PARAM_NAME, 0d, 10d);
		minMagParam.setDefaultValue(MIN_MAG_DEFAULT);
		minMagParam.setValueAsDefault();
		minMagParam.addParameterChangeListener(this);
		animParams.addParameter(minMagParam);
		
		updateMinMaxYearMillis();
	}
	
	public void setRups(ObsEqkRupList rups) {
		this.rups = rups;
		updateMinMaxYearMillis();
		fireRangeChangeEvent();
	}

	@Override
	public void addRangeChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	@Override
	public int getNumSteps() {
		if (geomGen == null || rups == null)
			return 0;
		rupsBeforeMinYear = 0;
		int rupsAfterMinYear = 0;
		for (int i=0; i<rups.size(); i++) {
			if (rups.get(i).getOriginTime() < minYearMillis)
				rupsBeforeMinYear++;
			if (rups.get(i).getOriginTime() > maxYearMillis)
				rupsAfterMinYear++;
		}
		int steps = rups.size() - rupsBeforeMinYear - rupsAfterMinYear;
		return steps < 0 ? 0 : steps;
	}
	
	@Override
	public int getPreferredInitialStep() {
		int num = getNumSteps();
		if (num <= 0)
			return 0;
		return num - 1;
	}

	@Override
	public void setCurrentStep(int step) {
		this.step = step;
	}

	@Override
	public boolean includeStepInLabel() {
		return true;
	}

	@Override
	public String getCurrentLabel() {
		long milis = getMilisForStep(step);
		return df.format(new Date(milis));
	}

	@Override
	public ParameterList getAnimationParameters() {
		return animParams;
	}

	@Override
	public Boolean getFaultVisibility(AbstractFaultSection fault) {
		if (step <= 0)
			return null;
		if (fault instanceof ObsEqkRupSection)
			return shouldBeVisible(((ObsEqkRupSection)fault).getRup());
		return null;
	}
	
	private boolean shouldBeVisible(ObsEqkRupture rup) {
		long milis = rup.getOriginTime();
		return milis <= getMilisForStep(step)
				&& milis >= minYearMillis && milis <= maxYearMillis && rup.getMag() >= minMagParam.getValue();
	}

	@Override
	public FaultColorer getFaultColorer() {
		return null;
	}

	@Override
	public void fireRangeChangeEvent() {
		if (listeners.isEmpty())
			return;
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener l : listeners)
			l.stateChanged(e);
	}

	@Override
	public String getName() {
		return "Animation!";
	}

	@Override
	public double getTimeForStep(int step) {
		// make it always positive and starting at zero
		long firstMilis = getMilisForStep(0);
		return (getMilisForStep(step) - firstMilis) / 1000d;
	}
	
	private ObsEqkRupture getRupForStep(int step) {
		return rups.get(step+rupsBeforeMinYear);
	}
	
	public long getMilisForStep(int step) {
		if (rups == null || rups.isEmpty())
			return 0l;
		return getRupForStep(step).getOriginTime();
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == minYearParam || event.getParameter() == maxYearParam) {
			updateMinMaxYearMillis();
			fireRangeChangeEvent();
		}
	}
	
	private void updateMinMaxYearMillis() {
		GregorianCalendar minCal = new GregorianCalendar(minYearParam.getValue(), 0, 0);
		GregorianCalendar maxCal = new GregorianCalendar(maxYearParam.getValue(), 11, 31);
		
		minYearMillis = minCal.getTimeInMillis();
		// now adjust it to be equal to the first rup after minYearMillis
		if (rups != null) {
			for (int i=0; i<rups.size(); i++) {
				if (rups.get(i).getOriginTime() >= minYearMillis) {
					minYearMillis = rups.get(i).getOriginTime();
					break;
				}
			}
		}
		maxYearMillis = maxCal.getTimeInMillis();
	}

	@Override
	public double getCurrentDuration() {
		if (getNumSteps() > 0)
			return getTimeForStep(getNumSteps()-1) - getTimeForStep(0);
		return 0;
	}

	@Override
	public boolean timeChanged(double time) {
		return false;
	}

}
