package org.scec.vtk.plugins.utils.components;

import java.awt.event.FocusEvent;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;


/**
 * Custom <code>JFormattedTextField</code> used for displaying/collecting integer numeric
 * data.
 * 
 * Created on Mar 2, 2005
 * 
 * @author P. Powers
 * @version $Id: IntegerTextField.java 57 2005-05-18 23:21:56Z pmpowers $
 */
public class IntegerTextField extends JFormattedTextField {

    private static final long serialVersionUID = 1L;

	/**
     * Constructs a new <code>IntegerTextField</code> with the given maximum digits,
     * whether grouping is used (eg comma separation), and column width.
     * width.
     * 
     * @param maxDigits the maximum number of digits to display
     * @param grouped whether grouping is used for displayed values
     * @param columns the width of the text field
     */
    public IntegerTextField(int maxDigits, boolean grouped, int columns) {
        super();
        this.setColumns(columns);
        
        NumberFormat numFormat = NumberFormat.getIntegerInstance();
        numFormat.setMaximumIntegerDigits(maxDigits);
        numFormat.setGroupingUsed(grouped);

        DefaultFormatterFactory formatFactory = new DefaultFormatterFactory(
                new NumberFormatter(numFormat));
        this.setFormatterFactory(formatFactory);
    }

    /**
     * Returns the <code>String</code> value of this text field as an <code>Integer</code>.
     * 
     * @return the <code>Integer</code> value of this text field
     */
    public Integer getIntegerValue() {
        try {
            return Integer.valueOf(this.getText());
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
