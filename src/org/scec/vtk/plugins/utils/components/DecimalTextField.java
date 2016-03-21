package org.scec.vtk.plugins.utils.components;

import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;


/**
 * Custom <code>JFormattedTextField</code> used for displaying/collecting decimal numeric
 * data.
 * 
 * Created on Mar 2, 2005
 * 
 * @author P. Powers
 * @version $Id: DecimalTextField.java 20 2005-05-04 19:44:40Z pmpowers $
 */
public class DecimalTextField extends JFormattedTextField {
    
    private static final long serialVersionUID = 1L;
	private final String format = "0.0";
    
    /**
     * Constructs a new <code>DecimalTextField</code> with the given minimum and
     * maximum number values and column width
     * width.
     * 
     * @param maxInteger the maximum integer digits to show
     * @param maxDecimal the maximum decimal digits to show
     * @param columns the width of the text field
     */
    public DecimalTextField(int maxInteger, int maxDecimal, int columns) {
        super();
        this.setColumns(columns);
        
        DecimalFormat decFormat = (DecimalFormat)NumberFormat.getNumberInstance();
        decFormat.applyPattern(this.format);
        decFormat.setMaximumIntegerDigits(maxInteger);
        decFormat.setMaximumFractionDigits(maxDecimal);
        
        DefaultFormatterFactory formatFactory = new DefaultFormatterFactory(
                new NumberFormatter(decFormat));
        this.setFormatterFactory(formatFactory);
    }
    
    /**
     * Returns the <code>String</code> value of this text field as a <code>Float</code>.
     * 
     * @return the <code>Float</code> value of this text field
     */
    public Float getFloatValue() {
        try {
            return Float.valueOf(this.getText());
        }
        catch (NumberFormatException nfe) {
            return null;
        }
    }
    
    /**
     * Override allows text fields to be reset to empty (null). 
     * 
     * @see java.awt.Component#processFocusEvent(java.awt.event.FocusEvent)
     */
    protected void processFocusEvent(FocusEvent e) {
        if (e.getID() == FocusEvent.FOCUS_LOST) {
            if (getText().length() == 0)
                setValue(null);
        }
        super.processFocusEvent(e);
    }
    
}