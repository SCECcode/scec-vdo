package org.scec.vtk.drawingTools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scec.vtk.plugins.utils.components.ColorWellButton;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;



public class DisplayAttributes extends JPanel implements ActionListener {

	private static final long serialVersionUID = -1L;
	
	private Color textColor = Color.WHITE;
	private Color coneColor = Color.WHITE;
	
	protected JPanel textPanel = new JPanel();
	protected JLabel showConeLabel = new JLabel("Show Cone:");
	protected JLabel showTextLabel = new JLabel("Show Label:");
	protected JLabel flattenTextLabel = new JLabel("Flatten Text:");
    protected JLabel coneColorLabel = new JLabel("Cone Color:");
    protected JLabel textColorLabel = new JLabel("Font Color:");
    protected JLabel textSizeLabel = new JLabel("Font Size:");
    protected JLabel strikeAngleText = new JLabel("Strike Angle:");
    protected JLabel rollAngleText = new JLabel("Roll Angle:");
//    protected JLabel conSizeLabel = new JLabel("Cone Size:");
    protected JCheckBox showConeCheckbox = new JCheckBox();
    protected JCheckBox showTextCheckbox = new JCheckBox();
    protected JCheckBox flattenTextCheckbox = new JCheckBox();
    protected ColorWellButton coneColorButton = new ColorWellButton(Color.WHITE, 16,16);
    protected ColorWellButton textColorButton = new ColorWellButton(Color.WHITE, 16,16);
    protected JFormattedTextField fontSizeField;
    protected JFormattedTextField strikeAngleField;
    protected JFormattedTextField rollAngleField;
	
	public DisplayAttributes() {
		//Font Size Field
		DecimalFormat formatInt = new DecimalFormat();
		formatInt.setMinimumIntegerDigits(1);
		formatInt.setMaximumFractionDigits(0);
		formatInt.setGroupingUsed(false);
		fontSizeField = new JFormattedTextField(formatInt);
		fontSizeField.setText("18");
		
		//Strike Angle Field
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setGroupingUsed(false);
		decimalFormat.setMinimumIntegerDigits(1);
		decimalFormat.setMaximumIntegerDigits(10);
		strikeAngleField = new JFormattedTextField(decimalFormat);
		strikeAngleField.setText("90");
		
		//Roll Angle Field
		DecimalFormat decimalFormat2 = new DecimalFormat();
		decimalFormat2.setGroupingUsed(false);
		decimalFormat2.setMinimumIntegerDigits(1);
		decimalFormat2.setMaximumIntegerDigits(10);
		rollAngleField = new JFormattedTextField(decimalFormat2);
		rollAngleField.setText("0");
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		JPanel enableLabelPanel = new JPanel();
		enableLabelPanel.setLayout(new BoxLayout(enableLabelPanel, BoxLayout.Y_AXIS));
		enableLabelPanel.add(showTextLabel);
		enableLabelPanel.add(Box.createVerticalStrut(5));
		enableLabelPanel.add(showConeLabel);
		enableLabelPanel.add(Box.createVerticalStrut(5));
		enableLabelPanel.add(flattenTextLabel);
		
		
		JPanel enableCheckboxPanel = new JPanel();
		enableCheckboxPanel.setLayout(new BoxLayout(enableCheckboxPanel, BoxLayout.Y_AXIS));
		enableCheckboxPanel.add(showTextCheckbox);
		enableCheckboxPanel.add(showConeCheckbox);
		enableCheckboxPanel.add(flattenTextCheckbox);
		
		JPanel anglePanel = new JPanel();
		anglePanel.setLayout(new BoxLayout(anglePanel, BoxLayout.X_AXIS));
		anglePanel.add(strikeAngleText);
		anglePanel.add(strikeAngleField);
		anglePanel.add(Box.createHorizontalGlue());
		anglePanel.add(rollAngleText);
		anglePanel.add(rollAngleField);
		
		
		JPanel textSettingPanel = new JPanel();
		textSettingPanel.setLayout(new BoxLayout(textSettingPanel, BoxLayout.X_AXIS));
		textSettingPanel.add(coneColorLabel);
		textSettingPanel.add(coneColorButton);
		textSettingPanel.add(Box.createHorizontalGlue());
		textSettingPanel.add(textColorLabel);
		textSettingPanel.add(textColorButton);
		textSettingPanel.add(Box.createHorizontalGlue());
		textSettingPanel.add(textSizeLabel);
		textSettingPanel.add(fontSizeField);
		
		JPanel labelPropertiesPanel = new JPanel();
		labelPropertiesPanel.setLayout(new BoxLayout(labelPropertiesPanel, BoxLayout.Y_AXIS));
		labelPropertiesPanel.add(textSettingPanel);
		textSettingPanel.add(Box.createVerticalGlue());
		labelPropertiesPanel.add(anglePanel);
		
		this.add(enableLabelPanel);
		this.add(enableCheckboxPanel);
		this.add(Box.createHorizontalGlue());
		this.add(labelPropertiesPanel);
		
		strikeAngleField.setPreferredSize(new Dimension(40, 20));
		strikeAngleField.setMaximumSize(new Dimension(40, 20));
		
		rollAngleField.setPreferredSize(new Dimension(40, 20));
		rollAngleField.setMaximumSize(new Dimension(40, 20));

		fontSizeField.setPreferredSize(new Dimension(40, 20));
		fontSizeField.setMaximumSize(new Dimension(40, 20));

		showTextCheckbox.setSelected(true);
		showConeCheckbox.setSelected(true);
		
		flattenTextCheckbox.setSelected(false);
		strikeAngleField.setEnabled(flattenTextCheckbox.isSelected());
		rollAngleField.setEnabled(flattenTextCheckbox.isSelected());
		
		showTextCheckbox.addActionListener(this);
		showConeCheckbox.addActionListener(this);
		textColorButton.addActionListener(this);
		coneColorButton.addActionListener(this);
		flattenTextCheckbox.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		if (src == textColorButton) {
	      	SingleColorChooser colorChooser = new SingleColorChooser(null);
	       	Color color = colorChooser.getColor();
	       	if(color!=null) {
	       		this.textColor = color;
	       		textColorButton.setColor(color);
	       	}
		}
		else if (src == coneColorButton) {
	      	SingleColorChooser colorChooser = new SingleColorChooser(null);
	       	Color color = colorChooser.getColor();
	       	if(color!=null) {
	       		this.coneColor = color;
	       		coneColorButton.setColor(coneColor);
	       	}
		}
		else if (src == showConeCheckbox) {
			coneColorLabel.setEnabled(showConeCheckbox.isSelected());
			coneColorButton.setEnabled(showConeCheckbox.isSelected());
		}
		else if (src == showTextCheckbox) {
			textColorLabel.setEnabled(showTextCheckbox.isSelected());
			textColorButton.setEnabled(showTextCheckbox.isSelected());
		}else if (src == flattenTextCheckbox) {
			//Disable/Enabled strike dip rake
			strikeAngleField.setEnabled(flattenTextCheckbox.isSelected());
			rollAngleField.setEnabled(flattenTextCheckbox.isSelected());
		}
	}
	
