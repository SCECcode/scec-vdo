package org.scec.vtk.plugins.utils.components;

import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFormattedTextField;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;


/**
 * Custom <code>JFormattedTextField</code> used for displaying/collecting date
 * information. Field only allows date patterns that match the <code>SimpleDateFormat</code>
 * provided to the constructor.
 * 
 * Created on Mar 2, 2005
 * 
 * @author P. Powers
 * @version $Id: DateTextField.java 20 2005-05-04 19:44:40Z pmpowers $
 */
public class DateTextField extends JFormattedTextField {

    private static final long serialVersionUID = 1L;
	private SimpleDateFormat format;
    
    /**
     * Constructs a new <code>DateTextField</code> with the given pattern and column
     * width.
     * 
     * @param pattern the date format pattern to use
     * @param columns the width of the text field
     */
    public DateTextField(SimpleDateFormat pattern, int columns) {
        super();
        this.format = pattern;
        this.setColumns(columns);
        
        DateFormatter formatter = new DateFormatter(pattern);
        DefaultFormatterFactory formatFactory = new DefaultFormatterFactory(formatter);
        this.setFormatterFactory(formatFactory);
    }

    /**
     * Returns the <code>String</code> value of this <code>DateTextField</code> converted to a
     * <code>Date</code>.
     * 
     * @return the <code>Date</code> value of this text field
     */
    public Date getDateValue() {
        try {
            return this.format.parse(this.getText());
        } catch (ParseException pe) {
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
