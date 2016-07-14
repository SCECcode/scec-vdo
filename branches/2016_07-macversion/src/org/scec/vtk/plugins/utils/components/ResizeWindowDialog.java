package org.scec.vtk.plugins.utils.components;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.scec.vtk.main.Info;
import org.scec.vtk.main.MainGUI;
import org.scec.vtk.tools.Prefs;


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
public class ResizeWindowDialog extends JDialog implements ActionListener {
	// TODO SJD consider renaming to something more domain-specific ... EarthquakeNotesDialog?
	private static final long serialVersionUID = 1L;

	private JLabel heightLabel    = new JLabel();
	private JTextField heightText = new JTextField();
	private JLabel widthLabel    = new JLabel();
	private JTextField widthText = new JTextField();

	private JButton ok = new JButton("OK");
	private JButton cancel = new JButton("Cancel");
	protected boolean cancelled;
	JComboBox sizeList ;
	Dimension oldSize;
	private TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"");
    

	public ResizeWindowDialog(Component owner) {
		super(JOptionPane.getFrameForComponent(owner),false);

		this.setSize(100,200);
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				ResizeWindowDialog.this.cancelled = true;
			}
		});

		this.setResizable(false);
		this.setTitle("Resize Render Window");

		// text fields
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10,10,0,10), this.border));

		this.heightLabel.setText("Height:");
		this.widthLabel.setText("Width:");
		
		String[] defaultSize={"Select...","720p","1080p","Default"};
	    sizeList = new JComboBox(defaultSize);
		sizeList.setSelectedIndex(1);
		sizeList.addActionListener(this);

		textPanel.add(this.heightLabel);//, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, a_l, f, new Insets(10,10,0,10), 0, 0 ));
		textPanel.add(this.heightText);//, new GridBagConstraints( 0, 1, 2, 1, 0.0, 0.0, a_l, h, new Insets(3,10,0,10), 0, 0 ));
		textPanel.add(this.widthLabel);//, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, a_l, f, new Insets(14,10,0,0), 0, 0 ));
		textPanel.add(this.widthText);//, new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0, a_r, f, new Insets(14,10,0,10), 0, 0 ));
		textPanel.add(this.sizeList);//, new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0, a_r, f, new Insets(14,10,0,10), 0, 0 ));

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
		oldSize = MainGUI.getRenderWindow().getComponent().getSize();
		heightText.setText(Integer.toString((int)oldSize.getHeight()));
		widthText.setText(Integer.toString((int)oldSize.getWidth()));
		
		// move over owner
		this.setLocationRelativeTo(owner);
		this.setVisible(true);
	}



	/**
	 * Returns whether user closed or cancelled the info window. Method typically
	 * used to abort import processes.
	 * 
	 * @return whether the window was closed
	 */
	public boolean windowWasCancelled() {
		return this.cancelled;
	}


	/**
	 * Required method to handle user actions
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src ==sizeList)
		{
			String option = (String)sizeList.getSelectedItem();
			System.out.println(option);
			if(option =="1080p" )
			{
			heightText.setText("1080");
			widthText.setText("1920");
			}
			if(option =="720p" )
			{
				heightText.setText("576");
				widthText.setText("720");
			}
			else
				if(option=="Default")
				{
					heightText.setText(Integer.toString((int)Prefs.getMainHeight()));
					widthText.setText(Integer.toString((int)Prefs.getMainWidth()));
				}
			MainGUI.getRenderWindow().getComponent().setSize(Integer.parseInt(widthText.getText()), Integer.parseInt(heightText.getText()));
		}
		if (src == this.ok) {
			MainGUI.getRenderWindow().getComponent().setSize(Integer.parseInt(widthText.getText()), Integer.parseInt(heightText.getText()));
		} else if (src == this.cancel) {
			this.cancelled = true;
			MainGUI.getRenderWindow().getComponent().setSize(oldSize);
			
		}
		Info.getMainGUI().repaint();
		this.setVisible(false);
	}
}

