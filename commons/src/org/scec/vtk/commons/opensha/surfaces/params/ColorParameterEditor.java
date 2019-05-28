package org.scec.vtk.commons.opensha.surfaces.params;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;

import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.editor.AbstractParameterEditor;
import org.scec.vtk.plugins.utils.components.ColorWellButton;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;

public class ColorParameterEditor extends AbstractParameterEditor<Color> implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ColorWellButton colorButton;
	private SingleColorChooser colorChooser;
	
	public ColorParameterEditor(ColorParameter colorParam) {
		super(colorParam);
		
		getColorButton();
		
		colorButton.addActionListener(this);
	}
	
	private ColorWellButton getColorButton() {
		if (colorButton == null)
			colorButton = new ColorWellButton(Color.GRAY, 80, 20);
		return colorButton;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		colorButton.setEnabled(isEnabled);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == colorButton) {
			if (colorChooser == null)
				colorChooser = new SingleColorChooser(this);
			Color newColor = colorChooser.getColor();
			if (newColor != null) {
				setValue(newColor);
				colorButton.setColor(newColor);
			}
		}
	}

	@Override
	public boolean isParameterSupported(Parameter<Color> param) {
		if (param == null)
			return false;
		return true;
	}

	@Override
	protected JComponent buildWidget() {
		ColorWellButton button = getColorButton();
		button.setColor(getParameter().getValue());
		return getColorButton();
	}

	@Override
	protected JComponent updateWidget() {
		return buildWidget();
	}

}
