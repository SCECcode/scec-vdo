package org.scec.vtk.plugins.opensha.ucerf3Rups.anims;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dom4j.DocumentException;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.observedEarthquake.parsers.UCERF3_CatalogParser;
import org.opensha.sha.faultSurface.PointSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.scec.vtk.commons.opensha.faults.AbstractFaultSection;
import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.ObsEqkRupSection;
import org.scec.vtk.commons.opensha.surfaces.FaultSectionActorList;
import org.scec.vtk.commons.opensha.surfaces.PointSurfaceGenerator;
import org.scec.vtk.commons.opensha.surfaces.PolygonSurfaceGenerator;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.opensha.obsEqkRup.ObsEqkRupBuilder;
import org.scec.vtk.plugins.opensha.obsEqkRup.RenamedParameterWrapper;
import org.scec.vtk.plugins.opensha.ucerf3Rups.UCERF3RupSetChangeListener;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.ETAS.association.FiniteFaultMappingData;
import scratch.UCERF3.erf.ETAS.association.FiniteFaultSectionResetCalc;
import scratch.UCERF3.erf.ETAS.association.FiniteFaultSectionResetCalc.SectRupDistances;
import scratch.UCERF3.inversion.InversionTargetMFDs;
import vtk.vtkActor;
import vtk.vtkProp;

public class SectionResetAnim implements FaultAnimation, ParameterChangeListener, UCERF3RupSetChangeListener, FaultColorer {
	
	private FileParameter catalogFileParam;
	private FileParameter finiteSurfsFileParam;
	private DoubleParameter fractAreaParam;
	private DoubleParameter faultBufferParam;
	private BooleanParameter removeOverlapsParam;
	private BooleanParameter polygonsParam;
	
	private List<ChangeListener> listeners = new ArrayList<>();
	private ColorerChangeListener colorListener;
	
	private ParameterList params;
	
	private PluginActors pluginActors;
	private PointSurfaceGenerator pointGen;
	private Parameter<?> pointSizeParam;
	private PolygonSurfaceGenerator polyGen;
	private Parameter<Double> opacityParam;
	private List<vtkProp> curActors;
	
	private FaultSystemRupSet rupSet;
	private FaultModels fm;
	
	private ObsEqkRupList obsRups;
	private FiniteFaultSectionResetCalc sectCalc;
	
	private int step = -1;
	private HashSet<Integer> curAreaInsideSects;
	private HashSet<Integer> curMappedSects;
	private HashSet<Integer> curResetSects;

	public SectionResetAnim(PluginActors pluginActors) {
		this.pluginActors = pluginActors;
		
		params = new ParameterList();
		
		catalogFileParam = new FileParameter("Catalog File");
		catalogFileParam.addParameterChangeListener(this);
		params.addParameter(catalogFileParam);
		
		finiteSurfsFileParam = new FileParameter("Finite Surfaces XML File");
		finiteSurfsFileParam.addParameterChangeListener(this);
		params.addParameter(finiteSurfsFileParam);
		
		File kevinGitDir = new File("/home/kevin/git/ucerf3-etas-launcher/inputs");
		if (kevinGitDir.exists()) {
			// interns: don't remove this. add an else statement if you insist on another default dir
			catalogFileParam.setDefaultInitialDir(kevinGitDir);
			finiteSurfsFileParam.setDefaultInitialDir(kevinGitDir);
		}
		
		fractAreaParam = new DoubleParameter("Fract Sect Area", 0.01, 1d);
		fractAreaParam.setValue(0.5);
		fractAreaParam.addParameterChangeListener(this);
		params.addParameter(fractAreaParam);
		
		faultBufferParam = new DoubleParameter("Fault Poly Buffer", 0.01, 20d);
		faultBufferParam.setValue(InversionTargetMFDs.FAULT_BUFFER);
		faultBufferParam.addParameterChangeListener(this);
		params.addParameter(faultBufferParam);
		
		removeOverlapsParam = new BooleanParameter("Remove Overlaps (dist-weighted)");
		removeOverlapsParam.setValue(true);
		removeOverlapsParam.addParameterChangeListener(this);
		params.addParameter(removeOverlapsParam);
		
		polygonsParam = new BooleanParameter("Show Sect Polygons", true);
		polygonsParam.addParameterChangeListener(this);
		params.addParameter(polygonsParam);
		
		pointGen = new PointSurfaceGenerator();
		pointGen.setBundlerEneabled(false);
		pointSizeParam = pointGen.getDisplayParams().getParameter(Double.class, PointSurfaceGenerator.POINT_SIZE_PARAM_NAME);
		params.addParameter(new RenamedParameterWrapper<>(pointSizeParam, "Unmapped Surf "+pointSizeParam.getName()));
		pointSizeParam.addParameterChangeListener(this);
		
		polyGen = new PolygonSurfaceGenerator();
		polyGen.setBundlerEneabled(false);
		opacityParam = polyGen.getDisplayParams().getParameter(Double.class, PolygonSurfaceGenerator.OPACITY_PARAM_NAME);
		opacityParam.setValue(0.3d);
		params.addParameter(new RenamedParameterWrapper<>(opacityParam, "Polygon "+opacityParam.getName()));
		opacityParam.addParameterChangeListener(this);
	}

