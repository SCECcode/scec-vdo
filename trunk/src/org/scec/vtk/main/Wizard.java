package org.scec.vtk.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.scec.vtk.main.MainMenu;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

@SuppressWarnings("serial")
public class Wizard extends JPanel {

   private static final String TITLE_TEXT = "Welcome to SCEC-VDO";
   private static final int TITLE_POINTS = 22;
   public Wizard(final MainMenu mainMenu, final MainGUI mainGUI) {
	   JPanel mainPanel = new JPanel();
	   mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//	   mainPanel.setSize(370, 130);

	  JLabel titleLabel = new JLabel(TITLE_TEXT);
	   titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD,
	          TITLE_POINTS));

	   FlowLayout layout = new FlowLayout();
	   layout.setAlignment(FlowLayout.CENTER);
	   
	   JPanel southBtnPanel = new JPanel(layout);
	  southBtnPanel.setLayout(new GridLayout(2,1,10,10));
	   
	   
	   //Open an existing file using function from mainMenu
	   JButton openButton = new JButton("Open Existing Project");
	   openButton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		          mainMenu.open();
		    	  //close frame after selection has been made
		          mainGUI.wizFrame.setVisible(false);
		          
		        }
		    });

	   //Create (save) a file using function from mainMenu
	   JButton createButton = new JButton("Create a New Project");
	   createButton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		    	  mainMenu.save();
		    	  //close frame after selection has been made
		          mainGUI.wizFrame.setVisible(false);
		      }
		    });
	   
	   JCheckBox checkBox = new JCheckBox("Do not show me this again");
	   checkBox.setLayout(new FlowLayout());
	   
	   checkBox.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
//		    	  mainMenu.save();
//		    	  close frame after selection has been made
		          mainGUI.wizFrame.setVisible(false);
	
		      }
		    });
	   
	   
	   southBtnPanel.add(openButton);
	   southBtnPanel.add(createButton);
	   southBtnPanel.add(checkBox, FlowLayout.RIGHT);
	   
	   
      JPanel titlePanel = new JPanel();
      titlePanel.add(titleLabel);
      mainPanel.add(titlePanel);
      mainPanel.add(southBtnPanel);
      add(mainPanel);
   }
}