package org.scec.vtk.commons.opensha.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.opensha.commons.mapping.gmt.gui.CPTPanel;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.impl.GriddedParameterListEditor;
import org.opensha.commons.param.editor.impl.ParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.ListUtils;
import org.opensha.commons.util.cpt.CPT;
import org.scec.vtk.commons.legend.LegendItem;
import org.scec.vtk.commons.legend.LegendUtils;
import org.scec.vtk.commons.opensha.faults.colorers.CPTBasedColorer;
import org.scec.vtk.commons.opensha.faults.colorers.ColorerChangeListener;
import org.scec.vtk.commons.opensha.faults.colorers.FaultColorer;
import org.scec.vtk.commons.opensha.faults.colorers.SlipRateColorer;
import org.scec.vtk.commons.opensha.tree.gui.FaultTreeTable;
import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginActors;
import org.scec.vtk.plugins.utils.components.ColorButton;
import org.scec.vtk.plugins.utils.components.ShowButton;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;

import vtk.vtkActor2D;
import vtk.vtkProp;

public class ColorerPanel extends JPanel implements ParameterChangeListener, ActionListener, ColorerChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<ColorerChangeListener> listeners = new ArrayList<ColorerChangeListener>();
	
	private static final String COLORER_SELECTOR_PARAM_NAME = "Color Faults By";
	private static final String COLORER_SELECTOR_CUSTOM = "(custom)";
	
	private Plugin plugin;
	
	private StringParameter colorerParam;
	
	private List<FaultColorer> colorers;
	
	private JButton browseButton = new JButton("Load CPT");
	private JButton rescaleButton = new JButton("Rescale CPT");
	private ShowButton visibilityButton = new ShowButton(this, "Visibility");
	private ColorButton colorButton = new ColorButton(this, "Colors");
	private JCheckBox logCheck = new JCheckBox("Log10 Scale");
	private JCheckBox legendCheckbox = new JCheckBox("Add Legend");
	private JFileChooser chooser;
	private SingleColorChooser colorChooser = new SingleColorChooser(this);
	
	private FaultTreeTable faultTable;
	
	private CPTPanel cptPanel;
	
	private ParameterListEditor rangeSelectEditor;
	private DoubleParameter rangeSelectMin;
	private DoubleParameter rangeSelectMax;
	
	private static int cpt_width = 350;
	private static int cpt_height = 70;
	private static int cpt_image_width = 300;
	private static int cpt_image_height = 20;
