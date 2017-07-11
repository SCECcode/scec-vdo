package org.scec.vtk.plugins.LegendPlugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.scec.vtk.plugins.LegendPlugin.Component.FontDialog;
import org.scec.vtk.plugins.utils.components.ColorWellButton;
import org.scec.vtk.plugins.utils.components.GradientColorChooser;

public class CreateLegendsGUI extends JDialog implements ActionListener, ChangeListener, MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private JTable table;
	
	private JPanel buttonPane;
	private JButton okButton;
	private JButton cancelButton;
	private JScrollPane scrollPane;
	private JButton btnTextColor;
	private ColorWellButton btnColorSelect;
	private JButton btnAddtoList;
	private JButton btnRemove;
	private JButton btnClear;
	private JButton btnCreateLegend; 
	private JLabel lblCreateLegend;
	private JTextArea textArea;
	private JLabel lblInstructions; 
	private DefaultTableModel tableModel;
	private FontDialog fontGUI = new FontDialog(textField);
	private JColorChooser colorchooser;
	private JColorChooser colorchooser2;
	private Color btnColor;
	private Color btnColor2;
	private Graphics2D g2;
	private BufferedImage myImage;
	private JButton moveUp;
	private JButton moveDown;
	private JFileChooser saveFile = new JFileChooser();
	private File bobby = null;
	private JRadioButton colorText;
	private JRadioButton textColor;
	private JRadioButton gradientColor;
	private boolean bcolortext = false;
	private JLabel field;
	JTextField scalefield;
	JButton displaybutton;
	JSlider transparencyslider;
//	Overlay overlay;
	File outputfile = null;
	private JButton btnDisplayLegend;
	private JLabel colorLabel;
	private boolean bcolor = false;
	private boolean btextcolor = false;
	private boolean bgradientcolor = false;
	private boolean btransparent = false;
	JDialog color;
	private GradientColorChooser gradientChooser;
	private JButton up;
	private JButton down;
	
	private DefaultTableModel model;
	
	private JCheckBox bgtransparent;
	private JButton left;
	private JButton right;
	private ColorWellButton btnColorSelect2;
	private JLabel colorLabel2;
	
	/**
	 * Create the dialog.
	 * @param transparencySlider 
	 * @param displayButton 
	 * @param scaleField 
	 */
	public CreateLegendsGUI(JTextField scaleField, JButton displayButton, JSlider transparencySlider ) {
		scalefield = scaleField; 
		displaybutton =displayButton;
		transparencyslider= transparencySlider;
		fontGUI.setForeground(new Color(0,0,0));
		setBounds(100, 100, 510, 703);
		getContentPane().setLayout(null);
		{
			buttonPane = new JPanel();
			buttonPane.setBounds(0, 632, 475, 33);
			buttonPane.setLayout((LayoutManager) new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane);
			{
				okButton = new JButton("OK");
				okButton.addActionListener(this);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(this);
				buttonPane.add(cancelButton);
			}
		}
		scrollPane = new JScrollPane();
		scrollPane.setBounds(25, 52, 254, 301);
		getContentPane().add(scrollPane);
		
		tableModel = new DefaultTableModel();

		
		
		table = new JTable(tableModel){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int rowIndex, int vColIndex) {
		        return false;
		    }
		};
		table.setShowGrid(false);
		table.setIntercellSpacing(new Dimension(0,2));
		table.setOpaque(true);
		
		JLabel columnAdjust = new JLabel("< Adjust Column");
		columnAdjust.setBounds(340,52, 100, 20);
		getContentPane().add(columnAdjust);
		
		colorText =new JRadioButton("Color/Text");
		colorText.setBounds(25,32,85,20);
		getContentPane().add(colorText);
		colorText.addActionListener(this);
		
		textColor =new JRadioButton("Text/Color");
		textColor.setBounds(110,32,85,20);
		getContentPane().add(textColor);
		textColor.addActionListener(this);
		
		gradientColor =new JRadioButton("Gradient");
		gradientColor.setBounds(195,32,85,20);
		getContentPane().add(gradientColor);
		gradientColor.addActionListener(this);
		
	
		
		textField = new JTextField();
		textField.setBounds(25, 435, 163, 20);
		getContentPane().add(textField);
		textField.setColumns(10);
		
		btnTextColor = new JButton("Font");
		btnTextColor.setBounds(198, 433, 83, 23);
		getContentPane().add(btnTextColor);
		btnTextColor.addActionListener(this);
		
		btnColorSelect = new ColorWellButton(new Color(255,0,0),80,20);
		btnColorSelect.setBounds(198, 460, 83, 23);
		btnColorSelect.addActionListener(this);
		getContentPane().add(btnColorSelect);
		
		btnColorSelect2 = new ColorWellButton(Color.WHITE,80,20);
		btnColorSelect2.setBounds(198, 490, 83, 23);
		btnColorSelect2.addActionListener(this);
		getContentPane().add(btnColorSelect2);
		
		colorLabel = new JLabel("Select Corresponding Color:");
		colorLabel.setBounds(25,460,163,20);
		getContentPane().add(colorLabel);
		
		colorLabel2 = new JLabel("Set Background Color:");
		colorLabel2.setBounds(25,490,163,20);
		getContentPane().add(colorLabel2);
		
		bgtransparent = new JCheckBox("Remove Background");
		bgtransparent.setBounds(20, 520, 163, 20);
		getContentPane().add(bgtransparent);
		bgtransparent.addActionListener(this);
		
		JLabel rowheight = new JLabel("Adjust Height");
		rowheight.setBounds(85, 360, 133, 20);
		getContentPane().add(rowheight);
		
		JLabel rowwidth = new JLabel("Adjust Width");
		rowwidth.setBounds(85, 400, 133, 20);
		getContentPane().add(rowwidth);
		
		btnAddtoList = new JButton("Add to List");
