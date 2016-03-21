package org.scec.vtk.plugins.utils.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Custom color chooser class. A <code>SingleColorChooser</code> extracts the
 * swatch based panel from a <code>JColorChooser</code>.
 * 
 * Created on Jan 30, 2005
 * 
 * @author P. Powers
 * @version $Id: SingleColorChooser.java 421 2006-07-24 17:07:45Z rapp $
 */
public class SingleColorChooser extends JDialog implements ChangeListener,ActionListener {
    
    private static final long serialVersionUID = 1L;
	private JColorChooser colorChooser;
    private DefaultColorSelectionModel selectionModel;
    private JButton applyButton;
    private JButton cancelButton;
    
    private Color color;
    
    /**
     * Constructs a <code>SingleColorChooser</code> with a given <code>owner</code>.
     * 
     * @param owner <code>Component</code> that owns/calls the chooser
     */
    public SingleColorChooser(Component owner) {
        
        super(JOptionPane.getFrameForComponent(owner), "Color Picker", true);
        
        // set up color chooser
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());
        this.selectionModel = new DefaultColorSelectionModel();
        this.selectionModel.addChangeListener(this);
        this.colorChooser = new JColorChooser(this.selectionModel);
       
        // get swatch panel from JColorChooser
        AbstractColorChooserPanel swatchPanel = this.colorChooser.getChooserPanels()[0];
        swatchPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        
        // get apply button
        this.applyButton = new JButton("Apply");
        this.applyButton.addActionListener(this);
        this.cancelButton = new JButton("Cancel");
        this.cancelButton.addActionListener(this);
        
        JPanel buttonGroup = new JPanel();
        buttonGroup.setLayout(new BoxLayout(buttonGroup,BoxLayout.X_AXIS));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        
        // assemble frame
        content.add(swatchPanel,BorderLayout.CENTER);
        buttonGroup.add(cancelButton,BorderLayout.CENTER);
        buttonGroup.add(applyButton,BorderLayout.EAST);
        buttonPanel.add(buttonGroup,BorderLayout.EAST);
        content.add(buttonPanel,BorderLayout.SOUTH);
        
        // pack it up
        this.pack();
        this.setResizable(false);
        
        // move over owner
        setLocationRelativeTo(owner);
    }
    
    /**
     * Displays this color chooser. The window is modal and method always returns a 
     * color or null if window is closed.
     * 
     * @return selected <code>Color</code> or null
     */
    public Color getColor() {
        this.color = null;
        // run modal chooser window
        this.setVisible(true);
        return this.color;
    }

    /**
     * Required event-handler method that responds to color selections.
     * 
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == this.applyButton) {
            this.color = this.selectionModel.getSelectedColor();
            setVisible(false);
        }
        else if(src == this.cancelButton) {
        	setVisible(false);
        }
    }
    
    public void stateChanged(ChangeEvent e) {
    	Object src = e.getSource();
    	if(src == this.colorChooser) {
    		
    	}
    }
}
