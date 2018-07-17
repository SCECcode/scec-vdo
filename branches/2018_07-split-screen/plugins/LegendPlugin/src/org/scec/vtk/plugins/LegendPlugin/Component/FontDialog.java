package org.scec.vtk.plugins.LegendPlugin.Component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.scec.vtk.plugins.utils.components.ColorButton;
import org.scec.vtk.plugins.utils.components.SingleColorChooser;

//import org.scec.geo3d.plugins.utils.components.ColorButton;
//import org.scec.geo3d.plugins.utils.components.SingleColorChooser;

public class FontDialog extends JDialog implements ActionListener, ListSelectionListener
{
	// serialization compatibility
    private static final long serialVersionUID = 1L;
    
    private Component owner;
    
    private JLabel fontLabel;
    private JTextField fontField;
    private JList fontList;
    
    private JLabel styleLabel;
    private JTextField styleField;
    private JList styleList;
    
    private JLabel sizeLabel;
    private JTextField sizeField;
    private JList sizeList;
    
    private JLabel colorLabel;
    private ColorButton colorButton;
    
    private JLabel sample;
    
    private JButton okButton;
    private JButton cancelButton;
    
    private Font selectedFont;
    private Color selectedColor;
    
    protected boolean cancelled;
    protected boolean okay;
    
	public FontDialog(Component owner)
	{
		super();
		
		this.owner = owner;
		
		cancelled = false;
		selectedFont = new Font("Arial",Font.BOLD,12);
	    selectedColor = Color.WHITE;
	    
		this.setTitle("Font Chooser");
		this.setModal(true);
	    selectedFont = new Font("Arial",Font.PLAIN,12);
	    
		this.setLayout(new GridLayout(2,4));
		this.setSize(500,300);
		
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.addWindowListener(new 
        	WindowAdapter() 
	        {
	            public void windowClosing(WindowEvent we) 
	            {
	            	FontDialog.this.cancelled = true;
	            }
	        });

        this.setResizable(false);
		
		fontLabel = new JLabel("Font:");
		fontField = new JTextField("");
		fontField.addActionListener(this);
		fontList = new JList(getSystemFonts());
		fontList.setSelectedIndex(0);
		fontField.setText((String)fontList.getSelectedValue());
		fontList.addListSelectionListener(this);
		JScrollPane fontListScroll = new JScrollPane(fontList);
		JPanel fontPanel = new JPanel();
		fontPanel.setLayout(new BoxLayout(fontPanel,BoxLayout.Y_AXIS));
		fontPanel.setBorder(new EmptyBorder(5,0,0,0));
		fontPanel.add(fontField);
		fontPanel.add(fontListScroll);
		fontPanel.add(fontLabel);
		
		styleLabel = new JLabel("Style:");
		styleField = new JTextField("");
		styleField.addActionListener(this);
		styleList = new JList(getFontStyles());
		styleList.setSelectedIndex(0);
		styleField.setText((String)styleList.getSelectedValue());
		styleList.addListSelectionListener(this);
		JScrollPane styleListScroll = new JScrollPane(styleList);
		JPanel stylePanel = new JPanel();
		stylePanel.setLayout(new BoxLayout(stylePanel,BoxLayout.Y_AXIS));
		stylePanel.setBorder(new EmptyBorder(5,0,0,0));
		stylePanel.add(styleField);
		stylePanel.add(styleListScroll);
		stylePanel.add(styleLabel);
		
		sizeLabel = new JLabel("Font:");
		sizeField = new JTextField("");
		sizeField.addActionListener(this);
		sizeList = new JList(getFontSizes());
		sizeList.setSelectedIndex(0);
		sizeField.setText((String)sizeList.getSelectedValue());
		sizeList.addListSelectionListener(this);
		JScrollPane sizeListScroll = new JScrollPane(sizeList);
		JPanel sizePanel = new JPanel();
		sizePanel.setLayout(new BoxLayout(sizePanel,BoxLayout.Y_AXIS));
		sizePanel.setBorder(new EmptyBorder(5,0,0,0));
		sizePanel.add(sizeField);
		sizePanel.add(sizeListScroll);
		sizePanel.add(sizeLabel);
		
		colorLabel = new JLabel("");
	    colorButton = new ColorButton(this,"Select a Color");
	    colorButton.setEnabled(true);
	    JPanel colorPanel = new JPanel();
	    colorPanel.setLayout(new BoxLayout(colorPanel,BoxLayout.Y_AXIS));
	    colorPanel.setBorder(BorderFactory.createTitledBorder("Color"));
	    colorPanel.add(colorButton);
	    colorPanel.add(colorLabel);
		
	    sample = new JLabel("");
	    sample.setFont(selectedFont);
	    sample.setText(fontField.getText());
	    JPanel samplePanel = new JPanel();
	    samplePanel.setLayout(new BoxLayout(samplePanel,BoxLayout.Y_AXIS));
	    samplePanel.setBorder(BorderFactory.createTitledBorder("Sample"));
	    samplePanel.add(sample);
	    
	    okButton = new JButton("OK");
	    okButton.addActionListener(this);
	    cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(this);
	    JPanel buttonPanel = new JPanel();
	    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
	    buttonPanel.add(okButton);
	    buttonPanel.add(cancelButton);
		
	    this.add(fontPanel);
	    this.add(stylePanel);
	    this.add(sizePanel);
	    this.add(buttonPanel);
	    this.add(new JPanel());
	    this.add(colorPanel);
	    this.add(samplePanel);
	    this.add(new JPanel());
	   
	}
	
