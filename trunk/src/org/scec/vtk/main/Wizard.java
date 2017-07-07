package org.scec.vtk.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.scec.vtk.main.MainMenu;

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.Font;

@SuppressWarnings("serial")
public class Wizard extends JPanel {

   private static final String TITLE_TEXT = "Welcome to SCEC-VDO";
   private static final int TITLE_POINTS = 22;
   public Wizard(MainMenu mainMenu, MainGUI mainGUI) {
	   JPanel mainPanel = new JPanel();
	   mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

	  JLabel titleLabel = new JLabel(TITLE_TEXT);
	   titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD,
	          TITLE_POINTS));

	   FlowLayout layout = new FlowLayout();
	   layout.setAlignment(FlowLayout.CENTER);
	   JPanel southBtnPanel = new JPanel(layout);
	   
	   //Open an existing file using function from mainMenu
	   JButton openButton = new JButton("Open a project");
	   openButton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		          mainMenu.open();
		    	  //close frame after selection has been made
		          mainGUI.wizFrame.setVisible(false);
		          
		        }
		    });

	   //Create (save) a file using function from mainMenu
	   JButton createButton = new JButton("Create a project");
	   createButton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		    	  mainMenu.save();
		    	  //close frame after selection has been made
		          mainGUI.wizFrame.setVisible(false);
		      }
		    });
	   
	   southBtnPanel.add(openButton);
	   southBtnPanel.add(createButton);
	        
      JPanel titlePanel = new JPanel();
      titlePanel.add(titleLabel);
      mainPanel.add(titlePanel);
      mainPanel.add(southBtnPanel);
      add(mainPanel);
   }
}