	@Override
	public String getName() {
		return "Obs Rup Finite Section Reset Anim";
	}

	@Override
	public void addRangeChangeListener(ChangeListener l) {
		this.listeners.add(l);
	}

	@Override
	public int getNumSteps() {
		if (obsRups == null)
			return 0;
		return obsRups.size();
	}
	
	private void clearCurrent() {
		curAreaInsideSects = null;
		curMappedSects = null;
		curResetSects = null;
		if (curActors != null) {
			for (vtkProp prop : curActors)
				pluginActors.removeActor(prop);
			curActors = null;
		}
	}

	@Override
	public void setCurrentStep(int step) {
		this.step = step;
		clearCurrent();
		if (step >= 0 && sectCalc != null) {
			Preconditions.checkNotNull(obsRups);
			Preconditions.checkState(step < obsRups.size());
			ObsEqkRupture rup = obsRups.get(step);
			
			RuptureSurface surf = rup.getRuptureSurface();
			double[] areas = sectCalc.getAreaInSectionPolygons(surf);
			if (areas != null) {
				curAreaInsideSects = new HashSet<>();
				for (int s=0; s<areas.length; s++)
					if (areas[s] > 0)
						curAreaInsideSects.add(s);
				
				SectRupDistances[] dists = removeOverlapsParam.getValue() ? sectCalc.getSectRupDistances(surf, areas) : null;
				List<FaultSectionPrefData> sects = sectCalc.getMatchingSections(surf, areas, dists);
				if (sects != null) {
					System.out.println("Found "+sects.size()+" sections to reset");
					curResetSects = new HashSet<>();
					for (FaultSectionPrefData sect : sects)
						curResetSects.add(sect.getSectionId());
				} else {
					System.out.println("Found no sections to reset");
				}
			}
			
			int rupIndex = getRupIndex(rup);
			if (rupIndex >= 0) {
				curMappedSects = new HashSet<>(rupSet.getSectionsIndicesForRup(rupIndex));
			} else {
				Preconditions.checkNotNull(rup.getRuptureSurface());
				FaultSectionActorList actors = pointGen.createFaultActors(
						rup.getRuptureSurface(), UNMAPPED_SURF, new ObsEqkRupSection(getRupName(rup), -1, rup));
				curActors = new ArrayList<>();
				for (vtkActor actor : actors) {
					pluginActors.addActor(actor);
					curActors.add(actor);
				}
			}
			
			System.out.println("=== "+getRupName(rup)+" ===");
			for (FaultSectionPrefData sect : rupSet.getFaultSectionDataList()) {
				int index = sect.getSectionId();
				boolean hasArea = contained(curAreaInsideSects, index);
				boolean isMapped = contained(curMappedSects, index);
				boolean isReset = contained(curResetSects, index);
				String areaFractStr;
				if (hasArea) {
					double sectArea = rupSet.getAreaForSection(index)*1e-6;
					double fract = areas[index] / sectArea;
					areaFractStr = "\t\t(area fract ="+twoDigit.format(areas[index])+" / "+twoDigit.format(sectArea)+" = "+twoDigit.format(fract);
					double thresh = fractAreaParam.getValue();
					if (fract >= thresh)
						areaFractStr += " > ";
					else
						areaFractStr += " < ";
					areaFractStr += twoDigit.format(thresh)+")";
				} else {
					areaFractStr = "\t\t(no area)";
				}
				if (isMapped) {
					if (isReset)
						System.out.println(sect.getName()+": MAPPED and RESET"+areaFractStr);
					else
						System.out.println(sect.getName()+": MAPPED and NOT RESET"+areaFractStr);
				} else if (isReset) {
					System.out.println(sect.getName()+": RESET and NOT MAPPED"+areaFractStr);
				} else if (hasArea) {
					System.out.println(sect.getName()+": NEITHER MAPPED and NOR RESET, HAS AREA"+areaFractStr);
				}
				
				if (hasArea && polygonsParam.getValue()) {
					if (curActors == null)
						curActors = new ArrayList<>();
					vtkActor actor = polyGen.buildSimplePolygon(sectCalc.getPolygon(index).getBorder(), getColor(index));
					pluginActors.addActor(actor);
					curActors.add(actor);
				}
			}
			System.out.println("=============================");
		}
		if (colorListener != null)
			colorListener.colorerChanged(this);
	}

