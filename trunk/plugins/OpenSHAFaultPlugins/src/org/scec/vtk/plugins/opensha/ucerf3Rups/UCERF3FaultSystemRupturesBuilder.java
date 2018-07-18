package org.scec.vtk.plugins.opensha.ucerf3Rups;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang3.text.WordUtils;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.editor.impl.FileParameterEditor;
import org.opensha.commons.param.editor.impl.GriddedParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.ClassUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.scec.vtk.commons.opensha.faults.anim.FaultAnimation;
import org.scec.vtk.commons.opensha.faults.colorers.AseismicityColorer;
import org.scec.vtk.commons.opensha.faults.colorers.CouplingCoefficientColorer;
import org.scec.vtk.commons.opensha.faults.colorers.DipColorer;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.colorers.RakeColorer;
import org.scec.vtk.commons.opensha.faults.faultSectionImpl.PrefDataSection;
import org.scec.vtk.commons.opensha.faults.params.AseismicityParam;
import org.scec.vtk.commons.opensha.faults.params.GridSpacingFitParam;
import org.scec.vtk.commons.opensha.gui.EventManager;
import org.scec.vtk.commons.opensha.gui.FaultHighlighter;
import org.scec.vtk.commons.opensha.gui.GeometryTypeSelectorPanel;
import org.scec.vtk.commons.opensha.tree.FaultCategoryNode;
import org.scec.vtk.commons.opensha.tree.FaultSectionNode;
import org.scec.vtk.commons.opensha.tree.builders.FaultTreeBuilder;
import org.scec.vtk.commons.opensha.tree.events.TreeChangeListener;
import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.opensha.ucerf3Rups.anims.ETASCatalogAnim;
import org.scec.vtk.plugins.opensha.ucerf3Rups.anims.RupturesAnim;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.ComparisonColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.DateLastEventColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.ETASMultiCatalogColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.InversionSlipRateColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.MaxMagColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.MultiFaultRupColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.NucleationRateColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.ParentSectColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.ParticipationRateColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.RSQSimRuptureMappingColorer;
import org.scec.vtk.tools.Prefs;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.analysis.FaultSpecificSegmentationPlotGen;
import scratch.UCERF3.inversion.BatchPlotGen;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.UCERF3_Observed_MFD_Fetcher;
import scratch.UCERF3.utils.aveSlip.AveSlipConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoFitPlotter;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoSiteCorrelationData;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoRateConstraintFetcher;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UCERF3FaultSystemRupturesBuilder implements FaultTreeBuilder, ParameterChangeListener {

	private FaultSystemRupSet rupSet;
	private FaultSystemSolution sol;
	
	private ArrayList<FaultAnimation> anims;
	private ArrayList<FaultColorer> colorers;
	
	private ArrayList<UCERF3RupSetChangeListener> rupSetChangeListeners;

	private ParameterList faultParams = PrefDataSection.createPrefDataParams();
	
	private static final String RUP_SET_FILE_PARAM_NAME = "Load Rupture Set/Solution File";
	private FileParameter rupSetFileParam;
	
	private enum Figures {
		
		PALEO_CONSTRAINT("Paleosiesmic Constraint Fit"),
		RATE_COMPARISON("Rupture Rates Comparison"),
		SECTION_DATA("View/Save Rupture Info"),
		REGIONAL_MFDS("Regional MFDs"),
		RUPTURE_RATE_VS_RANK("Rupture Rate Vs Rank"),
		RUP_MOM_RATE_VS_RANK("Rupture MoRate Vs Rank"),
		SAF_SEGMENTATION("SAF Segmentation"),
		PALEO_CORRELATION_GEN("Paleo Correlation Table"),
		PALEO_FAULT_BASED("Paleo Fault Based"),
		ALL_FILE_PLOTS("All File Based Plots");
		
		private String name;
		private Figures(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	private static final String FIGURE_PARAM_NAME = "Display Figure...";
	private static final String FIGURE_PARAM_DEFAULT = "(select)";
	private EnumParameter<Figures> figureParam;
	
	private RuptureComparisonViewer rupComp;
	
	private ComparisonColorer compColor;
	
	private ETASCatalogAnim etasAnim;
	
	private ParameterList builderParams;
	
	private TreeChangeListener l;
	
	private static final String DEFAULT_NAME = "All Fault Sections";
	private String name;
	
//	private FindEquivUCERF2Anim equivAnim;
	private EventManager em;
	private FaultHighlighter highlight;
	
//	private File dataDir;
	
	private File defaultLoadDir;

	public UCERF3FaultSystemRupturesBuilder(Plugin plugin) {
		faultParams.getParameter(Boolean.class, GridSpacingFitParam.NAME).setValue(false);
		faultParams.getParameter(Boolean.class, AseismicityParam.NAME).setValue(false);
		/*
		File defaultLoadDir = new File(MainGUI.getCWD().getParentFile(),
				"OpenSHA"+File.separator+"dev"+File.separator+"scratch"+File.separator+"UCERF3"
				+File.separator+"data"+File.separator+"scratch");
				*/
		
	    File defaultLoadDir = new File(MainGUI.getCWD(),
				"data");	    
		System.out.println(defaultLoadDir.getAbsolutePath() + " ? "+defaultLoadDir.exists());
		if (!defaultLoadDir.exists())
			defaultLoadDir = null;
		
		rupSetFileParam = new FileParameter(RUP_SET_FILE_PARAM_NAME);
		setDefaultDir(defaultLoadDir);
		rupSetFileParam.addParameterChangeListener(this);
		
		figureParam = new EnumParameter<UCERF3FaultSystemRupturesBuilder.Figures>(
				FIGURE_PARAM_NAME, EnumSet.allOf(Figures.class), null, FIGURE_PARAM_DEFAULT);
		figureParam.setValue(null);
		figureParam.addParameterChangeListener(this);
		figureParam.getEditor().setEnabled(false);
		
		builderParams = new ParameterList();
		builderParams.addParameter(rupSetFileParam);
		builderParams.addParameter(figureParam);

		rupSetChangeListeners = new ArrayList<UCERF3RupSetChangeListener>();
		anims = new ArrayList<FaultAnimation>();
		colorers = new ArrayList<FaultColorer>();
		colorers.add(new AseismicityColorer());
		colorers.add(new CouplingCoefficientColorer());
		InversionSlipRateColorer invSlipRate = new InversionSlipRateColorer();
		colorers.add(0, invSlipRate);
		rupSetChangeListeners.add(invSlipRate);
		
		// ANIMATIONS
		
//		UCERF3InversionClusterRupturesAnim clusterRupAnim = new UCERF3InversionClusterRupturesAnim(rupSet);
//		anims.add(clusterRupAnim);
//		rupSetChangeListeners.add(clusterRupAnim);
		
//		UCERF3InversionConnectionsAnim connectionsAnim = new UCERF3InversionConnectionsAnim(rupSet);
//		anims.add(connectionsAnim);
//		rupSetChangeListeners.add(connectionsAnim);
		
//		dataDir = new File(MainGUI.getRootPluginResourcesDir(), "WGCEP");
//		equivAnim = new FindEquivUCERF2Anim(rupSet, dataDir);
//		anims.add(equivAnim);
//		rupSetChangeListeners.add(equivAnim);
		
//		RupSmoothAnim smoothAnim = new RupSmoothAnim();
//		anims.add(smoothAnim);
//		rupSetChangeListeners.add(smoothAnim);
		
		etasAnim = new ETASCatalogAnim(plugin.getPluginActors());
		anims.add(etasAnim);
		rupSetChangeListeners.add(etasAnim);
		
		RupturesAnim rupturesAnim = new RupturesAnim();
		anims.add(rupturesAnim);
		rupSetChangeListeners.add(rupturesAnim);
		
//		ObsRupMatchAnim obsMatchAnim = new ObsRupMatchAnim();
//		anims.add(obsMatchAnim);
//		rupSetChangeListeners.add(obsMatchAnim);
		
		// COLORERES
		
		ParticipationRateColorer partRateColor = new ParticipationRateColorer(plugin.getPluginActors());
		colorers.add(partRateColor);
		rupSetChangeListeners.add(partRateColor);
		
		NucleationRateColorer nucleationColor = new NucleationRateColorer();
		colorers.add(nucleationColor);
		rupSetChangeListeners.add(nucleationColor);
		
//		PaleoVisibleRateColorer paleoRateColor = new PaleoVisibleRateColorer();
//		colorers.add(paleoRateColor);
//		rupSetChangeListeners.add(paleoRateColor);
		
//		SlipPDFColorer slipPDFColor = new SlipPDFColorer();
//		colorers.add(slipPDFColor);
//		rupSetChangeListeners.add(slipPDFColor);
		
		compColor = new ComparisonColorer(this);
		colorers.add(compColor);
		rupSetChangeListeners.add(compColor);
		
//		SectionPairsColorer pairsColorer = new SectionPairsColorer(sol);
//		colorers.add(pairsColorer);
//		rupSetChangeListeners.add(pairsColorer);
		
		MultiFaultRupColorer stackedRupColorer = new MultiFaultRupColorer();
		colorers.add(stackedRupColorer);
		rupSetChangeListeners.add(stackedRupColorer);
		
//		SegmentationColorer segColorer = new SegmentationColorer();
//		colorers.add(segColorer);
//		rupSetChangeListeners.add(segColorer);
		
//		IsolatedSectionsColorer isolatedColorer = new IsolatedSectionsColorer();
//		colorers.add(isolatedColorer);
//		rupSetChangeListeners.add(isolatedColorer);
		
//		PaleoSitesColorer paleoSitesColorer = new PaleoSitesColorer();
//		colorers.add(paleoSitesColorer);
//		rupSetChangeListeners.add(paleoSitesColorer);
		
//		try {
//			CoulombColorer coulombColorer = new CoulombColorer();
//			colorers.add(coulombColorer);
//			rupSetChangeListeners.add(coulombColorer);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		MaxMagColorer maxMagColorer = new MaxMagColorer();
		colorers.add(maxMagColorer);
		rupSetChangeListeners.add(maxMagColorer);
		
		colorers.add(new DipColorer());
		
		colorers.add(new RakeColorer());
		
//		colorers.add(new CompoundMapPlotColorer());
		
		DateLastEventColorer dateColorer = new DateLastEventColorer();
		colorers.add(dateColorer);
		rupSetChangeListeners.add(dateColorer);
		
		colorers.add(new ParentSectColorer());
		
//		ParticipationProbabilityColorer partProbColorer = new ParticipationProbabilityColorer();
//		colorers.add(partProbColorer);
//		rupSetChangeListeners.add(partProbColorer);
		
		// ETAS multi colorer
		ETASMultiCatalogColorer etasMulti = new ETASMultiCatalogColorer(plugin.getPluginActors());
		colorers.add(etasMulti);
		rupSetChangeListeners.add(etasMulti);
		
//		CyberShakeOEFColorer csColor = new CyberShakeOEFColorer();
//		colorers.add(csColor);
//		rupSetChangeListeners.add(csColor);
		
//		colorers.add(new U3TimeDepCSVLoader());
		
//		colorers.add(new FaultEvolutionColorer());
		
		RSQSimRuptureMappingColorer rsqsimMapping = new RSQSimRuptureMappingColorer();
		rupSetChangeListeners.add(rsqsimMapping);
		colorers.add(rsqsimMapping);
	}
	
	public void setGeometryTypeSelector(GeometryTypeSelectorPanel geomSelect) {
//		equivAnim.setGeomSelect(geomSelect);
	}

	@Override
	public ParameterList getBuilderParams() {
		return builderParams;
	}

	@Override
	public ParameterList getFaultParams() {
		return faultParams;
	}

	@Override
	public void setTreeChangeListener(TreeChangeListener l) {
		this.l = l;
	}

	@Override
	public void buildTree(DefaultMutableTreeNode root) {
		if (rupSet == null)
			return;
		
		String catName;
		if (name == null)
			catName = DEFAULT_NAME;
		else
			catName = name;
		
		String info = ClassUtils.getClassNameWithoutPackage(rupSet.getClass())
				+"\nFile Name: "+name
				+"\nNum Ruptures: "+rupSet.getNumRuptures();
		try {
			info += "\nOrig (creep reduced) Moment Rate: "+rupSet.getTotalOrigMomentRate();
			info += "\nFinal (creep & subseismogenic rup reduced) Moment Rate: "
						+rupSet.getTotalReducedMomentRate();
		} catch (RuntimeException e) {
			
		}
		if (sol != null) {
			try {
				// calculate the moment
				double totalSolutionMoment = sol.getTotalFaultSolutionMomentRate();
				info += "\nSolution Moment Rate: "+totalSolutionMoment;
				
				int numNonZeros = 0;
				for (double rate : sol.getRateForAllRups())
					if (rate != 0)
						numNonZeros++;
				float percent = (float)numNonZeros / rupSet.getNumRuptures() * 100f;
				info += "\nNum Non-Zero Rups: "+numNonZeros+"/"+rupSet.getNumRuptures()+" ("+percent+" %)";
			} catch (RuntimeException e) {
				
			}
		}
		
		String infoStr = rupSet.getInfoString();
		if (infoStr != null && !infoStr.isEmpty()) {
			info += "\n\n****Metadata****";
			for (String line : Splitter.on('\n').split(infoStr))
				info += "\n"+WordUtils.wrap(line, 100);
		}
		
		info = "<html>" + info.replaceAll("\n", "<br>") + "</html>";
		FaultCategoryNode catNode = new FaultCategoryNode(catName, info);

		List<FaultSectionPrefData> sectionData = rupSet.getFaultSectionDataList();
		
		for (FaultSectionPrefData data : sectionData) {
			String name = data.getSectionId()+". "+data.getName(); 

			PrefDataSection fault = new PrefDataSection(name, data);

			FaultSectionNode faultNode = new FaultSectionNode(fault);

			catNode.add(faultNode);
		}
		
		root.add(catNode);
	}

	public ArrayList<FaultAnimation> getFaultAnimations() {
		return anims;
	}

	public ArrayList<FaultColorer> getFaultColorers() {
		return colorers;
	}
	
	public static void showErrorMessage(Throwable t) {
		JOptionPane.showMessageDialog(null, "Error: "+t.getMessage(),
				"Error reading file!", JOptionPane.ERROR_MESSAGE);
	}
	
	private void hideRupComp() {
		if (rupComp != null) {
			rupComp.setVisible(false);
			rupComp.dispose();
			rupComp = null;
		}
	}
	
	@Override
	public void parameterChange(ParameterChangeEvent event) {
		name = null;
		if (event.getParameterName().equals(RUP_SET_FILE_PARAM_NAME)) {
			hideRupComp();
			
			// we're loading a new primary file
			File file = rupSetFileParam.getValue();
			
			FaultSystemRupSet prevRupSet = rupSet;
			
			if (rupSet != null) {
				rupSet = null;
				fireRupSetNullEvent();
			}
			
			System.gc();
			
			if (file != null) {
				try {
					compoundName = null;
					// first try to load a solution
					try {
						sol = loadSolutionFromFile(file, prevRupSet);
						rupSet = sol.getRupSet();
					} catch (Exception e) {
						// now just try rupSet
						rupSet = loadRupSetFromFile(file);
					}
					if (compoundName != null) {
						name = compoundName;
						rupSetFileParam.removeParameterChangeListener(this);
						rupSetFileParam.setValue(null);
						rupSetFileParam.addParameterChangeListener(this);
					} else {
						name = file.getName();
					}
					defaultLoadDir = file.getParentFile();
					compColor.setLoadDir(defaultLoadDir);
				} catch (Exception e) {
					e.printStackTrace();
					showErrorMessage(e);
					rupSetFileParam.setValue(null);
					return;
				}
			}
			
			System.gc();
			
			if (l != null)
				l.treeChanged(null);
			
			fireRupSetChangedEvent();
		} else if (event.getParameter() == figureParam) {
			Figures fig = figureParam.getValue();
			
			if (fig != null) {
				
				switch (fig) {
				case PALEO_CONSTRAINT:
					if (sol != null && sol instanceof InversionFaultSystemSolution) {
						try {
							ArrayList<PaleoRateConstraint> segRateConstraints =
									UCERF3_PaleoRateConstraintFetcher.getConstraints(rupSet.getFaultSectionDataList());
							List<AveSlipConstraint> aveSlipConstraints =
									AveSlipConstraint.load(rupSet.getFaultSectionDataList());
							PaleoFitPlotter.showSegRateComparison(segRateConstraints, aveSlipConstraints,
									(InversionFaultSystemSolution)sol);
						} catch (IOException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(null, e.getMessage(),
									"Error loading paleo data", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(null, "At least one solution must be loaded\nfor this plot.",
								"Error creating figure", JOptionPane.ERROR_MESSAGE);
					}
					break;
				case RATE_COMPARISON:
					if (sol != null && compColor != null
					&& compColor.getCompFaultSystemSolution() != null) {
						hideRupComp();

						FaultSystemSolution sol1 = sol;
						FaultSystemSolution sol2 = compColor.getCompFaultSystemSolution();

						rupComp = new RuptureComparisonViewer(sol1, sol2, highlight);
					} else {
						JOptionPane.showMessageDialog(null, "Two solutions must be loaded for this view...both above and" +
								" in the 'Comparison Colorer' section below.", "Error creating figure",
								JOptionPane.ERROR_MESSAGE);
					}
					break;
				case SECTION_DATA:
					if (rupSet != null) {
						new RuptureInfoViewSave(rupSet, sol);
					} else {
						JOptionPane.showMessageDialog(null, "No ruptures are loaded!", "Error creating figure",
								JOptionPane.ERROR_MESSAGE);
					}
					break;
				case REGIONAL_MFDS:
					if (sol != null) {
						if (sol instanceof InversionFaultSystemSolution) {
							((InversionFaultSystemSolution)sol).plotMFDs();
						} else {
//							sol.plotMFDs(Lists.newArrayList(UCERF3_Observed_MFD_Fetcher.
//									getTargetMFDConstraint(UCERF3_Observed_MFD_Fetcher.Area.ALL_CA)));
//							sol.plotMFDs(Lists.newArrayList(UCERF3_Observed_MFD_Fetcher.
//									getTargetMFDConstraint(UCERF3_Observed_MFD_Fetcher.Area.NO_CA)));
//							sol.plotMFDs(Lists.newArrayList(UCERF3_Observed_MFD_Fetcher.
//									getTargetMFDConstraint(UCERF3_Observed_MFD_Fetcher.Area.SO_CA)));
							// TODO
						}
					}
					break;
				case RUPTURE_RATE_VS_RANK:
					if (sol != null) {
						EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(0d, rupSet.getNumRuptures(), 1d);
						double[] rates = Arrays.copyOf(sol.getRateForAllRups(), rupSet.getNumRuptures());
						Arrays.sort(rates);
						int cnt = 0;
						int zeros = 0;
						for (int i=rates.length; --i >= 0;) {
							func.set(cnt++, rates[i]);
							if (rates[i] == 0)
								zeros++;
						}
						System.out.println("Min rate: "+rates[0]);
						System.out.println("Zero rates: "+zeros);
						ArrayList<DiscretizedFunc> funcs = new ArrayList<DiscretizedFunc>();
						funcs.add(func);
						ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList(
//								new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, PlotSymbol.CIRCLE, 5f, Color.BLACK));
								new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLACK));
						GraphWindow graph = new GraphWindow(funcs, "Rupture Rate Distribution", chars); 
						graph.setX_AxisLabel("Rank");
						graph.setY_AxisLabel("Rate");
						graph.setYLog(true);
					}
					break;
				case RUP_MOM_RATE_VS_RANK:
					if (sol != null) {
						EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(0d, rupSet.getNumRuptures(), 1d);
						double[] rates = Arrays.copyOf(sol.getRateForAllRups(), rupSet.getNumRuptures());
						if (rupSet instanceof InversionFaultSystemRupSet) {
							InversionFaultSystemRupSet invRupSet = (InversionFaultSystemRupSet)rupSet;
							for (int r=0; r<rates.length; r++)
								rates[r] = rates[r] * FaultMomentCalc.getMoment(rupSet.getAreaForRup(r), invRupSet.getAveSlipForRup(r));
						} else {
							for (int r=0; r<rates.length; r++)
								rates[r] = rates[r] * MagUtils.magToMoment(rupSet.getMagForRup(r));
						}
						Arrays.sort(rates);
						int cnt = 0;
						int zeros = 0;
						EvenlyDiscretizedFunc cmlFunc = new EvenlyDiscretizedFunc(0, rates.length, 1d);
						cmlFunc.setName("Cumulative Moment Rate");
						double cml = 0;
						for (int i=rates.length; --i >= 0;) {
							cml += rates[i];
							cmlFunc.set(cnt, cml);
							func.set(cnt++, rates[i]);
							if (rates[i] == 0)
								zeros++;
						}
						System.out.println("Min rate: "+rates[0]);
						System.out.println("Zero rates: "+zeros);
						ArrayList<DiscretizedFunc> funcs = new ArrayList<DiscretizedFunc>();
						funcs.add(func);
						ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList(
//								new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, PlotSymbol.CIRCLE, 5f, Color.BLACK));
								new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLACK));
						funcs.add(cmlFunc);
						chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
						GraphWindow graph = new GraphWindow(funcs, "Rupture Moment Rate Distribution", chars); 
						graph.setX_AxisLabel("Rank");
						graph.setY_AxisLabel("Moment RateRate");
						graph.setYLog(true);
					}
					break;
				case SAF_SEGMENTATION:
					if (sol != null && sol instanceof InversionFaultSystemSolution) {
						List<Integer> parentSects = FaultSpecificSegmentationPlotGen.getSAFParents(
								((InversionFaultSystemRupSet)rupSet).getFaultModel());
						FaultSpecificSegmentationPlotGen.plotSegmentation(parentSects, (InversionFaultSystemSolution)sol, 7d, false);
					}
					break;
				case PALEO_CORRELATION_GEN:
					if (sol != null && sol instanceof InversionFaultSystemSolution) {
						JFileChooser choose = new JFileChooser();
						
					    File defaultDir = new File(MainGUI.getCWD(),
								"data");	    
						if (defaultDir.exists())
							choose.setCurrentDirectory(defaultDir);
						
						int ret = choose.showSaveDialog(null);
						if (ret == JFileChooser.APPROVE_OPTION) {
							File outFile = choose.getSelectedFile();
							if (!outFile.getName().toLowerCase().endsWith(".xls"))
								outFile = new File(outFile.getParentFile(), outFile.getName()+".xls");
							try {
								PaleoSiteCorrelationData.loadPaleoCorrelationData(
										(InversionFaultSystemSolution)sol, outFile);
							} catch (IOException e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(null, "Error: "+e.getMessage(),
										"Error writing paleo correlation file!", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
					break;
				case PALEO_FAULT_BASED:
					if (sol != null && sol instanceof InversionFaultSystemSolution) {
						InversionFaultSystemSolution invSol = (InversionFaultSystemSolution)sol;
						JFileChooser choose = new JFileChooser();
						choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						int ret = choose.showSaveDialog(null);
						if (ret == JFileChooser.APPROVE_OPTION) {
							final File outDir = choose.getSelectedFile();
							if (!outDir.exists())
								outDir.mkdir();
							try {
								List<PaleoRateConstraint> paleoRateConstraints = CommandLineInversionRunner.getPaleoConstraints(
										invSol.getRupSet().getFaultModel(), invSol.getRupSet());
								List<AveSlipConstraint> aveSlipConstraints = AveSlipConstraint.load(
										sol.getRupSet().getFaultSectionDataList());
								Map<String, List<Integer>> namedFaultsMap = invSol.getRupSet().getFaultModel().getNamedFaultsMapAlt();
								CommandLineInversionRunner.writePaleoFaultPlots(
										paleoRateConstraints, aveSlipConstraints, namedFaultsMap, invSol, outDir);
							} catch (IOException e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(null, "Error: "+e.getMessage(),
										"Error writing plots!", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
					break;
				case ALL_FILE_PLOTS:
					if (sol != null && sol instanceof InversionFaultSystemSolution) {
						JFileChooser choose = new JFileChooser();
						choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						
						String s = File.separator;
					    File defaultDir = new File(MainGUI.getCWD(),
								"data"+s+"ShakeMapPlugin");	    
						if (defaultDir.exists())
							choose.setCurrentDirectory(defaultDir);
						
						int ret = choose.showSaveDialog(null);
						if (ret == JFileChooser.APPROVE_OPTION) {
							final File outDir = choose.getSelectedFile();
							if (!outDir.exists())
								outDir.mkdir();
							final InversionFaultSystemSolution theSol = (InversionFaultSystemSolution)sol;
							JOptionPane.showMessageDialog(null, "All plots will be written to:\n"
									+outDir.getName()+"\nYou can continue to use SCEC-VDO\n" +
											"while plots are generated (it can\n" +
											"take some time), a dialog will pop\n" +
											"up when complete.",
									"All plots will be writting", JOptionPane.INFORMATION_MESSAGE);
							new Thread() {
								@Override
								public void run() {
									try {
										File file = new File(outDir, outDir.getName()+"_sol.zip");
										FaultSystemIO.writeSol(theSol, file);
										BatchPlotGen.handleSolutionFile(file, outDir.getName(), theSol, null);
										SwingUtilities.invokeLater(new Runnable() {
											
											@Override
											public void run() {
												JOptionPane.showMessageDialog(null, "All plots were written to:\n"+outDir.getName(),
														"Plots Written Successfully", JOptionPane.INFORMATION_MESSAGE);
											}
										});
									} catch (final Exception e) {
										e.printStackTrace();
										SwingUtilities.invokeLater(new Runnable() {
											
											@Override
											public void run() {
												JOptionPane.showMessageDialog(null, "Error: "+e.getMessage(),
														"Error writing plots!", JOptionPane.ERROR_MESSAGE);
											}
										});
									}
								}
							}.start();
						}
					}
					break;

				default:
					System.out.println("Unknown figure type: "+fig);
					break;
				}
				
				figureParam.setValue(null);
			}
		}
	}
	
	private static FaultSystemSolution loadSolutionFromFile(File file, FaultSystemRupSet prevRupSet) throws Exception {
		FaultSystemSolution sol;
		try {
			sol = FaultSystemIO.loadSol(file);
		} catch (Exception e) {
			if (prevRupSet != null && (file.getName().toLowerCase().endsWith(".bin") || file.getName().toLowerCase().endsWith(".mat"))) {
				// maybe it's just a solution?
				try {
					double[] solution = MatrixIO.doubleArrayFromFile(file);
					if (prevRupSet instanceof InversionFaultSystemRupSet) {
						sol = new InversionFaultSystemSolution((InversionFaultSystemRupSet)prevRupSet, solution, null, null);
					} else {
						sol = new FaultSystemSolution(prevRupSet, solution);
					}
				} catch (Exception e1) {
					throw e1;
				}
			} else {
				sol = tryLoadFromCompoundSol(file);
				if (sol == null)
					throw e;
			}
		}
		sol.setShowProgress(true);
		return sol;
	}
	
	private static FaultSystemRupSet loadRupSetFromFile(File file) throws Exception {
		FaultSystemRupSet rupSet = FaultSystemIO.loadRupSet(file);
		rupSet.setShowProgress(true);
		return rupSet;
	}
	
	private static FaultSystemSolution tryLoadFromCompoundSol(File file) {
		try {
			CompoundFaultSystemSolution sol = CompoundFaultSystemSolution.fromZipFile(file);
			if (!sol.getBranches().isEmpty()) {
				CompoundSelectionGUI gui = new CompoundSelectionGUI(sol);
				int selection = JOptionPane.showConfirmDialog(null, gui.getEditor(),
						"Select Branch", JOptionPane.OK_CANCEL_OPTION);
				if (selection == JOptionPane.OK_OPTION)
					return gui.getSelectedSolution();
			}
		} catch (Exception e) {}
		return null;
	}
	
	private static String compoundName;
	
	private static class CompoundSelectionGUI implements ParameterChangeListener {
		
		private CompoundFaultSystemSolution sol;
		
		private List<LogicTreeBranch> branches;
		private ArrayList<String> strings;
		
		private GriddedParameterListEditor editor;
		
		private StringParameter filterParam;
		private ButtonParameter applyFilterParam;
		private StringParameter listParam;
		
		private static final String NONE_STRING = "(none)";
		
		public CompoundSelectionGUI(CompoundFaultSystemSolution sol) {
			this.sol = sol;
			branches = Lists.newArrayList(sol.getBranches());
			Collections.sort(branches, new Comparator<LogicTreeBranch>() {

				@Override
				public int compare(LogicTreeBranch o1, LogicTreeBranch o2) {
					return o1.buildFileName().compareTo(o2.buildFileName());
				}
				
			});
			strings = Lists.newArrayList();
			for (LogicTreeBranch branch : branches)
				strings.add(branch.buildFileName());
			
			ParameterList params = new ParameterList();
			
			filterParam = new StringParameter("Filter (multiple can be comma separated)", "");
			params.addParameter(filterParam);
			
			applyFilterParam = new ButtonParameter("Update Filter", "Apply");
			applyFilterParam.addParameterChangeListener(this);
			params.addParameter(applyFilterParam);
			
			listParam = new StringParameter("Available Branches", strings, strings.get(0));
			params.addParameter(listParam);
			
			editor = new GriddedParameterListEditor(params, 0, 1);
			editor.setPreferredSize(new Dimension(700, 200));
		}

		@Override
		public void parameterChange(ParameterChangeEvent event) {
			if (event.getParameter() == applyFilterParam) {
				String filterStr = filterParam.getValue().trim();
				if (!filterStr.isEmpty()) {
					List<String> filters = Lists.newArrayList();
					if (filterStr.contains(",")) {
						for (String filter : Splitter.on(",").split(filterStr))
							filters.add(filter.trim());
					} else {
						filters.add(filterStr);
					}
					updateFilter(filters);
				}
			}
		}
		
		private void updateFilter(List<String> filters) {
			ArrayList<String> filteredStrings;
			if (filters == null || filters.isEmpty()) {
				filteredStrings = strings;
			} else {
				filteredStrings = Lists.newArrayList();
				nameLoop:
				for (String name : strings) {
					for (String filter : filters)
						if (!name.contains(filter))
							continue nameLoop;
					filteredStrings.add(name);
				}
			}
			
			if (filteredStrings.isEmpty())
				filteredStrings.add(NONE_STRING);
			String selection = listParam.getValue();
			if (!filteredStrings.contains(selection))
				selection = filteredStrings.get(0);
			StringConstraint sconst = (StringConstraint)listParam.getConstraint();
			sconst.setStrings(filteredStrings);
			listParam.setValue(selection);
			listParam.getEditor().refreshParamEditor();
		}
		
		public FaultSystemSolution getSelectedSolution() {
			String name = listParam.getValue();
			int index = strings.indexOf(name);
			if (index < 0)
				return null;
			FaultSystemSolution fss = sol.getSolution(branches.get(index));
			compoundName = name;
			return fss;
		}
		
		public GriddedParameterListEditor getEditor() {
			return editor;
		}
	}
	
	public void setEventManager(EventManager em) {
		this.em = em;
		highlight = new FaultHighlighter(em, em);
//		etasAnim.setSphereBG(em.getMasterBG());
	}
	
	private void fireRupSetNullEvent() {
		for (UCERF3RupSetChangeListener l : rupSetChangeListeners)
			l.setRupSet(null, null);
	}
	
	private void fireRupSetChangedEvent() {
//		figureParam.getEditor().setEnabled(rupSet instanceof FaultSystemSolution);
		figureParam.getEditor().setEnabled(rupSet != null);
		if (em != null)
			try {
				em.waitOnCalcThread();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		for (UCERF3RupSetChangeListener l : rupSetChangeListeners)
			l.setRupSet(rupSet, sol);
	}
	
	public void addRupSetChangeListener(UCERF3RupSetChangeListener l) {
		rupSetChangeListeners.add(l);
	}
	
	public File getDefaultDir() {
		return defaultLoadDir;
	}
	
	public void setDefaultDir(File defaultLoadDir) {
		this.defaultLoadDir = defaultLoadDir;
		((FileParameterEditor)rupSetFileParam.getEditor()).setDefaultDir(defaultLoadDir);
	}

}