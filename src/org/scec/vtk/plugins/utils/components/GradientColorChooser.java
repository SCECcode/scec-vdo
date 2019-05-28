package org.scec.vtk.plugins.utils.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Custom color chooser class that is a modal color selction window.
 * <code>GradientColorChooser</code>s extract the
 * swatch color panel from a <code>JColorChooser</code> and provide the means
 * to select single color or two-color gradient.
 * 
 * Created on Jan 30, 2005
 * 
 * @author P. Powers
 * @version $Id: GradientColorChooser.java 20 2005-05-04 19:44:40Z pmpowers $
 */
public class GradientColorChooser extends JDialog implements 
        ActionListener,
        ChangeListener {
    
    private static final long serialVersionUID = 1L;
	private JColorChooser colorChooser;
    private DefaultColorSelectionModel selectionModel;
    private Color inColor1;
    private Color inColor2;
    private Color outColor1 = Color.WHITE;
    private Color outColor2 = Color.BLACK;
    private JPanel buttonPanel;
    private JLabel gradientLabel;
    private ColorWellIcon gradient;
    private ColorWellToggleButton gradButt_1;
    private ColorWellToggleButton gradButt_2;
    private JButton cancel_button;
    private JButton ok_button;
    
    /** Flag used to determine if window was closed or cancelled by user. */
    protected boolean cancelled;
    
    /**
     * Constructs a new <code>GradientColorChooser</code> with a given <code>owner</code>.
     * 
     * @param owner <code>Component</code> that owns/calls the chooser
     */
    public GradientColorChooser(Component owner) {
        
        super(JOptionPane.getFrameForComponent(owner),"Catalog Color Picker",true);
        
        // set up color chooser
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                GradientColorChooser.this.cancelled = true;
            }
        });
        Container content = this.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        this.selectionModel = new DefaultColorSelectionModel();
        this.selectionModel.addChangeListener(this);
        this.colorChooser = new JColorChooser(this.selectionModel);
        
        // get swatch panel from JColorChooser
        AbstractColorChooserPanel swatchPanel = this.colorChooser.getChooserPanels()[0];
        swatchPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        // button panel components
        this.ok_button = new JButton("OK");
        this.ok_button.addActionListener(this);
        this.ok_button.setEnabled(false);
        this.getRootPane().setDefaultButton(this.ok_button);
        
        this.cancel_button = new JButton("Cancel");
        this.cancel_button.addActionListener(this);

        this.gradButt_1 = new ColorWellToggleButton(this.outColor1, 18, 18);
        this.gradButt_1.addActionListener(this);
        this.gradButt_2 = new ColorWellToggleButton(this.outColor2, 18, 18);
        this.gradButt_2.addActionListener(this);
        this.gradient = new ColorWellIcon(this.outColor1, this.outColor2, 140, 10, 0);
        this.gradientLabel = new JLabel(this.gradient);
        
        // build button panel
        this.buttonPanel = new JPanel();
        this.buttonPanel.setLayout(new BoxLayout(this.buttonPanel, BoxLayout.LINE_AXIS));
        this.buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        this.buttonPanel.add(Box.createHorizontalGlue());
        this.buttonPanel.add(this.gradButt_1);
        this.buttonPanel.add(Box.createHorizontalStrut(6));
        this.buttonPanel.add(this.gradientLabel);
        this.buttonPanel.add(Box.createHorizontalStrut(6));
        this.buttonPanel.add(this.gradButt_2);
        this.buttonPanel.add(Box.createHorizontalGlue());
        this.buttonPanel.add(this.cancel_button);
        this.buttonPanel.add(this.ok_button);

        // assemble frame
        content.add(swatchPanel);
        content.add(new JSeparator());
        content.add(this.buttonPanel);
        
        // pack it up
        pack();
        setResizable(false);
        
        // move over owner
        setLocationRelativeTo(owner);
    }
    
    private void processColorToIcons(Color c) {
        
        // color selection button behavior is set such that one
        // or no buttons can be selected, but not both;
        // includes some repaint calls that are redundant if buttons
        // are setSelected(true) in actionPerformed() which also
        // repaints
        
        boolean b1 = this.gradButt_1.isSelected();
        boolean b2 = this.gradButt_2.isSelected();
        
        if (b1) {
            this.outColor1 = c;
            this.gradButt_1.setColor(c);
            this.gradient.setColor(c,this.outColor2);
        } else if (b2) {
            this.outColor2 = c;
            this.gradButt_2.setColor(c);
            this.gradient.setColor(this.outColor1,c);
        } else {
            this.outColor1 = c;
            this.outColor2 = c;
            this.gradButt_1.setColor(c);
            this.gradButt_2.setColor(c);
            this.gradient.setColor(c);
        }

        // need to repaint icon; buttons repaint themselves
        this.gradientLabel.repaint();
    }
    
    /**
     * Displays this color chooser and sets the specified "active" colors <code>c1</code> and
     * <code>c2</code>. The window is modal so method returns new colors or null if color
     * changes are accepted or cancelled, respectively.
     * 
     * @param c1 color to set left side of gradient icon and button
     * @param c2 color to set right side of gradient icon and button
     * @return array containing two <code>Color</code>s or null
     */
    public Color[] getColors(Color c1, Color c2) {
        
        // initialize chooser
        this.inColor1 = c1;
        this.outColor1 = c1;
        this.gradButt_1.setColor(c1);
        this.inColor2 = c2;
        this.outColor2 = c2;
        this.gradButt_2.setColor(c2);
        this.gradient.setColor(c1,c2);
        
        // run modal chooser window
        this.cancelled = false;
        setVisible(true);
        
        // deselect color buttons
        this.gradButt_1.setSelected(false);
        this.gradButt_2.setSelected(false);
        
        if (this.cancelled) {
            return null;
        }
        
        return new Color[] {this.outColor1, this.outColor2};
    }

    
    /**
     * Required event-handler method that responds to color selections.
     * 
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
        Object src = e.getSource();
        if (src == this.selectionModel) {
            Color c = ((DefaultColorSelectionModel)src).getSelectedColor();
            processColorToIcons(c);
            // if changed colors revert to originals disable "OK"
            this.ok_button.setEnabled(!(this.inColor1.equals(this.outColor1) && 
                    this.inColor2.equals(this.outColor2)));
            // after color is selected, color panel has focus and default button
            // action (return key) doesn't work; solution: refocus button panel
            this.buttonPanel.requestFocus();
        }
    }
    
    /**
     * Required event-handler method that processes user interaction with gui buttons.
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        
        // set color selection buttons such that one or
        // none can be selected at a time
        if (src == this.gradButt_1) {
            if (this.gradButt_1.isSelected()) {
                this.gradButt_2.setSelected(false);
            }
        }
        
        else if (src == this.gradButt_2) {
            if (this.gradButt_2.isSelected()) {
                this.gradButt_1.setSelected(false);
            }
        }
        
        else if (src == this.cancel_button) {
            this.cancelled = true;
            setVisible(false);
        }
        
        else if (src == this.ok_button) {
        	setVisible(false);
        }
    }
    
}
