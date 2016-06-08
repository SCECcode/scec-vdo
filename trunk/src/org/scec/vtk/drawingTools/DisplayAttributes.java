package org.scec.vtk.drawingTools;

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



public class DisplayAttributes extends JPanel implements ActionListener {

	private static final long serialVersionUID = -1L;
	
	protected JPanel textPanel = new JPanel();
	protected JLabel flattenTextLabel = new JLabel("Flatten Text:");
    protected JLabel textSizeLabel = new JLabel("Font Size:");
    protected JLabel strikeAngleText = new JLabel("Strike Angle:");
    protected JLabel rollAngleText = new JLabel("Roll Angle:");
    protected JLabel latText = new JLabel("Lat:");
    protected JLabel lonText = new JLabel("Lon:");
    protected JLabel altText = new JLabel("Alt:");
    protected JLabel rotateXText = new JLabel("Rotate X:");
    protected JLabel rotateYText = new JLabel("Rotate Y:");
    protected JLabel rotateZText = new JLabel("Rotate Z:");
//    protected JLabel conSizeLabel = new JLabel("Cone Size:");
    protected JCheckBox flattenTextCheckbox = new JCheckBox();
    protected JFormattedTextField fontSizeField;
    protected JFormattedTextField strikeAngleField;
    protected JFormattedTextField rotateXField;
    protected JFormattedTextField rotateYField;
    protected JFormattedTextField rotateZField;
    protected JFormattedTextField latField;
    protected JFormattedTextField lonField;
    protected JFormattedTextField altField;
    protected JFormattedTextField rollAngleField;
	
