package org.scec.vtk.plugins.opensha.ucerf3Rups;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumSet;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.dom4j.Document;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.util.XMLUtils;
import org.opensha.commons.param.editor.impl.GriddedParameterListEditor;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.utils.FaultSectionDataWriter;
import scratch.UCERF3.utils.FaultSystemIO;

public class RuptureInfoViewSave extends JFrame implements ParameterChangeListener {
	
	private FaultSystemRupSet rupSet;
	private FaultSystemSolution sol;
	
	private static final String CHOOSE_PARAM_NAME = "View Type";
	private enum Choice {
		SECTIONS_ASCII("Fault Section Data (ASCII)"),
		SECTIONS_XML("Fault Section Data (XML)"),
		RUPTURES("Rupture Information"),
		RUPTURES_AND_RATES("Rupture/Rates Information");
		
		private String name;
		private Choice(String name) {
			this.name = name;
		}
		public String toString() {
			return name;
		}
	}
	private EnumParameter<Choice> chooseParameter;
	
	private static final String SAVE_PARAM_NAME = "Data Files";
	private static final String SAVE_PARAM_BUTTON_TEXT = "Save";
	private ButtonParameter saveParam;
	
	private ParameterList params = new ParameterList();
	
	private JTextArea area;
	
	private JFileChooser fileChoose;
	
	public RuptureInfoViewSave(FaultSystemRupSet rupSet, FaultSystemSolution sol) {
		JPanel panel = new JPanel(new BorderLayout());
		
		this.rupSet = rupSet;
		this.sol = sol;
		
		chooseParameter = new EnumParameter<RuptureInfoViewSave.Choice>(CHOOSE_PARAM_NAME,
				EnumSet.allOf(Choice.class), Choice.SECTIONS_ASCII, null);
		params.addParameter(chooseParameter);
		chooseParameter.addParameterChangeListener(this);
		
		saveParam = new ButtonParameter(SAVE_PARAM_NAME, SAVE_PARAM_BUTTON_TEXT);
		params.addParameter(saveParam);
		saveParam.addParameterChangeListener(this);
		
		GriddedParameterListEditor paramEdit = new GriddedParameterListEditor(params);
		
		panel.add(paramEdit, BorderLayout.NORTH);
		
		area = new JTextArea();
		area.setEditable(false);
		panel.add(new JScrollPane(area), BorderLayout.CENTER);
		
		updateTextArea();
		
		this.setContentPane(panel);
		this.setSize(1000, 800);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
	
	private void updateTextArea() {
		Choice choice = chooseParameter.getValue();
		
		switch (choice) {
		case SECTIONS_ASCII:
			StringBuffer ascii = FaultSectionDataWriter.getSectionsASCII(rupSet.getFaultSectionDataList(), null, false);
			area.setText(ascii.toString());
			break;
		case RUPTURES:
			StringBuffer rups = FaultSectionDataWriter.getRupsASCII(rupSet);
			area.setText(rups.toString());
			break;
		case RUPTURES_AND_RATES:
			if (sol != null) {
				StringBuffer rupsAndRates = FaultSectionDataWriter.getRupsASCII(rupSet, sol);
				area.setText(rupsAndRates.toString());
			} else {
				JOptionPane.showMessageDialog(null, "A solution must be loaded to view rupture rates!",
						"No Solution Loaded!", JOptionPane.ERROR_MESSAGE);
			}
			break;
		case SECTIONS_XML:
			Document doc = XMLUtils.createDocumentWithRoot();
			FaultSystemIO.fsDataToXML(doc.getRootElement(), FaultSectionPrefData.XML_METADATA_NAME+"List", rupSet);
			String text = "";
			try {
				text = XMLUtils.getDocumentAsString(doc);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error: "+e, "Error Creating XML!", JOptionPane.ERROR_MESSAGE);
			}
			area.setText(text);
			break;

		default:
			break;
		}
		
		area.setCaretPosition(0);
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getParameter() == chooseParameter){
			updateTextArea();
		} else if (event.getParameter() == saveParam){
			if (fileChoose == null)
				fileChoose = new JFileChooser();
			
			int ret = fileChoose.showSaveDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = fileChoose.getSelectedFile();
				
				try {
					FileWriter fw = new FileWriter(file);
					fw.write(area.getText());
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "Error: "+e, "Error Saving File!", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

}