//		btnAddtoList.setBounds(104, 545, 100, 23);
		btnAddtoList.setBounds(25, 580, 120, 23);
		getContentPane().add(btnAddtoList);
		btnAddtoList.addActionListener(this);
		
		btnRemove = new JButton(" >> Remove");
		btnRemove.setBounds(340, 160, 103, 23);
		btnRemove.addActionListener(this);
		getContentPane().add(btnRemove);
		
		moveUp	= new JButton("Move Up");
		moveUp.setBounds(340,82,100,25);
		getContentPane().add(moveUp);
		moveUp.addActionListener(this);
		
		moveDown	= new JButton("Move Down");
		moveDown.setBounds(340,110,100,25);
		getContentPane().add(moveDown);
		moveDown.addActionListener(this);
		
	    btnClear = new JButton("Clear List");
		btnClear.setBounds(340, 204, 103, 23);
		btnClear.addActionListener(this);
		getContentPane().add(btnClear);
		
		btnCreateLegend = new JButton("Create Legend");
//		btnCreateLegend.setBounds(25, 580, 120, 23);
		btnCreateLegend.setBounds(180, 580, 120, 23);
		btnCreateLegend.addActionListener(this);
		getContentPane().add(btnCreateLegend);
		
//		btnDisplayLegend = new JButton("Display Legend");
//		btnDisplayLegend.setBounds(180, 580, 120, 23);
//		btnDisplayLegend.addActionListener(this);
//		getContentPane().add(btnDisplayLegend);
		
		lblCreateLegend = new JLabel("Select Orientation:");
		lblCreateLegend.setBounds(25, 11, 120, 14);
		getContentPane().add(lblCreateLegend);
		
		textArea = new JTextArea("1) Select the orientation of your legend\n" + "\n" +
				"2) Type your text in the white text field.\n" + "\n"+
				"3) Click the Font button to change font type, size, and color.\n" + "\n"+
				"4) Click the Select Corresponding Color button to insert a color\n" +"\n"+
				"4) To change background, click the Set Background Color button.\n" +"\n"+
				"5) For a transparent background, click the remove background check box.\n" +   "\n"+ 
				"6) Click the Add to List button to add input into the legend.\n"+"\n"+
				"7) To adjust column, click between column and use mouse to drag column to preferred size.\n" + "\n" +
				"7) Click the Create legend button and save the image to a directory on your hard drive.\n"+"\n"+
				"8) Finally, click ok and then click the add image button in the Legend Plugin to import your image", 1, 1);
		
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		getContentPane().add(textArea);
		
		JScrollPane textScroll = new JScrollPane(textArea);
		textScroll.setBounds(320, 360, 149, 268);
		getContentPane().add(textScroll);
		
		lblInstructions = new JLabel("Instructions:");
		lblInstructions.setBounds(348, 340, 80, 14);
		getContentPane().add(lblInstructions);
		
		
		colorchooser = new JColorChooser();
		colorchooser.getSelectionModel().addChangeListener(this);
		colorchooser2 = new JColorChooser();
		colorchooser2.getSelectionModel().addChangeListener(this);
		
		
		up = new JButton("+");
		up.setBounds(25, 360, 45,30);
		getContentPane().add(up);
		up.addActionListener(this);
		
		down = new JButton("-");
		down.setBounds(173, 360, 45,30);
		getContentPane().add(down);
		down.addActionListener(this);
		
		left = new JButton("<");
		left.setBounds(25, 400, 45,30);
		getContentPane().add(left);
		left.addActionListener(this);
		
		right = new JButton(">");
		right.setBounds(173, 400, 45,30);
		getContentPane().add(right);
		right.addActionListener(this);
		right.addMouseListener(this);
	}


	@SuppressWarnings("null")
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == up){
			//System.out.println("up was hit");
			
			table.setRowHeight(table.getRowHeight()+1);
		}
		if (e.getSource() == down){
			if(table.getRowHeight() <= 1){
				return;
			}else{
				table.setRowHeight(table.getRowHeight()-1);
			}
		}
		if (e.getSource() == left){
			if ((scrollPane.getWidth() <=1) || (table.getWidth()<=1)){
				return;
			}
			else{
				scrollPane.setSize(scrollPane.getWidth()-1, scrollPane.getHeight());
				table.setSize(scrollPane.getWidth(), (table.getRowHeight()*table.getRowCount()));
			}
		}
		if (e.getSource() == right){
			if ((scrollPane.getWidth() >= 315) ||(table.getWidth()>= 315)){
				return;
			}
			else{
				scrollPane.setSize(scrollPane.getWidth()+1, scrollPane.getHeight());
				table.setSize(scrollPane.getWidth(), (table.getRowHeight()*table.getRowCount()));
			}
		}
		// TODO Auto-generated method stub
		if (e.getSource() == btnClear){
			if(bgradientcolor){
				int k=table.getRowCount();
				for (int i=k; i>0; i--){
					model.removeRow(0);
				}
			}else{
				int k=table.getRowCount();
				for (int i=k; i>0; i--){
					tableModel.removeRow(0);
				}
			}
		}
		if (e.getSource()== okButton){
			this.dispose();
//			legendmodel.addTextLegend();
		}
		if (e.getSource()== cancelButton){
			this.dispose();
		}
		if (e.getSource()== btnRemove){
			   DefaultTableModel model = (DefaultTableModel) this.table.getModel();
			   int[] rows = table.getSelectedRows();
			   for(int i=0;i<rows.length;i++){
			     model.removeRow(rows[i]-i);

			   }
		}
		
		if (e.getSource() == btnTextColor){
		   
	           fontGUI.setLocation(this.getLocation());
	           fontGUI.setVisible(true);
	          
	           if(fontGUI.isVisible()==false){
	        	   
	                  textField.setFont(fontGUI.getFont());
	                  if ((fontGUI.getColor()==Color.WHITE)&&(textField.getBackground().equals(new Color(255,255,255)))){
	                	  textField.setForeground(Color.BLACK);
	                  }
	                	  else{
	                  textField.setForeground(fontGUI.getColor());
	                	  }
	                  bcolor =true;
	                 
	                  }
		}
	           
		
		

		
		if (e.getSource() == btnAddtoList){
		
			if ((bcolortext==false)&&(btextcolor==false)&&(bgradientcolor==false)){
				JLabel error = new JLabel("Please select an orientation at the top");
				JOptionPane.showMessageDialog(null,error,"ERROR",  JOptionPane.ERROR_MESSAGE);
				return;
			}
		    field = new JLabel(textField.getText());
		    
		    if(bcolor){
		    	field.setFont(fontGUI.getFont());
		    	field.setForeground(fontGUI.getColor());
		    }
			if (bcolortext){
				Object textcolor[] = {btnColorSelect.getColor1(),field};
				
			
				scrollPane.setViewportView(table);
				tableModel.addRow(textcolor);
			}
			else if (btextcolor){
				Object textcolor[] = {field,btnColorSelect.getColor1()};
				scrollPane.setViewportView(table);
				tableModel.addRow(textcolor);
			}
			else if (bgradientcolor){
				
		        JLabel frame = new JLabel();
		        gradient gradientExample = new gradient();
		        gradientExample.setSize(scrollPane.getWidth()*3, table.getRowHeight()*3);
		        frame.add(gradientExample);
		
				BufferedImage image;
				Graphics2D graphic2d;
				image = new BufferedImage(scrollPane.getWidth()*3,table.getRowHeight()*3, BufferedImage.TYPE_INT_RGB);
				graphic2d = image.createGraphics();
				frame.setSize(new Dimension(scrollPane.getWidth()*3, table.getRowHeight()*3));
				frame.paint(graphic2d);
				
				ImageIcon icon = new ImageIcon(image);
			
				JLabel imageIcon = new JLabel(icon);
	
				Object[] row = {imageIcon};
				
				scrollPane.setViewportView(table);
				
				model.addRow(row);
			}
		}
		if (e.getSource()== btnColorSelect2){
			final JDialog colorDialog = new JDialog();
			JButton okColorbtn = new JButton("Apply");
			JPanel okBtnPanel = new JPanel();
			okBtnPanel.setLayout(new GridLayout(1,1));
			JPanel colorChsrPanel = new JPanel();
			colorChsrPanel.setLayout(new GridLayout(0,1));
			JPanel container = new JPanel();
			
			
			container.add(colorChsrPanel);
			container.add(okBtnPanel);

			colorChsrPanel.add(colorchooser2);
			colorChsrPanel.add(colorchooser2);
			
			okBtnPanel.add(okColorbtn);
			
			//colorDialog.add(colorChsrPanel);
			//colorDialog.add(okBtnPanel);
			colorDialog.add(container);
			
			colorDialog.setSize(new Dimension(450,420));
			colorDialog.setLocation(this.getLocation());
			
			colorDialog.setResizable(false);
			colorDialog.setVisible(true);	
		
			
			okColorbtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					table.setBackground(btnColorSelect2.getColor1());	
					tableModel.fireTableDataChanged();
					textField.setBackground(btnColorSelect2.getColor1());
					colorDialog.dispose();
				}});
			
			colorchooser.setVisible(true);
		}
		
		if (e.getSource() == btnColorSelect){
			
			if (bgradientcolor){
				if (this.gradientChooser == null) {
					this.gradientChooser = new GradientColorChooser(this);
				}
				Color[] newColor = this.gradientChooser.getColors(
						this.btnColorSelect.getColor1(),
						this.btnColorSelect.getColor2());
				if (newColor != null) {
					this.btnColorSelect.setColor(newColor[0], newColor[1]);
					if (newColor[0].equals(newColor[1])) {
						
					}
					return;
				}
			}

			final JDialog colorDialog = new JDialog();
			JButton okColorbtn = new JButton("Apply");
			JPanel okBtnPanel = new JPanel();
			okBtnPanel.setLayout(new GridLayout(1,1));
			JPanel colorChsrPanel = new JPanel();
			colorChsrPanel.setLayout(new GridLayout(0,1));
			JPanel container = new JPanel();
			
			
			container.add(colorChsrPanel);
			container.add(okBtnPanel);

			colorChsrPanel.add(colorchooser);
			colorChsrPanel.add(colorchooser);
			
			okBtnPanel.add(okColorbtn);
			
			//colorDialog.add(colorChsrPanel);
			//colorDialog.add(okBtnPanel);
			colorDialog.add(container);
			
			colorDialog.setSize(new Dimension(450,420));
			colorDialog.setLocation(this.getLocation());
			
			colorDialog.setResizable(false);
			colorDialog.setVisible(true);	
		
			
			okColorbtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					colorDialog.dispose();
				}});
				
			colorchooser.setVisible(true);
		
		}
		if (e.getSource()== btnCreateLegend){
			String filename;
			int returnValue = saveFile.showSaveDialog(this);
			if (returnValue==JFileChooser.APPROVE_OPTION) {
				bobby = saveFile.getSelectedFile();
				//ralph = saveFile.getCurrentDirectory();
				  //String filepath = ralph.toString();
				  if (bobby.toString().contains("."))
						  filename =bobby.toString();
				  else
				  filename = bobby.toString() + ".JPG";
				  //table.setSize(table.getWidth(), table.getHeight());
				  
				   myImage = new BufferedImage(table.getWidth(),table.getHeight(), BufferedImage.SCALE_SMOOTH);
		           g2 = myImage.createGraphics();
		           //table.setSize(table.getSize());
		           table.paint(g2);
	           
	               // retrieve image
	              
	               outputfile = new File(filename);
	               try {
					ImageIO.write(myImage, "jpg", outputfile);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		if (e.getSource()==btnDisplayLegend){
 
//		   try
//		   {
//			   	if (outputfile == null)
//				   return;
//				legendmodel.importImageLegend(outputfile);
//				if (outputfile == null)
//				return;
//				legendGUIModel.addElement(outputfile.getName());
//				
//				if (bFirstLoad)
//				{
//					hudLegendPanel = new MiniHUDLegendPanel(legendgui);
//					hudLegendFrame = new HUDLegendFrame(legendgui,hudLegendPanel);
//					hudLegendFrame.setVisible(true);
//					bFirstLoad = false;
//				}
//				if(!hudLegendFrame.isVisible())
//				{
//					hudLegendFrame.setVisible(true);
//				}
//				if(!hudLegendFrame.isShowing())
//				{
//					hudLegendFrame.toFront();
//				}
//				
//				legendGUIList.setSelectedIndex(legendGUIModel.getSize()-1);
//				if (legendGUIList.getSelectedIndex() != -1)
//				{
////	       					hudLegendPanel.setLegend(legendmodel.getHUD().get(
////	       						legendGUIList.getSelectedIndex()));
////
////	       					displaybutton.setText(legendmodel.getHUD().get(
////	       						legendGUIList.getSelectedIndex()).isVisible() ?" Hide ":"Display");
////
////	       					float trans = legendmodel.getHUD().get(legendGUIList.getSelectedIndex()).getTransparency();
////	       					transparencyslider.setValue((int)(trans*100));
////	       					scalefield.setText("" + legendmodel.getHUD().get(legendGUIList.getSelectedIndex()).getScaleMultiplier());
//				}
//			}
//			catch(IOException ex)
//			{
//				JOptionPane.showMessageDialog(null, "Unable to load selected file.");
//			}
//			catch(NullPointerException ex)
//			{
//				JOptionPane.showMessageDialog(null, "Unable to load corrupted image file.");
//			}
		}
	               
	               

		
		if (e.getSource() == moveUp){
			
			
			if((!table.isShowing())||(table.getRowCount()==0)){
				return;
			}
			if (table.getSelectedRow()==0){
				return;
			}
			
			DefaultTableModel model2 = (DefaultTableModel) this.table.getModel();
			model2.moveRow(table.getSelectedRow(),table.getSelectedRow(),table.getSelectedRow() -1);
			table.setRowSelectionInterval(table.getSelectedRow()-1, table.getSelectedRow()-1);
			
			
		}
		if (e.getSource() == moveDown){
			if(!table.isShowing()){
				return;
			}
			if (table.getSelectedRow()==table.getRowCount()-1){
				return;
			}
			DefaultTableModel model2 = (DefaultTableModel) this.table.getModel();
			model2.moveRow(table.getSelectedRow(),table.getSelectedRow(),table.getSelectedRow() +1);
			table.setRowSelectionInterval(table.getSelectedRow()+1, table.getSelectedRow()+1);
			
		}
		if (e.getSource() == textColor){
			if (btextcolor){
				return;
			}
			scrollPane.setViewportView(table);
			
			if ((bcolortext)||(bgradientcolor)){
				color = new JDialog();
				color.setSize(600,100);
				color.setLocation(this.getLocation());
				JPanel lblPanel = new JPanel();
				JPanel mainPanel = new JPanel();
				JLabel label = new JLabel("In order to change orientation, you must clear legend.\n Would you like to clear now?");
				lblPanel.add(label);
				JButton yes = new JButton("Yes");
				color.add(lblPanel);
				
				yes.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (bgradientcolor){
							int k=table.getRowCount();
							for (int i=k; i>0; i--){
								model.removeRow(0);
						}
						}else{
						int k=table.getRowCount();
						for (int i=k; i>0; i--){
							tableModel.removeRow(0);
						}
						}
							color.dispose();
							colorText.setSelected(false);
							gradientColor.setSelected(false);
							scrollPane.setViewport(null);
							
							if (bgradientcolor){
								
								table = new JTable(tableModel);
								table.setShowGrid(false);
								table.setIntercellSpacing(new Dimension(0,2));
								table.repaint();
								scrollPane.setViewportView(table);
								table.getColumnModel().getColumn(0).setHeaderValue("Text");
								table.getColumnModel().getColumn(1).setHeaderValue("Color");
							}
//							if ((bgradientcolor)&&(bcolortext == false)){
//								
//							
//								tableModel.addColumn("Text");
//								tableModel.addColumn("Color");
//							}
							if  (bcolortext){
							table.getColumnModel().getColumn(0).setHeaderValue("Text");
							table.getColumnModel().getColumn(1).setHeaderValue("Color");
							}
							tableModel.fireTableDataChanged();
							table.getColumnModel().getColumn(1).setCellRenderer(new ColorTableCellRenderer());
							table.getColumnModel().getColumn(0).setCellRenderer(new TextColorTableCellRenderer());
							
							tableModel.fireTableDataChanged();

					
							btextcolor = true;
							bcolortext = false;
							bgradientcolor = false;
							return;
						
					}});
				JButton no = new JButton("No");
				no.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						color.dispose();
						textColor.setSelected(false);
						return;
					}});
				JPanel buttonPanel = new JPanel();
				buttonPanel.add(yes);
				buttonPanel.add(no);
				mainPanel.add(lblPanel);
				mainPanel.add(buttonPanel);
				color.add(mainPanel);
				
				
				//color.add(buttonPanel);

				
				color.setVisible(true);
				btextcolor = true;
				return;
			}
			colorText.setSelected(false);
			gradientColor.setSelected(false);
			bcolortext = false;
			bgradientcolor = false;
			tableModel.addColumn("Text");
			tableModel.addColumn("Color");
			table.getColumnModel().getColumn(1).setCellRenderer(new ColorTableCellRenderer());
			table.getColumnModel().getColumn(0).setCellRenderer(new TextColorTableCellRenderer());
			
			btextcolor = true;
		}
		if (e.getSource() == colorText){
			if (bcolortext){
				return;
			}
			scrollPane.setViewportView(table);
			//((CardLayout)cards.getLayout()).show(cards, "Rows/Columns");
			if ((btextcolor)||(bgradientcolor)){
				color = new JDialog();
				color.setSize(600,100);
				color.setLocation(this.getLocation());
				JPanel mainPanel = new JPanel();
				JPanel lblPanel = new JPanel();
				JLabel label = new JLabel("In order to change orientation, you must clear legend.\n Would you like to clear now?");
				lblPanel.add(label);
				JButton yes = new JButton("Yes");
				yes.addActionListener(new ActionListener() {					
					public void actionPerformed(ActionEvent e) {
						if (bgradientcolor){
							int k=table.getRowCount();
							for (int i=k; i>0; i--){
								model.removeRow(0);
						}
						}else{
						int k=table.getRowCount();
						for (int i=k; i>0; i--){
							tableModel.removeRow(0);
						}
						}
							color.dispose();
							//colorText.setSelected(true);
							textColor.setSelected(false);
							gradientColor.setSelected(false);
							scrollPane.setViewport(null);
						
							if (bgradientcolor){
								
								table = new JTable(tableModel);
								table.setShowGrid(false);
								table.setIntercellSpacing(new Dimension(0,2));
								scrollPane.setViewportView(table);
								table.getColumnModel().getColumn(0).setHeaderValue("Color");
								table.getColumnModel().getColumn(1).setHeaderValue("Text");
								table.repaint();
							}
//							if ((bgradientcolor)&&(btextcolor== false)){
//								
//							
//								tableModel.addColumn("Color");
//								tableModel.addColumn("Text");
//							}
							if  (btextcolor){
							table.getColumnModel().getColumn(0).setHeaderValue("Color");
							table.getColumnModel().getColumn(1).setHeaderValue("Text");
							}
							
							tableModel.fireTableDataChanged();
							table.getColumnModel().getColumn(0).setCellRenderer(new ColorTableCellRenderer());
							table.getColumnModel().getColumn(1).setCellRenderer(new TextColorTableCellRenderer());
							btextcolor = false;
							bgradientcolor = false;
							bcolortext = true;
							return;
						
					}});
				JButton no = new JButton("No");
				no.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						color.dispose();
						colorText.setSelected(false);
						return;
					}});
				JPanel buttonPanel = new JPanel();
				buttonPanel.add(yes);
				buttonPanel.add(no);
				mainPanel.add(lblPanel);
				mainPanel.add(buttonPanel);
				color.add(mainPanel);

				
				color.setVisible(true);
				return;
			}
			
			textColor.setSelected(false);
			btextcolor = false;
			bgradientcolor = false;
			gradientColor.setSelected(false);
			
			tableModel.addColumn("Color");
			tableModel.addColumn("Text");
			
			
			table.getColumnModel().getColumn(0).setCellRenderer(new ColorTableCellRenderer());
			table.getColumnModel().getColumn(1).setCellRenderer(new TextColorTableCellRenderer());
			bcolortext = true;
		}
		if (e.getSource()==gradientColor){
			if (bgradientcolor){
				gradientColor.setSelected(true);
				return;
			}
			if ((btextcolor)||(bcolortext)){
				color = new JDialog();
				color.setSize(600,100);
				color.setLocation(this.getLocation());
				JPanel mainPanel = new JPanel();
				JPanel lblPanel = new JPanel();
				JLabel label = new JLabel("In order to change orientation, you must clear legend.\n Would you like to clear now?");
				lblPanel.add(label);
				JButton yes = new JButton("Yes");
				yes.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int k=table.getRowCount();
						for (int i=k; i>0; i--){
							tableModel.removeRow(0);
						}
							color.dispose();
							//colorText.setSelected(true);
							textColor.setSelected(false);
							colorText.setSelected(false);
							scrollPane.setViewport(null);
							//scrollPane.setViewportView(table);
							model = new DefaultTableModel();
							table = new JTable(model){
								private static final long serialVersionUID = 1L;
								public boolean isCellEditable(int rowIndex, int vColIndex) {
							        return false;}
							    };
							scrollPane.setViewportView(table);
							model.addColumn("Gradient");
							table.repaint();
							model.fireTableDataChanged();
							table.getColumnModel().getColumn(0).setCellRenderer(new GradientColorTableCellRenderer());
							btextcolor = false;
							bcolortext = false;
							bgradientcolor = true;
							return;
						
					}});
				JButton no = new JButton("No");
				no.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						color.dispose();
						colorText.setSelected(false);
						return;
					}});
				JPanel buttonPanel = new JPanel();
				buttonPanel.add(yes);
				buttonPanel.add(no);
				mainPanel.add(lblPanel);
				mainPanel.add(buttonPanel);
				color.add(mainPanel);

				
				color.setVisible(true);
				return;
			}
			//DefaultTableModel model = new DefaultTableModel();
			model = new DefaultTableModel();
			table = new JTable(model){
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int rowIndex, int vColIndex) {
			        return false;}
			    };
			
			model.addColumn("Gradient");
			model.fireTableDataChanged();
			table.getColumnModel().getColumn(0).setCellRenderer(new GradientColorTableCellRenderer());
			scrollPane.setViewportView(table);
			gradientColor.setSelected(true);
			textColor.setSelected(false);
			colorText.setSelected(false);
			btextcolor = false;
			bcolortext = false;
			bgradientcolor = true;
		}
		if (e.getSource()== bgtransparent){
			if (btransparent){
				table.setBackground(btnColorSelect2.getColor1());				
				btransparent = false;
				table.setOpaque(true);
				
				tableModel.fireTableDataChanged();
				
				
			}else{

					btransparent = true;
					table.setOpaque(false);
					tableModel.fireTableDataChanged();

		}
		
	}
	}
	

	public class ColorTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
	

	    	this.setForeground((Color)value);
	    	this.setBackground((Color)value);
			//this.setIcon(((ColorWellButton)value).getIcon());


	        return this;
	    }
	}
	public class TextColorTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

		private static final long serialVersionUID = 1L;
	
		public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value,
		             isSelected,  hasFocus,  row,  column);

	    	this.setForeground(((JLabel)value).getForeground());
	    	this.setFont(((JLabel)value).getFont());
	    	this.setText(((JLabel)value).getText());
	    	if (btransparent){
	    		//this.setBackground(new Color(0,0,0));;
	    		this.setOpaque(false);
	    	}
	    	else if (btransparent==false){
	    		this.setOpaque(true);
	    		this.setBackground(btnColorSelect2.getColor1());
	    	}
	    	
	    
	    
			
	    	return this;      
		}
	}
	
	public class GradientColorTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

		private static final long serialVersionUID = 1L;
	
		public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value,
		             isSelected,  hasFocus,  row,  column);