	public DisplayAttributes() {
		//Font Size Field
		DecimalFormat formatInt = new DecimalFormat();
		formatInt.setMinimumIntegerDigits(1);
		formatInt.setMaximumFractionDigits(0);
		formatInt.setGroupingUsed(false);
		fontSizeField = new JFormattedTextField(formatInt);
		fontSizeField.setText("18");
		
		//Rotate XYZ
				DecimalFormat rotateX = new DecimalFormat();
				rotateX.setGroupingUsed(false);
				rotateX.setMinimumIntegerDigits(1);
				rotateX.setMaximumIntegerDigits(10);
				rotateXField = new JFormattedTextField(rotateX);
				rotateXField.setText("90");
				
				DecimalFormat rotateY = new DecimalFormat();
				rotateY.setGroupingUsed(false);
				rotateY.setMinimumIntegerDigits(1);
				rotateY.setMaximumIntegerDigits(10);
				rotateYField = new JFormattedTextField(rotateY);
				rotateYField.setText("90");
				
				DecimalFormat rotateZ = new DecimalFormat();
				rotateZ.setGroupingUsed(false);
				rotateZ.setMinimumIntegerDigits(1);
				rotateZ.setMaximumIntegerDigits(10);
				rotateZField = new JFormattedTextField(rotateZ);
				rotateZField.setText("90");
				
				//lat Long and altitude
				DecimalFormat lat = new DecimalFormat();
				lat.setGroupingUsed(false);
				lat.setMinimumIntegerDigits(1);
				lat.setMaximumIntegerDigits(10);
				latField = new JFormattedTextField(lat);
				latField.setText("37");

				DecimalFormat lon = new DecimalFormat();
				lon.setGroupingUsed(false);
				lon.setMinimumIntegerDigits(1);
				lon.setMaximumIntegerDigits(10);
				lonField = new JFormattedTextField(lon);
				lonField.setText("-120");
				
				DecimalFormat alt = new DecimalFormat();
				alt.setGroupingUsed(false);
				alt.setMinimumIntegerDigits(1);
				alt.setMaximumIntegerDigits(10);
				altField = new JFormattedTextField(alt);
				altField.setText("0");
		
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
		enableLabelPanel.add(flattenTextLabel);
		
		
		JPanel enableCheckboxPanel = new JPanel();
		enableCheckboxPanel.setLayout(new BoxLayout(enableCheckboxPanel, BoxLayout.Y_AXIS));
		enableCheckboxPanel.add(flattenTextCheckbox);
		
		
		JPanel translatePanel = new JPanel();
		translatePanel.setLayout(new BoxLayout(translatePanel, BoxLayout.X_AXIS));
		translatePanel.add(latText);
		translatePanel.add(latField);
		translatePanel.add(Box.createHorizontalGlue());
		translatePanel.add(lonText);
		translatePanel.add(lonField);
		translatePanel.add(Box.createHorizontalGlue());
		translatePanel.add(altText);
		translatePanel.add(altField);
		
		JPanel rotatePanel = new JPanel();
		translatePanel.setLayout(new BoxLayout(translatePanel, BoxLayout.X_AXIS));
		rotatePanel.add(Box.createHorizontalGlue());
		rotatePanel.add(rotateXText);
		rotatePanel.add(rotateXField);
		rotatePanel.add(Box.createHorizontalGlue());
		rotatePanel.add(rotateYText);
		rotatePanel.add(rotateYField);
		rotatePanel.add(Box.createHorizontalGlue());
		rotatePanel.add(rotateZText);
		rotatePanel.add(rotateZField);
		
		JPanel anglePanel = new JPanel();
		anglePanel.setLayout(new BoxLayout(anglePanel, BoxLayout.X_AXIS));
		anglePanel.add(strikeAngleText);
		anglePanel.add(strikeAngleField);
		anglePanel.add(Box.createHorizontalGlue());
		anglePanel.add(rollAngleText);
		anglePanel.add(rollAngleField);
		
		
		JPanel textSettingPanel = new JPanel();
		textSettingPanel.setLayout(new BoxLayout(textSettingPanel, BoxLayout.X_AXIS));
		textSettingPanel.add(Box.createHorizontalGlue());
		textSettingPanel.add(textSizeLabel);
		textSettingPanel.add(fontSizeField);
		
		JPanel labelPropertiesPanel = new JPanel();
		labelPropertiesPanel.setLayout(new BoxLayout(labelPropertiesPanel, BoxLayout.Y_AXIS));
		labelPropertiesPanel.add(translatePanel);
		labelPropertiesPanel.add(rotatePanel);
		rotatePanel.add(Box.createVerticalGlue());
		labelPropertiesPanel.add(textSettingPanel);
		//anglePanel.add(Box.createVerticalGlue());
		//labelPropertiesPanel.add(anglePanel);
		
		this.add(Box.createVerticalGlue());
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
		
		latField.setPreferredSize(new Dimension(40, 20));
		latField.setMaximumSize(new Dimension(40, 20));
		
		lonField.setPreferredSize(new Dimension(40, 20));
		lonField.setMaximumSize(new Dimension(40, 20));
		
		altField.setPreferredSize(new Dimension(40, 20));
		altField.setMaximumSize(new Dimension(40, 20));
		
		rotateXField.setPreferredSize(new Dimension(40, 20));
		rotateXField.setMaximumSize(new Dimension(40, 20));
		
		rotateYField.setPreferredSize(new Dimension(40, 20));
		rotateYField.setMaximumSize(new Dimension(40, 20));
		
		rotateZField.setPreferredSize(new Dimension(40, 20));
		rotateZField.setMaximumSize(new Dimension(40, 20));
		
		flattenTextCheckbox.setSelected(false);
		strikeAngleField.setEnabled(flattenTextCheckbox.isSelected());
		rollAngleField.setEnabled(flattenTextCheckbox.isSelected());
		
		flattenTextCheckbox.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
 if (src == flattenTextCheckbox) {
			//Disable/Enabled strike dip rake
			//strikeAngleField.setEnabled(flattenTextCheckbox.isSelected());
			//rollAngleField.setEnabled(flattenTextCheckbox.isSelected());
		}
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
