package org.scec.vtk.plugins.utils.components;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.scec.vtk.plugins.utils.DataAccessor;


/**
 * Class provides a custom dialog for a user to enter citation, reference, and notes
 * for a particular earthquake source catalog. 
 * <br/>
 * <br/>
 * <font color="red">
 * TODO problems:<br/>
 *    -- drag and drop between TextComponents produces non fatal awt error.
 *</font>
 *
 * Created on Feb 23, 2005
 * 
 * @author P. Powers
 * @version $Id: ObjectInfoDialog.java 3428 2010-07-16 19:19:44Z kmilner $
 */
public class ObjectInfoDialog extends JDialog implements ActionListener {
// TODO SJD consider renaming to something more domain-specific ... EarthquakeNotesDialog?
    private static final long serialVersionUID = 1L;
	private static int a_l = GridBagConstraints.LINE_START;       // anchor left
    private static int a_r = GridBagConstraints.LINE_END;         // anchor right

    private static int f = GridBagConstraints.NONE;               // fill none
    private static int h = GridBagConstraints.HORIZONTAL;         // fill horizontal
    private static int b = GridBagConstraints.BOTH;               // fill both
    
    private JLabel nameLabel    = new JLabel();
    private JTextField nameText = new JTextField();
    private JTextField citText  = new JTextField();
    private JTextArea refText   = new JTextArea();
    private JTextArea notesText = new JTextArea();
    
