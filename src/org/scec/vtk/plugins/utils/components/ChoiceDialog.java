package org.scec.vtk.plugins.utils.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * This class manages a dialog that presents a user with a <code>JComboBox</code> (editable
 * or uneditable) from which to make a choice for a file or directory name. A  <code>ChoiceDialog</code>
 * filters out invalid filenamme characters and replaces them with underscores (_) before
 * accepting a given file/directory name.
 *
 * Created on Mar 10, 2005
 * 
 * @author P. Powers
 * @version $Id: ChoiceDialog.java 20 2005-05-04 19:44:40Z pmpowers $
 */
public class ChoiceDialog extends JDialog implements 
    ActionListener {

    private static final long serialVersionUID = 1L;

	String input = null;

    JButton ok;
    JButton cancel;
    JComboBox choices;
    
    /**
     * Constructs a new <code>ChoiceDialog</code> for selecting/setting file or directory
     * names.
     * 
     * @param owner <code>Component</code> used for centering
     * @param title of dialog
     * @param modal whether to run modal
     * @param message1 primary informational message
     * @param message2 secondary message used/positioned for suggestions or hints
     * @param options <code>JComboBox</code> choices
     * @param optionsEditable whether chooser <code>JComboBox</code> is editable
     */
    public ChoiceDialog(
            Component owner,
            String title,
            boolean modal,
            String message1,
            String message2,
            String[] options,
            boolean optionsEditable) {
        
        super(JOptionPane.getFrameForComponent(owner), title, true);
        
        setResizable(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        
        JPanel content = new JPanel(new BorderLayout());
        
        JPanel buttonPanel = new JPanel();
        this.ok = new JButton("OK");
        getRootPane().setDefaultButton(this.ok);
        this.ok.addActionListener(this);
        this.cancel = new JButton("Cancel");
        this.cancel.addActionListener(this);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 20));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(this.cancel);
        buttonPanel.add(this.ok);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        JTextArea mssg = new JTextArea(message1);
        mssg.setOpaque(false);
        mssg.setLineWrap(true);
        mssg.setWrapStyleWord(true);
        mssg.setPreferredSize(new Dimension(400,42));
        mssg.setEditable(false);
        
        JPanel choicePanel = new JPanel();
        choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.LINE_AXIS));
        this.choices = new JComboBox(options);
        this.choices.setEditable(optionsEditable);
        this.choices.setSelectedIndex(-1);
        
        choicePanel.add(Box.createHorizontalGlue());
        choicePanel.add(new JLabel(message2));
        choicePanel.add(Box.createHorizontalStrut(6));
        choicePanel.add(this.choices);

        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        infoPanel.add(mssg, BorderLayout.CENTER);
        infoPanel.add(choicePanel, BorderLayout.PAGE_END);
        
        content.add(infoPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.PAGE_END);
        getContentPane().add(content);
        pack();
        setLocationRelativeTo(owner);
    }
    
    /**
     * Returns the currently displayed entry in this chooser's <code>JComboBox</code>.
     * 
     * @return the selected filename
     */
    public String getInput() {
        return this.input;
    }

    private boolean isValid(String name) {
        String initName = new String(name);
        name = name.replace(File.pathSeparatorChar, '_');
        name = name.replace(File.separatorChar, '_');
        name = name.replace('*', '_');
        name = name.replace(' ', '_');
        name = name.replace('(', '_');
        name = name.replace(')', '_');
        name = name.replace('\'', '_');
        name = name.replace('|', '_');
        name = name.replace('\"', '_');
        name = name.replace(';', '_');
        name = name.replace(':', '_');
        name = name.replace('>', '_');
        name = name.replace('<', '_');
        name = name.replace('[', '_');
        name = name.replace(']', '_');
        name = name.replace('&', '_');
        name = name.replace('#', '_');
        name = name.replace('@', '_');
        name = name.replace('$', '_');
        name = name.replace('%', '_');
        name = name.replace('^', '_');
        name = name.replace('=', '_');
        
        this.input = name;
        if (initName.equals(name)) {
            return true;
        }
        return false;
    }
    
    /**
     * Required event handler method.
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == this.ok) {
            if (isValid((String)this.choices.getEditor().getItem())) {
                setVisible(false);
            } else {
                this.choices.getEditor().setItem(this.input);
                return;
            }
        } else {
            setVisible(false);
        }
    }
}