//	private static int cpt_num_ticks = 4;
	private static int cpt_num_ticks = -1;
	private static int cpt_tick_width = 4;
	
	private GriddedParameterListEditor paramsEdit;
	
	private LegendItem legend;

	public ColorerPanel(Plugin plugin, List<FaultColorer> colorers, FaultColorer selected) {
		this.plugin = plugin;
		this.colorers = colorers;
		for (FaultColorer colorer : colorers)
			colorer.setColorerChangeListener(this);
		ArrayList<String> names = ListUtils.getNamesList(colorers);
		names.add(COLORER_SELECTOR_CUSTOM);
		
		String selectedName;
		if (selected == null)
			selectedName = COLORER_SELECTOR_CUSTOM;
		else
			selectedName = selected.getName();
		
		colorerParam = new StringParameter(COLORER_SELECTOR_PARAM_NAME, names, selectedName);
		
		colorerParam.addParameterChangeListener(this);
		
		browseButton.addActionListener(this);
		rescaleButton.addActionListener(this);
		logCheck.addActionListener(this);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// add everything to the top panel
		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS));
		selectPanel.add(colorerParam.getEditor().getComponent());
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
		
		legendCheckbox.addActionListener(this);
		
		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));
		wrapperPanel.add(logCheck);
		wrapperPanel.add(legendCheckbox);
		
		controlPanel.add(wrapperPanel);
		controlPanel.add(visibilityButton);
		controlPanel.add(colorButton);
		visibilityButton.setEnabled(true);
		colorButton.setEnabled(true);
		controlPanel.add(rescaleButton);
		controlPanel.add(browseButton);
		
		
		// this is the lower CPT panel
		cptPanel = new CPTPanel(null, cpt_width, cpt_image_width, cpt_image_height, cpt_tick_width);
		cptPanel.setDecimalFormat(new DecimalFormat("0.###"));
		Dimension cptDims = new Dimension(cpt_width, cpt_height);
		cptPanel.setSize(cptDims);
		cptPanel.setMinimumSize(cptDims);
		cptPanel.setPreferredSize(cptDims);
		
		this.add(selectPanel);
		this.add(controlPanel);
		JPanel cptPanelWrapper = new JPanel();
		cptPanelWrapper.add(cptPanel);
		this.add(cptPanelWrapper);
		
		
		ParameterList rangeSelectList = new ParameterList();
		rangeSelectMin = new DoubleParameter("Min");
		rangeSelectMax = new DoubleParameter("Max");
		rangeSelectList.addParameter(rangeSelectMin);
		rangeSelectList.addParameter(rangeSelectMax);
		rangeSelectEditor = new ParameterListEditor(rangeSelectList);
		rangeSelectEditor.setName("Select Range");
		
		updateForCPT();
	}
	
	public void setFaultTable(FaultTreeTable faultTable) {
		this.faultTable = faultTable;
	}
	
	public void addColorerChangeListener(ColorerChangeListener l) {
		listeners.add(l);
	}
	
	public boolean removeColorerChangeListener(ColorerChangeListener l) {
		return listeners.remove(l);
	}
	
	public FaultColorer getSelectedColorer() {
		String selected = colorerParam.getValue();
		if (selected.equals(COLORER_SELECTOR_CUSTOM))
			return null;
		else
			return colorers.get(ListUtils.getIndexByName(colorers, selected));
	}
	
	public void setSelectedColorer(FaultColorer colorer) {
		if (colorer == null){
			colorerParam.setValue(COLORER_SELECTOR_CUSTOM);
		}
		else
			colorerParam.setValue(colorer.getName());
		colorerParam.getEditor().refreshParamEditor();
	}
	
	private boolean isCPTBasedSelected() {
		FaultColorer selected = getSelectedColorer();
		if (selected == null)
			return false;
		return selected instanceof CPTBasedColorer;
	}
	
	private void updateForCPT() {
		boolean cptBased = isCPTBasedSelected();
		
		// note that this can be null!
		FaultColorer selected = getSelectedColorer();
		
		int paramCols = -1;
		if (cptBased)
			paramCols = ((CPTBasedColorer)selected).getParamColCount();
		if (paramCols < 1)
			paramCols = 2;
		
		// remove paramsEdit if any
		if (paramsEdit != null)
			this.remove(paramsEdit);
		if (selected != null) {
			ParameterList params = selected.getColorerParameters();
			if (params != null && params.size() > 0) {
				paramsEdit = new GriddedParameterListEditor(selected.getColorerParameters(), paramCols);
				this.add(paramsEdit);
			}
		}
		
		CPT cpt;
		if (cptBased) {
			CPTBasedColorer cptColor = (CPTBasedColorer)selected;
			logCheck.setSelected(cptColor.isCPTLog());
			cpt = cptColor.getCPT();
		} else {
			cpt = null;
			cptBased = false;
		}
		
		browseButton.setEnabled(cptBased);
		rescaleButton.setEnabled(cptBased);
		logCheck.setEnabled(cptBased);
		cptPanel.setEnabled(cptBased);
		if (cptBased) {
			cptPanel.update(cpt, cpt_image_width, cpt_image_height, cpt_num_ticks, cpt_tick_width);
		} else {
			cptPanel.updateCPT(null);
		}
		legendCheckbox.setEnabled(cptBased);
		cptPanel.repaint();
//		System.out.println("Updated cptBased="+cptBased);
		updateLegendIfVisible();
	}
	
	public void cptChangedExternally() {
		updateForCPT();
		fireColorerChangeEvent();
	}
	
	public void fireColorerChangeEvent() {
		for (ColorerChangeListener l : listeners) {
			l.colorerChanged(getSelectedColorer());
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == colorerParam) {
			updateForCPT();
			fireColorerChangeEvent();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == browseButton) {
			if (chooser == null) {
				chooser = new JFileChooser();
				String s = File.separator;
				try {
					File defaultDir = new File(MainGUI.getCWD().getParentFile(),
							"OpenSHA"+s+"src"+s+"resources"+s+"cpt");
					if (defaultDir.exists())
						chooser.setCurrentDirectory(defaultDir);
				} catch (Exception e1) {}
			}
			int retVal = chooser.showOpenDialog(this);
			if (retVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();
				try {
					CPT cpt = CPT.loadFromFile(selectedFile);
					CPTBasedColorer colorer = (CPTBasedColorer)getSelectedColorer();
					int ret = JOptionPane.showConfirmDialog(this, "Is this CPT file already in Log10 Space?",
							"Log10 CPT?", JOptionPane.YES_NO_OPTION);
					boolean cptLog = ret == JOptionPane.YES_OPTION;
					colorer.setCPT(cpt, cptLog);
					updateForCPT();
					fireColorerChangeEvent();
				} catch (IOException e1) {
					e1.printStackTrace();
					String title = "Error loading CPT";
					String msg = "The selected CPT file\n" +
							"couldn't be loaded!";
					JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (e.getSource() == rescaleButton) {
			CPTBasedColorer colorer = (CPTBasedColorer)getSelectedColorer();
			CPT newCPT = showRescaleDialog(colorer);
			colorer.setCPT(newCPT);
			updateForCPT();
			fireColorerChangeEvent();
		} else if (e.getSource() == logCheck) {
			CPTBasedColorer colorer = (CPTBasedColorer)getSelectedColorer();
			boolean log = logCheck.isSelected();
			CPT cpt = colorer.getCPT();
			while (log && cpt != null && cpt.getMinValue() <= 0) {
				JOptionPane.showMessageDialog(this, "The currently selected CPT has values <= 0." +
						"\nPlease rescale such that all values are >0.", "Invalid CPT for Log Scaling",
						JOptionPane.INFORMATION_MESSAGE);
				cpt = showRescaleDialog(colorer);
			}
			colorer.setCPT(cpt);
			colorer.setCPTLog(log);
			updateForCPT();
			fireColorerChangeEvent();
		} else if (e.getSource() == visibilityButton && faultTable != null) {
			faultTable.toggleSelectedVisibility();
//			faultTable.setColorForSelected(Color.GREEN);
		} else if (e.getSource() == colorButton && faultTable != null){
			Color color = colorChooser.getColor();
			if(color != null)
				faultTable.setColorForSelected(color);
		} else if (e.getSource() == legendCheckbox) {
			if (legendCheckbox.isSelected()) {
				if (legendCheckbox.isEnabled())
					addLegendScalarBar();
			} else {
				removeLegend();
			}
		}
	}

	private CPT showRescaleDialog(CPTBasedColorer colorer) {
		CPT cpt = colorer.getCPT();
		double min = cpt.getMinValue();
		double max = cpt.getMaxValue();
		rangeSelectMin.setValue(min);
		rangeSelectMin.getEditor().refreshParamEditor();
		rangeSelectMax.setValue(max);
		rangeSelectMax.getEditor().refreshParamEditor();
		
		boolean first = true;
		while (first || min >= max) {
			if (!first)
				JOptionPane.showMessageDialog(this, "min cannot be >= max!", "Error", JOptionPane.ERROR_MESSAGE);
			int selection = JOptionPane.showConfirmDialog(this, rangeSelectEditor,
					rangeSelectEditor.getTitle(), JOptionPane.OK_CANCEL_OPTION);
			if (selection != JOptionPane.OK_OPTION)
				return cpt;
			
			min = rangeSelectMin.getValue();
			max = rangeSelectMax.getValue();
			
			first = false;
		}
		
		return cpt.rescale(min, max);
	}
	
	public static void main(String args[]) throws IOException {
//		CPT cpt = SlipRateColorer.getDefaultCPT();
//		cpt.get(0).start = 0.1f;
//		cpt.asLog10().writeCPTFile(new File("/tmp/log.cpt"));
		SlipRateColorer slip = new SlipRateColorer();
		ArrayList<FaultColorer> colorers = new ArrayList<FaultColorer>();
		colorers.add(slip);
		
		ColorerPanel cp = new ColorerPanel(null, colorers, slip);
		
		JFrame frame = new JFrame();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 200);
		
		frame.setContentPane(cp);
		
		frame.setVisible(true);
	}

	@Override
	public void colorerChanged(FaultColorer newColorer) {
		updateForCPT();
		fireColorerChangeEvent();
	}
	
	private void updateLegendIfVisible() {
		if (legendCheckbox.isSelected() && legendCheckbox.isEnabled())
			addLegendScalarBar();
	}
	
	private void addLegendScalarBar() {
		PluginActors actors = plugin.getPluginActors();
		vtkActor2D prevActor = null;
		double x = 0.05;
		double y = 0.05;
		if (legend != null) {
			// duplicate - first remove old one but keep same position/size
			prevActor = legend.getActor();
			double[] position = prevActor.GetPosition();
			x = position[0];
			y = position[1];
			actors.removeLegend(legend);
		}
		FaultColorer fc = getSelectedColorer();
		CPTBasedColorer cptColor = (CPTBasedColorer)fc;
		CPT cpt = cptColor.getCPT();
		String title = fc.getLegendLabel();
		if (title == null)
			title = colorerParam.getValue();
		legend = LegendUtils.buildColorBarLegend(plugin, cpt, title, x, y);
		if (prevActor != null) {
			// set size
			vtkActor2D newActor = legend.getActor();
			newActor.SetWidth(prevActor.GetWidth());
			newActor.SetHeight(prevActor.GetHeight());
			// set color
			newActor.GetProperty().SetColor(prevActor.GetProperty().GetColor());
		}
		actors.addLegend(legend);
		MainGUI.updateRenderWindow();
	}
	
	private void removeLegend() {
		if (legend != null) {
			plugin.getPluginActors().removeLegend(legend);
			MainGUI.updateRenderWindow();
		}
	}
	
	public boolean isLegendVisible() {
		return legendCheckbox.isSelected();
	}
	
	public void setLegendVisible(boolean visible) {
		if (visible != legendCheckbox.isSelected()) {
			legendCheckbox.setSelected(visible); // does not trigger action event
			if (visible)
				updateLegendIfVisible();
			else
				removeLegend();
		}
	}
}