	public Color getTextColor() {
		return textColor;
	}
	
	public void setTextColor(Color color){
		this.textColor = color;
	}
	
	public Color getConeColor() {
		return coneColor;
	}
	
	public boolean isConeEnabled() {
		return showConeCheckbox.isSelected();
	}
	
	public void setConeEnabled(boolean cone) {
		showConeCheckbox.setSelected(cone);
	}
	
	public boolean isTextEnabled() {
		return showTextCheckbox.isSelected();
	}
	
	public void setTextEnabled(boolean cone) {
		showTextCheckbox.setSelected(cone);
	}
	
	public boolean isFlattened() {
		return flattenTextCheckbox.isSelected();
	}
	
	public void setFlatten(boolean flatten) {
		flattenTextCheckbox.setSelected(flatten);
	}
	
	public String getTextSize() {
		String fontSize;
		try {
			int fontTempSize = Integer.parseInt(fontSizeField.getText());
			if (fontTempSize <= 0) throw new NumberFormatException();
			fontSize = fontSizeField.getText();
		} catch (NumberFormatException e2) {
			fontSizeField.setText("18");
			fontSize = "18";
		}
		return fontSize;
	}
	
	public double getRollAngle(){
		double roll;
		try {
			String strikeAngle = rollAngleField.getText();
			roll = Double.parseDouble(strikeAngle);
		} catch (NumberFormatException e2) {
			rollAngleField.setText("0");
			roll = 0;
		}
		return roll;
	}
	
	public double getStrikeAngle(){
		double strike;
		try {
			String strikeAngle = strikeAngleField.getText();
			strike = Double.parseDouble(strikeAngle);
		} catch (NumberFormatException e2) {
			strikeAngleField.setText("90");
			strike = 90;
		}
		return strike;
	}
	
	public void setTestSize(String fontSize) {
		fontSizeField.setText(fontSize);
	}
}