	@Override
	public int getPreferredInitialStep() {
		if (obsRups != null && step < obsRups.size())
			// this will encourage it to return to the previously viewed rupture if possible
			return step;
		return 0;
	}

	@Override
	public boolean includeStepInLabel() {
		return false;
	}
	
	private String getRupName(ObsEqkRupture rup) {
		String name = "M"+twoDigit.format(rup.getMag())+", "+df.format(new Date(rup.getOriginTime()));
		int rupIndex = getRupIndex(rup);
		if (rupIndex >= 0)
			name += ", Mapped U3 Rup # "+rupIndex;
		else
			name += ", Unmapped";
		return name;
	}

	@Override
	public String getCurrentLabel() {
		if (obsRups == null || step < 0)
			return null;
		ObsEqkRupture rup = obsRups.get(step);
		return getRupName(rup);
	}
	
	private int getRupIndex(ObsEqkRupture rup) {
		if (rup instanceof ETAS_EqkRupture)
			return ((ETAS_EqkRupture)rup).getFSSIndex();
		return -1;
	}
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
	private static DecimalFormat twoDigit = new DecimalFormat("0.##");

	@Override
	public ParameterList getAnimationParameters() {
		return params;
	}

	@Override
	public Boolean getFaultVisibility(AbstractFaultSection fault) {
		int index = fault.getId();
		boolean hasArea = contained(curAreaInsideSects, index);
		boolean isMapped = contained(curMappedSects, index);
		boolean isReset = contained(curResetSects, index);
		return hasArea || isMapped || isReset;
	}
	
	private static final Color MAPPED_AND_RESET = Color.GREEN.darker();
	private static final Color MAPPED_NOT_RESET = Color.RED.darker();
	private static final Color RESET_NOT_MAPPED = Color.BLUE.darker();
	private static final Color AREA_ONLY = Color.LIGHT_GRAY.darker();
	private static final Color UNMAPPED_SURF = Color.ORANGE.darker();

	@Override
	public Color getColor(AbstractFaultSection fault) {
		int index = fault.getId();
		return getColor(index);
	}
	