    private JButton ok = new JButton("OK");
    private JButton cancel = new JButton("Cancel");
    private TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"");
    
    private DataAccessor obj;
    
    public boolean isOpen=false;
    
    /** 
     * Flag used to determine if window was closed or cancelled by user. Use
     * {@link ObjectInfoDialog#windowWasCancelled()} for access.
     */
    protected boolean cancelled;
    
    /**
     * Constructs a new info gathering dialog with a given <code>Component</code> owner.
     * 
     * @param owner parent component used for centering
     */
    public ObjectInfoDialog(Component owner) {
        super(JOptionPane.getFrameForComponent(owner),false);
       
        this.setSize(440,500);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                ObjectInfoDialog.this.cancelled = true;
            }
        });

        this.setResizable(false);
        this.setTitle("Object Information");
        
        // text fields
        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10,10,0,10), this.border));
        
        this.nameLabel.setText("Display name:");
        JLabel refLabel   = new JLabel("Reference:");
        JLabel refHelp    = new JLabel("[ full reference ]");
        JLabel citLabel   = new JLabel("Citation:");
        JLabel citHelp    = new JLabel("[ e.g. Shearer(2003) ]");
        JLabel notesLabel = new JLabel("Additional notes:");
        
        this.refText.setRows(5);
        this.refText.setLineWrap(true);
        this.refText.setWrapStyleWord(true);
        this.notesText.setLineWrap(true);
        this.notesText.setWrapStyleWord(true);

        JScrollPane refPane = new JScrollPane();
        refPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        refPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        refPane.setViewportView(this.refText);
        JScrollPane notesPane = new JScrollPane();
        notesPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        notesPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        notesPane.setViewportView(this.notesText);
        
        textPanel.add(  this.nameLabel, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, a_l, f, new Insets(10,10,0,10), 0, 0 ));
        textPanel.add(   this.nameText, new GridBagConstraints( 0, 1, 2, 1, 0.0, 0.0, a_l, h, new Insets(3,10,0,10), 0, 0 ));
        textPanel.add(        citLabel, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, a_l, f, new Insets(14,10,0,0), 0, 0 ));
        textPanel.add(         citHelp, new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0, a_r, f, new Insets(14,0,0,10), 0, 0 ));
        textPanel.add(    this.citText, new GridBagConstraints( 0, 3, 2, 1, 0.0, 0.0, a_l, h, new Insets(3,10,0,10), 0, 0 ));
        textPanel.add(        refLabel, new GridBagConstraints( 0, 4, 1, 1, 0.0, 0.0, a_l, f, new Insets(14,10,0,0), 0, 0 ));
        textPanel.add(         refHelp, new GridBagConstraints( 1, 4, 1, 1, 0.0, 0.0, a_r, f, new Insets(14,0,0,10), 0, 0 ));
        textPanel.add(         refPane, new GridBagConstraints( 0, 5, 2, 1, 0.0, 0.0, a_l, h, new Insets(3,10,0,10), 0, 0 ));
        textPanel.add(      notesLabel, new GridBagConstraints( 0, 6, 2, 1, 0.0, 0.0, a_l, f, new Insets(14,10,0,10), 0, 0 ));
        textPanel.add(       notesPane, new GridBagConstraints( 0, 7, 2, 1, 1.0, 1.0, a_l, b, new Insets(3,10,10,10), 0, 0 ));
        
        // buttons
        JPanel buttonPanel = new JPanel();
        this.ok.addActionListener(this);
        this.cancel.addActionListener(this);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4,10,10,10));
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(this.cancel);
        buttonPanel.add(this.ok);
        this.getRootPane().setDefaultButton(this.ok);

        JPanel content = new JPanel(new BorderLayout());
        content.add(textPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.PAGE_END);
        
        Container c  = this.getContentPane();
        c.add(content);
        
        // move over owner
        this.setLocationRelativeTo(owner);
    }
    
    
    
    /**
     * Brings up this modal dialog with fields for object reference information.
     * If user clicks "OK", new values are stored in the provided <code>DataAccessor</code>.
     * Given a new <code>DataAccessor</code>, fields will be empty.
     * 
     * @param object to edit
     * @param panelTitle title of this panel
     */
    public void showInfo(DataAccessor object, String panelTitle) {
        this.cancelled = false;
        this.obj = object;
        this.border.setTitle(" " + panelTitle + ": ");
        this.nameText.setEnabled(true);
        this.nameLabel.setEnabled(true);
        this.nameText.setText(this.obj.getDisplayName());
        this.citText.setText(this.obj.getCitation());
        this.refText.setText(this.obj.getReference());
        this.notesText.setText(this.obj.getNotes());
        this.setVisible(true);
        isOpen=true;
    }
    
    /**
     * Brings up this modal dialog with fields for object reference information.
     * Dialog assumes that information is to be applied to multiple objects; use getter
     * methods to gather reference info or determine if window was cancelled/closed.
     * 
     * @param panelTitle title of this panel
     */
    public void showInfo(String panelTitle) {
        this.cancelled = false;
        this.obj = null;
        this.border.setTitle(" " + panelTitle + ": ");
        this.nameText.setText("[ multiple files ]");
        this.citText.setText("");
        this.refText.setText("");
        this.notesText.setText("");
        this.nameText.setEnabled(false);
        this.nameLabel.setEnabled(false);
        this.setVisible(true);
        isOpen=true;
    }
    
    /**
     * Returns the current entry for an objects display name.
     * 
     * @return the display name text field value
     */
    public String getDisplayName() {
        return this.nameText.getText();
    }
    
    /**
     * Returns the current entry for an objects citation.
     * 
     * @return the citation text field value
     */
    public String getCitation() {
        return this.citText.getText();
    }
    
    /**
     * Returns the current entry for an objects reference.
     * 
     * @return the reference text field value
     */
    public String getReference() {
        return this.refText.getText();
    }
    
    /**
     * Returns the current entry for an objects notes.
     * 
     * @return the notes text field value
     */
    public String getNotes() {
        return this.notesText.getText();
    }

    /**
     * Returns whther user closed or cancelled the info window. Method typically
     * used to abort import processes.
     * 
     * @return whther the window was closed
     */
    public boolean windowWasCancelled() {
        return this.cancelled;
    }
    
    public boolean isOpen()
    {
    	return isOpen;
    }
    
    /**
     * Required method to handle user actions
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == this.ok && this.obj != null) {
            this.obj.setDisplayName(this.nameText.getText());
            this.obj.setCitation(this.citText.getText());
            this.obj.setReference(this.refText.getText());
            this.obj.setNotes(this.notesText.getText());
            isOpen=false;
        } else if (src == this.cancel) {
            this.cancelled = true;
            isOpen=false;
        }
        this.setVisible(false);
    }
}