	private String[] getSystemFonts()
	{
		GraphicsEnvironment ge =
			GraphicsEnvironment.getLocalGraphicsEnvironment( );
		String[] fontNames = ge.getAvailableFontFamilyNames( );
		return fontNames;
	}
	
	private String[] getFontStyles()
	{
		String[] styles = {"Regular", "Italic", "Bold"};
		return styles;
	}
	
	private String[] getFontSizes()
	{
		String[] sizes = {"8","9","10","12","14","16","18","20",
						  "22","24","26","28","36","48","72"};
		return sizes;
	}
	
	private int getSelectedFontStyle()
	{
		String style = (String)styleList.getSelectedValue();
		if (style.equals("Regular"))
			return Font.PLAIN;
		else if (style.equals("Italic"))
			return Font.ITALIC;
		else
			return Font.BOLD;
	}
	
	public void valueChanged(ListSelectionEvent e)
	{
		Object source = e.getSource();
		
		selectedFont = new Font(fontField.getText(),getSelectedFontStyle(),
				               (new Integer(sizeField.getText()).intValue()));
		
		if (source == fontList)
		{
			fontField.setText((String)fontList.getSelectedValue());
		}
		else if (source == styleList)
		{
			styleField.setText((String)styleList.getSelectedValue());
		}
		else if (source == sizeList)
		{
			sizeField.setText((String)sizeList.getSelectedValue());
		}
		draw();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		
		selectedFont = new Font(fontField.getText(),getSelectedFontStyle(),
	               (new Integer(sizeField.getText()).intValue()));
		
		if (source == colorButton)
		{
			selectedColor = new SingleColorChooser(this).getColor();
			
			if (selectedColor != null) 
			{
				colorLabel.setBackground(selectedColor);
			}
		}
		else if (source == okButton)
		{
			selectedFont = new Font(fontField.getText(),getSelectedFontStyle(),
		               				(new Integer(sizeField.getText()).intValue()));
			cancelled = true;
			okay = true;
			this.hide();
		}
		else if (source == cancelButton)
		{
			cancelled = true;
			okay = false;
			this.hide();
		}
		else if (source == fontField)
		{
			for (int i = 0; i < fontList.getModel().getSize(); i++)
			{
				if (fontList.getModel().getElementAt(i).equals(fontField.getText()))
				{
					fontList.setSelectedIndex(i);
				}
			}
		}
		else if (source == styleField)
		{
			for (int i = 0; i < styleList.getModel().getSize(); i++)
			{
				if (styleList.getModel().getElementAt(i).equals(styleField.getText()))
				{
					styleList.setSelectedIndex(i);
				}
			}
		}
		else if (source == sizeField)
		{
			for (int i = 0; i < sizeList.getModel().getSize(); i++)
			{
				if (sizeList.getModel().getElementAt(i).equals(sizeField.getText()))
				{
					sizeList.setSelectedIndex(i);
				}
			}
			
		}
		draw();
	}
	
	private void draw()
	{
		sample.setFont(selectedFont);
		sample.setText(fontField.getText());
	}
	
	public Font getFont()
	{
		return selectedFont;
	}
	
	public Color getColor()
	{
		return selectedColor;
	}
	
	public boolean windowWasCancelled() 
    {
        return this.cancelled;
    }
	
	public boolean selectedOkay()
	{
		return this.okay;
	}
	
	public void deselect()
	{
		okay = false;
	}
	
	public void showDialog() 
	{
        setLocationRelativeTo(this.owner);
        this.setVisible(true);
    }
}