//	    	this.setForeground(((JLabel)value).getForeground());
//	    	this.setFont(((JLabel)value).getFont());
//	    	this.setText(((JLabel)value).getText());
			//this.setBackground(((JLabel)value).getBackground());
			this.setIcon(((JLabel)value).getIcon()); 
	    	return this;      
		}
	}
	public class gradient extends JPanel {

	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void paint(Graphics graphics) {
	       
	        Graphics2D g2d = (Graphics2D) graphics;
	        //GradientPaint gradients;
	        g2d.setPaint(new GradientPaint(0, 0, btnColorSelect.getColor1(), 100, 100, btnColorSelect.getColor2(), false));
	        Rectangle2D.Double rectangle = new Rectangle2D.Double(0,0,scrollPane.getWidth()*3,(table.getRowHeight()*3));
	        g2d.fill(rectangle);
	    }
	    }
	    
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource()== colorchooser2.getSelectionModel()){
			btnColor2 = colorchooser2.getColor();
			btnColorSelect2.setColor(btnColor2);
			return;
			}
		if (e.getSource()==colorchooser.getSelectionModel()){
		btnColor = colorchooser.getColor();
		btnColorSelect.setColor(btnColor);
		return;
		}
	
		
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
//		while (e.getSource() == right){
//			if ((scrollPane.getWidth() >= 315) ||(table.getWidth()>= 315)){
//				return;
//			}
//			else{
//			scrollPane.setSize(scrollPane.getWidth()+1, scrollPane.getHeight());
//			table.setSize(scrollPane.getWidth(), (table.getRowHeight()*table.getRowCount()));
//			}
//		}
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
}
	



