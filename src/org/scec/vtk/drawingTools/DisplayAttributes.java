package org.scec.vtk.drawingTools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DisplayAttributes extends JPanel implements ActionListener {

	private static final long serialVersionUID = -1L;

	protected JPanel textPanel = new JPanel();
	protected JLabel textSizeLabel = new JLabel("Label font Size:");
	protected JLabel latText = new JLabel("Latitude:");
	protected JLabel lonText = new JLabel("Longitude:");
	protected JLabel altText = new JLabel("Altitude:");
	protected JLabel coneSizeText = new JLabel("Cone pin height:");
	protected JLabel coneBaseRadiusText = new JLabel("Cone pin base radius:");

	protected JFormattedTextField fontSizeField;
	protected JFormattedTextField coneHeightField;
	protected JFormattedTextField coneBaseRadiusField;
	protected JFormattedTextField latField;
	protected JFormattedTextField lonField;
	protected JFormattedTextField altField;

	/*
	 * Constructor
	 */
	public DisplayAttributes() {
		// Font size text field
		DecimalFormat formatInt = new DecimalFormat();
		formatInt.setMinimumIntegerDigits(1);
		formatInt.setMaximumFractionDigits(0);
		formatInt.setGroupingUsed(false);
		fontSizeField = new JFormattedTextField(formatInt);
		fontSizeField.setText("21");

		// Cone height text field
		DecimalFormat formatConeHeight = new DecimalFormat();
		formatConeHeight.setGroupingUsed(false);
		formatConeHeight.setMinimumIntegerDigits(1);
		formatConeHeight.setMaximumIntegerDigits(10);
		coneHeightField = new JFormattedTextField(formatConeHeight);
		coneHeightField.setText("10");
		
		// Cone radius text field
		DecimalFormat formatConeRadius = new DecimalFormat();
		formatConeRadius.setGroupingUsed(false);
		formatConeRadius.setMinimumIntegerDigits(1);
		formatConeRadius.setMaximumIntegerDigits(10);
		coneBaseRadiusField = new JFormattedTextField(formatConeRadius);
		coneBaseRadiusField.setText("5");

		// Latitude text field
		DecimalFormat lat = new DecimalFormat();
		lat.setGroupingUsed(false);
		lat.setMinimumIntegerDigits(1);
		lat.setMaximumIntegerDigits(10);
		latField = new JFormattedTextField(lat);
		latField.setText("37");
		
		// Longitude text field
		DecimalFormat lon = new DecimalFormat();
		lon.setGroupingUsed(false);
		lon.setMinimumIntegerDigits(1);
		lon.setMaximumIntegerDigits(10);
		lonField = new JFormattedTextField(lon);
		lonField.setText("-120");
		
		// Altitude text field
		DecimalFormat alt = new DecimalFormat();
		alt.setGroupingUsed(false);
		alt.setMinimumIntegerDigits(1);
		alt.setMaximumIntegerDigits(10);
		altField = new JFormattedTextField(alt);
		altField.setText("0");
		
		this.coneBaseRadiusField.setEnabled(false);
		this.coneHeightField.setEnabled(false);
		this.latField.setEnabled(false);
		this.lonField.setEnabled(false);
		this.altField.setEnabled(false);
		this.fontSizeField.setEnabled(false);
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		JPanel enableLabelPanel = new JPanel();
		enableLabelPanel.setLayout(new BoxLayout(enableLabelPanel, BoxLayout.Y_AXIS));

		JPanel translatePanel = new JPanel();
		translatePanel.setLayout(new BoxLayout(translatePanel, BoxLayout.Y_AXIS));
		translatePanel.add(latText);
		translatePanel.add(latField);
		translatePanel.add(Box.createHorizontalGlue());
		translatePanel.add(lonText);
		translatePanel.add(lonField);
		translatePanel.add(Box.createHorizontalGlue());
		translatePanel.add(altText);
		translatePanel.add(altField);

		JPanel rotatePanel = new JPanel();
		rotatePanel.setLayout(new BoxLayout(rotatePanel, BoxLayout.Y_AXIS));
		rotatePanel.add(Box.createHorizontalGlue());
		rotatePanel.add(coneSizeText);
		rotatePanel.add(coneHeightField);
		rotatePanel.add(Box.createHorizontalGlue());
		rotatePanel.add(coneBaseRadiusText);
		rotatePanel.add(coneBaseRadiusField);

		JPanel textSettingPanel = new JPanel();
		textSettingPanel.setLayout(new BoxLayout(textSettingPanel, BoxLayout.Y_AXIS));
		textSettingPanel.add(Box.createHorizontalGlue());
		textSettingPanel.add(textSizeLabel);
		textSettingPanel.add(fontSizeField);

		JPanel labelPropertiesPanel = new JPanel();
		labelPropertiesPanel.setLayout(new BoxLayout(labelPropertiesPanel, BoxLayout.Y_AXIS));
		labelPropertiesPanel.add(translatePanel);
		labelPropertiesPanel.add(rotatePanel);
		rotatePanel.add(Box.createVerticalGlue());
		labelPropertiesPanel.add(textSettingPanel);

		this.add(Box.createVerticalGlue());
		this.add(enableLabelPanel);
		this.add(Box.createHorizontalGlue());
		this.add(labelPropertiesPanel);

		fontSizeField.setPreferredSize(new Dimension(40, 20));
		fontSizeField.setMaximumSize(new Dimension(40, 20));

		latField.setPreferredSize(new Dimension(40, 20));
		latField.setMaximumSize(new Dimension(40, 20));

		lonField.setPreferredSize(new Dimension(40, 20));
		lonField.setMaximumSize(new Dimension(40, 20));

		altField.setPreferredSize(new Dimension(40, 20));
		altField.setMaximumSize(new Dimension(40, 20));

		coneHeightField.setPreferredSize(new Dimension(40, 20));
		coneHeightField.setMaximumSize(new Dimension(40, 20));

		coneBaseRadiusField.setPreferredSize(new Dimension(40, 20));
		coneBaseRadiusField.setMaximumSize(new Dimension(40, 20));
	}
	
	/*
	 * @param Nothing
	 * @return String Size of the cone label's text
	 */
	public String getTextSize() {
		String fontSize;
		try {
			int fontTempSize = Integer.parseInt(fontSizeField.getText());
			if (fontTempSize <= 0) { // Invalid font size if <0
				throw new NumberFormatException(); // Go to the catch block
			}
			fontSize = fontSizeField.getText();
		} catch (NumberFormatException nfe) {
			fontSizeField.setText("21"); // If the number format is off, set it to default size (21)
			fontSize = "21";
		}
		return fontSize;
	}
	
	/*
	 * @param fontSize Font size to change cone label to
	 * @return nothing
	 */
	public void setTextSize(String fontSize) {
		fontSizeField.setText(fontSize);
	}
	
	// Never used
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// Auto-generated method stub
	}
}