	private Color getColor(int index) {
		boolean hasArea = contained(curAreaInsideSects, index);
		boolean isMapped = contained(curMappedSects, index);
		boolean isReset = contained(curResetSects, index);
		if (isMapped) {
			if (isReset)
				return MAPPED_AND_RESET;
			else
				return MAPPED_NOT_RESET;
		}
		if (isReset)
			return RESET_NOT_MAPPED;
		if (hasArea)
			return AREA_ONLY;
		return Color.GRAY;
	}
	
	private boolean contained(HashSet<Integer> indexes, int index) {
		return indexes != null && indexes.contains(index);
	}

	@Override
	public FaultColorer getFaultColorer() {
		return this;
	}

	@Override
	public void fireRangeChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener l : listeners)
			l.stateChanged(e);
	}
	
	private void checkLoadCatalog() {
		File catalogFile = catalogFileParam.getValue();
		File finiteSurfsFile = finiteSurfsFileParam.getValue();
		
		clearCurrent();
		
		if (catalogFile != null && finiteSurfsFile != null && rupSet != null) {
			if (fm == null)
				fm = ObsEqkRupBuilder.detectFM(rupSet, null);
			try {
				// load UCERF3 catalog
				ObsEqkRupList rups = UCERF3_CatalogParser.loadCatalog(catalogFile);
				FiniteFaultMappingData.loadRuptureSurfaces(finiteSurfsFile, rups, fm, rupSet);
				
				ObsEqkRupList finiteRups = new ObsEqkRupList();
				for (ObsEqkRupture rup : rups) {
					RuptureSurface surf = rup.getRuptureSurface();
					if (surf == null || surf instanceof PointSurface || surf.isPointSurface())
						continue;
					finiteRups.add(rup);
				}
				System.out.println("Loaded "+finiteRups.size()+" finite ruptures");
				
				this.obsRups = finiteRups;
				sectCalc = new FiniteFaultSectionResetCalc(rupSet, fractAreaParam.getValue(),
						faultBufferParam.getValue(), removeOverlapsParam.getValue());
				
				fireRangeChangeEvent();
				colorListener.colorerChanged(this);
				setCurrentStep(0);
			} catch (IOException | DocumentException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		} else if (obsRups != null) {
			obsRups = null;
			fireRangeChangeEvent();
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {
		if (e.getParameter() == catalogFileParam) {
			if (finiteSurfsFileParam.getValue() == null)
				finiteSurfsFileParam.setDefaultInitialDir(catalogFileParam.getValue().getParentFile());
			checkLoadCatalog();
		} else if (e.getParameter() == finiteSurfsFileParam) {
			if (catalogFileParam.getValue() == null)
				catalogFileParam.setDefaultInitialDir(finiteSurfsFileParam.getValue().getParentFile());
			checkLoadCatalog();
		} else if (e.getParameter() == fractAreaParam) {
			if (sectCalc != null) {
				sectCalc.setMinFractionalAreaInPolygon(fractAreaParam.getValue());
				fireRangeChangeEvent();
			}
		} else if (e.getParameter() == faultBufferParam) {
			if (sectCalc != null) {
				sectCalc.setFaultBuffer(faultBufferParam.getValue());
				fireRangeChangeEvent();
			}
		} else if (e.getParameter() == removeOverlapsParam) {
			if (sectCalc != null) {
				sectCalc.setRemoveOverlapsWithDist(removeOverlapsParam.getValue());
				fireRangeChangeEvent();
			}
		} else if (e.getParameter() == pointSizeParam || e.getParameter() == opacityParam) {
			colorListener.colorerChanged(this);
			setCurrentStep(step);
		}
	}

	@Override
	public void setRupSet(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		this.rupSet = rupSet;
		this.fm = null;
	}

	@Override
	public ParameterList getColorerParameters() {
		return null;
	}

	@Override
	public void setColorerChangeListener(ColorerChangeListener l) {
		this.colorListener = l;
	}

	@Override
	public String getLegendLabel() {
		return null;
	}

}
